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

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.AbstractTransportSender;
import org.apache.axis.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MailTransportSender extends AbstractTransportSender {
    private String host;
    private String user;
    private String password;
    private String smtpPort = "25";

    private ByteArrayOutputStream byteArrayOutputStream; 

    public MailTransportSender() {

    }

    public void finalizeSendWithToAddress(MessageContext msgContext,OutputStream out)
        throws AxisFault {
            try {
                TransportOutDescription transportOut = msgContext.getTransportOut();
                user = Utils.getParameterValue(transportOut.getParameter(MailConstants.SMTP_USER));
                host = Utils.getParameterValue(transportOut.getParameter(MailConstants.SMTP_HOST));
                password =
                    Utils.getParameterValue(transportOut.getParameter(MailConstants.SMTP_PASSWORD));
                smtpPort = Utils.getParameterValue(transportOut.getParameter(MailConstants.SMTP_PORT));
                if (user != null && host != null && password != null && smtpPort != null) {
                    EMailSender sender = new EMailSender(user, host, smtpPort, password);

                    //TODO this is just a temporary hack, fix this to use input streams
                    
                
                    
                
                    String eprAddress = msgContext.getTo().getAddress();
                    int index = eprAddress.indexOf('/');
                    String subject = "";
                    String email = null;
                    if(index >= 0){
                        subject = eprAddress.substring(index+1);
                        email = eprAddress.substring(0,index);
                    }else{
                        email = eprAddress;
                    }
                
                    System.out.println(subject);
                    System.out.println(email);

                    sender.send(subject, email,new String(byteArrayOutputStream.toByteArray()));
                } else {
                    throw new AxisFault(
                        "user, port, host or password not set, "
                            + "   [user null = "
                            + (user == null)
                            + ", password null= "
                            + (password == null)
                            + ", host null "
                            + (host == null)
                            + ",port null "
                            + (smtpPort == null));

                }
            } catch (IOException e) {
                throw new AxisFault(e);
            }


    }

    public OutputStream startSendWithToAddress(MessageContext msgContext, OutputStream out) throws AxisFault {
        return out;
    }

    protected OutputStream openTheConnection(EndpointReference epr,MessageContext msgContext) throws AxisFault {
        byteArrayOutputStream = new ByteArrayOutputStream();
            return byteArrayOutputStream;
    }

    //Output Stream based cases are not supported 
    public OutputStream startSendWithOutputStreamFromIncomingConnection(
        MessageContext msgContext,
    OutputStream out)
        throws AxisFault {
        throw new UnsupportedOperationException();

    }
    public void finalizeSendWithOutputStreamFromIncomingConnection(
        MessageContext msgContext,OutputStream out)
        throws AxisFault {
    }
    /* (non-Javadoc)
     * @see org.apache.axis.transport.TransportSender#cleanUp()
     */
    public void cleanUp(MessageContext msgContext) throws AxisFault {
        // TODO Auto-generated method stub

    }

}
