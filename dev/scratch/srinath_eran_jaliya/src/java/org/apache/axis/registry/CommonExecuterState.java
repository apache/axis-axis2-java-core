/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.registry;

import javax.xml.namespace.QName;

/**
 * @author hemapani@opensource.lk
 */
public class CommonExecuterState {
    //the work is delegated to ConcreateXXInclude classes                       
    protected FlowInclude flowInclude;
    protected ModuleInclude modules;
    protected TypeMappingInclude typemappings;

    /**
     * 
     */
    public CommonExecuterState() {

        flowInclude = new ConcreateFlowInclude();
        modules = new ConcreateModuleInclude();
        typemappings = new ConcreateTypeMappingInclude();
    }
    /**
     * @return
     */
    public Flow getFaultFlow() {
        return flowInclude.getFaultFlow();
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
     * @param flow
     */
    public synchronized void setFaultFlow(Flow flow) {
        flowInclude.setFaultFlow(flow);
    }

    /**
     * @param flow
     */
    public synchronized void setInFlow(Flow flow) {
        flowInclude.setInFlow(flow);
    }

    /**
     * @param flow
     */
    public synchronized void setOutFlow(Flow flow) {
        flowInclude.setOutFlow(flow);
    }

    /**
     * @param typeMapping
     */
    public void addTypeMapping(TypeMapping typeMapping) {
        typemappings.addTypeMapping(typeMapping);
    }

    /**
     * @param javaType
     * @return
     */
    public TypeMapping getTypeMapping(Class javaType) {
        return typemappings.getTypeMapping(javaType);
    }

    /**
     * @param index
     * @return
     */
    public TypeMapping getTypeMapping(int index) {
        return typemappings.getTypeMapping(index);
    }

    /**
     * @param xmlType
     * @return
     */
    public TypeMapping getTypeMapping(QName xmlType) {
        return typemappings.getTypeMapping(xmlType);
    }

    /**
     * @return
     */
    public int getTypeMappingCount() {
        return typemappings.getTypeMappingCount();
    }

    /**
     * @param module
     */
    public synchronized void addModule(Module module) {
        modules.addModule(module);
    }

    /**
     * @param index
     * @return
     */
    public Module getModule(int index) {
        return modules.getModule(index);
    }

    /**
     * @return
     */
    public int getModuleCount() {
        return modules.getModuleCount();
    }
    

}
