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
package org.apache.axis2.clientapi;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.AxisFault;

/**
 * This manages the Listners, this class havely depends on the static constructs and should be 
 * re-architectured. This class only allow the Client to keep one ConfigurationContext in a given JVM.
 */
public class ListenerManager {

    public static int port = 6059;
    public static HashMap listeners = new HashMap();
    public static ConfigurationContext configurationContext;

    /**
     * This Method starts a Listenet if it is not already started. But only a one listener started for
     * a given one transport.
     * @param transport
     * @param configurationContext
     * @throws AxisFault
     */
    public static synchronized final void makeSureStarted(String transport,
                                             ConfigurationContext configurationContext)
            throws AxisFault {
        if (ListenerManager.configurationContext != null &&
                configurationContext != ListenerManager.configurationContext) {
            throw new AxisFault(
                    "Only One ConfigurationContext Instance we support at the Client Side");
        }

        ListenerManager.configurationContext = configurationContext;
        TransportListnerState tsState = (TransportListnerState) listeners.get(
                transport);
        if (tsState == null) {
            //means this transport not yet started, start the transport
            TransportInDescription tranportIn =
                    configurationContext.getAxisConfiguration().getTransportIn(
                            new QName(transport));
            TransportListener listener = tranportIn.getReciever();
            listener.start();
            tsState = new TransportListnerState(listener);
            listeners.put(transport, tsState);
        }
        tsState.waitingCalls++;
    }

    public static synchronized final void stop(String transport) throws AxisFault {
        TransportListnerState tsState = (TransportListnerState) listeners.get(
                transport);
        if (tsState != null) {
            tsState.waitingCalls--;
            if (tsState.waitingCalls == 0) {
                tsState.listener.stop();
            }
        }
    }

    /**
     * @param serviceName
     * @param transport
     * @return reply to URL return will direct the response for the message to the given transport listener
     * @throws AxisFault
     */
    public static EndpointReference replyToEPR(String serviceName,
                                               String transport)
            throws AxisFault {
        TransportListnerState tsState = (TransportListnerState) listeners.get(
                transport);
        if (tsState != null) {
            return tsState.listener.replyToEPR(serviceName);
        } else {
            throw new AxisFault(Messages.getMessage("replyNeedStarting",transport));
        }

    }

    public int getPort() {
        port++;
        return port;
    }
    /**
     * Class keep information about the listener for a given transport
     */
    public static class TransportListnerState {
        public TransportListnerState(TransportListener listener) {
            this.listener = listener;
        }

        public int waitingCalls = 0;
        public TransportListener listener;
    }

    /**
     * this method control the number of server sockets kept open
     */
    public static ServerSocket openSocket(int port) throws AxisFault {
        for (int i = 0; i < 5; i++) {
            try {
                return new ServerSocket(port + i);
            } catch (IOException e) {
            }
        }
        throw new AxisFault(Messages.getMessage("filedToOpenSocket"));
    }

}
