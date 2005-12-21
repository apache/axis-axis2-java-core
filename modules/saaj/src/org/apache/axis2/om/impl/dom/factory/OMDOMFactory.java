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
package org.apache.axis2.om.impl.dom.factory;

import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMComment;
import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMDocType;
import org.apache.axis2.om.OMDocument;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMProcessingInstruction;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.dom.AttrImpl;
import org.apache.axis2.om.impl.dom.CommentImpl;
import org.apache.axis2.om.impl.dom.DocumentFragmentimpl;
import org.apache.axis2.om.impl.dom.DocumentImpl;
import org.apache.axis2.om.impl.dom.ElementImpl;
import org.apache.axis2.om.impl.dom.NamespaceImpl;
import org.apache.axis2.om.impl.dom.OMDOMException;
import org.apache.axis2.om.impl.dom.ParentNode;
import org.apache.axis2.om.impl.dom.TextImpl;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;

public class OMDOMFactory implements OMFactory {
	
	protected DocumentImpl document;
	
	public OMDOMFactory() {}
	
	public OMDOMFactory(DocumentImpl doc) {
		this.document = doc;
	}
	
	public OMDocument createOMDocument() {
		if(this.document == null)
			this.document = new DocumentImpl();
		
		return this.document;
	}

	/**
	 * Configure this factory to use the given document
	 * Use with care :-)
	 * @param document
	 */
	public void setDocument(DocumentImpl document) {
		this.document = document;
	}
	
	public OMElement createOMElement(String localName, OMNamespace ns) {
		return new ElementImpl((DocumentImpl)this.createOMDocument(), localName, (NamespaceImpl)ns);
	}

	public OMElement createOMElement(String localName, OMNamespace ns, OMContainer parent) throws OMDOMException{
		switch(((ParentNode)parent).getNodeType()) {
			case Node.ELEMENT_NODE : // We are adding a new child to an elem
				ElementImpl parentElem = (ElementImpl)parent;
				ElementImpl elem = new ElementImpl((DocumentImpl)parentElem.getOwnerDocument(),localName,(NamespaceImpl)ns);
				parentElem.appendChild(elem);
				return elem;
				
			case Node.DOCUMENT_NODE :
				DocumentImpl docImpl = (DocumentImpl) parent;
				ElementImpl elem2 = new ElementImpl(docImpl,localName,(NamespaceImpl)ns);
				return elem2;
				
			case Node.DOCUMENT_FRAGMENT_NODE :
				DocumentFragmentimpl docFragImpl = (DocumentFragmentimpl)parent;
				ElementImpl elem3 = new ElementImpl((DocumentImpl)docFragImpl.getOwnerDocument(),localName, (NamespaceImpl)ns);
				return elem3;
			default:
				throw new OMDOMException("The parent container can only be an ELEMENT, DOCUMENT or a DOCUMENT FRAGMENT");
		}
	}

	/**
	 * Creating an OMElement with the builder
	 */
	public OMElement createOMElement(String localName, OMNamespace ns, OMContainer parent, OMXMLParserWrapper builder) {
		switch(((ParentNode)parent).getNodeType()) {
			case Node.ELEMENT_NODE: // We are adding a new child to an elem
				ElementImpl parentElem = (ElementImpl) parent;
				ElementImpl elem = new ElementImpl((DocumentImpl) parentElem
						.getOwnerDocument(), localName, (NamespaceImpl) ns, builder);
				parentElem.appendChild(elem);
				return elem;
			case Node.DOCUMENT_NODE:
				DocumentImpl docImpl = (DocumentImpl) parent;
				ElementImpl elem2 = new ElementImpl(docImpl, localName,
						(NamespaceImpl) ns, builder);
				docImpl.appendChild(elem2);
				return elem2;
	
			case Node.DOCUMENT_FRAGMENT_NODE:
				DocumentFragmentimpl docFragImpl = (DocumentFragmentimpl) parent;
				ElementImpl elem3 = new ElementImpl((DocumentImpl) docFragImpl
						.getOwnerDocument(), localName, (NamespaceImpl) ns, builder);
				return elem3;
			default:
				throw new OMDOMException(
						"The parent container can only be an ELEMENT, DOCUMENT or a DOCUMENT FRAGMENT");
		}
	}

	/**
	 * Create an OMElement
	 * @see org.apache.axis2.om.OMFactory#createOMElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public OMElement createOMElement(String localName, String namespaceURI, String namespacePrefix) {
		NamespaceImpl ns = new NamespaceImpl(namespaceURI,namespacePrefix);
		return this.createOMElement(localName, ns);
	}

	/**
	 * Create a new OMDOM Element node and add it to the given parent
	 * @see #createOMElement(String, OMNamespace, OMContainer)
	 * @see org.apache.axis2.om.OMFactory#createOMElement(javax.xml.namespace.QName, org.apache.axis2.om.OMContainer)
	 */
	public OMElement createOMElement(QName qname, OMContainer parent) throws OMException {
		NamespaceImpl ns;
		if(qname.getPrefix() != null) {
			ns = new NamespaceImpl(qname.getNamespaceURI(), qname.getPrefix());
		} else {
			ns = new NamespaceImpl(qname.getNamespaceURI());
		}
		return createOMElement(qname.getLocalPart(),ns,parent);
	}

	/**
	 * Create a new OMNamespace
	 * @see org.apache.axis2.om.OMFactory#createOMNamespace(java.lang.String, java.lang.String)
	 */
	public OMNamespace createOMNamespace(String uri, String prefix) {
		return new NamespaceImpl(uri,prefix);
	}

	/**
	 * Create a new OMDOM Text node with the given value and append it to the 
	 * given parent element
	 * @see org.apache.axis2.om.OMFactory#createText(org.apache.axis2.om.OMElement, java.lang.String)
	 */
	public OMText createText(OMElement parent, String text) {
			ElementImpl parentElem = (ElementImpl) parent;
			TextImpl txt = new TextImpl((DocumentImpl) parentElem.getOwnerDocument(), text);
			parentElem.addChild(txt);
			return txt;
	}

	/**
	 * Create a OMDOM Text node carrying the given value
	 * 
	 * @see org.apache.axis2.om.OMFactory#createText(java.lang.String)
	 */
	public OMText createText(String s) {
		return new TextImpl(s);
	}

	/**
	 * Create a Character node of the given type
	 * @see org.apache.axis2.om.OMFactory#createText(java.lang.String, int)
	 */
	public OMText createText(String text, int type) {
		switch (type) {
			case Node.TEXT_NODE:
				return new TextImpl(text);
			default:
				throw new OMDOMException("Only Text nodes are supported right now");
		}
	}

	/**
	 * Create a new OMDOM Text node with that has the value of the given text
	 * value along with the MTOM optimization parameters and return it
	 * @see org.apache.axis2.om.OMFactory#createText(java.lang.String, java.lang.String, boolean)
	 */
	public OMText createText(String text, String mimeType, boolean optimize) {
		return new TextImpl(text, mimeType, optimize);
	}

	/**
	 * Create a new OMDOM Text node with the given datahandler and the give 
	 * MTOM optimization configuration and return it
	 * @see org.apache.axis2.om.OMFactory#createText(java.lang.Object, boolean)
	 */
	public OMText createText(Object dataHandler, boolean optimize) {
		return new TextImpl(dataHandler, optimize);
	}

	/**
	 * Create an OMDOM Text node, add it to the give parent element and return it 
	 * @see org.apache.axis2.om.OMFactory#createText(org.apache.axis2.om.OMElement, java.lang.String, java.lang.String, boolean)
	 */
	public OMText createText(OMElement parent, String s, String mimeType, boolean optimize) {
		TextImpl text = new TextImpl((DocumentImpl)((ElementImpl)parent).getOwnerDocument(),s, mimeType, optimize);
		parent.addChild(text);
		return text;
	}

    public OMText createText(String contentID, OMElement parent,
            OMXMLParserWrapper builder) {
		TextImpl text = new TextImpl(contentID,parent,builder);
		parent.addChild(text);
		return text;
    }
	
	public OMAttribute createOMAttribute(String localName, OMNamespace ns, String value) {
		return new AttrImpl(this.getDocument() ,localName,ns, value);
	}

	public OMDocType createOMDocType(OMContainer parent, String content) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMProcessingInstruction createOMProcessingInstruction(OMContainer parent, String piTarget, String piData) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMComment createOMComment(OMContainer parent, String content) {
		DocumentImpl doc = null;
		if(parent instanceof DocumentImpl) {
			doc = (DocumentImpl)parent;
		} else {
			doc = (DocumentImpl)((ParentNode)parent).getOwnerDocument();
		}
		
		CommentImpl comment = new CommentImpl(doc, content);
		parent.addChild(comment);
		return comment;
	}
	
	public DocumentImpl getDocument() {
		return (DocumentImpl)this.createOMDocument();
	}


	public OMDocument createOMDocument(OMXMLParserWrapper builder) {
		this.document = new DocumentImpl(builder);
		return this.document;
	}
	
}
