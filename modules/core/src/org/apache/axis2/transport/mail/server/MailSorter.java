package org.apache.axis2.transport.mail.server;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.AxisFault;
import org.apache.axis2.om.impl.llom.builder.StAXBuilder;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis2.transport.mail.MailConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

/**
 * This class will be used to sort the messages into normal messages and mails
 * being sent to the Axis engine. If a mail is to be sent to the engine then a
 * new Axis engine is created using the configuration in the MailServer class
 * and the receive method is called.
 *
 * @author Chamil Thanthrimudalige
 */
public class MailSorter {
    Storage st = null;
    private ArrayList sUsers = new ArrayList(); // Special users. They are hard coded for the time being to axis2-server@localhost and axis2-server@127.0.0.1
    private ConfigurationContext configurationContext = null;
    protected static Log log = LogFactory.getLog(MailSorter.class.getName());

    public MailSorter(Storage st, ConfigurationContext configurationContext) {
        this.st = st;
        sUsers.add("axis2-server@localhost");
        sUsers.add("axis2-server@127.0.0.1");
        this.configurationContext = configurationContext;
    }

    public void sort(String user, MimeMessage msg) {
        if (sUsers.contains(user)) {
            processMail(configurationContext, msg);
        } else {
            st.addMail(user, msg);
        }
    }

    public void processMail(ConfigurationContext confContext, MimeMessage mimeMessage) {
        // create an Axis server
        AxisEngine engine = new AxisEngine(confContext);
        MessageContext msgContext = null;
        // create and initialize a message context
        try {
            msgContext =
                    new MessageContext(confContext,
                                       confContext.getAxisConfiguration().getTransportIn(new QName(Constants.TRANSPORT_MAIL)),
                                       confContext.getAxisConfiguration().getTransportOut(new QName(Constants.TRANSPORT_MAIL)));
            msgContext.setServerSide(true);

            msgContext.setProperty(MailConstants.CONTENT_TYPE, mimeMessage.getContentType());
            msgContext.setWSAAction(getMailHeader(MailConstants.HEADER_SOAP_ACTION, mimeMessage));

            String serviceURL = mimeMessage.getSubject();
            if (serviceURL == null) {
                serviceURL = "";
            }

            String replyTo = ((InternetAddress) mimeMessage.getReplyTo()[0]).getAddress();
            if (replyTo != null) {
                msgContext.setReplyTo(new EndpointReference(AddressingConstants.WSA_REPLY_TO, replyTo));
            }

            String recepainets = ((InternetAddress) mimeMessage.getAllRecipients()[0]).getAddress();


            if (recepainets != null) {
                msgContext.setTo(new EndpointReference(AddressingConstants.WSA_FROM, recepainets + "/" + serviceURL));
            }

            // add the SOAPEnvelope
            String message = mimeMessage.getContent().toString();
            System.out.println("message[" + message + "]");
            ByteArrayInputStream bais =
                    new ByteArrayInputStream(message.getBytes());
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(bais);
            StAXBuilder builder = new StAXSOAPModelBuilder(reader);
            msgContext.setEnvelope((SOAPEnvelope) builder.getDocumentElement());
            // invoke the Axis engine
            engine.receive(msgContext);
        } catch (Exception e) {
            AxisFault af;
            if (e instanceof AxisFault) {
                af = (AxisFault) e;
                log.debug("Error occured while trying to process the mail.", af);
            } else {
                af = AxisFault.makeFault(e);
            }
        }
    }


    private String getMailHeader(String headerName, MimeMessage mimeMessage) throws AxisFault {
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