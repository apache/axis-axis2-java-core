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

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.rampart.policy.model.RampartConfig;
import org.apache.ws.secpolicy.Constants;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.secpolicy.model.AlgorithmSuite;
import org.apache.ws.secpolicy.model.Header;
import org.apache.ws.secpolicy.model.SignedEncryptedElements;
import org.apache.ws.secpolicy.model.SignedEncryptedParts;
import org.apache.ws.secpolicy.model.SupportingToken;
import org.apache.ws.secpolicy.model.Token;
import org.apache.ws.secpolicy.model.X509Token;

public class RampartPolicyData {

    /*
     * Global settings for overall security processing
     */
    private boolean symmetricBinding;

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
    private RampartPolicyToken encryptionToken;

    private RampartPolicyToken signatureToken;

    /*
     * Message tokens for asymmetrical binding
     */
    private RampartPolicyToken recipientToken; // used to encrypt data to

    // receipient

    private RampartPolicyToken initiatorToken; // used to sign data by

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
    private RampartSupportingToken supportingToken;

    private RampartSupportingToken signedSupportingToken;

    private RampartSupportingToken endorsingSupportingToken;

    private RampartSupportingToken signedEndorsingSupportingToken;
    
    private RampartConfig rampartConfig;
    
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
     * Return the encryption token data.
     * 
     * The returned token data may be empty.
     * 
     * @return Returns the encryptionToken.
     */
    public RampartPolicyToken getEncryptionToken() {
        if (encryptionToken == null) {
            encryptionToken = new RampartPolicyToken();
        }
        return encryptionToken;
    }

    /**
     * Sets the parameters for the encryption token according to parsed policy.
     * 
     * The encryption token is specific to the symmetric binding.
     * 
     * @param encryptionToken
     *            The encryptionToken to set.
     */
    public void setEncryptionToken(Token encToken, AlgorithmSuite suite)
            throws WSSPolicyException {
        if (encToken instanceof X509Token) {
            if (encryptionToken == null) {
                encryptionToken = new RampartPolicyToken();
            }
            initializeWSS4JPolicyToken(encryptionToken, (X509Token) encToken,
                    suite);
        }
    }

    /**
     * Sets the parameters for the protection token according to parsed policy.
     * 
     * The protection token is specific to the symmetric binding.
     * 
     * @param protectionToken
     *            The protectionToken to set.
     */
    public void setProtectionToken(Token protectionToken, AlgorithmSuite suite)
            throws WSSPolicyException {
        setEncryptionToken(protectionToken, suite);
        setSignatureToken(protectionToken, suite);
    }

    /**
     * Return the signature token data.
     * 
     * The returned token data may be empty.
     * 
     * @return Returns the signatureToken.
     */
    public RampartPolicyToken getSignatureToken() {
        if (signatureToken == null) {
            signatureToken = new RampartPolicyToken();
        }
        return signatureToken;
    }

    /**
     * Sets the parameters for the signature token according to parsed policy.
     * 
     * The signature token is specific to the symmetric binding.
     * 
     * @param signatureToken
     *            The signatureToken to set.
     */
    public void setSignatureToken(Token sigToken, AlgorithmSuite suite)
            throws WSSPolicyException {
        if (sigToken instanceof X509Token) {
            if (signatureToken == null) {
                signatureToken = new RampartPolicyToken();
            }
            initializeWSS4JPolicyToken(signatureToken, (X509Token) sigToken,
                    suite);
        }
    }

    /**
     * Return the initiator token data.
     * 
     * The returned token data may be empty.
     * 
     * @return Returns the initiatorToken.
     */
    public RampartPolicyToken getInitiatorToken() {
        if (initiatorToken == null) {
            initiatorToken = new RampartPolicyToken();
        }
        return initiatorToken;
    }

    /**
     * Sets the parameters for the initiator token according to parsed policy.
     * 
     * The initiator token is specific to the symmetric binding. The message
     * initiator uses this token to sign its data. Thus this method initializes
     * the signature relevant parts of the WSS4JPolicyToken data.
     * 
     * @param initiatorToken
     *            The initiatorToken to set.
     */
    public void setInitiatorToken(Token iniToken, AlgorithmSuite suite)
            throws WSSPolicyException {
        if (iniToken instanceof X509Token) {
            if (initiatorToken == null) {
                initiatorToken = new RampartPolicyToken();
            }
            initializeWSS4JPolicyToken(initiatorToken, (X509Token) iniToken,
                    suite);
        }
    }

    /**
     * Return the recipient token data.
     * 
     * The returned token data may be empty.
     * 
     * @return Returns the recipientToken.
     */
    public RampartPolicyToken getRecipientToken() {
        if (recipientToken == null) {
            recipientToken = new RampartPolicyToken();
        }
        return recipientToken;
    }

    /**
     * Sets the parameters for the initiator token according to parsed policy.
     * 
     * The initiator token is specific to the symmetric binding. The message
     * initiator uses this token to encrypt data sent to the reipient. Thus this
     * method initializes the encryption relevant parts of the WSS4JPolicyToken
     * data.
     * 
     * @param recipientToken
     *            The recipientToken to set.
     */
    public void setRecipientToken(Token recToken, AlgorithmSuite suite)
            throws WSSPolicyException {
        if (recToken instanceof X509Token) {
            if (recipientToken == null) {
                recipientToken = new RampartPolicyToken();
            }
            initializeWSS4JPolicyToken(recipientToken, (X509Token) recToken,
                    suite);
        }
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

        Iterator it = null;
        RampartSupportingToken wst = new RampartSupportingToken();

        /*
         * Get and store the parts to sign of the supporting token
         */
        SignedEncryptedParts sep = suppToken.getSignedParts();
        if (sep != null) {
            it = sep.getHeaders().iterator();
            if (wst.sigParts == null) {
                wst.sigParts = new ArrayList();
            }
            while (it.hasNext()) {
                Header header = (Header) it.next();
                wst.sigParts.add(new WSEncryptionPart(header.getName(), header
                        .getNamespace(), "Content"));
            }
        }
        /*
         * Get and store the parts to encrypt of the supporting token
         */
        sep = suppToken.getEncryptedParts();
        if (sep != null) {
            it = sep.getHeaders().iterator();
            if (wst.encParts == null) {
                wst.encParts = new ArrayList();
            }
            while (it.hasNext()) {
                Header header = (Header) it.next();
                wst.encParts.add(new WSEncryptionPart(header.getName(), header
                        .getNamespace(), "Content"));
            }
        }

        /*
         * Get and store the elements (XPath) to sign of the supporting token
         */
        SignedEncryptedElements see = suppToken.getSignedElements();
        if (see != null) {
            it = see.getXPathExpressions().iterator();
            if (wst.sigElements == null) {
                wst.sigElements = new ArrayList();
            }
            while (it.hasNext()) {
                wst.sigElements.add((String) it.next());
            }
        }
        /*
         * Get and store the elements (XPath) to encrypt of the supporting token
         */
        see = suppToken.getEncryptedElements();
        if (see != null) {
            it = see.getXPathExpressions().iterator();
            if (wst.encElements == null) {
                wst.encElements = new ArrayList();
            }
            while (it.hasNext()) {
                wst.encElements.add((String) it.next());
            }
        }
        AlgorithmSuite suite = suppToken.getAlgorithmSuite();

        /*
         * Iterator over all tokens, initialize their data structure, and store
         * them in the support token data structure.
         */
        it = suppToken.getTokens().iterator();
        while (it.hasNext()) {
            if (wst.supportTokens == null) {
                wst.supportTokens = new ArrayList();
            }

            Token tok = (Token) it.next();
            if (tok instanceof X509Token) {
                RampartPolicyToken wpt = new RampartPolicyToken();
                wst.supportTokens.add(wpt);
                initializeWSS4JPolicyToken(wpt, (X509Token) tok, suite);
            }
        }
        /*
         * The supporting token is parsed and initialized, set it according to
         * its type.
         */
        wst.tokenType = suppToken.getType();
        if (wst.tokenType == Constants.SUPPORTING_TOKEN_SUPPORTING) {
            supportingToken = wst;
        } else if (wst.tokenType == Constants.SUPPORTING_TOKEN_SIGNED) {
            signedSupportingToken = wst;
        } else if (wst.tokenType == Constants.SUPPORTING_TOKEN_ENDORSING) {
            endorsingSupportingToken = wst;
        } else if (wst.tokenType == Constants.SUPPORTING_TOKEN_SIGNED_ENDORSING) {
            signedEndorsingSupportingToken = wst;
        }
    }
    
    

    /**
     * @return Returns the endorsingSupportingToken.
     */
    public RampartSupportingToken getEndorsingSupportingToken() {
        return endorsingSupportingToken;
    }

    /**
     * @return Returns the rampartConfig.
     */
    public RampartConfig getRampartConfig() {
        return rampartConfig;
    }

    /**
     * @return Returns the signedEndorsingSupportingToken.
     */
    public RampartSupportingToken getSignedEndorsingSupportingToken() {
        return signedEndorsingSupportingToken;
    }

    /**
     * @return Returns the signedSupportingToken.
     */
    public RampartSupportingToken getSignedSupportingToken() {
        return signedSupportingToken;
    }

    /**
     * @return Returns the supportingToken.
     */
    public RampartSupportingToken getSupportingToken() {
        return supportingToken;
    }

    private static void initializeWSS4JPolicyToken(RampartPolicyToken tok,
            X509Token x509Tok, AlgorithmSuite suite) throws WSSPolicyException {
        tok.tokenType = RampartPolicyToken.X509_TOKEN;
        tok.encAlgorithm = suite.getEncryption();
        tok.sigAlgorithm = suite.getAsymmetricSignature();
        tok.encTransportAlgorithm = suite.getAsymmetricKeyWrap();
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

}
