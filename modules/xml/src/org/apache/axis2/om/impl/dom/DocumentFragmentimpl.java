package org.apache.axis2.om.impl.dom;

import org.apache.axis2.om.OMException;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class DocumentFragmentimpl extends ParentNode implements
		DocumentFragment {

	/**
	 * @param ownerDocument
	 */
	public DocumentFragmentimpl(DocumentImpl ownerDocument) {
		super(ownerDocument);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	public short getNodeType() {
		return Node.DOCUMENT_FRAGMENT_NODE;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getNodeName()
	 */
	public String getNodeName() {
		return "#document-fragment";
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#getType()
	 */
	public int getType() throws OMException {
		return Node.DOCUMENT_FRAGMENT_NODE;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#setType(int)
	 */
	public void setType(int nodeType) throws OMException {
		//DO Nothing :-?
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#serializeWithCache(org.apache.axis2.om.OMOutput)
	 */
	public void serializeWithCache(OMOutputImpl omOutput) throws XMLStreamException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#serialize(org.apache.axis2.om.OMOutput)
	 */
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
	
}
