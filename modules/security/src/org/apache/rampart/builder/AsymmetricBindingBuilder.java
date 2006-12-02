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
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.rampart.util.RampartUtil;
import org.apache.ws.secpolicy.Constants;
import org.apache.ws.secpolicy.model.SupportingToken;
import org.apache.ws.secpolicy.model.Token;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecDKEncrypt;
import org.apache.ws.security.message.WSSecDKSign;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.message.WSSecSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class AsymmetricBindingBuilder extends BindingBuilder {

    private static Log log = LogFactory.getLog(AsymmetricBindingBuilder.class);

    private Token sigToken;

    private WSSecSignature sig;

    private WSSecEncryptedKey encrKey;
    
    private String encryptedKeyId;
    
    private byte[] encryptedKeyValue;

    private Vector signatureValues = new Vector();

    private Element encrTokenElement;
    
    private Element sigDKTElement;
    
    private Element encrDKTElement;

    private Vector sigParts = new Vector();
    
    private Element signatureElement; 

    public void build(RampartMessageData rmd) throws RampartException {
        log.debug("AsymmetricBindingBuilder build invoked");

        RampartPolicyData rpd = rmd.getPolicyData();
        if (rpd.isIncludeTimestamp()) {
            this.addTimestamp(rmd);
        }

        if (Constants.ENCRYPT_BEFORE_SIGNING.equals(rpd.getProtectionOrder())) {
            this.doEncryptBeforeSig(rmd);
        } else {
            this.doSignBeforeEncrypt(rmd);
        }

        log.debug("AsymmetricBindingBuilder build invoked : DONE");
    }

    private void doEncryptBeforeSig(RampartMessageData rmd)
            throws RampartException {

        RampartPolicyData rpd = rmd.getPolicyData();
        Document doc = rmd.getDocument();
        RampartConfig config = rpd.getRampartConfig();

        /*
         * We need to hold on to these two element to use them as refence in the
         * case of encypting the signature
         */
        Element encrDKTokenElem = null;
        WSSecEncrypt encr = null;
        Element refList = null;
        WSSecDKEncrypt dkEncr = null;

        /*
         * We MUST use keys derived from the same token
         */
        Token encryptionToken = rpd.getRecipientToken();
        Vector encrParts = RampartUtil.getEncryptedParts(rmd);

        if(encryptionToken == null && encrParts.size() > 0) {
            throw new RampartException("encryptionTokenMissing");
        }
        
        if (encryptionToken != null && encrParts.size() > 0) {
            if (encryptionToken.isDerivedKeys()) {
                try {
                    this.setupEncryptedKey(rmd, encryptionToken);
                    // Create the DK encryption builder
                    dkEncr = new WSSecDKEncrypt();
                    dkEncr.setParts(encrParts);
                    dkEncr.setExternalKey(this.encryptedKeyValue, 
                            this.encryptedKeyId);
                    dkEncr.prepare(doc);

                    // Get and add the DKT element
                    this.encrDKTElement = dkEncr.getdktElement();
                    encrDKTokenElem = RampartUtil.appendChildToSecHeader(rmd, this.encrDKTElement);

                    refList = dkEncr.encryptForExternalRef(null, encrParts);

                } catch (WSSecurityException e) {
                    throw new RampartException("errorCreatingEncryptedKey", e);
                } catch (ConversationException e) {
                    throw new RampartException("errorInDKEncr", e);
                }
            } else {
                try {
                    encr = new WSSecEncrypt();
                    encr.setParts(encrParts);
                    encr.setWsConfig(rmd.getConfig());
                    encr.setDocument(doc);
                    RampartUtil.setEncryptionUser(rmd, encr);
                    encr.setSymmetricEncAlgorithm(rpd.getAlgorithmSuite().getEncryption());
                    encr.setKeyEncAlgo(rpd.getAlgorithmSuite().getAsymmetricKeyWrap());
                    encr.prepare(doc, RampartUtil.getEncryptionCrypto(config, rmd.getCustomClassLoader()));

                    Element bstElem = encr.getBinarySecurityTokenElement();
                    if (bstElem != null) {
                        RampartUtil.appendChildToSecHeader(rmd, bstElem);
                    }

                    this.encrTokenElement = encr.getEncryptedKeyElement();
                    this.encrTokenElement = RampartUtil.appendChildToSecHeader(rmd,
                            encrTokenElement);

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
            this.sigParts = RampartUtil.getSignedParts(rmd);
            sigParts.add(new WSEncryptionPart(RampartUtil
                    .addWsuIdToElement((OMElement) this.timestampElement)));

            if (rmd.isClientSide()) {

                // Now add the supporting tokens
                SupportingToken sgndSuppTokens = rpd
                        .getSignedSupportingTokens();

                sigSuppTokMap = this
                        .handleSupportingTokens(rmd, sgndSuppTokens);

                SupportingToken endSuppTokens = rpd
                        .getEndorsingSupportingTokens();

                endSuppTokMap = this.handleSupportingTokens(rmd, endSuppTokens);

                SupportingToken sgndEndSuppTokens = rpd
                        .getSignedEndorsingSupportingTokens();

                sgndEndSuppTokMap = this.handleSupportingTokens(rmd,
                        sgndEndSuppTokens);

                // Setup signature parts
                sigParts = addSignatureParts(sigSuppTokMap, sigParts);
                sigParts = addSignatureParts(sgndEndSuppTokMap, sigParts);
            } else {
                addSignatureConfirmation(rmd, sigParts);
            }
            
            if(rpd.getInitiatorToken() != null) {
                this.doSignature(rmd);
            }

            if (rmd.isClientSide()) {
                // Do endorsed signatures
                Vector endSigVals = this.doEndorsedSignatures(rmd,
                        endSuppTokMap);
                for (Iterator iter = endSigVals.iterator(); iter.hasNext();) {
                    signatureValues.add(iter.next());
                }

                // Do signed endorsing signatures
                Vector sigEndSigVals = this.doEndorsedSignatures(rmd,
                        sgndEndSuppTokMap);
                for (Iterator iter = sigEndSigVals.iterator(); iter.hasNext();) {
                    signatureValues.add(iter.next());
                }
            }

            // Check for signature protection
            if (rpd.isSignatureProtection() && this.mainSigId != null) {

                Vector secondEncrParts = new Vector();

                // Now encrypt the signature using the above token
                secondEncrParts.add(new WSEncryptionPart(this.mainSigId,
                        "Element"));

                Element secondRefList = null;

                if (encryptionToken.isDerivedKeys()) {
                    try {

                        secondRefList = dkEncr.encryptForExternalRef(null,
                                secondEncrParts);
                        RampartUtil.insertSiblingAfter(rmd, encrDKTokenElem,
                                secondRefList);

                    } catch (WSSecurityException e) {
                        throw new RampartException("errorCreatingEncryptedKey",
                                e);
                    }
                } else {
                    try {
                        // Encrypt, get hold of the ref list and add it
                        secondRefList = encr.encryptForExternalRef(null,
                                encrParts);

                        // Insert the ref list after the encrypted key elem
                        this.setInsertionLocation(RampartUtil
                                .insertSiblingAfter(rmd, encrTokenElement,
                                        secondRefList));
                    } catch (WSSecurityException e) {
                        throw new RampartException("errorInEncryption", e);
                    }
                }
            }
        }

    }

    private void doSignBeforeEncrypt(RampartMessageData rmd)
            throws RampartException {
        RampartPolicyData rpd = rmd.getPolicyData();
        Document doc = rmd.getDocument();

        HashMap sigSuppTokMap = null;
        HashMap endSuppTokMap = null;
        HashMap sgndEndSuppTokMap = null;
        sigParts = RampartUtil.getSignedParts(rmd);
        
        //Add timestamp
        sigParts.add(new WSEncryptionPart(RampartUtil
                .addWsuIdToElement((OMElement) this.timestampElement)));

        if (rmd.isClientSide()) {
            // Now add the supporting tokens
            SupportingToken sgndSuppTokens = rpd.getSignedSupportingTokens();

            sigSuppTokMap = this.handleSupportingTokens(rmd, sgndSuppTokens);

            SupportingToken endSuppTokens = rpd.getEndorsingSupportingTokens();

            endSuppTokMap = this.handleSupportingTokens(rmd, endSuppTokens);

            SupportingToken sgndEndSuppTokens = rpd
                    .getSignedEndorsingSupportingTokens();

            sgndEndSuppTokMap = this.handleSupportingTokens(rmd,
                    sgndEndSuppTokens);

            // Setup signature parts
            sigParts = addSignatureParts(sigSuppTokMap, sigParts);
            sigParts = addSignatureParts(sgndEndSuppTokMap, sigParts);
        } else {
            addSignatureConfirmation(rmd, sigParts);
        }

        if(rpd.getInitiatorToken() != null) {
            // Do signature
            this.doSignature(rmd);
        }
        
        //Do endorsed signature

        if (rmd.isClientSide()) {
            // Do endorsed signatures
            Vector endSigVals = this.doEndorsedSignatures(rmd,
                    endSuppTokMap);
            for (Iterator iter = endSigVals.iterator(); iter.hasNext();) {
                signatureValues.add(iter.next());
            }

            // Do signed endorsing signatures
            Vector sigEndSigVals = this.doEndorsedSignatures(rmd,
                    sgndEndSuppTokMap);
            for (Iterator iter = sigEndSigVals.iterator(); iter.hasNext();) {
                signatureValues.add(iter.next());
            }
        }
        
        Vector encrParts = RampartUtil.getEncryptedParts(rmd);
        
        //Check for signature protection
        if(rpd.isSignatureProtection() && this.mainSigId != null) {
            encrParts.add(new WSEncryptionPart(RampartUtil.addWsuIdToElement((OMElement)this.signatureElement), "Element"));
        }
        
        //Do encryption
        Token encrToken = rpd.getRecipientToken();
        if(encrToken != null && encrParts.size() > 0) {
            Element refList = null;
            if(encrToken.isDerivedKeys()) {
                
                try {
                    WSSecDKEncrypt dkEncr = new WSSecDKEncrypt();
                    
                    if(this.encrKey == null) {
                        this.setupEncryptedKey(rmd, encrToken);
                    }
                    
                    dkEncr.setExternalKey(this.encryptedKeyValue, this.encryptedKeyId);
                    dkEncr.setSymmetricEncAlgorithm(rpd.getAlgorithmSuite().getEncryption());
                    dkEncr.prepare(doc);
                    
                    
                    if(this.encrTokenElement != null) {
                        this.encrDKTElement = RampartUtil.insertSiblingAfter(
                                rmd, this.encrTokenElement, dkEncr.getdktElement());
                    } else {
                        this.encrDKTElement = RampartUtil.insertSiblingBefore(
                                rmd, this.sigDKTElement, dkEncr.getdktElement());
                    }
                    
                    refList = dkEncr.encryptForExternalRef(null, encrParts);
                    
                    RampartUtil.insertSiblingAfter(rmd, 
                                                    this.encrDKTElement, 
                                                    refList);
                                                    
                } catch (WSSecurityException e) {
                    throw new RampartException("errorInDKEncr");
                } catch (ConversationException e) {
                    throw new RampartException("errorInDKEncr");
                }
            } else {
                try {
                    
                    WSSecEncrypt encr = new WSSecEncrypt();
                    
                    
                    if(encrToken.getInclusion().equals(Constants.INCLUDE_NEVER)) {
                        if(rpd.getWss10() != null && rpd.getWss10().isMustSupportRefKeyIdentifier()) {
                            encr.setKeyIdentifierType(WSConstants.SKI_KEY_IDENTIFIER);
                        } else if(rpd.getWss11() != null && rpd.getWss11().isMustSupportRefThumbprint()) {
                            encr.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);
                        }
                    } else {
                        encr.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
                    }
                    
                    encr.setWsConfig(rmd.getConfig());
                    
                    encr.setDocument(doc);
                    RampartUtil.setEncryptionUser(rmd, encr);
                    encr.setSymmetricEncAlgorithm(rpd.getAlgorithmSuite().getEncryption());
                    encr.setKeyEncAlgo(rpd.getAlgorithmSuite().getAsymmetricKeyWrap());
                    encr.prepare(doc, RampartUtil.getEncryptionCrypto(rpd
                            .getRampartConfig(), rmd.getCustomClassLoader()));
                    
                    this.setInsertionLocation(this.timestampElement);
                    if(encr.getBSTTokenId() != null) {
                        this.setInsertionLocation(RampartUtil
                                .insertSiblingAfter(rmd,
                                        this.getInsertionLocation(),
                                        encr.getBinarySecurityTokenElement()));
                    }
                    
                    Element encryptedKeyElement = encr.getEncryptedKeyElement();
                    this.setInsertionLocation(RampartUtil
                            .insertSiblingAfter(rmd,
                                    this.getInsertionLocation(),
                                    encryptedKeyElement));
                    
                    //Encrypt, get hold of the ref list and add it
                    refList = encr.encryptForInternalRef(null, encrParts);
    
                    //Add internal refs
                    encryptedKeyElement.appendChild(refList);
//                    RampartUtil.insertSiblingAfter(rmd,
//                                                    this.getInsertionLocation(),
//                                                    refList);
                } catch (WSSecurityException e) {
                    throw new RampartException("errorInEncryption", e);
                }    
            }
        }
        
    }

    private void doSignature(RampartMessageData rmd) throws RampartException {

        RampartPolicyData rpd = rmd.getPolicyData();
        Document doc = rmd.getDocument();

        sigToken = rpd.getInitiatorToken();

        if (sigToken.isDerivedKeys()) {
            // Set up the encrypted key to use
            if(this.encrKey == null) {
                setupEncryptedKey(rmd, sigToken);
            }
            
            WSSecDKSign dkSign = new WSSecDKSign();
            dkSign.setExternalKey(this.encryptedKeyValue, this.encryptedKeyId);

            // Set the algo info
            dkSign.setSignatureAlgorithm(rpd.getAlgorithmSuite()
                    .getSymmetricSignature());
            dkSign.setDerivedKeyLength(rpd.getAlgorithmSuite()
                    .getMinimumSymmetricKeyLength() / 8);
            
            try {
                dkSign.prepare(doc, rmd.getSecHeader());

                if (rpd.isTokenProtection()) {
                    sigParts.add(new WSEncryptionPart(encrKey.getId()));
                }

                dkSign.setParts(sigParts);

                dkSign.addReferencesToSign(sigParts, rmd.getSecHeader());

                // Do signature
                dkSign.computeSignature();

                 ;
                // Add elements to header
                 this.sigDKTElement = RampartUtil.insertSiblingAfter(rmd,
                        this.getInsertionLocation(), dkSign.getdktElement());
                this.setInsertionLocation(this.sigDKTElement);

                this.setInsertionLocation(RampartUtil.insertSiblingAfter(rmd,
                        this.getInsertionLocation(), dkSign
                                .getSignatureElement()));

                this.mainSigId = RampartUtil
                        .addWsuIdToElement((OMElement) dkSign
                                .getSignatureElement());

                signatureValues.add(dkSign.getSignatureValue());
                
                signatureElement = dkSign.getSignatureElement();
            } catch (WSSecurityException e) {
                throw new RampartException("errorInDerivedKeyTokenSignature", e);
            } catch (ConversationException e) {
                throw new RampartException("errorInDerivedKeyTokenSignature", e);
            }

        } else {
            sig = this.getSignatureBuider(rmd, sigToken);
            Element bstElem = sig.getBinarySecurityTokenElement();
            if(bstElem != null) {
                bstElem = RampartUtil.insertSiblingAfter(rmd, this
                                        .getInsertionLocation(), bstElem);
                this.setInsertionLocation(bstElem);
            }
            
            if (rmd.getPolicyData().isTokenProtection()
                    && sig.getBSTTokenId() != null) {
                sigParts.add(new WSEncryptionPart(sig.getBSTTokenId()));
            }

            try {
                sig.addReferencesToSign(sigParts, rmd.getSecHeader());
                sig.computeSignature();

                signatureElement = sig.getSignatureElement();
                
                this.setInsertionLocation(RampartUtil.insertSiblingAfter(
                                rmd, this.getInsertionLocation(), signatureElement));

                this.mainSigId = RampartUtil.addWsuIdToElement((OMElement) signatureElement);
            } catch (WSSecurityException e) {
                throw new RampartException("errorInSignatureWithX509Token", e);
            }
            signatureValues.add(sig.getSignatureValue());
        }

    }

    /**
     * @param rmd
     * @throws RampartException
     */
    private void setupEncryptedKey(RampartMessageData rmd, Token token) 
    throws RampartException {
        if(!rmd.isClientSide() && token.isDerivedKeys()) {
                
                //If we already have them, simply return
                if(this.encryptedKeyId != null && this.encryptedKeyValue != null) {
                    return;
                }
                
                //Use the secret from the incoming EncryptedKey element
                Object resultsObj = rmd.getMsgContext().getProperty(WSHandlerConstants.RECV_RESULTS);
                if(resultsObj != null) {
                    encryptedKeyId = RampartUtil.getRequestEncryptedKeyId((Vector)resultsObj);
                    encryptedKeyValue = RampartUtil.getRequestEncryptedKeyValue((Vector)resultsObj);
                    
                    //In the case where we don't have the EncryptedKey in the 
                    //request, for the control to have reached this state,
                    //the scenario MUST be a case where this is the response
                    //message by a listener created for an async client
                    //Therefor we will create a new EncryptedKey
                    if(encryptedKeyId == null && encryptedKeyValue == null) {
                        createEncryptedKey(rmd, token);
                    }
                } else {
                    throw new RampartException("noSecurityResults");
                }
        } else {
            createEncryptedKey(rmd, token);
        }
    }

    /**
     * Create an encrypted key element
     * @param rmd
     * @param token
     * @throws RampartException
     */
    private void createEncryptedKey(RampartMessageData rmd, Token token) throws RampartException {
        //Set up the encrypted key to use
        encrKey = this.getEncryptedKeyBuilder(rmd, token);

        Element bstElem = encrKey.getBinarySecurityTokenElement();
        if (bstElem != null) {
            // If a BST is available then use it
            RampartUtil.appendChildToSecHeader(rmd, bstElem);
        }
        
        // Add the EncryptedKey
        encrTokenElement = encrKey.getEncryptedKeyElement();
        this.encrTokenElement = RampartUtil.appendChildToSecHeader(rmd,
                encrTokenElement);
        encryptedKeyValue = encrKey.getEphemeralKey();
        encryptedKeyId = encrKey.getId();

        //Store the token for client - response verification 
        // and server - response creation
        try {
            org.apache.rahas.Token tok = new org.apache.rahas.Token(
                    encryptedKeyId, (OMElement)encrTokenElement , null, null);
            tok.setSecret(encryptedKeyValue);
            rmd.getTokenStorage().add(tok);
        } catch (TrustException e) {
            throw new RampartException("errorInAddingTokenIntoStore", e);
        }
    }
}
