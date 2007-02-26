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

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingFaultsHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.LoggingControl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AddressingInHandler extends AbstractHandler implements AddressingConstants {

    protected String addressingNamespace = Final.WSA_NAMESPACE;  // defaulting to final version
    protected String addressingVersion = null;
    private static final Log log = LogFactory.getLog(AddressingInHandler.class);


    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        // if another handler has already processed the addressing headers, do not do anything here.
        if (JavaUtils.isTrueExplicitly(msgContext.getProperty(IS_ADDR_INFO_ALREADY_PROCESSED))) {
            if(LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
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
            if(LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
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

        if(LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
            log.debug("Starting " + addressingVersion + " IN handler ...");
        }


        ArrayList addressingHeaders;
        addressingHeaders = header.getHeaderBlocksWithNSURI(namespace);
        if (addressingHeaders != null && addressingHeaders.size() > 0) {
            msgContext.setProperty(WS_ADDRESSING_VERSION, namespace);
            msgContext.setProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.FALSE);

            if(LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(addressingVersion + " Headers present in the SOAP message. Starting to process ...");
            }

            extractAddressingInformation(header, msgContext, addressingHeaders, namespace);
            msgContext.setProperty(IS_ADDR_INFO_ALREADY_PROCESSED, Boolean.TRUE);
        } else {
            msgContext.setProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
            if(LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
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
            if (!SOAP12Constants.SOAP_ROLE_NONE.equals(soapHeaderBlock.getRole())){
                if (WSA_ACTION.equals(soapHeaderBlock.getLocalName())) {
                    ignoreAction = checkDuplicateHeaders(WSA_ACTION, checkedHeaderNames, duplicateHeaderNames);
                } else if (WSA_TO.equals(soapHeaderBlock.getLocalName())) {
                    ignoreTo = checkDuplicateHeaders(WSA_TO, checkedHeaderNames, duplicateHeaderNames);
                } else if (WSA_MESSAGE_ID.equals(soapHeaderBlock.getLocalName())) {
                    ignoreMessageID = checkDuplicateHeaders(WSA_MESSAGE_ID, checkedHeaderNames, duplicateHeaderNames);
                } else if (WSA_REPLY_TO.equals(soapHeaderBlock.getLocalName())) {
                    ignoreReplyTo = checkDuplicateHeaders(WSA_REPLY_TO, checkedHeaderNames, duplicateHeaderNames);
                } else if (WSA_FAULT_TO.equals(soapHeaderBlock.getLocalName())) {
                    ignoreFaultTo = checkDuplicateHeaders(WSA_FAULT_TO, checkedHeaderNames, duplicateHeaderNames);
                } else if (WSA_FROM.equals(soapHeaderBlock.getLocalName())) {
                    ignoreFrom = checkDuplicateHeaders(WSA_FROM, checkedHeaderNames, duplicateHeaderNames);
                }
            }
        }
        
        // Now extract information
        Iterator addressingHeadersIt2 = addressingHeaders.iterator();
        while (addressingHeadersIt2.hasNext()) {
            SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) addressingHeadersIt2.next();
            if (!SOAP12Constants.SOAP_ROLE_NONE.equals(soapHeaderBlock.getRole())){
                if (WSA_ACTION.equals(soapHeaderBlock.getLocalName()) && !ignoreAction) {
                    extractActionInformation(soapHeaderBlock, namespace, messageContext);
                } else if (WSA_TO.equals(soapHeaderBlock.getLocalName()) && !ignoreTo) {
                    extractToEPRInformation(soapHeaderBlock, messageContextOptions, header, namespace);
                } else if (WSA_MESSAGE_ID.equals(soapHeaderBlock.getLocalName()) && !ignoreMessageID) {
                    extractMessageIDInformation(soapHeaderBlock, namespace, messageContext);
                } else if (WSA_REPLY_TO.equals(soapHeaderBlock.getLocalName()) && !ignoreReplyTo) {
                    extractReplyToEPRInformation(soapHeaderBlock, namespace, messageContext);
                } else if (WSA_FAULT_TO.equals(soapHeaderBlock.getLocalName()) && !ignoreFaultTo) {
                    extractFaultToEPRInformation(soapHeaderBlock, namespace, messageContext);
                } else if (WSA_RELATES_TO.equals(soapHeaderBlock.getLocalName())) {
                    extractRelatesToInformation(soapHeaderBlock, namespace, messageContextOptions);
                } else if (WSA_FROM.equals(soapHeaderBlock.getLocalName()) && !ignoreFrom) {
                    extractFromEPRInformation(soapHeaderBlock, namespace, messageContext);
                }
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
        
        if(log.isTraceEnabled()){
            log.trace("checkDuplicateHeaders: addressingHeaderName="+addressingHeaderName+" isDuplicate="+shouldIgnore);
        }
        
    	return shouldIgnore;
    }

    protected abstract void extractToEprReferenceParameters(EndpointReference toEPR, SOAPHeader header, String namespace);


    private void extractRelatesToInformation(SOAPHeaderBlock soapHeaderBlock, String addressingNamespace, Options messageContextOptions) {
        String address = soapHeaderBlock.getText();
        
        // Extract the RelationshipType attribute if it exists
        OMAttribute relationshipType =
                soapHeaderBlock.getAttribute(
                        new QName(AddressingConstants.WSA_RELATES_TO_RELATIONSHIP_TYPE));
        
        String relationshipTypeString = relationshipType == null ? null : relationshipType.getAttributeValue();
        
        if(log.isTraceEnabled()){
            log.trace("extractRelatesToInformation: Extracted Relationship. Value="+address+" RelationshipType="+relationshipTypeString);
        }
        
        RelatesTo relatesTo = new RelatesTo(address, relationshipTypeString);
        
        ArrayList attributes = extractAttributesFromSOAPHeaderBlock(soapHeaderBlock);
        relatesTo.setExtensibilityAttributes(attributes);
        
        messageContextOptions.addRelatesTo(relatesTo);
        
        // Completed processing of this header
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
        if(log.isTraceEnabled()){
            log.trace("extractFaultToEPRInformation: Extracted FaultTo EPR: "+epr);
        }
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
        if(log.isTraceEnabled()){
            log.trace("extractReplyToEPRInformation: Extracted ReplyTo EPR: "+epr);
        }
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
        if(log.isTraceEnabled()){
            log.trace("extractFromEPRInformation: Extracted From EPR: "+epr);
        }
        soapHeaderBlock.setProcessed();
    }

    private void extractToEPRInformation(SOAPHeaderBlock soapHeaderBlock, Options messageContextOptions, SOAPHeader header, String namespace) {

        EndpointReference epr;
        //here the addressing epr overidde what ever already there in the message context
        epr = new EndpointReference(soapHeaderBlock.getText());
        messageContextOptions.setTo(epr);

        // check for address attributes
        Iterator addressAttributes = soapHeaderBlock.getAllAttributes(); 
        if(addressAttributes != null && addressAttributes.hasNext()){
            ArrayList attributes = new ArrayList();
            while(addressAttributes.hasNext()){
                OMAttribute attr = (OMAttribute)addressAttributes.next();
                attributes.add(attr);
            }
            epr.setAddressAttributes(attributes);
        }
        
        // check for reference parameters
        extractToEprReferenceParameters(epr, header, namespace);
        soapHeaderBlock.setProcessed();

        if(log.isTraceEnabled()){
            log.trace("extractToEPRInformation: Extracted To EPR: "+epr);
        }
    }
    
    //We assume that any action that already exists in the message context must be the
    //soapaction. We compare that action to the WS-Addressing action, and if they are
    //different we throw a fault.
    private void extractActionInformation(SOAPHeaderBlock soapHeaderBlock, String addressingNamespace, MessageContext messageContext) throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();
        String soapAction = messageContextOptions.getAction();
        String wsaAction = soapHeaderBlock.getText();
        
        if(log.isTraceEnabled()){
            log.trace("extractActionInformation: soapAction='"+soapAction+"' wsa:Action='"+wsaAction+"'");
        }
        
        // Need to validate that the content of the wsa:Action header is not null or whitespace
        if((wsaAction==null) || "".equals(wsaAction.trim())){
            AddressingFaultsHelper.triggerActionNotSupportedFault(messageContext, wsaAction);
        }
        
        // The isServerSide check is because the underlying Options object is
        // shared between request and response MessageContexts for Sync
        // invocations. If the soapAction is set outbound and a wsa:Action is
        // received on the response they will differ (because there is no
        // SOAPAction header on an HTTP response). In this case we should not
        // check that soapAction==wsa:Action
        if (soapAction != null && !"".equals(soapAction) && messageContext.isServerSide()) {
            if (!soapAction.equals(wsaAction)) {
                AddressingFaultsHelper.triggerActionMismatchFault(messageContext);
            }
        }
        else {
            messageContextOptions.setAction(wsaAction);            
        }
        
        ArrayList attributes = extractAttributesFromSOAPHeaderBlock(soapHeaderBlock);
        if(attributes!=null){
            messageContext.setProperty(AddressingConstants.ACTION_ATTRIBUTES, attributes);
        }
        
        soapHeaderBlock.setProcessed();        
    }

    private void extractMessageIDInformation(SOAPHeaderBlock soapHeaderBlock, String addressingNamespace, MessageContext messageContext) throws AxisFault {
        messageContext.getOptions().setMessageId(soapHeaderBlock.getText());
        
        ArrayList attributes = extractAttributesFromSOAPHeaderBlock(soapHeaderBlock);
        if(attributes!=null){
            messageContext.setProperty(AddressingConstants.MESSAGEID_ATTRIBUTES, attributes);
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
        }catch (AxisFault af) {
            if(log.isTraceEnabled()){
                log.trace("extractEPRInformation: Exception occurred deserialising an EndpointReference.",af);
            }
            AddressingFaultsHelper.triggerMissingAddressInEPRFault(messageContext, headerBlock.getLocalName());
        }
    }
    
    private ArrayList extractAttributesFromSOAPHeaderBlock(SOAPHeaderBlock soapHeaderBlock){
        Iterator actionAttributes = soapHeaderBlock.getAllAttributes(); 
        if(actionAttributes != null && actionAttributes.hasNext()){
            ArrayList attributes = new ArrayList();
            while(actionAttributes.hasNext()){
                OMAttribute attr = (OMAttribute)actionAttributes.next();
                attributes.add(attr);
            }
            return attributes;
        }
        return null;
    }
}
