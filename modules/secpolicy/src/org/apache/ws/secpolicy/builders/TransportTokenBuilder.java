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
package org.apache.ws.security.secpolicy.builders;

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.XmlPrimtiveAssertion;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.ws.security.secpolicy.Constants;
import org.apache.ws.security.secpolicy.model.HttpsToken;
import org.apache.ws.security.secpolicy.model.TransportToken;

public class TransportTokenBuilder implements AssertionBuilder {
    
   
    
    public Assertion build(OMElement element, AssertionBuilderFactory factory) throws IllegalArgumentException {
        TransportToken transportToken = new TransportToken();
        
        Policy policy = PolicyEngine.getPolicy(element.getFirstElement());
        policy = (Policy) policy.normalize(false);
        
        for (Iterator iterator = policy.getAlternatives(); iterator.hasNext();) {
            processAlternative((List) iterator.next(), transportToken);
        }
        
        return transportToken;
    }
    
    
    private void processAlternative(List assertions, TransportToken parent) {
        TransportToken transportToken = new TransportToken();
        
        for (Iterator iterator = assertions.iterator(); iterator.hasNext();) {
            XmlPrimtiveAssertion primtive = (XmlPrimtiveAssertion) iterator.next();
            QName qname = primtive.getName();
            
            if (Constants.HTTPS_TOKEN.equals(qname)) {
                HttpsToken httpsToken = new HttpsToken();
                transportToken.setTransportToken(httpsToken);
            }
        }
        
        parent.addOption(transportToken);
    }
}
