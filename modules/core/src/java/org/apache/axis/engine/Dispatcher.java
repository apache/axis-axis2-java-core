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
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.wsdl.WSDLService;

/**
 * Class Dispatcher
 */
public class Dispatcher extends AbstractHandler implements Handler {
    /**
     * Field NAME
     */
    public static final QName NAME = new QName("http://axis.ws.apache.org", "Disapatcher");
    private AxisService service;

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
        final String URI_ID_STRING = "/services";
        if (msgctx.isServerSide()) {

            EndpointReference toEPR = msgctx.getTo();
            String filePart = toEPR.getAddress();

            int index = filePart.lastIndexOf(URI_ID_STRING);
            String serviceStr = null;
            if (index > 0) {
                serviceStr = filePart.substring(index + URI_ID_STRING.length() + 1);
                EngineRegistry registry = msgctx.getGlobalContext().getRegistry();
                QName serviceName = new QName(serviceStr);
                service = registry.getService(serviceName);
                if (service != null) {
                    msgctx.setService(service);
                    msgctx.setMessageStyle(service.getStyle());
                    // let add the Handlers
                    ExecutionChain chain = msgctx.getExecutionChain();
                    chain.addPhases(service.getPhases(EngineRegistry.INFLOW));
                } else {
                    throw new AxisFault("Service " + serviceName + " is not found");
                }

            } else {
                throw new AxisFault("Both the URI and SOAP_ACTION are Null");
            }

            if (WSDLService.STYLE_DOC.equals(msgctx.getMessageStyle())) {
                String soapAction = (String) msgctx.getProperty(MessageContext.SOAP_ACTION);
                soapAction = soapAction.replace('"',' ').trim();
                
                if (soapAction != null && soapAction.trim().length() > 0) {
                    QName operationName = new QName(soapAction);
                    AxisOperation op = service.getOperation(operationName);
                    if (op != null) {
                        msgctx.setOperation(op);
                    }
                }
            }
        } else {
            // TODO client side service Dispatch ,, What this really mean?
        }
    }
}
