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

import java.util.Stack;

import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.CommonExecutor;
import org.apache.axis.registry.EngineRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  There is one engine for the Server and the Client. the send() and recive() 
 *  Methods are the basic operations the Sync, Async messageing are build on top.
 *  Two methods will find and execute the <code>CommonExecuter</code>'s Transport,
 *  Global,Service.  
 */
public class AxisEngine {
    private Log log = LogFactory.getLog(getClass());
    private EngineRegistry registry;
    public AxisEngine(EngineRegistry registry){
        log.info("Axis Engine Started");        
        this.registry = registry;
    }
    
    public void send(MessageContext mc)throws AxisFault{
        Stack executionStack = new Stack();
        QName currentTansportName = null;
        QName currentServiceName = null;
        
        Transport transport = null;
        Global globel = null;
        Service service = null;
        Operation operation = null;
        
        log.info("Start the send()");
        //Dispatch the Global and Transport. 
        currentTansportName = mc.getCurrentTansport();
        log.info("Dispatch transport");
        transport = registry.getTransPort(currentTansportName);
        log.info("Axis Engine Dispatch Global");
        globel = registry.getGlobal();

        log.info("Dispatch Service Name");
        //dispatch the service Name
        currentServiceName = mc.getCurrentService();
        service = registry.getService(currentServiceName);
        try{
            service.send(mc);
            executionStack.push(service);
        
            globel.send(mc);
            executionStack.push(globel);
        
            transport.send(mc); 
            executionStack.push(transport);                
        }catch(AxisFault e){
            while(!executionStack.isEmpty()){
                CommonExecutor commonExecutor = (CommonExecutor)executionStack.pop();
                commonExecutor.rollback(mc);
            }
            if(mc.isProcessingFault()){
                //TODO log and exit
                log.debug("Error in fault flow",e);
            }else{
                log.debug("send failed",e);
                mc.setProcessingFault(true);
                service.processFaultFlow(mc);
                globel.processFaultFlow(mc);
                transport.processFaultFlow(mc);
            }
        }
        log.info("end the send()");
    }
    
    public  void recive(MessageContext mc)throws AxisFault{
        Stack executionStack = new Stack();
        QName currentTansportName = null;
        QName currentServiceName = null;
        
        Transport transport = null;
        Global globel = null;
        Service service = null;
        Operation operation = null;
        
        log.info("Start the recive()");
        //Dispatch the Global and Transport. 
        currentTansportName = mc.getCurrentTansport();
        log.info("Dispatch transport");
        transport = registry.getTransPort(currentTansportName);
        log.info("Axis Engine Dispatch Global");
        globel = registry.getGlobal();

        try{
            transport.recive(mc); 
            executionStack.push(transport);
        
            globel.recive(mc);
            executionStack.push(globel);
            
            log.info("Dispatch Service Name");
            //dispatch the service Name
            currentServiceName = mc.getCurrentService();
            service = registry.getService(currentServiceName);
                
            service.recive(mc);
            executionStack.push(service);
        }catch(AxisFault e){
            while(!executionStack.isEmpty()){
                CommonExecutor commonExecutor = (CommonExecutor)executionStack.pop();
                commonExecutor.rollback(mc);
            }
            if(mc.isProcessingFault()){
                //TODO log and exit
                log.debug("Error in fault flow",e);
            }else{
                log.debug("recive() failed",e);
                mc.setProcessingFault(true);
                service.processFaultFlow(mc);
                globel.processFaultFlow(mc);
                transport.processFaultFlow(mc);
            }
        }
        log.info("end the recive()");
    }    
}
