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

import org.apache.axis.context.MessageContext;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * <p> This is the ordered Collection of Phases as specified by the Server.xml file.
 * this has the execution logic as well. If something goes wrong inside the 
 * Execution Chain then the exeution chain itself revoke them.
 * </p>
 */
public class ExecutionChain {
    private HashMap phases;
    private ArrayList executionList;

    public ExecutionChain() {
        phases = new HashMap();
        executionList = new ArrayList();
    }
    
    public void addPhase(Phase phase) {
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
                Handler phase = (Handler) executionList.get(i);
                if (phase != null) {
                    executionStack.push(phase);
                    phase.invoke(msgctx);
                }
            }
        } catch (Exception e) {
            /////////////
            e.printStackTrace();
            ////////////////
            while (!executionStack.isEmpty()) {
                Handler handler = (Handler) executionStack.pop();
                handler.revoke(msgctx);
            }
            throw AxisFault.makeFault(e);
        }
    }
}
