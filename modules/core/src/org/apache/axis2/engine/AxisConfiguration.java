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

package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.storage.AxisStorage;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * The palce where all the Global states of Axis is kept.
 * All the Global states kept in the <code>EngineRegistry</code> and all the
 * Service states kept in the <code>MessageContext</code>. Other runtime
 * artifacts does not keep states foward from the execution.
 */
public interface AxisConfiguration extends ParameterInclude {
    /**
     * Field INFLOW
     */
    public static final int INFLOW = 10003;

    /**
     * Field OUTFLOW
     */
    public static final int OUTFLOW = 10004;

    /**
     * Field FAULT_IN_FLOW
     */
    public static final int FAULT_IN_FLOW = 10005;

    public static final int FAULT_OUT_FLOW = 10006;

    /**
     * Method getService
     *
     * @param name
     * @return
     * @throws AxisFault
     */
    public AxisService getService(String name) throws AxisFault;

    /**
     * Method addService
     *
     * @param service
     * @throws AxisFault
     */
    public void addService(AxisService service) throws AxisFault;

    //to Add service Groups
     public void addServiceGroup(AxisServiceGroup axisServiceGroup) throws AxisFault;

    /**
     * Method removeService
     *
     * @param name
     * @throws AxisFault
     */
    public void removeService(String name) throws AxisFault;

    /**
     * Modules is read only as they can not deployed while runing
     *
     * @param name
     * @return
     */
    public ModuleDescription getModule(QName name);

    /**
     * Method addMdoule
     *
     * @param module
     * @throws AxisFault
     */
    public void addModule(ModuleDescription module) throws AxisFault;

    public boolean isEngaged(QName moduleName);

    /**
     * To engage a module at the run time it can be used this method
     *
     * @param moduleref
     * @throws AxisFault
     */
    public void engageModule(QName moduleref) throws AxisFault;



    //
    public HashMap getServices();
    /**
     * This returns
     */
    public ArrayList getInPhasesUptoAndIncludingPostDispatch();

    public ArrayList getInFaultFlow();

    public Hashtable getFaultyServices();

    public Hashtable getFaultyModules();

    public TransportInDescription getTransportIn(QName name) throws AxisFault;

    public void addTransportIn(TransportInDescription transport) throws AxisFault;

    public TransportOutDescription getTransportOut(QName name) throws AxisFault;

    public void addTransportOut(TransportOutDescription transport) throws AxisFault;

    public HashMap getTransportsIn();

    public HashMap getTransportsOut();

    //to get and set Axis2 storges (the class which should handle storeg)
    public void setAxisStorage(AxisStorage axisStorage);

    public AxisStorage getAxisStorage();

    //to check whether a given paramter is locked
    public boolean isParameterLocked(String paramterName);

    public AxisServiceGroup getServiceGroup(String serviceNameAndGroupString);

    Iterator getServiceGroups();

    public void notifyObservers(int event_type , AxisService service);


    //the class loder which become the top most parent of all the modules and services
    public ClassLoader getSystemClassLoader();
    public void setSystemClassLoader(ClassLoader classLoader);

    // the class loder that become the paranet of all the services
    public ClassLoader getServiceClassLoader();
    public void setServiceClassLoader(ClassLoader classLoader);

    // the class loder that become the paranet of all the moduels
    public ClassLoader getModuleClassLoader();
    public void setModuleClassLoader(ClassLoader classLoader);
}
