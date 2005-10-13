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
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class AttrImpl extends NodeImpl implements OMAttribute, Attr {

	private String attrName;
	private TextImpl attrValue;
	
	private NamespaceImpl namespace;
	
	private boolean specified;
	
	protected AttrImpl(DocumentImpl ownerDocument) {
		super(ownerDocument);
	}
	
	public AttrImpl(String localName, OMNamespace ns, String value) {
		this.attrName = localName;
		this.attrValue = new TextImpl(value);
		this.namespace = (NamespaceImpl)ns;
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
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void discard() throws OMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public int getType() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void serialize(OMOutputImpl omOutput) throws XMLStreamException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void serializeAndConsume(OMOutputImpl omOutput) throws XMLStreamException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void serializeAndConsume(XMLStreamWriter xmlWriter) throws XMLStreamException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public OMNamespace getNamespace() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public QName getQName() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public String getValue() {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void setLocalName(String localName) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void setOMNamespace(OMNamespace omNamespace) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void setValue(String value) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void setParent(OMContainer element) {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void setType(int nodeType) throws OMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	
	

}
