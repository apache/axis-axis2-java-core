/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.transport.mail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.axis.context.ContextBuilder;
import org.apache.axis.context.EngineContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.EngineConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.pop3.POP3Client;
import org.apache.commons.net.pop3.POP3MessageInfo;

/**
 * This is a simple implementation of an SMTP/POP3 server for processing SOAP
 * requests via Apache's xml-axis. This is not intended for production use. Its
 * intended uses are for demos, debugging, and performance profiling.
 * 
 * @author Davanum Srinivas <dims@yahoo.com>
 * @author Rob Jellinghaus (robj@unrealities.com)
 * 
 * @author Chamil Thanthrimudalige <chamilt@gmail.com>Changes done to make the
 *         Class work inside Axis 2.
 */

/*
 * TODO ISSUES -- 1. Message.getMessage -- All messages are hardcoded in the
 * code till a replacement or a working verion of this is put into Axis 2. When
 * internationalization work is done this can be fixed. CT 15-Feb-2005
 *  
 */

public class SimpleMailListner implements Runnable {
    
    
    protected static Log log = LogFactory.getLog(SimpleMailListner.class.getName());

    private String host;

    private int port;

    private String userid;

    private String password;

    private static EngineContext er = null;

    public SimpleMailListner(String host, int port, String userid, String password,
            String dir) {
        this.host = host;
        this.port = port;
        this.userid = userid;
        this.password = password;
        try {
            ContextBuilder builder = new ContextBuilder();
            er = builder.buildEngineContext(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out
                    .println("Sleeping for a bit to let the engine start up.");
            Thread.sleep(9000);
        } catch (InterruptedException e1) {
            log.debug(e1.getMessage(), e1);
        }
    }

    // Are we doing threads?
    private static boolean doThreads = true;

    public void setDoThreads(boolean value) {
        doThreads = value;
    }

    public boolean getDoThreads() {
        return doThreads;
    }

    public String getHost() {
        return host;
    }

    // Axis server (shared between instances)
    // In axis2 AxisEngine gives the functionality of AxisServer in axis 1.
    private static AxisEngine myAxisEngine = null;

    //This is needed to create the AxisEngine. Have to find out how to get this
    // wrking in the class -- CT 07-Feb-2005.
    private static EngineConfiguration reg = null;

    protected static synchronized AxisEngine getAxisEngine() {
        if (myAxisEngine == null) {
            myAxisEngine = new AxisEngine();
        }
        return myAxisEngine;
    }

    // are we stopped?
    // latch to true if stop() is called
    private boolean stopped = false;

    /**
     * Accept requests from a given TCP port and send them through the Axis
     * engine for processing.
     */
    public void run() {
        // log.info(Message.getMessage("start00", "SimpleMailListner", host + ":" +
        // port)); TODO Issue #1 CT 07-Feb-2005.
        // Accept and process requests from the socket
        if (!stopped) {
            System.out
                    .println("Mail listner is being setup to listen to the address "
                            + userid + "@" + host + " On port " + port);
            log.info("Mail listner is being setup to listen to the address "
                    + userid + "@" + host + " On port " + port);
        }
        while (!stopped) {
            try {
                pop3.connect(host, port);
                pop3.login(userid, password);
                System.out.println("Checking for messages");
                log.info("Checking for messages");
                POP3MessageInfo[] messages = pop3.listMessages();
                if (messages != null && messages.length > 0) {
                    System.out.println("Found messages " + messages.length);
                    log.info("Found messages " + messages.length);
                    for (int i = 0; i < messages.length; i++) {
                        Reader reader = pop3
                                .retrieveMessage(messages[i].number);
                        if (reader == null) {
                            continue;
                        }

                        StringBuffer buffer = new StringBuffer();
                        BufferedReader bufferedReader = new BufferedReader(
                                reader);
                        int ch;
                        while ((ch = bufferedReader.read()) != -1) {
                            buffer.append((char) ch);
                        }
                        bufferedReader.close();
                        ByteArrayInputStream bais = new ByteArrayInputStream(
                                buffer.toString().getBytes());
                        Properties prop = new Properties();
                        Session session = Session
                                .getDefaultInstance(prop, null);

                        MimeMessage mimeMsg = new MimeMessage(session, bais);
                        pop3.deleteMessage(messages[i].number);
                        if (mimeMsg != null) {
                            MailWorker worker = new MailWorker(this, mimeMsg,
                                    er);
                            if (doThreads) {
                                Thread thread = new Thread(worker);
                                thread.setDaemon(true);
                                thread.start();
                            } else {
                                worker.run();
                            }
                        }
                    }
                }
            } catch (java.io.InterruptedIOException iie) {
                log.debug(
                        "InterruptedIOException error occured in the mail listner."
                                + iie.getMessage(), iie);
                System.out
                        .println("InterruptedIOException error occured in the mail listner."
                                + iie.getMessage());
            } catch (Exception e) {
                //log.debug(Messages.getMessage("exception00"), e); TODO Issue
                // #1 CT 07-Feb-2005.
                log.debug("An error occured when running the mail listner."
                        + e.getMessage(), e);
                System.out
                        .println("An error occured when running the mail listner."
                                + e.getMessage());
                break;
            }
            try {
                pop3.logout();
                pop3.disconnect();
                Thread.sleep(3000);
            } catch (Exception e) {
                //log.error(Messages.getMessage("exception00"), e); TODO Issue
                // #1 CT 07-Feb-2005.
                log.debug(
                        "An error occured when trying to disconnect from the Server."
                                + e.getMessage(), e);
                System.out
                        .println("An error occured when trying to disconnect from the Server."
                                + e.getMessage());
            }
        }

        log.info("Mail listner has been stoped.");
        System.out.println("Mail listner has been stoped.");
        //log.info(Messages.getMessage("quit00", "SimpleMailListner")); TODO Issue #1
        // CT 07-Feb-2005.

    }

    /**
     * POP3 connection
     */
    private POP3Client pop3;

    /**
     * Obtain the serverSocket that that SimpleMailListner is listening on.
     */
    public POP3Client getPOP3() {
        return pop3;
    }

    /**
     * Set the serverSocket this server should listen on. (note : changing this
     * will not affect a running server, but if you stop() and then start() the
     * server, the new socket will be used).
     */
    public void setPOP3(POP3Client pop3) {
        this.pop3 = pop3;
    }

    //CT 03-Feb-2005 I think it should be POP instead of HTTP
    /**
     * Start this server.
     * 
     * Spawns a worker thread to listen for HTTP requests.
     * 
     * @param daemon
     *            a boolean indicating if the thread should be a daemon.
     */
    public void start(boolean daemon) throws Exception {
        if (doThreads) {
            Thread thread = new Thread(this);
            thread.setDaemon(daemon);
            thread.start();
        } else {
            run();
        }
    }

    /**
     * Start this server as a NON-daemon.
     */
    public void start() throws Exception {
        start(false);
    }

    /**
     * Stop this server.
     * 
     * This will interrupt any pending accept().
     */
    public void stop() throws Exception {
        /*
         * Close the server socket cleanly, but avoid fresh accepts while the
         * socket is closing.
         */
        stopped = true;
        //log.info(Messages.getMessage("quit00", "SimpleMailListner")); TODO Issue #1
        // CT 07-Feb-2005.
        log.info("Quiting the mail listner");
    }

    /**
     * Server process.
     */
    public static void main(String args[]) {
        boolean optDoThreads = true;
        String optHostName = "localhost";
        boolean optUseCustomPort = false;
        int optCustomPortToUse = 0;
        String optDir = "/home/chamil/temp";
        String optUserName = "server";
        String optPassword = "server";
        System.out.println("Starting the mail listner");
        // Options object is not used for now. Hard coded values will be used.
        // TODO have to meke this a bit more generic. CT 07-Feb-2005.
        //Options opts = null;

        /*
         * try { opts = new Options(args); } catch (MalformedURLException e) {
         * log.error(Messages.getMessage("malformedURLException00"), e); return; }
         */
        try {
            doThreads = optDoThreads; //(opts.isFlagSet('t') > 0);
            String host = optHostName; //opts.getHost();
            int port = ((optUseCustomPort) ? optCustomPortToUse : 110);
            POP3Client pop3 = new POP3Client();
            SimpleMailListner sas = new SimpleMailListner(host, port, optUserName,
                    optPassword, optDir);
            sas.setPOP3(pop3);
            sas.start();
        } catch (Exception e) {
            // log.error(Messages.getMessage("exception00"), e); TODO Issue #1
            // CT 07-Feb-2005.
            log
                    .error("An error occured in the main method of SimpleMailListner. TODO Detailed error message needs to be inserted here.");
            return;
        }

    }
}