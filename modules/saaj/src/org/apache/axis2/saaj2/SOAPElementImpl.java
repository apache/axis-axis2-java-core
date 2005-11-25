package org.apache.axis2.saaj2;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;

import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.dom.DocumentImpl;
import org.apache.axis2.om.impl.dom.ElementImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class SOAPElementImpl extends NodeImplEx implements SOAPElement {

	
	/**
	 * Using a delegate because we can't extend from 
	 * org.apache.axis2.om.impl.dom.ElementImpl since this class
	 * must extend SNodeImpl
	 */
	private ElementImpl element;
	
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

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPElement#addAttribute(javax.xml.soap.Name, java.lang.String)
	 */
	public SOAPElement addAttribute(Name name, String value) throws SOAPException {
		this.element.setAttributeNS(name.getURI(), name.getPrefix() + ":" + name.getLocalName(), value);
		return this;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPElement#addChildElement(javax.xml.soap.Name)
	 */
	public SOAPElement addChildElement(Name name) throws SOAPException {
		this.addChildElement(name.getLocalName(),name.getPrefix(), name.getURI());
		return this;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPElement#addChildElement(javax.xml.soap.SOAPElement)
	 */
	public SOAPElement addChildElement(SOAPElement soapElement) throws SOAPException {
		this.element.appendChild(soapElement);
		return this;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public SOAPElement addChildElement(String localName, String prefix, String uri) throws SOAPException {
		this.element.declareNamespace(uri, prefix);
		this.addChildElement(localName, prefix);
		return this;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String, java.lang.String)
	 */
	public SOAPElement addChildElement(String localName, String prefix) throws SOAPException {
		String namespaceURI = this.getNamespaceURI(prefix);
		if(namespaceURI == null) {
			throw new SOAPException("Namespace not declared for the give prefix: " + prefix);
		}
		Element elem = this.getOwnerDocument().createElementNS(namespaceURI, localName);
		this.element.appendChild(elem);
		return this;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPElement#addChildElement(java.lang.String)
	 */
	public SOAPElement addChildElement(String localName) throws SOAPException {
		Element elem = this.getOwnerDocument().createElement(localName);
		this.element.appendChild(elem);
		return this;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPElement#addNamespaceDeclaration(java.lang.String, java.lang.String)
	 */
	public SOAPElement addNamespaceDeclaration(String prefix, String uri) throws SOAPException {
		this.element.declareNamespace(prefix, uri);
		return this;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPElement#addTextNode(java.lang.String)
	 */
	public SOAPElement addTextNode(String text) throws SOAPException {
		//OmElement.setText() will remove all the other text nodes that it contains
		//Therefore create a text node and add it
		Text textNode = this.getOwnerDocument().createTextNode(text);
		this.element.appendChild(textNode);
		return this;
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPElement#getAllAttributes()
	 */
	public Iterator getAllAttributes() {
		return this.element.getAllAttributes();
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPElement#getAttributeValue(javax.xml.soap.Name)
	 */
	public String getAttributeValue(Name name) {
        //This method is waiting on the finalization of the name for a method
        //in OMElement that returns a OMAttribute from an input QName
		return this.element.getFirstAttribute(
                new QName(name.getURI(),
                        name.getLocalName(),
                        name.getPrefix()))
                .getAttributeValue();
	}

	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPElement#getChildElements()
	 */
	public Iterator getChildElements() {
        //Actually all the children are being treated as OMNodes and are being
        //wrapped accordingly to a single type (SOAPElement) and being returned in an iterator.
        //Text nodes and element nodes are all being treated alike here. Is that a serious issue???
        Iterator childIter = this.element.getChildren();
        ArrayList arrayList = new ArrayList();
        while (childIter.hasNext()) {
            Object o = childIter.next();
            if (o instanceof javax.xml.soap.Node) {
            	arrayList.add(o);
                //javax.xml.soap.Node childElement = new NodeImpl((org.apache.axis2.om.OMNode)o);

            }
        }
        return arrayList.iterator();
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
		return ((DocumentImpl)this.getOwnerDocument()).getCharsetEncoding();
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
            	OMNamespace ns = (OMNamespace)o;
            	if(ns.getPrefix() != null) {
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
                	OMNamespace ns = (OMNamespace)o;
                	if(ns.getPrefix() != null) {
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
        org.apache.axis2.om.OMAttribute attr = this.element.getFirstAttribute(
                new QName(name.getURI(),
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
            if (o instanceof org.apache.axis2.om.OMNode)
                ((org.apache.axis2.om.OMNode) o).detach();
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
		((DocumentImpl)this.getOwnerDocument()).setCharsetEncoding(encodingStyle);
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
	
	
	
}
