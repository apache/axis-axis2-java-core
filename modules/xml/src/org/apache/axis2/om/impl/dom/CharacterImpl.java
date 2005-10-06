package org.apache.axis2.om.impl.dom;

import org.apache.axis2.om.OMText;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;

/**
 * This implements the OMCharater operations which are to be
 * inherited by TextImpl, CommentImpl, CDATASectionImpl
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public abstract class CharacterImpl extends ChildNode implements
		OMText, CharacterData {

	protected StringBuffer textValue;
	
	/**
	 * @param ownerNode
	 */
	public CharacterImpl(DocumentImpl ownerNode) {
		super(ownerNode);
	}

	public CharacterImpl(DocumentImpl ownerNode, String value){
		super(ownerNode);
		this.textValue = new StringBuffer(value);
	}
	
	///
	///org.w3c.dom.CharacterData mrthods
	///
	
	public void appendData(String value) throws DOMException {
		
		if (this.isReadonly()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"NO_MODIFICATION_ALLOWED_ERR", null));
		}
		
		this.textValue.append(value);
	}
	
	/**
	 * 
	 */
	public void deleteData(int offset, int count) throws DOMException {
		this.replaceData(offset,count,null);
	}
	
	/**
	 * If the given data is null the content will be deleted
	 */
	public void replaceData(int offset, int count, String data) throws DOMException {
		
		if (this.isReadonly()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"NO_MODIFICATION_ALLOWED_ERR", null));
		}
			
		int length = this.textValue.length();
		if (offset < 0 || offset > length - 1 || count < 0) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN, "INDEX_SIZE_ERR",
							null));
		} else {

			int end = Math.min(count + offset, length);

			if (data == null) {
				this.textValue.delete(offset, end);
			} else {
				this.textValue.replace(offset, end, data);
			}
		}

	}
	
	
	
	/**
	 * Returns the value of the data
	 */
	public String getData() throws DOMException {
		return this.textValue.toString();
	}
	
	/**
	 * Inserts a sting at the specified offset
	 */
	public void insertData(int offset, String data) throws DOMException {
		int length = this.textValue.length();
		
		if (this.isReadonly()) {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"NO_MODIFICATION_ALLOWED_ERR", null));
		}
		
		if(offset < 0 || offset > length-1) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"INDEX_SIZE_ERR", null));
		}
		
		this.textValue.insert(offset,data);

	}

	/**
	 * Sets the text value of data
	 */
	public void setData(String data) throws DOMException {
		if (!this.isReadonly()) {
			this.textValue.replace(0,textValue.length(), data);
		} else {
			throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"NO_MODIFICATION_ALLOWED_ERR", null));
		}
	}
	
	/**
	 * Extracts a range of data from the node.
	 * @return The specified substring. If the sum of offset and count exceeds
	 * the length, then all 16-bit units to the end of the data are returned.
	 */
	public String substringData(int offset, int count) throws DOMException {
		if(offset < 0 || offset > this.textValue.length() || count < 0) {
			throw new DOMException(DOMException.INDEX_SIZE_ERR,
					DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"INDEX_SIZE_ERR", null));
		}
		
		int end = Math.min( count + offset, textValue.length());
		return this.textValue.substring(offset, end);
	}
	
	/**
	 * returns the length of the sting value 
	 */
	public int getLength() {
		return this.textValue.length();
	}
	
	
	///OMCharacter methods 
	
	public String getText() {
		return this.textValue.toString();
	}
	
	
	///
	/// Unsupported binary manipulation related method
	///
	

	
	public String getContentID() {
		throw new UnsupportedOperationException("This is required to hanlde binary content in OM");
	}
	public Object getDataHandler() {
		throw new UnsupportedOperationException("This is required to hanlde binary content in OM");	
	}
	

		
}
