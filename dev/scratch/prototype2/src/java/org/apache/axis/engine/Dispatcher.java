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
            String filePart = (String) msgctx.getProperty(MessageContext.REQUEST_URL);
            String soapAction = (String) msgctx.getProperty(MessageContext.SOAP_ACTION);

            if (filePart.startsWith("axis/services/")) {
                String servicePart = filePart.substring(14);
                int separator = servicePart.indexOf('/');
                if (separator > -1) {
                    uri = servicePart.substring(0, separator);
                } else {
                    uri = servicePart;
                }
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
                    throw new AxisFault("Service Not found");
                }
            }else{
                throw new AxisFault("Both the URI and SOAP_ACTION Is Null");
            }
        }else{
            //TODO client side service Dispatch ,, What this really mean?
        }
    }
}
