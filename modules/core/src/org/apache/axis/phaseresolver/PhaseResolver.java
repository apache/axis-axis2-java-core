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

import org.apache.axis.context.SystemContext;
import org.apache.axis.description.*;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.AxisSystem;
import org.apache.axis.engine.AxisSystemImpl;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Class PhaseResolver
 */
public class PhaseResolver {
    /**
     * Field engineConfig
     */
    private final AxisSystem engineConfig;

    /**
     * Field axisService
     */
    private AxisService axisService;


    /**
     * Field phaseHolder
     */
    private PhaseHolder phaseHolder;

    /**
     * default constructor , to obuild chains for AxisGlobal
     *
     * @param engineConfig
     */
    public PhaseResolver(AxisSystem engineConfig) {
        this.engineConfig = engineConfig;
    }

    /**
     * Constructor PhaseResolver
     *
     * @param engineConfig
     * @param serviceContext
     */
    public PhaseResolver(AxisSystem engineConfig,
                         AxisService serviceContext) {
        this.engineConfig = engineConfig;
        this.axisService = serviceContext;
    }

    /**
     * Method buildchains
     *
     * @throws PhaseException
     * @throws AxisFault
     */
    public void buildchains() throws PhaseException, AxisFault {
        HashMap operations = axisService.getOperations();
        Collection col = operations.values();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            AxisOperation operation = (AxisOperation) iterator.next();
            for (int i = 1; i < 5; i++) {
                buildExcutionChains(i, operation);
            }
        }
    }

    private void buildModuleHandlers(ArrayList allHandlers, AxisModule module, int flowtype) throws PhaseException {
        Flow flow = null;
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
                if (PhaseMetadata.PRE_DISPATCH.equals(metadata.getRules().getPhaseName())) {
                    continue;
                }
                if (metadata.getRules().getPhaseName().equals("")) {
                    throw new PhaseException("Phase dose not specified");
                }
                allHandlers.add(metadata);
            }
        }
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
    private void buildExcutionChains(int type, AxisOperation operation)
            throws AxisFault, PhaseException {
        int flowtype = type;
        ArrayList allHandlers = new ArrayList();
        AxisModule module;
        Flow flow = null;
        ArrayList modules = (ArrayList) axisService.getModules();
        ///////////////////////////////////////////////////////////////////////////////////////
        ///////////////////// GLOBAL HANDLERS ////////////////////////////////////////////////
        for (int i = 0; i < modules.size(); i++) {
            QName name = (QName) modules.get(i);
            module = engineConfig.getModule(name);
            if (module != null) {
                buildModuleHandlers(allHandlers, module, flowtype);
            } else {
                throw new PhaseException("Referred module is NULL " + name.getLocalPart());
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////// SERVICE HANDLERS ///////////////////////////////////////////////


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

        ///////////////////////////////////////////////////////////////////////////////////////
        ///////////////////// SERVICE MODULE HANDLERS ////////////////////////////////////////////////
        Collection collection = axisService.getModules();
        Iterator itr = collection.iterator();
        while (itr.hasNext()) {
            QName moduleref = (QName) itr.next();
            module = engineConfig.getModule(moduleref);
            if (module != null) {
                buildModuleHandlers(allHandlers, module, flowtype);
            }
        }
        ///////////////////////////////////////////////////////////////////////////////////////
        ///////////////////// OPERATION HANDLERS ////////////////////////////////////////////////
        Collection opmodule = operation.getModules();
        Iterator opitr = opmodule.iterator();
        while (opitr.hasNext()) {
            QName moduleref = (QName) opitr.next();
            module = engineConfig.getModule(moduleref);
            if (module != null) {
                buildModuleHandlers(allHandlers, module, flowtype);
            }
        }

        phaseHolder = new PhaseHolder(engineConfig, operation);
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
    public SystemContext buildGlobalChains()
            throws AxisFault, PhaseException {
        SystemContext engineContext = new SystemContext(engineConfig);
        AxisGlobal global = engineConfig.getGlobal();
        List modules = (List) global.getModules();
        int count = modules.size();
        QName moduleName;
        AxisModule module;
        Flow flow = null;
        for (int type = 1; type < 5; type++) {
            phaseHolder = new PhaseHolder(engineConfig);
            phaseHolder.setFlowType(type);
            Collection col = ((AxisSystemImpl) engineConfig).getModules().values();
            for (Iterator iterator = col.iterator(); iterator.hasNext();) {
                AxisModule axismodule = (AxisModule) iterator.next();
                switch (type) {
                    case PhaseMetadata.IN_FLOW:
                        {
                            flow = axismodule.getInFlow();
                            break;
                        }
                    case PhaseMetadata.OUT_FLOW:
                        {
                            flow = axismodule.getOutFlow();
                            break;
                        }
                    case PhaseMetadata.FAULT_IN_FLOW:
                        {
                            flow = axismodule.getFaultInFlow();
                            break;
                        }
                    case PhaseMetadata.FAULT_OUT_FLOW:
                        {
                            flow = axismodule.getFaultOutFlow();
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
                        if (PhaseMetadata.PRE_DISPATCH.equals(metadata.getRules().getPhaseName())) {
                            phaseHolder.addHandler(metadata);
                        } else {
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
                        if (!PhaseMetadata.PRE_DISPATCH.equals(metadata.getRules().getPhaseName())) {
                            phaseHolder.addHandler(metadata);
                        } else {
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
