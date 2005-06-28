package org.apache.axis.transport.mail.server;

/**
 * @author Chamil Thanthrimudalige
 * @author Chamikara Jayalath
 */

public interface MailConstants {
	public final int SMTP_SERVER_PORT = (1024 + 25);
	public final String SERVER_DOMAIN = "localhost";
	public final int POP_SERVER_PORT = (1024 + 110);

    public final String COMMAND_UNKNOWN = "550 Unknown command";
    public final String COMMAND_EXIT = "EXIT";
    public final String MAIL_OK = "250 OK performed command MAIL";
    public final String MAIL_ERROR = "550 Error processign MAIL command";
    public final String RCPT_OK = "250 OK performed command RCPT";
    public final String RCPT_ERROR = "550 Unknown recipient";
    public final String DATA_START_SUCCESS = "354 OK Ready for data";
    public final String DATA_END_SUCCESS = "250 OK finished adding data";
    public final String COMMAND_TRANSMISSION_END = "221 Closing SMTP service.";
    public final String HELO_REPLY = "250 OK";
    public final String OK = "+OK ";
    public final String ERR = "-ERR ";
    public final String USER = "USER";
    public final String PASS = "PASS";
    public final String STAT = "STAT";
    public final String LIST = "LIST";
    public final String RETR = "RETR";
    public final String DELE = "DELE";
    public final String NOOP = "NOOP";
    public final String RSET = "RSET";
    public final String QUIT = "QUIT";
}
