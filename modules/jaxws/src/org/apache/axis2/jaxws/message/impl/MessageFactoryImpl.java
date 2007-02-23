/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.message.impl;

import java.util.Iterator;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.message.Attachment;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.databinding.SOAPEnvelopeBlock;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.util.MessageUtils;

/**
 * MessageFactoryImpl
 */
public class MessageFactoryImpl implements MessageFactory {

	/**
	 * Default Constructor required for Factory 
	 */
	public MessageFactoryImpl() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.factory.MessageFactory#createFrom(javax.xml.stream.XMLStreamReader)
	 */
	public Message createFrom(XMLStreamReader reader, Protocol protocol) throws XMLStreamException, WebServiceException {
		StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(reader, null);  // Pass null has the version to trigger autodetection
		SOAPEnvelope omEnvelope = builder.getSOAPEnvelope();
		return createFrom(omEnvelope, protocol);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.MessageFactory#createFrom(org.apache.axiom.om.OMElement)
	 */
	public Message createFrom(OMElement omElement, Protocol protocol) throws XMLStreamException, WebServiceException {
		return new MessageImpl(omElement, protocol);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.MessageFactory#create(org.apache.axis2.jaxws.message.Protocol)
	 */
	public Message create(Protocol protocol) throws XMLStreamException, WebServiceException {
		return new MessageImpl(protocol);
	}


	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.factory.MessageFactory#createFrom(javax.xml.soap.SOAPMessage)
	 */
	public Message createFrom(SOAPMessage message) throws XMLStreamException, WebServiceException {
		try {
            // Create a Message with an XMLPart from the SOAPEnvelope
			Message m = new MessageImpl(message.getSOAPPart().getEnvelope());
			if (message.countAttachments() > 0) {
                Iterator it = message.getAttachments();
                while (it.hasNext()) {
                    AttachmentPart ap = (AttachmentPart) it.next();
                    Attachment a = MessageUtils.createAttachment(ap, m);
                    m.addAttachment(a);
                }
            }
			return m;
		} catch (Exception e) {
			throw ExceptionFactory.makeWebServiceException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.factory.MessageFactory#createFrom(org.apache.axis2.jaxws.message.Block, java.lang.Object)
	 */
	public Message createFrom(Block block, Object context, Protocol protocol) throws XMLStreamException, WebServiceException {
		
		// Small optimization to quickly consider the SOAPEnvelope case
		if (block instanceof SOAPEnvelopeBlock) {
			return new MessageImpl((SOAPEnvelope) block.getBusinessObject(true), protocol);
		}
		return createFrom(block.getXMLStreamReader(true), protocol);
	}

}
