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

import org.apache.axis.engine.Provider;
import org.apache.wsdl.WSDLService;

import javax.swing.text.Style;
import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * <p>represent a implemented web service</p>
 */
public interface AxisService extends WSDLService,ParameterInclude,FlowInclude,PhasesInclude {
    //modules
    public void addModule(QName moduleref);
    public Collection getModules();
    public AxisOperation getOperation(QName operationName);
    public void addOperation(AxisOperation operationName);
    

    public void setClassLoader(ClassLoader classLaoder);
    public ClassLoader getClassLoader();
    
   
    public void setContextPath(String contextPath);
    public String getContextPath();

    //provider    
    public void setProvider(Provider provider);
    public Provider getProvider();

    //style    
    public void setStyle(Style style);
    public Style getStyle();

    public void setServiceClass(Class serviceclass);
    public Class getServiceClass();

    public void setServiceClassName(String className);
    public String getServiceClassName();


}
