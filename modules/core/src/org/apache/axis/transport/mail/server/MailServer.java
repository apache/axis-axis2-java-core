package org.apache.axis.transport.mail.server;

import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.transport.mail.SimpleMailListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chamil Thanthrimudalige
 */
public class MailServer {
	Storage st = null;
	public ConfigurationContext configurationContext = null;
	protected static Log log = LogFactory.getLog(SimpleMailListener.class.getName());

    public MailServer(String dir, int popPort,int smtpPort) {
            try {
                ConfigurationContextFactory builder = new ConfigurationContextFactory();
                configurationContext = builder.buildConfigurationContext(dir);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                System.out.println("Sleeping for a bit to let the engine start up.");
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                log.debug(e1.getMessage(), e1);
            }
            st = new Storage();
            // Start up the two servers and lets have some fun. - CT
            SMTPServer smtpServer = new SMTPServer(st,configurationContext,smtpPort);
            smtpServer.start();
            POP3Server pop3Server = new POP3Server(st,popPort);
            pop3Server.start();

        }
	public MailServer(ConfigurationContext configurationContext,int popPort,int smtpPort){
	    this.configurationContext = configurationContext;
        try {
            System.out.println("Sleeping for a bit to let the engine start up.");
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            log.debug(e1.getMessage(), e1);
        }

	    st = new Storage();
		// Start up the two servers and lets have some fun. - CT
		SMTPServer smtpServer = new SMTPServer(st,configurationContext,smtpPort);
		smtpServer.start();
		POP3Server pop3Server = new POP3Server(st,popPort);
		pop3Server.start();
	}
}
