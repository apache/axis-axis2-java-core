/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.axis2.transport.mail;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.AxisFault;
import org.apache.axis2.om.impl.llom.builder.StAXBuilder;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis2.util.threadpool.AxisWorker;
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

public class MailWorker implements AxisWorker {
    protected static Log log = LogFactory.getLog(MailWorker.class.getName());

    private String contentType = "text/xml";

    private ConfigurationContext reg = null;

    private MimeMessage mimeMessage;

    private Properties prop = new Properties();

    private Session session = Session.getDefaultInstance(prop, null);

    /**
     * Constructor for MailWorker
     *
     * @param server
     * @param mimeMessage
     */
    public MailWorker(MimeMessage mimeMessage, ConfigurationContext reg) {
        this.mimeMessage = mimeMessage;
        this.reg = reg;
    }

    /**
     * The main workhorse method.
     */
    public void doWork() {
        AxisEngine engine = new AxisEngine(reg);
        MessageContext msgContext = null;
        // create and initialize a message context
        try {
            TransportInDescription transportIn =
                reg.getAxisConfiguration().getTransportIn(new QName(Constants.TRANSPORT_MAIL));
            TransportOutDescription transportOut =
                reg.getAxisConfiguration().getTransportOut(new QName(Constants.TRANSPORT_MAIL));
            if (transportIn != null && transportOut != null) {
                //create Message Context
                msgContext = new MessageContext(reg, transportIn, transportOut);
                msgContext.setServerSide(true);
                msgContext.setProperty(MailConstants.CONTENT_TYPE, mimeMessage.getContentType());
                String soapAction = getMailHeader(MailConstants.HEADER_SOAP_ACTION);
                msgContext.setWSAAction(soapAction);
                msgContext.setSoapAction(soapAction);

                //Create Mail EPR, EPR is constructed using the format, foo@bar/axis2/services/echo and is constructed 
                //using the <to-email-address>/<email-subject>
                InternetAddress[] recepainets = (InternetAddress[]) mimeMessage.getAllRecipients();
                if (recepainets != null && recepainets.length > 0) {
                    String emailAddress = recepainets[0].getAddress();
                    String emailSubject = mimeMessage.getSubject();
                    EndpointReference to =
                        new EndpointReference(
                            AddressingConstants.WSA_TO,
                            emailAddress + "/" + (emailSubject != null ? emailSubject : ""));
                } else {
                    throw new AxisFault("No receptineist found in the Email");
                }
                //try to assume the reply to value
                InternetAddress[] replyToAs = (InternetAddress[]) mimeMessage.getAllRecipients();
                if (replyToAs != null && replyToAs.length > 0) {
                    String replyTo = replyToAs[0].getAddress();
                    if (replyTo != null) {
                        msgContext.setReplyTo(
                            new EndpointReference(AddressingConstants.WSA_REPLY_TO, replyTo));
                    }
                }

                //Create the SOAP Message
                //TODO This can we written better way, to use the streams better
                String message = mimeMessage.getContent().toString();
                ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(bais);
                StAXBuilder builder = new StAXSOAPModelBuilder(reader);
                SOAPEnvelope envelope = (SOAPEnvelope) builder.getDocumentElement();
                msgContext.setEnvelope(envelope);
                if (envelope.getBody().hasFault()) {
                    engine.receiveFault(msgContext);
                } else {
                    engine.receive(msgContext);
                }
            } else {
                throw new AxisFault("Unknown transport " + Constants.TRANSPORT_MAIL);
            }

        } catch (Exception e) {
            try {

                if (msgContext != null) {
                    MessageContext faultContext = engine.createFaultMessageContext(msgContext, e);
                    engine.sendFault(faultContext);
                } else {
                    log.error(e);
                    e.printStackTrace();
                }
            } catch (AxisFault e1) {
                log.error(e);
                e1.printStackTrace();
            }
        }

        /*
         * 
         * This part is ignored for the time being. CT 07-Feb-2005.
         * 
         * if (msgContext.getProperty(MessageContext.QUIT_REQUESTED) != null) { //
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