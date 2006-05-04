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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;

/**
 * JMSConnectorFactory is a factory class for creating JMSConnectors. It creates
 * both client connectors and server connectors.  A server connector
 * is configured to allow asynchronous message receipt, while a client
 * connector is not.
 * <p/>
 * JMSConnectorFactory is also used to select an appropriately configured
 * JMSConnector from an existing pool of connectors.
 */
public abstract class JMSConnectorFactory {
	private static final Log log = LogFactory.getLog(JMSConnectorFactory.class);

    /**
     * Static method to create a client connector. Client connectors cannot
     * accept incoming requests.
     *
     * @param connectorConfig
     * @param cfConfig
     * @param username
     * @param password
     * @return Returns JMSConnector.
     * @throws Exception
     */
    public static JMSConnector createClientConnector(HashMap connectorConfig, HashMap cfConfig,
                                                     String username, String password, JMSVendorAdapter adapter)
            throws Exception {
        return createConnector(connectorConfig, cfConfig, false, username, password, adapter);
    }

    private static JMSConnector createConnector(HashMap connectorConfig, HashMap cfConfig,
                                                boolean allowReceive, String username, String password, JMSVendorAdapter adapter)
            throws Exception {
        if (connectorConfig != null) {
            connectorConfig = (HashMap) connectorConfig.clone();
        }

        int numRetries = MapUtils.removeIntProperty(connectorConfig, JMSConstants.NUM_RETRIES,
                JMSConstants.DEFAULT_NUM_RETRIES);
        int numSessions = MapUtils.removeIntProperty(connectorConfig, JMSConstants.NUM_SESSIONS,
                JMSConstants.DEFAULT_NUM_SESSIONS);
        long connectRetryInterval = MapUtils.removeLongProperty(connectorConfig,
                JMSConstants.CONNECT_RETRY_INTERVAL,
                JMSConstants.DEFAULT_CONNECT_RETRY_INTERVAL);
        long interactRetryInterval = MapUtils.removeLongProperty(connectorConfig,
                JMSConstants.INTERACT_RETRY_INTERVAL,
                JMSConstants.DEFAULT_INTERACT_RETRY_INTERVAL);
        long timeoutTime = MapUtils.removeLongProperty(connectorConfig, JMSConstants.TIMEOUT_TIME,
                JMSConstants.DEFAULT_TIMEOUT_TIME);
        String clientID = MapUtils.removeStringProperty(connectorConfig, JMSConstants.CLIENT_ID,
                null);
        String domain = MapUtils.removeStringProperty(connectorConfig, JMSConstants.DOMAIN,
                JMSConstants.DOMAIN_DEFAULT);

        // this will be set if the target endpoint address was set on the Axis call
        JMSURLHelper jmsurl = (JMSURLHelper) connectorConfig.get(JMSConstants.JMS_URL);

        if (cfConfig == null) {
            throw new IllegalArgumentException("noCfConfig");
        }

        if (domain.equals(JMSConstants.DOMAIN_QUEUE)) {
            return new QueueConnector(adapter.getQueueConnectionFactory(cfConfig), numRetries,
                    numSessions, connectRetryInterval, interactRetryInterval,
                    timeoutTime, allowReceive, clientID, username, password,
                    adapter, jmsurl);
        } else    // domain is Topic
        {
            return new TopicConnector(adapter.getTopicConnectionFactory(cfConfig), numRetries,
                    numSessions, connectRetryInterval, interactRetryInterval,
                    timeoutTime, allowReceive, clientID, username, password,
                    adapter, jmsurl);
        }
    }

    /**
     * Static method to create a server connector. Server connectors can
     * accept incoming requests.
     *
     * @param connectorConfig
     * @param cfConfig
     * @param username
     * @param password
     * @return Returns JMSConnector.
     * @throws Exception
     */
    public static JMSConnector createServerConnector(HashMap connectorConfig, HashMap cfConfig,
                                                     String username, String password, JMSVendorAdapter adapter)
            throws Exception {
        return createConnector(connectorConfig, cfConfig, true, username, password, adapter);
    }

    /**
     * Performs an initial check on the connector properties, and then defers
     * to the vendor adapter for matching on the vendor-specific connection factory.
     *
     * @param connectors     the list of potential matches
     * @param connectorProps the set of properties to be used for matching the connector
     * @param cfProps        the set of properties to be used for matching the connection factory
     * @param username       the user requesting the connector
     * @param password       the password associated with the requesting user
     * @param adapter        the vendor adapter specified in the JMS URL
     * @return Returns a JMSConnector that matches the specified properties.
     */
    public static JMSConnector matchConnector(java.util.Set connectors, HashMap connectorProps,
                                              HashMap cfProps, String username, String password, JMSVendorAdapter adapter) {
        java.util.Iterator iter = connectors.iterator();

        while (iter.hasNext()) {
            JMSConnector conn = (JMSConnector) iter.next();

            // username
            String connectorUsername = conn.getUsername();

            if (!(((connectorUsername == null) && (username == null))
                    || ((connectorUsername != null) && (username != null)
                    && (connectorUsername.equals(username))))) {
                continue;
            }

            // password
            String connectorPassword = conn.getPassword();

            if (!(((connectorPassword == null) && (password == null))
                    || ((connectorPassword != null) && (password != null)
                    && (connectorPassword.equals(password))))) {
                continue;
            }

            // num retries
            int connectorNumRetries = conn.getNumRetries();
            String propertyNumRetries = (String) connectorProps.get(JMSConstants.NUM_RETRIES);
            int numRetries = JMSConstants.DEFAULT_NUM_RETRIES;

            if (propertyNumRetries != null) {
                numRetries = Integer.parseInt(propertyNumRetries);
            }

            if (connectorNumRetries != numRetries) {
                continue;
            }

            // client id
            String connectorClientID = conn.getClientID();
            String clientID = (String) connectorProps.get(JMSConstants.CLIENT_ID);

            if (!(((connectorClientID == null) && (clientID == null))
                    || ((connectorClientID != null) && (clientID != null)
                    && connectorClientID.equals(clientID)))) {
                continue;
            }

            // domain
            String connectorDomain = (conn instanceof QueueConnector)
                    ? JMSConstants.DOMAIN_QUEUE
                    : JMSConstants.DOMAIN_TOPIC;
            String propertyDomain = (String) connectorProps.get(JMSConstants.DOMAIN);
            String domain = JMSConstants.DOMAIN_DEFAULT;

            if (propertyDomain != null) {
                domain = propertyDomain;
            }

            if (!(((connectorDomain == null) && (domain == null))
                    || ((connectorDomain != null) && (domain != null)
                    && connectorDomain.equalsIgnoreCase(domain)))) {
                continue;
            }

            // the connection factory must also match for the connector to be reused
            JMSURLHelper jmsurl = conn.getJMSURL();

            if (adapter.isMatchingConnectionFactory(conn.getConnectionFactory(), jmsurl, cfProps)) {

                // attempt to reserve the connector
                try {
                    JMSConnectorManager.getInstance().reserve(conn);

                    if (log.isDebugEnabled()) {
                        log.debug("JMSConnectorFactory: Found matching connector");
                    }
                } catch (Exception e) {

                    // ignore. the connector may be in the process of shutting down, so try the next element
                    continue;
                }

                return conn;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("JMSConnectorFactory: No matching connectors found");
        }

        return null;
    }
}
