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

package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.cluster.ClusterManager;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.dataretrieval.AxisDataLocator;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.ModuleDeployer;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.*;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.util.TargetResolver;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.java2wsdl.Java2WSDLConstants;

import javax.xml.namespace.QName;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.*;
import java.io.File;

/**
 * Class AxisConfiguration
 */
public class AxisConfiguration extends AxisDescription {

    private static final Log log = LogFactory.getLog(AxisConfiguration.class);
    /* 
     * To store configured data locators
     */
    private HashMap dataLocators = new HashMap();
    private HashMap dataLocatorClassNames = new HashMap();
    /**
     * Field modules
     */
    // private final HashMap defaultModules = new HashMap();
    //
    // To store all the available modules (including version)
    private final HashMap allModules = new HashMap();

    // To store mapping between default version and module name
    private final HashMap nameToversionMap = new HashMap();

    // private final HashMap serviceGroups = new HashMap();
    private final HashMap transportsIn = new HashMap();

    private final HashMap transportsOut = new HashMap();

    private final HashMap policySupportedModules = new HashMap();

    /**
     * Stores the QNames of local policy assertions
     */
    private final ArrayList localPolicyAssertions = new ArrayList();

    // to store AxisObserver Objects
    private ArrayList observersList = null;

    private URL axis2Repository = null;

    private HashMap allServices = new HashMap();
    private HashMap allEndpoints = new HashMap();

    /**
     * Stores the module specified in the server.xml at the document parsing time.
     */
    private List globalModuleList;

    /**
     * Field engagedModules
     */
    private final List engagedModules;

    private Hashtable faultyModules;

    /**
     * To store faulty services
     */
    private Hashtable faultyServices;

    private ArrayList inFaultPhases;

    private ArrayList inPhasesUptoAndIncludingPostDispatch;

    private HashMap messageReceivers;

    private HashMap messageBuilders;

    private HashMap messageFormatters;

    private ClassLoader moduleClassLoader;

    private HashMap moduleConfigmap;

    private ArrayList outFaultPhases;

    private ArrayList outPhases;

    protected PhasesInfo phasesinfo;

    private ClassLoader serviceClassLoader;

    private ClassLoader systemClassLoader;

    //To keep track of whether the system has started or not
    private boolean start;

    private ArrayList targetResolvers;

    private ClusterManager clusterManager;

    /**
     * Constructor AxisConfigurationImpl.
     */
    public AxisConfiguration() {
        moduleConfigmap = new HashMap();
        engagedModules = new ArrayList();
        globalModuleList = new ArrayList();
        messageReceivers = new HashMap();
        messageBuilders = new HashMap();
        messageFormatters = new HashMap();
        outPhases = new ArrayList();
        inFaultPhases = new ArrayList();
        outFaultPhases = new ArrayList();
        faultyServices = new Hashtable();
        faultyModules = new Hashtable();
        observersList = new ArrayList();
        inPhasesUptoAndIncludingPostDispatch = new ArrayList();
        systemClassLoader = (ClassLoader) org.apache.axis2.java.security.AccessController
                .doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
        serviceClassLoader = systemClassLoader;
        moduleClassLoader = systemClassLoader;

        this.phasesinfo = new PhasesInfo();
        targetResolvers = new ArrayList();
    }

    public void addMessageReceiver(String mepURL,
                                   MessageReceiver messageReceiver) {
        messageReceivers.put(mepURL, messageReceiver);
    }

    /**
     * Register a messageBuilder implementation against a content type.
     * This is used by Axis2 to support different message formats.
     *
     * @param contentType
     * @param messageBuilder
     */
    public void addMessageBuilder(String contentType,
                                  Builder messageBuilder) {
        messageBuilders.put(contentType, messageBuilder);
    }

    /**
     * Register a messageFormatter implementation against a content type.
     * This is used by Axis2 to support serialization of messages to different
     * message formats. (Eg: JSON)
     *
     * @param contentType
     * @param messageFormatter
     */
    public void addMessageFormatter(String contentType,
                                    MessageFormatter messageFormatter) {
        messageFormatters.put(contentType, messageFormatter);
    }

    /**
     * Method addModule.
     *
     * @param module
     * @throws AxisFault
     */
    public void addModule(AxisModule module) throws AxisFault {
        module.setParent(this);
        notifyObservers(AxisEvent.MODULE_DEPLOY, module);

        String moduleName = module.getName();
        if (moduleName.endsWith("SNAPSHOT")) {
            moduleName = moduleName.substring(0, moduleName.indexOf("SNAPSHOT") - 1);
            module.setName(moduleName);
            allModules.put(moduleName, module);
        } else {
            allModules.put(module.getName(), module);
        }

        // Registering the policy namespaces that the module understand
        registerModulePolicySupport(module);
        // Registering the policy assertions that are local to the system
        registerLocalPolicyAssertions(module);

    }

    public void deployModule(String moduleFileName) throws DeploymentException {
        File moduleFile = new File(moduleFileName);
        if (!moduleFile.exists()) {
            throw new DeploymentException("Module archive '" + moduleFileName + "' doesn't exist");
        }
        DeploymentFileData dfd = new DeploymentFileData(moduleFile, new ModuleDeployer(this));
        dfd.deploy();
    }

    /**
     * To remove a given module from the system
     *
     * @param module
     */
    public void removeModule(String module) {
        allModules.remove(module);
        // TODO disengage has to be done here
    }

    /**
     * Adds module configuration, if there is a moduleConfig tag in service.
     *
     * @param moduleConfiguration
     */
    public void addModuleConfig(ModuleConfiguration moduleConfiguration) {
        moduleConfigmap.put(moduleConfiguration.getModuleName(),
                            moduleConfiguration);
    }

    public void addObservers(AxisObserver axisObserver) {
        observersList.add(axisObserver);
    }

    /**
     * Method addService.
     *
     * @param service
     * @throws AxisFault
     */
    public synchronized void addService(AxisService service) throws AxisFault {
        AxisServiceGroup axisServiceGroup = new AxisServiceGroup();
        axisServiceGroup.setServiceGroupName(service.getName());
        axisServiceGroup.setParent(this);
        axisServiceGroup.addService(service);
        addServiceGroup(axisServiceGroup);
    }

    public synchronized void addServiceGroup(AxisServiceGroup axisServiceGroup)
            throws AxisFault {
        notifyObservers(AxisEvent.SERVICE_DEPLOY, axisServiceGroup);
        axisServiceGroup.setParent(this);
        AxisService axisService;

        Iterator services = axisServiceGroup.getServices();
        while (services.hasNext()) {
            axisService = (AxisService) services.next();
            String serviceName = axisService.getName();
            if (allServices.get(serviceName) != null) {
                throw new AxisFault(Messages.getMessage(
                        "twoservicecannothavesamename", axisService.getName()));
            }
            if (axisService.getSchematargetNamespace() == null) {
                axisService
                        .setSchematargetNamespace(Java2WSDLConstants.AXIS2_XSD);
            }
        }
        services = axisServiceGroup.getServices();
        while (services.hasNext()) {
            axisService = (AxisService) services.next();
            if (axisService.isUseDefaultChains()) {
                Iterator operations = axisService.getOperations();
                while (operations.hasNext()) {
                    AxisOperation operation = (AxisOperation) operations.next();
                    phasesinfo.setOperationPhases(operation);
                }
            }
        }
        Iterator enModule = engagedModules.iterator();
        while (enModule.hasNext()) {
            String moduleName = (String) enModule.next();
            axisServiceGroup.engageModule(getModule(moduleName), this);
        }
        services = axisServiceGroup.getServices();
        while (services.hasNext()) {
            axisService = (AxisService) services.next();

            Map endpoints = axisService.getEndpoints();
            String serviceName = axisService.getName();
            addToAllServicesMap(serviceName, axisService);
            if (endpoints != null) {
                Iterator endpointNameIter = endpoints.keySet().iterator();
                while (endpointNameIter.hasNext()) {
                    String endpointName = (String) endpointNameIter.next();
                    allEndpoints.put(serviceName + "." + endpointName, axisService);
                }
            }

            if (!axisService.isClientSide()) {
                notifyObservers(AxisEvent.SERVICE_DEPLOY, axisService);
            }
        }
        // serviceGroups.put(axisServiceGroup.getServiceGroupName(),
        // axisServiceGroup);
        addChild(axisServiceGroup);
    }

    public void addToAllServicesMap(String serviceName, AxisService axisService) {
        allServices.put(serviceName, axisService);
    }

    public AxisServiceGroup removeServiceGroup(String serviceGroupName) throws AxisFault {
        AxisServiceGroup axisServiceGroup = (AxisServiceGroup) getChild(serviceGroupName);
        if (axisServiceGroup == null) {
            throw new AxisFault(Messages.getMessage("invalidservicegroupname",
                                                    serviceGroupName));
        }
        Iterator services = axisServiceGroup.getServices();
        while (services.hasNext()) {
            AxisService axisService = (AxisService) services.next();
            allServices.remove(axisService.getName());
            if (!axisService.isClientSide()) {
                notifyObservers(AxisEvent.SERVICE_REMOVE, axisService);
            }
        }
        removeChild(serviceGroupName);
        notifyObservers(AxisEvent.SERVICE_REMOVE, axisServiceGroup);
        return axisServiceGroup;
    }

    /**
     * Method addTransportIn.
     *
     * @param transport
     * @throws AxisFault
     */
    public void addTransportIn(TransportInDescription transport) throws AxisFault {
        if (transport.getReceiver() == null) {
            throw new AxisFault(
                    "Transport Receiver can not be null for the transport "
                    + transport.getName());
        }
        transportsIn.put(transport.getName(), transport);
    }

    /**
     * Method addTransportOut.
     *
     * @param transport
     * @throws AxisFault
     */
    public void addTransportOut(TransportOutDescription transport)
            throws AxisFault {
        if (transport.getSender() == null) {
            throw new AxisFault(
                    "Transport sender can not be null for the transport "
                    + transport.getName());
        }
        transportsOut.put(transport.getName(), transport);
    }

    /**
     * Engages the default module version corresponding to the given module name,
     * or if the module name contains version number in it then it will engage
     * the correct module. Both the below cases are valid : -
     * 1. engageModule("addressing"); 2. engageModule("addressing-1.23");
     *
     * @param moduleref QName of module to engage
     * @throws AxisFault
     * @deprecated Please use the String version instead
     */
    public void engageModule(QName moduleref) throws AxisFault {
        engageModule(moduleref.getLocalPart());
    }

    /**
     * Engages the default module version corresponding to given module name ,
     * or if the module name contains version number in it then it will engage
     * the correct module. Both of the below two cases are valid 1.
     * engageModule("addressing"); 2. engageModule("addressing-1.23");
     *
     * @param moduleref name of module to engage
     * @throws AxisFault
     */
    public void engageModule(String moduleref) throws AxisFault {
        AxisModule module = getModule(moduleref);
        if (module != null) {
            engageModule(module);
        } else {
            throw new AxisFault(Messages.getMessage("modulenotavailble", moduleref));
        }
    }

    /**
     * Engages a module using given name and its version ID.
     *
     * @param moduleName
     * @param versionID
     * @throws AxisFault
     */
    public void engageModule(String moduleName, String versionID)
            throws AxisFault {
        String actualName = Utils.getModuleName(moduleName, versionID);
        AxisModule module = getModule(actualName);
        if (module != null) {
            engageModule(module);
        } else {
            throw new AxisFault(Messages.getMessage("refertoinvalidmodule"));
        }
    }

    public void engageModule(AxisModule axisModule, AxisConfiguration axisConfig)
            throws AxisFault {
        engageModule(axisModule);
    }

    private void engageModule(AxisModule module) throws AxisFault {
        boolean isEngagable;
        if (module != null) {
            String moduleName = module.getName();
            for (Iterator iterator = engagedModules.iterator(); iterator
                    .hasNext();) {
                String thisModule = (String) iterator.next();

                isEngagable = Utils.checkVersion(moduleName, thisModule);
                if (!isEngagable) {
                    return;
                }
            }
        } else {
            throw new AxisFault(Messages.getMessage("refertoinvalidmodule"));
        }
        Iterator servicegroups = getServiceGroups();
        while (servicegroups.hasNext()) {
            AxisServiceGroup serviceGroup = (AxisServiceGroup) servicegroups
                    .next();
            serviceGroup.engageModule(module, this);
        }
        engagedModules.add(module.getName());
    }

    /**
     * To dis-engage a module from the system. This will remove all the handlers
     * belonging to this module from all the handler chains.
     *
     * @param module
     */
    public void disengageModule(AxisModule module) {
        if (module != null && isEngaged(module.getName())) {
            PhaseResolver phaseResolver = new PhaseResolver(this);
            phaseResolver.disengageModuleFromGlobalChains(module);
            Iterator serviceItr = getServices().values().iterator();
            while (serviceItr.hasNext()) {
                AxisService axisService = (AxisService) serviceItr.next();
                axisService.disengageModule(module);
            }
            Iterator serviceGroups = getServiceGroups();
            while (serviceGroups.hasNext()) {
                AxisServiceGroup axisServiceGroup = (AxisServiceGroup) serviceGroups
                        .next();
                axisServiceGroup.removeFromEngageList(module.getName());
            }
            engagedModules.remove(module.getName());
        }
    }

    public void notifyObservers(int event_type, AxisService service) {
        AxisEvent event = new AxisEvent(event_type);

        for (int i = 0; i < observersList.size(); i++) {
            AxisObserver axisObserver = (AxisObserver) observersList.get(i);

            try {
                if (!service.isClientSide()) {
                    axisObserver.serviceUpdate(event, service);
                }
            } catch (Throwable e) {
                // No need to stop the system due to this, so log and ignore
                log.debug(e);
            }
        }
    }

    public void notifyObservers(int event_type, AxisModule moule) {
        AxisEvent event = new AxisEvent(event_type);

        for (int i = 0; i < observersList.size(); i++) {
            AxisObserver axisObserver = (AxisObserver) observersList.get(i);

            try {
                axisObserver.moduleUpdate(event, moule);
            } catch (Throwable e) {
                // No need to stop the system due to this, so log and ignore
                log.debug(e);
            }
        }
    }

    public void notifyObservers(int event_type, AxisServiceGroup serviceGroup) {
        AxisEvent event = new AxisEvent(event_type);

        for (int i = 0; i < observersList.size(); i++) {
            AxisObserver axisObserver = (AxisObserver) observersList.get(i);

            try {
                axisObserver.serviceGroupUpdate(event, serviceGroup);
            } catch (Throwable e) {
                // No need to stop the system due to this, so log and ignore
                log.debug(e);
            }
        }
    }

    /**
     * Method removeService.
     *
     * @param name
     * @throws AxisFault
     */
    public synchronized void removeService(String name) throws AxisFault {
        AxisService service = (AxisService) allServices.remove(name);
        if (service != null) {
            AxisServiceGroup serviceGroup = (AxisServiceGroup) service.getParent();
            serviceGroup.removeService(name);
            log.debug(Messages.getMessage("serviceremoved", name));
        }
    }

    /**
     * Method getEngagedModules.
     *
     * @return Collection
     */
    public Collection getEngagedModules() {
        return engagedModules;
    }

    public List getGlobalModules() {
        return globalModuleList;
    }

    public Hashtable getFaultyModules() {
        return faultyModules;
    }

    public Hashtable getFaultyServices() {
        return faultyServices;
    }

    public void removeFaultyService(String key) {
        Iterator itr = faultyServices.keySet().iterator();
        while (itr.hasNext()) {
            String fullFileName = (String) itr.next();
            if (fullFileName.indexOf(key) > 0) {
                faultyServices.remove(fullFileName);
                return;
            }
        }
    }

    // to get the out flow correpodning to the global out flow;
    public ArrayList getGlobalOutPhases() {
        return this.outPhases;
    }

    /**
     * @return Returns ArrayList.
     */
    public ArrayList getInFaultFlow() {
        return inFaultPhases;
    }

    public ArrayList getGlobalInFlow() {
        return inPhasesUptoAndIncludingPostDispatch;
    }

    public MessageReceiver getMessageReceiver(String mepURL) {
        return (MessageReceiver) messageReceivers.get(mepURL);
    }

    /**
     * @param contentType
     * @return the configured message builder implementation class name against
     *         the given content type.
     */
    public Builder getMessageBuilder(String contentType) {
        return (Builder) messageBuilders.get(contentType);
    }

    /**
     * @param contentType
     * @return the configured message formatter implementation class name
     *         against the given content type.
     */
    public MessageFormatter getMessageFormatter(String contentType) {
        return (MessageFormatter) messageFormatters.get(contentType);
    }

//    /**
//     *
//     * @deprecate Please use String version instead
//     * @param qname
//     * @return
//     */
//    public AxisModule getModule(QName qname) {
//        return getModule(qname.getLocalPart());
//    }

    /**
     * Method getModule. First it will check whether the given module is there
     * in the hashMap, if so returns that and the name, which can be either with
     * version string or without version string. <p/> If its not found and the
     * name does not contain the version string in it then checks whether the default
     * version of the module is available in the sytem for the given name, then returns
     * that.
     *
     * @param name
     * @return Returns ModuleDescription.
     */
    public AxisModule getModule(String name) {
        AxisModule module = (AxisModule) allModules.get(name);
        if (module != null) {
            return module;
        }
        // checks whether the version string seperator is not there in the
        // module name
        String moduleName = name;
        String defaultModuleVersion = getDefaultModuleVersion(moduleName);
        if (defaultModuleVersion != null) {
            module = (AxisModule) allModules.get(Utils.getModuleName(
                    moduleName, defaultModuleVersion));
            if (module != null) {
                return module;
            }
        }
        return null;
    }

    // the class loader that becomes the parent of all the modules
    public ClassLoader getModuleClassLoader() {
        return this.moduleClassLoader;
    }

    public ModuleConfiguration getModuleConfig(String moduleName) {
        return (ModuleConfiguration) moduleConfigmap.get(moduleName);
    }

    /**
     * @return Returns HashMap.
     */
    public HashMap getModules() {
        return allModules;
    }

    /**
     * @return Returns ArrayList.
     */
    public ArrayList getOutFaultFlow() {
        return outFaultPhases;
    }

    public PhasesInfo getPhasesInfo() {
        return phasesinfo;
    }

    public URL getRepository() {
        return axis2Repository;
    }

    /**
     * Method getService.
     *
     * @param name
     * @return Returns AxisService.
     */
    public AxisService getService(String name) throws AxisFault {
        AxisService axisService = (AxisService) allServices.get(name);
        if (axisService != null) {
            if (axisService.isActive()) {
                return axisService;
            } else {
                throw new AxisFault(Messages
                        .getMessage("serviceinactive", name));
            }
        } else {
            axisService = (AxisService) allEndpoints.get(name);
            if (axisService != null) {
                if (axisService.isActive()) {
                    return axisService;
                } else {
                    throw new AxisFault(Messages
                            .getMessage("serviceinactive", name));
                }
            }
        }
        return null;
    }

    /**
     * Service can start and stop, once stopped it cannot be accessed, so we
     * need a way to get the service even if service is not active.
     *
     * @return AxisService
     */
    public AxisService getServiceForActivation(String serviceName) {
        AxisService axisService = (AxisService) allServices.get(serviceName);
        if (axisService != null) {
            return axisService;
        } else {
            return null;
        }
    }

    // The class loader that becomes the parent of all the services
    public ClassLoader getServiceClassLoader() {
        return this.serviceClassLoader;
    }

    public AxisServiceGroup getServiceGroup(String serviceNameAndGroupString) {
        // return (AxisServiceGroup)
        // serviceGroups.get(serviceNameAndGroupString);
        return (AxisServiceGroup) getChild(serviceNameAndGroupString);
    }

    public Iterator getServiceGroups() {
        // return serviceGroups.values().iterator();
        return getChildren();
    }

    // To get all the services in the system
    public HashMap getServices() {
        Iterator sgs = getServiceGroups();

        while (sgs.hasNext()) {
            AxisServiceGroup axisServiceGroup = (AxisServiceGroup) sgs.next();
            Iterator servics = axisServiceGroup.getServices();

            while (servics.hasNext()) {
                AxisService axisService = (AxisService) servics.next();
                addToAllServicesMap(axisService.getName(), axisService);
            }
        }

        return allServices;
    }

    // The class loader which become the top most parent of all the modules and
    // services
    public ClassLoader getSystemClassLoader() {
        return this.systemClassLoader;
    }

    public TransportInDescription getTransportIn(String name) {
        return (TransportInDescription) transportsIn.get(name);
    }

    public TransportOutDescription getTransportOut(String name) {
        return (TransportOutDescription) transportsOut.get(name);
    }

    public HashMap getTransportsIn() {
        return transportsIn;
    }

    public HashMap getTransportsOut() {
        return transportsOut;
    }

    public boolean isEngaged(String moduleName) {
        boolean isEngaged = engagedModules.contains(moduleName);
        AxisModule defaultModule = getDefaultModule(moduleName);
        if (!isEngaged && defaultModule != null) {
            isEngaged = engagedModules.contains(defaultModule.getName());
        }
        return isEngaged;
    }

    public void setGlobalOutPhase(ArrayList outPhases) {
        this.outPhases = outPhases;
    }

    /**
     * @param list
     */
    public void setInFaultPhases(ArrayList list) {
        inFaultPhases = list;
    }

    public void setInPhasesUptoAndIncludingPostDispatch(
            ArrayList inPhasesUptoAndIncludingPostDispatch) {
        this.inPhasesUptoAndIncludingPostDispatch = inPhasesUptoAndIncludingPostDispatch;
    }

    public void setModuleClassLoader(ClassLoader classLoader) {
        this.moduleClassLoader = classLoader;
    }

    /**
     * @param list
     */
    public void setOutFaultPhases(ArrayList list) {
        outFaultPhases = list;
    }

    public void setPhasesInfo(PhasesInfo phasesInfo) {
        this.phasesinfo = phasesInfo;
    }

    public void setRepository(URL axis2Repository) {
        this.axis2Repository = axis2Repository;
    }

    public void setServiceClassLoader(ClassLoader classLoader) {
        this.serviceClassLoader = classLoader;
    }

    public void setSystemClassLoader(ClassLoader classLoader) {
        this.systemClassLoader = classLoader;
    }

    /**
     * Adds a default module version, which can be done either programmatically
     * or by using axis2.xml. The default module version is important if user
     * asks to engage a module without given version ID, in which case,
     * the default version is engaged.
     *
     * @param moduleName
     * @param moduleVersion
     */
    public void addDefaultModuleVersion(String moduleName, String moduleVersion) {
        if (nameToversionMap.get(moduleName) == null) {
            nameToversionMap.put(moduleName, moduleVersion);
        }
    }

    public String getDefaultModuleVersion(String moduleName) {
        return (String) nameToversionMap.get(moduleName);
    }

    public AxisModule getDefaultModule(String moduleName) {
        String defaultModuleVersion = getDefaultModuleVersion(moduleName);
        if (defaultModuleVersion == null) {
            return (AxisModule) allModules.get(moduleName);
        } else {
            return (AxisModule) allModules.get(moduleName + "-" + defaultModuleVersion);
        }
    }

    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    public void setClusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    public Object getKey() {
        return toString();
    }

    public void stopService(String serviceName) throws AxisFault {
        AxisService service = (AxisService) allServices.get(serviceName);
        if (service == null) {
            throw new AxisFault(Messages.getMessage("servicenamenotvalid",
                                                    serviceName));
        }
        service.setActive(false);
        notifyObservers(AxisEvent.SERVICE_STOP, service);
    }

    public void startService(String serviceName) throws AxisFault {
        AxisService service = (AxisService) allServices.get(serviceName);
        if (service == null) {
            throw new AxisFault(Messages.getMessage("servicenamenotvalid",
                                                    serviceName));
        }
        service.setActive(true);
        notifyObservers(AxisEvent.SERVICE_START, service);
    }

    public List getModulesForPolicyNamesapce(String namesapce) {
        return (List) policySupportedModules.get(namesapce);
    }

    public void registerModulePolicySupport(AxisModule axisModule) {
        String[] namespaces = axisModule.getSupportedPolicyNamespaces();

        if (namespaces == null) {
            return;
        }

        List modulesList;

        for (int i = 0; i < namespaces.length; i++) {
            modulesList = (List) policySupportedModules.get(namespaces[i]);

            if (modulesList != null) {
                modulesList.add(axisModule);
            } else {
                modulesList = new ArrayList();
                modulesList.add(axisModule);
                policySupportedModules.put(namespaces[i], modulesList);
            }
        }
    }

    public void registerLocalPolicyAssertions(AxisModule axisModule) {
        QName[] localPolicyAssertions = axisModule.getLocalPolicyAssertions();

        if (localPolicyAssertions == null) {
            return;
        }

        for (int i = 0; i < localPolicyAssertions.length; i++) {
            addLocalPolicyAssertion(localPolicyAssertions[i]);
        }
    }

    public ArrayList getObserversList() {
        return observersList;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    /**
     * getTargetResolverChain returns an instance of
     * TargetResolver which iterates over the registered
     * TargetResolvers, calling each one in turn when
     * resolveTarget is called.
     */
    public TargetResolver getTargetResolverChain() {
        if (targetResolvers.isEmpty()) {
            return null;
        }
        return new TargetResolver() {
            public void resolveTarget(MessageContext messageContext) {
                Iterator iter = targetResolvers.iterator();
                while (iter.hasNext()) {
                    TargetResolver tr = (TargetResolver) iter.next();
                    tr.resolveTarget(messageContext);
                }
            }
        };
    }

    public void addTargetResolver(TargetResolver tr) {
        targetResolvers.add(tr);
    }

    public void addLocalPolicyAssertion(QName name) {
        this.localPolicyAssertions.add(name);
    }

    public List getLocalPolicyAssertions() {
        return this.localPolicyAssertions;
    }

    public void removeLocalPolicyAssertion(QName name) {
        this.localPolicyAssertions.remove(name);
    }

    public boolean isAssertionLocal(QName name) {
        return this.localPolicyAssertions.contains(name);
    }

    /**
     * Allows to define/configure Data Locator for specified dialect at Axis 2 Configuration.
     *
     * @param dialect-  an absolute URI represents the format and version of data
     * @param classname - class name of the Data Locator configured to support retrieval
     *                  for the specified dialect.
     */
    public void addDataLocatorClassNames(String dialect, String classname) {
        dataLocatorClassNames.put(dialect, classname);
    }

    /**
     * For internal used only! To store instance of DataLocator when it is first loaded. This allows to
     * reuse of the DataLocator after it is initially loaded.
     *
     * @param dialect-    an absolute URI represents the format and version of data
     * @param dataLocator - specified an DataLocator instance  to support retrieval
     *                    of the specified dialect.
     */
    public void addDataLocator(String dialect, AxisDataLocator dataLocator) {
        dataLocators.put(dialect, dataLocator);
    }

    /**
     * Return DataLocator instance for specified dialect.
     */
    public AxisDataLocator getDataLocator(String dialect) {
        return (AxisDataLocator) dataLocators.get(dialect);
    }


    /**
     * Return classname of DataLocator configured for specified dialect.
     */
    public String getDataLocatorClassName(String dialect) {
        return (String) dataLocatorClassNames.get(dialect);
    }


    /**
     * Checks whether the system pre-defined phases
     * for all the flows, have been changed. If they have been changed, throws a DeploymentException.
     *
     * @throws org.apache.axis2.deployment.DeploymentException
     *
     */
    public void validateSystemPredefinedPhases() throws DeploymentException {
        PhasesInfo phasesInfo = getPhasesInfo();
        setInPhasesUptoAndIncludingPostDispatch(phasesInfo.getGlobalInflow());
        setInFaultPhases(phasesInfo.getGlobalInFaultPhases());
        setGlobalOutPhase(phasesInfo.getGlobalOutPhaseList());
        setOutFaultPhases(phasesInfo.getOUT_FaultPhases());
    }
}
