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

package org.apache.axis2.security.trust;

import javax.xml.namespace.QName;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.security.trust.types.RequestSecurityTokenType;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.impl.builder.StAXOMBuilder;
import org.apache.ws.commons.soap.SOAPEnvelope;

public class TokenRequestDispatcher {

    private TokenRequestDispatcherConfig config;

    public TokenRequestDispatcher(TokenRequestDispatcherConfig config)
            throws TrustException {
        this.config = config;
    }

    public TokenRequestDispatcher(OMElement config) throws TrustException {
        this(TokenRequestDispatcherConfig.load(config));
    }

    public TokenRequestDispatcher(String configFilePath) throws TrustException {
        this(TokenRequestDispatcherConfig.load(configFilePath));
    }

    /**
     * Processes the incoming request and returns a SOAPEnvelope
     * @param request 
     * @param inMsgCtx
     * @return
     * @throws TrustException
     */
    public SOAPEnvelope handle(MessageContext inMsgCtx, MessageContext outMsgCtx)
            throws TrustException {

        
        RequestSecurityTokenType request = null;
        try {
            request = RequestSecurityTokenType.Factory
                    .parse(inMsgCtx.getEnvelope().getBody()
                            .getFirstChildWithName(
                                    new QName(Constants.WST_NS,
                                            "RequestSecurityToken"))
                            .getXMLStreamReader());
        } catch (Exception e) {
            e.printStackTrace();
            throw new TrustException(TrustException.INVALID_REQUEST, e);
        }
        
        URI reqType = request.getRequestType();
        URI tokenType = request.getTokenType();

        if (reqType == null
                || (reqType != null && "".equals(reqType.toString()))) {
            throw new TrustException(TrustException.INVALID_REQUEST);
        }
        if (Constants.REQ_TYPE_ISSUE.equals(reqType.toString())) {
            TokenIssuer issuer = null;
            if (tokenType == null
                    || (tokenType != null && "".equals(tokenType.toString()))) {
                issuer = config.getDefaultIssuerInstace();
            } else {
                issuer = config.getIssuer(tokenType.toString());
            }
            
            SOAPEnvelope response = issuer.issue(inMsgCtx.getEnvelope().getBody().getFirstChildWithName(new QName(Constants.WST_NS, "RequestSecurityToken")), inMsgCtx);
            
            //set the response wsa/soap action in teh out message context
            outMsgCtx.getOptions().setAction(
                    issuer.getResponseAction(new StAXOMBuilder(request
                            .getPullParser(null)).getDocumentElement(),
                            inMsgCtx));
            
            return response;
        } else if(Constants.REQ_TYPE_VALIDATE.equals(reqType.toString())) {
            throw new UnsupportedOperationException("TODO: handle " +
                    "validate requests");
        } else if(Constants.REQ_TYPE_RENEW.equals(reqType.toString())) {
            throw new UnsupportedOperationException("TODO: handle " +
                    "renew requests");            
        } else if(Constants.REQ_TYPE_CANCEL.equals(reqType.toString())) {
            throw new UnsupportedOperationException("TODO: handle " +
                    "cancel requests");
        } else {
            throw new TrustException(TrustException.INVALID_REQUEST);
        }
    }

}
