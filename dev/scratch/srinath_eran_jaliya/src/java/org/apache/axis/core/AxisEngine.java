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

package org.apache.axis.core;

import javax.xml.namespace.QName;

import org.apache.axis.core.context.MessageContext;
import org.apache.axis.core.exec.ExecutionChain;
import org.apache.axis.core.registry.EngineRegistry;
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
        QName currentServiceName = null;
        Service service = null;
        //dispatch the service Name
        currentServiceName = mc.getCurrentService();
        service = registry.getService(currentServiceName);
        try{
            ExecutionChain exeChain = service.getOutputExecutionChain();
            exeChain.invoke(mc);
        }catch(AxisFault e){
            if(mc.isProcessingFault()){
                //TODO log and exit
                log.debug("Error in fault flow",e);
            }else{
                log.debug("send failed",e);
                mc.setProcessingFault(true);
                ExecutionChain faultExeChain = service.getFaultExecutionChain();
                faultExeChain.invoke(mc);
            }
        }
        log.info("end the send()");
    }
    
    public void recive(MessageContext mc)throws AxisFault{
        QName currentServiceName = null;
        Service service = null;

        currentServiceName = mc.getCurrentService();
        service = registry.getService(currentServiceName);

        try{
            ExecutionChain exeChain = service.getInputExecutionChain();
            exeChain.invoke(mc);
        }catch(AxisFault e){
            if(mc.isProcessingFault()){
                //TODO log and exit
                log.debug("Error in fault flow",e);
            }else{
                log.debug("recive failed",e);
                mc.setProcessingFault(true);
                ExecutionChain faultExeChain = service.getFaultExecutionChain();
                faultExeChain.invoke(mc);
            }
        }
        log.info("end the recive()");
    }    
	/**
	 * @return Returns the registry.
	 */
	public EngineRegistry getRegistry() {
		return registry;
	}
	/**
	 * @param registry The registry to set.
	 */
	public void setRegistry(EngineRegistry registry) {
		this.registry = registry;
	}
}
