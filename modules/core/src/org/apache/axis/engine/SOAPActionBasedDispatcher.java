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

import org.apache.axis.context.MessageContext;
import org.apache.axis.description.HandlerDescription;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.ServiceDescription;

/**
 * Class Dispatcher
 */
public class SOAPActionBasedDispatcher extends AbstractDispatcher {
    /**
     * Field NAME
     */
    public static final QName NAME =
        new QName("http://axis.ws.apache.org", "SOAPActionBasedDispatcher");

    public SOAPActionBasedDispatcher() {
        init(new HandlerDescription(NAME));
    }

    public OperationDescription findOperation(
        ServiceDescription service,
        MessageContext messageContext)
        throws AxisFault {

        String action = (String) messageContext.getSoapAction();
        if (action != null) {
            OperationDescription op = service.getOperationBySOAPAction(action);
            return op;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.axis.engine.AbstractDispatcher#findService(org.apache.axis.context.MessageContext)
     */
    public ServiceDescription findService(MessageContext messageContext) throws AxisFault {
        return null;
    }

}
