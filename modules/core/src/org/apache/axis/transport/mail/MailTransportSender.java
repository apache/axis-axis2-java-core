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

package org.apache.axis.transport.mail;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.AbstractTransportSender;
import org.apache.axis.util.Utils;

public class MailTransportSender extends AbstractTransportSender {
    private String host;
    private String user;
    private String password;
    private String smtpPort = "25";

    private PipedInputStream in;

    public MailTransportSender() {

    }

    public void finalizeSendWithToAddress(MessageContext msgContext, Writer writer)
        throws AxisFault {

    }

    public void startSendWithToAddress(MessageContext msgContext, Writer writer) throws AxisFault {
        try {
            TransportOutDescription transportOut = msgContext.getTransportOut();
            user = Utils.getParameterValue(transportOut.getParameter(MailConstants.SMTP_USER));
            host = Utils.getParameterValue(transportOut.getParameter(MailConstants.SMTP_HOST));
            password =
                Utils.getParameterValue(transportOut.getParameter(MailConstants.SMTP_PASSWORD));
            smtpPort = Utils.getParameterValue(transportOut.getParameter(MailConstants.SMTP_PORT));
            if (user != null && host != null && password != null && smtpPort != null) {
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

                MimeMessage msg = new MimeMessage(session, in);
                String action = msgContext.getWSAAction();
                if (action != null) {
                    msg.setHeader("transport.mail.soapaction", action);
                }
                
                msg.addRecipient(Message.RecipientType.TO,  new InternetAddress(msgContext.getTo().getAddress()));
                msg.setSubject(msgContext.getTo().getAddress());
                Transport.send(msg);

            } else {
                throw new AxisFault(
                    "user, port, host or password not set, "
                        + "   [user null = "
                        + (user == null)
                        + ", password null= "
                        + (password == null)
                        + ", host null "
                        + (host == null)
                        + ",port null "
                        + (smtpPort == null));

            }
        } catch (MessagingException e) {
            throw new AxisFault(e);
        }

    }

    protected Writer openTheConnection(EndpointReference epr) throws AxisFault {

        try {

            in = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(in);

            return new OutputStreamWriter(out);
        } catch (IOException e) {
            throw new AxisFault(e);
        }

    }

    //Output Stream based cases are not supported 
    public void startSendWithOutputStreamFromIncomingConnection(
        MessageContext msgContext,
        Writer writer)
        throws AxisFault {
        throw new UnsupportedOperationException();

    }
    public void finalizeSendWithOutputStreamFromIncomingConnection(
        MessageContext msgContext,
        Writer writer)
        throws AxisFault {
    }
    /* (non-Javadoc)
     * @see org.apache.axis.transport.TransportSender#cleanUp()
     */
    public void cleanUp() throws AxisFault {
        // TODO Auto-generated method stub

    }

}
