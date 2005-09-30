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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

public class SMTPWorker extends Thread {

    private BufferedReader reader = null;

    private BufferedWriter writer = null;

    private boolean actAsMailet = false;

    private ArrayList receivers = new ArrayList();

    private Storage st = null;

    boolean runThread = true;

    private MimeMessage mail = null;

    private ConfigurationContext configurationContext = null;

    private String temp = "";

    private boolean dataWriting = false;

    private boolean transmitionEnd = false;

    private boolean bodyData = false;
    private Log log = LogFactory.getLog(getClass());

    public SMTPWorker(Socket socket, Storage st,
            ConfigurationContext configurationContext) {
        doWork(socket, st, configurationContext);
    }

    public SMTPWorker(Socket socket, Storage st) {
        doWork(socket, st, null);
    }

    private void doWork(Socket socket, Storage st,
            ConfigurationContext configurationContext) {
        try {
            this.st = st;
            if (configurationContext == null) {
                actAsMailet = false;
            } else {
                this.configurationContext = configurationContext;
                actAsMailet = true;
            }
            //get the streams from the socket and save in instance variables.
            reader = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket
                    .getOutputStream()));
        } catch (IOException ex) {
            log.info(ex.getMessage());
        }
    }

    //running the thread
    public void run() {
        try {
            //do initial transmission.
            initializeClient();

            //analyze all the inputs from client and work accordingly.
            while (runThread) {
                String input = null;

                //get client input
                input = reader.readLine();

                String retString = processInput(input);

                if (MailSrvConstants.COMMAND_EXIT.equals(retString)) {
                    exitWorker();
                } else {
                    if (retString != null) {
                        send(retString); //Send the reply
                    }
                    if (mail != null && transmitionEnd) {
                        exitWorker();
                    }
                }
            }
            for (int idx = 0; idx < receivers.size(); idx++) {
                try {
                    MailSorter mSort = null;
                    if (actAsMailet) {
                        mSort = new MailSorter(this.st,
                                this.configurationContext);
                    } else {
                        mSort = new MailSorter(this.st, null);
                    }
                    mSort.sort((String) receivers.get(idx), new MimeMessage(
                            mail));
                } catch (MessagingException e1) {
                    log.info(e1.getMessage());
                    //e1.printStackTrace();
                }
            }
            //

        } catch (IOException e) {
            log.info("ERROR: CLIENT CLOSED THE SOCKET");
        }
    }

    private void send(String s) throws IOException {
        writer.write(s);
        writer.newLine();
        writer.flush();
    }

    private String processInput(String input) {
        byte[] CR_LF = new byte[] { 0x0D, 0x0A };
        if (input == null)
            return MailSrvConstants.COMMAND_UNKNOWN;
        if (mail != null && transmitionEnd)
            return MailSrvConstants.COMMAND_TRANSMISSION_END;

        if (input.startsWith("MAIL")) {
            mail = new MimeMessage(Session.getInstance(new Properties(),
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return null;
                        }
                    }));

            int start = input.indexOf("<") + 1;
            int end;

            if (start <= 0) {
                start = input.indexOf("FROM:") + 5;
                end = input.length();
            } else {
                end = input.indexOf(">");
            }

            String from = input.substring(start, end);

            if (from != null && !from.trim().equals("")) {
                //TODO this is an ugly hack to get the from address in. There
                // should be a better way to do this.
                MailAddress mailFrom[] = new MailAddress[1];
                mailFrom[0] = new MailAddress(from);
                try {
                    mail.addFrom(mailFrom);
                } catch (MessagingException e) {
                    log.info(e.getMessage());
                    // TODO Auto-generated catch block
                }
            }

            return MailSrvConstants.MAIL_OK;

        }
        if (input.startsWith("HELO")) {
            return MailSrvConstants.HELO_REPLY;

        } else if (input.startsWith("RCPT")) {

            String domain = MailSrvConstants.SERVER_DOMAIN;
            //System.out.println("RCPT:" + input);
            //temp += input + "\n"; TODO Check this
            int start = input.indexOf("<") + 1;
            int end;

            if (start <= 0) {
                start = input.indexOf("TO:") + 3;
                /*
                 * if(!input.endsWith(domain)){ System.out.println("ERROR: wrong
                 * donmain name"); return MailSrvConstants.RCPT_ERROR; }
                 */
            } else {
                /*
                 * if(!input.endsWith(domain + ">")){ System.out.println("ERROR:
                 * wrong donmain name"); return MailSrvConstants.RCPT_ERROR; }
                 */
            }

            end = input.indexOf(">");
            String toStr = input.substring(start, end);

            try {
                mail.addRecipient(Message.RecipientType.TO, new MailAddress(
                        toStr));
                receivers.add(toStr);
            } catch (MessagingException e) {
                log.info(e.getMessage());
                // TODO Auto-generated catch block
            }
            return MailSrvConstants.RCPT_OK;

        } else if (input.equalsIgnoreCase("DATA")) {
            dataWriting = true;
            return MailSrvConstants.DATA_START_SUCCESS;

        } else if (input.equalsIgnoreCase("QUIT")) {
            dataWriting = true;
            transmitionEnd = true;
            return MailSrvConstants.COMMAND_TRANSMISSION_END;

        } else if (input.equals(".")) {
            dataWriting = false;
            return MailSrvConstants.DATA_END_SUCCESS;
        } else if (input.equals("") && !bodyData) {
            bodyData = true;
            return null;
        } else if (mail != null && dataWriting) {
            try {
                if (bodyData) {
                    temp += input;
                    mail.setContent(temp, "text/plain");
                } else {
                    mail.addHeaderLine(input);
                }
            } catch (MessagingException e) {
                log.info(e.getMessage());
                // TODO Auto-generated catch block
            }
            return null;

        } else {
            return MailSrvConstants.COMMAND_UNKNOWN;
        }

    }

    //transmission is over. setting to exit
    private void exitWorker() throws IOException {
        reader.close();
        writer.close();
        runThread = false;
    }

    //initializing the client by sending the initial message.
    private void initializeClient() throws IOException {
        if (writer != null) {
            send("220 SMTP Server IS UP");
        }
    }
}
