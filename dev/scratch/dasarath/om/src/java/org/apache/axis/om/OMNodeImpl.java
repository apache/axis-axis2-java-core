/*
 * Copyright  2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

/*
 * Created on Sep 24, 2004
 *
 */
package org.apache.axis.om;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNode;

/**
 * @author Dasarath Weeratunge
 *
 */
public abstract class OMNodeImpl implements OMNode {
	protected OMElement parent;
	protected OMNode nextSibling;
	protected OMNode prevSibling;
	protected String value;
	
	protected OMNodeImpl(){
	}
	
	protected OMNodeImpl(OMElement parent) {
		this.parent= parent;
	}

	public OMElement getParent() throws OMException {
		return parent;
	}

	public OMNode getNextSibling() throws OMException {
		if (nextSibling == null && !parent.isComplete())
			parent.buildNext();
		return nextSibling;
	}

	public void setNextSibling(OMNode node) {
		nextSibling= node;
	}

	public String getValue() throws OMException {
		return value;
	}

	public void setValue(String value) {
		this.value= value;
	}

	public boolean isComplete() {
		return true;
	}

	public void setParent(OMElement element) {
		parent= element;
	}

	public OMNode getPrevSibling() {
		return prevSibling;
	}

	public void setPrevSibling(OMNode node) {
		prevSibling= node;
	}

	public void detach() throws OMException {
		if (parent == null)
			throw new OMException();
		OMNode nextSibling= getNextSibling();
		if (prevSibling == null)
			parent.setFirstChild(nextSibling);
		else
			prevSibling.setNextSibling(nextSibling);
		if (nextSibling != null)
			nextSibling.setPrevSibling(prevSibling);
	}

	public void insertSiblingAfter(OMNode sibling) throws OMException {
		if (parent == null)
			throw new OMException();
		sibling.setParent(parent);
		if (nextSibling == null)
			getNextSibling();
		sibling.setPrevSibling(this);
		if (nextSibling != null)
			nextSibling.setPrevSibling(sibling);
		sibling.setNextSibling(nextSibling);
		nextSibling= sibling;
	}

	public void insertSiblingBefore(OMNode sibling) throws OMException {
		if (parent == null)
			throw new OMException();
		sibling.setParent(parent);
		sibling.setPrevSibling(prevSibling);
		sibling.setNextSibling(this);
		if (prevSibling == null)
			parent.setFirstChild(sibling);
		else
			prevSibling.setNextSibling(sibling);
		prevSibling= sibling;
	}
}
