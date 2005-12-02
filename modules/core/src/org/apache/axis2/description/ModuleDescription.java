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

import org.apache.axis2.AxisFault;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.modules.Module;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>This holds the information about a Module. </p>
 * <ol>
 * <li>parameters<li>
 * <li>handlers<li>
 * <ol>
 * <p>Handler are registered once they are avlible but they avalibe to all services if axis2.xml
 * has a module ref="." or avalible to a single service if services.xml have module ref=".."</p>
 */
public class ModuleDescription implements FlowInclude, ParameterInclude {

    private Module module;
    /**
     * Field name
     */
    private QName name;

    private AxisConfiguration parent;

    private ClassLoader moduleClassLoader;

    /**
     * Field flowInclude
     */
    private final FlowInclude flowInclude = new FlowIncludeImpl();

    //to store module operations , which are suppose to be added to a service if it is engaged to a service
    private HashMap operations;

    /**
     * Field parameters
     */
    private final ParameterInclude parameters = new ParameterIncludeImpl();

    /**
     * Constructor ModuleDescription
     */
    public ModuleDescription() {
        operations = new HashMap();
    }

    /**
     * Constructor ModuleDescription
     *
     * @param name
     */
    public ModuleDescription(QName name) {
        this();
        this.name = name;
    }

    /**
     * @return
     */
    public Flow getFaultInFlow() {
        return flowInclude.getFaultInFlow();
    }

    public Flow getFaultOutFlow() {
        return flowInclude.getFaultOutFlow();
    }

    /**
     * @return
     */
    public Flow getInFlow() {
        return flowInclude.getInFlow();
    }

    /**
     * @return
     */
    public Flow getOutFlow() {
        return flowInclude.getOutFlow();
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
     * @param outFlow
     */
    public void setOutFlow(Flow outFlow) {
        flowInclude.setOutFlow(outFlow);
    }

    /**
     * @param param
     */
    public void addParameter(Parameter param)throws AxisFault{
        if(isParameterLocked(param.getName())){
            throw new AxisFault("Parmter is locked can not overide: " + param.getName());
        } else{
            parameters.addParameter(param);
        }
    }

    /**
     * @param name
     * @return
     */
    public Parameter getParameter(String name) {
        return parameters.getParameter(name);
    }

    public ArrayList getParameters() {
        return parameters.getParameters();
    }

    /**
     * @return
     */
    public QName getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(QName name) {
        this.name = name;
    }

    /**
     * @return
     */
    public Module getModule() {
        return module;
    }

    /**
     * @param module
     */
    public void setModule(Module module) {
        this.module = module;
    }

    public void addOperation(AxisOperation axisOperation) {
        operations.put(axisOperation.getName(), axisOperation);
    }

    public HashMap getOperations() {
        return operations;
    }

    public AxisConfiguration getParent() {
        return parent;
    }

    public void setParent(AxisConfiguration parent) {
        this.parent = parent;
    }

    //to check whether a given parameter is locked
    public boolean isParameterLocked(String parameterName) {
        // checking the locked value of parent
          boolean loscked =  false;
        if (getParent() !=null) {
            loscked=    getParent().isParameterLocked(parameterName);
        }
        if(loscked){
            return true;
        } else {
            Parameter parameter = getParameter(parameterName);
            if(parameter != null && parameter.isLocked()){
                return true;
            } else {
                return false;
            }
        }
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        this.parameters.deserializeParameters(parameterElement);
    }

    public ClassLoader getModuleClassLoader() {
        return moduleClassLoader;
    }

    public void setModuleClassLoader(ClassLoader moduleClassLoader) {
        this.moduleClassLoader = moduleClassLoader;
    }

}
