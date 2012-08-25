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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.AbstractHTTPSender;
import org.apache.axis2.transport.http.CommonsHTTPTransportSender;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportConstants;
import org.apache.axis2.transport.http.HTTPTransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class HTTPClient4TransportSender use HC HTTPClient 4.X. 
 */
public class HTTPClient4TransportSender extends CommonsHTTPTransportSender implements
                                                                           HTTPTransportSender {

    private static final Log log = LogFactory.getLog(HTTPClient4TransportSender.class);

    @Override
    public void cleanup(MessageContext msgContext) throws AxisFault {
        // We don't need to call cleanup here because httpclient4 releases
        // the connection and cleanup automatically
        // TODO : Don't do this if we're not on the right thread! Can we confirm?
        log.trace("cleanup() releasing connection");
        // guard against multiple calls
        msgContext.removeProperty(HTTPConstants.HTTP_METHOD);

    }

    public void setHTTPClientVersion(ConfigurationContext configurationContext) {
        configurationContext.setProperty(HTTPTransportConstants.HTTP_CLIENT_VERSION,
                                         HTTPTransportConstants.HTTP_CLIENT_4_X_VERSION);
    }


    @Override
    protected AbstractHTTPSender createHTTPSender() {
        return new HTTPSenderImpl();
    }

}
