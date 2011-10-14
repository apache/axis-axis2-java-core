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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis2.transport.OutTransportInfo;

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
    private Map<String, String> headers;
    private ByteArrayOutputStream byteArrayOutputStream;   
    
    public MockHttpServletResponse() {
        headers = new HashMap<String, String>();
        byteArrayOutputStream = new ByteArrayOutputStream();
    }
    
    public ByteArrayOutputStream getByteArrayOutputStream(){
        return byteArrayOutputStream;        
    }
        
    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getCharacterEncoding() {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return (ServletOutputStream) outStream;
    }

    public PrintWriter getWriter() throws IOException {
        return null;
    }

    public int getBufferSize() {
        return 0;
    }

    public void flushBuffer() throws IOException {
    }

    public boolean isCommitted() {
        return committed;
    }

    public void reset() {
    }

    public Locale getLocale() {
        return null;
    }

    public void resetBuffer() {
    }

    public void setContentLength(int len) {
        this.ContentLength = len;
    }

    public void setContentType(String type) {
        this.ContentType = type;
    }

    public void setBufferSize(int size) {

    }

    public void setLocale(Locale loc) {

    }

    public void addCookie(Cookie cookie) {

    }

    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    public String encodeURL(String url) {
        return null;
    }

    public String encodeRedirectURL(String url) {
        return null;
    }

    public String encodeUrl(String url) {
        return null;
    }

    public String encodeRedirectUrl(String url) {
        return null;
    }

    public void sendError(int sc, String msg) throws IOException {
    }

    public void sendError(int sc) throws IOException {
    }

    public void sendRedirect(String location) throws IOException {
    }

    public void setDateHeader(String name, long date) {
        headers.remove(name);
        headers.put(name, new Date(date).toString());
    }

    public void addDateHeader(String name, long date) {
        headers.put(name, new Date(date).toString());
    }

    public void setHeader(String name, String value) {
        headers.remove(name);
        headers.put(name, value);
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public void setIntHeader(String name, int value) {
        headers.remove(name);
        headers.put(name, String.valueOf(value));
    }

    public void addIntHeader(String name, int value) {
        headers.put(name, String.valueOf(value));
    }

    public void setStatus(int sc) {
    }

    public void setStatus(int sc, String sm) {
    }
}