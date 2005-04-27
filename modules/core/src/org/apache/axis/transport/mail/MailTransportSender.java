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

import java.io.Writer;
import java.net.Socket;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.AbstractTransportSender;

public class MailTransportSender extends AbstractTransportSender {
    protected Writer out;

    private Socket socket;

    public MailTransportSender() {

    }

 

    protected Writer obtainOutputStream(MessageContext msgContext,
            EndpointReference epr) throws AxisFault {
        throw new UnsupportedOperationException();
    }

     
    protected void startSending(MessageContext msgContext) throws AxisFault {
        try {
            Writer writer = (Writer) msgContext
                    .getProperty(MessageContext.TRANSPORT_WRITER);
            startSending(msgContext, writer);
        } catch (Exception e) {
            throw new AxisFault(e.getMessage());
        }
    }

    protected void startSending(MessageContext msgContext, Writer writer) throws AxisFault {
        try {
            writer.write("Content-Type: text/plain; charset=us-ascii\n");
            writer.write("Content-Transfer-Encoding: 7bit\n");
            writer
                    .write("Accept: application/soap+xml, application/dime, multipart/related, text\n");
            //writer.write("MIME-Version: 1.0\n");
            writer.write("User-Agent: Axis2 M1\n");
            writer.write("Cache-Control: no-cache\n");
            writer.write("Pragma: no-cache\n");
            writer.write("Subject: Re:"
                    + msgContext.getProperty(MailConstants.SUBJECT) + "\n\n");
        } catch (Exception e) {
            throw new AxisFault(e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.axis.transport.AbstractTransportSender#finalizeSendWithOutputStreamFromIncomingConnection(org.apache.axis.context.MessageContext, java.io.Writer)
     */
    public void finalizeSendWithOutputStreamFromIncomingConnection(
        MessageContext msgContext,
        Writer writer)
        throws AxisFault {
            throw new UnsupportedOperationException();

    }

    /* (non-Javadoc)
     * @see org.apache.axis.transport.AbstractTransportSender#finalizeSendWithToAddress(org.apache.axis.context.MessageContext, org.apache.axis.addressing.EndpointReference, java.io.Writer)
     */
    public void finalizeSendWithToAddress(
        MessageContext msgContext,
        EndpointReference epr,
        Writer writer)
        throws AxisFault {
            throw new UnsupportedOperationException();

    }

    /* (non-Javadoc)
     * @see org.apache.axis.transport.AbstractTransportSender#openTheConnection(org.apache.axis.addressing.EndpointReference)
     */
    protected Writer openTheConnection(EndpointReference epr) throws AxisFault {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.axis.transport.AbstractTransportSender#startSendWithOutputStreamFromIncomingConnection(org.apache.axis.context.MessageContext, java.io.Writer)
     */
    public void startSendWithOutputStreamFromIncomingConnection(
        MessageContext msgContext,
        Writer writer)
        throws AxisFault {
            throw new UnsupportedOperationException();

    }

    /* (non-Javadoc)
     * @see org.apache.axis.transport.AbstractTransportSender#startSendWithToAddress(org.apache.axis.context.MessageContext, org.apache.axis.addressing.EndpointReference, java.io.Writer)
     */
    public void startSendWithToAddress(
        MessageContext msgContext,
        EndpointReference epr,
        Writer writer)
        throws AxisFault {
            throw new UnsupportedOperationException();

    }

}

