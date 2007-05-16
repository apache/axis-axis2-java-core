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
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Phase;

import java.util.ArrayList;
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
     * @param axisconfig
     */
    public PhaseResolver(AxisConfiguration axisconfig) {
        this.axisConfig = axisconfig;
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
                                axisConfig.getInFlowPhases().iterator();

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
                        Iterator itr_axis_config = axisConfig.getOutFlowPhases().iterator();

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
                        Iterator itr_axis_config = axisConfig.getInFaultFlowPhases().iterator();

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
                    ArrayList phases = new ArrayList();
                    Iterator itr_ops = axisOperation.getPhasesOutFaultFlow().iterator();
                    while (itr_ops.hasNext()) {
                        Object o = itr_ops.next();

                        phases.add(o);
                    }
                    if (axisConfig != null) {
                        Iterator itr_axis_config = axisConfig.getOutFaultFlowPhases().iterator();
                        while (itr_axis_config.hasNext()) {
                            Object o = itr_axis_config.next();
                            phases.add(o);
                        }
                    }
                    phaseHolder = new PhaseHolder(phases);
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

    /**
     * To remove handlers from global chians this method can be used , first it take inflow
     * of the module and then take handler one by one and then remove those handlers from
     * global inchain ,
     * the same procedure will be carry out for all the other flows as well.
     *
     * @param module
     */
    public void disengageModuleFromGlobalChains(AxisModule module) {
        //INFLOW
        Flow flow = module.getInFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, axisConfig.getInFlowPhases());
            }
        }
        //OUTFLOW
        flow = module.getOutFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, axisConfig.getOutFlowPhases());
            }
        }
        //INFAULTFLOW
        flow = module.getFaultInFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, axisConfig.getInFaultFlowPhases());
            }
        }
        //OUTFAULTFLOW
        flow = module.getFaultOutFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, axisConfig.getOutFaultFlowPhases());
            }
        }
    }

    /**
     * To remove handlers from operations chians this method can be used , first it take inflow
     * of the module and then take handler one by one and then remove those handlers from
     * global inchain ,
     * the same procedure will be carry out for all the other flows as well.
     *
     * @param module
     */
    public void disengageModuleFromOperationChain(AxisModule module, AxisOperation operation) {
        //INFLOW
        Flow flow = module.getInFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, operation.getRemainingPhasesInFlow());
            }
        }
        //OUTFLOW
        flow = module.getOutFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, operation.getPhasesOutFlow());
            }
        }
        //INFAULTFLOW
        flow = module.getFaultInFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, operation.getPhasesInFaultFlow());
            }
        }
        //OUTFAULTFLOW
        flow = module.getFaultOutFlow();
        if (flow != null) {
            for (int j = 0; j < flow.getHandlerCount(); j++) {
                HandlerDescription handler = flow.getHandler(j);
                removeHandlerfromaPhase(handler, operation.getPhasesOutFaultFlow());
            }
        }
    }

    /**
     * To remove a single handler from a given list of phases
     *
     * @param handler
     * @param phaseList
     */
    private void removeHandlerfromaPhase(HandlerDescription handler, ArrayList phaseList) {
        String phaseName = handler.getRules().getPhaseName();
        Iterator phaseItr = phaseList.iterator();
        while (phaseItr.hasNext()) {
            Phase phase = (Phase) phaseItr.next();
            if (phase.getPhaseName().equals(phaseName)) {
                phase.removeHandler(handler);
                break;
            }
        }
    }
}
