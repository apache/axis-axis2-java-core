package org.apache.axis2;

import org.apache.axis2.context.*;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.handlers.AbstractHandler;

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
 *
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

public class InstanceDispatcher extends AbstractHandler {
    /**
     * By the time the control comes to this handler the dispatching must have happened
     * so that the message context contains the ServiceGroupDescription, ServiceDescription and
     * OperationDescription.
     * This will then try to find the Contexts of ServiceGroup, Service and the Operation.
     */


    /**
     * Post Condition : All the Contexts must be populated.
     *
     * @param msgContext
     * @throws AxisFault
     */
    public void invoke(MessageContext msgContext) throws AxisFault {

        if (msgContext.getOperationContext() != null && msgContext.getServiceContext() != null) {
            return;
        }

        OperationDescription operationDesc = msgContext.getOperationDescription();

        //  1. look up opCtxt using mc.addressingHeaders.relatesTo[0]
        OperationContext operationContext = operationDesc.findForExistingOperationContext(msgContext);

        if (operationContext != null) {
            // register operation context and message context
            operationDesc.registerOperationContext(msgContext, operationContext);
            ServiceContext serviceContext = (ServiceContext) operationContext.getParent();
            ServiceGroupContext serviceGroupContext = (ServiceGroupContext) serviceContext.getParent();
            msgContext.setServiceContext(serviceContext);
            msgContext.setServiceGroupContext(serviceGroupContext);
            return;

        } else { //  2. if null, create new opCtxt
            operationContext =
                    OperationContextFactory.createOperationContext(operationDesc.getAxisSpecifMEPConstant(), operationDesc);
            operationDesc.registerOperationContext(msgContext, operationContext);

            //  fill the service group context and service context info
            msgContext.getSystemContext().
                    fillServiceContextAndServiceGroupContext(msgContext);
        }
    }
}
