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
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.mail.server.MailSrvConstants;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import javax.xml.namespace.QName;
import java.io.File;

/**
 * This is a simple implementation of an SMTP/POP3 server for processing SOAP
 * requests via Apache's xml-axis2. This is not intended for production use. Its
 * intended uses are for demos, debugging, and performance profiling.
 */

/*
 * TODO ISSUES -- 1. Message.getMessage -- All messages are hardcoded in the
 * code till a replacement or a working verion of this is put into Axis 2. When
 * internationalization work is done this can be fixed. CT 15-Feb-2005
 *
 */
public class SimpleMailListener implements Runnable, TransportListener {
    protected static Log log = LogFactory.getLog(SimpleMailListener.class.getName());

    // Are we doing threads?
    private static boolean doThreads = true;
    private ConfigurationContext configurationContext = null;

    // are we stopped?
    // latch to true if stop() is called
    private boolean stopped = false;
    private String host;
    private String password;
    private String port;
    private String replyTo;
    private String user;

    public SimpleMailListener() {
    }

    public SimpleMailListener(String host, String port, String userid, String password,
                              ConfigurationContext er) {
        this.host = host;
        this.port = port;
        this.user = userid;
        this.password = password;
        this.configurationContext = er;
    }

    public SimpleMailListener(String host, String port, String userid, String password,
                              String dir) {
        this.host = host;
        this.port = port;
        this.user = userid;
        this.password = password;

        try {
            File repo = new File(dir);
            if (repo.exists()) {
                File axis2xml = new File(repo, "axis2.xml");
                this.configurationContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        dir, axis2xml.getName());
            } else {
                throw new Exception("repository not found");
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        try {
            log.info("Sleeping for a bit to let the engine start up.");
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            log.debug(e1.getMessage(), e1);
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.transport.TransportListener#init(org.apache.axis2.context.ConfigurationContext, org.apache.axis2.description.TransportInDescription)
     */
    public void init(ConfigurationContext configurationContext, TransportInDescription transportIn)
            throws AxisFault {
        this.configurationContext = configurationContext;
        user = Utils.getParameterValue(transportIn.getParameter(MailSrvConstants.POP3_USER));
        host = Utils.getParameterValue(transportIn.getParameter(MailSrvConstants.POP3_HOST));
        password = Utils.getParameterValue(transportIn.getParameter(MailSrvConstants.POP3_PASSWORD));
        port = Utils.getParameterValue(transportIn.getParameter(MailSrvConstants.POP3_PORT));
        replyTo = Utils.getParameterValue(transportIn.getParameter(MailSrvConstants.RAPLY_TO));

        if ((user == null) || (host == null) || (password == null) || (port == null)) {
            if (this.user == null) {
                throw new AxisFault(Messages.getMessage("canNotBeNull", "User"));
            }

            if (this.host == null) {
                throw new AxisFault(Messages.getMessage("canNotBeNull", "Host"));
            }

            if (this.port == null) {
                throw new AxisFault(Messages.getMessage("canNotBeNull", "Port"));
            }

            if (this.password == null) {
                throw new AxisFault(Messages.getMessage("canNotBeNull", "Password"));
            }
        }
    }

    /**
     * Server process.
     */
    public static void main(String args[]) throws AxisFault {
        if (args.length != 1) {
            log.info("java SimpleMailListener <repository>");
        } else {
            String dir = args[0];
            ConfigurationContext configurationContext;
            File repo = new File(dir);
            if (repo.exists()) {
                File axis2xml = new File(repo, "axis2.xml");
                configurationContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        dir, axis2xml.getName());
            } else {
                throw new AxisFault("repository not found");
            }
            SimpleMailListener sas = new SimpleMailListener();
            TransportInDescription transportIn =
                    configurationContext.getAxisConfiguration().getTransportIn(
                            new QName(Constants.TRANSPORT_MAIL));
            if (transportIn != null) {
                sas.init(configurationContext, transportIn);
                log.info("Starting the SimpleMailListener with repository "
                        + new File(args[0]).getAbsolutePath());
                sas.start();
            } else {
                log.info(
                        "Startup failed, mail transport not configured, Configure the mail trnasport in the axis2.xml file");
            }
        }
    }

    /**
     * Accept requests from a given TCP port and send them through the Axis
     * engine for processing.
     */
    public void run() {

        // Accept and process requests from the socket
        if (!stopped) {
            String logMessage = "Mail listner is being setup to listen to the address " + user
                    + "@" + host + " On port " + port;

            log.info(logMessage);
        }

        while (!stopped) {
            try {
                EmailReceiver receiver = new EmailReceiver(user, host, port, password);

                receiver.connect();

                Message[] msgs = receiver.receive();

                if ((msgs != null) && (msgs.length > 0)) {
                    log.info(msgs.length + " Message Found");

                    for (int i = 0; i < msgs.length; i++) {
                        MimeMessage msg = (MimeMessage) msgs[i];

                        if (msg != null) {
                            MailWorker worker = new MailWorker(msg, configurationContext);

                            worker.run();
                        }

                        msg.setFlag(Flags.Flag.DELETED, true);
                    }
                }

                receiver.disconnect();

                // Waiting for 3 seconds.
                Thread.sleep(3000);
            } catch (Exception e) {

                log.debug("An error occured when running the mail listner." + e.getMessage(), e);

                break;
            }
        }

        log.info("Mail listener has been stoped.");
    }

    /**
     * Start this server as a NON-daemon.
     */
    public void start() throws AxisFault {
        start(false);
    }

    /**
     * Start this server.
     * <p/>
     * Spawns a worker thread to listen for HTTP requests.
     *
     * @param daemon a boolean indicating if the thread should be a daemon.
     */
    public void start(boolean daemon) throws AxisFault {
        if (this.user == null) {
            throw new AxisFault(Messages.getMessage("canNotBeNull", "User"));
        }

        if (this.host == null) {
            throw new AxisFault(Messages.getMessage("canNotBeNull", "Host"));
        }

        if (this.port == null) {
            throw new AxisFault(Messages.getMessage("canNotBeNull", "Port"));
        }

        if (this.password == null) {
            throw new AxisFault(Messages.getMessage("canNotBeNull", "Password"));
        }

        if (doThreads) {
            this.configurationContext.getThreadPool().execute(this);
        } else {
            run();
        }
    }

    /**
     * Stop this server.
     * <p/>
     * This will interrupt any pending accept().
     */
    public void stop() {

        /*
         * Close the server socket cleanly, but avoid fresh accepts while the
         * socket is closing.
         */
        stopped = true;
        log.info("Quiting the mail listner");
    }

    public boolean getDoThreads() {
        return doThreads;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.transport.TransportListener#replyToEPR(java.lang.String)
     */
    public EndpointReference getEPRForService(String serviceName) throws AxisFault {
        return new EndpointReference(replyTo + "/services/" + serviceName);
    }

    public void setDoThreads(boolean value) {
        doThreads = value;
    }
}
