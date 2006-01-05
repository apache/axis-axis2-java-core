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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.impl.llom.builder.StAXBuilder;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis2.transport.mail.server.MailSrvConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.Properties;

public class MailWorker implements Runnable {
    protected static Log log = LogFactory.getLog(MailWorker.class.getName());
    private ConfigurationContext configContext = null;
    private Properties prop = new Properties();
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
                    configContext.getAxisConfiguration().getTransportIn(new QName(Constants.TRANSPORT_MAIL));
            TransportOutDescription transportOut =
                    configContext.getAxisConfiguration().getTransportOut(new QName(Constants.TRANSPORT_MAIL));

            if ((transportIn != null) && (transportOut != null)) {

                // create Message Context
                msgContext = new MessageContext();
                msgContext.setConfigurationContext(configContext);
                msgContext.setTransportIn(transportIn);
                msgContext.setTransportOut(transportOut);
                msgContext.setServerSide(true);
                msgContext.setProperty(MailSrvConstants.CONTENT_TYPE, mimeMessage.getContentType());
                msgContext.setProperty(MessageContext.CHARACTER_SET_ENCODING,
                        mimeMessage.getEncoding());

                String soapAction = getMailHeader(MailSrvConstants.HEADER_SOAP_ACTION);

                msgContext.setWSAAction(soapAction);
                msgContext.setSoapAction(soapAction);

                // TODO add the transport Headers to the Message Context
                // Create Mail EPR, EPR is constructed using the format, foo@bar/axis2/services/echo and is constructed
                // using the <to-email-address>/<email-subject>
                InternetAddress[] recepainets = (InternetAddress[]) mimeMessage.getAllRecipients();

                if ((recepainets != null) && (recepainets.length > 0)) {
                    String emailAddress = recepainets[0].getAddress();
                    String emailSubject = mimeMessage.getSubject();
                    EndpointReference to = new EndpointReference(emailAddress + "/"
                            + ((emailSubject != null)
                            ? emailSubject
                            : ""));
                    //TODO: FixME: There's nothing happening here ??? 
                } else {
                    throw new AxisFault(Messages.getMessage("noRecep4Email"));
                }

                // try to assume the reply to value
                InternetAddress[] replyToAs = (InternetAddress[]) mimeMessage.getAllRecipients();

                if ((replyToAs != null) && (replyToAs.length > 0)) {
                    String replyTo = replyToAs[0].getAddress();

                    if (replyTo != null) {
                        msgContext.setReplyTo(new EndpointReference(replyTo));
                    }
                }

                // Create the SOAP Message
                // TODO This can we written better way, to use the streams better
                String message = mimeMessage.getContent().toString();
                ByteArrayInputStream bais =
                        new ByteArrayInputStream(message.getBytes());
                XMLStreamReader reader =
                        XMLInputFactory.newInstance().createXMLStreamReader(bais);
                String soapNamespaceURI = "";

                if (mimeMessage.getContentType().indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE)
                        > -1) {
                    soapNamespaceURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
                } else if (mimeMessage.getContentType().indexOf(
                        SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
                    soapNamespaceURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
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
                        Constants.TRANSPORT_MAIL));
            }
        } catch (Exception e) {
            try {
                if (msgContext != null) {
                    MessageContext faultContext = engine.createFaultMessageContext(msgContext, e);

                    engine.sendFault(faultContext);
                } else {
                    log.error(e);
                }
            } catch (AxisFault e1) {
                log.error(e);
            }
        }

        /*
         *
         * This part is ignored for the time being. CT 07-Feb-2005.
         *
         * if (msgContext.getProperty(MessageContext.QUIT_REQUESTED) != null) { /
         * why then, quit! try { server.stop(); } catch (Exception e) { } }
         */
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
