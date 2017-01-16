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
package org.apache.axis2.transport.http.impl.httpclient3;

import java.io.IOException;
import java.net.URL;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class PostRequest extends RequestBase<PostMethod> {
    private static final Log log = LogFactory.getLog(PostRequest.class);

    PostRequest(HTTPSenderImpl sender, URL url, MessageContext msgContext, String soapActionString, MessageFormatter messageFormatter) throws AxisFault {
        super(sender, soapActionString, msgContext, url, messageFormatter, new PostMethod());
    }

    @Override
    public void execute() throws AxisFault {
        if (log.isTraceEnabled()) {
            log.trace(Thread.currentThread() + " PostMethod " + method + " / " + httpClient);
        }

        method.setRequestEntity(new AxisRequestEntityImpl(new AxisRequestEntity(messageFormatter, msgContext, sender.getFormat(),
                soapActionString, sender.isChunked(), sender.isAllowedRetry())));

        if (!sender.getHttpVersion().equals(HTTPConstants.HEADER_PROTOCOL_10) && sender.isChunked()) {
            method.setContentChunked(true);
        }

        /*
         * main excecution takes place..
         */
        try {
            sender.executeMethod(httpClient, msgContext, url, method);
            sender.handleResponse(msgContext, method);
        } catch (IOException e) {
            log.info("Unable to sendViaPost to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            sender.cleanup(msgContext, method);
        }
    }
}