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
 *  Runtime state of the engine
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.description.PhasesInclude;
import org.apache.axis.description.PhasesIncludeImpl;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineConfiguration;

public class EngineContext extends AbstractContext implements PhasesInclude{

    private EngineConfiguration engineConfig;
    private Map serviceContextMap;
    private Map sessionContextMap;
    private Map moduleContextMap;
    
    
    private PhasesInclude phaseInclude; 
    
    public EngineContext(EngineConfiguration registry){
        this.engineConfig = registry;
        serviceContextMap = new HashMap();
        moduleContextMap = new HashMap();
        sessionContextMap = new HashMap();
        phaseInclude = new PhasesIncludeImpl();
      
    }
    
    /**
     * The method is used to do the intialization of the EngineContext, right now we know that
     * module.init(..) is called here
     * @throws AxisFault
     */
    
    
    public void init() throws AxisFault{
    
    }
    
    
    public void addService(ServiceContext service){
        serviceContextMap.put(service.getServiceConfig().getName(),service);
    }
    
    public ServiceContext getService(QName serviceName){
        return (ServiceContext)serviceContextMap.get(serviceName);
    
    }
    
    

    






    /**
     * @return
     */
    public EngineConfiguration getEngineConfig() {
        return engineConfig;
    }

    /**
     * @param configuration
     */
    public void setEngineConfig(EngineConfiguration configuration) {
        engineConfig = configuration;
    }

  
    /**
     * @param flow
     * @return
     * @throws AxisFault
     */
    public ArrayList getPhases(int flow) throws AxisFault {
        return phaseInclude.getPhases(flow);
    }

    /**
     * @param phases
     * @param flow
     * @throws AxisFault
     */
    public void setPhases(ArrayList phases, int flow) throws AxisFault {
        phaseInclude.setPhases(phases, flow);
    }

}
