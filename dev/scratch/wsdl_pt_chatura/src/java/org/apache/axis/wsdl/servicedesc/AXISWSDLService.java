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
package org.apache.axis.wsdl.servicedesc;

import javax.xml.namespace.QName;

import org.apache.axis.deployment.MetaData.BeanMappingMetaData;
import org.apache.axis.engine.Operation;
import org.apache.axis.engine.Provider;
import org.apache.axis.engine.registry.Flow;
import org.apache.axis.engine.registry.Parameter;
import org.apache.axis.engine.registry.TypeMapping;
import org.apache.wsdl.wom.WSDLService;

/**
 * @author chathura@opensource.lk
 *
 */
public interface AXISWSDLService extends WSDLService{
    public void addModules(QName moduleref);

    public void addParameters(Parameter parameter);

    public void setInFlow(Flow inflow);

    public Flow getInFlow();
    
    public void setOutFlow(Flow outflow);

    public Flow getOutFlow();

    public void setFaultFlow(Flow faultflow);

    public Flow getFaultFlow();

    public void setClassLoader(ClassLoader classloader);

    public void setTypeMapping(TypeMapping typemapping);

    public void setBeanMapping(BeanMappingMetaData beanmapping);

    public void setProvider(Provider provider);

    public void setStyle(String style);

    public void setContextPath(String contextpath);

    public Operation getOperation();

    public void setOperation(Operation operation);

}
