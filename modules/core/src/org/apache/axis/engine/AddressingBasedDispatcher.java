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

import javax.xml.namespace.QName;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.OperationContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.handlers.AbstractHandler;

/**
 * Class Dispatcher
 */
public class AddressingBasedDispatcher extends AbstractHandler implements Handler {
    /**
     * Field NAME
     */
    public static final QName NAME =
        new QName("http://axis.ws.apache.org", "AddressingBasedDispatcher");
    private AxisService service;

    /**
     * Constructor Dispatcher
     */
    public AddressingBasedDispatcher() {
        init(new HandlerMetadata(NAME));
    }

    /**
     * Method invoke
     *
     * @param msgctx
     * @throws AxisFault
     */
    public void invoke(MessageContext msgctx) throws AxisFault {
        if (msgctx.getServiceContext() == null) {
            EndpointReference toEPR = msgctx.getTo();
            QName serviceName = new QName(toEPR.getAddress());
            service = msgctx.getEngineContext().getEngineConfig().getService(serviceName);
            ServiceContext serviceContext = new ServiceContext(service);
            if (service != null) {
                msgctx.setServiceContext(serviceContext);
                msgctx.setMessageStyle(service.getStyle());
                // let add the Handlers
                ExecutionChain chain = msgctx.getExecutionChain();
                chain.addPhases(serviceContext.getPhases(EngineConfiguration.INFLOW));

            } else {
                throw new AxisFault("Both the URI and SOAP_ACTION are Null");
            }
            if (msgctx.getOperationContext() == null) {
                String action = (String) msgctx.getWSAAction();
                QName operationName = new QName(action);
                AxisOperation op = service.getOperation(operationName);
                if (op != null) {
                    OperationContext opContext = new OperationContext(op);
                    msgctx.setOperationContext(opContext);
                }else{
                    throw new AxisFault("Operation not found");
                }

            }
        } else {
            // TODO client side service Dispatch ,, What this really mean?
        }
    }
}
