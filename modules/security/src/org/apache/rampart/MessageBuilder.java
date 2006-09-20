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

package org.apache.rampart;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rampart.builder.AsymmetricBindingBuilder;
import org.apache.rampart.builder.SymmetricBindingBuilder;
import org.apache.rampart.builder.TransportBindingBuilder;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.security.WSSecurityException;

public class MessageBuilder {
    
    private static Log log = LogFactory.getLog(MessageBuilder.class);
    
    public void build(MessageContext msgCtx) throws WSSPolicyException,
            RampartException, WSSecurityException, AxisFault {
        

        RampartMessageData rmd = new RampartMessageData(msgCtx, true);
        
//      Nothing to do to handle the other bindings
        RampartPolicyData rpd = rmd.getPolicyData();
        if(rpd.isTransportBinding()) {
            log.debug("Building transport binding");
            TransportBindingBuilder building = new TransportBindingBuilder();
            building.build(rmd);
        } else if(rpd.isSymmetricBinding()) {
            log.debug("Building SymmetricBinding");
            SymmetricBindingBuilder builder = new SymmetricBindingBuilder();
            builder.build(rmd);
        } else {
            AsymmetricBindingBuilder builder = new AsymmetricBindingBuilder();
            builder.build(rmd);
        }
    }

}
