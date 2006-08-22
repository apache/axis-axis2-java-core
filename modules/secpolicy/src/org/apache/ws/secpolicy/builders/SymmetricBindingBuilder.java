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
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.ws.secpolicy.Constants;
import org.apache.ws.secpolicy.model.EncryptionToken;
import org.apache.ws.secpolicy.model.ProtectionToken;
import org.apache.ws.secpolicy.model.SignatureToken;
import org.apache.ws.secpolicy.model.SymmetricBinding;

public class SymmetricBindingBuilder implements AssertionBuilder {

    public Assertion build(OMElement element, AssertionBuilderFactory factory) throws IllegalArgumentException {
        SymmetricBinding symmetricBinding = new SymmetricBinding();
        
        
        return symmetricBinding;
    }

    public QName getKnownElement() {
        return Constants.SYMMETRIC_BINDING;
    }
    
    private void processAlternatives(List assertions, SymmetricBinding parent) {
        SymmetricBinding symmetricBinding = new SymmetricBinding();
        
        Assertion assertion;
        QName name;
        
        for (Iterator iterator = assertions.iterator(); iterator.hasNext();) {
            assertion = (Assertion) iterator.next();
            name = assertion.getName();
            
            if (Constants.ENCRYPTION_TOKEN.equals(name)) {
                symmetricBinding.setEncryptionToken((EncryptionToken) assertion);
                
            } else if (Constants.SIGNATURE_TOKEN.equals(name)) {
                symmetricBinding.setSignatureToken((SignatureToken) assertion);
                
            } else if (Constants.PROTECTION_TOKEN.equals(name)) {
                symmetricBinding.setProtectionToken((ProtectionToken) assertion);
                
            }
        }
        
        parent.addConfiguration(symmetricBinding);
        
    }
}
