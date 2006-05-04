package org.apache.axis2.transport.mail.server;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.mail.SimpleMailListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MailServer {
	private static final Log log = LogFactory.getLog(MailServer.class);
    Storage st = null;
    public ConfigurationContext configurationContext = null;
    private POP3Server pop3Server;
    private SMTPServer smtpServer;

    public MailServer(int popPort, int smtpPort) throws AxisFault {
        st = new Storage();

        // Start up the two servers and lets have some fun. - CT
        smtpServer = new SMTPServer(st, smtpPort);
        smtpServer.start();
        pop3Server = new POP3Server(st, popPort);
        pop3Server.start();
        try {
            log.info("Sleeping for a bit to let the mail server start up.");
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            log.error(e1);
        }
    }

    public MailServer(ConfigurationContext configurationContext, int popPort, int smtpPort)
            throws AxisFault {
        this.configurationContext = configurationContext;

        st = new Storage();

        // Start up the two servers and lets have some fun. - CT
        smtpServer = new SMTPServer(st, configurationContext, smtpPort);
        smtpServer.start();
        pop3Server = new POP3Server(st, popPort);
        pop3Server.start();
        try {
            log.info("Sleeping for a bit to let the mail server start up.");
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            log.error(e1);
        } 
    }

    public MailServer(String dir, int popPort, int smtpPort) throws AxisFault {
        try {
            configurationContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(dir, null);
        } catch (Exception e) {
            log.error(e);
        }

        try {
            log.info("Sleeping for a bit to let the engine start up.");
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            log.error(e1);
        }

        st = new Storage();

        // Start up the two servers and lets have some fun. - CT
        smtpServer = new SMTPServer(st, configurationContext, smtpPort);
        smtpServer.start();
        pop3Server = new POP3Server(st, popPort);
        pop3Server.start();
        try {
            log.info("Sleeping for a bit to let the engine start up.");
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            log.error(e1);
        }
    }

    public static void main(String args[]) {
        int smtpPost = MailSrvConstants.SMTP_SERVER_PORT;
        int popPort = MailSrvConstants.POP_SERVER_PORT;

        if (args.length == 2) {
            try {
                smtpPost = Integer.parseInt(args[0]);
                popPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e1) {
                log.info("Error in parsing the custom ports.");
            }
        } else {
            log.info("Usage MailServer <SMTP_PORT> <POP_PORT>");
            log.info("Using 1134 as the SMTP port and 1049 as the POP port");
        }

        try {
            new MailServer(popPort, smtpPost);
        } catch (AxisFault e) {
            log.info(e.getMessage());
        }
    }

    public void stop() throws AxisFault {
        smtpServer.stopServer();
        pop3Server.stopServer();
        try {
            log.info("Giving some time for the sockets to close.");
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            log.error(e1);
        }
    }
}
