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
package org.apache.axis.impl.description;

import java.util.Collection;
import java.util.ArrayList;

import javax.swing.text.Style;
import javax.xml.namespace.QName;

import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.description.DescriptionConstants;
import org.apache.axis.description.Flow;
import org.apache.axis.description.Parameter;
import org.apache.axis.engine.ExecutionChain;
import org.apache.axis.engine.Provider;
import org.apache.wsdl.impl.WSDLServiceImpl;


public class AxisServiceImpl extends WSDLServiceImpl implements AxisService , DescriptionConstants{

    public AxisServiceImpl(){
        this.setComponentProperty(MODULEREF_KEY,new ArrayList());
        this.setComponentProperty(PARAMETER_KEY, new ParameterIncludeImpl());
        this.setComponentProperty(PHASES_KEY, new PhasesIncludeImpl());
    }
    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#addModule(javax.xml.namespace.QName)
     */
    public void addModule(QName moduleref) {
        // TODO Auto-generated method stub
        if( null == moduleref)return;

       Collection collectionModule = (Collection) this.getComponentProperty(MODULEREF_KEY);
       if (null != collectionModule){
               collectionModule.add(moduleref);
       }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getModules()
     */
    public Collection getModules() {
        // TODO Auto-generated method stub
        return (Collection)this.getComponentProperty(MODULEREF_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#setExecutableInChain(org.apache.axis.engine.ExecutionChain)
     */
    public void setExecutableInChain(ExecutionChain executableInChain) {
        if(null !=executableInChain){
            this.setComponentProperty(EXECUTION_CHAIN_KEY, executableInChain);
        }

    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getExecutableInChain()
     */
    public ExecutionChain getExecutableInChain() {
        return (ExecutionChain)this.getComponentProperty(EXECUTION_CHAIN_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#setExecutableOutChain(org.apache.axis.engine.ExecutionChain)
     */
    public void setExecutableOutChain(ExecutionChain executableOutChain) {
        // TODO Auto-generated method stub
        if(null !=executableOutChain){
            this.setComponentProperty(EXECUTION_OUT_CHAIN_KEY, executableOutChain);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getExecutableOutChain()
     */
    public ExecutionChain getExecutableOutChain() {
        // TODO Auto-generated method stub
        return (ExecutionChain)this.getComponentProperty(EXECUTION_OUT_CHAIN_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#setExecutableFaultChain(org.apache.axis.engine.ExecutionChain)
     */
    public void setExecutableFaultChain(ExecutionChain executableFaultChain) {
        // TODO Auto-generated method stub
        if(null !=executableFaultChain){
            this.setComponentProperty(EXECUTION_FAULT_CHAIN_KEY, executableFaultChain);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getExecutableFaultChain()
     */
    public ExecutionChain getExecutableFaultChain() {
        // TODO Auto-generated method stub
        return (ExecutionChain)this.getComponentProperty(EXECUTION_FAULT_CHAIN_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getOperation(javax.xml.namespace.QName)
     */
    public AxisOperation getOperation(QName operationName) {
        // TODO Auto-generated method stub
        return (AxisOperation)this.getComponentProperty(OPERATION_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#addOperation(org.apache.axis.description.AxisOperation)
     */
    public void addOperation(AxisOperation operationName) {
        // TODO Auto-generated method stub
        if(null != operationName){
            this.setComponentProperty(OPERATION_KEY, operationName);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#setClassLoader(java.lang.ClassLoader)
     */
    public void setClassLoader(ClassLoader classLoader) {
        // TODO Auto-generated method stub
        if(null != classLoader){
            this.setComponentProperty(CLASSLOADER_KEY, classLoader);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getClassLoader()
     */
    public ClassLoader getClassLoader() {
        // TODO Auto-generated method stub
        return (ClassLoader)this.getComponentProperty(CLASSLOADER_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#setContextPath(java.lang.String)
     */
    public void setContextPath(String contextPath) {
        // TODO Auto-generated method stub
        if(null != contextPath){
            this.setComponentProperty(CONTEXTPATH_KEY, contextPath);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getContextPath()
     */
    public String getContextPath() {
        // TODO Auto-generated method stub
        return (String)this.getComponentProperty(CONTEXTPATH_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#setProvider(org.apache.axis.engine.Provider)
     */
    public void setProvider(Provider provider) {
        // TODO Auto-generated method stub
        if(null != provider){
            this.setComponentProperty(PROVIDER_KEY, provider);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getProvider()
     */
    public Provider getProvider() {
        // TODO Auto-generated method stub
        return (Provider)this.getComponentProperty(PROVIDER_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#setStyle(javax.swing.text.Style)
     */
    public void setStyle(Style style) {
        // TODO Auto-generated method stub
        if(null != style){
            this.setComponentProperty(STYLE_KEY, style);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getStyle()
     */
    public Style getStyle() {
        // TODO Auto-generated method stub
        return (Style)this.getComponentProperty(STYLE_KEY);
    }

    public void setPhases(ArrayList phases, int flow) throws AxisFault{
        if(phases == null) return;
        PhasesIncludeImpl phaseInclude =
            (PhasesIncludeImpl)this.getComponentProperty(PHASES_KEY);
        if(phaseInclude != null){
            phaseInclude.setPhases(phases, flow);
        }
    }

    public ArrayList getPhases(int flow) throws AxisFault{
        PhasesIncludeImpl phaseInclude =
            (PhasesIncludeImpl)this.getComponentProperty(PHASES_KEY);
        if(phaseInclude == null) return null;
        return(ArrayList)phaseInclude.getPhases(flow);
    }
    /* (non-Javadoc)
     * @see org.apache.axis.description.ParameterInclude#addParameter(org.apache.axis.description.Parameter)
     */
    public void addParameter(Parameter param) {
        // TODO Auto-generated method stub
        if(null == param) return;
        ParameterIncludeImpl paramInclude =
             (ParameterIncludeImpl)this.getComponentProperty(PARAMETER_KEY);
        if(null != paramInclude){
            paramInclude.addParameter(param);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.ParameterInclude#getParameter(java.lang.String)
     */
    public Parameter getParameter(String name) {
        // TODO Auto-generated method stub
        ParameterIncludeImpl paramInclude = (ParameterIncludeImpl)this.getComponentProperty(PARAMETER_KEY);
        if(null == paramInclude)return null;
        return (Parameter)paramInclude.getParameter(name);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.FlowInclude#getInFlow()
     */
    public Flow getInFlow() {
        // TODO Auto-generated method stub
        return (Flow)this.getComponentProperty(INFLOW_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.FlowInclude#setInFlow(org.apache.axis.description.Flow)
     */
    public void setInFlow(Flow inFlow) {
        // TODO Auto-generated method stub
        if(null != inFlow){
            this.setComponentProperty(INFLOW_KEY, inFlow);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.FlowInclude#getOutFlow()
     */
    public Flow getOutFlow() {
        // TODO Auto-generated method stub
        return (Flow)this.getComponentProperty(OUTFLOW_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.FlowInclude#setOutFlow(org.apache.axis.description.Flow)
     */
    public void setOutFlow(Flow outFlow) {
        // TODO Auto-generated method stub
        if(null != outFlow){
            this.setComponentProperty(OUTFLOW_KEY, outFlow);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.FlowInclude#getFaultFlow()
     */
    public Flow getFaultFlow() {
        // TODO Auto-generated method stub
        return (Flow)this.getComponentProperty(FAULTFLOW_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.FlowInclude#setFaultFlow(org.apache.axis.description.Flow)
     */
    public void setFaultFlow(Flow faultFlow) {
        // TODO Auto-generated method stub
        if(null != faultFlow){
            this.setComponentProperty(FAULTFLOW_KEY, faultFlow);
        }
    }
}
