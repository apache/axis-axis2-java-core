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
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.AddressingFaultsHelper;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.util.JavaUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class AddressingInHandler extends AddressingHandler implements AddressingConstants {

    private static final long serialVersionUID = 3907988439637261572L;

    private static final Log log = LogFactory.getLog(AddressingInHandler.class);
    private static final boolean isDebugEnabled = log.isDebugEnabled();

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        // if another handler has already processed the addressing headers, do not do anything here.
        if (JavaUtils.isTrueExplicitly(msgContext.getProperty(IS_ADDR_INFO_ALREADY_PROCESSED))) {
            if(isDebugEnabled) {
                log.debug("Another handler has processed the addressing headers. Nothing to do here.");
            }

            return InvocationResponse.CONTINUE;        
        }
        
        // check whether someone has explicitly set which addressing handler should run.
        String namespace = (String) msgContext.getProperty(WS_ADDRESSING_VERSION);
        if (namespace == null) { 
            namespace = addressingNamespace;
        }
        else if (!namespace.equals(addressingNamespace)) {
            if(isDebugEnabled) {
                log.debug("This addressing handler does not match the specified namespace, " + namespace);
            }

            return InvocationResponse.CONTINUE;        
        }

        SOAPHeader header = null;
        if(msgContext.isHeaderPresent()) {
            header = msgContext.getEnvelope().getHeader();
        }
        
        // if there are not headers put a flag to disable addressing temporary
        if (header == null) {
            msgContext.setProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
            return InvocationResponse.CONTINUE;        
        }

		if(isDebugEnabled) {
			log.debug("Starting " + addressingVersion + " IN handler ...");
		}

        ArrayList addressingHeaders;
        addressingHeaders = header.getHeaderBlocksWithNSURI(namespace);
        if (addressingHeaders != null && addressingHeaders.size() > 0) {
            msgContext.setProperty(WS_ADDRESSING_VERSION, namespace);
            msgContext.setProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.FALSE);

			if(isDebugEnabled) {
				log.debug(addressingVersion + " Headers present in the SOAP message. Starting to process ...");
			}
            extractAddressingInformation(header, msgContext, addressingHeaders, namespace);
            msgContext.setProperty(IS_ADDR_INFO_ALREADY_PROCESSED, Boolean.TRUE);
        } else {
            msgContext.setProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
			if(isDebugEnabled) {
				log.debug("No Headers present corresponding to " + addressingVersion);
			}
        }
        return InvocationResponse.CONTINUE;        
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
                extractFromEPRInformation(soapHeaderBlock, namespace, messageContext);
            } else if (WSA_REPLY_TO.equals(soapHeaderBlock.getLocalName()) && !ignoreReplyTo) {
                extractReplyToEPRInformation(soapHeaderBlock, namespace, messageContext);
            } else if (WSA_FAULT_TO.equals(soapHeaderBlock.getLocalName()) && !ignoreFaultTo) {
                extractFaultToEPRInformation(soapHeaderBlock, namespace, messageContext);
            } else if (WSA_MESSAGE_ID.equals(soapHeaderBlock.getLocalName()) && !ignoreMessageID) {
                messageContextOptions.setMessageId(soapHeaderBlock.getText());
                soapHeaderBlock.setProcessed();
            } else if (WSA_ACTION.equals(soapHeaderBlock.getLocalName()) && !ignoreAction) {
                extractActionInformation(soapHeaderBlock, namespace, messageContext);
            } else if (WSA_RELATES_TO.equals(soapHeaderBlock.getLocalName())) {
                extractRelatesToInformation(soapHeaderBlock, namespace, messageContextOptions);
            }
        }

        // Now that all the valid wsa headers have been read, throw an exception if there was an invalid cardinality
        // This means that if for example there are multiple MessageIDs and a FaultTo, the FaultTo will be respected.
        if(!duplicateHeaderNames.isEmpty()){
        	// Simply choose the first problem header we came across as we can only fault for one of them.
            AddressingFaultsHelper.triggerInvalidCardinalityFault(messageContext, (String)duplicateHeaderNames.get(0));
        }
        
        // check for the presence of madatory addressing headers
        checkForMandatoryHeaders(checkedHeaderNames, messageContext);
         
        // provide default values for headers that have not been found.
        setDefaults(checkedHeaderNames, messageContext);
         
        return messageContextOptions;
    }
    
    protected abstract void checkForMandatoryHeaders(ArrayList alreadyFoundAddrHeader, MessageContext messageContext) throws AxisFault;

    protected abstract void setDefaults(ArrayList alreadyFoundAddrHeader, MessageContext messageContext) throws AxisFault;

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

    protected abstract void extractToEprReferenceParameters(EndpointReference toEPR, SOAPHeader header, String namespace);


    private void extractRelatesToInformation(SOAPHeaderBlock soapHeaderBlock, String addressingNamespace, Options messageContextOptions) {
        String address = soapHeaderBlock.getText();
        OMAttribute relationshipType =
                soapHeaderBlock.getAttribute(
                        new QName(AddressingConstants.WSA_RELATES_TO_RELATIONSHIP_TYPE));
        String relationshipTypeDefaultValue =
                Submission.WSA_NAMESPACE.equals(addressingNamespace)
                        ? Submission.WSA_DEFAULT_RELATIONSHIP_TYPE
                        : Final.WSA_DEFAULT_RELATIONSHIP_TYPE;
        RelatesTo relatesTo =
                new RelatesTo(
                        address,
                        relationshipType == null
                                ? relationshipTypeDefaultValue
                                : relationshipType.getAttributeValue());
        messageContextOptions.addRelatesTo(relatesTo);
        soapHeaderBlock.setProcessed();
    }

    private void extractFaultToEPRInformation(SOAPHeaderBlock soapHeaderBlock, String addressingNamespace, MessageContext messageContext) throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();
        EndpointReference epr = messageContextOptions.getFaultTo();
        if (epr == null) {
            epr = new EndpointReference("");
            messageContextOptions.setFaultTo(epr);
        }
        extractEPRInformation(soapHeaderBlock, epr, addressingNamespace, messageContext);
        soapHeaderBlock.setProcessed();
    }

    private void extractReplyToEPRInformation(SOAPHeaderBlock soapHeaderBlock, String addressingNamespace, MessageContext messageContext) throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();
        EndpointReference epr = messageContextOptions.getReplyTo();
        if (epr == null) {
            epr = new EndpointReference("");
            messageContextOptions.setReplyTo(epr);
        }
        extractEPRInformation(soapHeaderBlock, epr, addressingNamespace, messageContext);
        soapHeaderBlock.setProcessed();
    }

    private void extractFromEPRInformation(SOAPHeaderBlock soapHeaderBlock, String addressingNamespace, MessageContext messageContext) throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();
        EndpointReference epr = messageContextOptions.getFrom();
        if (epr == null) {
            epr = new EndpointReference("");  // I don't know the address now. Let me pass the empty string now and fill this
            // once I process the Elements under this.
            messageContextOptions.setFrom(epr);
        }
        extractEPRInformation(soapHeaderBlock, epr, addressingNamespace, messageContext);
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
    
    //We assume that any action that already exists in the message context must be the
    //soapaction. We compare that action to the WS-Addressing action, and if they are
    //different we throw a fault.
    private void extractActionInformation(SOAPHeaderBlock soapHeaderBlock, String addressingNamespace, MessageContext messageContext) throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();
        String soapAction = messageContextOptions.getAction();
        
        if (soapAction != null && !"".equals(soapAction)) {
            if (!soapAction.equals(soapHeaderBlock.getText())) {
                AddressingFaultsHelper.triggerActionMismatchFault(messageContext);
            }
        }
        else {
            messageContextOptions.setAction(soapHeaderBlock.getText());            
        }
        
        soapHeaderBlock.setProcessed();        
    }

    /**
     * Given the soap header block, this should extract the information within EPR.
     *
     * @param headerBlock
     * @param epr
     * @param addressingNamespace
     */
    private void extractEPRInformation(SOAPHeaderBlock headerBlock, EndpointReference epr, String addressingNamespace, MessageContext messageContext) throws AxisFault {
        try {
            EndpointReferenceHelper.fromOM(epr, headerBlock, addressingNamespace);
        }
        catch (AxisFault af) {
            AddressingFaultsHelper.triggerMissingAddressInEPRFault(messageContext, headerBlock.getLocalName());
        }
    }
}
