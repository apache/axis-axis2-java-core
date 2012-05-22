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

package org.apache.axis2.transport.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.TransportSender;
import org.apache.axis2.transport.http.impl.httpclient4.HTTPClient4TransportSender;
import org.apache.http.client.methods.HttpGet;


public class HTTPClient4TransportSenderTest extends CommonsHTTPTransportSenderTest{

    @Override
    protected TransportSender getTransportSender() {
        return new HTTPClient4TransportSender();
    }

    @Override
    public void testCleanup() throws AxisFault {
        TransportSender sender = getTransportSender();
        MessageContext msgContext = new MessageContext();
        HttpGet httpMethod = new HttpGet();
        msgContext.setProperty(HTTPConstants.HTTP_METHOD, httpMethod);
        assertNotNull("HttpMethod can not be null",
                msgContext.getProperty(HTTPConstants.HTTP_METHOD));
        sender.cleanup(msgContext);
        assertNull("HttpMethod should be null", msgContext.getProperty(HTTPConstants.HTTP_METHOD));
    }
}
