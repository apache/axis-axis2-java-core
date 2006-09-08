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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.mail.server.MailSrvConstants;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EMailSender {
    private String host;
    private String password;
    private String smtpPort;
    private String user;
    private MessageContext messageContext;

    public EMailSender(String user, String host, String smtpPort, String password) {
        this.user = user;
        this.host = host;
        this.smtpPort = smtpPort;
        this.password = password;
    }

    public void setMessageContext(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    public static void main(String[] args) throws Exception {
        String user = "hemapani";
        String host = "127.0.0.1";
        String smtpPort = "25";
        String password = "hemapani";
        EMailSender sender = new EMailSender(user, host, smtpPort, password);
        OMOutputFormat format = new OMOutputFormat();

        sender.send("Testing mail sending", "hemapani@127.0.0.1", "Hellp, testing", format);

        EmailReceiver receiver = new EmailReceiver(user, host, "110", password);

        receiver.connect();

        Message[] msgs = receiver.receive();

        if (msgs != null) {
            for (int i = 0; i < msgs.length; i++) {
                MimeMessage msg = (MimeMessage) msgs[i];

                if (msg != null) {
                }

                msg.setFlag(Flags.Flag.DELETED, true);
            }
        }

        receiver.disconnect();
    }

    public void send(String subject, String targetEmail, String message, OMOutputFormat format)
            throws AxisFault {
        try {
            final PasswordAuthentication authentication =
                    new PasswordAuthentication(user, password);
            Properties props = new Properties();

            props.put("mail.user", user);
            props.put("mail.host", host);
            props.put("mail.store.protocol", "pop3");
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.port", smtpPort);

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return authentication;
                }
            });
            MimeMessage msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress((user)));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(targetEmail));
            msg.setSubject(subject);

            String contentType = format.getContentType() != null ? format.getContentType() :
                                 MailSrvConstants.DEFAULT_CONTENT_TYPE;
            if (contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
                if (messageContext.getSoapAction() != null) {
                    msg.setHeader(MailSrvConstants.HEADER_SOAP_ACTION,
                                  messageContext.getSoapAction());
                    msg.setHeader("Content-Transfer-Encoding", "QUOTED-PRINTABLE");
                }
            }
            if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
                if (messageContext.getSoapAction() != null) {
                    msg.setContent(message,
                                   contentType + "; charset=" + format.getCharSetEncoding() +
                                   " ; action=" + messageContext.getSoapAction());
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
