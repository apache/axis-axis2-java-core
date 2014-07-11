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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Version;
import org.apache.axiom.soap.SOAP12Version;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.namespace.Constants;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class SOAPHeaderImpl extends SOAPElementImpl<org.apache.axiom.soap.SOAPHeader> implements SOAPHeader {

    /**
     * Constructor
     *
     * @param header
     */
    public SOAPHeaderImpl(org.apache.axiom.soap.SOAPHeader header) {
        super(header);
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
    public SOAPElement addChildElement(String localName, String prefix, String uri)
            throws SOAPException {
        OMNamespace ns = omTarget.getOMFactory().createOMNamespace(uri, prefix);
        SOAPHeaderBlock headerBlock = ((SOAPFactory)this.omTarget.getOMFactory()).createSOAPHeaderBlock(localName, ns, omTarget);
        SOAPHeaderElementImpl soapHeaderElement = new SOAPHeaderElementImpl(headerBlock);
        target.setUserData(SAAJ_NODE, this, null);
        soapHeaderElement.target.setUserData(SAAJ_NODE, soapHeaderElement, null);
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
        OMNamespace ns = omTarget.getOMFactory().createOMNamespace(soapElement.getNamespaceURI(),
                                           soapElement.getPrefix());
        SOAPHeaderBlock headerBlock = ((SOAPFactory)this.omTarget.getOMFactory()).createSOAPHeaderBlock(
                soapElement.getLocalName(), ns, omTarget);
        SOAPHeaderElementImpl soapHeaderElement = new SOAPHeaderElementImpl(headerBlock);
        target.setUserData(SAAJ_NODE, this, null);
        soapHeaderElement.target.setUserData(SAAJ_NODE, soapHeaderElement, null);
        soapHeaderElement.setParentElement(this);
        return soapHeaderElement;
    }

    @Override
    protected Element appendElement(Element child) throws SOAPException {     
        OMNamespace ns = omTarget.getOMFactory().createOMNamespace(child.getNamespaceURI(),
                                           child.getPrefix());
        SOAPHeaderBlock headerBlock = ((SOAPFactory)this.omTarget.getOMFactory()).createSOAPHeaderBlock(
                child.getLocalName(), ns, omTarget);
     
        target.setUserData(SAAJ_NODE, this, null);
        
        SOAPHeaderElementImpl soapHeaderElement = new SOAPHeaderElementImpl(headerBlock);
        copyContents(soapHeaderElement, child);
        soapHeaderElement.target.setUserData(SAAJ_NODE, soapHeaderElement, null);
        soapHeaderElement.setParentElement(this);
        return soapHeaderElement;
    }
    
    /**
     * Creates a new <CODE>SOAPHeaderElement</CODE> object initialized with the specified name and
     * adds it to this <CODE>SOAPHeader</CODE> object.
     *
     * @param name a <CODE>Name</CODE> object with the name of the new <CODE>SOAPHeaderElement</CODE>
     *             object
     * @return the new <CODE>SOAPHeaderElement</CODE> object that was inserted into this
     *         <CODE>SOAPHeader</CODE> object
     * @throws SOAPException if a SOAP error occurs
     */
    public SOAPHeaderElement addHeaderElement(Name name) throws SOAPException {
        
        if (name.getURI() == null
                || name.getURI().trim().length() == 0) {
            throw new SOAPException("SOAP1.1 and SOAP1.2 requires all HeaderElements to have " +
                    "a namespace.");
        }
        String prefix = name.getPrefix() == null ? "" : name.getPrefix();
        OMNamespace ns = omTarget.getOMFactory().createOMNamespace(name.getURI(), prefix);

        SOAPHeaderBlock headerBlock = ((SOAPFactory)this.omTarget.getOMFactory()).createSOAPHeaderBlock(
                name.getLocalName(), ns, omTarget);

        SOAPHeaderElementImpl soapHeaderElement = new SOAPHeaderElementImpl(headerBlock);
        target.setUserData(SAAJ_NODE, this, null);
        soapHeaderElement.target.setUserData(SAAJ_NODE, soapHeaderElement, null);
        soapHeaderElement.setParentElement(this);
        return soapHeaderElement;
    }

    /**
     * Returns a list of all the <CODE>SOAPHeaderElement</CODE> objects in this
     * <CODE>SOAPHeader</CODE> object that have the the specified actor. An actor is a global
     * attribute that indicates the intermediate parties to whom the message should be sent. An
     * actor receives the message and then sends it to the next actor. The default actor is the
     * ultimate intended recipient for the message, so if no actor attribute is included in a
     * <CODE>SOAPHeader</CODE> object, the message is sent to its ultimate destination.
     *
     * @param actor a <CODE>String</CODE> giving the URI of the actor for which to search
     * @return an <CODE>Iterator</CODE> object over all the <CODE> SOAPHeaderElement</CODE> objects
     *         that contain the specified actor
     * @see #extractHeaderElements(String) extractHeaderElements(java.lang.String)
     */
    public Iterator examineHeaderElements(String actor) {
        Collection elements = new ArrayList();
        for (Iterator iterator = omTarget.examineHeaderBlocks(actor); iterator.hasNext();) {
            elements.add(toSAAJNode((org.w3c.dom.Node)iterator.next()));
        }
        return elements.iterator();
    }

    /**
     * Returns a list of all the <CODE>SOAPHeaderElement</CODE> objects in this
     * <CODE>SOAPHeader</CODE> object that have the the specified actor and detaches them from this
     * <CODE> SOAPHeader</CODE> object.
     * <p/>
     * <P>This method allows an actor to process only the parts of the <CODE>SOAPHeader</CODE>
     * object that apply to it and to remove them before passing the message on to the next actor.
     *
     * @param actor a <CODE>String</CODE> giving the URI of the actor for which to search
     * @return an <CODE>Iterator</CODE> object over all the <CODE> SOAPHeaderElement</CODE> objects
     *         that contain the specified actor
     * @see #examineHeaderElements(String) examineHeaderElements(java.lang.String)
     */
    public Iterator extractHeaderElements(String actor) {
        Collection elements = new ArrayList();
        for (Iterator iterator = omTarget.extractHeaderBlocks(actor); iterator.hasNext();) {
            elements.add(toSAAJNode((org.w3c.dom.Node)iterator.next()));
        }
        return elements.iterator();
    }

    /**
     * Returns an <code>Iterator</code> over all the <code>SOAPHeaderElement</code> objects in this
     * <code>SOAPHeader</code> object that have the specified actor and that have a MustUnderstand
     * attribute whose value is equivalent to <code>true</code>.
     *
     * @param actor a <code>String</code> giving the URI of the actor for which to search
     * @return an <code>Iterator</code> object over all the <code>SOAPHeaderElement</code> objects
     *         that contain the specified actor and are marked as MustUnderstand
     */
    public Iterator examineMustUnderstandHeaderElements(String actor) {
        Collection elements = new ArrayList();
        for (Iterator iterator = omTarget.examineMustUnderstandHeaderBlocks(actor);
             iterator.hasNext();) {
            elements.add(toSAAJNode((org.w3c.dom.Node)iterator.next()));
        }
        return elements.iterator();
    }

    /**
     * Returns an <code>Iterator</code> over all the <code>SOAPHeaderElement</code> objects in this
     * <code>SOAPHeader</code> object.
     *
     * @return an <code>Iterator</code> object over all the <code>SOAPHeaderElement</code> objects
     *         contained by this <code>SOAPHeader</code>
     */
    public Iterator examineAllHeaderElements() {
        Collection elements = new ArrayList();
        for (Iterator iterator = omTarget.examineAllHeaderBlocks(); iterator.hasNext();) {
            elements.add(toSAAJNode((org.w3c.dom.Node)iterator.next()));
        }
        return elements.iterator();
    }

    /**
     * Returns an <code>Iterator</code> over all the <code>SOAPHeaderElement</code> objects in this
     * <code>SOAPHeader </code> object and detaches them from this <code>SOAPHeader</code> object.
     *
     * @return an <code>Iterator</code> object over all the <code>SOAPHeaderElement</code> objects
     *         contained by this <code>SOAPHeader</code>
     */
    public Iterator extractAllHeaderElements() {
        Collection elements = new ArrayList();
        for (Iterator iterator = omTarget.extractAllHeaderBlocks(); iterator.hasNext();) {
            elements.add(toSAAJNode((org.w3c.dom.Node)iterator.next()));
        }
        return elements.iterator();
    }

    public SOAPHeaderElement addHeaderElement(QName qname) throws SOAPException {
        return (SOAPHeaderElement)addChildElement(qname.getLocalPart(), qname.getPrefix()
                , qname.getNamespaceURI());
    }


    /**
     * Creates a new NotUnderstood SOAPHeaderElement object initialized with the specified name and
     * adds it to this SOAPHeader object. This operation is supported only by SOAP 1.2
     *
     * @param name - a QName object with the name of the SOAPHeaderElement object that was not
     *             understood.
     * @return the new SOAPHeaderElement object that was inserted into this SOAPHeader object
     * @throws SOAPException- if a SOAP error occurs. java.lang.UnsupportedOperationException - if
     *                        this is a SOAP 1.1 Header.
     */

    public SOAPHeaderElement addNotUnderstoodHeaderElement(QName qname) throws SOAPException {
        SOAPHeaderBlock soapHeaderBlock = null;
        OMNamespace ns = omTarget.getOMFactory().createOMNamespace(qname.getNamespaceURI(), qname.getPrefix());
        if (((SOAPFactory)this.omTarget.getOMFactory()).getSOAPVersion() == SOAP11Version.getSingleton()) {
            throw new UnsupportedOperationException();
        } else {
            soapHeaderBlock = this.omTarget.addHeaderBlock(
                    Constants.ELEM_NOTUNDERSTOOD, this.omTarget.getNamespace());
            soapHeaderBlock.addAttribute(qname.getLocalPart(), qname.getPrefix(), ns);
        }
        SOAPHeaderElementImpl soapHeaderElementImpl = new SOAPHeaderElementImpl(soapHeaderBlock);
        return soapHeaderElementImpl;
    }

    /**
     * Creates a new Upgrade SOAPHeaderElement object initialized with the specified List of
     * supported SOAP URIs and adds it to this SOAPHeader object. This operation is supported on
     * both SOAP 1.1 and SOAP 1.2 header.
     *
     * @param supportedSOAPURIs - an Iterator object with the URIs of SOAP versions supported.
     * @return the new SOAPHeaderElement object that was inserted into this SOAPHeader object
     * @throws SOAPException - if a SOAP error occurs.
     */
    public SOAPHeaderElement addUpgradeHeaderElement(Iterator iterator) throws SOAPException {
        SOAPHeaderBlock upgrade = this.omTarget.addHeaderBlock(
                Constants.ELEM_UPGRADE, this.omTarget.getNamespace());

        int index = 0;
        String prefix = "ns";
        while (iterator.hasNext()) {
            index++;
            String supported = (String)iterator.next();

            OMNamespace namespace = omTarget.getOMFactory().createOMNamespace(supported, prefix + index);

            OMElement supportedEnvelop =
                    omTarget.getOMFactory().createOMElement(Constants.ELEM_SUPPORTEDENVELOPE,
                            namespace);
            supportedEnvelop.addAttribute(Constants.ATTR_QNAME, prefix + index + ":"
                    + Constants.ELEM_ENVELOPE, null);
            upgrade.addChild(supportedEnvelop);
        }
        SOAPHeaderElementImpl soapHeaderElementImpl = new SOAPHeaderElementImpl(upgrade);
        return soapHeaderElementImpl;
    }

    public SOAPHeaderElement addUpgradeHeaderElement(String[] as) throws SOAPException {
        ArrayList supportedEnvelops = new ArrayList();
        for (int a = 0; a < as.length; a++) {
            String supported = (String)as[a];
            supportedEnvelops.add(supported);
        }
        if (supportedEnvelops.size() > 0) {
            return addUpgradeHeaderElement(supportedEnvelops.iterator());
        }
        return null;
    }

    public SOAPHeaderElement addUpgradeHeaderElement(String s) throws SOAPException {
        if (s == null || s.trim().length() == 0) {
            return null;
        }
        ArrayList supportedEnvelops = new ArrayList();
        supportedEnvelops.add(s);
        return addUpgradeHeaderElement(supportedEnvelops.iterator());
    }

    public SOAPElement addTextNode(String text) throws SOAPException {
        if (((SOAPFactory)this.omTarget.getOMFactory()).getSOAPVersion() == SOAP11Version.getSingleton()) {
            return super.addTextNode(text);
        } else if (((SOAPFactory)this.omTarget.getOMFactory()).getSOAPVersion() == SOAP12Version.getSingleton()) {
            throw new SOAPException("Cannot add text node to SOAPHeader");
        } else {
            return null;
        }
    }

    public Iterator getChildElements(Name name) {
        QName qName = new QName(name.getURI(), name.getLocalName());
        return getChildren(omTarget.getChildrenWithName(qName));
    }

    public Iterator getChildElements() {
        return getChildren(omTarget.getChildren());
    }

    private Iterator getChildren(Iterator childIter) {
        Collection childElements = new ArrayList();
        while (childIter.hasNext()) {
            org.w3c.dom.Node domNode = (org.w3c.dom.Node)childIter.next();
            org.w3c.dom.Node saajNode = toSAAJNode(domNode);
            if (saajNode instanceof javax.xml.soap.Text) {
                childElements.add(saajNode);
            } else if (!(saajNode instanceof SOAPHeaderElement)) {
                // silently replace node, as per saaj 1.2 spec
                SOAPHeaderElement headerEle = new SOAPHeaderElementImpl((SOAPHeaderBlock)domNode);
                domNode.setUserData(SAAJ_NODE, headerEle, null);
                childElements.add(headerEle);
            } else {
                childElements.add(saajNode);
            }
        }
        return childElements.iterator();
    }
}
