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
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AttributeHelper;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingFaultsHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.addressing.i18n.AddressingMessages;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AddressingOutHandler extends AbstractHandler implements AddressingConstants {

    private static final Log log = LogFactory.getLog(AddressingOutHandler.class);
    
    /**
     * This variable should only be updated inside the {@link #init(HandlerDescription)} method.
     */
    private boolean includeOptionalHeaders = false;
    
    /**
     * Initialize the addressing out handler.
     */
    public void init(HandlerDescription arg0) {
        super.init(arg0);

        //Determine whether to include optional addressing headers in the output message.
        //The default is not to include any headers that can be safely omitted.
        Parameter param = arg0.getParameter(INCLUDE_OPTIONAL_HEADERS);
        String value = Utils.getParameterValue(param);
        includeOptionalHeaders = JavaUtils.isTrueExplicitly(value);
    }

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        // it should be able to disable addressing by some one.
        Object property = msgContext.getProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        if (JavaUtils.isTrueExplicitly(property)) {
            log.debug(msgContext.getLogIDString()+" Addressing is disabled .....");
            return InvocationResponse.CONTINUE;
        }

        // Determine the addressin namespace in effect.
        Object addressingVersionFromCurrentMsgCtxt = msgContext.getProperty(WS_ADDRESSING_VERSION);
        boolean isFinalAddressingNamespace = true;
        String namespace = Final.WSA_NAMESPACE;
        
        if (Submission.WSA_NAMESPACE.equals(addressingVersionFromCurrentMsgCtxt)) {
            isFinalAddressingNamespace = false;
            namespace = Submission.WSA_NAMESPACE;
        }
        
        // Determine whether to include optional addressing headers in the output.
        boolean includeOptionalHeaders = this.includeOptionalHeaders;
        Object includeHeadersPropery = msgContext.getProperty(INCLUDE_OPTIONAL_HEADERS);
        if (includeHeadersPropery != null) {
            includeOptionalHeaders = JavaUtils.isTrueExplicitly(includeHeadersPropery);
        }
        
        // Determine if a MustUnderstand attribute will be added to all headers in the
        // addressing namespace.
        Object mustUnderstandProperty = msgContext.getProperty(AddressingConstants.ADD_MUST_UNDERSTAND_TO_ADDRESSING_HEADERS);
        boolean addMustUnderstandAttribute = JavaUtils.isTrueExplicitly(mustUnderstandProperty);

        // what if there are addressing headers already in the message. Do you replace that or not?
        // Lets have a parameter to control that. The default behavior is you won't replace addressing
        // headers if there are any (this was the case so far).
        Object replaceHeadersProperty = msgContext.getProperty(REPLACE_ADDRESSING_HEADERS);
        boolean replaceHeaders = JavaUtils.isTrueExplicitly(replaceHeadersProperty);

        SOAPFactory factory = (SOAPFactory) msgContext.getEnvelope().getOMFactory();
        OMNamespace addressingNamespaceObject = factory.createOMNamespace(namespace, WSA_DEFAULT_PREFIX);

        Options messageContextOptions = msgContext.getOptions();
        SOAPEnvelope envelope = msgContext.getEnvelope();
        SOAPHeader soapHeader = envelope.getHeader();

        // if there is no soap header in the envelope being processed, add one.
        if (soapHeader == null) {
            soapHeader = factory.createSOAPHeader(envelope);
        }

        // by this time, we definitely have some addressing information to be sent. This is because,
        // we have tested at the start of this whether messageInformationHeaders are null or not.
        // So rather than declaring addressing namespace in each and every addressing header, lets
        // define that in the Header itself.
        envelope.declareNamespace(addressingNamespaceObject);

        // processing WSA To
        processToEPR(messageContextOptions, envelope, addressingNamespaceObject, replaceHeaders, isFinalAddressingNamespace, includeOptionalHeaders);

        // processing WSA replyTo
        processReplyTo(envelope, messageContextOptions, msgContext, addressingNamespaceObject, replaceHeaders, isFinalAddressingNamespace, includeOptionalHeaders);
        
        // processing WSA From
        processFromEPR(messageContextOptions, envelope, addressingNamespaceObject, replaceHeaders, isFinalAddressingNamespace, includeOptionalHeaders);

        // processing WSA FaultTo
        processFaultToEPR(messageContextOptions, envelope, addressingNamespaceObject, replaceHeaders, isFinalAddressingNamespace, includeOptionalHeaders);

        // processing WSA MessageID
        processMessageID(messageContextOptions, envelope, msgContext, addressingNamespaceObject, replaceHeaders, isFinalAddressingNamespace);

        // processing WSA Action
        processWSAAction(messageContextOptions, envelope, msgContext, addressingNamespaceObject, replaceHeaders, isFinalAddressingNamespace);

        // processing WSA RelatesTo
        processRelatesTo(envelope, messageContextOptions, addressingNamespaceObject, replaceHeaders, isFinalAddressingNamespace, includeOptionalHeaders);

        // process fault headers, if present
        processFaultsInfoIfPresent(envelope, msgContext, addressingNamespaceObject, replaceHeaders, isFinalAddressingNamespace);
        
        // process mustUnderstand attribute, if required.
        processMustUnderstandProperty(envelope, msgContext, addressingNamespaceObject, addMustUnderstandAttribute);
        
        return InvocationResponse.CONTINUE;
    }

    private void processMessageID(Options messageContextOptions, SOAPEnvelope envelope, MessageContext msgContext, OMNamespace addressingNamespaceObject, boolean replaceHeaders, boolean isFinalAddressingNamespace) {
        String messageID = messageContextOptions.getMessageId();
        if (messageID != null && !isAddressingHeaderAlreadyAvailable(WSA_MESSAGE_ID, envelope,
                addressingNamespaceObject, replaceHeaders, false)) {//optional
            OMElement oe = processStringInfo(messageID, WSA_MESSAGE_ID, envelope, addressingNamespaceObject);
            ArrayList attributes = (ArrayList)messageContextOptions.getProperty(AddressingConstants.MESSAGEID_ATTRIBUTES);
            if(attributes!= null && !attributes.isEmpty()){
                Iterator attrIterator = attributes.iterator();
                while(attrIterator.hasNext()){
                    AttributeHelper.importOMAttribute((OMAttribute)attrIterator.next(), oe);
                }
            }
        }
    }

    private void processWSAAction(Options messageContextOptions, SOAPEnvelope envelope,
                                  MessageContext msgCtxt, OMNamespace addressingNamespaceObject, boolean replaceHeaders, boolean isFinalAddressingNamespace) throws AxisFault {
        String action = messageContextOptions.getAction();
        
        if(log.isTraceEnabled()){
            log.trace(msgCtxt.getLogIDString()+" processWSAAction: action from messageContext: "+action);
        }
        if(action == null || "".equals(action)){
            if(msgCtxt.getAxisOperation()!=null){
                action = msgCtxt.getAxisOperation().getOutputAction();
                if(log.isTraceEnabled()){
                    log.trace(msgCtxt.getLogIDString()+" processWSAAction: action from AxisOperation: "+action);
                }
            }
        }
        
        // Use the correct fault action for the selected namespace
        if (Final.WSA_FAULT_ACTION.equals(action) || Submission.WSA_FAULT_ACTION.equals(action)) {
            action = isFinalAddressingNamespace ? Final.WSA_FAULT_ACTION : Submission.WSA_FAULT_ACTION;
            messageContextOptions.setAction(action);
        }
        else if (!isFinalAddressingNamespace && Final.WSA_SOAP_FAULT_ACTION.equals(action)) {
            action = Submission.WSA_FAULT_ACTION;
            messageContextOptions.setAction(action);
        }

        // If we need to add a wsa:Action header
        if(!isAddressingHeaderAlreadyAvailable(WSA_ACTION, envelope,
                addressingNamespaceObject, replaceHeaders, false)){
            if(log.isTraceEnabled()){
                log.trace(msgCtxt.getLogIDString()+" processWSAAction: No existing wsa:Action header found");
            }
            // If we don't have an action to add,
            if(action == null || "".equals(action)){
                if(log.isTraceEnabled()){
                    log.trace(msgCtxt.getLogIDString()+" processWSAAction: No action to add to header");
                }
                // Fault unless validation has been explictily turned off
                if(!Utils.isExplicitlyTrue(msgCtxt, AddressingConstants.DISABLE_OUTBOUND_ADDRESSING_VALIDATION)){
                    throw new AxisFault(AddressingMessages.getMessage("outboundNoAction"));
                }
            }else{
                if(log.isTraceEnabled()){
                    log.trace(msgCtxt.getLogIDString()+" processWSAAction: Adding action to header: "+action);
                }
                // Otherwise just add the header
                OMElement oe = processStringInfo(action, WSA_ACTION, envelope, addressingNamespaceObject);
                ArrayList attributes = (ArrayList)messageContextOptions.getProperty(AddressingConstants.ACTION_ATTRIBUTES);
                if(attributes!= null && !attributes.isEmpty()){
                    Iterator attrIterator = attributes.iterator();
                    while(attrIterator.hasNext()){
                        AttributeHelper.importOMAttribute((OMAttribute)attrIterator.next(), oe);
                    }
                }
            }
        }
    }

    private void processFaultsInfoIfPresent(SOAPEnvelope envelope, MessageContext msgContext, OMNamespace addressingNamespaceObject, boolean replaceHeaders, boolean isFinalAddressingNamespace) {
        OMElement detailElement = AddressingFaultsHelper.getDetailElementForAddressingFault(msgContext, addressingNamespaceObject);
        if(detailElement != null){
            //The difference between SOAP 1.1 and SOAP 1.2 fault messages is explained in the WS-Addressing Specs.
            if(isFinalAddressingNamespace && msgContext.isSOAP11()){
                // Add detail as a wsa:FaultDetail header
                if (!isAddressingHeaderAlreadyAvailable(Final.FAULT_HEADER_DETAIL, envelope, addressingNamespaceObject, replaceHeaders, false)) {
                    SOAPHeaderBlock faultDetail = envelope.getHeader().addHeaderBlock(Final.FAULT_HEADER_DETAIL, addressingNamespaceObject);
                    faultDetail.addChild(ElementHelper.importOMElement(detailElement, envelope.getOMFactory()));
                }
            }
            else if (!msgContext.isSOAP11()) {
                // Add detail to the Fault in the SOAP Body
                SOAPFault fault = envelope.getBody().getFault();
                if (fault != null && fault.getDetail() != null) {
                    fault.getDetail().addDetailEntry(ElementHelper.importOMElement(detailElement, envelope.getOMFactory()));
                }
            }
        }
    }

    private void processRelatesTo(SOAPEnvelope envelope, Options messageContextOptions, OMNamespace addressingNamespaceObject, boolean replaceHeaders, boolean isFinalAddressingNamespace, boolean includeOptionalHeaders) {
        if (!isAddressingHeaderAlreadyAvailable(WSA_RELATES_TO, envelope, addressingNamespaceObject, replaceHeaders, true))
        {
            RelatesTo[] relatesTo = messageContextOptions.getRelationships();

            if (relatesTo != null) {
                for (int i = 0, length = relatesTo.length; i < length; i++) {
                    OMElement relatesToHeader = processStringInfo(relatesTo[i].getValue(),
                                    WSA_RELATES_TO, envelope, addressingNamespaceObject);
                    String relationshipType = relatesTo[i].getRelationshipType();

                    if (relatesToHeader != null) {
                        if(relatesTo[i].getExtensibilityAttributes() != null){
                            Iterator attributes = relatesTo[i].getExtensibilityAttributes().iterator();
                            while(attributes.hasNext()){
                                OMAttribute oma = (OMAttribute)attributes.next();
                                AttributeHelper.importOMAttribute(oma, relatesToHeader);
                            }
                        }
                        
                        if (Final.WSA_DEFAULT_RELATIONSHIP_TYPE.equals(relationshipType) ||
                            Submission.WSA_DEFAULT_RELATIONSHIP_TYPE.equals(relationshipType)) {
                            if (includeOptionalHeaders) {
                                relationshipType = isFinalAddressingNamespace ?
                                        Final.WSA_DEFAULT_RELATIONSHIP_TYPE : Submission.WSA_DEFAULT_RELATIONSHIP_TYPE;
                                relatesTo[i].setRelationshipType(relationshipType);
                            }
                            else {
                                continue; //Omit the relationship type
                            }
                        }
                        
                        relatesToHeader.addAttribute(WSA_RELATES_TO_RELATIONSHIP_TYPE,
                                relationshipType,
                                null);
                    }
                }
            }
        }
    }

    private void processFaultToEPR(Options messageContextOptions, SOAPEnvelope envelope, OMNamespace addressingNamespaceObject, boolean replaceHeaders, boolean isFinalAddressingNamespace, boolean includeOptionalHeaders) throws AxisFault {
        EndpointReference epr = messageContextOptions.getFaultTo();
        String headerName = AddressingConstants.WSA_FAULT_TO;
        
        //Omit the header if the epr is null.
        if (epr != null && !isAddressingHeaderAlreadyAvailable(headerName, envelope, addressingNamespaceObject, replaceHeaders, false)) {
            addToSOAPHeader(epr, headerName, envelope, addressingNamespaceObject, replaceHeaders, isFinalAddressingNamespace, includeOptionalHeaders);
        }
    }

    private void processFromEPR(Options messageContextOptions, SOAPEnvelope envelope, OMNamespace addressingNamespaceObject, boolean replaceHeaders, boolean isFinalAddressingNamespace, boolean includeOptionalHeaders) throws AxisFault {
        EndpointReference epr = messageContextOptions.getFrom();
        String headerName = AddressingConstants.WSA_FROM;
        
        //Omit the header if the epr is null.
        if (epr != null && !isAddressingHeaderAlreadyAvailable(headerName, envelope, addressingNamespaceObject, replaceHeaders, false)) {
            addToSOAPHeader(epr, headerName, envelope, addressingNamespaceObject, replaceHeaders, isFinalAddressingNamespace, includeOptionalHeaders);
        }
    }

    private void processReplyTo(SOAPEnvelope envelope, Options messageContextOptions, MessageContext msgContext, OMNamespace addressingNamespaceObject, boolean replaceHeaders, boolean isFinalAddressingNamespace, boolean includeOptionalHeaders) throws AxisFault {
        EndpointReference epr = messageContextOptions.getReplyTo();
        String headerName = AddressingConstants.WSA_REPLY_TO;
        
        //Don't check epr for null here as addToSOAPHeader() will provide an appropriate default.
        //This default is especially useful for client side outbound processing.
        if (!isAddressingHeaderAlreadyAvailable(headerName, envelope, addressingNamespaceObject, replaceHeaders, false)) {
            addToSOAPHeader(epr, headerName, envelope, addressingNamespaceObject, replaceHeaders, isFinalAddressingNamespace, includeOptionalHeaders);            
        }
    }

    private void processToEPR(Options messageContextOptions, SOAPEnvelope envelope, OMNamespace addressingNamespaceObject, boolean replaceHeaders, boolean isFinalAddressingNamespace, boolean includeOptionalHeaders) {
        EndpointReference epr = messageContextOptions.getTo();
        if (epr != null && !isAddressingHeaderAlreadyAvailable(WSA_TO, envelope, addressingNamespaceObject, replaceHeaders, false))
        {
            Map referenceParameters = epr.getAllReferenceParameters();
            String address = epr.getAddress();

            if (!"".equals(address) && address != null) {
                if (!includeOptionalHeaders && isFinalAddressingNamespace &&
                    (Final.WSA_ANONYMOUS_URL.equals(address) ||      //Don't use epr.hasAnonymousAddress() here as it may
                    Submission.WSA_ANONYMOUS_URL.equals(address))) { //recognize none WS-Addressing anonymous values.
                    return; //Omit the header.
                }
                        
                SOAPHeaderBlock toHeaderBlock = envelope.getHeader().addHeaderBlock(WSA_TO, addressingNamespaceObject);
                toHeaderBlock.setText(address);
                if(epr.getAddressAttributes() != null){
                    Iterator addressAttributes = epr.getAddressAttributes().iterator();
                    while(addressAttributes.hasNext()){
                        OMAttribute attr = (OMAttribute)addressAttributes.next();
                        AttributeHelper.importOMAttribute(attr, toHeaderBlock);
                    }
                }
            }
            processToEPRReferenceInformation(referenceParameters, envelope.getHeader(), addressingNamespaceObject, isFinalAddressingNamespace);
        }
    }


    private OMElement processStringInfo(String value,
                                        String headerName,
                                        SOAPEnvelope soapEnvelope, OMNamespace addressingNamespaceObject) {
        if (!"".equals(value) && value != null) {
            SOAPHeaderBlock soapHeaderBlock =
                    soapEnvelope.getHeader().addHeaderBlock(headerName, addressingNamespaceObject);
            soapHeaderBlock.addChild(
                    soapEnvelope.getOMFactory().createOMText(value));
            return soapHeaderBlock;
        }
        return null;
    }

    private void addToSOAPHeader(EndpointReference epr,
                                 String headerName,
                                 SOAPEnvelope envelope, OMNamespace addressingNamespaceObject, boolean replaceHeaders, boolean isFinalAddressingNamespace, boolean includeOptionalHeaders) throws AxisFault {
        String namespace = addressingNamespaceObject.getNamespaceURI();
        String prefix = addressingNamespaceObject.getPrefix();
        String anonymous = isFinalAddressingNamespace ?
                Final.WSA_ANONYMOUS_URL : Submission.WSA_ANONYMOUS_URL;

        if(log.isTraceEnabled()){
        	log.trace("addToSOAPHeader: epr="+epr+" headerName="+headerName);
        }
        
        if (epr == null) {
            if (!includeOptionalHeaders && isFinalAddressingNamespace &&
                AddressingConstants.WSA_REPLY_TO.equals(headerName)) {
                return; //Omit the header.
            }
            else {
                epr = new EndpointReference(anonymous);
            }
        }
        else if (!isFinalAddressingNamespace && epr.hasNoneAddress()) {
            return; //Omit the header.
        }
        else if (Final.WSA_ANONYMOUS_URL.equals(epr.getAddress()) ||      //Don't use epr.hasAnonymousAddress() here as it may
                 Submission.WSA_ANONYMOUS_URL.equals(epr.getAddress())) { //recognize none WS-Addressing anonymous values.
            
            if (!includeOptionalHeaders && isFinalAddressingNamespace &&
                AddressingConstants.WSA_REPLY_TO.equals(headerName)) {
                return; //Omit the header.
            }
            else {
                epr.setAddress(anonymous);
            }
        }

        OMElement soapHeaderBlock = EndpointReferenceHelper.toOM(envelope.getOMFactory(), 
                                        epr, 
                                        new QName(namespace, headerName, prefix), namespace);
        envelope.getHeader().addChild(soapHeaderBlock);
    }

    /**
     * This will add reference parameters and/or reference properties in to the message
     *
     * @param referenceInformation
     */
    private void processToEPRReferenceInformation(Map referenceInformation, OMElement parent, OMNamespace addressingNamespaceObject, boolean isFinalAddressingNamespace) {
        if (referenceInformation != null && parent != null) {
            Iterator iterator = referenceInformation.keySet().iterator();
            while (iterator.hasNext()) {
                QName key = (QName) iterator.next();
                OMElement omElement = (OMElement) referenceInformation.get(key);
                parent.addChild(ElementHelper.importOMElement(omElement, parent.getOMFactory()));

                if (isFinalAddressingNamespace) {
                    omElement.addAttribute(Final.WSA_IS_REFERENCE_PARAMETER_ATTRIBUTE, Final.WSA_TYPE_ATTRIBUTE_VALUE,
                            addressingNamespaceObject);
                }
            }
        }
    }

    /**
     * This will check for the existence of message information headers already in the message. If there are already headers,
     * then replacing them or not depends on the replaceHeaders property.
     *
     * @param name - Name of the message information header
     * @param envelope
     * @param addressingNamespaceObject - namespace object of addressing representing the addressing version being used
     * @param replaceHeaders - determines whether we replace the existing headers or not, if they present
     * @param multipleHeaders - determines whether to search for multiple headers, or not.
     * @return false - if one can add new headers (always the case if multipleHeaders is true),
     * true - if new headers can't be added.
     */
    private boolean isAddressingHeaderAlreadyAvailable(String name, SOAPEnvelope envelope, OMNamespace addressingNamespaceObject, boolean replaceHeaders, boolean multipleHeaders) {
        QName qname = new QName(addressingNamespaceObject.getNamespaceURI(), name, addressingNamespaceObject.getPrefix());
        boolean status = false;
        
        if (multipleHeaders) {
            if (replaceHeaders) {
                Iterator iterator = envelope.getHeader().getChildrenWithName(qname);

                while (iterator.hasNext()) {
                    OMElement addressingHeader = (OMElement) iterator.next();
                    addressingHeader.detach();
                }
            }
        }
        else {
            OMElement addressingHeader = envelope.getHeader().getFirstChildWithName(qname);
    
            if (addressingHeader != null && replaceHeaders) {
            	if(log.isTraceEnabled()){
            		log.trace("isAddressingHeaderAlreadyAvailable: Removing existing header:"+addressingHeader.getLocalName());
            	}
                addressingHeader.detach();
            }
            else {
                status = addressingHeader != null;
            }
        }

        return status;
    }
    
    /**
     * Sets a mustUnderstand attribute on all headers that are found with the appropriate
     * addressing namespace.
     * 
     * @param envelope
     * @param msgContext
     * @param addressingNamespaceObject
     */
    private void processMustUnderstandProperty(SOAPEnvelope envelope, MessageContext msgContext, OMNamespace addressingNamespaceObject, boolean addMustUnderstandAttribute) {
        if (addMustUnderstandAttribute) {
            List headers = envelope.getHeader().getHeaderBlocksWithNSURI(addressingNamespaceObject.getNamespaceURI());

            for (int i = 0, size = headers.size(); i < size; i++) {
            	SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) headers.get(i);
                soapHeaderBlock.setMustUnderstand(true);  
                if(log.isTraceEnabled()){
            		log.trace("processMustUnderstandProperty: Setting mustUnderstand=true on: "+soapHeaderBlock.getLocalName());
            	}
            }
        }
    }
}

