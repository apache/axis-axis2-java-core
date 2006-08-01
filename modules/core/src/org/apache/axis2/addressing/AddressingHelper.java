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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AddressingHelper {
    
    private static final Log log = LogFactory.getLog(AddressingHelper.class);
    
    /**
     * Returns true if the ReplyTo address does not match one of the supported
     * anonymous urls. If the ReplyTo is not set, anonymous is assumed, per the Final
     * spec. The AddressingInHandler should have set the ReplyTo to non-null in the 
     * 2004/08 case to ensure the different semantics. (per AXIS2-885)
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
    
    public static class FinalFaults{
        
        private static final Log log = LogFactory.getLog(FinalFaults.class);
        
        //    wsa:InvalidAddressingHeader [Reason] the string: "A header representing a Message Addressing Property is not valid and the message cannot be processed"
        //      wsa:InvalidAddress
        //      wsa:InvalidEPR
        //      wsa:InvalidCardinality
        public static void triggerInvalidCardinalityFault(MessageContext messageContext, String incorrectHeaderName) throws AxisFault {
            if(log.isDebugEnabled()){
                log.debug("triggerInvalidCardinalityFault: messageContext: "+messageContext+" incorrectHeaderName: "+incorrectHeaderName);
            }
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME, AddressingConstants.WSA_DEFAULT_PREFIX + ":" + incorrectHeaderName, Final.FAULT_INVALID_HEADER, "InvalidCardinality", Final.FAULT_INVALID_HEADER_REASON);
        }

        //      wsa:MissingAddressInEPR
        //      wsa:DuplicateMessageID
        //      wsa:ActionMismatch
        public static void triggerActionMismatchFault(MessageContext messageContext) throws AxisFault {
            if(log.isDebugEnabled()){
                log.debug("triggerActionMismatchFault: messageContext: "+messageContext);
            }
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME, AddressingConstants.WSA_DEFAULT_PREFIX + ":"+"Action", Final.FAULT_INVALID_HEADER, "ActionMismatch", Final.FAULT_INVALID_HEADER_REASON);
        }

        //      wsa:OnlyAnonymousAddressSupported
        public static void triggerOnlyAnonymousAddressSupportedFault(MessageContext messageContext, String incorrectHeaderName) throws AxisFault {
            if(log.isDebugEnabled()){
                log.debug("triggerOnlyAnonymousAddressSupportedFault: messageContext: "+messageContext+" incorrectHeaderName: "+incorrectHeaderName);
            }
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME, AddressingConstants.WSA_DEFAULT_PREFIX + ":" + incorrectHeaderName, Final.FAULT_INVALID_HEADER, Final.FAULT_ONLY_ANONYMOUS_ADDRESS_SUPPORTED, Final.FAULT_INVALID_HEADER_REASON);
        }

        //      wsa:OnlyNonAnonymousAddressSupported
        public static void triggerOnlyNonAnonymousAddressSupportedFault(MessageContext messageContext, String incorrectHeaderName) throws AxisFault {
            if(log.isDebugEnabled()){
                log.debug("triggerOnlyNonAnonymousAddressSupportedFault: messageContext: "+messageContext+" incorrectHeaderName: "+incorrectHeaderName);
            }
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME, AddressingConstants.WSA_DEFAULT_PREFIX + ":" + incorrectHeaderName, Final.FAULT_INVALID_HEADER, Final.FAULT_ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED, Final.FAULT_INVALID_HEADER_REASON);
        }

        //    wsa:MessageAddressingHeaderRequired [Reason] the string: "A required header representing a Message Addressing Property is not present"
        public static void triggerMessageAddressingRequiredFault(MessageContext messageContext, String missingHeaderName) throws AxisFault {
            if(log.isDebugEnabled()){
                log.debug("triggerMessageAddressingRequiredFault: messageContext: "+messageContext+" missingHeaderName: "+missingHeaderName);
            }
            triggerAddressingFault(messageContext, Final.FAULT_HEADER_PROB_HEADER_QNAME, AddressingConstants.WSA_DEFAULT_PREFIX + ":" + missingHeaderName, Final.FAULT_ADDRESSING_HEADER_REQUIRED, null, Final.FAULT_ADDRESSING_HEADER_REQUIRED_REASON);
        }

        //    wsa:ActionNotSupported [Reason] the string: "The [action] cannot be processed at the receiver"
        public static void triggerActionNotSupportedFault(MessageContext messageContext, String problemAction) throws AxisFault {
            if(log.isDebugEnabled()){
                log.debug("triggerActionNotSupportedFault: messageContext: "+messageContext+" problemAction: "+problemAction);
            }
            triggerAddressingFault(messageContext, "PROBLEM_ACTION", problemAction, Final.FAULT_ACTION_NOT_SUPPORTED, null, Final.FAULT_ACTION_NOT_SUPPORTED_REASON);
        }
        //    wsa:EndpointUnavailable [Reason] the string "The endpoint is unable to process the message at this time"

        private static void triggerAddressingFault(MessageContext messageContext, String faultInformationKey, Object faultInformationValue, String faultcode, String faultSubcode, String faultReason) throws AxisFault{
            Map faultInformation = (Map) messageContext.getProperty(Constants.FAULT_INFORMATION_FOR_HEADERS);
            if (faultInformation == null) {
                faultInformation = new HashMap();
                messageContext.setProperty(Constants.FAULT_INFORMATION_FOR_HEADERS, faultInformation);
            }

            if(messageContext.getMessageID() != null) {
                faultInformation.put(AddressingConstants.WSA_RELATES_TO,messageContext.getMessageID());
            }

            faultInformation.put(Final.WSA_FAULT_ACTION, Final.WSA_FAULT_ACTION);
            faultInformation.put(faultInformationKey, faultInformationValue);

            if (!messageContext.isSOAP11()) {
                setFaultCode(messageContext, faultcode, faultSubcode);
            }

            messageContext.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.FALSE);
            AxisFault fault = new AxisFault(faultReason, new QName(AddressingConstants.Final.WSA_NAMESPACE,faultcode));
            throw fault;
        }

        private static void setFaultCode(MessageContext messageContext, String faultCode, String faultSubCode) {
            SOAPFactory soapFac = OMAbstractFactory.getSOAP12Factory();
            SOAPFaultCode soapFaultCode = soapFac.createSOAPFaultCode();
            SOAPFaultValue soapFaultValue = soapFac.createSOAPFaultValue(soapFaultCode);
            soapFaultValue.setText(SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX + ":" + SOAP12Constants.FAULT_CODE_SENDER);
            SOAPFaultSubCode soapFaultSubCode = soapFac.createSOAPFaultSubCode(soapFaultCode);
            SOAPFaultValue soapFaultSubcodeValue = soapFac.createSOAPFaultValue(soapFaultSubCode);
            soapFaultSubcodeValue.setText(AddressingConstants.WSA_DEFAULT_PREFIX + ":" + faultCode);
            if (faultSubCode != null) {
                SOAPFaultSubCode soapFaultSubCode2 = soapFac.createSOAPFaultSubCode(soapFaultSubCode);
                SOAPFaultValue soapFaultSubcodeValue2 = soapFac.createSOAPFaultValue(soapFaultSubCode2);
                soapFaultSubcodeValue2.setText(AddressingConstants.WSA_DEFAULT_PREFIX + ":" + faultSubCode);
            }
            messageContext.setProperty(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME, soapFaultCode);
        }
    }
}
