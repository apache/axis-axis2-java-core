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

import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.om.impl.dom.ElementImpl;
import org.apache.axiom.om.impl.dom.NamespaceImpl;
import org.apache.axiom.om.impl.dom.NodeImpl;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11HeaderBlockImpl;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class SOAPHeaderImpl extends SOAPElementImpl implements SOAPHeader {

    private org.apache.axiom.soap.SOAPHeader omSOAPHeader;

    /**
     * Constructor
     *
     * @param header
     */
    public SOAPHeaderImpl(org.apache.axiom.soap.SOAPHeader header) {
        super((ElementImpl) header);
        omSOAPHeader = header;
    }

    /* (non-Javadoc)
    * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String)
    */
    public SOAPElement addChildElement(String localName) throws SOAPException {
        return addHeaderElement(new PrefixedQName(null, localName, null));
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
    * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String, java.lang.String, java.lang.String)
    */
    public SOAPElement addChildElement(String localName, String prefix, String uri) throws SOAPException {
        OMNamespace ns = new NamespaceImpl(uri, prefix);
        SOAPHeaderBlock headerBlock =
                new SOAP11HeaderBlockImpl(localName, ns, omSOAPHeader, (SOAPFactory)this.element.getOMFactory());
        SOAPHeaderElementImpl soapHeaderElement = new SOAPHeaderElementImpl(headerBlock);
        element.setUserData(SAAJ_NODE, this, null);
        soapHeaderElement.element.setUserData(SAAJ_NODE, soapHeaderElement, null);
        soapHeaderElement.setParentElement(this);
        return soapHeaderElement;
    }

    /* (non-Javadoc)
    * @see javax.xml.soap.SOAPElement#addChildElement(javax.xml.soap.Name)
    */
    public SOAPElement addChildElement(Name name) throws SOAPException {
        return addHeaderElement(name);
    }

    /* (non-Javadoc)
    * @see javax.xml.soap.SOAPElement#addChildElement(javax.xml.soap.SOAPElement)
    */
    public SOAPElement addChildElement(SOAPElement soapElement) throws SOAPException {
        OMNamespace ns = new NamespaceImpl(soapElement.getNamespaceURI(), 
                soapElement.getPrefix());
        SOAPHeaderBlock headerBlock =
                new SOAP11HeaderBlockImpl(soapElement.getLocalName(), ns, 
                        omSOAPHeader,
                        (SOAPFactory)this.element.getOMFactory());
        SOAPHeaderElementImpl soapHeaderElement = new SOAPHeaderElementImpl(headerBlock);
        element.setUserData(SAAJ_NODE, this, null);
        soapHeaderElement.element.setUserData(SAAJ_NODE, soapHeaderElement, null);
        soapHeaderElement.setParentElement(this);
        return soapHeaderElement;
    }

    /**
     * Creates a new <CODE>SOAPHeaderElement</CODE> object
     * initialized with the specified name and adds it to this
     * <CODE>SOAPHeader</CODE> object.
     *
     * @param name a <CODE>Name</CODE> object with
     *             the name of the new <CODE>SOAPHeaderElement</CODE>
     *             object
     * @return the new <CODE>SOAPHeaderElement</CODE> object that
     *         was inserted into this <CODE>SOAPHeader</CODE>
     *         object
     * @throws SOAPException if a SOAP error occurs
     */
    public SOAPHeaderElement addHeaderElement(Name name) throws SOAPException {
        OMNamespace ns = new NamespaceImpl(name.getURI(), name.getPrefix());
        SOAPHeaderBlock headerBlock =
                new SOAP11HeaderBlockImpl(name.getLocalName(), ns, omSOAPHeader,
                        (SOAPFactory)this.element.getOMFactory());
        SOAPHeaderElementImpl soapHeaderElement = new SOAPHeaderElementImpl(headerBlock);
        element.setUserData(SAAJ_NODE, this, null);
        soapHeaderElement.element.setUserData(SAAJ_NODE, soapHeaderElement, null);
        soapHeaderElement.setParentElement(this);
        return soapHeaderElement;
    }

    /**
     * Returns a list of all the <CODE>SOAPHeaderElement</CODE>
     * objects in this <CODE>SOAPHeader</CODE> object that have the
     * the specified actor. An actor is a global attribute that
     * indicates the intermediate parties to whom the message should
     * be sent. An actor receives the message and then sends it to
     * the next actor. The default actor is the ultimate intended
     * recipient for the message, so if no actor attribute is
     * included in a <CODE>SOAPHeader</CODE> object, the message is
     * sent to its ultimate destination.
     *
     * @param actor a <CODE>String</CODE> giving the
     *              URI of the actor for which to search
     * @return an <CODE>Iterator</CODE> object over all the <CODE>
     *         SOAPHeaderElement</CODE> objects that contain the
     *         specified actor
     * @see #extractHeaderElements(java.lang.String) extractHeaderElements(java.lang.String)
     */
    public Iterator examineHeaderElements(String actor) {
        Collection elements = new ArrayList();
        for (Iterator iterator = omSOAPHeader.examineHeaderBlocks(actor); iterator.hasNext();) {
            elements.add(((NodeImpl) iterator.next()).getUserData(SAAJ_NODE));
        }
        return elements.iterator();
    }

    /**
     * Returns a list of all the <CODE>SOAPHeaderElement</CODE>
     * objects in this <CODE>SOAPHeader</CODE> object that have
     * the the specified actor and detaches them from this <CODE>
     * SOAPHeader</CODE> object.
     * <p/>
     * <P>This method allows an actor to process only the parts of
     * the <CODE>SOAPHeader</CODE> object that apply to it and to
     * remove them before passing the message on to the next
     * actor.
     *
     * @param actor a <CODE>String</CODE> giving the
     *              URI of the actor for which to search
     * @return an <CODE>Iterator</CODE> object over all the <CODE>
     *         SOAPHeaderElement</CODE> objects that contain the
     *         specified actor
     * @see #examineHeaderElements(java.lang.String) examineHeaderElements(java.lang.String)
     */
    public Iterator extractHeaderElements(String actor) {
        Collection elements = new ArrayList();
        for (Iterator iterator = omSOAPHeader.extractHeaderBlocks(actor); iterator.hasNext();) {
            elements.add(((NodeImpl) iterator.next()).getUserData(SAAJ_NODE));
        }
        return elements.iterator();
    }

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderElement</code> objects in this <code>SOAPHeader</code>
     * object that have the specified actor and that have a MustUnderstand
     * attribute whose value is equivalent to <code>true</code>.
     *
     * @param actor a <code>String</code> giving the URI of the actor for which
     *              to search
     * @return an <code>Iterator</code> object over all the
     *         <code>SOAPHeaderElement</code> objects that contain the
     *         specified actor and are marked as MustUnderstand
     */
    public Iterator examineMustUnderstandHeaderElements(String actor) {
        Collection elements = new ArrayList();
        for (Iterator iterator = omSOAPHeader.examineMustUnderstandHeaderBlocks(actor);
             iterator.hasNext();) {
            elements.add(((NodeImpl) iterator.next()).getUserData(SAAJ_NODE));
        }
        return elements.iterator();
    }

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderElement</code> objects in this <code>SOAPHeader</code>
     * object.
     *
     * @return an <code>Iterator</code> object over all the
     *         <code>SOAPHeaderElement</code> objects contained by this
     *         <code>SOAPHeader</code>
     */
    public Iterator examineAllHeaderElements() {
        Collection elements = new ArrayList();
        for (Iterator iterator = omSOAPHeader.examineAllHeaderBlocks(); iterator.hasNext();) {
            elements.add(((NodeImpl) iterator.next()).getUserData(SAAJ_NODE));
        }
        return elements.iterator();
    }

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderElement</code> objects in this <code>SOAPHeader </code>
     * object and detaches them from this <code>SOAPHeader</code> object.
     *
     * @return an <code>Iterator</code> object over all the
     *         <code>SOAPHeaderElement</code> objects contained by this
     *         <code>SOAPHeader</code>
     */
    public Iterator extractAllHeaderElements() {
        Collection elements = new ArrayList();
        for (Iterator iterator = omSOAPHeader.extractAllHeaderBlocks(); iterator.hasNext();) {
            elements.add(((NodeImpl) iterator.next()).getUserData(SAAJ_NODE));
        }
        return elements.iterator();
    }

    public SOAPElement addTextNode(String text) throws SOAPException {
        throw new UnsupportedOperationException("Cannot add text node to SOAPHeader");
    }

    public Iterator getChildElements(Name name) {
        QName qName = new QName(name.getURI(), name.getLocalName());
        return getChildren(element.getChildrenWithName(qName));
    }

    public Iterator getChildElements() {
        return getChildren(element.getChildren());
    }

    private Iterator getChildren(Iterator childIter) {
        Collection childElements = new ArrayList();
        while (childIter.hasNext()) {
            org.w3c.dom.Node domNode = (org.w3c.dom.Node) childIter.next();
            Node saajNode = toSAAJNode(domNode);
            if (saajNode instanceof javax.xml.soap.Text) {
                childElements.add(saajNode);
            } else if (!(saajNode instanceof SOAPHeaderElement)) {
                // silently replace node, as per saaj 1.2 spec
                SOAPHeaderElement headerEle = new SOAPHeaderElementImpl((SOAPHeaderBlock) domNode);
                ((NodeImpl) domNode).setUserData(SAAJ_NODE, headerEle, null);
                childElements.add(headerEle);
            } else {
                childElements.add(saajNode);
            }
        }
        return childElements.iterator();
    }

    public void detachNode() {
        this.detach();
    }

    public OMNode detach() {
        OMNode omNode = omSOAPHeader.detach();
        parentElement = null;
        return omNode;
    }
}