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
import org.apache.axis2.transport.EmailReceiver;
import org.apache.axis2.transport.mail.server.MailSrvConstants;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EMailSender {
    private String user;
    private String host;
    private String smtpPort;
    private String password;

    public EMailSender(String user,
                       String host,
                       String smtpPort,
                       String password) {
        this.user = user;
        this.host = host;
        this.smtpPort = smtpPort;
        this.password = password;
    }

    public void send(String subject, String targetEmail, String message, String charSet) throws AxisFault {
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
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(targetEmail));
            msg.setSubject(subject);

            if (charSet == null){
                charSet = MailSrvConstants.DEFAULT_CHAR_SET;
            }

            msg.addHeaderLine("Content-Type: " + MailSrvConstants.DEFAULT_CONTENT_TYPE + "; charset=" + charSet);

            msg.setText(message);
            msg.setHeader("Content-Transfer-Encoding", MailSrvConstants.DEFAULT_CHAR_SET_ENCODING);
            Transport.send(msg);
        } catch (AddressException e) {
            throw new AxisFault(e);
        } catch (MessagingException e) {
            throw new AxisFault(e);
        }
    }

    public static void main(String[] args) throws Exception {

        String user = "hemapani";
        String host = "127.0.0.1";
        String smtpPort = "25";
        String password = "hemapani";
        EMailSender sender = new EMailSender(user, host, smtpPort, password);

        sender.send("Testing mail sending",
                "hemapani@127.0.0.1",
                "Hellp, testing",  MailSrvConstants.DEFAULT_CHAR_SET);

        EmailReceiver receiver = new EmailReceiver(user,
                host,
                "110",
                password);
        receiver.connect();
        Message[] msgs = receiver.receive();
        if (msgs != null) {
            for (int i = 0; i < msgs.length; i++) {
                MimeMessage msg = (MimeMessage) msgs[i];
                if (msg != null) {
//                    System.out.println(msg.getSender());
//                    System.out.println(msg.getContent());
                }
                msg.setFlag(Flags.Flag.DELETED, true);
            }

        }
        receiver.disconnect();

    }
}
