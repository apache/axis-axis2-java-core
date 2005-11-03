/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/server/SimpleConnectionThread.java,v 1.3 2004/11/13 22:38:27 mbecke Exp $
 * $Revision: 224451 $
 * $Date: 2005-07-23 06:23:59 -0400 (Sat, 23 Jul 2005) $
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
 */

package org.apache.axis2.transport.http.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * Simple HTTP connection thread.
 */
public class SimpleConnectionThread implements Runnable {

    private static final Log LOG = LogFactory.getLog(SimpleConnectionThread.class);
    
    public static final String DEFAULT_CONTENT_CHARSET = "ISO-8859-1";

    private SimpleHttpServerConnection conn = null;
    private SimpleConnSet connpool = null;    
    private HttpRequestHandler handler = null;
    transient boolean stopped; 
    private String name = null;

    public SimpleConnectionThread(
            final String name,
            final SimpleHttpServerConnection conn,
            final SimpleConnSet connpool,
            final HttpRequestHandler handler) 
    throws IOException {
        //super(tg, name);
        if (conn == null) {
            throw new IllegalArgumentException("Connection may not be null");
        }
        if (connpool == null) {
            throw new IllegalArgumentException("Connection pool not be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("Request handler may not be null");
        }
        this.conn = conn;
        this.connpool = connpool;
        this.handler = handler;
        this.stopped = false; 
        this.name = name;
    }

    public synchronized void destroy() {
        if (this.stopped) {
            return;
        }
        this.stopped = true; 
        if (conn != null) {
            conn.close();
            conn = null;
        }
        //interrupt();
    }

    public void run() {
        try {
            do {
                this.conn.setKeepAlive(false);
                SimpleRequest request = this.conn.readRequest();
                if (request != null) {
                    this.handler.processRequest(this.conn, request);
                }
            } while (this.conn.isKeepAlive());
        } catch (InterruptedIOException e) {
        } catch (IOException e) {
            if (!this.stopped && !Thread.interrupted() && LOG.isWarnEnabled()) {
                LOG.warn("[" + this.name + "] I/O error: " + e.getMessage());
            }
        } finally {
            destroy();
            this.connpool.removeConnection(this.conn);
        }
    }

}
    