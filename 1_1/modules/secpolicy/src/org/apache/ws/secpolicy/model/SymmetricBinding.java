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

package org.apache.ws.secpolicy.model;

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.All;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.ws.secpolicy.Constants;

public class SymmetricBinding extends SymmetricAsymmetricBindingBase {

    private EncryptionToken encryptionToken;
    
    private SignatureToken signatureToken;
    
    private ProtectionToken protectionToken;
    
    /**
     * @return Returns the encryptionToken.
     */
    public EncryptionToken getEncryptionToken() {
        return encryptionToken;
    }

    /**
     * @param encryptionToken The encryptionToken to set.
     */
    public void setEncryptionToken(EncryptionToken encryptionToken)  {
        if(this.protectionToken != null) {
//            throw new WSSPolicyException("Cannot use an EncryptionToken in a " +
//                    "SymmetricBinding when there is a ProtectionToken");
        }
        this.encryptionToken = encryptionToken;
    }

    /**
     * @return Returns the protectionToken.
     */
    public ProtectionToken getProtectionToken() {
        return protectionToken;
    }

    /**
     * @param protectionToken The protectionToken to set.
     */
    public void setProtectionToken(ProtectionToken protectionToken)  {
        if(this.encryptionToken != null || this.signatureToken != null) {
//            throw new WSSPolicyException("Cannot use a ProtectionToken in a " +
//            "SymmetricBinding when there is a SignatureToken or an" +
//            "EncryptionToken");
        }
        this.protectionToken = protectionToken;
    }

    /**
     * @return Returns the signatureToken.
     */
    public SignatureToken getSignatureToken() {
        return signatureToken;
    }

    /**
     * @param signatureToken The signatureToken to set.
     */
    public void setSignatureToken(SignatureToken signatureToken) {
        if(this.protectionToken != null) {
//            throw new WSSPolicyException("Cannot use a SignatureToken in a " +
//                    "SymmetricBinding when there is a ProtectionToken");
        }
        this.signatureToken = signatureToken;
    }
    
    public QName getName() {
        return Constants.SYMMETRIC_BINDING;
    }

    public PolicyComponent normalize() {
        if (isNormalized()) {
            return this;
        }
        
        AlgorithmSuite algorithmSuite = getAlgorithmSuite();
        List configurations = algorithmSuite.getConfigurations();
        
        if (configurations == null && configurations.size() == 1) {
            setNormalized(true);
            return this;
        }
        
        Policy policy = new Policy();
        ExactlyOne exactlyOne = new ExactlyOne();
        
        All wrapper;
        SymmetricBinding symmetricBinding;
        
        for (Iterator iterator = configurations.iterator(); iterator.hasNext();) {
            wrapper = new All();
            symmetricBinding = new SymmetricBinding();
            
            algorithmSuite = (AlgorithmSuite) iterator.next();
            symmetricBinding.setAlgorithmSuite(algorithmSuite);
            
            symmetricBinding.setEncryptionToken(getEncryptionToken());
            symmetricBinding.setEntireHeadersAndBodySignatures(isEntireHeadersAndBodySignatures());
            symmetricBinding.setIncludeTimestamp(isIncludeTimestamp());
            symmetricBinding.setLayout(getLayout());
            symmetricBinding.setProtectionOrder(getProtectionOrder());
            symmetricBinding.setProtectionToken(getProtectionToken());
            symmetricBinding.setSignatureProtection(isSignatureProtection());
            symmetricBinding.setSignatureToken(getSignatureToken());
            symmetricBinding.setSignedEndorsingSupportingTokens(getSignedEndorsingSupportingTokens());
            symmetricBinding.setSignedSupportingToken(getSignedSupportingToken());
            symmetricBinding.setTokenProtection(isTokenProtection());
            
            symmetricBinding.setNormalized(true);
            wrapper.addPolicyComponent(symmetricBinding);
            exactlyOne.addPolicyComponent(wrapper);
        }
        
        policy.addPolicyComponent(exactlyOne);
        return policy;
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        
        String localname = Constants.SYMMETRIC_BINDING.getLocalPart();
        String namespaceURI = Constants.SYMMETRIC_BINDING.getNamespaceURI();
        
        String prefix;
        String writerPrefix = writer.getPrefix(namespaceURI);
        
        if (writerPrefix == null) {
            prefix = Constants.SYMMETRIC_BINDING.getPrefix();
            writer.setPrefix(prefix, namespaceURI);
        } else {
            prefix = writerPrefix;
        }

        // <sp:SymmetricBinding>
        writer.writeStartElement(prefix, localname, namespaceURI);
        
        if (writerPrefix == null) {
            // xmlns:sp=".."
            writer.writeNamespace(prefix, namespaceURI);
        }
        
        
        String policyLocalName = Constants.POLICY.getLocalPart();
        String policyNamespaceURI = Constants.POLICY.getNamespaceURI();
        
        String wspPrefix;
        
        String wspWriterPrefix = writer.getPrefix(policyNamespaceURI);
        if (wspWriterPrefix == null) {
            wspPrefix = Constants.POLICY.getPrefix();
            writer.setPrefix(wspPrefix, policyNamespaceURI);
            
        } else {
           wspPrefix = wspWriterPrefix;
        }
        // <wsp:Policy>
        writer.writeStartElement(wspPrefix, policyLocalName, policyNamespaceURI);
        
        if ( protectionToken != null) {
            protectionToken.serialize(writer);
            
        } else if (encryptionToken != null &&  signatureToken != null) {
            encryptionToken.serialize(writer);
            signatureToken.serialize(writer);
            
        } else {
            throw new RuntimeException("Either (EncryptionToken and SignatureToken) or ProtectionToken must be set");
        }
        
        AlgorithmSuite algorithmSuite = getAlgorithmSuite();
        
        if (algorithmSuite == null) {
            throw new RuntimeException("AlgorithmSuite must be set");
        }
        // <sp:AlgorithmSuite />
        algorithmSuite.serialize(writer);
        
        Layout layout = getLayout();
        if (layout != null) {
            // <sp:Layout />
            layout.serialize(writer);
        }
        
        if (isIncludeTimestamp()) {
            // <sp:IncludeTimestamp />
            writer.writeStartElement(prefix, Constants.INCLUDE_TIMESTAMP.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        if (Constants.ENCRYPT_BEFORE_SIGNING.equals(getProtectionOrder())) {
            // <sp:EncryptBeforeSigning />
            writer.writeStartElement(prefix, Constants.ENCRYPT_BEFORE_SIGNING, namespaceURI);
            writer.writeEndElement();
            
        } else if (Constants.SIGN_BEFORE_ENCRYPTING.equals(getProtectionOrder())) {
            // <sp:SignBeforeEncrypt />
            writer.writeStartElement(prefix, Constants.SIGN_BEFORE_ENCRYPTING, namespaceURI);
            writer.writeEndElement();
        }
        
        if (isSignatureProtection()) {
            // <sp:EncryptSignature />
            writer.writeStartElement(prefix, Constants.ENCRYPT_SIGNATURE.getLocalPart(), namespaceURI);
            writer.writeEndElement();
        }
        
        if (isEntireHeadersAndBodySignatures()) {
            // <sp:OnlySignEntireHeadersAndBody />
            writer.writeStartElement(prefix, Constants.ONLY_SIGN_ENTIRE_HEADERS_AND_BODY, namespaceURI);
            writer.writeEndElement();
        }
        
        // </wsp:Policy>
        writer.writeEndElement();
        
        // </sp:SymmetricBinding>
        writer.writeEndElement();
        
    }
}
