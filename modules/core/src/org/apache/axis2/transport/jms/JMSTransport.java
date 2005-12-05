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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;

/**
 * JMSTransport is the JMS-specific implemenation of org.apache.axis.client.Transport.
 * It implements the setupMessageContextImpl() function to set JMS-specific message
 * context fields and transport chains.
 * <p/>
 * There are two
 * Connector and connection factory
 * properties are passed in during instantiation and are in turn passed through
 * when creating a connector.
 */
public class JMSTransport {
    protected static Log log =
            LogFactory.getLog(JMSTransport.class.getName());

    private static HashMap vendorConnectorPools = new HashMap();

    private HashMap defaultConnectorProps;
    private HashMap defaultConnectionFactoryProps;
    private String transportName;

    static {
        // add a shutdown hook to close JMS connections
        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    public void run() {
                        JMSTransport.closeAllConnectors();
                    }
                }
        );
    }

    public JMSTransport() {
        transportName = "JMSTransport";
    }

    // this cons is provided for clients that instantiate the JMSTransport directly
    public JMSTransport(HashMap connectorProps,
                        HashMap connectionFactoryProps) {
        this();
        defaultConnectorProps = connectorProps;
        defaultConnectionFactoryProps = connectionFactoryProps;
    }

    /**
     * Set up any transport-specific derived properties in the message context.
     *
     * @param context the context to set up
     * @throws AxisFault if service cannot be found
     */
    public void setupMessageContextImpl(MessageContext context)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Enter: JMSTransport::setupMessageContextImpl");
        }

        JMSConnector connector = null;
        HashMap connectorProperties = null;
        HashMap connectionFactoryProperties = null;

        JMSVendorAdapter vendorAdapter = null;
        JMSURLHelper jmsurl = null;

        // a security context is required to create/use JMSConnectors
        // TODO: Fill username password from context
        String username = "";
        String password = "";

        // the presence of an endpoint address indicates whether the client application
        //  is instantiating the JMSTransport directly (deprecated) or indirectly via JMS URL
        String endpointAddr = context.getTo().getAddress();
        if (endpointAddr != null) {
            try {
                // performs minimal validation ('jms:/destination?...')
                jmsurl = new JMSURLHelper(new java.net.URL(endpointAddr));

                // lookup the appropriate vendor adapter
                String vendorId = jmsurl.getVendor();
                if (vendorId == null)
                    vendorId = JMSConstants.JNDI_VENDOR_ID;

                if (log.isDebugEnabled())
                    log.debug("JMSTransport.setupMessageContextImpl(): endpt=" + endpointAddr +
                            ", vendor=" + vendorId);

                vendorAdapter = JMSVendorAdapterFactory.getJMSVendorAdapter(vendorId);
                if (vendorAdapter == null) {
                    throw new AxisFault("cannotLoadAdapterClass:" + vendorId);
                }

                // populate the connector and connection factory properties tables
                connectorProperties = vendorAdapter.getJMSConnectorProperties(jmsurl);
                connectionFactoryProperties = vendorAdapter.getJMSConnectionFactoryProperties(jmsurl);
            }
            catch (java.net.MalformedURLException e) {
                log.error(Messages.getMessage("malformedURLException00"), e);
                throw new AxisFault(Messages.getMessage("malformedURLException00"), e);
            }
        } else {
            // the JMSTransport was instantiated directly, use the default adapter
            try {
                vendorAdapter = JMSVendorAdapterFactory.getJMSVendorAdapter();
            } catch (Exception e) {
                throw new AxisFault("cannotLoadAdapterClass");
            }

            // use the properties passed in to the constructor
            connectorProperties = defaultConnectorProps;
            connectionFactoryProperties = defaultConnectionFactoryProps;
        }

        try {
            connector = JMSConnectorManager.getInstance().getConnector(connectorProperties, connectionFactoryProperties,
                    username, password, vendorAdapter);
        }
        catch (Exception e) {
            log.error(Messages.getMessage("cannotConnectError"), e);

            if (e instanceof AxisFault)
                throw (AxisFault) e;
            throw new AxisFault("cannotConnect", e);
        }

        // store these in the context for later use
        context.setProperty(JMSConstants.CONNECTOR, connector);
        context.setProperty(JMSConstants.VENDOR_ADAPTER, vendorAdapter);

        // vendors may populate the message context
        vendorAdapter.setupMessageContext(context, jmsurl);

        if (log.isDebugEnabled()) {
            log.debug("Exit: JMSTransport::setupMessageContextImpl");
        }
    }

    /**
     * Shuts down the connectors managed by this JMSTransport.
     */
    public void shutdown() {
        if (log.isDebugEnabled()) {
            log.debug("Enter: JMSTransport::shutdown");
        }

        closeAllConnectors();

        if (log.isDebugEnabled()) {
            log.debug("Exit: JMSTransport::shutdown");
        }
    }

    /**
     * Closes all JMS connectors
     */
    public static void closeAllConnectors() {
        if (log.isDebugEnabled()) {
            log.debug("Enter: JMSTransport::closeAllConnectors");
        }

        JMSConnectorManager.getInstance().closeAllConnectors();

        if (log.isDebugEnabled()) {
            log.debug("Exit: JMSTransport::closeAllConnectors");
        }
    }

    /**
     * Closes JMS connectors that match the specified endpoint address
     *
     * @param endpointAddr the JMS endpoint address
     * @param username
     * @param password
     */
    public static void closeMatchingJMSConnectors(String endpointAddr, String username, String password) {
        if (log.isDebugEnabled()) {
            log.debug("Enter: JMSTransport::closeMatchingJMSConnectors");
        }

        try {
            JMSURLHelper jmsurl = new JMSURLHelper(new java.net.URL(endpointAddr));
            String vendorId = jmsurl.getVendor();

            JMSVendorAdapter vendorAdapter = null;
            if (vendorId == null)
                vendorId = JMSConstants.JNDI_VENDOR_ID;
            vendorAdapter = JMSVendorAdapterFactory.getJMSVendorAdapter(vendorId);

            // the vendor adapter may not exist
            if (vendorAdapter == null)
                return;

            // determine the set of properties to be used for matching the connection
            HashMap connectorProps = vendorAdapter.getJMSConnectorProperties(jmsurl);
            HashMap cfProps = vendorAdapter.getJMSConnectionFactoryProperties(jmsurl);

            JMSConnectorManager.getInstance().closeMatchingJMSConnectors(connectorProps, cfProps,
                    username, password,
                    vendorAdapter);
        }
        catch (java.net.MalformedURLException e) {
            log.warn(Messages.getMessage("malformedURLException00"), e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Exit: JMSTransport::closeMatchingJMSConnectors");
        }
    }
}