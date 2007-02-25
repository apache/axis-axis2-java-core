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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SMTPServer extends Thread {
    private static final Log log = LogFactory.getLog(SMTPServer.class);
    private boolean actAsMailet = false;
    private boolean running = false;
    private ConfigurationContext configurationContext;
    private int port;
    private ServerSocket ss;
    private Storage st;

    public SMTPServer(Storage st, int port) {
        this.st = st;
        this.port = port;
        actAsMailet = false;
    }

    public SMTPServer(Storage st, ConfigurationContext configurationContext, int port) {
        this.st = st;
        this.configurationContext = configurationContext;
        this.port = port;
        actAsMailet = true;
    }

    public void run() {
        runServer();
    }

    public void runServer() {
        try {
            synchronized (this) {
                running = true;
                ss = new ServerSocket(port);
                log.info("SMTP Server started on port " + port);
            }
        } catch (IOException ex) {
            log.info(ex.getMessage());
        }

        while (running) {
            try {

                // wait for a client
                Socket socket = ss.accept();
                SMTPWorker thread = null;

                if (actAsMailet) {
                    thread = new SMTPWorker(socket, st, configurationContext);
                } else {
                    thread = new SMTPWorker(socket, st);
                }

                thread.start();
            } catch (IOException ex) {
                if (running) {
                    log.info(ex.getMessage());
                }
            }
        }
    }

    public void stopServer() throws AxisFault {
        try {
            synchronized (this) {
                running = false;
                ss.close();
                ss = null;
            }
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }
}
