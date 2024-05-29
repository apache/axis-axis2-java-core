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

package org.apache.axis2.transport.http.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.axis2.kernel.OutTransportInfo;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.HeaderGroup;

/**
 * The Class MockHttpServletResponse is a mock implementation of
 * HttpServletResponse to used with unit tests.
 * 
 * @since 1.7.0
 */
public class MockHttpServletResponse implements HttpServletResponse, OutTransportInfo, MockHTTPResponse {

    private String ContentType;
    private int ContentLength;
    private OutputStream outStream;
    private boolean committed;
    private HeaderGroup headerGroup;
    private ByteArrayOutputStream byteArrayOutputStream;   
    
    public MockHttpServletResponse() {
	headerGroup = new HeaderGroup();
        byteArrayOutputStream = new ByteArrayOutputStream();
    }
    
    @Override
    public ByteArrayOutputStream getByteArrayOutputStream(){
        return byteArrayOutputStream;        
    }
        
    @Override
    public Header[] getHeaders() {
        int size = headerGroup != null ? headerGroup.getHeaders().length : 0;
	System.out.println("MockHttpServletResponse.getHeaders() returning size: " + size);
        return headerGroup != null ? headerGroup.getHeaders() : null;
    }

    @Override
    public void setIntHeader(String name, int value) {
        headerGroup.removeHeaders(name);
        headerGroup.addHeader(new BasicHeader(name, String.valueOf(value)));
    }

    @Override
    public void addIntHeader(String name, int value) {
        headerGroup.addHeader(new BasicHeader(name, String.valueOf(value)));
    }

    @Override
    public void addDateHeader(String name, long date) {
        headerGroup.addHeader(new BasicHeader(name, new Date(date).toString()));
    }

    @Override
    public void setDateHeader(String name, long date) {
        headerGroup.removeHeaders(name);
        headerGroup.addHeader(new BasicHeader(name, new Date(date).toString()));
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return (ServletOutputStream) outStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return null;
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    @Override
    public void reset() {
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public void resetBuffer() {
    }

    @Override
    public void setContentLength(int len) {
        this.ContentLength = len;
    }

    @Override
    public void setContentType(String type) {
        this.ContentType = type;
    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public boolean containsHeader(String name) {
        return headerGroup.containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return null;
    }

    @Override
    public String encodeUrl(String url) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return null;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
    }

    @Override
    public void sendError(int sc) throws IOException {
    }

    @Override
    public void sendRedirect(String location) throws IOException {
    }

    @Override
    public void setHeader(String name, String value) {
	System.out.println("MockHttpServletResponse.setHeader() , name: " +name+ " , value: " + value);
        headerGroup.removeHeaders(name);
        headerGroup.addHeader(new BasicHeader(name, value));
    }

    @Override
    public void addHeader(String name, String value) {
	System.out.println("MockHttpServletResponse.addHeader() , name: " +name+ " , value: " + value);
        headerGroup.addHeader(new BasicHeader(name, value));
    }

    @Override
    public void setStatus(int sc) {
    }

    @Override
    public void setStatus(int sc, String sm) {
    }

    @Override
    public String getContentType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterEncoding(String charset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContentLengthLong(long len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatus() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHeader(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getHeaderNames() {
        throw new UnsupportedOperationException();
    }
}
