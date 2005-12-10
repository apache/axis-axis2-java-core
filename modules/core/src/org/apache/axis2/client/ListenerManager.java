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

package org.apache.axis2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.TransportListener;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * This class manages the listeners and depends heavily on static constructs and should be
 * re-architectured. It allows the client to initialize only one ConfigurationContext in a given JVM.
 */
public class ListenerManager {

    public static int port = 6059;
    public static Map configurationContextMap = new HashMap();

    /**
     * Starts a listener for a given transport if it has not already started.
     *
     * @param transport
     * @param configurationContext
     * @throws AxisFault
     */
    public static synchronized void makeSureStarted(String transport,
                                                          ConfigurationContext configurationContext)
            throws AxisFault {
        // If this config context is in the map, that means we already have a listener for that
        // config context
        HashMap listeners = (HashMap) configurationContextMap.get(configurationContext);
        if (listeners == null) {
            listeners = new HashMap();
            configurationContextMap.put(configurationContext, listeners);
        } else {
            TransportListenerState tsState = (TransportListenerState) listeners.get(transport);
            if (tsState != null) {
                tsState.waitingCalls++;
                return;
            }
        }

        //means this transport not yet started, start the transport
        TransportInDescription tranportIn =
                configurationContext.getAxisConfiguration().getTransportIn(
                        new QName(transport));
        TransportListener listener = tranportIn.getReceiver();
        listener.start();
        TransportListenerState tsState = new TransportListenerState(listener);
        listeners.put(transport, tsState);

        tsState.waitingCalls++;
    }

    public static synchronized void stop(ConfigurationContext configurationContext, String transport) throws AxisFault {
        HashMap listeners = (HashMap) configurationContextMap.get(configurationContext);

        if (listeners != null) {
            TransportListenerState tsState = (TransportListenerState) listeners.get(transport);
            if (tsState != null) {
                tsState.waitingCalls--;
                if (tsState.waitingCalls == 0) {
                    tsState.listener.stop();
                    listeners.remove(transport);
                }
            }
        }
    }

    /**
     * Returns the replyTo endpoint reference for the servicename/transport combination.
     *
     * @param serviceName
     * @param transport
     * @return endpoint reference
     * @throws AxisFault
     */
    public static EndpointReference replyToEPR(ConfigurationContext configurationContext,
                                               String serviceName,
                                               String transport)
            throws AxisFault {
        HashMap listeners = (HashMap) configurationContextMap.get(configurationContext);
        if (listeners != null) {
            TransportListenerState tsState = (TransportListenerState) listeners.get(
                    transport);
            if (tsState != null) {
                return tsState.listener.getReplyToEPR(serviceName);
            } else {
                throw new AxisFault(Messages.getMessage("replyNeedStarting", transport));
            }
        } else {
            throw new AxisFault("Can not find listeners for " + transport + " for the given config" +
                    " context " + configurationContext + ". So can not provide a replyTo epr here. ");
        }
    }

    /**
     * This class keeps information about the listener for a given transport.
     */
    public static class TransportListenerState {
        public TransportListenerState(TransportListener listener) {
            this.listener = listener;
        }

        public int waitingCalls = 0;
        public TransportListener listener;
    }

    /**
     * Controls the number of server sockets kept open.
     */
    public static ServerSocket openSocket(int port) throws AxisFault {
        for (int i = 0; i < 5; i++) {
            try {
                return new ServerSocket(port + i);
            } catch (IOException e) {
                // What I'm gonna do here. Try again. 
            }
        }
        throw new AxisFault(Messages.getMessage("failedToOpenSocket"));
    }

}
