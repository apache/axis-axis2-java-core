package org.apache.axis2.handlers.addressing;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.AddressingHelper.FinalFaults;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
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

public class AddressingFinalInHandler extends AddressingInHandler {

    private static final Log log = LogFactory.getLog(AddressingFinalInHandler.class);
    private static final long serialVersionUID = -4020680449342946484L;

    public AddressingFinalInHandler() {
        addressingNamespace = Final.WSA_NAMESPACE;
        addressingVersion = "WS-Addressing Final";
    }


    protected void extractToEprReferenceParameters(EndpointReference toEPR, SOAPHeader header, String namespace) {
        Iterator headerBlocks = header.getChildElements();
        while (headerBlocks.hasNext()) {
            SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) headerBlocks.next();
            OMAttribute isRefParamAttr = soapHeaderBlock.getAttribute(new QName(namespace, "IsReferenceParameter"));
            if (isRefParamAttr != null && "true".equals(isRefParamAttr.getAttributeValue())) {
                toEPR.addReferenceParameter(soapHeaderBlock.getQName(), soapHeaderBlock.getText());
            }
        }
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
            if (checkElement(new QName(addressingNamespace, AddressingConstants.Final.WSA_METADATA), eprChildElement.getQName()))
            {
                Iterator referenceParameters = eprChildElement.getChildElements();
                while (referenceParameters.hasNext()) {
                    OMElement element = (OMElement) referenceParameters.next();
                    epr.addMetaData(element);
                }
            } else {
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
        if (!alreadyFoundAddrHeader.contains(WSA_ACTION)) {
            FinalFaults.triggerMessageAddressingRequiredFault(messageContext, WSA_ACTION);
        } 
    }
    
    protected void setDefaults(ArrayList alreadyFoundAddrHeader, MessageContext messageContext) {
        //According to the WS-Addressing spec, we should default the wsa:To header to the
        //anonymous URL. Doing that, however, might prevent a different value from being
        //used instead, such as the transport URL.
        
        if (!alreadyFoundAddrHeader.contains(WSA_REPLY_TO)) {
            Options messageContextOptions = messageContext.getOptions();
            EndpointReference epr = messageContextOptions.getReplyTo();
            
            if (epr == null) {
                epr = new EndpointReference("");
                messageContextOptions.setReplyTo(epr);
            }
            
            if (log.isTraceEnabled())
                log.trace("setDefaults: Setting WS-Addressing default value for the ReplyTo property.");
            
            epr.setAddress(Final.WSA_ANONYMOUS_URL);
        }        
    }
}
