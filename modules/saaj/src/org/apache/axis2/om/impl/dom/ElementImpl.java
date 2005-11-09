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

import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMConstants;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.OMSerializerUtil;
import org.apache.axis2.om.impl.llom.OMStAXWrapper;
import org.apache.axis2.om.impl.llom.traverse.OMChildElementIterator;
import org.apache.axis2.om.impl.llom.util.EmptyIterator;
import org.apache.axis2.om.util.ElementHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Implementation of the org.w3c.dom.Element and org.apache.axis2.om.Element
 * interfaces.
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class ElementImpl extends ParentNode implements Element,OMElement, OMConstants {
	
	protected OMNamespace namespace;
	protected String localName;
	private AttributeMap attributes;
	private HashMap namespaces;
	
	/**
	 * @param ownerDocument
	 */
	public ElementImpl(DocumentImpl ownerDocument, String tagName) {
		super(ownerDocument);
		if(ownerDocument.firstChild == null)
			ownerDocument.firstChild = this;
		this.localName = tagName;
	}
	
	/**
	 * Create a  new element with the namespace
	 * @param ownerDocument
	 * @param tagName
	 * @param ns
	 */
	public ElementImpl(DocumentImpl ownerDocument, String tagName, NamespaceImpl ns) {
		super(ownerDocument);
		this.localName = tagName;
		this.namespace = ns;
		this.declareNamespace(ns);
	}
	
	public ElementImpl(DocumentImpl ownerDocument, String tagName, NamespaceImpl ns, OMXMLParserWrapper builder) {
		super(ownerDocument);
		this.localName = tagName;
		this.namespace = ns;
		this.builder = builder;
		this.declareNamespace(ns);
	}
	
	public ElementImpl(ParentNode parentNode, String tagName, NamespaceImpl ns) {
		this((DocumentImpl)parentNode.getOwnerDocument(), tagName, ns);
		this.parentNode.addChild(this);
	}
	
	public ElementImpl(ParentNode parentNode, String tagName, NamespaceImpl ns, OMXMLParserWrapper builder) {
		this(tagName,ns,builder);
		if(this.parentNode != null) {
			this.ownerNode = (DocumentImpl)parentNode.getOwnerDocument();
			this.isOwned(true);
			this.parentNode.addChild(this);
		}
		
	}
	
	public ElementImpl(String tagName, NamespaceImpl ns, OMXMLParserWrapper builder) {
		this.localName = tagName;
		this.namespace = ns;
		this.builder = builder;
		if(ns != null) {
			this.declareNamespace(ns);
		}
	}
	
	
	///
	///org.w3c.dom.Node methods
	///
	
	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	public short getNodeType() {
		return Node.ELEMENT_NODE;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getNodeName()
	 */
	public String getNodeName() {
		return this.localName;
	}

	/**
	 * Returns the value of the namespace URI
	 */
	public String getNamespaceURI() {
		return this.namespace.getName();
	}
	
	///
	///org.apache.axis2.om.OMNode methods
	///
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#getType()
	 */
	public int getType() throws OMException {
		return Node.ELEMENT_NODE;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#setType(int)
	 */
	public void setType(int nodeType) throws OMException {
		//Do nothing ...
		//This is an Eement Node...
	}


	///
	/// org.w3c.dom.Element methods
	///
	
	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#getTagName()
	 */
	public String getTagName() {
		return this.localName;
	}

	/**
	 * Removes an attribute by name.
	 * @param name The name of the attribute to remove
	 * @see org.w3c.dom.Element#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) throws DOMException {
		if(this.isReadonly()) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
		}
		
		if(this.attributes != null) {
			this.attributes.removeNamedItem(name);
		}
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#hasAttribute(java.lang.String)
	 */
	public boolean hasAttribute(String name) {
		return this.getAttributeNode(name) != null;
	}

	/**
	 * Look in the local list of attributes and return if found
	 * if the local list is null return null
	 * @see org.w3c.dom.Element#getAttribute(java.lang.String)
	 */
	public String getAttribute(String arg0) {
		if(attributes == null) {
			return "";
		} else {
			return ((Attr)attributes.getNamedItem(arg0)).getValue();
		}
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#removeAttributeNS(java.lang.String, java.lang.String)
	 */
	public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
		if(this.isReadonly()) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
		}
		
		if(this.attributes != null) {
			this.attributes.removeNamedItemNS(namespaceURI, localName);
		}
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#setAttribute(java.lang.String, java.lang.String)
	 */
	public void setAttribute(String name, String value) throws DOMException {
		//Check for invalid charaters
		if(!DOMUtil.isValidChras(name)) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
		}
		
		if(this.isReadonly()) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
		}
		if(this.attributes == null) {
			this.attributes = new AttributeMap((ParentNode)this.ownerNode);
		}
		this.attributes.setNamedItem(new AttrImpl(name, value));
	}

	/**
	 * Returns whether the  given attr is available or not
	 * @see org.w3c.dom.Element#hasAttributeNS(java.lang.String, java.lang.String)
	 */
	public boolean hasAttributeNS(String namespaceURI, String localName) {
		return this.getAttributeNodeNS(namespaceURI, localName) != null;
	}

	/**
	 * Retrieves an attribute node by name. 
	 * @see org.w3c.dom.Element#getAttributeNode(java.lang.String)
	 */
	public Attr getAttributeNode(String name) {
		if(this.attributes == null) {
			return null;
		}
		
		return (AttrImpl)this.attributes.getNamedItem(name);
	}

	/**
	 * Removes the specified attribute node
	 * @see org.w3c.dom.Element#removeAttributeNode(org.w3c.dom.Attr)
	 */
	public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
		if(isReadonly()) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
		} 
		if(this.attributes == null || this.attributes.getNamedItem(oldAttr.getName()) == null) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_FOUND_ERR", null);
            throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
		}
		AttrImpl tempAttr = (AttrImpl)this.attributes.removeNamedItem(oldAttr.getName());
		return tempAttr;
	}

	/**
	 * Adds a new attribute node.
	 * @see org.w3c.dom.Element#setAttributeNode(org.w3c.dom.Attr)
	 */
	public Attr setAttributeNode(Attr attr) throws DOMException {
		AttrImpl attrImpl = (AttrImpl)attr;

		if(attrImpl.isOwned()) {//check for ownership
			if(!this.getOwnerDocument().equals(attr.getOwnerDocument())) {
				String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null);
	            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
			}
		}
		
		if(this.isReadonly()) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
		}
		
		//check whether the attr is in use
		if(attrImpl.isUsed()) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INUSE_ATTRIBUTE_ERR", null);
            throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, msg);
		}
		
		if(this.attributes == null) {
			this.attributes = new AttributeMap((ParentNode)this.ownerNode);
		}

		return (Attr)this.attributes.setNamedItem(attr);

	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#setAttributeNodeNS(org.w3c.dom.Attr)
	 */
	public Attr setAttributeNodeNS(Attr attr) throws DOMException {
		
		AttrImpl attrImpl = (AttrImpl)attr;

		if(attrImpl.isOwned()) {//check for ownership
			if(!this.getOwnerDocument().equals(attr.getOwnerDocument())) {
				String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "WRONG_DOCUMENT_ERR", null);
	            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, msg);
			}
		}
		
		if(this.isReadonly()) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
		}
		
		//check whether the attr is in use
		if(attrImpl.isUsed()) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INUSE_ATTRIBUTE_ERR", null);
            throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, msg);
		}
		
		if(this.attributes == null) {
			this.attributes = new AttributeMap((ParentNode)this.ownerNode);
		}

		//handle the namespaces
        if (attr.getNamespaceURI() != null && findNamespace(attr.getNamespaceURI(), attr.getPrefix()) == null) {
        	//TODO checkwhether the same ns is declared with a different prefix and remove it
        	this.declareNamespace(new NamespaceImpl(attr.getNamespaceURI(),attr.getPrefix()));
        }
		
		return (Attr)this.attributes.setNamedItemNS(attr);
		
	}


	/**
	 * Retrieves an attribute value by local name and namespace URI. 
	 * @see org.w3c.dom.Element#getAttributeNS(java.lang.String, java.lang.String)
	 */
	public String getAttributeNS(String namespaceURI, String localName) {
		if(this.attributes == null) {
			return "";
		}
		
		return this.attributes.getNamedItemNS(namespaceURI, localName).getNodeValue();
	}

	/**
	 * Adds a new attribute
	 * @see org.w3c.dom.Element#setAttributeNS(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
		
        if (namespaceURI != null && findNamespace(namespaceURI, DOMUtil.getPrefix(qualifiedName)) == null) {
        	//TODO checkwhether the same ns is declared with a different prefix and remove it
        	this.declareNamespace(new NamespaceImpl(namespaceURI,DOMUtil.getPrefix(qualifiedName)));
        }
        
		this.addAttribute(namespaceURI,qualifiedName,value);
	}
	
	private OMAttribute addAttribute(String namespaceURI, String qualifiedName, String value) throws DOMException {
		if(!DOMUtil.isValidChras(qualifiedName)) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INVALID_CHARACTER_ERR", null);
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, msg);
		}
		
		if(this.isReadonly()) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
		}
		
		if(!DOMUtil.isValidNamespace(namespaceURI, qualifiedName)) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NAMESPACE_ERR", null);
            throw new DOMException(DOMException.NAMESPACE_ERR, msg);			
		}
		
		if(this.attributes == null) {
			this.attributes = new AttributeMap((ParentNode)this.ownerNode);
		}
		
		//Check whether there's an existing Attr with same local name and namespace URI
		Attr attributeNode = this.getAttributeNodeNS(namespaceURI, DOMUtil.getLocalName(qualifiedName));
		if(attributeNode != null) {
			AttrImpl tempAttr = ((AttrImpl)attributeNode);
			tempAttr.setOMNamespace(new NamespaceImpl(namespaceURI,DOMUtil.getPrefix(qualifiedName)));
			tempAttr.setAttributeValue(value);
			return tempAttr;
		} else {
			NamespaceImpl ns = new NamespaceImpl(namespaceURI, DOMUtil.getPrefix(qualifiedName));
			AttrImpl attr = new AttrImpl(DOMUtil.getLocalName(qualifiedName),ns,value);
			return attr;
		}
	}

	/**
	 * Retrieves an Attr node by local name and namespace URI. 
	 * @see org.w3c.dom.Element#getAttributeNodeNS(java.lang.String, java.lang.String)
	 */
	public Attr getAttributeNodeNS(String namespaceURI, String localName) {
		return (this.attributes == null)?null:(Attr)this.attributes.getNamedItemNS(namespaceURI,localName);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#getElementsByTagNameNS(java.lang.String, java.lang.String)
	 */
	public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
		return new NodeListImpl(this, namespaceURI, localName);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#getElementsByTagName(java.lang.String)
	 */
	public NodeList getElementsByTagName(String name) {
		return new NodeListImpl(this, name);
	}
	
	///
	///OmElement methods
	///

	/**
	 * @see org.apache.axis2.om.OMElement#addAttribute(org.apache.axis2.om.OMAttribute)
	 */
	public OMAttribute addAttribute(OMAttribute attr) {
		
        OMNamespace namespace = attr.getNamespace();
        if ( namespace != null && this.findNamespace(namespace.getName(), namespace.getPrefix()) == null) {
            this.declareNamespace(namespace.getName(), namespace.getPrefix());
        }
        
		if(attr.getNamespace() != null) { //If the attr has a namespace
			return (AttrImpl)this.setAttributeNode((Attr)attr);
		} else {
			return (AttrImpl)this.setAttributeNodeNS((Attr)attr);
		}
	}

	/**
	 * The behaviour of this is the same as org.w3c.dom.Element#setAttributeNS
	 * @see org.apache.axis2.om.OMElement#addAttribute(java.lang.String, java.lang.String, org.apache.axis2.om.OMNamespace)
	 */
	public OMAttribute addAttribute(String attributeName, String value, OMNamespace ns) {
		
        if (ns != null && findNamespace(ns.getName(), ns.getPrefix()) != null){
            declareNamespace(ns);
        }
        
		return this.addAttribute(ns.getName(),attributeName,value);
	}

	/**
	 * @see org.apache.axis2.om.OMElement#declareNamespace(org.apache.axis2.om.OMNamespace)
	 */
	public OMNamespace declareNamespace(OMNamespace namespace) {
        if (namespaces == null) {
            this.namespaces = new HashMap(5);
        }
        namespaces.put(namespace.getPrefix(), namespace);
        return namespace;
	}

	/**
	 * @see org.apache.axis2.om.OMElement#declareNamespace(java.lang.String, java.lang.String)
	 */
	public OMNamespace declareNamespace(String uri, String prefix) {
        NamespaceImpl ns = new NamespaceImpl(uri, prefix);
        return declareNamespace(ns);
	}

	/**
	 * @see org.apache.axis2.om.OMElement#findNamespace(java.lang.String, java.lang.String)
	 */
	public OMNamespace findNamespace(String uri, String prefix) {

        // check in the current element
        OMNamespace namespace = findDeclaredNamespace(uri, prefix);
        if (namespace != null) {
            return namespace;
        }

        // go up to check with ancestors
        if (this.parentNode != null) {
            //For the OMDocumentImpl there won't be any explicit namespace
            //declarations, so going up the parent chain till the document
            //element should be enough.
            if (parentNode instanceof OMElement) {
                namespace = ((ElementImpl) parentNode).findNamespace(uri, prefix);
            }
        }

        if (namespace == null && uri != null && prefix != null
                && prefix.equals(OMConstants.XMLNS_PREFIX)
                && uri.equals(OMConstants.XMLNS_URI)) {
            declareNamespace(OMConstants.XMLNS_URI, OMConstants.XMLNS_PREFIX);
            namespace = findNamespace(uri, prefix);
        }
        return namespace;
	}

    public OMNamespace findNamespaceURI(String prefix) {
        // TODO Ruchith please fix this
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * This will ckeck for the namespace <B>only</B> in the current Element.
     * <p/>
     * This can also be used to retrieve the prefix of a known namespace URI
     */
    private OMNamespace findDeclaredNamespace(String uri, String prefix) {
        if (namespaces == null) {
            return null;
        }
        if (prefix == null || "".equals(prefix)) {
            Iterator namespaceListIterator = namespaces.values().iterator();
            while (namespaceListIterator.hasNext()) {
                OMNamespace omNamespace =
                        (OMNamespace) namespaceListIterator.next();
                if (omNamespace.getName() != null &&
                        omNamespace.getName().equals(uri)) {
                    return omNamespace;
                }
            }
            return null;
        } else {
            return (OMNamespace) namespaces.get(prefix);
        }
    }
    
	/**
	 * Returns a named attribute if present.
	 * @see org.apache.axis2.om.OMElement#getAttribute(javax.xml.namespace.QName)
	 */
	public OMAttribute getAttribute(QName qname) {
		if(this.attributes == null) {
			return null;
		}
		
		if(qname.getNamespaceURI() == null || qname.getNamespaceURI().equals("")){
			return (AttrImpl)this.getAttributeNode(qname.getLocalPart());
		} else {
			return (AttrImpl)this.getAttributeNodeNS(qname.getNamespaceURI(), qname.getLocalPart());
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#getBuilder()
	 */
	public OMXMLParserWrapper getBuilder() {
		return this.builder;
	}


	/**
	 * Returns the first attribute of the set of attributes if there
	 * are any attributes. Otherwise returns null
	 * @see org.apache.axis2.om.OMElement#getFirstAttribute(javax.xml.namespace.QName)
	 */
	public OMAttribute getFirstAttribute(QName qname) {
		if(this.attributes == null) {
			return null;
		} else {
			return (AttrImpl)this.attributes.getItem(0);
		}
	}

	/**
	 * Returns the first Element node
	 * @see org.apache.axis2.om.OMElement#getFirstElement()
	 */
	public OMElement getFirstElement() {
        OMNode node = getFirstOMChild();
        while (node != null) {
            if (node.getType() == Node.ELEMENT_NODE) {
                return (OMElement) node;
            } else {
                node = node.getNextOMSibling();
            }
        }
        return null;
	}

	/**
	 * Returns the namespace of this element
	 * @see org.apache.axis2.om.OMElement#getNamespace()
	 */
	public OMNamespace getNamespace() throws OMException {
		return this.namespace;
	}

	/**
	 * Returns the QName of this element
	 * @see org.apache.axis2.om.OMElement#getQName()
	 */
	public QName getQName() {
        QName qName;
        if (namespace != null) {
            if (namespace.getPrefix() != null) {
                qName = new QName(namespace.getName(), this.localName, namespace.getPrefix());
            } else {
                qName = new QName(namespace.getName(), this.localName);
            }
        } else {
            qName = new QName(this.localName);
        }
        return qName;
	}



    /**
	 * select all the text children and concat them to a single string
	 * @see org.apache.axis2.om.OMElement#getText()
	 */
	public String getText() {
		String childText = "";
		OMNode child = this.getFirstOMChild();
		OMText textNode;

		while (child != null) {
			if (child.getType() == Node.TEXT_NODE) {
				textNode = (OMText) child;
				if (textNode.getText() != null
						&& !"".equals(textNode.getText())) {
					childText += textNode.getText();
				}
			}
			child = child.getNextOMSibling();
		}

		return childText;
	}

	/**
	 * Removes an attribute fron the element
	 * 
	 * @see org.apache.axis2.om.OMElement#removeAttribute(org.apache.axis2.om.OMAttribute)
	 */
	public void removeAttribute(OMAttribute attr) {
		this.removeAttributeNode((AttrImpl)attr);
	}

	/**
	 * Sets the OM builder
	 * @see org.apache.axis2.om.OMElement#setBuilder(org.apache.axis2.om.OMXMLParserWrapper)
	 */
	public void setBuilder(OMXMLParserWrapper wrapper) {
		this.builder = wrapper;
	}

	/**
	 * Set the local name
	 * @see org.apache.axis2.om.OMElement#setLocalName(java.lang.String)
	 */
	public void setLocalName(String localName) {
		this.localName = localName;
	}

	/**
	 * Set the namespace
	 * @see org.apache.axis2.om.OMElement#setNamespace(org.apache.axis2.om.OMNamespace)
	 */
	public void setNamespace(OMNamespace namespace) {
		this.namespace = namespace;
	}

	/**
	 * Creates a text node with the given value and adds it to the 
	 * element
	 * @see org.apache.axis2.om.OMElement#setText(java.lang.String)
	 */
	public void setText(String text) {
		if(this.isReadonly()) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
		}
		
		TextImpl textNode = (TextImpl)((DocumentImpl)this.ownerNode).createTextNode(text);
		this.addChild(textNode);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#serialize(org.apache.axis2.om.OMOutput)
	 */
	public void serialize(OMOutputImpl omOutput) throws XMLStreamException {
		serialize(omOutput, true);
	}

	public void serializeAndConsume(OMOutputImpl omOutput) throws XMLStreamException {
		this.serialize(omOutput, false);
	}
	
	
    protected void serialize(org.apache.axis2.om.impl.OMOutputImpl omOutput, boolean cache) throws XMLStreamException {

        if (cache) {
            //in this case we don't care whether the elements are built or not
            //we just call the serializeAndConsume methods
            OMSerializerUtil.serializeStartpart(this, omOutput);
            //serilize children
            Iterator children = this.getChildren();
            while (children.hasNext()) {
                ((OMNode) children.next()).serialize(omOutput);
            }
            OMSerializerUtil.serializeEndpart(omOutput);

        } else {
            //Now the caching is supposed to be off. However caching been switched off
            //has nothing to do if the element is already built!
            if (this.done) {
                OMSerializerUtil.serializeStartpart(this, omOutput);
                //serializeAndConsume children
                Iterator children = this.getChildren();
                while (children.hasNext()) {
                    //A call to the  Serialize or the serializeAndConsume wont make a difference here
                    ((OMNode) children.next()).serializeAndConsume(omOutput);
                }
                OMSerializerUtil.serializeEndpart(omOutput);
            } else {
                //take the XMLStream reader and feed it to the stream serilizer.
                //todo is this right ?????
                OMSerializerUtil.serializeByPullStream(this, omOutput, cache);
            }


        }
    }
	
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#getXMLStreamReaderWithoutCaching()
	 */
	public XMLStreamReader getXMLStreamReaderWithoutCaching() {
		return getXMLStreamReader(false);
	}
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#getXMLStreamReader()
	 */
	public XMLStreamReader getXMLStreamReader() {
		return getXMLStreamReader(true);
	}
	

    /**
     * getXMLStreamReader
     *
     * @return reader
     */
    private XMLStreamReader getXMLStreamReader(boolean cache) {
        if ((builder == null) && !cache) {
            throw new UnsupportedOperationException(
                    "This element was not created in a manner to be switched");
        }
        if (builder != null && builder.isCompleted() && !cache) {
            throw new UnsupportedOperationException(
                    "The parser is already consumed!");
        }
        return new OMStAXWrapper(builder, this, cache);
    }
    
	
    public String toStringWithConsume() throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OMOutputImpl out = new OMOutputImpl(baos, false);
        this.serializeAndConsume(out);
        out.flush();
        return new String(baos.toByteArray());
    }
    
    /**
     * Overridden toString() for ease of debuging
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	return (this.namespace != null)?namespace.getName():"" + this.localName;
    }
	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMElement#getChildElements()
	 */
	public Iterator getChildElements() {
		return new OMChildElementIterator(getFirstElement());
	}
	
	/**
	 * @see org.apache.axis2.om.OMElement#getAllDeclaredNamespaces()
	 */
	public Iterator getAllDeclaredNamespaces() throws OMException {
        if (namespaces == null) {
            return null;
        }
        return namespaces.values().iterator();
	}
	
	/**
	 * @see org.apache.axis2.om.OMElement#getAllAttributes()
	 */
	public Iterator getAllAttributes() {
        if (attributes == null) {
            return new EmptyIterator();
        }
//        return 
        //TODO Create a new AttrIterator and return it
        throw new UnsupportedOperationException("TODO");
	}
	
	/**
	 * Returns the local name of this element node
	 * @see org.w3c.dom.Node#getLocalName()
	 */
    public String getLocalName()
    {
        return this.localName; 
    }
    
    /**
     * returns the namespace prefix of this element node
     * @see org.w3c.dom.Node#getPrefix()
     */
    public String getPrefix()
    {
    	//TODO Error checking
        return (this.namespace == null)?null:this.namespace.getPrefix();
    }

	/**
	 * @see org.apache.axis2.om.impl.dom.NodeImpl#setOwnerDocument(org.apache.axis2.om.impl.dom.DocumentImpl)
	 */
	protected void setOwnerDocument(DocumentImpl document) {
		this.ownerNode = document;
		this.isOwned(true);
		if(document.firstChild == null)
			document.firstChild = this;
	}

    /**
     * Turn a prefix:local qname string into a proper QName, evaluating it in the OMElement context
     * unprefixed qnames resolve to the local namespace
     *
     * @param qname prefixed qname string to resolve
     * @return null for any failure to extract a qname.
     */
    public QName resolveQName(String qname) {
        ElementHelper helper = new ElementHelper(this);
        return helper.resolveQName(qname);
    }
}