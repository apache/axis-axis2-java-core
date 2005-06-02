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
 *
 *  Runtime state of the engine
 */
package org.apache.axis.transport.mail;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Properties;

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

import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.EmailReceiver;

/**
 * @author hemapani
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EMailSender {
    private String user;
    private String host;
    private String smtpPort;
    private String password;

    public EMailSender(String user, String host, String smtpPort, String password) {
        this.user = user;
        this.host = host;
        this.smtpPort = smtpPort;
        this.password = password;
    }

    public void send(String subject, String targetEmail, String message) throws AxisFault {
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

            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(targetEmail));
            msg.setSubject(subject);
            msg.setText(message);
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

        sender.send("Testing mail sending", "hemapani@127.0.0.1", "Hellp, testing");

        EmailReceiver receiver = new EmailReceiver(user, host, "110", password);
        receiver.connect();
        Message[] msgs = receiver.receive();
        if (msgs != null) {
            for (int i = 0; i < msgs.length; i++) {
                MimeMessage msg = (MimeMessage) msgs[i];
                if (msg != null) {
                    System.out.println(msg.getSender());
                    System.out.println(msg.getContent());
                }
                msg.setFlag(Flags.Flag.DELETED, true);
            }

        }
        receiver.disconnect();

    }
}
