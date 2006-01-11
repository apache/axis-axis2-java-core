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

import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PolicyReference;
import org.apache.ws.policy.util.PolicyRegistry;

import java.util.ArrayList;
import java.util.Iterator;

public class PolicyInclude {

    public static final String ANON_POLICY = "anonymous";

    public static final int AXIS_POLICY = 1;

    public static final int AXIS_SERVICE_POLICY = 2;

    public static final int AXIS_OPERATION_POLICY = 14;

    public static final int AXIS_MESSAGE_POLICY = 15;

    public static final int MODULE_POLICY = 3;

    public static final int SERVICE_POLICY = 3;

    public static final int PORT_POLICY = 4;

    public static final int PORT_TYPE_POLICY = 5;

    public static final int BINDING_POLICY = 6;

    public static final int OPERATION_POLICY = 7;

    public static final int BINDING_OPERATION_POLICY = 8;

    public static final int INPUT_POLICY = 9;

    public static final int OUTPUT_POLICY = 10;

    public static final int BINDING_INPUT_POLICY = 11;

    public static final int BINDING_OUTPUT_POLICY = 12;

    public static final int MESSAGE_POLICY = 13;

    private Policy policy = null;

    private Policy effectivePolicy = null;

    private PolicyInclude parent = null;

    private PolicyRegistry reg;

    private ArrayList policyElements = new ArrayList();

    public PolicyInclude() {
        reg = new PolicyRegistry();
    }

    public PolicyInclude(PolicyInclude parent) {
        reg = new PolicyRegistry();
        setParent(parent);
    }

    public void setParent(PolicyInclude parent) {
        this.parent = parent;
        reg.setParent(parent.getPolicyRegistry());
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

                Object policyElement = ((PolicyElement) iterator.next()).value;
                Policy p = null;

                if (policyElement instanceof PolicyReference) {
                    p = (Policy) ((PolicyReference) policyElement)
                            .normalize(getPolicyRegistry());

                } else if (policyElement instanceof Policy) {
                    p = (Policy) policyElement;

                } else {
                    // TODO an exception ?
                }
                policy = (policy == null) ? (Policy) p.normalize(reg)
                        : (Policy) policy.merge(p, reg);
            }
        }

        return policy;
    }

    public Policy getEffectivePolicy() {

        if (effectivePolicy != null) {
            return effectivePolicy;
        }

        Policy parentEffectivePolicy = parent.getEffectivePolicy();

        if (parent == null || parentEffectivePolicy == null) {
            return getPolicy();
        }

        if (getPolicy() != null) {
            return parent.getEffectivePolicy();
        }

        return (Policy) parentEffectivePolicy.merge(getPolicy(), reg);

    }

    //    public void setPolicyElements(ArrayList policyElements) {
    //        this.policyElements = policyElements;
    //    }

    public ArrayList getPolicyElements() {
        ArrayList policyElementsList = new ArrayList();
        Iterator policyElementIterator = policyElements.iterator();

        while (policyElementIterator.hasNext()) {
            policyElementsList.add(((PolicyElement) policyElementIterator
                    .next()).value);
        }
        return policyElementsList;
    }


    public ArrayList getPolicyElements(int type) {
        ArrayList policyElementList = new ArrayList();
        Iterator policyElementIterator = policyElements.iterator();

        PolicyElement policyElement;

        while (policyElementIterator.hasNext()) {
            policyElement = (PolicyElement) policyElementIterator.next();

            if (policyElement.type == type) {
                policyElementList.add(policyElement.value);
            }
        }

        return policyElementList;

    }

    public void registerPolicy(Policy policy) {
        reg.register(policy.getPolicyURI(), policy);
    }

    public Policy getPolicy(String policyURI) {
        return reg.lookup(policyURI);
    }

    public void addPolicyElement(int type, Policy policy) {
        PolicyElement policyElement = new PolicyElement();
        policyElement.type = type;
        policyElement.value = policy;
        policyElements.add(policyElement);

        if (policy.getPolicyURI() != null) {
            reg.register(policy.getPolicyURI(), policy);
        }
    }

    public void addPolicyRefElement(int type, PolicyReference policyReference) {
        PolicyElement policyElement = new PolicyElement();
        policyElement.type = type;
        policyElement.value = policyReference;
        policyElements.add(policyElement);
    }

    private class PolicyElement {
        int type;

        Object value;
    }
}
