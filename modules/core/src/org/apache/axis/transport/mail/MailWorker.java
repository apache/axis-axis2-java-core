/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.axis.transport.mail;

import java.io.ByteArrayInputStream;
import java.io.Writer;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.EngineContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.impl.llom.builder.StAXBuilder;
import org.apache.axis.om.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;

public class MailWorker implements Runnable {
    protected static Log log = LogFactory.getLog(MailWorker.class.getName());

    private String contentType = "text/xml";

    // Server
    private SimpleMailListner server;

    private SMTPClient client = null;

    private EngineContext reg = null;

    // Current message
    private MimeMessage mimeMessage;

    //Processed responce CT 08-Feb-2005
    private MimeMessage outputMimeMessage;

    // Axis specific constants
    private static String transportName = "mail";

    private Properties prop = new Properties();

    private Session session = Session.getDefaultInstance(prop, null);

    /**
     * Constructor for MailWorker
     *
     * @param server
     * @param mimeMessage
     */
    public MailWorker(SimpleMailListner server, MimeMessage mimeMessage,
                      EngineContext reg) {
        this.server = server;
        this.mimeMessage = mimeMessage;
        this.reg = reg;
    }

    /**
     * The main workhorse method.
     */
    public void run() {
        // create an Axis server
        AxisEngine engine = SimpleMailListner.getAxisEngine();
        MessageContext msgContext = null;
        // create and initialize a message context
        try {
            msgContext = new MessageContext(this.reg, null, null,
                    reg.getEngineConfig().getTransportIn(new QName(Constants.TRANSPORT_HTTP)),
                    reg.getEngineConfig().getTransportOut(new QName(Constants.TRANSPORT_HTTP)));
            msgContext.setServerSide(true);
        } catch (AxisFault af) {
            log.error("Error occured while creating the message context", af);
        }

        Message requestMsg = null;

        // buffers for the headers we care about
        StringBuffer soapAction = new StringBuffer();
        StringBuffer fileName = new StringBuffer();
        StringBuffer contentType = new StringBuffer();
        StringBuffer contentLocation = new StringBuffer();

        Message responseMsg = null;

        // prepare request (do as much as possible while waiting for the
        // next connection).
 
        //msgContext.setResponseMessage(null);
        //msgContext.reset();
        // msgContext.setTransport(new AxisTransport(transportName)); There is
        // no way to set the transport. CT 07-Feb-2005.

        responseMsg = null;

        try {
            // parse all headers into hashtable
            parseHeaders(mimeMessage, contentType, contentLocation,
                    soapAction);

            String soapActionString = soapAction.toString();
            if (soapActionString != null) {
                //msgContext.setUseSOAPAction(true); Not present CT
                // 07-Feb-2005
                msgContext.setWSAAction(
                        soapActionString);
            }

            System.out
                    .println("This is the data that is to be processed  \n "
                    + mimeMessage.getContent().toString() + "\n");

            ByteArrayInputStream bais = new ByteArrayInputStream(mimeMessage.getContent().toString().getBytes());
            XMLStreamReader reader = XMLInputFactory.newInstance()
                    .createXMLStreamReader(bais);
            StAXBuilder builder = new StAXSOAPModelBuilder(reader);

            msgContext.setEnvelope((SOAPEnvelope) builder
                    .getDocumentElement());

            //A writer is created and sent to the engine so that the engine
            // can write straight to the writer
            String replyTo = ((InternetAddress) mimeMessage.getReplyTo()[0])
                    .getAddress();
            String sendFrom = ((InternetAddress) mimeMessage
                    .getAllRecipients()[0]).getAddress();
            String subject = mimeMessage.getSubject();
            msgContext.setProperty(MailConstants.FROM_ADDRESS, sendFrom);
            msgContext.setProperty(MailConstants.TO_ADDRESS, replyTo);
            msgContext.setProperty(MailConstants.SUBJECT, subject);
            Writer wr = getMailWriter(server.getHost(), msgContext);

            msgContext.setProperty(MessageContext.TRANSPORT_WRITER, wr);
            msgContext.setTo(new EndpointReference(AddressingConstants.WSA_TO, replyTo));

            // invoke the Axis engine
            engine.receive(msgContext);

            sendMessage(wr);

        } catch (Exception e) {
            e.printStackTrace();
            AxisFault af;
            if (e instanceof AxisFault) {
                af = (AxisFault) e;
                //log.debug(Messages.getMessage("serverFault00"), af);
                // CT 07-Feb-2005
                log.debug("Error occured while trying to process the mail.",
                        af);
            } else {
                af = AxisFault.makeFault(e);
            }
        }

        /*
         * 
         * This part is ignored for the time being. CT 07-Feb-2005.
         * 
         * if (msgContext.getProperty(MessageContext.QUIT_REQUESTED) != null) { //
         * why then, quit! try { server.stop(); } catch (Exception e) { } }
         */
    }

    private Writer getMailWriter(String smtpHost, MessageContext msgContext)
            throws Exception {
        client = new SMTPClient();
        client.connect(smtpHost);

        // After connection attempt, you should check the reply code to verify
        // success.
        int reply = client.getReplyCode();
        if (!SMTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            AxisFault fault = new AxisFault("SMTP"
                    + "( SMTP server refused connection )"); //Issue #2 CT
            // 07-Feb-2005.
            throw fault;
        }

        client.login(smtpHost);
        reply = client.getReplyCode();
        if (!SMTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            AxisFault fault = new AxisFault("SMTP"
                    + "( SMTP server refused connection )");
            throw fault;
        }
        client.setSender((String) msgContext
                .getProperty(MailConstants.FROM_ADDRESS));
        client.addRecipient((String) msgContext
                .getProperty(MailConstants.TO_ADDRESS));
        Writer writer = client.sendMessageData();

        return writer;
    }

    private void sendMessage(Writer writer) throws Exception {
        writer.flush();
        writer.close();

        System.out.print(client.getReplyString());
        if (!client.completePendingCommand()) {
            System.out.print(client.getReplyString());
            AxisFault fault = new AxisFault("SMTP" + "( Failed to send email )");
            throw fault;
        }
        client.logout();
        client.disconnect();
    }

    /**
     * Read all mime headers, returning the value of Content-Length and
     * SOAPAction.
     *
     * @param mimeMessage     InputStream to read from
     * @param contentType     The content type.
     * @param contentLocation The content location
     * @param soapAction      StringBuffer to return the soapAction into
     */
    private void parseHeaders(MimeMessage mimeMessage,
                              StringBuffer contentType, StringBuffer contentLocation,
                              StringBuffer soapAction) throws Exception {
        contentType.append(mimeMessage.getContentType());
        contentLocation.append(mimeMessage.getContentID());
        String values[] = mimeMessage
                .getHeader(MailConstants.HEADER_SOAP_ACTION);
        if (values != null)
            soapAction.append(values[0]);
        System.out.println("Calling soap action " + soapAction);
    }
}