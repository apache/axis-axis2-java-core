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
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Flow;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.phaseresolver.util.PhaseValidator;

import javax.xml.namespace.QName;
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
     * Field axisService
     */
    private AxisService axisService;


    /**
     * Field phaseHolder
     */
    private PhaseHolder phaseHolder;

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
     * Constructor PhaseResolver
     *
     * @param axisConfig
     * @param serviceContext
     */
    public PhaseResolver(AxisConfiguration axisConfig,
                         AxisService serviceContext) {
        this.axisConfig = axisConfig;
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
            AxisOperation axisOperation = (AxisOperation) iterator.next();
            for (int i = 1; i < 5; i++) {
                buildExcutionChains(i, axisOperation);
            }
        }
    }

    /**
     * To build the opration for the opeartion which the module going to be added
     *
     * @param opartion <code>AxisOperation</code>
     * @throws AxisFault
     */
    public void buildModuleOperation(AxisOperation opartion) throws AxisFault {
        for (int i = 1; i < 5; i++) {
            buildExcutionChains(i, opartion);
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
    private void buildExcutionChains(int type, AxisOperation axisOperation)
            throws AxisFault, PhaseException {
        ArrayList allHandlers = new ArrayList();
        ModuleDescription module;
        Flow flow = null;
        ///////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////// Handlers from   axis2.xml from modules/////////////////////////
        ArrayList modulqnames = (ArrayList) ((AxisConfigurationImpl) axisConfig).getEngadgedModules();
        for (int i = 0; i < modulqnames.size(); i++) {
            QName modulename = (QName) modulqnames.get(i);
            module = axisConfig.getModule(modulename);
            if (module != null) {
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
                axisService.addToEngagModuleList(module);
                axisOperation.addToEngageModuleList(module);
            } else {
                throw new PhaseException(Messages.getMessage(
                        DeploymentErrorMsgs.INVALID_MODULE_REF, modulename.getLocalPart()));
            }

            if (flow != null) {
                for (int j = 0; j < flow.getHandlerCount(); j++) {
                    HandlerDescription metadata = flow.getHandler(j);

                    if (!PhaseValidator.isSystemPhases(metadata.getRules().getPhaseName())) {
                        allHandlers.add(metadata);
                    } else {
                        /**
                         *This handler is trying to added to system pre defined phases , but those handlers
                         * are already added to global chain which run irrespective of the service
                         *
                         */
                    }
                }
            }

        }

        ///////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////// SERVICE HANDLERS ///////////////////////////////////////////////
        switch (type) {
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
                HandlerDescription metadata = flow.getHandler(j);

                // todo change this in properway
                if (metadata.getRules().getPhaseName().equals("")) {
                    throw new PhaseException(
                            Messages.getMessage(DeploymentErrorMsgs.PHASE_DOES_NOT_SPECIFED,
                                    metadata.getName().getLocalPart()));
                } else if(PhaseValidator.isSystemPhases(metadata.getRules().getPhaseName())){
                    throw new PhaseException(Messages.getMessage(
                            DeploymentErrorMsgs.SERVICE_MODULE_CAN_NOT_REFER_GLOBAL_PHASE,
                            metadata.getRules().getPhaseName()));
                } else {
                    allHandlers.add(metadata);
                }
            }
        }
        switch (type) {
            case PhaseMetadata.IN_FLOW:
            {
                phaseHolder =
                        new PhaseHolder(axisOperation.getRemainingPhasesInFlow());
                break;
            }
            case PhaseMetadata.OUT_FLOW:
            {
                phaseHolder =
                        new PhaseHolder(axisOperation.getPhasesOutFlow());
                break;
            }
            case PhaseMetadata.FAULT_IN_FLOW:
            {
                phaseHolder =
                        new PhaseHolder(axisOperation.getPhasesInFaultFlow());
                break;
            }
            case PhaseMetadata.FAULT_OUT_FLOW:
            {
                phaseHolder =
                        new PhaseHolder(axisOperation.getPhasesOutFaultFlow());
                break;
            }
        }
        for (int i = 0; i < allHandlers.size(); i++) {
            HandlerDescription handlerMetaData =
                    (HandlerDescription) allHandlers.get(i);
            phaseHolder.addHandler(handlerMetaData);
        }
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
        for (int type = 1; type < 4; type++) {
            switch (type) {
                case PhaseMetadata.IN_FLOW:
                {
                    flow = transport.getInFlow();
                    phase = transport.getInPhase();
                    break;
                }
                case PhaseMetadata.FAULT_IN_FLOW:
                {
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
        for (int type = 1; type < 5; type++) {
            switch (type) {
                case PhaseMetadata.OUT_FLOW:
                {
                    flow = transport.getOutFlow();
                    phase = transport.getOutPhase();
                    break;
                }
                case PhaseMetadata.FAULT_OUT_FLOW:
                {
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
            AxisServiceGroup sericeGroup = (AxisServiceGroup) servicegroups.next();
            Iterator services = sericeGroup.getServices();
            while (services.hasNext()) {
                AxisService axisService = (AxisService) services.next();
                axisService.addModuleOperations(module, axisConfig);
                engageModuleToServiceFromGlobal(axisService, module);
                axisService.addToEngagModuleList(module);
            }
            sericeGroup.addModule(module.getName());
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
            for (int type = 1; type < 5; type++) {
                switch (type) {
                    case PhaseMetadata.IN_FLOW:
                    {
                        phaseHolder =
                                new PhaseHolder(opDesc.getRemainingPhasesInFlow());
                        break;
                    }
                    case PhaseMetadata.OUT_FLOW:
                    {
                        phaseHolder =
                                new PhaseHolder(opDesc.getPhasesOutFlow());
                        break;
                    }
                    case PhaseMetadata.FAULT_IN_FLOW:
                    {
                        phaseHolder =
                                new PhaseHolder(opDesc.getPhasesInFaultFlow());
                        break;
                    }
                    case PhaseMetadata.FAULT_OUT_FLOW:
                    {
                        phaseHolder =
                                new PhaseHolder(opDesc.getPhasesOutFaultFlow());
                        break;
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////////
                /////////////////// Modules refered by axis2.xml //////////////////////////////////
                ////////////////////////////////////////////////////////////////////////////////////
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
        for (int type = 1; type < 5; type++) {
            switch (type) {
                case PhaseMetadata.IN_FLOW:
                {
                    phaseHolder =
                            new PhaseHolder(axisConfig.
                                    getInPhasesUptoAndIncludingPostDispatch());
                    break;
                }
                case PhaseMetadata.OUT_FLOW:
                {
                    phaseHolder =
                            new PhaseHolder(((AxisConfigurationImpl) axisConfig).getOutFlow());
                    break;
                }
                case PhaseMetadata.FAULT_IN_FLOW:
                {
                    phaseHolder =
                            new PhaseHolder(axisConfig.getInFaultFlow());
                    break;
                }
                case PhaseMetadata.FAULT_OUT_FLOW:
                {
                    phaseHolder =
                            new PhaseHolder(((AxisConfigurationImpl) axisConfig).getOutFaultFlow());
                    break;
                }
            }
            ////////////////////////////////////////////////////////////////////////////////////
            /////////////////// Modules refered by axis2.xml //////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////
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
        for (int type = 1; type < 5; type++) {
            switch (type) {
                case PhaseMetadata.IN_FLOW:
                {
                    phaseHolder =
                            new PhaseHolder(axisOperation.getRemainingPhasesInFlow());
                    break;
                }
                case PhaseMetadata.OUT_FLOW:
                {
                    phaseHolder =
                            new PhaseHolder(axisOperation.getPhasesOutFlow());
                    break;
                }
                case PhaseMetadata.FAULT_IN_FLOW:
                {
                    phaseHolder =
                            new PhaseHolder(axisOperation.getPhasesInFaultFlow());
                    break;
                }
                case PhaseMetadata.FAULT_OUT_FLOW:
                {
                    phaseHolder =
                            new PhaseHolder(axisOperation.getPhasesOutFaultFlow());
                    break;
                }
            }

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
                    HandlerDescription metadata = flow.getHandler(j);
                    if (!PhaseValidator.isSystemPhases(metadata.getRules().getPhaseName())) {
                        phaseHolder.addHandler(metadata);
                    } else {
                        throw new PhaseException(Messages.getMessage(DeploymentErrorMsgs.SERVICE_MODULE_CAN_NOT_REFER_GLOBAL_PHASE,
                                metadata.getRules().getPhaseName()));
                    }
                }
            }
        }
    }
}
