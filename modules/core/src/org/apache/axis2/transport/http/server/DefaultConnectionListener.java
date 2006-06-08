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
import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpServerConnection;

public class DefaultConnectionListener implements IOProcessor {

    private static Log LOG = LogFactory.getLog(DefaultConnectionListener.class);
	
    private volatile boolean destroyed = false;
    
    private final HttpConnectionFactory connfactory;
    private final HttpConnectionManager connmanager;
    private final ServerSocket serversocket;
        
    public DefaultConnectionListener(
    		int port,
            final HttpConnectionFactory connfactory,
            final HttpConnectionManager connmanager) throws IOException {
    	super();
        if (connfactory == null) {
            throw new IllegalArgumentException("Connection factory may not be null");
        }
        if (connmanager == null) {
            throw new IllegalArgumentException("Connection manager may not be null");
        }
        this.connmanager = connmanager;
        this.connfactory = connfactory;
        this.serversocket = new ServerSocket(port);
    }

    public void run() {
    	if (LOG.isInfoEnabled()) {
            LOG.info("Listening on port " + this.serversocket.getLocalPort());
    	}
        try {
            while (!this.serversocket.isClosed() && !Thread.interrupted()) {
                try {
                    LOG.debug("Waiting for incoming HTTP connection");
                    Socket socket = this.serversocket.accept();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Incoming HTTP connection from " + 
                        		socket.getRemoteSocketAddress());
                    }
                    HttpServerConnection conn = this.connfactory.newConnection(socket);
                    this.connmanager.process(conn);
                } catch (IOException ex) {
                    if (ex instanceof SocketException) {
                        if (LOG.isDebugEnabled() 
                                && !this.destroyed && !Thread.interrupted()) {
                            LOG.debug("Connection listener terminated due to an I/O error: " + 
                                    ex.getMessage());
                        }
                    } else {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Connection listener terminated due to an I/O error: " + 
                                    ex.getMessage(), ex);
                        }
                    }
                    break;
                } catch (Throwable ex) {
                    LOG.error("Connection listener terminated due to a runtime error", ex);
                    break;
                }
            }
        } finally {
            destroy();
        }
    }
    
	public void close() throws IOException {
        this.serversocket.close();
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
