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

package org.apache.axis.engine;

import javax.xml.namespace.QName;

import org.apache.axis.engine.exec.ExecutionChain;
import org.apache.axis.engine.registry.FlowInclude;
import org.apache.axis.engine.registry.ModuleInclude;
import org.apache.axis.engine.registry.NamedEngineElement;
import org.apache.axis.engine.registry.TypeMappingInclude;

/**
 * Runtime representation of the WSDL Service
 */

public interface Service extends FlowInclude,NamedEngineElement,
    TypeMappingInclude,ModuleInclude{
    public Operation getOperation(QName index);
    public void addOperation(Operation op);
    public Provider getProvider();
    public Handler getSender();
    public void setProvider(Provider handler);    
    public ClassLoader getClassLoader();
    public void setClassLoader(ClassLoader cl); 
    /**
     * ExecutionChain gives all the Handlers (including the Global,  Transport, Service Handlers
     * This is resolved by the Deployment sub system at the deployment time)
     * @return ordered set of Handlers and Phases
     */
    public ExecutionChain getInputExecutionChain();
    public void setInputExecutionChain(ExecutionChain execChain);
    
    public ExecutionChain getOutputExecutionChain();
    public void setOutExecutionChain(ExecutionChain execChain);

    public ExecutionChain getFaultExecutionChain();
    public void setFaultExecutionChain(ExecutionChain execChain);

}
