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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.impl.llom.builder.StAXBuilder;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPProcessingException;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis2.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.BytesMessage;
import javax.xml.namespace.QName;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * SimpleJMSWorker is a worker thread that processes messages that are
 * received by SimpleJMSListener. It creates a new message context, invokes
 * the server, and sends back response msg to the replyTo destination.
 */
public class SimpleJMSWorker implements Runnable {
    protected static Log log = LogFactory.getLog(SimpleJMSWorker.class.getName());
    private ConfigurationContext configurationContext;
    SimpleJMSListener listener;
    BytesMessage message;

    public SimpleJMSWorker(ConfigurationContext configurationContext, SimpleJMSListener listener,
                           BytesMessage message) {
        this.listener = listener;
        this.message = message;
        this.configurationContext = configurationContext;
    }

    public static void processJMSRequest(MessageContext msgContext, InputStream in,
                                         String contentType)
            throws AxisFault {
        boolean soap11 = false;

        try {
            msgContext.setServerSide(true);

            SOAPEnvelope envelope = null;
            StAXBuilder builder = null;

            if (contentType != null) {
                if (contentType.indexOf(HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED) > -1) {

                    // It is MTOM
                    builder = HTTPTransportUtils.selectBuilderForMIME(msgContext, in, contentType);
                    envelope = (SOAPEnvelope) builder.getDocumentElement();
                } else {
                    Reader reader = new InputStreamReader(in);
                    XMLStreamReader xmlreader;

                    // Figure out the char set encoding and create the reader

                    // If charset is not specified
                    if (TransportUtils.getCharSetEncoding(contentType) == null) {
                        xmlreader = XMLInputFactory.newInstance().createXMLStreamReader(in,
                                MessageContext.DEFAULT_CHAR_SET_ENCODING);

                        // Set the encoding scheme in the message context
                        msgContext.setProperty(MessageContext.CHARACTER_SET_ENCODING,
                                MessageContext.DEFAULT_CHAR_SET_ENCODING);
                    } else {

                        // get the type of char encoding
                        String charSetEnc = TransportUtils.getCharSetEncoding(contentType);

                        xmlreader = XMLInputFactory.newInstance().createXMLStreamReader(in,
                                charSetEnc);

                        // Setting the value in msgCtx
                        msgContext.setProperty(MessageContext.CHARACTER_SET_ENCODING, charSetEnc);
                    }

                    if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
                        soap11 = false;

                        // it is SOAP 1.2
                        builder =
                                new StAXSOAPModelBuilder(xmlreader,
                                        SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
                        envelope = (SOAPEnvelope) builder.getDocumentElement();
                    } else if (contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
                        soap11 = true;
                        builder =
                                new StAXSOAPModelBuilder(xmlreader,
                                        SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
                        envelope = (SOAPEnvelope) builder.getDocumentElement();
                    }
                }
            }

            String charsetEncoding = builder.getDocument().getCharsetEncoding();

            if ((charsetEncoding != null) && !"".equals(charsetEncoding)
                    && !((String) msgContext.getProperty(
                    MessageContext.CHARACTER_SET_ENCODING)).equalsIgnoreCase(charsetEncoding)) {
                String faultCode;

                if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(
                        envelope.getNamespace().getName())) {
                    faultCode = SOAP12Constants.FAULT_CODE_SENDER;
                } else {
                    faultCode = SOAP11Constants.FAULT_CODE_SENDER;
                }

                throw new AxisFault(
                        "Character Set Encoding from " + "transport information do not match with "
                                + "character set encoding in the received SOAP message", faultCode);
            }

            msgContext.setEnvelope(envelope);

            AxisEngine engine = new AxisEngine(msgContext.getConfigurationContext());

            if (envelope.getBody().hasFault()) {
                engine.receiveFault(msgContext);
            } else {
                engine.receive(msgContext);
            }
        } catch (SOAPProcessingException e) {
            throw new AxisFault(e);
        } catch (AxisFault e) {

            // rethrow
            throw e;
        } catch (OMException e) {
            throw new AxisFault(e);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError e) {
            throw new AxisFault(e);
        } catch (UnsupportedEncodingException e) {
            throw new AxisFault(e);
        } finally {
            if ((msgContext.getEnvelope() == null) && !soap11) {
                msgContext.setEnvelope(new SOAP12Factory().createSOAPEnvelope());
            }
        }
    }

    /**
     * This is where the incoming message is processed.
     */
    public void run() {
        InputStream in = null;

        try {

            // get the incoming msg content into a byte array
            byte[]                buffer = new byte[8 * 1024];
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            for (int bytesRead = message.readBytes(buffer); bytesRead != -1;
                 bytesRead = message.readBytes(buffer)) {
                out.write(buffer, 0, bytesRead);
            }

            in = new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.error(Messages.getMessage("exception00"), e);
            e.printStackTrace();

            return;
        }

        // if the incoming message has a contentType set,
        // pass it to my new Message
        String contentType = null;

        try {
            contentType = message.getStringProperty("contentType");
        } catch (Exception e) {
            log.error(Messages.getMessage("exception00"), e);
            e.printStackTrace();

            return;
        }

        // if the incoming message has a contentType set,
        // pass it to my new Message
        String soapAction = null;

        try {
            soapAction = message.getStringProperty("SOAPAction");
        } catch (Exception e) {
            log.error(Messages.getMessage("exception00"), e);
            e.printStackTrace();

            return;
        }

        MessageContext msgContext;

        try {
            TransportInDescription transportIn =
                    configurationContext.getAxisConfiguration().getTransportIn(
                            new QName(Constants.TRANSPORT_JMS));
            TransportOutDescription transportOut =
                    configurationContext.getAxisConfiguration().getTransportOut(
                            new QName(Constants.TRANSPORT_JMS));

            msgContext = new MessageContext(configurationContext, transportIn, transportOut);
            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                    new JMSOutTransportInfo(message.getJMSReplyTo(),
                            listener.getProperties()));
            msgContext.setTransportOut(transportOut);
            msgContext.setServerSide(true);
        } catch (Exception e) {
            log.error(Messages.getMessage("exception00"), e);
            e.printStackTrace();

            return;
        }

        msgContext.setServiceGroupContextId(UUIDGenerator.getUUID());

        if (soapAction != null) {
            msgContext.setSoapAction(soapAction);
        }

        try {
            processJMSRequest(msgContext, in, contentType);
        } catch (Exception e) {
            log.error(Messages.getMessage("exception00"), e);
            e.printStackTrace();

            return;
        }
    }
}
