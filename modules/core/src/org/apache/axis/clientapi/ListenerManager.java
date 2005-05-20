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
 *
 *  Runtime state of the engine
 */
package org.apache.axis.clientapi;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.axis.Constants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.TransportReceiver;
import org.apache.axis.transport.http.SimpleHTTPServer;
import org.apache.axis.transport.tcp.TCPServer;

public class ListenerManager {

    public static TransportReceiver httpListener;
    public static TransportReceiver tcpListener;
    public static TransportReceiver mailListener;
    public static TransportReceiver jmsListener;
    public static ConfigurationContext configurationContext;

    public static final void makeSureStarted(
        String transport,
        ConfigurationContext configurationContext)
        throws AxisFault {
        try {
            ListenerManager.configurationContext = configurationContext;
            if (Constants.TRANSPORT_HTTP.equals(transport) && httpListener == null) {
                httpListener = new SimpleHTTPServer(configurationContext, new ServerSocket(6060));
                httpListener.start();
            } else if (Constants.TRANSPORT_JMS.equals(transport) && jmsListener == null) {
                throw new UnsupportedOperationException();
            } else if (Constants.TRANSPORT_MAIL.equals(transport) && mailListener == null) {
                throw new UnsupportedOperationException();
            } else if (Constants.TRANSPORT_TCP.equals(transport) && tcpListener == null) {
                tcpListener = new TCPServer(7070, configurationContext);
            }
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    public static EndpointReference replyToEPR(String serviceName, String transport) throws AxisFault {
        ListenerManager.configurationContext = configurationContext;
        if (Constants.TRANSPORT_HTTP.equals(transport) && httpListener != null) {
            return httpListener.replyToEPR(serviceName);
        } else if (Constants.TRANSPORT_JMS.equals(transport) && jmsListener != null) {
            return jmsListener.replyToEPR(serviceName);
        } else if (Constants.TRANSPORT_MAIL.equals(transport) && mailListener != null) {
            return mailListener.replyToEPR(serviceName);
        } else if (Constants.TRANSPORT_TCP.equals(transport) && tcpListener != null) {
            return tcpListener.replyToEPR(serviceName);
        }
        throw new AxisFault(
            "Calling method before starting the with makeSureStarted(..) Listener transport =  "
                + transport);

    }

}
