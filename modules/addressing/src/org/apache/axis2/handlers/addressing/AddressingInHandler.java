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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class AddressingInHandler extends AddressingHandler implements AddressingConstants {

    private static final long serialVersionUID = 3907988439637261572L;
    private OMNamespace addressingNSObject;

    public void invoke(MessageContext msgContext) throws AxisFault {
        String namespace = addressingNamespace;
        SOAPHeader header = msgContext.getEnvelope().getHeader();

        // if there is some one who has already found addressing, do not do anything here.
        if (msgContext.getProperty(WS_ADDRESSING_VERSION) != null) {
            return;
        }

        // if there are not headers put a flag to disable addressing temporary
        if (header == null) {
            msgContext.setProperty(Constants.Configuration.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
            return;
        }

        logger.debug("Starting " + addressingVersion + " IN handler ...");

        ArrayList addressingHeaders;
        addressingHeaders = header.getHeaderBlocksWithNSURI(namespace);
        if (addressingHeaders != null && addressingHeaders.size() > 0) {
            msgContext.setProperty(WS_ADDRESSING_VERSION, namespace);
            msgContext.setProperty(Constants.Configuration.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.FALSE);
            addressingNSObject = ((OMElement) addressingHeaders.get(0)).findNamespace(namespace, "");
            logger.debug(addressingVersion + " Headers present in the SOAP message. Starting to process ...");
            extractAddressingInformation(header, msgContext, addressingHeaders, namespace);
        } else {
            msgContext.setProperty(Constants.Configuration.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
            logger.debug("No Headers present corresponding to " + addressingVersion);
        }


    }

    protected Options extractAddressingInformation(SOAPHeader header, MessageContext messageContext,
                                                   ArrayList addressingHeaders, String namespace) throws AxisFault {

        Options messageContextOptions = messageContext.getOptions();
        
        ArrayList checkedHeaderNames = new ArrayList(7); // Up to 7 header names to be recorded
        ArrayList duplicateHeaderNames = new ArrayList(1); // Normally will not be used for more than 1 header
        
        // Per the SOAP Binding spec "headers with an incorrect cardinality MUST NOT be used" So these variables
        // are used to keep track of invalid cardinality headers so they are not deserialised.
        boolean ignoreTo = false, ignoreFrom = false, ignoreReplyTo = false, ignoreFaultTo = false, ignoreMessageID = false, ignoreAction = false; 
        
        // First pass just check for duplicates
        Iterator addressingHeadersIt = addressingHeaders.iterator();
        while (addressingHeadersIt.hasNext()) {
            SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) addressingHeadersIt.next();
            if (SOAP12Constants.SOAP_ROLE_NONE.equals(soapHeaderBlock.getRole()))
                continue;

	        if (WSA_TO.equals(soapHeaderBlock.getLocalName())) {
	            ignoreTo = checkDuplicateHeaders(WSA_TO, checkedHeaderNames, duplicateHeaderNames);
	        } else if (WSA_FROM.equals(soapHeaderBlock.getLocalName())) {
	            ignoreFrom = checkDuplicateHeaders(WSA_FROM, checkedHeaderNames, duplicateHeaderNames);
	        } else if (WSA_REPLY_TO.equals(soapHeaderBlock.getLocalName())) {
	            ignoreReplyTo = checkDuplicateHeaders(WSA_REPLY_TO, checkedHeaderNames, duplicateHeaderNames);
	        } else if (WSA_FAULT_TO.equals(soapHeaderBlock.getLocalName())) {
	            ignoreFaultTo = checkDuplicateHeaders(WSA_FAULT_TO, checkedHeaderNames, duplicateHeaderNames);
	        } else if (WSA_MESSAGE_ID.equals(soapHeaderBlock.getLocalName())) {
	            ignoreMessageID = checkDuplicateHeaders(WSA_MESSAGE_ID, checkedHeaderNames, duplicateHeaderNames);
	        } else if (WSA_ACTION.equals(soapHeaderBlock.getLocalName())) {
	            ignoreAction = checkDuplicateHeaders(WSA_ACTION, checkedHeaderNames, duplicateHeaderNames);
	        }
        }
        
        // Now extract information
        Iterator addressingHeadersIt2 = addressingHeaders.iterator();
        while (addressingHeadersIt2.hasNext()) {
            SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) addressingHeadersIt2.next();
            if (SOAP12Constants.SOAP_ROLE_NONE.equals(soapHeaderBlock.getRole()))
                continue;
            
            if (WSA_TO.equals(soapHeaderBlock.getLocalName()) && !ignoreTo) {
                extractToEPRInformation(soapHeaderBlock, messageContextOptions, header, namespace);
            } else if (WSA_FROM.equals(soapHeaderBlock.getLocalName()) && !ignoreFrom) {
                extractFromEPRInformation(messageContextOptions, soapHeaderBlock, namespace);
            } else if (WSA_REPLY_TO.equals(soapHeaderBlock.getLocalName()) && !ignoreReplyTo) {
                extractReplyToEPRInformation(messageContextOptions, soapHeaderBlock, namespace);
            } else if (WSA_FAULT_TO.equals(soapHeaderBlock.getLocalName()) && !ignoreFaultTo) {
                extractFaultToEPRInformation(messageContextOptions, soapHeaderBlock, namespace);
            } else if (WSA_MESSAGE_ID.equals(soapHeaderBlock.getLocalName()) && !ignoreMessageID) {
                messageContextOptions.setMessageId(soapHeaderBlock.getText());
                soapHeaderBlock.setProcessed();
            } else if (WSA_ACTION.equals(soapHeaderBlock.getLocalName()) && !ignoreAction) {
                messageContextOptions.setAction(soapHeaderBlock.getText());
                soapHeaderBlock.setProcessed();
            } else if (WSA_RELATES_TO.equals(soapHeaderBlock.getLocalName())) {
                extractRelatesToInformation(soapHeaderBlock, namespace, messageContextOptions);
            }
        }

        // Now that all the valid wsa headers have been read, throw an exception if there was an invalid cardinality
        // This means that if for example there are multiple MessageIDs and a FaultTo, the FaultTo will be respected.
        if(!duplicateHeaderNames.isEmpty()){
        	// Simply choose the first problem header we came across as we can only fault for one of them.
        	throwFault(messageContext,(String)duplicateHeaderNames.get(0),Final.FAULT_INVALID_HEADER, Final.FAULT_INVALID_CARDINALITY);
        }
        
        // check for the presence of madatory addressing headers
        checkForMandatoryHeaders(checkedHeaderNames, messageContext);

        return messageContextOptions;
    }

    private void checkForMandatoryHeaders(ArrayList alreadyFoundAddrHeader, MessageContext messageContext) throws AxisFault {
        if (!alreadyFoundAddrHeader.contains(WSA_ACTION)) {
            throwFault(messageContext, WSA_ACTION, Final.FAULT_ADDRESSING_HEADER_REQUIRED, null);
        } 
    }

    private boolean checkDuplicateHeaders(String addressingHeaderName, ArrayList checkedHeaderNames, ArrayList duplicateHeaderNames) {//throws AxisFault {
    	// If the header name has been seen before then we should return true and add it to the list
    	// of duplicate header names. Otherwise it is the first time we've seen the header so add it
    	// to the checked liat and return false. 
    	boolean shouldIgnore = checkedHeaderNames.contains(addressingHeaderName);
    	if(shouldIgnore){
    		duplicateHeaderNames.add(addressingHeaderName);
    	}else{
    		checkedHeaderNames.add(addressingHeaderName);
    	}
    	return shouldIgnore;
    }

    private void throwFault(MessageContext messageContext, String addressingHeaderName, String faultCode, String faultSubCode) throws AxisFault {
        Map faultInformation = (Map) messageContext.getProperty(Constants.FAULT_INFORMATION_FOR_HEADERS);
        if (faultInformation == null) {
            faultInformation = new HashMap();
            messageContext.setProperty(Constants.FAULT_INFORMATION_FOR_HEADERS, faultInformation);
        }

        if(messageContext.getMessageID() != null) {
            faultInformation.put(AddressingConstants.WSA_RELATES_TO,messageContext.getMessageID());
        }
        
        faultInformation.put(Final.FAULT_HEADER_PROB_HEADER_QNAME, WSA_DEFAULT_PREFIX + ":" + addressingHeaderName);
        faultInformation.put(Final.WSA_FAULT_ACTION, Final.WSA_FAULT_ACTION);
        if (!messageContext.isSOAP11()) {
            Utils.setFaultCode(messageContext, faultCode, faultSubCode);
        }
        throw new AxisFault("A header representing a Message Addressing Property is not valid and the message cannot be processed", WSA_DEFAULT_PREFIX + ":" + faultCode);
    }

    protected abstract void extractToEprReferenceParameters(EndpointReference toEPR, SOAPHeader header, String namespace);


    private void extractRelatesToInformation(SOAPHeaderBlock soapHeaderBlock, String addressingNamespace, Options messageContextOptions) {
        String address = soapHeaderBlock.getText();
        OMAttribute relationshipType =
                soapHeaderBlock.getAttribute(
                        new QName(AddressingConstants.WSA_RELATES_TO_RELATIONSHIP_TYPE));
        String relationshipTypeDefaultValue =
                Submission.WSA_NAMESPACE.equals(addressingNamespace)
                        ? Submission.WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE
                        : Final.WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE;
        RelatesTo relatesTo =
                new RelatesTo(
                        address,
                        relationshipType == null
                                ? relationshipTypeDefaultValue
                                : relationshipType.getAttributeValue());
        messageContextOptions.addRelatesTo(relatesTo);
        soapHeaderBlock.setProcessed();
    }

    private void extractFaultToEPRInformation(Options messageContextOptions, SOAPHeaderBlock soapHeaderBlock, String addressingNamespace) {
        EndpointReference epr;
        epr = messageContextOptions.getFaultTo();
        if (epr == null) {
            epr = new EndpointReference("");
            messageContextOptions.setFaultTo(epr);
        }
        extractEPRInformation(soapHeaderBlock, epr, addressingNamespace);
        soapHeaderBlock.setProcessed();
    }

    private void extractReplyToEPRInformation(Options messageContextOptions, SOAPHeaderBlock soapHeaderBlock, String addressingNamespace) {
        EndpointReference epr;
        epr = messageContextOptions.getReplyTo();
        if (epr == null) {
            epr = new EndpointReference("");
            messageContextOptions.setReplyTo(epr);
        }
        extractEPRInformation(soapHeaderBlock, epr, addressingNamespace);
        soapHeaderBlock.setProcessed();
    }

    private void extractFromEPRInformation(Options messageContextOptions, SOAPHeaderBlock soapHeaderBlock, String addressingNamespace) {
        EndpointReference epr;
        epr = messageContextOptions.getFrom();
        if (epr == null) {
            epr = new EndpointReference("");  // I don't know the address now. Let me pass the empty string now and fill this
            // once I process the Elements under this.
            messageContextOptions.setFrom(epr);
        }
        extractEPRInformation(soapHeaderBlock, epr, addressingNamespace);
        soapHeaderBlock.setProcessed();
    }

    private void extractToEPRInformation(SOAPHeaderBlock soapHeaderBlock, Options messageContextOptions, SOAPHeader header, String namespace) {

        EndpointReference epr;
        //here the addressing epr overidde what ever already there in the message context
        epr = new EndpointReference(soapHeaderBlock.getText());
        messageContextOptions.setTo(epr);

        // check for reference parameters
        extractToEprReferenceParameters(epr, header, namespace);
        soapHeaderBlock.setProcessed();

    }

    /**
     * Given the soap header block, this should extract the information within EPR.
     *
     * @param headerBlock
     * @param epr
     * @param addressingNamespace
     */
    protected abstract void extractEPRInformation(SOAPHeaderBlock headerBlock, EndpointReference epr, String addressingNamespace);

    /**
     * @param expectedQName
     * @param actualQName
     */
    protected boolean checkElement(QName expectedQName, QName actualQName) {
        return (expectedQName.getLocalPart().equals(actualQName.getLocalPart()) && expectedQName.getNamespaceURI().equals(actualQName.getNamespaceURI()));
    }
}
