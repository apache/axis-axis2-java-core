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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.AbstractTransportSender;
import org.apache.axis2.transport.mail.server.MailSrvConstants;
import org.apache.axis2.util.Utils;

public class MailTransportSender extends AbstractTransportSender {
	
    private static final long serialVersionUID = -2858556361961169381L;
	private String smtpPort = "25";
    private ByteArrayOutputStream byteArrayOutputStream;
    private String host;
    private String password;
    private String user;
    private TransportOutDescription transportOut;

    public void init(ConfigurationContext confContext, TransportOutDescription transportOut)
    throws AxisFault {
    		this.transportOut = transportOut;
    }
    
    public MailTransportSender() {
    }

    public void cleanup(MessageContext msgContext) throws AxisFault {
    }

    public void finalizeSendWithOutputStreamFromIncomingConnection(MessageContext msgContext,
                                                                   OutputStream out)
            throws AxisFault {
    }

    public void finalizeSendWithToAddress(MessageContext msgContext, OutputStream out)
            throws AxisFault {
        try {
           // TransportOutDescription transportOut = msgContext.getTransportOut();

            user =
                    Utils.getParameterValue(transportOut.getParameter(MailSrvConstants.SMTP_USER));
            host =
                    Utils.getParameterValue(transportOut.getParameter(MailSrvConstants.SMTP_HOST));
            password =
                    Utils.getParameterValue(transportOut.getParameter(MailSrvConstants.SMTP_PASSWORD));
            smtpPort =
                    Utils.getParameterValue(transportOut.getParameter(MailSrvConstants.SMTP_PORT));

            if ((user != null) && (host != null) && (password != null) && (smtpPort != null)) {

            	   EMailSender sender = new EMailSender(user, host, smtpPort, password);

                String eprAddress = msgContext.getTo().getAddress();

                // In mail char set is what is being used. Charset encoding is not what is expected here.
                String charSet =
                        (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
                if (charSet == null) {
                    charSet = MailSrvConstants.DEFAULT_CHAR_SET;
                }

                int mailNameIndex = eprAddress.indexOf("mail:");
                if (mailNameIndex > -1){
                		eprAddress = eprAddress.substring(mailNameIndex + 5);
                }
                int index = eprAddress.indexOf('/');
                String subject = "";
                String email = null;

                if (index >= 0) {
                    subject = eprAddress.substring(index + 1);
                    email = eprAddress.substring(0, index);
                } else {
                    email = eprAddress;
                }
                int emailColon = email.indexOf(":");
                if (emailColon >= 0) {
                		email = email.substring(emailColon + 1);
                }
                
                sender.send(subject, email, new String(byteArrayOutputStream.toByteArray()),
                        charSet);
            } else {
                if (user == null) {
                    throw new AxisFault(Messages.getMessage("canNotBeNull", "User"));
                } else if (smtpPort == null) {
                    throw new AxisFault(Messages.getMessage("canNotBeNull", "smtpPort"));
                } else if (host == null) {
                    throw new AxisFault(Messages.getMessage("canNotBeNull", "Host"));
                } else if (password == null) {
                    throw new AxisFault(Messages.getMessage("canNotBeNull", "password"));
                }
            }
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    protected OutputStream openTheConnection(EndpointReference epr, MessageContext msgContext)
            throws AxisFault {
        byteArrayOutputStream = new ByteArrayOutputStream();

        return byteArrayOutputStream;
    }

    // Output Stream based cases are not supported
    public OutputStream startSendWithOutputStreamFromIncomingConnection(MessageContext msgContext,
                                                                        OutputStream out)
            throws AxisFault {
        throw new UnsupportedOperationException();
    }

    public OutputStream startSendWithToAddress(MessageContext msgContext, OutputStream out)
            throws AxisFault {
        return out;
    }
    public void writeMessage(MessageContext msgContext, OutputStream out) throws AxisFault {
        SOAPEnvelope envelope = msgContext.getEnvelope();
        OMElement outputMessage = envelope;

        if ((envelope != null) && msgContext.isDoingREST()) {
            outputMessage = envelope.getBody().getFirstElement();
        }

        if (outputMessage != null) {
            try {
                OMOutputFormat format = new OMOutputFormat();

                format.setDoOptimize(msgContext.isDoingMTOM());
                format.setCharSetEncoding(null); //Set to null so that the code will not fail on 7bit.
                outputMessage.serializeAndConsume(out, format);
                out.flush();
            } catch (Exception e) {
                throw new AxisFault(e);
            }
        } else {
            throw new AxisFault(Messages.getMessage("outMessageNull"));
        }
    }

    public void stop() {
    }
}
