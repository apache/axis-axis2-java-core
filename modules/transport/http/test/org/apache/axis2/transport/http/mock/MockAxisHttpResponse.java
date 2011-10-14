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
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.http.server.AxisHttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.message.BasicHttpRequest;

/**
 * The Class MockAxisHttpResponse is a mock implementation of AxisHttpResponse
 * to used with unit tests.
 * 
 * @since 1.7.0
 */
public class MockAxisHttpResponse extends BasicHttpRequest implements AxisHttpResponse,
        OutTransportInfo, MockHTTPResponse {

    private Map<String, String> headers = new HashMap<String, String>();
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    public MockAxisHttpResponse(RequestLine requestline) {
        super(requestline);
    }

    /**
     * Gets all the headers as a Map of <Header-Name, Header-Value>.
     * 
     * This method can be used in test cases to retrieve all headers written to
     * the HttpServletResponse.
     * 
     * @return the headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setStatus(int sc) {

    }

    public void sendError(int sc, String msg) {

    }

    public void sendError(int sc) {
    }

    public void setContentType(String contentType) {

    }

    public OutputStream getOutputStream() {
        return null;
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

    public ByteArrayOutputStream getByteArrayOutputStream() {       
        return byteArrayOutputStream;
    }

}