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
package org.apache.axis.impl.engine;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisModule;
import org.apache.axis.description.AxisTransport;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.impl.description.AxisService;



public class EngineRegistryImpl implements EngineRegistry{
    private HashMap modules = new HashMap();
    private HashMap services = new HashMap();
    private HashMap transports = new HashMap();
    private AxisGlobal global;
    private ArrayList phases;
    
    public EngineRegistryImpl(AxisGlobal global){
        this.global = global;
        phases = new ArrayList();
    }

    public synchronized void addMdoule(AxisModule module) throws AxisFault {
        modules.put(module.getName(),module);
    }

    public synchronized  void addService(AxisService service) throws AxisFault {
        services.put(service.getName(),service);
    }

    public AxisGlobal getGlobal() throws AxisFault {
        return global;
    }

    public AxisModule getModule(QName name) throws AxisFault {
        return (AxisModule)modules.get(name);
    }

    public AxisService getService(QName name) throws AxisFault {
        return (AxisService)services.get(name);
    }

    public  synchronized void removeService(QName name) throws AxisFault {
        services.remove(name);
    }
    
    public AxisTransport getTransport(QName name) throws AxisFault {
         return (AxisTransport)transports.get(name);
     }

    public synchronized  void addTransport(AxisTransport transport) throws AxisFault {
        transports.put(transport.getName(),transport);
     }
    /**
     * @return
     */
    public ArrayList getPhases() {
        return phases;
    }

    /**
     * @param list
     */
    public void setPhases(ArrayList list) {
        phases = list;
    }

}
