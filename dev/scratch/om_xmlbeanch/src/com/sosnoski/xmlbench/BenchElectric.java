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

import electric.xml.*;

/**
 * Benchmark for measuring performance of the Electric XML document
 * representation.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public class BenchElectric extends BenchDocBase
{
	/**
	 * Constructor.
	 */

	public BenchElectric() {
		super("EXML");
	}

	/**
	 * Build document representation by parsing XML. This implementation uses
	 * the method defined by Electric DOM to build the document from an input
	 * stream. Note that Electric DOM supports other methods for constructing
	 * the document, but an input stream is considered the most representative
	 * of real applications.
	 *
	 * @param in XML document input stream
	 * @return document representation
	 */

	protected Object build(InputStream in) {
		Object doc = null;
		try {
			doc = new Document(in);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(0);
		}
		return doc;
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
			Attributes attrs = element.getAttributes();
			Attribute attr;
			while ((attr = attrs.next()) != null) {
				summary.addAttribute(attr.getValue().length());
			}
		}

		// loop through children
		if (element.hasChildren()) {
			Child child = element.getChildren().first();
			while (child != null) {

				// handle child by type
				if (child instanceof Text) {
					summary.addContent(((Text)child).getString().length());
				} else if (child instanceof Element) {
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
		walkElement(((Document)doc).getRoot(), summary);
	}

	/**
	 * Output a document as XML text. This implementation uses the method
	 * defined by Electric DOM to output a text representation of the document.
	 *
	 * @param doc document representation to be output
	 * @param out XML document output stream
	 */

	protected void output(Object doc, OutputStream out) {
		Document edoc = (Document)doc;
		try {
			edoc.write(out);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(0);
		}
	}

	/**
	 * Modify subtree for element. This recursively walks through the document
	 * nodes under an element, performing the modifications.
	 *
	 * @param element element to be walked
	 */

	protected void modifyElement(Element element) {

		// check for children present
		if (element.hasChildren()) {

			// loop through child nodes
			Child child;
			Child next = element.getChildren().first();
			String prefix = null;
			boolean content = false;
			while ((child = next) != null) {

				// set next before we change anything
				next = child.getNextSibling();

				// handle child by node type
				if (child instanceof Text) {

					// trim whitespace from content text
					String trimmed = ((Text)child).getString().trim();
					if (trimmed.length() == 0) {

						// delete child if only whitespace (adjusting index)
						child.remove();

					} else {

						// construct qualified name for wrapper element
						if (!content) {
							prefix = element.getPrefix();
							content = true;
						}

						// wrap the trimmed content with new element
						Element text = new Element();
						text.addText(trimmed);
						child.replaceWith(text);
						text.setName(prefix, "text");

					}
				} else if (child instanceof Element) {

					// handle child elements with recursive call
					modifyElement((Element)child);

				}
			}

			// check if we've seen any non-whitespace content for element
			if (content) {

				// add attribute flagging content found
				element.setAttribute(prefix, "text", "true");

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
		modifyElement(((Document)doc).getRoot());
	}
}
