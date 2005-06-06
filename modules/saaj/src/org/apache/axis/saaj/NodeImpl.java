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
package org.apache.axis.saaj;

import java.util.Iterator;

import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMText;
import org.apache.axis.util.Dom2OmUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.CharacterData;

/**
 * Class NodeImpl
 * 
 * @author Ashutosh Shahi
 * ashutosh.shahi@gmail.com
 */
public class NodeImpl implements Node {
	
	/**
	 * Field omNode
	 */
	protected org.apache.axis.om.OMNode omNode;
	/**
	 * field document
	 */
	protected org.w3c.dom.Document document;
	
	//protected CharacterData textRep = null;
	
	/**
	 * Constructor NodeImpl
	 *
	 */
	public NodeImpl(){
	
	}
	
	/**
	 * Constructor NodeImpl
	 * 
	 * @param node
	 */
	public NodeImpl(OMNode node){
		this.omNode = node;
	}
	/**
	 * Constructor NodeImpl
	 * 
	 * @param attrib
	 */
	public NodeImpl(OMAttribute attrib){
		//TODO
		// To be implemented
		// Find out a way to construct OMNode from a OMAttribute
		// as OMAttributes are immutable
	}
	
	/**
	 * Constructor NodeImpl
	 * 
	 * @param ns
	 */
	public NodeImpl(OMNamespace ns){
		//TODO
		// To be implemented
		// Find out a way to construct OMNode from OMNamespace
		// OMNamespace is immutable
	}
	
     /**
     * constructor which adopts the name and NS of the char data, and its text
     * @param text
     */
/*    public NodeImpl(CharacterData text) {
    	
    }
*/
	
	public OMNode getOMNode(){
		return omNode;
	}
	
	/**
	 * Method getValue
	 * 
	 * @see javax.xml.soap.Node#getValue()
	 */
	public String getValue() {
		
		if(omNode.getType() == OMNode.TEXT_NODE)
			return ((OMText)omNode).getText();
		else if(omNode.getType() == OMNode.ELEMENT_NODE)
			return new NodeImpl(((OMElement)omNode).getFirstChild()).getValue();
		return null;
	}

	/**
	 * Method setParentElement
	 * @param parent
	 * 
	 * @see javax.xml.soap.Node#setParentElement(javax.xml.soap.SOAPElement)
	 */
	public void setParentElement(SOAPElement parent) throws SOAPException {
		
		OMElement omParent = ((SOAPElementImpl)parent).getOMElement();
		omNode.setParent(omParent);
	}

	/**
	 * Method getParentElement
	 * @see javax.xml.soap.Node#getParentElement()
	 */
	public SOAPElement getParentElement() {
		
		OMElement omParent = omNode.getParent();
		return new SOAPElementImpl(omParent);
	}

	/**
	 * Method detachNode
	 * @see javax.xml.soap.Node#detachNode()
	 */
	public void detachNode() {
	
		omNode.detach();
	}

	/**
	 * Method recycleNode
	 * @see javax.xml.soap.Node#recycleNode()
	 */
	public void recycleNode() {
		// No corresponding implementation in OM
		// There is no implementation in Axis 1.2 also

	}

	/**
	 * Method setValue
	 * @param value
	 * 
	 * @see javax.xml.soap.Node#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		
		if(omNode.getType() == OMNode.TEXT_NODE){
			OMElement parent = omNode.getParent();
			((OMText)omNode).discard();
			omNode = org.apache.axis.om.OMAbstractFactory.getOMFactory().createText(parent, value);
		} else if(omNode.getType() == OMNode.ELEMENT_NODE){
			OMNode firstChild = ((OMElement)omNode).getFirstChild();
			if(firstChild == null ){
				firstChild = org.apache.axis.om.OMAbstractFactory.getOMFactory().createText((OMElement)omNode, value);
			}
			else if(firstChild.getType() == OMNode.TEXT_NODE){
				((OMText)firstChild).discard();
				firstChild = org.apache.axis.om.OMAbstractFactory.getOMFactory().createText((OMElement)omNode, value);
			}	
		} else{
			throw new IllegalStateException();
		}
	}

	/**
	 * Method getNodeType
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	public short getNodeType() {
		
		return (short)omNode.getType();
	}

	/**
	 * Method normalize
	 * @see org.w3c.dom.Node#normalize()
	 */
	public void normalize() {
		// No corresponding function in OM
		//Axis 1.2 also doesn't have any implementation for this

	}

	/**
	 * Method hasAttributes
	 * @see org.w3c.dom.Node#hasAttributes()
	 */
	public boolean hasAttributes() {
		if(omNode instanceof OMElement){
			Iterator iter = ((OMElement)omNode).getAttributes();
			return (iter.hasNext() ? true : false);
		}
		return false;
	}

	/**
	 * Method hasChildNodes
	 * @see org.w3c.dom.Node#hasChildNodes()
	 */
	public boolean hasChildNodes() {
		if(omNode instanceof OMElement){
			Iterator iter = ((OMElement)omNode).getChildren();
			return (iter.hasNext() ? true : false);
		}
		return false;
	}

	/**
	 * Method getLocalName
	 * @see org.w3c.dom.Node#getLocalName()
	 */
	public String getLocalName() {
		if(omNode.getType() == ELEMENT_NODE || omNode.getType()
				== ATTRIBUTE_NODE)
			return ((OMElement)omNode).getLocalName();
		// TODO: else if(omNode.getType() == ATTRIBUTE_NODE)
		//	return some
		return null;
	}

	/**
	 * Method getNamespaceURI
	 * @see org.w3c.dom.Node#getNamespaceURI()
	 */
	public String getNamespaceURI() {
		
		return ((OMElement)omNode).getNamespace().getName();
	}

	/**
	 * Method getNodeName
	 * @see org.w3c.dom.Node#getNodeName()
	 */
	public String getNodeName() {
		
		if(omNode.getType() == OMNode.ELEMENT_NODE )
			return ((OMElement)omNode).getLocalName();
		else if(omNode.getType() == OMNode.COMMENT_NODE)
			return "#comment";
		else if(omNode.getType() == OMNode.CDATA_SECTION_NODE)
			return "#cdata-section";
		else if(omNode.getType() == OMNode.TEXT_NODE)
			return "#text";
		//TODO else if Attribute Node so something
		// return attribute name
		return null;
	}

	/**
	 * Method getNodeValue
	 * @see org.w3c.dom.Node#getNodeValue()
	 */
	public String getNodeValue() throws DOMException {
		// Returns text for a TEXT_NODE, null otherwise
		if(omNode.getType() == OMNode.TEXT_NODE)
			return ((OMText)omNode).getText();
		//TODO else if(omNode.getType() == Attribute)
		else
			return null;
	}

	/**
	 * Method getPrefix
	 * @see org.w3c.dom.Node#getPrefix()
	 */
	public String getPrefix() {
		if(omNode.getType() == OMNode.ELEMENT_NODE)
			return ((OMElement)omNode).getNamespace().getPrefix();
		return null;
	}

	/**
	 * Method setNodeValue
	 * @see org.w3c.dom.Node#setNodeValue(java.lang.String)
	 */
	public void setNodeValue(String value) throws DOMException {
		
		if(omNode.getType() == OMNode.TEXT_NODE){
			OMElement parent = omNode.getParent();
			((OMText)omNode).discard();
			omNode = org.apache.axis.om.OMAbstractFactory.getOMFactory().createText(parent, value);
		}
	}

	/**
	 * Method setPrefix
	 * @see org.w3c.dom.Node#setPrefix(java.lang.String)
	 */
	public void setPrefix(String prefix) throws DOMException {
		//TODO - Do for attribute Node
		if(omNode.getType() == OMNode.ELEMENT_NODE /*|| Attribute Node*/){
			OMNamespace ns = ((OMElement)omNode).getNamespace();
			String uri = ns.getName();
			OMNamespace newNs = org.apache.axis.om.OMAbstractFactory.getOMFactory().createOMNamespace(uri, prefix);
			((OMElement)omNode).setNamespace(newNs);
		}

	}

	/**
	 * Method setOwnerDocument
	 * @param doc
	 */
	public void setOwnerDocument(Document doc){
		// method not part of org.w3c.dom.Node, created to set the document
		this.document = doc;
	}
	
	/**
	 * Method getOwnerDocument
	 * @see org.w3c.dom.Node#getOwnerDocument()
	 */
	public Document getOwnerDocument() {
		// return the set document
		return document;
	}

	/**
	 * Method getAttributes
	 * @see org.w3c.dom.Node#getAttributes()
	 */
	public NamedNodeMap getAttributes() {
		// Will have to provide an implementation of NamedNodeMap
		// Dropping for now
		// TODO
		Iterator iter = ((OMElement)omNode).getAttributes();
		
		return null;
	}

	/**
	 * Method getFirstChild
	 * @see org.w3c.dom.Node#getFirstChild()
	 */
	public org.w3c.dom.Node getFirstChild() {
		//
		OMNode child = ((OMElement)omNode).getFirstChild();
		return new NodeImpl(child);
	}

	/**
	 * Method getLastChild
	 * @see org.w3c.dom.Node#getLastChild()
	 */
	public org.w3c.dom.Node getLastChild() {
		
		Iterator children = ((OMElement)omNode).getChildren();
		Object child = null;
		while(children.hasNext()){
			child = children.next();
		}
		if(child instanceof OMNode){
			return new NodeImpl((OMNode)child);
		}
		return null;
	}

	/**
	 * dom Node method
	 */
	public org.w3c.dom.Node getNextSibling() {
		
		OMNode sibling = omNode.getNextSibling();
		return new NodeImpl(sibling);
	}

	
	public org.w3c.dom.Node getParentNode() {
		
		OMElement parent = omNode.getParent();
		return new NodeImpl(parent);
	}

	/**
	 * dom Node method
	 */
	public org.w3c.dom.Node getPreviousSibling() {
		
		OMNode prevSibling = omNode.getPreviousSibling();
		return new NodeImpl(prevSibling);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#cloneNode(boolean)
	 */
	public org.w3c.dom.Node cloneNode(boolean deep) {
		//TODO
		return null;
	}

	/**
	 * DOM Node method
	 */
	public NodeList getChildNodes() {
		Iterator iter = ((OMElement)omNode).getChildren();
		NodeListImpl list = new NodeListImpl();
		while(iter.hasNext()){
			OMNode omChild = (OMNode)iter.next();
			Node child = new NodeImpl(omChild);
			list.addNode(child);
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#isSupported(java.lang.String, java.lang.String)
	 */
	public boolean isSupported(String arg0, String arg1) {
		//TODO: Not implemented in 1.2 as well
		return false;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#appendChild(org.w3c.dom.Node)
	 */
	public org.w3c.dom.Node appendChild(org.w3c.dom.Node node)
			throws DOMException {
		
		OMNode child = Dom2OmUtils.toOM(node);
		if(omNode.getType() == OMNode.ELEMENT_NODE)
			((OMElement)omNode).addChild(child);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#removeChild(org.w3c.dom.Node)
	 */
	public org.w3c.dom.Node removeChild(org.w3c.dom.Node oldChild)
			throws DOMException {
		//Check if equals method has been removed from OMNode
		OMNode child = Dom2OmUtils.toOM(oldChild);
		if(omNode.getType() == OMNode.ELEMENT_NODE){
			Iterator iter = ((OMElement)omNode).getChildren();
			while(iter.hasNext()){
				Object nextChild = iter.next();
				if(nextChild instanceof OMNode && nextChild.equals(child)){
					((OMElement)nextChild).discard();
					return oldChild;
				}
			}
		}
		
		return null;
	}

	/**
	 * @see org.w3c.dom.Node#insertBefore(org.w3c.dom.Node, org.w3c.dom.Node)
	 */
	public org.w3c.dom.Node insertBefore(org.w3c.dom.Node arg0,
			org.w3c.dom.Node arg1) throws DOMException {
		
		return null;
	}

	/**
	 * @see org.w3c.dom.Node#replaceChild(org.w3c.dom.Node, org.w3c.dom.Node)
	 */
	public org.w3c.dom.Node replaceChild(org.w3c.dom.Node newChild,
			org.w3c.dom.Node refChild) throws DOMException {
		OMNode newOmChild = Dom2OmUtils.toOM(newChild);
		OMNode refOmChild = Dom2OmUtils.toOM(refChild);
		if(omNode.getType() == OMNode.ELEMENT_NODE){
			Iterator iter = ((OMElement)omNode).getChildren();
			while(iter.hasNext()){
				Object nextChild = iter.next();
				if(nextChild instanceof OMNode && nextChild.equals(refOmChild)){
					
				}
			}
		}
		return null;
	}
	
	public boolean equals(Object o){
		if(o instanceof NodeImpl){
			if(this.omNode.equals(((NodeImpl)o).omNode))
					return true;
		}
		return false;
	}

}
