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

import org.apache.axis.Constants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.axis.handlers.OpNameFinder;
import org.apache.wsdl.WSDLService;

import javax.xml.namespace.QName;

/**
 * Class Dispatcher
 */
public class Dispatcher extends AbstractHandler implements Handler {
    /**
     * Field NAME
     */
    public static final QName NAME = new QName("http://axis.ws.apache.org",
                    "Disapatcher");

    /**
     * Constructor Dispatcher
     */
    public Dispatcher() {
        init(new HandlerMetadata(NAME));
    }

    /**
     * Method invoke
     *
     * @param msgctx
     * @throws AxisFault
     */
    public void invoke(MessageContext msgctx) throws AxisFault {
        if (msgctx.isServerSide()) {
            EndpointReference toEPR = msgctx.getTo();
            String filePart = toEPR.getAddress();
            String soapAction =
                    (String) msgctx.getProperty(MessageContext.SOAP_ACTION);
            int index = filePart.lastIndexOf('/');
            String serviceAndMethodStr = null;
            if (index > 0) {
                serviceAndMethodStr = filePart.substring(index + 1);
            }
            if (serviceAndMethodStr == null) {
                serviceAndMethodStr = soapAction;
            }
            index = serviceAndMethodStr.lastIndexOf(
                    Constants.METHOD_NAME_ESCAPE_CHARACTOR);
            QName serviceName = null;
            QName operationName = null;
            if (index > 0) {
                serviceName = new QName(serviceAndMethodStr.substring(0,
                                index - 1));
                operationName = new QName(serviceAndMethodStr.substring(index
                                        + 1));
            } else {
                serviceName = new QName(serviceAndMethodStr);
            }
            if (serviceName != null) {
                EngineRegistry registry =
                        msgctx.getGlobalContext().getRegistry();
                AxisService service = registry.getService(serviceName);
                if (service != null) {
                    msgctx.setService(service);
                    msgctx.setMessageStyle(service.getStyle());
                    if (!WSDLService.STYLE_RPC.equals(
                            msgctx.getMessageStyle())) {
                        if (operationName != null) {
                            AxisOperation op =
                                    service.getOperation(operationName);
                            if (op != null) {
                                msgctx.setOperation(op);
                            } else {
                                throw new AxisFault("Operation not found "
                                                + operationName);
                            }
                        } else {
                            throw new AxisFault("Operation Name not specifed");
                        }
                    }

                    // let add the Handlers
                    ExecutionChain chain = msgctx.getExecutionChain();
                    chain.addPhases(service.getPhases(EngineRegistry.INFLOW));

                    // add invoke Phase
                    Phase invokePhase = new Phase(Phase.SERVICE_INVOCATION);
                    invokePhase.addHandler(new OpNameFinder());
                    invokePhase.addHandler(
                            ReceiverLocator.locateReceiver(msgctx));
                    chain.addPhase(invokePhase);
                } else {
                    throw new AxisFault("Service " + serviceName
                                    + " is not found");
                }
            } else {
                throw new AxisFault("Both the URI and SOAP_ACTION Is Null");
            }
        } else {
            // TODO client side service Dispatch ,, What this really mean?
        }
    }
}
