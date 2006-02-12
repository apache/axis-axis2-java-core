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
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMOutputFormat;
import org.apache.ws.commons.soap.SOAPEnvelope;

import javax.jms.Destination;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is meant to be used on a SOAP Client to call a SOAP server.
 */
public class JMSSender extends AbstractHandler implements TransportSender {
	
    private static final long serialVersionUID = -3883554138407525394L;
    
	protected static Log log = LogFactory.getLog(JMSSender.class.getName());

    static {

        // add a shutdown hook to close JMS connections
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                JMSSender.closeAllConnectors();
            }
        });
    }

    HashMap params = new HashMap();

    public JMSSender() {
    }

    public void cleanUp(MessageContext msgContext) throws AxisFault {
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
    public static void closeMatchingJMSConnectors(String endpointAddr, String username,
                                                  String password) {
        if (log.isDebugEnabled()) {
            log.debug("Enter: JMSTransport::closeMatchingJMSConnectors");
        }

        try {
            JMSURLHelper jmsurl = new JMSURLHelper(endpointAddr);
            String vendorId = jmsurl.getVendor();
            JMSVendorAdapter vendorAdapter = null;

            if (vendorId == null) {
                vendorId = JMSConstants.JNDI_VENDOR_ID;
            }

            vendorAdapter = JMSVendorAdapterFactory.getJMSVendorAdapter(vendorId);

            // the vendor adapter may not exist
            if (vendorAdapter == null) {
                return;
            }

            // determine the set of properties to be used for matching the connection
            HashMap connectorProps = vendorAdapter.getJMSConnectorProperties(jmsurl);
            HashMap cfProps = vendorAdapter.getJMSConnectionFactoryProperties(jmsurl);

            JMSConnectorManager.getInstance().closeMatchingJMSConnectors(connectorProps, cfProps,
                    username, password, vendorAdapter);
        } catch (Exception e) {
            log.warn(Messages.getMessage("malformedURLException00"), e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Exit: JMSTransport::closeMatchingJMSConnectors");
        }
    }

    /**
     * Return a map of properties that makeup the application-specific
     * for the JMS Messages.
     */
    protected HashMap createApplicationProperties(MessageContext context) {
        HashMap props = new HashMap();

        if (context.getProperty(JMSConstants.JMS_APPLICATION_MSG_PROPS) != null) {
            props.putAll((Map) context.getProperty(JMSConstants.JMS_APPLICATION_MSG_PROPS));
        }

        return props;
    }

    private HashMap createSendProperties(MessageContext context) {

        // I'm not sure why this helper method is private, but
        // we need to delegate to factory method that can build the
        // application-specific map of properties so make a change to
        // delegate here.
        HashMap props = createApplicationProperties(context);

        if (context.getProperty(JMSConstants.PRIORITY) != null) {
            props.put(JMSConstants.PRIORITY, context.getProperty(JMSConstants.PRIORITY));
        }

        if (context.getProperty(JMSConstants.DELIVERY_MODE) != null) {
            props.put(JMSConstants.DELIVERY_MODE, context.getProperty(JMSConstants.DELIVERY_MODE));
        }

        if (context.getProperty(JMSConstants.TIME_TO_LIVE) != null) {
            props.put(JMSConstants.TIME_TO_LIVE, context.getProperty(JMSConstants.TIME_TO_LIVE));
        }

        if (context.getProperty(JMSConstants.JMS_CORRELATION_ID) != null) {
            props.put(JMSConstants.JMS_CORRELATION_ID,
                    context.getProperty(JMSConstants.JMS_CORRELATION_ID));
        }

        return props;
    }

    public void init(ConfigurationContext confContext, TransportOutDescription transportOut)
            throws AxisFault {
        Iterator iterator = transportOut.getParameters().iterator();

        while (iterator.hasNext()) {
            Parameter param = (Parameter) iterator.next();

            params.put(param.getName(), param.getValue());
        }
    }

    /**
     * invoke() creates an endpoint, sends the request SOAP message, and then
     * either reads the response SOAP message or simply returns.
     *
     * @param msgContext
     * @throws AxisFault
     */
    public void invoke(MessageContext msgContext) throws AxisFault {
        JMSConnector connector = null;
        HashMap properties = null;
        Destination dest = null;

        if (msgContext.isServerSide()) {
            JMSOutTransportInfo transportInfo =
                    (JMSOutTransportInfo) msgContext.getProperty(Constants.OUT_TRANSPORT_INFO);

            if (transportInfo != null) {
                dest = transportInfo.getDestination();
                properties = transportInfo.getProperties();
            }
        }

        String endpointAddress = msgContext.getTo().getAddress();
        boolean waitForResponse = false;

        if (dest == null) {
            if ((msgContext
                    .getProperty(Constants.Configuration
                            .IS_USING_SEPARATE_LISTENER) != null) && msgContext
                    .getProperty(Constants.Configuration.IS_USING_SEPARATE_LISTENER)
                    .equals(Boolean.TRUE)) {
                waitForResponse = !((Boolean) msgContext.getProperty(
                        Constants.Configuration.IS_USING_SEPARATE_LISTENER)).booleanValue();
            }
        } else {
            if (properties != null) {
                JMSURLHelper url = null;

                try {
                    url = new JMSURLHelper("jms:/" + dest);
                } catch (Exception e) {
                    throw AxisFault.makeFault(e);
                }

                url.getProperties().putAll(properties);
                endpointAddress = url.getURLString();
            }
        }

        setupTransport(msgContext, endpointAddress);

        if (connector == null) {
            connector = (JMSConnector) msgContext.getProperty(JMSConstants.CONNECTOR);
        }

        try {
            JMSEndpoint endpoint = null;

            if (dest == null) {
                Object destination = msgContext.getProperty(JMSConstants.DESTINATION);

                if ((destination == null) && (msgContext.getTo() != null)) {
                    String to = msgContext.getTo().getAddress();

                    if (to != null) {
                        JMSURLHelper url = new JMSURLHelper(to);

                        destination = url.getDestination();
                    }
                }

                if (destination == null) {
                    throw new AxisFault("noDestination");
                }

                if (destination instanceof String) {
                    endpoint = connector.createEndpoint((String) destination);
                } else {
                    endpoint = connector.createEndpoint((Destination) destination);
                }
            } else {
                endpoint = connector.createEndpoint(dest);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // TODO: How do we fix Attachments?
            writeMessage(msgContext, out);

            HashMap props = createSendProperties(msgContext);

            props.put("contentType", getContentType(msgContext));
            props.put("SOAPAction", getSOAPAction(msgContext));

            if (waitForResponse) {
                long timeout = Options.DEFAULT_TIMEOUT_MILLISECONDS;

                if (msgContext.getProperty(JMSConstants.TIMEOUT_TIME) != null) {
                    timeout =
                            ((Long) msgContext.getProperty(JMSConstants.TIMEOUT_TIME)).longValue();
                }

                byte[]      response = endpoint.call(out.toByteArray(), timeout, props);
                InputStream in = new ByteArrayInputStream(response);

                msgContext.setProperty(MessageContext.TRANSPORT_IN, in);
            } else {
                endpoint.send(out.toByteArray(), props);
            }
        } catch (Exception e) {
            throw new AxisFault("failedSend", e);
        } finally {
            if (connector != null) {
                JMSConnectorManager.getInstance().release(connector);
            }
        }
    }

    /**
     * Set up any transport-specific derived properties in the message context.
     *
     * @param context the context to set up
     * @throws AxisFault if service cannot be found
     */
    public void setupTransport(MessageContext context, String endpointAddr) throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("Enter: JMSTransport::invoke");
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
        // is instantiating the JMSTransport directly (deprecated) or indirectly via JMS URL
        if (endpointAddr != null) {
            try {

                // performs minimal validation ('jms:/destination?...')
                jmsurl = new JMSURLHelper(endpointAddr);

                // lookup the appropriate vendor adapter
                String vendorId = jmsurl.getVendor();

                if (vendorId == null) {
                    vendorId = JMSConstants.JNDI_VENDOR_ID;
                }

                if (log.isDebugEnabled()) {
                    log.debug("JMSTransport.invoke(): endpt=" + endpointAddr + ", vendor="
                            + vendorId);
                }

                vendorAdapter = JMSVendorAdapterFactory.getJMSVendorAdapter(vendorId);

                if (vendorAdapter == null) {
                    throw new AxisFault("cannotLoadAdapterClass:" + vendorId);
                }

                // populate the connector and connection factory properties tables
                connectorProperties = vendorAdapter.getJMSConnectorProperties(jmsurl);
                connectionFactoryProperties =
                        vendorAdapter.getJMSConnectionFactoryProperties(jmsurl);
            } catch (Exception e) {
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
            connectorProperties = params;
            connectionFactoryProperties = params;
        }

        try {
            connector = JMSConnectorManager.getInstance().getConnector(connectorProperties,
                    connectionFactoryProperties, username, password, vendorAdapter);
        } catch (Exception e) {
            log.error(Messages.getMessage("cannotConnectError"), e);

            if (e instanceof AxisFault) {
                throw(AxisFault) e;
            }

            throw new AxisFault("cannotConnect", e);
        }

        // store these in the context for later use
        context.setProperty(JMSConstants.CONNECTOR, connector);
        context.setProperty(JMSConstants.VENDOR_ADAPTER, vendorAdapter);

        // vendors may populate the message context
        vendorAdapter.setupMessageContext(context, jmsurl);

        if (log.isDebugEnabled()) {
            log.debug("Exit: JMSTransport::invoke");
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

    public void writeMessage(MessageContext msgContext, OutputStream out) throws AxisFault {
        SOAPEnvelope envelope = msgContext.getEnvelope();
        OMElement outputMessage = envelope;

        if ((envelope != null) && msgContext.isDoingREST()) {
            outputMessage = envelope.getBody().getFirstElement();
        }

        if (outputMessage != null) {
            try {
                OMOutputFormat format = new OMOutputFormat();

                // Pick the char set encoding from the msgContext
                String charSetEnc =
                        (String) msgContext.getProperty(MessageContext.CHARACTER_SET_ENCODING);

                format.setDoOptimize(msgContext.isDoingMTOM());
                format.setCharSetEncoding(charSetEnc);
                outputMessage.serializeAndConsume(out, format);
                out.flush();
            } catch (Exception e) {
                throw new AxisFault(e);
            }
        } else {
            throw new AxisFault(Messages.getMessage("outMessageNull"));
        }
    }

    public String getContentType(MessageContext msgCtx) {
        OMOutputFormat format = new OMOutputFormat();
        String soapActionString = getSOAPAction(msgCtx);
        String charSetEnc =
                (String) msgCtx.getProperty(MessageContext.CHARACTER_SET_ENCODING);

        if (charSetEnc != null) {
            format.setCharSetEncoding(charSetEnc);
        } else {
            OperationContext opctx = msgCtx.getOperationContext();

            if (opctx != null) {
                charSetEnc = (String) opctx.getProperty(MessageContext.CHARACTER_SET_ENCODING);
            }
        }

        /**
         * If the char set enc is still not found use the default
         */
        if (charSetEnc == null) {
            charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        format.setSOAP11(msgCtx.isSOAP11());
        format.setCharSetEncoding(charSetEnc);

        String encoding = format.getCharSetEncoding();
        String contentType = format.getContentType();

        if (encoding != null) {
            contentType += "; charset=" + encoding;
        }

        // action header is not mandated in SOAP 1.2. So putting it, if available
        if (!msgCtx.isSOAP11() && (soapActionString != null)
                && !"".equals(soapActionString.trim())) {
            contentType = contentType + ";action=\"" + soapActionString + "\";";
        }

        return contentType;
    }

    private String getSOAPAction(MessageContext msgCtx) {
        String soapActionString = msgCtx.getSoapAction();

        if ((soapActionString == null) || (soapActionString.length() == 0)) {
            soapActionString = msgCtx.getWSAAction();
        }

        if (soapActionString == null) {
            soapActionString = "";
        }

        return soapActionString;
    }
}
