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

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

/**
 * QueueConnector is a concrete JMSConnector subclass that specifically handles
 * connections to queues (ptp domain).
 */
public class QueueConnector extends JMSConnector {
    public QueueConnector(ConnectionFactory factory, int numRetries, int numSessions,
                          long connectRetryInterval, long interactRetryInterval, long timeoutTime,
                          boolean allowReceive, String clientID, String username, String password,
                          JMSVendorAdapter adapter, JMSURLHelper jmsurl)
            throws JMSException {
        super(factory, numRetries, numSessions, connectRetryInterval, interactRetryInterval,
                timeoutTime, allowReceive, clientID, username, password, adapter, jmsurl);
    }

    protected AsyncConnection createAsyncConnection(ConnectionFactory factory,
                                                    javax.jms.Connection connection, String threadName, String clientID, String username,
                                                    String password)
            throws JMSException {
        return new QueueAsyncConnection((QueueConnectionFactory) factory,
                (QueueConnection) connection, threadName, clientID,
                username, password);
    }

    /**
     * Creates an endpoint for a queue destination.
     *
     * @param destination
     * @return Returns JMSEndPoint.
     * @throws JMSException
     */
    public JMSEndpoint createEndpoint(Destination destination) throws JMSException {
        if (!(destination instanceof Queue)) {
            throw new IllegalArgumentException("The input must be a queue for this connector");
        }

        return new QueueDestinationEndpoint((Queue) destination);
    }

    public JMSEndpoint createEndpoint(String destination) {
        return new QueueEndpoint(destination);
    }

    private Queue createQueue(QueueSession session, String subject) throws Exception {
        return m_adapter.getQueue(session, subject);
    }

    private QueueSession createQueueSession(QueueConnection connection, int ackMode)
            throws JMSException {
        return connection.createQueueSession(false, ackMode);
    }

    private QueueReceiver createReceiver(QueueSession session, Queue queue, String messageSelector)
            throws JMSException {
        return session.createReceiver(queue, messageSelector);
    }

    protected SyncConnection createSyncConnection(ConnectionFactory factory,
                                                  javax.jms.Connection connection, int numSessions, String threadName, String clientID,
                                                  String username, String password)
            throws JMSException {
        return new QueueSyncConnection((QueueConnectionFactory) factory,
                (QueueConnection) connection, numSessions, threadName,
                clientID, username, password);
    }

    protected javax.jms.Connection internalConnect(ConnectionFactory connectionFactory,
                                                   String username, String password)
            throws JMSException {
        QueueConnectionFactory qcf = (QueueConnectionFactory) connectionFactory;

        if (username == null) {
            return qcf.createQueueConnection();
        }

        return qcf.createQueueConnection(username, password);
    }

    private final class QueueAsyncConnection extends AsyncConnection {
        QueueAsyncConnection(QueueConnectionFactory connectionFactory, QueueConnection connection,
                             String threadName, String clientID, String username, String password)
                throws JMSException {
            super(connectionFactory, connection, threadName, clientID, username, password);
        }

        protected ListenerSession createListenerSession(javax.jms.Connection connection,
                                                        Subscription subscription)
                throws Exception {
            QueueSession session = createQueueSession((QueueConnection) connection,
                    subscription.m_ackMode);
            QueueReceiver receiver = createReceiver(session,
                    (Queue) subscription.m_endpoint.getDestination(session),
                    subscription.m_messageSelector);

            return new ListenerSession(session, receiver, subscription);
        }
    }


    private final class QueueDestinationEndpoint extends QueueEndpoint {
        Queue m_queue;

        QueueDestinationEndpoint(Queue queue) throws JMSException {
            super(queue.getQueueName());
            m_queue = queue;
        }

        Destination getDestination(Session session) {
            return m_queue;
        }
    }


    private class QueueEndpoint extends JMSEndpoint {
        String m_queueName;

        QueueEndpoint(String queueName) {
            super(QueueConnector.this);
            m_queueName = queueName;
        }

        public boolean equals(Object object) {
            if (!super.equals(object)) {
                return false;
            }

            if (!(object instanceof QueueEndpoint)) {
                return false;
            }

            return m_queueName.equals(((QueueEndpoint) object).m_queueName);
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer("QueueEndpoint:");

            buffer.append(m_queueName);

            return buffer.toString();
        }

        Destination getDestination(Session session) throws Exception {
            return createQueue((QueueSession) session, m_queueName);
        }
    }


    private final class QueueSyncConnection extends SyncConnection {
        QueueSyncConnection(QueueConnectionFactory connectionFactory, QueueConnection connection,
                            int numSessions, String threadName, String clientID, String username,
                            String password)
                throws JMSException {
            super(connectionFactory, connection, numSessions, threadName, clientID, username,
                    password);
        }

        protected SendSession createSendSession(javax.jms.Connection connection)
                throws JMSException {
            QueueSession session = createQueueSession((QueueConnection) connection,
                    JMSConstants.DEFAULT_ACKNOWLEDGE_MODE);
            QueueSender sender = session.createSender(null);

            return new QueueSendSession(session, sender);
        }

        private final class QueueSendSession extends SendSession {
            QueueSendSession(QueueSession session, QueueSender sender) throws JMSException {
                super(session, sender);
            }

            protected MessageConsumer createConsumer(Destination destination) throws JMSException {
                return createReceiver((QueueSession) m_session, (Queue) destination, null);
            }

            protected Destination createTemporaryDestination() throws JMSException {
                return ((QueueSession) m_session).createTemporaryQueue();
            }

            protected void deleteTemporaryDestination(Destination destination) throws JMSException {
                ((TemporaryQueue) destination).delete();
            }

            protected void send(Destination destination, Message message, int deliveryMode,
                                int priority, long timeToLive)
                    throws JMSException {
                ((QueueSender) m_producer).send((Queue) destination, message, deliveryMode,
                        priority, timeToLive);
            }
        }
    }
}
