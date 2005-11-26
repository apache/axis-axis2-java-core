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

package org.apache.axis2.phaseresolver;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.phaseresolver.util.PhaseValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class PhaseResolver
 */
public class PhaseResolver {
    /**
     * Field axisConfig
     */
    private AxisConfiguration axisConfig;
    /**
     * Field phaseHolder
     */
    private PhaseHolder phaseHolder;
    private static final int IN_FLOW = 1;
    private static final int OUT_FAULT_FLOW = 5;

    public PhaseResolver() {
    }

    /**
     * default constructor , to obuild chains for GlobalDescription
     *
     * @param engineConfig
     */
    public PhaseResolver(AxisConfiguration engineConfig) {
        this.axisConfig = engineConfig;
    }

    /**
     * Method buildTranspotsChains
     *
     * @throws PhaseException
     */
    public void buildTranspotsChains() throws PhaseException {
        HashMap axisTransportIn = axisConfig.getTransportsIn();
        HashMap axisTransportOut = axisConfig.getTransportsOut();

        Collection colintrnsport = axisTransportIn.values();
        for (Iterator iterator = colintrnsport.iterator();
             iterator.hasNext();) {
            TransportInDescription transport = (TransportInDescription) iterator.next();
            buildINTransportChains(transport);
        }

        Collection colouttrnsport = axisTransportOut.values();
        for (Iterator iterator = colouttrnsport.iterator();
             iterator.hasNext();) {
            TransportOutDescription transport = (TransportOutDescription) iterator.next();
            buildOutTransportChains(transport);
        }
    }


    private void buildINTransportChains(TransportInDescription transport)
            throws PhaseException {
        Flow flow = null;
        Phase phase = null;
        for (int type = IN_FLOW; type < 4; type++) {
            switch (type) {
                case PhaseMetadata.IN_FLOW: {
                    flow = transport.getInFlow();
                    phase = transport.getInPhase();
                    break;
                }
                case PhaseMetadata.FAULT_IN_FLOW: {
                    flow = transport.getFaultFlow();
                    phase = transport.getFaultPhase();
                    break;
                }
            }
            if (flow != null) {
                ArrayList handlers = new ArrayList();
                for (int j = 0; j < flow.getHandlerCount(); j++) {
                    HandlerDescription metadata = flow.getHandler(j);
                    metadata.getRules().setPhaseName(PhaseMetadata.TRANSPORT_PHASE);
                    handlers.add(metadata);
                }
                new PhaseHolder().buildTransportHandlerChain(phase, handlers);
            } else {
            }
        }
    }


    /**
     * Method buildTransportChains
     *
     * @param transport
     * @throws PhaseException
     */
    private void buildOutTransportChains(TransportOutDescription transport)
            throws PhaseException {
        Flow flow = null;
        Phase phase = null;
        for (int type = IN_FLOW; type < OUT_FAULT_FLOW; type++) {
            switch (type) {
                case PhaseMetadata.OUT_FLOW: {
                    flow = transport.getOutFlow();
                    phase = transport.getOutPhase();
                    break;
                }
                case PhaseMetadata.FAULT_OUT_FLOW: {
                    flow = transport.getFaultFlow();
                    phase = transport.getFaultPhase();
                    break;
                }
            }
            if (flow != null) {
                ArrayList handlers = new ArrayList();
                for (int j = 0; j < flow.getHandlerCount(); j++) {
                    HandlerDescription metadata = flow.getHandler(j);
                    metadata.getRules().setPhaseName(PhaseMetadata.TRANSPORT_PHASE);
                    handlers.add(metadata);
                }
                new PhaseHolder().buildTransportHandlerChain(phase, handlers);
            } else {
            }
        }
    }


    public void engageModuleGlobally(ModuleDescription module) throws AxisFault {
        enageToGlobalChain(module);
        Iterator servicegroups = axisConfig.getServiceGroups();
        while (servicegroups.hasNext()) {
            AxisServiceGroup serviceGroup = (AxisServiceGroup) servicegroups.next();
            Iterator services = serviceGroup.getServices();
            while (services.hasNext()) {
                AxisService axisService = (AxisService) services.next();
                axisService.addModuleOperations(module, axisConfig);
                engageModuleToServiceFromGlobal(axisService, module);
                axisService.addToEngagModuleList(module);
            }
            serviceGroup.addModule(module.getName());
        }
    }

    /**
     * To engage modules come form global
     *
     * @param service
     * @param module
     * @throws PhaseException
     */
    public void engageModuleToServiceFromGlobal(AxisService service,
                                                ModuleDescription module) throws PhaseException {
        HashMap opeartions = service.getOperations();
        Collection opCol = opeartions.values();
        boolean engaged = false;
        for (Iterator iterator = opCol.iterator(); iterator.hasNext();) {
            AxisOperation opDesc = (AxisOperation) iterator.next();
            Collection modules = opDesc.getModules();
            for (Iterator iterator1 = modules.iterator();
                 iterator1.hasNext();) {
                ModuleDescription description = (ModuleDescription) iterator1.next();
                if (description.getName().equals(module.getName())) {
                    engaged = true;
                    break;
                }
            }
            if (engaged) {
                continue;
            }
            Flow flow = null;
            for (int type = IN_FLOW; type < OUT_FAULT_FLOW; type++) {
                switch (type) {
                    case PhaseMetadata.IN_FLOW: {
                        phaseHolder =
                                new PhaseHolder(opDesc.getRemainingPhasesInFlow());
                        break;
                    }
                    case PhaseMetadata.OUT_FLOW: {
                        phaseHolder =
                                new PhaseHolder(opDesc.getPhasesOutFlow());
                        break;
                    }
                    case PhaseMetadata.FAULT_IN_FLOW: {
                        phaseHolder =
                                new PhaseHolder(opDesc.getPhasesInFaultFlow());
                        break;
                    }
                    case PhaseMetadata.FAULT_OUT_FLOW: {
                        phaseHolder =
                                new PhaseHolder(opDesc.getPhasesOutFaultFlow());
                        break;
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////////
                /////////////////// Modules refered by axis2.xml //////////////////////////////////
                ////////////////////////////////////////////////////////////////////////////////////
                switch (type) {
                    case PhaseMetadata.IN_FLOW: {
                        flow = module.getInFlow();
                        break;
                    }
                    case PhaseMetadata.OUT_FLOW: {
                        flow = module.getOutFlow();
                        break;
                    }
                    case PhaseMetadata.FAULT_IN_FLOW: {
                        flow = module.getFaultInFlow();
                        break;
                    }
                    case PhaseMetadata.FAULT_OUT_FLOW: {
                        flow = module.getFaultOutFlow();
                        break;
                    }
                }
                if (flow != null) {
                    for (int j = 0; j < flow.getHandlerCount(); j++) {
                        HandlerDescription metadata = flow.getHandler(j);
                        if (!PhaseValidator.isSystemPhases(metadata.getRules().getPhaseName())) {
                            phaseHolder.addHandler(metadata);
                        }
                    }
                }
            }
            opDesc.addToEngageModuleList(module);
        }
    }


    private void enageToGlobalChain(ModuleDescription module) throws PhaseException {
        Flow flow = null;
        for (int type = IN_FLOW; type < OUT_FAULT_FLOW; type++) {
            switch (type) {
                case PhaseMetadata.IN_FLOW: {
                    phaseHolder =
                            new PhaseHolder(axisConfig.
                                    getInPhasesUptoAndIncludingPostDispatch());
                    break;
                }
                case PhaseMetadata.OUT_FLOW: {
                    phaseHolder =
                            new PhaseHolder(((AxisConfigurationImpl) axisConfig).getOutFlow());
                    break;
                }
                case PhaseMetadata.FAULT_IN_FLOW: {
                    phaseHolder =
                            new PhaseHolder(axisConfig.getInFaultFlow());
                    break;
                }
                case PhaseMetadata.FAULT_OUT_FLOW: {
                    phaseHolder =
                            new PhaseHolder(((AxisConfigurationImpl) axisConfig).getOutFaultFlow());
                    break;
                }
            }
            ////////////////////////////////////////////////////////////////////////////////////
            /////////////////// Modules refered by axis2.xml //////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////
            switch (type) {
                case PhaseMetadata.IN_FLOW: {
                    flow = module.getInFlow();
                    break;
                }
                case PhaseMetadata.OUT_FLOW: {
                    flow = module.getOutFlow();
                    break;
                }
                case PhaseMetadata.FAULT_IN_FLOW: {
                    flow = module.getFaultInFlow();
                    break;
                }
                case PhaseMetadata.FAULT_OUT_FLOW: {
                    flow = module.getFaultOutFlow();
                    break;
                }
            }
            if (flow != null) {
                for (int j = 0; j < flow.getHandlerCount(); j++) {
                    HandlerDescription metadata = flow.getHandler(j);
                    if (PhaseValidator.isSystemPhases(metadata.getRules().getPhaseName())) {
                        phaseHolder.addHandler(metadata);
                    } else {
                        /**
                         * These handlers will go to operation's handler chains , since the module
                         * try to add handlres to both sytem predefined phases and user defined phase
                         * so global module can do that. here the global module are the module which are
                         * reffred by axis2.xml
                         */
                    }
                }
            }
        }
    }


    public void engageModuleToService(AxisService service,
                                      ModuleDescription module) throws AxisFault {
        HashMap opeartions = service.getOperations();
        Collection opCol = opeartions.values();
        boolean engaged = false;
        service.addModuleOperations(module, axisConfig);
        for (Iterator iterator = opCol.iterator(); iterator.hasNext();) {
            AxisOperation opDesc = (AxisOperation) iterator.next();
            Collection modules = opDesc.getModules();
            for (Iterator iterator1 = modules.iterator();
                 iterator1.hasNext();) {
                ModuleDescription description = (ModuleDescription) iterator1.next();
                if (description.getName().equals(module.getName())) {
                    engaged = true;
                    break;
                }
            }
            if (!engaged) {
                engageModuleToOperation(opDesc, module);
                opDesc.addToEngageModuleList(module);
            }
        }
    }


    public void engageModuleToOperation(AxisOperation axisOperation,
                                        ModuleDescription module) throws PhaseException {
        Flow flow = null;
        for (int type = IN_FLOW; type < OUT_FAULT_FLOW; type++) {
            switch (type) {
                case PhaseMetadata.IN_FLOW: {
                    ArrayList phases = new ArrayList();
                    if (axisConfig != null) {
                        Iterator itr_axis_config = axisConfig.getInPhasesUptoAndIncludingPostDispatch().iterator();
                        while (itr_axis_config.hasNext()) {
                            Object o = itr_axis_config.next();
                            phases.add(o);
                        }
                    }
                    Iterator itr_ops = axisOperation.getRemainingPhasesInFlow().iterator();
                    while (itr_ops.hasNext()) {
                        Object o = itr_ops.next();
                        phases.add(o);
                    }
                    phaseHolder =
                            new PhaseHolder(phases);
                    break;
                }
                case PhaseMetadata.OUT_FLOW: {
                    phaseHolder =
                            new PhaseHolder(axisOperation.getPhasesOutFlow());
                    break;
                }
                case PhaseMetadata.FAULT_IN_FLOW: {
                    ArrayList phases = new ArrayList();
                    if (axisConfig != null) {
                        Iterator itr_axis_config = axisConfig.getInFaultFlow().iterator();
                        while (itr_axis_config.hasNext()) {
                            Object o = itr_axis_config.next();
                            phases.add(o);
                        }
                    }
                    Iterator itr_ops = axisOperation.getPhasesInFaultFlow().iterator();
                    while (itr_ops.hasNext()) {
                        Object o = itr_ops.next();
                        phases.add(o);
                    }
                    phaseHolder =
                            new PhaseHolder(axisOperation.getPhasesInFaultFlow());
                    break;
                }
                case PhaseMetadata.FAULT_OUT_FLOW: {
                    phaseHolder =
                            new PhaseHolder(axisOperation.getPhasesOutFaultFlow());
                    break;
                }
            }

            switch (type) {
                case PhaseMetadata.IN_FLOW: {
                    flow = module.getInFlow();
                    break;
                }
                case PhaseMetadata.OUT_FLOW: {
                    flow = module.getOutFlow();
                    break;
                }
                case PhaseMetadata.FAULT_IN_FLOW: {
                    flow = module.getFaultInFlow();
                    break;
                }
                case PhaseMetadata.FAULT_OUT_FLOW: {
                    flow = module.getFaultOutFlow();
                    break;
                }
            }
            if (flow != null) {
                for (int j = 0; j < flow.getHandlerCount(); j++) {
                    HandlerDescription metadata = flow.getHandler(j);
                    phaseHolder.addHandler(metadata);
                    //commented the following code to provide ability to add service module to global
                    //chain
//                    if (!PhaseValidator.isSystemPhases(metadata.getRules().getPhaseName())) {
//                        phaseHolder.addHandler(metadata);
//                    } else {
//                        throw new PhaseException(Messages.getMessage(DeploymentErrorMsgs.SERVICE_MODULE_CAN_NOT_REFER_GLOBAL_PHASE,
//                                metadata.getRules().getPhaseName()));
//                    }
                }
            }
        }
    }
}
