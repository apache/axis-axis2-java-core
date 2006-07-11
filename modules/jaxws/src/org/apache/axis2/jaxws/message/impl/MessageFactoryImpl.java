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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.MessageFactory;

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
	public Message createFrom(XMLStreamReader reader) throws XMLStreamException, MessageException {
		StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(reader, null);  // Pass null has the version to trigger autodetection
		SOAPEnvelope omEnvelope = builder.getSOAPEnvelope();
		return createFrom(omEnvelope);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.MessageFactory#createFrom(org.apache.axiom.om.OMElement)
	 */
	public Message createFrom(OMElement omElement) throws XMLStreamException, MessageException {
		return new MessageImpl(omElement);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.MessageFactory#create(org.apache.axis2.jaxws.message.Protocol)
	 */
	public Message create(Protocol protocol) throws XMLStreamException, MessageException {
		return new MessageImpl(protocol);
	}

}
