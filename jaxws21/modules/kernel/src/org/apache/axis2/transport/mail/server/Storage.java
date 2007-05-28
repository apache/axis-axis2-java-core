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

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Hashtable;

public class Storage {
    private Hashtable users = new Hashtable();

    public Storage() {
    }

    public void addMail(String user, MimeMessage mail) {
        ArrayList curMailBox = null;

        if (users.containsKey(user)) {
            curMailBox = (ArrayList) users.get(user);
        } else {
            curMailBox = new ArrayList();
            users.put(user, curMailBox);
        }

        curMailBox.add(mail);
    }

    public ArrayList popUserMails(String user) {
        ArrayList usrMailBox =
                new ArrayList();    // This will return a emty list when the user has no mails or no mail box.

        if (users.containsKey(user)) {
            usrMailBox = (ArrayList) users.get(user);
        }

        return usrMailBox;
    }
}
