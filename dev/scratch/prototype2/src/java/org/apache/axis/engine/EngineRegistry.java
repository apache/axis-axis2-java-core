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


import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisModule;
import org.apache.axis.description.AxisTransport;
import org.apache.axis.impl.description.AxisService;

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 *  The palce where all the Globel states of Axis is kept. 
 *  All the Global states kept in the <code>EngineRegistry</code> and all the 
 *  Service states kept in the <code>MessageContext</code>. Other runtime
 *  artifacts does not keep states foward from the execution.  
 */

public interface EngineRegistry {
    public static final int INFLOW = 10003;
    public static final int OUTFLOW = 10004;
    public static final int FAULTFLOW = 10005; 

    public AxisGlobal getGlobal()throws AxisFault;
    
    public AxisService getService(QName name)throws AxisFault;
    public void addService(AxisService service)throws AxisFault;
    public void removeService(QName name)throws AxisFault;
    
    /**
     * Modules is read only as they can not deployed while runing 
     */
    public AxisModule getModule(QName name)throws AxisFault;
    public void addMdoule(AxisModule module)throws AxisFault;
    
    public AxisTransport getTransport(QName name) throws AxisFault;
    public void addTransport(AxisTransport transport) throws AxisFault;
    /**
     * Ordred list of phases
     */
    public ArrayList getPhases();

//    public ArrayList getTransPorts() ;
//    public void setTransPorts(ArrayList transPorts) ;

}
