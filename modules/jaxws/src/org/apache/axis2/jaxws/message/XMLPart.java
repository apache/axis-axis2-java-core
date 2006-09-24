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
package org.apache.axis2.jaxws.message;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.message.factory.BlockFactory;

/**
 * XMLPart
 * 
 * The XML portion of a Message
 * 
 * The JAX-WS implementation (proxy, message receiver, etc.) interact with the 
 * Message via Blocks.  A Block is represented in the message as root element tree 
 * in either the header, body or fault detail section.  The Blocks can be easily
 * decomposed into business objects (which are needed on the JAX-WS interfaces).
 * 
 * In addition, the JAX-WS handler model requires that the XMLPart be exposed as
 * an SAAJ SOAPEnvelope.  
 * 
 * The XMLPart abstraction hides the details of the message transformations from
 * the JAX-WS implementation.
 * 
 * @see org.apache.axis2.jaxws.message.Message
 * @see org.apache.axis2.jaxws.message.Block
 * @see org.apache.axis2.jaxws.message.impl.XMLPartBase for implementation details
 * 
 */
public interface XMLPart {
	
	/**
	 * Get the protocol for this Message (soap11, soap12, etc.)
	 * @return Protocl
	 */
	public Protocol getProtocol();
	
	/**
	 * Write out the Message
	 * @param writer XMLStreamWriter
	 * @param consume true if this is the last request on the block.
	 * @throws MessageException
	 */
	public void outputTo(XMLStreamWriter writer, boolean consume) throws XMLStreamException, MessageException;	
	
	/**
	 * Get the XMLStreamReader represented by this Message for the xml part
	 * @param consume true if this is the last request on the Message
	 * @return XMLStreamReader
	 * @throws MessageException
	 * @throws XMLStreamException
	 */
	public XMLStreamReader getXMLStreamReader(boolean consume) throws MessageException;
	
	/**
	 * isConsumed
	 * Return true if the part is consumed.  Once consumed, the information in the 
	 * part is no longer available.
	 * @return true if the block is consumed (a method was called with consume=true)
	 */
	public boolean isConsumed();
    
    /**
     * getParent
     * Get the Message object that this XMLPart is attached to, if it is 
     * attached to one at all.
     * @return
     */
    public Message getParent();
    
    /**
     * setParent
     * Set the Message object that will hold this XMLPart
     * @param m
     */
    public void setParent(Message m);
    
	/**
	 * getAsEnvelope
	 * Get the xml part as a read/write SOAPEnvelope
	 * @return SOAPEnvelope
	 * @throws MessageException
	 */
	public SOAPEnvelope getAsSOAPEnvelope() throws MessageException;
	
	/**
	 * getAsOMElement
	 * Get the xml part as a read/write OM
	 * @return OMElement (probably OM SOAPEnvelope)
	 * @throws MessageException
	 */
	public OMElement getAsOMElement() throws MessageException;
	
	/**
	 * getAsBlock
	 * Get the entire xml part as a Block
	 * The BlockFactory and object context are passed in to help create the 
	 * proper kind of block.
	 * 
	 * @return Block
	 * @throws MessageException
	 */
	public Block getAsBlock(
			Object context, 
			BlockFactory blockFactory)  
		throws MessageException, XMLStreamException;
	
	/**
	 * getNumBodyBlocks
	 * @return number of body blocks
	 * @throws MessageException
	 */
	public int getNumBodyBlocks() throws MessageException;
	
	/**
	 * getBodyBlock
	 * Get the body block as the specificed index.
	 * The BlockFactory and object context are passed in to help create the 
	 * proper kind of block.
	 * 
	 * @param index
	 * @param context
	 * @param blockFactory
	 * @return Block
	 * @throws MessageException
	 */
	public Block getBodyBlock(int index, Object context, BlockFactory blockFactory)  
		throws MessageException;
	
	/**
	 * setBodyBlock
	 * Set the block at the specified index
	 * Once set, the Message owns the block.  You must
	 * use the getBodyBlock method to access it.
	 * @param index
	 * @param block
	 * @throws MessageException
	 */
	public void setBodyBlock(int index, Block block) throws MessageException;
	
	/**
	 * removePayload
	 * Removes the indicated BodyBlock
	 * @param index
	 * @throws MessageException
	 */
	public void removeBodyBlock(int index) throws MessageException;
	
	
	/**
	 * getNumHeaderBlocks
	 * @return number of header blocks
	 * @throws MessageException
	 */
	public int getNumHeaderBlocks() throws MessageException;
	
	/**
	 * getHeaderBlock
	 * Get the header block with the specified name
	 * The BlockFactory and object context are passed in to help create the 
	 * proper kind of block.
	 * 
	 * @param namespace
	 * @param localPart
	 * @param context
	 * @param blockFactory
	 * @return Block
	 * @throws MessageException
	 */
	public Block getHeaderBlock(String namespace, String localPart, 
			Object context, 
			BlockFactory blockFactory)  
		throws MessageException;
	
	/**
	 * appendHeaderBlock
	 * Append the block to the list of header blocks.
	 * The Message owns the block.  You must
	 * use the getHeaderBlock method to access it.
	 * @param namespace
	 * @param localPart
	 * @param block
	 * @throws MessageException
	 */
	public void setHeaderBlock(String namespace, String localPart, Block block) 
		throws MessageException;
	
	/**
	 * removePayload
	 * Removes the indicated block
	 * @param namespace
	 * @param localPart
	 * @throws MessageException
	 */
	public void removeHeaderBlock(String namespace, String localPart) 
		throws MessageException;
	
	
	/**
	 * Get a traceString...the trace string dumps the contents of the Block without forcing an underlying
	 * ill-performant transformation of the message.
	 * @boolean indent String containing indent characters
	 * @return String containing trace information
	 */
	public String traceString(String indent);
    
}
