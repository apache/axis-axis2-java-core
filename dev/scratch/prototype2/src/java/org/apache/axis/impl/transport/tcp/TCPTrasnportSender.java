/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.impl.transport.tcp;

import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.AbstractTrasnportSender;
import org.apache.axis.addressing.EndpointReference;

import java.io.OutputStream;

public class TCPTrasnportSender extends AbstractTrasnportSender {
    protected OutputStream out;

    public TCPTrasnportSender(OutputStream out) {
        this.out = out;
    }

    protected OutputStream obtainOutPutStream(MessageContext msgContext) throws AxisFault {
        OutputStream out = (OutputStream) msgContext.getProperty(MessageContext.TRANSPORT_DATA);
        if (out == null) {
            throw new AxisFault("can not find the suffient information to find endpoint");
        } else {
            return out;
        }

    }

    protected OutputStream obtainOutPutStream(MessageContext msgContext, EndpointReference epr) {
        throw new UnsupportedOperationException("Addressing not suppotrted yet");
    }

    protected void finalizeSending() {
    }

    protected void startSending() {
    }

}
