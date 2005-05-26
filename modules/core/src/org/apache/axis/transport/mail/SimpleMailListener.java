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

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.description.TransportInDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.TransportListener;
import org.apache.axis.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.pop3.POP3Client;

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

public class SimpleMailListener extends TransportListener implements Runnable {

    protected static Log log = LogFactory.getLog(SimpleMailListener.class.getName());

    private String host;

    private String port;

    private String user;

    private String password;

    private ConfigurationContext configurationContext = null;

    private String replyTo;

    public SimpleMailListener() {
    }

    public SimpleMailListener(
        String host,
        String port,
        String userid,
        String password,
        String dir) {
        this.host = host;
        this.port = port;
        this.user = userid;
        this.password = password;
        try {
            ConfigurationContextFactory builder = new ConfigurationContextFactory();
            configurationContext = builder.buildEngineContext(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Sleeping for a bit to let the engine start up.");
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            log.debug(e1.getMessage(), e1);
        }
    }

    public SimpleMailListener(
        String host,
        String port,
        String userid,
        String password,
        ConfigurationContext er) {
        this.host = host;
        this.port = port;
        this.user = userid;
        this.password = password;
        this.configurationContext = er;
    }

    // Are we doing threads?
    private static boolean doThreads = true;

    public void setDoThreads(boolean value) {
        doThreads = value;
    }

    public boolean getDoThreads() {
        return doThreads;
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
            String logMessage =
                "Mail listner is being setup to listen to the address "
                    + user
                    + "@"
                    + host
                    + " On port "
                    + port;
            System.out.println(logMessage);
            log.info(logMessage);
        }
        while (!stopped) {
            try {
                final PasswordAuthentication authentication = new PasswordAuthentication(user, password);
                Properties props = new Properties();
                props.put("mail.user", user);
                props.put("mail.host", host);
                props.put("mail.store.protocol", "pop3");
                props.put("mail.transport.protocol", "smtp");
                Session session = Session.getInstance(props,new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return authentication;
                    }
                });
                Store store = session.getStore();
                store.connect();
                Folder root = store.getDefaultFolder();
                Folder inbox = root.getFolder("inbox");
                inbox.open(Folder.READ_WRITE);
                Message[] msgs = inbox.getMessages();
                int numMessages = msgs.length;
                if (msgs.length == 0) {
                    System.out.println("No messages in inbox");
                }
                for (int i = 0; i < msgs.length; i++) {
                    MimeMessage msg = (MimeMessage) msgs[i];
                    if (msg != null) {
                        MailWorker worker = new MailWorker(msg, configurationContext);
                        if (doThreads) {
                            Thread thread = new Thread(worker);
                            thread.setDaemon(true);
                            thread.start();
                        } else {
                            worker.run();
                        }
                    }
                    msg.setFlag(Flags.Flag.DELETED, true);
                }
                inbox.close(true);
                store.close();
            } catch (Exception e) {
                //log.debug(Messages.getMessage("exception00"), e); TODO Issue
                // #1 CT 07-Feb-2005.
                log.debug("An error occured when running the mail listner." + e.getMessage(), e);
                System.out.println(
                    "An error occured when running the mail listner." + e.getMessage());
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
                    "An error occured when trying to disconnect from the Server." + e.getMessage(),
                    e);
                System.out.println(
                    "An error occured when trying to disconnect from the Server." + e.getMessage());
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
    public void start(boolean daemon) {
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
    public void start() {
        start(false);
    }

    /**
     * Stop this server.
     * 
     * This will interrupt any pending accept().
     */
    public void stop() {
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
            String port = String.valueOf(((optUseCustomPort) ? optCustomPortToUse : 110));
            POP3Client pop3 = new POP3Client();
            SimpleMailListener sas =
                new SimpleMailListener(host, port, optUserName, optPassword, optDir);
            sas.setPOP3(pop3);
            sas.start();
        } catch (Exception e) {
            // log.error(Messages.getMessage("exception00"), e); TODO Issue #1
            // CT 07-Feb-2005.
            log.error(
                "An error occured in the main method of SimpleMailListner. TODO Detailed error message needs to be inserted here.");
            return;
        }

    }
    /* (non-Javadoc)
     * @see org.apache.axis.transport.TransportListener#init(org.apache.axis.context.ConfigurationContext, org.apache.axis.description.TransportInDescription)
     */
    public void init(ConfigurationContext configurationContext, TransportInDescription transportIn)
        throws AxisFault {
        this.configurationContext = configurationContext;

        user = Utils.getParameterValue(transportIn.getParameter(MailConstants.POP3_USER));
        host = Utils.getParameterValue(transportIn.getParameter(MailConstants.POP3_HOST));
        password = Utils.getParameterValue(transportIn.getParameter(MailConstants.POP3_PASSWORD));
        port = Utils.getParameterValue(transportIn.getParameter(MailConstants.POP3_PORT));
        replyTo = Utils.getParameterValue(transportIn.getParameter(MailConstants.RAPLY_TO));
        if (user == null || host == null || password == null || port == null) {
            throw new AxisFault(
                "user, port, host or password not set, "
                    + "   [user null = "
                    + (user == null)
                    + ", password null= "
                    + (password == null)
                    + ", host null "
                    + (host == null)
                    + ",port null "
                    + (port == null));
        }

        POP3Client pop3 = new POP3Client();
        this.setPOP3(pop3);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.transport.TransportListener#replyToEPR(java.lang.String)
     */
    public EndpointReference replyToEPR(String serviceName) throws AxisFault {
        // TODO Auto-generated method stub
        return new EndpointReference(AddressingConstants.WSA_REPLY_TO, replyTo);
    }

}