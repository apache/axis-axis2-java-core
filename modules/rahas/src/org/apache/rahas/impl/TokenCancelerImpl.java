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
package org.apache.rahas.impl;

import org.apache.rahas.TokenCanceler;
import org.apache.rahas.RahasData;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.TokenStorage;
import org.apache.rahas.Token;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.context.MessageContext;

import javax.xml.namespace.QName;

/**
 * 
 */
public class TokenCancelerImpl implements TokenCanceler {

    private String configFile;
    private OMElement configElement;
    private String configParamName;
    
    private static final QName QNAME_CANCEL_TARGET =
            new QName(RahasConstants.CancelBindingLocalNames.CANCEL_TARGET);
    private static final QName QNAME_SEC_TOKEN_REF =
            new QName(RahasConstants.CancelBindingLocalNames.SECURITY_TOKEN_REF);
    private static final QName QNAME_REFERENCE =
            new QName(RahasConstants.CancelBindingLocalNames.REFERENCE);
    private static final QName QNAME_URI = new QName(RahasConstants.CancelBindingLocalNames.URI);

    /**
     * Cancel the token specified in the request.
     *
     * @param data A populated <code>RahasData</code> instance
     * @return Response SOAPEnveloper
     * @throws org.apache.rahas.TrustException
     *
     */
    public SOAPEnvelope cancel(RahasData data) throws TrustException {
        TokenCancelerConfig config = null;
        if (this.configElement != null) {
            config = TokenCancelerConfig.load(configElement.
                    getFirstChildWithName(SCTIssuerConfig.SCT_ISSUER_CONFIG));
        }

        // Look for the file
        if (config == null && this.configFile != null) {
            config = TokenCancelerConfig.load(this.configFile);
        }

        // Look for the param
        if (config == null && this.configParamName != null) {
            Parameter param = data.getInMessageContext().getParameter(this.configParamName);
            if (param != null && param.getParameterElement() != null) {
                config = TokenCancelerConfig.load(param.getParameterElement()
                        .getFirstChildWithName(SCTIssuerConfig.SCT_ISSUER_CONFIG));
            } else {
                throw new TrustException("expectedParameterMissing",
                                         new String[]{this.configParamName});
            }
        }

        if (config == null) {
            throw new TrustException("missingConfiguration",
                                     new String[]{SCTIssuerConfig.SCT_ISSUER_CONFIG
                                             .getLocalPart()});
        }

        OMElement rstEle = data.getRstElement();
        OMElement cancelTargetEle = rstEle.getFirstChildWithName(QNAME_CANCEL_TARGET);
        if (cancelTargetEle == null) {
            throw new TrustException("requiredElementNotFound",
                                     new String[]{QNAME_CANCEL_TARGET.getLocalPart()});
        }
        OMElement secTokenRefEle = cancelTargetEle.getFirstChildWithName(QNAME_SEC_TOKEN_REF);
        String tokenId;
        if (secTokenRefEle != null) {

            /*
            <o:SecurityTokenReference
                 xmlns:o="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
              <o:Reference URI="urn:uuid:8e6a3a95-fd1b-4c24-96d4-28e875025ff7"
                           ValueType="http://schemas.xmlsoap.org/ws/2005/02/sc/sct" />
            </o:SecurityTokenReference>
            */
            OMElement referenceEle = secTokenRefEle.getFirstChildWithName(QNAME_REFERENCE);
            if (referenceEle != null) {
                OMAttribute uri = referenceEle.getAttribute(QNAME_URI);
                if (uri != null) {
                    tokenId = uri.getAttributeValue().substring(1);
                } else {
                    throw new TrustException("cannotDetermineTokenId");
                }
            } else {
                throw new TrustException("cannotDetermineTokenId");
            }
        } else {
            // TODO: we need to handle situation where the token itself is contained within the
            // TODO:  <wst:CancelTarget> element
            throw new TrustException("cannotDetermineTokenId");
        }

        // Cancel the token
        MessageContext inMsgCtx = data.getInMessageContext();
        TokenStorage tokenStore = TrustUtil.getTokenStore(inMsgCtx);
        Token token = tokenStore.getToken(tokenId);
        if (token == null) {
            throw new TrustException("tokenNotFound", new String[]{tokenId});
        }
        token.setState(Token.CANCELLED);
        tokenStore.update(token);

        // Create the response SOAP Envelope
        SOAPEnvelope responseEnv =
                TrustUtil.
                        createSOAPEnvelope(inMsgCtx.getEnvelope().getNamespace().getNamespaceURI());
        OMElement rstrElem;
        int version = data.getVersion();
        if (RahasConstants.VERSION_05_02 == version) {
            rstrElem = TrustUtil
                    .createRequestSecurityTokenResponseElement(version, responseEnv.getBody());
        } else {
            OMElement rstrcElem = TrustUtil
                    .createRequestSecurityTokenResponseCollectionElement(
                            version, responseEnv.getBody());

            rstrElem = TrustUtil.createRequestSecurityTokenResponseElement(version, rstrcElem);
        }
        OMElement reqTokenCanceledEle =
                TrustUtil.createRequestedTokenCanceledElement(version, rstrElem);
        return responseEnv;
    }

    /**
     * Set the configuration file of this TokenCanceller.
     * <p/>
     * This is the text value of the &lt;configuration-file&gt; element of the
     * token-dispatcher-configuration
     *
     * @param configFile
     */
    public void setConfigurationFile(String configFile) {
        this.configFile = configFile;
    }

    /**
     * Set the configuration element of this TokenCanceller.
     * <p/>
     * This is the &lt;configuration&gt; element of the
     * token-dispatcher-configuration
     *
     * @param configElement <code>OMElement</code> representing the configuation
     */
    public void setConfigurationElement(OMElement configElement) {
        this.configElement = configElement;
    }

    /**
     * Set the name of the configuration parameter.
     * <p/>
     * If this is used then there must be a
     * <code>org.apache.axis2.description.Parameter</code> object available in
     * the via the messageContext when the <code>TokenIssuer</code> is called.
     *
     * @param configParamName
     * @see org.apache.axis2.description.Parameter
     */
    public void setConfigurationParamName(String configParamName) {
        this.configParamName = configParamName;
    }

    /**
     * Returns the <code>wsa:Action</code> of the response.
     *
     * @param data A populated <code>RahasData</code> instance
     * @return Returns the <code>wsa:Action</code> of the response
     * @throws org.apache.rahas.TrustException
     *
     */
    public String getResponseAction(RahasData data) throws TrustException {
        return TrustUtil.getActionValue(data.getVersion(), RahasConstants.RSTR_ACTION_CANCEL);
    }
}
