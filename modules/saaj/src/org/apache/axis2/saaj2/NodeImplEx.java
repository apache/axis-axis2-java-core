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

import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.impl.dom.NodeImpl;

public abstract class NodeImplEx extends NodeImpl implements Node {

	private SOAPElement parentElement;
	
	public void detachNode() {
		this.detach();
	}

	public SOAPElement getParentElement() {
		return this.parentElement;
	}
	
	public OMContainer getParent() {
		return (OMContainer)this.parentElement;
	}

	public String getValue() {
		if(this.getNodeType() == Node.TEXT_NODE) {
			return this.getNodeValue();
		} else if(this.getType() == Node.TEXT_NODE) {
			return ((NodeImplEx)(((OMElement)this).getFirstOMChild())).getValue();
		} else {
			return null;
		}
	}

	public void recycleNode() {
        // No corresponding implementation in OM
        // There is no implementation in Axis 1.2 also
	}

	public void setParentElement(SOAPElement parent) throws SOAPException {
		this.parentElement = parent;
	}

	public void setValue(String value) {
		if(this.getNodeType() == Node.TEXT_NODE) {
			this.setNodeValue(value);
		} else if(this.getNodeType() == Node.ELEMENT_NODE) {
			OMElement elem = ((OMElement)this);
			OMNode firstChild = elem.getFirstOMChild();
			if(firstChild == null ||
				(((Node)firstChild).getNodeType() == Node.TEXT_NODE && firstChild.getNextOMSibling() == null)) {
				//If there are no children OR
				//the first child is a text node and the only child

				((OMElement)this).setText(value);				
			} else {
				throw new IllegalStateException("This node is not a Text  node and either has more than one child node or has a child node that is not a Text node");
			}
			
			
		}
	}

	public void setType(int nodeType) throws OMException {
		throw new UnsupportedOperationException("TODO");
	}

	public int getType() {
		return this.getNodeType();
	}

}
