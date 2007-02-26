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

package org.apache.axis2.transport.mail.server;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.namespace.QName;

import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.mail.Constants;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class will be used to sort the messages into normal messages and mails
 * being sent to the Axis engine. If a mail is to be sent to the engine then a
 * new Axis engine is created using the configuration in the MailServer class
 * and the receive method is called.
 */
public class MailSorter {
    private static final Log log = LogFactory.getLog(MailSorter.class);
    Storage st = null;
    private ArrayList sUsers = new ArrayList();

    // Special users. They are hard coded for the time being to axis2-server@localhost and axis2-server@127.0.0.1
    private ConfigurationContext configurationContext = null;
    private boolean actAsMailet = false;

    public MailSorter(Storage st, ConfigurationContext configurationContext) {
        this.st = st;
        sUsers.add("axis2-server@localhost");
        sUsers.add("axis2-server@127.0.0.1");

        if (configurationContext == null) {
            actAsMailet = false;
        } else {
            this.configurationContext = configurationContext;
            actAsMailet = true;
        }
    }

    public void processMail(ConfigurationContext confContext, MimeMessage mimeMessage) {
        // create an Axis server
        AxisEngine engine = new AxisEngine(confContext);
        MessageContext msgContext = null;

        // create and initialize a message context
        try {
            msgContext = ContextFactory.createMessageContext(confContext);
            msgContext.setTransportIn(confContext.getAxisConfiguration().getTransportIn(new QName(org.apache.axis2.Constants.TRANSPORT_MAIL)));
            msgContext.setTransportOut(confContext.getAxisConfiguration().getTransportOut(new QName(org.apache.axis2.Constants.TRANSPORT_MAIL)));

            msgContext.setServerSide(true);
            msgContext.setProperty(Constants.CONTENT_TYPE, mimeMessage.getContentType());
            msgContext.setProperty(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING,
                    mimeMessage.getEncoding());
            String soapAction = getMailHeader(Constants.HEADER_SOAP_ACTION, mimeMessage);
            if (soapAction == null){
                soapAction = mimeMessage.getSubject();
            }

            msgContext.setSoapAction(soapAction);
            msgContext.setIncomingTransportName(org.apache.axis2.Constants.TRANSPORT_MAIL);

            String serviceURL = mimeMessage.getSubject();

            if (serviceURL == null) {
                serviceURL = "";
            }

            String replyTo = ((InternetAddress) mimeMessage.getReplyTo()[0]).getAddress();

            if (replyTo != null) {
                msgContext.setReplyTo(new EndpointReference(replyTo));
            }

            String recepainets = ((InternetAddress) mimeMessage.getAllRecipients()[0]).getAddress();

            if (recepainets != null) {
                msgContext.setTo(new EndpointReference(recepainets + "/" + serviceURL));
            }

            // add the SOAPEnvelope
            String message = mimeMessage.getContent().toString();

            log.info("message[" + message + "]");

            ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());
            String soapNamespaceURI = "";

            if (mimeMessage.getContentType().indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
                soapNamespaceURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            } else if (mimeMessage.getContentType().indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE)
                    > -1) {
                soapNamespaceURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            }

            StAXBuilder builder = BuilderUtil.getSOAPBuilder(bais, soapNamespaceURI);

            SOAPEnvelope envelope = (SOAPEnvelope) builder.getDocumentElement();

            msgContext.setEnvelope(envelope);

            if (envelope.getBody().hasFault()) {
                engine.receiveFault(msgContext);
            } else {
                engine.receive(msgContext);
            }
        } catch (Exception e) {
            try {
                if (msgContext != null) {
                    MessageContext faultContext = MessageContextBuilder.createFaultMessageContext(msgContext, e);

                    engine.sendFault(faultContext);
                }
            } catch (Exception e1) {
                log.error(e);
            }
        }
    }

    public void sort(String user, MimeMessage msg) {
        if (actAsMailet) {
            if (sUsers.contains(user)) {
                processMail(configurationContext, msg);
            } else {
                st.addMail(user, msg);
            }
        } else {
            st.addMail(user, msg);
        }
    }

    private String getMailHeader(String headerName, MimeMessage mimeMessage) throws AxisFault {
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
