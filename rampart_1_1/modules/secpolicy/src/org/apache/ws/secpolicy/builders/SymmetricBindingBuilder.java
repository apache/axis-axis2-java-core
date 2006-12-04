/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.ws.secpolicy.builders;

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.ws.secpolicy.Constants;
import org.apache.ws.secpolicy.model.AlgorithmSuite;
import org.apache.ws.secpolicy.model.Layout;
import org.apache.ws.secpolicy.model.ProtectionToken;
import org.apache.ws.secpolicy.model.SymmetricBinding;

public class SymmetricBindingBuilder implements AssertionBuilder {

    public Assertion build(OMElement element, AssertionBuilderFactory factory) throws IllegalArgumentException {
        SymmetricBinding symmetricBinding = new SymmetricBinding(); 
        
        Policy policy = PolicyEngine.getPolicy(element.getFirstElement());
        policy = (Policy) policy.normalize(false);
        
        for (Iterator iterator = policy.getAlternatives(); iterator.hasNext();) {
            processAlternatives((List) iterator.next(), symmetricBinding);
            
            /*
             * since there should be only one alternative ..
             */
            break; 
        }
        return symmetricBinding;
    }

    public QName[] getKnownElements() {
        return new QName[] {Constants.SYMMETRIC_BINDING};
    }
    
    private void processAlternatives(List assertions, SymmetricBinding symmetricBinding) {
        Assertion assertion;
        QName name;
        
        for (Iterator iterator = assertions.iterator(); iterator.hasNext();) {
            assertion = (Assertion) iterator.next();
            name = assertion.getName();
            
            if (Constants.ALGORITHM_SUITE.equals(name)) {
                symmetricBinding.setAlgorithmSuite((AlgorithmSuite) assertion);
                
            } else if (Constants.LAYOUT.equals(name)) {
                symmetricBinding.setLayout((Layout) assertion);
                
            } else if (Constants.INCLUDE_TIMESTAMP.equals(name)) {
                symmetricBinding.setIncludeTimestamp(true);
                
            } else if (Constants.PROTECTION_TOKEN.equals(name)) {
                symmetricBinding.setProtectionToken((ProtectionToken) assertion);
                
            } else if (Constants.ENCRYPT_BEFORE_SIGNING.equals(name.getLocalPart())) {
                symmetricBinding.setProtectionOrder(Constants.ENCRYPT_BEFORE_SIGNING);
                
            } else if (Constants.SIGN_BEFORE_ENCRYPTING.equals(name.getLocalPart())) {
                symmetricBinding.setProtectionOrder(Constants.SIGN_BEFORE_ENCRYPTING);
                
            } else if (Constants.ONLY_SIGN_ENTIRE_HEADERS_AND_BODY.equals(name.getLocalPart())) {
                symmetricBinding.setEntireHeadersAndBodySignatures(true);
                
            } else if (Constants.ENCRYPT_SIGNATURE.equals(name)) {
                symmetricBinding.setSignatureProtection(true);
            }
        }        
    }
}
