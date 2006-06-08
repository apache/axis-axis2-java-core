/*
* $HeadURL$
* $Revision$
* $Date$
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
*/
package org.apache.axis2.transport.http.server;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpVersion;
import org.apache.http.RequestLine;
import org.apache.http.UnsupportedHttpVersionException;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpService;

public class DefaultHttpServiceProcessor extends HttpServiceProcessor {

    private static final Log LOG = LogFactory.getLog(DefaultHttpServiceProcessor.class);
    private static final Log HEADERLOG = LogFactory.getLog("org.apache.axis2.transport.http.server.wire");
    
    private final Worker worker;
    private final IOProcessorCallback callback;
    
    public DefaultHttpServiceProcessor(
            final HttpServerConnection conn, 
            final Worker worker,
            final IOProcessorCallback callback) {
        super(conn);
        if (worker == null) {
            throw new IllegalArgumentException("Worker may not be null");
        }
        this.worker = worker;
        this.callback = callback;
    }
    
    protected void postprocessResponse(final HttpResponse response, final HttpContext context) 
            throws IOException, HttpException {
        super.postprocessResponse(response, context);
        if (HEADERLOG.isDebugEnabled()) {
            HEADERLOG.debug("<< " + response.getStatusLine().toString());
            Header[] headers = response.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                HEADERLOG.debug("<< " + headers[i].toString());
            }
        }
    }

    protected void preprocessRequest(final HttpRequest request, final HttpContext context) 
            throws IOException, HttpException {
        super.preprocessRequest(request, context);
        if (HEADERLOG.isDebugEnabled()) {
            HEADERLOG.debug(">> " + request.getRequestLine().toString());
            Header[] headers = request.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                HEADERLOG.debug(">> " + headers[i].toString());
            }
        }
    }

    protected void doService(final HttpRequest request, final HttpResponse response) 
            throws HttpException, IOException {
        RequestLine reqline = request.getRequestLine();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Request method: " + reqline.getMethod());
            LOG.debug("Target URI: " + reqline.getUri());
        }
        
        HttpVersion ver = reqline.getHttpVersion();
        if (!ver.lessEquals(HttpVersion.HTTP_1_1)) {
            throw new UnsupportedHttpVersionException("Unsupported HTTP version: " + ver); 
        }
        this.worker.service(request, response);
    }
    
    protected void logIOException(final IOException ex) {
        if (ex instanceof SocketTimeoutException) {
            LOG.debug(ex.getMessage());
        } else if (ex instanceof SocketException) {
            LOG.debug(ex.getMessage());
        }
        else {
            LOG.warn(ex.getMessage(), ex);
        }
    }

    protected void logMessage(final String s) {
        LOG.debug(s);
    }

    protected void logProtocolException(final HttpException ex) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("HTTP protocol error: " + ex.getMessage());
        }
    }

    public void close() throws IOException {
        closeConnection();
    }

    public void run() {
        LOG.debug("New connection thread");
        try {
            while (!Thread.interrupted() && !isDestroyed() && isActive()) {
                handleRequest();
            }
        } finally {
            destroy();
            if (this.callback != null) {
                this.callback.completed(this);
            }
        }
    }
                   
}
