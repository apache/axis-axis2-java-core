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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;

import javax.xml.namespace.QName;

/**
 * Dispatches based on the SOAPAction 
 */
public class SOAPActionBasedDispatcher extends AbstractDispatcher {
    /**
     * Field NAME
     */
    public static final QName NAME =
            new QName("http://axis.ws.apache.org",
                    "SOAPActionBasedDispatcher");

     public void initDispatcher() {
        init(new HandlerDescription(NAME));
    }
    public OperationDescription findOperation(ServiceDescription service,
                                              MessageContext messageContext)
            throws AxisFault {

        String action = messageContext.getSoapAction();
        if (action != null) {
            OperationDescription op = service.getOperationBySOAPAction(action);
            if (op == null) {
                op = service.getOperation(new QName(action));
            }
            /* HACK: Please remove this when we add support for custom action uri */
            if (op == null && action.lastIndexOf('/')!=-1) {
                op = service.getOperation(new QName(action.substring(action.lastIndexOf('/'),action.length())));
            }
            return op;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.engine.AbstractDispatcher#findService(org.apache.axis2.context.MessageContext)
     */
    public ServiceDescription findService(MessageContext messageContext) throws AxisFault {
        return null;
    }

}
