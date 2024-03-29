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
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPVersion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jakarta.xml.soap.Name;
import jakarta.xml.soap.Node;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPBodyElement;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
// import org.apache.commons.collections4.IteratorUtils;
import java.util.Locale;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.StringWriter;
public class SOAPBodyImpl extends SOAPElementImpl<org.apache.axiom.soap.SOAPBody> implements SOAPBody {

    private static Log log = LogFactory.getLog(SOAPBodyImpl.class);

    private boolean isBodyElementAdded;

    /** @param omSOAPBody  */
    public SOAPBodyImpl(org.apache.axiom.soap.SOAPBody omSOAPBody) {
        super(omSOAPBody);
    }

    /* (non-Javadoc)
    * @see jakarta.xml.soap.SOAPElement#addChildElement(java.lang.String)
    */
    public SOAPElement addChildElement(String localName) throws SOAPException {
        if (omTarget.hasFault()) {
            throw new SOAPException("A SOAPFault has been already added to this SOAPBody");
        }
        SOAPBodyElementImpl<OMElement> childEle =
                new SOAPBodyElementImpl<OMElement>((OMElement)target.getOwnerDocument().createElementNS(null, localName));
        target.appendChild(childEle.target);
        isBodyElementAdded = true;
        return childEle;
    }

    public SOAPElement addChildElement(String localName, String prefix) throws SOAPException {
        String namespaceURI = getNamespaceURI(prefix);

        if (namespaceURI == null) {
            throw new SOAPException("Namespace not declared for the give prefix: " + prefix);
        }
        SOAPBodyElementImpl<OMElement> childEle =
                new SOAPBodyElementImpl<OMElement>(
                        (OMElement)target.getOwnerDocument().createElementNS(namespaceURI,
                                                                        localName));
        childEle.omTarget.setNamespace(childEle.omTarget.declareNamespace(namespaceURI, prefix));
        target.appendChild(childEle.target);
        childEle.setParentElement(this);
        return childEle;
    }

    @Override
    protected Element appendElement(Element child) throws SOAPException {    
        String namespaceURI = child.getNamespaceURI();
        String prefix = child.getPrefix();

        SOAPBodyElementImpl<?> childEle = toSOAPBodyElement(child);

        if (namespaceURI != null && namespaceURI.trim().length() > 0) {
            childEle.omTarget.setNamespace(childEle.omTarget.declareNamespace(namespaceURI, prefix));
        }
        target.appendChild(childEle.target);
        childEle.setParentElement(this);
        return childEle;
    }
    
    private SOAPBodyElementImpl<?> toSOAPBodyElement(Element element) {
        if (element instanceof SOAPBodyElementImpl) {
            return (SOAPBodyElementImpl<?>)element;
        } else {
            return new SOAPBodyElementImpl<OMElement>(((SOAPElementImpl<?>)element).omTarget);
        }
    }
    
    public SOAPElement addChildElement(SOAPElement soapElement) throws SOAPException {

	// TODO remove this if we ever find the problem with
	// soapElement.getAllAttributes() as explained below
	/*
	if (log.isDebugEnabled()) {
            String bodyStr = null;
            try {
                org.w3c.dom.Document document = soapElement.getOwnerDocument();
                Source source = new DOMSource(document);
                StringWriter out = new StringWriter(); 
                Result result = new StreamResult(out); 
                TransformerFactory tFactory = TransformerFactory.newInstance(); 
                Transformer transformer = tFactory.newTransformer(); 
                transformer.transform(source, result); 
                bodyStr = out.toString();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
	    log.debug("saaj SOAPBodyImpl starting on soapElement class: " +soapElement.getClass().getProtectionDomain().getCodeSource().getLocation() + " , name : " +soapElement.getLocalName()+ " , soapElement namespaceURI " + soapElement.getNamespaceURI() +" , soapElement prefix: " +soapElement.getPrefix() + " , Document as String: " + bodyStr);
	}
	*/

        String namespaceURI = soapElement.getNamespaceURI();
        String prefix = soapElement.getPrefix();
        String localName = soapElement.getLocalName();

        SOAPBodyElementImpl<OMElement> childEle;
        if (namespaceURI == null || namespaceURI.trim().length() == 0) {
            childEle =
                new SOAPBodyElementImpl<OMElement>(
                        (OMElement)target.getOwnerDocument().createElementNS(null, localName));
        } else {
            omTarget.declareNamespace(namespaceURI, prefix);
            childEle =
                new SOAPBodyElementImpl<OMElement>(
                        (OMElement)target.getOwnerDocument().createElementNS(namespaceURI,
                                                                        localName));            
        }

	/*
	if (log.isDebugEnabled()) {
            long size = IteratorUtils.size(soapElement.getAllAttributes());
            log.debug("saaj SOAPBodyImpl addChildElement() found attributes size: " + size);
            
            long size2 = IteratorUtils.size(soapElement.getChildElements());
            log.debug("saaj SOAPBodyImpl getChildElements() found elements size: " + size2);
	}
	*/

        for (Iterator iter = soapElement.getAllAttributes(); iter.hasNext();) {
            Name name = (Name)iter.next();
            try {
		if (name.getLocalName().toLowerCase().equals("xmlns")  && (name.getPrefix() == null || name.getPrefix().equals(""))) {
		}
                childEle.addAttribute(name, soapElement.getAttributeValue(name));
            } catch (Exception e) {

                log.error("addAttribute() failed on soapElement name: " +soapElement.getLocalName()+ " , soapElement namespaceURI " +soapElement.getNamespaceURI() +" , soapElement prefix: " +soapElement.getPrefix() + " , attribute name: " +name.getLocalName()+ " , name URI: " +name.getURI()+ " , name prefix: " +name.getPrefix()+ " , namespace-qualified name of the XML name: " +name.getQualifiedName()+ " , attribute value: " +soapElement.getAttributeValue(name)+ " , error: " + e.getMessage(), e);
                // throw e;
		// FIXME, AXIS2-6051 the move to jakarta 
		// now has attributes being returned in the 
		// unit tests such as xmlns="urn://mtom.test.org"
		// yet the same SOAPBody previously did not return
		// attributes here. There is no prefix and there is
		// an error because the Axiom method 
		// validateAttributeName() receives an empty 
		// namespaceURI, an empty prefix, localName: xmlns,
		// and throws:
		// NAMESPACE_ERR: An attempt is made to create or change an object in a way which is incorrect with regard to namespaces.
		// Yet xmlns="urn://mtom.test.org" still ends up
		// in the SOAPBody anyways.
            }
        }

        for (Iterator iter = soapElement.getChildElements(); iter.hasNext();) {
            Object o = iter.next();
            if (o instanceof Text) {
                childEle.addTextNode(((Text)o).getData());
                log.debug("addTextNode() found: " + ((Text)o).getData());
            } else {
                childEle.addChildElement((SOAPElement)o);
		SOAPElement se = (SOAPElement)o;
                log.debug("addChildElement() found: " + se.getLocalName());
            }
        }

        if (namespaceURI != null && namespaceURI.trim().length() > 0) {
            childEle.omTarget.setNamespace(childEle.omTarget.declareNamespace(namespaceURI, prefix));
        }
        target.appendChild(childEle.target);
        childEle.setParentElement(this);
	// TODO remove this if we ever find the problem with
	// soapElement.getAllAttributes() as explained above
	/*
        String bodyStr2 = null;
        try {
            org.w3c.dom.Document document2 = soapElement.getOwnerDocument();
            Source source2 = new DOMSource(document2);
            StringWriter out2 = new StringWriter(); 
            Result result2 = new StreamResult(out2); 
            TransformerFactory tFactory2 = TransformerFactory.newInstance(); 
            Transformer transformer2 = tFactory2.newTransformer(); 
            transformer2.transform(source2, result2); 
            bodyStr2 = out2.toString();
	    log.error("saaj childEle as String: " + bodyStr2);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
	}
	*/
        return childEle;
    }

    /* (non-Javadoc)
    * @see jakarta.xml.soap.SOAPElement#addChildElement(java.lang.String, java.lang.String, java.lang.String)
    */
    public SOAPElement addChildElement(String localName, String prefix, String uri)
            throws SOAPException {
        if (omTarget.hasFault()) {
            throw new SOAPException("A SOAPFault has been already added to this SOAPBody");
        }
        if (prefix == null) {
            prefix = "";
        }
        SOAPBodyElementImpl<OMElement> childEle;
        if (uri == null || "".equals(uri)) {
            childEle = new SOAPBodyElementImpl<OMElement>(
                    (OMElement)target.getOwnerDocument().createElementNS(null, localName));
        } else if (prefix.length() == 0) {
            childEle = new SOAPBodyElementImpl<OMElement>(
                (OMElement)target.getOwnerDocument().createElementNS(uri,
                                                                localName));
        } else {
            childEle = new SOAPBodyElementImpl<OMElement>(
                    (OMElement)target.getOwnerDocument().createElementNS(uri,
                                                                    prefix + ":" + localName));
        }
        childEle.omTarget.setNamespace(omTarget.getOMFactory().createOMNamespace(uri, prefix));
        target.appendChild(childEle.target);
        isBodyElementAdded = true;
        childEle.setParentElement(this);
        return childEle;
    }

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to this <code>SOAPBody</code>
     * object.
     *
     * @return the new <code>SOAPFault</code> object
     * @throws SOAPException if there is a SOAP error
     */
    public SOAPFault addFault() throws SOAPException {
        if (isBodyElementAdded) {
            throw new SOAPException("A SOAPBodyElement has been already added to this SOAPBody");
        }
        org.apache.axiom.soap.SOAPFault fault = ((SOAPFactory)this.omTarget.getOMFactory()).createSOAPFault(omTarget);
        SOAPFaultImpl saajSOAPFault = new SOAPFaultImpl(fault);
        // set default fault code and string
        saajSOAPFault.setDefaults();
        
        return saajSOAPFault;
    }

    /**
     * Indicates whether a <code>SOAPFault</code> object exists in this <code>SOAPBody</code>
     * object.
     *
     * @return <code>true</code> if a <code>SOAPFault</code> object exists in this
     *         <code>SOAPBody</code> object; <code>false</code> otherwise
     */
    public boolean hasFault() {
        return omTarget.hasFault();
    }

    /**
     * Returns the <code>SOAPFault</code> object in this <code>SOAPBody</code> object.
     *
     * @return the <code>SOAPFault</code> object in this <code>SOAPBody</code> object
     */
    public SOAPFault getFault() {
        if (omTarget.hasFault()) {
            return (SOAPFault)toSAAJNode((org.w3c.dom.Node)omTarget.getFault());
        }
        return null;
    }

    /**
     * Creates a new <code>SOAPBodyElement</code> object with the specified name and adds it to this
     * <code>SOAPBody</code> object.
     *
     * @param name a <code>Name</code> object with the name for the new <code>SOAPBodyElement</code>
     *             object
     * @return the new <code>SOAPBodyElement</code> object
     * @throws SOAPException if a SOAP error occurs
     */
    public SOAPBodyElement addBodyElement(Name name) throws SOAPException {
        return (SOAPBodyElement)addChildElement(name);
    }

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to this <code>SOAPBody</code> object.
     * The new <code>SOAPFault</code> will have a <code>faultcode</code> element that is set to the
     * <code>faultCode</code> parameter and a <code>faultstring</code> set to
     * <code>faultstring</code> and localized to <code>locale</code>.
     *
     * @param faultCode   a <code>Name</code> object giving the fault code to be set; must be one of
     *                    the fault codes defined in the SOAP 1.1 specification and of type QName
     * @param faultString a <code>String</code> giving an explanation of the fault
     * @param locale      a <code>Locale</code> object indicating the native language of the
     *                    <ocde>faultString</code>
     * @return the new <code>SOAPFault</code> object
     * @throws SOAPException if there is a SOAP error
     */
    public SOAPFault addFault(Name faultCode, String faultString, Locale locale)
            throws SOAPException {
        org.apache.axiom.soap.SOAPFault fault = ((SOAPFactory)this.omTarget.getOMFactory()).createSOAPFault(omTarget);
        SOAPFaultImpl faultImpl = new SOAPFaultImpl(fault);
        faultImpl.setFaultCode(faultCode);

        if (locale != null) {
            faultImpl.setFaultString(faultString, locale);
        } else {
            faultImpl.setFaultString(faultString);
        }

        return faultImpl;
    }

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to this <code>SOAPBody</code> object.
     * The new <code>SOAPFault</code> will have a <code>faultcode</code> element that is set to the
     * <code>faultCode</code> parameter and a <code>faultstring</code> set to
     * <code>faultstring</code>.
     *
     * @param faultCode   a <code>Name</code> object giving the fault code to be set; must be one of
     *                    the fault codes defined in the SOAP 1.1 specification and of type QName
     * @param faultString a <code>String</code> giving an explanation of the fault
     * @return the new <code>SOAPFault</code> object
     * @throws SOAPException if there is a SOAP error
     */
    public SOAPFault addFault(Name faultCode, String faultString) throws SOAPException {
        return addFault(faultCode, faultString, null);
    }

    /**
     * Adds the root node of the DOM <code>Document</code> to this <code>SOAPBody</code> object.
     * <p/>
     * Calling this method invalidates the <code>document</code> parameter. The client application
     * should discard all references to this <code>Document</code> and its contents upon calling
     * <code>addDocument</code>. The behavior of an application that continues to use such
     * references is undefined.
     *
     * @param document the <code>Document</code> object whose root node will be added to this
     *                 <code>SOAPBody</code>
     * @return the <code>SOAPBodyElement</code> that represents the root node that was added
     * @throws SOAPException if the <code>Document</code> cannot be added
     */
    public SOAPBodyElement addDocument(Document document) throws SOAPException {
        Element docEle = document.getDocumentElement();

        SOAPElement saajSOAPEle = (SOAPElement)toSAAJNode(docEle, this);
        SOAPBodyElementImpl<OMElement> bodyEle =
                new SOAPBodyElementImpl<OMElement>(((SOAPElementImpl<?>)saajSOAPEle).omTarget);
        addChildElement(bodyEle);
        return bodyEle;
    }

    /**
     * Creates a new <code>SOAPBodyElement</code> object with the specified name and adds it to this
     * <code>SOAPBody</code> object.
     *
     * @param qname a <code>QName</code> object with the name for the new <code>SOAPBodyElement</code>
     *              object
     * @return the new <code>SOAPBodyElement</code> object
     * @throws SOAPException if a SOAP error occurs
     */
    public SOAPBodyElement addBodyElement(QName qname) throws SOAPException {
        return (SOAPBodyElement)addChildElement(qname);
    }


    public SOAPFault addFault(QName faultcode, String faultString) throws SOAPException {
        return addFault(faultcode, faultString, null);
    }

    /**
     * Creates a new <code>SOAPFault</code> object and adds it to this <code>SOAPBody</code> object.
     * The new <code>SOAPFault</code> will have a <code>faultcode</code> element that is set to the
     * <code>faultCode</code> parameter and a <code>faultstring</code> set to
     * <code>faultstring</code> and localized to <code>locale</code>.
     *
     * @param faultCode   a <code>QName</code> object giving the fault code to be
     * @param faultString a <code>String</code> giving an explanation of the fault
     * @param locale      a <code>Locale</code> object indicating the native language of the
     *                    <ocde>faultString</code>
     * @return the new <code>SOAPFault</code> object
     * @throws SOAPException if there is a SOAP error
     */
    public SOAPFault addFault(QName faultCode, String faultString, Locale locale)
            throws SOAPException {
        org.apache.axiom.soap.SOAPFault fault = ((SOAPFactory)this.omTarget.getOMFactory()).createSOAPFault(omTarget);
        SOAPFaultImpl faultImpl = new SOAPFaultImpl(fault);

        if (faultImpl != null) {
            faultImpl.setFaultCode(faultCode);
            if (locale != null) {
                faultImpl.setFaultString(faultString, locale);
            } else {
                faultImpl.setFaultString(faultString);
            }
        }
        return faultImpl;
    }

    /**
     * Creates a new DOM org.w3c.dom.Document and sets the first child of this SOAPBody as its
     * document element. The child SOAPElement is removed as part of the process.
     *
     * @return The org.w3c.dom.Document representation of the SOAPBody content.
     * @throws SOAPException - if there is not exactly one child SOAPElement of the SOAPBody.
     */
    public Document extractContentAsDocument() throws SOAPException {
        Iterator childElements = this.getChildElements();
        org.w3c.dom.Node domNode = null;
        int childCount = 0;
        while (childElements.hasNext()) {
            domNode = (org.w3c.dom.Node)childElements.next();
            childCount++;
            if (childCount > 1) {
                throw new SOAPException("SOAPBody contains more than one child element");
            }
        }
        //The child SOAPElement is removed as part of the process
        this.removeContents();


        Document document;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();
            Element element =
                    document.createElementNS(domNode.getNamespaceURI(), domNode.getLocalName());
            element.setNodeValue(domNode.getNodeValue());
            document.appendChild(element);
        } catch (ParserConfigurationException e) {
            throw new SOAPException(e);
        }
        return document;
    }

    private jakarta.xml.soap.Node toSAAJNode(org.w3c.dom.Node node,
                                           SOAPElement parent) throws SOAPException {
        if (node == null) {
            return null;
        }
        if (node instanceof org.w3c.dom.Text) {
            org.w3c.dom.Text domText = (org.w3c.dom.Text)node;
            return new TextImplEx(domText.getData());
        }
        if (node instanceof org.w3c.dom.Comment) {
            org.w3c.dom.Comment domText = (org.w3c.dom.Comment)node;
            return new CommentImpl(domText.getData());
        }
        Element domEle = ((Element)node);
        int indexOfColon = domEle.getTagName().indexOf(":");
        OMNamespace ns;
        String localName;
        if (indexOfColon != -1) {
            localName = domEle.getTagName().substring(indexOfColon + 1);
            ns = omTarget.getOMFactory().createOMNamespace(domEle.getNamespaceURI(),
                                   domEle.getTagName().substring(0, indexOfColon));
        } else {
            localName = domEle.getLocalName();
            if (localName == null) {  //it is possible that localname isn't set but name is set
                localName = domEle.getTagName();
            }     
            
            String prefix = domEle.getPrefix();
            if(prefix == null) {
                prefix = "";
            }
            if (domEle.getNamespaceURI() != null) {
                ns = omTarget.getOMFactory().createOMNamespace(domEle.getNamespaceURI(), prefix);
            } else {
                if (prefix != null) {
                    ns = omTarget.getOMFactory().createOMNamespace("", prefix);
                } else {
                    ns = omTarget.getOMFactory().createOMNamespace("", "");
                    
                }
            }
        }
        OMElement eleImpl = this.omTarget.getOMFactory().createOMElement(localName, ns);
        getOwnerDocument().adoptNode((Element)eleImpl);

        SOAPElementImpl<OMElement> saajEle = new SOAPElementImpl<OMElement>(eleImpl);

        saajEle.setParentElement(parent);
        NamedNodeMap domAttrs = domEle.getAttributes();
        for (int i = 0; i < domAttrs.getLength(); i++) {
            org.w3c.dom.Node attrNode = domAttrs.item(i);
            String attrLocalName = attrNode.getLocalName();
            if (attrLocalName == null) {
                attrLocalName = attrNode.getNodeName();
            }
            if (attrLocalName == null) {
                //local part is required.  "" is allowed to preserve compatibility with QName 1.0
                attrLocalName = "";
            } 
            saajEle.addAttribute(new PrefixedQName(attrNode.getNamespaceURI(),
                                                   attrLocalName,
                                                   attrNode.getPrefix()),
                                                   attrNode.getNodeValue());                
        }

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childSAAJNode = toSAAJNode(childNodes.item(i), saajEle);
            if (childSAAJNode instanceof jakarta.xml.soap.Text) {
                saajEle.addTextNode(childSAAJNode.getValue());
            } else {
                saajEle.addChildElement((jakarta.xml.soap.SOAPElement)childSAAJNode);
            }
        }
        return saajEle;
    }

    public Iterator getChildElements(Name name) {
        QName qName = new QName(name.getURI(), name.getLocalName());
        return getChildren(omTarget.getChildrenWithName(qName));
    }

    public SOAPElement addAttribute(QName qname, String value) throws SOAPException {
        OMNamespace omNamespace = this.omTarget.getOMFactory().createOMNamespace(qname.getNamespaceURI(), qname.getPrefix());
        this.omTarget.addAttribute(qname.getLocalPart(), value, omNamespace);
        return this;
    }

    public SOAPElement addChildElement(QName qname) throws SOAPException {
        if (omTarget.hasFault()) {
            throw new SOAPException("A SOAPFault has been already added to this SOAPBody");
        }
        SOAPBodyElementImpl<OMElement> childEle;
        if (qname.getNamespaceURI() == null || "".equals(qname.getNamespaceURI())) {
            childEle = new SOAPBodyElementImpl<OMElement>(
                    (OMElement)target.getOwnerDocument().createElementNS(null, qname.getLocalPart()));
        }else if(null == qname.getPrefix() || "".equals(qname.getPrefix().trim())) {
            childEle = new SOAPBodyElementImpl<OMElement>(
                    (OMElement)target.getOwnerDocument().createElementNS(qname.getNamespaceURI(),
                                                                            qname.getLocalPart()));
        }else {
            childEle = new SOAPBodyElementImpl<OMElement>(
                    (OMElement)target.getOwnerDocument().createElementNS(qname.getNamespaceURI(),
                                                                    qname.getPrefix() + ":" +
                                                                            qname.getLocalPart()));
        }
        childEle.omTarget.setNamespace(omTarget.getOMFactory().createOMNamespace(qname.getNamespaceURI(), qname.getPrefix()));

        target.appendChild(childEle.target);
        isBodyElementAdded = true;
        childEle.setParentElement(this);
        return childEle;
    }

    public QName createQName(String localName, String prefix) throws SOAPException {
        if (((SOAPFactory)this.omTarget.getOMFactory()).getSOAPVersion() == SOAPVersion.SOAP11) {
            return super.createQName(localName, prefix);
        } else if (((SOAPFactory)this.omTarget.getOMFactory()).getSOAPVersion() == SOAPVersion.SOAP12) {
            if (this.omTarget.findNamespaceURI(prefix) == null) {
                throw new SOAPException("Only Namespace Qualified elements are allowed");
            } else {
                return super.createQName(localName, prefix);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }


    public Iterator getAllAttributesAsQNames() {
        return super.getAllAttributesAsQNames();
    }

    public String getAttributeValue(QName qname) {
        return super.getAttributeValue(qname);
    }

    public Iterator getChildElements(QName qname) {
        return super.getChildElements(qname);
    }

    public QName getElementQName() {
        return super.getElementQName();
    }

    public boolean removeAttribute(QName qname) {
        return super.removeAttribute(qname);
    }

    public SOAPElement setElementQName(QName qname) throws SOAPException {
        return super.setElementQName(qname);
    }

    public Iterator getChildElements() {
        return getChildren(omTarget.getChildren());
    }

    public SOAPElement addTextNode(String text) throws SOAPException {
        return super.addTextNode(text);
    }

    private Iterator getChildren(Iterator<? extends OMNode> childIter) {
        Collection childElements = new ArrayList();
        while (childIter.hasNext()) {
            org.w3c.dom.Node domNode = (org.w3c.dom.Node)childIter.next();
            org.w3c.dom.Node saajNode = toSAAJNode(domNode);
            if (saajNode instanceof jakarta.xml.soap.Text) {
                childElements.add(saajNode);
            } else if (!(saajNode instanceof SOAPBodyElement)) {
                // silently replace node, as per saaj 1.2 spec
                if (domNode instanceof Element) {
                    if (omTarget.hasFault()) {

                        SOAPFactory omFactory = (SOAPFactory)this.omTarget.getOMFactory();
                        org.apache.axiom.soap.SOAPFault fault = omFactory.createSOAPFault(omTarget);
                        childElements.add(new SOAPFaultImpl(fault));
                    } else {
                        childElements.add(new SOAPBodyElementImpl<OMElement>((OMElement)domNode));
                    }
                }
            } else {
                childElements.add(saajNode);
            }
        }
        return childElements.iterator();
    }
}
