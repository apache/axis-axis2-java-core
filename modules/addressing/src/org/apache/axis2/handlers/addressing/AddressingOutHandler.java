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
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class AddressingOutHandler extends AddressingHandler {

    public void invoke(MessageContext msgContext) throws AxisFault {

        SOAPFactory factory = (SOAPFactory)msgContext.getEnvelope().getOMFactory();
        
        OMNamespace addressingNamespaceObject;
        String namespace = addressingNamespace;

        // it should be able to disable addressing by some one.
        Object property = msgContext.getProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        if (property == null && msgContext.getOperationContext() != null) {
            // check in the IN message context, if available
            MessageContext inMsgCtxt = msgContext.getOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inMsgCtxt != null) {
                property = inMsgCtxt.getProperty(DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
            }
        }
        if (property != null && JavaUtils.isTrueExplicitly(property)) {
            log.debug("Addressing is disabled .....");
            return;
        }


        Object addressingVersionFromCurrentMsgCtxt = msgContext.getProperty(WS_ADDRESSING_VERSION);
        if (addressingVersionFromCurrentMsgCtxt != null) {
            // since we support only two addressing versions I can avoid multiple  ifs here.
            // see that if message context property holds something other than Final.WSA_NAMESPACE
            // we always defaults to Submission.WSA_NAMESPACE. Hope this is fine.
            namespace = Final.WSA_NAMESPACE.equals(addressingVersionFromCurrentMsgCtxt)
                    ? Final.WSA_NAMESPACE : Submission.WSA_NAMESPACE;
        } else if (msgContext.getOperationContext() != null)
        { // check for a IN message context, else default to WSA Final
            MessageContext inMessageContext = msgContext.getOperationContext()
                    .getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inMessageContext != null) {
                namespace =
                        (String) inMessageContext.getProperty(
                                WS_ADDRESSING_VERSION);
            }
        }

        if (namespace == null || "".equals(namespace)) {
            namespace = Final.WSA_NAMESPACE;
        }
        addressingNamespaceObject = factory.createOMNamespace(
                namespace, WSA_DEFAULT_PREFIX);
        anonymousURI = namespace.equals(Final.WSA_NAMESPACE) ? Final.WSA_ANONYMOUS_URL : Submission.WSA_ANONYMOUS_URL;
        relationshipType = namespace.equals(Final.WSA_NAMESPACE) ? Final.WSA_DEFAULT_RELATIONSHIP_TYPE : Submission.WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE;


        Options messageContextOptions = msgContext.getOptions();
        SOAPEnvelope envelope = msgContext.getEnvelope();
        SOAPHeader soapHeader = envelope.getHeader();

        // if there is no soap header in the envelope being processed, add one.
        if (soapHeader == null) {
//            SOAPFactory soapFac = msgContext.isSOAP11() ? OMAbstractFactory.getSOAP11Factory() : OMAbstractFactory.getSOAP12Factory();
            soapHeader = factory.createSOAPHeader(envelope);
        }

        // by this time, we definitely have some addressing information to be sent. This is because,
        // we have tested at the start of this whether messageInformationHeaders are null or not.
        // So rather than declaring addressing namespace in each and every addressing header, lets
        // define that in the Header itself.
        envelope.declareNamespace(addressingNamespaceObject);

        // what if there are addressing headers already in the message. Do you replace that or not?
        // Lets have a parameter to control that. The default behavior is you won't replace addressing
        // headers if there are any (this was the case so far).
        Object replaceHeadersParam = msgContext.getProperty(REPLACE_ADDRESSING_HEADERS);
        boolean replaceHeaders = false;
        if (replaceHeadersParam != null) {
            replaceHeaders = JavaUtils.isTrueExplicitly(replaceHeadersParam);
        }

        // processing WSA To
        processToEPR(messageContextOptions, envelope, addressingNamespaceObject, namespace, replaceHeaders);

        // processing WSA replyTo
        processReplyTo(envelope, messageContextOptions, msgContext, addressingNamespaceObject, namespace, replaceHeaders);

        // processing WSA From
        processFromEPR(messageContextOptions, envelope, addressingNamespaceObject, namespace, replaceHeaders);

        // processing WSA FaultTo
        processFaultToEPR(messageContextOptions, envelope, addressingNamespaceObject, namespace, replaceHeaders);

        String messageID = messageContextOptions.getMessageId();
        if (messageID != null && !isAddressingHeaderAlreadyAvailable(WSA_MESSAGE_ID, envelope,
                addressingNamespaceObject, replaceHeaders)) {//optional
            processStringInfo(messageID, WSA_MESSAGE_ID, envelope, addressingNamespaceObject);
        }

        // processing WSA Action
        processWSAAction(messageContextOptions, envelope, msgContext, addressingNamespaceObject, replaceHeaders);

        // processing WSA RelatesTo
        processRelatesTo(envelope, messageContextOptions, addressingNamespaceObject, replaceHeaders);

        // process fault headers, if present
        processFaultsInfoIfPresent(envelope, msgContext, addressingNamespaceObject, replaceHeaders);

        // We are done, cleanup the references
        addressingNamespaceObject = null;
    }

    private void processWSAAction(Options messageContextOptions, SOAPEnvelope envelope,
                                  MessageContext msgCtxt, OMNamespace addressingNamespaceObject, boolean replaceHeaders) {
        String action = messageContextOptions.getAction();
        if (action != null && !isAddressingHeaderAlreadyAvailable(WSA_ACTION, envelope,
                addressingNamespaceObject, replaceHeaders)) {
            processStringInfo(action, WSA_ACTION, envelope, addressingNamespaceObject);
        }
    }

    private void processFaultsInfoIfPresent(SOAPEnvelope envelope, MessageContext msgContext, OMNamespace addressingNamespaceObject, boolean replaceHeaders) {
        Map faultInfo = (Map) msgContext.getProperty(Constants.FAULT_INFORMATION_FOR_HEADERS);
        if (faultInfo != null) {
            String faultyHeaderQName = (String) faultInfo.get(Final.FAULT_HEADER_PROB_HEADER_QNAME);
            if (faultyHeaderQName != null && !"".equals(faultyHeaderQName)) {
                // add to header
                SOAPHeaderBlock faultDetail = envelope.getHeader().addHeaderBlock(Final.FAULT_HEADER_DETAIL, addressingNamespaceObject);
                OMElement probHeaderQName = envelope.getOMFactory().createOMElement(Final.FAULT_HEADER_PROB_HEADER_QNAME, addressingNamespaceObject, faultDetail);
                probHeaderQName.setText(faultyHeaderQName);

                String messageID = (String) faultInfo.get(AddressingConstants.WSA_RELATES_TO);
                if (messageID != null) {
                    SOAPHeaderBlock relatesTo = envelope.getHeader().addHeaderBlock(AddressingConstants.WSA_RELATES_TO, addressingNamespaceObject);
                    relatesTo.setText(messageID);
                }

                // add to header
                SOAPFault fault = envelope.getBody().getFault();
                if (fault != null && fault.getDetail() != null) {
                    OMElement probHeaderQName2 = envelope.getOMFactory().createOMElement(Final.FAULT_HEADER_PROB_HEADER_QNAME, addressingNamespaceObject, fault.getDetail());
                    probHeaderQName2.setText(faultyHeaderQName);
                }

            }

        }
    }

    private void processRelatesTo(SOAPEnvelope envelope, Options messageContextOptions, OMNamespace addressingNamespaceObject, boolean replaceHeaders) {
        if (!isAddressingHeaderAlreadyAvailable(WSA_RELATES_TO, envelope, addressingNamespaceObject,replaceHeaders))
        {
            RelatesTo[] relatesTo = messageContextOptions.getRelationships();

            if (relatesTo != null) {
                for (int i = 0; i < relatesTo.length; i++) {
                    OMElement relatesToHeader =
                            processStringInfo(relatesTo[i].getValue(),
                                    WSA_RELATES_TO,
                                    envelope, addressingNamespaceObject);

                    String relationshipType = relatesTo[i].getRelationshipType();

                    if (relatesToHeader != null) {
                        if (Final.WSA_DEFAULT_RELATIONSHIP_TYPE.equals(relationshipType) || relationshipType == null || "".equals(relationshipType)) {
                            relatesToHeader.addAttribute(WSA_RELATES_TO_RELATIONSHIP_TYPE,
                                    this.relationshipType,
                                    addressingNamespaceObject);
                        } else {
                            relatesToHeader.addAttribute(WSA_RELATES_TO_RELATIONSHIP_TYPE,
                                    relationshipType,
                                    addressingNamespaceObject);
                        }
                    }
                }
            }
        }
    }

    private void processFaultToEPR(Options messageContextOptions, SOAPEnvelope envelope, OMNamespace addressingNamespaceObject, String namespace, boolean replaceHeaders) {
        EndpointReference epr;
        epr = messageContextOptions.getFaultTo();
        if (epr != null) {//optional
            addToSOAPHeader(epr, AddressingConstants.WSA_FAULT_TO, envelope, addressingNamespaceObject, namespace, replaceHeaders);
        }
    }

    private void processFromEPR(Options messageContextOptions, SOAPEnvelope envelope, OMNamespace addressingNamespaceObject, String namespace, boolean replaceHeaders) {
        EndpointReference epr;
        epr = messageContextOptions.getFrom();
        if (epr != null) {//optional
            addToSOAPHeader(epr, AddressingConstants.WSA_FROM, envelope, addressingNamespaceObject, namespace, replaceHeaders);
        }
    }

    private void processReplyTo(SOAPEnvelope envelope, Options messageContextOptions, MessageContext msgContext, OMNamespace addressingNamespaceObject, String namespace, boolean replaceHeaders) {
        EndpointReference epr;
        if (!isAddressingHeaderAlreadyAvailable(WSA_REPLY_TO, envelope, addressingNamespaceObject, replaceHeaders))
        {
            epr = messageContextOptions.getReplyTo();
            if (epr == null) {//optional
                ServiceContext serviceContext = msgContext.getServiceContext();
                if (serviceContext != null &&
                        serviceContext.getMyEPR() != null) {
                    epr = serviceContext.getMyEPR();
                } else {
                    // setting anonymous URI. Defaulting to Final.
                    epr = new EndpointReference(anonymousURI);
                }
            } else if ("".equals(epr.getAddress())) {
                ServiceContext serviceContext = msgContext.getServiceContext();
                if (serviceContext != null &&
                        serviceContext.getMyEPR() != null) {
                    epr.setAddress(serviceContext.getMyEPR().getAddress());
                } else {
                    // setting anonymous URI. Defaulting to Final.
                    epr.setAddress(anonymousURI);
                }
            }
            addToSOAPHeader(epr, AddressingConstants.WSA_REPLY_TO, envelope, addressingNamespaceObject, namespace, replaceHeaders);
        }
    }

    private void processToEPR(Options messageContextOptions, SOAPEnvelope envelope, OMNamespace addressingNamespaceObject, String namespace, boolean replaceHeaders) {
        EndpointReference epr = messageContextOptions.getTo();
        if (epr != null && !isAddressingHeaderAlreadyAvailable(WSA_TO, envelope, addressingNamespaceObject, replaceHeaders))
        {
            Map referenceParameters = null;
            String address = "";
            address = epr.getAddress();
            referenceParameters = epr.getAllReferenceParameters();

            if (!"".equals(address) && address != null) {
                SOAPHeaderBlock toHeaderBlock = envelope.getHeader().addHeaderBlock(WSA_TO, addressingNamespaceObject);
                toHeaderBlock.setText(address);
            }
            processToEPRReferenceInformation(referenceParameters, envelope.getHeader(),addressingNamespaceObject, namespace);
        }
    }


    private OMElement processStringInfo(String value,
                                        String type,
                                        SOAPEnvelope soapEnvelope, OMNamespace addressingNamespaceObject) {
        if (!"".equals(value) && value != null) {
            SOAPHeaderBlock soapHeaderBlock =
                    soapEnvelope.getHeader().addHeaderBlock(type, addressingNamespaceObject);
            soapHeaderBlock.addChild(
                    soapEnvelope.getOMFactory().createOMText(value));
            return soapHeaderBlock;
        }
        return null;
    }

    protected void addToSOAPHeader(EndpointReference epr,
                                   String type,
                                   SOAPEnvelope envelope, OMNamespace addressingNamespaceObject, String namespace, boolean replaceHeaders) {
        if (epr == null || isAddressingHeaderAlreadyAvailable(type, envelope, addressingNamespaceObject,replaceHeaders))
        {
            return;
        }

        SOAPHeaderBlock soapHeaderBlock =
                envelope.getHeader().addHeaderBlock(type, addressingNamespaceObject);

        // add epr address
        String address = epr.getAddress();
        if (!"".equals(address) && address != null) {
            OMElement addressElement = envelope.getOMFactory().createOMElement(EPR_ADDRESS, addressingNamespaceObject, soapHeaderBlock);
            addressElement.setText(address);
        }

        // add reference parameters
        Map referenceParameters = epr.getAllReferenceParameters();
        if (referenceParameters != null) {
            OMElement reference =
                    envelope.getOMFactory().createOMElement(
                            EPR_REFERENCE_PARAMETERS,
                            addressingNamespaceObject, soapHeaderBlock);
            processReferenceInformation(referenceParameters, reference, namespace);

        }

        // add xs:any
        ArrayList omElements = epr.getExtensibleElements();
        if (omElements != null) {
            for (int i = 0; i < omElements.size(); i++) {
                soapHeaderBlock.addChild((OMElement) omElements.get(i));
            }
        }

        // add metadata
        ArrayList metaDataList = epr.getMetaData();
        if (metaDataList != null) {
            OMElement metadata =
                    envelope.getOMFactory().createOMElement(
                            Final.WSA_METADATA,
                            addressingNamespaceObject, soapHeaderBlock);
            for (int i = 0; i < metaDataList.size(); i++) {
                metadata.addChild((OMNode) metaDataList.get(i));
            }

        }

        if (epr.getAttributes() != null) {
            Iterator attrIter = epr.getAttributes().iterator();
            while (attrIter.hasNext()) {
                OMAttribute omAttributes = (OMAttribute) attrIter.next();
                soapHeaderBlock.addAttribute(omAttributes);
            }
        }


    }


    /**
     * This will add reference parameters and/or reference properties in to the message
     *
     * @param referenceInformation
     */
    private void processReferenceInformation(Map referenceInformation, OMElement parent, String namespace) {

        boolean processingWSAFinal = Final.WSA_NAMESPACE.equals(namespace);
        if (referenceInformation != null && parent != null) {
            Iterator iterator = referenceInformation.keySet().iterator();
            while (iterator.hasNext()) {
                QName key = (QName) iterator.next();
                OMElement omElement = (OMElement) referenceInformation.get(key);
                parent.addChild(omElement);
            }
        }
    }

    /**
     * This will add reference parameters and/or reference properties in to the message
     *
     * @param referenceInformation
     */
    private void processToEPRReferenceInformation(Map referenceInformation, OMElement parent, OMNamespace addressingNamespaceObject, String namespace) {

        boolean processingWSAFinal = Final.WSA_NAMESPACE.equals(namespace);
        if (referenceInformation != null && parent != null) {
            Iterator iterator = referenceInformation.keySet().iterator();
            while (iterator.hasNext()) {
                QName key = (QName) iterator.next();
                OMElement omElement = (OMElement) referenceInformation.get(key);
                parent.addChild(omElement);

                if (processingWSAFinal) {
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
     * @return false - if one can add new headers, true - if one should not touch them.
     */
    private boolean isAddressingHeaderAlreadyAvailable(String name, SOAPEnvelope envelope, OMNamespace addressingNamespaceObject, boolean replaceHeaders) {
        OMElement addressingHeader = envelope.getHeader().getFirstChildWithName(new QName(addressingNamespaceObject.getNamespaceURI(), name, addressingNamespaceObject.getPrefix()));

        if (addressingHeader != null && replaceHeaders) {
            addressingHeader.detach();
            return false;
        }

        return addressingHeader != null;
    }
}

