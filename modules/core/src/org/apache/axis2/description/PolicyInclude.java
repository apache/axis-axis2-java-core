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

package org.apache.axis2.description;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PolicyReference;
import org.apache.ws.policy.util.PolicyRegistry;

/**
 * @author Sanka Samaranayake (sanka@apache.org)
 */
public class PolicyInclude {

    private Policy policy = null;
    private Policy effectivePolicy = null;
    
    private PolicyInclude parent = null;
    private PolicyRegistry reg;

    private ArrayList policyElements = new ArrayList();

    public PolicyInclude() {
        reg = new PolicyRegistry();
    }

    public PolicyInclude(PolicyInclude parent) {
        reg = new PolicyRegistry(parent.getPolicyRegistry());
    }

    public void setPolicyRegistry(PolicyRegistry reg) {
        this.reg = reg;
    }
    
    public PolicyRegistry getPolicyRegistry() {
        return reg;
    }
    
    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public Policy getPolicy() {

        if (policy == null) {
            Iterator iterator = policyElements.iterator();

            while (iterator.hasNext()) {
                Object policyElement = iterator.next();
                Policy p = null;

                if (policyElement instanceof PolicyReference) {
                    p = (Policy) ((PolicyReference) policyElement)
                            .normalize(getPolicyRegistry());

                } else if (policyElement instanceof Policy) {
                    p = (Policy) iterator.next();

                } else {
                    // TODO an exception ?
                }
                policy = (policy == null) ? p : (Policy) policy.merge(p, reg);
            }
        }
        return policy;
    }

    public Policy getEffectivePolicy() {

        if (parent == null || parent.getEffectivePolicy() == null) {
            return getPolicy();
        }
        
        if (getPolicy() != null) {
            return parent.getEffectivePolicy();
        }
        
        return (Policy) parent.getEffectivePolicy().merge(getPolicy(), reg);

    }
    
    public void setPolicyElements(ArrayList policyElements) {
        this.policyElements = policyElements;
    }
    
    public ArrayList getPolicyElements() {
        return policyElements;
    }

    public void addPolicyElement(Policy policyElement) {
        if (policyElement.getPolicyURI() != null) {
            reg.register(policyElement.getPolicyURI(), policyElement);
        }
        
        policyElements.add(policyElement);
    }

    public void addPolicyRefElement(PolicyReference policyRefElement) {
        policyElements.add(policyRefElement);
    }
}
