/*
* Copyright 2006 The Apache Software Foundation.
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
package org.apache.axis2.addressing;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AddressingHelper {
    
    private static final Log log = LogFactory.getLog(AddressingHelper.class);
    
    /**
     * Returns true if the ReplyTo address does not match one of the supported
     * anonymous urls. If the ReplyTo is not set, anonymous is assumed, per the Final
     * spec. The AddressingInHandler should have set the ReplyTo to non-null in the 
     * 2004/08 case to ensure the different semantics. (per AXIS2-xxxx)
     * 
     * @param messageContext
     * @return
     */
    public static boolean isReplyRedirected(MessageContext messageContext){
        EndpointReference replyTo = messageContext.getReplyTo();
        if(replyTo == null){
            if(log.isDebugEnabled()){
                log.debug("isReplyRedirected: ReplyTo is null. Returning false");
            }
            return false;
        }else{
            return !replyTo.hasAnonymousAddress();
        }
    }
    
    /**
     * Returns true if the FaultTo address does not match one of the supported
     * anonymous urls. If the FaultTo is not set, the ReplyTo is checked per the
     * spec. 
     * @see isReplyRedirected
     * @param messageContext
     * @return
     */
    public static boolean isFaultRedirected(MessageContext messageContext){
        EndpointReference faultTo = messageContext.getFaultTo();
        if(faultTo == null){
            if(log.isDebugEnabled()){
                log.debug("isReplyRedirected: FaultTo is null. Returning isReplyRedirected");
            }
            return isReplyRedirected(messageContext);
        }else{
            return !faultTo.hasAnonymousAddress(); 
        }
    }
}
