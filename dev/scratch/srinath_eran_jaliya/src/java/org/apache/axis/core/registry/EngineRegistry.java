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

package org.apache.axis.core.registry;

import javax.xml.namespace.QName;

import org.apache.axis.core.AxisFault;
import org.apache.axis.core.Global;
import org.apache.axis.core.Service;
import org.apache.axis.core.Transport;

/**
 *  The palce where all the Globel states of Axis is kept. 
 *  All the Global states kept in the <code>EngineRegistry</code> and all the 
 *  Service states kept in the <code>MessageContext</code>. Other runtime
 *  artifacts does not keep states foward from the execution.  
 */

public interface EngineRegistry {
    public Global getGlobal()throws AxisFault;
    
    public int getTransportCount()throws AxisFault;
    public Transport getTransPort(QName name)throws AxisFault;
    public Transport getTransPort(int index)throws AxisFault;
    public void addTransport(Transport transport)throws AxisFault;
    public void removeTransport(QName name)throws AxisFault;
    
    public int getServiceCount()throws AxisFault;
    public Service getService(QName name)throws AxisFault;
    public Service getService(int index)throws AxisFault;
    public void addService(Service service)throws AxisFault;
    public void removeService(QName name)throws AxisFault;
    
    public int getModuleCount()throws AxisFault;
    public Module getModule(QName name)throws AxisFault;
    public Module getModule(int index)throws AxisFault;
    public void addMdoule(Module module)throws AxisFault;
}
