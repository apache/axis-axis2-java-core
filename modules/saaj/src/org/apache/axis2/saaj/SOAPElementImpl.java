/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.saaj;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAP11Version;
import org.apache.axiom.soap.SOAP12Version;
import org.apache.axiom.soap.SOAPFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPFaultElement;
import javax.xml.soap.SOAPHeader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class SOAPElementImpl<T extends OMElement> extends NodeImpl<Element,T> implements SOAPElement {
    private String encodingStyle;

    public SOAPElementImpl(T element) {
        super((Element)element, element);
    }

    /**
     * Adds an attribute with the specified name and value to this <code>SOAPElement</code> object.
     * <p/>
     *
     * @param name  a <code>Name</code> object with the name of the attribute
     * @param value a <code>String</code> giving the value of the attribute
     * @return the <code>SOAPElement</code> object into which the attribute was inserted
     * @throws SOAPException if there is an error in creating the Attribute
     */
    public SOAPElement addAttribute(Name name, String value) throws SOAPException {
        if (name.getURI() == null || name.getURI().trim().length() == 0) {
            target.setAttributeNS("", name.getLocalName(), value);
        } else {
            target.setAttributeNS(name.getURI(), name.getPrefix() + ":" + name.getLocalName(),
                                   value);
        }
        return this;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#addChildElement(javax.xml.soap.Name)
      */
    public SOAPElement addChildElement(Name name) throws SOAPException {
        String prefix = name.getPrefix();
        return addChildElement(name.getLocalName(), "".equals(prefix) ? null : prefix,
                               name.getURI());
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#addChildElement(javax.xml.soap.SOAPElement)
      */
    public SOAPElement addChildElement(SOAPElement soapElement) throws SOAPException {
        String namespaceURI = soapElement.getNamespaceURI();
        String prefix = soapElement.getPrefix();
        String localName = soapElement.getLocalName();

        SOAPElementImpl<OMElement> childEle;        
        if (namespaceURI == null || namespaceURI.trim().length() == 0) {
            childEle =  new SOAPElementImpl<OMElement>((OMElement)getOwnerDocument().createElementNS(null, localName));
        } else {
            omTarget.declareNamespace(namespaceURI, prefix);
            childEle =
                new SOAPElementImpl<OMElement>((OMElement)target.getOwnerDocument().createElementNS(namespaceURI,
                                                                                    localName));
        }
        
        for (Iterator iter = soapElement.getAllAttributes(); iter.hasNext();) {
            Name name = (Name)iter.next();
            childEle.addAttribute(name, soapElement.getAttributeValue(name));
        }

        for (Iterator iter = soapElement.getChildElements(); iter.hasNext();) {
            Object o = iter.next();
            if (o instanceof Text) {
                childEle.addTextNode(((Text)o).getData());
            } else {
                childEle.addChildElement((SOAPElement)o);
            }
        }

        childEle.target.setUserData(SAAJ_NODE, childEle, null);
        if (namespaceURI != null && namespaceURI.trim().length() > 0) {
            childEle.omTarget.setNamespace(childEle.omTarget.declareNamespace(namespaceURI, prefix));
        }
        target.appendChild(childEle.target);
        childEle.target.getParentNode().setUserData(SAAJ_NODE, this, null);
        childEle.setParentElement(this);
        return childEle;
    }

    /* (non-Javadoc)
    * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String, java.lang.String, java.lang.String)
    */
    public SOAPElement addChildElement(String localName, String prefix, String namespaceURI)
            throws SOAPException {
        if (prefix == null) {
            prefix = "";
        }
        SOAPElementImpl<OMElement> childEle = (SOAPElementImpl<OMElement>)getOwnerDocument().
                        createElementNS(namespaceURI, prefix.length() == 0 ? localName : prefix + ":" + localName);
    
        childEle.target.setUserData(SAAJ_NODE, childEle, null);
        childEle.omTarget.setNamespace(prefix.length() == 0
                ? childEle.omTarget.declareDefaultNamespace(namespaceURI)
                : childEle.omTarget.declareNamespace(namespaceURI, prefix));
        target.appendChild(childEle.target);
        childEle.target.getParentNode().setUserData(SAAJ_NODE, this, null);
        childEle.setParentElement(this);
        return childEle;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String, java.lang.String)
      */
    public SOAPElement addChildElement(String localName, String prefix) throws SOAPException {
        String namespaceURI = getNamespaceURI(prefix);

        if (namespaceURI == null) {
            throw new SOAPException("Namespace not declared for the give prefix: " + prefix);
        }

        return addChildElement(localName, prefix, namespaceURI);
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String)
      */
    public SOAPElement addChildElement(String localName) throws SOAPException {
        SOAPElementImpl<OMElement> childEle =
                new SOAPElementImpl<OMElement>((OMElement)getOwnerDocument().createElementNS(null, localName));
        childEle.target.setUserData(SAAJ_NODE, childEle, null);
        target.appendChild(childEle.target);
        childEle.target.getParentNode().setUserData(SAAJ_NODE, this, null);
        childEle.setParentElement(this);
        return childEle;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#addNamespaceDeclaration(java.lang.String, java.lang.String)
      */
    public SOAPElement addNamespaceDeclaration(String prefix, String uri) throws SOAPException {
        if (prefix == null || prefix.length() == 0) {
            omTarget.declareDefaultNamespace(uri);
        } else {
            omTarget.declareNamespace(uri, prefix);
        }
        return this;
    }

    /**
     * Creates a new <code>Text</code> object initialized with the given <code>String</code> and
     * adds it to this <code>SOAPElement</code> object.
     *
     * @param text a <code>String</code> object with the textual content to be added
     * @return the <code>SOAPElement</code> object into which the new <code>Text</code> object was
     *         inserted
     * @throws SOAPException if there is an error in creating the new <code>Text</code> object
     */
    public SOAPElement addTextNode(String text) throws SOAPException {
        //OmElement.setText() will remove all the other text nodes that it contains
        //Therefore create a text node and add it
        //TODO: May need to address the situation where the prev sibling of the textnode itself is a textnode
        Text textNode = getOwnerDocument().createTextNode(text);
        appendChild(textNode);
        return this;
    }

    /**
     * Returns an iterator over all of the attribute names in this <CODE>SOAPElement</CODE> object.
     * The iterator can be used to get the attribute names, which can then be passed to the method
     * <CODE>getAttributeValue</CODE> to retrieve the value of each attribute.
     *
     * @return an iterator over the names of the attributes
     */
    public Iterator getAllAttributes() {
        final Iterator attribIter = omTarget.getAllAttributes();
        Collection attribName = new ArrayList();
        Attr attr;
        while (attribIter.hasNext()) {
            attr = (Attr)attribIter.next();
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
        final OMAttribute attribute = omTarget.getAttribute(new QName(name.getURI(),
                                                                     name.getLocalName(),
                                                                     name.getPrefix()));
        if (attribute == null) {
            return null;
        }
        return attribute.getAttributeValue();
    }

    /**
     * Returns an iterator over all the immediate content of this element. This includes
     * <CODE>Text</CODE> objects as well as <CODE>SOAPElement</CODE> objects.
     *
     * @return an iterator over <CODE>Text</CODE> and <CODE>SOAPElement</CODE> contained within this
     *         <CODE>SOAPElement</CODE> object
     */
    public Iterator getChildElements() {
        Iterator childIter = omTarget.getChildren();
        Collection childElements = new ArrayList();
        while (childIter.hasNext()) {
            childElements.add(toSAAJNode((org.w3c.dom.Node)childIter.next()));
        }
        return childElements.iterator();
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#getChildElements(javax.xml.soap.Name)
      */
    public Iterator getChildElements(Name name) {
        QName qName = new QName(name.getURI(), name.getLocalName());
        Iterator childIter = omTarget.getChildrenWithName(qName);
        Collection childElements = new ArrayList();
        while (childIter.hasNext()) {
            childElements.add(toSAAJNode((org.w3c.dom.Node)childIter.next()));
        }
        return childElements.iterator();
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#getElementName()
      */
    public Name getElementName() {
        QName qName = omTarget.getQName();
        return new PrefixedQName(qName.getNamespaceURI(),
                                 qName.getLocalPart(),
                                 qName.getPrefix());
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#getEncodingStyle()
      */
    public String getEncodingStyle() {
        return this.encodingStyle;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#getNamespacePrefixes()
      */
    public Iterator getNamespacePrefixes() {
        //Get all declared namespace, make a list of their prefixes and return an iterator over that list
        ArrayList prefixList = new ArrayList();
        Iterator nsIter = omTarget.getAllDeclaredNamespaces();
        while (nsIter.hasNext()) {
            Object o = nsIter.next();
            if (o instanceof org.apache.axiom.om.OMNamespace) {
                org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace)o;
                prefixList.add(ns.getPrefix());
            }
        }
        return prefixList.iterator();
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#getNamespaceURI(java.lang.String)
      */
    public String getNamespaceURI(String prefix) {
        OMNamespace ns = omTarget.findNamespaceURI(prefix);
        return ns != null ? ns.getNamespaceURI() : null;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#getVisibleNamespacePrefixes()
      */
    public Iterator getVisibleNamespacePrefixes() {
        //I'll recursively return all the declared namespaces till this node, including its parents etc.
        Iterator namespacesIter = omTarget.getAllDeclaredNamespaces();
        ArrayList returnList = new ArrayList();
        while (namespacesIter.hasNext()) {
            Object o = namespacesIter.next();
            if (o instanceof OMNamespace) {
                OMNamespace ns = (OMNamespace)o;
                if (ns.getPrefix() != null) {
                    returnList.add(ns.getPrefix());
                }
            }
        }
        //taken care of adding namespaces of this node.
        //now we have to take care of adding the namespaces that are in the scope till the level of
        //this nodes' parent.
        org.apache.axiom.om.OMContainer parent = omTarget.getParent();
        if (parent != null && parent instanceof org.apache.axiom.om.OMElement) {
            Iterator parentScopeNamespacesIter =
                    ((org.apache.axiom.om.OMElement)parent).getAllDeclaredNamespaces();
            while (parentScopeNamespacesIter.hasNext()) {
                Object o = parentScopeNamespacesIter.next();
                if (o instanceof OMNamespace) {
                    OMNamespace ns = (OMNamespace)o;
                    if (ns.getPrefix() != null) {
                        returnList.add(ns.getPrefix());
                    }
                }
            }
        }
        return returnList.iterator();
    }


    public SOAPElement addAttribute(QName qname, String value) throws SOAPException {
        if (qname.getNamespaceURI() == null || qname.getNamespaceURI().trim().length() == 0) {
            target.setAttribute(qname.getLocalPart(), value);
        } else {
            target.setAttributeNS(qname.getNamespaceURI(), qname.getPrefix() + ":" +
                    qname.getLocalPart(), value);
        }
        return this;
    }

    public SOAPElement addChildElement(QName qname) throws SOAPException {
        String prefix = qname.getPrefix();
        return addChildElement(qname.getLocalPart(), "".equals(prefix) ?
                null : prefix, qname.getNamespaceURI());
    }

    /**
     * Creates a QName whose namespace URI is the one associated with the parameter, prefix, in the
     * context of this SOAPElement. The remaining elements of the new QName are taken directly from
     * the parameters, localName and prefix.
     *
     * @param localName - a String containing the local part of the name. prefix - a String
     *                  containing the prefix for the name.
     * @return a QName with the specified localName and prefix, and with a namespace that is
     *         associated with the prefix in the context of this SOAPElement. This namespace will be
     *         the same as the one that would be returned by getNamespaceURI(String) if it were
     *         given prefix as its parameter.
     * @throws SOAPException - if the QName cannot be created.
     * @since SAAJ 1.3
     */
    public QName createQName(String localName, String prefix) throws SOAPException {
        String namespaceURI = getNamespaceURI(prefix);
        if (namespaceURI == null) {
            throw new SOAPException("Invalid prefix");
        } else {
            return new QName(namespaceURI, localName, prefix);
        }
    }

    public Iterator getAllAttributesAsQNames() {
        final Iterator attribIter = omTarget.getAllAttributes();
        Collection attributesAsQNames = new ArrayList();
        Attr attr;
        QName qname;
        while (attribIter.hasNext()) {
            attr = (Attr)attribIter.next();
            //Check : attr.getLocalName() | attr.getName()
            qname = new QName(attr.getNamespaceURI(), attr.getName(), attr.getPrefix());
            attributesAsQNames.add(qname);
        }
        return attributesAsQNames.iterator();
    }

    public String getAttributeValue(QName qname) {
        final OMAttribute attribute = omTarget.getAttribute(qname);
        if (attribute == null) {
            return null;
        }
        return attribute.getAttributeValue();
    }

    public Iterator getChildElements(QName qname) {
        Iterator childIter = omTarget.getChildrenWithName(qname);
        Collection childElements = new ArrayList();
        while (childIter.hasNext()) {
            childElements.add(toSAAJNode((org.w3c.dom.Node)childIter.next()));
        }
        return childElements.iterator();
    }

    public QName getElementQName() {
        return omTarget.getQName();
    }

    public boolean removeAttribute(QName qname) {
        org.apache.axiom.om.OMAttribute attr = omTarget.getAttribute(qname);
        if (attr != null) {
            omTarget.removeAttribute(attr);
            return true;
        }
        return false;
    }

    public SOAPElement setElementQName(QName newName) throws SOAPException {
        String localName = this.target.getLocalName();
        if (org.apache.axiom.soap.SOAPConstants.BODY_LOCAL_NAME.equals(localName)
                || org.apache.axiom.soap.SOAPConstants.HEADER_LOCAL_NAME.equals(localName)
                || org.apache.axiom.soap.SOAPConstants.SOAPENVELOPE_LOCAL_NAME .equals(localName)) {
            throw new SOAPException("changing this element name is not allowed");
        }
        OMNamespace omNamespace =
                omTarget.getOMFactory().createOMNamespace(newName.getNamespaceURI(), newName.getPrefix());
        this.omTarget.setNamespace(omNamespace);
        this.omTarget.setLocalName(newName.getLocalPart());
        return this;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#removeAttribute(javax.xml.soap.Name)
      */
    public boolean removeAttribute(Name name) {
        org.apache.axiom.om.OMAttribute attr = omTarget.getAttribute(new QName(name.getURI(),
                                                                              name.getLocalName(),
                                                                              name.getPrefix()));
        if (attr != null) {
            omTarget.removeAttribute(attr);
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#removeContents()
      */
    public void removeContents() {
        //We will get all the children and iteratively call the detach() on all of 'em.
        Iterator childIter = omTarget.getChildElements();
        while (childIter.hasNext()) {
            childIter.next();
            childIter.remove();
        }
    }

    /* (non-Javadoc)
      * @see javax.xml.soap.SOAPElement#removeNamespaceDeclaration(java.lang.String)
      */
    public boolean removeNamespaceDeclaration(String prefix) {
        for (Iterator<OMNamespace> it = omTarget.getAllDeclaredNamespaces(); it.hasNext(); ) {
            if (it.next().getPrefix().equals(prefix)) {
                it.remove();
                return true;
            }
        }
        return false;
    }


    /**
     * Sets the encoding style for this SOAPElement object to one specified.
     *
     * @param encodingStyle - a String giving the encoding style
     * @throws IllegalArgumentException
     *          - if there was a problem in the encoding style being set. SOAPException - if setting
     *          the encodingStyle is invalid for this SOAPElement.
     */
    public void setEncodingStyle(String encodingStyle) throws SOAPException {
        if (((SOAPFactory)this.omTarget.getOMFactory()).getSOAPVersion() == SOAP11Version.getSingleton()) {
            try {
                URI uri = new URI(encodingStyle);
                if (!(this instanceof SOAPEnvelope)) {
                    if (!encodingStyle.equals(SOAPConstants.URI_NS_SOAP_ENCODING)) {
                        throw new IllegalArgumentException(
                                "Invalid Encoding style : " + encodingStyle);
                    }
                }
                this.encodingStyle = encodingStyle;
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid Encoding style : "
                        + encodingStyle + ":" + e);
            }
        } else if (((SOAPFactory)this.omTarget.getOMFactory()).getSOAPVersion() == SOAP12Version.getSingleton()) {
            if (this instanceof SOAPHeader || this instanceof SOAPBody ||
                    this instanceof SOAPFault ||
                    this instanceof SOAPFaultElement || this instanceof SOAPEnvelope ||
                    this instanceof Detail) {
                throw new SOAPException("EncodingStyle attribute cannot appear in : " + this);
            }
        }
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#getAttribute(java.lang.String)
      */
    public String getAttribute(String name) {
        return target.getAttribute(name);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#getAttributeNode(java.lang.String)
      */
    public Attr getAttributeNode(String name) {
        return target.getAttributeNode(name);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#getAttributeNodeNS(java.lang.String, java.lang.String)
      */
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        return target.getAttributeNodeNS(namespaceURI, localName);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#getAttributeNS(java.lang.String, java.lang.String)
      */
    public String getAttributeNS(String namespaceURI, String localName) {
        return target.getAttributeNS(namespaceURI, localName);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#getElementsByTagName(java.lang.String)
      */
    public NodeList getElementsByTagName(String name) {
        return toSAAJNodeList(target.getElementsByTagName(name));
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#getElementsByTagNameNS(java.lang.String, java.lang.String)
      */
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return toSAAJNodeList(target.getElementsByTagNameNS(namespaceURI, localName));
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#getTagName()
      */
    public String getTagName() {
        return target.getTagName();
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#hasAttribute(java.lang.String)
      */
    public boolean hasAttribute(String name) {
        return target.hasAttribute(name);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#hasAttributeNS(java.lang.String, java.lang.String)
      */
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return target.hasAttributeNS(namespaceURI, localName);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#removeAttribute(java.lang.String)
      */
    public void removeAttribute(String name) throws DOMException {
        target.removeAttribute(name);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#removeAttributeNode(org.w3c.dom.Attr)
      */
    public Attr removeAttributeNode(Attr attr) throws DOMException {
        return target.removeAttributeNode(attr);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#removeAttributeNS(java.lang.String, java.lang.String)
      */
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        target.removeAttributeNS(namespaceURI, localName);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#setAttribute(java.lang.String, java.lang.String)
      */
    public void setAttribute(String name, String value) throws DOMException {
        target.setAttribute(name, value);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#setAttributeNode(org.w3c.dom.Attr)
      */
    public Attr setAttributeNode(Attr attr) throws DOMException {
        return target.setAttributeNode(attr);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#setAttributeNodeNS(org.w3c.dom.Attr)
      */
    public Attr setAttributeNodeNS(Attr attr) throws DOMException {
        return target.setAttributeNodeNS(attr);
    }

    /* (non-Javadoc)
      * @see org.w3c.dom.Element#setAttributeNS(java.lang.String, java.lang.String, java.lang.String)
      */
    public void setAttributeNS(String namespaceURI,
                               String qualifiedName, String value) throws DOMException {
        target.setAttributeNS(namespaceURI, qualifiedName, value);
    }

    /**
     * Returns the the value of the immediate child of this <code>Node</code> object if a child
     * exists and its value is text.
     *
     * @return a <code>String</code> with the text of the immediate child of this <code>Node</code>
     *         object if (1) there is a child and (2) the child is a <code>Text</code> object;
     *         <code>null</code> otherwise
     */
    public String getValue() {
        if (omTarget.getType() == OMNode.TEXT_NODE) {
            return omTarget.getText();
        } else if (omTarget.getType() == OMNode.ELEMENT_NODE) {
            final OMNode firstOMChild = omTarget.getFirstOMChild();
            if (firstOMChild instanceof Text) {
                return ((Text)firstOMChild).getData();
            } else if (firstOMChild instanceof SOAPElementImpl) {
                return ((SOAPElement)firstOMChild).getValue();
            }
        }
        return null;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }

    /**
     * If this is a Text node then this method will set its value, otherwise it sets the value of
     * the immediate (Text) child of this node. The value of the immediate child of this node can be
     * set only if, there is one child node and that node is a Text node, or if there are no
     * children in which case a child Text node will be created.
     *
     * @param value the text to set
     * @throws IllegalStateException if the node is not a Text  node and either has more than one
     *                               child node or has a child node that is not a Text node
     */
    public void setValue(String value) {
        OMNode firstChild = omTarget.getFirstOMChild();
        if (firstChild == null) {
            try {
                this.addTextNode(value);
            } catch (SOAPException e) {
                throw new RuntimeException("Cannot add text node", e);
            }
        } else if (((org.w3c.dom.Node)firstChild).getNodeType() == javax.xml.soap.Node.TEXT_NODE
                && firstChild.getNextOMSibling() == null) {
            ((org.w3c.dom.Text)firstChild).setData(value);
        } else {
            throw new IllegalStateException("This node is not a Text  node and " +
                    "either has more than one child node or has a child " +
                    "node that is not a Text node");
        }
    }

    public String toString() {
        return target.toString();
    }
        
    protected void copyContents(SOAPElementImpl childEle, Node child) throws SOAPException {
        NamedNodeMap attributes = child.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr node = (Attr)attributes.item(i);
            QName name;
            if (node.getPrefix() == null) {
                name = new QName(node.getNamespaceURI(), 
                                 node.getLocalName());
            } else {
                name = new QName(node.getNamespaceURI(), 
                                 node.getLocalName(),
                                 node.getPrefix());
            }
            childEle.addAttribute(name, node.getValue());
        }

        Node node;
        while ((node = child.getFirstChild()) != null) {
            childEle.appendChild(node);
        }
    }
}
