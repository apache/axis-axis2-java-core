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
import org.apache.ws.secpolicy.model.SupportingToken;
import org.apache.ws.secpolicy.model.Token;
import org.apache.ws.secpolicy.model.UsernameToken;
import org.apache.ws.secpolicy.model.X509Token;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public abstract class BindingBuilder {
    private static Log log = LogFactory.getLog(BindingBuilder.class);
            
    private Element insertionLocation;
    
    /**
     * @param rmd
     * @param doc
     */
    protected void addTimestamp(RampartMessageData rmd) {
        log.debug("Adding timestamp");
        
        WSSecTimestamp timeStampBuilder = new WSSecTimestamp();
        timeStampBuilder.setWsConfig(rmd.getConfig());

        timeStampBuilder.setTimeToLive(RampartUtil.getTimeToLive(rmd));
        
        // add the Timestamp to the SOAP Enevelope

        timeStampBuilder.build(rmd.getDocument(), rmd
                .getSecHeader());
        
        log.debug("Timestamp id: " + timeStampBuilder.getId());

        rmd.setTimestampId(timeStampBuilder.getId());
        
        log.debug("Adding timestamp: DONE");
    }
    
    /**
     * Add a UsernameToken to the security header
     * @param rmd
     * @param rpd
     * @param doc
     * @return 
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
    protected WSSecEncryptedKey getEncryptedKeyBuilder(RampartMessageData rmd, Token token) throws WSSecurityException, RampartException {
        
        RampartPolicyData rpd = rmd.getPolicyData();
        Document doc = rmd.getDocument();
        
        WSSecEncryptedKey encrKey = new WSSecEncryptedKey();
        if(token.getInclusion().equals(Constants.INCLUDE_NEVER)) {
            //Use thumbprint
            encrKey.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
        } else {
            encrKey.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        }
        encrKey.setUserInfo(rpd.getRampartConfig().getEncryptionUser());
        encrKey.setKeySize(rpd.getAlgorithmSuite().getMaximumSymmetricKeyLength());
        encrKey.setKeyEncAlgo(rpd.getAlgorithmSuite().getAsymmetricKeyWrap());
        
        encrKey.prepare(doc, RampartUtil.getEncryptionCrypto(rpd.getRampartConfig()));
        
        return encrKey;
    }
    
    
    protected WSSecSignature getSignatureBuider(RampartMessageData rmd, Token token) throws RampartException {

        RampartPolicyData rpd = rmd.getPolicyData();
        
        WSSecSignature sig = new WSSecSignature();
        sig.setWsConfig(rmd.getConfig());
        
        log.debug("Token inclusion: " + token.getInclusion());
        if(token.getInclusion().equals(Constants.INCLUDE_NEVER)) {
            //Use thumbprint
            sig.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
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
                    .getRampartConfig()), rmd.getSecHeader());
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
    protected ArrayList handleSupportingTokens(RampartMessageData rmd, SupportingToken suppTokens)
            throws RampartException {
        
        //Create the list to hold the tokens
        ArrayList endSuppTokList = new ArrayList();
        
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
                            .insertSiblingAfter(this.getInsertionLocation(),
                                    (Element) endSuppTok.getToken());
                    this.setInsertionLocation(siblingElem);
                    
                    //Add the extracted token
                    endSuppTokList.add(endSuppTok);
                    
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
                                        .insertSiblingAfter(this.getInsertionLocation(),
                                                bstElem);
                               this.setInsertionLocation(siblingElem);
                            }
                            
                            Element siblingElem = RampartUtil
                                    .insertSiblingAfter(
                                            this.getInsertionLocation(),
                                            encrKey.getEncryptedKeyElement());
                            
                            this.setInsertionLocation(siblingElem);
                            
                            Date now = new Date();
                            endSuppTok =  
                                new org.apache.rahas.Token(encrKey.getId(), 
                                        (OMElement)encrKey.getEncryptedKeyElement(),
                                        now, new Date(now.getTime() + 300000));
                            
                            endSuppTokList.add(endSuppTok);
                            
                        } catch (WSSecurityException e) {
                            throw new RampartException("errorCreatingEncryptedKey", e);
                        } catch (TrustException e) {
                            throw new RampartException("errorCreatingRahasToken", e);
                        }
                    } else {
                        //We have to use a cert
                        //Prepare X509 signature
                        WSSecSignature sig = this.getSignatureBuider(rmd, token);
                        Element bstElem = sig.getBinarySecurityTokenElement();
                        if(bstElem != null) {   
                            bstElem = RampartUtil.insertSiblingAfter(this
                                    .getInsertionLocation(), bstElem);
                            this.setInsertionLocation(bstElem);
                        }
                        endSuppTokList.add(sig);
                    }
                } else if(token instanceof UsernameToken) {
                    WSSecUsernameToken utBuilder = addUsernameToken(rmd);
                    
                    utBuilder.prepare(rmd.getDocument());
                    
                    //Add the UT
                    Element elem = utBuilder.getUsernameTokenElement();
                    RampartUtil.insertSiblingAfter(this.getInsertionLocation(), elem);
                    
                    //Move the insert location to th enext element
                    this.setInsertionLocation(elem);
                    Date now = new Date();
                    try {
                        endSuppTokList.add(new org.apache.rahas.Token(utBuilder
                            .getId(), (OMElement)elem, now,
                            new Date(now.getTime() + 300000)));
                    } catch (TrustException e) {
                        throw new RampartException("errorCreatingRahasToken", e);
                    }
                }
            }
        }
        
        return endSuppTokList;
    }

    
    public Element getInsertionLocation() {
        return insertionLocation;
    }

    public void setInsertionLocation(Element insertionLocation) {
        this.insertionLocation = insertionLocation;
    }
    
}
