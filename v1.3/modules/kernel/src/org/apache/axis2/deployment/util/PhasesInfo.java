/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.deployment.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.phaseresolver.PhaseMetadata;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

public class PhasesInfo {
    private ArrayList INPhases;
    private ArrayList IN_FaultPhases;
    private ArrayList OUTPhases;
    private ArrayList OUT_FaultPhases;

    public PhasesInfo() {
        INPhases = new ArrayList();
        IN_FaultPhases = new ArrayList();
        OUTPhases = new ArrayList();
        OUT_FaultPhases = new ArrayList();
    }

    /**
     * To copy phase information from one to another
     *
     * @param phase
     */
    private Phase copyPhase(Phase phase) throws DeploymentException {
        Phase newPhase = new Phase(phase.getPhaseName());
        Iterator handlers = phase.getHandlers().iterator();

        while (handlers.hasNext()) {
            try {
                Handler handlerDescription = (Handler) handlers.next();

                newPhase.addHandler(handlerDescription.getHandlerDesc());
            } catch (PhaseException e) {
                throw new DeploymentException(e);
            }
        }

        return newPhase;
    }

    HandlerDescription makeHandler(OMElement handlerElement) {
        String name = handlerElement.getAttributeValue(new QName("name"));
        QName qname = handlerElement.resolveQName(name);
        HandlerDescription desc = new HandlerDescription(qname.getLocalPart());
        String className = handlerElement.getAttributeValue(new QName("class"));

        desc.setClassName(className);

        return desc;
    }

    public Phase makePhase(OMElement phaseElement) throws PhaseException {
        String phaseName = phaseElement.getAttributeValue(new QName("name"));
        Phase phase = new Phase(phaseName);
        Iterator children = phaseElement.getChildElements();

        while (children.hasNext()) {
            OMElement handlerElement = (OMElement) children.next();
            HandlerDescription handlerDesc = makeHandler(handlerElement);

            phase.addHandler(handlerDesc);
        }

        return phase;
    }

    public ArrayList getGlobalInflow() throws DeploymentException {
        ArrayList globalphase = new ArrayList();
        boolean foundDispatchPhase = false;
        for (int i = 0; i < INPhases.size(); i++) {
            Phase phase = (Phase) INPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (!foundDispatchPhase) {
                if (PhaseMetadata.PHASE_DISPATCH.equals(phaseName)) {
                    foundDispatchPhase = true;
                }
                globalphase.add(phase);
            }
        }
        if (!foundDispatchPhase) {
            throw new DeploymentException(
                    Messages.getMessage(DeploymentErrorMsgs.DISPATCH_PHASE_NOT_FOUND));
        }
        return globalphase;
    }

    public ArrayList getGlobalOutPhaseList() throws DeploymentException {
        /**
         * I have assumed that     PolicyDetermination and  MessageProcessing are global out phase
         */
        ArrayList globalPhaseList = new ArrayList();

        boolean messageOut = false;
        for (int i = 0; i < OUTPhases.size(); i++) {
            Phase phase = (Phase) OUTPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (!messageOut) {
                if (PhaseMetadata.PHASE_MESSAGE_OUT.equals(phaseName)) {
                    messageOut = true;
                    globalPhaseList.add(copyPhase(phase));
                }
            } else {
                globalPhaseList.add(copyPhase(phase));
            }
        }
        return globalPhaseList;
    }

    public ArrayList getINPhases() {
        return INPhases;
    }

    public ArrayList getIN_FaultPhases() {
        return IN_FaultPhases;
    }

    public ArrayList getOUTPhases() {
        return OUTPhases;
    }

    public ArrayList getOUT_FaultPhases() throws DeploymentException {
        ArrayList globalPhaseList = new ArrayList();
        boolean messageOut = false;
        for (int i = 0; i < OUT_FaultPhases.size(); i++) {
            Phase phase = (Phase) OUT_FaultPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (!messageOut) {
                if (PhaseMetadata.PHASE_MESSAGE_OUT.equals(phaseName)) {
                    messageOut = true;
                    globalPhaseList.add(copyPhase(phase));
                }
            } else {
                globalPhaseList.add(copyPhase(phase));
            }
        }
        return globalPhaseList;
    }

    public ArrayList getOperationInFaultPhases() throws DeploymentException {
        ArrayList operationINPhases = new ArrayList();
        boolean foundDispathPhase = false;
        for (int i = 0; i < IN_FaultPhases.size(); i++) {
            Phase phase = (Phase) IN_FaultPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (foundDispathPhase) {
                operationINPhases.add(copyPhase(phase));
            }
            if (PhaseMetadata.PHASE_DISPATCH.equals(phaseName)) {
                foundDispathPhase = true;
            }
        }
        return operationINPhases;
    }

    public ArrayList getGlobalInFaultPhases() throws DeploymentException {
        ArrayList globalInfaultphase = new ArrayList();
        boolean foundDispatchPhase = false;
        for (int i = 0; i < IN_FaultPhases.size(); i++) {
            Phase phase = (Phase) IN_FaultPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (!foundDispatchPhase) {
                if (PhaseMetadata.PHASE_DISPATCH.equals(phaseName)) {
                    foundDispatchPhase = true;
                }
                globalInfaultphase.add(phase);
            }
        }
        if (!foundDispatchPhase) {
            throw new DeploymentException(
                    Messages.getMessage(DeploymentErrorMsgs.DISPATCH_PHASE_NOT_FOUND));
        }
        return globalInfaultphase;
    }


    public ArrayList getOperationInPhases() throws DeploymentException {
        ArrayList operationINPhases = new ArrayList();
        boolean foundDispathPhase = false;
        for (int i = 0; i < INPhases.size(); i++) {
            Phase phase = (Phase) INPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (foundDispathPhase) {
                operationINPhases.add(copyPhase(phase));
            }
            if (PhaseMetadata.PHASE_DISPATCH.equals(phaseName)) {
                foundDispathPhase = true;
            }
        }
        return operationINPhases;
    }

    public ArrayList getOperationOutFaultPhases() throws DeploymentException {
        ArrayList operationFaultOutPhases = new ArrayList();
        for (int i = 0; i < OUT_FaultPhases.size(); i++) {
            Phase phase = (Phase) OUT_FaultPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (PhaseMetadata.PHASE_MESSAGE_OUT.equals(phaseName)) {
                break;
            }
            operationFaultOutPhases.add(copyPhase(phase));

        }
        return operationFaultOutPhases;
    }

    public ArrayList getOperationOutPhases() throws DeploymentException {
        ArrayList oprationOUTPhases = new ArrayList();

        for (int i = 0; i < OUTPhases.size(); i++) {
            Phase phase = (Phase) OUTPhases.get(i);
            String phaseName = phase.getPhaseName();
            if (PhaseMetadata.PHASE_MESSAGE_OUT.equals(phaseName)) {
                break;
            }
            oprationOUTPhases.add(copyPhase(phase));
        }

        return oprationOUTPhases;
    }

    public void setINPhases(ArrayList INPhases) {
        this.INPhases = INPhases;
    }

    public void setIN_FaultPhases(ArrayList IN_FaultPhases) {
        this.IN_FaultPhases = IN_FaultPhases;
    }

    public void setOUTPhases(ArrayList OUTPhases) {
        this.OUTPhases = OUTPhases;
    }

    public void setOUT_FaultPhases(ArrayList OUT_FaultPhases) {
        this.OUT_FaultPhases = OUT_FaultPhases;
    }

    public void setOperationPhases(AxisOperation axisOperation) throws AxisFault {
        if (axisOperation != null) {
            try {
                axisOperation.setRemainingPhasesInFlow(getOperationInPhases());
                axisOperation.setPhasesOutFlow(getOperationOutPhases());
                axisOperation.setPhasesInFaultFlow(getOperationInFaultPhases());
                axisOperation.setPhasesOutFaultFlow(getOperationOutFaultPhases());
            } catch (DeploymentException e) {
                throw AxisFault.makeFault(e);
            }
        }
    }
}
