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

import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.OMBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.Builder;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JMSUtils {

    private static final Log log = LogFactory.getLog(JMSUtils.class);

    /**
     * Should this service be enabled on JMS transport?
     * @param service the Axis service
     * @return true if JMS should be enabled
     */
    public static boolean isJMSService(AxisService service) {
        boolean process = service.isEnableAllTransports();
        if (process) {
            return true;

        } else {
            List transports = service.getExposedTransports();
            for (int i=0; i<transports.size(); i++) {
                if (Constants.TRANSPORT_JMS.equals(transports.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the JMS destination used by this service
     * @param service the Axis Service
     * @return the name of the JMS destination
     */
    public static String getDestination(AxisService service) {
        Parameter destParam = service.getParameter(JMSConstants.DEST_PARAM);

        // validate destination
        String destination = null;
        if (destParam != null) {
            destination = (String) destParam.getValue();
        } else {
            destination = service.getName();
        }
        return destination;
    }


    /**
     * Extract connection factory properties from a given URL
     * @param url a JMS URL of the form jms:/<destination>?[<key>=<value>&]*
     * @return a Hashtable of extracted properties
     */
    public static Hashtable getProperties(String url) {
        Hashtable h = new Hashtable();
        int propPos = url.indexOf("?");
        if (propPos != -1) {
            StringTokenizer st = new StringTokenizer(url.substring(propPos+1), "&");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int sep = token.indexOf("=");
                if (sep != -1) {
                    h.put(token.substring(0, sep), token.substring(sep+1));
                } else {
                    continue; // ignore, what else can we do?
                }
            }
        }
        return h;
    }

    /**
     * Marks the given service as faulty with the given comment
     * @param serviceName service name
     * @param msg comment for being faulty
     * @param axisCfg configuration context
     */
    public static void markServiceAsFaulty(String serviceName, String msg,
                                           AxisConfiguration axisCfg ) {
        if (serviceName != null) {
            try {
                AxisService service = axisCfg.getService(serviceName);
                axisCfg.getFaultyServices().put(service.getName(), msg);

            } catch (AxisFault axisFault) {
                log.warn("Error marking service : " + serviceName +
                         " as faulty due to : " + msg, axisFault);
            }
        }
    }

    /**
     * Get an InputStream to the message
     * @param message the JMS message
     * @return an InputStream
     */
    public static InputStream getInputStream(Message message) {

        try {
            // get the incoming msg content into a byte array
            if (message instanceof BytesMessage) {
                byte[] buffer = new byte[8 * 1024];
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                BytesMessage byteMsg = (BytesMessage) message;
                for (int bytesRead = byteMsg.readBytes(buffer); bytesRead != -1;
                     bytesRead = byteMsg.readBytes(buffer)) {
                    out.write(buffer, 0, bytesRead);
                }
                return new ByteArrayInputStream(out.toByteArray());

            } else if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;
                String contentType = message.getStringProperty(JMSConstants.CONTENT_TYPE);
                if (contentType != null) {
                    return
                        new ByteArrayInputStream(
                            txtMsg.getText().getBytes(
                            Builder.getCharSetEncoding(contentType)));
                } else {
                    return
                        new ByteArrayInputStream(txtMsg.getText().getBytes());
                }

            } else {
                handleException("Unsupported JMS message type : " +
                                message.getClass().getName());
            }


        } catch (JMSException e) {
            handleException("JMS Exception getting InputStream into message", e);
        } catch (UnsupportedEncodingException e) {
            handleException("Encoding exception getting InputStream into message", e);
        }
        return null;
    }

    /**
     * Get a String property from the JMS message
     * @param message JMS message
     * @param property property name
     * @return property value
     */
    public static String getProperty(Message message, String property) {
        try {
            return message.getStringProperty(property);
        } catch (JMSException e) {
            return null;
        }
    }

    /**
     * Get the context type from the Axis MessageContext
     * @param msgCtx message context
     * @return the content type
     */
    public static String getContentType(MessageContext msgCtx) {
        OMOutputFormat format = new OMOutputFormat();
        String soapActionString = getSOAPAction(msgCtx);
        String charSetEnc = (String) msgCtx.getProperty(
            Constants.Configuration.CHARACTER_SET_ENCODING);

        if (charSetEnc != null) {
            format.setCharSetEncoding(charSetEnc);
        } else {
            OperationContext opctx = msgCtx.getOperationContext();
            if (opctx != null) {
                charSetEnc = (String) opctx.getProperty(
                    Constants.Configuration.CHARACTER_SET_ENCODING);
            }
        }

        // If the char set enc is still not found use the default
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
        if (!msgCtx.isSOAP11() && soapActionString != null &&
            !"".equals(soapActionString.trim())) {
            contentType = contentType + ";action=\"" + soapActionString + "\";";
        }

        return contentType;
    }

    /**
     * Get the SOAP Action from the message context
     * @param msgCtx the MessageContext
     * @return the SOAP Action as s String if present, or the WS-Action
     */
    private static String getSOAPAction(MessageContext msgCtx) {
        String soapActionString = msgCtx.getSoapAction();

        if (soapActionString == null || soapActionString.trim().length() == 0) {
            soapActionString = msgCtx.getWSAAction();
        }

        Object disableSoapAction =
            msgCtx.getOptions().getProperty(Constants.Configuration.DISABLE_SOAP_ACTION);
        
        if (soapActionString == null || JavaUtils.isTrueExplicitly(disableSoapAction)) {
            soapActionString = "";
        }

        return soapActionString;
    }

    /**
     * Return the destination name from the given URL
     * @param url the URL
     * @return the destination name
     */
    public static String getDestination(String url) {
        String tempUrl = url.substring(JMSConstants.JMS_PREFIX.length());
        int propPos = tempUrl.indexOf("?");

        if (propPos == -1) {
            return tempUrl;
        } else {
            return tempUrl.substring(0, propPos);
        }
    }

    /**
     * Return a SOAPEnvelope created from the given JMS Message and Axis
     * MessageContext, and the InputStream into the message
     * @param message the JMS Message
     * @param msgContext the Axis MessageContext
     * @param in the InputStream into the message
     * @return SOAPEnvelope for the message
     * @throws javax.xml.stream.XMLStreamException
     */
    public static SOAPEnvelope getSOAPEnvelope(
        Message message, MessageContext msgContext, InputStream in)
        throws XMLStreamException {

        SOAPEnvelope envelope = null;
        StAXBuilder builder;
        String contentType = JMSUtils.getProperty(message, JMSConstants.CONTENT_TYPE);

        if (contentType != null && contentType.indexOf(
                HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED) > -1) {
            builder = Builder.getAttachmentsBuilder(
                    msgContext, in, contentType, true);
            envelope = (SOAPEnvelope) builder.getDocumentElement();
        } else {
            String charSetEnc = Builder.getCharSetEncoding(contentType);
            String soapNS = Builder.getEnvelopeNamespace(contentType);
            builder = Builder.getSOAPBuilder(in, charSetEnc, soapNS);

            // Set the encoding scheme in the message context
            msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);
        }
        envelope = (SOAPEnvelope) builder.getDocumentElement();

        String charEncOfMessage = builder.getCharsetEncoding();
        String charEncOfTransport = ((String) msgContext.getProperty(
            Constants.Configuration.CHARACTER_SET_ENCODING));

        if (charEncOfMessage != null &&
            !(charEncOfMessage.trim().length() == 0) &&
            !charEncOfMessage.equalsIgnoreCase(charEncOfTransport)) {

            String faultCode;

            if (envelope.getNamespace() != null &&
                SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.
                    equals(envelope.getNamespace().getNamespaceURI())) {
                faultCode = SOAP12Constants.FAULT_CODE_SENDER;
            } else {
                faultCode = SOAP11Constants.FAULT_CODE_SENDER;
            }

            handleException(
                "Character Set Encoding from transport information do not " +
                "match with character set encoding in the received " +
                "SOAP message");
        }
        return envelope;
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
