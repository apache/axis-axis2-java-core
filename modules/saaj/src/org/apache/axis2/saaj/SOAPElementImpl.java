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

import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.impl.OMNodeEx;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class SOAPElementImpl
 */
public class SOAPElementImpl extends NodeImpl implements SOAPElement {
    /**
     * Field omElement
     * The corresponding OM object for SOAPElement is OMElement, so we would
     * have a datamember of type OMElement in this class
     */
    protected org.apache.axis2.om.OMElement omElement;

    /**
     * Constructor SOAPElementImpl
     * The standard constructor for being able to create SOAPElement given a omElement
     *
     * @param omElement
     */
    public SOAPElementImpl(org.apache.axis2.om.OMElement omElement) {
        super(omElement);
        this.omElement = omElement;
    }

    /**
     * Constructor SOAPElementImpl
     * The empty constructor
     */
    public SOAPElementImpl() {
        super();
    }

    /**
     * Method getOMElement
     * getter method on the data member omElement
     *
     * @return
     */
    public org.apache.axis2.om.OMElement getOMElement() {
        return this.omElement;
    }

    /**
     * Method addChildElement
     *
     * @param name
     * @return SOAPElement
     * @throws SOAPException
     * @see javax.xml.soap.SOAPElement#addChildElement(javax.xml.soap.Name)
     */
    public SOAPElement addChildElement(Name name) throws SOAPException {
        //We will create a new OMElement and add that as a child to the OMElement datamember that
        //we are carrying along. And return back a wrapped SOAPElement corresponding to the
        //created OMElement

        //Since a <code>Name</code> object is given as parameter we should try to create an OMElement
        //and register it with the contents of the <code>name</code> element
        org.apache.axis2.om.OMElement newOMElement = org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createOMElement(
                        new QName(name.getURI(),
                                name.getLocalName(),
                                name.getPrefix()),
                        omElement);
        omElement.addChild(newOMElement);
        return new SOAPElementImpl(newOMElement);
    }

    /**
     * Method addChildElement
     *
     * @param localName
     * @return
     * @throws SOAPException
     * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String)
     */
    public SOAPElement addChildElement(String localName) throws SOAPException {
        //We will create a new OMElement and add that as a child to the OMElement datamember that
        //we are carrying along. And return back a wrapped SOAPElement corresponding to the
        //created OMElement
        org.apache.axis2.om.OMElement newOMElement = org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createOMElement(new QName(localName), omElement);
        omElement.addChild(newOMElement);
        return new SOAPElementImpl(newOMElement);
    }

    /**
     * Method addChildElement
     *
     * @param localName
     * @param prefix
     * @return
     * @throws SOAPException
     * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String, java.lang.String)
     */
    public SOAPElement addChildElement(String localName, String prefix)
            throws SOAPException {
        org.apache.axis2.om.OMElement newOMElement = org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createOMElement(new QName(null, localName, prefix),
                        omElement);
        omElement.addChild(newOMElement);
        return new SOAPElementImpl(newOMElement);
    }

    /**
     * Method addChildElement
     *
     * @param localName
     * @param prefix
     * @return
     * @throws SOAPException
     * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public SOAPElement addChildElement(String localName, String prefix,
                                       String uri) throws SOAPException {
        org.apache.axis2.om.OMElement newOMElement = org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createOMElement(new QName(uri, localName, prefix), omElement);
        omElement.addChild(newOMElement);
        return new SOAPElementImpl(newOMElement);
    }

    /**
     * Method addChildElement
     *
     * @param element
     * @return
     * @throws SOAPException
     * @see javax.xml.soap.SOAPElement#addChildElement(javax.xml.soap.SOAPElement)
     */
    public SOAPElement addChildElement(SOAPElement element)
            throws SOAPException {
        //TODO:
        //The fragment rooted in element is either added as a whole or not at all, if there was an error.
        //The fragment rooted in element cannot contain elements named ?Envelope?, ?Header? or ?Body?
        //and in the SOAP namespace. Any namespace prefixes present in the fragment should be fully
        //resolved using appropriate namespace declarations within the fragment itself.

        org.apache.axis2.om.OMElement omElementToAdd = ((SOAPElementImpl) element).getOMElement();
        ((OMNodeEx)omElementToAdd).setParent(omElement);
        omElement.addChild(omElementToAdd);
        return new SOAPElementImpl(omElementToAdd);
    }

    /**
     * Method addTextNode
     *
     * @param text
     * @return
     * @throws SOAPException
     * @see javax.xml.soap.SOAPElement#addTextNode(java.lang.String)
     */
    public SOAPElement addTextNode(String text) throws SOAPException {
        //We need to create an OMText node and add that to
        //the omElement delegate member that we have with us. All this OMElement's setText() does
        omElement.setText(text);
        return this;
    }

    /**
     * Method addAttribute
     * This method adds an attribute to the underlying omElement datamember and returns ourselves
     *
     * @param name
     * @param value
     * @return ourself
     * @throws SOAPException
     * @see javax.xml.soap.SOAPElement#addAttribute(javax.xml.soap.Name, java.lang.String)
     */
    public SOAPElement addAttribute(Name name, String value)
            throws SOAPException {
        org.apache.axis2.om.OMNamespace omNS = org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createOMNamespace(name.getURI(), name.getPrefix());
        omElement.declareNamespace(omNS);
        //TODO:
        //The namespace of the attribute must be within the scope of the SOAPElement
        //That check should be performed here.
        omElement.addAttribute(name.getLocalName(), value, omNS);
        return this;
    }

    /**
     * Method addNamespaceDeclaration
     *
     * @param prefix
     * @param uri
     * @return
     * @throws SOAPException
     * @see javax.xml.soap.SOAPElement#addNamespaceDeclaration(java.lang.String, java.lang.String)
     */
    public SOAPElement addNamespaceDeclaration(String prefix, String uri)
            throws SOAPException {
        omElement.declareNamespace(uri, prefix);
        return this;
    }

    /**
     * Method getAttributeValue
     *
     * @param name
     * @return
     * @see javax.xml.soap.SOAPElement#getAttributeValue(javax.xml.soap.Name)
     */
    public String getAttributeValue(Name name) {
        //This method is waiting on the finalization of the name for a method
        //in OMElement that returns a OMAttribute from an input QName
        return omElement.getFirstAttribute(
                new QName(name.getURI(),
                        name.getLocalName(),
                        name.getPrefix()))
                .getAttributeValue();
    }

    /**
     * Method getAllAttributes
     *
     * @return
     * @see javax.xml.soap.SOAPElement#getAllAttributes()
     */
    public Iterator getAllAttributes() {
        Iterator attrIter = omElement.getAllAttributes();
        ArrayList arrayList = new ArrayList();
        while (attrIter.hasNext()) {
            Object o = attrIter.next();
            if (o instanceof org.apache.axis2.om.OMAttribute) {
                //we need to create a SOAPNode for this and add to the arrayList
                /*javax.xml.soap.Node soapNode = new NodeImpl(
                        (org.apache.axis2.om.OMAttribute) o);
                arrayList.add(soapNode);*/
            	//We need to return javax.xml.soap.Name 
                OMNamespace ons = ((org.apache.axis2.om.OMAttribute)o).getNamespace();
                String lName = ((org.apache.axis2.om.OMAttribute)o).getLocalName();
                arrayList.add(new PrefixedQName(ons.getName(), lName, ons.getPrefix()));
            }
        }
        return arrayList.iterator();
    }

    /**
     * Method getNamespaceURI
     *
     * @param prefix
     * @return
     * @see javax.xml.soap.SOAPElement#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix) {
        //Lets get all the inscope namespaces of this SOAPElement and iterate over them,
        //whenever the prefix mathces break and return the corresponding URI.
        Iterator nsIter = omElement.getAllDeclaredNamespaces();

        //loop over to see a prefix matching namespace.
        while (nsIter.hasNext()) {
            Object o = nsIter.next();
            if (o instanceof org.apache.axis2.om.OMNamespace) {
                org.apache.axis2.om.OMNamespace ns = (org.apache.axis2.om.OMNamespace) o;
                if (ns.getPrefix().equalsIgnoreCase(prefix))
                    return ns.getName();
            }
        }
        return null;
    }

    /**
     * method getNamespacePrefixes
     * This method returns an iterator over all the declared namespaces prefix names.
     *
     * @return Iterator
     * @see javax.xml.soap.SOAPElement#getNamespacePrefixes()
     */
    public Iterator getNamespacePrefixes() {
        //Get all declared namespace, make a list of their prefixes and return an iterator over that list
        ArrayList prefixList = new ArrayList();
        Iterator nsIter = omElement.getAllDeclaredNamespaces();
        while (nsIter.hasNext()) {
            Object o = nsIter.next();
            if (o instanceof org.apache.axis2.om.OMNamespace) {
                org.apache.axis2.om.OMNamespace ns = (org.apache.axis2.om.OMNamespace) o;
                prefixList.add(ns.getPrefix());
            }
        }
        return prefixList.iterator();
    }

    /**
     * Method getElementName
     *
     * @return
     * @see javax.xml.soap.SOAPElement#getElementName()
     */
    public Name getElementName() {
        QName qName = omElement.getQName();
        return new PrefixedQName(qName.getNamespaceURI(),
                qName.getLocalPart(),
                qName.getPrefix());
    }

    /**
     * method removeAttribute
     * This method removes an attribute with the specified name from the element.
     * Returns true if the attribute was removed successfully; false if it was not
     *
     * @param name
     * @return boolean
     * @see javax.xml.soap.SOAPElement#removeAttribute(javax.xml.soap.Name)
     */
    public boolean removeAttribute(Name name) {
        //get the OMAttribute with the given Name first, and call a removeAttribute(OMAttribute)
        //method on the omElement datamember this SOAPElement has in it.
        org.apache.axis2.om.OMAttribute attr = omElement.getFirstAttribute(
                new QName(name.getURI(),
                        name.getLocalName(),
                        name.getPrefix()));
        if (attr != null) {
            omElement.removeAttribute(attr);
            return true;
        }
        return false;
    }

    /**
     * method removeNamespaceDeclaration
     *
     * @param prefix
     * @return
     * @see javax.xml.soap.SOAPElement#removeNamespaceDeclaration(java.lang.String)
     */
    public boolean removeNamespaceDeclaration(String prefix) {
        //TODO:
        //I'm waiting on a removeNamespace method to be added to OMElement API
        return false;
    }

    /**
     * method getChildElements
     *
     * @return
     * @see javax.xml.soap.SOAPElement#getChildElements()
     */
    public Iterator getChildElements() {
        //Actually all the children are being treated as OMNodes and are being
        //wrapped accordingly to a single type (SOAPElement) and being returned in an iterator.
        //Text nodes and element nodes are all being treated alike here. Is that a serious issue???
        Iterator childIter = omElement.getChildren();
        ArrayList arrayList = new ArrayList();
        while (childIter.hasNext()) {
            Object o = childIter.next();
            if (o instanceof org.apache.axis2.om.OMNode) {
                if (o instanceof org.apache.axis2.om.OMText) {
                    javax.xml.soap.Text childText = new TextImpl(
                            ((org.apache.axis2.om.OMText) o).getText());
                    arrayList.add(childText);
                } else {
                    SOAPElement childElement = new SOAPElementImpl(
                            (org.apache.axis2.om.OMElement) o);
                    arrayList.add(childElement);
                }
                //javax.xml.soap.Node childElement = new NodeImpl((org.apache.axis2.om.OMNode)o);

            }
        }
        return arrayList.iterator();
    }

    /**
     * method getChildElements
     *
     * @param name
     * @return
     * @see javax.xml.soap.SOAPElement#getChildElements(javax.xml.soap.Name)
     */
    public Iterator getChildElements(Name name) {
        QName qName = new QName(name.getURI(), name.getLocalName());
        Iterator childIter = omElement.getChildrenWithName(qName);
        ArrayList arrayList = new ArrayList();
        while (childIter.hasNext()) {
            Object o = childIter.next();
            if (o instanceof org.apache.axis2.om.OMNode) {
                SOAPElement childElement = new SOAPElementImpl(
                        (org.apache.axis2.om.OMElement) o);
                arrayList.add(childElement);
            }
        }
        return arrayList.iterator();
    }

    /**
     * method setEncodingStyle
     *
     * @param encodingStyle
     * @see javax.xml.soap.SOAPElement#setEncodingStyle(java.lang.String)
     */
    public void setEncodingStyle(String encodingStyle) throws SOAPException {

        //TODO:
        //Donno how to tackle this right now.
        //Couldn't figure out corresponding functionality in OM
        //Should re-visit

    }

    /**
     * method getEncodingStyle
     *
     * @return
     * @see javax.xml.soap.SOAPElement#getEncodingStyle()
     */
    public String getEncodingStyle() {
        //TODO:
        //This is incomplete, needs to be revisited later
        return null;
    }

    /**
     * method removeContents
     *
     * @see javax.xml.soap.SOAPElement#removeContents()
     */
    public void removeContents() {
        //We will get all the children and iteratively call the detach() on all of 'em.
        Iterator childIter = omElement.getChildren();

        while (childIter.hasNext()) {
            Object o = childIter.next();
            if (o instanceof org.apache.axis2.om.OMNode)
                ((org.apache.axis2.om.OMNode) o).detach();
        }
    }

    /**
     * method getVisibleNamespacePrefixes
     *
     * @return
     * @see javax.xml.soap.SOAPElement#getVisibleNamespacePrefixes()
     */
    public Iterator getVisibleNamespacePrefixes() {
        //I'll recursively return all the declared namespaces till this node, including its parents etc.
        Iterator namespacesIter = omElement.getAllDeclaredNamespaces();
        ArrayList returnList = new ArrayList();
        while (namespacesIter.hasNext()) {
            Object o = namespacesIter.next();
            if (o instanceof org.apache.axis2.om.OMNamespace) {
                javax.xml.soap.Node soapNode = new NodeImpl(
                        (org.apache.axis2.om.OMNamespace) o);
                returnList.add(soapNode);
            }
        }//taken care of adding namespaces of this node.
        //now we have to take care of adding the namespaces that are in the scope till the level of
        //this nodes' parent.
        org.apache.axis2.om.OMContainer parent = omElement.getParent();
        if (parent != null && parent instanceof org.apache.axis2.om.OMElement) {
            Iterator parentScopeNamespacesIter = ((org.apache.axis2.om.OMElement) parent).getAllDeclaredNamespaces();
            while (parentScopeNamespacesIter.hasNext()) {
                Object o = parentScopeNamespacesIter.next();
                if (o instanceof org.apache.axis2.om.OMNamespace) {
                    javax.xml.soap.Node soapNode = new NodeImpl(
                            (org.apache.axis2.om.OMNamespace) o);
                    returnList.add(soapNode);
                }
            }
        }
        return returnList.iterator();
    }

    /**
     * method getTagName
     *
     * @return
     * @see org.w3c.dom.Element#getTagName()
     */
    public String getTagName() {
        return this.getLocalName();
    }

    /**
     * method removeAttribute
     *
     * @param arg0
     * @see org.w3c.dom.Element#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String localName) throws DOMException {
        //just got a localName, so assuming the namespace to be that of element
        Name elementQualifiedName = this.getElementName();
        //now try to remove that Attribute from this SOAPElement
        this.removeAttribute(
                new PrefixedQName(elementQualifiedName.getURI(),
                        localName,
                        elementQualifiedName.getPrefix()));
    }

    /**
     * method hasAttribute
     * This method returns true when an attribute with a given name is specified
     * on this element, false otherwise.
     *
     * @param localName
     * @return
     * @see org.w3c.dom.Element#hasAttribute(java.lang.String)
     */
    public boolean hasAttribute(String localName) {
        Iterator attrIter = omElement.getAllAttributes();
        while (attrIter.hasNext()) {
            org.apache.axis2.om.OMAttribute omAttr = (org.apache.axis2.om.OMAttribute) (attrIter.next());
            if (omAttr.getLocalName().equals(localName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * method getAttribute
     * This method retrieves the value of an attribute having specified localname.
     * In case of an element having multiple attributes with same localname but declared
     * in different namespaces, use of this method is unadvised.
     *
     * @param name
     * @return String
     * @see org.w3c.dom.Element#getAttribute(java.lang.String)
     */
    public String getAttribute(String name) {
        Iterator attrIter = omElement.getAllAttributes();
        while (attrIter.hasNext()) {
            org.apache.axis2.om.OMAttribute omAttr = (org.apache.axis2.om.OMAttribute) (attrIter.next());
            if (omAttr.getLocalName().equals(name)) {
                return omAttr.getAttributeValue();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.w3c.dom.Element#removeAttributeNS(java.lang.String, java.lang.String)
     */
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        Name name = new PrefixedQName(namespaceURI, localName, null);
        this.removeAttribute(name);
    }

    /**
     * Method setAttribute
     * This method creates and adds an attribute with the given localName and value
     * into the underlying OM. It uses the namespace of omElement datamember of this SOAPElement for the
     * newly added attribute.
     *
     * @param localName
     * @param value
     * @return
     * @see org.w3c.dom.Element#setAttribute(java.lang.String, java.lang.String)
     */
    public void setAttribute(String localName, String value) throws DOMException {
        //We will create a OMAttribute for the given input params, add it
        //to the omElement datamemeber
        org.apache.axis2.om.OMAttribute omAttr = org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createOMAttribute(localName, omElement.getNamespace(), value);
        omElement.addAttribute(omAttr);
    }

    /**
     * method hasAttributeNS
     * This method returns true when an attribute with a given local name and
     * namespace URI is specified on this element or has a default value, false
     * otherwise.
     *
     * @param namespaceURI
     * @param localName
     * @return boolean
     * @see org.w3c.dom.Element#hasAttributeNS(java.lang.String, java.lang.String)
     */
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        Iterator attrIter = omElement.getAllAttributes();
        while (attrIter.hasNext()) {
            org.apache.axis2.om.OMAttribute omAttr = (org.apache.axis2.om.OMAttribute) (attrIter.next());
            if (omAttr.getLocalName().equals(localName) &&
                    omAttr.getNamespace().getName().equals(namespaceURI)) {
                return true;
            }
        }
        return false;
    }

    /**
     * method getAttributeNode
     * This method retrieves an attribute node by the specified localname
     *
     * @param name
     * @returns Attr
     * @see org.w3c.dom.Element#getAttributeNode(java.lang.String)
     */
    public Attr getAttributeNode(String localName) {
        Iterator attrIter = omElement.getAllAttributes();
        while (attrIter.hasNext()) {
            org.apache.axis2.om.OMAttribute omAttr = (org.apache.axis2.om.OMAttribute) (attrIter.next());
            if (omAttr.getLocalName().equals(localName)) {
                //So we have the right OMAttribute in hand.
                //wrap it into a org.w3c.dom.Attr object and return
                return (new org.apache.axis2.saaj.AttrImpl(omAttr, this));
            }
        }
        return null;
    }

    /**
     * method removeAttributeNode
     * This method removes the specified attribute node from this element.
     *
     * @param Attr The attribute node that should be removed.
     * @return Attr
     *         The removed attribute node
     * @see org.w3c.dom.Element#removeAttributeNode(org.w3c.dom.Attr)
     */
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        //create a OMAttribute with the input object's localName, namespace (URI + prefix), value
        //remove from underlying omElement such an attribute
        //wrap the OMAttribute used for removing, into a dom Attr object and return.
        org.apache.axis2.om.OMNamespace oldAttrNS = org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createOMNamespace(oldAttr.getNamespaceURI(),
                        oldAttr.getPrefix());
        org.apache.axis2.om.OMAttribute omAttr = org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createOMAttribute(oldAttr.getName(),
                        oldAttrNS,
                        oldAttr.getValue());
        omElement.removeAttribute(omAttr);
        return (new org.apache.axis2.saaj.AttrImpl(omAttr, this));
    }

    /**
     * Method setAttributeNode
     * This method creates and adds an attribute corresponding to the supplied <code>Attr</code>
     * object into the underlying OM. The attribute that gets added to OM is created against this.omElement's namespace
     *
     * @param attr - a dom Attr object
     * @return Attr - a dom Attr object corresponding to the added attribute.
     * @see org.w3c.dom.Element#setAttributeNode(org.w3c.dom.Attr)
     */
    public Attr setAttributeNode(Attr attr) throws DOMException {
        //Create a OMAttribute out of the supplied Attr, add this to the
        //omElement and now wrap the created OMAttribute into a Attr and return
        org.apache.axis2.om.OMAttribute omAttr = org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createOMAttribute(attr.getName(),
                        omElement.getNamespace(),
                        attr.getValue());
        omElement.addAttribute(omAttr);
        return (new org.apache.axis2.saaj.AttrImpl(omAttr, this));
    }

    /**
     * Method setAttributeNode
     * This method creates and adds an attribute corresponding to the supplied <code>Attr</code>
     * object into the underlying OM. The attribute added is created against it's own namespace
     *
     * @param attr - a dom Attr object
     * @return Attr - a dom Attr object corresponding to the added attribute.
     * @see org.w3c.dom.Element#setAttributeNodeNS(org.w3c.dom.Attr)
     */
    public Attr setAttributeNodeNS(Attr attr) throws DOMException {
        org.apache.axis2.om.OMNamespace attrNS = org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createOMNamespace(attr.getNamespaceURI(), attr.getPrefix());
        org.apache.axis2.om.OMAttribute omAttr = org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createOMAttribute(attr.getName(), attrNS, attr.getValue());
        omElement.addAttribute(omAttr);
        return (new org.apache.axis2.saaj.AttrImpl(omAttr, this));
    }

    /**
     * Method getElementsByTagName
     * Returns a NodeList of all the descendant Elements with the given local
     * name, in the order in which they are encountered in a preorder traversal
     * of this Element tree.
     * Current SOAPElement MAY not feature in the returned NodeList, only
     * the descendant elements matching the criterion should be added.
     *
     * @param localName
     * @return NodeList
     * @see org.w3c.dom.Element#getElementsByTagName(java.lang.String)
     */
    public NodeList getElementsByTagName(String localName) {
        Iterator childIter = this.getChildElements();
        NodeListImpl returnList;
        if (childIter == null)
            return null;
        else {
            returnList = new NodeListImpl();
            while (childIter.hasNext()) {
                NodeList list = getElementsByTagNamePreOrder(
                        (SOAPElement) childIter.next(), localName);
                //should *append* this list to the existing list. Remember, we are doing preorder
                returnList.addNodeList(list);
            }
        }
        return returnList;
    }

    private NodeList getElementsByTagNamePreOrder(SOAPElement child,
                                                  String localName) {
        NodeListImpl returnList = new NodeListImpl();
        //We are doing preorder, so see if root itself is a match and place it first in the order
        if (child.getLocalName().equals(localName)) {
            //so this must be first in the returnList
            returnList.addNode(child);
        }
        returnList.addNodeList(child.getElementsByTagName(localName));
        return returnList;
    }

    /**
     * method getAttributeNS
     * This method retrieves the value of the attribute matching the specified namespaceURI, and localName
     *
     * @param namespaceURI
     * @param localName
     * @return String
     * @see org.w3c.dom.Element#getAttributeNS(java.lang.String, java.lang.String)
     */
    public String getAttributeNS(String namespaceURI, String localName) {
        Iterator attrIter = omElement.getAllAttributes();
        while (attrIter.hasNext()) {
            org.apache.axis2.om.OMAttribute omAttr = (org.apache.axis2.om.OMAttribute) (attrIter.next());
            if (omAttr.getLocalName().equals(localName) &&
                    omAttr.getNamespace().getName().equals(namespaceURI)) {
                return omAttr.getAttributeValue();
            }
        }
        return null;
    }

    /**
     * Method setAttributeNS
     * This method creates and adds an attribute with the given namespaceURI, localName and value
     * into the underlying OM.
     *
     * @param localName
     * @param value
     * @return
     * @see org.w3c.dom.Element#setAttributeNS(java.lang.String, java.lang.String, java.lang.String)
     */
    public void setAttributeNS(String namespaceURI,
                               String localName,
                               String value)
            throws DOMException {
        //since no prefix is given, we create a OMNamespace with given URI and null prefix. How good is it???
        org.apache.axis2.om.OMNamespace attrNS = org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createOMNamespace(namespaceURI, null);
        org.apache.axis2.om.OMAttribute omAttr = org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createOMAttribute(localName, attrNS, value);
        omElement.addAttribute(omAttr);
    }

    /**
     * method getAttributeNodeNS
     * This method retrieves an org.w3c.dom.Attr node matching the specified namespaceURI and localName
     *
     * @param namespaceURI
     * @param localName
     * @return Attr
     * @see org.w3c.dom.Element#getAttributeNodeNS(java.lang.String, java.lang.String)
     */
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        Iterator attrIter = omElement.getAllAttributes();
        while (attrIter.hasNext()) {
            org.apache.axis2.om.OMAttribute omAttr = (org.apache.axis2.om.OMAttribute) (attrIter.next());
            if (omAttr.getLocalName().equals(localName) &&
                    omAttr.getNamespace().getName().equals(namespaceURI)) {
                return (new org.apache.axis2.saaj.AttrImpl(omAttr, this));
            }
        }
        return null;
    }

    /**
     * getElementsByTagNameNS
     * Returns a NodeList of all the descendant Elements with a given local
     * name and namespace URI in the order in which they are encountered in a
     * preorder traversal of this Element tree.
     * Current SOAPElement MAY not feature in the returned NodeList, only
     * the descendant elements matching the criterion should be added.
     *
     * @param namespaceURI
     * @param localName
     * @return NodeList
     * @see org.w3c.dom.Element#getElementsByTagNameNS(java.lang.String, java.lang.String)
     */
    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName) {
        Iterator childIter = this.getChildElements();
        NodeListImpl returnList;
        if (childIter == null)
            return null;
        else {
            returnList = new NodeListImpl();
            while (childIter.hasNext()) {
                NodeList list = getElementsByTagNameNSPreOrder(
                        (SOAPElement) childIter.next(),
                        namespaceURI,
                        localName);
                //should *append* this list to the existing list. Remember, we are doing preorder
                returnList.addNodeList(list);
            }
        }
        return returnList;
    }

    private NodeList getElementsByTagNameNSPreOrder(SOAPElement child,
                                                    String namespaceURI,
                                                    String localName) {
        NodeListImpl returnList = new NodeListImpl();
        //We are doing preorder, so see if root itself is a match and place it first in the order
        if (child.getNamespaceURI().equals(namespaceURI) &&
                child.getLocalName().equals(localName)) {
            //so this must be first in the returnList
            returnList.addNode(child);
        }
        returnList.addNodeList(
                child.getElementsByTagNameNS(namespaceURI, localName));
        return returnList;
    }

}
