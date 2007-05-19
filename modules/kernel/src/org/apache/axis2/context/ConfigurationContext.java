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

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.cluster.ClusterManager;
import org.apache.axis2.cluster.ClusteringConstants;
import org.apache.axis2.cluster.configuration.ConfigurationManager;
import org.apache.axis2.cluster.context.ContextManager;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.threadpool.ThreadFactory;
import org.apache.axis2.util.threadpool.ThreadPool;

import java.io.File;
import java.net.URL;
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
    private Hashtable applicationSessionServiceGroupContextTable = new Hashtable();
    private AxisConfiguration axisConfiguration;
    private ThreadFactory threadPool;
    //To keep TransportManager instance
    private ListenerManager listenerManager;

    // current time out interval is 30 secs. Need to make this configurable
    private long serviceGroupContextTimoutInterval = 30 * 1000;

    //To specify url mapping for services
    private String contextRoot;
    private String servicePath;

    private String cachedServicePath = null;

    public ConfigurationContext(AxisConfiguration axisConfiguration) {
        super(null);
        this.axisConfiguration = axisConfiguration;
        initConfigContextTimeout(axisConfiguration);
    }

    private void initConfigContextTimeout(AxisConfiguration axisConfiguration) {
        Parameter parameter = axisConfiguration
                .getParameter(Constants.Configuration.CONFIG_CONTEXT_TIMOUT_INTERVAL);
        if (parameter != null) {
            Object value = parameter.getValue();
            if (value != null && value instanceof String) {
                serviceGroupContextTimoutInterval = Integer.parseInt((String) value);
            }
        }
    }

    public void initCluster() throws AxisFault {
        ClusterManager clusterManager = axisConfiguration.getClusterManager();
        if (clusterManager != null) {
            ContextManager contextManager = clusterManager.getContextManager();
            if (contextManager != null) {
                contextManager.setConfigurationContext(this);
            }
            ConfigurationManager configManager = clusterManager.getConfigurationManager();
            if (configManager != null) {
                configManager.setConfigurationContext(this);
            }
            if (shouldClusterBeInitiated(clusterManager)) {
                clusterManager.init();
            }
        }
    }

    private static boolean shouldClusterBeInitiated(ClusterManager clusterManager) {
        Parameter param = clusterManager.getParameter(ClusteringConstants.AVOID_INITIATION_KEY);
        return !(param != null && JavaUtils.isTrueExplicitly(param.getValue()));
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
     * @param messageContext : MessageContext
     * @throws org.apache.axis2.AxisFault : If something goes wrong
     */
    public void fillServiceContextAndServiceGroupContext(MessageContext messageContext)
            throws AxisFault {
        // by this time service group context id must have a value. Either from transport or from addressing
        ServiceGroupContext serviceGroupContext;
        ServiceContext serviceContext = messageContext.getServiceContext();

        AxisService axisService = messageContext.getAxisService();

        if (serviceContext == null) {
            String scope = axisService.getScope();
            if (Constants.SCOPE_APPLICATION.equals(scope)) {
                String serviceGroupName =
                        ((AxisServiceGroup) axisService.getParent()).getServiceGroupName();
                serviceGroupContext =
                        (ServiceGroupContext) applicationSessionServiceGroupContextTable.get(
                                serviceGroupName);
                if (serviceGroupContext == null) {
                    AxisServiceGroup axisServiceGroup = messageContext.getAxisServiceGroup();
                    if (axisServiceGroup == null) {
                        axisServiceGroup = (AxisServiceGroup) axisService.getParent();
                        messageContext.setAxisServiceGroup(axisServiceGroup);
                    }
                    ConfigurationContext cfgCtx = messageContext.getConfigurationContext();
                    serviceGroupContext = cfgCtx.createServiceGroupContext(axisServiceGroup);
                    applicationSessionServiceGroupContextTable
                            .put(serviceGroupName, serviceGroupContext);

                    ClusterManager clusterManager = this.getAxisConfiguration().getClusterManager();
                    if (clusterManager != null) {
                        ContextManager contextManager = clusterManager.getContextManager();
                        if (contextManager != null) {
                            contextManager.addContext(serviceGroupContext);
                        }
                    }
                }
                messageContext.setServiceGroupContext(serviceGroupContext);
                messageContext.setServiceContext(
                        serviceGroupContext.getServiceContext(axisService));
            } else if (Constants.SCOPE_SOAP_SESSION.equals(scope)) {
                String serviceGroupContextId = messageContext.getServiceGroupContextId();
                if (serviceGroupContextId != null) {
                    serviceGroupContext = getServiceGroupContextFromSoapSessionTable(
                            serviceGroupContextId, messageContext);
                    if (serviceGroupContext == null) {
                        throw new AxisFault("Unable to find corresponding context" +
                                            " for the serviceGroupId: " + serviceGroupContextId);
                    }
                } else {
                    AxisServiceGroup axisServiceGroup = (AxisServiceGroup) axisService.getParent();
                    serviceGroupContext = createServiceGroupContext(axisServiceGroup);
                    serviceContext = serviceGroupContext.getServiceContext(axisService);
                    // set the serviceGroupContextID
                    serviceGroupContextId = UUIDGenerator.getUUID();
                    serviceGroupContext.setId(serviceGroupContextId);

                    ClusterManager clusterManager = this.getAxisConfiguration().getClusterManager();
                    if (clusterManager != null) {
                        ContextManager contextManager = clusterManager.getContextManager();
                        if (contextManager != null) {
                            contextManager.addContext(serviceGroupContext);
                        }
                    }

                    messageContext.setServiceGroupContextId(serviceGroupContextId);
                    registerServiceGroupContextintoSoapSessionTable(serviceGroupContext);
                }
                messageContext.setServiceGroupContext(serviceGroupContext);
                messageContext.setServiceContext(
                        serviceGroupContext.getServiceContext(axisService));
            } else if (Constants.SCOPE_REQUEST.equals(scope)) {
                AxisServiceGroup axisServiceGroup = (AxisServiceGroup) axisService.getParent();
                serviceGroupContext = createServiceGroupContext(axisServiceGroup);
                messageContext.setServiceGroupContext(serviceGroupContext);
                serviceContext = serviceGroupContext.getServiceContext(axisService);
                messageContext.setServiceContext(serviceContext);
            }
        }
        if (messageContext.getOperationContext() != null) {
            messageContext.getOperationContext().setParent(serviceContext);
        }
    }

    /**
     * Registers a OperationContext with a given message ID.
     * If the given message id already has a registered operation context,
     * no change is made and the methid resturns false.
     *
     * @param messageID
     * @param mepContext
     */
    public boolean registerOperationContext(String messageID,
                                            OperationContext mepContext) {
        boolean alreadyInMap;
        mepContext.setKey(messageID);
        synchronized (operationContextMap) {
            alreadyInMap = operationContextMap.containsKey(messageID);
            if (!alreadyInMap) {
                this.operationContextMap.put(messageID, mepContext);
            }
        }
        return (!alreadyInMap);
    }

    /**
     * Unregisters the operation context associated with the given messageID
     *
     * @param key
     */
    public void unregisterOperationContext(String key) {
        synchronized (operationContextMap) {
            operationContextMap.remove(key);
        }
    }

    public void registerServiceGroupContextintoSoapSessionTable(ServiceGroupContext serviceGroupContext) {
        String id = serviceGroupContext.getId();
        serviceGroupContextMap.put(id, serviceGroupContext);
        serviceGroupContext.touch();
        serviceGroupContext.setParent(this);
        // this is the best time to clean up the SGCtxts since are not being used anymore
        cleanupServiceGroupContexts();
    }

    public void addServiceGroupContextintoApplicatoionScopeTable(
            ServiceGroupContext serviceGroupContext) {
        if (applicationSessionServiceGroupContextTable == null) {
            applicationSessionServiceGroupContextTable = new Hashtable();
        }
        applicationSessionServiceGroupContextTable.put(
                serviceGroupContext.getDescription().getServiceGroupName(), serviceGroupContext);
    }

    public AxisConfiguration getAxisConfiguration() {
        return axisConfiguration;
    }

    /**
     * Gets a OperationContext given a Message ID.
     *
     * @return Returns OperationContext <code>OperationContext<code>
     */
    public OperationContext getOperationContext(String id) {
        OperationContext opCtx;
        synchronized (operationContextMap) {
            if (operationContextMap == null) {
                return null;
            }
            opCtx = (OperationContext) this.operationContextMap.get(id);
        }

        return opCtx;
    }

    public OperationContext findOperationContext(String operationName, String serviceName,
                                                 String serviceGroupName) {
        if (operationName == null) {
            return null;
        }

        if (serviceName == null) {
            return null;
        }

        // group name is not necessarily a prereq
        // but if the group name is non-null, then it has to match

        synchronized (operationContextMap) {
            Iterator it = operationContextMap.keySet().iterator();

            while (it.hasNext()) {
                Object key = it.next();
                OperationContext value = (OperationContext) operationContextMap.get(key);

                String valueOperationName;
                String valueServiceName;
                String valueServiceGroupName;

                if (value != null) {
                    valueOperationName = value.getOperationName();
                    valueServiceName = value.getServiceName();
                    valueServiceGroupName = value.getServiceGroupName();

                    if ((valueOperationName != null) && (valueOperationName.equals(operationName)))
                    {
                        if ((valueServiceName != null) && (valueServiceName.equals(serviceName))) {
                            if ((valueServiceGroupName != null) && (serviceGroupName != null)
                                && (valueServiceGroupName.equals(serviceGroupName))) {
                                // match
                                return value;
                            }

                            // or, both need to be null
                            if ((valueServiceGroupName == null) && (serviceGroupName == null)) {
                                // match
                                return value;
                            }
                        }
                    }
                }
            }
        }

        // if we got here, we did not find an operation context
        // that fits the criteria
        return null;
    }

    protected ArrayList contextListeners;

    /**
     * Register a ContextListener to be notified of all sub-context creation events.
     * Note that we currently only support a single listener.
     *
     * @param contextListener a ContextListener
     */
    public void registerContextListener(ContextListener contextListener) {
        if (contextListeners == null) contextListeners = new ArrayList();
        contextListeners.add(contextListener);
    }

    /**
     * Inform any listeners of a new context
     *
     * @param context the just-created subcontext
     */
    void contextCreated(AbstractContext context) {
        if (contextListeners == null) return;
        for (Iterator i = contextListeners.iterator(); i.hasNext();) {
            ContextListener listener = (ContextListener)i.next();
            listener.contextCreated(context);
        }
    }

    /**
     * Create a MessageContext, and notify any registered ContextListener.
     * 
     * @return a new MessageContext
     */
    public MessageContext createMessageContext() {
        MessageContext msgCtx = new MessageContext(this);
        contextCreated(msgCtx);
        return msgCtx;
    }

    /**
     * Create a ServiceGroupContext for the specified service group, and notify any
     * registered ContextListener.
     *
     * @param serviceGroup an AxisServiceGroup
     * @return a new ServiceGroupContext
     */
    public ServiceGroupContext createServiceGroupContext(AxisServiceGroup serviceGroup) {
        ServiceGroupContext sgCtx = new ServiceGroupContext(this, serviceGroup);
        contextCreated(sgCtx);
        return sgCtx;
    }

    /**
     * Allows users to resolve the path relative to the root diretory.
     *
     * @param path
     */
    public File getRealPath(String path) {
        URL repository = axisConfiguration.getRepository();
        if (repository != null) {
            File repo = new File(repository.getFile());
            return new File(repo, path);
        }
        return null;
    }

    public ServiceGroupContext getServiceGroupContextFromSoapSessionTable(
            String serviceGroupContextId,
            MessageContext msgContext) {
        ServiceGroupContext serviceGroupContext =
                (ServiceGroupContext) serviceGroupContextMap.get(serviceGroupContextId);

        if (serviceGroupContext != null) {
            serviceGroupContext.touch();
        }
        return serviceGroupContext;
    }


    /**
     * Returns a ServiceGroupContext object associated
     * with the specified ID from the internal table.
     *
     * @param Id The ID string associated with the ServiceGroupContext object
     * @return The ServiceGroupContext object, or null if not found
     */
    public ServiceGroupContext getServiceGroupContext(String Id) {

        if (Id == null) {
            // Hashtables require non-null key-value pairs
            return null;
        }

        ServiceGroupContext serviceGroupContext = null;

        if (serviceGroupContextMap != null) {
            serviceGroupContext = (ServiceGroupContext) serviceGroupContextMap.get(Id);
            if (serviceGroupContext != null) {
                serviceGroupContext.touch();
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

    /**
     * @param configuration
     */
    public void setAxisConfiguration(AxisConfiguration configuration) {
        axisConfiguration = configuration;
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
            throw new AxisFault(Messages.getMessage("threadpoolset"));
        }
    }

    private void cleanupServiceGroupContexts() {
        if (serviceGroupContextMap == null) {
            return;
        }
        long currentTime = new Date().getTime();
        Iterator sgCtxtMapKeyIter = serviceGroupContextMap.keySet().iterator();
        while (sgCtxtMapKeyIter.hasNext()) {
            String sgCtxtId = (String) sgCtxtMapKeyIter.next();
            ServiceGroupContext serviceGroupContext =
                    (ServiceGroupContext) serviceGroupContextMap.get(sgCtxtId);
            if ((currentTime - serviceGroupContext.getLastTouchedTime()) >
                getServiceGroupContextTimoutInterval()) {
                sgCtxtMapKeyIter.remove();
                cleanupServiceContexts(serviceGroupContext);
            }
        }
    }

    public ListenerManager getListenerManager() {
        return listenerManager;
    }

    public void setTransportManager(ListenerManager listenerManager) {
        this.listenerManager = listenerManager;
    }

    private void cleanupServiceContexts(ServiceGroupContext serviceGroupContext) {
        if (serviceGroupContext == null) {
            return;
        }
        Iterator serviceContecxtes = serviceGroupContext.getServiceContexts();
        if (serviceContecxtes == null) {
            return;
        }
        while (serviceContecxtes.hasNext()) {
            ServiceContext serviceContext = (ServiceContext) serviceContecxtes.next();
            DependencyManager.destroyServiceObject(serviceContext);
        }
    }

    public void cleanupContexts() {
        if ((applicationSessionServiceGroupContextTable != null) &&
            (applicationSessionServiceGroupContextTable.size() > 0)) {
            Iterator applicationScopeSgs =
                    applicationSessionServiceGroupContextTable.values().iterator();
            while (applicationScopeSgs.hasNext()) {
                ServiceGroupContext serviceGroupContext =
                        (ServiceGroupContext) applicationScopeSgs.next();
                cleanupServiceContexts(serviceGroupContext);
            }
            applicationSessionServiceGroupContextTable.clear();
        }
        if ((serviceGroupContextMap != null) && (serviceGroupContextMap.size() > 0)) {
            Iterator sopaSessionSgs = serviceGroupContextMap.values().iterator();
            while (sopaSessionSgs.hasNext()) {
                ServiceGroupContext serviceGroupContext =
                        (ServiceGroupContext) sopaSessionSgs.next();
                cleanupServiceContexts(serviceGroupContext);
            }
            serviceGroupContextMap.clear();
        }
    }

    public void terminate()
            throws AxisFault {
        if (listenerManager != null) {
            listenerManager.stop();
        }
    }


    public String getServiceContextPath() {
        if (cachedServicePath == null) {
            cachedServicePath = internalGetServiceContextPath();
        }
        return cachedServicePath;
    }

    private String internalGetServiceContextPath() {
        String ctxRoot = getContextRoot();
        String path = "/";
        if (ctxRoot != null) {
            if (!ctxRoot.equals("/")) {
                path = ctxRoot + "/";
            }
            if (servicePath == null || servicePath.trim().length() == 0) {
                throw new IllegalArgumentException("service path cannot be null or empty");
            } else {
                path += servicePath.trim();
            }
        }
        return path;
    }

    public String getServicePath() {
        if (servicePath == null || servicePath.trim().length() == 0) {
            throw new IllegalArgumentException("service path cannot be null or empty");
        }
        return servicePath.trim();
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(String contextRoot) {
        if (contextRoot != null) {
            this.contextRoot = contextRoot.trim();  // Trim before storing away for good hygiene
            cachedServicePath = internalGetServiceContextPath();
        }
    }

    /**
     * This will be used to fetch the serviceGroupContextTimoutInterval from any place available.
     *
     * @return long
     */
    public long getServiceGroupContextTimoutInterval() {
        Integer serviceGroupContextTimoutIntervalParam =
                (Integer) getProperty(Constants.Configuration.CONFIG_CONTEXT_TIMOUT_INTERVAL);
        if (serviceGroupContextTimoutIntervalParam != null) {
            serviceGroupContextTimoutInterval = serviceGroupContextTimoutIntervalParam.intValue();
        }
        return serviceGroupContextTimoutInterval;
    }

    public void removeServiceGroupContext(AxisServiceGroup serviceGroup) {
        if (serviceGroup != null) {
            Object obj = applicationSessionServiceGroupContextTable.get(
                    serviceGroup.getServiceGroupName());
            if (obj == null) {
                ArrayList toBeRemovedList = new ArrayList();
                Iterator serviceGroupContexts = serviceGroupContextMap.values().iterator();
                while (serviceGroupContexts.hasNext()) {
                    ServiceGroupContext serviceGroupContext =
                            (ServiceGroupContext) serviceGroupContexts.next();
                    if (serviceGroupContext.getDescription().equals(serviceGroup)) {
                        toBeRemovedList.add(serviceGroupContext.getId());
                    }
                }
                for (int i = 0; i < toBeRemovedList.size(); i++) {
                    String s = (String) toBeRemovedList.get(i);
                    serviceGroupContextMap.remove(s);
                }
            }

        }
    }

    public ConfigurationContext getRootContext() {
        return this;
    }


}
