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

package org.apache.axis.engine;

import java.io.OutputStream;

import org.apache.axis.context.MessageContext;
import org.apache.axis.transport.http.HTTPTransportSender;
import org.apache.axis.transport.tcp.TCPTrasnportSender;


public class TransportSenderLocator {
    public static final String TRANSPORT_TCP = "tcp";
    public static final String TRANSPORT_HTTP = "http";

    public static TransportSender locateTransPortSender(MessageContext msgContext) throws AxisFault {
        String type = (String) msgContext.getProperty(MessageContext.TRANSPORT_TYPE);
        OutputStream out = (OutputStream) msgContext.getProperty(MessageContext.TRANSPORT_DATA);

        if (TransportSenderLocator.TRANSPORT_TCP.equals(type)) {
            if (out != null) {
                return new TCPTrasnportSender(out);
            } else {
                throw new AxisFault("if TCP trsport used there should be a propoerty named " + MessageContext.TRANSPORT_DATA);
            }
        } else if (TransportSenderLocator.TRANSPORT_HTTP.equals(type)) {
            if (out != null) {
                return new HTTPTransportSender(out);
            } else {
                throw new AxisFault("if TCP trsport used there should be a propoerty named " + MessageContext.TRANSPORT_DATA);
            }
        }
        throw new AxisFault("No tranport found");
    }
}
