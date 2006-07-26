/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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

package org.apache.axis2.jaxws;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

/**
 * The AxisCallback is the touch point for asynchronous invocations 
 * between the Axis2 runtime and the JAX-WS implementation.  This
 * object will be handed to the ServiceClient/OperationClient APIs
 * to use in processing the async response.
 * 
 * The AxisCallback is responsible for taking the incoming message and
 * MessageContext from Axis2 and turning that into a MessageContext
 * that can be used by the JAX-WS implementation.
 */
public class AxisCallback extends Callback {

    private MessageContext responseMsgCtx;
    
    /**
     * This method will be called when the Axis2 implementation is
     * ready to send the async response back to the client.
     */
    public void onComplete(AsyncResult result) {
        org.apache.axis2.context.MessageContext axisMsgCtx = 
            result.getResponseMessageContext();
        responseMsgCtx = new MessageContext(axisMsgCtx);
        
        try {
            OMElement responseEnv = result.getResponseEnvelope();
            
            MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
            Message msg = mf.createFrom(responseEnv);
            
            responseMsgCtx.setMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // FIXME: Figure out what needs to be done when this method is called
    // and we've received an error from Axis2.
    public void onError(Exception e) {
        e.printStackTrace();
    }
    
    /**
     * Returns the <@link org.apache.axis2.jaxws.core.MessageContext> that was
     * created for the response message.
     * @return - a MessageContext with the response contents
     */
    public MessageContext getResponseMessageContext() {
        return responseMsgCtx;
    }
}
