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
import org.apache.axis2.util.Utils;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.modules.Module;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.Map;

public abstract class AxisDescription implements ParameterInclude,
        DescriptionConstants {

    private AxisDescription parent = null;

    private ParameterInclude parameterInclude;

    private PolicyInclude policyInclude = null;

    private HashMap children;

    protected Map engagedModules;

    // Holds the documentation details for each element
    private String documentation;

    // creating a logger instance
    private Log log = LogFactory.getLog(this.getClass());

    public AxisDescription() {
        parameterInclude = new ParameterIncludeImpl();
        children = new HashMap();
    }

    public void addParameter(Parameter param) throws AxisFault {
        if (param == null) {
            return;
        }

        if (isParameterLocked(param.getName())) {
            throw new AxisFault(Messages.getMessage("paramterlockedbyparent",
                                                    param.getName()));
        }

        parameterInclude.addParameter(param);
    }

    public void addParameter(String name, Object value) throws AxisFault {
        addParameter(new Parameter(name, value));
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
        if (parameter != null) {
            return parameter;
        }
        if (parent != null) {
            return parent.getParameter(name);
        }
        return null;
    }

    public Object getParameterValue(String name) {
        Parameter param = getParameter(name);
        if (param == null) {
            return null;
        }
        return param.getValue();
    }

    public boolean isParameterTrue(String name) {
        Parameter param = getParameter(name);
        if (param == null) {
            return false;
        }
        return JavaUtils.isTrue(param.getValue());
    }

    public ArrayList getParameters() {
        return parameterInclude.getParameters();
    }

    public boolean isParameterLocked(String parameterName) {

        if (this.parent != null && this.parent.isParameterLocked(parameterName)) {
            return true;
        }

        Parameter parameter = getParameter(parameterName);
        return parameter != null && parameter.isLocked();
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
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
     * <p/>
     * (1) Engage whatever modules necessary to execute new the effective policy
     * of this AxisDescription instance. (2) Disengage whatever modules that are
     * not necessary to execute the new effective policy of this AxisDescription
     * instance. (3) Check whether each module can execute the new effective
     * policy of this AxisDescription instance. (4) If not throw an AxisFault to
     * notify the user. (5) Else notify each module about the new effective
     * policy.
     *
     * @param policy the new policy of this AxisDescription instance. The effective
     *               policy is the merge of this argument with effective policy of
     *               parent of this AxisDescription.
     * @throws AxisFault if any module is unable to execute the effective policy of
     *                   this AxisDescription instance successfully or no module to
     *                   execute some portion (one or more PrimtiveAssertions ) of
     *                   that effective policy.
     */
    public void applyPolicy(Policy policy) throws AxisFault {
        AxisConfiguration configuration = getAxisConfiguration();

        if (configuration == null) {
            // FIXME return or throw an Exception?
            return;
        }

        // sets AxisDescription policy
        getPolicyInclude().setPolicy(policy);

        /*
         * now we should take the effective one .. it is necessary since
         * AxisDescription.applyPolicy(..) doesn't override policies at the
         * Upper levels.
         */
        Policy effPolicy = getPolicyInclude().getEffectivePolicy();

        /*
         * for the moment we consider policies with only one alternative. If the
         * policy contains multiple alternatives only the first alternative will
         * be considered.
         */
        Iterator iterator = effPolicy.getAlternatives();
        if (!iterator.hasNext()) {
            throw new AxisFault(
                    "Policy doesn't contain any policy alternatives");
        }

        List assertionList = (List) iterator.next();

        Assertion assertion;
        String namespaceURI;

        List moduleList;

        List namespaceList = new ArrayList();
        List modulesToEngage = new ArrayList();

        for (Iterator assertions = assertionList.iterator(); assertions
                .hasNext();) {
            assertion = (Assertion) assertions.next();
            namespaceURI = assertion.getName().getNamespaceURI();

            moduleList = configuration
                    .getModulesForPolicyNamesapce(namespaceURI);

            if (moduleList == null) {
                log.debug("can't find any module to process "
                        + assertion.getName() + " type assertions");
                continue;
            }

            if (!canSupportAssertion(assertion, moduleList)) {
                throw new AxisFault("atleast one module can't support "
                        + assertion.getName());
            }

            if (!namespaceList.contains(namespaceURI)) {
                namespaceList.add(namespaceURI);
                modulesToEngage.addAll(moduleList);
            }
        }

        /*
         * FIXME We need to disengage any modules that are already engaged *but*
         * has nothing to do with the policy to apply
         */

        engageModulesToAxisDescription(modulesToEngage, this);
    }

    /**
     * Applies the policies on the Description Hierarchy recursively.
     *
     * @throws AxisFault an error occurred applying the policy
     */
    public void applyPolicy() throws AxisFault {

        if (this instanceof AxisMessage) {
            return;
        }

        AxisConfiguration configuration = getAxisConfiguration();
        if (configuration == null) {
            return; // CHECKME: May be we need to throw an Exception ??
        }

        Policy effPolicy = getApplicablePolicy(this);

        if (effPolicy != null) {

            /*
             * for the moment we consider policies with only one alternative. If
             * the policy contains multiple alternatives only the first
             * alternative will be considered.
             */
            Iterator iterator = effPolicy.getAlternatives();
            if (!iterator.hasNext()) {
                throw new AxisFault(
                        "Policy doesn't contain any policy alternatives");
            }

            List assertionList = (List) iterator.next();

            Assertion assertion;
            String namespaceURI;

            List moduleList;

            List namespaceList = new ArrayList();
            List modulesToEngage = new ArrayList();

            for (Iterator assertions = assertionList.iterator(); assertions
                    .hasNext();) {
                assertion = (Assertion) assertions.next();
                namespaceURI = assertion.getName().getNamespaceURI();

                moduleList = configuration
                        .getModulesForPolicyNamesapce(namespaceURI);

                if (moduleList == null) {
                    log.debug("can't find any module to process "
                            + assertion.getName() + " type assertions");
                    continue;
                }

                if (!canSupportAssertion(assertion, moduleList)) {
                    throw new AxisFault("atleast one module can't support "
                            + assertion.getName());
                }

                if (!namespaceList.contains(namespaceURI)) {
                    namespaceList.add(namespaceURI);
                    modulesToEngage.addAll(moduleList);
                }
            }

            /*
             * FIXME We need to disengage any modules that are already engaged
             * *but* has nothing to do with the policy to apply
             */

            engageModulesToAxisDescription(modulesToEngage, this);

        }

        AxisDescription child;

        for (Iterator children = getChildren(); children.hasNext();) {
            child = (AxisDescription) children.next();
            child.applyPolicy();
        }
    }

    private boolean canSupportAssertion(Assertion assertion, List moduleList) {

        AxisModule axisModule;
        Module module;

        for (Iterator iterator = moduleList.iterator(); iterator.hasNext();) {
            axisModule = (AxisModule) iterator.next();
            // FIXME is this step really needed ??
            // Shouldn't axisMoudle.getModule always return not-null value ??
            module = axisModule.getModule();

            if (!(module == null || module.canSupportAssertion(assertion))) {
                log.debug(axisModule.getName() + " says it can't support " + assertion.getName());
                return false;
            }
        }

        return true;
    }

    private void engageModulesToAxisDescription(List moduleList,
                                                AxisDescription description) throws AxisFault {

        AxisModule axisModule;
        Module module;

        for (Iterator iterator = moduleList.iterator(); iterator.hasNext();) {
            axisModule = (AxisModule) iterator.next();
            // FIXME is this step really needed ??
            // Shouldn't axisMoudle.getModule always return not-null value ??
            module = axisModule.getModule();

            if (!(module == null || description.isEngaged(axisModule.getName()))) {
                // engages the module to AxisDescription
                description.engageModule(axisModule);
                // notifies the module about the engagement
                axisModule.getModule().engageNotify(description);
            }
        }
    }

    public AxisConfiguration getAxisConfiguration() {

        if (this instanceof AxisConfiguration) {
            return (AxisConfiguration) this;
        }

        if (this.parent != null) {
            return this.parent.getAxisConfiguration();
        }

        return null;
    }

    public abstract Object getKey();

    /**
     * Engage a Module at this level
     *
     * @param axisModule the Module to engage
     * @throws AxisFault if there's a problem engaging
     */
    public void engageModule(AxisModule axisModule) throws AxisFault {
        engageModule(axisModule, this);
    }

    /**
     * Engage a Module at this level, keeping track of which level the engage was originally
     * called from.  This is meant for internal use only.
     *
     * @param axisModule module to engage
     * @param source the AxisDescription which originally called engageModule()
     * @throws AxisFault if there's a problem engaging
     */
    public void engageModule(AxisModule axisModule, AxisDescription source) throws AxisFault {
        if (engagedModules == null) engagedModules = new HashMap();
        String moduleName = axisModule.getName();
        for (Iterator iterator = engagedModules.values().iterator(); iterator.hasNext();) {
            String existing = ((AxisModule)iterator.next()).getName();
            if (!Utils.checkVersion(moduleName, existing)) {
                throw new AxisFault(Messages.getMessage("mismatchedModuleVersions",
                        getClass().getName(),
                        moduleName,
                        existing));
            }
        }
        //We need to call engageNotify before we engage the module , then only
        // we can make sure everything is ok to engage the module to description (as an example policy)
        Module module = axisModule.getModule();
        if (module != null) {
            module.engageNotify(this);
        }
        // If we have anything specific to do, let that happen
        onEngage(axisModule, source);

        engagedModules.put(axisModule.getName(), axisModule);
    }

    protected void onEngage(AxisModule module, AxisDescription engager) throws AxisFault {
        // Default version does nothing, feel free to override
    }

    static Collection NULL_MODULES = new ArrayList(0);
    public Collection getEngagedModules() {
        return engagedModules == null ? NULL_MODULES : engagedModules.values();
    }

    /**
     * Check if a given module is engaged at this level.
     *
     * @param moduleName module to investigate.
     * @return true if engaged, false if not.
     * TODO: Handle versions?  isEngaged("addressing") should be true even for versioned modulename...
     */
    public boolean isEngaged(String moduleName) {
        if (engagedModules != null) {
            return engagedModules.keySet().contains(moduleName);
        }
        return false;
    }

    public void disengageModule(AxisModule module) throws AxisFault {
        if ((module == null) || (engagedModules == null)) return;
        String moduleName = module.getName();
        if (isEngaged(moduleName)) {
            onDisengage(module);
            engagedModules.remove(module.getName());
        }
    }

    protected void onDisengage(AxisModule module) throws AxisFault {
        // Base version does nothing
    }

    private Policy getApplicablePolicy(AxisDescription axisDescription) {

        if (axisDescription instanceof AxisOperation) {
            AxisOperation operation = (AxisOperation) axisDescription;
            AxisService service = (AxisService) operation.getParent();

            if (service != null) {

                AxisEndpoint axisEndpoint = service.getEndpoint(service.getEndpointName());

                AxisBinding axisBinding = null;

                if (axisEndpoint != null) {
                    axisBinding = axisEndpoint.getBinding();
                }

                AxisBindingOperation axisBindingOperation = null;

                if (axisBinding != null) {
                    axisBindingOperation = (AxisBindingOperation) axisBinding.getChild(operation.getName());
                }

                if (axisBindingOperation != null) {
                    return axisBindingOperation.getEffectivePolicy();
                }
            }

            return operation.getPolicyInclude().getEffectivePolicy();

        } else if (axisDescription instanceof AxisService) {
            AxisService service = (AxisService) axisDescription;

            AxisEndpoint axisEndpoint = service.getEndpoint(service.getEndpointName());
            AxisBinding axisBinding = null;

            if (axisEndpoint != null) {
                axisBinding = axisEndpoint.getBinding();
            }

            if (axisBinding != null) {
                return axisBinding.getEffectivePolicy();
            }

            return service.getPolicyInclude().getEffectivePolicy();

        } else {
            return axisDescription.getPolicyInclude().getEffectivePolicy();
        }
    }
}
