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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.impl.dom.jaxp.DocumentBuilderFactoryImpl;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.rahas.Constants;
import org.apache.rahas.TokenIssuer;
import org.apache.rahas.TrustException;
import org.apache.rahas.TrustUtil;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.saml.SAMLIssuer;
import org.apache.ws.security.saml.SAMLIssuerFactory;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLException;
import org.opensaml.SAMLSubject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Vector;

/**
 * Issuer to issue SAMl tokens
 */
public class SAMLTokenIssuer implements TokenIssuer {
    
    private String configParamName;
    private OMElement configElement;
    private String configFile;
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.rahas.TokenIssuer#issue(org.apache.axiom.om.OMElement,
     *      org.apache.axis2.context.MessageContext)
     */
    public SOAPEnvelope issue(OMElement request, MessageContext inMsgCtx)
            throws TrustException {


        /*
         * User can be identifier using a UsernameToken or a certificate
         *  - If a certificate is found then we use that to 
         *      - identify the user and 
         *      - encrypt the response (if required)
         *  - If a UsernameToken is found then we will not be encrypting the 
         *    response 
         */
        
        //Flag to identify whether we found a cert or not
        Principal principal = null;
        X509Certificate cert = null;
        
        Vector results = null;
        if ((results = (Vector) inMsgCtx
                .getProperty(WSHandlerConstants.RECV_RESULTS)) == null) {
            throw new TrustException(TrustException.REQUEST_FAILED);
        } else {

            for (int i = 0; i < results.size(); i++) {
                WSHandlerResult rResult = (WSHandlerResult) results.get(i);
                Vector wsSecEngineResults = rResult.getResults();

                for (int j = 0; j < wsSecEngineResults.size(); j++) {
                    WSSecurityEngineResult wser = 
                        (WSSecurityEngineResult) wsSecEngineResults.get(j);
                    if (wser.getAction() == WSConstants.SIGN
                            && wser.getPrincipal() != null) {
                        cert = wser.getCertificate();
                        principal = wser.getPrincipal();
                    } else if(wser.getAction() == WSConstants.UT
                            && wser.getPrincipal() != null){
                        principal = wser.getPrincipal();
                    }
                }
            }
            //If the principal is missing
            if(principal == null) {
                throw new TrustException(TrustException.REQUEST_FAILED);
            }
        }
        
        //Get ApliesTo to figureout which service to issue the token for
        
        
        SOAPEnvelope env = TrustUtil.createSOAPEnvelope(inMsgCtx.getEnvelope()
                .getNamespace().getName());
        // Get the document
        Document doc = ((Element) env).getOwnerDocument();

        
        SAMLTokenIssuerConfig config = null;
        if(this.configElement != null) {
            config = SAMLTokenIssuerConfig
                    .load(configElement
                            .getFirstChildWithName(SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG));
        } 

        //Look for the file
        if(config == null && this.configFile != null) {
            config = SAMLTokenIssuerConfig.load(this.configFile);
        }
        
        //Look for the param
        if(config == null && this.configParamName != null) {
            Parameter param = inMsgCtx.getParameter(this.configParamName);
            if(param != null && param.getParameterElement() != null) {
                config = SAMLTokenIssuerConfig.load(param.getParameterElement()
                        .getFirstChildWithName(
                                SAMLTokenIssuerConfig.SAML_ISSUER_CONFIG));
            } else {
                throw new TrustException("expectedParameterMissing",
                        new String[] { this.configParamName });
            }
        }

        
        Crypto crypto = CryptoFactory.getInstance(config.cryptoPropFile,
                inMsgCtx.getAxisService().getClassLoader());

        SAMLIssuer saml = SAMLIssuerFactory.getInstance(config.samlPropFile);
        saml.setUsername(config.user);
        saml.setUserCrypto(crypto);
        saml.setInstanceDoc(doc);

        // Set the DOM impl to DOOM
        DocumentBuilderFactoryImpl.setDOOMRequired(true);

        SAMLAssertion assertion = saml.newAssertion();

        OMElement rstrElem = TrustUtil
                .createRequestSecurityTokenResponseElement(env.getBody());
        OMElement reqSecTokenElem = TrustUtil
                .createRequestedSecurityTokenElement(rstrElem);

        if (config.addRequestedAttachedRef) {
            TrustUtil.createRequestedAttachedRef(rstrElem, "#"
                    + assertion.getId(), Constants.TOK_TYPE_SAML_10);
        }

        if (config.addRequestedUnattachedRef) {
            TrustUtil.createRequestedUnattachedRef(rstrElem, assertion.getId(),
                    Constants.TOK_TYPE_SAML_10);
        }

        try {
            Node tempNode = assertion.toDOM();
            reqSecTokenElem.addChild((OMNode) ((Element) rstrElem).getOwnerDocument()
                    .importNode(tempNode, true));
        } catch (SAMLException e) {
            throw new TrustException("samlConverstionError", e);
        }

        // Set the DOM impl to DOOM
        DocumentBuilderFactoryImpl.setDOOMRequired(false);
        return env;
    }
    
    /**
     * 
     * @param secret
     * @return
     */
    private SAMLAssertion createAssertion(String secret, Document doc, SAMLTokenIssuerConfig config) throws TrustException {

        //Create the EncryptedKey
        WSSecEncryptedKey encryptedKeyBuiler = new WSSecEncryptedKey();
//        encryptedKeyBuiler.prepare(doc, )
        
        try {
        
        String[] confirmationMethods = new String[]{SAMLSubject.CONF_HOLDER_KEY};
        
        SAMLSubject subject = new SAMLSubject(null, Arrays.asList(confirmationMethods),
                null,
                null);
        } catch (SAMLException e) {
            throw new TrustException("samlAssertionCreationError", e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.rahas.TokenIssuer#getResponseAction(org.apache.axiom.om.OMElement,
     *      org.apache.axis2.context.MessageContext)
     */
    public String getResponseAction(OMElement request, MessageContext inMsgCtx)
            throws TrustException {
        return Constants.RSTR_ACTON_ISSUE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.rahas.TokenIssuer#setConfigurationFile(java.lang.String)
     */
    public void setConfigurationFile(String configFile) {
        // TODO TODO SAMLTokenIssuer setConfigurationFile

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.rahas.TokenIssuer#setConfigurationElement(org.apache.axiom.om.OMElement)
     */
    public void setConfigurationElement(OMElement configElement) {
        // TODO TODO SAMLTokenIssuer setConfigurationElement
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.rahas.TokenIssuer#setConfigurationParamName(java.lang.String)
     */
    public void setConfigurationParamName(String configParamName) {
        this.configParamName = configParamName;
    }

}
