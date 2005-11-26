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
package org.apache.axis2.saaj2;

import javax.xml.soap.Text;
import javax.xml.stream.XMLStreamException;

import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.dom.TextImpl;
import org.w3c.dom.DOMException;

public class TextImplEx extends NodeImplEx implements Text {

	TextImpl textNode;
	
	/* (non-Javadoc)
	 * @see javax.xml.soap.Text#isComment()
	 */
	public boolean isComment() {
		String value = this.textNode.getText();
		return value.startsWith("<!--") && value.endsWith("-->");
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getNodeName()
	 */
	public String getNodeName() {
		return this.textNode.getNodeName();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	public short getNodeType() {
		return this.textNode.getNodeType();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Text#splitText(int)
	 */
	public org.w3c.dom.Text splitText(int offset) throws DOMException {
		return this.textNode.splitText(offset);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.CharacterData#getData()
	 */
	public String getData() throws DOMException {
		return this.textNode.getData();
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.CharacterData#setData(java.lang.String)
	 */
	public void setData(String data) throws DOMException {
		this.textNode.setData(data);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.CharacterData#substringData(int, int)
	 */
	public String substringData(int offset, int count) throws DOMException {
		return this.textNode.substringData(offset, count);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.CharacterData#appendData(java.lang.String)
	 */
	public void appendData(String value) throws DOMException {
		this.textNode.appendData(value);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.CharacterData#insertData(int, java.lang.String)
	 */
	public void insertData(int offset, String data) throws DOMException {
		this.textNode.insertData(offset, data);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.CharacterData#deleteData(int, int)
	 */
	public void deleteData(int offset, int count) throws DOMException {
		this.textNode.deleteData(offset, count);
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.CharacterData#replaceData(int, int, java.lang.String)
	 */
	public void replaceData(int offset, int count, String data) throws DOMException {
		this.textNode.replaceData(offset, count, data);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.impl.OMNodeEx#setParent(org.apache.axis2.om.OMContainer)
	 */
	public void setParent(OMContainer element) {
		this.textNode.setParent(element);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#getParent()
	 */
	public OMContainer getParent() {
		return this.textNode.getParent();
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#discard()
	 */
	public void discard() throws OMException {
		this.textNode.discard();
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#serialize(org.apache.axis2.om.impl.OMOutputImpl)
	 */
	public void serialize(OMOutputImpl omOutput) throws XMLStreamException {
		this.textNode.serialize(omOutput);
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.om.OMNode#serializeAndConsume(org.apache.axis2.om.impl.OMOutputImpl)
	 */
	public void serializeAndConsume(OMOutputImpl omOutput) throws XMLStreamException {
		this.textNode.serializeAndConsume(omOutput);
	}

}
