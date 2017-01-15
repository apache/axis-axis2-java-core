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
import org.apache.axis2.transport.http.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.AbstractHttpClient;

class DeleteRequest implements Request {
    private static final Log log = LogFactory.getLog(DeleteRequest.class);

    private final HTTPSenderImpl sender;
    private final MessageContext msgContext;
    private final URL url;
    private final String soapActionString;

    DeleteRequest(HTTPSenderImpl sender, MessageContext msgContext, URL url, String soapActionString) {
        this.sender = sender;
        this.msgContext = msgContext;
        this.url = url;
        this.soapActionString = soapActionString;
    }

    @Override
    public void execute() throws AxisFault {
        HttpDelete deleteMethod = new HttpDelete();
        AbstractHttpClient httpClient = sender.getHttpClient(msgContext);
        sender.populateCommonProperties(msgContext, url, deleteMethod, httpClient, soapActionString);

        /*
         * main execution takes place..
         */
        HttpResponse response = null;
        try {
            response = sender.executeMethod(httpClient, msgContext, url, deleteMethod);
            sender.handleResponse(msgContext, response);
        } catch (IOException e) {
            log.info("Unable to sendViaDelete to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            sender.cleanup(msgContext, response);
        }
    }
}