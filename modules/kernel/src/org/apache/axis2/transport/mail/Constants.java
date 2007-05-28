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

public class Constants {

    public final static String HEADER_SOAP_ACTION = "mail.soapaction";

    public final static String FROM_ADDRESS = "mail.smtp.from";
    public final static String SMTP_USER = "mail.smtp.user";
    public final static String SMTP_PORT = "mail.smtp.port";
    public final static String SMTP_HOST = "mail.smtp.host";
    public final static String SMTP_USER_PASSWORD = "transport.mail.smtp.password";

    public final static String POP3_USER = "mail.pop3.user";
    public final static String POP3_PORT = "mail.pop3.port";
    public final static String POP3_PASSWORD = "transport.mail.pop3.password";
    public final static String POP3_HOST = "mail.pop3.host";
    public final static String STORE_PROTOCOL = "mail.store.protocol";


    public final static String REPLY_TO = "transport.mail.replyToAddress";

    public final static String LISTENER_INTERVAL = "transport.listener.interval";

    public final static int SMTP_SERVER_PORT = (1024 + 25);

    public final static String SERVER_DOMAIN = "localhost";
    public final static String RCPT_OK = "250 OK performed command RCPT";
    public final static String RCPT_ERROR = "550 Unknown recipient";


    public final static int POP_SERVER_PORT = (1024 + 110);

    public final static String OK = "+OK ";
    public final static String MAIL_OK = "250 OK performed command MAIL";
    public final static String MAIL_ERROR = "550 Error processign MAIL command";
    public final static String HELO_REPLY = "250 OK";

    public final static String ERR = "-ERR ";
    public final static String DEFAULT_CONTENT_TYPE = "text/xml";
    public final static String DEFAULT_CHAR_SET_ENCODING = "7bit";
    public final static String DEFAULT_CHAR_SET = "us-ascii";
    public final static String DATA_START_SUCCESS = "354 OK Ready for data";
    public final static String DATA_END_SUCCESS = "250 OK finished adding data";
    public final static String CONTENT_TYPE = "mail.contenttype";
    public final static String CONTENT_LOCAION = "mail.contentlocation";
    public final static String COMMAND_UNKNOWN = "550 Unknown command";
    public final static String COMMAND_TRANSMISSION_END = "221 Closing SMTP service.";
    public final static String COMMAND_EXIT = "EXIT";
    public final static String USER = "USER";
    public final static String STAT = "STAT";
    public final static String RSET = "RSET";
    public final static String RETR = "RETR";
    public final static String QUIT = "QUIT";
    public final static String PASS = "PASS";
    public final static String NOOP = "NOOP";
    public final static String LIST = "LIST";
    public final static String DELE = "DELE";

    public static final String MAIL_SMTP = "_MAIL_SMTP_";

    public static final String MAIL_POP3 = "_MAIL_POP3_";

    public static final String X_SERVICE_PATH = "X-Service-Path";
    public static final String MAIL_SYNC = "_MAIL_SYNC_";
    public static final String IN_REPLY_TO = "In-Reply-To";
    public static final String MAILTO = "mailto";
    public static final String MAPPING_TABLE = "mappingTable";
    public static final String CALLBACK_TABLE = "callbackTable";
}
