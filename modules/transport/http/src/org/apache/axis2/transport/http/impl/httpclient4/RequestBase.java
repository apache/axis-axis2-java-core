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

import java.net.URL;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.Request;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.AbstractHttpClient;

abstract class RequestBase implements Request {
    protected final HTTPSenderImpl sender;
    protected final MessageContext msgContext;
    protected final URL url;
    protected final HttpRequestBase method;
    protected final AbstractHttpClient httpClient;

    RequestBase(HTTPSenderImpl sender, MessageContext msgContext, URL url, AxisRequestEntity requestEntity, HttpRequestBase method) throws AxisFault {
        this.sender = sender;
        this.msgContext = msgContext;
        this.url = url;
        this.method = method;
        httpClient = sender.getHttpClient(msgContext);
        sender.populateCommonProperties(msgContext, url, method, httpClient);
        if (requestEntity != null) {
            AxisRequestEntityImpl requestEntityAdapter = new AxisRequestEntityImpl(requestEntity);
            ((HttpEntityEnclosingRequest)method).setEntity(requestEntityAdapter);
    
            if (!sender.getHttpVersion().equals(HTTPConstants.HEADER_PROTOCOL_10) && sender.isChunked()) {
                requestEntity.setChunked(sender.isChunked());
            }
        }
    }

    @Override
    public void setHeader(String name, String value) {
        method.setHeader(name, value);
    }
}
