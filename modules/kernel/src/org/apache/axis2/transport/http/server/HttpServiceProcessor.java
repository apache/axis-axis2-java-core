/*
 * HttpServiceProcessor.java
 *
 * ====================================================================
 *
 *  Copyright 1999-2004 The Apache Software Foundation
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
 * Created on May 25, 2006, 4:09 PM
 *
 */

package org.apache.axis2.transport.http.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpExecutionContext;
import org.apache.http.protocol.HttpService;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * I/O processor intended to process requests and fill in responses.
 *
 * @author Chuck Williams
 */
public class HttpServiceProcessor implements IOProcessor {

    private static final Log LOG = LogFactory.getLog(HttpServiceProcessor.class);

    private volatile boolean terminated;

    private final HttpService httpservice;
    private final HttpServerConnection conn;
    private final IOProcessorCallback callback;

    public HttpServiceProcessor(
            final HttpService httpservice,
            final HttpServerConnection conn,
            final IOProcessorCallback callback) {
        super();
        this.httpservice = httpservice;
        this.conn = conn;
        this.callback = callback;
        this.terminated = false;
    }

    public void run() {
        LOG.debug("New connection thread");
        HttpContext context = new HttpExecutionContext(null);
        try {
            while (!Thread.interrupted() && !isDestroyed() && this.conn.isOpen()) {
                this.httpservice.handleRequest(this.conn, context);
            }
        } catch (ConnectionClosedException ex) {
            LOG.debug("Client closed connection");
        } catch (IOException ex) {
            if (ex instanceof SocketTimeoutException) {
                LOG.debug(ex.getMessage());
            } else if (ex instanceof SocketException) {
                LOG.debug(ex.getMessage());
            } else {
                LOG.warn(ex.getMessage(), ex);
            }
        } catch (HttpException ex) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("HTTP protocol error: " + ex.getMessage());
            }
        } finally {
            destroy();
            if (this.callback != null) {
                this.callback.completed(this);
            }
        }
    }

    public void close() throws IOException {
        this.conn.close();
    }

    public void destroy() {
        if (this.terminated) {
            return;
        }
        this.terminated = true;
        try {
            this.conn.shutdown();
        } catch (IOException ex) {
            LOG.debug("I/O error shutting down connection");
        }
    }

    public boolean isDestroyed() {
        return this.terminated;
    }

}
