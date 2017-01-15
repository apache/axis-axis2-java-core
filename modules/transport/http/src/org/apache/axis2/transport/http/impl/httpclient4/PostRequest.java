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

import java.io.IOException;
import java.net.URL;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;

class PostRequest implements Request {
    private static final Log log = LogFactory.getLog(PostRequest.class);

    private final HTTPSenderImpl sender;
    private final MessageContext msgContext;
    private final URL url;
    private final String soapActionString;

    PostRequest(HTTPSenderImpl sender, MessageContext msgContext, URL url, String soapActionString) {
        this.sender = sender;
        this.msgContext = msgContext;
        this.url = url;
        this.soapActionString = soapActionString;
    }

    @Override
    public void execute() throws AxisFault {
        AbstractHttpClient httpClient = sender.getHttpClient(msgContext);

        /*
         * What's up with this, it never gets used anywhere?? --Glen String
         * charEncoding = (String)
         * msgContext.getProperty(Constants.Configuration
         * .CHARACTER_SET_ENCODING);
         *
         * if (charEncoding == null) { charEncoding =
         * MessageContext.DEFAULT_CHAR_SET_ENCODING; }
         */

        HttpPost postMethod = new HttpPost();
        if (log.isTraceEnabled()) {
            log.trace(Thread.currentThread() + " PostMethod " + postMethod + " / " + httpClient);
        }
        MessageFormatter messageFormatter = sender.populateCommonProperties(msgContext, url, postMethod,
                                                                     httpClient, soapActionString);
        AxisRequestEntityImpl requestEntity =
                new AxisRequestEntityImpl(messageFormatter, msgContext, sender.getFormat(),
                                          soapActionString, sender.isChunked(), sender.isAllowedRetry());
        postMethod.setEntity(requestEntity);

        if (!sender.getHttpVersion().equals(HTTPConstants.HEADER_PROTOCOL_10) && sender.isChunked()) {
            requestEntity.setChunked(sender.isChunked());
        }

        String soapAction = messageFormatter.formatSOAPAction(msgContext, sender.getFormat(), soapActionString);

        if (soapAction != null && !msgContext.isDoingREST()) {
            postMethod.setHeader(HTTPConstants.HEADER_SOAP_ACTION, soapAction);
        }

        /*
         * main execution takes place..
         */
        HttpResponse response = null;
        try {
            response = sender.executeMethod(httpClient, msgContext, url, postMethod);
            sender.handleResponse(msgContext, response);
        } catch (IOException e) {
            log.info("Unable to sendViaPost to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            sender.cleanup(msgContext, response);
        }
    }
}