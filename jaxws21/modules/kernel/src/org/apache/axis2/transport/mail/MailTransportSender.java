/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.transport.mail;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.util.Utils;

import javax.mail.PasswordAuthentication;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 0. For this profile we only care about SOAP 1.2
 * 1. There should be no mime text body in this case
 * 2. The SOAP envelope should be base64 encoded and marked with the following headers
 * SOAP 1.2
 * Content-Type: application/soap+xml; charset=UTF-8 ; action="soap-action-goes-here"
 * Content-Transfer-Encoding: base64
 * Content-Description: "/serviceName"
 * 3. The content-description is the logical name of the service. It should be quoted.
 * 4. The subject can be anything. Perhaps something like "SOAP Message" might be useful to people looking at the mail in a normal mail browser.
 * 5. If there are attachments the there will be a mime multipart. There should only be one part with content-type: application/soap+xml.
 * 6. The service URL will be created as mailto:paul@wso2.com?X-Service-Path=/axis2/services/MyService
 * or paul@wso2.com?/axis2/services/MyService
 * <p/>
 * <p/>
 * Example without attachments
 * ========================
 * From: rm_client@lenio.dk
 * To: lenioserver@oiositest.dk
 * Message-ID: <8868170.01165394158287.JavaMail.hgk@hans-guldager-knudsens-computer.local>
 * Subject: ANYTHING
 * MIME-Version: 1.0
 * Content-Type: application/soap+xml; charset=UTF-8 ; action="http://rep.oio.dk/oiosi/IMessageHandler/RequestRespondRequest"
 * Content-Transfer-Encoding: base64
 * Content-Description: /my/service/urlpath
 * <p/>
 * PD94bWwgdmVyc2lvbj0nMS4wJyBlbmNvZGluZz0ndXRmLTgnPz48c29hcGVudjpFbnZlbG9wZSB4
 * Y3VtZW50YXRpb24uPC9EZXNjcmlwdGlvbj48L05vdGlmaWNhdGlvbj48L3NvYXBlbnY6Qm9keT48
 * L3NvYXBlbnY6RW52ZWxvcGU+
 * <p/>
 * ========================
 * <p/>
 * Example with attachments
 * <p/>
 * ========================
 * From: rm_client@lenio.dk
 * To: lenioserver@oiositest.dk
 * Message-ID: <8868170.01165394158287.JavaMail.hgk@hans-guldager-knudsens-computer.local>
 * Subject: ANYTHING
 * MIME-Version: 1.0
 * content-type: multipart/mixed; boundary=--boundary_0_9fdec710-2336-4dc9-8bcd-45f2c06cf605
 * <p/>
 * ----boundary_0_9fdec710-2336-4dc9-8bcd-45f2c06cf605
 * Content-Type: application/soap+xml; charset=UTF-8 ; action="http://rep.oio.dk/oiosi/IMessageHandler/RequestRespondRequest"
 * Content-Transfer-Encoding: base64
 * Content-Description: /my/service/urlpath
 * <p/>
 * PD94bWwgdmVyc2lvbj0nMS4wJyBlbmNvZGluZz0ndXRmLTgnPz48c29hcGVudjpFbnZlbG9wZSB4
 * Y3VtZW50YXRpb24uPC9EZXNjcmlwdGlvbj48L05vdGlmaWNhdGlvbj48L3NvYXBlbnY6Qm9keT48
 * L3NvYXBlbnY6RW52ZWxvcGU+
 * <p/>
 * ----boundary_0_9fdec710-2336-4dc9-8bcd-45f2c06cf605
 * ----boundary_0_9fdec710-2336-4dc9-8bcd-45f2c06cf605
 * content-type: application/octet-stream
 * content-transfer-encoding: base64
 * <p/>
 * PHM6RW52ZWxvcGUgeG1sbnM6cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMy8wNS9zb2FwLWVu
 * dmVsb3BlIiB4bWxuczphPSJodHRwOi8vd3d3LnczLm9yZy8yMDA1LzA4L2FkZHJlc3Npbmci
 * IHhtbG5zOnU9Imh0dHA6Ly9kb2NzLm9hc2lzLW9wZW4ub3JnL3dzcy8yMDA0LzAxL29hc2lz
 * LTIwMDQwMS13c3Mtd3NzZWN1cml0eS11dGlsaXR5LTEuMC54c2QiPjxzOkhlYWRlcj48YTpB
 * Y3Rpb24gczptdXN0VW5kZXJzdGFuZD0iMSI+aHR0cDovL3JlcC5vaW8uZGsvb2lvc2kvSU1l
 * c3NhZ2VIYW5kbGVyL1JlcXVlc3RSZXNwb25kUmVxdWVzdDwvYTpBY3Rpb24+PGE6TWVzc2Fn
 */
public class MailTransportSender extends AbstractHandler implements TransportSender {

    /* smtpProperties holds all the parameters needed to Java Mail. This will be filled either from Axis2.xml or
       from runtime.
     */
    private final java.util.Properties smtpProperties = new java.util.Properties();

    private PasswordAuthentication passwordAuthentication;

    private ByteArrayOutputStream byteArrayOutputStream;

    private final static String NAME = "MailTransportSender";

    public MailTransportSender() {
        init(new HandlerDescription(NAME));
    }


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
                                                        "Parameter name and value"));

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


    public void cleanup(MessageContext msgContext) throws AxisFault {
    }

    private void mailProperties(MessageContext msgContext) {
        Object obj = msgContext.getProperty(Constants.MAIL_SMTP);
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
            } else if (obj instanceof java.util.Properties) {
                smtpProperties.clear();
                java.util.Properties props = (java.util.Properties) obj;
                smtpProperties.putAll(props);
            }
        }

    }

    public void sendMimeMessage(MessageContext msgContext) throws AxisFault {
        try {
            // Override with runtime settings
            mailProperties(msgContext);

            EMailSender sender = new EMailSender();
            sender.setOutputStream(byteArrayOutputStream);
            sender.setMessageContext(msgContext);
            sender.setProperties(smtpProperties);
            sender.setPasswordAuthentication(passwordAuthentication);

            // In mail char set is what is being used. Charset encoding is not what is expected here.
            String charSet =
                    (String) msgContext.getProperty(
                            org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING);
            if (charSet == null) {
                charSet = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }

            OMOutputFormat format = new OMOutputFormat();

            format.setSOAP11(msgContext.isSOAP11());
            format.setCharSetEncoding(charSet);
            // Check if msg is 'In-Reply-To' received message
            OutTransportInfo transportInfo = (OutTransportInfo) msgContext
                    .getProperty(org.apache.axis2.Constants.OUT_TRANSPORT_INFO);

            if (transportInfo != null && transportInfo instanceof MailBasedOutTransportInfo) {
                MailBasedOutTransportInfo mailTransportInfo =
                        (MailBasedOutTransportInfo) transportInfo;

                sender.setInReplyTo(mailTransportInfo.getInReplyTo());
                sender.setFrom(mailTransportInfo.getFrom());
            }
            sender.setFormat(format);

            sender.send();

        } catch (IOException e) {
            throw new AxisFault(e.getMessage(),e);
        }
    }

    public void writeMimeMessage(MessageContext msgContext, OutputStream out) throws AxisFault {
        try {
            OMOutputFormat format = new OMOutputFormat();
            MessageFormatter messageFormatter = TransportUtils
                    .getMessageFormatter(msgContext);
            format.setDoOptimize(msgContext.isDoingMTOM());
            //Set to null so that the code will not fail on 7bit.
            format.setCharSetEncoding(null);
            messageFormatter.writeTo(msgContext, format, out, false);
            out.flush();
        } catch (Exception e) {
            throw new AxisFault(e.getMessage(),e);
        }
    }

    public void stop() {
    }

    /**
     * @param msgContext
     * @return
     * @throws AxisFault
     */
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {

        msgContext.setDoingMTOM(HTTPTransportUtils.doWriteMTOM(msgContext));
        msgContext.setDoingSwA(HTTPTransportUtils.doWriteSwA(msgContext));

        byteArrayOutputStream = new ByteArrayOutputStream();
        writeMimeMessage(msgContext, byteArrayOutputStream);

        sendMimeMessage(msgContext);

        return InvocationResponse.CONTINUE;
    }
}
