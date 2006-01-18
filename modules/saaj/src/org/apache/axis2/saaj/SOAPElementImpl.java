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
package org.apache.axis2.saaj;

import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.dom.DocumentImpl;
import org.apache.axis2.om.impl.dom.ElementImpl;
import org.apache.axis2.om.impl.dom.TextImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


public class SOAPElementImpl extends NodeImplEx implements SOAPElement {


    /**
     * Using a delegate because we can't extend from
     * org.apache.axis2.om.impl.dom.ElementImpl since this class
     * must extend SNodeImpl
     */
    protected ElementImpl element;

    public SOAPElementImpl(ElementImpl element) {
        this.element = element;
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.om.OMNode#discard()
      */
    public void discard() throws OMException {
        this.element.discard();
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.om.OMNode#serialize(org.apache.axis2.om.impl.OMOutputImpl)
      */
    public void serialize(OMOutputImpl omOutput) throws XMLStreamException {
        this.element.serialize(omOutput);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.om.OMNode#serializeAndConsume(org.apache.axis2.om.impl.OMOutputImpl)
      */
    public void serializeAndConsume(OMOutputImpl omOutput) throws XMLStreamException {
        this.element.serializeAndConsume(omOutput);
    }

    /**
     * Adds an attribute with the specified name and value to this
     * <code>SOAPElement</code> object.
     * <p/>
     *
     * @param name  a <code>Name</code> object with the name of the attribute
     * @param value a <code>String</code> giving the value of the attribute
     * @return the <code>SOAPElement</code> object into which the attribute was
     *         inserted
     * @throws SOAPException if there is an error in creating the
     *                       Attribute
     */
    public SOAPElement addAttribute(Name name, String value) throws SOAPException {
        if (name.getURI() == null || name.getURI().trim().length() == 0) {
            this.element.setAttribute(name.getLocalName(), value);
        } else {
            this.element.setAttributeNS(name.getURI(), name.getPrefix() + ":" + name.getLocalName(), value);
        }
        return this;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#addChildElement(javax.xml.soap.Name)
      */
    public SOAPElement addChildElement(Name name) throws SOAPException {
        return this.addChildElement(name.getLocalName(), name.getPrefix(), name.getURI());
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#addChildElement(javax.xml.soap.SOAPElement)
      */
    public SOAPElement addChildElement(SOAPElement soapElement) throws SOAPException {
        this.element.appendChild(soapElement);
        return soapElement;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String, java.lang.String, java.lang.String)
      */
    public SOAPElement addChildElement(String localName, String prefix, String uri) throws SOAPException {
        this.element.declareNamespace(uri, prefix);
        return this.addChildElement(localName, prefix);
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String, java.lang.String)
      */
    public SOAPElement addChildElement(String localName, String prefix) throws SOAPException {
        String namespaceURI = this.getNamespaceURI(prefix);

        if (namespaceURI == null) {
            throw new SOAPException("Namespace not declared for the give prefix: " + prefix);
        }
        SOAPElementImpl elem =
                new SOAPElementImpl((ElementImpl) this.getOwnerDocument().createElementNS(namespaceURI,
                                                                                          localName));
        this.element.appendChild(elem.element);
        return elem;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String)
      */
    public SOAPElement addChildElement(String localName) throws SOAPException {
        SOAPElementImpl elem = new SOAPElementImpl((ElementImpl) this.getOwnerDocument().createElement(localName));
        this.element.appendChild(elem.element);
        return elem;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#addNamespaceDeclaration(java.lang.String, java.lang.String)
      */
    public SOAPElement addNamespaceDeclaration(String prefix, String uri) throws SOAPException {
        this.element.declareNamespace(prefix, uri);
        return this;
    }

    /**
     * Creates a new <code>Text</code> object initialized with the given
     * <code>String</code> and adds it to this <code>SOAPElement</code> object.
     *
     * @param text a <code>String</code> object with the textual content to be added
     * @return the <code>SOAPElement</code> object into which
     *         the new <code>Text</code> object was inserted
     * @throws SOAPException if there is an error in creating the
     *                       new <code>Text</code> object
     */
    public SOAPElement addTextNode(String text) throws SOAPException {
        //OmElement.setText() will remove all the other text nodes that it contains
        //Therefore create a text node and add it
        Text textNode = this.getOwnerDocument().createTextNode(text);
        this.element.appendChild(textNode);
        return this;
    }

    /**
     * Returns an iterator over all of the attribute names in
     * this <CODE>SOAPElement</CODE> object. The iterator can be
     * used to get the attribute names, which can then be passed to
     * the method <CODE>getAttributeValue</CODE> to retrieve the
     * value of each attribute.
     *
     * @return an iterator over the names of the attributes
     */
    public Iterator getAllAttributes() {
        final Iterator attribIter = this.element.getAllAttributes();
        Collection attribName = new ArrayList();
        Attr attr;
        while (attribIter.hasNext()) {
            attr = (Attr) attribIter.next();
            PrefixedQName qname;
            if (attr.getNamespaceURI() == null || attr.getNamespaceURI().trim().length() == 0) {
                qname = new PrefixedQName(attr.getNamespaceURI(),
                                          attr.getName(),
                                          attr.getPrefix());
            } else {
                qname = new PrefixedQName(attr.getNamespaceURI(),
                                          attr.getLocalName(),
                                          attr.getPrefix());
            }
            attribName.add(qname);
        }
        return attribName.iterator();
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#getAttributeValue(javax.xml.soap.Name)
      */
    public String getAttributeValue(Name name) {
        //This method is waiting on the finalization of the name for a method
        //in OMElement that returns a OMAttribute from an input QName
        final OMAttribute attribute = this.element.getAttribute(new QName(name.getURI(),
                                                                          name.getLocalName(),
                                                                          name.getPrefix()));
        return attribute.getAttributeValue();
    }

    /**
     * Returns an iterator over all the immediate content of
     * this element. This includes <CODE>Text</CODE> objects as well
     * as <CODE>SOAPElement</CODE> objects.
     *
     * @return an iterator over <CODE>Text</CODE> and <CODE>SOAPElement</CODE>
     *         contained within this <CODE>SOAPElement</CODE> object
     */
    public Iterator getChildElements() {
        //Actually all the children are being treated as OMNodes and are being
        //wrapped accordingly to a single type (SOAPElement) and being returned in an iterator.
        //Text nodes and element nodes are all being treated alike here. Is that a serious issue???

        Iterator childIter = this.element.getChildren();
        Collection childElements = new ArrayList();
        while (childIter.hasNext()) {
            Object o = childIter.next();
            if (o instanceof Text) {
                childElements.add(new TextImplEx(((Text) o).getData()));
            } else {
                childElements.add(new SOAPElementImpl((ElementImpl) o));
            }
        }
        return childElements.iterator();
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#getChildElements(javax.xml.soap.Name)
      */
    public Iterator getChildElements(Name name) {
        QName qName = new QName(name.getURI(), name.getLocalName());
        Iterator childIter = this.element.getChildrenWithName(qName);
        ArrayList arrayList = new ArrayList();
        while (childIter.hasNext()) {
            Object o = childIter.next();
            if (o instanceof javax.xml.soap.Node) {
                arrayList.add(o);
            }
        }
        return arrayList.iterator();
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#getElementName()
      */
    public Name getElementName() {
        QName qName = this.element.getQName();
        return new PrefixedQName(qName.getNamespaceURI(),
                                 qName.getLocalPart(),
                                 qName.getPrefix());
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#getEncodingStyle()
      */
    public String getEncodingStyle() {
        return ((DocumentImpl) this.getOwnerDocument()).getCharsetEncoding();
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#getNamespacePrefixes()
      */
    public Iterator getNamespacePrefixes() {
        //Get all declared namespace, make a list of their prefixes and return an iterator over that list
        ArrayList prefixList = new ArrayList();
        Iterator nsIter = this.element.getAllDeclaredNamespaces();
        while (nsIter.hasNext()) {
            Object o = nsIter.next();
            if (o instanceof org.apache.axis2.om.OMNamespace) {
                org.apache.axis2.om.OMNamespace ns = (org.apache.axis2.om.OMNamespace) o;
                prefixList.add(ns.getPrefix());
            }
        }
        return prefixList.iterator();
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#getNamespaceURI(java.lang.String)
      */
    public String getNamespaceURI(String prefix) {
        return this.element.getNamespaceURI(prefix);
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#getVisibleNamespacePrefixes()
      */
    public Iterator getVisibleNamespacePrefixes() {
        //I'll recursively return all the declared namespaces till this node, including its parents etc.
        Iterator namespacesIter = this.element.getAllDeclaredNamespaces();
        ArrayList returnList = new ArrayList();
        while (namespacesIter.hasNext()) {
            Object o = namespacesIter.next();
            if (o instanceof OMNamespace) {
                OMNamespace ns = (OMNamespace) o;
                if (ns.getPrefix() != null) {
                    returnList.add(ns.getPrefix());
                }
            }
        }
        //taken care of adding namespaces of this node.
        //now we have to take care of adding the namespaces that are in the scope till the level of
        //this nodes' parent.
        org.apache.axis2.om.OMContainer parent = this.element.getParent();
        if (parent != null && parent instanceof org.apache.axis2.om.OMElement) {
            Iterator parentScopeNamespacesIter = ((org.apache.axis2.om.OMElement) parent).getAllDeclaredNamespaces();
            while (parentScopeNamespacesIter.hasNext()) {
                Object o = parentScopeNamespacesIter.next();
                if (o instanceof OMNamespace) {
                    OMNamespace ns = (OMNamespace) o;
                    if (ns.getPrefix() != null) {
                        returnList.add(ns.getPrefix());
                    }
                }
            }
        }
        return returnList.iterator();
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#removeAttribute(javax.xml.soap.Name)
      */
    public boolean removeAttribute(Name name) {
        org.apache.axis2.om.OMAttribute attr = element.getAttribute(new QName(name.getURI(),
                                                                              name.getLocalName(),
                                                                              name.getPrefix()));
        if (attr != null) {
            this.element.removeAttribute(attr);
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#removeContents()
      */
    public void removeContents() {
        //We will get all the children and iteratively call the detach() on all of 'em.
        Iterator childIter = this.element.getChildren();

        while (childIter.hasNext()) {
            Object o = childIter.next();
            if (o instanceof org.apache.axis2.om.OMNode) {
                ((org.apache.axis2.om.OMNode) o).detach();
            }
        }
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#removeNamespaceDeclaration(java.lang.String)
      */
    public boolean removeNamespaceDeclaration(String prefix) {
        return this.element.removeNamespace(prefix);
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#setEncodingStyle(java.lang.String)
      */
    public void setEncodingStyle(String encodingStyle) throws SOAPException {
        ((DocumentImpl) this.getOwnerDocument()).setCharsetEncoding(encodingStyle);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.om.impl.OMNodeEx#setParent(org.apache.axis2.om.OMContainer)
      */
    public void setParent(OMContainer parentElement) {
        this.element.setParent(parentElement);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#getAttribute(java.lang.String)
      */
    public String getAttribute(String name) {
        return this.element.getAttribute(name);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#getAttributeNode(java.lang.String)
      */
    public Attr getAttributeNode(String name) {
        return this.element.getAttributeNode(name);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#getAttributeNodeNS(java.lang.String, java.lang.String)
      */
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        return this.element.getAttributeNodeNS(namespaceURI, localName);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#getAttributeNS(java.lang.String, java.lang.String)
      */
    public String getAttributeNS(String namespaceURI, String localName) {
        return this.element.getAttributeNS(namespaceURI, localName);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#getElementsByTagName(java.lang.String)
      */
    public NodeList getElementsByTagName(String name) {
        return this.element.getElementsByTagName(name);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#getElementsByTagNameNS(java.lang.String, java.lang.String)
      */
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return this.element.getElementsByTagNameNS(namespaceURI, localName);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#getTagName()
      */
    public String getTagName() {
        return this.element.getTagName();
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#hasAttribute(java.lang.String)
      */
    public boolean hasAttribute(String name) {
        return this.element.hasAttribute(name);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#hasAttributeNS(java.lang.String, java.lang.String)
      */
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return this.element.hasAttributeNS(namespaceURI, localName);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#removeAttribute(java.lang.String)
      */
    public void removeAttribute(String name) throws DOMException {
        this.element.removeAttribute(name);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#removeAttributeNode(org.w3c.dom.Attr)
      */
    public Attr removeAttributeNode(Attr attr) throws DOMException {
        return this.element.removeAttributeNode(attr);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#removeAttributeNS(java.lang.String, java.lang.String)
      */
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        this.element.removeAttributeNS(namespaceURI, localName);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#setAttribute(java.lang.String, java.lang.String)
      */
    public void setAttribute(String name, String value) throws DOMException {
        this.element.setAttribute(name, value);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#setAttributeNode(org.w3c.dom.Attr)
      */
    public Attr setAttributeNode(Attr attr) throws DOMException {
        return this.element.setAttributeNode(attr);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#setAttributeNodeNS(org.w3c.dom.Attr)
      */
    public Attr setAttributeNodeNS(Attr attr) throws DOMException {
        return this.element.setAttributeNodeNS(attr);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#setAttributeNS(java.lang.String, java.lang.String, java.lang.String)
      */
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
        this.element.setAttributeNS(namespaceURI, qualifiedName, value);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Node#getNodeName()
      */
    public String getNodeName() {
        return this.element.getNodeName();
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Node#getNodeType()
      */
    public short getNodeType() {
        return Node.ELEMENT_NODE;
    }

    public ElementImpl getElement() {
        return element;
    }

    /**
     * Returns the parent element of this <code>Node</code> object.
     * This method can throw an <code>UnsupportedOperationException</code>
     * if the tree is not kept in memory.
     *
     * @return the <code>SOAPElement</code> object that is the parent of
     *         this <code>Node</code> object or <code>null</code> if this
     *         <code>Node</code> object is root
     * @throws UnsupportedOperationException if the whole tree is not kept in memory
     * @see #setParentElement(javax.xml.soap.SOAPElement) setParentElement(javax.xml.soap.SOAPElement)
     */
    public SOAPElement getParentElement() {
        return (SOAPElement) this.element.getParent();
    }


    /**
     * Find the Document that this Node belongs to (the document in
     * whose context the Node was created). The Node may or may not
     */
    public Document getOwnerDocument() {
        return this.element.getOwnerDocument();
    }

    /**
     * Returns the the value of the immediate child of this <code>Node</code>
     * object if a child exists and its value is text.
     *
     * @return a <code>String</code> with the text of the immediate child of
     *         this <code>Node</code> object if (1) there is a child and
     *         (2) the child is a <code>Text</code> object;
     *         <code>null</code> otherwise
     */
    public String getValue() {

        if (element.getType() == OMNode.TEXT_NODE) {
            return element.getText();
        } else if (element.getType() == OMNode.ELEMENT_NODE) {
            final OMNode firstOMChild = element.getFirstOMChild();
            if (firstOMChild instanceof TextImpl) {
                return ((TextImpl) firstOMChild).getData();
            } else if(firstOMChild instanceof SOAPElementImpl) {
                return ((SOAPElementImpl) firstOMChild).getValue();
            }
        }
        return null;
    }


    public org.w3c.dom.Node getFirstChild() {
        return this.element.getFirstChild();
    }

    /**
     * Method getLastChild
     *
     * @see org.w3c.dom.Node#getLastChild()
     */
    public org.w3c.dom.Node getLastChild() {
        return this.element.getLastChild();
    }

    /**
     * dom Node method
     */
    public org.w3c.dom.Node getNextSibling() {
        return this.element.getNextSibling();
    }

    public Node getPreviousSibling() {
        return this.element.getPreviousSibling();
    }

    public NodeList getChildNodes() {
        return this.element.getChildNodes();
    }

    public boolean hasChildNodes() {
        return this.element.hasChildNodes();
    }

    /**
     * If this is a Text node then this method will set its value, otherwise it
     * sets the value of the immediate (Text) child of this node. The value of
     * the immediate child of this node can be set only if, there is one child
     * node and that node is a Text node, or if there are no children in which
     * case a child Text node will be created.
     *
     * @param value the text to set
     * @throws IllegalStateException if the node is not a Text  node and
     *                               either has more than one child node or has a child node that
     *                               is not a Text node
     */
    public void setValue(String value) {
        OMNode firstChild = element.getFirstOMChild();
        if (firstChild == null ||
            (((javax.xml.soap.Node) firstChild).getNodeType() == javax.xml.soap.Node.TEXT_NODE &&
             firstChild.getNextOMSibling() == null)) {

            //If there are no children OR
            //the first child is a text node and the only child

            element.setText(value);
        } else {
            throw new IllegalStateException("This node is not a Text  node and " +
                                            "either has more than one child node or has a child " +
                                            "node that is not a Text node");
        }
    }
}
