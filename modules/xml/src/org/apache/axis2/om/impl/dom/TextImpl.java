package org.apache.axis2.om.impl.dom;

import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class TextImpl extends CharacterImpl implements Text {

	/**
	 * @param ownerNode
	 */
	public TextImpl(DocumentImpl ownerNode) {
		super(ownerNode);
	}

	/**
	 * @param ownerNode
	 * @param value
	 */
	public TextImpl(DocumentImpl ownerNode, String value) {
		super(ownerNode, value);
	}

	/**
	 * Breaks this node into two nodes at the specified offset, keeping both 
	 * in the tree as siblings. After being split, this node will contain all 
	 * the content up to the offset point. A new node of the same type, which 
	 * contains all the content at and after the offset point, is returned. If 
	 * the original node had a parent node, the new node is inserted as the 
	 * next sibling of the original node. When the offset is equal to the 
	 * length of this node, the new node has no data.
	 */
	public Text splitText(int offset) throws DOMException {
		if (this.isReadonly()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"NO_MODIFICATION_ALLOWED_ERR", null));
		}
		if(offset < 0 || offset > this.textValue.length()) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN, "INDEX_SIZE_ERR",
							null));
		}
		String newValue = this.textValue.substring(offset);
		this.deleteData(offset, this.textValue.length());
		
		TextImpl newText = (TextImpl)this.getOwnerDocument().createTextNode(newValue);
		newText.setParent(this.parentNode);
		
		this.insertSiblingAfter(newText);
		

		return null;
	}
	
	///
	///org.w3c.dom.Node methods
	///
	public String getNodeName() {
		return "#text";
	}
	public short getNodeType() {
		return OMNode.TEXT_NODE;
	}
	
	///
	///OMNode methods
	///
		
	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#getType()
	 */
	public int getType() throws OMException {
		return Node.TEXT_NODE;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#setType(int)
	 */
	public void setType(int nodeType) throws OMException {
		//do not do anything here
		//Its not clear why we should let someone change the type of a node
	}


	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#serialize(org.apache.axis2.om.OMOutput)
	 */
	public void serialize(OMOutputImpl omOutput) throws XMLStreamException {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}
	

	public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void serializeAndConsume(OMOutputImpl omOutput) throws XMLStreamException {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}

	public void serializeAndConsume(XMLStreamWriter xmlWriter) throws XMLStreamException {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}
	
	public void doOptimize(boolean value) {
		throw new UnsupportedOperationException("TODO");
	}
	public boolean isOptimized() {
		throw new UnsupportedOperationException("TODO");
	}

	public void setOptimize(boolean value) {
		// TODO
		throw new UnsupportedOperationException("TODO");
	}
	
}
