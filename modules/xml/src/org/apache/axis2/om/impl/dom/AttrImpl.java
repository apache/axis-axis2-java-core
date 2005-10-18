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
import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class AttrImpl extends NodeImpl implements OMAttribute, Attr {

	private String attrName;
	private TextImpl attrValue;
	
	private NamespaceImpl namespace;
	
	private boolean used;
	
	private ParentNode parent;
	
	protected AttrImpl(DocumentImpl ownerDocument) {
		super(ownerDocument);
	}
	
	public AttrImpl(DocumentImpl ownerDocument, String localName, OMNamespace ns, String value) {
		super(ownerDocument);
		this.attrName = localName;
		this.attrValue = new TextImpl(value);
		this.namespace = (NamespaceImpl)ns;
	}
	
	public AttrImpl(DocumentImpl ownerDocument, String name, String value) {
		super(ownerDocument);
		this.attrName = name;
		this.attrValue = new TextImpl(value);
	}
	
	public AttrImpl(String localName, OMNamespace ns, String value) {
		this.attrName = localName;
		this.attrValue = new TextImpl(value);
		this.namespace = (NamespaceImpl)ns;
	}
	
	public AttrImpl(String name, String value) {
		this.attrName = name;
		this.attrValue = new TextImpl(value);
	}
	
	public AttrImpl(DocumentImpl ownerDocument, String name) {
		super(ownerDocument);
		this.attrName = name;
	}

	///
	///org.w3c.dom.Node methods
	///
	public String getNodeName() {
		return this.attrName.toString();
	}
	public short getNodeType() {
		return Node.ATTRIBUTE_NODE;
	}
	
	public String getNodeValue() throws DOMException {
		return (this.attrName==null)?"":this.attrValue.getData();
	}
	
	public String getValue() {
		return (this.attrValue == null)? null:this.attrValue.getText();
	}

	///
	///org.w3c.dom.Attr methods
	///
	public String getName() {
		return this.attrName;
	}
	public Element getOwnerElement() {
		//Owned is set to an element instance when the attribute is added to an element
		return (Element) (isOwned() ? ownerNode : null);
	}

	public boolean getSpecified() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMNode detach() throws OMException {
		throw new UnsupportedOperationException("Not supported");
	}

	public void discard() throws OMException {
		throw new UnsupportedOperationException("Not supported");
	}

	public int getType() {
		return Node.ATTRIBUTE_NODE;
	}

	public void serialize(OMOutputImpl omOutput) throws XMLStreamException {
		throw new UnsupportedOperationException("Not supported");
	}

	public void serializeAndConsume(OMOutputImpl omOutput) throws XMLStreamException {
		throw new UnsupportedOperationException("Not supported");
	}

	public OMNamespace getNamespace() {
		return this.namespace;
	}

	public QName getQName() {
		return (this.namespace == null) ? new QName(this.attrName) : 
			new QName(this.namespace.getName(), this.attrName, this.namespace
						.getPrefix());
		
	}

	public String getAttributeValue() {
		return this.attrValue.getText();
	}

	public void setLocalName(String localName) {
		this.attrName = localName;
	}

	public void setOMNamespace(OMNamespace omNamespace) {
		this.namespace = (NamespaceImpl)omNamespace;
	}

	public void setAttributeValue(String value) {
		if(isReadonly()) {
			String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null);
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, msg);
		}
		this.attrValue = (TextImpl)this.getOwnerDocument().createTextNode(value);
	}

	public void setParent(OMContainer element) {
		this.parent = (ParentNode)element;
	}

	public void setType(int nodeType) throws OMException {
		//not necessary ???
	}

	/**
	 * @return Returns the used.
	 */
	protected boolean isUsed() {
		return used;
	}

	/**
	 * @param used The used to set.
	 */
	protected void setUsed(boolean used) {
		this.used = used;
	}

	public void setValue(String value) throws DOMException {
		this.attrValue = (TextImpl) this.getOwnerDocument().createTextNode(
				value);
	}

	public OMContainer getParent() {
		return this.parent;
	}
	
    public String getLocalName()
    {
        return this.attrName;
    }

    public String getNamespaceURI() {
		if(this.namespace != null) {
			return namespace.getName();
		}
		
		return null;
	}

}
