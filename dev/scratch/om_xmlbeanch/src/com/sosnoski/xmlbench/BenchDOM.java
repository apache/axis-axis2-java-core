/*
 * Copyright (c) 2000-2001 Sosnoski Software Solutions, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.sosnoski.xmlbench;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

/**
 * Abstract base class for measuring performance of any of the DOM document
 * representations. Subclasses need to implement the actual document building
 * and text output methods, but can use the common tree walking code provided
 * here.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public abstract class BenchDOM extends BenchDocBase
{
	/**
	 * Constructor.
	 *
	 * @param config test configuration name
	 */

	protected BenchDOM(String config) {
		super(config);
	}

	/**
	 * Walk subtree for element. This recursively walks through the document
	 * nodes under an element, accumulating summary information.
	 *
	 * @param element element to be walked
	 * @param summary document summary information
	 */

	protected void walkElement(Element element, DocumentSummary summary) {

		// include attribute values in summary
		if (element.hasAttributes()) {
			NamedNodeMap attrs = element.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				summary.addAttribute(attrs.item(i).getNodeValue().length());
			}
		}

		// loop through children
		if (element.hasChildNodes()) {
			Node child = (Node)element.getFirstChild();
			while (child != null) {

				// handle child by type
				int type = child.getNodeType();
				if (type == Node.TEXT_NODE) {
					summary.addContent(child.getNodeValue().length());
				} else if (type == Node.ELEMENT_NODE) {
					summary.addElements(1);
					walkElement((Element)child, summary);
				}
				child = child.getNextSibling();
			}
		}
	}

	/**
	 * Walk and summarize document. This method walks through the nodes
	 * of the document, accumulating summary information.
	 *
	 * @param doc document representation to be walked
	 * @param summary output document summary information
	 */

	protected void walk(Object doc, DocumentSummary summary) {
		summary.addElements(1);
		walkElement(((Document)doc).getDocumentElement(), summary);
	}

	/**
	 * Modify subtree for element. This recursively walks through the document
	 * nodes under an element, performing the modifications.
	 *
	 * @param element element to be walked
	 */

	protected void modifyElement(Element element) {

		// check for children present
		if (element.hasChildNodes()) {

			// loop through child nodes
			Node child;
			Node next = (Node)element.getFirstChild();
			Document doc = null;
			String prefix = null;
			String uri = null;
			boolean content = false;
			while ((child = next) != null) {

				// set next before we change anything
				next = child.getNextSibling();

				// handle child by node type
				if (child.getNodeType() == Node.TEXT_NODE) {

					// trim whitespace from content text
					String trimmed = child.getNodeValue().trim();
					if (trimmed.length() == 0) {

						// delete child if nothing but whitespace
						element.removeChild(child);

					} else {

						// make sure we have the parent element information
						if (!content) {
							doc = element.getOwnerDocument();
							prefix = element.getPrefix();
							uri = element.getNamespaceURI();
							content = true;
						}

						// create a "text" element matching parent namespace
						Element text;
						if (uri == null) {
							text = doc.createElement("text");
						} else {
							text = doc.createElementNS(uri, prefix + ":text");
						}

						// wrap the trimmed content with new element
						text.appendChild(doc.createTextNode(trimmed));
						element.replaceChild(text, child);

					}
				} else if (child.getNodeType() == Node.ELEMENT_NODE) {

					// handle child elements with recursive call
					modifyElement((Element)child);

				}
			}

			// check if we've seen any non-whitespace content for element
			if (content) {

				// add attribute flagging content found
				if (prefix == null || prefix.length() == 0) {
					element.setAttribute("text", "true");
				} else {
					element.setAttributeNS(uri, prefix + ":text", "true");
				}

			}
		}
	}

	/**
	 * Modify a document representation. This implementation of the abstract
	 * superclass method walks the document representation performing the
	 * following modifications: remove all content segments which consist only
	 * of whitespace; add an attribute "text" set to "true" to any elements
	 * which directly contain non-whitespace text content; and replace each
	 * non-whitespace text content segment with a "text" element which wraps
	 * the trimmed content.
	 *
	 * @param doc document representation to be modified
	 */

	protected void modify(Object doc) {
		modifyElement(((Document)doc).getDocumentElement());
       

	}
}
