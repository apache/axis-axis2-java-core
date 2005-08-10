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
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;

import javax.xml.namespace.QName;

/**
 * Dispatcher based on the WS-Addressing properties
 */
public class AddressingBasedDispatcher extends AbstractDispatcher {
    /**
     * Field NAME
     */
    public static final QName NAME =
            new QName("http://axis.ws.apache.org",
                    "AddressingBasedDispatcher");

    public AddressingBasedDispatcher() {
        init(new HandlerDescription(NAME));
    }
    //TODO this logic needed to be improved, as the Dispatching is almost garentnee to fail
    public OperationDescription findOperation(ServiceDescription service,
                                              MessageContext messageContext)
            throws AxisFault {

        String action = messageContext.getWSAAction();
        if (action != null) {
            QName operationName = new QName(action);
            return service.getOperation(operationName);
        }
        return null;
    }

    //  TODO this logic needed to be improved, as the Dispatching is almost garentnee to fail
    public ServiceDescription findService(MessageContext messageContext) throws AxisFault {
        EndpointReference toEPR = messageContext.getTo();
        ServiceDescription service = null;
        if (toEPR != null) {
            QName serviceName = new QName(toEPR.getAddress());
            service =
                    messageContext.getSystemContext().getAxisConfiguration()
                    .getService(serviceName);
        }
        return service;
    }

}
