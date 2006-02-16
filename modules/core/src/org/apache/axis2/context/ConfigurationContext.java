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
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.util.SessionUtils;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.axis2.util.threadpool.ThreadFactory;
import org.apache.axis2.util.threadpool.ThreadPool;

import java.io.File;
import java.util.*;

/**
 * This contains all the configuration information for Axis2.
 */
public class ConfigurationContext extends AbstractContext {

    /**
     * Map containing <code>MessageID</code> to
     * <code>OperationContext</code> mapping.
     */
    private final Map operationContextMap = new HashMap();
    private Hashtable serviceGroupContextMap = new Hashtable();
    private Hashtable applicationSessionServiceGroupContextTabale = new Hashtable();
    private transient AxisConfiguration axisConfiguration;
    private File rootDir;
    private transient ThreadFactory threadPool;
    //To keep TransportManager instance
    private ListenerManager listenerManager;

    // current time out interval is 30 secs. Need to make this configurable
    private long serviceGroupContextTimoutInterval = 30 * 1000;

    public ConfigurationContext(AxisConfiguration axisConfiguration) {
        super(null);
        this.axisConfiguration = axisConfiguration;
    }

    protected void finalize() throws Throwable {
        super.finalize();
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
    public void fillServiceContextAndServiceGroupContext(
            MessageContext messageContext)
            throws AxisFault {
        String serviceGroupContextId = messageContext.getServiceGroupContextId();
        SessionContext sessionContext = messageContext.getSessionContext();

        // by this time service group context id must have a value. Either from transport or from addressing
        ServiceGroupContext serviceGroupContext;
        ServiceContext serviceContext = messageContext.getServiceContext();

        AxisService axisService = messageContext.getAxisService();

        if (serviceContext == null) {
            if (Constants.SCOPE_APPLICATION.equals(axisService.getScope())) {
                serviceGroupContext = (ServiceGroupContext) applicationSessionServiceGroupContextTabale.get(
                        ((AxisServiceGroup) axisService.getParent()).getServiceGroupName());
                if (serviceGroupContext == null) {
                    AxisServiceGroup axisServiceGroup = messageContext.getAxisServiceGroup();
                    if (axisServiceGroup == null) {
                        axisServiceGroup = (AxisServiceGroup) messageContext.getAxisService().getParent();
                    }
                    serviceGroupContext = new ServiceGroupContext(messageContext.getConfigurationContext(),
                            axisServiceGroup);
                }
                serviceContext = serviceGroupContext.getServiceContext(axisService);

            } else if (!isNull(serviceGroupContextId)
                    && (getServiceGroupContext(serviceGroupContextId, messageContext) != null)) {

                // SGC is already there
                serviceGroupContext =
                        getServiceGroupContext(serviceGroupContextId, messageContext);
                serviceContext =
                        serviceGroupContext.getServiceContext(messageContext.getAxisService());
            } else {

                // either the key is null or no SGC is found from the give key
                if (isNull(serviceGroupContextId)) {
                    serviceGroupContextId = UUIDGenerator.getUUID();
                    messageContext.setServiceGroupContextId(serviceGroupContextId);
                }

                if (messageContext.getAxisService() != null) {
                    AxisServiceGroup axisServiceGroup = (AxisServiceGroup) messageContext.getAxisService().getParent();

                    serviceGroupContext = new ServiceGroupContext(this, axisServiceGroup);
                    serviceContext = serviceGroupContext.getServiceContext(messageContext.getAxisService());

                    // set the serviceGroupContextID
                    serviceGroupContext.setId(serviceGroupContextId);
                } else {
                    throw new AxisFault("AxisService Not found yet");
                }
            }

            /**
             * 1. Check the max scope of the service gruop , if it is grater than TransportSession
             *    then need to store in configurationContext
             * 2. Else need to store in SessionContext , and need to store both service context and
             *    service group context
             */
            String maxScope = SessionUtils.calculateMaxScopeForServiceGroup(serviceGroupContext.getDescription());
            if (Constants.SCOPE_APPLICATION.equals(maxScope)) {
                addServiceGroupContextintoApplicatoionScopeTable(serviceGroupContext);
            } else if (Constants.SCOPE_SOAP_SESSION.equals(maxScope)) {
                registerServiceGroupContext(serviceGroupContext);
            } else if (Constants.SCOPE_TRANSPORT_SESSION.equals(maxScope)) {
                sessionContext.addServiceGroupContext(serviceGroupContext, serviceGroupContextId);
                sessionContext.addServiceContext(serviceContext);
            }
            messageContext.setServiceContext(serviceContext);
            if (Constants.SCOPE_REQUEST.equals(maxScope)) {
                messageContext.setServiceGroupContextId(null);
            } else {
                messageContext.setServiceGroupContext(serviceGroupContext);
            }
        }
        if (sessionContext != null) {
            // when you come here operation context MUST already been assigned to the message context
            serviceContext.setProperty(Constants.COOKIE_STRING, sessionContext.getCookieID());
        }
        messageContext.getOperationContext().setParent(serviceContext);
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
            serviceGroupContext.touch();
            serviceGroupContext.setParent(this);
        }

        // this is the best time to clean up the SGCtxts that are not being used anymore
        cleanupServiceGroupContexts();
    }

    private synchronized void addServiceGroupContextintoApplicatoionScopeTable(
            ServiceGroupContext serviceGroupContext) {
        applicationSessionServiceGroupContextTabale.put(
                serviceGroupContext.getDescription().getServiceGroupName(), serviceGroupContext);
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

    public synchronized ServiceGroupContext getServiceGroupContext(String serviceGroupContextId,
                                                                   MessageContext msgContext) {
        ServiceGroupContext serviceGroupContext = null;
        if (serviceGroupContextMap != null) {
            serviceGroupContext = (ServiceGroupContext) serviceGroupContextMap.get(serviceGroupContextId);
            if (serviceGroupContext != null) {
                serviceGroupContext.touch();
            }
        }
        if (serviceGroupContext == null && msgContext.getSessionContext() != null) {
            serviceGroupContext = msgContext.getSessionContext().getServiceGroupContext(
                    serviceGroupContextId);
        }
        if (serviceGroupContext == null) {
            AxisService axisService = msgContext.getAxisService();
            if (axisService != null) {
                AxisServiceGroup asg = (AxisServiceGroup) axisService.getParent();
                if (asg != null) {
                    serviceGroupContext = (ServiceGroupContext)
                            applicationSessionServiceGroupContextTabale.get(asg.getServiceGroupName());
                }
            }

        }

        return serviceGroupContext;
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

    private void cleanupServiceGroupContexts() {
        synchronized (serviceGroupContextMap) {
            long currentTime = new Date().getTime();
            Iterator sgCtxtMapKeyIter = serviceGroupContextMap.keySet().iterator();
            while (sgCtxtMapKeyIter.hasNext()) {
                String sgCtxtId = (String) sgCtxtMapKeyIter.next();
                ServiceGroupContext serviceGroupContext =
                        (ServiceGroupContext) serviceGroupContextMap.get(sgCtxtId);
                if ((currentTime - serviceGroupContext.getLastTouchedTime()) >
                        serviceGroupContextTimoutInterval) {
                    sgCtxtMapKeyIter.remove();
                }
            }
        }
    }

    public ListenerManager getListenerManager() {
        return listenerManager;
    }

    public void setTransportManager(ListenerManager listenerManager) {
        this.listenerManager = listenerManager;
    }
}
