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
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rahas.RahasConstants;
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
import org.apache.ws.secpolicy.model.X509Token;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.message.WSSecDKEncrypt;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


public class SymmetricBindingBuilder extends BindingBuilder {

    private static Log log = LogFactory.getLog(SymmetricBindingBuilder.class);
    
    
    public void build(RampartMessageData rmd) throws RampartException {
        
        log.debug("SymmetricBindingBuilder build invoked");
        
        RampartPolicyData rpd = rmd.getPolicyData();
        if(rpd.isIncludeTimestamp()) {
            this.addTimestamp(rmd);
        }
        
        if(rmd.isClientSide()) {
            //Setup required tokens
            initializeTokens(rmd);
        }
        
            
        if(Constants.ENCRYPT_BEFORE_SIGNING.equals(rpd.getProtectionOrder())) {
            this.doEncryptBeforeSig(rmd);
        } else {
            this.doSignBeforeEncrypt(rmd);
        }

    
        log.debug("SymmetricBindingBuilder build invoked : DONE");
        
    }
    
    private void doEncryptBeforeSig(RampartMessageData rmd) throws RampartException {
        
        RampartPolicyData rpd = rmd.getPolicyData();
        
        Vector signatureValues = new Vector();
        
        Token encryptionToken = rpd.getEncryptionToken();
        Vector encrParts = RampartUtil.getEncryptedParts(rmd);

        if(encryptionToken == null && encrParts.size() > 0) {
            throw new RampartException("encryptionTokenMissing");
        }
        
        if(encryptionToken != null && encrParts.size() > 0) {
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
            } else if (encryptionToken instanceof X509Token) {
                tokenId = setupEncryptedKey(rmd, encryptionToken);
            } //TODO SAMLToken
            
            if(tokenId == null || tokenId.length() == 0) {
                throw new RampartException("noSecurityToken");
            }
            
            /*
             * Get hold of the token from the token storage
             */
            tok = this.getToken(rmd, tokenId);

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
                    Constants.INCLUDE_ONCE.equals(encryptionToken.getInclusion()) ||
                    (rmd.isClientSide() && Constants.INCLUDE_ALWAYS_TO_RECIPIENT.equals(encryptionToken.getInclusion()))) {
                encrTokenElement = RampartUtil.appendChildToSecHeader(rmd, tok.getToken());
                attached = true;
            }
            
            //In the X509 case we MUST add the EncryptedKey
            if(encryptionToken instanceof X509Token) {
                RampartUtil.appendChildToSecHeader(rmd, tok.getToken());
            }
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
                } else {
                    dkEncr.setExternalKey(tok.getSecret(), tok.getId());
                }
                try {
                    dkEncr.prepare(doc);
                    encrDKTokenElem = dkEncr.getdktElement();
                    RampartUtil.appendChildToSecHeader(rmd, encrDKTokenElem);
                    
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
            
            this.setInsertionLocation(encrTokenElement);

            HashMap sigSuppTokMap = null;
            HashMap endSuppTokMap = null;
            HashMap sgndEndSuppTokMap = null;
            Vector sigParts = RampartUtil.getSignedParts(rmd);
            sigParts.add(new WSEncryptionPart(RampartUtil
                    .addWsuIdToElement((OMElement) this.timestampElement)));

            if(rmd.isClientSide()) {
            
    //          Now add the supporting tokens
                SupportingToken sgndSuppTokens = rpd.getSignedSupportingTokens();
                
                sigSuppTokMap = this.handleSupportingTokens(rmd, sgndSuppTokens);
                
                SupportingToken endSuppTokens = rpd.getEndorsingSupportingTokens();
    
                endSuppTokMap = this.handleSupportingTokens(rmd, endSuppTokens);
    
                SupportingToken sgndEndSuppTokens = rpd.getSignedEndorsingSupportingTokens();
                
                sgndEndSuppTokMap = this.handleSupportingTokens(rmd, sgndEndSuppTokens);
    
                //Setup signature parts
                sigParts = addSignatureParts(sigSuppTokMap, sigParts);
                sigParts = addSignatureParts(sgndEndSuppTokMap, sigParts);
            } else {
                addSignatureConfirmation(rmd, sigParts);
            }
            
            //Sign the message
            //We should use the same key in the case of EncryptBeforeSig
            signatureValues.add(this.doSymmSignature(rmd, encryptionToken, tok, sigParts));

            this.mainSigId = RampartUtil.addWsuIdToElement((OMElement)this.getInsertionLocation());
            
            
            if(rmd.isClientSide()) {
                //Do endorsed signatures
                Vector endSigVals = this.doEndorsedSignatures(rmd, endSuppTokMap);
                for (Iterator iter = endSigVals.iterator(); iter.hasNext();) {
                    signatureValues.add(iter.next());
                }
                
                //Do signed endorsing signatures
                Vector sigEndSigVals = this.doEndorsedSignatures(rmd, sgndEndSuppTokMap);
                for (Iterator iter = sigEndSigVals.iterator(); iter.hasNext();) {
                    signatureValues.add(iter.next());
                }
            }
            
            //Check for signature protection
            if(rpd.isSignatureProtection() && this.mainSigId != null) {
                
                Vector secondEncrParts = new Vector();
                
                //Now encrypt the signature using the above token
                secondEncrParts.add(new WSEncryptionPart(this.mainSigId, "Element"));
                
                Element secondRefList = null;
                
                if(encryptionToken.isDerivedKeys()) {
                    try {
                        secondRefList = dkEncr.encryptForExternalRef(null, 
                                secondEncrParts);
                        RampartUtil.insertSiblingAfter(
                                rmd, 
                                encrDKTokenElem, 
                                secondRefList);
                    } catch (WSSecurityException e) {
                        throw new RampartException("errorInDKEncr");
                    }
                } else {
                    try {
                        //Encrypt, get hold of the ref list and add it
                        secondRefList = encr.encryptForExternalRef(null,
                                encrParts);
                        RampartUtil.insertSiblingAfter(
                                rmd, 
                                encrTokenElement,
                                secondRefList);
                    } catch (WSSecurityException e) {
                        throw new RampartException("errorInEncryption", e);
                    }    
                }
            }
           
        } else {
            throw new RampartException("encryptionTokenMissing");
        }
    }


    private void doSignBeforeEncrypt(RampartMessageData rmd) throws RampartException {

        RampartPolicyData rpd = rmd.getPolicyData();
        Document doc = rmd.getDocument();
        
        Token sigToken = rpd.getSignatureToken();
        
        String encrTokId = null;
        String sigTokId = null;
        
        org.apache.rahas.Token encrTok = null;
        org.apache.rahas.Token sigTok = null;
        
        Element sigTokElem = null;
        
        Vector signatureValues = new Vector();
        
        if(sigToken != null) {
            if(sigToken instanceof SecureConversationToken) {
                sigTokId = rmd.getSecConvTokenId();
            } else if(sigToken instanceof IssuedToken) {
                sigTokId = rmd.getIssuedSignatureTokenId();
            } else if(sigToken instanceof X509Token) {
                sigTokId = setupEncryptedKey(rmd, sigToken);
            }
        } else {
            throw new RampartException("signatureTokenMissing");
        }
        
        if(sigTokId == null || sigTokId.length() == 0) {
            throw new RampartException("noSecurityToken");
        }
        
        sigTok = this.getToken(rmd, sigTokId);

        if(Constants.INCLUDE_ALWAYS.equals(sigToken.getInclusion()) ||
                Constants.INCLUDE_ONCE.equals(sigToken.getInclusion()) ||
                (rmd.isClientSide() && 
                        Constants.INCLUDE_ALWAYS_TO_RECIPIENT.equals(
                                sigToken.getInclusion()))) {
            sigTokElem = RampartUtil.appendChildToSecHeader(rmd, 
                                                            sigTok.getToken());
            this.setInsertionLocation(sigTokElem);
        }
        

        
        //In the X509 case we MUST add the EncryptedKey
        if(sigToken instanceof X509Token) {
            sigTokElem = RampartUtil.appendChildToSecHeader(rmd, sigTok.getToken());
            
            //Set the insertion location
            this.setInsertionLocation(sigTokElem);
        }
        

        HashMap sigSuppTokMap = null;
        HashMap endSuppTokMap = null;
        HashMap sgndEndSuppTokMap = null;
        Vector sigParts = RampartUtil.getSignedParts(rmd);
        sigParts.add(new WSEncryptionPart(RampartUtil
                .addWsuIdToElement((OMElement) this.timestampElement)));

        if(rmd.isClientSide()) {
    //      Now add the supporting tokens
            SupportingToken sgndSuppTokens = rpd.getSignedSupportingTokens();
            
            sigSuppTokMap = this.handleSupportingTokens(rmd, sgndSuppTokens);
            
            SupportingToken endSuppTokens = rpd.getEndorsingSupportingTokens();
    
            endSuppTokMap = this.handleSupportingTokens(rmd, endSuppTokens);
    
            SupportingToken sgndEndSuppTokens = rpd.getSignedEndorsingSupportingTokens();
            
            sgndEndSuppTokMap = this.handleSupportingTokens(rmd, sgndEndSuppTokens);
    
            //Setup signature parts
            sigParts = addSignatureParts(sigSuppTokMap, rpd.getSignedParts());
            sigParts = addSignatureParts(sgndEndSuppTokMap, sigParts);
        } else {
            addSignatureConfirmation(rmd, sigParts);
        }
        //Sign the message
        signatureValues.add(this.doSymmSignature(rmd, sigToken, sigTok, sigParts));

        this.mainSigId = RampartUtil.addWsuIdToElement((OMElement)this.getInsertionLocation());

        if(rmd.isClientSide()) {
            //Do endorsed signatures
            Vector endSigVals = this.doEndorsedSignatures(rmd, endSuppTokMap);
            for (Iterator iter = endSigVals.iterator(); iter.hasNext();) {
                signatureValues.add(iter.next());
            }
            
            //Do signed endorsing signatures
            Vector sigEndSigVals = this.doEndorsedSignatures(rmd, sgndEndSuppTokMap);
            for (Iterator iter = sigEndSigVals.iterator(); iter.hasNext();) {
                signatureValues.add(iter.next());
            }
        }

        //Encryption
        Token encrToken = rpd.getEncryptionToken();
        Element encrTokElem = null;
        if(sigToken.equals(encrToken)) {
            //Use the same token
            encrTokId = sigTokId;
            encrTok = sigTok;
            encrTokElem = sigTokElem;
        } else {
            encrTokId = rmd.getIssuedEncryptionTokenId();
            encrTok = this.getToken(rmd, encrTokId);
            
            if(Constants.INCLUDE_ALWAYS.equals(encrToken.getInclusion()) ||
                    Constants.INCLUDE_ONCE.equals(encrToken.getInclusion()) ||
                    (rmd.isClientSide() && Constants.INCLUDE_ALWAYS_TO_RECIPIENT.equals(encrToken.getInclusion()))) {
                encrTokElem = (Element)encrTok.getToken();
                
                //Add the encrToken element before the sigToken element
                RampartUtil.insertSiblingBefore(rmd, sigTokElem, encrTokElem);
            }
            
        }
        
        Vector encrParts = RampartUtil.getEncryptedParts(rmd);
        
        //Check for signature protection
        if(rpd.isSignatureProtection() && this.mainSigId != null) {
            //Now encrypt the signature using the above token
            encrParts.add(new WSEncryptionPart(this.mainSigId, "Element"));
        }
        Element refList = null;
        
        if(encrToken.isDerivedKeys() || encrToken instanceof SecureConversationToken) {
            
            try {
                WSSecDKEncrypt dkEncr = new WSSecDKEncrypt();
                
                if(encrTokElem != null && encrTok.getAttachedReference() != null) {
                    
                    dkEncr.setExternalKey(encrTok.getSecret(), (Element) doc
                            .importNode((Element) encrTok.getAttachedReference(),
                                    true));
                    
                } else if(encrTok.getUnattachedReference() != null) {
                    dkEncr.setExternalKey(encrTok.getSecret(), (Element) doc
                            .importNode((Element) encrTok.getUnattachedReference(),
                                    true));
                } else {
                    dkEncr.setExternalKey(encrTok.getSecret(), encrTok.getId());
                }
                
                dkEncr.prepare(doc);
                Element encrDKTokenElem = null;
                encrDKTokenElem = dkEncr.getdktElement();
                if(encrTokElem != null) {
                    RampartUtil.insertSiblingAfter(rmd, encrTokElem, encrDKTokenElem);
                } else {
                    RampartUtil.insertSiblingAfter(rmd, this.timestampElement, encrDKTokenElem);
                }
                
                refList = dkEncr.encryptForExternalRef(null, encrParts);
                
                RampartUtil.insertSiblingAfter(rmd, 
                                                encrDKTokenElem, 
                                                refList);

            } catch (WSSecurityException e) {
                throw new RampartException("errorInDKEncr");
            } catch (ConversationException e) {
                throw new RampartException("errorInDKEncr");
            }
        } else {
            try {
                
                WSSecEncrypt encr = new WSSecEncrypt();
                
                encr.setWsConfig(rmd.getConfig());
                
                encr.setEphemeralKey(encrTok.getSecret());
                RampartUtil.setEncryptionUser(rmd, encr);
                encr.setDocument(doc);
                encr.prepare(doc, RampartUtil.getEncryptionCrypto(rpd
                        .getRampartConfig(), rmd.getCustomClassLoader()));
                
                //Encrypt, get hold of the ref list and add it
                refList = encr.encryptForExternalRef(null, encrParts);

                RampartUtil.insertSiblingAfter(rmd,
                                                encrTokElem,
                                                refList);
            } catch (WSSecurityException e) {
                throw new RampartException("errorInEncryption", e);
            }    
        }
    }

    /**
     * @param rmd
     * @param sigToken
     * @return
     * @throws RampartException
     */
    private String setupEncryptedKey(RampartMessageData rmd, Token sigToken) 
    throws RampartException {
        try {
            WSSecEncryptedKey encrKey = this.getEncryptedKeyBuilder(rmd, 
                                                                sigToken);
            String id = encrKey.getId();
            //Create a rahas token from this info and store it so we can use
            //it in the next steps
    
            Date created = new Date();
            Date expires = new Date();
            //TODO make this lifetime configurable ???
            expires.setTime(System.currentTimeMillis() + 300000);
            org.apache.rahas.Token tempTok = new org.apache.rahas.Token(
                            id, 
                            (OMElement) encrKey.getEncryptedKeyElement(),
                            created, 
                            expires);
            tempTok.setSecret(encrKey.getEphemeralKey());
            
            rmd.getTokenStorage().add(tempTok);
            
            String bstTokenId = encrKey.getBSTTokenId();
            //If direct ref is used to refer to the cert
            //then add the cert to the sec header now
            if(bstTokenId != null && bstTokenId.length() > 0) {
                RampartUtil.appendChildToSecHeader(rmd, 
                        encrKey.getBinarySecurityTokenElement());
            }
            
            return id;
            
        } catch (TrustException e) {
            throw new RampartException("errorInAddingTokenIntoStore");
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
        
        MessageContext msgContext = rmd.getMsgContext();
        if(rpd.isSymmetricBinding() && !msgContext.isServerSide()) {
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
                
                //TODO check for an existing token and use it 
                
                String secConvTokenId = rmd.getSecConvTokenId();
                
                //The RSTR has to be secured with the cancelled token
                String action = msgContext.getOptions().getAction();
                boolean cancelReqResp = action.equals(RahasConstants.WST_NS_05_02 + RahasConstants.RSTR_ACTION_CANCEL_SCT) || 
                                           action.equals(RahasConstants.WST_NS_05_02 + RahasConstants.RSTR_ACTION_CANCEL_SCT) ||
                                           action.equals(RahasConstants.WST_NS_05_02 + RahasConstants.RST_ACTION_CANCEL_SCT) || 
                                           action.equals(RahasConstants.WST_NS_05_02 + RahasConstants.RST_ACTION_CANCEL_SCT);
                
                //In the case of the cancel req or resp we should mark the token as cancelled
                if(secConvTokenId != null && cancelReqResp) {
                    try {
                        rmd.getTokenStorage().getToken(secConvTokenId).setState(org.apache.rahas.Token.CANCELLED);
                        msgContext.setProperty(RampartMessageData.SCT_ID, secConvTokenId);
                        
                        //remove from the local map of contexts
                        String contextIdentifierKey = RampartUtil.getContextIdentifierKey(msgContext);
                        RampartUtil.getContextMap(msgContext).remove(contextIdentifierKey);
                    } catch (TrustException e) {
                        throw new RampartException("errorExtractingToken");
                    }
                }
                
                if (secConvTokenId == null
                        || (secConvTokenId != null && 
                                (!RampartUtil.isTokenValid(rmd, secConvTokenId) && !cancelReqResp))) {
                
                    log.debug("No SecureConversationToken found, " +
                            "requesting a new token");
                    
                    SecureConversationToken secConvTok = 
                                        (SecureConversationToken) sigTok;
                    
                    try {

                        String id = RampartUtil.getSecConvToken(rmd, secConvTok);
                        rmd.setSecConvTokenId(id);
                        
                    } catch (TrustException e) {
                        throw new RampartException("errorInObtainingSct", e);
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
