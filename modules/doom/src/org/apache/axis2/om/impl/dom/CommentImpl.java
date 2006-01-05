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

import org.apache.axis2.om.OMComment;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class CommentImpl extends CharacterImpl implements Comment, OMComment {

	public CommentImpl(DocumentImpl ownerNode) {
		super(ownerNode);
		this.done = true;
	}

	public CommentImpl(DocumentImpl ownerNode, String value) {
		super(ownerNode, value);
		this.done = true;
	}

	public String getNodeName() {
		return "#comment";
	}

	public short getNodeType() {
		return Node.COMMENT_NODE;
	}

	public String getValue() {
		return this.getData();
	}

	public void setValue(String text) {
		this.textValue.delete(0,this.textValue.length());
		this.textValue.append(text);
	}

	public int getType() {
		return Node.COMMENT_NODE;
	}

	public void setType(int nodeType) throws OMException {
		throw new UnsupportedOperationException("You should not set the node type of a comment");
	}

	public void serialize(OMOutputImpl omOutput) throws XMLStreamException {
        XMLStreamWriter writer = omOutput.getXmlStreamWriter();
        writer.writeComment(this.textValue.toString());
	}

	public void serializeAndConsume(OMOutputImpl omOutput)
			throws XMLStreamException {
		serialize(omOutput);
	}

}
