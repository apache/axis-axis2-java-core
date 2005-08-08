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
 *
 *  Runtime state of the engine
 */
package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPHeaderBlock;

import java.util.Iterator;

/**
 * This handler checks that the SOAP processing model rules have been followed
 * by the various modules and handlers.
 */
public class SOAPProcessingModelChecker extends AbstractHandler {

    public void invoke(MessageContext msgContext) throws AxisFault {
        SOAPEnvelope se = msgContext.getEnvelope();
        if (se.getHeader() == null) {
            return;
        }
        Iterator hbs = se.getHeader().examineAllHeaderBlocks();
        while (hbs.hasNext()) {
            SOAPHeaderBlock hb = (SOAPHeaderBlock) hbs.next();

            // if this header block has been processed or mustUnderstand isn't
            // turned on then its cool
            if (hb.isProcessed() || !hb.getMustUnderstand()) {
                continue;
            }
            // if this header block is not targetted to me then its not my
            // problem. Currently this code only supports the "next" role; we
            // need to fix this to allow the engine/service to be in one or more
            // additional roles and then to check that any headers targetted for
            // that role too have been dealt with.

            
            String role = hb.getRole();

            if (!msgContext.isSOAP11()) {
                //if must understand and soap 1.2 the Role should be NEXT , if it is null we considerr
                // it to be NEXT
                if (role != null && !SOAP12Constants.SOAP_ROLE_NEXT.equals(role)) {
                    throw new AxisFault("Must Understand check failed", SOAP11Constants.FAULT_CODE_MUST_UNDERSTAND);
                }
                
                //TODO what should be do with the Ulitmate Receiver? Axis2 is ultimate Receiver most of the time
                //should we support that as well
            } else {
                //if must understand and soap 1.1 the actor should be NEXT , if it is null we considerr
                // it to be NEXT
                if (role != null && !SOAP11Constants.SOAP_ACTOR_NEXT.equals(role)) {
                    throw new AxisFault("Must Understand check failed", SOAP12Constants.FAULT_CODE_MUST_UNDERSTAND);
                }
            }

        }

    }

}
