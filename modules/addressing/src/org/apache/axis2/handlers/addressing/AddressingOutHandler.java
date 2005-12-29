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
import org.apache.axis2.addressing.ServiceName;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;

public class AddressingOutHandler extends AddressingHandler {


    public void invoke(MessageContext msgContext) throws AxisFault {
        OMNamespace addressingNamespaceObject = null;
        String addressingNamespace = null;

        Object addressingVersionFromCurrentMsgCtxt = msgContext.getProperty(WS_ADDRESSING_VERSION);
        if (addressingVersionFromCurrentMsgCtxt != null) {
            // since we support only two addressing versions I can avoid multiple  ifs here.
            // see that if message context property holds something other than Final.WSA_NAMESPACE
            // we always defaults to Submission.WSA_NAMESPACE. Hope this is fine.
            addressingNamespace = Final.WSA_NAMESPACE.equals(addressingVersionFromCurrentMsgCtxt)
                    ? Final.WSA_NAMESPACE : Submission.WSA_NAMESPACE;
        } else if (msgContext.getOperationContext() != null) { // check for a IN message context, else default to WSA Submission
            MessageContext inMessageContext = msgContext.getOperationContext()
                    .getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inMessageContext == null) {
                addressingNamespace = Submission.WSA_NAMESPACE; // setting Submission version as the default addressing namespace
            } else {
                addressingNamespace =
                        (String) inMessageContext.getProperty(
                                WS_ADDRESSING_VERSION);
                if (addressingNamespace == null) {
                    addressingNamespace = Submission.WSA_NAMESPACE; // Addressing version has not been set in the IN path
                }
            }
        }

        if (addressingNamespace == null || "".equals(addressingNamespace)) {
            addressingNamespace = Submission.WSA_NAMESPACE;
        }
        addressingNamespaceObject =
                OMAbstractFactory.getOMFactory().createOMNamespace(
                        addressingNamespace, WSA_DEFAULT_PREFIX);


        Options messageContextOptions = msgContext.getOptions();
        SOAPEnvelope envelope = msgContext.getEnvelope();
        SOAPHeader soapHeader = envelope.getHeader();

        // by this time, we definitely have some addressing information to be sent. This is because,
        // we have tested at the start of this whether messageInformationHeaders are null or not.
        // So rather than declaring addressing namespace in each and every addressing header, lets
        // define that in the Header itself.
        envelope.declareNamespace(addressingNamespaceObject);

        // processing WSA To
        EndpointReference epr = messageContextOptions.getTo();
        if (epr != null && !isAddressingHeaderAlreadyAvailable(WSA_TO, envelope, addressingNamespaceObject)) {

            String address = epr.getAddress();
            if (!"".equals(address) && address != null) {
                SOAPHeaderBlock toHeaderBlock = envelope.addHeaderBlock(WSA_TO, addressingNamespaceObject);
                toHeaderBlock.setText(address);
            }

            processReferenceInformation(epr.getAllReferenceParameters(), soapHeader, addressingNamespaceObject);
            processReferenceInformation(epr.getAllReferenceProperties(), soapHeader, addressingNamespaceObject);

            addToHeader(epr, soapHeader, addressingNamespaceObject, addressingNamespace);
        }

        // processing WSA Action
        String action = messageContextOptions.getAction();
        if (action != null && !isAddressingHeaderAlreadyAvailable(WSA_ACTION, envelope, addressingNamespaceObject)) {
            processStringInfo(action, WSA_ACTION, envelope, addressingNamespaceObject);
        }

        // processing WSA replyTo
        if (!isAddressingHeaderAlreadyAvailable(WSA_REPLY_TO, envelope, addressingNamespaceObject)) {
            epr = messageContextOptions.getReplyTo();
            if (epr == null) {//optional
                // setting anonymous URI. Defaulting to Final.
                String anonymousURI = Final.WSA_ANONYMOUS_URL;
                if (Submission.WSA_NAMESPACE.equals(addressingNamespace)) {
                    anonymousURI = Submission.WSA_ANONYMOUS_URL;
                }
                epr = new EndpointReference(anonymousURI);
            }
            // add the service group id as a reference parameter
            String serviceGroupContextId = msgContext.getServiceGroupContextId();
            if (serviceGroupContextId != null && !"".equals(serviceGroupContextId)) {
                epr.addReferenceParameter(new QName(Constants.AXIS2_NAMESPACE_URI,
                        Constants.SERVICE_GROUP_ID, Constants.AXIS2_NAMESPACE_PREFIX), serviceGroupContextId);
            }
            addToSOAPHeader(epr, AddressingConstants.WSA_REPLY_TO, envelope, addressingNamespaceObject, addressingNamespace);
        }

        epr = messageContextOptions.getFrom();
        if (epr != null) {//optional
            addToSOAPHeader(epr, AddressingConstants.WSA_FROM, envelope, addressingNamespaceObject, addressingNamespace);
        }

        epr = messageContextOptions.getFaultTo();
        if (epr != null) {//optional
            addToSOAPHeader(epr, AddressingConstants.WSA_FAULT_TO, envelope, addressingNamespaceObject, addressingNamespace);
        }

        String messageID = messageContextOptions.getMessageId();
        if (messageID != null && !isAddressingHeaderAlreadyAvailable(WSA_MESSAGE_ID, envelope, addressingNamespaceObject)) {//optional
            processStringInfo(messageID, WSA_MESSAGE_ID, envelope, addressingNamespaceObject);
        }

        if (!isAddressingHeaderAlreadyAvailable(WSA_RELATES_TO, envelope, addressingNamespaceObject)) {
            RelatesTo relatesTo = messageContextOptions.getRelatesTo();
            OMElement relatesToHeader = null;

            if (relatesTo != null) {
                relatesToHeader =
                        processStringInfo(relatesTo.getValue(),
                                WSA_RELATES_TO,
                                envelope, addressingNamespaceObject);
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
        
        // We are done, cleanup the references
        addressingNamespaceObject = null;
        addressingNamespace = null;
    }


    private OMElement processStringInfo(String value,
                                        String type,
                                        SOAPEnvelope soapEnvelope, OMNamespace addressingNamespaceObject) {
        if (!"".equals(value) && value != null) {
            SOAPHeaderBlock soapHeaderBlock =
                    soapEnvelope.addHeaderBlock(type, addressingNamespaceObject);
            soapHeaderBlock.addChild(
                    OMAbstractFactory.getOMFactory().createText(value));
            return soapHeaderBlock;
        }
        return null;
    }

    protected void addToSOAPHeader(EndpointReference epr,
                                   String type,
                                   SOAPEnvelope envelope, OMNamespace addressingNamespaceObject, String addressingNamespace) {
        if (epr == null || isAddressingHeaderAlreadyAvailable(type, envelope, addressingNamespaceObject)) {
            return;
        }

        SOAPHeaderBlock soapHeaderBlock =
                envelope.addHeaderBlock(type, addressingNamespaceObject);

        String address = epr.getAddress();
        if (!"".equals(address) && address != null) {
            OMElement addressElement =
                    OMAbstractFactory.getOMFactory().createOMElement(
                            EPR_ADDRESS,
                            addressingNamespaceObject);
            soapHeaderBlock.addChild(addressElement);
            addressElement.setText(address);
        }

        addToHeader(epr, soapHeaderBlock, addressingNamespaceObject, addressingNamespace);


        Map referenceParameters = epr.getAllReferenceParameters();
        if (referenceParameters != null) {
            OMElement reference =
                    OMAbstractFactory.getOMFactory().createOMElement(
                            EPR_REFERENCE_PARAMETERS,
                            addressingNamespaceObject);
            soapHeaderBlock.addChild(reference);
            processReferenceInformation(referenceParameters, reference, addressingNamespaceObject);

        }

        if (Submission.WSA_NAMESPACE.equals(addressingNamespace)) {
            Map referenceProperties = epr.getAllReferenceProperties();
            if (referenceProperties != null) {
                OMElement reference =
                        OMAbstractFactory.getOMFactory().createOMElement(
                                Submission.EPR_REFERENCE_PROPERTIES,
                                addressingNamespaceObject);
                envelope.getHeader().addChild(reference);
                processReferenceInformation(referenceParameters, reference, addressingNamespaceObject);
            }

        }

    }

    private void addToHeader(EndpointReference epr, OMElement parentElement, OMNamespace addressingNamespaceObject, String addressingNamespace) {

        if (addressingNamespace.equals(Submission.WSA_NAMESPACE)) {
            QName portType = epr.getPortType();
            if (portType != null) {
                OMElement interfaceName =
                        OMAbstractFactory.getOMFactory().createOMElement(Submission.EPR_PORT_TYPE, addressingNamespaceObject);
                interfaceName.addChild(
                        OMAbstractFactory.getOMFactory().createText(
                                portType.getPrefix() + ":" +
                                        portType.getLocalPart()));
                parentElement.addChild(interfaceName);
            }

            ServiceName serviceName = epr.getServiceName();
            if (serviceName != null) {
                OMElement serviceNameElement =
                        OMAbstractFactory.getOMFactory().createOMElement(
                                EPR_SERVICE_NAME,
                                addressingNamespaceObject);
                serviceNameElement.addAttribute(Submission.EPR_SERVICE_NAME_PORT_NAME, serviceName.getPortName(),
                        addressingNamespaceObject);
                serviceNameElement.addChild(
                        OMAbstractFactory.getOMFactory().createText(
                                serviceName.getName().getPrefix()
                                        + ":"
                                        + serviceName.getName().getLocalPart()));
                parentElement.addChild(serviceNameElement);
            }
        }


    }


    /**
     * This will add reference parameters and/or reference properties in to the message
     *
     * @param referenceInformation
     */
    private void processReferenceInformation(Map referenceInformation, OMElement parent, OMNamespace addressingNamespaceObject) {
        if (referenceInformation != null && parent != null) {
            Iterator iterator = referenceInformation.keySet().iterator();
            while (iterator.hasNext()) {
                QName key = (QName) iterator.next();
                OMElement omElement = (OMElement) referenceInformation.get(key);

                if (Final.WSA_NAMESPACE.equals(addressingNamespace)) {
                    omElement.addAttribute(Final.WSA_IS_REFERENCE_PARAMETER_ATTRIBUTE, Final.WSA_TYPE_ATTRIBUTE_VALUE,
                            addressingNamespaceObject);

                }
                parent.addChild(omElement);
            }
        }
    }

    private boolean isAddressingHeaderAlreadyAvailable(String name, SOAPEnvelope envelope, OMNamespace addressingNamespaceObject) {
        return envelope.getHeader().getFirstChildWithName(new QName(addressingNamespaceObject.getName(), name, addressingNamespaceObject.getPrefix())) != null;
    }
}

