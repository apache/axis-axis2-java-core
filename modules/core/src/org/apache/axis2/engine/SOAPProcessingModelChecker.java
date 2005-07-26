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

import java.util.Iterator;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPHeaderBlock;

/**
 * This handler checks that the SOAP processing model rules have been followed
 * by the various modules and handlers.
 */
public class SOAPProcessingModelChecker extends AbstractHandler {

public void invoke(MessageContext msgContext) throws AxisFault {
    // determine SOAP version of message (waiting for Chinthaka's
    // method)
//  	boolean isSOAP12 = true;
//   
//       SOAPEnvelope se = msgContext.getEnvelope();
//        if (se.getHeader() == null) {
//            return;
//        }
//        Iterator hbs = se.getHeader().examineAllHeaderBlocks();
//        while (hbs.hasNext()) {
//            SOAPHeaderBlock hb = (SOAPHeaderBlock) hbs.next();
//
//            // if this header block has been processed or mustUnderstand isn't
//            // turned on then its cool
//            if (hb.isProcessed() || !hb.getMustUnderstand())
//                continue;
//            }
//
//            // if this header block is not targetted to me then its not my
//            // problem. Currently this code only supports the "next" role; we
//            // need to fix this to allow the engine/service to be in one or more
//            // additional roles and then to check that any headers targetted for
//            // that role too have been dealt with.
//        	if there's no role or if the role is NEXT then throw fault
//        }
    }
}
