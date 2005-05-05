package org.apache.axis.handlers.addressing;

import java.util.Iterator;
import javax.xml.namespace.QName;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.AnyContentType;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.ServiceName;
import org.apache.axis.addressing.miheaders.RelatesTo;
import org.apache.axis.addressing.MessageInformationHeadersCollection;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.SOAPHeader;
import org.apache.axis.om.SOAPHeaderBlock;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 */
public class AddressingOutHandler
        extends AbstractHandler
        implements AddressingConstants {

    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */

    private boolean isAddressingEnabled = true;

    // IN message, if any, has messageId and replyTo and faultTo addresses that needs to be used
    // in the OUT message. User may sometimes override these values, at his discretion .The following
    // boolean variable will create room for that.
    private boolean overrideINMessageInformation = false;

    OMNamespace addressingNamespace =
            OMAbstractFactory.getSOAP11Factory().createOMNamespace(WSA_NAMESPACE, "wsa");

    public void invoke(MessageContext msgContext) throws AxisFault {

        if (!isAddressingEnabled) {
            return;
        }

        MessageContext lastInMessageContext = msgContext.getMepContext().getLastInMessageContext();


        MessageInformationHeadersCollection messageInformationHeaders =
                msgContext.getMessageInformationHeaders();
        SOAPHeader soapHeader = msgContext.getEnvelope().getHeader();


        EndpointReference epr = messageInformationHeaders.getTo();
        if (epr != null && (lastInMessageContext == null || overrideINMessageInformation)) {
            addToSOAPHeader(epr, AddressingConstants.WSA_TO, soapHeader);
        } else if (lastInMessageContext != null) {
            if (!msgContext.getEnvelope().getBody().hasFault()) {
                setINReplyToAsOUTTo(lastInMessageContext, soapHeader);
            } else {
                setINFaultToAsOUTTo(lastInMessageContext, soapHeader);
            }
        }

        String action = messageInformationHeaders.getAction();
        if (action != null) {
            processStringInfo(action, WSA_ACTION, soapHeader);
        }

        epr = messageInformationHeaders.getReplyTo();
        if (epr != null) {//optional
            addToSOAPHeader(epr, AddressingConstants.WSA_REPLY_TO, soapHeader);
        }

        epr = messageInformationHeaders.getFrom();
        if (epr != null) {//optional
            addToSOAPHeader(epr, AddressingConstants.WSA_FROM, soapHeader);
        }
        epr = messageInformationHeaders.getFaultTo();
        if (epr != null) {//optional
            addToSOAPHeader(epr, AddressingConstants.WSA_FAULT_TO, soapHeader);
        }

        String messageID = messageInformationHeaders.getMessageId();
        if (messageID != null) {//optional
            processStringInfo(messageID, WSA_MESSAGE_ID, soapHeader);
        }

        RelatesTo relatesTo = messageInformationHeaders.getRelatesTo();
        OMElement relatesToHeader = null;

        if (!overrideINMessageInformation && lastInMessageContext != null && "".equals(lastInMessageContext.getMessageID())) {
            relatesToHeader = processStringInfo(lastInMessageContext.getMessageID(), WSA_RELATES_TO, soapHeader);
        } else if (relatesTo != null) {
            relatesToHeader = processStringInfo(relatesTo.getValue(), WSA_RELATES_TO, soapHeader);
        }

        if (relatesToHeader != null)
            if ("".equals(relatesTo.getRelationshipType())) {
                relatesToHeader.addAttribute(WSA_RELATES_TO_RELATIONSHIP_TYPE,
                        WSA_RELATES_TO_RELATIONSHIP_TYPE_DEFAULT_VALUE,
                        addressingNamespace);
            } else {
                relatesToHeader.addAttribute(WSA_RELATES_TO_RELATIONSHIP_TYPE,
                        relatesTo.getRelationshipType(),
                        addressingNamespace);
            }
    }


    private void setINFaultToAsOUTTo(MessageContext lastInMessageContext, SOAPHeader soapHeader) {
        EndpointReference lastInFaultTo = lastInMessageContext.getFaultTo();
        if (lastInFaultTo != null) {
            addToSOAPHeader(lastInFaultTo, AddressingConstants.WSA_TO, soapHeader);
        } else {
            setINReplyToAsOUTTo(lastInMessageContext, soapHeader);
        }
    }

    private void setINReplyToAsOUTTo(MessageContext lastInMessageContext, SOAPHeader soapHeader) {
        EndpointReference lastInReplyTo = lastInMessageContext.getReplyTo();
        if (lastInReplyTo != null) {
            addToSOAPHeader(lastInReplyTo, AddressingConstants.WSA_TO, soapHeader);
        }
    }

    private OMElement processStringInfo(String value,
                                        String type,
                                        SOAPHeader soapHeader) {
        if (!"".equals(value) && value != null) {
            SOAPHeaderBlock soapHeaderBlock =
                    soapHeader.addHeaderBlock(type, addressingNamespace);
            soapHeaderBlock.addChild(OMAbstractFactory.getSOAP11Factory().createText(value));
            return soapHeaderBlock;
        }
        return null;
    }

    protected void addToSOAPHeader(EndpointReference epr,
                                   String type,
                                   SOAPHeader soapHeader) {
        if (epr == null) {
            return;
        }
        String address = epr.getAddress();
        if (!"".equals(address) && address != null) {
            SOAPHeaderBlock soapHeaderBlock =
                    soapHeader.addHeaderBlock(type, addressingNamespace);
            OMElement addressElement =
                    OMAbstractFactory.getSOAP11Factory().createOMElement(EPR_ADDRESS,
                            addressingNamespace);
            soapHeaderBlock.addChild(addressElement);
            addressElement.setText(address);

        }

        QName portType = epr.getPortType();
        if (portType != null) {
            SOAPHeaderBlock soapHeaderBlock =
                    soapHeader.addHeaderBlock(EPR_PORT_TYPE, addressingNamespace);
            soapHeaderBlock.addChild(OMAbstractFactory.getSOAP11Factory().createText(portType.getPrefix() + ":" + portType.getLocalPart()));
        }

        ServiceName serviceName = epr.getServiceName();
        if (serviceName != null) {
            SOAPHeaderBlock soapHeaderBlock =
                    soapHeader.addHeaderBlock(EPR_SERVICE_NAME,
                            addressingNamespace);
            soapHeaderBlock.addAttribute(EPR_SERVICE_NAME_PORT_NAME,
                    serviceName.getPortName(),
                    addressingNamespace);
            soapHeaderBlock.addChild(OMAbstractFactory.getSOAP11Factory().createText(serviceName.getName().getPrefix()
                    + ":"
                    + serviceName.getName().getLocalPart()));
        }

        AnyContentType referenceParameters = epr.getReferenceParameters();
        processAnyContentType(referenceParameters, soapHeader);

        AnyContentType referenceProperties = epr.getReferenceProperties();
        processAnyContentType(referenceProperties, soapHeader);

    }

    private void processAnyContentType(AnyContentType referenceParameters,
                                       SOAPHeader soapHeader) {
        if (referenceParameters != null) {
            Iterator iterator = referenceParameters.getKeys();
            while (iterator.hasNext()) {
                QName key = (QName) iterator.next();
                String value = referenceParameters.getReferenceValue(key);
                OMElement omElement =
                        OMAbstractFactory.getSOAP11Factory().createOMElement(key, soapHeader);
                soapHeader.addChild(omElement);
                omElement.addChild(OMAbstractFactory.getSOAP11Factory().createText(value));
            }
        }
    }
}
