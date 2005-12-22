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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.*;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.handlers.AbstractHandler;

/**
 * By the time the control comes to this handler, the dispatching must have happened
 * so that the message context contains the AxisServiceGroup, AxisService and
 * AxisOperation.
 * This will then try to find the Contexts of ServiceGroup, Service and the Operation.
 */
public class InstanceDispatcher extends AbstractHandler {

    /**
     * Post Condition : All the Contexts must be populated.
     *
     * @param msgContext
     * @throws org.apache.axis2.AxisFault
     */
    public void invoke(MessageContext msgContext) throws AxisFault {
        if ((msgContext.getOperationContext() != null)
                && (msgContext.getServiceContext() != null)) {
            msgContext.setServiceGroupContextId(
                    ((ServiceGroupContext) msgContext.getServiceContext().getParent()).getId());

            return;
        }
        //trying to get service context from Session context
        fillContextsFromSessionContetxt(msgContext);

        AxisOperation axisOperation = msgContext.getAxisOperation();

        // 1. look up opCtxt using mc.addressingHeaders.relatesTo[0]
        if (axisOperation == null) {
            return;
        }

        OperationContext operationContext =
                axisOperation.findForExistingOperationContext(msgContext);

        if (operationContext != null) {

            // register operation context and message context
            axisOperation.registerOperationContext(msgContext, operationContext);

            ServiceContext serviceContext = (ServiceContext) operationContext.getParent();
            ServiceGroupContext serviceGroupContext =
                    (ServiceGroupContext) serviceContext.getParent();

            msgContext.setServiceContext(serviceContext);
            msgContext.setServiceGroupContext(serviceGroupContext);
            msgContext.setServiceGroupContextId(serviceGroupContext.getId());
        } else {    // 2. if null, create new opCtxt
            operationContext = new OperationContext(axisOperation);

            axisOperation.registerOperationContext(msgContext, operationContext);
            if (msgContext.getServiceContext() != null) {
                // no need to added to configuration conetxt , since we are happy in
                //  storing in session context
                operationContext.setParent(msgContext.getServiceContext());
            } else {
                // fill the service group context and service context info
                msgContext.getConfigurationContext().fillServiceContextAndServiceGroupContext(
                        msgContext);
            }
        }
    }

    private void fillContextsFromSessionContetxt(MessageContext msgContext) throws AxisFault {
        AxisService service = msgContext.getAxisService();
        if (service == null) {
            throw new AxisFault("AxisService Not found yet");
        }
        SessionContext sessionContext = msgContext.getSessionContext();
        String serviceGroupContextId = msgContext.getServiceGroupContextId();
        if (serviceGroupContextId != null) {
            //setting service group context which is teken from session context
            msgContext.setServiceGroupContext(
                    sessionContext.getServiceGroupContext(serviceGroupContextId));
        }
        String scope = service.getScope();

        if (Constants.TRANSPORT_SESSION_SCOPE.equals(scope)) {
            ServiceContext sreviceContext = sessionContext.getServiceContext(service);
            //found the serviceContext from session context , so adding that into msgContext
            if (sreviceContext != null) {
                msgContext.setServiceContext(sreviceContext);
            }
        }
    }

}
