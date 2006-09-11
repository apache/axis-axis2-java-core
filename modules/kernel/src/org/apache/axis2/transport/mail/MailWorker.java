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


package org.apache.axis2.transport.mail;

import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

public class MailWorker implements Runnable {
    private static final Log log = LogFactory.getLog(MailWorker.class);
    private ConfigurationContext configContext = null;
    private MimeMessage mimeMessage;

    /**
     * Constructor for MailWorker
     *
     * @param mimeMessage
     * @param reg
     */
    public MailWorker(MimeMessage mimeMessage, ConfigurationContext reg) {
        this.mimeMessage = mimeMessage;
        this.configContext = reg;
    }

    /**
     * The main workhorse method.
     */
    public void run() {
        AxisEngine engine = new AxisEngine(configContext);
        MessageContext msgContext = null;
        // create and initialize a message context
        try {
            TransportInDescription transportIn =
                    configContext.getAxisConfiguration()
                            .getTransportIn(new QName(org.apache.axis2.Constants.TRANSPORT_MAIL));
            TransportOutDescription transportOut =
                    configContext.getAxisConfiguration()
                            .getTransportOut(new QName(org.apache.axis2.Constants.TRANSPORT_MAIL));
            if ((transportIn != null) && (transportOut != null)) {
                // create Message Context
                msgContext = new MessageContext();
                msgContext.setConfigurationContext(configContext);
                msgContext.setTransportIn(transportIn);
                msgContext.setTransportOut(transportOut);
                msgContext.setServerSide(true);
                msgContext.setProperty(Constants.CONTENT_TYPE, mimeMessage.getContentType());

                if (TransportUtils.getCharSetEncoding(mimeMessage.getContentType()) != null) {
                    msgContext.setProperty(
                            org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING,
                            TransportUtils.getCharSetEncoding(
                                    mimeMessage.getContentType()));
                } else {
                    msgContext.setProperty(
                            org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING,
                            MessageContext.DEFAULT_CHAR_SET_ENCODING);
                }

                msgContext.setIncomingTransportName(org.apache.axis2.Constants.TRANSPORT_MAIL);
                String soapAction = getMailHeader(Constants.HEADER_SOAP_ACTION);
                msgContext.setSoapAction(soapAction);
                if (mimeMessage.getSubject() != null) {
                    msgContext.setTo(new EndpointReference(mimeMessage.getSubject()));
                }

                // Create the SOAP Message
                // SMTP basically a text protocol, thus, following would be the optimal way to build the
                // SOAP11/12 body from it.
                String message = mimeMessage.getContent().toString();
                ByteArrayInputStream bais =
                        new ByteArrayInputStream(message.getBytes());
                XMLStreamReader reader =
                        StAXUtils.createXMLStreamReader(bais);
                String soapNamespaceURI = "";
                if (mimeMessage.getContentType().indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE)
                    > -1) {
                    soapNamespaceURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
                } else if (mimeMessage.getContentType().indexOf(
                        SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
                    soapNamespaceURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
                } else {
                    log.warn(
                            "MailWorker found a message other than text/xml or application/soap+xml");
                    return;
                }

                StAXBuilder builder = new StAXSOAPModelBuilder(reader, soapNamespaceURI);
                SOAPEnvelope envelope = (SOAPEnvelope) builder.getDocumentElement();
                msgContext.setEnvelope(envelope);
                if (envelope.getBody().hasFault()) {
                    engine.receiveFault(msgContext);
                } else {
                    engine.receive(msgContext);
                }
            } else {
                throw new AxisFault(Messages.getMessage("unknownTransport",
                                                        org.apache.axis2.Constants.TRANSPORT_MAIL));
            }
        } catch (Exception e) {
            try {
                if (msgContext != null) {
                    if (msgContext.isServerSide()) {
                        MessageContext faultContext =
                                engine.createFaultMessageContext(msgContext, e);
                        engine.sendFault(faultContext);
                    }
                } else {
                    log.error(e);
                }
            } catch (AxisFault e1) {
                log.error(e);
            }
        }
        
    }

    private String getMailHeader(String headerName) throws AxisFault {
        try {
            String values[] = mimeMessage.getHeader(headerName);

            if (values != null) {
                return values[0];
            } else {
                return null;
            }
        } catch (MessagingException e) {
            throw new AxisFault(e);
        }
    }
}
