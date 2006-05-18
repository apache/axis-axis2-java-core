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
import org.apache.axis2.modules.Module;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.URI;
import java.net.URL;

/**
 * <p>This holds the information about a Module. </p>
 * <ol>
 * <li>parameters<li>
 * <li>handlers<li>
 * <ol>
 * <p>Handler are registered once they are available. They are available to all services if axis2.xml
 * has a module ref="." or avalible to a single service if services.xml have module ref=".."</p>
 */
public class AxisModule implements FlowInclude, ParameterInclude {

    /**
     * Field flowInclude
     */
    private final FlowInclude flowInclude = new FlowIncludeImpl();

    /**
     * Field parameters
     */
    private final ParameterInclude parameters = new ParameterIncludeImpl();
    private Module module;
    private ClassLoader moduleClassLoader;
    // To keep the File that moduel came from
    private URL fileName;

    /**
     * Field name
     */
    private QName name;

    // to store module operations , which are suppose to be added to a service if it is engaged to a service
    private HashMap operations;
    private AxisConfiguration parent;

    /*
    * to store policies which are falid for any service for which the module is
    */
    private PolicyInclude policyInclude = null;

    // Small description about the module
    private String moduleDescription;

    private String[] supportedPolicyNames;

    /**
     * Constructor ModuleDescription.
     */
    public AxisModule() {
        operations = new HashMap();
    }

    /**
     * Constructor ModuleDescription.
     *
     * @param name
     */
    public AxisModule(QName name) {
        this();
        this.name = name;
    }

    public void addOperation(AxisOperation axisOperation) {
        operations.put(axisOperation.getName(), axisOperation);
    }

    /**
     * @param param
     */
    public void addParameter(Parameter param) throws AxisFault {
        if (isParameterLocked(param.getName())) {
            throw new AxisFault(Messages.getMessage("paramterlockedbyparent", param.getName()));
        } else {
            parameters.addParameter(param);
        }
    }

    public void removeParameter(Parameter param) throws AxisFault {
        if (isParameterLocked(param.getName())) {
            throw new AxisFault(Messages.getMessage("paramterlockedbyparent", param.getName()));
        } else {
            parameters.removeParameter(param);
        }
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        this.parameters.deserializeParameters(parameterElement);
    }

    /**
     * @return Returns Flow.
     */
    public Flow getFaultInFlow() {
        return flowInclude.getFaultInFlow();
    }

    public Flow getFaultOutFlow() {
        return flowInclude.getFaultOutFlow();
    }

    /**
     * @return Returns Flow.
     */
    public Flow getInFlow() {
        return flowInclude.getInFlow();
    }

    /**
     * @return Returns Module.
     */
    public Module getModule() {
        return module;
    }

    public ClassLoader getModuleClassLoader() {
        return moduleClassLoader;
    }

    /**
     * @return Returns QName.
     */
    public QName getName() {
        return name;
    }

    public HashMap getOperations() {
        return operations;
    }

    /**
     * @return Returns Flow.
     */
    public Flow getOutFlow() {
        return flowInclude.getOutFlow();
    }

    /**
     * @param name
     * @return Returns Parameter.
     */
    public Parameter getParameter(String name) {
        return parameters.getParameter(name);
    }

    public ArrayList getParameters() {
        return parameters.getParameters();
    }

    public AxisConfiguration getParent() {
        return parent;
    }

    // to check whether a given parameter is locked
    public boolean isParameterLocked(String parameterName) {

        // checking the locked value of parent
        boolean loscked = false;

        if (getParent() != null) {
            loscked = getParent().isParameterLocked(parameterName);
        }

        if (loscked) {
            return true;
        } else {
            Parameter parameter = getParameter(parameterName);

            return (parameter != null) && parameter.isLocked();
        }
    }

    /**
     * @param faultFlow
     */
    public void setFaultInFlow(Flow faultFlow) {
        flowInclude.setFaultInFlow(faultFlow);
    }

    /**
     * @param faultFlow
     */
    public void setFaultOutFlow(Flow faultFlow) {
        flowInclude.setFaultOutFlow(faultFlow);
    }

    /**
     * @param inFlow
     */
    public void setInFlow(Flow inFlow) {
        flowInclude.setInFlow(inFlow);
    }

    /**
     * @param module
     */
    public void setModule(Module module) {
        this.module = module;
    }

    public void setModuleClassLoader(ClassLoader moduleClassLoader) {
        this.moduleClassLoader = moduleClassLoader;
    }

    /**
     * @param name
     */
    public void setName(QName name) {
        this.name = name;
    }

    /**
     * @param outFlow
     */
    public void setOutFlow(Flow outFlow) {
        flowInclude.setOutFlow(outFlow);
    }

    public void setParent(AxisConfiguration parent) {
        this.parent = parent;
    }

    public void setPolicyInclude(PolicyInclude policyInclude) {
        this.policyInclude = policyInclude;
    }

    public PolicyInclude getPolicyInclude() {
        if(policyInclude == null) {
            policyInclude = new PolicyInclude();
        }
        return policyInclude;
    }

    public String getModuleDescription() {
        return moduleDescription;
    }

    public void setModuleDescription(String moduleDescription) {
        this.moduleDescription = moduleDescription;
    }

    public String[] getSupportedPolicyNamespaces() {
        return supportedPolicyNames;
    }

    public void setSupportedPolicyNamespaces(String[] supportedPolicyNamespaces) {
        this.supportedPolicyNames = supportedPolicyNamespaces;
    }

    public URL getFileName() {
        return fileName;
    }

    public void setFileName(URL fileName) {
        this.fileName = fileName;
    }
}
