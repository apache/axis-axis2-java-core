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
import org.apache.axis.description.AxisTransportIn;
import org.apache.axis.description.AxisTransportOut;

/**
 * The palce where all the Global states of Axis is kept.
 * All the Global states kept in the <code>EngineRegistry</code> and all the
 * Service states kept in the <code>MessageContext</code>. Other runtime
 * artifacts does not keep states foward from the execution.
 */
public interface AxisSystem {
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
    public AxisGlobal getGlobal() throws AxisFault;

    /**
     * Method getService
     *
     * @param name
     * @return
     * @throws AxisFault
     */
    public AxisService getService(QName name) throws AxisFault;

    /**
     * Method addService
     *
     * @param service
     * @throws AxisFault
     */
    public void addService(AxisService service) throws AxisFault;

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
    public AxisModule getModule(QName name) throws AxisFault;

    /**
     * Method addMdoule
     *
     * @param module
     * @throws AxisFault
     */
    public void addMdoule(AxisModule module) throws AxisFault;


    /**
     * Method getTransports
     *
     * @return
     * @throws AxisFault
     */
    public HashMap getTransports() throws AxisFault;


    public ArrayList getInPhases();
    public ArrayList getOutPhases();
    public ArrayList getInFaultPhases() ;
    public ArrayList getOutFaultPhases();
    
    /**
     * Method getServices
     *
     * @return
     */
    public HashMap getServices();

    public Hashtable getFaulytServices();


    public AxisTransportIn getTransportIn(QName name) throws AxisFault ;
    public  void addTransportIn(AxisTransportIn transport) throws AxisFault;
    public AxisTransportOut getTransportOut(QName name) throws AxisFault ;
    public  void addTransportOut(AxisTransportOut transport) throws AxisFault ;
    public HashMap getTransportsIn();
    public HashMap getTransportsOut();

    ArrayList getFaultPhases();
}
