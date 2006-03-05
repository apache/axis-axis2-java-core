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
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.i18n.Messages;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMAttribute;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPFaultCode;
import org.apache.ws.commons.soap.SOAPFaultReason;
import org.apache.ws.commons.soap.SOAPFaultSubCode;
import org.apache.ws.commons.soap.SOAPFaultText;
import org.apache.ws.commons.soap.SOAPFaultValue;
import org.apache.ws.commons.soap.SOAPHeader;
import org.apache.ws.commons.soap.SOAPHeaderBlock;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class AddressingInHandler extends AddressingHandler implements AddressingConstants {

    private static final long serialVersionUID = 3907988439637261572L;
    private OMNamespace addressingNSObject;

    public void invoke(MessageContext msgContext) throws AxisFault {
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
        addressingHeaders = header.getHeaderBlocksWithNSURI(addressingNamespace);
        if (addressingHeaders != null && addressingHeaders.size() > 0) {
            msgContext.setProperty(WS_ADDRESSING_VERSION, addressingNamespace);
            msgContext.setProperty(Constants.Configuration.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.FALSE);
            addressingNSObject = ((OMElement) addressingHeaders.get(0)).findNamespace(addressingNamespace, "");
            logger.debug(addressingVersion + " Headers present in the SOAP message. Starting to process ...");
            extractAddressingInformation(header, msgContext, addressingHeaders, addressingNamespace);
        } else {
            msgContext.setProperty(Constants.Configuration.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
            logger.debug("No Headers present corresponding to " + addressingVersion);
        }


    }

    protected Options extractAddressingInformation(SOAPHeader header, MessageContext messageContext,
                                                   ArrayList addressingHeaders, String addressingNamespace) throws AxisFault {

        Options messageContextOptions = messageContext.getOptions();
        Map alreadyFoundAddrHeader = new HashMap(7); // there are seven frequently used WS-A headers

        Iterator addressingHeadersIt = addressingHeaders.iterator();
        while (addressingHeadersIt.hasNext()) {
            SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) addressingHeadersIt.next();
            EndpointReference epr;
            if (WSA_TO.equals(soapHeaderBlock.getLocalName())) {
                if (!hasDuplicateHeaders(WSA_TO, messageContext, alreadyFoundAddrHeader)) {
                    extractToEPRInformation(soapHeaderBlock, messageContextOptions, header);
                }
            } else if (WSA_FROM.equals(soapHeaderBlock.getLocalName())) {
                if (!hasDuplicateHeaders(WSA_FROM, messageContext, alreadyFoundAddrHeader)) {
                    extractFromEPRInformation(messageContextOptions, soapHeaderBlock, addressingNamespace);
                }
            } else if (WSA_REPLY_TO.equals(soapHeaderBlock.getLocalName())) {
                if (!hasDuplicateHeaders(WSA_REPLY_TO, messageContext, alreadyFoundAddrHeader)) {
                    extractReplyToEPRInformation(messageContextOptions, soapHeaderBlock, addressingNamespace);
                }
            } else if (WSA_FAULT_TO.equals(soapHeaderBlock.getLocalName())) {
                if (!hasDuplicateHeaders(WSA_FAULT_TO, messageContext, alreadyFoundAddrHeader)) {
                    extractFaultToEPRInformation(messageContextOptions, soapHeaderBlock, addressingNamespace);
                }
            } else if (WSA_MESSAGE_ID.equals(soapHeaderBlock.getLocalName())) {
                if (!hasDuplicateHeaders(WSA_MESSAGE_ID, messageContext, alreadyFoundAddrHeader)) {
                    messageContextOptions.setMessageId(soapHeaderBlock.getText());
                    soapHeaderBlock.setProcessed();
                }
            } else if (WSA_ACTION.equals(soapHeaderBlock.getLocalName())) {
                if (!hasDuplicateHeaders(WSA_ACTION, messageContext, alreadyFoundAddrHeader)) {
                    messageContextOptions.setAction(soapHeaderBlock.getText());
                    soapHeaderBlock.setProcessed();
                }
            } else if (WSA_RELATES_TO.equals(soapHeaderBlock.getLocalName())) {
                if (!hasDuplicateHeaders(WSA_RELATES_TO, messageContext, alreadyFoundAddrHeader)) {
                    extractRelatesToInformation(soapHeaderBlock, addressingNamespace, messageContextOptions);
                }
            }
        }

        // check for the presense of madatory addressing headers
        checkForMandatoryHeaders(alreadyFoundAddrHeader, messageContext);

        return messageContextOptions;
    }

    private void checkForMandatoryHeaders(Map alreadyFoundAddrHeader, MessageContext messageContext) throws AxisFault {
        if (alreadyFoundAddrHeader.get(WSA_ACTION) == null) {
            makeFault(messageContext, WSA_ACTION, Final.FAULT_ADDRESSING_HEADER_REQUIRED, null);
        } else if (alreadyFoundAddrHeader.get(WSA_TO) == null) {
            makeFault(messageContext, WSA_TO, Final.FAULT_ADDRESSING_HEADER_REQUIRED, null);
        }
    }

    private boolean hasDuplicateHeaders(String addressingHeaderName, MessageContext messageContext, Map alreadyFoundAddressingHeaders) throws AxisFault {
        if (alreadyFoundAddressingHeaders.get(addressingHeaderName) != null) {
            return makeFault(messageContext, addressingHeaderName, Final.FAULT_INVALID_HEADER, Final.FAULT_INVALID_CARDINALITY);
        } else {
            alreadyFoundAddressingHeaders.put(addressingHeaderName, addressingHeaderName);
        }
        return false;
    }

    private boolean makeFault(MessageContext messageContext, String addressingHeaderName, String faultCode, String faultSubCode) throws AxisFault {
        Map faultInformation = (Map) messageContext.getProperty(Constants.FAULT_INFORMATION_FOR_HEADERS);
        if (faultInformation == null) {
            faultInformation = new HashMap();
            messageContext.setProperty(Constants.FAULT_INFORMATION_FOR_HEADERS, faultInformation);
        }

        faultInformation.put(Final.FAULT_HEADER_PROB_HEADER_QNAME, WSA_DEFAULT_PREFIX + ":" + addressingHeaderName);
        faultInformation.put(Final.WSA_FAULT_ACTION, Final.WSA_FAULT_ACTION);
        if (!messageContext.isSOAP11()) {
            setFaultCode(messageContext, faultCode, faultSubCode);
        }
        throw new AxisFault("A header representing a Message Addressing Property is not valid and the message cannot be processed", WSA_DEFAULT_PREFIX + ":" + faultCode);
    }

    private void setFaultCode(MessageContext messageContext, String faultCode, String faultSubCode) {
        SOAPFactory soapFac = OMAbstractFactory.getSOAP12Factory();
        SOAPFaultCode soapFaultCode = soapFac.createSOAPFaultCode();
        SOAPFaultValue soapFaultValue = soapFac.createSOAPFaultValue(soapFaultCode);
        soapFaultValue.setText(SOAP12Constants.SOAP_DEFAULT_NAMESPACE_PREFIX + ":" + SOAP12Constants.FAULT_CODE_SENDER);
        SOAPFaultSubCode soapFaultSubCode = soapFac.createSOAPFaultSubCode(soapFaultCode);
        SOAPFaultValue soapFaultSubcodeValue = soapFac.createSOAPFaultValue(soapFaultSubCode);
        soapFaultSubcodeValue.setText(WSA_DEFAULT_PREFIX + ":" + faultCode);
        if(faultSubCode != null) {
            SOAPFaultSubCode soapFaultSubCode2 = soapFac.createSOAPFaultSubCode(soapFaultSubCode);
            SOAPFaultValue soapFaultSubcodeValue2 = soapFac.createSOAPFaultValue(soapFaultSubCode2);
            soapFaultSubcodeValue2.setText(WSA_DEFAULT_PREFIX + ":" + faultSubCode);
        }
        messageContext.setProperty(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME, soapFaultCode);
    }

    protected abstract void extractToEprReferenceParameters(EndpointReference toEPR, SOAPHeader header);

    private void handleNoServiceGroupContextIDCase(MessageContext msgContext) {
        SOAPFactory soapFac = msgContext.isSOAP11() ? OMAbstractFactory.getSOAP11Factory() : OMAbstractFactory.getSOAP12Factory();
        SOAPFaultReason soapFaultReason = soapFac.createSOAPFaultReason(null);
        SOAPFaultText soapFaultText = soapFac.createSOAPFaultText(soapFaultReason);
        soapFaultText.setLang("en");
        soapFaultText.setText(Messages.getMessage("serviceGroupIDNotFound"));

        msgContext.setProperty(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME, soapFaultReason);
    }


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
        messageContextOptions.setRelatesTo(relatesTo);
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

    private void extractToEPRInformation(SOAPHeaderBlock soapHeaderBlock, Options messageContextOptions, SOAPHeader header) {

        EndpointReference epr;
        //here the addressing epr overidde what ever already there in the message context
        epr = new EndpointReference(soapHeaderBlock.getText());
        messageContextOptions.setTo(epr);

        // check for reference parameters
        extractToEprReferenceParameters(epr, header);
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
