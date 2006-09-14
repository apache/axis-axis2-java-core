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
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.message.WSSecDKEncrypt;
import org.apache.ws.security.message.WSSecDKSign;
import org.apache.ws.security.message.WSSecEncrypt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Vector;


public class SymmetricBindingBuilder extends BindingBuilder {

    private static Log log = LogFactory.getLog(SymmetricBindingBuilder.class);
    
    
    public void build(RampartMessageData rmd) throws RampartException {
        
        log.debug("SymmetricBindingBuilder build invoked");
        
        RampartPolicyData rpd = rmd.getPolicyData();
        if(rpd.isIncludeTimestamp()) {
            this.addTimestamp(rmd);
        }
        
        //Setup required tokens
        initializeTokens(rmd);
        
        if(rmd.isClientSide()) {
            
            if(Constants.ENCRYPT_BEFORE_SIGNING.equals(rpd.getProtectionOrder())) {
                this.doEncryptBeforeSig(rmd);
            } else {
            }
        } else {
            
        }
        
        log.debug("SymmetricBindingBuilder build invoked : DONE");
        
    }
    
    private void doEncryptBeforeSig(RampartMessageData rmd) throws RampartException {
        
        RampartPolicyData rpd = rmd.getPolicyData();
        
        Vector signatureValues = new Vector();
        
        Token encryptionToken = rpd.getEncryptionToken();
        if(encryptionToken != null) {
            //The encryption token can be an IssuedToken or a 
             //SecureConversationToken
            String tokenId = null;
            org.apache.rahas.Token tok = null;
            
            if(encryptionToken instanceof IssuedToken) {
                tokenId = rmd.getIssuedEncryptionTokenId();
                log.debug("Issued EncryptionToken Id : " + tokenId);
            } else if(encryptionToken instanceof SecureConversationToken) {
                tokenId = rmd.getSecConvTokenId();
                log.debug("SCT Id : " + tokenId);
            }
            
            /*
             * Get hold of the token from the token storage
             */
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

            /*
             * Attach the token into the message based on token inclusion 
             * values
             */
            boolean attached = false;
            Element encrTokenElement = null;
            Element refList = null;
            WSSecDKEncrypt dkEncr = null;
            WSSecEncrypt encr = null;
            Element encrDKTokenElem = null;
            
            if(Constants.INCLUDE_ALWAYS.equals(encryptionToken.getInclusion()) ||
                    Constants.INCLUDE_ONCE.equals(encryptionToken.getInclusion())) {
                encrTokenElement = RampartUtil.appendChildToSecHeader(rmd, tok.getToken());
                attached = true;
            }
            
            Vector encrParts = RampartUtil.getEncryptedParts(rmd);
            
            Document doc = rmd.getDocument();
            

            
            if(encryptionToken.isDerivedKeys()) {
                log.debug("Use drived keys");
                
                dkEncr = new WSSecDKEncrypt();
                
                if(attached && tok.getAttachedReference() != null) {
                    
                    dkEncr.setExternalKey(tok.getSecret(), (Element) doc
                            .importNode((Element) tok.getAttachedReference(),
                                    true));
                    
                } else if(tok.getUnattachedReference() != null) {
                    dkEncr.setExternalKey(tok.getSecret(), (Element) doc
                            .importNode((Element) tok.getUnattachedReference(),
                                    true));
                }
                try {
                    encrDKTokenElem = dkEncr.getdktElement();
                    RampartUtil.appendChildToSecHeader(rmd, encrDKTokenElem);
                    dkEncr.prepare(doc);
                    refList = dkEncr.encryptForExternalRef(null, encrParts);
                    
                } catch (WSSecurityException e) {
                    throw new RampartException("errorInDKEncr");
                } catch (ConversationException e) {
                    throw new RampartException("errorInDKEncr");
                }
            } else {
                log.debug("NO derived keys, use the shared secret");
                encr = new WSSecEncrypt();
                
                encr.setWsConfig(rmd.getConfig());
                
                encr.setEphemeralKey(tok.getSecret());
                encr.setDocument(doc);
                
                try {
                    //Encrypt, get hold of the ref list and add it
                    refList = encr.encryptForExternalRef(null, encrParts);
                } catch (WSSecurityException e) {
                    throw new RampartException("errorInEncryption", e);
                }
            }
            
            RampartUtil.appendChildToSecHeader(rmd, refList);
            
            this.setInsertionLocation(refList);

//          Now add the supporting tokens
            SupportingToken sgndSuppTokens = rpd.getSignedSupportingTokens();
            
            HashMap sigSuppTokMap = this.handleSupportingTokens(rmd, sgndSuppTokens);
            
            SupportingToken endSuppTokens = rpd.getEndorsingSupportingTokens();

            HashMap endSuppTokMap = this.handleSupportingTokens(rmd, endSuppTokens);

            SupportingToken sgndEndSuppTokens = rpd.getSignedEndorsingSupportingTokens();
            
            HashMap sgndEndSuppTokMap = this.handleSupportingTokens(rmd, sgndEndSuppTokens);

            //Setup signature parts
            Vector sigParts = addSignatureParts(sigSuppTokMap, rpd.getSignedParts());
            sigParts = addSignatureParts(sgndEndSuppTokMap, sigParts);
            
            //Sign the message
            //We should use the same key in the case of EncryptBeforeSig
            if(encryptionToken.isDerivedKeys()) {
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
                    
                    
                    dkSign.prepare(doc);
                    
                    sigParts.add(new WSEncryptionPart(rmd.getTimestampId()));                          
                    
                    if(rpd.isTokenProtection() && attached) {
                        sigParts.add(new WSEncryptionPart(tokenId));
                    }
                    
                    dkSign.setParts(sigParts);
                    
                    dkSign.addReferencesToSign(sigParts, rmd.getSecHeader());
                    
                    //Do signature
                    dkSign.computeSignature();
                    
                    signatureValues.add(dkSign.getSignatureValue());
                    
                    //Add elements to header
                    this.setInsertionLocation(RampartUtil
                            .insertSiblingAfter(this.getInsertionLocation(),
                                    dkSign.getdktElement()));

                    this.setInsertionLocation(RampartUtil.insertSiblingAfter(
                            this.getInsertionLocation(), dkSign
                                    .getSignatureElement()));
                    this.mainSigId = RampartUtil.addWsuIdToElement((OMElement)dkSign.getSignatureElement());
                    
                } catch (ConversationException e) {
                    throw new RampartException(
                            "errorInDerivedKeyTokenSignature", e);
                } catch (WSSecurityException e) {
                    throw new RampartException(
                            "errorInDerivedKeyTokenSignature", e);
                }
            } else {
                //TODO :  Example SAMLTOken Signature
            }
            
            //Do endorsed signatures
            this.doEndorsedSignatures(rmd, endSuppTokMap);
            
            //Do signed endorsing signatures
            this.doEndorsedSignatures(rmd, sgndEndSuppTokMap);
            
            //Check for signature protection
            if(rpd.isSignatureProtection() && this.mainSigId != null) {
                //Now encrypt the signature using the above token
                Vector secondEncrParts = new Vector();
                secondEncrParts.add(new WSEncryptionPart(this.mainSigId, "Element"));
                
                Element secondRefList = null;
                
                if(encryptionToken.isDerivedKeys()) {
                    try {
                        secondRefList = dkEncr.encryptForExternalRef(null, 
                                secondEncrParts);
                        RampartUtil.insertSiblingAfter(encrDKTokenElem, 
                                secondRefList);
                    } catch (WSSecurityException e) {
                        throw new RampartException("errorInDKEncr");
                    }
                } else {
                    try {
                        //Encrypt, get hold of the ref list and add it
                        secondRefList = encr.encryptForExternalRef(null,
                                encrParts);
                        RampartUtil.insertSiblingAfter(encrTokenElement,
                                secondRefList);
                    } catch (WSSecurityException e) {
                        throw new RampartException("errorInEncryption", e);
                    }    
                }
            }
        }
    }
    
    
    
    /**
     * Setup the required tokens
     * @param rmd
     * @param rpd
     * @throws RampartException
     */
    private void initializeTokens(RampartMessageData rmd) throws RampartException {
        
        RampartPolicyData rpd = rmd.getPolicyData();
        
        if(rpd.isSymmetricBinding() && !rmd.getMsgContext().isServerSide()) {
            log.debug("Procesing symmentric binding: " +
                    "Setting up encryption token and signature token");
            //Setting up encryption token and signature token
            
            Token sigTok = rpd.getSignatureToken();
            Token encrTok = rpd.getEncryptionToken();
            if(sigTok instanceof IssuedToken) {
                
                log.debug("SignatureToken is an IssuedToken");
                
                if(rmd.getIssuedSignatureTokenId() == null) {
                    log.debug("No Issuedtoken found, requesting a new token");
                    
                    IssuedToken issuedToken = (IssuedToken)sigTok;
                    
                    String id = RampartUtil.getIssuedToken(rmd, 
                            issuedToken);
                    rmd.setIssuedSignatureTokenId(id);
                    
                    
                }
                
            } else if(sigTok instanceof SecureConversationToken) {
                
                log.debug("SignatureToken is a SecureConversationToken");
                
                if(rmd.getSecConvTokenId() == null) {
                
                    log.debug("No SecureConversationToken found, " +
                            "requesting a new token");
                    
                    SecureConversationToken secConvTok = 
                                        (SecureConversationToken) sigTok;
                    
                    try {
                        
                        String id = RampartUtil.getSecConvToken(rmd, 
                                secConvTok);
                        rmd.setSecConvTokenId(id);
                        
                    } catch (TrustException e) {
                        throw new RampartException(e.getMessage(), e);
                    }
                }
            }
            
            //If it was the ProtectionToken assertion then sigTok is the
            //same as encrTok
            if(sigTok.equals(encrTok) && sigTok instanceof IssuedToken) {
                
                log.debug("Symmetric binding uses a ProtectionToken, both" +
                        " SignatureToken and EncryptionToken are the same");
                
                rmd.setIssuedEncryptionTokenId(rmd.getIssuedEncryptionTokenId());
            } else {
                //Now we'll have to obtain the encryption token as well :-)
                //ASSUMPTION: SecureConversationToken is used as a 
                //ProtectionToken therfore we only have to process a issued 
                //token here
                
                log.debug("Obtaining the Encryption Token");
                if(rmd.getIssuedEncryptionTokenId() != null) {
                    
                    log.debug("EncrytionToken not alredy set");

                    IssuedToken issuedToken = (IssuedToken)encrTok;
                        
                    String id = RampartUtil.getIssuedToken(rmd, 
                            issuedToken);
                    rmd.setIssuedEncryptionTokenId(id);

                }
                
            }
        }
        
        //TODO : Support processing IssuedToken and SecConvToken assertoins
        //in supporting tokens, right now we only support UsernameTokens and 
        //X.509 Tokens
    }


    
}
