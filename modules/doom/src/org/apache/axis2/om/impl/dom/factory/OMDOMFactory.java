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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMComment;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMDocType;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMProcessingInstruction;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.OMNodeEx;
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

    public OMDOMFactory() {
    }

    public OMDOMFactory(DocumentImpl doc) {
        this.document = doc;
    }

    public OMDocument createOMDocument() {
        if (this.document == null)
            this.document = new DocumentImpl(this);

        return this.document;
    }

    /**
     * Configure this factory to use the given document. Use with care.
     * 
     * @param document
     */
    public void setDocument(DocumentImpl document) {
        this.document = document;
    }

    public OMElement createOMElement(String localName, OMNamespace ns) {
        return new ElementImpl((DocumentImpl) this.createOMDocument(),
                localName, (NamespaceImpl) ns, this);
    }

    public OMElement createOMElement(String localName, OMNamespace ns,
            OMContainer parent) throws OMDOMException {
        switch (((ParentNode) parent).getNodeType()) {
        case Node.ELEMENT_NODE: // We are adding a new child to an elem
            ElementImpl parentElem = (ElementImpl) parent;
            ElementImpl elem = new ElementImpl((DocumentImpl) parentElem
                    .getOwnerDocument(), localName, (NamespaceImpl) ns, this);
            parentElem.appendChild(elem);
            return elem;

        case Node.DOCUMENT_NODE:
            DocumentImpl docImpl = (DocumentImpl) parent;
            ElementImpl elem2 = new ElementImpl(docImpl, localName,
                    (NamespaceImpl) ns, this);
            return elem2;

        case Node.DOCUMENT_FRAGMENT_NODE:
            DocumentFragmentimpl docFragImpl = (DocumentFragmentimpl) parent;
            ElementImpl elem3 = new ElementImpl((DocumentImpl) docFragImpl
                    .getOwnerDocument(), localName, (NamespaceImpl) ns, this);
            return elem3;
        default:
            throw new OMDOMException(
                    "The parent container can only be an ELEMENT, DOCUMENT " +
                    "or a DOCUMENT FRAGMENT");
        }
    }

    /**
     * Creates an OMElement with the builder.
     */
    public OMElement createOMElement(String localName, OMNamespace ns,
            OMContainer parent, OMXMLParserWrapper builder) {
        switch (((ParentNode) parent).getNodeType()) {
        case Node.ELEMENT_NODE: // We are adding a new child to an elem
            ElementImpl parentElem = (ElementImpl) parent;
            ElementImpl elem = new ElementImpl((DocumentImpl) parentElem
                    .getOwnerDocument(), localName, (NamespaceImpl) ns,
                    builder, this);
            parentElem.appendChild(elem);
            return elem;
        case Node.DOCUMENT_NODE:
            DocumentImpl docImpl = (DocumentImpl) parent;
            ElementImpl elem2 = new ElementImpl(docImpl, localName,
                    (NamespaceImpl) ns, builder, this);
            docImpl.appendChild(elem2);
            return elem2;

        case Node.DOCUMENT_FRAGMENT_NODE:
            DocumentFragmentimpl docFragImpl = (DocumentFragmentimpl) parent;
            ElementImpl elem3 = new ElementImpl((DocumentImpl) docFragImpl
                    .getOwnerDocument(), localName, (NamespaceImpl) ns,
                    builder, this);
            return elem3;
        default:
            throw new OMDOMException(
                    "The parent container can only be an ELEMENT, DOCUMENT " +
                    "or a DOCUMENT FRAGMENT");
        }
    }

    /**
     * Creates an OMElement.
     * 
     * @see org.apache.axiom.om.OMFactory#createOMElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public OMElement createOMElement(String localName, String namespaceURI,
            String namespacePrefix) {
        NamespaceImpl ns = new NamespaceImpl(namespaceURI, namespacePrefix,
                this);
        return this.createOMElement(localName, ns);
    }

    /**
     * Creates a new OMDOM Element node and adds it to the given parent.
     * 
     * @see #createOMElement(String, OMNamespace, OMContainer)
     * @see org.apache.axiom.om.OMFactory#createOMElement(
     * javax.xml.namespace.QName, org.apache.axiom.om.OMContainer)
     */
    public OMElement createOMElement(QName qname, OMContainer parent)
            throws OMException {
        NamespaceImpl ns;
        if (qname.getPrefix() != null) {
            ns = new NamespaceImpl(qname.getNamespaceURI(), qname.getPrefix(), this);
        } else {
            ns = new NamespaceImpl(qname.getNamespaceURI(), this);
        }
        return createOMElement(qname.getLocalPart(), ns, parent);
    }

    /**
     * Creates a new OMNamespace.
     * 
     * @see org.apache.axiom.om.OMFactory#createOMNamespace(java.lang.String,
     *      java.lang.String)
     */
    public OMNamespace createOMNamespace(String uri, String prefix) {
        return new NamespaceImpl(uri, prefix, this);
    }

    /**
     * Creates a new OMDOM Text node with the given value and appends it to the
     * given parent element.
     * 
     * @see org.apache.axiom.om.OMFactory#createText(
     *      org.apache.axiom.om.OMElement,java.lang.String)
     */
    public OMText createText(OMElement parent, String text) {
        ElementImpl parentElem = (ElementImpl) parent;
        TextImpl txt = new TextImpl((DocumentImpl) parentElem
                .getOwnerDocument(), text, this);
        parentElem.addChild(txt);
        return txt;
    }

    public OMText createText(OMElement parent, String text, int type) {
        OMText textNode = createText(parent, text);
        ((OMNodeEx) textNode).setType(type);
        return textNode;
    }

    /**
     * Creates a OMDOM Text node carrying the given value.
     * 
     * @see org.apache.axiom.om.OMFactory#createText(java.lang.String)
     */
    public OMText createText(String s) {
        return new TextImpl(s, this);
    }

    /**
     * Creates a Character node of the given type.
     * 
     * @see org.apache.axiom.om.OMFactory#createText(java.lang.String, int)
     */
    public OMText createText(String text, int type) {
        switch (type) {
        case Node.TEXT_NODE:
            return new TextImpl(text, this);
        default:
            throw new OMDOMException("Only Text nodes are supported right now");
        }
    }

    /**
     * Creates a new OMDOM Text node with the value of the given text value
     * along with the MTOM optimization parameters and returns it.
     * 
     * @see org.apache.axiom.om.OMFactory#createText(java.lang.String,
     *      java.lang.String, boolean)
     */
    public OMText createText(String text, String mimeType, boolean optimize) {
        return new TextImpl(text, mimeType, optimize, this);
    }

    /**
     * Creates a new OMDOM Text node with the given datahandler and the given
     * MTOM optimization configuration and returns it.
     * 
     * @see org.apache.axiom.om.OMFactory#createText(java.lang.Object, boolean)
     */
    public OMText createText(Object dataHandler, boolean optimize) {
        return new TextImpl(dataHandler, optimize, this);
    }

    /**
     * Creates an OMDOM Text node, adds it to the give parent element and
     * returns it.
     * 
     * @see org.apache.axiom.om.OMFactory#createText(org.apache.axiom.om.OMElement,
     *      java.lang.String, java.lang.String, boolean)
     */
    public OMText createText(OMElement parent, String s, String mimeType,
            boolean optimize) {
        TextImpl text = new TextImpl((DocumentImpl) ((ElementImpl) parent)
                .getOwnerDocument(), s, mimeType, optimize, this);
        parent.addChild(text);
        return text;
    }

    public OMText createText(String contentID, OMElement parent,
            OMXMLParserWrapper builder) {
        TextImpl text = new TextImpl(contentID, parent, builder, this);
        parent.addChild(text);
        return text;
    }

    public OMAttribute createOMAttribute(String localName, OMNamespace ns,
            String value) {
        return new AttrImpl(this.getDocument(), localName, ns, value, this);
    }

    public OMDocType createOMDocType(OMContainer parent, String content) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public OMProcessingInstruction createOMProcessingInstruction(
            OMContainer parent, String piTarget, String piData) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public OMComment createOMComment(OMContainer parent, String content) {
        DocumentImpl doc = null;
        if (parent instanceof DocumentImpl) {
            doc = (DocumentImpl) parent;
        } else {
            doc = (DocumentImpl) ((ParentNode) parent).getOwnerDocument();
        }

        CommentImpl comment = new CommentImpl(doc, content, this);
        parent.addChild(comment);
        return comment;
    }

    public DocumentImpl getDocument() {
        return (DocumentImpl) this.createOMDocument();
    }

    public OMDocument createOMDocument(OMXMLParserWrapper builder) {
        this.document = new DocumentImpl(builder, this);
        return this.document;
    }

}
