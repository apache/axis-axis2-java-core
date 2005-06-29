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
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.TransportInDescription;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.impl.llom.builder.StAXBuilder;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.util.threadpool.AxisWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MailWorker implements AxisWorker {
    protected static Log log = LogFactory.getLog(MailWorker.class.getName());

    private String contentType = "text/xml";

    private ConfigurationContext reg = null;

    // Current message
    private MimeMessage mimeMessage;

    private Properties prop = new Properties();

    private Session session = Session.getDefaultInstance(prop, null);

    /**
     * Constructor for MailWorker
     *
     * @param server
     * @param mimeMessage
     */
    public MailWorker(MimeMessage mimeMessage, ConfigurationContext reg) {
        this.mimeMessage = mimeMessage;
        this.reg = reg;
    }

    /**
     * The main workhorse method.
     */
    public void doWork() {
        // create an Axis server
        AxisEngine engine = new AxisEngine(reg);
        MessageContext msgContext = null;
        // create and initialize a message context
        try {
            TransportInDescription transportIn = reg.getAxisConfiguration().getTransportIn(new QName(Constants.TRANSPORT_MAIL));
            TransportOutDescription transportOut = reg.getAxisConfiguration().getTransportOut(new QName(Constants.TRANSPORT_MAIL));
            
            msgContext = new MessageContext(reg,transportIn,transportOut);
            msgContext.setServerSide(true);
            msgContext.setProperty(MailConstants.CONTENT_TYPE, mimeMessage.getContentType());
            String soapAction  = getMailHeader(MailConstants.HEADER_SOAP_ACTION);
            msgContext.setWSAAction(soapAction);
            msgContext.setSoapAction(soapAction);

            String serviceURL = mimeMessage.getSubject();
            if (serviceURL == null) {
                serviceURL = "";
            }

            String replyTo = ((InternetAddress) mimeMessage.getReplyTo()[0]).getAddress();
            if (replyTo != null) {
                msgContext.setReplyTo(
                    new EndpointReference(AddressingConstants.WSA_REPLY_TO, replyTo));
            }
            
            String recepainets = ((InternetAddress) mimeMessage.getAllRecipients()[0]).getAddress();
            if (recepainets != null) {
                msgContext.setTo(new EndpointReference(AddressingConstants.WSA_FROM, recepainets+ "/"+serviceURL));
            }else{
                throw new AxisFault("No receptineist found in the Email");
            }

            // add the SOAPEnvelope
            String message = mimeMessage.getContent().toString();
            System.out.println("message["+message+"]");
            ByteArrayInputStream bais =
                new ByteArrayInputStream(message.getBytes());
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(bais);
            StAXBuilder builder = new StAXSOAPModelBuilder(reader);
            msgContext.setEnvelope((SOAPEnvelope) builder.getDocumentElement());

            // invoke the Axis engine
            engine.receive(msgContext);
        } catch (Exception e) {
            e.printStackTrace();
            AxisFault af;
            if (e instanceof AxisFault) {
                af = (AxisFault) e;
                //log.debug(Messages.getMessage("serverFault00"), af);
                // CT 07-Feb-2005
                log.debug("Error occured while trying to process the mail.", af);
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

    private String getMailHeader(String headerName) throws AxisFault {
        try {
            String values[] = mimeMessage.getHeader(headerName);
            if (values != null) {
                return values[0];
            } else {
                return null;
            }
        } catch (MessagingException e) {
            throw new AxisFault(e);
        }

    }

}