package org.apache.axis.phaseresolver;

import org.apache.axis.deployment.DeploymentConstants;
import org.apache.axis.deployment.metadata.ServerMetaData;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.HandlerMetaData;
import org.apache.axis.engine.ExecutionChain;
import org.apache.axis.engine.Phase;
import org.apache.axis.phaseresolver.PhaseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Vector;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Nov 8, 2004
 *         2:21:17 PM
 *
 */


/**
 * This class hold all the phases found in the service.xml and server.xml
 */
public class PhaseHolder implements DeploymentConstants {

    private Log log = LogFactory.getLog(getClass());
    private Vector phaseholder = new Vector();

    /**
     * Referance to ServerMetaData inorder to get information about phases.
     */
    private ServerMetaData serverMetaData;// = new  ServerMetaData();
    private AxisService service;


    public PhaseHolder(ServerMetaData serverMetaDatain,AxisService serviceIN) {
        this.serverMetaData = serverMetaDatain;
        this.service = serviceIN;
    }

    private boolean isPhaseExist(String phaseName) {
        for (int i = 0; i < phaseholder.size(); i++) {
            PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
            if (phase.getName().equals(phaseName)) {
                return true;
            }

        }
        return false;
    }

    public void addHandler(HandlerMetaData handler) throws PhaseException {
        String phaseName = handler.getRules().getPhaseName();

        if (isPhaseExist(phaseName)) {
            getPhase(phaseName).addHandler(handler);
        } else {
            if (serverMetaData.isPhaseExist(phaseName)) {
                PhaseMetaData newpPhase = new PhaseMetaData(phaseName);
                addPhase(newpPhase);
                newpPhase.addHandler(handler);
            } else {
                throw new PhaseException("Invalid Phase ," + phaseName + "for the handler " + handler.getName()    + " dose not exit in server.xml");
            }

        }
    }

    private void addPhase(PhaseMetaData phase) {
        phaseholder.add(phase);
    }

    private PhaseMetaData getPhase(String phaseName) {
        for (int i = 0; i < phaseholder.size(); i++) {
            PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
            if (phase.getName().equals(phaseName)) {
                return phase;
            }

        }
        return null;
    }

    private  void OrderdPhases() {
        //todo complet this using phaseordeer
        PhaseMetaData[] phase = new PhaseMetaData[phaseholder.size()];
        for (int i = 0; i < phaseholder.size(); i++) {
            PhaseMetaData tempphase = (PhaseMetaData) phaseholder.elementAt(i);
            phase[i] = tempphase;
        }
        phase = serverMetaData.getOrderPhases(phase);
        // remove all items inorder to rearrange them
        phaseholder.removeAllElements();

        for (int i = 0; i < phase.length; i++) {
            PhaseMetaData phaseMetaData = phase[i];
            phaseholder.add(phaseMetaData);

        }
    }

    /**
     * cahinType
     *  1 : inFlowExcChain
     *  2 : OutFlowExeChain
     *  3 : FaultFlowExcechain
     * @param chainType
     * @throws org.apache.axis.phaseresolver.PhaseException
     */
    public  void getOrderdHandlers(int chainType) throws PhaseException {
        OrderdPhases();
        Vector tempHander = new Vector();
        HandlerMetaData[] handlers;

        switch (chainType) {
            case 1 : {
                ExecutionChain inChain =  new ExecutionChain();//       service.getExecutableInChain();
                for (int i = 0; i < phaseholder.size(); i++) {
                    PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
                    Phase axisPhase = new Phase(phase.getName());
                    handlers = phase.getOrderedHandlers();
                    for (int j = 0; j < handlers.length; j++) {
                        axisPhase.addHandler(handlers[j].getHandler());
                    }
                    inChain.addPhase(axisPhase);
                }
               service.setExecutableInChain(inChain);
                break;
            }
            case 2 : {
                ExecutionChain outChain =new ExecutionChain();// service.getExecutableOutChain();
                for (int i = 0; i < phaseholder.size(); i++) {
                    PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
                    Phase axisPhase = new Phase(phase.getName());
                    handlers = phase.getOrderedHandlers();
                    for (int j = 0; j < handlers.length; j++) {
                        axisPhase.addHandler(handlers[j].getHandler());
                    }
                    outChain.addPhase(axisPhase);
                }
                service.setExecutableOutChain(outChain);
                break;
            }
            case 3 : {
                ExecutionChain faultChain = new ExecutionChain();//service.getExecutableFaultChain();
                for (int i = 0; i < phaseholder.size(); i++) {
                    PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
                    Phase axisPhase = new Phase(phase.getName());
                    handlers = phase.getOrderedHandlers();
                    for (int j = 0; j < handlers.length; j++) {
                        axisPhase.addHandler(handlers[j].getHandler());
                    }
                    faultChain.addPhase(axisPhase);
                }
                service.setExecutableFaultChain(faultChain);
                break;
            }
        }
    }

}
