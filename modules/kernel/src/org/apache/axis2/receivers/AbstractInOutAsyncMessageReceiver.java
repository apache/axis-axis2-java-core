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


package org.apache.axis2.receivers;

import org.apache.axis2.AxisFault;

import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is takes care of the IN-OUT sync MEP in the server side
 */
public abstract class AbstractInOutAsyncMessageReceiver extends AbstractMessageReceiver {
    private static final Log log = LogFactory.getLog(AbstractInOutAsyncMessageReceiver.class);

    public abstract void invokeBusinessLogic(MessageContext inMessage,
                                             MessageContext outMessage) throws AxisFault;

    public final void receive(final MessageContext messageCtx) throws AxisFault {
        final ServerCallback callback = new ServerCallback() {
            public void handleResult(MessageContext result) throws AxisFault {
                AxisEngine.send(result);
                result.getTransportOut().getSender().cleanup(result);
            }

            public void handleFault(AxisFault fault) throws AxisFault {
                MessageContext faultContext =
                        MessageContextBuilder.createFaultMessageContext(messageCtx, fault);

                AxisEngine.sendFault(faultContext);
            }
        };
        
			
        AsyncMessageReceiverWorker theadedTask = new AsyncMessageReceiverWorker(messageCtx,callback);
		messageCtx.getEnvelope().build();
		
		
		/**
		 * If this type of Message Reciecer is used without addressing, the condtions given in Jira 
		 * https://issues.apache.org/jira/browse/AXIS2-1363 would occuer(Both 202 accepted and 200 OK is written to the 
		 * output stream). To avoid it if addressing is not present we do not spawn a new thread. This will disapper when
		 * async message recivers are removed later
		 */
		
		if(messageCtx.getReplyTo() != null 
				&& !AddressingConstants.Submission.WSA_ANONYMOUS_URL.equals(messageCtx.getReplyTo().getAddress())
				&& !AddressingConstants.Final.WSA_ANONYMOUS_URL.equals(messageCtx.getReplyTo().getAddress())){
			messageCtx.getConfigurationContext().getThreadPool().execute(theadedTask);
		}else{
			theadedTask.run();
		}
    }
    public class AsyncMessageReceiverWorker implements Runnable{
    	private MessageContext messageCtx;
    	private ServerCallback callback;
    	
    	public AsyncMessageReceiverWorker(MessageContext messageCtx,ServerCallback callback){
    		this.messageCtx = messageCtx;
    		this.callback = callback;
    	}
    	
        public void run() {
            try {
                MessageContext newmsgCtx =
                        MessageContextBuilder.createOutMessageContext(messageCtx);
                newmsgCtx.getOperationContext().addMessageContext(newmsgCtx);
                ThreadContextDescriptor tc = setThreadContext(messageCtx);
                try {
                    invokeBusinessLogic(messageCtx, newmsgCtx);
                } finally {
                    restoreThreadContext(tc);
                }
                callback.handleResult(newmsgCtx);
            } catch (AxisFault e) {
                try {
                    callback.handleFault(e);
                } catch (AxisFault axisFault) {
                    log.error(e);
                }
                log.error(e);
            }
        }
    };
    
}
