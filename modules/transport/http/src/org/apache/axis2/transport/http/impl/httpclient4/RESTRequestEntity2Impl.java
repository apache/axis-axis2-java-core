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

package org.apache.axis2.transport.http.impl.httpclient4;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.RESTRequestEntity2;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RESTRequestEntity2Impl extends RESTRequestEntity2 implements HttpEntity{

    public RESTRequestEntity2Impl(String postRequestBody, String contentType) {
        super(postRequestBody, contentType);
    }

    public boolean isChunked() {
        return false;
    }

    public Header getContentType() {
        return new BasicHeader(HTTPConstants.HEADER_CONTENT_TYPE, getContentTypeAsString());
    }

    public Header getContentEncoding() {
        return null;
    }

    public InputStream getContent() throws IOException{
        return getRequestEntityContent();
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        writeRequest(outputStream);
    }

    public boolean isStreaming() {
        return false;
    }

    public void consumeContent() {
        //TODO : Handle this correctly
    }
}
