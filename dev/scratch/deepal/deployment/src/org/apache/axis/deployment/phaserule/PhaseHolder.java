package org.apache.axis.deployment.phaserule;

import org.apache.axis.deployment.util.Handler;

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
public class PhaseHolder {

    private Vector phaseholder = new Vector();

    public PhaseHolder() {
        phaseholder.removeAllElements();
    }

    private boolean isPhaseExist(String phaseName){
        for (int i = 0; i < phaseholder.size(); i++) {
            Phase phase = (Phase) phaseholder.elementAt(i);
            if(phase.getName().equals(phaseName)){
                return true;
            }

        }
        return false;
    }

    public void addHandler(Handler handler) throws PhaseException{
        String phaseName = handler.getPhase();
        if(isPhaseExist(phaseName)){
            getPhase(phaseName).addHandler(handler);
        }   else{
            Phase newpPhase = new Phase(phaseName);
            addPhase(newpPhase);
            newpPhase.addHandler(handler);

        }
    }

    private void addPhase(Phase phase){
        phaseholder.add(phase);
    }

    private Phase getPhase(String phaseName){
        for (int i = 0; i < phaseholder.size(); i++) {
            Phase phase = (Phase) phaseholder.elementAt(i);
            if(phase.getName().equals(phaseName)){
                return phase;
            }

        }
        return null;
    }

    private Phase [] getOrderdPhases(){
        //todo complet this using phaseordeer

        Phase [] phase = new Phase[phaseholder.size()];
        for (int i = 0; i < phaseholder.size(); i++) {
            Phase tempphase = (Phase) phaseholder.elementAt(i);
            phase[i] = tempphase;
        }
        return phase;
    }

    public void listOrderdhandlers(){
        int size = phaseholder.size();
        Phase [] tempPhase = getOrderdPhases();
        for (int i = 0; i < size ; i++) {
            tempPhase[i].listOrderdHandlers();

        }

    }

    public Handler[] getOrderdHandlers() throws PhaseException{
        Vector tempHander = new Vector();
        Handler [] handlers;
        for (int i = 0; i < phaseholder.size(); i++) {
            Phase phase = (Phase) phaseholder.elementAt(i);
            handlers = phase.getOrderedHandlers();

            for (int j = 0; j < handlers.length; j++) {
                tempHander.add(handlers[j]);
            }

        }
        Handler [] handler = new Handler[tempHander.size()];

        for (int i = 0; i < tempHander.size(); i++) {
            handler[i] = (Handler) tempHander.elementAt(i);

        }
        return handler;
    }

}
