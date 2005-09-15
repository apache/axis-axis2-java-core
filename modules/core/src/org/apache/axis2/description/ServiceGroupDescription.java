package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.phaseresolver.PhaseResolver;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
*
*/

public class ServiceGroupDescription implements ParameterInclude{

    //to add and get paramters
    protected ParameterInclude paramInclude;

    // to keep name of the service group
    private String serviceGroupName;

    // to keep the parent of service group , to chcek paramter lock checking and serching
    private  AxisConfiguration parent;

    /**
     * Field services
     */
    private HashMap services;

    //to store modeule configuration info
    private HashMap moduleConfigmap;

    // to store service Group modules name
    private ArrayList modules;

    //to store module ref at deploy time parsing
    private ArrayList mdoulesList = new ArrayList();


    public ServiceGroupDescription() {
        paramInclude = new ParameterIncludeImpl();
        services = new HashMap();
        moduleConfigmap = new HashMap();
        modules = new ArrayList();
    }

    public ServiceGroupDescription(AxisConfiguration axisDescription) {
        this();
        this.parent = axisDescription;
    }

    public void addParameter(Parameter param) throws AxisFault {
        paramInclude.addParameter(param);
    }

    public Parameter getParameter(String name) {
        return paramInclude.getParameter(name);
    }

    public ArrayList getParameters() {
        return paramInclude.getParameters();
    }

    public boolean isParamterLocked(String paramterName) {
        // checking the locked value of parent
        boolean loscked = false;

        if (getParent() !=null) {
            loscked =  getParent().isParamterLocked(paramterName);
        }
        if(loscked){
            return true;
        } else {
            Parameter parameter = getParameter(paramterName);
            if(parameter != null && parameter.isLocked()){
                return true;
            } else {
                return false;
            }
        }
    }

    public String getServiceGroupName() {
        return serviceGroupName;
    }

    public void setServiceGroupName(String serviceGroupName) {
        this.serviceGroupName = serviceGroupName;
    }

    public AxisConfiguration getParent() {
        return parent;
    }

    public void setParent(AxisConfiguration parent) {
        this.parent = parent;
    }

    /**
     * Adding module configuration , if there is moduleConfig tag in service
     * @param moduleConfiguration
     */
    public void addModuleConfig(ModuleConfiguration moduleConfiguration){
        if(moduleConfigmap == null){
            moduleConfigmap = new HashMap();
        }
        moduleConfigmap.put(moduleConfiguration.getModuleName(),moduleConfiguration);
    }

    public ModuleConfiguration getModuleConfig(QName moduleName){
        return  (ModuleConfiguration)moduleConfigmap.get(moduleName);
    }

    public void addModule(QName moduleName){
        modules.add(moduleName);
    }

    public void engageModuleToGroup(QName moduleName) throws AxisFault {
        if (moduleName == null) {
            return;
        }
        for (Iterator iterator = modules.iterator();
             iterator.hasNext();) {
            ModuleDescription modu = (ModuleDescription) iterator.next();
            if (modu.getName().equals(moduleName)) {
                throw new AxisFault(moduleName.getLocalPart() +
                        " module has alredy been engaged on the service Group. " +
                        " Operation terminated !!!");
            }
        }
        Iterator srevice = getServices();
        PhaseResolver phaseResolver = new PhaseResolver(this.getParent());
        ModuleDescription module = this.parent.getModule(moduleName);
        if (module !=null) {
            while (srevice.hasNext()) {
                // engagin per each service
                ServiceDescription serviceDescription = (ServiceDescription) srevice.next();
                phaseResolver.engageModuleToService(serviceDescription, module);
            }
        }
        addModule(moduleName);
    }

    public ArrayList getServiceGroupModules(){
        return modules;
    }


    public Iterator getServices(){
        return services.values().iterator();
    }

    public synchronized void addService(ServiceDescription service) throws AxisFault {
        services.put(service.getName(), service);
        PhaseResolver handlerResolver = new PhaseResolver(this.parent, service);
        handlerResolver.buildchains();
        service.setLastupdate();
        service.setParent(this);
    }

    public AxisConfiguration getAxisDescription() {
        return parent;
    }

    public void setAxisDescription(AxisConfiguration axisDescription) {
        this.parent = axisDescription;
    }

    public ServiceDescription getService(QName name) throws AxisFault {
        return (ServiceDescription) services.get(name);
    }

    public void addModuleref(QName moduleref){
        mdoulesList.add(moduleref);
    }

    public ArrayList getModules(){
        return mdoulesList;
    }


    public synchronized void removeService(QName name) throws AxisFault {
        ServiceDescription service = getService(name);
        if (service != null) {
            this.parent.notifyObservers(AxisEvent.SERVICE_DEPLOY , service);
        }
        services.remove(name);
    }

    public ServiceGroupContext getServiceGroupContext(ConfigurationContext parent){
        ServiceGroupContext serviceGroupContext = new ServiceGroupContext(parent,this) ;
        return serviceGroupContext;
    }
}
