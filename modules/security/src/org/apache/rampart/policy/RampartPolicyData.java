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

package org.apache.rampart.policy;

import org.apache.rampart.policy.model.RampartConfig;
import org.apache.ws.secpolicy.Constants;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.secpolicy.model.AlgorithmSuite;
import org.apache.ws.secpolicy.model.SupportingToken;
import org.apache.ws.secpolicy.model.Token;
import org.apache.ws.secpolicy.model.Trust10;
import org.apache.ws.secpolicy.model.X509Token;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;

import java.util.ArrayList;

public class RampartPolicyData {

    /*
     * Global settings for overall security processing
     */
    private boolean symmetricBinding;
    
    private boolean transportBinding;

    private String layout;

    private boolean includeTimestamp;

    private boolean entireHeaderAndBodySignatures;

    private String protectionOrder;

    private boolean signatureProtection;

    private boolean tokenProtection;

    private boolean signatureConfirmation;

    /*
     * Message tokens for symmetrical binding
     */
    private Token encryptionToken;

    private Token signatureToken;

    /*
     * Message tokens for asymmetrical binding
     */
    private Token recipientToken; // used to encrypt data to

    // receipient

    private Token initiatorToken; // used to sign data by

    // initiator

    /*
     * Which parts or elements of the message to sign/encrypt with the messagen
     * tokens. Parts or elements to sign/encrypt with supporting tokens are
     * stored together with the tokens (see WSS4JPolicyToken).
     */
    private boolean signBody;

    private boolean encryptBody;

    private ArrayList signedParts;

    private ArrayList signedElements;

    private ArrayList encryptedParts;

    private ArrayList encryptedElements;

    /*
     * Holds the supporting tokens elements
     */
    private SupportingToken supportingToken;

    private SupportingToken signedSupportingToken;

    private SupportingToken endorsingSupportingToken;

    private SupportingToken signedEndorsingSupportingToken;
    
    private AlgorithmSuite algorithmSuite;
    
    private RampartConfig rampartConfig;
    
    private Trust10 trust10;
    
    /**
     * @return Returns the symmetricBinding.
     */
    public boolean isSymmetricBinding() {
        return symmetricBinding;
    }

    /**
     * @param symmetricBinding
     *            The symmetricBinding to set.
     */
    public void setSymmetricBinding(boolean symmetricBinding) {
        this.symmetricBinding = symmetricBinding;
    }

    /**
     * @return Returns the entireHeaderAndBodySignatures.
     */
    public boolean isEntireHeaderAndBodySignatures() {
        return entireHeaderAndBodySignatures;
    }

    /**
     * @param entireHeaderAndBodySignatures
     *            The entireHeaderAndBodySignatures to set.
     */
    public void setEntireHeaderAndBodySignatures(
            boolean entireHeaderAndBodySignatures) {
        this.entireHeaderAndBodySignatures = entireHeaderAndBodySignatures;
    }

    /**
     * @return Returns the includeTimestamp.
     */
    public boolean isIncludeTimestamp() {
        return includeTimestamp;
    }

    /**
     * @param includeTimestamp
     *            The includeTimestamp to set.
     */
    public void setIncludeTimestamp(boolean includeTimestamp) {
        this.includeTimestamp = includeTimestamp;
    }

    /**
     * @return Returns the layout.
     */
    public String getLayout() {
        return layout;
    }

    /**
     * @param layout
     *            The layout to set.
     */
    public void setLayout(String layout) {
        this.layout = layout;
    }

    /**
     * @return Returns the protectionOrder.
     */
    public String getProtectionOrder() {
        return protectionOrder;
    }

    /**
     * @param protectionOrder
     *            The protectionOrder to set.
     */
    public void setProtectionOrder(String protectionOrder) {
        this.protectionOrder = protectionOrder;
    }

    /**
     * @return Returns the signatureProtection.
     */
    public boolean isSignatureProtection() {
        return signatureProtection;
    }

    /**
     * @param signatureProtection
     *            The signatureProtection to set.
     */
    public void setSignatureProtection(boolean signatureProtection) {
        this.signatureProtection = signatureProtection;
    }

    /**
     * @return Returns the tokenProtection.
     */
    public boolean isTokenProtection() {
        return tokenProtection;
    }

    /**
     * @param tokenProtection
     *            The tokenProtection to set.
     */
    public void setTokenProtection(boolean tokenProtection) {
        this.tokenProtection = tokenProtection;
    }

    /**
     * @return Returns the signatureConfirmation.
     */
    public boolean isSignatureConfirmation() {
        return signatureConfirmation;
    }

    /**
     * @param signatureConfirmation
     *            The signatureConfirmation to set.
     */
    public void setSignatureConfirmation(boolean signatureConfirmation) {
        this.signatureConfirmation = signatureConfirmation;
    }

    /**
     * @return Returns the encryptedElements.
     */
    public ArrayList getEncryptedElements() {
        return encryptedElements;
    }

    /**
     * @param encElement
     *            The encrypted Element (XPath) to set.
     */
    public void setEncryptedElements(String encElement) {
        if (encryptedElements == null) {
            encryptedElements = new ArrayList();
        }
        encryptedElements.add(encElement);
    }

    /**
     * @return Returns the encryptedParts.
     */
    public ArrayList getEncryptedParts() {
        return encryptedParts;
    }

    /**
     * @param namespace
     *            The namespace of the part.
     * @param element
     *            The part's element name.
     */
    public void setEncryptedParts(String namespace, String element) {
        if (encryptedParts == null) {
            encryptedParts = new ArrayList();
        }
        WSEncryptionPart wep = new WSEncryptionPart(element, namespace,
                "Content");
        encryptedParts.add(wep);
    }

    /**
     * @return Returns the encryptBody.
     */
    public boolean isEncryptBody() {
        return encryptBody;
    }

    /**
     * @param encryptBody
     *            The encryptBody to set.
     */
    public void setEncryptBody(boolean encryptBody) {
        this.encryptBody = encryptBody;
    }

    /**
     * @return Returns the signBody.
     */
    public boolean isSignBody() {
        return signBody;
    }

    /**
     * @param signBody
     *            The signBody to set.
     */
    public void setSignBody(boolean signBody) {
        this.signBody = signBody;
    }

    /**
     * @return Returns the signedElements.
     */
    public ArrayList getSignedElements() {
        return signedElements;
    }

    /**
     * @param sigElement
     *            The signed Element (XPath) to set.
     */
    public void setSignedElements(String sigElement) {
        if (signedElements == null) {
            signedElements = new ArrayList();
        }
        signedElements.add(sigElement);
    }

    /**
     * @return Returns the signedParts.
     */
    public ArrayList getSignedParts() {
        return signedParts;
    }

    /**
     * @param namespace
     *            The namespace of the part.
     * @param element
     *            The part's element name.
     */
    public void setSignedParts(String namespace, String element) {
        if (signedParts == null) {
            signedParts = new ArrayList();
        }
        WSEncryptionPart wep = new WSEncryptionPart(element, namespace,
                "Content");
        signedParts.add(wep);
    }

    public void setSupportingTokens(SupportingToken suppToken)
            throws WSSPolicyException {

        int tokenType = suppToken.getType();
        if (tokenType == Constants.SUPPORTING_TOKEN_SUPPORTING) {
            supportingToken = suppToken;
        } else if (tokenType == Constants.SUPPORTING_TOKEN_SIGNED) {
            signedSupportingToken = suppToken;
        } else if (tokenType == Constants.SUPPORTING_TOKEN_ENDORSING) {
            endorsingSupportingToken = suppToken;
        } else if (tokenType == Constants.SUPPORTING_TOKEN_SIGNED_ENDORSING) {
            signedEndorsingSupportingToken = suppToken;
        }
    }
    
    

    /**
     * @return Returns the rampartConfig.
     */
    public RampartConfig getRampartConfig() {
        return rampartConfig;
    }


    private static void initializeRampartPolicyToken(RampartPolicyToken tok,
            X509Token x509Tok) throws WSSPolicyException {
        tok.tokenType = RampartPolicyToken.X509_TOKEN;

        if (x509Tok.isRequireIssuerSerialReference()) {
            tok.keyIdentifier = WSConstants.ISSUER_SERIAL;
        } else if (x509Tok.isRequireThumbprintReference()) {
            tok.keyIdentifier = WSConstants.THUMBPRINT_IDENTIFIER;
        } else if (x509Tok.isRequireEmbeddedTokenReference()) {
            tok.keyIdentifier = WSConstants.BST_DIRECT_REFERENCE;
        } else {
            throw new WSSPolicyException(
                    "Unknown key reference specifier for X509Token");

        }
    }

    /**
     * @return Returns the encryptionToken.
     */
    public Token getEncryptionToken() {
        return encryptionToken;
    }

    /**
     * @param encryptionToken The encryptionToken to set.
     */
    public void setEncryptionToken(Token encryptionToken) {
        this.encryptionToken = encryptionToken;
    }

    /**
     * @return Returns the initiatorToken.
     */
    public Token getInitiatorToken() {
        return initiatorToken;
    }

    /**
     * @param initiatorToken The initiatorToken to set.
     */
    public void setInitiatorToken(Token initiatorToken) {
        this.initiatorToken = initiatorToken;
    }

    /**
     * @return Returns the recipientToken.
     */
    public Token getRecipientToken() {
        return recipientToken;
    }

    /**
     * @param recipientToken The recipientToken to set.
     */
    public void setRecipientToken(Token recipientToken) {
        this.recipientToken = recipientToken;
    }
    
    public void setProtectionToken(Token protectionToken) {
        this.setEncryptionToken(protectionToken);
        this.setSignatureToken(protectionToken);
    }

    /**
     * @return Returns the signatureToken.
     */
    public Token getSignatureToken() {
        return signatureToken;
    }

    /**
     * @param signatureToken The signatureToken to set.
     */
    public void setSignatureToken(Token signatureToken) {
        this.signatureToken = signatureToken;
    }

    /**
     * @return Returns the signedEndorsingSupportingToken.
     */
    public SupportingToken getSignedEndorsingSupportingToken() {
        return signedEndorsingSupportingToken;
    }

    /**
     * @param signedEndorsingSupportingToken The signedEndorsingSupportingToken to set.
     */
    public void setSignedEndorsingSupportingToken(
            SupportingToken signedEndorsingSupportingToken) {
        this.signedEndorsingSupportingToken = signedEndorsingSupportingToken;
    }

    /**
     * @return Returns the signedSupportingToken.
     */
    public SupportingToken getSignedSupportingToken() {
        return signedSupportingToken;
    }

    /**
     * @param signedSupportingToken The signedSupportingToken to set.
     */
    public void setSignedSupportingToken(SupportingToken signedSupportingToken) {
        this.signedSupportingToken = signedSupportingToken;
    }

    /**
     * @return Returns the supportingToken.
     */
    public SupportingToken getSupportingToken() {
        return supportingToken;
    }

    /**
     * @param supportingToken The supportingToken to set.
     */
    public void setSupportingToken(SupportingToken supportingToken) {
        this.supportingToken = supportingToken;
    }

    /**
     * @param endorsingSupportingToken The endorsingSupportingToken to set.
     */
    public void setEndorsingSupportingToken(SupportingToken endorsingSupportingToken) {
        this.endorsingSupportingToken = endorsingSupportingToken;
    }

    /**
     * @return Returns the endorsingSupportingToken.
     */
    public SupportingToken getEndorsingSupportingToken() {
        return endorsingSupportingToken;
    }

    /**
     * @return Returns the algorithmSuite.
     */
    public AlgorithmSuite getAlgorithmSuite() {
        return algorithmSuite;
    }

    /**
     * @param algorithmSuite The algorithmSuite to set.
     */
    public void setAlgorithmSuite(AlgorithmSuite algorithmSuite) {
        this.algorithmSuite = algorithmSuite;
    }

    /**
     * @return Returns the trust10.
     */
    public Trust10 getTrust10() {
        return trust10;
    }

    /**
     * @param trust10 The trust10 to set.
     */
    public void setTrust10(Trust10 trust10) {
        this.trust10 = trust10;
    }

    /**
     * @param rampartConfig The rampartConfig to set.
     */
    public void setRampartConfig(RampartConfig rampartConfig) {
        this.rampartConfig = rampartConfig;
    }

    /**
     * @return Returns the transportBinding.
     */
    public boolean isTransportBinding() {
        return transportBinding;
    }

    /**
     * @param transportBinding The transportBinding to set.
     */
    public void setTransportBinding(boolean transportBinding) {
        this.transportBinding = transportBinding;
    }

    
}
