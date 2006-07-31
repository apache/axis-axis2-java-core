package org.apache.axis2.handlers.addressing;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.Iterator;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
 */

public class AddressingSubmissionInHandler extends AddressingInHandler {

    private static final Log log = LogFactory.getLog(AddressingSubmissionInHandler.class);
    private static final long serialVersionUID = 365417374773955107L;

    public AddressingSubmissionInHandler() {
        addressingNamespace = Submission.WSA_NAMESPACE;
        addressingVersion = "WS-Addressing Submission";
    }


    protected void extractToEprReferenceParameters(EndpointReference toEPR, SOAPHeader header, String namespace) {
        // there is no exact way to identify ref parameters for Submission version. So let's have a handler
        // at the end of the flow, which puts all the handlers (which are of course mustUnderstand=false)
        // as reference parameters

        // TODO : Chinthaka
    }

    protected void extractEPRInformation(SOAPHeaderBlock headerBlock, EndpointReference epr, String addressingNamespace) {

        Iterator childElements = headerBlock.getChildElements();
        while (childElements.hasNext()) {
            OMElement eprChildElement = (OMElement) childElements.next();
            if (checkElement(new QName(addressingNamespace, AddressingConstants.EPR_ADDRESS),
                    eprChildElement.getQName())) {
                epr.setAddress(eprChildElement.getText());
            } else
            if (checkElement(new QName(addressingNamespace, AddressingConstants.EPR_REFERENCE_PARAMETERS)
                    , eprChildElement.getQName())) {

                Iterator referenceParameters = eprChildElement.getChildElements();
                while (referenceParameters.hasNext()) {
                    OMElement element = (OMElement) referenceParameters.next();
                    epr.addReferenceParameter(element);
                }
            } else
            if (checkElement(new QName(addressingNamespace, AddressingConstants.Submission.EPR_REFERENCE_PROPERTIES)
                    , eprChildElement.getQName())) {

                // since we have the model for WS-Final, we don't have a place to keep this reference properties.
                // The only compatible place is reference properties

                Iterator referenceParameters = eprChildElement.getChildElements();
                while (referenceParameters.hasNext()) {
                    OMElement element = (OMElement) referenceParameters.next();
                    epr.addReferenceParameter(element);
                }
            }else {
                epr.addExtensibleElement(eprChildElement);
            }
        }
        
        Iterator allAttributes = headerBlock.getAllAttributes();
        while (allAttributes.hasNext()) {
            OMAttribute attribute = (OMAttribute) allAttributes.next();
            epr.addAttribute(attribute);
        }
    }
    
    protected void checkForMandatoryHeaders(ArrayList alreadyFoundAddrHeader, MessageContext messageContext) throws AxisFault {
        if (!alreadyFoundAddrHeader.contains(WSA_TO)) {
            throwFault(messageContext, WSA_TO, Final.FAULT_ADDRESSING_HEADER_REQUIRED, null);
        }
        
        if (!alreadyFoundAddrHeader.contains(WSA_ACTION)) {
            throwFault(messageContext, WSA_ACTION, Final.FAULT_ADDRESSING_HEADER_REQUIRED, null);
        }
        
        if (alreadyFoundAddrHeader.contains(WSA_REPLY_TO) ||
            alreadyFoundAddrHeader.contains(WSA_FAULT_TO)) {
            
            if (!alreadyFoundAddrHeader.contains(WSA_MESSAGE_ID)) {
                throwFault(messageContext, WSA_MESSAGE_ID, Final.FAULT_ADDRESSING_HEADER_REQUIRED, null);
            }
        }
    }
    
    protected void setDefaults(ArrayList alreadyFoundAddrHeader, MessageContext messageContext) {
        //The none URI is not defined in the 2004/08 spec, but it is used here anyway
        //as a flag to indicate the correct semantics to apply, i.e. in the 2004/08 spec
        //the absence of a ReplyTo header indicates that a response is NOT required.
        if (!alreadyFoundAddrHeader.contains(WSA_REPLY_TO)) {
            Options messageContextOptions = messageContext.getOptions();
            EndpointReference epr = messageContextOptions.getReplyTo();
            
            if (epr == null) {
                epr = new EndpointReference("");
                messageContextOptions.setReplyTo(epr);
            }
            
            if (log.isTraceEnabled())
                log.trace("setDefaults: Setting WS-Addressing default value for the ReplyTo property.");
            
            epr.setAddress(Final.WSA_NONE_URI);
        }        
    }
}
