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
 * Created on Sep 25, 2004
 *
 */
package org.apache.axis.om;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.apache.axis.om.*;

/**
 * @author Dasarath Weeratunge
 *
 */
public class OMXmlPullParserWrapper {
	private XmlPullParser parser;
	private OMElement root;
	private OMNode lastNode;
	private boolean cache= true;
	private boolean slip= false;
	private boolean navigate= false;
	private boolean done= false;
	private OMNavigator navigator= new OMNavigator();

	public OMXmlPullParserWrapper(XmlPullParser parser) {
		this.parser= parser;
	}

	public OMElement getDocument() throws OMException {
		if (root == null)
			next();
		return root;
	}

	private OMNode createOMElement() throws OMException {
		OMElement node;
		if (lastNode == null) {
			root= new OMElement(parser.getName(), null, null, this);
			node= root;
		}
		else
			if (lastNode.isComplete()) {
				node= new OMElement(parser.getName(), null, lastNode.getParent(), this);
				lastNode.setNextSibling(node);
				node.setPrevSibling(lastNode);
			}
			else {
				OMElement e= (OMElement)lastNode;
				node= new OMElement(parser.getName(), null, (OMElement)lastNode, this);
				e.setFirstChild(node);
			}

		int i, j;
		try {
			j= parser.getNamespaceCount(parser.getDepth());
			i= 0;
			if (j > 1)
				i= parser.getNamespaceCount(parser.getDepth() - 1);
			while (i < j) {
				node.createNamespace(parser.getNamespaceUri(i), parser.getNamespacePrefix(i));
				i++;
			}
		}
		catch (XmlPullParserException e) {
			throw new OMException(e);
		}

		node.setNamespace(node.resolveNamespace(parser.getNamespace(), parser.getPrefix()));

		j= parser.getAttributeCount();
		for (i= 0; i < j; i++) {
			OMNamespace ns= null;
			String uri= parser.getAttributeNamespace(i);
			if (uri.hashCode() != 0)
				ns= node.resolveNamespace(uri, parser.getAttributePrefix(i));
			node.insertAttribute(
				new OMAttribute(parser.getAttributeName(i), ns, parser.getAttributeValue(i), node));
		}

		return node;
	}

	private OMNode createOMText() throws OMException {
		if (lastNode == null)
			throw new OMException();
		OMNode node;
		if (lastNode.isComplete()) {
			node= new OMText(lastNode.getParent(), parser.getText());
			lastNode.setNextSibling(node);
			node.setPrevSibling(lastNode);
		}
		else {
			OMElement e= (OMElement)lastNode;
			node= new OMText(e, parser.getText());
			e.setFirstChild(node);
		}
		return node;
	}

	public void reset(OMNode node) throws OMException {
		navigate= true;
		lastNode= null;
		navigator.init(node);
	}

	//	TODO:
	public int next() throws OMException {
		try {
			if (navigate) {
				OMNode next= navigator.next();
				if (next != null) {
					lastNode= next;
					if (lastNode instanceof OMText)
						return XmlPullParser.TEXT;
					else
						if (navigator.visited())
							return XmlPullParser.END_TAG;
						else
							return XmlPullParser.START_TAG;
				}
				navigate= false;
				if (done)
					return XmlPullParser.END_DOCUMENT;
				if (slip)
					throw new OMException();
			}

			if (done)
				throw new OMException();

			int token= parser.nextToken();

			if (!cache) {
				slip= true;
				return token;
			}

			switch (token) {
				case XmlPullParser.START_TAG :
					lastNode= createOMElement();
					break;

				case XmlPullParser.TEXT :
					lastNode= createOMText();
					break;

				case XmlPullParser.END_TAG :
					if (lastNode.isComplete()) {
						OMElement parent= lastNode.getParent();
						parent.complete();
						lastNode= parent;
					}
					else {
						OMElement e= (OMElement)lastNode;
						e.complete();
					}
					break;

				case XmlPullParser.END_DOCUMENT :
					done= true;
					break;

				default :
					throw new OMException();
			}
			return token;
		}
		catch (OMException e) {
			throw e;
		}
		catch (Exception e) {
			throw new OMException(e);
		}
	}

	public void discard(OMElement el) throws OMException {
		if (el.isComplete() || !cache)
			throw new OMException();
		try {
			cache= false;
			do {
				while (parser.next() != XmlPullParser.END_TAG);
				//	TODO:
			}
			while (!parser.getName().equals(el.getLocalName()));
			lastNode= el.getPrevSibling();
			if (lastNode != null)
				lastNode.setNextSibling(null);
			else {
				OMElement parent= el.getParent();
				if (parent == null)
					throw new OMException();
				parent.setFirstChild(null);
				lastNode= parent;
			}
			slip= false;
			cache= true;
		}
		catch (OMException e) {
			throw e;
		}
		catch (Exception e) {
			throw new OMException(e);
		}
	}

	public void setCache(boolean b) {
		cache= b;
	}

	public String getName() throws OMException {
		if (navigate) {
			try {
				OMElement e= (OMElement)lastNode;
				return e.getLocalName();
			}
			catch (Exception e) {
				throw new OMException(e);
			}
		}
		return parser.getName();
	}

	public String getText() throws OMException {
		if (navigate) {
			try {
				return (String)lastNode.getValue();
			}
			catch (Exception e) {
				throw new OMException(e);
			}
		}
		return parser.getText();
	}

	public String getNamespace() throws OMException {
		if (navigate) {
			if (lastNode instanceof OMElement) {
				OMElement node= (OMElement)lastNode;
				OMNamespace ns= node.getNamespace();
				if (ns != null)
					return ns.getValue();
				//	TODO: else						
			}
			throw new OMException();
		}
		return parser.getNamespace();
	}

	public int getNamespaceCount(int arg) throws OMException {
		if (navigate)
			//	TODO:
			throw new OMException();
		try {
			return parser.getNamespaceCount(arg);
		}
		catch (Exception e) {
			throw new OMException(e);
		}
	}

	public String getNamespacePrefix(int arg) throws OMException {
		if (navigate)
			//	TODO:
			throw new OMException();
		try {
			return parser.getNamespacePrefix(arg);
		}
		catch (Exception e) {
			throw new OMException(e);
		}
	}

	public String getNamespaceUri(int arg) throws OMException {
		if (navigate)
			//	TODO:
			throw new OMException();
		try {
			return parser.getNamespaceUri(arg);
		}
		catch (Exception e) {
			throw new OMException(e);
		}
	}

	public String getNamespace(String arg) throws OMException {
		if (navigate)
			//	TODO:
			throw new OMException();
		try {
			return parser.getNamespace(arg);
		}
		catch (Exception e) {
			throw new OMException(e);
		}
	}

	public String getPrefix() throws OMException {
		if (navigate)
			//	TODO:
			throw new OMException();
		return parser.getPrefix();
	}

	public int getAttributeCount() throws OMException {
		if (navigate)
			//	TODO:
			throw new OMException();
		return parser.getAttributeCount();
	}

	public String getAttributeNamespace(int arg) throws OMException {
		if (navigate)
			//	TODO:
			throw new OMException();
		return parser.getAttributeNamespace(arg);
	}

	public String getAttributeName(int arg) throws OMException {
		if (navigate)
			//	TODO:
			throw new OMException();
		return parser.getAttributeNamespace(arg);
	}

	public String getAttributePrefix(int arg) throws OMException {
		if (navigate)
			//	TODO:
			throw new OMException();
		return parser.getAttributeNamespace(arg);
	}
}
