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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.ws.secpolicy.Constants;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

public class SecpolicyModelTest extends TestCase {
    
    
    public void testSymmBinding() {
        try {
            Policy p = this.getPolicy("test-resources/policy-symm-binding.xml");
            List assertions = (List)p.getAlternatives().next();
            
            boolean symmBindingFound = false;
            
            for (Iterator iter = assertions.iterator(); iter.hasNext();) {
                Assertion assertion = (Assertion) iter.next();
                if(assertion instanceof SymmetricBinding) {
                    symmBindingFound = true;
                    SymmetricBinding binding = (SymmetricBinding)assertion;
                    assertEquals("IncludeTimestamp assertion not processed", true, binding.isIncludeTimestamp());
                    
                    ProtectionToken protectionToken = binding.getProtectionToken();
                    assertNotNull("ProtectionToken missing", protectionToken);
                    
                    Token token = protectionToken.getProtectionToken();
                    if(token instanceof X509Token) {
                        assertEquals("incorrect X509 token versin and type",
                                Constants.WSS_X509_V3_TOKEN10,
                                ((X509Token) token).getTokenVersionAndType());
                    } else {
                        fail("ProtectionToken must contain a X509Token assertion");
                    }
                    
                }
            }
            //The Asymm binding mean is not built in the policy processing :-(
            assertTrue("SymmetricBinding not porcessed",  symmBindingFound);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testAsymmBinding() {
        try {
            this.getPolicy("test-resources/policy-asymm-binding.xml");
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    public void testTransportBinding() {
        try {
            this.getPolicy("test-resources/policy-transport-binding.xml");
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    private Policy getPolicy(String filePath) throws Exception {
        StAXOMBuilder builder = new StAXOMBuilder(filePath);
        OMElement elem = builder.getDocumentElement();
        return PolicyEngine.getPolicy(elem);
    }
}
