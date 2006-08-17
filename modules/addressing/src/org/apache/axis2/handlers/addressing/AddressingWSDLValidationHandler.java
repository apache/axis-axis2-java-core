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
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.FinalFaultsHelper;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AddressingWSDLValidationHandler extends AbstractHandler implements AddressingConstants {

    private static final Log log = LogFactory.getLog(AddressingWSDLValidationHandler.class);
    
    /* (non-Javadoc)
     * @see org.apache.axis2.engine.Handler#invoke(org.apache.axis2.context.MessageContext)
     */
    public void invoke(MessageContext msgContext) throws AxisFault {
        // Check that if wsaddressing=required that addressing headers were found inbound
        checkUsingAddressing(msgContext);
        // Check that if anonymous flag is in effect that the replyto and faultto are valid
        checkAnonymous(msgContext);
        // If no AxisOperation has been found at the end of the dispatch phase and addressing
        // is in use we should throw and ActionNotSupported Fault
        checkAction(msgContext);
    }
    
    /**
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
            Object flag = msgContext.getProperty(AddressingConstants.IS_ADDR_INFO_ALREADY_PROCESSED);
            if (log.isTraceEnabled())
                log.trace("checkUsingAddressing: WS_ADDRESSING_VERSION=" + flag);
            if (JavaUtils.isFalseExplicitly(flag)) {
                FinalFaultsHelper.triggerMessageAddressingRequiredFault(msgContext,AddressingConstants.WSA_ACTION);
            }
        }
    }
    
    /**
     * Check that if a wsaw:Anonymous value was set on the AxisOperation that the values in the
     * ReplyTo+FaultTo are valid and fault if not.
     */
    private void checkAnonymous(MessageContext msgContext) throws AxisFault {
        String anonymous = AddressingHelper.getAnonymousParameterValue(msgContext.getAxisOperation());
        if (log.isTraceEnabled())
            log.trace("checkAnonymous: Anonymous=" + anonymous);
        if("required".equals(anonymous)){
            if(AddressingHelper.isReplyRedirected(msgContext)){
                EndpointReference anonEPR = new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL);
                msgContext.setReplyTo(anonEPR);
                msgContext.setFaultTo(anonEPR);
                FinalFaultsHelper.triggerOnlyAnonymousAddressSupportedFault(msgContext, AddressingConstants.WSA_REPLY_TO);
            }
            if(AddressingHelper.isFaultRedirected(msgContext)){
                EndpointReference anonEPR = new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL);
                msgContext.setReplyTo(anonEPR);
                msgContext.setFaultTo(anonEPR);
                FinalFaultsHelper.triggerOnlyAnonymousAddressSupportedFault(msgContext, AddressingConstants.WSA_FAULT_TO);
            }
        }else if("prohibited".equals(anonymous)){
            if(!AddressingHelper.isReplyRedirected(msgContext)){
                FinalFaultsHelper.triggerOnlyNonAnonymousAddressSupportedFault(msgContext, AddressingConstants.WSA_REPLY_TO);
            }
            if(!AddressingHelper.isFaultRedirected(msgContext)){
                FinalFaultsHelper.triggerOnlyNonAnonymousAddressSupportedFault(msgContext, AddressingConstants.WSA_FAULT_TO);
            }
        }
    }
    
    /**
     * If addressing was found and the dispatch failed we SHOULD (and hence will) return a
     * WS-Addressing ActionNotSupported fault. This will make more sense once the AddressingBasedDsipatcher
     * is moved into the addressing module
     */
    private void checkAction(MessageContext msgContext) throws AxisFault{
        if(msgContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION)!=null){
            if((msgContext.getAxisService() == null) || (msgContext.getAxisOperation() == null)){
                FinalFaultsHelper.triggerActionNotSupportedFault(msgContext, msgContext.getWSAAction());
            }
        }
    }
}
