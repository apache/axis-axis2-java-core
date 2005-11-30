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

import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMDocument;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.dom.util.XMLChar;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import java.util.Hashtable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class DocumentImpl extends ParentNode implements Document, OMDocument {

	
    protected Hashtable identifiers;
    
    private String xmlVersion;
    
    private String charEncoding;
    
	/**
	 * @param ownerDocument
	 */
	public DocumentImpl(DocumentImpl ownerDocument) {
		super(ownerDocument);
	}
	
	public DocumentImpl(OMXMLParserWrapper parserWrapper) {
		this.builder = parserWrapper;
	}

	public DocumentImpl() {
		
	}
	
	///
	///OMNode methods
	////
	public void setType(int nodeType) throws OMException {
		throw new UnsupportedOperationException("In OM Document object doesn't have a type");
	}
	public int getType() throws OMException {
		throw new UnsupportedOperationException("In OM Document object doesn't have a type");
	}
	
	public void serialize(OMOutputImpl omOutput) throws XMLStreamException {
		// TODO Auto-generated method stub
	}	

	///
	///Override ChildNode specific methods
	///
	public OMNode getNextOMSibling() throws OMException {
		throw new UnsupportedOperationException("This is the document node");
	}
	public Node getNextSibling() {
		throw new UnsupportedOperationException("This is the document node");
	}
	public OMContainer getParent() throws OMException {
		throw new UnsupportedOperationException("This is the document node");
	}
	public OMNode getPreviousOMSibling() {
		throw new UnsupportedOperationException("This is the document node");
	}
	public Node getPreviousSibling() {
		throw new UnsupportedOperationException("This is the document node");
	}
	public void setNextOMSibling(OMNode node) {
		throw new UnsupportedOperationException("This is the document node");
	}
	public void setParent(OMContainer element) {
		throw new UnsupportedOperationException("This is the document node");
	}
	public void setPreviousOMSibling(OMNode node) {
		throw new UnsupportedOperationException("This is the document node");
	}
	
	
	
	///
	///org.w3c.dom.Node methods
	///
	public String getNodeName() {
		return "#document";
	}
	public short getNodeType() {
		return Node.DOCUMENT_NODE;
	}
	
	///org.w3c.dom.Document methods
	///
	
	public Attr createAttribute(String name) throws DOMException {
		if(!DOMUtil.isValidChras(name)) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
		}
		return new AttrImpl(this,name);
	}
	
	public Attr createAttributeNS(String namespaceURI, String qualifiedName)
			throws DOMException {
		
		String localName = DOMUtil.getLocalName(qualifiedName);
		String prefix = DOMUtil.getPrefix(qualifiedName);
		
		this.checkQName(prefix,localName);
		
		return new AttrImpl(this,localName,
				new NamespaceImpl(namespaceURI, prefix));
	}
	
	public CDATASection createCDATASection(String arg0) throws DOMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
	
	public Comment createComment(String arg0) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
	
	public DocumentFragment createDocumentFragment() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
	
	public Element createElement(String tagName) throws DOMException {
		return new ElementImpl(this, tagName);
	}
	
	public Element createElementNS(String ns, String qualifiedName) 
			throws DOMException {
		
		String localName = DOMUtil.getLocalName(qualifiedName);
		String prefix = DOMUtil.getPrefix(qualifiedName);
		
		this.checkQName(prefix,localName);
		
		NamespaceImpl namespace = new NamespaceImpl(ns, prefix);
		return new ElementImpl(this, localName, namespace);
	}
	
	public EntityReference createEntityReference(String arg0)
			throws DOMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
	public ProcessingInstruction createProcessingInstruction(String arg0,
			String arg1) throws DOMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
	public Text createTextNode(String value) {
		return new TextImpl(this, value);
	}
	public DocumentType getDoctype() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
	
	public Element getElementById(String arg0) {
		//TODO getElementById
		throw new UnsupportedOperationException("TODO: getElementById");
	}
	public NodeList getElementsByTagName(String arg0) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
	public NodeList getElementsByTagNameNS(String arg0, String arg1) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
	public DOMImplementation getImplementation() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
	public Node importNode(Node arg0, boolean arg1) throws DOMException {
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
	///OMDocument Methods
	///
	public String getCharsetEncoding() {
		return this.charEncoding;
	}

	public String getXMLVersion() {
		return this.xmlVersion;
	}

	public String isStandalone() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void serialize(OMOutputImpl omOutput, boolean includeXMLDeclaration) throws XMLStreamException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void serializeAndConsume(OMOutputImpl omOutput, boolean includeXMLDeclaration) throws XMLStreamException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void setCharsetEncoding(String charsetEncoding) {
		this.charEncoding = charsetEncoding;
	}

	public void setOMDocumentElement(OMElement rootElement) {
		this.firstChild = (ElementImpl)rootElement;
	}

	public void setStandalone(String isStandalone) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void setXMLVersion(String version) {
		this.xmlVersion = version;
	}
	
	/**
	 * Returns the document element
	 * @see org.apache.axis2.om.OMDocument#getOMDocumentElement()
	 */
	public OMElement getOMDocumentElement() {
		/*
		 * We'r sure that only an element can be the first child 
		 * of a Document
		 */
		if(this.firstChild == null && !this.done) {
			this.build();
		}
		return (OMElement)this.firstChild;
	}
	
	/**
	 * Returns the document element
	 * @see org.w3c.dom.Document#getDocumentElement()
	 */
	public Element getDocumentElement() {
		return (Element)this.firstChild;
	}
	
    /**
     * Borrowed from the Xerces impl
     * 
     * Checks if the given qualified name is legal with respect
     * to the version of XML to which this document must conform.
     *
     * @param prefix prefix of qualified name
     * @param local local part of qualified name
     */
    protected final void checkQName(String prefix, String local) {

		// check that both prefix and local part match NCName
        boolean validNCName = (prefix == null || XMLChar.isValidNCName(prefix))
                && XMLChar.isValidNCName(local);


        if (!validNCName) {
            // REVISIT: add qname parameter to the message
            String msg =
            DOMMessageFormatter.formatMessage(
            DOMMessageFormatter.DOM_DOMAIN,
            "INVALID_CHARACTER_ERR",
            null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
        }
    }
    
    /*
     * DOM-Level 3 methods 
     */

	public Node adoptNode(Node arg0) throws DOMException {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	public String getDocumentURI() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	public DOMConfiguration getDomConfig() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	public String getInputEncoding() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	public boolean getStrictErrorChecking() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	public String getXmlEncoding() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	public boolean getXmlStandalone() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	public String getXmlVersion() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void normalizeDocument() {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	public Node renameNode(Node arg0, String arg1, String arg2) throws DOMException {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void setDocumentURI(String arg0) {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void setStrictErrorChecking(boolean arg0) {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void setXmlStandalone(boolean arg0) throws DOMException {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void setXmlVersion(String arg0) throws DOMException {
		// TODO TODO
		throw new UnsupportedOperationException("TODO");
	}

}
