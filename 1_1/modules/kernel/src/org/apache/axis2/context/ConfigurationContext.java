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
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.SessionUtils;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.axis2.util.threadpool.ThreadFactory;
import org.apache.axis2.util.threadpool.ThreadPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * This contains all the configuration information for Axis2.
 */
public class ConfigurationContext extends AbstractContext {

    private static final Log log = LogFactory.getLog(ConfigurationContext.class);
    /**
     * Map containing <code>MessageID</code> to
     * <code>OperationContext</code> mapping.
     */
    private final Map operationContextMap = new HashMap();
    private Hashtable serviceGroupContextMap = new Hashtable();
    private Hashtable applicationSessionServiceGroupContextTable = new Hashtable();
    private transient AxisConfiguration axisConfiguration;
    private transient ThreadFactory threadPool;
    //To keep TransportManager instance
    private ListenerManager listenerManager;

    // current time out interval is 30 secs. Need to make this configurable
    private long serviceGroupContextTimoutInterval = 30 * 1000;

    //To specify url mapping for services
    private String contextRoot = "axis2";
    private String servicePath = "services";
    private String restPath = Constants.DEFAULT_REST_PATH;
    //To have your own context path

    public ConfigurationContext(AxisConfiguration axisConfiguration) {
        super(null);
        this.axisConfiguration = axisConfiguration;
        initConfigContextTimeout(axisConfiguration);
    }

    private void initConfigContextTimeout(AxisConfiguration axisConfiguration) {
        Parameter parameter = axisConfiguration.getParameter(Constants.Configuration.CONFIG_CONTEXT_TIMOUT_INTERVAL);
        if (parameter != null) {
            Object value = parameter.getValue();
            if (value != null && value instanceof String) {
                serviceGroupContextTimoutInterval = Integer.parseInt((String) value);
            }
        }
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
                String serviceGroupName = ((AxisServiceGroup) axisService.getParent()).getServiceGroupName();
                serviceGroupContext = (ServiceGroupContext) applicationSessionServiceGroupContextTable.get(
                        serviceGroupName);
                if (serviceGroupContext == null) {
                    AxisServiceGroup axisServiceGroup = messageContext.getAxisServiceGroup();
                    if (axisServiceGroup == null) {
                        axisServiceGroup = (AxisServiceGroup) messageContext.getAxisService().getParent();
                    }
                    serviceGroupContext = new ServiceGroupContext(messageContext.getConfigurationContext(),
                            axisServiceGroup);
                    applicationSessionServiceGroupContextTable.put(serviceGroupName, serviceGroupContext);
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
                    throw new AxisFault(Messages.getMessage("servicenotfound"));
                }
            }

            /**
             * 1. Check the max scope of the service group , if it is grater than TransportSession
             *    then need to store in configurationContext
             * 2. Else need to store in SessionContext , and need to store both service context and
             *    service group context
             */
            String maxScope = SessionUtils.calculateMaxScopeForServiceGroup(serviceGroupContext.getDescription());
            if (Constants.SCOPE_SOAP_SESSION.equals(maxScope)) {
                registerServiceGroupContext(serviceGroupContext);
            } else if (Constants.SCOPE_TRANSPORT_SESSION.equals(maxScope)) {
                if (sessionContext != null) {
                    sessionContext.addServiceGroupContext(serviceGroupContext, serviceGroupContextId);
                    sessionContext.addServiceContext(serviceContext);
                }
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
            serviceContext.setProperty(HTTPConstants.COOKIE_STRING, sessionContext.getCookieID());
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
        mepContext.setKey(messageID);
        this.operationContextMap.put(messageID, mepContext);
    }

    public synchronized void registerServiceGroupContext(ServiceGroupContext serviceGroupContext) {
        String id = serviceGroupContext.getId();

        if (serviceGroupContextMap.get(id) == null) {
            serviceGroupContextMap.put(id, serviceGroupContext);
            serviceGroupContext.touch();
            serviceGroupContext.setParent(this);
        }

        // this is the best time to clean up the SGCtxts since are not being used anymore
        cleanupServiceGroupContexts();
    }

    public synchronized void addServiceGroupContextintoApplicatoionScopeTable(
            ServiceGroupContext serviceGroupContext) {
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
        return (OperationContext) this.operationContextMap.get(id);
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
        URL repository = axisConfiguration.getRepository();
        if (repository != null) {
            File repo = new File(repository.getFile());
            return new File(repo, path);
        }
        return null;
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
        if (serviceGroupContext == null
                && msgContext != null
                && msgContext.getSessionContext() != null) {
            serviceGroupContext = msgContext.getSessionContext().getServiceGroupContext(
                    serviceGroupContextId);
        }
        if (serviceGroupContext == null && msgContext != null) {
            AxisService axisService = msgContext.getAxisService();
            if (axisService != null) {
                AxisServiceGroup asg = (AxisServiceGroup) axisService.getParent();
                if (asg != null) {
                    serviceGroupContext = (ServiceGroupContext)
                            applicationSessionServiceGroupContextTable.get(asg.getServiceGroupName());
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
        synchronized (serviceGroupContextMap) {
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
    }

    public ListenerManager getListenerManager() {
        return listenerManager;
    }

    public void setTransportManager(ListenerManager listenerManager) {
        this.listenerManager = listenerManager;
    }

    private void cleanupServiceContexts(ServiceGroupContext serviceGroupContext) {
        Iterator serviceContecxtes = serviceGroupContext.getServiceContexts();
        while (serviceContecxtes.hasNext()) {
            ServiceContext serviceContext = (ServiceContext) serviceContecxtes.next();
            try {
                DependencyManager.destroyServiceObject(serviceContext);
            } catch (AxisFault axisFault) {
                log.info(axisFault.getMessage());
            }
        }
    }

    public void cleanupContexts() {
        if (applicationSessionServiceGroupContextTable.size() > 0) {
            Iterator applicationScopeSgs =
                    applicationSessionServiceGroupContextTable.values().iterator();
            while (applicationScopeSgs.hasNext()) {
                ServiceGroupContext serviceGroupContext =
                        (ServiceGroupContext) applicationScopeSgs.next();
                cleanupServiceContexts(serviceGroupContext);
            }
        }
        if (serviceGroupContextMap.size() > 0) {
            Iterator sopaSessionSgs = serviceGroupContextMap.values().iterator();
            while (sopaSessionSgs.hasNext()) {
                ServiceGroupContext serviceGroupContext =
                        (ServiceGroupContext) sopaSessionSgs.next();
                cleanupServiceContexts(serviceGroupContext);
            }
        }
    }

    public String getServiceContextPath() {
        String ctxRoot = getContextRoot().trim();
        String path = "/";
        if (!ctxRoot.equals("/")) {
            path = ctxRoot + "/";
        }
        if (servicePath == null || servicePath.trim().length() == 0) {
            throw new IllegalArgumentException("service path cannot be null or empty");
        } else {
            path += servicePath.trim();
        }
        return path;
    }

    public String getRESTContextPath() {
        String ctxRoot = getContextRoot().trim();
        String path = "/";
        if (!ctxRoot.equals("/")) {
            path = ctxRoot + "/";
        }
        if (restPath == null || restPath.trim().length() == 0) {
            throw new IllegalArgumentException("service path cannot be null or empty");
        } else {
            path += restPath.trim();
        }
        return path;
    }

    public String getServicePath() {
        if (servicePath == null || servicePath.trim().length() == 0) {
            throw new IllegalArgumentException("service path cannot be null or empty");
        }
        return servicePath.trim();
    }

    public String getRESTPath() {
        if (restPath == null || restPath.trim().length() == 0) {
            throw new IllegalArgumentException("REST path cannot be null or empty");
        }
        return restPath.trim();
    }

    public String getContextRoot() {
        if (contextRoot == null || contextRoot.trim().length() == 0) {
            throw new IllegalArgumentException("context root cannot be null or empty");
        }
        return this.contextRoot.trim();
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public void setRESTPath(String restPath) {
        this.restPath = restPath;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    /**
     * This will be used to fetch the serviceGroupContextTimoutInterval from any place available.
     */
    public long getServiceGroupContextTimoutInterval() {
        Integer serviceGroupContextTimoutIntervalParam =
                (Integer) getProperty(Constants.Configuration.CONFIG_CONTEXT_TIMOUT_INTERVAL);
        if (serviceGroupContextTimoutIntervalParam != null) {
            serviceGroupContextTimoutInterval = serviceGroupContextTimoutIntervalParam.intValue();
        }
        return serviceGroupContextTimoutInterval;
    }
}
