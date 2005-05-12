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

import org.apache.axis.description.GlobalDescription;
import org.apache.axis.description.ModuleDescription;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.description.TransportInDescription;
import org.apache.axis.description.TransportOutDescription;

/**
 * The palce where all the Global states of Axis is kept.
 * All the Global states kept in the <code>EngineRegistry</code> and all the
 * Service states kept in the <code>MessageContext</code>. Other runtime
 * artifacts does not keep states foward from the execution.
 */
public interface AxisConfiguration {
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
     * Method getGlobal
     *
     * @return
     * @throws AxisFault
     */
    public GlobalDescription getGlobal() throws AxisFault;

    /**
     * Method getService
     *
     * @param name
     * @return
     * @throws AxisFault
     */
    public ServiceDescription getService(QName name) throws AxisFault;

    /**
     * Method addService
     *
     * @param service
     * @throws AxisFault
     */
    public void addService(ServiceDescription service) throws AxisFault;

    /**
     * Method removeService
     *
     * @param name
     * @throws AxisFault
     */
    public void removeService(QName name) throws AxisFault;

    /**
     * Modules is read only as they can not deployed while runing
     *
     * @param name
     * @return
     * @throws AxisFault
     */
    public ModuleDescription getModule(QName name) throws AxisFault;

    /**
     * Method addMdoule
     *
     * @param module
     * @throws AxisFault
     */
    public void addMdoule(ModuleDescription module) throws AxisFault;

    /**
     * Method getTransports
     *
     * @return
     * @throws AxisFault
     */
    public HashMap getTransports() throws AxisFault;

    /**
     *  This returns 
     */
    public ArrayList getInPhasesUptoAndIncludingPostDispatch();

    public ArrayList getPhasesInOutFaultFlow();
    /**
     * Method getServices
     *
     * @return
     */
    public HashMap getServices();

    public Hashtable getFaulytServices();

    public TransportInDescription getTransportIn(QName name) throws AxisFault;
    public void addTransportIn(TransportInDescription transport) throws AxisFault;
    public TransportOutDescription getTransportOut(QName name) throws AxisFault;
    public void addTransportOut(TransportOutDescription transport) throws AxisFault;
    public HashMap getTransportsIn();
    public HashMap getTransportsOut();

}
