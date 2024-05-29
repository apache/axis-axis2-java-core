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

package org.apache.axis2.transport.http.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.RejectedExecutionException;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpResponseFactory;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpResponseFactory;
import org.apache.hc.core5.http.io.SocketConfig;

public class DefaultConnectionListener implements IOProcessor {

    private static Log LOG = LogFactory.getLog(DefaultConnectionListener.class);

    private volatile boolean destroyed = false;

    private final String scheme;
    private final int port;
    private final HttpConnectionManager connmanager;
    private final ConnectionListenerFailureHandler failureHandler;
    private final Http1Config http1Config;
    private final SocketConfig socketConfig;

    private ServerSocket serversocket = null;

    /**
     * Use this constructor to provide a custom ConnectionListenerFailureHandler, e.g. by subclassing DefaultConnectionListenerFailureHandler
     */
    public DefaultConnectionListener(
	    String scheme,	    
            int port,
            final HttpConnectionManager connmanager,
            final ConnectionListenerFailureHandler failureHandler,
            final Http1Config http1Config,
            final SocketConfig socketConfig) throws IOException {
        super();
        if (connmanager == null) {
            throw new IllegalArgumentException("Connection manager may not be null");
        }
        if (failureHandler == null) {
            throw new IllegalArgumentException("Failure handler may not be null");
        }
        if (http1Config == null) {
            throw new IllegalArgumentException("http1Config may not be null");
        }
        if (socketConfig == null) {
            throw new IllegalArgumentException("socketConfig may not be null");
        }
        this.scheme = scheme;
        this.port = port;
        this.connmanager = connmanager;
        this.failureHandler = failureHandler;
        this.http1Config = http1Config;
        this.socketConfig = socketConfig;
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                try {
                    if (serversocket == null || serversocket.isClosed()) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Listening on port " + port);
                        }
                        synchronized (this) {
                            serversocket = new ServerSocket(port);
                            serversocket.setReuseAddress(true);
                            notifyAll();
                        }
                    }
                    LOG.debug("Waiting for incoming HTTP connection");
                    Socket socket = this.serversocket.accept();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Incoming HTTP connection from " +
                                socket.getRemoteSocketAddress());
                    }
                    AxisHttpConnection conn = new AxisHttpConnectionImpl(this.scheme, socket, this.http1Config, this.socketConfig);
                    try {
                        this.connmanager.process(conn);
                    } catch (RejectedExecutionException e) {
			HttpResponseFactory<ClassicHttpResponse> responseFactory = DefaultClassicHttpResponseFactory.INSTANCE;    
                        conn.sendResponse(responseFactory.newHttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE));
                    }
                } catch(java.io.InterruptedIOException ie) {
                    break;
                } catch (Throwable ex) {
                    if (Thread.interrupted()) {
                        break;
                    }
                    if (!failureHandler.failed(this, ex)) {
                        break;
                    }
                }
            }
        } finally {
            destroy();
            synchronized (this) {
                notifyAll();
            }
        }
    }

    public synchronized void awaitSocketOpen() throws InterruptedException {
        while (serversocket == null && !destroyed) {
            wait();
        }
    }
    
    public synchronized int getPort() {
        return serversocket.getLocalPort();
    }
    
    public synchronized void close() throws IOException {
        if (this.serversocket != null) {
            this.serversocket.close();
            this.serversocket = null;
        }
    }

    public synchronized void destroy() {
        this.destroyed = true;
        try {
            close();
        } catch (IOException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("I/O error closing listener", ex);
            }
        }
    }

    public synchronized boolean isDestroyed() {
        return this.destroyed;
    }

}
