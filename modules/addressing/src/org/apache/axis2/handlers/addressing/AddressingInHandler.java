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
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.ws.commons.om.OMAttribute;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.soap.SOAPHeader;
import org.apache.ws.commons.soap.SOAPHeaderBlock;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class AddressingInHandler extends AddressingHandler {

    private static final long serialVersionUID = 3907988439637261572L;

    public void invoke(MessageContext msgContext) throws AxisFault {
        SOAPHeader header = msgContext.getEnvelope().getHeader();

        // if there are no headers or addressing version is already determined, pass through
        if (header == null || msgContext.getProperty(WS_ADDRESSING_VERSION) != null) {
            return;
        }

        logger.debug("Starting " + addressingVersion + " IN handler ...");

        ArrayList addressingHeaders;
        try {
            addressingHeaders = header.getHeaderBlocksWithNSURI(addressingNamespace);
            if (addressingHeaders != null && addressingHeaders.size() > 0) {
                msgContext.setProperty(WS_ADDRESSING_VERSION, addressingNamespace);

                logger.debug(addressingVersion + " Headers present in the SOAP message. Starting to process ...");
                extractAddressingInformation(header, msgContext.getOptions(),
                        addressingHeaders, addressingNamespace);
            } else {
                logger.debug("No Headers present corresponding to " + addressingVersion);
            }

            // extract service group context, if available
            extractServiceGroupContextId(header, msgContext);

        } catch (AddressingException e) {
            logger.info("Exception occurred in Addressing Module");
            throw new AxisFault(e);
        }

    }

    protected Options extractAddressingInformation(SOAPHeader header, Options messageContextOptions,
                                                   ArrayList addressingHeaders, String addressingNamespace)
            throws AddressingException {

        Iterator addressingHeadersIt = addressingHeaders.iterator();
        while (addressingHeadersIt.hasNext()) {
            SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) addressingHeadersIt.next();
            EndpointReference epr;
            if (AddressingConstants.WSA_TO.equals(soapHeaderBlock.getLocalName())) {
                extractToEPRInformation(soapHeaderBlock, messageContextOptions, header);
            } else if (AddressingConstants.WSA_FROM.equals(soapHeaderBlock.getLocalName())) {
                extractFromEPRInformation(messageContextOptions, soapHeaderBlock, addressingNamespace);
            } else if (AddressingConstants.WSA_REPLY_TO.equals(soapHeaderBlock.getLocalName())) {
                extractReplyToEPRInformation(messageContextOptions, soapHeaderBlock, addressingNamespace);
            } else if (AddressingConstants.WSA_FAULT_TO.equals(soapHeaderBlock.getLocalName())) {
                extractFaultToEPRInformation(messageContextOptions, soapHeaderBlock, addressingNamespace);
            } else if (AddressingConstants.WSA_MESSAGE_ID.equals(soapHeaderBlock.getLocalName())) {
                messageContextOptions.setMessageId(soapHeaderBlock.getText());
                soapHeaderBlock.setProcessed();
            } else if (AddressingConstants.WSA_ACTION.equals(soapHeaderBlock.getLocalName())) {
                messageContextOptions.setAction(soapHeaderBlock.getText());
                soapHeaderBlock.setProcessed();
            } else if (AddressingConstants.WSA_RELATES_TO.equals(soapHeaderBlock.getLocalName())) {
                extractRelatesToInformation(soapHeaderBlock, addressingNamespace, messageContextOptions);
            }
        }
        return messageContextOptions;
    }

    protected abstract void extractToEprReferenceParameters(EndpointReference toEPR, SOAPHeader header);

    private void extractServiceGroupContextId(SOAPHeader header, MessageContext msgContext) throws AxisFault {
        OMElement serviceGroupId = header.getFirstChildWithName(new QName(Constants.AXIS2_NAMESPACE_URI,
                Constants.SERVICE_GROUP_ID, Constants.AXIS2_NAMESPACE_PREFIX));
        if (serviceGroupId != null) {
            String groupId = serviceGroupId.getText();
            ServiceGroupContext serviceGroupContext = msgContext.getConfigurationContext().
                    getServiceGroupContext(groupId, msgContext);
            if (serviceGroupContext == null) {
                throw new AxisFault("Invalid Service Group Id." + groupId);
            }
            msgContext.setServiceGroupContextId(serviceGroupId.getText());
        }
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
