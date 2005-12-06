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
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMOutputFormat;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.AbstractTransportSender;
import org.apache.axis2.transport.TransportSender;

import javax.jms.Destination;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This is meant to be used on a SOAP Client to call a SOAP server.
 */
public class JMSSender extends JMSTransport implements TransportSender {
    public JMSSender() {
    }

    public void init(HandlerDescription handlerdesc) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void init(ConfigurationContext confContext, TransportOutDescription transportOut) throws AxisFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * invoke() creates an endpoint, sends the request SOAP message, and then
     * either reads the response SOAP message or simply returns.
     *
     * @param msgContext
     * @throws AxisFault
     */
    public void invoke(MessageContext msgContext) throws AxisFault {
        
        super.invoke(msgContext);
        
        JMSConnector connector = null;
        try {
            Object destination = msgContext.getProperty(JMSConstants.DESTINATION);
            if (destination == null)
                throw new AxisFault("noDestination");

            connector = (JMSConnector) msgContext.getProperty(JMSConstants.CONNECTOR);

            JMSEndpoint endpoint = null;
            if (destination instanceof String)
                endpoint = connector.createEndpoint((String) destination);
            else
                endpoint = connector.createEndpoint((Destination) destination);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeMessage(msgContext, out);

            HashMap props = createSendProperties(msgContext);

// TODO: How do we fix Attachments?            
//            // If the request message contains attachments, set
//            // a contentType property to go in the outgoing message header
//            String ret = null;
//            Message message = msgContext.getRequestMessage();
//            Attachments mAttachments = message.getAttachmentsImpl();
//            if (mAttachments != null && 0 != mAttachments.getAttachmentCount()) {
//                String contentType = mAttachments.getContentType();
//                if (contentType != null && !contentType.trim().equals("")) {
//                    props.put("contentType", contentType);
//                }
//            }

            boolean waitForResponse = true;
            if (msgContext.getProperty(JMSConstants.WAIT_FOR_RESPONSE).equals(Boolean.TRUE))
                waitForResponse =
                        ((Boolean) msgContext.getProperty(
                                JMSConstants.WAIT_FOR_RESPONSE)).booleanValue();
            if (waitForResponse) {
                long timeout = ((Long) msgContext.getProperty(JMSConstants._TIMEOUT_TIME)).longValue();
                byte[] response = endpoint.call(out.toByteArray(), timeout, props);
                InputStream in = new ByteArrayInputStream(response);
                msgContext.setProperty(MessageContext.TRANSPORT_IN, in);
            } else {
                endpoint.send(out.toByteArray(), props);
            }
        }
        catch (Exception e) {
            throw new AxisFault("failedSend", e);
        }
        finally {
            if (connector != null)
                JMSConnectorManager.getInstance().release(connector);
        }
    }

    private HashMap createSendProperties(MessageContext context) {
        //I'm not sure why this helper method is private, but 
        //we need to delegate to factory method that can build the
        //application-specific map of properties so make a change to
        //delegate here. 
        HashMap props = createApplicationProperties(context);

        if (context.getProperty(JMSConstants.PRIORITY) != null)
            props.put(JMSConstants.PRIORITY,
                    context.getProperty(JMSConstants.PRIORITY));
        if (context.getProperty(JMSConstants.DELIVERY_MODE) != null)
            props.put(JMSConstants.DELIVERY_MODE,
                    context.getProperty(JMSConstants.DELIVERY_MODE));
        if (context.getProperty(JMSConstants.TIME_TO_LIVE) != null)
            props.put(JMSConstants.TIME_TO_LIVE,
                    context.getProperty(JMSConstants.TIME_TO_LIVE));
        if (context.getProperty(JMSConstants.JMS_CORRELATION_ID) != null)
            props.put(JMSConstants.JMS_CORRELATION_ID,
                    context.getProperty(JMSConstants.JMS_CORRELATION_ID));
        return props;
    }

    /**
     * Return a map of properties that makeup the application-specific
     * for the JMS Messages.
     */
    protected HashMap createApplicationProperties(MessageContext context) {
        HashMap props = null;
        if (context.getProperty(
                JMSConstants.JMS_APPLICATION_MSG_PROPS) != null) {
            props = new HashMap();
            props.putAll((Map) context.getProperty(
                    JMSConstants.JMS_APPLICATION_MSG_PROPS));
        }
        return props;
    }

    public QName getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Parameter getParameter(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void cleanup() throws AxisFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public HandlerDescription getHandlerDesc() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void cleanUp(MessageContext msgContext) throws AxisFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void writeMessage(MessageContext msgContext, OutputStream out)
            throws AxisFault {
        SOAPEnvelope envelope = msgContext.getEnvelope();
        OMElement outputMessage = envelope;

        if (envelope != null && msgContext.isDoingREST()) {
            outputMessage = envelope.getBody().getFirstElement();
        }

        if (outputMessage != null) {
            try {
                OMOutputFormat format = new OMOutputFormat();
                //Pick the char set encoding from the msgContext
                String charSetEnc = (String) msgContext
						.getProperty(MessageContext.CHARACTER_SET_ENCODING);
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

}