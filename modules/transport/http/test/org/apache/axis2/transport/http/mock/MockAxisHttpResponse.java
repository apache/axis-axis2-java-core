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

import org.apache.axis2.kernel.OutTransportInfo;
import org.apache.axis2.transport.http.server.AxisHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.HeaderGroup;
import org.apache.hc.core5.http.message.RequestLine;

/**
 * The Class MockAxisHttpResponse is a mock implementation of AxisHttpResponse
 * to used with unit tests.
 * 
 * @since 1.7.0
 */
public class MockAxisHttpResponse extends BasicHttpRequest implements AxisHttpResponse,
        OutTransportInfo, MockHTTPResponse {

    private HeaderGroup headerGroup;
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    public MockAxisHttpResponse(RequestLine requestline) {
        super(requestline.getMethod(), requestline.getUri());
	headerGroup = new HeaderGroup();
    }

    /**
     * Gets all the headers as an array of org.apache.hc.core5.http.Header.
     * 
     * This method can be used in test cases to retrieve all headers written to
     * the HttpServletResponse.
     * 
     * @return the headers
     */
    @Override
    public Header[] getHeaders() {
        int size = headerGroup != null ? headerGroup.getHeaders().length : 0;
        return headerGroup != null ? headerGroup.getHeaders() : null;
    }

    @Override
    public void setContentType(String contentType) {

    }

    @Override
    public void setStatus(int sc) {

    }

    @Override
    public void sendError(int sc, String msg) {

    }

    @Override
    public void sendError(int sc) {
    }

    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public void addHeader(String name, Object value) {
        headerGroup.addHeader(new BasicHeader(name, value));
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {       
        return byteArrayOutputStream;
    }

}
