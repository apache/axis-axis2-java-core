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

package org.apache.ws.security.policy.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.policy.Constants;
import org.apache.ws.security.policy.WSSPolicyException;
import org.apache.ws.security.policy.parser.SecurityPolicy;

import javax.xml.namespace.QName;

public class PolicyEngineData {

	private static final Log log = LogFactory.getLog(PolicyEngineData.class);
    
    public void initializeWithDefaults() {
        
    }
    
    public static  PolicyEngineData copy(QName name) throws WSSPolicyException {
        String localPart = name.getLocalPart();
        if(localPart.equals(SecurityPolicy.symmetricBinding.getTokenName())) {
            return new SymmetricBinding();
        } else if (localPart.equals(SecurityPolicy.asymmetricBinding.getTokenName())) {
            return new AsymmetricBinding();
        } else if (localPart.equals(SecurityPolicy.transportBinding.getTokenName())) {
            return new TransportBinding();
        } else if (localPart.equals(SecurityPolicy.algorithmSuite.getTokenName())) {
            return new AlgorithmSuite();
        } else if (localPart.equals(SecurityPolicy.signedElements.getTokenName())) {
            return new SignedEncryptedElements(true);
        } else if (localPart.equals(SecurityPolicy.encryptedElements.getTokenName())) {
            return new SignedEncryptedElements(false);
        } else if (localPart.equals(SecurityPolicy.signedParts.getTokenName())) {
            return new SignedEncryptedParts(true);
        } else if (localPart.equals(SecurityPolicy.encryptedParts.getTokenName())) {
            return new SignedEncryptedParts(false);
        } else if (localPart.equals(SecurityPolicy.header.getTokenName())) {
            return new Header();
        } else if (localPart.equals(SecurityPolicy.protectionToken.getTokenName())) {
            return new ProtectionToken();
        } else if (localPart.equals(SecurityPolicy.signatureToken.getTokenName())) {
            return new SignatureToken();
        } else if (localPart.equals(SecurityPolicy.encryptionToken.getTokenName())) {
            return new EncryptionToken();
        } else if (localPart.equals(SecurityPolicy.x509Token.getTokenName())) {
            return new X509Token();
        } else if (localPart.equals(SecurityPolicy.layout.getTokenName())) {
            return new Layout();
        } else if (localPart.equals(SecurityPolicy.signedSupportingTokens.getTokenName())) {
            return new SupportingToken(Constants.SUPPORTING_TOKEN_SIGNED);
        } else if (localPart.equals(SecurityPolicy.signedEndorsingSupportingTokens.getTokenName())) {
            return new SupportingToken(Constants.SUPPORTING_TOKEN_SIGNED_ENDORSING);
        } else if (localPart.equals(SecurityPolicy.supportingTokens.getTokenName())) {
            return new SupportingToken(Constants.SUPPORTING_TOKEN_SUPPORTING);
        } else if (localPart.equals(SecurityPolicy.endorsingSupportingTokens.getTokenName())) {
            return new SupportingToken(Constants.SUPPORTING_TOKEN_ENDORSING);
        } else if (localPart.equals(SecurityPolicy.usernameToken.getTokenName())) {
            return new UsernameToken();
        } else if (localPart.equals(SecurityPolicy.wss10.getTokenName())) {
            return new Wss10();
        } else if (localPart.equals(SecurityPolicy.wss11.getTokenName())) {
            return new Wss11();
        } else if (localPart.equals(SecurityPolicy.initiatorToken.getTokenName())) {
            return new InitiatorToken();
        } else if (localPart.equals(SecurityPolicy.recipientToken.getTokenName())) {
            return new RecipientToken();
        } else if (localPart.equals(SecurityPolicy.transportToken.getTokenName())) {
            return new TransportToken();
        } else if (localPart.equals(SecurityPolicy.httpsToken.getTokenName())) {
            return new HttpsToken();
        } else {
            log.error("Unsuppotred: " + localPart);
            throw new WSSPolicyException("Unsuppotred complex assertion :" + localPart);
        }
    }
}
