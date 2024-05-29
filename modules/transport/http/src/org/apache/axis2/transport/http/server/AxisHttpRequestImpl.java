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

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.impl.io.SocketHolder;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;

import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;

public class AxisHttpRequestImpl implements AxisHttpRequest {

    private final ClassicHttpRequest request;
    private final AxisHttpConnection conn;
    private final HttpProcessor httpproc;
    private final HttpContext context;
    
    public AxisHttpRequestImpl(
            final AxisHttpConnection conn,
            final ClassicHttpRequest request, 
            final HttpProcessor httpproc,
            final HttpContext context) {
        super();
        if (conn == null) {
            throw new IllegalArgumentException("HttpHeaders connection may not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("HttpHeaders request may not be null");
        }
        if (httpproc == null) {
            throw new IllegalArgumentException("HttpHeaders processor may not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("HttpHeaders context may not be null");
        }
        this.request = request;
        this.conn = conn;
        this.httpproc = httpproc;
        this.context = context;
    }
    
    public void prepare() throws IOException, HttpException {
        this.context.setAttribute(HttpCoreContext.CONNECTION_ENDPOINT, this.conn);
        this.context.setAttribute(HttpCoreContext.HTTP_REQUEST, this.request);
        
        this.httpproc.process(this.request, this.request.getEntity(), this.context);
    }

    public String getMethod() {
        return this.request.getMethod();
    }

    public String getRequestURI() {
        return this.request.getRequestUri();
    }

    public ProtocolVersion getVersion() {
        return this.request.getVersion();
    }

    public String getContentType() {
        Header header = this.request.getFirstHeader(HttpHeaders.CONTENT_TYPE);
        if (header != null) {
            return header.getValue();
        } else {
            return null;
        }
    }

    public void setVersion(final ProtocolVersion version) {
        this.request.setVersion(version);
    }

    public org.apache.hc.core5.http.Header[] getHeaders() {
        return this.request.getHeaders();
    }

    public org.apache.hc.core5.http.Header getHeader(final String name) throws ProtocolException {
        return this.request.getHeader(name);
    }

    public int countHeaders(final String name) {
        return this.request.countHeaders(name);
    }

    public void addHeader(final Header header) {
        this.request.addHeader(header);
    }

    public void addHeader(final String name, final String value) {
        this.request.addHeader(name, value);
    }

    public void addHeader(final String name, final Object value) {
        this.request.addHeader(name, value);
    }

    public boolean containsHeader(final String name) {
        return this.request.containsHeader(name);
    }

    public Header[] getAllHeaders() {
        return this.request.getHeaders();
    }

    public Header getFirstHeader(final String name) {
        return this.request.getFirstHeader(name);
    }

    public Header[] getHeaders(String name) {
        return this.request.getHeaders(name);
    }

    public Header getLastHeader(final String name) {
        return this.request.getLastHeader(name);
    }

    public Iterator<Header> headerIterator() {
        return this.request.headerIterator();
    }

    public Iterator<Header> headerIterator(final String name) {
        return this.request.headerIterator(name);
    }

    public boolean removeHeader(final Header header) {
        return this.request.removeHeader(header);
    }

    public boolean removeHeaders(final String name) {
        return this.request.removeHeaders(name);
    }

    public void setHeader(final Header header) {
        this.request.setHeader(header);
    }

    public void setHeader(final String name, final String value) {
        this.request.setHeader(name, value);
    }

    public void setHeaders(Header[] headers) {
        this.request.setHeaders(headers);
    }

    public void setHeader(final String name, final Object value) {
        this.request.setHeader(new BasicHeader(name, value));
    }

    public SocketHolder getSocketHolder() {
        return this.conn.getSocketHolder();
    }
    public InputStream getInputStream() {
        return this.conn.getInputStream();
    }
}
