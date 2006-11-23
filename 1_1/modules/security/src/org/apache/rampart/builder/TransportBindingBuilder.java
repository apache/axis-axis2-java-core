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
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecDKSign;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class TransportBindingBuilder extends BindingBuilder {

    private static Log log = LogFactory.getLog(TransportBindingBuilder.class);
    
    public void build(RampartMessageData rmd) throws RampartException {
        
        log.debug("TransportBindingBuilder build invoked");
        
        RampartPolicyData rpd = rmd.getPolicyData();

        addTimestamp(rmd);
        
        /*
         * Process Supporting tokens
         */
        if(rmd.isClientSide()) {
            Vector signatureValues = new Vector();
            
            SupportingToken sgndSuppTokens = rpd.getSignedSupportingTokens();
            
            if(sgndSuppTokens != null && sgndSuppTokens.getTokens() != null &&
                    sgndSuppTokens.getTokens().size() > 0) {
                
                log.debug("Processing signed supporting tokens");
                
                ArrayList tokens = sgndSuppTokens.getTokens();
                for (Iterator iter = tokens.iterator(); iter.hasNext();) {
                    
                    Token token = (Token) iter.next();
                    if(token instanceof UsernameToken) {
                        WSSecUsernameToken utBuilder = addUsernameToken(rmd);
                        utBuilder.setPasswordType(WSConstants.PASSWORD_TEXT);
                        
                        utBuilder.prepare(rmd.getDocument());
                        
                        //Add the UT
                        utBuilder.appendToHeader(rmd.getSecHeader());
                        
                    } else {
                        throw new RampartException("unsupportedSignedSupportingToken", 
                                new String[]{"{" +token.getName().getNamespaceURI() 
                                + "}" + token.getName().getLocalPart()});
                    }
                }
            }
            
            SupportingToken sgndEndSuppTokens = rpd.getSignedEndorsingSupportingTokens();
            if(sgndEndSuppTokens != null && sgndEndSuppTokens.getTokens() != null &&
                    sgndEndSuppTokens.getTokens().size() > 0) {
                
                log.debug("Processing endorsing signed supporting tokens");
                
                ArrayList tokens = sgndEndSuppTokens.getTokens();
                for (Iterator iter = tokens.iterator(); iter.hasNext();) {
                    Token token = (Token) iter.next();
                    if(token instanceof IssuedToken && rmd.isClientSide()) {
                        signatureValues.add(doIssuedTokenSignature(rmd, token));
                    } else if(token instanceof X509Token) {
                        signatureValues.add(doX509TokenSignature(rmd, token));
                    }
                }
            }
    
            SupportingToken endSupptokens = rpd.getEndorsingSupportingTokens();
            if(endSupptokens != null && endSupptokens.getTokens() != null &&
                    endSupptokens.getTokens().size() > 0) {
                log.debug("Processing endorsing supporting tokens");
                ArrayList tokens = endSupptokens.getTokens();
                for (Iterator iter = tokens.iterator(); iter.hasNext();) {
                    Token token = (Token) iter.next();
                    if(token instanceof IssuedToken && rmd.isClientSide()){
                        signatureValues.add(doIssuedTokenSignature(rmd, token));
                    } else if(token instanceof X509Token) {
                        signatureValues.add(doX509TokenSignature(rmd, token));
                    }
                }
            }
            
            //Store the signature values vector
            rmd.getMsgContext().setProperty(WSHandlerConstants.SEND_SIGV, signatureValues);
        } else {
            addSignatureConfirmation(rmd, null);
        }
    }



    /**
     * X.509 signature
     * @param rmd
     * @param token
     */
    private byte[] doX509TokenSignature(RampartMessageData rmd, Token token) throws RampartException {
        
        RampartPolicyData rpd = rmd.getPolicyData();
        Document doc = rmd.getDocument();
        
        if(token.isDerivedKeys()) {
            //In this case we will have to encrypt the ephmeral key with the 
            //other party's key and then use it as the parent key of the
            // derived keys
            try {
                
                WSSecEncryptedKey encrKey = getEncryptedKeyBuilder(rmd, token);
                
                Element bstElem = encrKey.getBinarySecurityTokenElement();
                if(bstElem != null) {
                   RampartUtil.appendChildToSecHeader(rmd, bstElem); 
                }
                
                encrKey.appendToHeader(rmd.getSecHeader());
                
                WSSecDKSign dkSig = new WSSecDKSign();
                
                dkSig.setWsConfig(rmd.getConfig());
                
                dkSig.setSigCanonicalization(rpd.getAlgorithmSuite().getInclusiveC14n());
                dkSig.setSignatureAlgorithm(rpd.getAlgorithmSuite().getSymmetricSignature());
                dkSig.setDerivedKeyLength(rpd.getAlgorithmSuite().getMinimumSymmetricKeyLength()/8);
                
                dkSig.setExternalKey(encrKey.getEphemeralKey(), encrKey.getId());
                
                dkSig.prepare(doc, rmd.getSecHeader());
                
                Vector sigParts = new  Vector();
                
                sigParts.add(new WSEncryptionPart(rmd.getTimestampId()));                          
                
                if(rpd.isTokenProtection()) {
                    sigParts.add(new WSEncryptionPart(encrKey.getBSTTokenId()));
                }
                
                dkSig.setParts(sigParts);
                
                dkSig.addReferencesToSign(sigParts, rmd.getSecHeader());
                
                //Do signature
                dkSig.computeSignature();
                
                dkSig.appendDKElementToHeader(rmd.getSecHeader());

                dkSig.appendSigToHeader(rmd.getSecHeader());
                
                return dkSig.getSignatureValue();
                
            } catch (WSSecurityException e) {
                throw new RampartException("errorInDerivedKeyTokenSignature", e);
            } catch (ConversationException e) {
                throw new RampartException("errorInDerivedKeyTokenSignature", e);
            }
            
        } else {
            
            try {
                WSSecSignature sig = this.getSignatureBuider(rmd, token);
                

                sig.appendBSTElementToHeader(rmd.getSecHeader());
                
                Vector sigParts = new Vector();
                sigParts.add(new WSEncryptionPart(rmd.getTimestampId()));
                if (rpd.isTokenProtection()
                        && !Constants.INCLUDE_NEVER
                                .equals(token.getInclusion())) {
                    sigParts.add(new WSEncryptionPart(sig.getBSTTokenId()));
                }
                
                sig.addReferencesToSign(sigParts, rmd.getSecHeader());
                
                sig.appendToHeader(rmd.getSecHeader());
                
                sig.computeSignature();
                
                return sig.getSignatureValue();    
            } catch (WSSecurityException e) {
                throw new RampartException("errorInSignatureWithX509Token", e);
            }
            
            
        }
        
    }


    /**
     * IssuedToken signature
     * @param rmd
     * @param token
     * @throws RampartException
     */
    private byte[] doIssuedTokenSignature(RampartMessageData rmd, Token token) throws RampartException {
        
        RampartPolicyData rpd = rmd.getPolicyData();
        Document doc= rmd.getDocument();
        
        //Get the issued token
        String id = RampartUtil.getIssuedToken(rmd, (IssuedToken)token);
   
        String inclusion = token.getInclusion();
        org.apache.rahas.Token tok = null;
        try {
          tok = rmd.getTokenStorage().getToken(id);
        } catch (TrustException e) {
          throw new RampartException("errorExtractingToken",
                  new String[]{id} ,e);
        }
   
        boolean tokenIncluded = false;
        
        if(inclusion.equals(Constants.INCLUDE_ALWAYS) ||
        ((inclusion.equals(Constants.INCLUDE_ALWAYS_TO_RECIPIENT) 
                || inclusion.equals(Constants.INCLUDE_ONCE)) 
                && rmd.isClientSide())) {
          
            //Add the token
            rmd.getSecHeader().getSecurityHeader().appendChild(
                  doc.importNode((Element) tok.getToken(), true));
          
            tokenIncluded = true;
        }
   
        //check for dirived keys
        if(token.isDerivedKeys()) {
          //Create a derived key and add
          try {
   
              //Do Signature with derived keys
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
              
              dkSign.appendDKElementToHeader(rmd.getSecHeader());
              
              Vector sigParts = new  Vector();
              
              sigParts.add(new WSEncryptionPart(rmd.getTimestampId()));                          
              
              if(rpd.isTokenProtection() && tokenIncluded) {
                  sigParts.add(new WSEncryptionPart(id));
              }
              
              dkSign.setParts(sigParts);
              
              dkSign.addReferencesToSign(sigParts, rmd.getSecHeader());
              
              //Do signature
              dkSign.computeSignature();
              
              dkSign.appendSigToHeader(rmd.getSecHeader());
              
              return dkSign.getSignatureValue();
              
          } catch (ConversationException e) {
              throw new RampartException(
                      "errorInDerivedKeyTokenSignature", e);
          } catch (WSSecurityException e) {
              throw new RampartException(
                      "errorInDerivedKeyTokenSignature", e);
          }
          
        } else {
          //TODO: Do signature withtout derived keys with the Issuedtoken ??
            return null;
        }
    }
}
