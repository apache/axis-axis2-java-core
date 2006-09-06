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

public class AsymmetricBinding extends SymmetricAsymmetricBindingBase {
    
    private InitiatorToken initiatorToken;
    
    private RecipientToken recipientToken;
    
    /**
     * @return Returns the initiatorToken.
     */
    public InitiatorToken getInitiatorToken() {
        return initiatorToken;
    }
    /**
     * @param initiatorToken The initiatorToken to set.
     */
    public void setInitiatorToken(InitiatorToken initiatorToken) {
        this.initiatorToken = initiatorToken;
    }
    /**
     * @return Returns the recipientToken.
     */
    public RecipientToken getRecipientToken() {
        return recipientToken;
    }
    /**
     * @param recipientToken The recipientToken to set.
     */
    public void setRecipientToken(RecipientToken recipientToken) {
        this.recipientToken = recipientToken;
    }
    
    public QName getName() {
        return Constants.ASYMMETRIC_BINDING;
    }
    public PolicyComponent normalize() {
        
        if (isNormalized()) {
            return this;
        }
        
        AlgorithmSuite algorithmSuite = getAlgorithmSuite();
        List configs = algorithmSuite.getConfigurations();
        
        Policy policy = new Policy();
        ExactlyOne exactlyOne = new ExactlyOne();
        
        policy.addPolicyComponent(exactlyOne);
        
        All wrapper;
        AsymmetricBinding asymmetricBinding;
        
        for (Iterator iterator = configs.iterator(); iterator.hasNext();) {
            wrapper = new All();
            asymmetricBinding = new AsymmetricBinding();
            
            asymmetricBinding.setAlgorithmSuite((AlgorithmSuite) iterator.next());
            asymmetricBinding.setEntireHeaderAndBodySignatures(isEntireHeaderAndBodySignatures());
            asymmetricBinding.setIncludeTimestamp(isIncludeTimestamp());
            asymmetricBinding.setInitiatorToken(getInitiatorToken());
            asymmetricBinding.setLayout(getLayout());
            asymmetricBinding.setProtectionOrder(getProtectionOrder());
            asymmetricBinding.setRecipientToken(getRecipientToken());
            asymmetricBinding.setSignatureProtection(isSignatureProtection());
            asymmetricBinding.setSignedEndorsingSupportingTokens(getSignedEndorsingSupportingTokens());
            asymmetricBinding.setTokenProtection(isTokenProtection());
            
            asymmetricBinding.setNormalized(true);
            wrapper.addPolicyComponent(wrapper);
        }
        
        return policy; 
        
    }
    
    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
}
