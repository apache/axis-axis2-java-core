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
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import java.util.HashMap;

/**
 * TopicConnector is a concrete JMSConnector subclass that specifically handles
 * connections to topics (pub-sub domain).
 */
public class TopicConnector extends JMSConnector {
    public TopicConnector(TopicConnectionFactory factory, int numRetries, int numSessions,
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
        return new TopicAsyncConnection((TopicConnectionFactory) factory,
                (TopicConnection) connection, threadName, clientID,
                username, password);
    }

    private TopicSubscriber createDurableSubscriber(TopicSession session, Topic topic,
                                                    String subscriptionName, String messageSelector, boolean noLocal)
            throws JMSException {
        return session.createDurableSubscriber(topic, subscriptionName, messageSelector, noLocal);
    }

    /**
     * Create an endpoint for a queue destination.
     *
     * @param destination
     * @return
     * @throws JMSException
     */
    public JMSEndpoint createEndpoint(Destination destination) throws JMSException {
        if (!(destination instanceof Topic)) {
            throw new IllegalArgumentException("The input be a topic for this connector");
        }

        return new TopicDestinationEndpoint((Topic) destination);
    }

    public JMSEndpoint createEndpoint(String destination) {
        return new TopicEndpoint(destination);
    }

    private TopicSubscriber createSubscriber(TopicSession session, TopicSubscription subscription)
            throws Exception {
        if (subscription.isDurable()) {
            return createDurableSubscriber(session,
                    (Topic) subscription.m_endpoint.getDestination(session),
                    subscription.m_subscriptionName,
                    subscription.m_messageSelector, subscription.m_noLocal);
        } else {
            return createSubscriber(session,
                    (Topic) subscription.m_endpoint.getDestination(session),
                    subscription.m_messageSelector, subscription.m_noLocal);
        }
    }

    private TopicSubscriber createSubscriber(TopicSession session, Topic topic,
                                             String messageSelector, boolean noLocal)
            throws JMSException {
        return session.createSubscriber(topic, messageSelector, noLocal);
    }

    protected SyncConnection createSyncConnection(ConnectionFactory factory,
                                                  javax.jms.Connection connection, int numSessions, String threadName, String clientID,
                                                  String username, String password)
            throws JMSException {
        return new TopicSyncConnection((TopicConnectionFactory) factory,
                (TopicConnection) connection, numSessions, threadName,
                clientID, username, password);
    }

    private Topic createTopic(TopicSession session, String subject) throws Exception {
        return m_adapter.getTopic(session, subject);
    }

    private TopicSession createTopicSession(TopicConnection connection, int ackMode)
            throws JMSException {
        return connection.createTopicSession(false, ackMode);
    }

    protected javax.jms.Connection internalConnect(ConnectionFactory connectionFactory,
                                                   String username, String password)
            throws JMSException {
        TopicConnectionFactory tcf = (TopicConnectionFactory) connectionFactory;

        if (username == null) {
            return tcf.createTopicConnection();
        }

        return tcf.createTopicConnection(username, password);
    }

    private final class TopicAsyncConnection extends AsyncConnection {
        TopicAsyncConnection(TopicConnectionFactory connectionFactory, TopicConnection connection,
                             String threadName, String clientID, String username, String password)
                throws JMSException {
            super(connectionFactory, connection, threadName, clientID, username, password);
        }

        protected ListenerSession createListenerSession(javax.jms.Connection connection,
                                                        Subscription subscription)
                throws Exception {
            TopicSession session = createTopicSession((TopicConnection) connection,
                    subscription.m_ackMode);
            TopicSubscriber subscriber = createSubscriber(session,
                    (TopicSubscription) subscription);

            return new TopicListenerSession(session, subscriber, (TopicSubscription) subscription);
        }

        private final class TopicListenerSession extends ListenerSession {
            TopicListenerSession(TopicSession session, TopicSubscriber subscriber,
                                 TopicSubscription subscription)
                    throws Exception {
                super(session, subscriber, subscription);
            }

            void cleanup() {
                try {
                    m_consumer.close();
                } catch (Exception ignore) {
                }

                try {
                    TopicSubscription sub = (TopicSubscription) m_subscription;

                    if (sub.isDurable() && sub.m_unsubscribe) {
                        ((TopicSession) m_session).unsubscribe(sub.m_subscriptionName);
                    }
                } catch (Exception ignore) {
                }

                try {
                    m_session.close();
                } catch (Exception ignore) {
                }
            }
        }
    }


    private final class TopicDestinationEndpoint extends TopicEndpoint {
        Topic m_topic;

        TopicDestinationEndpoint(Topic topic) throws JMSException {
            super(topic.getTopicName());
            m_topic = topic;
        }

        Destination getDestination(Session session) {
            return m_topic;
        }
    }


    private class TopicEndpoint extends JMSEndpoint {
        String m_topicName;

        TopicEndpoint(String topicName) {
            super(TopicConnector.this);
            m_topicName = topicName;
        }

        protected Subscription createSubscription(MessageListener listener, HashMap properties) {
            return new TopicSubscription(listener, this, properties);
        }

        public boolean equals(Object object) {
            if (!super.equals(object)) {
                return false;
            }

            if (!(object instanceof TopicEndpoint)) {
                return false;
            }

            return m_topicName.equals(((TopicEndpoint) object).m_topicName);
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer("TopicEndpoint:");

            buffer.append(m_topicName);

            return buffer.toString();
        }

        Destination getDestination(Session session) throws Exception {
            return createTopic((TopicSession) session, m_topicName);
        }
    }


    private final class TopicSubscription extends Subscription {
        boolean m_noLocal;
        String m_subscriptionName;
        boolean m_unsubscribe;

        TopicSubscription(MessageListener listener, JMSEndpoint endpoint, HashMap properties) {
            super(listener, endpoint, properties);
            m_subscriptionName = MapUtils.removeStringProperty(properties,
                    JMSConstants.SUBSCRIPTION_NAME, null);
            m_unsubscribe = MapUtils.removeBooleanProperty(properties, JMSConstants.UNSUBSCRIBE,
                    JMSConstants.DEFAULT_UNSUBSCRIBE);
            m_noLocal = MapUtils.removeBooleanProperty(properties, JMSConstants.NO_LOCAL,
                    JMSConstants.DEFAULT_NO_LOCAL);
        }

        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }

            if (!(obj instanceof TopicSubscription)) {
                return false;
            }

            TopicSubscription other = (TopicSubscription) obj;

            if ((other.m_unsubscribe != m_unsubscribe) || (other.m_noLocal != m_noLocal)) {
                return false;
            }

            if (isDurable()) {
                return other.isDurable() && other.m_subscriptionName.equals(m_subscriptionName);
            } else if (other.isDurable()) {
                return false;
            } else {
                return true;
            }
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer(super.toString());

            buffer.append(":").append(m_noLocal).append(":").append(m_unsubscribe);

            if (isDurable()) {
                buffer.append(":");
                buffer.append(m_subscriptionName);
            }

            return buffer.toString();
        }

        boolean isDurable() {
            return m_subscriptionName != null;
        }
    }


    private final class TopicSyncConnection extends SyncConnection {
        TopicSyncConnection(TopicConnectionFactory connectionFactory, TopicConnection connection,
                            int numSessions, String threadName, String clientID, String username,
                            String password)
                throws JMSException {
            super(connectionFactory, connection, numSessions, threadName, clientID, username,
                    password);
        }

        protected SendSession createSendSession(javax.jms.Connection connection)
                throws JMSException {
            TopicSession session = createTopicSession((TopicConnection) connection,
                    JMSConstants.DEFAULT_ACKNOWLEDGE_MODE);
            TopicPublisher publisher = session.createPublisher(null);

            return new TopicSendSession(session, publisher);
        }

        private final class TopicSendSession extends SendSession {
            TopicSendSession(TopicSession session, TopicPublisher publisher) throws JMSException {
                super(session, publisher);
            }

            protected MessageConsumer createConsumer(Destination destination) throws JMSException {
                return createSubscriber((TopicSession) m_session, (Topic) destination, null,
                        JMSConstants.DEFAULT_NO_LOCAL);
            }

            protected Destination createTemporaryDestination() throws JMSException {
                return ((TopicSession) m_session).createTemporaryTopic();
            }

            protected void deleteTemporaryDestination(Destination destination) throws JMSException {
                ((TemporaryTopic) destination).delete();
            }

            protected void send(Destination destination, Message message, int deliveryMode,
                                int priority, long timeToLive)
                    throws JMSException {
                ((TopicPublisher) m_producer).publish((Topic) destination, message, deliveryMode,
                        priority, timeToLive);
            }
        }
    }
}
