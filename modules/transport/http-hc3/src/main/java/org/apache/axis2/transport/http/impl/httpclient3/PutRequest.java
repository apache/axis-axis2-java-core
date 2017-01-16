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
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class PutRequest extends RequestBase {
    private static final Log log = LogFactory.getLog(PutRequest.class);

    PutRequest(HTTPSenderImpl sender, MessageContext msgContext, URL url, AxisRequestEntity requestEntity) throws AxisFault {
        super(sender, msgContext, url, requestEntity, new PutMethod());
    }

    @Override
    public void execute() throws AxisFault {
        /*
         * main excecution takes place..
         */
        try {
            sender.executeMethod(httpClient, msgContext, url, method);
            sender.handleResponse(msgContext, method);
        } catch (IOException e) {
            log.info("Unable to sendViaPut to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            sender.cleanup(msgContext, method);
        }
    }
}