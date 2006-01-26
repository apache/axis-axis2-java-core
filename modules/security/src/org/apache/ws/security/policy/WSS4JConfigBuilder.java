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
package org.apache.ws.security.policy;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.policy.model.AsymmetricBinding;
import org.apache.ws.security.policy.model.Binding;
import org.apache.ws.security.policy.model.Header;
import org.apache.ws.security.policy.model.PolicyEngineData;
import org.apache.ws.security.policy.model.SignedEncryptedParts;
import org.apache.ws.security.policy.model.SymmetricAsymmetricBindingBase;
import org.apache.ws.security.policy.model.Token;
import org.apache.ws.security.policy.model.TransportBinding;
import org.apache.ws.security.policy.model.Wss10;
import org.apache.ws.security.policy.model.Wss11;
import org.apache.ws.security.policy.model.X509Token;

public class WSS4JConfigBuilder {
    
    public static WSS4JConfig build(ArrayList topLevelPeds) throws WSSPolicyException {
        Iterator topLevelPEDIterator = topLevelPeds.iterator();
        WSS4JConfig config = new WSS4JConfig();
        while (topLevelPEDIterator.hasNext()) {
            PolicyEngineData ped = (PolicyEngineData) topLevelPEDIterator.next();
            if(ped instanceof Binding) {
                config.binding = (Binding)ped;
            } else if(ped instanceof Wss10) {
                processWSS10((Wss10)ped, config);
            } else if(ped instanceof Wss11) {
                processWSS11((Wss11)ped, config);
            } else if (ped instanceof SignedEncryptedParts) {
                processSignedEncryptedParts((SignedEncryptedParts)ped, config);
            } else {
              //Unrecognized token  
            }
        }
        finalizeConfig(config);
        return config;
    }

    private static void finalizeConfig(WSS4JConfig config) throws WSSPolicyException{
        
        if(config.binding instanceof TransportBinding) {
            //TODO TransportBinding
            throw new UnsupportedOperationException("TODO TransportBinding");
        } else {
            //Handle common properties from SymmetricAsymmetricBindingBase
            SymmetricAsymmetricBindingBase base = (SymmetricAsymmetricBindingBase) config.binding;
            if(base.isEntireHeaderAndBodySignatures()) {
                config.getOutflowConfiguration().setSignAllHeadersAndBody();
            }
            if (base.isSignatureProtection()) {
                if (base.getProtectionOrder().equals(
                        Constants.SIGN_BEFORE_ENCRYPTING)) {
                    //Makesure encryption is on
                    config.encryption = true;
                    
                    //Add a sign part pointing to the signature
                    String encrParts = config.getOutflowConfiguration()
                            .getEncryptionParts();
                    boolean otherSignPartsExists = encrParts != null
                            && encrParts.length() > 0;
                    String part = getEncryptedPartSnippet(false, WSConstants.SIG_NS,
                            WSConstants.SIG_LN, !otherSignPartsExists);
                    if(otherSignPartsExists) {
                        part = encrParts + part;
                    }
                    config.getOutflowConfiguration().setEncryptionParts(part);
                } else {
                    throw new WSSPolicyException("To enable SignatureProtection" +
                            " the ProtectionOrder must be SignBeforeEncrypting");
                }
            }
            if(base.isTokenProtection()) {
                throw new WSSPolicyException(
                        "TokenProtection is not supported right now " +
                        "since there's no way to specify how to sign " +
                        "the token that is used to sign ???");
            }
            
            //Start building action items
            String actionItems = "";
            if(config.signature && config.encryption) {
                if(base.getProtectionOrder().equals(Constants.SIGN_BEFORE_ENCRYPTING)) {
                    actionItems = "Signature Encrypt";
                } else {
                    actionItems = "Encrypt Signature";
                }
            } else if(config.signature) {
                actionItems = " Signature";
            } else if(config.encryption) {
                actionItems  = " Encrypt";
            }
            
            if(base.isIncludeTimestamp()) {
                //TODO: Caution: including Timestamp as the starting action item  
                actionItems = " Timestamp " + actionItems;
                
            }
            if(actionItems.length() == 0) {
                actionItems = "NoSecurity";
            }
            config.getInflowConfiguration().setActionItems(actionItems.trim());
            config.getOutflowConfiguration().setActionItems(actionItems.trim());
        }


        if(config.binding instanceof AsymmetricBinding) {
            AsymmetricBinding asymmetricBinding = (AsymmetricBinding) config.binding;
            Token initiatorToken = asymmetricBinding.getInitiatorToken()
                    .getInitiatorToken();
            String initiatorInclusion = initiatorToken.getInclusion();
            if (initiatorInclusion
                    .equals(Constants.INCLUDE_ALWAYS_TO_RECIPIENT)
                    || initiatorInclusion.equals(Constants.INCLUDE_ALWAYS)) {
                config.getOutflowConfiguration().setSignatureKeyIdentifier(
                        WSSHandlerConstants.BST_DIRECT_REFERENCE);
            } else {
                if(initiatorToken instanceof X509Token) {
                    config.getOutflowConfiguration().setSignatureKeyIdentifier(
                            WSSHandlerConstants.X509_KEY_IDENTIFIER);
                }
            }
        } else {
            //TODO Handle symmetric binding
        }
    }


    private static void processWSS10(Wss10 wss10, WSS4JConfig config) {
        //There's nothing to populate in WSS4J Config right now
    }

    private static void processWSS11(Wss11 wss11, WSS4JConfig config) {
       if(wss11.isRequireSignatureConfirmation()) {
           config.getInflowConfiguration().setEnableSignatureConfirmation(true);
           config.getOutflowConfiguration().setEnableSignatureConfirmation(true);
       }
    }

    private static void processSignedEncryptedParts(SignedEncryptedParts parts,
            WSS4JConfig config) {
        if(parts.isSignedParts()) {
            config.signature = true;
            if(parts.isBody()) {
                config.getOutflowConfiguration().setSignBody();
            }
            Iterator headersIter = parts.getHeaders().iterator();
            String signedParts = "";
            while (headersIter.hasNext()) {
                Header header = (Header) headersIter.next();
                signedParts += getSignedPartSnippet(header.getNamespace(),
                        header.getNamespace(), signedParts.length() == 0);
            }
            if(signedParts.length() != 0) {
                config.getOutflowConfiguration().setSignatureParts(signedParts);
            }
        } else {
            config.encryption = true;
            if(parts.isBody()) {
                config.getOutflowConfiguration().setEncryptBody();
            }
            Iterator headersIter = parts.getHeaders().iterator();
            String encryptedParts = "";
            while (headersIter.hasNext()) {
                Header header = (Header) headersIter.next();
                encryptedParts += getEncryptedPartSnippet(false, header
                        .getNamespace(), header.getName(), encryptedParts
                        .length() == 0);
            }
            if(encryptedParts.length() != 0) {
                config.getOutflowConfiguration().setEncryptionParts(encryptedParts);
            }
        }
    }

    private static String getSignedPartSnippet(String namespace, String name,
            boolean first) {
        return first ? "{Element}{" + namespace + "}" + name : ";{Element}{"
                + namespace + "}" + name;
    }

    private static String getEncryptedPartSnippet(boolean content,
            String namespace, String name, boolean first) {
        String ret = "";
        if(!first) {
            ret=";";
        }
        return content ? ret + "{}{" + namespace + "}" + name : ret
                + "{Element}{" + namespace + "}" + name;
    }
}
