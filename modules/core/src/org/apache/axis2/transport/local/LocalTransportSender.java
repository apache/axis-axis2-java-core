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

import java.io.*;


public class LocalTransportSender extends AbstractTransportSender {
    private ByteArrayOutputStream out;

    public LocalTransportSender() {

    }

    public OutputStream startSendWithToAddress(MessageContext msgContext,
                                               OutputStream out) throws AxisFault {
        return out;
    }

    public void finalizeSendWithToAddress(MessageContext msgContext,
                                          OutputStream out)
            throws AxisFault {
        try {
            InputStream in = new ByteArrayInputStream(this.out.toByteArray());
            LocalTransportReceiver localTransportReceiver = new LocalTransportReceiver();
            localTransportReceiver.processMessage(in, msgContext.getTo());
            in.close();
            out.close();
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.transport.AbstractTransportSender#openTheConnection(org.apache.axis2.addressing.EndpointReference)
     */
    protected OutputStream openTheConnection(EndpointReference epr,
                                             MessageContext msgContext) throws AxisFault {
        //out = new PipedOutputStream();
        out = new ByteArrayOutputStream();
        return out;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.transport.AbstractTransportSender#startSendWithOutputStreamFromIncomingConnection(org.apache.axis2.context.MessageContext, java.io.Writer)
     */
    public OutputStream startSendWithOutputStreamFromIncomingConnection(
            MessageContext msgContext,
            OutputStream out)
            throws AxisFault {
        throw new UnsupportedOperationException();

    }

    public void finalizeSendWithOutputStreamFromIncomingConnection(
            MessageContext msgContext, OutputStream out)
            throws AxisFault {
        throw new UnsupportedOperationException();

    }

    public void cleanUp(MessageContext msgContext) throws AxisFault {
    }

}
