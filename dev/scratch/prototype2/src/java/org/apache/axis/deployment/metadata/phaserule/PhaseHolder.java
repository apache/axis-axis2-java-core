package org.apache.axis.deployment.metadata.phaserule;

import org.apache.axis.deployment.DeploymentConstants;
import org.apache.axis.deployment.DeploymentEngine;
import org.apache.axis.deployment.metadata.HandlerMetaData;
import org.apache.axis.deployment.metadata.ServerMetaData;

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

    private Vector phaseholder = new Vector();

    /**
     * Referance to ServerMetaData inorder to get information about phases.
     */
    ServerMetaData serverMetaData;// = new  ServerMetaData();

    public PhaseHolder() {
        this.serverMetaData = DeploymentEngine.getServerMetaData();
        phaseholder.removeAllElements();
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
        String phaseName = handler.getPhase();
        /**
         * if user dose not specify the phase that belong to service phase
         * todo i have to check that before and after property of handers if they
         * represent phases the default name should be change
         */
        if (phaseName.equals("")) {
            phaseName = SERVICETAG;
            handler.setPhase(SERVICETAG);
        }

        if (isPhaseExist(phaseName)) {
            getPhase(phaseName).addHandler(handler);
        } else {
            if (serverMetaData.isPhaseExist(phaseName)) {
                PhaseMetaData newpPhase = new PhaseMetaData(phaseName);
                addPhase(newpPhase);
                newpPhase.addHandler(handler);
            } else {
                throw new PhaseException("Invalid Phase ," + phaseName + " dose not exit in server.xml");
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

    private void OrderdPhases() {
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



    public HandlerMetaData[] getOrderdHandlers() throws PhaseException {
        OrderdPhases() ;
        Vector tempHander = new Vector();
        HandlerMetaData[] handlers;
        for (int i = 0; i < phaseholder.size(); i++) {
            PhaseMetaData phase = (PhaseMetaData) phaseholder.elementAt(i);
            handlers = phase.getOrderedHandlers();

            for (int j = 0; j < handlers.length; j++) {
                tempHander.add(handlers[j]);
            }

        }
        HandlerMetaData[] handler = new HandlerMetaData[tempHander.size()];

        for (int i = 0; i < tempHander.size(); i++) {
            handler[i] = (HandlerMetaData) tempHander.elementAt(i);

        }
        return handler;
    }

}
