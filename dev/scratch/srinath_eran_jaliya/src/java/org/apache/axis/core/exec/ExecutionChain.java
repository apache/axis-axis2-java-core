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
package org.apache.axis.core.exec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.apache.axis.core.AxisFault;
import org.apache.axis.core.Handler;
import org.apache.axis.core.context.MessageContext;

/**
 * <p>This is the ordered Collection of Phases as specified by the Server.xml file.</p> 
 * @author Srinath Perera (hemapani@opensource.lk)
 */
public class ExecutionChain {
    private HashMap phases;
    private ArrayList executionList;
    
    public ExecutionChain(){
        phases = new HashMap();
        executionList = new ArrayList();
    }
    
    public void addPhase(Phase phase){
        phases.put(phase.getPhaseName(),phase);
        executionList.add(phase);
    }
    
    public void addHandlerDirectly(Handler directHandler,int index){
        phases.put(directHandler.getName(),directHandler);
        executionList.add(index,directHandler);
    }

    public void addHandler(String phaseName,Handler handler,int index){
        Phase phase = (Phase)phases.get(phaseName);
        phase.addHandler(handler,index);
    }

    
    public void addHandler(String phaseName,Handler handler) throws AxisFault{
        Phase phase = (Phase)phases.get(phaseName);
        if(phase == null)
            throw new AxisFault("Can't find the Phase "+phaseName);
        phase.addHandler(handler);
    }
    
    public void invoke(MessageContext msgctx)throws AxisFault{
        Stack executionStack = new Stack();
        try{
            for(int i = 0;i<executionList.size();i++){
                Handler phase = (Handler)executionList.get(i);
                if(phase != null){
                    executionStack.push(phase);
                    phase.invoke(msgctx);
                }
            }
        }catch(Exception e){
            while(executionStack.isEmpty()){
                Handler handler  = (Handler)executionStack.pop();
                handler.revoke(msgctx);
            }
            throw AxisFault.makeFault(e);
        }    
    }
    public void revoke(MessageContext msgctx)throws AxisFault{
        for(int i = executionList.size()-1;i > -1;i--){
            Phase phase = (Phase)executionList.get(i);
            if(phase != null){
                phase.revoke(msgctx);
            }
        }
    }
}
