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

import javax.xml.namespace.QName;

import org.apache.axis.context.MessageContext;
import org.apache.axis.impl.handlers.OpNameFinder;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.registry.EngineRegistry;
import org.apache.axis.registry.Service;
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
        Service service = null;
        //dispatch the service Name
        service = mc.getService();
        try{
            //what are we suppose to do in the client side 
            //how the client side handlers are deployed ??? this is a hack and no client side handlers
            if(mc.isServerSide() || service != null){
                ExecutionChain exeChain = service.getOutputExecutionChain();
                exeChain.invoke(mc);
            }
            sendTheMessage(mc);
        }catch(AxisFault e){
            handleFault(mc,e,service);
        }
        log.info("end the send()");
    }
    
    public void recive(MessageContext mc)throws AxisFault{
        Service service = null;
        try{
            if(mc.isServerSide()){
                service = ServiceLocator.locateService(mc);
                mc.setService(service);
            }

            if(service != null){
                ExecutionChain exeChain = service.getInputExecutionChain();
                exeChain.invoke(mc);
            }        
            if(mc.isServerSide()){
                OpNameFinder finder = new OpNameFinder();
                finder.invoke(mc);
                Receiver reciver = ReceiverLocator.locateReciver(mc);
                reciver.invoke(mc);
            }
        }catch(AxisFault e){
            handleFault(mc,e,service);
        }
        log.info("end the recive()");
    }    
    
    private void sendTheMessage(MessageContext msgCtx)throws AxisFault{
        TransportSender ts = TransportSenderLocator.locateTransPortSender(msgCtx);
        ts.invoke(msgCtx);
    }
    
    private void handleFault(MessageContext mc,Exception e,Service service) throws AxisFault{
        if(mc.isProcessingFault()){
            //TODO log and exit
            log.error("Error in fault flow",e);
        }else{
            log.debug("recive failed",e);
            mc.setProcessingFault(true);
            if(service != null){
                ExecutionChain faultExeChain = service.getFaultExecutionChain();
                faultExeChain.invoke(mc);
            }
            SOAPEnvelope envelope = OMFactory.newInstance().getDefaultEnvelope();
            //TODO do we need to set old Headers back?
            envelope.getBody().addFault(e);
            mc.setEnvelope(envelope);
            sendTheMessage(mc);
        }
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
