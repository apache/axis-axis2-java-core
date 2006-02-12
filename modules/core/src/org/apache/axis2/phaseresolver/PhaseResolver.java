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

import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Flow;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Phase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class PhaseResolver
 */
public class PhaseResolver {
    private static final int IN_FLOW = 1;
    private static final int OUT_FAULT_FLOW = 5;

    /**
     * Field axisConfig
     */
    private AxisConfiguration axisConfig;

    /**
     * Field phaseHolder
     */
    private PhaseHolder phaseHolder;

    /**
     * default constructor , to obuild chains for GlobalDescription
     *
     * @param engineConfig
     */
    public PhaseResolver(AxisConfiguration engineConfig) {
        this.axisConfig = engineConfig;
    }

    private void buildINTransportChains(TransportInDescription transport) throws PhaseException {
        Flow flow = null;
        Phase phase = null;

        for (int type = IN_FLOW; type < OUT_FAULT_FLOW; type++) {
            switch (type) {
                case PhaseMetadata.IN_FLOW : {
                    flow = transport.getInFlow();
                    phase = transport.getInPhase();

                    break;
                }

                case PhaseMetadata.FAULT_IN_FLOW : {
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
    private void buildOutTransportChains(TransportOutDescription transport) throws PhaseException {
        Flow flow = null;
        Phase phase = null;

        for (int type = IN_FLOW; type < OUT_FAULT_FLOW; type++) {
            switch (type) {
                case PhaseMetadata.OUT_FLOW : {
                    flow = transport.getOutFlow();
                    phase = transport.getOutPhase();

                    break;
                }

                case PhaseMetadata.FAULT_OUT_FLOW : {
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
     * Method buildTranspotsChains
     *
     * @throws PhaseException
     */
    public void buildTranspotsChains() throws PhaseException {
        HashMap axisTransportIn = axisConfig.getTransportsIn();
        HashMap axisTransportOut = axisConfig.getTransportsOut();
        Collection colintrnsport = axisTransportIn.values();

        for (Iterator iterator = colintrnsport.iterator(); iterator.hasNext();) {
            TransportInDescription transport = (TransportInDescription) iterator.next();

            buildINTransportChains(transport);
        }

        Collection colouttrnsport = axisTransportOut.values();

        for (Iterator iterator = colouttrnsport.iterator(); iterator.hasNext();) {
            TransportOutDescription transport = (TransportOutDescription) iterator.next();

            buildOutTransportChains(transport);
        }
    }

    public void engageModuleToOperation(AxisOperation axisOperation, AxisModule module)
            throws PhaseException {
        Flow flow = null;

        for (int type = IN_FLOW; type < OUT_FAULT_FLOW; type++) {
            switch (type) {
                case PhaseMetadata.IN_FLOW : {
                    ArrayList phases = new ArrayList();

                    if (axisConfig != null) {
                        Iterator itr_axis_config =
                                axisConfig.getInPhasesUptoAndIncludingPostDispatch().iterator();

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

                    phaseHolder = new PhaseHolder(phases);

                    break;
                }

                case PhaseMetadata.OUT_FLOW : {
                    ArrayList phases = new ArrayList();
                    Iterator itr_ops = axisOperation.getPhasesOutFlow().iterator();

                    while (itr_ops.hasNext()) {
                        Object o = itr_ops.next();

                        phases.add(o);
                    }

                    if (axisConfig != null) {
                        Iterator itr_axis_config = axisConfig.getGlobalOutPhases().iterator();

                        while (itr_axis_config.hasNext()) {
                            Object o = itr_axis_config.next();

                            phases.add(o);
                        }
                    }

                    phaseHolder = new PhaseHolder(phases);

                    break;
                }

                case PhaseMetadata.FAULT_IN_FLOW : {
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

                    phaseHolder = new PhaseHolder(phases);

                    break;
                }

                case PhaseMetadata.FAULT_OUT_FLOW : {
                    phaseHolder = new PhaseHolder(axisOperation.getPhasesOutFaultFlow());

                    break;
                }
            }

            switch (type) {
                case PhaseMetadata.IN_FLOW : {
                    flow = module.getInFlow();

                    break;
                }

                case PhaseMetadata.OUT_FLOW : {
                    flow = module.getOutFlow();

                    break;
                }

                case PhaseMetadata.FAULT_IN_FLOW : {
                    flow = module.getFaultInFlow();

                    break;
                }

                case PhaseMetadata.FAULT_OUT_FLOW : {
                    flow = module.getFaultOutFlow();

                    break;
                }
            }

            if (flow != null) {
                for (int j = 0; j < flow.getHandlerCount(); j++) {
                    HandlerDescription metadata = flow.getHandler(j);

                    phaseHolder.addHandler(metadata);
                }
            }
        }
    }
}
