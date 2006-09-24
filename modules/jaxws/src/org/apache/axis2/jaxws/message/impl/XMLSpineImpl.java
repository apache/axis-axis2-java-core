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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.MessageInternalException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.OMBlockFactory;
import org.apache.axis2.jaxws.message.util.Reader2Writer;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

/**
 * XMLSpineImpl
 * 
 * An XMLSpine consists of an OM SOAPEnvelope which defines the "spine" of the message
 * (i.e. the SOAPEnvelope element, the SOAPHeader element, the SOAPBody element and
 * perhaps the SOAPFault element).  The real payload portions of the message are 
 * contained in Block object (headerBlocks, bodyBlocks, detailBlocks).
 * 
 * This "shortened" OM tree allows the JAX-WS implementation to process the message
 * without creating fully populated OM trees.
 *
 */
class XMLSpineImpl implements XMLSpine {
	
	private static OMBlockFactory obf = (OMBlockFactory) FactoryRegistry.getFactory(OMBlockFactory.class);
	
	private Protocol protocol = Protocol.unknown;
	private SOAPEnvelope root = null;
	private SOAPFactory soapFactory = null;
	private List<Block> headerBlocks = new ArrayList<Block>();
	private List<Block> bodyBlocks   = new ArrayList<Block>();
	private List<Block> detailBlocks = new ArrayList<Block>();
	private boolean consumed = false;
	private Iterator bodyIterator = null;
    private Message parent;

	/**
	 * Create a lightweight representation of this protocol
	 * (i.e. the Envelope, Header and Body)
	 */
	public XMLSpineImpl(Protocol protocol) {
		super();
		this.protocol = protocol;
		soapFactory = getFactory(protocol);
		root = createEmptyEnvelope(protocol, soapFactory);
	}
	
	/**
	 * Create spine from an existing OM tree
	 * @param envelope
	 * @throws MessageException
	 */
	public XMLSpineImpl(SOAPEnvelope envelope) throws MessageException {
		super();
		init(envelope);
		if (root.getNamespace().getName().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
			protocol = Protocol.soap11;
		} else if (root.getNamespace().getName().equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
			protocol = Protocol.soap12;
		} else {
			// TODO Support for REST
			throw ExceptionFactory.makeMessageInternalException(Messages.getMessage("RESTIsNotSupported"), null);
		}
	} 

	private void init(SOAPEnvelope envelope) throws MessageException {
		root = envelope;
		headerBlocks.clear();
		bodyBlocks.clear();
		detailBlocks.clear();
		bodyIterator = null;
		
		
		// If a header block exists, create an OMBlock for each element
		// This advances the StAX parser past the header end tag
		SOAPHeader header = root.getHeader();
		if (header != null) {
            Iterator it = header.getChildren();
            advanceIterator(it, headerBlocks, true);            
        }

		
		SOAPBody body = root.getBody();
		if (!body.hasFault()) {
			// Normal processing
			// Only create the first body block, this ensures that the StAX 
			// cursor is not advanced beyond the tag of the first element
			bodyIterator = body.getChildren();
			advanceIterator(bodyIterator, bodyBlocks, false);
		} else {
			// Process the Fault
			// TODO Add Fault Processing
			throw ExceptionFactory.makeMessageException(Messages.getMessage("SOAPFaultIsNotImplemented"));
		}
		return;
	}
	
	/**
	 * Utility method to advance the iterator and populate the blocks
	 * @param it Iterator
	 * @param blocks List<Block> to update
	 * @param toEnd if true, iterator is advanced to the end, otherwise it is advanced one Element
	 * @throws MessageException
	 */
	private  void advanceIterator(Iterator it, List<Block> blocks, boolean toEnd) throws MessageException {
		
		// TODO This code must be reworked.  The OM Iterator causes the entire OMElement to be 
		// parsed when it.next() is invoked.  I will need to fix this to gain performance.  (scheu)
		
		boolean found = false;
		boolean first = true;
		while (it.hasNext() && (!found && !toEnd)) {
			// Remove the nodes as they are converted into blocks
			if (!first) {
				it.remove();
			}
			first = true;
			
			OMNode node = (OMNode) it.next();
			if (node instanceof OMElement) {
				// Elements are converted into Blocks
				Block block = null;
				try { 
					block = obf.createFrom((OMElement) node, null, null);
				} catch (XMLStreamException xse) {
					throw ExceptionFactory.makeMessageException(xse);
				}
				blocks.add(block);
                block.setParent(this);
			} else {
				System.out.println("NON-ELEMENT FOUND: " +  node.getClass().getName());
			}
		}
	}
	
	private static SOAPFactory getFactory(Protocol protocol) {
		SOAPFactory soapFactory;
		if (protocol == Protocol.soap11) {
			soapFactory = new SOAP11Factory();
		} else if (protocol == Protocol.soap12) {
			soapFactory = new SOAP12Factory();
		} else {
			// TODO REST Support is needed
			throw ExceptionFactory.makeMessageInternalException(Messages.getMessage("RESTIsNotSupported"), null);
		}
		return soapFactory;
	}
	private static SOAPEnvelope createEmptyEnvelope(Protocol protocol, SOAPFactory factory) {
		SOAPEnvelope env = factory.createSOAPEnvelope();
		// Add an empty body and header
		factory.createSOAPBody(env);
		factory.createSOAPHeader(env);

		return env;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.XMLPart#getProtocol()
	 */
	public Protocol getProtocol() {
		return protocol;
	}
    
    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.XMLPart#getParent()
     */
    public Message getParent() {
        return parent;
    }
    
    /*
     * Set the backpointer to this XMLPart's parent Message
     */
    public void setParent(Message p) {
        parent = p;
    }

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.XMLPart#outputTo(javax.xml.stream.XMLStreamWriter, boolean)
	 */
	public void outputTo(XMLStreamWriter writer, boolean consume) throws XMLStreamException, MessageException {
		Reader2Writer r2w = new Reader2Writer(getXMLStreamReader(consume));
		r2w.outputTo(writer);
	}

	public XMLStreamReader getXMLStreamReader(boolean consume) throws MessageException {
		return new XMLStreamReaderForXMLSpine(root, protocol,
					headerBlocks, bodyBlocks, detailBlocks, consume);
	}

	public boolean isConsumed() {
		return consumed;
	}

	public javax.xml.soap.SOAPEnvelope getAsSOAPEnvelope() throws MessageException {
		throw ExceptionFactory.makeMessageInternalException(Messages.getMessage("NeverCalled", "XMLSpineImpl.getAsSOAPEnvelope()"), null);
	}

	public OMElement getAsOMElement() throws MessageException {
		throw ExceptionFactory.makeMessageInternalException(Messages.getMessage("NeverCalled", "XMLSpineImpl.getAsOMElement()"), null);
	}
	
	

	public Block getAsBlock(Object context, BlockFactory blockFactory) throws MessageException, XMLStreamException {
		throw ExceptionFactory.makeMessageInternalException(Messages.getMessage("NeverCalled", "XMLSpineImpl.getAsBlock()"), null);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.message.XMLPart#getNumBodyBlocks()
	 */
	public int getNumBodyBlocks() throws MessageException {
		if (bodyIterator != null) {
			advanceIterator(bodyIterator, bodyBlocks, true);
		}
		return bodyBlocks.size();
	}

	public Block getBodyBlock(int index, Object context, BlockFactory blockFactory) throws MessageException {
		if (index >= bodyBlocks.size() && bodyIterator != null) {
			advanceIterator(bodyIterator, bodyBlocks, true);
		}
		try {
			Block oldBlock = bodyBlocks.get(index);
		
			// Convert to new Block
			Block newBlock = blockFactory.createFrom(oldBlock, context);
			if (newBlock != oldBlock) {
				bodyBlocks.set(index, newBlock);
			}
			return newBlock;
		} catch (XMLStreamException xse) {
			throw ExceptionFactory.makeMessageException(xse);
		}
	}

	public void setBodyBlock(int index, Block block) throws MessageException {
		if (index >= bodyBlocks.size() && bodyIterator != null) {
			advanceIterator(bodyIterator, bodyBlocks, true);
		}
		bodyBlocks.add(index, block);
        block.setParent(this);
	}

	public void removeBodyBlock(int index) throws MessageException {
		if (index >= bodyBlocks.size() && bodyIterator != null) {
			advanceIterator(bodyIterator, bodyBlocks, true);
		}
		bodyBlocks.remove(index);
	}

	public int getNumHeaderBlocks() throws MessageException {
		return headerBlocks.size();
	}

	public Block getHeaderBlock(String namespace, String localPart, Object context, BlockFactory blockFactory) throws MessageException {
		int index = getHeaderBlockIndex(namespace, localPart);
		try {
			Block oldBlock = bodyBlocks.get(index);
		
			// Convert to new Block
			Block newBlock = blockFactory.createFrom(oldBlock, context);
			if (newBlock != oldBlock) {
				headerBlocks.set(index, newBlock);
			}
			return newBlock;
		} catch (XMLStreamException xse) {
			throw ExceptionFactory.makeMessageException(xse);
		}
	}

	public void setHeaderBlock(String namespace, String localPart, Block block) throws MessageException {
		int index = getHeaderBlockIndex(namespace, localPart);
		headerBlocks.set(index, block);
        block.setParent(this);
	}

	/**
	 * Utility method to locate header block
	 * @param namespace
	 * @param localPart
	 * @return index of header block or -1
	 * @throws MessageException
	 */
	private int getHeaderBlockIndex(String namespace, String localPart) throws MessageException {
		for (int i=0; i<headerBlocks.size(); i++) {
			Block block = headerBlocks.get(i);
			QName qName = block.getQName();
			if (qName.getNamespaceURI().equals(namespace) &&
				qName.getLocalPart().equals(localPart)) {
				return i;
			}
		}
		return -1;
	}
	public void removeHeaderBlock(String namespace, String localPart) throws MessageException {
		int index = getHeaderBlockIndex(namespace, localPart);
		headerBlocks.remove(index);
	}

	public String traceString(String indent) {
		// TODO Trace String Support
		return null;
	}
	
}
