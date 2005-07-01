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
package org.apache.axis2.transport;

import org.apache.axis2.engine.AxisFault;

import javax.mail.*;
import java.util.Properties;

/**
 * @author hemapani
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EmailReceiver {

    private String user;
    private String host;
    private String popPort;
    private String password;
    private Store store;
    private Folder inbox;

    public EmailReceiver(String user, String host, String popPort, String password) {
        this.user = user;
        this.host = host;
        this.popPort = popPort;
        this.password = password;
    }
    
    
    public void connect() throws AxisFault{
        try {
            final PasswordAuthentication authentication =
                new PasswordAuthentication(user, password);
            Properties props = new Properties();
            props.put("mail.user", user);
            props.put("mail.host", host);
            props.put("mail.store.protocol", "pop3");
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.pop3.port", popPort);
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return authentication;
                }
            });

            store = session.getStore();
            store.connect();
            Folder root = store.getDefaultFolder();
            inbox = root.getFolder("inbox");


        } catch (NoSuchProviderException e) {
            throw new AxisFault(e);
        } catch (MessagingException e) {
            throw new AxisFault(e);
        }
    
    }
    
    public void disconnect() throws AxisFault{
        try {
            inbox.close(true);
            store.close();
        } catch (MessagingException e) {
            throw new AxisFault(e);
        }
    }
    

    public Message[] receive() throws AxisFault {
        try{
            inbox.open(Folder.READ_WRITE);
            Message[] msgs = inbox.getMessages();

            int numMessages = msgs.length;
            if (msgs.length == 0) {
                return null;
            } else {
                return msgs;
            }

        } catch (NoSuchProviderException e) {
            throw new AxisFault(e);
        } catch (MessagingException e) {
            throw new AxisFault(e);
        }
    }

}
