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
import org.apache.axis2.addressing.AnyContentType;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.MessageInformationHeaders;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

public class AddressingInHandler extends AbstractHandler implements AddressingConstants {
    // this parameter has to be set by the module deployer.
    private boolean isAddressingOptional = true;
    protected String addressingNamespace = null;

    private Log logger = LogFactory.getLog(getClass());

    public void invoke(MessageContext msgContext) throws AxisFault {
        logger.debug("Starting Addressing IN Handler .........");
        SOAPHeader header = msgContext.getEnvelope().getHeader();
        if (header == null) {
            return;
        }

        ArrayList addressingHeaders = null;
        try {
            addressingHeaders = header.getHeaderBlocksWithNSURI(Submission.WSA_NAMESPACE);
            if (addressingHeaders != null) {
                addressingNamespace = Submission.WSA_NAMESPACE;
                extractAddressingSubmissionInformationFromHeaders(
                        header,
                        msgContext.getMessageInformationHeaders(),
                        addressingHeaders);
            } else {
                addressingHeaders = header.getHeaderBlocksWithNSURI(Final.WSA_NAMESPACE);
                if (addressingHeaders != null) {
                    addressingNamespace = Final.WSA_NAMESPACE;
                    extractAddressingFinalInformationFromHeaders(
                            header,
                            msgContext.getMessageInformationHeaders(),
                            addressingHeaders);
                    extractReferenceParameters(header, msgContext.getMessageInformationHeaders());

                } else {
                    // Addressing headers are not present in the SOAP message
                    if (!isAddressingOptional) {
                        throw new AxisFault("Addressing Handlers should present, but doesn't present in the incoming message !!");
                    }
                    logger.debug(
                            "No Addressing Headers present in the IN message. Addressing In Handler does nothing.");
                }
            }
            msgContext.setProperty(WS_ADDRESSING_VERSION, addressingNamespace);

            // extract service group context, if available
            extractServiceGroupContextId(header, msgContext);

        } catch (AddressingException e) {
            logger.info("Exception occurred in Addressing Module");
            throw new AxisFault(e);
        }

    }

    private void extractServiceGroupContextId(SOAPHeader header, MessageContext msgContext) throws AxisFault {
        OMElement serviceGroupId = header.getFirstChildWithName(new QName(Constants.AXIS2_NAMESPACE_URI,
                Constants.SERVICE_GROUP_ID, Constants.AXIS2_NAMESPACE_PREFIX));
        if (serviceGroupId != null) {
            String groupId = serviceGroupId.getText();
            ServiceGroupContext serviceGroupContext = msgContext.getSystemContext().getServiceGroupContext(groupId);
            if (serviceGroupContext == null) {
                throw new AxisFault("Invalid Service Group Id." + groupId);
            }
            msgContext.setServiceGroupContextId(serviceGroupId.getText());
        }
    }

    /**
     * WSA 1.0 specification mandates all the reference parameters to have a attribute as wsa:Type=???parameter???. So
     * here this will check for header blocks with the above attribute and will put them in message information header collection
     *
     * @param header
     * @param messageInformationHeaders
     */
    private void extractReferenceParameters(
            SOAPHeader header,
            MessageInformationHeaders messageInformationHeaders) {
        Iterator headerBlocks = header.getChildren();
        while (headerBlocks.hasNext()) {
            SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) headerBlocks.next();
            if (Final
                    .WSA_TYPE_ATTRIBUTE_VALUE
                    .equals(
                            soapHeaderBlock
                                    .getFirstAttribute(
                                            new QName(
                                                    Final.WSA_NAMESPACE,
                                                    Final.WSA_IS_REFERENCE_PARAMETER_ATTRIBUTE))
                                    .getAttributeValue())) {
                messageInformationHeaders.addReferenceParameter(soapHeaderBlock);
            }
        }
    }

    private void extractAddressingFinalInformationFromHeaders(
            SOAPHeader header,
            MessageInformationHeaders messageInformationHeaders,
            ArrayList addressingHeaders)
            throws AddressingException {
        extractCommonAddressingParameters(
                header,
                messageInformationHeaders,
                addressingHeaders,
                Final.WSA_NAMESPACE);
    }

    private void extractAddressingSubmissionInformationFromHeaders(
            SOAPHeader header,
            MessageInformationHeaders messageInformationHeaders,
            ArrayList addressingHeaders)
            throws AddressingException {
        extractCommonAddressingParameters(
                header,
                messageInformationHeaders,
                addressingHeaders,
                Submission.WSA_NAMESPACE);
    }

    public MessageInformationHeaders extractCommonAddressingParameters(
            SOAPHeader header,
            MessageInformationHeaders messageInformationHeaders,
            ArrayList addressingHeaders,
            String addressingNamespace)
            throws AddressingException {
        if (messageInformationHeaders == null) {
            messageInformationHeaders = new MessageInformationHeaders();
        }

        Iterator addressingHeadersIt = addressingHeaders.iterator();
        while (addressingHeadersIt.hasNext()) {
            SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) addressingHeadersIt.next();
            EndpointReference epr = null;
            if (AddressingConstants.WSA_TO.equals(soapHeaderBlock.getLocalName())) {
                //here the addressing epr overidde what ever already there is 
                epr = new EndpointReference(soapHeaderBlock.getText());
                messageInformationHeaders.setTo(epr);

                // check for reference parameters
                extractToEprReferenceParameters(epr, header);
                soapHeaderBlock.setProcessed();

            } else if (AddressingConstants.WSA_FROM.equals(soapHeaderBlock.getLocalName())) {
                epr = messageInformationHeaders.getFrom();
                if (epr == null) {
                    epr = new EndpointReference("");
                    messageInformationHeaders.setFrom(epr);
                }
                extractEPRInformation(soapHeaderBlock, epr, addressingNamespace);
                soapHeaderBlock.setProcessed();
            } else if (AddressingConstants.WSA_REPLY_TO.equals(soapHeaderBlock.getLocalName())) {
                epr = messageInformationHeaders.getReplyTo();
                if (epr == null) {
                    epr = new EndpointReference("");
                    messageInformationHeaders.setReplyTo(epr);
                }
                extractEPRInformation(soapHeaderBlock, epr, addressingNamespace);
                soapHeaderBlock.setProcessed();
            } else if (AddressingConstants.WSA_FAULT_TO.equals(soapHeaderBlock.getLocalName())) {
                epr = messageInformationHeaders.getFaultTo();
                if (epr == null) {
                    epr = new EndpointReference("");
                    messageInformationHeaders.setFaultTo(epr);
                }
                extractEPRInformation(soapHeaderBlock, epr, addressingNamespace);
                soapHeaderBlock.setProcessed();
            } else if (AddressingConstants.WSA_MESSAGE_ID.equals(soapHeaderBlock.getLocalName())) {
                messageInformationHeaders.setMessageId(soapHeaderBlock.getText());
                soapHeaderBlock.setProcessed();
            } else if (AddressingConstants.WSA_ACTION.equals(soapHeaderBlock.getLocalName())) {
                messageInformationHeaders.setAction(soapHeaderBlock.getText());
                soapHeaderBlock.setProcessed();
            } else if (AddressingConstants.WSA_RELATES_TO.equals(soapHeaderBlock.getLocalName())) {
                String address = soapHeaderBlock.getText();
                OMAttribute relationshipType =
                        soapHeaderBlock.getFirstAttribute(
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
                messageInformationHeaders.setRelatesTo(relatesTo);
                soapHeaderBlock.setProcessed();

            }
        }

        return messageInformationHeaders;
    }

    private void extractToEprReferenceParameters(EndpointReference toEPR, SOAPHeader header) {
        Iterator headerBlocks = header.getChildElements();
        while (headerBlocks.hasNext()) {
            SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) headerBlocks.next();
            OMAttribute isRefParamAttr = soapHeaderBlock.getAttribute(new QName(addressingNamespace, "IsReferenceParameter"));
            if (isRefParamAttr != null && "true".equals(isRefParamAttr.getAttributeValue())) {
                toEPR.addReferenceParameter(soapHeaderBlock.getQName(), soapHeaderBlock.getText());
            }
        }
    }

    private void extractEPRInformation(
            SOAPHeaderBlock headerBlock,
            EndpointReference epr,
            String addressingNamespace) {

        Iterator childElements = headerBlock.getChildElements();
        while (childElements.hasNext()) {
            OMElement eprChildElement = (OMElement) childElements.next();
            if (checkElement(new QName(addressingNamespace, AddressingConstants.EPR_ADDRESS), eprChildElement.getQName())) {
                epr.setAddress(eprChildElement.getText());
            } else if (checkElement(new QName(addressingNamespace, AddressingConstants.EPR_REFERENCE_PARAMETERS), eprChildElement.getQName())) {
                AnyContentType anyContentType = new AnyContentType();
                if (epr.getReferenceParameters() == null) {
                    epr.setReferenceParameters(anyContentType);
                }
                Iterator referenceParameters = eprChildElement.getChildElements();
                while (referenceParameters.hasNext()) {
                    OMElement element = (OMElement) referenceParameters.next();
                    epr.getReferenceParameters().addReferenceValue(element.getQName(), element.getText());
                }
            } else if (checkElement(new QName(addressingNamespace, AddressingConstants.Final.WSA_METADATA), eprChildElement.getQName())) {
                AnyContentType anyContentType = new AnyContentType();
                if (epr.getMetadata() == null) {
                    epr.setMetadata(anyContentType);
                }
                Iterator metadataChildren = eprChildElement.getChildElements();
                while (metadataChildren.hasNext()) {
                    OMElement element = (OMElement) metadataChildren.next();
                    epr.getMetadata().addReferenceValue(element.getQName(), element.getText());
                }
            }
        }
    }

    private boolean checkElement(QName expectedQName, QName actualQName) {
        return (expectedQName.getLocalPart().equals(actualQName.getLocalPart()) && expectedQName.getNamespaceURI().equals(actualQName.getNamespaceURI()));
    }
}
