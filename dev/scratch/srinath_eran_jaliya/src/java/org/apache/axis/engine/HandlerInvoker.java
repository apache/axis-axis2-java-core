/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import org.apache.axis.AxisFault;
import org.apache.axis.CommonExecutor;
import org.apache.axis.Handler;
import org.apache.axis.registry.Flow;

/**
 * A utility class to 
 * <ol>
 *      <li>accepts Flows, Handlers and find all the Handlers.</li>
 *      <li>Order them accoreding to the Pahsed rules.</li> 
 *      <li>Execute the Handlers.</li> 
 * </ol>
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class HandlerInvoker {
    private ArrayList handlers;
    private CommonExecutor worker;
    private int currentHandlerIndex = 0;
    
    public HandlerInvoker(CommonExecutor worker){
        this.worker = worker;
        handlers = new ArrayList();
    }
    public void addFlow(Flow flow){
        if(flow != null){
            int length = flow.getHandlerCount();
            for(int i = 0;i<length;i++){
                handlers.add(flow.getHandler(i));
            }
        }    
    }
    
    public void addHandler(Handler handler){
        handlers.add(handler);
    }
    
    public void orderTheHandlers(){
        //TODO
    }
    
    public void invoke(MessageContext msgcontext)throws AxisFault{
        int length = handlers.size();
        for(;currentHandlerIndex<length;currentHandlerIndex++){
            Handler handler = (Handler)handlers.get(currentHandlerIndex);
            handler.invoke(msgcontext); 
        }
        currentHandlerIndex = 0;
    }
    public void revoke(MessageContext msgcontext)throws AxisFault{
        int length = handlers.size();
        if(currentHandlerIndex == 0){
            currentHandlerIndex = length;
        }
        for(;currentHandlerIndex > 0 ;currentHandlerIndex--){
            Handler handler = (Handler)handlers.get(currentHandlerIndex);
            handler.revoke(msgcontext); 
        }
        currentHandlerIndex = 0;
     }
}
