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

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.impl.description.AxisService;
import org.apache.axis.impl.handlers.AbstractHandler;
import org.apache.axis.impl.handlers.OpNameFinder;
/**
 * this find the service to invoke 
 */
import javax.xml.namespace.QName;

public class Dispatcher extends AbstractHandler implements Handler {
    public void invoke(MessageContext msgctx) throws AxisFault{
        if(msgctx.isServerSide()){
            String uri = null;
            EndpointReference toEPR = msgctx.getTo();
            String filePart = toEPR.getAddress();
            String soapAction = (String) msgctx.getProperty(MessageContext.SOAP_ACTION);

            String pattern = "services/";
            int serviceIndex = 0;
            if((serviceIndex = filePart.indexOf(pattern)) > 0){
                uri = filePart.substring(serviceIndex + pattern.length());

            }

            QName serviceName = null;
            if (uri != null) {
                int index = uri.indexOf('?');
                if (index > -1) {
                    //TODO get the opeartion name from URI as well 
                    serviceName = new QName(uri);
                } else {
                    serviceName = new QName(uri);
                }
            } else {
                if (soapAction != null) {
                    serviceName = new QName(soapAction);
                }
            }
            if (serviceName != null) {
                AxisService service = msgctx.getGlobalContext().getRegistry().getService(serviceName);
                if (service != null) {
                    msgctx.setService(service);
                    //let add the Handlers 
                    ExecutionChain chain = msgctx.getExecutionChain();
                    chain.addPhases(service.getPhases(EngineRegistry.INFLOW));
                    //add invoke Phase
                    Phase invokePhase = new Phase(Phase.SERVICE_INVOCATION);                    
                    invokePhase.addHandler(new OpNameFinder());
                    invokePhase.addHandler(ReceiverLocator.locateReceiver(msgctx));
                    chain.addPhase(invokePhase);
                } else {
                    throw new AxisFault("Service " + serviceName + " is not found");
                }
            }else{
                throw new AxisFault("Both the URI and SOAP_ACTION Is Null");
            }
        }else{
            //TODO client side service Dispatch ,, What this really mean?
        }
    }
}
