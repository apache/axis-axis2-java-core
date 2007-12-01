/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.handlers.addressing;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.soap.RolePlayer;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.*;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.LoggingControl;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AddressingInHandler extends AbstractHandler implements AddressingConstants {

    protected String addressingNamespace = Final.WSA_NAMESPACE;  // defaulting to final version
    protected String addressingVersion = null;

    public static final String DISABLE_REF_PARAMETER_EXTRACT = "disableRefParamExtract";

    private static final Log log = LogFactory.getLog(AddressingInHandler.class);

    private boolean disableRefparamExtract = false;

    public void init(HandlerDescription handlerdesc) {
        super.init(handlerdesc);
        disableRefparamExtract = JavaUtils.isTrueExplicitly(
                Utils.getParameterValue(handlerdesc.getParameter(DISABLE_REF_PARAMETER_EXTRACT)));
        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
            log.debug("AddressingInHandler.init disableRefparamExtract=" + disableRefparamExtract);
        }
    }

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
    	//Determine if we want to ignore addressing headers.
    	Parameter disableParam = msgContext.getParameter(DISABLE_ADDRESSING_HANDLERS);
        String value = Utils.getParameterValue(disableParam);
        if (JavaUtils.isTrueExplicitly(value)) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(
                        "The handler has been disabled. No further processing will take place.");
            }
            msgContext.setProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
            return InvocationResponse.CONTINUE;        	
        }

        // if another handler has already processed the addressing headers, do not do anything here.
        if (JavaUtils.isTrueExplicitly(msgContext.getLocalProperty(IS_ADDR_INFO_ALREADY_PROCESSED),
                                       false)) {
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(
                        "Another handler has processed the addressing headers. Nothing to do here.");
            }
            return InvocationResponse.CONTINUE;
        }

        // check whether someone has explicitly set which addressing handler should run.
    	Parameter namespaceParam = msgContext.getParameter(WS_ADDRESSING_VERSION);
        String namespace = Utils.getParameterValue(namespaceParam);
        if (namespace == null) {
        	namespace = (String)msgContext.getProperty(WS_ADDRESSING_VERSION);
        	if (namespace == null) {
        		namespace = addressingNamespace;
        	}
        }
        
        if (!namespace.equals(addressingNamespace)) {
    		if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
    			log.debug("This addressing handler does not match the specified namespace, " +
    					namespace);
    		}
    		
    		return InvocationResponse.CONTINUE;
    	}
        
        SOAPHeader header = msgContext.getEnvelope().getHeader();
        RolePlayer rolePlayer = (RolePlayer) msgContext.getConfigurationContext()
                .getAxisConfiguration().getParameterValue(Constants.SOAP_ROLE_PLAYER_PARAMETER);

        // if there are not headers put a flag to disable addressing temporary
        if (header == null) {
            msgContext.setProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
            return InvocationResponse.CONTINUE;
        }

        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
            log.debug("Starting " + addressingVersion + " IN handler ...");
        }

        Iterator iterator = header.getHeadersToProcess(rolePlayer, namespace);
        if (iterator.hasNext()) {
            msgContext.setProperty(WS_ADDRESSING_VERSION, namespace);
            msgContext.setProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.FALSE);

            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug(addressingVersion +
                          " Headers present in the SOAP message. Starting to process ...");
            }
            if (extractAddressingInformation(header, msgContext, iterator, namespace)) {
                msgContext.setProperty(IS_ADDR_INFO_ALREADY_PROCESSED, Boolean.TRUE);
            }
        } else {
            msgContext.setProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("No Headers present corresponding to " + addressingVersion);
            }
        }

        return InvocationResponse.CONTINUE;
    }

    protected static final int TO_FLAG = 1, FROM_FLAG = 2, REPLYTO_FLAG = 3,
            FAULTO_FLAG = 4, MESSAGEID_FLAG = 6, ACTION_FLAG = 0;

    /**
     * Pull addressing headers out from the SOAP message.
     *
     * @param header the header of the SOAP message
     * @param messageContext the active MessageContext
     * @param headers an Iterator over the addressing headers targeted to me
     * @param namespace the addressing namespace
     * @return true if addressing information was found
     * @throws AxisFault if an error occurs
     */
    protected boolean extractAddressingInformation(SOAPHeader header, MessageContext messageContext,
                                                   Iterator headers, String namespace)
            throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();

        ArrayList duplicateHeaderNames = new ArrayList(1); // Normally will not be used for more than 1 header

        ArrayList relatesToHeaders = null;
        SOAPHeaderBlock actionBlock = null, toBlock = null, messageIDBlock = null, replyToBlock =
                null, faultToBlock = null, fromBlock = null;

        // Per the SOAP Binding spec "headers with an incorrect cardinality MUST NOT be used" So these variables
        // are used to keep track of invalid cardinality headers so they are not deserialised.
        boolean[] ignoreHeaders = new boolean[7];
        boolean[] checkedHeaderNames = new boolean[7];

        // First pass just check for duplicates
        while (headers.hasNext()) {
            SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock)headers.next();
            String localName = soapHeaderBlock.getLocalName();
            if (WSA_ACTION.equals(localName)) {
                actionBlock = soapHeaderBlock;
                checkDuplicateHeaders(WSA_ACTION, ACTION_FLAG,
                                      checkedHeaderNames, ignoreHeaders,
                                      duplicateHeaderNames);
            } else if (WSA_TO.equals(localName)) {
                toBlock = soapHeaderBlock;
                checkDuplicateHeaders(WSA_TO, TO_FLAG, checkedHeaderNames, ignoreHeaders,
                                      duplicateHeaderNames);
            } else if (WSA_MESSAGE_ID.equals(localName)) {
                messageIDBlock = soapHeaderBlock;
                checkDuplicateHeaders(WSA_MESSAGE_ID, MESSAGEID_FLAG,
                                      checkedHeaderNames, ignoreHeaders,
                                      duplicateHeaderNames);
            } else if (WSA_REPLY_TO.equals(localName)) {
                replyToBlock = soapHeaderBlock;
                checkDuplicateHeaders(WSA_REPLY_TO, REPLYTO_FLAG,
                                      checkedHeaderNames, ignoreHeaders,
                                      duplicateHeaderNames);
            } else if (WSA_FAULT_TO.equals(localName)) {
                faultToBlock = soapHeaderBlock;
                checkDuplicateHeaders(WSA_FAULT_TO, FAULTO_FLAG,
                                      checkedHeaderNames, ignoreHeaders,
                                      duplicateHeaderNames);
            } else if (WSA_FROM.equals(localName)) {
                fromBlock = soapHeaderBlock;
                checkDuplicateHeaders(WSA_FROM, FROM_FLAG,
                                      checkedHeaderNames, ignoreHeaders,
                                      duplicateHeaderNames);
            } else if (WSA_RELATES_TO.equals(localName)) {
                if (relatesToHeaders == null) {
                    relatesToHeaders = new ArrayList(1);
                }
                relatesToHeaders.add(soapHeaderBlock);
            }
        }

        if (actionBlock == null && toBlock == null && messageIDBlock == null
            && replyToBlock == null && faultToBlock == null
            && fromBlock == null && relatesToHeaders == null) {
            // All of the headers must have had the non local roles so further
            // processing should be skipped.
            return false;
        }

        if (actionBlock != null && !ignoreHeaders[ACTION_FLAG]) {
            extractActionInformation(actionBlock, messageContext);
        }
        if (toBlock != null && !ignoreHeaders[TO_FLAG]) {
            extractToEPRInformation(toBlock,
                                    messageContextOptions,
                                    header,
                                    namespace);
        }
        if (messageIDBlock != null && !ignoreHeaders[MESSAGEID_FLAG]) {
            extractMessageIDInformation(messageIDBlock, messageContext);
        }
        if (relatesToHeaders != null) {
            for (int i = 0; i < relatesToHeaders.size(); i++) {
                extractRelatesToInformation((SOAPHeaderBlock) relatesToHeaders.get(i),
                                            messageContextOptions);
            }
        }
        if (replyToBlock != null && !ignoreHeaders[REPLYTO_FLAG]) {
            extractReplyToEPRInformation(replyToBlock, namespace, messageContext);
        }
        if (faultToBlock != null && !ignoreHeaders[FAULTO_FLAG]) {
            extractFaultToEPRInformation(faultToBlock, namespace, messageContext);
        }
        if (fromBlock != null && !ignoreHeaders[FROM_FLAG]) {
            extractFromEPRInformation(fromBlock, namespace, messageContext);
        }

        // Now that all the valid wsa headers have been read, throw an exception if there was an invalid cardinality
        // This means that if for example there are multiple MessageIDs and a FaultTo, the FaultTo will be respected.
        if (!duplicateHeaderNames.isEmpty()) {
            // Simply choose the first problem header we came across as we can only fault for one of them.
            AddressingFaultsHelper.triggerInvalidCardinalityFault(messageContext,
                                                                  (String) duplicateHeaderNames
                                                                          .get(0));
        }

        // check for the presence of madatory addressing headers
        checkForMandatoryHeaders(checkedHeaderNames, messageContext);

        // provide default values for headers that have not been found.
        setDefaults(checkedHeaderNames, messageContext);

        return true;
    }

    // Copied from SOAPHeaderImpl.java - some reconciliation probably a good idea....
    protected boolean isInRole(SOAPHeaderBlock soapHeaderBlock,
                               RolePlayer rolePlayer,
                               boolean isSOAP11) {
        String role = soapHeaderBlock.getRole();

        // 1. If role is ultimatedest, go by what the rolePlayer says
        if (role == null || role.equals("") ||
            (!isSOAP11 &&
             role.equals(SOAP12Constants.SOAP_ROLE_ULTIMATE_RECEIVER))) {
            return (rolePlayer == null || rolePlayer.isUltimateDestination());
        }

        // 2. If role is next, always return true
        if (role.equals(soapHeaderBlock.getVersion().getNextRoleURI())) return true;

        // 3. If role is none, always return false
        if (!isSOAP11 && role.equals(SOAP12Constants.SOAP_ROLE_NONE)) {
            return false;
        }

        // 4. Return t/f depending on match
        List roles = (rolePlayer == null) ? null : rolePlayer.getRoles();
        if (roles != null) {
            for (int i = 0; i < roles.size(); i++) {
                String thisRole = (String) roles.get(i);
                if (thisRole.equals(role)) return true;
            }
        }

        return false;
    }

    protected abstract void checkForMandatoryHeaders(boolean[] alreadyFoundAddrHeader,
                                                     MessageContext messageContext)
            throws AxisFault;

    protected abstract void setDefaults(boolean[] alreadyFoundAddrHeader,
                                        MessageContext messageContext) throws AxisFault;


    private void checkDuplicateHeaders(String addressingHeaderName, int headerFlag,
                                       boolean[] checkedHeaderNames, boolean[] ignoreHeaders,
                                       ArrayList duplicateHeaderNames) {//throws AxisFault {
        // If the header name has been seen before then we should return true and add it to the list
        // of duplicate header names. Otherwise it is the first time we've seen the header so add it
        // to the checked liat and return false.
        ignoreHeaders[headerFlag] = checkedHeaderNames[headerFlag];
        if (ignoreHeaders[headerFlag]) {
            duplicateHeaderNames.add(addressingHeaderName);
        } else {
            checkedHeaderNames[headerFlag] = true;
        }

        if (LoggingControl.debugLoggingAllowed && log.isTraceEnabled()) {
            log.trace("checkDuplicateHeaders: addressingHeaderName=" + addressingHeaderName
                      + " isDuplicate=" + ignoreHeaders[headerFlag]);
        }
    }

    protected abstract void extractToEprReferenceParameters(EndpointReference toEPR,
                                                            SOAPHeader header, String namespace);


    private void extractRelatesToInformation(SOAPHeaderBlock soapHeaderBlock,
                                             Options messageContextOptions) {
        String address = soapHeaderBlock.getText();

        // Extract the RelationshipType attribute if it exists
        OMAttribute relationshipType =
                soapHeaderBlock.getAttribute(
                        new QName(AddressingConstants.WSA_RELATES_TO_RELATIONSHIP_TYPE));

        String relationshipTypeString =
                relationshipType == null ? null : relationshipType.getAttributeValue();

        if (log.isTraceEnabled()) {
            log.trace("extractRelatesToInformation: Extracted Relationship. Value=" + address +
                      " RelationshipType=" + relationshipTypeString);
        }

        RelatesTo relatesTo = new RelatesTo(address, relationshipTypeString);

        ArrayList attributes = extractAttributesFromSOAPHeaderBlock(soapHeaderBlock);
        relatesTo.setExtensibilityAttributes(attributes);

        messageContextOptions.addRelatesTo(relatesTo);

        // Completed processing of this header
        soapHeaderBlock.setProcessed();
    }

    private void extractFaultToEPRInformation(SOAPHeaderBlock soapHeaderBlock,
                                              String addressingNamespace,
                                              MessageContext messageContext) throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();
        EndpointReference epr = messageContextOptions.getFaultTo();
        if (epr == null) {
            epr = new EndpointReference("");
            messageContextOptions.setFaultTo(epr);
        }
        extractEPRInformation(soapHeaderBlock, epr, addressingNamespace, messageContext);
        if (log.isTraceEnabled()) {
            log.trace("extractFaultToEPRInformation: Extracted FaultTo EPR: " + epr);
        }
        soapHeaderBlock.setProcessed();
    }

    private void extractReplyToEPRInformation(SOAPHeaderBlock soapHeaderBlock,
                                              String addressingNamespace,
                                              MessageContext messageContext) throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();
        EndpointReference epr = messageContextOptions.getReplyTo();
        if (epr == null) {
            epr = new EndpointReference("");
            messageContextOptions.setReplyTo(epr);
        }
        extractEPRInformation(soapHeaderBlock, epr, addressingNamespace, messageContext);
        if (log.isTraceEnabled()) {
            log.trace("extractReplyToEPRInformation: Extracted ReplyTo EPR: " + epr);
        }
        soapHeaderBlock.setProcessed();
    }

    private void extractFromEPRInformation(SOAPHeaderBlock soapHeaderBlock,
                                           String addressingNamespace,
                                           MessageContext messageContext) throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();
        EndpointReference epr = messageContextOptions.getFrom();
        if (epr == null) {
            epr = new EndpointReference(
                    "");  // I don't know the address now. Let me pass the empty string now and fill this
            // once I process the Elements under this.
            messageContextOptions.setFrom(epr);
        }
        extractEPRInformation(soapHeaderBlock, epr, addressingNamespace, messageContext);
        if (log.isTraceEnabled()) {
            log.trace("extractFromEPRInformation: Extracted From EPR: " + epr);
        }
        soapHeaderBlock.setProcessed();
    }

    private void extractToEPRInformation(SOAPHeaderBlock soapHeaderBlock,
                                         Options messageContextOptions, SOAPHeader header,
                                         String namespace) {

        EndpointReference epr;
        //here the addressing epr overidde what ever already there in the message context
        epr = new EndpointReference(soapHeaderBlock.getText());
        messageContextOptions.setTo(epr);

        // check for address attributes
        Iterator addressAttributes = soapHeaderBlock.getAllAttributes();
        if (addressAttributes != null && addressAttributes.hasNext()) {
            ArrayList attributes = new ArrayList();
            while (addressAttributes.hasNext()) {
                OMAttribute attr = (OMAttribute) addressAttributes.next();
                attributes.add(attr);
            }
            epr.setAddressAttributes(attributes);
        }

        // check for reference parameters
        if (!disableRefparamExtract) {
            extractToEprReferenceParameters(epr, header, namespace);
        }
        soapHeaderBlock.setProcessed();

        if (log.isTraceEnabled()) {
            log.trace("extractToEPRInformation: Extracted To EPR: " + epr);
        }
    }

    //We assume that any action that already exists in the message context must be the
    //soapaction. We compare that action to the WS-Addressing action, and if they are
    //different we throw a fault.
    private void extractActionInformation(SOAPHeaderBlock soapHeaderBlock,
                                          MessageContext messageContext)
            throws AxisFault {
        Options messageContextOptions = messageContext.getOptions();
        String soapAction = messageContextOptions.getAction();
        String wsaAction = soapHeaderBlock.getText();

        if (log.isTraceEnabled()) {
            log.trace("extractActionInformation: soapAction='" + soapAction + "' wsa:Action='" +
                      wsaAction + "'");
        }

        // Need to validate that the content of the wsa:Action header is not null or whitespace
        if ((wsaAction == null) || "".equals(wsaAction.trim())) {
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
                AddressingFaultsHelper.triggerActionMismatchFault(messageContext, soapAction, wsaAction);
            }
        } else {
            messageContextOptions.setAction(wsaAction);
        }

        ArrayList attributes = extractAttributesFromSOAPHeaderBlock(soapHeaderBlock);
        if (attributes != null) {
            messageContext.setProperty(AddressingConstants.ACTION_ATTRIBUTES, attributes);
        }

        soapHeaderBlock.setProcessed();
    }

    private void extractMessageIDInformation(SOAPHeaderBlock soapHeaderBlock,
                                             MessageContext messageContext) throws AxisFault {
        messageContext.getOptions().setMessageId(soapHeaderBlock.getText());

        ArrayList attributes = extractAttributesFromSOAPHeaderBlock(soapHeaderBlock);
        if (attributes != null) {
            messageContext.setProperty(AddressingConstants.MESSAGEID_ATTRIBUTES, attributes);
        }

        soapHeaderBlock.setProcessed();
    }

    /**
     * Given the soap header block, this should extract the information within EPR.
     *
     * @param headerBlock         a SOAP header which is of type EndpointReference
     * @param epr                 the EndpointReference to fill in with the extracted data
     * @param addressingNamespace the WSA namespace URI
     * @param messageContext      the active MessageContext
     * @throws AxisFault if there is a problem
     */
    private void extractEPRInformation(SOAPHeaderBlock headerBlock, EndpointReference epr,
                                       String addressingNamespace, MessageContext messageContext)
            throws AxisFault {
        try {
            EndpointReferenceHelper.fromOM(epr, headerBlock, addressingNamespace);
        } catch (AxisFault af) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "extractEPRInformation: Exception occurred deserialising an EndpointReference.",
                        af);
            }
            AddressingFaultsHelper
                    .triggerMissingAddressInEPRFault(messageContext, headerBlock.getLocalName());
        }
    }

    private ArrayList extractAttributesFromSOAPHeaderBlock(SOAPHeaderBlock soapHeaderBlock) {
        Iterator actionAttributes = soapHeaderBlock.getAllAttributes();
        if (actionAttributes != null && actionAttributes.hasNext()) {
            ArrayList attributes = new ArrayList();
            while (actionAttributes.hasNext()) {
                OMAttribute attr = (OMAttribute) actionAttributes.next();
                attributes.add(attr);
            }
            return attributes;
        }
        return null;
    }
}
