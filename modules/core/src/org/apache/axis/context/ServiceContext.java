package org.apache.axis.context;

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
 *
 * 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.PhasesInclude;
import org.apache.axis.description.PhasesIncludeImpl;
import org.apache.axis.engine.AxisFault;

public class ServiceContext  extends AbstractContext implements PhasesInclude{
    private Map operationContextMap;
    private AxisService serviceConfig;
    private PhasesInclude phaseInclude;
   

    public ServiceContext(AxisService serviceConfig) {
        super();
        this.serviceConfig = serviceConfig;
        this.operationContextMap = new HashMap();
        phaseInclude = new PhasesIncludeImpl();
    }

    public void addOperation(AxisOperation ctxt){
        this.operationContextMap.put(ctxt.getName(),ctxt);
    }

    public AxisOperation getOperationContext(String opId){
        return (AxisOperation)operationContextMap.get(opId);
    }

    public void removeOperationContext(AxisOperation ctxt){
        operationContextMap.remove(ctxt.getName());
    }


    /**
     * @return
     */
    public AxisService getServiceConfig() {
        return serviceConfig;
    }
    
    public QName getName(){
        return serviceConfig.getName();
    }
  

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return phaseInclude.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return phaseInclude.toString();
    }

    /**
     * @param phases
     * @param flow
     * @throws AxisFault
     */
    public void setPhases(ArrayList phases, int flow) throws AxisFault {
        phaseInclude.setPhases(phases, flow);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return phaseInclude.equals(obj);
    }

    /**
     * @param flow
     * @return
     * @throws AxisFault
     */
    public ArrayList getPhases(int flow) throws AxisFault {
        return phaseInclude.getPhases(flow);
    }

}
