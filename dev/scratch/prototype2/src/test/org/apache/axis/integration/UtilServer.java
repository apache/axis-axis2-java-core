/*
 * Copyright 2003,2004 The Apache Software Foundation.
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

package org.apache.axis.integration;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineUtils;
import org.apache.axis.impl.description.AxisService;
import org.apache.axis.impl.transport.http.SimpleHTTPReceiver;

public class UtilServer {
    private static int count = 0;
    private static SimpleHTTPReceiver reciver;

    public static synchronized void deployService(AxisService service)
        throws AxisFault {
        reciver.getEngineReg().addService(service);
    }

    public static synchronized void start() throws IOException {
        if (count == 0) {
            reciver = new SimpleHTTPReceiver("target/test-resources/samples/");

            ServerSocket serverSoc = null;
            serverSoc = new ServerSocket(EngineUtils.TESTING_PORT);
            reciver.setServerSocket(serverSoc);
            Thread thread = new Thread(reciver);
            thread.setDaemon(true);

            try {
                thread.start();
            } finally {

            }
        } else {
            count++;
        }
    }

    public static synchronized void stop() {
        if (count == 1) {
            reciver.stop();
        } else {
            count--;
        }
    }
}
