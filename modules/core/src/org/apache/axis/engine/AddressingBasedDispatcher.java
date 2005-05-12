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
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.description.HandlerDescription;

/**
 * Class Dispatcher
 */
public class AddressingBasedDispatcher extends AbstractDispatcher {
    /**
     * Field NAME
     */
    public static final QName NAME =
        new QName("http://axis.ws.apache.org", "AddressingBasedDispatcher");

    /**
     * Constructor Dispatcher
     */
    private ConfigurationContext engineContext;

    public AddressingBasedDispatcher() {
        init(new HandlerDescription(NAME));
    }

  
    public OperationDescription findOperation(ServiceDescription service, MessageContext messageContext)
        throws AxisFault {

        String action = (String) messageContext.getWSAAction();
        if (action != null) {
            QName operationName = new QName(action);
            OperationDescription op = service.getOperation(operationName);
            if (op != null) {
                return op;
            } else {
                throw new AxisFault("No Operation named " + operationName + " Not found");
            }
            //if no operation found let it go, this is for a handler may be. e.g. Create Sequance in RM
        } else {
            throw new AxisFault("Operation not found, WSA Action is Null");
        }

    }

    /* (non-Javadoc)
     * @see org.apache.axis.engine.AbstractDispatcher#findService(org.apache.axis.context.MessageContext)
     */
    public ServiceDescription findService(MessageContext messageContext) throws AxisFault {
        EndpointReference toEPR = messageContext.getTo();
        QName serviceName = new QName(toEPR.getAddress());
        ServiceDescription service = engineContext.getEngineConfig().getService(serviceName);

        if (service != null) {
            return service;
        } else {
            throw new AxisFault("No service found under the " + toEPR.getAddress());
        }
    }

}
