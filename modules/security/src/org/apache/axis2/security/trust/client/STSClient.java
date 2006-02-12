/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.security.trust.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.axis2.security.handler.config.InflowConfiguration;
import org.apache.axis2.security.handler.config.OutflowConfiguration;
import org.apache.axis2.security.trust.TrustException;
import org.apache.axis2.security.trust.token.RequestSecurityToken;
import org.apache.axis2.security.trust.token.RequestSecurityTokenResponse;
import org.apache.ws.commons.om.OMElement;

import javax.xml.namespace.QName;

/**
 * Client to interact with a given SecurityTokenService
 */
public class STSClient {

    private String stsUrl;

    private OutflowConfiguration outConfig;
    private InflowConfiguration inConfig;

    public STSClient(String stsUrl, OutflowConfiguration outConfig, InflowConfiguration inConfig) {
        this.stsUrl = stsUrl;
        this.outConfig = outConfig;
        this.inConfig = inConfig;
    }


    public RequestSecurityTokenResponse doRequest(RequestSecurityToken rst) throws TrustException {
        try {
            Options options = new Options();
            options.setTo(new EndpointReference(this.stsUrl));
            options.setProperty(WSSHandlerConstants.OUTFLOW_SECURITY, this.outConfig.getProperty());
            options.setProperty(WSSHandlerConstants.INFLOW_SECURITY, this.inConfig.getProperty());

            ServiceClient sender = new ServiceClient();
            sender.engageModule(new QName(org.apache.axis2.Constants.MODULE_ADDRESSING));
            sender.setOptions(options);
            OMElement res = sender.sendReceive(this.prepareRequst(rst));

            RequestSecurityTokenResponse rstr = new RequestSecurityTokenResponse(res);
            return rstr;
        } catch (AxisFault e) {
            throw new TrustException("Problem in communicating with the SecurityTokenService", e);
        }
    }

    /**
     * Do Encryption and Signing of the request
     *
     * @param rst
     * @return
     * @throws TrustException
     */
    private OMElement prepareRequst(RequestSecurityToken rst) throws TrustException {

        throw new UnsupportedOperationException();
    }


}
