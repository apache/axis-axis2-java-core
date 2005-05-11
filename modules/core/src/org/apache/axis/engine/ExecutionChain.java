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
package org.apache.axis.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.apache.axis.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p> This is the ordered Collection of Phases as specified by the Server.xml file.
 * this has the execution logic as well. If something goes wrong inside the
 * Execution Chain then the exeution chain itself revoke them.
 * </p>
 */
public class ExecutionChain {
    /**
     * Field phases
     */
    private final HashMap phases;

    /**
     * Field executionList
     */
    private final ArrayList executionList;

    /**
     * Field log
     */
    private Log log = LogFactory.getLog(getClass());
    
    private int indexOfPhaseToExecuted = 0;

    /**
     * Constructor ExecutionChain
     */
    public ExecutionChain() {
        phases = new HashMap();
        executionList = new ArrayList();
    }

    /**
     * Method addPhase
     *
     * @param phase
     */
    public void addPhase(Phase phase) {
        log.info("Phase " + phase.getPhaseName() + "Added ");
        phases.put(phase.getPhaseName(), phase);
        executionList.add(phase);
    }

    /**
     * Method addPhases
     *
     * @param phases
     */
    public void addPhases(ArrayList phases) {
        if ((phases != null) && !phases.isEmpty()) {
            for (int i = 0; i < phases.size(); i++) {
                addPhase((Phase) phases.get(i));
            }
        }
    }

    /**
     * Method getPhase
     *
     * @param name
     * @return
     */
    public Phase getPhase(String name) {
        return (Phase) phases.get(name);
    }

    /**
         * Method invoke
         *
         * @param msgctx
         * @throws AxisFault
         */
    public void invoke(MessageContext msgctx) throws AxisFault {
        Stack executionStack = new Stack();
        try {
            while (indexOfPhaseToExecuted < executionList.size()) {
                if(msgctx.isPaused()){
                    if(indexOfPhaseToExecuted > 0){
                        //need to run the last Phase agien so it can finish the handler
                        indexOfPhaseToExecuted--;
                    }
                    break;
                }else{
                    Phase phase = (Phase) executionList.get(indexOfPhaseToExecuted);
                    if (phase != null) {
                        log.info("Invoke the Phase " + phase.getPhaseName());
                        phase.invoke(msgctx);
                        //This line should be after the invoke as if the invocation failed this phases is takn care of and 
                        //no need to revoke agien
                        executionStack.push(phase);
                        indexOfPhaseToExecuted++;
                    }
                }
            }
        } catch (Exception e) {
            log.info("Execution Chain failed with the " + e.getMessage());
//            while (!executionStack.isEmpty()) {
//                SimplePhase phase = (SimplePhase) executionStack.pop();
//                phase.revoke(msgctx);
//                log.info("revoke the Phase " + phase.getPhaseName());
//            }
            throw AxisFault.makeFault(e);
        }
    }
}
