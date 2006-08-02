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
package org.apache.axis2.handlers.addressing;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingHelper.FinalFaults;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AddressingWSDLValidationHandler extends AbstractHandler implements AddressingConstants {

    private static final Log log = LogFactory.getLog(AddressingWSDLValidationHandler.class);
    
    public void invoke(MessageContext msgContext) throws AxisFault {
        // Check that if wsaddressing=required that addressing headers were found inbound
        checkUsingAddressing(msgContext);
        // Check that if anonymous flag is in effect that the replyto and faultto are valid
            // Not yet implemented
        // If no AxisOperation has been found at the end of the dispatch phase and addressing
        // is in use we should throw and ActionNotSupported Fault
            // Not yet implemented
    }
    
    /*
     * Check that if the wsaddressing="required" attribute exists on the service
     * definition or <wsaw:UsingAddressing wsdl:required="true" /> was found in the
     * WSDL that WS-Addressing headers were found on the inbound message
     */
    private void checkUsingAddressing(MessageContext msgContext)
            throws AxisFault {
        String addressingFlag = msgContext.getAxisService().getWSAddressingFlag();
        if (log.isTraceEnabled())
            log.trace("checkUsingAddressing: WSAddressingFlag=" + addressingFlag);
        if (AddressingConstants.ADDRESSING_REQUIRED.equals(addressingFlag)) {
            Object flag = msgContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
            if (log.isTraceEnabled())
                log.trace("checkUsingAddressing: WS_ADDRESSING_VERSION=" + flag);
            if (flag == null) {
                FinalFaults.triggerMessageAddressingRequiredFault(msgContext,AddressingConstants.WSA_ACTION);
            }
        }
    }
}
