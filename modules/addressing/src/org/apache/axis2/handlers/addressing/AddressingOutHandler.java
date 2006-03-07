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
import org.apache.axis2.context.ServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.om.OMNode;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPFault;
import org.apache.ws.commons.soap.SOAPHeader;
import org.apache.ws.commons.soap.SOAPHeaderBlock;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class AddressingOutHandler extends AddressingHandler {

    private static final long serialVersionUID = -2623986992336863995L;

    private Log log = LogFactory.getLog(getClass());

    protected OMNamespace addressingNamespaceObject;

    private MessageContext msgCtxt;

    public void invoke(MessageContext msgContext) throws AxisFault {

        // it should be able to disable addressing by some one.
        Boolean
                property = (Boolean) msgContext.getProperty(Constants.Configuration.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        if (property == null && msgContext.getOperationContext() != null) {
            // check in the IN message context, if available
            MessageContext inMsgCtxt = msgContext.getOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inMsgCtxt != null) {
                property = (Boolean) inMsgCtxt.getProperty(Constants.Configuration.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
            }
        }
        if (property != null && property.booleanValue()) {
            log.info("Addressing is disbaled .....");
            return;
        }

        this.msgCtxt = msgContext;

        Object addressingVersionFromCurrentMsgCtxt = msgContext.getProperty(WS_ADDRESSING_VERSION);
        if (addressingVersionFromCurrentMsgCtxt != null) {
            // since we support only two addressing versions I can avoid multiple  ifs here.
            // see that if message context property holds something other than Final.WSA_NAMESPACE
            // we always defaults to Submission.WSA_NAMESPACE. Hope this is fine.
            addressingNamespace = Final.WSA_NAMESPACE.equals(addressingVersionFromCurrentMsgCtxt)
                    ? Final.WSA_NAMESPACE : Submission.WSA_NAMESPACE;
        } else if (msgContext.getOperationContext() != null)
        { // check for a IN message context, else default to WSA Final
            MessageContext inMessageContext = msgContext.getOperationContext()
                    .getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inMessageContext != null) {
                addressingNamespace =
                        (String) inMessageContext.getProperty(
                                WS_ADDRESSING_VERSION);
            }
        }

        if (addressingNamespace == null || "".equals(addressingNamespace)) {
            addressingNamespace = Final.WSA_NAMESPACE;
        }
        addressingNamespaceObject = OMAbstractFactory.getOMFactory().createOMNamespace(
                addressingNamespace, WSA_DEFAULT_PREFIX);
        anonymousURI = addressingNamespace.equals(Final.WSA_NAMESPACE) ? Final.WSA_ANONYMOUS_URL : Submission.WSA_ANONYMOUS_URL;


        Options messageContextOptions = msgContext.getOptions();
        SOAPEnvelope envelope = msgContext.getEnvelope();
        SOAPHeader soapHeader = envelope.getHeader();
        if (soapHeader == null) {
            SOAPFactory soapFac = msgContext.isSOAP11() ? OMAbstractFactory.getSOAP11Factory() : OMAbstractFactory.getSOAP12Factory();
            soapHeader = soapFac.createSOAPHeader(envelope);
        }

        // by this time, we definitely have some addressing information to be sent. This is because,
        // we have tested at the start of this whether messageInformationHeaders are null or not.
        // So rather than declaring addressing namespace in each and every addressing header, lets
        // define that in the Header itself.
        envelope.declareNamespace(addressingNamespaceObject);

        EndpointReference epr;

        // processing WSA To
        processToEPR(messageContextOptions, envelope);

        // processing WSA replyTo
        processReplyTo(envelope, messageContextOptions, msgContext);

        // processing WSA From
        processFromEPR(messageContextOptions, envelope);

        // processing WSA FaultTo
        processFaultToEPR(messageContextOptions, envelope);

        String messageID = messageContextOptions.getMessageId();
        if (messageID != null && !isAddressingHeaderAlreadyAvailable(WSA_MESSAGE_ID, envelope,
                addressingNamespaceObject)) {//optional
            processStringInfo(messageID, WSA_MESSAGE_ID, envelope);
        }

        // processing WSA Action
        processWSAAction(messageContextOptions, envelope);

        // processing WSA RelatesTo
        processRelatesTo(envelope, messageContextOptions);

        // process fault headers, if present
        processFaultsInfoIfPresent(envelope, msgContext);

        // We are done, cleanup the references
        addressingNamespaceObject = null;
        addressingNamespace = null;

    }

    private void processWSAAction(Options messageContextOptions, SOAPEnvelope envelope) {
        if (msgCtxt.isProcessingFault()) {
            processStringInfo(Final.WSA_FAULT_ACTION, WSA_ACTION, envelope);
        }
        String action = messageContextOptions.getAction();
        if (action != null && !isAddressingHeaderAlreadyAvailable(WSA_ACTION, envelope,
                addressingNamespaceObject)) {
            processStringInfo(action, WSA_ACTION, envelope);
        }
    }

    private void processFaultsInfoIfPresent(SOAPEnvelope envelope, MessageContext msgContext) {
        Map faultInfo = (Map) msgContext.getProperty(Constants.FAULT_INFORMATION_FOR_HEADERS);
        if (faultInfo != null) {
            String faultyHeaderQName = (String) faultInfo.get(Final.FAULT_HEADER_PROB_HEADER_QNAME);
            if (faultyHeaderQName != null && !"".equals(faultyHeaderQName)) {
                // add to header
                SOAPHeaderBlock faultDetail = envelope.getHeader().addHeaderBlock(Final.FAULT_HEADER_DETAIL, addressingNamespaceObject);
                OMElement probHeaderQName = OMAbstractFactory.getOMFactory().createOMElement(Final.FAULT_HEADER_PROB_HEADER_QNAME, addressingNamespaceObject, faultDetail);
                probHeaderQName.setText(faultyHeaderQName);

                String messageID = (String) faultInfo.get(AddressingConstants.WSA_RELATES_TO);
                if(messageID != null) {
                    SOAPHeaderBlock relatesTo = envelope.getHeader().addHeaderBlock(AddressingConstants.WSA_RELATES_TO, addressingNamespaceObject);
                    relatesTo.setText(messageID);
                }

                // add to header
                SOAPFault fault = envelope.getBody().getFault();
                if (fault != null && fault.getDetail() != null) {
                    OMElement probHeaderQName2 = OMAbstractFactory.getOMFactory().createOMElement(Final.FAULT_HEADER_PROB_HEADER_QNAME, addressingNamespaceObject, fault.getDetail());
                    probHeaderQName2.setText(faultyHeaderQName);
                }

            }

        }
    }

    private void processRelatesTo(SOAPEnvelope envelope, Options messageContextOptions) {
        if (!isAddressingHeaderAlreadyAvailable(WSA_RELATES_TO, envelope, addressingNamespaceObject))
        {
            RelatesTo relatesTo = messageContextOptions.getRelatesTo();
            OMElement relatesToHeader = null;

            if (relatesTo != null) {
                relatesToHeader =
                        processStringInfo(relatesTo.getValue(),
                                WSA_RELATES_TO,
                                envelope);
            }

            if (relatesToHeader != null)
                if ("".equals(relatesTo.getRelationshipType())) {
                    relatesToHeader.addAttribute(WSA_RELATES_TO_RELATIONSHIP_TYPE,
                            Submission.WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE,
                            addressingNamespaceObject);
                } else {
                    relatesToHeader.addAttribute(WSA_RELATES_TO_RELATIONSHIP_TYPE,
                            relatesTo.getRelationshipType(),
                            addressingNamespaceObject);
                }
        }
    }

    private void processFaultToEPR(Options messageContextOptions, SOAPEnvelope envelope) {
        EndpointReference epr;
        epr = messageContextOptions.getFaultTo();
        if (epr != null) {//optional
            addToSOAPHeader(epr, AddressingConstants.WSA_FAULT_TO, envelope);
        }
    }

    private void processFromEPR(Options messageContextOptions, SOAPEnvelope envelope) {
        EndpointReference epr;
        epr = messageContextOptions.getFrom();
        if (epr != null) {//optional
            addToSOAPHeader(epr, AddressingConstants.WSA_FROM, envelope);
        }
    }

    private void processReplyTo(SOAPEnvelope envelope, Options messageContextOptions, MessageContext msgContext) {
        EndpointReference epr;
        if (!isAddressingHeaderAlreadyAvailable(WSA_REPLY_TO, envelope, addressingNamespaceObject))
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
            addToSOAPHeader(epr, AddressingConstants.WSA_REPLY_TO, envelope);
        }
    }

    private void processToEPR(Options messageContextOptions, SOAPEnvelope envelope) throws AxisFault {
        EndpointReference epr = messageContextOptions.getTo();
        if (epr != null && !isAddressingHeaderAlreadyAvailable(WSA_TO, envelope, addressingNamespaceObject))
        {
            Map referenceParameters = null;
            String address = "";
//            System.out.println("envelope = " + envelope);
//            if (envelope.getBody().hasFault()) {
//                MessageContext inMsgCtxt = msgCtxt.getOperationContext().getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
//                if (inMsgCtxt != null && inMsgCtxt.getFaultTo() != null && inMsgCtxt.getFaultTo().getAddress() != null)
//                {
//                    EndpointReference faultTo = inMsgCtxt.getFaultTo();
//                    address = faultTo.getAddress();
//                    referenceParameters = faultTo.getAllReferenceParameters();
//                }
//            } else {
            address = epr.getAddress();
            referenceParameters = epr.getAllReferenceParameters();
//            }

            if (!"".equals(address) && address != null) {
                SOAPHeaderBlock toHeaderBlock = envelope.getHeader().addHeaderBlock(WSA_TO, addressingNamespaceObject);
                toHeaderBlock.setText(address);
            }
            processReferenceInformation(referenceParameters, envelope.getHeader());
        }
    }


    private OMElement processStringInfo(String value,
                                        String type,
                                        SOAPEnvelope soapEnvelope) {
        if (!"".equals(value) && value != null) {
            SOAPHeaderBlock soapHeaderBlock =
                    soapEnvelope.getHeader().addHeaderBlock(type, addressingNamespaceObject);
            soapHeaderBlock.addChild(
                    OMAbstractFactory.getOMFactory().createText(value));
            return soapHeaderBlock;
        }
        return null;
    }

    protected void addToSOAPHeader(EndpointReference epr,
                                   String type,
                                   SOAPEnvelope envelope) {
        if (epr == null || isAddressingHeaderAlreadyAvailable(type, envelope, addressingNamespaceObject))
        {
            return;
        }

        SOAPHeaderBlock soapHeaderBlock =
                envelope.getHeader().addHeaderBlock(type, addressingNamespaceObject);

        // add epr address
        String address = epr.getAddress();
        if (!"".equals(address) && address != null) {
            OMElement addressElement = OMAbstractFactory.getOMFactory().createOMElement(EPR_ADDRESS, addressingNamespaceObject, soapHeaderBlock);
            addressElement.setText(address);
        }

        // add reference parameters
        Map referenceParameters = epr.getAllReferenceParameters();
        if (referenceParameters != null) {
            OMElement reference =
                    OMAbstractFactory.getOMFactory().createOMElement(
                            EPR_REFERENCE_PARAMETERS,
                            addressingNamespaceObject, soapHeaderBlock);
            processReferenceInformation(referenceParameters, reference);

        }

        // add xs:any
        ArrayList omElements = epr.getOmElements();
        if (omElements != null) {
            for (int i = 0; i < omElements.size(); i++) {
                soapHeaderBlock.addChild((OMElement) omElements.get(i));
            }
        }

        // add metadata
        ArrayList metaDataList = epr.getMetaData();
        if (metaDataList != null) {
            OMElement metadata =
                    OMAbstractFactory.getOMFactory().createOMElement(
                            Final.WSA_METADATA,
                            addressingNamespaceObject, soapHeaderBlock);
            for (int i = 0; i < metaDataList.size(); i++) {
                  metadata.addChild((OMNode) metaDataList.get(i));
            }

        }

    }


    /**
     * This will add reference parameters and/or reference properties in to the message
     *
     * @param referenceInformation
     */
    private void processReferenceInformation(Map referenceInformation, OMElement parent) {

        boolean processingWSAFinal = Final.WSA_NAMESPACE.equals(addressingNamespace);
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


    private boolean isAddressingHeaderAlreadyAvailable(String name, SOAPEnvelope envelope, OMNamespace addressingNamespaceObject) {
        return envelope.getHeader().getFirstChildWithName(new QName(addressingNamespaceObject.getName(), name, addressingNamespaceObject.getPrefix())) != null;
    }
}

