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

package org.apache.axis.transport.http;



import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.axis.core.AxisEngine;
import org.apache.axis.utils.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a simple implementation of an HTTP server for processing
 * SOAP requests via Apache's xml-axis.  This is not intended for production
 * use.  Its intended uses are for demos, debugging, and performance
 * profiling.
 *
 * Note this classes uses static objects to provide a thread pool, so you should
 * not use multiple instances of this class in the same JVM/classloader unless
 * you want bad things to happen at shutdown.
 * @author Sam Ruby (ruby@us.ibm.com)
 * @author Rob Jellinghaus (robj@unrealities.com)
 * @author Alireza Taherkordi (a_taherkordi@users.sourceforge.net)
 */
public class SimpleAxisServer implements Runnable {
    protected static Log log =
            LogFactory.getLog(SimpleAxisServer.class.getName());
    private static AxisEngine myAxisServer = null;
    private ServerSocket serverSocket;    
    /**
    are we stopped?
    latch to true if stop() is called
     */
    private boolean stopped = false;
    

    public SimpleAxisServer(AxisEngine myAxisServer) {
    	SimpleAxisServer.myAxisServer = myAxisServer;
		
    }


    /**
     * stop the server if not already told to.
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
    	 System.out.println("request accepted");
        // Accept and process requests from the socket
        while (!stopped) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
               
            } catch (java.io.InterruptedIOException iie) {
            } catch (Exception e) {
                log.debug(Messages.getMessage("exception00"), e);
                break;
            }
            if (socket != null) {
                SimpleAxisWorker worker = new SimpleAxisWorker(this, socket,myAxisServer);
                worker.run();
            }
        }
        log.info(Messages.getMessage("quit00", "SimpleAxisServer"));
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
     *  stop() and then start() the server, the new socket will be used).
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
     *
     * This will interrupt any pending accept().
     */
    public void stop() {
        //recognise use before we are live
        if(stopped ) {
            return;
        }
        /*
         * Close the server socket cleanly, but avoid fresh accepts while
         * the socket is closing.
         */
        stopped = true;

        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.info(Messages.getMessage("exception00"), e);
        } finally {
            serverSocket=null;
        }

        log.info(Messages.getMessage("quit00", "SimpleAxisServer"));

    }

}
