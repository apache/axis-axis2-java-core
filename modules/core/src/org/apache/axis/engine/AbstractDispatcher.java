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

import org.apache.axis.context.EngineContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.handlers.AbstractHandler;

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
    private EngineContext engineContext;
    
    public AbstractDispatcher() {
        init(new HandlerMetadata(NAME));
    }

    /**
     * Method invoke
     *
     * @param msgctx
     * @throws AxisFault
     */
    public final void invoke(MessageContext msgctx) throws AxisFault {
  
        if(msgctx.getServiceContext() == null){
            AxisService axisService = findService(msgctx);
            if(axisService != null){
                msgctx.setServiceContext(axisService.findServiceContext(msgctx));
            }
        }

        if (msgctx.getServiceContext() == null && msgctx.getOperationContext() == null) {
            AxisOperation axisOperation = findOperation(msgctx.getServiceContext().getServiceConfig(),msgctx);
            if(axisOperation != null){
                msgctx.setOperationContext(axisOperation.findOperationContext(msgctx,msgctx.getServiceContext(),msgctx.isServerSide()));
            }
        }

    }
    
    public abstract AxisService findService(MessageContext messageContext)throws AxisFault;
    public abstract AxisOperation findOperation(AxisService service,MessageContext messageContext)throws AxisFault;
    
}
