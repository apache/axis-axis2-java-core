package org.apache.axis.handlers.addressing;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.MessageInformationHeadersCollection;
import org.apache.axis.addressing.miheaders.RelatesTo;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMElement;
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

    // this parameter has to be set by the module deployer.
    private boolean isAddressingOptional = true;

    private Log logger = LogFactory.getLog(getClass());


    public void invoke(MessageContext msgContext) throws AxisFault {
        logger.debug("Starting Addressing IN Handler .........");
        SOAPHeader header = msgContext.getEnvelope().getHeader();
        if(header == null){
            return;
        }

        ArrayList addressingHeaders = header.getHeaderBolcksWithNSURI(AddressingConstants.WSA_NAMESPACE);
        if (addressingHeaders != null) {
            extractAddressingInformationFromHeaders(header, msgContext.getMessageInformationHeaders(),addressingHeaders);
        } else {
            // no addressing headers present
            if(!isAddressingOptional){
                throw new AxisFault("Addressing Handlers should present, but doesn't present in the incoming message !!");
            }
            logger.debug("No Addressing Headers present in the IN message. Addressing In Handler does nothing.");
        }
    }

    protected MessageInformationHeadersCollection extractAddressingInformationFromHeaders(SOAPHeader header, MessageInformationHeadersCollection messageInformationHeadersCollection,ArrayList addressingHeaders) {
        if(messageInformationHeadersCollection == null){
             messageInformationHeadersCollection = new MessageInformationHeadersCollection();
        }

        Iterator addressingHeadersIt = addressingHeaders.iterator();
        while (addressingHeadersIt.hasNext()) {
            SOAPHeaderBlock soapHeaderBlock = (SOAPHeaderBlock) addressingHeadersIt.next();
            EndpointReference epr = null;
            if (AddressingConstants.WSA_TO.equals(soapHeaderBlock.getLocalName())) {
                epr = messageInformationHeadersCollection.getTo();
                if(epr == null){
                    epr = new EndpointReference(AddressingConstants.WSA_TO, "");
                    messageInformationHeadersCollection.setTo(epr);
                }
                extractEPRInformation(soapHeaderBlock, epr);
            } else if (AddressingConstants.WSA_FROM.equals(soapHeaderBlock.getLocalName())) {
                epr = messageInformationHeadersCollection.getFrom();
                if(epr == null){
                    epr = new EndpointReference(AddressingConstants.WSA_FROM, "");
                    messageInformationHeadersCollection.setFrom(epr);
                }
                extractEPRInformation(soapHeaderBlock, epr);
            } else if (AddressingConstants.WSA_REPLY_TO.equals(soapHeaderBlock.getLocalName())) {
                epr = messageInformationHeadersCollection.getReplyTo();
                if( epr == null){
                    epr = new EndpointReference(AddressingConstants.WSA_REPLY_TO, "");
                    messageInformationHeadersCollection.setReplyTo(epr);
                }
                extractEPRInformation(soapHeaderBlock, epr);
            } else if (AddressingConstants.WSA_FAULT_TO.equals(soapHeaderBlock.getLocalName())) {
                epr = messageInformationHeadersCollection.getFaultTo();
                if( epr == null){
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
                OMAttribute relationshipType = soapHeaderBlock.getFirstAttribute(new QName(AddressingConstants.WSA_NAMESPACE, AddressingConstants.WSA_RELATES_TO_RELATIONSHIP_TYPE));
                RelatesTo relatesTo = new RelatesTo(address, relationshipType == null ? "wsa:Reply" : relationshipType.getValue());
                messageInformationHeadersCollection.setRelatesTo(relatesTo);
            }
        }

        return messageInformationHeadersCollection;
    }


    private void extractEPRInformation(SOAPHeaderBlock headerBlock, EndpointReference epr) {
        OMElement address =  headerBlock.getFirstChildWithName(new QName(AddressingConstants.WSA_NAMESPACE, AddressingConstants.EPR_ADDRESS));
        if (address != null) {
            epr.setAddress(address.getText());
        }


    }
}
