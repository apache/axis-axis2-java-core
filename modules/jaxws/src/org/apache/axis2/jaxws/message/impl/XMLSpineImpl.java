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

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.OMBlockFactory;
import org.apache.axis2.jaxws.message.util.MessageUtils;
import org.apache.axis2.jaxws.message.util.Reader2Writer;
import org.apache.axis2.jaxws.message.util.XMLFaultUtils;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	
    private static Log log = LogFactory.getLog(XMLSpine.class);
	private static OMBlockFactory obf = (OMBlockFactory) FactoryRegistry.getFactory(OMBlockFactory.class);
	
	private Protocol protocol = Protocol.unknown;
    private Style style = Style.DOCUMENT;
	private SOAPEnvelope root = null;
	private SOAPFactory soapFactory = null;
	private List<Block> headerBlocks = new ArrayList<Block>();
	private List<Block> bodyBlocks   = new ArrayList<Block>();
	private List<Block> detailBlocks = new ArrayList<Block>();
	private boolean consumed = false;
	private Iterator bodyIterator = null;
	private Iterator detailIterator = null;
    private Message parent = null;
    
	/**
	 * Create a lightweight representation of this protocol
	 * (i.e. the Envelope, Header and Body)
     * @param protocol Protocol
     * @param style Style
     * @param opQName QName if the Style is RPC
	 */
	public XMLSpineImpl(Protocol protocol, Style style) {
		super();
		this.protocol = protocol;
        this.style = style;
		soapFactory = getFactory(protocol);
		root = createEmptyEnvelope(protocol, style, soapFactory);
	}
	
	/**
	 * Create spine from an existing OM tree
	 * @param envelope
     * @param style Style
	 * @throws MessageException
	 */
	public XMLSpineImpl(SOAPEnvelope envelope, Style style) throws MessageException {
		super();
        this.style = style;
		init(envelope);
		if (root.getNamespace().getNamespaceURI().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
			protocol = Protocol.soap11;
		} else if (root.getNamespace().getNamespaceURI().equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
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
		detailIterator = null;
        soapFactory = MessageUtils.getSOAPFactory(root);
		
		
		// If a header block exists, create an OMBlock for each element
		// This advances the StAX parser past the header end tag
		SOAPHeader header = root.getHeader();
		if (header != null) {
            Iterator it = header.getChildren();
            advanceIterator(it, headerBlocks, true);             
        }

		
		SOAPBody body = root.getBody();
        if (body == null) {
            // Create the body if one does not exist
            body = soapFactory.createSOAPBody(root);
        }
        
		if (!body.hasFault()) {
			// Normal processing
			// Only create the first body block, this ensures that the StAX 
			// cursor is not advanced beyond the tag of the first element
            if (style == Style.DOCUMENT) {
                bodyIterator = body.getChildren();
            } else {
                // For RPC the blocks are within the operation element
                OMElement op = body.getFirstElement();
                if (op == null) {
                    // Create one
                    OMNamespace ns = soapFactory.createOMNamespace("", "");
                    op = soapFactory.createOMElement("PLACEHOLDER_OPERATION", ns, body);
                }
                bodyIterator = op.getChildren();
            }
            // We only want to advance past the first block...we will lazily parse the other blocks
			advanceIterator(bodyIterator, bodyBlocks, false);
		} else {
			// Process the Fault

			SOAPFault fault = body.getFault();
			SOAPFaultDetail detail = fault.getDetail();
			if (detail != null) {
			  detailIterator = detail.getChildren();
			  advanceIterator(detailIterator, detailBlocks, true);  // Advance through all of the detail blocks
			}
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
		while (it.hasNext() && !found) {
			OMNode node = (OMNode) it.next();
			if (node instanceof OMElement) {
				// Elements are converted into Blocks
				Block block = null;
				try { 
					block = obf.createFrom((OMElement) node, null, null);
                    it.remove();  // Remove the nodes as they are converted 
				} catch (XMLStreamException xse) {
					throw ExceptionFactory.makeMessageException(xse);
				}
				blocks.add(block);
                if (!toEnd) {
                    found = true;  // Found the one element, indicate that we can quit
                }
			} else {
                // TODO LOGGING ?
				// A Non-element is found, it is probably whitespace text, but since
                // there is no way to represent this as a block, it is ignored.
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
	private static SOAPEnvelope createEmptyEnvelope(Protocol protocol, Style style, SOAPFactory factory) {
		SOAPEnvelope env = factory.createSOAPEnvelope();
		// Add an empty body and header
		factory.createSOAPBody(env);
		factory.createSOAPHeader(env);
        
        // Create a dummy operation element if this is an rpc message
        if (style == Style.RPC) {
            OMNamespace ns = factory.createOMNamespace("", "");
            factory.createOMElement("PLACEHOLDER_OPERATION", ns, env.getBody());
        }

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

	public XMLFault getXMLFault() throws MessageException {
		if (!isFault()) {
		    return null;
        }
        
        // Advance through all of the detail blocks
        int numDetailBlocks = getNumDetailBlocks();
        
        Block[] blocks = null;
        if (numDetailBlocks > 0) {
            blocks = new Block[numDetailBlocks];
            blocks = detailBlocks.toArray(blocks);
        }
        
        XMLFault xmlFault = XMLFaultUtils.createXMLFault(root.getBody().getFault(), blocks);
        return xmlFault;
	}
    
    private int getNumDetailBlocks() throws MessageException {
        if (detailIterator != null) {
            advanceIterator(detailIterator, detailBlocks, true);
        }
        return detailBlocks.size();
    }
	
	public void setXMLFault(XMLFault xmlFault) throws MessageException {
        
        // Clear out the existing body and detail blocks
        SOAPBody body = root.getBody();
        getNumDetailBlocks(); // Forces parse of existing detail blocks
        getNumBodyBlocks();  // Forces parse over body
        bodyBlocks.clear();
        detailBlocks.clear();
        OMNode child = body.getFirstOMChild();
        while (child != null) {
            child.detach();
            child = body.getFirstOMChild();
        }
        
	    // Add a SOAPFault to the body.  Don't add the detail blocks to the SOAPFault
        SOAPFault soapFault =XMLFaultUtils.createSOAPFault(xmlFault, body, true);
        
        // The spine now owns the Detail Blocks from the XMLFault
        Block[] blocks = xmlFault.getDetailBlocks();
        if (blocks != null) {
            for(int i=0; i<blocks.length; i++) {
                detailBlocks.add(blocks[i]);
            }
        }
	}

	public boolean isConsumed() {
		return consumed;
	}

	public OMElement getAsOMElement() throws MessageException {
	    if (headerBlocks != null) {        
	        for (int i=0; i<headerBlocks.size(); i++) {                   
	            Block b = (Block) headerBlocks.get(i);                   
	            OMElement e = new OMSourcedElementImpl(b.getQName(),soapFactory, b);                  
	            root.getHeader().addChild(e);                   
	        }               
	        headerBlocks.clear();               
	    }            
	    if (bodyBlocks != null) {
	        for (int i=0; i<bodyBlocks.size(); i++) {                   
	            Block b = (Block) bodyBlocks.get(i);       
                
                // In Dispatch<String> Provider<String> and some source modes, the block is built from text data.
                // The assumption is that the text data represents one or more elements, but this may not be the
                // case.  It could represent whitespace.  In such cases querying the qname will cause a parse of the
                // code and the parse will fail.  We will interpret a failure as an indication that the block does not represent
                // an element
                
                QName blockQName = null;
                try {
                    blockQName = b.getQName();
                } catch (Exception e) {
                    log.debug("The block does not represent an element");
                }
                
                // Only create an OMElement if the block represents an element
                if (blockQName != null) {
                    OMElement e = new OMSourcedElementImpl(b.getQName(),soapFactory, b);
                
                    // The blocks are directly under the body if DOCUMENT.
                    // The blocks are under the operation element if RPC
                    if (style == Style.DOCUMENT) {
                        root.getBody().addChild(e);    
                    } else {
                        root.getBody().getFirstElement().addChild(e);   
                    }
                }             
	        }               
	        bodyBlocks.clear();               
	    }
	    if (detailBlocks != null) {
	        for (int i=0; i<detailBlocks.size(); i++) {                   
	            Block b = (Block) detailBlocks.get(i);                  
	            OMElement e = new OMSourcedElementImpl(b.getQName(),soapFactory, b);                  
	            root.getBody().getFault().getDetail().addChild(e);                  
	        }               
	        detailBlocks.clear();  
	    }
	    return root;
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
        if (index < 0) {
            // REVIEW What should happen if header block not found
            return null;  
        }
		try {
			Block oldBlock = headerBlocks.get(index);
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
        if (index >= 0) {
            headerBlocks.set(index, block);
        } else {
            headerBlocks.add(block);
        }
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
        if (index >= 0) {
            headerBlocks.remove(index);
        }
	}

	public String traceString(String indent) {
		// TODO Trace String Support
		return null;
	}

	public String getXMLPartContentType() {
        return "SPINE";
    }

    public boolean isFault() throws MessageException {
		return XMLFaultUtils.isFault(root);
	}

    public Style getStyle() {
        return style;
    }

    public QName getOperationElement() {
        if (style == style.DOCUMENT) {
            return null;
        } else {
            return root.getBody().getFirstElement().getQName();
        }
    }

    public void setOperationElement(QName operationQName) {
        if (style == style.RPC) {
            if (soapFactory == null) {
                soapFactory = MessageUtils.getSOAPFactory(root);
            }
            OMNamespace ns = soapFactory.createOMNamespace(operationQName.getNamespaceURI(), operationQName.getPrefix());
            OMElement opElement = root.getBody().getFirstElement();
            opElement.setLocalName(operationQName.getLocalPart());
            opElement.setNamespace(ns);
        }
    }
    
}
