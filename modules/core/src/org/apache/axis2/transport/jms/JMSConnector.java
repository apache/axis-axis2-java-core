/*
* Copyright 2001, 2002,2004 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package org.apache.axis2.transport.jms;

import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * JMSConnector is an abstract class that encapsulates the work of connecting
 * to JMS destinations. Its subclasses are TopicConnector and QueueConnector
 * which further specialize connections to the pub-sub and the ptp domains.
 * It also implements the capability to retry connections in the event of
 * failures.
 */
public abstract class JMSConnector {
    protected JMSVendorAdapter m_adapter;
    protected boolean m_allowReceive;
    protected long m_connectRetryInterval;
    protected long m_interactRetryInterval;
    protected JMSURLHelper m_jmsurl;
    protected int m_numRetries;
    protected int m_numSessions;
    protected long m_poolTimeout;
    protected AsyncConnection m_receiveConnection;
    protected SyncConnection m_sendConnection;
    protected long m_timeoutTime;

    public JMSConnector(ConnectionFactory connectionFactory, int numRetries, int numSessions,
                        long connectRetryInterval, long interactRetryInterval, long timeoutTime,
                        boolean allowReceive, String clientID, String username, String password,
                        JMSVendorAdapter adapter, JMSURLHelper jmsurl)
            throws JMSException {
        m_numRetries = numRetries;
        m_connectRetryInterval = connectRetryInterval;
        m_interactRetryInterval = interactRetryInterval;
        m_timeoutTime = timeoutTime;
        m_poolTimeout = timeoutTime / (long) numRetries;
        m_numSessions = numSessions;
        m_allowReceive = allowReceive;
        m_adapter = adapter;
        m_jmsurl = jmsurl;

        // try to connect initially so we can fail fast
        // in the case of irrecoverable errors.
        // If we fail in a recoverable fashion we will retry
        javax.jms.Connection sendConnection = createConnectionWithRetry(connectionFactory,
                username, password);

        m_sendConnection = createSyncConnection(connectionFactory, sendConnection, m_numSessions,
                "SendThread", clientID, username, password);
        m_sendConnection.start();

        if (m_allowReceive) {
            javax.jms.Connection receiveConnection = createConnectionWithRetry(connectionFactory,
                    username, password);

            m_receiveConnection = createAsyncConnection(connectionFactory, receiveConnection,
                    "ReceiveThread", clientID, username, password);
            m_receiveConnection.start();
        }
    }

    protected abstract AsyncConnection createAsyncConnection(ConnectionFactory factory,
                                                             javax.jms.Connection connection, String threadName, String clientID, String username,
                                                             String password)
            throws JMSException;

    protected javax.jms.Connection createConnectionWithRetry(ConnectionFactory connectionFactory,
                                                             String username, String password)
            throws JMSException {
        javax.jms.Connection connection = null;

        for (int numTries = 1; connection == null; numTries++) {
            try {
                connection = internalConnect(connectionFactory, username, password);
            } catch (JMSException jmse) {
                if (!m_adapter.isRecoverable(jmse, JMSVendorAdapter.CONNECT_ACTION)
                        || (numTries == m_numRetries)) {
                    throw jmse;
                } else {
                    try {
                        Thread.sleep(m_connectRetryInterval);
                    } catch (InterruptedException ie) {
                    }
                }
                ;
            }
        }

        return connection;
    }

    public abstract JMSEndpoint createEndpoint(Destination destination) throws JMSException;

    public abstract JMSEndpoint createEndpoint(String destinationName) throws JMSException;

    protected abstract SyncConnection createSyncConnection(ConnectionFactory factory,
                                                           javax.jms.Connection connection, int numSessions, String threadName, String clientID,
                                                           String username, String password)
            throws JMSException;

    protected abstract javax.jms.Connection internalConnect(ConnectionFactory connectionFactory,
                                                            String username, String password)
            throws JMSException;

    public int numSessions() {
        return m_numSessions;
    }

    public void shutdown() {
        m_sendConnection.shutdown();

        if (m_allowReceive) {
            m_receiveConnection.shutdown();
        }
    }

    public void start() {
        m_sendConnection.startConnection();

        if (m_allowReceive) {
            m_receiveConnection.startConnection();
        }

        JMSConnectorManager.getInstance().addConnectorToPool(this);
    }

    public void stop() {
        JMSConnectorManager.getInstance().removeConnectorFromPool(this);
        m_sendConnection.stopConnection();

        if (m_allowReceive) {
            m_receiveConnection.stopConnection();
        }
    }

    public String getClientID() {
        return getSendConnection().getClientID();
    }

    public ConnectionFactory getConnectionFactory() {

        // there is always a send connection
        return getSendConnection().getConnectionFactory();
    }

    public JMSURLHelper getJMSURL() {
        return m_jmsurl;
    }

    public int getNumRetries() {
        return m_numRetries;
    }

    public String getPassword() {
        return getSendConnection().getPassword();
    }

    AsyncConnection getReceiveConnection() {
        return m_receiveConnection;
    }

    SyncConnection getSendConnection() {
        return m_sendConnection;
    }

    public String getUsername() {
        return getSendConnection().getUsername();
    }

    public JMSVendorAdapter getVendorAdapter() {
        return m_adapter;
    }

    protected abstract class AsyncConnection extends Connection {
        Object m_subscriptionLock;
        HashMap m_subscriptions;

        protected AsyncConnection(ConnectionFactory connectionFactory,
                                  javax.jms.Connection connection, String threadName,
                                  String clientID, String username, String password)
                throws JMSException {
            super(connectionFactory, connection, threadName, clientID, username, password);
            m_subscriptions = new HashMap();
            m_subscriptionLock = new Object();
        }

        protected abstract ListenerSession createListenerSession(javax.jms.Connection connection,
                                                                 Subscription subscription)
                throws Exception;

        protected void onConnect() throws Exception {
            synchronized (m_subscriptionLock) {
                Iterator subscriptions = m_subscriptions.keySet().iterator();

                while (subscriptions.hasNext()) {
                    Subscription subscription = (Subscription) subscriptions.next();

                    if (m_subscriptions.get(subscription) == null) {
                        m_subscriptions.put(subscription,
                                createListenerSession(m_connection, subscription));
                    }
                }

                m_subscriptionLock.notifyAll();
            }
        }

        protected void onException() {
            synchronized (m_subscriptionLock) {
                Iterator subscriptions = m_subscriptions.keySet().iterator();

                while (subscriptions.hasNext()) {
                    Subscription subscription = (Subscription) subscriptions.next();

                    m_subscriptions.put(subscription, null);
                }
            }
        }

        protected void onShutdown() {
            synchronized (m_subscriptionLock) {
                Iterator subscriptions = m_subscriptions.keySet().iterator();

                while (subscriptions.hasNext()) {
                    Subscription subscription = (Subscription) subscriptions.next();
                    ListenerSession session =
                            (ListenerSession) m_subscriptions.get(subscription);

                    if (session != null) {
                        session.cleanup();
                    }
                }

                m_subscriptions.clear();
            }
        }

        /**
         * @param subscription
         * @todo add in security exception propagation
         */
        void subscribe(Subscription subscription) throws Exception {
            long timeoutTime = System.currentTimeMillis() + m_timeoutTime;

            synchronized (m_subscriptionLock) {
                if (m_subscriptions.containsKey(subscription)) {
                    return;
                }

                while (true) {
                    if (System.currentTimeMillis() > timeoutTime) {
                        throw new InvokeTimeoutException("Cannot subscribe listener");
                    }

                    try {
                        ListenerSession session = createListenerSession(m_connection, subscription);

                        m_subscriptions.put(subscription, session);

                        break;
                    } catch (JMSException jmse) {
                        if (!m_adapter.isRecoverable(jmse, JMSVendorAdapter.SUBSCRIBE_ACTION)) {
                            throw jmse;
                        }

                        try {
                            m_subscriptionLock.wait(m_interactRetryInterval);
                        } catch (InterruptedException ignore) {
                        }

                        // give reconnect a chance
                        Thread.yield();

                        continue;
                    } catch (NullPointerException jmse) {

                        // we ARE reconnecting
                        try {
                            m_subscriptionLock.wait(m_interactRetryInterval);
                        } catch (InterruptedException ignore) {
                        }

                        // give reconnect a chance
                        Thread.yield();

                        continue;
                    }
                }
            }
        }

        void unsubscribe(Subscription subscription) {
            long timeoutTime = System.currentTimeMillis() + m_timeoutTime;

            synchronized (m_subscriptionLock) {
                if (!m_subscriptions.containsKey(subscription)) {
                    return;
                }

                while (true) {
                    if (System.currentTimeMillis() > timeoutTime) {
                        throw new InvokeTimeoutException("Cannot unsubscribe listener");
                    }

                    // give reconnect a chance
                    Thread.yield();

                    try {
                        ListenerSession session =
                                (ListenerSession) m_subscriptions.get(subscription);

                        session.cleanup();
                        m_subscriptions.remove(subscription);

                        break;
                    } catch (NullPointerException jmse) {

                        // we are reconnecting
                        try {
                            m_subscriptionLock.wait(m_interactRetryInterval);
                        } catch (InterruptedException ignore) {
                        }

                        continue;
                    }
                }
            }
        }

        protected class ListenerSession extends ConnectorSession {
            protected MessageConsumer m_consumer;
            protected Subscription m_subscription;

            ListenerSession(Session session, MessageConsumer consumer, Subscription subscription)
                    throws Exception {
                super(session);
                m_subscription = subscription;
                m_consumer = consumer;

                Destination destination = subscription.m_endpoint.getDestination(m_session);

                m_consumer.setMessageListener(subscription.m_listener);
            }

            void cleanup() {
                try {
                    m_consumer.close();
                } catch (Exception ignore) {
                }

                try {
                    m_session.close();
                } catch (Exception ignore) {
                }
            }
        }
    }


    private abstract class Connection extends Thread implements ExceptionListener {
        private String m_clientID;
        protected javax.jms.Connection m_connection;
        private ConnectionFactory m_connectionFactory;
        protected boolean m_isActive;
        private Object m_jmsLock;
        private Object m_lifecycleLock;
        private boolean m_needsToConnect;
        private String m_password;
        private boolean m_startConnection;
        private String m_username;

        protected Connection(ConnectionFactory connectionFactory, javax.jms.Connection connection,
                             String threadName, String clientID, String username, String password)
                throws JMSException {
            super(threadName);
            m_connectionFactory = connectionFactory;
            m_clientID = clientID;
            m_username = username;
            m_password = password;
            m_jmsLock = new Object();
            m_lifecycleLock = new Object();

            if (connection != null) {
                m_needsToConnect = false;
                m_connection = connection;
                m_connection.setExceptionListener(this);

                if (m_clientID != null) {
                    m_connection.setClientID(m_clientID);
                }
            } else {
                m_needsToConnect = true;
            }

            m_isActive = true;
        }

        private void internalOnConnect() throws Exception {
            onConnect();

            synchronized (m_lifecycleLock) {
                if (m_startConnection) {
                    try {
                        m_connection.start();
                    } catch (Throwable e) {
                    }    // ignore
                }
            }
        }

        private void internalOnShutdown() {
            stopConnection();
            onShutdown();

            try {
                m_connection.close();
            } catch (Throwable e) {
            }    // ignore
        }

        protected abstract void onConnect() throws Exception;

        protected abstract void onException();

        public void onException(JMSException exception) {
            if (m_adapter.isRecoverable(exception, JMSVendorAdapter.ON_EXCEPTION_ACTION)) {
                return;
            }

            onException();

            synchronized (m_jmsLock) {
                m_jmsLock.notifyAll();
            }
        }

        protected abstract void onShutdown();

        /**
         * @todo handle non-recoverable errors
         */
        public void run() {

            // loop until a connection is made and when a connection is made (re)establish
            // any subscriptions
            while (m_isActive) {
                if (m_needsToConnect) {
                    m_connection = null;

                    try {
                        m_connection = internalConnect(m_connectionFactory, m_username, m_password);
                        m_connection.setExceptionListener(this);

                        if (m_clientID != null) {
                            m_connection.setClientID(m_clientID);
                        }
                    } catch (JMSException e) {

                        // simply backoff for a while and then retry
                        try {
                            Thread.sleep(m_connectRetryInterval);
                        } catch (InterruptedException ie) {
                        }

                        continue;
                    }
                } else {
                    m_needsToConnect = true;    // When we'll get to the "if (needsToConnect)" statement the next time it will be because
                }

                // we lost the connection

                // we now have a valid connection so establish some context
                try {
                    internalOnConnect();
                } catch (Exception e) {

                    // insert code to handle non recoverable errors
                    // simply retry
                    continue;
                }

                synchronized (m_jmsLock) {
                    try {
                        m_jmsLock.wait();
                    } catch (InterruptedException ie) {
                    }    // until notified due to some change in status
                }
            }

            // no longer staying connected, so see what we can cleanup
            internalOnShutdown();
        }

        void shutdown() {
            m_isActive = false;

            synchronized (m_jmsLock) {
                m_jmsLock.notifyAll();
            }
        }

        void startConnection() {
            synchronized (m_lifecycleLock) {
                if (m_startConnection) {
                    return;
                }

                m_startConnection = true;

                try {
                    m_connection.start();
                } catch (Throwable e) {
                }    // ignore
            }
        }

        void stopConnection() {
            synchronized (m_lifecycleLock) {
                if (!m_startConnection) {
                    return;
                }

                m_startConnection = false;

                try {
                    m_connection.stop();
                } catch (Throwable e) {
                }    // ignore
            }
        }

        public String getClientID() {
            return m_clientID;
        }

        public ConnectionFactory getConnectionFactory() {
            return m_connectionFactory;
        }

        public String getPassword() {
            return m_password;
        }

        public String getUsername() {
            return m_username;
        }
    }


    private abstract class ConnectorSession {
        Session m_session;

        ConnectorSession(Session session) throws JMSException {
            m_session = session;
        }
    }


    protected abstract class SyncConnection extends Connection {
        int m_numSessions;
        Object m_senderLock;
        LinkedList m_senders;

        SyncConnection(ConnectionFactory connectionFactory, javax.jms.Connection connection,
                       int numSessions, String threadName, String clientID, String username,
                       String password)
                throws JMSException {
            super(connectionFactory, connection, threadName, clientID, username, password);
            m_senders = new LinkedList();
            m_numSessions = numSessions;
            m_senderLock = new Object();
        }

        byte[] call(JMSEndpoint endpoint, byte[] message, long timeout, HashMap properties)
                throws Exception {
            long timeoutTime = System.currentTimeMillis() + timeout;

            while (true) {
                if (System.currentTimeMillis() > timeoutTime) {
                    throw new InvokeTimeoutException("Unable to complete call in time allotted");
                }

                SendSession sendSession = null;

                try {
                    sendSession = getSessionFromPool(m_poolTimeout);

                    byte[] response = sendSession.call(endpoint, message,
                            timeoutTime - System.currentTimeMillis(),
                            properties);

                    returnSessionToPool(sendSession);

                    if (response == null) {
                        throw new InvokeTimeoutException(
                                "Unable to complete call in time allotted");
                    }

                    return response;
                } catch (JMSException jmse) {
                    if (!m_adapter.isRecoverable(jmse, JMSVendorAdapter.SEND_ACTION)) {

                        // this we cannot recover from
                        // but it does not invalidate the session
                        returnSessionToPool(sendSession);

                        throw jmse;
                    }

                    // for now we will assume this is a reconnect related issue
                    // and let the sender be collected
                    // give the reconnect thread a chance to fill the pool
                    Thread.yield();

                    continue;
                } catch (NullPointerException npe) {
                    Thread.yield();

                    continue;
                }
            }
        }

        protected abstract SendSession createSendSession(javax.jms.Connection connection)
                throws JMSException;

        protected void onConnect() throws JMSException {
            synchronized (m_senderLock) {
                for (int i = 0; i < m_numSessions; i++) {
                    m_senders.add(createSendSession(m_connection));
                }

                m_senderLock.notifyAll();
            }
        }

        protected void onException() {
            synchronized (m_senderLock) {
                m_senders.clear();
            }
        }

        protected void onShutdown() {
            synchronized (m_senderLock) {
                Iterator senders = m_senders.iterator();

                while (senders.hasNext()) {
                    SendSession session = (SendSession) senders.next();

                    session.cleanup();
                }

                m_senders.clear();
            }
        }

        private void returnSessionToPool(SendSession sendSession) {
            synchronized (m_senderLock) {
                m_senders.addLast(sendSession);
                m_senderLock.notifyAll();
            }
        }

        /**
         * @todo add in handling for security exceptions
         * @todo add support for timeouts
         */
        void send(JMSEndpoint endpoint, byte[] message, HashMap properties) throws Exception {
            long timeoutTime = System.currentTimeMillis() + m_timeoutTime;

            while (true) {
                if (System.currentTimeMillis() > timeoutTime) {
                    throw new InvokeTimeoutException("Cannot complete send in time allotted");
                }

                SendSession sendSession = null;

                try {
                    sendSession = getSessionFromPool(m_poolTimeout);
                    sendSession.send(endpoint, message, properties);
                    returnSessionToPool(sendSession);
                } catch (JMSException jmse) {
                    if (!m_adapter.isRecoverable(jmse, JMSVendorAdapter.SEND_ACTION)) {

                        // this we cannot recover from
                        // but it does not invalidate the session
                        returnSessionToPool(sendSession);

                        throw jmse;
                    }

                    // for now we will assume this is a reconnect related issue
                    // and let the sender be collected
                    // give the reconnect thread a chance to fill the pool
                    Thread.yield();

                    continue;
                } catch (NullPointerException npe) {

                    // give the reconnect thread a chance to fill the pool
                    Thread.yield();

                    continue;
                }

                break;
            }
        }

        private SendSession getSessionFromPool(long timeout) {
            synchronized (m_senderLock) {
                while (m_senders.size() == 0) {
                    try {
                        m_senderLock.wait(timeout);

                        if (m_senders.size() == 0) {
                            return null;
                        }
                    } catch (InterruptedException ignore) {
                        return null;
                    }
                }

                return (SendSession) m_senders.removeFirst();
            }
        }

        protected abstract class SendSession extends ConnectorSession {
            MessageProducer m_producer;

            SendSession(Session session, MessageProducer producer) throws JMSException {
                super(session);
                m_producer = producer;
            }

            byte[] call(JMSEndpoint endpoint, byte[] message, long timeout, HashMap properties)
                    throws Exception {
                Destination reply = createTemporaryDestination();
                MessageConsumer subscriber = createConsumer(reply);
                BytesMessage jmsMessage = m_session.createBytesMessage();

                jmsMessage.writeBytes(message);
                jmsMessage.setJMSReplyTo(reply);

                int deliveryMode = extractDeliveryMode(properties);
                int priority = extractPriority(properties);
                long timeToLive = extractTimeToLive(properties);

                if ((properties != null) && !properties.isEmpty()) {
                    setProperties(properties, jmsMessage);
                }

                send(endpoint.getDestination(m_session), jmsMessage, deliveryMode, priority,
                        timeToLive);

                BytesMessage response = null;

                try {
                    response = (BytesMessage) subscriber.receive(timeout);
                } catch (ClassCastException cce) {
                    throw new InvokeException(
                            "Error: unexpected message type received - expected BytesMessage");
                }

                byte[] respBytes = null;

                if (response != null) {
                    byte[]                buffer = new byte[8 * 1024];
                    ByteArrayOutputStream out = new ByteArrayOutputStream();

                    for (int bytesRead = response.readBytes(buffer); bytesRead != -1;
                         bytesRead = response.readBytes(buffer)) {
                        out.write(buffer, 0, bytesRead);
                    }

                    respBytes = out.toByteArray();
                }

                subscriber.close();
                deleteTemporaryDestination(reply);

                return respBytes;
            }

            void cleanup() {
                try {
                    m_producer.close();
                } catch (Throwable t) {
                }

                try {
                    m_session.close();
                } catch (Throwable t) {
                }
            }

            protected abstract MessageConsumer createConsumer(Destination destination)
                    throws JMSException;

            protected abstract Destination createTemporaryDestination() throws JMSException;

            protected abstract void deleteTemporaryDestination(Destination destination)
                    throws JMSException;

            private int extractDeliveryMode(HashMap properties) {
                return MapUtils.removeIntProperty(properties, JMSConstants.DELIVERY_MODE,
                        JMSConstants.DEFAULT_DELIVERY_MODE);
            }

            private int extractPriority(HashMap properties) {
                return MapUtils.removeIntProperty(properties, JMSConstants.PRIORITY,
                        JMSConstants.DEFAULT_PRIORITY);
            }

            private long extractTimeToLive(HashMap properties) {
                return MapUtils.removeLongProperty(properties, JMSConstants.TIME_TO_LIVE,
                        JMSConstants.DEFAULT_TIME_TO_LIVE);
            }

            void send(JMSEndpoint endpoint, byte[] message, HashMap properties) throws Exception {
                BytesMessage jmsMessage = m_session.createBytesMessage();

                jmsMessage.writeBytes(message);

                int deliveryMode = extractDeliveryMode(properties);
                int priority = extractPriority(properties);
                long timeToLive = extractTimeToLive(properties);

                if ((properties != null) && !properties.isEmpty()) {
                    setProperties(properties, jmsMessage);
                }

                send(endpoint.getDestination(m_session), jmsMessage, deliveryMode, priority,
                        timeToLive);
            }

            protected abstract void send(Destination destination, Message message,
                                         int deliveryMode, int priority, long timeToLive)
                    throws JMSException;

            private void setProperties(HashMap properties, Message message) throws JMSException {
                Iterator propertyIter = properties.entrySet().iterator();

                while (propertyIter.hasNext()) {
                    Map.Entry property = (Map.Entry) propertyIter.next();

                    setProperty((String) property.getKey(), property.getValue(), message);
                }
            }

            private void setProperty(String property, Object value, Message message)
                    throws JMSException {
                if (property == null) {
                    return;
                }

                if (property.equals(JMSConstants.JMS_CORRELATION_ID)) {
                    message.setJMSCorrelationID((String) value);
                } else if (property.equals(JMSConstants.JMS_CORRELATION_ID_AS_BYTES)) {
                    message.setJMSCorrelationIDAsBytes((byte[]) value);
                } else if (property.equals(JMSConstants.JMS_TYPE)) {
                    message.setJMSType((String) value);
                } else {
                    message.setObjectProperty(property, value);
                }
            }
        }
    }
}
