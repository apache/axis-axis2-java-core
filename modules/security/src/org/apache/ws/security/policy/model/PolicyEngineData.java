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

    private static Log log = LogFactory.getLog(PolicyEngineData.class);
    
    public void initializeWithDefaults() {
        
    }
    
    public static  PolicyEngineData copy(QName name) throws WSSPolicyException {
        if(name.getLocalPart().equals(SecurityPolicy.symmetricBinding.getTokenName())) {
            return new SymmetricBinding();
        } else if (name.getLocalPart().equals(SecurityPolicy.asymmetricBinding.getTokenName())) {
            return new AsymmetricBinding();
        } else if (name.getLocalPart().equals(SecurityPolicy.transportBinding.getTokenName())) {
            return new TransportBinding();
        } else if (name.getLocalPart().equals(SecurityPolicy.algorithmSuite.getTokenName())) {
            return new AlgorithmSuite();
        } else if (name.getLocalPart().equals(SecurityPolicy.signedElements.getTokenName())) {
            return new SignedEncryptedElements(true);
        } else if (name.getLocalPart().equals(SecurityPolicy.encryptedElements.getTokenName())) {
            return new SignedEncryptedElements(false);
        } else if (name.getLocalPart().equals(SecurityPolicy.signedParts.getTokenName())) {
            return new SignedEncryptedParts(true);
        } else if (name.getLocalPart().equals(SecurityPolicy.encryptedParts.getTokenName())) {
            return new SignedEncryptedParts(false);
        } else if (name.getLocalPart().equals(SecurityPolicy.header.getTokenName())) {
            return new Header();
        } else if (name.getLocalPart().equals(SecurityPolicy.protectionToken.getTokenName())) {
            return new ProtectionToken();
        } else if (name.getLocalPart().equals(SecurityPolicy.signatureToken.getTokenName())) {
            return new SignatureToken();
        } else if (name.getLocalPart().equals(SecurityPolicy.encryptionToken.getTokenName())) {
            return new EncryptionToken();
        } else if (name.getLocalPart().equals(SecurityPolicy.x509Token.getTokenName())) {
            return new X509Token();
        } else if (name.getLocalPart().equals(SecurityPolicy.layout.getTokenName())) {
            return new Layout();
        } else if (name.getLocalPart().equals(SecurityPolicy.signedSupportingTokens.getTokenName())) {
            return new SupportingToken(Constants.SUPPORTING_TOKEN_SIGNED);
        } else if (name.getLocalPart().equals(SecurityPolicy.signedEndorsingSupportingTokens.getTokenName())) {
            return new SupportingToken(Constants.SUPPORTING_TOKEN_SIGNED_ENDORSING);
        } else if (name.getLocalPart().equals(SecurityPolicy.supportingTokens.getTokenName())) {
            return new SupportingToken(Constants.SUPPORTING_TOKEN_SUPPORTING);
        } else if (name.getLocalPart().equals(SecurityPolicy.endorsingSupportingTokens.getTokenName())) {
            return new SupportingToken(Constants.SUPPORTING_TOKEN_ENDORSING);
        } else if (name.getLocalPart().equals(SecurityPolicy.usernameToken.getTokenName())) {
            return new UsernameToken();
        } else if (name.getLocalPart().equals(SecurityPolicy.wss10.getTokenName())) {
            return new Wss10();
        } else if (name.getLocalPart().equals(SecurityPolicy.wss11.getTokenName())) {
            return new Wss11();
        } else if (name.getLocalPart().equals(SecurityPolicy.initiatorToken.getTokenName())) {
            return new InitiatorToken();
        } else if (name.getLocalPart().equals(SecurityPolicy.recipientToken.getTokenName())) {
            return new RecipientToken();
        } else {
            log.error("Unsuppotred: " + name.getLocalPart());
            throw new WSSPolicyException("Unsuppotred complex assertion :" + name.getLocalPart());
        }
    }
}
