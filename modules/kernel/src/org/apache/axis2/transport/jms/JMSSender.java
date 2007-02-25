/*
* Copyright 2004,2005 The Apache Software Foundation.
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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The TransportSender for JMS
 */
public class JMSSender extends AbstractHandler implements TransportSender {

    private static final Log log = LogFactory.getLog(JMSSender.class);

    /**
     * Performs the actual sending of the JMS message
     *
     * @param msgContext the message context to be sent
     * @throws AxisFault on exception
     */
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

        log.debug("JMSSender invoke()");

        JMSOutTransportInfo transportInfo = null;
        String targetAddress = null;

        // is there a transport url? which may be different from the WS-A To..
        targetAddress = (String) msgContext.getProperty(
            Constants.Configuration.TRANSPORT_URL);

        if (targetAddress != null) {
            transportInfo = new JMSOutTransportInfo(targetAddress);            
        } else if (targetAddress == null && msgContext.getTo() != null &&
            !msgContext.getTo().hasAnonymousAddress()) {
            targetAddress = msgContext.getTo().getAddress();
            
            if (!msgContext.getTo().hasNoneAddress()) {
                transportInfo = new JMSOutTransportInfo(targetAddress);
            } else {
                //Don't send the message.
              return InvocationResponse.CONTINUE;        
            }
        } else if (msgContext.isServerSide()) {
            // get the jms ReplyTo
            transportInfo = (JMSOutTransportInfo)
                msgContext.getProperty(Constants.OUT_TRANSPORT_INFO);                
        }

        // should we wait and listen for a response?
        boolean waitForResponse = false;

        Object seprLisn = msgContext.getProperty(
                Constants.Configuration.IS_USING_SEPARATE_LISTENER);
        if (Boolean.TRUE.equals(seprLisn)) {
            waitForResponse = false;
        } else if (!msgContext.isServerSide()) {
            waitForResponse = !msgContext.getOptions().isUseSeparateListener();
        }

        // get the ConnectionFactory to be used for the send
        ConnectionFactory connectionFac = transportInfo.getConnectionFactory();

        Connection con = null;
        try {
            con = connectionFac.createConnection();
            Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Message message = createJMSMessage(msgContext, session);

            // get the JMS destination for the message being sent
            Destination dest = transportInfo.getDestination();

            if (dest == null) {
                // if it does not exist, create it
                String name = JMSUtils.getDestination(targetAddress);
                try {
                    dest = session.createQueue(name);
                } catch (JMSException e) {
                    handleException("Error creating destination Queue : " + name, e);
                }
            }

            MessageProducer producer = session.createProducer(dest);
            Destination replyDest = null;

            if (waitForResponse) {
                try {
                    // create temporary queue to receive reply
                    replyDest = session.createTemporaryQueue();
                    message.setJMSReplyTo(replyDest);
                } catch (JMSException e) {
                    handleException("Error creating temporary queue for response");
                }
            }

            try {
                log.debug("[" + (msgContext.isServerSide()?"Server" : "Client") +
                    "]Sending message to destination : " + dest);
                producer.send(message);
                producer.close();

            } catch (JMSException e) {
                handleException("Error sending JMS message to destination : " +
                    dest.toString(), e);
            }

            if (waitForResponse) {
                try {
                    // wait for reply
                    MessageConsumer consumer = session.createConsumer(replyDest);

                    long timeout = JMSConstants.DEFAULT_JMS_TIMEOUT;
                    Long waitReply = (Long) msgContext.getProperty(JMSConstants.JMS_WAIT_REPLY);
                    if (waitReply != null) {
                        timeout = waitReply.longValue();
                    }

                    log.debug("Waiting for a maximum of " + timeout +
                        "ms for a response message to destination : " + replyDest);
                    con.start();
                    Message reply = consumer.receive(timeout);

                    if (reply != null) {
                        msgContext.setProperty(MessageContext.TRANSPORT_IN,
                            JMSUtils.getInputStream(reply));
                    } else {
                        log.warn("Did not receive a JMS response within " +
                            timeout + " ms to destination : " + dest);
                    }

                } catch (JMSException e) {
                    handleException("Error reading response from temporary " +
                        "queue : " + replyDest, e);
                }
            }
        } catch (JMSException e) {
            handleException("Error preparing to send message to destination", e);

        } finally {
            if (con != null) {
                try {
                    con.close(); // closes all sessions, producers, temp Q's etc
                } catch (JMSException e) {
                } // ignore
            }
        }
        return InvocationResponse.CONTINUE;        
    }

    public void cleanup(MessageContext msgContext) throws AxisFault {
        // do nothing
    }

    public void init(ConfigurationContext confContext,
        TransportOutDescription transportOut) throws AxisFault {
        // do nothing
    }

    public void stop() {
        // do nothing
    }

    /**
     * Create a JMS Message from the given MessageContext and using the given
     * session
     *
     * @param msgContext the MessageContext
     * @param session the JMS session
     * @return a JMS message from the context and session
     * @throws JMSException on exception
     */
    private Message createJMSMessage(MessageContext msgContext, Session session)
        throws JMSException {

        Message message = null;
        String msgType = getProperty(msgContext, JMSConstants.JMS_MESSAGE_TYPE);

        OMElement msgElement = msgContext.getEnvelope();
        if (msgContext.isDoingREST()) {
            msgElement = msgContext.getEnvelope().getBody().getFirstElement();
        }

        if (msgType != null && JMSConstants.JMS_BYTE_MESSAGE.equals(msgType)) {

            message = session.createBytesMessage();
            BytesMessage bytesMsg = (BytesMessage) message;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OMOutputFormat format = new OMOutputFormat();
            format.setCharSetEncoding(
                 getProperty(msgContext, Constants.Configuration.CHARACTER_SET_ENCODING));
            format.setDoOptimize(msgContext.isDoingMTOM());
            try {
                msgElement.serializeAndConsume(baos, format);
                baos.flush();
            } catch (XMLStreamException e) {
                handleException("XML serialization error creating BytesMessage", e);
            } catch (IOException e) {
                handleException("IO Error while creating BytesMessage", e);
            }
            bytesMsg.writeBytes(baos.toByteArray());

        } else {
            message = session.createTextMessage();  // default
            TextMessage txtMsg = (TextMessage) message;
            txtMsg.setText(msgElement.toString());
        }

        // set the JMS correlation ID if specified
        String correlationId = getProperty(msgContext, JMSConstants.JMS_COORELATION_ID);
        if (correlationId != null) {
            message.setJMSCorrelationID(correlationId);
        }

        if (msgContext.isServerSide()) {
            // set SOAP Action and context type as properties on the JMS message
            setProperty(message, msgContext, JMSConstants.SOAPACTION);
            setProperty(message, msgContext, JMSConstants.CONTENT_TYPE);
        } else {
            String action = msgContext.getOptions().getAction();
            if (action != null) {
                message.setStringProperty(JMSConstants.SOAPACTION, action);
            }
        }

        return message;
    }

    private void setProperty(Message message, MessageContext msgCtx, String key) {

        String value = getProperty(msgCtx, key);
        if (value != null) {
            try {
                message.setStringProperty(key, value);
            } catch (JMSException e) {
                log.warn("Couldn't set message property : " + key + " = " + value, e);
            }
        }
    }

    private String getProperty(MessageContext mc, String key) {
        return (String) mc.getProperty(key);
    }

    private static void handleException(String s) {
        log.error(s);
        throw new AxisJMSException(s);
    }

    private static void handleException(String s, Exception e) {
        log.error(s, e);
        throw new AxisJMSException(s, e);
    }

}
