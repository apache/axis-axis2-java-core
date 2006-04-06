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
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.security.trust.Constants;
import org.apache.axis2.security.trust.SimpleTokenStore;
import org.apache.axis2.security.trust.Token;
import org.apache.axis2.security.trust.TokenIssuer;
import org.apache.axis2.security.trust.TokenStorage;
import org.apache.axis2.security.trust.TrustException;
import org.apache.axis2.util.Base64;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.message.token.Reference;
import org.apache.ws.security.message.token.SecurityContextToken;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import java.security.Principal;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Vector;

public class SCTIssuer implements TokenIssuer {

    public final static String ENCRYPTED_KEY = "EncryptedKey";
    public final static String COMPUTED_KEY = "ComputedKey";
    public final static String BINARY_SECRET = "BinarySecret";
    
    private String configFile;
    
    private OMElement configElement;
    
    private String configParamName;
    
    /**
     * Issue a SecuritycontextToken based on the wsse:Signature or 
     * wsse:UsernameToken
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
            
            SCTIssuerConfig config = null;
            if(this.configElement != null) {
                config = SCTIssuerConfig
                        .load(configElement
                                .getFirstChildWithName(SCTIssuerConfig.SCT_ISSUER_CONFIG));
            } 

            //Look for the file
            if(config == null && this.configFile != null) {
                config = SCTIssuerConfig.load(this.configFile);
            }
            
            //Look for the file
            if(config == null && this.configParamName != null) {
                Parameter param = inMsgCtx.getParameter(this.configParamName);
                if(param != null && param.getParameterElement() != null) {
                    config = SCTIssuerConfig.load(param.getParameterElement()
                            .getFirstChildWithName(
                                    SCTIssuerConfig.SCT_ISSUER_CONFIG));
                } else {
                    throw new TrustException("expectedParameterMissing",
                            new String[] { this.configParamName });
                }
            }
            
            if(config == null) {
                throw new TrustException("missingConfiguration",
                        new String[] { SCTIssuerConfig.SCT_ISSUER_CONFIG
                                .getLocalPart() });
            }
            
            if(ENCRYPTED_KEY.equals(config.proofTokenType)) {
                SOAPEnvelope responseEnv = this.doEncryptedKey(config,
                        inMsgCtx, cert);
                return responseEnv;
            } else if(BINARY_SECRET.equals(config.proofTokenType)) {
                SOAPEnvelope responseEnv = this.doBinarySecret(config,
                        inMsgCtx);
                return responseEnv;
            } else if(COMPUTED_KEY.equals(config.proofTokenType)) {
                // TODO 
                throw new UnsupportedOperationException("TODO");
            } else {
                // TODO 
                throw new UnsupportedOperationException("TODO: Default");
            }
        }


    }
    
    /**
     * @param config
     * @param inMsgCtx
     * @param cert
     * @return
     */
    private SOAPEnvelope doBinarySecret(SCTIssuerConfig config, MessageContext msgCtx) throws TrustException {
        
        SOAPEnvelope env = this.getSOAPEnvelope(msgCtx);
        //Get the document
        Document doc = ((Element)env).getOwnerDocument();
        
        SecurityContextToken sct = new SecurityContextToken(doc);
        sct.setID("sctId-" + sct.getElement().hashCode());
        
        OMElement rstrElem = env.getOMFactory().createOMElement(
                new QName(Constants.WST_NS,
                        Constants.REQUEST_SECURITY_TOKEN_RESPONSE_LN,
                        Constants.WST_PREFIX), env.getBody());

        OMElement rstElem = env.getOMFactory().createOMElement(
                new QName(Constants.WST_NS,
                        Constants.REQUESTED_SECURITY_TOKEN_LN,
                        Constants.WST_PREFIX), rstrElem);
        
        rstElem.addChild((OMElement)sct.getElement());
        
        OMElement reqProofTok = env.getOMFactory().createOMElement(
                new QName(Constants.WST_NS, Constants.REQUESTED_PROOF_TOKEN_LN,
                        Constants.WST_PREFIX), rstrElem);
        
        OMElement binSecElem = env.getOMFactory().createOMElement(
                new QName(Constants.WST_NS, Constants.BINARY_SECRET,
                        Constants.WST_PREFIX), reqProofTok);

        byte[] secret = this.generateEphemeralKey();
        binSecElem.setText(Base64.encode(secret));
    
        //Store the tokens
        Token sctToken = new Token(sct.getIdentifier(), (OMElement)sct.getElement());
        sctToken.setSecret(secret);
        this.getTokenStore(msgCtx).add(sctToken);
        
        return env;
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
        String sctId = "sctId-" + sct.getElement().hashCode();
        sct.setID(sctId);
        
        OMElement rstrElem = env.getOMFactory().createOMElement(
                new QName(Constants.WST_NS,
                        Constants.REQUEST_SECURITY_TOKEN_RESPONSE_LN,
                        Constants.WST_PREFIX), env.getBody());

        OMElement rstElem = env.getOMFactory().createOMElement(
                new QName(Constants.WST_NS,
                        Constants.REQUESTED_SECURITY_TOKEN_LN,
                        Constants.WST_PREFIX), rstrElem);
        
        rstElem.addChild((OMElement)sct.getElement());
        
        OMElement reqAttRef = env.getOMFactory().createOMElement(
                new QName(Constants.WST_NS,
                        Constants.REQUESTED_ATTACHED_REFERENCE,
                        Constants.WST_PREFIX), rstrElem);
        OMElement reqUnattRef = env.getOMFactory().createOMElement(
                new QName(Constants.WST_NS,
                        Constants.REQUESTED_UNATTACHED_REFERENCE,
                        Constants.WST_PREFIX), rstrElem);
        
        reqAttRef.addChild((OMElement) this.createSecurityTokenReference(doc,
                sctId, Constants.TOK_TYPE_SCT));
        
        reqUnattRef.addChild((OMElement) this.createSecurityTokenReference(doc,
                sct.getIdentifier(), Constants.TOK_TYPE_SCT));
        
        Element encryptedKeyElem = encrKeyBuilder.getEncryptedKeyElement();
        Element bstElem = encrKeyBuilder.getBinarySecurityTokenElement();
        
        OMElement reqProofTok = env.getOMFactory().createOMElement(
                new QName(Constants.WST_NS, Constants.REQUESTED_PROOF_TOKEN_LN,
                        Constants.WST_PREFIX), rstrElem);

        if(bstElem != null) {
            reqProofTok.addChild((OMElement)bstElem);
        }
        
        reqProofTok.addChild((OMElement)encryptedKeyElem);
    
        //Store the tokens
        Token sctToken = new Token(sct.getIdentifier(), (OMElement)sct.getElement());
        sctToken.setSecret(encrKeyBuilder.getEphemeralKey());
        this.getTokenStore(msgCtx).add(sctToken);
        
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
     * @see org.apache.axis2.security.trust.TokenIssuer#setConfigurationFile(java.lang.String)
     */
    public void setConfigurationFile(String configFile) {
        this.configFile = configFile;
    }

    /**
     * @see org.apache.axis2.security.trust.TokenIssuer#setConfigurationElement(java.lang.String)
     */
    public void setConfigurationElement(OMElement configElement) {
        this.configElement = configElement;
    }
    
    /**
     * Returns the token store.
     * If the token store is aleady available in the service context then
     * fetch it and return it. If not create a new one, hook it up in the 
     * service context and return it
     * @param msgCtx
     * @return
     */
    private TokenStorage getTokenStore(MessageContext msgCtx) {
        String tempKey = TokenStorage.TOKEN_STORAGE_KEY
                                + msgCtx.getAxisService().getName();
        TokenStorage storage = (TokenStorage) msgCtx.getProperty(tempKey);
        if (storage == null) {
            storage = new SimpleTokenStore();
            msgCtx.getConfigurationContext().setProperty(tempKey, storage);
        }
        return storage;
    }

    /**
     * Create an ephemeral key
     * 
     * @return
     * @throws WSSecurityException
     */
    private byte[] generateEphemeralKey() throws TrustException {
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] temp = new byte[16];
            random.nextBytes(temp);
            return temp;
        } catch (Exception e) {
            throw new TrustException ("errorCreatingSymmKey", e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.axis2.security.trust.TokenIssuer#setConfigurationParamName(java.lang.String)
     */
    public void setConfigurationParamName(String configParamName) {
        this.configParamName = configParamName;
    }
    
    private Element createSecurityTokenReference(Document doc, String refUri, String refValueType) {
        
        Reference ref = new Reference(doc);
        ref.setURI(refUri);
        ref.setValueType(refValueType);
        SecurityTokenReference str = new SecurityTokenReference(doc);
        str.setReference(ref);
        
        return str.getElement();
    }
    
}
