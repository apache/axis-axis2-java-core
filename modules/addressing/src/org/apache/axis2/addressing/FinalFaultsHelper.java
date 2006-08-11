/**
 * 
 */
package org.apache.axis2.addressing;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
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

public class FinalFaultsHelper{
    
    private static final Log log = LogFactory.getLog(FinalFaultsHelper.class);
    
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
        triggerAddressingFault(messageContext, AddressingConstants.Final.FAULT_PROBLEM_ACTION_NAME, problemAction, Final.FAULT_ACTION_NOT_SUPPORTED, null, Final.FAULT_ACTION_NOT_SUPPORTED_REASON);
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
    
    public static OMElement getDetailElementForAddressingFault(MessageContext messageContext, OMNamespace addressingNamespaceObject){
        Map faultInfo = (Map) messageContext.getProperty(Constants.FAULT_INFORMATION_FOR_HEADERS);
        OMElement probHeaderQName = null;
        if (faultInfo != null) {
            String faultyHeaderQName = (String) faultInfo.get(Final.FAULT_HEADER_PROB_HEADER_QNAME);
            String faultyAction = (String) faultInfo.get(AddressingConstants.Final.FAULT_PROBLEM_ACTION_NAME);
            if(faultyAction!=null && !"".equals(faultyAction)){
                probHeaderQName = messageContext.getEnvelope().getOMFactory().createOMElement(AddressingConstants.Final.FAULT_PROBLEM_ACTION_NAME, addressingNamespaceObject);
                OMElement probH2 = messageContext.getEnvelope().getOMFactory().createOMElement(AddressingConstants.WSA_ACTION, addressingNamespaceObject,probHeaderQName);
                probH2.setText(faultyAction);
                
            }
            if (faultyHeaderQName != null && !"".equals(faultyHeaderQName)) {
                probHeaderQName = messageContext.getEnvelope().getOMFactory().createOMElement(Final.FAULT_HEADER_PROB_HEADER_QNAME, addressingNamespaceObject);
                probHeaderQName.setText(faultyHeaderQName);
            }
        }
        return probHeaderQName;
    }
}