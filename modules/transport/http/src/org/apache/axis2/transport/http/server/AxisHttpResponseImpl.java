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

import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.apache.hc.core5.http.io.entity.EmptyInputStream;
import org.apache.hc.core5.http.message.BasicHeaderIterator;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;

import org.apache.axis2.kernel.OutTransportInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Iterator;

public class AxisHttpResponseImpl implements AxisHttpResponse, OutTransportInfo {

    private final ClassicHttpResponse response;
    private final AxisHttpConnection conn;
    private final HttpProcessor httpproc;
    private final HttpContext context;
    
    private AutoCommitOutputStream outstream;
    private String contentType;
    
    private volatile boolean commited;
    
    public AxisHttpResponseImpl(
            final AxisHttpConnection conn,
            final ClassicHttpResponse response, 
            final HttpProcessor httpproc,
            final HttpContext context) {
        super();
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }
        if (conn == null) {
            throw new IllegalArgumentException("HTTP connection may not be null");
        }
        if (httpproc == null) {
            throw new IllegalArgumentException("HTTP processor may not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        }
        this.response = response;
        this.conn = conn;
        this.httpproc = httpproc;
        this.context = context;
    }

    private void assertNotCommitted() {
        if (this.commited) {
            throw new IllegalStateException("Response already committed");
        }
    }
    
    public boolean isCommitted() {
        return this.commited;
    }
    
    public void commit() throws IOException, HttpException {
        if (this.commited) {
            return;
        }
        this.commited = true;
        
        this.context.setAttribute(HttpCoreContext.CONNECTION_ENDPOINT, this.conn);
        this.context.setAttribute(HttpCoreContext.HTTP_RESPONSE, this.response);

        ContentType contentTypeObj = null;
	if (this.contentType != null) {
            contentTypeObj = ContentType.parse(this.contentType);
	}	

	// AXIS2-6051, the move from javax to jakarta
	// broke HTTPClient by sending Content-Length,
	// resulting in:
	// ProtocolException: Content-Length header already present
	this.response.removeHeaders("Content-Length");
	this.response.setEntity(new BasicHttpEntity(EmptyInputStream.INSTANCE, contentTypeObj, true));
        this.httpproc.process(this.response, this.response.getEntity(), this.context);
        this.conn.sendResponse(this.response);
    }
    
    public OutputStream getOutputStream() {
        if (this.outstream == null) {
            this.outstream = new AutoCommitOutputStream();
        }
        return this.outstream;
    }

    @Override
    public Header getFirstHeader(final String name) {
        return this.response.getFirstHeader(name);
    }

    @Override
    public Header getLastHeader(final String name) {
        return this.response.getLastHeader(name);
    }

    @Override
    public Iterator<Header> headerIterator() {
        return this.response.headerIterator();
    }

    @Override
    public Iterator<Header> headerIterator(String name) {
        return this.response.headerIterator(name);
    }

    @Override
    public void setHeader(final Header header) {
        assertNotCommitted();
        this.response.setHeader(header);
    }

    @Override
    public void setHeader(final String name, final Object value) {
        assertNotCommitted();
        this.response.setHeader(name, value);
    }

    @Override
    public void setHeaders(Header[] headers) {
        assertNotCommitted();
        this.response.setHeaders(headers);
    }

    @Override
    public void setStatus(int sc) {
        assertNotCommitted();
        this.response.setCode(sc);
    }

    @Override
    public void sendError(int sc, final String msg) {
        assertNotCommitted();
        this.response.setCode(sc);
        this.response.setReasonPhrase(msg);
    }

    @Override
    public void sendError(int sc) {
        assertNotCommitted();
        this.response.setCode(sc);
    }

    @Override
    public void setContentType(final String contentType) {
        assertNotCommitted();
        this.contentType = contentType;
    }

    @Override
    public void addHeader(final Header header) {
        assertNotCommitted();
        response.addHeader(header);
    }

    @Override
    public void addHeader(final String name, final Object value) {
        assertNotCommitted();
        response.addHeader(name, value);
    }

    @Override
    public ProtocolVersion getVersion() {
        return response.getVersion();
    }

    @Override
    public void setVersion(final ProtocolVersion version) {
        assertNotCommitted();
        response.setVersion(version);
    }

    @Override
    public Header[] getHeaders(final String name) {
        return response.getHeaders(name);
    }

    @Override
    public Header[] getHeaders() {
        return response.getHeaders();
    }

    @Override
    public boolean removeHeader(final Header header) {
        assertNotCommitted();
        return this.response.removeHeader(header);
    }

    @Override
    public boolean removeHeaders(final String name) {
        assertNotCommitted();
        return this.response.removeHeaders(name);
    }

    @Override
    public boolean containsHeader(final String name) {
        return response.containsHeader(name);
    }

    @Override
    public int countHeaders(final String name) {
        return response.countHeaders(name);
    }

    @Override
    public Header getHeader(final String name) throws ProtocolException {
        return response.getHeader(name);
    }

    class AutoCommitOutputStream extends OutputStream {

        private OutputStream out;
        
        public AutoCommitOutputStream() {
            super();
        }

        private void ensureCommitted() throws IOException {
            try {
                commit();
            } catch (HttpException ex) {
                throw (IOException) new IOException().initCause(ex);  
            }
            if (this.out == null) {
                this.out = conn.getOutputStream();
            }
        }
        
        public void close() throws IOException {
            ensureCommitted();
            this.out.close();
        }

        public void write(final byte[] b, int off, int len) throws IOException {
            ensureCommitted();
            this.out.write(b, off, len);
        }

        public void write(final byte[] b) throws IOException {
            ensureCommitted();
            this.out.write(b);
        }

        public void write(int b) throws IOException {
            ensureCommitted();
            this.out.write(b);
        }
        
        public void flush() throws IOException {
            ensureCommitted();
            this.out.flush();
        }

    }
    
}
