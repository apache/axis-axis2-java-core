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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.RESTRequestEntity;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RESTRequestEntityImpl extends RESTRequestEntity implements HttpEntity {

    public RESTRequestEntityImpl(OMElement element, boolean chunked, MessageContext msgCtxt,
            String charSetEncoding, String soapActionString, OMOutputFormat format) {
        super(element, chunked, msgCtxt, charSetEncoding, soapActionString, format);
    }

    public Header getContentType() {
        return new BasicHeader(HTTPConstants.HEADER_CONTENT_TYPE, getContentTypeAsString());
    }

    public Header getContentEncoding() {
        return null;
    }

    public InputStream getContent() throws AxisFault {
        return new ByteArrayInputStream(writeBytes());
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
