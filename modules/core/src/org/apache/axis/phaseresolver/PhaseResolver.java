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
package org.apache.axis.phaseresolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis.context.EngineContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisModule;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisTransportIn;
import org.apache.axis.description.AxisTransportOut;
import org.apache.axis.description.Flow;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineConfiguration;
import org.apache.axis.engine.EngineConfigurationImpl;

/**
 * Class PhaseResolver
 */
public class PhaseResolver {
    /**
     * Field engineConfig
     */
    private final EngineConfiguration engineConfig;

    /**
     * Field axisService
     */
    private AxisService axisService;

    private ServiceContext serviceContext;

    /**
     * Field phaseHolder
     */
    private PhaseHolder phaseHolder;

    /**
     * default constructor , to obuild chains for AxisGlobal
     *
     * @param engineConfig
     */
    public PhaseResolver(EngineConfiguration engineConfig) {
        this.engineConfig = engineConfig;
    }

    /**
     * Constructor PhaseResolver
     *
     * @param engineConfig
     * @param serviceContext
     */
    public PhaseResolver(EngineConfiguration engineConfig,
                         ServiceContext serviceContext) {
        this.engineConfig = engineConfig;
        this.axisService = serviceContext.getServiceConfig();
        this.serviceContext = serviceContext;
    }

    /**
     * Method buildchains
     *
     * @throws PhaseException
     * @throws AxisFault
     */
    public ServiceContext buildchains() throws PhaseException, AxisFault {
        for (int i = 1; i < 5; i++) {
            buildExcutionChains(i);
        }
        return this.serviceContext;
    }

    /**
     * this opeartion is used to build all the three cahins ,
     * so type varible is used to difrenciate them
     * type = 1 inflow
     * type = 2 out flow
     * type = 3 fault flow
     *
     * @param type
     * @throws AxisFault
     * @throws PhaseException
     */
    private void buildExcutionChains(int type)
            throws AxisFault, PhaseException {
        int flowtype = type;
        ArrayList allHandlers = new ArrayList();
        AxisModule module;
        Flow flow = null;
        ArrayList modules = (ArrayList)axisService.getModules();//   (ArrayList)engineConfig.getGlobal().getModules());
        //Global modules
        for (int i = 0; i < modules.size(); i++) {
            QName name = (QName) modules.get(i);
            module = engineConfig.getModule(name);
            if(module != null){
                switch (flowtype) {
                    case PhaseMetadata.IN_FLOW:
                        {
                            flow = module.getInFlow();
                            break;
                        }
                    case PhaseMetadata.OUT_FLOW:
                        {
                            flow = module.getOutFlow();
                            break;
                        }
                    case PhaseMetadata.FAULT_IN_FLOW:
                        {
                            flow = module.getFaultInFlow();
                            break;
                        }
                    case PhaseMetadata.FAULT_OUT_FLOW:
                        {
                            flow = module.getFaultOutFlow();
                            break;
                        }
                }
                if (flow != null) {
                    for (int j = 0; j < flow.getHandlerCount(); j++) {
                        HandlerMetadata metadata = flow.getHandler(j);
                        /**
                         * If the phase property of a handler is pre-dispatch then those handlers
                         * should go to the global chain , to the pre-dispatch phase
                         */
                        if(PhaseMetadata.PRE_DISPATCH.equals(metadata.getRules().getPhaseName())){
                            continue;
                        }
                        if (metadata.getRules().getPhaseName().equals("")) {
                            metadata.getRules().setPhaseName("service");
                        }
                        allHandlers.add(metadata);
                    }
                }
            } else {
                throw new PhaseException("Referred module is NULL " +  name.getLocalPart());
            }
        }

        // service module handlers
        Collection collection = axisService.getModules();
        Iterator itr = collection.iterator();
        while (itr.hasNext()) {
            QName moduleref = (QName) itr.next();
            module = engineConfig.getModule(moduleref);
            switch (flowtype) {
                case PhaseMetadata.IN_FLOW:
                    {
                        flow = module.getInFlow();
                        break;
                    }
                case PhaseMetadata.OUT_FLOW:
                    {
                        flow = module.getOutFlow();
                        break;
                    }
                case PhaseMetadata.FAULT_IN_FLOW:
                    {
                        flow = module.getFaultInFlow();
                        break;
                    }
                case PhaseMetadata.FAULT_OUT_FLOW:
                    {
                        flow = module.getFaultOutFlow();
                        break;
                    }
            }
            if (flow != null) {
                for (int j = 0; j < flow.getHandlerCount(); j++) {
                    HandlerMetadata metadata = flow.getHandler(j);

                    /**
                     * If the phase property of a handler is pre-dispatch then those handlers
                     * should go to the global chain , to the pre-dispatch phase
                     */
                    if(PhaseMetadata.PRE_DISPATCH.equals(metadata.getRules().getPhaseName())){
                        continue;
                    }
                    if (metadata.getRules().getPhaseName().equals("")) {
                        metadata.getRules().setPhaseName("service");
                    }
                    allHandlers.add(metadata);
                }
            }
        }
        switch (flowtype) {
            case PhaseMetadata.IN_FLOW:
                {
                    flow = axisService.getInFlow();
                    break;
                }
            case PhaseMetadata.OUT_FLOW:
                {
                    flow = axisService.getOutFlow();
                    break;
                }
            case PhaseMetadata.FAULT_IN_FLOW:
                {
                    flow = axisService.getFaultInFlow();
                    break;
                }
            case PhaseMetadata.FAULT_OUT_FLOW:
                {
                    flow = axisService.getFaultOutFlow();
                    break;
                }
        }
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerMetadata metadata = flow.getHandler(j);

                // todo change this in properway
                if (metadata.getRules().getPhaseName().equals("")) {
                    metadata.getRules().setPhaseName("service");
                }
                allHandlers.add(metadata);
            }
        }
        phaseHolder = new PhaseHolder(engineConfig, axisService, serviceContext);
        phaseHolder.setFlowType(flowtype);
        for (int i = 0; i < allHandlers.size(); i++) {
            HandlerMetadata handlerMetaData =
                    (HandlerMetadata) allHandlers.get(i);
            phaseHolder.addHandler(handlerMetaData);
        }
        phaseHolder.getOrderedHandlers(type);
    }

    /**
     * Method buildTranspotsChains
     *
     * @throws PhaseException
     */
    public void buildTranspotsChains() throws PhaseException {
        HashMap axisTransportIn = engineConfig.getTransportsIn();
        HashMap axisTransportOut = engineConfig.getTransportsOut();

        Collection colintrnsport = axisTransportIn.values();
        for (Iterator iterator = colintrnsport.iterator();
             iterator.hasNext();) {
            AxisTransportIn transport = (AxisTransportIn) iterator.next();
            buildINTransportChains(transport);
        }

        Collection colouttrnsport = axisTransportOut.values();
        for (Iterator iterator = colouttrnsport.iterator();
             iterator.hasNext();) {
            AxisTransportOut transport = (AxisTransportOut) iterator.next();
            buildOutTransportChains(transport);
        }
    }


    private void buildINTransportChains(AxisTransportIn transport)
            throws PhaseException {
        Flow flow = null;
        for (int type = 1; type < 4; type++) {
            phaseHolder = new PhaseHolder(engineConfig);
            phaseHolder.setFlowType(type);
            switch (type) {
                case PhaseMetadata.IN_FLOW:
                    {
                        flow = transport.getInFlow();
                        break;
                    }
                case PhaseMetadata.FAULT_IN_FLOW:
                    {
                        flow = transport.getFaultFlow();
                        break;
                    }
            }
            if (flow != null) {
                for (int j = 0; j < flow.getHandlerCount(); j++) {
                    HandlerMetadata metadata = flow.getHandler(j);

                    // todo change this in properway
                    if (metadata.getRules().getPhaseName().equals("")) {
                        metadata.getRules().setPhaseName("transport");
                    }
                    phaseHolder.addHandler(metadata);
                }
            }
            phaseHolder.buildTransportChain(transport, type);
        }
    }


    /**
     * Method buildTransportChains
     *
     * @param transport
     * @throws PhaseException
     */
    private void buildOutTransportChains(AxisTransportOut transport)
            throws PhaseException {
        Flow flow = null;
        for (int type = 1; type < 4; type++) {
            phaseHolder = new PhaseHolder(engineConfig);
            phaseHolder.setFlowType(type);
            switch (type) {
                case PhaseMetadata.OUT_FLOW:
                    {
                        flow = transport.getOutFlow();
                        break;
                    }
                case PhaseMetadata.FAULT_OUT_FLOW:
                    {
                        flow = transport.getFaultFlow();
                        break;
                    }
            }
            if (flow != null) {
                for (int j = 0; j < flow.getHandlerCount(); j++) {
                    HandlerMetadata metadata = flow.getHandler(j);

                    // todo change this in properway
                    if (metadata.getRules().getPhaseName().equals("")) {
                        metadata.getRules().setPhaseName("transport");
                    }
                    phaseHolder.addHandler(metadata);
                }
            }
            phaseHolder.buildTransportChain(transport, type);
        }
    }

    /**
     * Method buildGlobalChains
     *
     * @throws AxisFault
     * @throws PhaseException
     */
    public EngineContext buildGlobalChains()
            throws AxisFault, PhaseException {
        EngineContext engineContext = new EngineContext(engineConfig);
        AxisGlobal global = engineConfig.getGlobal();
        List modules = (List) global.getModules();
        int count = modules.size();
        QName moduleName;
        AxisModule module;
        Flow flow = null;
        for (int type = 1; type < 4; type++) {
            phaseHolder = new PhaseHolder(engineConfig);
            phaseHolder.setFlowType(type);
            List globalModule = (List)((EngineConfigurationImpl)engineConfig).getModules().values();
            int gcount =  globalModule.size();
            for (int intA = 0; intA < gcount; intA++) {
                module = (AxisModule) globalModule.get(intA);
                switch (type) {
                    case PhaseMetadata.IN_FLOW:
                        {
                            flow = module.getInFlow();
                            break;
                        }
                    case PhaseMetadata.OUT_FLOW:
                        {
                            flow = module.getOutFlow();
                            break;
                        }
                    case PhaseMetadata.FAULT_IN_FLOW:
                        {
                            flow = module.getFaultInFlow();
                            break;
                        }
                    case PhaseMetadata.FAULT_OUT_FLOW:
                        {
                            flow = module.getFaultOutFlow();
                            break;
                        }
                }
                if (flow != null) {
                    for (int j = 0; j < flow.getHandlerCount(); j++) {
                        HandlerMetadata metadata = flow.getHandler(j);
                        /**
                         * If the phase property of a handler is pre-dispatch then those handlers
                         * should go to the global chain , to the pre-dispatch phase
                         */
                        if(PhaseMetadata.PRE_DISPATCH.equals(metadata.getRules().getPhaseName())){
                            phaseHolder.addHandler(metadata);
                        }  else {
                            continue;
                        }
                    }
                }
            }
            ///////////////////////////////////////////////////
            for (int intA = 0; intA < count; intA++) {
                moduleName = (QName) modules.get(intA);
                module = engineConfig.getModule(moduleName);
                switch (type) {
                    case PhaseMetadata.IN_FLOW:
                        {
                            flow = module.getInFlow();
                            break;
                        }
                    case PhaseMetadata.OUT_FLOW:
                        {
                            flow = module.getOutFlow();
                            break;
                        }
                    case PhaseMetadata.FAULT_IN_FLOW:
                        {
                            flow = module.getFaultInFlow();
                            break;
                        }
                    case PhaseMetadata.FAULT_OUT_FLOW:
                        {
                            flow = module.getFaultOutFlow();
                            break;
                        }
                }
                if (flow != null) {
                    for (int j = 0; j < flow.getHandlerCount(); j++) {
                        HandlerMetadata metadata = flow.getHandler(j);
                        if(! PhaseMetadata.PRE_DISPATCH.equals(metadata.getRules().getPhaseName())){
                            phaseHolder.addHandler(metadata);
                        }  else {
                            continue;
                        }
                    }
                }
            }
            phaseHolder.buildGlobalChain(engineContext, type);
        }
        return engineContext;
    }
}
