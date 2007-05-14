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

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Properties;

public class EMailSender {
    private Properties properties;
    private MessageContext messageContext;
    private PasswordAuthentication passwordAuthentication;
    private OutputStream outputStream;
    private String inReplyTo;
    private EndpointReference from;
    private OMOutputFormat format;

    protected static Log log = LogFactory.getLog(EMailSender.class);

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

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void setPasswordAuthentication(PasswordAuthentication passwordAuthentication) {
        this.passwordAuthentication = passwordAuthentication;
    }

    public void send()
            throws AxisFault {

        try {

            Session session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return passwordAuthentication;
                }
            });
            MimeMessage msg = new MimeMessage(session);


            EndpointReference epr = null;
            MailToInfo mailToInfo = null;

            if (messageContext.getTo() != null && !messageContext.getTo().hasAnonymousAddress()) {
                epr = messageContext.getTo();
            }

            if (epr != null) {
                if (!epr.hasNoneAddress()) {
                    mailToInfo = new MailToInfo(epr);
                    msg.addRecipient(Message.RecipientType.TO,
                                     new InternetAddress(mailToInfo.getEmailAddress()));

                } else {
                    if (from != null) {
                        mailToInfo = new MailToInfo(from);
                        msg.addRecipient(Message.RecipientType.TO,
                                         new InternetAddress(mailToInfo.getEmailAddress()));
                    } else {
                        String error = EMailSender.class.getName() + "Couldn't countinue due to" +
                                       " FROM addressing is NULL";
                        log.error(error);
                        throw new AxisFault(error);
                    }
                }
            } else {
                // replyto : from : or reply-path;
                if (from != null) {
                    mailToInfo = new MailToInfo(from);
                    msg.addRecipient(Message.RecipientType.TO,
                                     new InternetAddress(mailToInfo.getEmailAddress()));
                } else {
                    String error = EMailSender.class.getName() + "Couldn't countinue due to" +
                                   " FROM addressing is NULL and EPR is NULL";
                    log.error(error);
                    throw new AxisFault(error);
                }

            }

            msg.setSubject("__ Axis2/Java Mail Message __");

            if (mailToInfo.isxServicePath()) {
                msg.setHeader(Constants.X_SERVICE_PATH,
                              "\"" + mailToInfo.getContentDescription() + "\"");
            }

            if (inReplyTo != null) {
                msg.setHeader(Constants.IN_REPLY_TO, inReplyTo);
            }

            createMailMimeMessage(msg, mailToInfo, format);
            Transport.send(msg);

            sendReceive(messageContext, msg.getMessageID());
        } catch (AddressException e) {
            throw new AxisFault(e);
        } catch (MessagingException e) {
            throw new AxisFault(e);
        }
    }

    private void createMailMimeMessage(final MimeMessage msg, MailToInfo mailToInfo,
                                       OMOutputFormat format)
            throws MessagingException {

        // Create the message part
        BodyPart messageBodyPart = new MimeBase64BodyPart();
        messageBodyPart.setText("");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        DataSource source = null;

        // Part two is attachment
        if (outputStream instanceof ByteArrayOutputStream) {
            source = new ByteArrayDataSource(((ByteArrayOutputStream) outputStream).toByteArray());
        }
        messageBodyPart = new MimeBase64BodyPart();
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setDisposition(Part.ATTACHMENT);

        messageBodyPart
                .addHeader("Content-Description", "\"" + mailToInfo.getContentDescription() + "\"");

        String contentType = format.getContentType() != null ? format.getContentType() :
                             Constants.DEFAULT_CONTENT_TYPE;
        if (contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1) {
            if (messageContext.getSoapAction() != null) {
                messageBodyPart.setHeader(Constants.HEADER_SOAP_ACTION,
                                          messageContext.getSoapAction());
            }
        }

        if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
            if (messageContext.getSoapAction() != null) {
                messageBodyPart.setHeader("Content-Type",
                                          contentType + "; charset=" + format.getCharSetEncoding() +
                                          " ; action=\"" + messageContext.getSoapAction() +
                                          "\"");
            }
        } else {
            messageBodyPart.setHeader("Content-Type",
                                      contentType + "; charset=" + format.getCharSetEncoding());
        }

        multipart.addBodyPart(messageBodyPart);
        msg.setContent(multipart);

    }


    public void setInReplyTo(String inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public void setFrom(EndpointReference from) {
        this.from = from;
    }

    public void setFormat(OMOutputFormat format) {
        this.format = format;
    }

    private void sendReceive(MessageContext msgContext, String msgId) throws AxisFault {
        Object obj = msgContext.getProperty(Constants.MAIL_SYNC);
        if (obj == null) {
            return;
        }

        Options options = msgContext.getOptions();

        SynchronousMailListener listener =
                new SynchronousMailListener(options.getTimeOutInMilliSeconds());
        listener.sendReceive(msgContext, msgId);
    }
}
