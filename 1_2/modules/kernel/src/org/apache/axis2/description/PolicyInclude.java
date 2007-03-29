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

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyReference;
import org.apache.neethi.PolicyRegistry;
import org.apache.neethi.PolicyRegistryImpl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class PolicyInclude {

    public static final int ANON_POLICY = 100;

    public static final int AXIS_POLICY = 1;

    public static final int AXIS_MODULE_POLICY = 2;
    
    public static final int AXIS_MODULE_OPERATION_POLICY = 17;

    public static final int AXIS_SERVICE_POLICY = 3;

    public static final int AXIS_OPERATION_POLICY = 4;

    public static final int AXIS_MESSAGE_POLICY = 5;

    public static final int SERVICE_POLICY = 6;

    public static final int PORT_POLICY = 7;

    public static final int PORT_TYPE_POLICY = 8;

    public static final int BINDING_POLICY = 9;

    public static final int OPERATION_POLICY = 10;

    public static final int BINDING_OPERATION_POLICY = 11;

    public static final int INPUT_POLICY = 12;

    public static final int OUTPUT_POLICY = 13;

    public static final int BINDING_INPUT_POLICY = 14;

    public static final int BINDING_OUTPUT_POLICY = 15;

    public static final int MESSAGE_POLICY = 16;

    private Policy policy = null;

    private Policy effectivePolicy = null;

    private PolicyRegistry reg;

    private AxisDescription description;

    private Hashtable wrapperElements = new Hashtable();

    public PolicyInclude() {
        reg = new PolicyRegistryImpl();
    }

    public PolicyInclude(AxisDescription axisDescription) {

        if (axisDescription.getParent() != null) {
            PolicyInclude parentPolicyInclude = axisDescription.getParent().getPolicyInclude();
            reg = new PolicyRegistryImpl(parentPolicyInclude.getPolicyRegistry());
        } else {
            reg = new PolicyRegistryImpl();
        }
        setDescription(axisDescription);
    }

    public void setPolicyRegistry(PolicyRegistry reg) {
        this.reg = reg;
    }

    public PolicyRegistry getPolicyRegistry() {
        return reg;
    }

    public void setPolicy(Policy policy) {
        wrapperElements.clear();

        if (policy.getName() == null && policy.getId() == null) {
            policy.setId(UUIDGenerator.getUUID());
        }

        Wrapper wrapper = new Wrapper(PolicyInclude.ANON_POLICY, policy);
        if (policy.getName() != null) {
            wrapperElements.put(policy.getName(), wrapper);
        } else {
            wrapperElements.put(policy.getId(), wrapper);
        }
    }

    public void updatePolicy(Policy policy) {
        String key;

        if ((key = policy.getName()) == null && (key = policy.getId()) == null) {
            // TODO throw more meaningful exception ..
            throw new RuntimeException("policy doesn't have a name or an id ");
        }

        Wrapper wrapper = (Wrapper) wrapperElements.get(key);
        wrapper.value = policy;
    }

    public void setEffectivePolicy(Policy effectivePolicy) {
        this.effectivePolicy = effectivePolicy;
    }

    public void setDescription(AxisDescription description) {
        this.description = description;
    }

    public AxisDescription getDescription() {
        return description;
    }

    private PolicyInclude getParent() {

        if (description != null && description.getParent() != null) {
            return description.getParent().getPolicyInclude();
        }
        return null;
    }

    private void calculatePolicy() {

        Policy result = null;
        Iterator iterator = wrapperElements.values().iterator();

        while (iterator.hasNext()) {
            Object policyElement = ((Wrapper) iterator.next()).getValue();
            Policy p;

            if (policyElement instanceof PolicyReference) {
                PolicyRegistry r = getPolicyRegistry();
                p = (Policy) ((PolicyReference) policyElement)
                        .normalize(getPolicyRegistry(), false);

            } else if (policyElement instanceof Policy) {
                p = (Policy) policyElement;

            } else {
                // TODO AxisFault?
                throw new RuntimeException();
            }

            result = (result == null) ? (Policy) p : (Policy) result.merge(p);
        }

        this.policy = result;
    }

    private void calculateEffectivePolicy() {
        Policy result;

        if (getParent() != null) {
            Policy parentPolicy = getParent().getEffectivePolicy();

            if (parentPolicy == null) {
                result = getPolicy();

            } else {

                if (getPolicy() != null) {
                    result = (Policy) parentPolicy.merge(getPolicy());

                } else {
                    result = parentPolicy;
                }
            }

        } else {
            result = getPolicy();
        }
        setEffectivePolicy(result);
    }

    public Policy getPolicy() {
        calculatePolicy();
        return policy;
    }

    public Policy getEffectivePolicy() {
        calculateEffectivePolicy();
        return effectivePolicy;
    }

    public ArrayList getPolicyElements() {
        ArrayList policyElementsList = new ArrayList();
        Iterator policyElementIterator = wrapperElements.values().iterator();

        while (policyElementIterator.hasNext()) {
            policyElementsList
                    .add(((Wrapper) policyElementIterator.next()).getValue());
        }
        return policyElementsList;
    }

    public ArrayList getPolicyElements(int type) {
        ArrayList policyElementList = new ArrayList();
        Iterator wrapperElementIterator = wrapperElements.values().iterator();
        Wrapper wrapper;

        while (wrapperElementIterator.hasNext()) {
            wrapper = (Wrapper) wrapperElementIterator.next();

            if (wrapper.getType() == type) {
                policyElementList.add(wrapper.getValue());
            }
        }
        return policyElementList;
    }

    public void registerPolicy(String key, Policy policy) {
        reg.register(key, policy);
    }

    public Policy getPolicy(String key) {
        return reg.lookup(key);
    }

    public void addPolicyElement(int type, Policy policy) {

        String key;

        if ((key = policy.getName()) == null && (key = policy.getId()) == null) {
            policy.setId(UUIDGenerator.getUUID());
        }

        key = (policy.getName() != null) ? policy.getName() : policy.getId();

        Wrapper wrapper = new Wrapper(type, policy);
        wrapperElements.put(key, wrapper);
        reg.register(key, policy);
    }

    public void addPolicyRefElement(int type, PolicyReference policyReference) {
        Wrapper wrapper = new Wrapper(type, policyReference);
        wrapperElements.put(policyReference.getURI(), wrapper);
    }

    class Wrapper {
        private int type;
        private Object value;

        Wrapper(int type, Object value) {
            setType(type);
            setValue(value);
        }

        void setType(int type) {
            this.type = type;
        }

        int getType() {
            return type;
        }

        void setValue(Object value) {
            this.value = value;
        }

        Object getValue() {
            return value;
        }
    }

    public void removePolicyElement(String policyURI) {
        wrapperElements.remove(policyURI);
        reg.remove(policyURI);
    }

    public void removeAllPolicyElements() {
        wrapperElements.clear();
    }
}
