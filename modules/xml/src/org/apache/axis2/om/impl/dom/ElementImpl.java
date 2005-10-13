/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.apache.axis2.om.impl.dom;

import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class ElementImpl extends ParentNode implements Element,OMElement {
	
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
	 * Create a  new element with the namespace
	 * @param ownerDocument
	 * @param tagName
	 * @param ns
	 */
	public ElementImpl(DocumentImpl ownerDocument, String tagName, NamespaceImpl ns) {
		super(ownerDocument);
		this.tagName = tagName;
		this.namespace = ns;
	}
	
	public ElementImpl(DocumentImpl ownerDocument, String tagName, NamespaceImpl ns, OMXMLParserWrapper builder) {
		super(ownerDocument);
		this.tagName = tagName;
		this.namespace = ns;
		this.builder = builder;
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
		throw new UnsupportedOperationException("TODO");
	}

	public void serializeAndConsume(XMLStreamWriter xmlWriter) throws XMLStreamException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	
	///
	///OmElement methods
	///

	/**
	 * @see org.apache.axis2.om.OMElement#addAttribute(org.apache.axis2.om.OMAttribute)
	 */
	public OMAttribute addAttribute(OMAttribute attr) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/**
	 * @see org.apache.axis2.om.OMElement#addAttribute(java.lang.String, java.lang.String, org.apache.axis2.om.OMNamespace)
	 */
	public OMAttribute addAttribute(String attributeName, String value, OMNamespace ns) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/**
	 * @see org.apache.axis2.om.OMElement#declareNamespace(org.apache.axis2.om.OMNamespace)
	 */
	public OMNamespace declareNamespace(OMNamespace namespace) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/**
	 * @see org.apache.axis2.om.OMElement#declareNamespace(java.lang.String, java.lang.String)
	 */
	public OMNamespace declareNamespace(String uri, String prefix) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/**
	 * @see org.apache.axis2.om.OMElement#findNamespace(java.lang.String, java.lang.String)
	 */
	public OMNamespace findNamespace(String uri, String prefix) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/**
	 * @see org.apache.axis2.om.OMElement#getAllAttributes()
	 */
	public Iterator getAllAttributes() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/**
	 * @see org.apache.axis2.om.OMElement#getAllDeclaredNamespaces()
	 */
	public Iterator getAllDeclaredNamespaces() throws OMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#getAttribute(javax.xml.namespace.QName)
	 */
	public OMAttribute getAttribute(QName qname) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#getBuilder()
	 */
	public OMXMLParserWrapper getBuilder() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#getChildElements()
	 */
	public Iterator getChildElements() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#getFirstAttribute(javax.xml.namespace.QName)
	 */
	public OMAttribute getFirstAttribute(QName qname) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#getFirstElement()
	 */
	public OMElement getFirstElement() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#getNamespace()
	 */
	public OMNamespace getNamespace() throws OMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#getQName()
	 */
	public QName getQName() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#getText()
	 */
	public String getText() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#getXMLStreamReader()
	 */
	public XMLStreamReader getXMLStreamReader() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#getXMLStreamReaderWithoutCaching()
	 */
	public XMLStreamReader getXMLStreamReaderWithoutCaching() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#removeAttribute(org.apache.axis2.om.OMAttribute)
	 */
	public void removeAttribute(OMAttribute attr) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#setBuilder(org.apache.axis2.om.OMXMLParserWrapper)
	 */
	public void setBuilder(OMXMLParserWrapper wrapper) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#setLocalName(java.lang.String)
	 */
	public void setLocalName(String localName) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#setNamespace(org.apache.axis2.om.OMNamespace)
	 */
	public void setNamespace(OMNamespace namespace) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#setText(java.lang.String)
	 */
	public void setText(String text) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
	
}