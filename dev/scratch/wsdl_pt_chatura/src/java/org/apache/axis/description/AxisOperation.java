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

import org.apache.axis.registry.Flow;
import org.apache.axis.registry.Parameter;
import org.apache.wsdl.wom.WSDLInterface;

/**
 * @author chathura@opensource.lk
 *
 */
public interface AxisOperation extends WSDLInterface {

    public void addParameter(Parameter param);
    
    public Parameter[] getparameters();
    
    public Object getParameterValue(String name);
    
    public Flow getInFlow();
    
    public void setInflow(Flow inFlow);
    
    public Flow getOutFlow();
    
    public void setOutFlow(Flow outFlow);
    
    public Flow getFaultFlow();
    
    public void setFaultFlow();
    //FIXME please do the Class for ExecutableChain and change all teh *Object*
    public void setExecutableInChain(Object executableInChain);
    
    public Object getExecutableInChain();
    
    public void setExecutableOutChain(Object executableOutChain);
    
    public Object getExecutableOutChain();
        
    public void setExecutableFaultChain(Object executableFaultChain);
    
    public Object getExecutableFaultChain();
    
}
