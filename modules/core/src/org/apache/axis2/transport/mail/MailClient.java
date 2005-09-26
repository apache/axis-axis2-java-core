package org.apache.axis2.transport.mail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

public class MailClient
        extends Authenticator {
    public static final int SHOW_MESSAGES = 1;
    public static final int CLEAR_MESSAGES = 2;
    public static final int SHOW_AND_CLEAR =
            SHOW_MESSAGES + CLEAR_MESSAGES;

    protected String from;
    protected Session session;
    protected PasswordAuthentication authentication;
    private Log log = LogFactory.getLog(getClass());

    public MailClient(String user, String host) {
        this(user, host, user, false);
    }

    public MailClient(String user, String host, String password) {
        this(user, host, password, false);
    }

    public MailClient(String user,
                      String host,
                      String password,
                      boolean debug) {
        from = user + '@' + host;
        authentication = new PasswordAuthentication(user, password);
        Properties props = new Properties();
        props.put("mail.user", user);
        props.put("mail.host", host);
        props.put("mail.debug", debug ? "true" : "false");
        props.put("mail.store.protocol", "pop3");
        props.put("mail.transport.protocol", "smtp");
        session = Session.getInstance(props, this);
    }

    public PasswordAuthentication getPasswordAuthentication() {
        return authentication;
    }

    public void sendMessage(String to,
                            String subject,
                            String content,
                            String soapAction)
            throws MessagingException {
        log.info("SENDING message from " + from + " to " + to);
        MimeMessage msg = new MimeMessage(session);
        msg.setHeader("transport.mail.soapaction", soapAction);
        msg.addRecipients(Message.RecipientType.TO, to);
        msg.setSubject(subject);
        msg.setText(content);
        Transport.send(msg);
    }

    public int checkInbox(int mode)
            throws MessagingException, IOException {
        int numMessages = 0;
        if (mode == 0) return 0;
        boolean show = (mode & SHOW_MESSAGES) > 0;
        boolean clear = (mode & CLEAR_MESSAGES) > 0;
        String action =
                (show ? "Show" : "") +
                (show && clear ? " and " : "") +
                (clear ? "Clear" : "");
        log.info(action + " INBOX for " + from);
        Store store = session.getStore();
        store.connect();
        Folder root = store.getDefaultFolder();
        Folder inbox = root.getFolder("inbox");
        inbox.open(Folder.READ_WRITE);
        Message[] msgs = inbox.getMessages();
        numMessages = msgs.length;
        if (msgs.length == 0 && show) {
            log.info("No messages in inbox");
        }
        for (int i = 0; i < msgs.length; i++) {
            MimeMessage msg = (MimeMessage) msgs[i];
            if (show) {
                log.info("    From: " + msg.getFrom()[0]);
                log.info(" Subject: " + msg.getSubject());
                log.info(" Content: " + msg.getContent());
            }
            if (clear) {
                msg.setFlag(Flags.Flag.DELETED, true);
            }
        }
        inbox.close(true);
        store.close();
        return numMessages;
    }
}
