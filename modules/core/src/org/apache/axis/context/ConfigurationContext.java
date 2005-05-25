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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.description.ServiceDescription;
import org.apache.axis.engine.AxisConfiguration;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.storage.AxisStorage;

public class ConfigurationContext extends AbstractContext{

    private AxisConfiguration axisConfiguration;
    private AxisStorage storage;

    private Map sessionContextMap;
    private Map moduleContextMap;

    /**
     * Map containing <code>MessageID</code> to
     * <code>OperationContext</code> mapping.
     */
    private final Map operationContextMap = new HashMap();

    private final Map serviceContextMap;

    public ConfigurationContext(AxisConfiguration registry) {
        super(null);
        this.axisConfiguration = registry;
        serviceContextMap = new HashMap();
        moduleContextMap = new HashMap();
        sessionContextMap = new HashMap();
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
    public AxisConfiguration getAxisConfiguration() {
        return axisConfiguration;
    }

    /**
     * @param configuration
     */
    public void setAxisConfiguration(AxisConfiguration configuration) {
        axisConfiguration = configuration;
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
        ServiceDescription service = axisConfiguration.getService(serviceName);
        if(service != null){
            ServiceContext serviceContext = new ServiceContext(service, this);
            return serviceContext;
        }else{
            throw new AxisFault("Service not found service name = "+serviceName );
        }
    }

}
