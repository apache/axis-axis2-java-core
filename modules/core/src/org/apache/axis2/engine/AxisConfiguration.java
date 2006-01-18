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
import org.apache.axis2.Constants;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.*;
import org.apache.axis2.receivers.RawXMLINOnlyMessageReceiver;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.util.HostConfiguration;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.*;

/**
 * Class AxisConfigurationImpl
 */
public class AxisConfiguration extends AxisDescription {

    private Log log = LogFactory.getLog(getClass());
    /**
     * Field modules
     */
//    private final HashMap defaultModules = new HashMap();
//
    //to store all the availble modules (including version)
    private final HashMap allModules = new HashMap();

    //to store mapping between default version to module name
    private final HashMap nameToverionMap = new HashMap();

    private final HashMap serviceGroups = new HashMap();
    private final HashMap transportsIn = new HashMap();
    private final HashMap transportsOut = new HashMap();

    // to store AxisObserver Objects
    private ArrayList observersList = null;
    private String axis2Repository = null;
    private HashMap allservices = new HashMap();

    /**
     * Field engagedModules
     */
    private final List engagedModules;
    private Hashtable faultyModules;
    /**
     * To store faulty services
     */
    private Hashtable faultyServices;

    // to store host configuration if any
    private HostConfiguration hostConfiguration;
    private ArrayList inFaultPhases;
    private ArrayList inPhasesUptoAndIncludingPostDispatch;
    private HashMap messageReceivers;

    private ClassLoader moduleClassLoader;
    private HashMap moduleConfigmap;
    private ArrayList outFaultPhases;
    private ArrayList outPhases;
    protected PhasesInfo phasesinfo;
    private ClassLoader serviceClassLoader;
    private ClassLoader systemClassLoader;

    /**
     * Constructor AxisConfigurationImpl.
     */
    public AxisConfiguration() {
        moduleConfigmap = new HashMap();
        engagedModules = new ArrayList();
        messageReceivers = new HashMap();
        outPhases = new ArrayList();
        inFaultPhases = new ArrayList();
        outFaultPhases = new ArrayList();
        faultyServices = new Hashtable();
        faultyModules = new Hashtable();
        observersList = new ArrayList();
        inPhasesUptoAndIncludingPostDispatch = new ArrayList();

        systemClassLoader = Thread.currentThread().getContextClassLoader();
        serviceClassLoader = Thread.currentThread().getContextClassLoader();
        moduleClassLoader = Thread.currentThread().getContextClassLoader();

        this.phasesinfo = new PhasesInfo();

        // setting the default flow , if some one creating AxisConfig programatically
        // most required handles will be there in the flow.

        // todo we need to fix this , we know that we are doing wrong thing here
        createDefaultChain();
        //setting default message receivers
        addDefaultMessageReceivers();
    }

    public void addMessageReceiver(String mepURL, MessageReceiver messageReceiver) {
        messageReceivers.put(mepURL, messageReceiver);
    }

    /**
     * This is required if we are going to create AxisConfiguration programatically
     * in that case , no dafault message recivers will there be in the system
     */
    private void addDefaultMessageReceivers() {
        addMessageReceiver("http://www.w3.org/2004/08/wsdl/in-only", new RawXMLINOnlyMessageReceiver());
        addMessageReceiver("http://www.w3.org/2004/08/wsdl/in-out", new RawXMLINOutMessageReceiver());
    }

    /**
     * Method addModule.
     *
     * @param module
     * @throws AxisFault
     */
    public void addModule(ModuleDescription module) throws AxisFault {
        module.setParent(this);
        allModules.put(module.getName(), module);
    }

    /**
     * Adds module configuration, if there is moduleConfig tag in service.
     *
     * @param moduleConfiguration
     */
    public void addModuleConfig(ModuleConfiguration moduleConfiguration) {
        moduleConfigmap.put(moduleConfiguration.getModuleName(), moduleConfiguration);
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

    public synchronized void addServiceGroup(AxisServiceGroup axisServiceGroup) throws AxisFault {
        Iterator services = axisServiceGroup.getServices();

        axisServiceGroup.setParent(this);

        AxisService description;

        while (services.hasNext()) {
            description = (AxisService) services.next();

            if (allservices.get(description.getName()) != null) {
                throw new AxisFault("Two services can not have same name, a service with "
                        + description.getName() + " already exists in the system");
            }
        }

        services = axisServiceGroup.getServices();

        while (services.hasNext()) {
            description = (AxisService) services.next();
            if (description.isUseDefaultChains()) {
                Iterator operations = description.getOperations().values().iterator();
                while (operations.hasNext()) {
                    AxisOperation operation = (AxisOperation) operations.next();
                    phasesinfo.setOperationPhases(operation);
                }
            }
            allservices.put(description.getName(), description);
            notifyObservers(AxisEvent.SERVICE_DEPLOY, description);
        }

        Iterator enModule = engagedModules.iterator();

        while (enModule.hasNext()) {
            QName moduleName = (QName) enModule.next();
            axisServiceGroup.engageModule(getModule(moduleName));
        }

        serviceGroups.put(axisServiceGroup.getServiceGroupName(), axisServiceGroup);
    }

    /**
     * Method addTransportIn.
     *
     * @param transport
     * @throws AxisFault
     */
    public void addTransportIn(TransportInDescription transport) throws AxisFault {
        transportsIn.put(transport.getName(), transport);
    }

    /**
     * Method addTransportOut.
     *
     * @param transport
     * @throws AxisFault
     */
    public void addTransportOut(TransportOutDescription transport) throws AxisFault {
        transportsOut.put(transport.getName(), transport);
    }

    private void createDefaultChain() {
        Phase transportIN = new Phase("TransportIn");
        Phase preDispatch = new Phase("PreDispatch");
        DispatchPhase dispatchPhase = new DispatchPhase();

        dispatchPhase.setName("Dispatch");

        AddressingBasedDispatcher abd = new AddressingBasedDispatcher();

        abd.initDispatcher();

        RequestURIBasedDispatcher rud = new RequestURIBasedDispatcher();

        rud.initDispatcher();

        SOAPActionBasedDispatcher sabd = new SOAPActionBasedDispatcher();

        sabd.initDispatcher();

        SOAPMessageBodyBasedDispatcher smbd = new SOAPMessageBodyBasedDispatcher();

        smbd.initDispatcher();

        InstanceDispatcher id = new InstanceDispatcher();

        id.init(new HandlerDescription(new QName("InstanceDispatcher")));
        dispatchPhase.addHandler(abd);
        dispatchPhase.addHandler(rud);
        dispatchPhase.addHandler(sabd);
        dispatchPhase.addHandler(smbd);
        dispatchPhase.addHandler(id);
        inPhasesUptoAndIncludingPostDispatch.add(transportIN);
        inPhasesUptoAndIncludingPostDispatch.add(preDispatch);
        inPhasesUptoAndIncludingPostDispatch.add(dispatchPhase);
    }

    /**
     * Engages the default module version corresponding to given module name , or if the module
     * name contains version number in it then it will engage the correct module.
     * Both of the below two cases are valid
     * 1. engageModule("addressing");
     * 2. engageModule("addressing-1.23");
     *
     * @param moduleref
     * @throws AxisFault
     */
    public void engageModule(QName moduleref) throws AxisFault {
        ModuleDescription module = getModule(moduleref);
        if (module == null) {
            // there is no module found with the given name , so better check for dafult module version
            String moduleName = moduleref.getLocalPart();
            String defaultModuleVersion = getDefaultModuleVersion(moduleName);
            if (defaultModuleVersion != null) {
                QName moduleQName = Utils.getModuleName(moduleName, defaultModuleVersion);
                module = loadModulefromResources(moduleQName.getLocalPart());
            } else {
                module = loadModulefromResources(moduleName);
            }
        }
        engageModule(module, moduleref);
    }

    /**
     * Engages a module using give name and its version ID.
     *
     * @param moduleName
     * @param versionID
     * @throws AxisFault
     */
    public void engageModule(String moduleName, String versionID) throws AxisFault {
        QName moduleQName = Utils.getModuleName(moduleName, versionID);
        ModuleDescription module = getModule(moduleQName);
        if (module == null) {
            module = loadModulefromResources(moduleQName.getLocalPart());
            engageModule(module, moduleQName);
        } else {
            engageModule(module, moduleQName);
        }

    }

    private void engageModule(ModuleDescription module, QName moduleQName) throws AxisFault {
        if (module != null) {
            for (Iterator iterator = engagedModules.iterator(); iterator.hasNext();) {
                QName qName = (QName) iterator.next();
                if (moduleQName.equals(qName)) {
                    log.info("Attempt to engage an already engaged module " + qName);
                    return;
                }
            }
        } else {
            throw new AxisFault(this + " Refer to invalid module " + moduleQName.getLocalPart()
                    + " has not bean deployed yet !");
        }
        Iterator servicegroups = getServiceGroups();
        while (servicegroups.hasNext()) {
            AxisServiceGroup serviceGroup = (AxisServiceGroup) servicegroups.next();
            serviceGroup.engageModule(module);
        }
        engagedModules.add(module.getName());
    }

    /**
     * Loads module from class path - the mar files in a jar file inside
     * modules/    directory
     *
     * @param moduleName
     * @return Returns ModuleDescription.
     * @throws AxisFault
     */
    public ModuleDescription loadModulefromResources(String moduleName) throws AxisFault {
        ModuleDescription module;
        // trying to read from resources
        File file = new ArchiveReader().creatModuleArchivefromResource(moduleName,
                getRepository());
        module = new DeploymentEngine().buildModule(file, this);
        if (module != null) {
            // since it is a new module
            addModule(module);
        }
        return module;
    }

    public void notifyObservers(int event_type, AxisService service) {
        AxisEvent event = new AxisEvent(service, event_type);

        for (int i = 0; i < observersList.size(); i++) {
            AxisObserver axisObserver = (AxisObserver) observersList.get(i);

            axisObserver.update(event);
        }
    }

    /**
     * Method removeService.
     *
     * @param name
     * @throws AxisFault
     */
    public synchronized void removeService(String name) throws AxisFault {
        AxisService service = (AxisService) allservices.remove(name);

        if (service != null) {
            log.info("Removed service " + name);
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

    public Hashtable getFaultyModules() {
        return faultyModules;
    }

    public Hashtable getFaultyServices() {
        return faultyServices;
    }

    // to get the out flow correpodning to the global out flow;
    public ArrayList getGlobalOutPhases() {
        return this.outPhases;
    }

    public HostConfiguration getHostConfiguration() {
        return this.hostConfiguration;
    }

    /**
     * @return Returns ArrayList.
     */
    public ArrayList getInFaultFlow() {
        return inFaultPhases;
    }

    public ArrayList getInPhasesUptoAndIncludingPostDispatch() {
        return inPhasesUptoAndIncludingPostDispatch;
    }

    public MessageReceiver getMessageReceiver(String mepURL) {
        return (MessageReceiver) messageReceivers.get(mepURL);
    }

    /**
     * Method getModule.
     * first it will check whether the given module is there in the hashMap , if so just return that
     * and the name can be either with version string or without vresion string
     * <p/>
     * if it not found and , the nane does not have version string in it  then try to check
     * whether default vresion of module available in the sytem for the give name , if so return that
     *
     * @param name
     * @return Returns ModuleDescription.
     */
    public ModuleDescription getModule(QName name) {
        ModuleDescription module = (ModuleDescription) allModules.get(name);
        if (module != null) {
            return module;
        }
        String moduelName = name.getLocalPart();
        // checking whether the version string seperator is not there in the module name
        if (moduelName.indexOf('-') < 0) {
            String moduleName = name.getLocalPart();
            String defaultModuleVersion = getDefaultModuleVersion(moduleName);
            if (defaultModuleVersion != null) {
                module = (ModuleDescription) allModules.get(
                        Utils.getModuleName(moduleName, defaultModuleVersion));
                if (module != null) {
                    return module;
                }
            }
        }
        return null;
    }

    // the class loder that become the parent of all the modules
    public ClassLoader getModuleClassLoader() {
        return this.moduleClassLoader;
    }

    public ModuleConfiguration getModuleConfig(QName moduleName) {
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

    public String getRepository() {
        return axis2Repository;
    }

    /**
     * Method getService.
     *
     * @param name
     * @return Returns AxisService.
     */
    public AxisService getService(String name) {
        return (AxisService) allservices.get(name);
    }

    // the class loder that become the parent of all the services
    public ClassLoader getServiceClassLoader() {
        return this.serviceClassLoader;
    }

    public AxisServiceGroup getServiceGroup(String serviceNameAndGroupString) {
        return (AxisServiceGroup) serviceGroups.get(serviceNameAndGroupString);
    }

    public Iterator getServiceGroups() {
        return serviceGroups.values().iterator();
    }

    // to get all the services in the system
    public HashMap getServices() {
        Iterator sgs = getServiceGroups();

        while (sgs.hasNext()) {
            AxisServiceGroup axisServiceGroup = (AxisServiceGroup) sgs.next();
            Iterator servics = axisServiceGroup.getServices();

            while (servics.hasNext()) {
                AxisService axisService = (AxisService) servics.next();

                allservices.put(axisService.getName(), axisService);
            }
        }

        return allservices;
    }

    // the class loder which become the top most parent of all the modules and services
    public ClassLoader getSystemClassLoader() {
        return this.systemClassLoader;
    }

    public TransportInDescription getTransportIn(QName name) throws AxisFault {
        return (TransportInDescription) transportsIn.get(name);
    }

    public TransportOutDescription getTransportOut(QName name) throws AxisFault {
        return (TransportOutDescription) transportsOut.get(name);
    }

    public HashMap getTransportsIn() {
        return transportsIn;
    }

    public HashMap getTransportsOut() {
        return transportsOut;
    }

    public boolean isEngaged(QName moduleName) {
        return engagedModules.contains(moduleName);
    }

    /**
     * Checks whether a given parameter is locked.
     *
     * @param parameterName
     * @return Returns boolean.
     */
    public boolean isParameterLocked(String parameterName) {
        Parameter parameter = getParameter(parameterName);

        return (parameter != null) && parameter.isLocked();
    }

    public void setGlobalOutPhase(ArrayList outPhases) {
        this.outPhases = outPhases;
    }

    // to set and get host configuration
    public void setHostConfiguration(HostConfiguration hostConfiguration) {
        this.hostConfiguration = hostConfiguration;
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

    public void setPhasesinfo(PhasesInfo phasesInfo) {
        this.phasesinfo = phasesInfo;
    }

    public void setRepository(String axis2Repository) {
        this.axis2Repository = axis2Repository;
    }

    public void setServiceClassLoader(ClassLoader classLoader) {
        this.serviceClassLoader = classLoader;
    }

    public void setSystemClassLoader(ClassLoader classLoader) {
        this.systemClassLoader = classLoader;
    }

    public static String getAxis2HomeDirectory() {
        // if user has set the axis2 home variable try to get that from System properties
        String axis2home = System.getProperty(Constants.HOME_AXIS2);
        if (axis2home == null) {
            axis2home = System.getProperty(Constants.HOME_USER);
            if (axis2home != null) {
                axis2home = axis2home + '/' + DeploymentConstants.DIRECTORY_AXIS2_HOME;
            }
        }
        return axis2home;
    }

    /**
     * Adds a dafault module version , which can be done either programatically or by using
     * axis2.xml . The default module version is important if user asks to engage
     * a module without given version ID, in which case, we will engage the default version.
     *
     * @param moduleName
     * @param moduleVersion
     */
    public void addDefaultModuleVersion(String moduleName, String moduleVersion) {
        if (nameToverionMap.get(moduleName) == null) {
            nameToverionMap.put(moduleName, moduleVersion);
        }
    }

    public String getDefaultModuleVersion(String moduleName) {
        return (String) nameToverionMap.get(moduleName);
    }

    public ModuleDescription getDefaultModule(String moduleName) {
        String defualtModuleVersion = getDefaultModuleVersion(moduleName);
        if (defualtModuleVersion == null) {
            return (ModuleDescription) allModules.get(new QName(moduleName));
        } else {
            return (ModuleDescription) allModules.get(new QName(moduleName + "-" + defualtModuleVersion));
        }
    }

    public Object getKey() {
        return getAxis2HomeDirectory(); // TODO CheckMe
    }
}
