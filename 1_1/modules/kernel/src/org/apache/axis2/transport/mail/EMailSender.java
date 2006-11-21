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

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.activation.MailcapCommandMap;
import javax.activation.CommandMap;
import java.util.Properties;

public class EMailSender {
    private Properties properties;
    private MessageContext messageContext;
    private PasswordAuthentication passwordAuthentication;

    static {
        //Initializing the proper mime types
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap(
                "application/soap+xml;;x-java-content-handler=com.sun.mail.handlers.text_xml");
        CommandMap.setDefaultCommandMap(mc);
    }

    public EMailSender() {
    }

    public void setMessageContext(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setPasswordAuthentication(PasswordAuthentication passwordAuthentication) {
        this.passwordAuthentication = passwordAuthentication;
    }

    public void send(String subject, String targetEmail, String message, OMOutputFormat format)
            throws AxisFault {
        try {

            Session session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return passwordAuthentication;
                }
            });
            MimeMessage msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress((passwordAuthentication.getUserName())));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(targetEmail));
            msg.setSubject(subject);

            String contentType = format.getContentType() != null ? format.getContentType() :
                                 Constants.DEFAULT_CONTENT_TYPE;
            if (contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
                if (messageContext.getSoapAction() != null) {
                    msg.setHeader(Constants.HEADER_SOAP_ACTION,
                                  messageContext.getSoapAction());
                    msg.setHeader("Content-Transfer-Encoding", "QUOTED-PRINTABLE");
                }
            }
            if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
                if (messageContext.getSoapAction() != null) {
                    msg.setContent(message,
                                   contentType + "; charset=" + format.getCharSetEncoding() +
                                   " ; action=\"" + messageContext.getSoapAction() + "\"");
                }
            } else {
                msg.setContent(message, contentType + "; charset=" + format.getCharSetEncoding());
            }
            Transport.send(msg);
        } catch (AddressException e) {
            throw new AxisFault(e);
        } catch (MessagingException e) {
            throw new AxisFault(e);
        }
    }
}
