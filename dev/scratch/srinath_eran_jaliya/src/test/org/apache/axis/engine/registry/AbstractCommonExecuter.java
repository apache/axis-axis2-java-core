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
import org.apache.axis.registry.CommonExecuterState;
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
    protected CommonExecuterState state;

    public AbstractCommonExecuter(CommonExecuterState state){
        invoker = new HandlerInvoker(this);
        this.state = state;
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
     * @param module
     */
    public void addModule(Module module) {
        state.addModule(module);
    }

    /**
     * @param typeMapping
     */
    public void addTypeMapping(TypeMapping typeMapping) {
        state.addTypeMapping(typeMapping);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return state.equals(obj);
    }

    /**
     * @return
     */
    public Flow getFaultFlow() {
        return state.getFaultFlow();
    }

    /**
     * @return
     */
    public Flow getInFlow() {
        return state.getInFlow();
    }

    /**
     * @param index
     * @return
     */
    public Module getModule(int index) {
        return state.getModule(index);
    }

    /**
     * @return
     */
    public int getModuleCount() {
        return state.getModuleCount();
    }

    /**
     * @return
     */
    public Flow getOutFlow() {
        return state.getOutFlow();
    }

    /**
     * @param javaType
     * @return
     */
    public TypeMapping getTypeMapping(Class javaType) {
        return state.getTypeMapping(javaType);
    }

    /**
     * @param index
     * @return
     */
    public TypeMapping getTypeMapping(int index) {
        return state.getTypeMapping(index);
    }

    /**
     * @param xmlType
     * @return
     */
    public TypeMapping getTypeMapping(QName xmlType) {
        return state.getTypeMapping(xmlType);
    }

    /**
     * @return
     */
    public int getTypeMappingCount() {
        return state.getTypeMappingCount();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return state.hashCode();
    }

    /**
     * @param flow
     */
    public void setFaultFlow(Flow flow) {
        state.setFaultFlow(flow);
    }

    /**
     * @param flow
     */
    public void setInFlow(Flow flow) {
        state.setInFlow(flow);
    }

    /**
     * @param flow
     */
    public void setOutFlow(Flow flow) {
        state.setOutFlow(flow);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return state.toString();
    }

}
