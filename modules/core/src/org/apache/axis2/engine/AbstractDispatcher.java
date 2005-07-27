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
package org.apache.axis2.engine;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.AxisFault;

import javax.xml.namespace.QName;

/**
 * This the base class for all dispatchers, it is a Handler which has a one 
 * traget, that is to find the Service a given SOAP message is targeted to.
 * 
 * Axis2 service dispatching is model via a Chain of diapatchers, each trying to 
 * Diaptach but let go without throwing a execption in case they fail. 
 */
public abstract class AbstractDispatcher extends AbstractHandler implements Handler {
    /**
     * Field NAME
     */
    public static final QName NAME =
            new QName("http://axis.ws.apache.org",
                    "AddressingBasedDispatcher");

    /**
     * Constructor Dispatcher
     */
    private ConfigurationContext engineContext;

    public AbstractDispatcher() {
        init(new HandlerDescription(NAME));
    }

    /**
     * This is final, obivously not for overiding
     *
     * @param msgctx
     * @throws org.apache.axis2.AxisFault
     */
    public final void invoke(MessageContext msgctx) throws AxisFault {

        if (msgctx.getServiceContext() == null) {
            ServiceDescription axisService = findService(msgctx);
            if (axisService != null) {
                msgctx.setServiceContext(
                        axisService.findServiceContext(msgctx));
            }
        }

        if (msgctx.getServiceContext() != null &&
                msgctx.getOperationContext() == null) {
            OperationDescription axisOperation = findOperation(
                    msgctx.getServiceContext().getServiceConfig(), msgctx);
            if (axisOperation != null) {
                OperationContext operationContext = axisOperation.findOperationContext(
                        msgctx, msgctx.getServiceContext());
                msgctx.setOperationContext(operationContext);
            }
        }

    }

    /**
     * Give the diaptacher turn to find the Service
     * @param messageContext
     * @return
     * @throws AxisFault
     */
    public abstract ServiceDescription findService(
            MessageContext messageContext) throws AxisFault;

    /**
     * Give the diaptacher turn to find the Operation
     * @param service
     * @param messageContext
     * @return
     * @throws AxisFault
     */
    public abstract OperationDescription findOperation(
            ServiceDescription service, MessageContext messageContext) throws AxisFault;

}
