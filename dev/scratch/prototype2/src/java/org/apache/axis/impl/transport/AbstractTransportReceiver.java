/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.impl.transport;


import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This is a simple implementation of an HTTP server for processing
 * SOAP requests via Apache's xml-axis.  This is not intended for production
 * use.  Its intended uses are for demos, debugging, and performance
 * profiling.
 * <p/>
 * Note this classes uses static objects to provide a thread pool, so you should
 * not use multiple instances of this class in the same JVM/classloader unless
 * you want bad things to happen at shutdown.
 *
 * @author Sam Ruby (ruby@us.ibm.com)
 * @author Rob Jellinghaus (robj@unrealities.com)
 * @author Alireza Taherkordi (a_taherkordi@users.sourceforge.net)
 */
public abstract class AbstractTransportReceiver implements Runnable {
    protected Log log =
            LogFactory.getLog(AbstractTransportReceiver.class.getName());
    protected AxisEngine engine = null;
    protected ServerSocket serverSocket;
    protected Socket socket = null;
    /**
     * are we stopped?
     * latch to true if stop() is called
     */
    private boolean stopped = false;


    public AbstractTransportReceiver(AxisEngine myAxisServer) {
        this.engine = myAxisServer;

    }

    /**
     * stop the server if not already told to.
     *
     * @throws Throwable
     */
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    }

    /**
     * Accept requests from a given TCP port and send them through the
     * Axis engine for processing.
     */
    public void run() {
        try {
            try {
                // Accept and process requests from the socket
                while (!stopped) {
                    try {
                        this.socket = serverSocket.accept();

                    } catch (java.io.InterruptedIOException iie) {
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                        break;
                    }
                    if (socket != null) {
                        MessageContext msgContext = parseTheTransport(engine, socket.getInputStream());
                        storeOutputInfo(msgContext, socket.getOutputStream());
                        engine.receive(msgContext);
                        this.socket.close();
                        this.socket = null;
                    }
                }
            } catch (AxisFault e) {
                log.error(e);
                this.socket.close();
                this.socket = null;
            }
        } catch (IOException e) {
            log.error(e);
        }
        stop();
        log.info("Simple Axis Server Quit");
    }


    /**
     * Obtain the serverSocket that that SimpleAxisServer is listening on.
     */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * Set the serverSocket this server should listen on.
     * (note : changing this will not affect a running server, but if you
     * stop() and then start() the server, the new socket will be used).
     */
    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }


    /**
     * Start this server as a NON-daemon.
     */
    public void start() throws Exception {
        run();
    }

    /**
     * Stop this server. Can be called safely if the system is already stopped,
     * or if it was never started.
     * <p/>
     * This will interrupt any pending accept().
     */
    public void stop() {
        log.info("stop called");
        //recognise use before we are live
        if (stopped) {
            return;
        }
        /*
         * Close the server socket cleanly, but avoid fresh accepts while
         * the socket is closing.
         */
        stopped = true;

        try {
            if (serverSocket != null) {
                serverSocket.close();
//                while(socket != null){
//                    try {
//                        //make sure all sockets closed by the time 
//                        //else we got in to lot of trouble testing
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e1) {
//                        log.error(e1);
//                    }
//                }
            }
        } catch (IOException e) {
            log.info(e);
        } finally {
            serverSocket = null;
        }
        log.info("Simple Axis Server Quits");
    }

    protected abstract MessageContext parseTheTransport(AxisEngine engine, InputStream in) throws AxisFault;

    protected abstract void storeOutputInfo(MessageContext msgctx, OutputStream out) throws AxisFault;
}
