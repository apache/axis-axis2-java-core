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

package org.apache.rampart;

import org.apache.axiom.om.impl.dom.jaxp.DocumentBuilderFactoryImpl;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.rampart.policy.RampartPolicyBuilder;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.rampart.policy.RampartPolicyToken;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.rampart.util.RampartUtil;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.security.SOAPConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.policy.Constants;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.security.auth.callback.CallbackHandler;

import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

public class MessageBuilder {
    
    private static Log log = LogFactory.getLog(MessageBuilder.class);
    
    public void build(MessageContext msgCtx) throws WSSPolicyException, RampartException, WSSecurityException {
        
        //TODO: Get hold of the policy from the message context
        Policy policy = new Policy();
        Iterator it = (Iterator)policy.getAlternatives().next();
        
        RampartPolicyData policyData = RampartPolicyBuilder.build(it);
        
        processEnvelope(msgCtx, policyData);
    }
    
    private void processEnvelope(MessageContext msgCtx, RampartPolicyData policyData) throws RampartException, WSSecurityException {
        log.info("Before create Message assym....");

        DocumentBuilderFactoryImpl.setDOOMRequired(true);
        
//      TODO: Convert to DOOM
        SOAPEnvelope env = msgCtx.getEnvelope();
        
        /*
         * First get the SOAP envelope as document, then create a security
         * header and insert into the document (Envelope)
         */
        Document doc = ((Element)env).getOwnerDocument();
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(doc
                .getDocumentElement());

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        Vector sigParts = new Vector();
        Vector encPartsInternal = new Vector();
        Vector encPartsExternal = new Vector();

        /*
         * Check if a timestamp is required. If yes create one and add its Id to
         * signed parts. According to WSP a timestamp must be signed
         */
        WSSecTimestamp timestamp = null;
        if (policyData.isIncludeTimestamp()) {
            timestamp = new WSSecTimestamp();
            timestamp.prepare(doc);
            sigParts.add(new WSEncryptionPart(timestamp.getId()));
        }

        /*
         * Check for a recipient token. If one is avaliable use it as token to
         * encrypt data to the recipient. This is according to WSP
         * specification. 
         */
        WSSecEncrypt recEncrypt = null;
        RampartPolicyToken recToken = null;
        if ((recToken = policyData.getRecipientToken()) != null) {
            recEncrypt = new WSSecEncrypt();
            
            recEncrypt.setKeyIdentifierType(recToken.getKeyIdentifier());
            recEncrypt.setSymmetricEncAlgorithm(recToken.getEncAlgorithm());
            recEncrypt.setKeyEnc(recToken.getEncTransportAlgorithm());
            
            
            //Get the user name from RampartConfig assertion
            String encryptionUser = policyData.getRampartConfig().getEncryptionUser();
            validateEncryptionUser(encryptionUser);
            recEncrypt.setUserInfo(encryptionUser);
            recEncrypt.prepare(doc, this.getEncryptionCrypto(policyData.getRampartConfig()));
        }

        /*
         * Check for an initiator token. If one is avaliable use it as token to
         * sign data. This is according to WSP specification. 
         * 
         * If SignatureProtection is enabled add the signature to the encrypted
         * parts vector. In any case the signature must be in the internal
         * ReferenceList (this list is a child of the EncryptedKey element).
         * 
         * If TokenProtection is enabled add an appropriate signature reference.
         * 
         * TODO Check / enable for STRTransform
         */
        WSSecSignature iniSignature = null;
        RampartPolicyToken iniToken = null;
        if ((iniToken = policyData.getInitiatorToken()) != null) {
            iniSignature = new WSSecSignature();
            iniSignature.setKeyIdentifierType(iniToken.getKeyIdentifier());
            iniSignature.setSignatureAlgorithm(iniToken.getSigAlgorithm());
            
            if (policyData.isTokenProtection()) {
                sigParts.add(new WSEncryptionPart("Token", null, null));
            }
            if (policyData.isSignatureProtection()) {
                encPartsInternal.add(new WSEncryptionPart(iniSignature.getId(),
                        "Element"));
            }
            
            String user = policyData.getRampartConfig().getUser();
            String password = this.getPrivateKeyPasswprd(policyData.getRampartConfig(), msgCtx);
            iniSignature.setUserInfo(user,password);
            iniSignature.prepare(doc, this.getSignatureCrypto(policyData.getRampartConfig()), secHeader);


        }

        Element body = WSSecurityUtil.findBodyElement(doc, soapConstants);
        if (body == null) {
            System.out
                    .println("No SOAP Body found - illegal message structure. Processing terminated");
            return;
        }
        WSEncryptionPart bodyPart = new WSEncryptionPart("Body", soapConstants
                .getEnvelopeURI(), "Content");

        /*
         * Check the protection order. If Encrypt before signing then first take
         * all parts and elements to encrypt and encrypt them. Take their ids
         * after encryption and put them to the parts to be signed.
         * 
         */
        Element externRefList = null;
        if (Constants.ENCRYPT_BEFORE_SIGNING.equals(policyData.getProtectionOrder())) {
            /*
             * Process Body: it sign and encrypt: first encrypt the body, insert
             * the body to the parts to be signed.
             * 
             * If just to be signed: add the plain Body to the parts to be
             * signed
             */
            if (policyData.isSignBody()) {
                if (policyData.isEncryptBody()) {
                    Vector parts = new Vector();
                    parts.add(bodyPart);
                    externRefList = recEncrypt.encryptForExternalRef(
                            externRefList, parts);
                    sigParts.add(bodyPart);
                } else {
                    sigParts.add(bodyPart);
                }
            }
            /*
             * Here we need to handle signed/encrypted parts:
             * 
             * Get all parts that need to be encrypted _and_ signed, encrypt
             * them, get ids of thier encrypted data elements and add these ids
             * to the parts to be signed
             * 
             * Then encrypt the remaining parts that don't need to be signed.
             * 
             * Then add the remaining parts that don't nedd to be encrypted to
             * the parts to be signed.
             * 
             * Similar handling for signed/encrypted elements (compare XPath
             * strings?)
             * 
             * After all elements are encrypted put the external refernce list
             * to the security header. is at the bottom of the security header)
             */

            recEncrypt.addExternalRefElement(externRefList, secHeader);

            /*
             * Now handle the supporting tokens - according to OASIS WSP
             * supporting tokens are not part of a Binding assertion but a top
             * level assertion similar to Wss11 or SignedParts. If supporting
             * tokens are available their BST elements have to be added later
             * (probably prepended to the initiator token - see below)
             */

            /*
             * Now add the various elements to the header. We do a strict layout
             * here.
             * 
             */
            /*
             * Prepend Signature to the supporting tokens that sign the primary
             * signature
             */
            iniSignature.prependToHeader(secHeader);
            /*
             * This prepends a possible initiator token to the security header
             */
            iniSignature.prependBSTElementToHeader(secHeader);
            /*
             * Here prepend BST elements of supporting tokens
             * (EndorsingSupportTokens), then prepend supporting token that do
             * not sign the primary signature but are signed by the primary
             * signature. Take care of the TokenProtection protery!?
             */

            /*
             * Add the encrypted key element and then the associated BST element
             * recipient token)
             */
            recEncrypt.prependToHeader(secHeader);
            recEncrypt.prependBSTElementToHeader(secHeader);

            /*
             * Now we are ready to per Signature processing.
             * 
             * First the primary Signature then supporting tokens (Signatures)
             * that sign the primary Signature.
             */
            timestamp.prependToHeader(secHeader);

            iniSignature.addReferencesToSign(sigParts, secHeader);
            iniSignature.computeSignature();
            Element internRef = recEncrypt.encryptForInternalRef(null,
                    encPartsInternal);
            recEncrypt.addInternalRefElement(internRef);
        } else {
            System.out.println("SignBeforeEncrypt needs to be implemented");
        }

        log.info("After creating Message asymm....");

    }

    /**
     * 
     * @param user
     * @param config
     * @return
     * @throws Exception
     */
    private String getPrivateKeyPasswprd(RampartConfig config, MessageContext msgCtx) throws RampartException {
        
        String user = config.getUser();
        
        log.debug("Retrieving password for user : " + user);
        
        //Fist look for a call back handler in the configuration
        CallbackHandler handler = RampartUtil.getPasswordCB(msgCtx.getAxisService().getClassLoader(),config.getPwCbClass());
        
        if(handler == null) {
            //If not found then try to find a call back handler instance located in the message context
            handler = (CallbackHandler)msgCtx.getProperty(WSHandlerConstants.PW_CALLBACK_REF);
        }
        
        if(handler == null) {
            throw new RampartException("missingCallbackHandler");
        }
        
        return RampartUtil.performCallback(handler, user, WSPasswordCallback.SIGNATURE).getPassword();
        
    }

    /**
     * @param encryptionUser
     */
    private void validateEncryptionUser(String encryptionUser) throws RampartException {
        log.debug("Validating encryption user : " + encryptionUser);
        if(encryptionUser == null || "".equals(encryptionUser)) {
            throw new RampartException("missingEncryptionUser");
        }
    }

    /**
     * Create the <code>Crypto</code> instance for signature using information 
     * from the rampart configuration assertion
     * 
     * @param config
     * @return
     * @throws RampartException
     */
    private Crypto getSignatureCrypto(RampartConfig config) throws RampartException {
        log.debug("Loading Signature crypto");
        
        CryptoConfig cryptoConfig = config.getSigCryptoConfig();
        if(cryptoConfig != null) {
            String provider = cryptoConfig.getProvider();
            log.debug("Usig provider: " + provider);
            Properties prop = cryptoConfig.getProp();
            return CryptoFactory.getInstance(provider, prop);
        } else {
            throw new RampartException("missingSignatureCrypto");
        }
    }
    
    /**
     * Create the <code>Crypto</code> instance for encryption using information 
     * from the rampart configuration assertion
     * 
     * @param config
     * @return
     * @throws RampartException
     */
    private Crypto getEncryptionCrypto(RampartConfig config) throws RampartException{
        log.debug("Loading encryption crypto");
        
        CryptoConfig cryptoConfig = config.getEncrCryptoConfig();
        if(cryptoConfig != null) {
            String provider = cryptoConfig.getProvider();
            log.debug("Usig provider: " + provider);
            Properties prop = cryptoConfig.getProp();
            return CryptoFactory.getInstance(provider, prop);
        } else {
            log.debug("Trying the signature crypto info");
            //Try using signature crypto infomation
            cryptoConfig = config.getSigCryptoConfig();
            
            if(cryptoConfig != null) {
                String provider = cryptoConfig.getProvider();
                log.debug("Usig provider: " + provider);
                Properties prop = cryptoConfig.getProp();
                return CryptoFactory.getInstance(provider, prop);
            } else {
                throw new RampartException("missingEncryptionCrypto");
            }
        }
    }
    
}
