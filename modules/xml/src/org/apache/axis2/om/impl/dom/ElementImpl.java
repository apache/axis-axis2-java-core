package org.apache.axis2.om.impl.dom;

import org.apache.axis2.om.OMException;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class ElementImpl extends ParentNode implements Element {
	
	private NamespaceImpl namespace;
	private String tagName;
	
	/**
	 * @param ownerDocument
	 */
	public ElementImpl(DocumentImpl ownerDocument, String tagName) {
		super(ownerDocument);
		this.tagName = tagName;
	}
	
	/**
	 * Create a  new element with the 
	 * @param ownerDocument
	 * @param tagName
	 * @param ns
	 */
	public ElementImpl(DocumentImpl ownerDocument, String tagName, NamespaceImpl ns) {
		super(ownerDocument);
		this.tagName = tagName;
		this.namespace = ns;
	}
	
	
	///
	///org.w3c.dom.Node methods
	///
	
	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	public short getNodeType() {
		return Node.ELEMENT_NODE;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getNodeName()
	 */
	public String getNodeName() {
		return this.tagName;
	}

	/**
	 * Returns the value of the namespace URI
	 */
	public String getNamespaceURI() {
		return this.namespace.getName();
	}
	
	///
	///org.apache.axis2.om.OMNode methods
	///
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#getType()
	 */
	public int getType() throws OMException {
		return Node.ELEMENT_NODE;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#setType(int)
	 */
	public void setType(int nodeType) throws OMException {
		//Do nothing ...
		//This is an Eement Node...
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#serialize(org.apache.axis2.om.OMOutput)
	 */
	public void serialize(OMOutputImpl omOutput) throws XMLStreamException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	
	///
	/// org.w3c.dom.Element methods
	///
	
	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#getTagName()
	 */
	public String getTagName() {
		return this.tagName;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) throws DOMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#hasAttribute(java.lang.String)
	 */
	public boolean hasAttribute(String arg0) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#getAttribute(java.lang.String)
	 */
	public String getAttribute(String arg0) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#removeAttributeNS(java.lang.String, java.lang.String)
	 */
	public void removeAttributeNS(String arg0, String arg1) throws DOMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#setAttribute(java.lang.String, java.lang.String)
	 */
	public void setAttribute(String arg0, String arg1) throws DOMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#hasAttributeNS(java.lang.String, java.lang.String)
	 */
	public boolean hasAttributeNS(String arg0, String arg1) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#getAttributeNode(java.lang.String)
	 */
	public Attr getAttributeNode(String arg0) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#removeAttributeNode(org.w3c.dom.Attr)
	 */
	public Attr removeAttributeNode(Attr arg0) throws DOMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#setAttributeNode(org.w3c.dom.Attr)
	 */
	public Attr setAttributeNode(Attr arg0) throws DOMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#setAttributeNodeNS(org.w3c.dom.Attr)
	 */
	public Attr setAttributeNodeNS(Attr arg0) throws DOMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#getElementsByTagName(java.lang.String)
	 */
	public NodeList getElementsByTagName(String arg0) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#getAttributeNS(java.lang.String, java.lang.String)
	 */
	public String getAttributeNS(String arg0, String arg1) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#setAttributeNS(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void setAttributeNS(String arg0, String arg1, String arg2) throws DOMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#getAttributeNodeNS(java.lang.String, java.lang.String)
	 */
	public Attr getAttributeNodeNS(String arg0, String arg1) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#getElementsByTagNameNS(java.lang.String, java.lang.String)
	 */
	public NodeList getElementsByTagNameNS(String arg0, String arg1) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
	

	public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void serializeAndConsume(OMOutputImpl omOutput) throws XMLStreamException {
		//TODO
		throw new UnsupportedOperationException("TODO");// TODO Auto-generated method stub
		
	}

	public void serializeAndConsume(XMLStreamWriter xmlWriter) throws XMLStreamException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
}
