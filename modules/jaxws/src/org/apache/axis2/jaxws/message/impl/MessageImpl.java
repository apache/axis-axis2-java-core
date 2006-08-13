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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Attachment;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLPart;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLPartFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

import java.io.ByteArrayOutputStream;

/**
 * MessageImpl
 * A Message is an XML part + Attachments.
 * Most of the implementation delegates to the XMLPart implementation.
 */
public class MessageImpl implements Message {

	Protocol protocol = Protocol.unknown; // the protocol, defaults to unknown
	XMLPart xmlPart = null; // the representation of the xmlpart
	List<Attachment> attachments = new ArrayList<Attachment>(); // non-xml parts
	
	/**
	 * MessageImpl should be constructed via the MessageFactory.
	 * This constructor constructs an empty message with the specified protocol
	 * @param protocol
	 */
	MessageImpl(Protocol protocol) throws MessageException, XMLStreamException {
		super();
		this.protocol = protocol;
		if (protocol.equals(Protocol.unknown)) {
			throw ExceptionFactory.makeMessageException(Messages.getMessage("ProtocolIsNotKnown"));
		} else if (protocol.equals(Protocol.rest)) {
			// TODO Need REST support
			throw ExceptionFactory.makeMessageException(Messages.getMessage("RESTIsNotSupported"));
		}
		XMLPartFactory factory = (XMLPartFactory) FactoryRegistry.getFactory(XMLPartFactory.class);
		xmlPart = factory.create(protocol);
	}
	
	/**
	 * Message is constructed by the MessageFactory.
	 * This constructor creates a message from the specified root.
	 * @param root
	 */
	MessageImpl(OMElement root) throws MessageException, XMLStreamException  {
		XMLPartFactory factory = (XMLPartFactory) FactoryRegistry.getFactory(XMLPartFactory.class);
		xmlPart = factory.createFrom(root);
		protocol = xmlPart.getProtocol();
	}
	
	/**
	 * Message is constructed by the MessageFactory.
	 * This constructor creates a message from the specified root.
	 * @param root
	 */
	MessageImpl(SOAPEnvelope root) throws MessageException, XMLStreamException  {
		XMLPartFactory factory = (XMLPartFactory) FactoryRegistry.getFactory(XMLPartFactory.class);
		xmlPart = factory.createFrom(root);
		protocol = xmlPart.getProtocol();
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.Message#getAsSOAPMessage()
	 */
	public SOAPMessage getAsSOAPMessage() throws MessageException {

		// TODO: 
		// This is a non performant way to create SOAPMessage. I will serialize
		// the xmlpart content and then create an InputStream of byte.
		// Finally create SOAPMessage using this InputStream.
		// The real solution may involve using non-spec, implementation
		// constructors to create a Message from an Envelope
		try {
			// Get OMElement from XMLPart.
			OMElement element = xmlPart.getAsOMElement();
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			element.serialize(outStream);

			// Create InputStream
			ByteArrayInputStream inStream = new ByteArrayInputStream(outStream
					.toByteArray());
			MessageFactory mf = MessageFactory.newInstance();

			// Create soapMessage object from Message Factory using the input
			// stream created from OM.

			// TODO should we read the MIME Header from JAXWS MessageContext.
			// For now I will create a default header
			MimeHeaders defaultHeader = new MimeHeaders();

			// FIXME: Need to toggle based on SOAP 1.1 or SOAP 1.2
			defaultHeader.addHeader("Content-type", "text/xml; charset=UTF-8");
			SOAPMessage soapMessage = mf.createMessage(defaultHeader, inStream);
			return soapMessage;
		} catch (Exception e) {
			throw ExceptionFactory.makeMessageException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.axis2.jaxws.message.XMLPart#getAsBlock(java.lang.Object,
	 *      org.apache.axis2.jaxws.message.factory.BlockFactory)
	 */
	public Block getAsBlock(Object context, BlockFactory blockFactory) throws MessageException, XMLStreamException {
		return xmlPart.getAsBlock(context, blockFactory);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.XMLPart#getAttachments()
	 */
	public List<Attachment> getAttachments() {
		return attachments;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.XMLPart#getProtocol()
	 */
	public Protocol getProtocol() {
		return protocol;
	}

	public OMElement getAsOMElement() throws MessageException {
		return xmlPart.getAsOMElement();
	}

	public javax.xml.soap.SOAPEnvelope getAsSOAPEnvelope() throws MessageException {
		return xmlPart.getAsSOAPEnvelope();
	}

	public Block getBodyBlock(int index, Object context, BlockFactory blockFactory) throws MessageException {
		return xmlPart.getBodyBlock(index, context, blockFactory);
	}

	public Block getHeaderBlock(String namespace, String localPart, Object context, BlockFactory blockFactory) throws MessageException {
		return xmlPart.getHeaderBlock(namespace, localPart, context, blockFactory);
	}

	public int getNumBodyBlocks() throws MessageException {
		return xmlPart.getNumBodyBlocks();
	}

	public int getNumHeaderBlocks() throws MessageException {
		return xmlPart.getNumHeaderBlocks();
	}

	public XMLStreamReader getXMLStreamReader(boolean consume) throws MessageException {
		return xmlPart.getXMLStreamReader(consume);
	}

	public boolean isConsumed() {
		return xmlPart.isConsumed();
	}

	public void outputTo(XMLStreamWriter writer, boolean consume) throws XMLStreamException, MessageException {
		xmlPart.outputTo(writer, consume);
	}

	public void removeBodyBlock(int index) throws MessageException {
		xmlPart.removeBodyBlock(index);
	}

	public void removeHeaderBlock(String namespace, String localPart) throws MessageException {
		xmlPart.removeHeaderBlock(namespace, localPart);
	}

	public void setBodyBlock(int index, Block block) throws MessageException {
		xmlPart.setBodyBlock(index, block);
	}

	public void setHeaderBlock(String namespace, String localPart, Block block) throws MessageException {
		xmlPart.setHeaderBlock(namespace, localPart, block);
	}

	public String traceString(String indent) {
		return xmlPart.traceString(indent);
	}
}
