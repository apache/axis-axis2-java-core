/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.engine.registry;

import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.CommonExecutor;
import org.apache.axis.engine.HandlerInvoker;
import org.apache.axis.engine.MessageContext;
import org.apache.axis.registry.AbstractEngineElement;
import org.apache.axis.registry.ConcreateFlowInclude;
import org.apache.axis.registry.ConcreateModuleInclude;
import org.apache.axis.registry.ConcreateTypeMappingInclude;
import org.apache.axis.registry.EngineElement;
import org.apache.axis.registry.Flow;
import org.apache.axis.registry.FlowInclude;
import org.apache.axis.registry.Module;
import org.apache.axis.registry.ModuleInclude;
import org.apache.axis.registry.TypeMapping;
import org.apache.axis.registry.TypeMappingInclude;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class use delegeation to give the multiple inheritance effect 
 * which is prefered over the generalization. 
 * @author (Srinath Perera)hemapani@opensource.lk
 */
public abstract class AbstractCommonExecuter 
            extends AbstractEngineElement 
            implements FlowInclude,EngineElement,
                TypeMappingInclude,CommonExecutor,ModuleInclude {
    private Log log = LogFactory.getLog(getClass());                    

    protected HandlerInvoker invoker;    
    
    //the work is delegated to ConcreateXXInclude classes                       
    protected FlowInclude flowInclude;
    protected ModuleInclude modules;
    protected TypeMappingInclude typemappings;

    public AbstractCommonExecuter(){
        invoker = new HandlerInvoker(this);
        flowInclude = new ConcreateFlowInclude();
        modules = new ConcreateModuleInclude();
        typemappings = new ConcreateTypeMappingInclude();
    }
    


    public void processFaultFlow(MessageContext mc) throws AxisFault {
        invoker.addFlow(getFaultFlow());
        int moduleCount = getModuleCount();
        for(int i = 0;i<moduleCount;i++){
            Module module = getModule(i);
            invoker.addFlow(module.getFaultFlow());
        }
        invoker.orderTheHandlers();
        invoker.invoke(mc);
    }

    
    public void recive(MessageContext mc) throws AxisFault {
        invoker.addFlow(getInFlow());
        int moduleCount = getModuleCount();
        for(int i = 0;i<moduleCount;i++){
            Module module = getModule(i);
            invoker.addFlow(module.getInFlow());
        }
        invoker.orderTheHandlers();
        invoker.invoke(mc);
        log.info("invoked "+getClass());
    }

    public void rollback(MessageContext mc) throws AxisFault {
        invoker.revoke(mc);
    }

    public void send(MessageContext mc) throws AxisFault {
        invoker.addFlow(getOutFlow());
        int moduleCount = getModuleCount();
        for(int i = 0;i<moduleCount;i++){
            Module module = getModule(i);
            invoker.addFlow(module.getOutFlow());
        }
        invoker.orderTheHandlers();
        invoker.invoke(mc);
        log.info("invoked "+getClass());
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
    public void setFaultFlow(Flow flow) {
        flowInclude.setFaultFlow(flow);
    }

    /**
     * @param flow
     */
    public void setInFlow(Flow flow) {
        flowInclude.setInFlow(flow);
    }

    /**
     * @param flow
     */
    public void setOutFlow(Flow flow) {
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
    public void addModule(Module module) {
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
