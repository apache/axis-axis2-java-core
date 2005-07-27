package org.apache.axis2.transport.mail.server;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.mail.SimpleMailListener;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chamil Thanthrimudalige
 */
public class MailServer {
    Storage st = null;
    private SMTPServer smtpServer;
    private POP3Server pop3Server; 

    public ConfigurationContext configurationContext = null;

    protected static Log log = LogFactory.getLog(SimpleMailListener.class
            .getName());

    public MailServer(String dir, int popPort, int smtpPort) throws AxisFault {
        try {
            ConfigurationContextFactory builder = new ConfigurationContextFactory();
            configurationContext = builder.buildConfigurationContext(dir);
        } catch (Exception e) {
            log.error(e);
        }
        try {
            System.out
                    .println("Sleeping for a bit to let the engine start up.");
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            log.error(e1);
        }
        st = new Storage();
        // Start up the two servers and lets have some fun. - CT
        smtpServer = new SMTPServer(st, configurationContext,
                smtpPort);
        smtpServer.start();
        pop3Server = new POP3Server(st, popPort);
        pop3Server.start();

    }

    public MailServer(ConfigurationContext configurationContext, int popPort,
            int smtpPort) throws AxisFault {
        this.configurationContext = configurationContext;
        try {
            System.out
                    .println("Sleeping for a bit to let the engine start up.");
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            log.error(e1);
        }

        st = new Storage();
        // Start up the two servers and lets have some fun. - CT
        smtpServer = new SMTPServer(st, configurationContext,
                smtpPort);
        smtpServer.start();
        pop3Server = new POP3Server(st, popPort);
        pop3Server.start();
    }

    public MailServer(int popPort, int smtpPort) throws AxisFault {
        st = new Storage();
        // Start up the two servers and lets have some fun. - CT
        smtpServer = new SMTPServer(st, smtpPort);
        smtpServer.start();
        pop3Server = new POP3Server(st, popPort);
        pop3Server.start();
    }
    
    public void stop() throws AxisFault{
        smtpServer.stopServer();
        pop3Server.stopServer();
    }

    public static void main(String args[]){
        int smtpPost = MailConstants.SMTP_SERVER_PORT;
        int popPort = MailConstants.POP_SERVER_PORT;
        if (args.length == 2) {
            try {
                smtpPost = Integer.parseInt(args[0]);
                popPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e1) {
                System.out.println("Error in parsing the custom ports.");
            }
        } else {
            System.out.println("Usage MailServer <SMTP_PORT> <POP_PORT>");
            System.out.println("Using 1134 as the SMTP port and 1049 as the POP port");
        }

        try {
            MailServer ms = new MailServer(popPort, smtpPost);
        } catch (AxisFault e) {
            e.printStackTrace();
        }
    }
}
