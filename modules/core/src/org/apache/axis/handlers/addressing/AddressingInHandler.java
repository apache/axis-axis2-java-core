package org.apache.axis.handlers.addressing;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.miheaders.RelatesTo;
import org.apache.axis.addressing.om.MessageInformationHeadersCollection;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.SOAPHeader;
import org.apache.axis.om.SOAPHeaderBlock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public class AddressingInHandler extends AbstractHandler {
    /**
     * Eran Chinthaka (chinthaka@apache.org) Date : 03-04-2005 Time : 14:42
     */
    private Log logger = LogFactory.getLog(getClass());


    public void invoke(MessageContext msgContext) throws AxisFault {
        logger.debug("Starting Addressing IN Handler .........");
        SOAPHeader header = msgContext.getEnvelope().getHeader();
        OMNamespace addressingNamespace = header.findInScopeNamespace(AddressingConstants.WSA_NAMESPACE, "");
        if (addressingNamespace != null) {
            extractAddressingInformationFromHeaders(header, msgContext.getMessageInformationHeaders());
        } else {
            // no addressing headers present
            logger.debug("No Addressing Headers present in the IN message. Addressing In Handler does nothing.");
        }
    }

    protected MessageInformationHeadersCollection extractAddressingInformationFromHeaders(SOAPHeader header, MessageInformationHeadersCollection messageInformationHeadersCollection) {
        if(messageInformationHeadersCollection == null){
             messageInformationHeadersCollection = new MessageInformationHeadersCollection();
        }

        Iterator addressingHeaders = header.getChildrenWithName(new QName(AddressingConstants.WSA_NAMESPACE, ""));
        while (addressingHeaders.hasNext()) {
            SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) addressingHeaders.next();
            EndpointReference epr = null;
            if (AddressingConstants.WSA_TO.equals(soapHeaderBlock.getLocalName())) {
                if(messageInformationHeadersCollection.getTo() == null){
                    epr = new EndpointReference(AddressingConstants.WSA_TO, "");
                    messageInformationHeadersCollection.setTo(epr);
                }
                extractEPRInformation(soapHeaderBlock, epr);
            } else if (AddressingConstants.WSA_FROM.equals(soapHeaderBlock.getLocalName())) {
                if(messageInformationHeadersCollection.getFrom() == null){
                    epr = new EndpointReference(AddressingConstants.WSA_FROM, "");
                    messageInformationHeadersCollection.setFrom(epr);
                }
                extractEPRInformation(soapHeaderBlock, epr);
            } else if (AddressingConstants.WSA_REPLY_TO.equals(soapHeaderBlock.getLocalName())) {
                if(messageInformationHeadersCollection.getReplyTo() == null){
                    epr = new EndpointReference(AddressingConstants.WSA_REPLY_TO, "");
                    messageInformationHeadersCollection.setReplyTo(epr);
                }
                extractEPRInformation(soapHeaderBlock, epr);
            } else if (AddressingConstants.WSA_FAULT_TO.equals(soapHeaderBlock.getLocalName())) {
                if(messageInformationHeadersCollection.getFaultTo() == null){
                    epr = new EndpointReference(AddressingConstants.WSA_FAULT_TO, "");
                    messageInformationHeadersCollection.setTo(epr);
                }
                extractEPRInformation(soapHeaderBlock, epr);
            } else if (AddressingConstants.WSA_MESSAGE_ID.equals(soapHeaderBlock.getLocalName())) {
                messageInformationHeadersCollection.setMessageId(soapHeaderBlock.getText());
            } else if (AddressingConstants.WSA_ACTION.equals(soapHeaderBlock.getLocalName())) {
                messageInformationHeadersCollection.setAction(soapHeaderBlock.getText());
            } else if (AddressingConstants.WSA_RELATES_TO.equals(soapHeaderBlock.getLocalName())) {
                String address = soapHeaderBlock.getText();
                OMAttribute relationshipType = soapHeaderBlock.getAttributeWithQName(new QName(AddressingConstants.WSA_NAMESPACE, AddressingConstants.WSA_RELATES_TO_RELATIONSHIP_TYPE));
                RelatesTo relatesTo = new RelatesTo(address, relationshipType == null ? "wsa:Reply" : relationshipType.getValue());
                messageInformationHeadersCollection.setRelatesTo(relatesTo);
            }
        }

        return messageInformationHeadersCollection;
    }


    private void extractEPRInformation(SOAPHeaderBlock headerBlock, EndpointReference epr) {
        OMElement address = (OMElement) headerBlock.getChildWithName(new QName(AddressingConstants.WSA_NAMESPACE, AddressingConstants.EPR_ADDRESS));
        if (address != null) {
            epr.setAddress(address.getText());
        }


    }
}
