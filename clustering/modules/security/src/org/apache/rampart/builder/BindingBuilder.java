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

package org.apache.rampart.builder;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rahas.TrustException;
import org.apache.rampart.RampartException;
import org.apache.rampart.RampartMessageData;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.rampart.util.RampartUtil;
import org.apache.ws.secpolicy.Constants;
import org.apache.ws.secpolicy.model.IssuedToken;
import org.apache.ws.secpolicy.model.SecureConversationToken;
import org.apache.ws.secpolicy.model.SupportingToken;
import org.apache.ws.secpolicy.model.Token;
import org.apache.ws.secpolicy.model.UsernameToken;
import org.apache.ws.secpolicy.model.X509Token;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.message.WSSecDKSign;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecSignatureConfirmation;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public abstract class BindingBuilder {
    private static Log log = LogFactory.getLog(BindingBuilder.class);
            
    private Element insertionLocation;
    
    protected String mainSigId = null;
    
    protected Element timestampElement;
    
    /**
     * @param rmd
     */
    protected void addTimestamp(RampartMessageData rmd) {
        log.debug("Adding timestamp");
        
        WSSecTimestamp timestampBuilder = new WSSecTimestamp();
        timestampBuilder.setWsConfig(rmd.getConfig());

        timestampBuilder.setTimeToLive(RampartUtil.getTimeToLive(rmd));
        
        // add the Timestamp to the SOAP Enevelope

        timestampBuilder.build(rmd.getDocument(), rmd
                .getSecHeader());
        
        log.debug("Timestamp id: " + timestampBuilder.getId());

        rmd.setTimestampId(timestampBuilder.getId());
        
        this.timestampElement = timestampBuilder.getElement();
        log.debug("Adding timestamp: DONE");
    }
    
    /**
     * Add a UsernameToken to the security header
     * @param rmd
     * @return The <code>WSSecUsernameToken</code> instance
     * @throws RampartException
     */
    protected WSSecUsernameToken addUsernameToken(RampartMessageData rmd) throws RampartException {
       
        log.debug("Adding a UsernameToken");
        
        RampartPolicyData rpd = rmd.getPolicyData();
        
        //Get the user
        String user = rpd.getRampartConfig().getUser();
        if(user != null && !"".equals(user)) {
            log.debug("User : " + user);
            
            //Get the password
            CallbackHandler handler = RampartUtil.getPasswordCB(rmd);
            
            if(handler == null) {
                //If the callback handler is missing
                throw new RampartException("cbHandlerMissing");
            }
            
            WSPasswordCallback[] cb = { new WSPasswordCallback(user,
                    WSPasswordCallback.USERNAME_TOKEN) };
            
            try {
                handler.handle(cb);
                
                //get the password
                String password = cb[0].getPassword();
                
                log.debug("Password : " + password);
                
                if(password != null && !"".equals(password)) {
                    //If the password is available then build the token
                    
                    WSSecUsernameToken utBuilder = new WSSecUsernameToken();
                    
                    //TODO Get the UT type, only WS-SX spec supports this
                    utBuilder.setUserInfo(user, password);
                    
                    return utBuilder;
                } else {
                    //If there's no password then throw an exception
                    throw new RampartException("noPasswordForUser", 
                            new String[]{user});
                }
            } catch (IOException e) {
                throw new RampartException("errorInGettingPasswordForUser", 
                        new String[]{user}, e);
            } catch (UnsupportedCallbackException e) {
                throw new RampartException("errorInGettingPasswordForUser", 
                        new String[]{user}, e);
            }
            
        } else {
            log.debug("No user value specified in the configuration");
            throw new RampartException("userMissing");
        }
        
    }
    
    
    /**
     * @param rmd
     * @param token
     * @return
     * @throws WSSecurityException
     * @throws RampartException
     */
    protected WSSecEncryptedKey getEncryptedKeyBuilder(RampartMessageData rmd, Token token) throws RampartException {
        
        RampartPolicyData rpd = rmd.getPolicyData();
        Document doc = rmd.getDocument();
        
        WSSecEncryptedKey encrKey = new WSSecEncryptedKey();
        if(token.getInclusion().equals(Constants.INCLUDE_NEVER)) {
            if(rpd.getWss11() != null) {
                //Use thumbprint
                encrKey.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
            } else {
                //Use SKI
                encrKey.setKeyIdentifierType(WSConstants.SKI_KEY_IDENTIFIER);
            }
        } else {
            encrKey.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        }
        try {
            RampartUtil.setEncryptionUser(rmd, encrKey);
            encrKey.setKeySize(rpd.getAlgorithmSuite().getMaximumSymmetricKeyLength());
            encrKey.setKeyEncAlgo(rpd.getAlgorithmSuite().getAsymmetricKeyWrap());
            
            encrKey.prepare(doc, RampartUtil.getEncryptionCrypto(rpd.getRampartConfig(), rmd.getCustomClassLoader()));
            
            return encrKey;
        } catch (WSSecurityException e) {
            throw new RampartException("errorCreatingEncryptedKey", e);
        }
    }
    
    
    protected WSSecSignature getSignatureBuider(RampartMessageData rmd, Token token) throws RampartException {

        RampartPolicyData rpd = rmd.getPolicyData();
        
        WSSecSignature sig = new WSSecSignature();
        sig.setWsConfig(rmd.getConfig());
        
        log.debug("Token inclusion: " + token.getInclusion());
        if(token.getInclusion().equals(Constants.INCLUDE_NEVER)) {
            if(rpd.getWss11() != null) {
                //Use thumbprint
                sig.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
            } else {
                //Use SKI
                sig.setKeyIdentifierType(WSConstants.SKI_KEY_IDENTIFIER);
            }
        } else {
            sig.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        }

        //Get the user
        String user = rpd.getRampartConfig().getUser();
        String password = null;

        if(user != null && !"".equals(user)) {
            log.debug("User : " + user);
            
            //Get the password
            CallbackHandler handler = RampartUtil.getPasswordCB(rmd);
            
            if(handler == null) {
                //If the callback handler is missing
                throw new RampartException("cbHandlerMissing");
            }
            
            WSPasswordCallback[] cb = { new WSPasswordCallback(user,
                    WSPasswordCallback.SIGNATURE) };
            
            try {
                handler.handle(cb);
                if(cb[0].getPassword() != null && !"".equals(cb[0].getPassword())) {
                    password = cb[0].getPassword();
                    log.debug("Password : " + password);
                } else {
                    //If there's no password then throw an exception
                    throw new RampartException("noPasswordForUser", 
                            new String[]{user});
                }
            } catch (IOException e) {
                throw new RampartException("errorInGettingPasswordForUser", 
                        new String[]{user}, e);
            } catch (UnsupportedCallbackException e) {
                throw new RampartException("errorInGettingPasswordForUser", 
                        new String[]{user}, e);
            }
            
        } else {
            log.debug("No user value specified in the configuration");
            throw new RampartException("userMissing");
        }
        
        sig.setUserInfo(user, password);
        sig.setSignatureAlgorithm(rpd.getAlgorithmSuite().getAsymmetricSignature());
        sig.setSigCanonicalization(rpd.getAlgorithmSuite().getInclusiveC14n());
        
        try {
            sig.prepare(rmd.getDocument(), RampartUtil.getSignatureCrypto(rpd
                    .getRampartConfig(), rmd.getCustomClassLoader()), 
                    rmd.getSecHeader());
        } catch (WSSecurityException e) {
            throw new RampartException("errorInSignatureWithX509Token", e);
        }
        
        return sig;
    }
    
    /**
     * @param rmd
     * @param suppTokens
     * @throws RampartException
     */
    protected HashMap handleSupportingTokens(RampartMessageData rmd, SupportingToken suppTokens)
            throws RampartException {
        
        //Create the list to hold the tokens
        HashMap endSuppTokMap = new HashMap();
        
        if(suppTokens != null && suppTokens.getTokens() != null &&
                suppTokens.getTokens().size() > 0) {
            log.debug("Processing endorsing supporting tokens");
            
            ArrayList tokens = suppTokens.getTokens();
            for (Iterator iter = tokens.iterator(); iter.hasNext();) {
                Token token = (Token) iter.next();
                org.apache.rahas.Token endSuppTok = null;
                if(token instanceof IssuedToken && rmd.isClientSide()){
                    String id = RampartUtil.getIssuedToken(rmd, (IssuedToken)token);
                    try {
                        endSuppTok = rmd.getTokenStorage().getToken(id);
                    } catch (TrustException e) {
                        throw new RampartException("errorInRetrievingTokenId", 
                                new String[]{id}, e);
                    }
                    
                    if(endSuppTok == null) {
                        throw new RampartException("errorInRetrievingTokenId", 
                                new String[]{id});
                    }
                    
                    //Add the token to the header
                    Element siblingElem = RampartUtil
                            .insertSiblingAfter(rmd, this.getInsertionLocation(),
                                    (Element) endSuppTok.getToken());
                    this.setInsertionLocation(siblingElem);
                    
                    //Add the extracted token
                    endSuppTokMap.put(token, endSuppTok);
                    
                } else if(token instanceof X509Token) {
                    //Get the to be added
                    if(token.isDerivedKeys()) {
                        //We have to use an EncryptedKey
                        try {
                            WSSecEncryptedKey encrKey = this
                                    .getEncryptedKeyBuilder(rmd, token);
                            
                            Element bstElem = encrKey.getBinarySecurityTokenElement();
                            if(bstElem != null) {
                               Element siblingElem = RampartUtil
                                        .insertSiblingAfter(rmd, this.getInsertionLocation(),
                                                bstElem);
                               this.setInsertionLocation(siblingElem);
                            }
                            
                            Element siblingElem = RampartUtil
                                    .insertSiblingAfter(rmd, 
                                            this.getInsertionLocation(),
                                            encrKey.getEncryptedKeyElement());
                            
                            this.setInsertionLocation(siblingElem);
                            
                            Date now = new Date();
                            endSuppTok =  
                                new org.apache.rahas.Token(encrKey.getId(), 
                                        (OMElement)encrKey.getEncryptedKeyElement(),
                                        now, new Date(now.getTime() + 300000));
                            
                            endSuppTokMap.put(token, endSuppTok);
                            
                        } catch (TrustException e) {
                            throw new RampartException("errorCreatingRahasToken", e);
                        }
                    } else {
                        //We have to use a cert
                        //Prepare X509 signature
                        WSSecSignature sig = this.getSignatureBuider(rmd, token);
                        Element bstElem = sig.getBinarySecurityTokenElement();
                        if(bstElem != null) {   
                            bstElem = RampartUtil.insertSiblingAfter(rmd, 
                                    this.getInsertionLocation(), bstElem);
                            this.setInsertionLocation(bstElem);
                        }
                        endSuppTokMap.put(token, sig);
                    }
                } else if(token instanceof UsernameToken) {
                    WSSecUsernameToken utBuilder = addUsernameToken(rmd);
                    
                    utBuilder.prepare(rmd.getDocument());
                    
                    //Add the UT
                    Element elem = utBuilder.getUsernameTokenElement();
                    RampartUtil.insertSiblingAfter(rmd, this.getInsertionLocation(), elem);
                    
                    //Move the insert location to th enext element
                    this.setInsertionLocation(elem);
                    Date now = new Date();
                    try {
                        org.apache.rahas.Token tempTok = new org.apache.rahas.Token(
                                utBuilder.getId(), (OMElement) elem, now,
                                new Date(now.getTime() + 300000));
                        endSuppTokMap.put(token, tempTok);
                    } catch (TrustException e) {
                        throw new RampartException("errorCreatingRahasToken", e);
                    }
                }
            }
        }
        
        return endSuppTokMap;
    }
    /**
     * @param tokenMap
     * @param sigParts
     * @throws RampartException
     */
    protected Vector addSignatureParts(HashMap tokenMap, Vector sigParts) throws RampartException {
        
        Set entrySet = tokenMap.entrySet();
        
        for (Iterator iter = entrySet.iterator(); iter.hasNext();) {
            Object tempTok =  iter.next();
            WSEncryptionPart part = null;
            if(tempTok instanceof org.apache.rahas.Token) {
                part = new WSEncryptionPart(
                        ((org.apache.rahas.Token) tempTok).getId());
            } else if(tempTok instanceof WSSecSignature) {
                WSSecSignature tempSig = (WSSecSignature) tempTok;
                if(tempSig.getBSTTokenId() != null) {
                    part = new WSEncryptionPart(tempSig.getBSTTokenId());
                }
            } else {
              throw new RampartException("UnsupportedTokenInSupportingToken");  
            }
            sigParts.add(part);
        }
                
        return sigParts;
    }

    
    public Element getInsertionLocation() {
        return insertionLocation;
    }

    public void setInsertionLocation(Element insertionLocation) {
        this.insertionLocation = insertionLocation;
    }
    
    
    protected Vector doEndorsedSignatures(RampartMessageData rmd, HashMap tokenMap) throws RampartException {
        
        Set tokenSet = tokenMap.keySet();
        
        Vector sigValues = new Vector();
        
        for (Iterator iter = tokenSet.iterator(); iter.hasNext();) {
            
            Token token = (Token)iter.next();
            
            Object tempTok = tokenMap.get(token);
            
            Vector sigParts = new Vector();
            sigParts.add(new WSEncryptionPart(this.mainSigId));
            
            if (tempTok instanceof org.apache.rahas.Token) {
                org.apache.rahas.Token tok = (org.apache.rahas.Token)tempTok;
                if(rmd.getPolicyData().isTokenProtection()) {
                    sigParts.add(new WSEncryptionPart(tok.getId()));
                }
                
                this.doSymmSignature(rmd, token, (org.apache.rahas.Token)tempTok, sigParts);
                
            } else if (tempTok instanceof WSSecSignature) {
                WSSecSignature sig = (WSSecSignature)tempTok;
                if(rmd.getPolicyData().isTokenProtection() &&
                        sig.getBSTTokenId() != null) {
                    sigParts.add(new WSEncryptionPart(sig.getBSTTokenId()));
                }
                
                try {
                    sig.addReferencesToSign(sigParts, rmd.getSecHeader());
                    sig.computeSignature();
                    
                    this.setInsertionLocation(RampartUtil.insertSiblingAfter(
                            rmd, 
                            this.getInsertionLocation(), 
                            sig.getSignatureElement()));
                    
                } catch (WSSecurityException e) {
                    throw new RampartException("errorInSignatureWithX509Token", e);
                }
                sigValues.add(sig.getSignatureValue());
            }
        } 

        return sigValues;
            
    }
    
    
    protected byte[] doSymmSignature(RampartMessageData rmd, Token policyToken, org.apache.rahas.Token tok, Vector sigParts) throws RampartException {
        
        Document doc = rmd.getDocument();
        RampartPolicyData rpd = rmd.getPolicyData();
        
        if(policyToken.isDerivedKeys() || policyToken instanceof SecureConversationToken) {
            try {
                WSSecDKSign dkSign = new WSSecDKSign();

                OMElement ref = tok.getAttachedReference();
                if(ref == null) {
                    ref = tok.getUnattachedReference();
                }
                if(ref != null) {
                    dkSign.setExternalKey(tok.getSecret(), (Element) 
                            doc.importNode((Element) ref, true));
                } else {
                    dkSign.setExternalKey(tok.getSecret(), tok.getId());
                }

                //Set the algo info
                dkSign.setSignatureAlgorithm(rpd.getAlgorithmSuite().getSymmetricSignature());
                dkSign.setDerivedKeyLength(rpd.getAlgorithmSuite().getMinimumSymmetricKeyLength()/8);
                
                dkSign.prepare(doc, rmd.getSecHeader());
                
                if(rpd.isTokenProtection()) {
                    sigParts.add(new WSEncryptionPart(tok.getId()));
                }
                
                dkSign.setParts(sigParts);
                
                dkSign.addReferencesToSign(sigParts, rmd.getSecHeader());
                
                //Do signature
                dkSign.computeSignature();
                
                //Add elements to header
                this.setInsertionLocation(RampartUtil
                        .insertSiblingAfter(rmd, 
                                this.getInsertionLocation(),
                                dkSign.getdktElement()));

                this.setInsertionLocation(RampartUtil.insertSiblingAfter(
                        rmd, 
                        this.getInsertionLocation(), 
                        dkSign.getSignatureElement()));

                return dkSign.getSignatureValue();
                
            } catch (ConversationException e) {
                throw new RampartException(
                        "errorInDerivedKeyTokenSignature", e);
            } catch (WSSecurityException e) {
                throw new RampartException(
                        "errorInDerivedKeyTokenSignature", e);
            }
        } else {
            //TODO :  Example SAMLTOken Signature
            throw new UnsupportedOperationException("TODO");
        }
    }
    
    /**
     * Get hold of the token from the token storage
     * @param rmd
     * @param tokenId
     * @return token from the token storage
     * @throws RampartException
     */
    protected org.apache.rahas.Token getToken(RampartMessageData rmd, 
                    String tokenId) throws RampartException {
        org.apache.rahas.Token tok = null;
        try {
            tok = rmd.getTokenStorage().getToken(tokenId);
        } catch (TrustException e) {
            throw new RampartException("errorInRetrievingTokenId", 
                    new String[]{tokenId}, e);
        }
        
        if(tok == null) {
            throw new RampartException("errorInRetrievingTokenId", 
                    new String[]{tokenId});
        }
        return tok;
    }
    

    protected void addSignatureConfirmation(RampartMessageData rmd, Vector sigParts) {
        
        if(!rmd.getPolicyData().isSignatureConfirmation()) {
            
            //If we don't require sig confirmation simply go back :-)
            return;
        }
        
        Document doc = rmd.getDocument();
        
        Vector results = (Vector)rmd.getMsgContext().getProperty(WSHandlerConstants.RECV_RESULTS);
        /*
         * loop over all results gathered by all handlers in the chain. For each
         * handler result get the various actions. After that loop we have all
         * signature results in the signatureActions vector
         */
        Vector signatureActions = new Vector();
        for (int i = 0; i < results.size(); i++) {
            WSHandlerResult wshResult = (WSHandlerResult) results.get(i);

            WSSecurityUtil.fetchAllActionResults(wshResult.getResults(),
                    WSConstants.SIGN, signatureActions);
            WSSecurityUtil.fetchAllActionResults(wshResult.getResults(),
                    WSConstants.ST_SIGNED, signatureActions);
            WSSecurityUtil.fetchAllActionResults(wshResult.getResults(),
                    WSConstants.UT_SIGN, signatureActions);
        }
        
        // prepare a SignatureConfirmation token
        WSSecSignatureConfirmation wsc = new WSSecSignatureConfirmation();
        if (signatureActions.size() > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Signature Confirmation: number of Signature results: "
                        + signatureActions.size());
            }
            for (int i = 0; i < signatureActions.size(); i++) {
                WSSecurityEngineResult wsr = (WSSecurityEngineResult) signatureActions
                        .get(i);
                byte[] sigVal = wsr.getSignatureValue();
                wsc.setSignatureValue(sigVal);
                wsc.prepare(doc);
                RampartUtil.appendChildToSecHeader(rmd, wsc.getSignatureConfirmationElement());
                if(sigParts != null) {
                    sigParts.add(new WSEncryptionPart(wsc.getId()));
                }
            }
        } else {
            //No Sig value
            wsc.prepare(doc);
            RampartUtil.appendChildToSecHeader(rmd, wsc.getSignatureConfirmationElement());
            if(sigParts != null) {
                sigParts.add(new WSEncryptionPart(wsc.getId()));
            }
        }
    }

    
}
