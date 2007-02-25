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

package org.apache.axis2.transport.mail.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class POP3Server extends Thread {
    private static final Log log = LogFactory.getLog(POP3Server.class);
    private Storage st = null;
    private ServerSocket serverSocket;

    public POP3Server(Storage st, int port) throws AxisFault {
        this.st = st;

        try {
            synchronized (this) {
                serverSocket = new ServerSocket(port);
                log.info("Server started on port " + port);
            }
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                POP3Worker thread = new POP3Worker(socket, st);

                thread.start();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    public void stopServer() throws AxisFault {
        try {
            synchronized (this) {
                serverSocket.close();
            }
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }
}
