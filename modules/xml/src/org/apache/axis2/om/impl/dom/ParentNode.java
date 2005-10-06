package org.apache.axis2.om.impl.dom;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.impl.OMContainerEx;
import org.apache.axis2.om.impl.llom.traverse.OMChildrenIterator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;

import javax.xml.namespace.QName;

/**
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public abstract class ParentNode extends ChildNode implements OMContainerEx{


	protected ChildNode firstChild;
	
	protected ChildNode lastChild;
	

	
	/**
	 * @param ownerDocument
	 */
	protected ParentNode(DocumentImpl ownerDocument) {
		super(ownerDocument);
	}
	
	protected ParentNode() {
	}
	
	///
	///OMContainer methods
	///
	
	public void addChild(OMNode omNode) {
		this.appendChild((Node)omNode);
	}
	
	
	public void buildNext() {
		if(!this.done)
			builder.next();
	}
	
	public Iterator getChildren() {
		return new OMChildrenIterator(this.firstChild);
	}
	
	public Iterator getChildrenWithName(QName elementQName) throws OMException {
		// TODO Cannot use OMChildrenQNameIterator since it uses llom.ElementImpl
		// TODO
		throw new UnsupportedOperationException("TODO");
	}
	
	public OMElement getFirstChildWithName(QName elementQName)
			throws OMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
	
	public OMNode getFirstOMChild() {
		return this.firstChild;
	}
	
	public void setFirstChild(OMNode omNode) {
		this.firstChild = (ChildNode) omNode;
	}
	
	
	///
	///DOM Node methods
	///	
	
	public NodeList getChildNodes() {
		return new NodeListImpl(this, this.getNamespaceURI(),this.getLocalName());
	}
	
	public Node getFirstChild() {
		return this.firstChild;
	}
	
	public Node getLastChild() {
		return this.lastChild;
	}
			
	public boolean hasChildNodes() {
		return this.firstChild != null;
	}
	
	/**
	 * Inserts newChild before the refChild
	 * If the refChild is null then the newChild is nade the last child  
	 */
	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
	
		ChildNode newDomChild = (ChildNode)newChild;
		ChildNode refDomChild = (ChildNode)refChild;
		
		if(this == newChild || !isAncestor(newChild)) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"HIERARCHY_REQUEST_ERR", null));
		}
		
		if(!this.ownerNode.equals(newDomChild.ownerNode)) {
			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"WRONG_DOCUMENT_ERR", null));			
		}
		
		if(this.isReadonly()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"NO_MODIFICATION_ALLOWED_ERR", null));
		}
		
		if(refChild == null) { //Append the child to the end of the list
			this.lastChild.nextSibling = newDomChild;
			newDomChild.previousSubling = this.lastChild;
			
			this.lastChild = newDomChild;
			return newChild;
		} else {
			Iterator children = this.getChildren(); 
			boolean found = false;
			while(children.hasNext()) {
				ChildNode tempNode = (ChildNode)children.next();
				
				if(tempNode.equals(refChild)) { 
					//RefChild found
					if(tempNode.isFirstChild()) { //If the refChild is the first child
						
						if(newChild instanceof DocumentFragmentimpl) {
							//The new child is a DocumentFragment
							DocumentFragmentimpl docFrag = (DocumentFragmentimpl)newChild;
							this.firstChild = docFrag.firstChild;
							docFrag.lastChild.nextSibling = refDomChild;
							refDomChild.previousSubling = docFrag.lastChild.nextSibling; 
					
						} else {
							
							//Make the newNode the first Child
							this.firstChild = newDomChild;
							
							newDomChild.nextSibling = refDomChild;
							refDomChild.previousSubling = newDomChild;
							
							newDomChild.previousSubling = null; //Just to be sure :-)
						}
					} else { //If the refChild is not the fist child
						ChildNode previousNode = refDomChild.previousSubling;
						
						if(newChild instanceof DocumentFragmentimpl) {
							//the newChild is a document fragment
							DocumentFragmentimpl docFrag = (DocumentFragmentimpl)newChild;
							
							previousNode.nextSibling = docFrag.firstChild;
							docFrag.firstChild.previousSubling = previousNode;
							
							docFrag.lastChild.nextSibling = refDomChild;
							refDomChild.previousSubling = docFrag.lastChild;
						} else {
							
							previousNode.nextSibling = newDomChild;
							newDomChild.previousSubling = previousNode;
							
							newDomChild.nextSibling = refDomChild;
							refDomChild.previousSubling = newDomChild;
						}
						
					}
					found = true;
					break;
				}
			}
			
			if(!found) {
				throw new DOMException(DOMException.NOT_FOUND_ERR,
						DOMMessageFormatter.formatMessage(
								DOMMessageFormatter.DOM_DOMAIN,
								"NOT_FOUND_ERR", null));
			}
			return newChild;
		}
	}
	
	/**
	 * Replaces the oldChild with the newChild
	 */
	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
		ChildNode newDomChild = (ChildNode)newChild;
		ChildNode oldDomChild = (ChildNode)oldChild;
		
		if(this == newChild || !isAncestor(newChild)) {
				throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
						DOMMessageFormatter.formatMessage(
								DOMMessageFormatter.DOM_DOMAIN,
								"HIERARCHY_REQUEST_ERR", null));
		}
		
		if(!this.ownerNode.equals(newDomChild.ownerNode)) {
			throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"WRONG_DOCUMENT_ERR", null));			
		}
		
		if (this.isReadonly()) { 
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"NO_MODIFICATION_ALLOWED_ERR", null));
		}
		
		Iterator children = this.getChildren(); 
		boolean found = false;
		while(children.hasNext()) {
			ChildNode tempNode = (ChildNode)children.next();
			if(tempNode.equals(oldChild)) {
				if(newChild instanceof DocumentFragmentimpl) {
					DocumentFragmentimpl docFrag = (DocumentFragmentimpl)newDomChild;
					docFrag.firstChild.previousSubling = oldDomChild.previousSubling;
					
				} else {
					newDomChild.nextSibling = oldDomChild.nextSibling;
					newDomChild.previousSubling = oldDomChild.previousSubling;
					
					oldDomChild.previousSubling.nextSibling = newDomChild;
					oldDomChild.nextSibling.previousSubling = newDomChild;
				}
				found = true;
				
				//remove the old child's references to this tree
				oldDomChild.nextSibling = null;
				oldDomChild.previousSubling = null;
			}	
		}
		
		
		if(!found) 
			throw new DOMException(DOMException.NOT_FOUND_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"NOT_FOUND_ERR", null));
		
		return oldChild;
	}
	
	
	/**
	 * Removes the given child from the DOM Tree
	 */
	public Node removeChild(Node oldChild) throws DOMException {
		//Check if this node is readonly
		if(this.isReadonly()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"NO_MODIFICATION_ALLOWED_ERR", null));
		}
		
		//Check if the Child is there
		Iterator children = this.getChildren();
		boolean childFound = false;
		while(children.hasNext()) {
			ChildNode tempNode = (ChildNode)children.next();
			if(tempNode.equals(oldChild)) {
				//Child found
				ChildNode oldDomChild = (ChildNode)oldChild;
				ChildNode privChild = oldDomChild.previousSubling;
				
				privChild.nextSibling = oldDomChild.nextSibling;
				oldDomChild.nextSibling.previousSubling = privChild;
				
				//Remove old child's references to this tree
				oldDomChild.nextSibling = null;
				oldDomChild.previousSubling = null;
				
				childFound = true;
			}
		}
		
		if(!childFound) 
			throw new DOMException(DOMException.NOT_FOUND_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"NOT_FOUND_ERR", null));
		
		return oldChild;
	}

	
	
	private boolean isAncestor(Node newNode) {
		
		//TODO isAncestor
		return true;
	}
	
}
