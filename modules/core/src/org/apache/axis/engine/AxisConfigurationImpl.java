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
package org.apache.axis.engine;

import org.apache.axis.description.*;
import org.apache.axis.phaseresolver.PhaseMetadata;
import org.apache.axis.phaseresolver.PhaseResolver;
import org.apache.axis.deployment.repository.utill.ArchiveReader;
import org.apache.axis.deployment.DeploymentEngine;

import javax.xml.namespace.QName;
import java.util.*;
import java.io.File;

/**
 * Class EngineRegistryImpl
 */
public class AxisConfigurationImpl implements AxisConfiguration {
    /**
     * To store Erroness services
     */
    private Hashtable errornesServices;

    /**
     * Field modules
     */
    private final HashMap modules = new HashMap();

    /**
     * Field services
     */
    private final HashMap services = new HashMap();

    /**
     * Field transports
     */
    private final HashMap transports = new HashMap();

    private final HashMap transportsIn = new HashMap();

    private final HashMap transportsOut = new HashMap();

    /**
     * Field phases
     */
    private ArrayList inPhases;
    private ArrayList outPhases;
    private ArrayList inFaultPhases;
    private ArrayList outFaultPhases;

    private ArrayList inPhasesUptoAndIncludingPostDispatch;
    private ArrayList faultPhases;



    /////////////////////// From AxisGlobal /////////////////////////////////////
    /**
     * Field paramInclude
     */
    protected final ParameterInclude paramInclude;

    /**
     * Field modules
     */
    protected final List engagedModules;

    protected HashMap messagRecievers;
    /////////////////////// From AxisGlobal /////////////////////////////////////
    /**
     * Constructor EngineRegistryImpl
     */
    public AxisConfigurationImpl() {
        paramInclude = new ParameterIncludeImpl();
        engagedModules = new ArrayList();
        messagRecievers = new HashMap();

        inPhases = new ArrayList();
        outPhases = new ArrayList();
        inFaultPhases = new ArrayList();
        outFaultPhases = new ArrayList();
        errornesServices = new Hashtable();

        inPhasesUptoAndIncludingPostDispatch = new ArrayList();
        inPhasesUptoAndIncludingPostDispatch.add(new Phase(PhaseMetadata.PHASE_TRANSPORTIN));
        inPhasesUptoAndIncludingPostDispatch.add(new Phase(PhaseMetadata.PHASE_PRE_DISPATCH));
        Phase dispatch = new Phase(PhaseMetadata.PHASE_DISPATCH);
        dispatch.addHandler(new RequestURIBasedDispatcher(), 0);
        dispatch.addHandler(new AddressingBasedDispatcher(), 1);
        inPhasesUptoAndIncludingPostDispatch.add(dispatch);
        inPhasesUptoAndIncludingPostDispatch.add(new Phase(PhaseMetadata.PHASE_POST_DISPATCH));
    }

    /**
     * Method getServices
     *
     * @return
     */
    public HashMap getServices() {
        return services;
    }

    public Hashtable getFaulytServices() {
        return errornesServices;
    }

    /**
     * Method addMdoule
     *
     * @param module
     * @throws AxisFault
     */
    public synchronized void addMdoule(ModuleDescription module) throws AxisFault {
        modules.put(module.getName(), module);
    }

    /**
     * Method addService
     *
     * @param service
     * @throws AxisFault
     */
    public synchronized void addService(ServiceDescription service) throws AxisFault {
        services.put(service.getName(), service);
        PhaseResolver handlerResolver = new PhaseResolver(this,service);
        handlerResolver.buildchains();
    }

    /**
     * Method getModule
     *
     * @param name
     * @return
     * @throws AxisFault
     */
    public ModuleDescription getModule(QName name) throws AxisFault {
        return (ModuleDescription) modules.get(name);
    }

    /**
     * @return
     */
    public HashMap getModules() {
        return modules;
    }


    /**
     * Method getService
     *
     * @param name
     * @return
     * @throws AxisFault
     */
    public ServiceDescription getService(QName name) throws AxisFault {
        return (ServiceDescription) services.get(name);
    }

    /**
     * Method removeService
     *
     * @param name
     * @throws AxisFault
     */
    public synchronized void removeService(QName name) throws AxisFault {
        services.remove(name);
    }


    public TransportInDescription getTransportIn(QName name) throws AxisFault {
        return (TransportInDescription) transportsIn.get(name);
    }

    /**
     * Method addTransport
     *
     * @param transport
     * @throws AxisFault
     */
    public synchronized void addTransportIn(TransportInDescription transport)
            throws AxisFault {
        transportsIn.put(transport.getName(), transport);
    }

    public TransportOutDescription getTransportOut(QName name) throws AxisFault {
        return (TransportOutDescription) transportsOut.get(name);
    }

    /**
     * Method addTransport
     *
     * @param transport
     * @throws AxisFault
     */
    public synchronized void addTransportOut(TransportOutDescription transport)
            throws AxisFault {
        transportsOut.put(transport.getName(), transport);
    }

    public HashMap getTransportsIn() {
        return transports;
    }

    public HashMap getTransportsOut() {
        return transportsOut;
    }

    public ArrayList getFaultPhases() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Method getTransports
     *
     * @return
     * @throws AxisFault
     */
    public HashMap getTransports() throws AxisFault {
        return transports;
    }

    public void setInPhases(ArrayList inPhases) {
        this.inPhases = inPhases;
    }

    public void setOutPhases(ArrayList outPhases) {
        this.outPhases = outPhases;
    }


    public ArrayList getInPhasesUptoAndIncludingPostDispatch() {
        return inPhasesUptoAndIncludingPostDispatch;
    }

    public ArrayList getPhasesInOutFaultFlow() {
        return faultPhases;
    }

    public ArrayList getOutFlow() {
        return outPhases;
    }


    /**
     * @return
     */
    public ArrayList getInFaultFlow() {
        return inFaultPhases;
    }

    /**
     * @return
     */
    public ArrayList getOutFaultFlow() {
        return outFaultPhases;
    }

    /**
     * @param list
     */
    public void setInFaultPhases(ArrayList list) {
        inFaultPhases = list;
    }

    /**
     * @param list
     */
    public void setOutFaultPhases(ArrayList list) {
        outFaultPhases = list;
    }

    ////////////////////////// Form Axis Global

    public void addMessageReceiver(String key, MessageReceiver messageReceiver) {
        messagRecievers.put(key, messageReceiver);
    }

    public MessageReceiver getMessageReceiver(String key) {
        return (MessageReceiver) messagRecievers.get(key);
    }

    /**
     * Method getParameter
     *
     * @param name
     * @return
     */
    public Parameter getParameter(String name) {
        return paramInclude.getParameter(name);
    }

    /**
     * Method addParameter
     *
     * @param param
     */
    public void addParameter(Parameter param) {
        paramInclude.addParameter(param);
    }
    /**
     * Method getEngadgedModules
     *
     * @return
     */
    public Collection getEngadgedModules() {
        return engagedModules;
    }

    public void engageModule(QName moduleref) throws AxisFault {
        ModuleDescription module = getModule(moduleref);
        if(module == null ) {
            File file =  new  ArchiveReader().creatModuleArchivefromResource(moduleref.getLocalPart());
            module =  new DeploymentEngine().buildModule(file);
        }
        if (module != null) {
            new PhaseResolver(this).engageModuleGlobally(module);
        } else {
             throw new AxisFault(this + " Refer to invalid module "
                     + moduleref.getLocalPart() + " has not bean deployed yet !");
        }
        engagedModules.add(moduleref);
    }

}
