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
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;

class PutRequest extends RequestBase<HttpPut> {
    private static final Log log = LogFactory.getLog(PutRequest.class);

    PutRequest(HTTPSenderImpl sender, URL url, String soapActionString, MessageContext msgContext, MessageFormatter messageFormatter) throws AxisFault {
        super(sender, soapActionString, msgContext, url, messageFormatter, new HttpPut());
    }

    @Override
    public void execute() throws AxisFault {
        AxisRequestEntityImpl requestEntity =
                new AxisRequestEntityImpl(new AxisRequestEntity(messageFormatter, msgContext, sender.getFormat(),
                                          soapActionString, sender.isChunked(), sender.isAllowedRetry()));
        method.setEntity(requestEntity);

        if (!sender.getHttpVersion().equals(HTTPConstants.HEADER_PROTOCOL_10) && sender.isChunked()) {
            requestEntity.setChunked(sender.isChunked());
        }

        String soapAction = messageFormatter.formatSOAPAction(msgContext, sender.getFormat(), soapActionString);
        if (soapAction != null && !msgContext.isDoingREST()) {
            method.setHeader(HTTPConstants.HEADER_SOAP_ACTION, soapAction);
        }

        /*
         * main execution takes place..
         */
        HttpResponse response = null;
        try {
            response = sender.executeMethod(httpClient, msgContext, url, method);
            sender.handleResponse(msgContext, response);
        } catch (IOException e) {
            log.info("Unable to sendViaPut to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            sender.cleanup(msgContext, response);
        }
    }
}