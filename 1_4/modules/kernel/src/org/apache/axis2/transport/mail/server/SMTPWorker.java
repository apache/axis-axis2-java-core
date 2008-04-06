/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.transport.mail.server;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.mail.Constants;
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
    private boolean transmitionEnd = false;
    private String temp = "";
    private Storage st = null;
    boolean runThread = true;
    private ArrayList receivers = new ArrayList();
    private MimeMessage mail = null;
    private static final Log log = LogFactory.getLog(SMTPWorker.class);
    private boolean dataWriting = false;
    private ConfigurationContext configurationContext = null;
    private boolean bodyData = false;
    private boolean actAsMailet = false;

    public SMTPWorker(Socket socket, Storage st) {
        doWork(socket, st, null);
    }

    public SMTPWorker(Socket socket, Storage st, ConfigurationContext configurationContext) {
        doWork(socket, st, configurationContext);
    }

    private void doWork(Socket socket, Storage st, ConfigurationContext configurationContext) {
        try {
            this.st = st;

            if (configurationContext == null) {
                actAsMailet = false;
            } else {
                this.configurationContext = configurationContext;
                actAsMailet = true;
            }

            // get the streams from the socket and save in instance variables.
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException ex) {
            log.info(ex.getMessage());
        }
    }

    // transmission is over. setting to exit
    private void exitWorker() throws IOException {
        reader.close();
        writer.close();
        runThread = false;
    }

    // initializing the client by sending the initial message.
    private void initializeClient() throws IOException {
        if (writer != null) {
            send("220 SMTP Server IS UP");
        }
    }

    private String processInput(String input) {
        if (input == null) {
            return Constants.COMMAND_UNKNOWN;
        }

        if ((mail != null) && transmitionEnd) {
            return Constants.COMMAND_TRANSMISSION_END;
        }

        if (input.startsWith("MAIL")) {
            mail = new MimeMessage(Session.getInstance(new Properties(), new Authenticator() {
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

            if ((from != null) && from.trim().length() != 0) {

                // TODO this is an ugly hack to get the from address in. There
                // should be a better way to do this.
                MailAddress mailFrom[] = new MailAddress[1];

                mailFrom[0] = new MailAddress(from);

                try {
                    mail.addFrom(mailFrom);
                } catch (MessagingException e) {
                    log.info(e.getMessage());
                }
            }

            return Constants.MAIL_OK;
        }

        if (input.startsWith("HELO")) {
            return Constants.HELO_REPLY;
        } else if (input.startsWith("RCPT")) {

            int start = input.indexOf("<") + 1;
            int end;

            if (start <= 0) {
                start = input.indexOf("TO:") + 3;
                /*
                 * if(!input.endsWith(domain)){ System.out.println("ERROR: wrong
                 * donmain name"); return Constants.RCPT_ERROR; }
                 */
            } else {

                /*
                 * if(!input.endsWith(domain + ">")){ System.out.println("ERROR:
                 * wrong donmain name"); return Constants.RCPT_ERROR; }
                 */
            }

            end = input.indexOf(">");

            String toStr = input.substring(start, end);

            try {
                mail.addRecipient(Message.RecipientType.TO, new MailAddress(toStr));
                receivers.add(toStr);
            } catch (MessagingException e) {
                log.info(e.getMessage());
            }

            return Constants.RCPT_OK;
        } else if (input.equalsIgnoreCase("DATA")) {
            dataWriting = true;

            return Constants.DATA_START_SUCCESS;
        } else if (input.equalsIgnoreCase("QUIT")) {
            dataWriting = true;
            transmitionEnd = true;

            return Constants.COMMAND_TRANSMISSION_END;
        } else if (input.equals(".")) {
            dataWriting = false;

            return Constants.DATA_END_SUCCESS;
        } else if (input.length() == 0 && !bodyData) {
            bodyData = true;

            return null;
        } else if ((mail != null) && dataWriting) {
            try {
                if (bodyData) {
                    temp += input;
                    mail.setContent(temp, "text/xml"); //Since this is for axis2 :-)
                } else {
                    mail.addHeaderLine(input);
                }
            } catch (MessagingException e) {
                log.info(e.getMessage());
            }

            return null;
        } else {
            return Constants.COMMAND_UNKNOWN;
        }
    }

    // running the thread
    public void run() {
        try {

            // do initial transmission.
            initializeClient();

            // analyze all the inputs from client and work accordingly.
            while (runThread) {
                String input = null;

                // get client input
                input = reader.readLine();

                String retString = processInput(input);

                if (Constants.COMMAND_EXIT.equals(retString)) {
                    exitWorker();
                } else {
                    if (retString != null) {
                        send(retString);    // Send the reply
                    }

                    if ((mail != null) && transmitionEnd) {
                        exitWorker();
                    }
                }
            }

            for (int idx = 0; idx < receivers.size(); idx++) {
                try {
                    MailSorter mSort = null;

                    if (actAsMailet) {
                        mSort = new MailSorter(this.st, this.configurationContext);
                    } else {
                        mSort = new MailSorter(this.st, null);
                    }

                    mSort.sort((String) receivers.get(idx), new MimeMessage(mail));
                } catch (MessagingException e1) {
                    log.info(e1.getMessage());

                    // e1.printStackTrace();
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
}
