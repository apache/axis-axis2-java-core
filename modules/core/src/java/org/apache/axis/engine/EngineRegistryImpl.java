/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.namespace.QName;

import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisModule;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisTransport;

/**
 * Class EngineRegistryImpl
 */
public class EngineRegistryImpl implements EngineRegistry {
    /**
     * To store Erroness services
     */
    private  Hashtable errornesServices;

    /**
     * Field modules
     */
    private final HashMap modules = new HashMap();

    /**
     * Field services
     */
    private final HashMap services = new HashMap();

    /**
     * Field transports
     */
    private final HashMap transports = new HashMap();

    /**
     * Field global
     */
    private final AxisGlobal global;

    /**
     * Field phases
     */
    private ArrayList phases;

    /**
     * Constructor EngineRegistryImpl
     *
     * @param global
     */
    public EngineRegistryImpl(AxisGlobal global) {
        this.global = global;
        phases = new ArrayList();
        errornesServices = new Hashtable();
    }

    /**
     * Method getServices
     *
     * @return
     */
    public HashMap getServices() {
        return services;
    }

    public Hashtable getFaulytServices() {
        return  errornesServices;
    }

    /**
     * Method addMdoule
     *
     * @param module
     * @throws AxisFault
     */
    public synchronized void addMdoule(AxisModule module) throws AxisFault {
        modules.put(module.getName(), module);
    }

    /**
     * Method addService
     *
     * @param service
     * @throws AxisFault
     */
    public synchronized void addService(AxisService service) throws AxisFault {
        services.put(service.getName(), service);
    }

    /**
     * Method getGlobal
     *
     * @return
     * @throws AxisFault
     */
    public AxisGlobal getGlobal() throws AxisFault {
        return global;
    }

    /**
     * Method getModule
     *
     * @param name
     * @return
     * @throws AxisFault
     */
    public AxisModule getModule(QName name) throws AxisFault {
        return (AxisModule) modules.get(name);
    }

    /**
     * Method getService
     *
     * @param name
     * @return
     * @throws AxisFault
     */
    public AxisService getService(QName name) throws AxisFault {
        return (AxisService) services.get(name);
    }

    /**
     * Method removeService
     *
     * @param name
     * @throws AxisFault
     */
    public synchronized void removeService(QName name) throws AxisFault {
        services.remove(name);
    }

    /**
     * Method getTransport
     *
     * @param name
     * @return
     * @throws AxisFault
     */
    public AxisTransport getTransport(QName name) throws AxisFault {
        return (AxisTransport) transports.get(name);
    }

    /**
     * Method addTransport
     *
     * @param transport
     * @throws AxisFault
     */
    public synchronized void addTransport(AxisTransport transport)
            throws AxisFault {
        transports.put(transport.getName(), transport);
    }

    /**
     * Method getTransports
     *
     * @return
     * @throws AxisFault
     */
    public HashMap getTransports() throws AxisFault {
        return transports;
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
