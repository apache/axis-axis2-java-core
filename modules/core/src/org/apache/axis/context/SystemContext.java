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

import org.apache.axis.description.AxisService;
import org.apache.axis.description.PhasesInclude;
import org.apache.axis.description.PhasesIncludeImpl;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.AxisSystem;
import org.apache.axis.storage.AxisStorage;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SystemContext extends AbstractContext implements PhasesInclude {

    private AxisSystem engineConfig;
    private AxisStorage storage;

    private Map sessionContextMap;
    private Map moduleContextMap;

    /**
     * Map containing <code>MessageID</code> to
     * <code>OperationContext</code> mapping.
     */
    private final Map operationContextMap = new HashMap();

    private final Map serviceContextMap;

    private PhasesInclude phaseInclude;

    public SystemContext(AxisSystem registry) {
        super(null);
        this.engineConfig = registry;
        serviceContextMap = new HashMap();
        moduleContextMap = new HashMap();
        sessionContextMap = new HashMap();
        phaseInclude = new PhasesIncludeImpl();

    }

    /**
     * The method is used to do the intialization of the EngineContext, right now we know that
     * module.init(..) is called here
     *
     * @throws AxisFault
     */

    public void init() throws AxisFault {

    }

    public void removeService(QName name) {
        serviceContextMap.remove(name);
    }

    /**
     * @return
     */
    public AxisSystem getEngineConfig() {
        return engineConfig;
    }

    /**
     * @param configuration
     */
    public void setEngineConfig(AxisSystem configuration) {
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

    public void registerOperationContext(String messageID, OperationContext mepContext) {
        this.operationContextMap.put(messageID, mepContext);
    }

    public OperationContext getOperationContext(String messageID) {
        return (OperationContext) this.operationContextMap.get(messageID);
    }

    public Map getOperationContextMap() {
        return this.operationContextMap;
    }

    public void registerServiceContext(String serviceInstanceID, ServiceContext serviceContext) {
        this.serviceContextMap.put(serviceInstanceID, serviceContext);
    }

    public ServiceContext getServiceContext(String serviceInstanceID) {
        return (ServiceContext) this.serviceContextMap.get(serviceInstanceID);
    }

    public AxisStorage getStorage() {
        return storage;
    }

    public void setStorage(AxisStorage storage) {
        this.storage = storage;
    }

    public ServiceContext createServiceContext(QName serviceName) throws AxisFault {
        AxisService service = engineConfig.getService(serviceName);
        ServiceContext serviceContext = new ServiceContext(service, this);
        return serviceContext;
    }

}
