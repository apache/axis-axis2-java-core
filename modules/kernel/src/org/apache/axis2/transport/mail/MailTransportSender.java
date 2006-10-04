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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.AbstractTransportSender;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.util.Utils;

import javax.mail.PasswordAuthentication;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class MailTransportSender extends AbstractTransportSender {

    /* smtpProperties holds all the parameters needed to Java Mail. This will be filled either from Axis2.xml or
       from runtime.
     */
    private java.util.Properties smtpProperties = new java.util.Properties();

    private PasswordAuthentication passwordAuthentication;

    private ByteArrayOutputStream byteArrayOutputStream;
    // assosiation with OMOutputFormat
    private OMOutputFormat format = new OMOutputFormat();


    public void init(ConfigurationContext configurationContext,
                     TransportOutDescription transportOut)
            throws AxisFault {

        ArrayList mailParameters = transportOut.getParameters();

        String password = "";
        String username = "";

        for (Iterator iterator = mailParameters.iterator(); iterator.hasNext();) {
            Parameter param = (Parameter) iterator.next();
            String paramKey = param.getName();
            String paramValue = Utils.getParameterValue(param);
            if (paramKey == null || paramValue == null) {
                throw new AxisFault(Messages.getMessage("canNotBeNull",
                                                        "Parameter name nor value should be null"));

            }
            smtpProperties.setProperty(paramKey, paramValue);
            if (paramKey.equals(Constants.SMTP_USER)) {
                username = paramValue;
            }
            if (paramKey.equals(Constants.SMTP_USER_PASSWORD)) {
                password = paramValue;
            }

        }
        passwordAuthentication = new PasswordAuthentication(username, password);
    }

    public MailTransportSender() {
    }

    public void cleanup(MessageContext msgContext) throws AxisFault {
    }

    public void finalizeSendWithOutputStreamFromIncomingConnection(MessageContext msgContext,
                                                                   OutputStream out)
            throws AxisFault {
    }

    private void runtimeMailParameterSetting(MessageContext msgContext) {
        Object obj = msgContext.getProperty(HTTPConstants.MAIL_SMTP);
        if (obj != null) {
            // Overide the axis2.xml cofiguration setting
            if (obj instanceof HttpTransportProperties.MailProperties) {
                HttpTransportProperties.MailProperties props =
                        (HttpTransportProperties.MailProperties) obj;
                smtpProperties.clear();
                smtpProperties.putAll(props.getProperties());
                String username = (String) smtpProperties.get(Constants.SMTP_USER);
                String passwd = props.getPassword();
                passwordAuthentication = new PasswordAuthentication(username, passwd);
            }
        }

    }

    public void finalizeSendWithToAddress(MessageContext msgContext, OutputStream out)
            throws AxisFault {
        try {
            // Override with runtime settings
            runtimeMailParameterSetting(msgContext);

            EMailSender sender = new EMailSender();
            sender.setMessageContext(msgContext);
            sender.setProperties(smtpProperties);
            sender.setPasswordAuthentication(passwordAuthentication);

            String eprAddress = msgContext.getTo().getAddress();

            // In mail char set is what is being used. Charset encoding is not what is expected here.
            String charSet =
                    (String) msgContext.getProperty(
                            org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING);
            if (charSet == null) {
                charSet =
                        MessageContext.DEFAULT_CHAR_SET_ENCODING;// Since we are deleaing only SOAP and XML messages here
            }
            format.setSOAP11(msgContext.isSOAP11());
            format.setCharSetEncoding(charSet);

            int mailNameIndex = eprAddress.indexOf("mail:");
            if (mailNameIndex > -1) {
                eprAddress = eprAddress.substring(mailNameIndex + 5);
            }
            int index = eprAddress.indexOf('/');
            String subject = "";
            String email;

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
                        format);

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
                format.setCharSetEncoding(
                        null); //Set to null so that the code will not fail on 7bit.
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
