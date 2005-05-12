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
import org.apache.axis.context.SystemContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.http.SimpleHTTPServer;

public class ListenerManager {
    private SystemContext systemContext;
    private boolean started = false;
    private int numberOfserver = 0;
    private SimpleHTTPServer simpleHttpServer;
    private ServerSocket scoket;

    public ListenerManager(SystemContext engineContext) {
        this.systemContext = engineContext;
    }

    public void makeSureStarted() throws AxisFault {
        synchronized (ListenerManager.class) {
            try {
                if (started == false) {

                    scoket = new ServerSocket(6060);
                    simpleHttpServer = new SimpleHTTPServer(systemContext, scoket);
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
    public SystemContext getSystemContext() {
        return systemContext;
    }

    public void stopAServer() {
        numberOfserver--;
        if (numberOfserver == 0) {
            simpleHttpServer.stop();
        }
    }

    public EndpointReference replyToEPR(String serviceName) {
        return new EndpointReference(
            AddressingConstants.WSA_REPLY_TO,
            "http://127.0.0.1:" + (scoket.getLocalPort() + 1) + "/axis/services/" + serviceName);
    }
    /**
     * @param context
     */
    public void setSystemContext(SystemContext context) {
        systemContext = context;
    }

}
