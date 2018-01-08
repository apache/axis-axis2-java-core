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
package org.apache.axis2.testutils.jaxws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.spi.http.HttpContext;
import javax.xml.ws.spi.http.HttpExchange;

final class HttpExchangeImpl extends HttpExchange {
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    HttpExchangeImpl(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public Map<String, List<String>> getRequestHeaders() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void addResponseHeader(String name, String value) {
        response.setHeader(name, value);
    }

    @Override
    public String getRequestURI() {
        return request.getRequestURI();
    }

    @Override
    public String getContextPath() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestMethod() {
        return request.getMethod();
    }

    @Override
    public HttpContext getHttpContext() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        response.getOutputStream().close();
    }

    @Override
    public InputStream getRequestBody() throws IOException {
        return request.getInputStream();
    }

    @Override
    public OutputStream getResponseBody() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public void setStatus(int status) {
        response.setStatus(status);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProtocol() {
        return request.getProtocol();
    }

    @Override
    public String getScheme() {
        return request.getScheme();
    }

    @Override
    public String getPathInfo() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getQueryString() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(String name) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getAttributeNames() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getUserPrincipal() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserInRole(String role) {
        // TODO
        throw new UnsupportedOperationException();
    }
}
