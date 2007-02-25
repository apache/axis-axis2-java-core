/*
 * $HeadURL$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 *  Copyright 1999-2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.axis2.transport.http.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpServerConnection;

public class DefaultConnectionListener implements IOProcessor {

    private static Log LOG = LogFactory.getLog(DefaultConnectionListener.class);
    
    private volatile boolean destroyed = false;

    private final int port;
    private final HttpConnectionFactory connfactory;
    private final HttpConnectionManager connmanager;
    private ServerSocket serversocket = null;
    private final ConnectionListenerFailureHandler failureHandler;
    
    /** Default constructor called by HttpFactory.  A custom HttpFactory subclass can call the other constructor to provide a custom ConnectionListenerErrorHandler */
    public DefaultConnectionListener(int port, HttpConnectionFactory connfactory, HttpConnectionManager connmanager) throws IOException {
        this(port, connfactory, connmanager, new DefaultConnectionListenerFailureHandler());
    }

    /** Use this constructor to provide a custom ConnectionListenerFailureHandler, e.g. by subclassing DefaultConnectionListenerFailureHandler */
    public DefaultConnectionListener(int port, HttpConnectionFactory connfactory, HttpConnectionManager connmanager,
                                     ConnectionListenerFailureHandler failureHandler)
    throws IOException {
        super();
        if (connfactory == null)
            throw new IllegalArgumentException("Connection factory may not be null");
        if (connmanager == null)
            throw new IllegalArgumentException("Connection manager may not be null");
        if (failureHandler == null)
            throw new IllegalArgumentException("Failure handler may not be null");
        this.port = port;
        this.connmanager = connmanager;
        this.connfactory = connfactory;
        this.failureHandler = failureHandler;
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                try {
                    if (serversocket == null || serversocket.isClosed()){
                        serversocket = new ServerSocket(port);
                        serversocket.setReuseAddress(true);
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Listening on port " + this.serversocket.getLocalPort());
                        }
                    }
                    LOG.debug("Waiting for incoming HTTP connection");
                    Socket socket = this.serversocket.accept();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Incoming HTTP connection from " + 
                                socket.getRemoteSocketAddress());
                    }
                    HttpServerConnection conn = this.connfactory.newConnection(socket);
                    this.connmanager.process(conn);
                } catch (Throwable ex) {
                    if (Thread.interrupted())
                        break;
                    if (!failureHandler.failed(this, ex))
                        break;
                }
            }
        } finally {
            destroy();
        }
    }
    
    public void close() throws IOException {
        if(this.serversocket != null){
            this.serversocket.close();
        }
    }
    
    public void destroy() {
        this.destroyed = true;
        try {
            close();
        } catch (IOException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("I/O error closing listener", ex);
            }
        }
    }

    public boolean isDestroyed() {
        return this.destroyed;
    }
    
}
