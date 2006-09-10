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

package org.apache.rampart.handler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.Handler;
import org.apache.rampart.MessageBuilder;
import org.apache.rampart.RampartException;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.security.WSSecurityException;


public class RampartSender implements Handler {
    
    private static HandlerDescription EMPTY_HANDLER_METADATA =
        new HandlerDescription("deafult Handler");

    private HandlerDescription handlerDesc;
    
    
    public RampartSender() {
        this.handlerDesc = EMPTY_HANDLER_METADATA;
    }
    
    public void cleanup() {        
    }

    public void init(HandlerDescription handlerdesc) {
        this.handlerDesc = handlerdesc;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.engine.Handler#invoke(org.apache.axis2.context.MessageContext)
     */
    public void invoke(MessageContext msgContext) throws AxisFault {
        
        MessageBuilder builder = new MessageBuilder();
        try {
            builder.build(msgContext);
        } catch (WSSecurityException e) {
            throw new AxisFault(e.getMessage(), e);
        } catch (WSSPolicyException e) {
            throw new AxisFault(e.getMessage(), e);
        } catch (RampartException e) {
            throw new AxisFault(e.getMessage(), e);
        }
    }

    public HandlerDescription getHandlerDesc() {
        return this.handlerDesc;
    }

    public String getName() {
        return "Apache Rampart outflow handler";
    }

    public Parameter getParameter(String name) {
        return this.handlerDesc.getParameter(name);
    }

}
