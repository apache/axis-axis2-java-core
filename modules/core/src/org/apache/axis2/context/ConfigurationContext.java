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
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.SGContextGarbageCollector;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.axis2.util.threadpool.ThreadFactory;
import org.apache.axis2.util.threadpool.ThreadPool;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;

/**
 * This contains all the configuration information for Axis2.
 */
public class ConfigurationContext extends AbstractContext {

    /**
     * Map containing <code>MessageID</code> to
     * <code>OperationContext</code> mapping.
     */
    private final Map operationContextMap = new HashMap();
    private final Map serviceContextMap = new Hashtable();
    private Hashtable serviceGroupContextMap = new Hashtable();
    private transient AxisConfiguration axisConfiguration;
    private File rootDir;
    private transient ThreadFactory threadPool;

    public ConfigurationContext(AxisConfiguration axisConfiguration) {
        super(null);
        this.axisConfiguration = axisConfiguration;
        startTimerTaskToTimeOutSGCtxt();
    }

    private void startTimerTaskToTimeOutSGCtxt() {
        int delay = 5000;   // delay for 5 sec.
        int period = 3000;  // repeat every 3 sec.
        Timer timer = new Timer();

        timer.schedule(new SGContextGarbageCollector(serviceGroupContextMap, 5 * 1000), delay, period);
    }

    protected void finalize() throws Throwable {
        super.finalize();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Searches for a ServiceGroupContext in the map with given id as the key.
     * <pre>
     * If(key != null && found)
     * check for a service context for the intended service.
     * if (!found)
     * create one and hook up to ServiceGroupContext
     * else
     * create new ServiceGroupContext with the given key or if key is null with a new key
     * create a new service context for the service
     * </pre>
     *
     * @param messageContext
     */
    public ServiceGroupContext fillServiceContextAndServiceGroupContext(
            MessageContext messageContext)
            throws AxisFault {
        String serviceGroupContextId = messageContext.getServiceGroupContextId();

        // by this time service group context id must have a value. Either from transport or from addressing
        ServiceGroupContext serviceGroupContext;
        ServiceContext serviceContext;

        if (!isNull(serviceGroupContextId)
                && (serviceGroupContextMap.get(serviceGroupContextId) != null)) {

            // SGC is already there
            serviceGroupContext =
                    (ServiceGroupContext) serviceGroupContextMap.get(serviceGroupContextId);
            serviceContext =
                    serviceGroupContext.getServiceContext(messageContext.getAxisService().getName());
        } else {

            // either the key is null or no SGC is found from the give key
            if (isNull(serviceGroupContextId)) {
                serviceGroupContextId = UUIDGenerator.getUUID();
                messageContext.setServiceGroupContextId(serviceGroupContextId);
            }

            if (messageContext.getAxisService() != null) {
                AxisServiceGroup axisServiceGroup = messageContext.getAxisService().getParent();

                serviceGroupContext = new ServiceGroupContext(this, axisServiceGroup);
                serviceContext = serviceGroupContext.getServiceContext(
                        messageContext.getAxisService().getName());

                // set the serviceGroupContextID
                serviceGroupContext.setId(serviceGroupContextId);
                this.registerServiceGroupContext(serviceGroupContext);
            } else {
                throw new AxisFault("AxisService Not found yet");
            }
        }

        // when you come here operation context MUST already been assigned to the message context
        messageContext.getOperationContext().setParent(serviceContext);
        messageContext.setServiceContext(serviceContext);
        messageContext.setServiceGroupContext(serviceGroupContext);

        return serviceGroupContext;
    }

    /**
     * Registers a OperationContext with a given message ID.
     *
     * @param messageID
     * @param mepContext
     */
    public synchronized void registerOperationContext(String messageID,
                                                      OperationContext mepContext) {
        this.operationContextMap.put(messageID, mepContext);
    }

    public synchronized void registerServiceGroupContext(ServiceGroupContext serviceGroupContext) {
        String id = serviceGroupContext.getId();

        if (serviceGroupContextMap.get(id) == null) {
            serviceGroupContextMap.put(id, serviceGroupContext);
            serviceGroupContext.setParent(this);
        }
    }

    public AxisConfiguration getAxisConfiguration() {
        return axisConfiguration;
    }

    /**
     * Gets a OperationContext given a Message ID.
     *
     * @param messageID
     * @return Returns OperationContext <code>OperationContext<code>
     */
    public OperationContext getOperationContext(String messageID) {
        return (OperationContext) this.operationContextMap.get(messageID);
    }

    public Map getOperationContextMap() {
        return this.operationContextMap;
    }

    /**
     * Allows users to resolve the path relative to the root diretory.
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
     * Gets the ServiceContext for a service id.
     *
     * @param serviceInstanceID
     */
    public ServiceContext getServiceContext(String serviceInstanceID) {
        return (ServiceContext) this.serviceContextMap.get(serviceInstanceID);
    }

    public ServiceGroupContext getServiceGroupContext(String serviceGroupContextId) {
        if (serviceGroupContextMap != null) {
            return (ServiceGroupContext) serviceGroupContextMap.get(serviceGroupContextId);
        }

        return null;
    }

    /**
     * Gets all service groups in the system.
     *
     * @return Returns hashmap of ServiceGroupContexts.
     */
    public Hashtable getServiceGroupContexts() {
        return serviceGroupContextMap;
    }

    /**
     * Returns the thread factory.
     *
     * @return Returns configuration specific thread pool
     */
    public ThreadFactory getThreadPool() {
        if (threadPool == null) {
            threadPool = new ThreadPool();
        }

        return threadPool;
    }

    private boolean isNull(String string) {
        return "".equals(string) || (string == null);
    }

    /**
     * @param configuration
     */
    public void setAxisConfiguration(AxisConfiguration configuration) {
        axisConfiguration = configuration;
    }

    /**
     * @param file
     */
    public void setRootDir(File file) {
        rootDir = file;
    }

    /**
     * Sets the thread factory.
     *
     * @param pool
     */
    public void setThreadPool(ThreadFactory pool) throws AxisFault {
        if (threadPool == null) {
            threadPool = pool;
        } else {
            throw new AxisFault("Thread pool already set.");
        }
    }
}
