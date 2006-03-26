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

package org.apache.axis2.security.trust.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.om.DOOMAbstractFactory;
import org.apache.axis2.security.trust.Constants;
import org.apache.axis2.security.trust.TokenIssuer;
import org.apache.axis2.security.trust.TrustException;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.token.SecurityContextToken;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Vector;

public class SCTIssuer implements TokenIssuer {

    public final static String ENCRYPTED_KEY = "EncryptedKey";
    public final static String COMPUTED_KEY = "ComputedKey";
    public final static String BINARY_SECRET = "BinarySecret";

    public final static String SCT_ISSUER_CONFIG_PARAM = "sct-issuer-config";
    
    /**
     * Issue a SecuritycontextToken based on the wsse:Signature or wsse:UsernameToken
     * 
     * This will support returning the SecurityContextToken with the following 
     * types of wst:RequestedProof tokens:
     * <ul>
     *  <li>xenc:EncryptedKey</li>
     *  <li>wst:ComputedKey</li>
     *  <li>wst:BinarySecret (for secure transport)</li>
     * </ul> 
     */
    public SOAPEnvelope issue(OMElement request, MessageContext inMsgCtx)
            throws TrustException {

        Vector results = null;
        if ((results = (Vector) inMsgCtx
                .getProperty(WSHandlerConstants.RECV_RESULTS)) == null) {
            throw new TrustException(TrustException.REQUEST_FAILED);
        } else {
            Principal principal = null;
            X509Certificate cert = null;
            for (int i = 0; i < results.size(); i++) {
                WSHandlerResult rResult = (WSHandlerResult) results.get(i);
                Vector wsSecEngineResults = rResult.getResults();

                for (int j = 0; j < wsSecEngineResults.size(); j++) {
                    WSSecurityEngineResult wser = 
                        (WSSecurityEngineResult) wsSecEngineResults.get(j);
                    if (wser.getAction() != WSConstants.ENCR
                            && wser.getPrincipal() != null) {
                        cert = wser.getCertificate();
                        principal = wser.getPrincipal();
                    }
                }
            }
            //If the principal is missing
            if(principal == null) {
                throw new TrustException(TrustException.REQUEST_FAILED);
            }
            
            Parameter param = inMsgCtx.getParameter(SCT_ISSUER_CONFIG_PARAM);
            SCTIssuerConfig config = new SCTIssuerConfig(param
                    .getParameterElement().getFirstChildWithName(
                            new QName(SCT_ISSUER_CONFIG_PARAM)));
            if(ENCRYPTED_KEY.equals(config.proofTokenType)) {
                SOAPEnvelope responseEnv = this.doEncryptedKey(config,
                        inMsgCtx, cert);
                return responseEnv;
            } else if(BINARY_SECRET.equals(config.proofTokenType)) {
                // TODO 
                throw new UnsupportedOperationException("TODO");
            } else if(COMPUTED_KEY.equals(config.proofTokenType)) {
                // TODO 
                throw new UnsupportedOperationException("TODO");
            } else {
                // TODO 
                throw new UnsupportedOperationException("TODO: Default");
            }
        }


    }
    
    private SOAPEnvelope doEncryptedKey(SCTIssuerConfig config,
            MessageContext msgCtx, X509Certificate cert) throws TrustException {
        
        SOAPEnvelope env = this.getSOAPEnvelope(msgCtx);
        //Get the document
        Document doc = ((Element)env).getOwnerDocument();
        
        WSSecEncryptedKey encrKeyBuilder = new WSSecEncryptedKey();
        Crypto crypto = CryptoFactory.getInstance(config.cryptoPropertiesFile,
                msgCtx.getAxisService().getClassLoader());

        encrKeyBuilder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
        try {
            encrKeyBuilder.setUseThisCert(cert);
            encrKeyBuilder.prepare(doc, crypto);
        } catch (WSSecurityException e) {
            throw new TrustException(
                    "errorInBuildingTheEncryptedKeyForPrincipal",
                    new String[] { cert.getSubjectDN().getName()});
        }
        
        SecurityContextToken sct = new SecurityContextToken(doc);
        sct.setID("sctId-" + sct.getElement().hashCode());
        
        OMElement rstrElem = env.getOMFactory().createOMElement(
                new QName(Constants.WST_NS, "RequestSecurityTokenResponse",
                        Constants.WST_PREFIX), env.getBody());
        
        OMElement rstElem = env.getOMFactory().createOMElement(
                new QName(Constants.WST_NS, "RequestSecurityToken",
                        Constants.WST_PREFIX), rstrElem);
        
        rstElem.addChild((OMElement)sct.getElement());
        
        
        
        Element encryptedKeyElem = encrKeyBuilder.getEncryptedKeyElement();
        Element bstElem = encrKeyBuilder.getBinarySecurityTokenElement();
        
        OMElement reqProofTok = env.getOMFactory().createOMElement(
                new QName(Constants.WST_NS, "RequestedProofToken",
                        Constants.WST_PREFIX), rstrElem);
        
        if(bstElem != null) {
            reqProofTok.addChild((OMElement)bstElem);
        }
        
        reqProofTok.addChild((OMElement)encryptedKeyElem);
        
        return env;
    }

    
    private SOAPEnvelope getSOAPEnvelope(MessageContext msgCtx) {
        if(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(msgCtx.getEnvelope().getNamespace().getName())) {
            return DOOMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        } else {
            return DOOMAbstractFactory.getSOAP12Factory().getDefaultEnvelope();
        }
    }
    
    

    public String getResponseAction(OMElement request, MessageContext inMsgCtx) throws TrustException {
        return Constants.RSTR_ACTON_SCT;
    }
    
    
    
    
    /**
     * SCTIssuer Configuration processor
     *
     */
    protected class SCTIssuerConfig {

        protected String proofTokenType = SCTIssuer.ENCRYPTED_KEY;

        protected String cryptoPropertiesFile = null;

        public SCTIssuerConfig(OMElement elem) throws TrustException {
            OMElement proofTokenElem = (OMElement) elem.getFirstChildWithName(
                    new QName("proofToken"));
            if (proofTokenElem != null) {
                this.proofTokenType = proofTokenElem.getText();
            }

            OMElement cryptoPropertiesElem = (OMElement) elem
                    .getFirstChildWithName(new QName("cryptoProperties"));

            if (!SCTIssuer.BINARY_SECRET.equals(proofTokenType)
                    && cryptoPropertiesElem == null) {
                throw new TrustException("sctIssuerCryptoPropertiesMissing");
            }

            this.cryptoPropertiesFile = cryptoPropertiesElem.getText();
        }
    }
    
}
