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
package org.apache.axis.description;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.text.Style;
import javax.xml.namespace.QName;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Provider;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.impl.WSDLServiceImpl;


public class AxisService extends WSDLServiceImpl implements WSDLService,ParameterInclude,FlowInclude,PhasesInclude , DescriptionConstants{

    protected HashMap operationsMap = new HashMap();

    public AxisService(){
        this.setComponentProperty(MODULEREF_KEY,new ArrayList());
        this.setComponentProperty(PARAMETER_KEY, new ParameterIncludeImpl());
        this.setComponentProperty(PHASES_KEY, new PhasesIncludeImpl());
    }
    
    public AxisService(QName qName){
        this();
        this.setName(qName);        
    }
    
    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#addModule(javax.xml.namespace.QName)
     */
    public void addModule(QName moduleref) {
        if( null == moduleref)return;
       Collection collectionModule = (Collection) this.getComponentProperty(MODULEREF_KEY);
       collectionModule.add(moduleref);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getModules()
     */
    public Collection getModules() {
        return (Collection)this.getComponentProperty(MODULEREF_KEY);
    }

    public AxisOperation getOperation(QName operationName) {
        //todo The key has been changed from the qname to the local name because
        //todo when comparing the namespace will not be available
        return (AxisOperation)this.operationsMap.get(operationName.getLocalPart());
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#addOperation(org.apache.axis.description.AxisOperation)
     */
    public void addOperation(AxisOperation operation) {
        //todo The key has been changed from the qname to the local name because
        //todo when comparing the namespace will not be available
        if(null != operation){
            this.operationsMap.put(operation.getName().getLocalPart(),operation);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#setClassLoader(java.lang.ClassLoader)
     */
    public void setClassLoader(ClassLoader classLoader) {
        if(null != classLoader){
            this.setComponentProperty(CLASSLOADER_KEY, classLoader);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getClassLoader()
     */
    public ClassLoader getClassLoader() {
        return (ClassLoader)this.getComponentProperty(CLASSLOADER_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#setContextPath(java.lang.String)
     */
    public void setContextPath(String contextPath) {
        if(null != contextPath){
            this.setComponentProperty(CONTEXTPATH_KEY, contextPath);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getContextPath()
     */
    public String getContextPath() {
        return (String)this.getComponentProperty(CONTEXTPATH_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#setProvider(org.apache.axis.engine.Provider)
     */
    public void setProvider(Provider provider) {
        if(null != provider){
            this.setComponentProperty(PROVIDER_KEY, provider);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getProvider()
     */
    public Provider getProvider() {
        return (Provider)this.getComponentProperty(PROVIDER_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#setStyle(javax.swing.text.Style)
     */
    public void setStyle(Style style) {
        if(null != style){
            this.setComponentProperty(STYLE_KEY, style);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getStyle()
     */
    public Style getStyle() {
        return (Style)this.getComponentProperty(STYLE_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.PhasesInclude#getPhases(java.util.ArrayList, int)
     */
    public void setPhases(ArrayList phases, int flow) throws AxisFault{
        if(phases == null) return;
        PhasesIncludeImpl phaseInclude =
            (PhasesIncludeImpl)this.getComponentProperty(PHASES_KEY);
        phaseInclude.setPhases(phases, flow);
    }
    
    /* (non-Javadoc)
     * @see org.apache.axis.description.PhasesInclude#getPhases(int)
     */
    public ArrayList getPhases(int flow) throws AxisFault{
        PhasesIncludeImpl phaseInclude =
            (PhasesIncludeImpl)this.getComponentProperty(PHASES_KEY);
        return(ArrayList)phaseInclude.getPhases(flow);
    }
    
    /* (non-Javadoc)
     * @see org.apache.axis.description.ParameterInclude#addParameter(org.apache.axis.description.Parameter)
     */
    public void addParameter(Parameter param) {
        if(null == param) return;
        ParameterIncludeImpl paramInclude =
             (ParameterIncludeImpl)this.getComponentProperty(PARAMETER_KEY);
        paramInclude.addParameter(param);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.ParameterInclude#getParameter(java.lang.String)
     */
    public Parameter getParameter(String name) {
        ParameterIncludeImpl paramInclude = (ParameterIncludeImpl)this.getComponentProperty(PARAMETER_KEY);
        return (Parameter)paramInclude.getParameter(name);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.FlowInclude#getInFlow()
     */
    public Flow getInFlow() {
        return (Flow)this.getComponentProperty(INFLOW_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.FlowInclude#setInFlow(org.apache.axis.description.Flow)
     */
    public void setInFlow(Flow inFlow) {
        if(null != inFlow){
            this.setComponentProperty(INFLOW_KEY, inFlow);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.FlowInclude#getOutFlow()
     */
    public Flow getOutFlow() {
        return (Flow)this.getComponentProperty(OUTFLOW_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.FlowInclude#setOutFlow(org.apache.axis.description.Flow)
     */
    public void setOutFlow(Flow outFlow){
        if(null != outFlow){
            this.setComponentProperty(OUTFLOW_KEY, outFlow);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.FlowInclude#getFaultFlow()
     */
    public Flow getFaultFlow() {
        return (Flow)this.getComponentProperty(FAULTFLOW_KEY);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.description.FlowInclude#setFaultFlow(org.apache.axis.description.Flow)
     */
    public void setFaultFlow(Flow faultFlow) {
        if(null != faultFlow){
            this.setComponentProperty(FAULTFLOW_KEY, faultFlow);
        }
    }
    
    public void setServiceClass(Class serviceclass) {
        if(serviceclass != null)
            this.setComponentProperty(DescriptionConstants.SERVICE_CLASS, serviceclass);
    }

    public Class getServiceClass() {
        return (Class)this.getComponentProperty(DescriptionConstants.SERVICE_CLASS);
    }

    

}
