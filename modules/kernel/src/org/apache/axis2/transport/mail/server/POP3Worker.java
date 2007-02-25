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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.axis2.transport.mail.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class POP3Worker extends Thread {
    private static final Log log = LogFactory.getLog(POP3Worker.class);
    boolean doneProcess = false;
    int numDeleted = 0;    // This is a small hack to get the deleting working with the ArrayList. To keep it simple.
    ArrayList messages = new ArrayList();
    private Socket socket;
    private Storage st;

    public POP3Worker(Socket socket, Storage st) {
        this.socket = socket;
        this.st = st;
    }

    private void processInput(String input, PrintWriter printWriter) {
        byte[] CR_LF_DOT_CR_LF = new byte[]{0x0D, 0x0A, '.', 0x0D, 0x0A};
        String user = "";

        if (input == null) {
            this.doneProcess = true;    // This should not be happening
        } else {
            ArrayList tokens = new ArrayList();
            StringTokenizer stk = new StringTokenizer(input);

            while (stk.hasMoreTokens()) {
                tokens.add(stk.nextToken());
            }

            if (tokens.get(0).equals(Constants.USER)) {
                user = (String) tokens.get(1);
                messages = st.popUserMails(user);
                printWriter.println(Constants.OK);
            } else if (tokens.get(0).equals(Constants.PASS)) {
                printWriter.println(Constants.OK);    // Passwords are not checked.
            } else if (input.equals(Constants.QUIT)) {
                printWriter.println(Constants.OK + "POP3 server signing off");
                doneProcess = true;
            } else if (input.equals(Constants.STAT)) {
                printWriter.println(Constants.OK + messages.size() + " 1");    // We take the maildrop size as one.
            } else if (tokens.get(0).equals(Constants.LIST)) {                               // scan listing
                if (tokens.size() > 1) {
                    try {
                        int optArg = Integer.parseInt((String) tokens.get(1));
                        int messageArrayIndex = optArg - 1;

                        if ((messageArrayIndex < messages.size()) && (messageArrayIndex >= 0))
                        {    // that is OK careful with numbering
                            printWriter.println(Constants.OK + messageArrayIndex + 1
                                    + " 120");    // Mail size of 120 is just some number.
                        } else {
                            printWriter.println(Constants.ERR + "no such message, only "
                                    + (messages.size() + 1) + " messages in maildrop");
                        }
                    } catch (NumberFormatException e) {
                        log.info(e.getMessage());
                        printWriter.println(Constants.ERR
                                + "problem passing the index. Index submited was "
                                + tokens.get(1));
                    }
                } else {
                    printWriter.println(Constants.OK + messages.size());

                    for (int i = 0; i < messages.size(); i++) {
                        int messageIndex = i + 1;

                        printWriter.println(messageIndex + " 120");    // List out all the messages with a message size octet of 120
                    }

                    printWriter.println(".");
                }
            } else if (tokens.get(0).equals(Constants.RETR)) {
                String i = (String) tokens.get(1);

                try {
                    int index = Integer.parseInt(i);

                    printWriter.println(Constants.OK);

                    MimeMessage m = (MimeMessage) messages.get(index - 1);

                    m.writeTo(socket.getOutputStream());

                    socket.getOutputStream().write(CR_LF_DOT_CR_LF);    // This is a bit of a hack to get it working. Have to find a bette way to handle this.
                    socket.getOutputStream().flush();
                } catch (NumberFormatException e) {
                    printWriter.println(Constants.ERR);
                } catch (IOException e1) {
                    printWriter.println(Constants.ERR);
                } catch (MessagingException e2) {
                    printWriter.println(Constants.ERR);
                }
            } else if (tokens.get(0).equals(Constants.DELE)) {
                String smIndex = (String) tokens.get(1);

                try {
                    int mIndex = Integer.parseInt(smIndex) - 1 - numDeleted;    // When one mail is deleted the index of the other mails will reduce. Asumed that the delete will occure from bottom up.

                    if ((mIndex >= 0) && (mIndex < messages.size())) {
                        messages.remove(mIndex);
                        numDeleted++;
                        printWriter.println(Constants.OK);
                    } else {
                        printWriter.println(Constants.ERR);
                    }
                } catch (NumberFormatException e) {
                    printWriter.println(Constants.ERR);
                }
            } else if (tokens.get(0).equals(Constants.NOOP)
                    || tokens.get(0).equals(Constants.RSET)) {
                printWriter.println(Constants.OK);
            } else {
                printWriter.println(Constants.ERR);
            }
        }
    }

    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            printWriter.println(Constants.OK + " POP3 server ready");

            String s;

            while (!doneProcess) {
                s = bufferedReader.readLine();
                processInput(s, printWriter);
            }

            socket.close();
        } catch (Exception e) {
            log.error(e);
        }
    }
}
