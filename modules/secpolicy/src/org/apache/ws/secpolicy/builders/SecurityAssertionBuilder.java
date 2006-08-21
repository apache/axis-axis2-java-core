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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.builders.AssertionBuilder;

public class SecurityAssertionBuilder implements AssertionBuilder {

    private static final String TRANSPORT_BINDING = "TransportBinding";
    private static final String SYMMETRIC_BINDING = "SymmetricBinding";
    private static final String ASYMMETRIC_BINDING = "AsymmetricBinding";
    
    public Assertion build(OMElement element, AssertionBuilderFactory factory) throws IllegalArgumentException {
        
        QName qname = element.getQName();
        
        if (TRANSPORT_BINDING.equals(qname.getLocalPart())) {
            
        } else if (SYMMETRIC_BINDING.equals(qname.getLocalPart())) {
            
        } else if (ASYMMETRIC_BINDING.equals(qname.getLocalPart())) {
            
        }
        // TODO Auto-generated method stub
        return null;
    }
    
    
}
