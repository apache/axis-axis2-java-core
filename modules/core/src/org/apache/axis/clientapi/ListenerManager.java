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

import java.net.ServerSocket;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.EngineContext;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineConfigurationImpl;
import org.apache.axis.transport.http.SimpleHTTPServer;

public class ListenerManager {
    private static EngineContext engineContext;
    private static boolean started = false;
    private static int numberOfserver = 0;
    private static SimpleHTTPServer simpleHttpServer;
    private static ServerSocket scoket;

    public static void makeSureStarted() throws AxisFault {
        synchronized (ListenerManager.class) {
            try {
                if (started == false) {
                    engineContext =
                        new EngineContext(new EngineConfigurationImpl(new AxisGlobal()));
                    scoket = new ServerSocket(6060);
                    simpleHttpServer = new SimpleHTTPServer(engineContext, scoket);
                    simpleHttpServer.start();
                    started = true;
                    numberOfserver++;
                }
            } catch (Exception e) {
                throw new AxisFault(e.getMessage(), e);
            }

        }

    }
    /**
     * @return
     */
    public static EngineContext getEngineContext() {
        return engineContext;
    }

    public static void stopAServer() {
        numberOfserver--;
        if (numberOfserver == 0) {
            simpleHttpServer.stop();
        }
    }
    
    public static EndpointReference replyToEPR(String serviceName){
        return new EndpointReference(AddressingConstants.WSA_REPLY_TO,"http://127.0.0.1:"+ (scoket.getLocalPort())+"/axis/services/"+serviceName);
    }
}
