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


package org.apache.axis2.transport.local;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.AbstractTransportSender;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LocalTransportSender extends AbstractTransportSender {
	
    private static final long serialVersionUID = -5245866514826025561L;
	private ByteArrayOutputStream out;
    private ByteArrayOutputStream response;

    public LocalTransportSender() {
    }

    public void cleanup(MessageContext msgContext) throws AxisFault {
    }

    public void finalizeSendWithOutputStreamFromIncomingConnection(MessageContext msgContext,
                                                                   OutputStream out)
            throws AxisFault {
        throw new UnsupportedOperationException();
    }

    public void finalizeSendWithToAddress(MessageContext msgContext, OutputStream out)
            throws AxisFault {
        try {
            InputStream in = new ByteArrayInputStream(this.out.toByteArray());

            response = new ByteArrayOutputStream();

            LocalTransportReceiver localTransportReceiver = new LocalTransportReceiver(this);

            localTransportReceiver.processMessage(in, msgContext.getTo());
            in.close();
            out.close();
            in = new ByteArrayInputStream(response.toByteArray());
            msgContext.setProperty(MessageContext.TRANSPORT_IN, in);
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.transport.AbstractTransportSender#openTheConnection(org.apache.axis2.addressing.EndpointReference)
     */
    protected OutputStream openTheConnection(EndpointReference epr, MessageContext msgContext)
            throws AxisFault {

        out = new ByteArrayOutputStream();

        return out;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.transport.AbstractTransportSender#startSendWithOutputStreamFromIncomingConnection(org.apache.axis2.context.MessageContext, java.io.Writer)
     */
    public OutputStream startSendWithOutputStreamFromIncomingConnection(MessageContext msgContext,
                                                                        OutputStream out)
            throws AxisFault {
        throw new UnsupportedOperationException();
    }

    public OutputStream startSendWithToAddress(MessageContext msgContext, OutputStream out)
            throws AxisFault {
        return out;
    }

    OutputStream getResponse() {
        return response;
    }

    public void stop() {
       
    }
}
