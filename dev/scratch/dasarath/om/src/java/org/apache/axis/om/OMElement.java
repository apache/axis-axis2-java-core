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

import org.apache.axis.om.OMAttribute;

import java.io.PrintStream;

/**
 * @author Dasarath Weeratunge
 *
 */
public class OMElement extends OMNamedNode {
	OMXmlPullParserWrapper builder;
	boolean complete= false;
	OMNode firstChild;
	OMAttribute firstAttribute;
	OMNamespace firstNamespace;

	public OMElement(String localName, OMNamespace ns) {
		super(localName, ns, null);
		complete= true;
	}

	public OMElement(String localName, OMNamespace ns, OMElement parent, OMXmlPullParserWrapper builder) {
		super(localName, ns, parent);
		this.builder= builder;
	}

	public void setFirstChild(OMNode node) throws OMException {
		firstChild= node;
	}

	public void complete() {
		this.complete= true;
	}

	public boolean isComplete() {
		return complete;
	}

	protected void buildNext() throws OMException {
		builder.next();
	}

	public void print(PrintStream s) throws OMException {
		s.print('<');
		super.print(s);

		OMNode node= firstAttribute;
		while (node != null) {
			s.print(" ");
			node.print(s);
			node= node.getNextSibling();
		}

		node= firstNamespace;
		while (node != null) {
			s.print(" ");
			node.print(s);
			node= node.getNextSibling();		
		}

		node= getFirstChild();
		if (node != null) {
			s.print('>');
			while (node != null) {
				node.print(s);
				node= node.getNextSibling();
			}
			s.print('<');
			s.print('/');
			super.print(s);
		}
		else
			s.print('/');
		s.print('>');
	}

	public OMNode getFirstChild() throws OMException {
		if (firstChild == null && !complete)
			buildNext();
		return firstChild;
	}

	public void detach() throws OMException {
		if (complete)
			super.detach();
		else
			builder.discard(this);
	}

	public void insertChild(OMNode child) throws OMException {
		if (firstChild == null && !complete)
			builder.next();
		child.setPrevSibling(null);
		child.setNextSibling(firstChild);
		if (firstChild != null)
			firstChild.setPrevSibling(child);
		child.setParent(this);
		firstChild= child;
	}

	public OMNode getNextSibling() throws OMException {
		while (!complete)
			builder.next();
		return super.getNextSibling();
	}

	public OMAttribute getFirstAttribute() {
		return firstAttribute;
	}

	public void setFirstAttribute(OMAttribute attr) {
		firstAttribute= attr;
	}

	public void insertAttribute(OMAttribute attr) {
		attr.setPrevSibling(null);
		attr.setNextSibling(firstAttribute);
		if (firstAttribute != null)
			firstAttribute.setPrevSibling(attr);
		attr.setParent(this);
		firstAttribute= attr;
	}

	public OMNamespace createNamespace(String uri, String prefix) {
		OMNamespace ns= new OMNamespace(uri, prefix);
		ns.setNextSibling(firstNamespace);
		firstNamespace= ns;
		return ns;
	}

	public OMNamespace resolveNamespace(String uri, String prefix) throws OMException {
		OMNamespace ns= firstNamespace;
		while (ns != null) {
			if (ns.equals(uri, prefix))
				return ns;
			ns= (OMNamespace)ns.getNextSibling();
		}
		if (parent != null)
			return parent.resolveNamespace(uri, prefix);
		return null;
	}
}
