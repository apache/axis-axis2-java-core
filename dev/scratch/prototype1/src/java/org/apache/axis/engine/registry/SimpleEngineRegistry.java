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

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Global;
import org.apache.axis.engine.Service;
import org.apache.axis.engine.Transport;
import org.apache.axis.utils.HashedBaundle;

public class SimpleEngineRegistry implements EngineRegistry{
    private HashedBaundle modules;
    private HashedBaundle transports;
    private HashedBaundle services;
    private Global global;
    
    public SimpleEngineRegistry(Global global){
        this.modules = new HashedBaundle();
        this.transports = new HashedBaundle();
        this.services = new HashedBaundle();
        this.global = global;
    
    }
    public Global getGlobal() throws AxisFault {
        return global;
    }

    public Module getModule(int index) throws AxisFault {
        return (Module)modules.get(index);
    }

    public Module getModule(QName name) throws AxisFault {
        return (Module)modules.get(name);
    }

    public int getModuleCount() throws AxisFault {
        return modules.getCount();
    }

    public ArrayList getPahsesInOrder() throws AxisFault {
        // TODO Auto-generated method stub
        return null;
    }

    public Service getService(int index) throws AxisFault {
        return (Service)services.get(index);
    }

    public Service getService(QName name) throws AxisFault {
        return (Service)services.get(name);
    }

    public int getServiceCount() throws AxisFault {
        return services.getCount();
    }

    public Transport getTransPort(int index) throws AxisFault {
        return (Transport)transports.get(index);
    }

    public Transport getTransPort(QName name) throws AxisFault {
        return (Transport)transports.get(name);
    }

    public int getTransportCount() throws AxisFault {
        return transports.getCount();
    }

    public void addMdoule(Module module) throws AxisFault {
        modules.add(module.getName(),module);
    }

    public void addService(Service service) throws AxisFault {
        services.add(service.getName(),service);

    }

    public void addTransport(Transport transport) throws AxisFault {
        transports.add(transport.getName(),transport);
    }

    public void removeService(QName name) throws AxisFault {
        services.remove(name);
    }

    public void removeTransport(QName name) throws AxisFault {
        transports.remove(name);
    }

}
