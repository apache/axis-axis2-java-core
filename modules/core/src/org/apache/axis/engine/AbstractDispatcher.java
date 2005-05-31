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

import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.OperationContext;
import org.apache.axis.description.HandlerDescription;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.handlers.AbstractHandler;

import javax.xml.namespace.QName;

/**
 * Class Dispatcher
 */
public abstract class AbstractDispatcher extends AbstractHandler implements Handler {
    /**
     * Field NAME
     */
    public static final QName NAME =
        new QName("http://axis.ws.apache.org", "AddressingBasedDispatcher");

    /**
     * Constructor Dispatcher
     */
    private ConfigurationContext engineContext;
    
    public AbstractDispatcher() {
        init(new HandlerDescription(NAME));
    }

    /**
     * Method invoke
     *
     * @param msgctx
     * @throws AxisFault
     */
    public final void invoke(MessageContext msgctx) throws AxisFault {
  
        if(msgctx.getServiceContext() == null){
            ServiceDescription axisService = findService(msgctx);
            if(axisService != null){
                msgctx.setServiceContext(axisService.findServiceContext(msgctx));
            }
        }

        if (msgctx.getServiceContext() != null && msgctx.getOperationContext() == null) {
            OperationDescription axisOperation = findOperation(msgctx.getServiceContext().getServiceConfig(),msgctx);
            if(axisOperation != null){
                OperationContext operationContext = axisOperation.findOperationContext(msgctx,msgctx.getServiceContext(),msgctx.isServerSide());
                msgctx.setOperationContext(operationContext);
            }
        }

    }
    
    public abstract ServiceDescription findService(MessageContext messageContext)throws AxisFault;
    public abstract OperationDescription findOperation(ServiceDescription service,MessageContext messageContext)throws AxisFault;
    
}
