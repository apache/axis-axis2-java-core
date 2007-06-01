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

import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.*;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.*;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Hashtable;

/**
 * This is the implementation for Mail Listener in Axis2. It has the full capability
 * of connecting to a POP3 or IMPA server with SSL or regualar connection. This listener intend
 * to use as a server in client side as well with the involcation is Async with addressing.
 */


public class SimpleMailListener implements Runnable, TransportListener {
    private static final Log log = LogFactory.getLog(SimpleMailListener.class);

    private ConfigurationContext configurationContext = null;

    private boolean running = true;
    /*password and replyTo is Axis2 specific*/
    private String user = "";
    private String replyTo = "";

    /*This hold properties for pop3 or impa server connection*/
    private Properties pop3Properties = new Properties();

    private final EmailReceiver receiver ;

    /**
     * Time has been put from best guest. Let the default be 3 mins.
     * This value is configuralble from Axis2.xml. Under mail transport listener
     * simply set the following parameter.
     * <parameter name="transport.listener.interval">[custom listener interval]</parameter>
     */
    private int listenerWaitInterval = 1000 * 60 * 3;

    public SimpleMailListener() {
          receiver = new EmailReceiver();
    }

    public void init(ConfigurationContext configurationContext, TransportInDescription transportIn)
            throws AxisFault {
        this.configurationContext = configurationContext;

        ArrayList mailParameters = transportIn.getParameters();

        String password = "";
        String host = "";
        String protocol = "";
        String port = "";
        URLName urlName;

        for (Iterator iterator = mailParameters.iterator(); iterator.hasNext();) {
            Parameter param = (Parameter) iterator.next();
            String paramKey = param.getName();
            String paramValue = Utils.getParameterValue(param);
            if (paramKey == null || paramValue == null) {
                String error = Messages.getMessage("canNotBeNull", "Parameter name and value");
                log.error(error);
                throw new AxisFault(error);

            }
            pop3Properties.setProperty(paramKey, paramValue);
            if (paramKey.equals(org.apache.axis2.transport.mail.Constants.POP3_USER)) {
                user = paramValue;
            }
            if (paramKey.equals(org.apache.axis2.transport.mail.Constants.POP3_PASSWORD)) {
                password = paramValue;
            }
            if (paramKey.equals(org.apache.axis2.transport.mail.Constants.POP3_HOST)) {
                host = paramValue;
            }
            if (paramKey.equals(org.apache.axis2.transport.mail.Constants.STORE_PROTOCOL)) {
                protocol = paramValue;
            }
            if (paramKey.equals(org.apache.axis2.transport.mail.Constants.POP3_PORT)) {
                port = paramValue;
            }

            //Transport specific
            if (paramKey.equals(org.apache.axis2.transport.mail.Constants.REPLY_TO)) {
                replyTo = paramValue;
            }
            if (paramKey.equals(org.apache.axis2.transport.mail.Constants.LISTENER_INTERVAL)) {
                listenerWaitInterval = Integer.parseInt(paramValue);
            }

        }
        if (password.length() == 0 || user.length() == 0 || host.length() == 0 ||
            protocol.length() == 0) {
            String error = SimpleMailListener.class.getName() +
                           " one or more of Password, User, Host and Protocol are null or empty";
            log.error(error);
            throw new AxisFault(error);
        }

        if (port.length() == 0) {
            urlName = new URLName(protocol, host, -1, "", user, password);
        } else {
            urlName = new URLName(protocol, host, Integer.parseInt(port), "", user, password);
        }

        receiver.setPop3Properties(pop3Properties);
        receiver.setUrlName(urlName);
        Object obj = configurationContext.
                getProperty(org.apache.axis2.transport.mail.Constants.MAPPING_TABLE);
        if (obj == null) {
            configurationContext.setProperty(
                    org.apache.axis2.transport.mail.Constants.MAPPING_TABLE, new Hashtable());
        }

        Object callBackTable = configurationContext.
                getProperty(org.apache.axis2.transport.mail.Constants.CALLBACK_TABLE);
        if (callBackTable == null) {
            configurationContext.setProperty(
                    org.apache.axis2.transport.mail.Constants.CALLBACK_TABLE, new Hashtable());
        }


    }

    public void initFromRuntime(Properties properties, MessageContext msgContext) throws AxisFault {

        this.configurationContext = msgContext.getConfigurationContext();

        String password = "";
        String host = "";
        String protocol = "";
        String port = "";
        URLName urlName;

        pop3Properties.clear();
        pop3Properties.putAll(properties);

        user = properties.getProperty(org.apache.axis2.transport.mail.Constants.POP3_USER);
        password = properties.getProperty(org.apache.axis2.transport.mail.Constants.POP3_PASSWORD);
        host = properties.getProperty(org.apache.axis2.transport.mail.Constants.POP3_HOST);
        protocol = properties.getProperty(org.apache.axis2.transport.mail.Constants.STORE_PROTOCOL);
        port = properties.getProperty(org.apache.axis2.transport.mail.Constants.POP3_PORT);
        replyTo = properties.getProperty(org.apache.axis2.transport.mail.Constants.REPLY_TO);
        String value =
                properties.getProperty(org.apache.axis2.transport.mail.Constants.LISTENER_INTERVAL);
        if (value != null) {
            listenerWaitInterval = Integer.parseInt(value);
        }

        if (password.length() == 0 || user.length() == 0 || host.length() == 0 ||
            protocol.length() == 0) {
            String error = SimpleMailListener.class.getName() + " one or more of Password, User," +
                           " Host and Protocol are null or empty" + "in runtime settings";
            log.error(error);
            throw new AxisFault(error);
        }

        if (port == null) {
            urlName = new URLName(protocol, host, -1, "", user, password);
        } else {
            urlName = new URLName(protocol, host, Integer.parseInt(port), "", user, password);
        }

        receiver.setPop3Properties(pop3Properties);
        receiver.setUrlName(urlName);
        Object obj = configurationContext.
                getProperty(org.apache.axis2.transport.mail.Constants.MAPPING_TABLE);
        if (obj == null) {
            configurationContext.setProperty(
                    org.apache.axis2.transport.mail.Constants.MAPPING_TABLE, new Hashtable());
        }
        Object callBackTable = configurationContext.
                getProperty(org.apache.axis2.transport.mail.Constants.CALLBACK_TABLE);
        if (callBackTable == null) {
            configurationContext.setProperty(
                    org.apache.axis2.transport.mail.Constants.CALLBACK_TABLE, new Hashtable());
        }
    }

    /**
     * Server process.
     */
    public static void main(String args[]) throws AxisFault {
        if (args.length < 2) {
            log.info("java SimpleMailListener <repository>");
            printUsage();
        } else {
            String path = args[0];
            String axis2xml = args[1];
            ConfigurationContext configurationContext;
            File repo = new File(path);
            if (repo.exists()) {
                configurationContext =
                        ConfigurationContextFactory
                                .createConfigurationContextFromFileSystem(path, axis2xml);
            } else {
                printUsage();
                throw new AxisFault("repository not found");
            }
            SimpleMailListener sas = new SimpleMailListener();
            TransportInDescription transportIn =
                    configurationContext.
                            getAxisConfiguration().getTransportIn(Constants.TRANSPORT_MAIL);
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

    private static void printUsage() {
        System.out.println("Please provide the repository location and axis2.xml location ");
    }

    /**
     * Accept requests from a given TCP port and send them through the Axis
     * engine for processing.
     */
    public void run() {

        // Accept and process requests from the socket
        if (running) {
            log.info("Mail listner strated to listen to the address " + user);
        }

        while (running) {
            log.info("Info started polling");
            try {
                synchronized (receiver) {
                    receiver.connect();

                    Message[] msgs = receiver.receiveMessages();

                    if ((msgs != null) && (msgs.length > 0)) {
                        log.info(msgs.length + " Message(s) Found");

                        for (int i = 0; i < msgs.length; i++) {
                            MimeMessage msg = (MimeMessage) msgs[i];
                            try {
                                MessageContext mc = createMessageContextToMailWorker(msg);
                                if(mc==null){
                                    continue;
                                }
                                msg.setFlag(Flags.Flag.DELETED, true);
                                MailWorker worker = new MailWorker(configurationContext,mc);
                                this.configurationContext.getThreadPool().execute(worker);
                            } catch (Exception e) {
                                log.error("Error in SimpleMailListener - processing mail", e);
                            } finally {
                                // delete mail in any case
                            }
                        }
                    }

                    receiver.disconnect();
                }

            } catch (Exception e) {
                log.error("Error in SimpleMailListener", e);
            } finally {
                try {
                    Thread.sleep(listenerWaitInterval);
                } catch (InterruptedException e) {
                    log.warn("Error Encountered " + e);
                }
            }
        }

    }

    private MessageContext createMessageContextToMailWorker(MimeMessage msg) throws Exception {
        Object content = msg.getContent();
        if(!(content instanceof Multipart)){
            return null;
        }
        MessageContext msgContext = null;
        TransportInDescription transportIn =
                configurationContext.getAxisConfiguration()
                        .getTransportIn(org.apache.axis2.Constants.TRANSPORT_MAIL);
        TransportOutDescription transportOut =
                configurationContext.getAxisConfiguration()
                        .getTransportOut(org.apache.axis2.Constants.TRANSPORT_MAIL);
        if ((transportIn != null) && (transportOut != null)) {
            // create Message Context
            msgContext = configurationContext.createMessageContext();
            msgContext.setTransportIn(transportIn);
            msgContext.setTransportOut(transportOut);
            msgContext.setServerSide(true);
            msgContext.setProperty(org.apache.axis2.transport.mail.Constants.CONTENT_TYPE,
                                   msg.getContentType());
            msgContext.setIncomingTransportName(org.apache.axis2.Constants.TRANSPORT_MAIL);

            MailBasedOutTransportInfo transportInfo = new MailBasedOutTransportInfo();
            Address[] mimefroms = msg.getFrom();
            if (mimefroms != null && mimefroms.length > 0) {
                EndpointReference fromEPR = new EndpointReference(
                        org.apache.axis2.transport.mail.Constants.MAILTO + ":" +
                        msg.getFrom()[0].toString());
                transportInfo.setFrom(fromEPR);
            }

            // Save Message-Id to set as In-Reply-To on reply
            String smtpMessageId = msg.getMessageID();
            if (smtpMessageId != null) {
                transportInfo.setInReplyTo(smtpMessageId);
            }
            String inReplyTo =
                    getMailHeader(msg, org.apache.axis2.transport.mail.Constants.IN_REPLY_TO);
            if (inReplyTo != null) {
                transportInfo.setInReplyTo(inReplyTo);
            }
            msgContext.setProperty(org.apache.axis2.Constants.OUT_TRANSPORT_INFO, transportInfo);

            buildSOAPEnvelope(msg, msgContext);
            if(!fillMessageContextFromAvaiableData(msgContext,inReplyTo)){
                return null;
            }
        }
        return msgContext;
    }

    private boolean fillMessageContextFromAvaiableData(MessageContext msgContext ,
                                                    String messageID) throws AxisFault{
        Hashtable mappingTable = (Hashtable) configurationContext.
                getProperty(org.apache.axis2.transport.mail.Constants.MAPPING_TABLE);

        if(mappingTable!=null&&messageID!=null){
            String messageConetextId= (String) mappingTable.get(messageID);
            if(messageConetextId!=null){
                OperationContext opContext = configurationContext.getOperationContext(messageConetextId);
                if(opContext!=null && !opContext.isComplete()){
                    AxisOperation axisOp = opContext.getAxisOperation();
                    //TODO need to handle fault case as well ,
                    //TODO  need to check whether the message contains fault , if so we need to get the fault message
                    AxisMessage inMessage = axisOp.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                    msgContext.setOperationContext(opContext);
                    msgContext.setAxisMessage(inMessage);
                    opContext.addMessageContext(msgContext);
                    msgContext.setServiceContext(opContext.getServiceContext());
                    return true;
                }
            }
        }
        Hashtable callBackTable = (Hashtable) configurationContext.getProperty(
                org.apache.axis2.transport.mail.Constants.CALLBACK_TABLE);
        if(messageID!=null&&callBackTable!=null){
            SynchronousMailListener listener = (SynchronousMailListener) callBackTable.get(messageID);
            if(listener!=null){
                listener.setInMessageContext(msgContext);
            }
        }
        return false;
    }

    private void buildSOAPEnvelope(MimeMessage msg, MessageContext msgContext)
            throws AxisFault {
        //TODO we assume for the time being that there is only one attachement and this attachement contains  the soap evelope
        try {
            Multipart mp = (Multipart) msg.getContent();
            if (mp != null) {
                for (int i = 0, n = mp.getCount(); i < n; i++) {
                    Part part = mp.getBodyPart(i);

                    String disposition = part.getDisposition();

                    if (disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
                        String soapAction;

                        /* Set the Charactorset Encoding */
                        if (BuilderUtil.getCharSetEncoding(part.getContentType()) != null) {
                            msgContext.setProperty(
                                    org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING,
                                    BuilderUtil.getCharSetEncoding(
                                            part.getContentType()));
                        } else {
                            msgContext.setProperty(
                                    org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING,
                                    MessageContext.DEFAULT_CHAR_SET_ENCODING);
                        }

                        /* SOAP Action */
                        soapAction = getMailHeaderFromPart(part,
                                                           org.apache.axis2.transport.mail.Constants.HEADER_SOAP_ACTION);
                        msgContext.setSoapAction(soapAction);

                        String contentDescription =
                                getMailHeaderFromPart(part, "Content-Description");

                        /* As an input stream - using the getInputStream() method.
                        Any mail-specific encodings are decoded before this stream is returned.*/
                        if (contentDescription != null) {
                            msgContext.setTo(new EndpointReference(contentDescription));
                        }

                        if (part.getContentType().indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) >
                            -1) {
                            TransportUtils
                                    .processContentTypeForAction(part.getContentType(), msgContext);
                        }
                        InputStream inputStream = part.getInputStream();
                        SOAPEnvelope envelope = TransportUtils
                                .createSOAPMessage(msgContext, inputStream, part.getContentType());
                        msgContext.setEnvelope(envelope);
                    }
                }


            }
        } catch (IOException e) {
            throw new AxisFault(e.getMessage(),e);
        }
        catch (MessagingException e) {
            throw new AxisFault(e.getMessage(),e);
        } catch (XMLStreamException e) {
            throw new AxisFault(e.getMessage(),e);
        }
    }

    private String getMailHeader(MimeMessage msg, String headerName) throws AxisFault {
        try {
            String values[] = msg.getHeader(headerName);

            if (values != null) {
                return parseHeaderForQuotes(values[0]);
            } else {
                return null;
            }
        } catch (MessagingException e) {
            throw new AxisFault(e.getMessage(),e);
        }
    }

    private String parseHeaderForQuotes(String value) {
        if (value != null) {
            if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

        }
        return value;
    }

    private String getMailHeaderFromPart(Part part, String headerName) throws AxisFault {
        try {
            String values[] = part.getHeader(headerName);

            if (values != null) {
                return parseHeaderForQuotes(values[0]);
            } else {
                return null;
            }
        } catch (MessagingException e) {
            throw new AxisFault(e.getMessage(),e);
        }
    }

    /**
     * Start this listener
     */
    public void start() throws AxisFault {
        this.configurationContext.getThreadPool().execute(this);
    }

    /**
     * Stop this server.
     * <p/>
     */
    public void stop() {
        running = false;
        log.info("Stopping the mail listner");
    }


    public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
        return getEPRsForService(serviceName, ip)[0];
    }

    public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault {
        return new EndpointReference[]{
                new EndpointReference(Constants.TRANSPORT_MAIL + ":" + replyTo + "?" +
                                      org.apache.axis2.transport.mail.Constants.X_SERVICE_PATH + "="
                                      + configurationContext.getServiceContextPath() + "/" +
                                      serviceName),
                new EndpointReference(Constants.TRANSPORT_MAIL + ":" + replyTo + "?" +
                                      configurationContext.getServiceContextPath() + "/" +
                                      serviceName)
                };
    }


    public SessionContext getSessionContext(MessageContext messageContext) {
        return null;
    }

    public void destroy() {
        this.configurationContext = null;
    }
}
