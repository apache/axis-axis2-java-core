/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.testutils;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ThreadLocalRandom;

public final class PortAllocator {
    private PortAllocator() {}
    
    /**
     * Allocate a TCP port.
     *
     * @return the allocated port
     */
    public static int allocatePort() {
        try {
            ServerSocket ss = new ServerSocket(0);
            int port = ss.getLocalPort();
            ss.close();

            // Add retry mechanism to reduce race condition where another process
            // grabs the port between close() and actual bind
            for (int retry = 0; retry < 5; retry++) {
                try {
                    // Test if the port is still available by trying to bind again
                    ServerSocket testSocket = new ServerSocket(port);
                    testSocket.close();

                    // Add small random delay to reduce parallel test conflicts
                    if (retry > 0) {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
                    }

                    return port;
                } catch (IOException bindEx) {
                    // Port already taken, try allocating a new one
                    ss = new ServerSocket(0);
                    port = ss.getLocalPort();
                    ss.close();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            return port;
        } catch (IOException ex) {
            throw new Error("Unable to allocate TCP port", ex);
        }
    }
}
