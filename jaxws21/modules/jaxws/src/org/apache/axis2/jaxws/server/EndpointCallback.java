/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxws.server;

import org.apache.axis2.context.OperationContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.message.util.MessageUtils;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.axis2.util.ThreadContextMigratorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EndpointCallback {
    
    private static final Log log = LogFactory.getLog(EndpointCallback.class);
    
    public void handleResponse(EndpointInvocationContext eic) {
        MessageContext responseMsgCtx = eic.getResponseMessageContext();
        org.apache.axis2.context.MessageContext axisResponseMsgCtx =
                responseMsgCtx.getAxisMessageContext();

        try {
            MessageUtils.putMessageOnMessageContext(responseMsgCtx.getMessage(),
                                                    axisResponseMsgCtx);

            OperationContext opCtx = axisResponseMsgCtx.getOperationContext();
            opCtx.addMessageContext(axisResponseMsgCtx);
            
            // This assumes that we are on the ultimate execution thread
            ThreadContextMigratorUtil.performMigrationToContext(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID,
                                                                axisResponseMsgCtx);
    
            //Create the AxisEngine for the reponse and send it.
            AxisEngine engine =
                    new AxisEngine(axisResponseMsgCtx.getConfigurationContext());
            if (log.isDebugEnabled()) {
                log.debug("Sending async response.");
            }
            engine.send(axisResponseMsgCtx);
            
            //This assumes that we are on the ultimate execution thread
            ThreadContextMigratorUtil.performContextCleanup(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID,
                                                            axisResponseMsgCtx);
        } catch (Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug("An error occurred while attempting to send the async response.");
                t.printStackTrace();
            }
            
            ThreadContextMigratorUtil.performThreadCleanup(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID,
                eic.getRequestMessageContext().getAxisMessageContext());
            
            // FIXME (NLG): This is probably not right
            handleFaultResponse(eic);
        } 
    }
    
    public void handleFaultResponse(EndpointInvocationContext eic) {
        MessageContext responseMsgCtx = eic.getResponseMessageContext();
        org.apache.axis2.context.MessageContext axisResponseMsgCtx =
                responseMsgCtx.getAxisMessageContext();
        
        try {
            MessageUtils.putMessageOnMessageContext(responseMsgCtx.getMessage(),
                axisResponseMsgCtx);

            OperationContext opCtx = axisResponseMsgCtx.getOperationContext();
            opCtx.addMessageContext(axisResponseMsgCtx);
            
            ThreadContextMigratorUtil.performThreadCleanup(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID,
                eic.getRequestMessageContext().getAxisMessageContext());
            
            //Create the AxisEngine for the reponse and send it.
            AxisEngine engine =
                new AxisEngine(axisResponseMsgCtx.getConfigurationContext());
            engine.sendFault(axisResponseMsgCtx);
            
        } catch (Throwable t) {
            // TODO Auto-generated catch block
            t.printStackTrace();
        }
    }
    
}
