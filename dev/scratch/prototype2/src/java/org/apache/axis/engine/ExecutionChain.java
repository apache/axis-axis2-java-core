/*
 * Copyright 2003,2004 The Apache Software Foundation.
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

import javax.xml.namespace.QName;

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
    private HashMap phases;
    private ArrayList executionList;
	private Log log = LogFactory.getLog(getClass());
	
    public ExecutionChain() {
        phases = new HashMap();
        executionList = new ArrayList();
    }
    
    public void addPhase(Phase phase) {
    	log.info("Phase "+ phase.getPhaseName() + "Added ");
        phases.put(phase.getPhaseName(), phase);
        executionList.add(phase);
    }
    
    
    public void addPhases(ArrayList phases){
        if (phases != null && !phases.isEmpty()) {
            for (int i = 0; i < phases.size(); i++) {
                addPhase((Phase) phases.get(i));
            }
        }
    }
    
    public Phase getPhase(QName name){
        return (Phase)phases.get(name);
    }

    public void invoke(MessageContext msgctx) throws AxisFault {
        Stack executionStack = new Stack();
        try {
            for (int i = 0; i < executionList.size(); i++) {
                Phase phase = (Phase) executionList.get(i);
                if (phase != null) {
					log.info("Invoke the Phase "+ phase.getPhaseName());
                    executionStack.push(phase);
                    phase.invoke(msgctx);
                }
            }
        } catch (Exception e) {
        	log.info("Execution Chain failed with the "+e.getMessage());
            while (!executionStack.isEmpty()) {
				Phase phase = (Phase) executionStack.pop();
				phase.revoke(msgctx);
				log.info("revoke the Phase "+ phase.getPhaseName());
            }
            throw AxisFault.makeFault(e);
        }
    }
}
