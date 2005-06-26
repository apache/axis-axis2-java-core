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
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.description.TransportInDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.TransportListener;

public class ListenerManager {

    public static int port = 6059;
    public static HashMap listeners = new HashMap();
    public static ConfigurationContext configurationContext;

    public static final void makeSureStarted(
        String transport,
        ConfigurationContext configurationContext)
        throws AxisFault {
        if (ListenerManager.configurationContext != null && configurationContext != ListenerManager.configurationContext) {
            throw new AxisFault("Only One ConfigurationContext Instance we support at the Client Side");
        }

        ListenerManager.configurationContext = configurationContext;
        TransportListnerState tsState = (TransportListnerState) listeners.get(transport);
        if (tsState == null) {
            TransportInDescription tranportIn =
                configurationContext.getAxisConfiguration().getTransportIn(new QName(transport));
            TransportListener listener = tranportIn.getReciever();
//            listener.init(configurationContext, tranportIn);
            listener.start();
            tsState = new TransportListnerState(listener);
            listeners.put(transport,tsState);
        }
        tsState.waitingCalls++;
    }

    public static final void stop(String transport) throws AxisFault {
        TransportListnerState tsState = (TransportListnerState) listeners.get(transport);
        if (tsState != null) {
            tsState.waitingCalls--;
            if (tsState.waitingCalls == 0) {
                tsState.listener.stop();
            }
        }
    }

    public static EndpointReference replyToEPR(String serviceName, String transport)
        throws AxisFault {
        TransportListnerState tsState = (TransportListnerState) listeners.get(transport);
        if (tsState != null) {
            return tsState.listener.replyToEPR(serviceName);
        } else {
            throw new AxisFault(
                "Calling method before starting the with makeSureStarted(..) Listener transport =  "
                    + transport);

        }

    }

    public int getPort() {
        port++;
        return port;
    }

    public static class TransportListnerState {
        public TransportListnerState(TransportListener listener) {
            this.listener = listener;
        }
        public int waitingCalls = 0;
        public TransportListener listener;
    }

    public static ServerSocket openSocket(int port) throws AxisFault  {
        for (int i = 0; i < 5; i++) {
            try {
                return new ServerSocket(port + i);
            } catch (IOException e) {
            }
        }
        throw new AxisFault("failed to open the scoket");
    }

}
