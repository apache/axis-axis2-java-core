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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.ws.policy.All;
import org.apache.ws.policy.ExactlyOne;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PrimitiveAssertion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

public abstract class AxisDescription implements ParameterInclude,
        DescriptionConstants {

    private AxisDescription parent = null;

    private ParameterInclude parameterInclude;

    private PolicyInclude policyInclude = null;

    private HashMap children;


    public AxisDescription() {
        parameterInclude = new ParameterIncludeImpl();
        children = new HashMap();
    }

    public void addParameter(Parameter param) throws AxisFault {

        if (param == null) {
            return;
        }

        if (isParameterLocked(param.getName())) {
            throw new AxisFault(Messages.getMessage("paramterlockedbyparent", param.getName()));
        }

        parameterInclude.addParameter(param);
    }

    public void removeParameter(Parameter param) throws AxisFault {
        parameterInclude.removeParameter(param);
    }

    public void deserializeParameters(OMElement parameterElement)
            throws AxisFault {

        parameterInclude.deserializeParameters(parameterElement);

    }

    public Parameter getParameter(String name) {
        Parameter parameter = parameterInclude.getParameter(name);
        if (parameter == null && parent != null) {
            return parent.getParameter(name);
        } else {
            return parameter;
        }
    }

    public ArrayList getParameters() {
        return parameterInclude.getParameters();
    }

    public boolean isParameterLocked(String parameterName) {

        if (getParent() != null && getParent().isParameterLocked(parameterName)) {
            return true;
        }

        return getParameter(parameterName) != null
                && getParameter(parameterName).isLocked();
    }


    public void setParent(AxisDescription parent) {
        this.parent = parent;
    }

    public AxisDescription getParent() {
        return parent;
    }

    public void setPolicyInclude(PolicyInclude policyInclude) {
        this.policyInclude = policyInclude;
    }

    public PolicyInclude getPolicyInclude() {
        if (policyInclude == null) {
            policyInclude = new PolicyInclude(this);
        }
        return policyInclude;
    }

    public void addChild(AxisDescription child) {
        children.put(child.getKey(), child);
    }

    public void addChild(Object key, AxisDescription child) {
        children.put(key, child);
    }

    public Iterator getChildren() {
        return children.values().iterator();
    }

    public AxisDescription getChild(Object key) {
        return (AxisDescription) children.get(key);
    }

    public void removeChild(Object key) {
        children.remove(key);
    }
    

    /**
     * This method sets the policy as the default of this AxisDescription
     * instance. Further more this method does the followings.
     *
     *  (1) Engage whatever modules necessary to execute new the effective
     *  policy of this AxisDescription instance.
     *  (2) Disengage whatever modules that are not necessary to excute the
     *  new effective policy of this AxisDescription instance.
     *  (3) Check whether each module can execute the new effective policy
     *  of this AxisDescription instance.
     *  (4) If not throw an AxisFault to notify the user.
     *  (5) Else notify each module about the new effective policy.
     *
     * @param policy the new policy of this AxisDescription instance. The
     *        effective policy is the merge of this argument with effective
     *        policy of parent of this AxisDescription.
     * @throws AxisFault if any module is unable to execute the effective policy
     *         of this AxisDescription instance sucessfully or no module to
     *         execute some portion (one or more PrimtiveAssertions ) of that
     *         effective policy.
     */
    public void applyPolicy(Policy policy) throws AxisFault {
        AxisConfiguration configuration = getAxisConfiguration();

        this.policyInclude.setPolicy(policy);

        Policy effPolicy = this.policyInclude.getEffectivePolicy();
        ExactlyOne exactlyOne = (ExactlyOne) effPolicy.getTerms().get(0);

        ArrayList list = new ArrayList();

        for (Iterator iterator = exactlyOne.getTerms().iterator(); iterator.hasNext();) {
            All all = (All) iterator.next();
            if (!checkAllternative(all.getTerms(), configuration)) {
                list.add(all);
            }
        }

        exactlyOne.getTerms().removeAll(list);

        if (exactlyOne.isEmpty()) {
            throw new AxisFault("can't find any Alternative with known Policy assertions");
        }

        Map modules = configuration.getModules();

        for (Iterator iterator = modules.values().iterator(); iterator.hasNext();) {
            AxisModule module = (AxisModule) iterator.next();
            // TODO needs to implement this method
//            module.validate(effPolicy);
        }

        if (exactlyOne.isEmpty()) {
            throw new AxisFault("can't find any compaitible Alternative");
        }

        // pick an arbitary Alternative
        All target = (All) exactlyOne.getTerms().get(0);
        exactlyOne.getTerms().removeAll(exactlyOne.getTerms());
        exactlyOne.addTerm(target);

        List requiredModules =  getModulesForAlternative(target.getTerms(), configuration);

        for (Iterator iterator = requiredModules.iterator(); iterator.hasNext();) {
            AxisModule module = (AxisModule) iterator.next();
            if (! isEngaged(module.getName())) {
                engageModule(module, configuration);
            } else {
                // TODO needs to implement this method
//                module.applyPolicy(effPolicy, this);
            }
        }

    }

    private List getModulesForAlternative(List primitiveTerms, AxisConfiguration configuration) {

        ArrayList namespaceURIs = new ArrayList();
        ArrayList modulesList = new ArrayList();

        PrimitiveAssertion primitive;
        String namespaceURI;

        for (Iterator iterator = primitiveTerms.iterator(); iterator.hasNext();) {
            primitive = (PrimitiveAssertion) iterator.next();
            namespaceURI = primitive.getName().getNamespaceURI();

            if (! namespaceURIs.contains(namespaceURI)) {
                namespaceURIs.add(namespaceURI);
            }
        }

        for (Iterator iterator = namespaceURIs.iterator(); iterator.hasNext();) {
            modulesList.addAll(configuration.getModulesForPolicyNamesapce((String) iterator.next()));
        }

        return modulesList;
    }
    
    private boolean checkAllternative(List terms, AxisConfiguration configuration) {

        PrimitiveAssertion assertion;

        for (Iterator iterator = terms.iterator(); iterator.hasNext();) {
            assertion = (PrimitiveAssertion) iterator.next();

            String namespace = assertion.getName().getNamespaceURI();
            List modulesList = configuration.getModulesForPolicyNamesapce(namespace);
            if (modulesList != null) {
                return false;
            }

        }
        return true;
    }

    
    public AxisConfiguration getAxisConfiguration() {
        
        if (this instanceof AxisConfiguration) {
            return (AxisConfiguration) this;
        }
        
        if (getParent() != null) {
            return getParent().getAxisConfiguration();
        }
        
        return null;
    }

    public abstract Object getKey();

    /**
     * Engagaging a module to diferrent level
     *
     * @param axisModule
     * @param axisConfig
     */
    public abstract void engageModule(AxisModule axisModule,
                                      AxisConfiguration axisConfig) throws AxisFault;
    
    public abstract boolean isEngaged(QName axisModule);
}
