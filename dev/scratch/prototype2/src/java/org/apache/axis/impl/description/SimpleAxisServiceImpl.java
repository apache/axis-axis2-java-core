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
package org.apache.axis.impl.description;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.text.Style;
import javax.xml.namespace.QName;

import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.Flow;
import org.apache.axis.description.FlowInclude;
import org.apache.axis.description.Parameter;
import org.apache.axis.description.ParameterInclude;
import org.apache.axis.engine.ExecutionChain;
import org.apache.axis.engine.Provider;
import org.apache.wsdl.wom.WSDLEndpoint;
import org.apache.wsdl.wom.WSDLInterface;
import org.apache.wsdl.wom.WSDLService;
import org.apache.wsdl.wom.impl.WSDLServiceImpl;

public class SimpleAxisServiceImpl implements AxisService {
    protected WSDLService wsdlService;
    protected FlowInclude flowInclude;
    protected String contextPath;
    protected Provider provider;
    protected Style style;
    protected AxisOperation Operation;
    protected ExecutionChain executableInChain;
    protected ExecutionChain executableOutChain;
    protected ExecutionChain executableFaultChain;
    protected Vector modules;
    protected HashMap operations;
    protected ParameterInclude parameters;
    protected ClassLoader classLoader;
    
    /**
     * 
     */
    public SimpleAxisServiceImpl(QName name) {
        modules = new Vector();
        operations = new HashMap();
        wsdlService = new WSDLServiceImpl();
        wsdlService.setName(name);
        flowInclude = new FlowIncludeImpl();
        parameters = new ParameterIncludeImpl();
    }

    /**
     * @return
     */
    public WSDLInterface getServiceInterface() {
        return wsdlService.getServiceInterface();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return wsdlService.hashCode();
    }

    /**
     * @param endpoint
     * @param nCName
     */
    public void setEndpoint(WSDLEndpoint endpoint, String nCName) {
        wsdlService.setEndpoint(endpoint, nCName);
    }

    /**
     * @return
     */
    public String getNamespace() {
        return wsdlService.getNamespace();
    }

    /**
     * @param name
     */
    public void setName(QName name) {
        wsdlService.setName(name);
    }

    public String toString() {
        return wsdlService.toString();
    }

    /**
     * @param endpoints
     */
    public void setEndpoints(HashMap endpoints) {
        wsdlService.setEndpoints(endpoints);
    }

    public boolean equals(Object obj) {
        return wsdlService.equals(obj);
    }


    /**
     * @return
     */
    public QName getName() {
        return wsdlService.getName();
    }

    /**
     * @param serviceInterface
     */
    public void setServiceInterface(WSDLInterface serviceInterface) {
        wsdlService.setServiceInterface(serviceInterface);
    }


    /**
     * @return
     */
    public HashMap getEndpoints() {
        return wsdlService.getEndpoints();
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

    public void addModule(QName moduleref) {
        modules.add(moduleref);
    }

    public void addOperation(AxisOperation operation) {
        if(operation != null){
            operations.put(operation.getName(),operation);
        }    
    }


    public Collection getModules() {
        return modules;
    }

    public AxisOperation getOperation(QName operationName) {
        return (AxisOperation)operations.get(operationName);
    }


    /**
     * @return
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * @return
     */
    public ExecutionChain getExecutableFaultChain() {
        return executableFaultChain;
    }

    /**
     * @return
     */
    public ExecutionChain getExecutableInChain() {
        return executableInChain;
    }

    /**
     * @return
     */
    public ExecutionChain getExecutableOutChain() {
        return executableOutChain;
    }

    /**
     * @return
     */
    public Style getStyle() {
        return style;
    }

    /**
     * @param string
     */
    public void setContextPath(String string) {
        contextPath = string;
    }

    /**
     * @param chain
     */
    public void setExecutableFaultChain(ExecutionChain chain) {
        executableFaultChain = chain;
    }

    /**
     * @param chain
     */
    public void setExecutableInChain(ExecutionChain chain) {
        executableInChain = chain;
    }

    /**
     * @param chain
     */
    public void setExecutableOutChain(ExecutionChain chain) {
        executableOutChain = chain;
    }

    /**
     * @param style
     */
    public void setStyle(Style style) {
        this.style = style;
    }

    /**
     * @return
     */
    public Provider getProvider() {
        return provider;
    }

    /**
     * @param provider
     */
    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    /**
     * @return
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * @param loader
     */
    public void setClassLoader(ClassLoader loader) {
        classLoader = loader;
    }

    /**
     * @param param
     */
    public void addParameter(Parameter param) {
        parameters.addParameter(param);
    }

    /**
     * @param name
     * @return
     */
    public Parameter getParameter(String name) {
        return parameters.getParameter(name);
    }

    public WSDLService getEndpoint(String nCName) {
        return wsdlService.getEndpoint(nCName);
    }

    public HashMap getComponentProperties() {
        return wsdlService.getComponentProperties();
    }

    public Object getComponentProperty(Object key) {
        return wsdlService.getComponentProperty(key); 
    }

    public void setComponentProperties(HashMap properties) {
        wsdlService.setComponentProperties(properties);

    }

    public void setComponentProperty(Object key, Object obj) {
        wsdlService.setComponentProperty(key,obj); 
    }

}
