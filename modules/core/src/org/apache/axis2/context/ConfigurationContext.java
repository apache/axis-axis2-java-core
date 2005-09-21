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

package org.apache.axis2.context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.ServiceGroupDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.storage.AxisStorage;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.axis2.util.threadpool.ThreadPool;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is the biggest memeber of the Axis2 information hierachy, and if this is serialized completly
 * the whole Axis2 is saved to the disc.
 */

public class ConfigurationContext extends AbstractContext {

    private transient AxisConfiguration axisConfiguration;
    private AxisStorage storage;

    private Map sessionContextMap;
    private Map moduleContextMap;

    private transient ThreadPool threadPool;

    private File rootDir;

    /**
     * Map containing <code>MessageID</code> to
     * <code>OperationContext</code> mapping.
     */
    private final Map operationContextMap = new HashMap();

    private final Map serviceContextMap = new HashMap();

    private final Map serviceGroupContextMap = new HashMap();

    public ConfigurationContext(AxisConfiguration axisConfiguration) {
        super(null);
        this.axisConfiguration = axisConfiguration;
    }

    /**
     * The method is used to do the intialization of the EngineContext
     *
     * @throws AxisFault
     */

    public void init() throws AxisFault {

    }

    public synchronized void removeService(QName name) {
        serviceContextMap.remove(name);
    }


    public void init(AxisConfiguration axisConfiguration) throws AxisFault {
        this.axisConfiguration = axisConfiguration;
        Iterator operationContextIt = operationContextMap.keySet().iterator();
        while (operationContextIt.hasNext()) {
            Object key = operationContextIt.next();
            OperationContext operationContext = (OperationContext) operationContextMap.get(key);
            if (operationContext != null)
                operationContext.init(axisConfiguration);
        }
        Iterator serviceContextIt = serviceContextMap.keySet().iterator();
        while (serviceContextIt.hasNext()) {
            Object key = serviceContextIt.next();
            ServiceContext serviceContext = (ServiceContext) serviceContextMap.get(key);
            if (serviceContext != null)
                serviceContext.init(axisConfiguration);
        }
        Iterator serviceGroupContextIt = serviceGroupContextMap.keySet().iterator();
        while (serviceGroupContextIt.hasNext()) {
            Object key = serviceGroupContextIt.next();
            ServiceGroupContext serviceGroupContext = (ServiceGroupContext) serviceGroupContextMap.get(key);
            if (serviceGroupContext != null)
                serviceGroupContext.init(axisConfiguration);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        threadPool = new ThreadPool();
    }

    /**
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

    /**
     * Register a OperationContext agienst a given Message ID.
     *
     * @param messageID
     * @param mepContext
     */
    public synchronized void registerOperationContext(
            String messageID,
            OperationContext mepContext) {
        this.operationContextMap.put(messageID, mepContext);
    }

    /**
     * get a OperationContext given a Message ID
     *
     * @param messageID
     * @return OperationContext <code>OperationContext<code>
     */
    public OperationContext getOperationContext(String messageID) {
        return (OperationContext) this.operationContextMap.get(messageID);
    }

    public Map getOperationContextMap() {
        return this.operationContextMap;
    }

    /**
     * Register a ServiceContext agienst a given Message ID.
     */
    public synchronized void registerServiceContext(
            String serviceInstanceID,
            ServiceContext serviceContext) {
        this.serviceContextMap.put(serviceInstanceID, serviceContext);
    }

    /**
     * get the ServiceContext given a id
     *
     * @param serviceInstanceID
     */
    public ServiceContext getServiceContext(String serviceInstanceID) {
        return (ServiceContext) this.serviceContextMap.get(serviceInstanceID);
    }

    public AxisStorage getStorage() {
        return axisConfiguration.getAxisStorage();   //storage;
    }

    public void setStorage(AxisStorage storage) {
        this.storage = storage;
    }


    /**
     * @return the Gloal ThradPool
     */
    public ThreadPool getThreadPool() {
        if (threadPool == null) {
            threadPool = new ThreadPool();
        }
        return threadPool;
    }

    /**
     * This method allows users to reolve the paths relative to the
     * root diretory
     *
     * @param path
     */
    public File getRealPath(String path) {
        if (rootDir == null) {
            return new File(path);
        } else {
            return new File(rootDir, path);
        }
    }

    /**
     * @param file
     */
    public void setRootDir(File file) {
        rootDir = file;
    }

    /**
     * This method should search for a SGC in the map with given id as the key.
     * If(key != null && found)
     * check for a service context for the intended service.
     * if (!found)
     * create one and hook up to SGC
     * else
     * create new sgc with the given key or if key is null with a new key
     * create a new service context for the service
     *
     * @param messageContext
     */
    public ServiceGroupContext fillServiceContextAndServiceGroupContext(MessageContext messageContext) throws AxisFault {

        String serviceGroupContextId = messageContext.getServiceGroupContextId();

        // by this time service group context id must have a value. Either from transport or from addressing
        ServiceGroupContext serviceGroupContext;
        ServiceContext serviceContext;
        if (!isNull(serviceGroupContextId) && serviceGroupContextMap.get(serviceGroupContextId) != null) {
            // SGC is already there
            serviceGroupContext = (ServiceGroupContext) serviceGroupContextMap.get(serviceGroupContextId);

            // check service context is there or not

            //todo Chinthka : I think we do not need to do this
            // ckecking here , inside ServiceGropContext I have done that  , please take a look at taht

            serviceContext = serviceGroupContext.getServiceContext(messageContext.getServiceDescription().getName().
                    getLocalPart());
//            if (serviceContext == null) {
//                serviceContext = new ServiceContext(messageContext.getServiceDescription(), serviceGroupContext);
//                serviceGroupContext.registerServiceContext(serviceContext);
//            }
        } else {
            // either the key is null or no SGC is found from the give key
            if (isNull(serviceGroupContextId)) {
                serviceGroupContextId = UUIDGenerator.getUUID();
                messageContext.setServiceGroupContextId(serviceGroupContextId);
            }
            if (messageContext.getServiceDescription() != null) {
//                String servicName = messageContext.getServiceDescription().getName().getLocalPart();
                ServiceGroupDescription servicGroupDescription =
                        messageContext.getServiceDescription().getParent();
//                ServiceGroupDescription servicGroupDescription =
//                        this.getAxisConfiguration().getServiceGroup(servicName);
                serviceGroupContext = servicGroupDescription.getServiceGroupContext(this);
                serviceContext = serviceGroupContext.getServiceContext(
                        messageContext.getServiceDescription().getName().
                                getLocalPart());
                //set the serviceGroupContextID
                serviceGroupContext.setId(serviceGroupContextId);
                this.registerServiceGroupContext(serviceGroupContext);
            } else {
                throw new AxisFault("ServiceDescription Not found yet");
            }
        }

        // when you come here operation context MUST already been assigned to the message context
        messageContext.getOperationContext().setParent(serviceContext);
        messageContext.setServiceContext(serviceContext);
        messageContext.setServiceGroupContext(serviceGroupContext);
        return serviceGroupContext;
    }

    public void registerServiceGroupContext(ServiceGroupContext serviceGroupContext) {
        String id = serviceGroupContext.getId();
        if (serviceGroupContextMap.get(id) == null) {
            serviceGroupContextMap.put(id, serviceGroupContext);
            serviceGroupContext.setParent(this);
        }
    }

    public ServiceGroupContext getServiceGroupContext(String serviceGroupContextId){
        if(serviceGroupContextMap != null){
            return (ServiceGroupContext) serviceGroupContextMap.get(serviceGroupContextId);
        }
        return null;
    }

    private boolean isNull(String string) {
        return "".equals(string) || string == null;
    }

    /**
     * To get all the service groups in the system
     * @return
     */
    public HashMap getServiceGroupContexts(){
        return (HashMap)serviceGroupContextMap;
    }

}
