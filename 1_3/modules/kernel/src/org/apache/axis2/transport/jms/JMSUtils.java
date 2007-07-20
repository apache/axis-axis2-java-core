/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.transport.jms;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.namespace.QName;
import javax.activation.DataHandler;
import java.io.*;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

public class JMSUtils {

    private static final Log log = LogFactory.getLog(JMSUtils.class);

    /**
     * Should this service be enabled on JMS transport?
     *
     * @param service the Axis service
     * @return true if JMS should be enabled
     */
    public static boolean isJMSService(AxisService service) {
        boolean process = service.isEnableAllTransports();
        if (process) {
            return true;

        } else {
            List transports = service.getExposedTransports();
            for (int i = 0; i < transports.size(); i++) {
                if (Constants.TRANSPORT_JMS.equals(transports.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the JMS destination used by this service
     *
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
     *
     * @param url a JMS URL of the form jms:/<destination>?[<key>=<value>&]*
     * @return a Hashtable of extracted properties
     */
    public static Hashtable getProperties(String url) {
        Hashtable h = new Hashtable();
        int propPos = url.indexOf("?");
        if (propPos != -1) {
            StringTokenizer st = new StringTokenizer(url.substring(propPos + 1), "&");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int sep = token.indexOf("=");
                if (sep != -1) {
                    h.put(token.substring(0, sep), token.substring(sep + 1));
                } else {
                    continue; // ignore, what else can we do?
                }
            }
        }
        return h;
    }

    /**
     * Marks the given service as faulty with the given comment
     *
     * @param serviceName service name
     * @param msg         comment for being faulty
     * @param axisCfg     configuration context
     */
    public static void markServiceAsFaulty(String serviceName, String msg,
                                           AxisConfiguration axisCfg) {
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
     *
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
                                            BuilderUtil.getCharSetEncoding(contentType)));
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
     *
     * @param message  JMS message
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
     *
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
     *
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
     *
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
     *
     * @param message    the JMS Message
     * @param msgContext the Axis MessageContext
     * @param in         the InputStream into the message
     * @return SOAPEnvelope for the message
     * @throws javax.xml.stream.XMLStreamException
     *
     */
    public static SOAPEnvelope getSOAPEnvelope(
            Message message, MessageContext msgContext, InputStream in)
            throws XMLStreamException {

        SOAPEnvelope envelope = null;
        StAXBuilder builder = null;
        String contentType = JMSUtils.getProperty(message, JMSConstants.CONTENT_TYPE);

        if (contentType != null) {
            if (contentType.indexOf(
                    HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED) > -1) {
                builder = BuilderUtil.getAttachmentsBuilder(
                        msgContext, in, contentType, true);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
            } else {
                String charSetEnc = BuilderUtil.getCharSetEncoding(contentType);
                builder = BuilderUtil.getSOAPBuilder(in, charSetEnc);

                // Set the encoding scheme in the message context
                msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
            }
        }

        // handle pure plain vanilla POX and binary content (non SOAP)
        if (builder == null) {
            SOAPFactory soapFactory = new SOAP11Factory();
            try {
                XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader
                    (in, MessageContext.DEFAULT_CHAR_SET_ENCODING);

                // Set the encoding scheme in the message context
                msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING,
                                       MessageContext.DEFAULT_CHAR_SET_ENCODING);
                builder = new StAXOMBuilder(xmlreader);
                builder.setOMBuilderFactory(soapFactory);

                String ns = builder.getDocumentElement().getNamespace().getNamespaceURI();
                if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(ns)) {
                    envelope = getEnvelope(in, SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
                } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(ns)) {
                    envelope = getEnvelope(in, SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
                } else {
                    // this is POX ... mark MC as REST
                    msgContext.setDoingREST(true);
                    envelope = soapFactory.getDefaultEnvelope();
                    envelope.getBody().addChild(builder.getDocumentElement());
                }
            } catch (Exception e) {
                log.debug("Non SOAP/XML JMS message received");

                Parameter operationParam = msgContext.getAxisService().
                    getParameter(JMSConstants.OPERATION_PARAM);
                QName operationQName = (operationParam != null ?
                    getQName(operationParam.getValue()) : JMSConstants.DEFAULT_OPERATION);

                AxisOperation operation = msgContext.getAxisService().getOperation(operationQName);
                if (operation != null) {
                    msgContext.setAxisOperation(operation);
                } else {
                    handleException("Cannot find operation : " + operationQName + " on the service "
                        + msgContext.getAxisService());
                }

                Parameter wrapperParam = msgContext.getAxisService().
                    getParameter(JMSConstants.WRAPPER_PARAM);
                QName wrapperQName = (wrapperParam != null ?
                    getQName(wrapperParam.getValue()) : JMSConstants.DEFAULT_WRAPPER);

                OMElement wrapper = soapFactory.createOMElement(wrapperQName, null);

                try {
                    if (message instanceof TextMessage) {
                        OMTextImpl textData = (OMTextImpl) soapFactory.createOMText(
                            ((TextMessage) message).getText());
                        wrapper.addChild(textData);
                    } else if (message instanceof BytesMessage) {
                        BytesMessage bm = (BytesMessage) message;
                        byte[] msgBytes = new byte[(int) bm.getBodyLength()];
                        bm.reset();
                        bm.readBytes(msgBytes);
                        DataHandler dataHandler = new DataHandler(
                            new ByteArrayDataSource(msgBytes));
                        OMText textData = soapFactory.createOMText(dataHandler, true);
                        wrapper.addChild(textData);
                        msgContext.setDoingMTOM(true);
                    } else {
                        handleException("Unsupported JMS Message format : " + message.getJMSType());
                    }
                    envelope = soapFactory.getDefaultEnvelope();
                    envelope.getBody().addChild(wrapper);

                } catch (JMSException j) {
                    handleException("Error wrapping JMS message into a SOAP envelope ", j);
                }
            }
        }

        String charEncOfMessage = builder == null ? null :
            builder.getDocument() == null ? null : builder.getDocument().getCharsetEncoding();
        String charEncOfTransport = ((String) msgContext.getProperty(
                Constants.Configuration.CHARACTER_SET_ENCODING));

        if (charEncOfMessage != null &&
                !(charEncOfMessage.trim().length() == 0) &&
                !charEncOfMessage.equalsIgnoreCase(charEncOfTransport)) {

            handleException(
                    "Character Set Encoding from transport information do not " +
                            "match with character set encoding in the received " +
                            "SOAP message");
        }
        return envelope;
    }

    private static SOAPEnvelope getEnvelope(InputStream in, String namespace) throws XMLStreamException {

        try {
            in.reset();
        } catch (IOException e) {
            throw new XMLStreamException("Error resetting message input stream", e);
        }
        XMLStreamReader xmlreader = StAXUtils.createXMLStreamReader
            (in, MessageContext.DEFAULT_CHAR_SET_ENCODING);
        StAXBuilder builder = new StAXSOAPModelBuilder(xmlreader, namespace);
        return (SOAPEnvelope) builder.getDocumentElement();
    }

    private static QName getQName(Object obj) {
        String value;
        if (obj instanceof QName) {
            return (QName) obj;
        } else {
            value = obj.toString();
        }
        int open = value.indexOf('{');
        int close = value.indexOf('}');
        if (close > open && open > -1 && value.length() > close) {
            return new QName(value.substring(open+1, close-open), value.substring(close+1));
        } else {
            return new QName(value);
        }
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
