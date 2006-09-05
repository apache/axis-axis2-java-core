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
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;

import junit.framework.TestCase;

public class SecpolicyModelTest extends TestCase {
    
    
    public void testSymmBinding() {
        try {
            Policy p = this.getPolicy("test-resources/policy-symm-binding.xml");
            
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
