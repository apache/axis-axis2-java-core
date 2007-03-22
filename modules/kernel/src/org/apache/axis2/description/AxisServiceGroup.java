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


package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.modules.Module;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AxisServiceGroup extends AxisDescription {

    // to store module ref at deploy time parsing
    private ArrayList modulesList = new ArrayList();

    // to store service Group engagedModules name
    private ArrayList engagedModules;

    // to store modeule configuration info
    private HashMap moduleConfigmap;

    // class loader
    private ClassLoader serviceGroupClassLoader;

    // to keep name of the service group
    private String serviceGroupName;

    //to check whether user has put WWW dir or not
    private boolean foundWebResources;

    //To check whether server side service or client side service

    /**
     * Field services
     */
//    private HashMap services;
    public AxisServiceGroup() {
//        services = new HashMap();
        moduleConfigmap = new HashMap();
        engagedModules = new ArrayList();
    }

    public AxisServiceGroup(AxisConfiguration axisDescription) {
        this();
        setParent(axisDescription);
    }

    /**
     * Adds module configuration , if there is moduleConfig tag in service.
     *
     * @param moduleConfiguration
     */
    public void addModuleConfig(ModuleConfiguration moduleConfiguration) {
        if (moduleConfigmap == null) {
            moduleConfigmap = new HashMap();
        }

        moduleConfigmap.put(moduleConfiguration.getModuleName(), moduleConfiguration);
    }

    public void addModuleref(QName moduleref) {
        modulesList.add(moduleref);
    }

    public void addService(AxisService service) throws AxisFault {
        if (service == null) {
            return;
        }

        if (serviceGroupName == null) {
            // setup a temporary name based on the first service under this group
            serviceGroupName = service.getName();
        }

        service.setParent(this);

        AxisConfiguration axisConfig = (AxisConfiguration) getParent();

        if (axisConfig != null) {
            Iterator modules = this.engagedModules.iterator();

            while (modules.hasNext()) {
                String moduleName = (String) modules.next();
                AxisModule axisModule = axisConfig.getModule(moduleName);

                if (axisModule != null) {
                    Module moduleImpl = axisModule.getModule();
                    if (moduleImpl != null) {
                        // notyfying module for service engagement
                        moduleImpl.engageNotify(service);
                    }
                    service.engageModule(axisModule, axisConfig);
                } else {
                    throw new AxisFault(Messages.getMessage("modulenotavailble", moduleName));
                }
            }
        }

        service.setLastupdate();
        addChild(service);
    }

    public void addToGroup(AxisService service) throws Exception{
          if (service == null) {
            return;
        }
        service.setParent(this);

        AxisConfiguration axisConfig = (AxisConfiguration) getParent();

        if (axisConfig != null) {
            Iterator modules = this.engagedModules.iterator();

            while (modules.hasNext()) {
                String moduleName = (String) modules.next();
                AxisModule axisModule = axisConfig.getModule(moduleName);

                if (axisModule != null) {
                    Module moduleImpl = axisModule.getModule();
                    if (moduleImpl != null) {
                        // notyfying module for service engagement
                        moduleImpl.engageNotify(service);
                    }
                    service.engageModule(axisModule, axisConfig);
                } else {
                    throw new AxisFault(Messages.getMessage("modulenotavailble", moduleName));
                }
            }
        }
        service.setLastupdate();
        addChild(service);
        if(axisConfig!=null){
            axisConfig.addToAllServicesMap(service.getName(),service);
        }
    }

//    /**
//     * @deprecate Please use String version instead
//     * @param moduleName
//     */
//    public void addToengagedModules(String moduleName) {
//    }

    public void addToengagedModules(String moduleName) {
        engagedModules.add(moduleName);
    }

//    /**
//     * @deprecate Please use String version instead
//     * @param moduleName
//     */
//    public void removeFromEngageList(QName moduleName) {
//    }

    public void removeFromEngageList(String moduleName) {
        engagedModules.remove(moduleName);
    }

    public void engageModule(AxisModule module, AxisConfiguration axisConfig) throws AxisFault {
        String moduleName = module.getName();
        boolean isEngagable;
        for (Iterator iterator = engagedModules.iterator(); iterator.hasNext();) {
            String modu = (String) iterator.next();
            isEngagable = Utils.checkVersion(moduleName, modu);
            if (!isEngagable) {
                return;
            }
        }
        for (Iterator serviceIter = getServices(); serviceIter.hasNext();) {
            AxisService axisService = (AxisService) serviceIter.next();
            axisService.engageModule(module, axisConfig);
        }
        addToengagedModules(moduleName);
    }

    public void disengageModule(AxisModule module) throws AxisFault {
        for (Iterator serviceIter = getServices(); serviceIter.hasNext();) {
            AxisService axisService = (AxisService) serviceIter.next();
            axisService.disengageModule(module);
        }
        removeFromEngageList(module.getName());
    }

    public void removeService(String name) throws AxisFault {
        AxisService service = getService(name);

        if (service != null) {
            ((AxisConfiguration) getParent()).notifyObservers(AxisEvent.SERVICE_REMOVE, service);
        }

//        services.remove(name);
        removeChild(name);
    }

    public AxisConfiguration getAxisDescription() {
        return (AxisConfiguration) getParent();
    }

    public ArrayList getEngagedModules() {
        return engagedModules;
    }

    public ModuleConfiguration getModuleConfig(String moduleName) {
        return (ModuleConfiguration) moduleConfigmap.get(moduleName);
    }

    public ArrayList getModuleRefs() {
        return modulesList;
    }

    public AxisService getService(String name) throws AxisFault {
//        return (AxisService) services.get(name);
        return (AxisService) getChild(name);
    }

    public ClassLoader getServiceGroupClassLoader() {
        return serviceGroupClassLoader;
    }

    public String getServiceGroupName() {
        // Note: if the serviceGroupName is not set, then this could be null.
        // If the serviceGroupName has not been set and a service is added to this group, 
        // then the serviceGroupName will default to the name of the first service
        return serviceGroupName;
    }

    public Iterator getServices() {
//        return services.values().iterator();
        return getChildren();
    }

    public void setAxisDescription(AxisConfiguration axisDescription) {
        setParent(axisDescription);
    }

    public void setServiceGroupClassLoader(ClassLoader serviceGroupClassLoader) {
        this.serviceGroupClassLoader = serviceGroupClassLoader;
    }

    public void setServiceGroupName(String serviceGroupName) {
        this.serviceGroupName = serviceGroupName;
    }

    public Object getKey() {
        // Note: if the serviceGroupName is not set, then this could be null.
        // If the serviceGroupName has not been set and a service is added to this group, 
        // then the serviceGroupName will default to the name of the first service
        return this.serviceGroupName;
    }

    public boolean isEngaged(String moduleName) {
        AxisModule module = getAxisDescription().getModule(moduleName);
        if (module == null) {
            return false;
        }
        Iterator engagedModuleItr = engagedModules.iterator();
        while (engagedModuleItr.hasNext()) {
            QName axisModule = (QName) engagedModuleItr.next();
            if (axisModule.getLocalPart().equals(module.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isFoundWebResources() {
        return foundWebResources;
    }

    public void setFoundWebResources(boolean foundWebResources) {
        this.foundWebResources = foundWebResources;
    }
}
