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

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import java.util.Properties;

public class EmailReceiver {

    private URLName urlName;
    private Properties pop3Properties;
    private Folder folder;
    /* This store could be either POP3Store or POP3SSLStore */
    private Store store;


    public EmailReceiver() {
    }

    public void setUrlName(URLName urlName) {
        this.urlName = urlName;
    }

    public void setPop3Properties(Properties pop3Properties) {
        this.pop3Properties = pop3Properties;
    }

    public void connect() throws AxisFault {
        try {

            Session session = Session.getInstance(pop3Properties, null);
            store = session.getStore(urlName);

            store.connect();

            folder = store.getDefaultFolder();

            folder = folder.getFolder("inbox");
        } catch (NoSuchProviderException e) {
            throw new AxisFault(e);
        } catch (MessagingException e) {
            throw new AxisFault(e);
        }
    }

    public void disconnect() throws AxisFault {
        try {
            folder.close(true);
            store.close();
        } catch (MessagingException e) {
            throw new AxisFault(e);
        }
    }

    public Message[] receiveMessages() throws AxisFault {
        try {
            folder.open(Folder.READ_WRITE);

            Message[] msgs = folder.getMessages();

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
