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

import javax.xml.parsers.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

/**
 * Benchmark for measuring performance of the JDOM document representation.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public class BenchJDOM extends BenchDocBase
{
	/** SAX builder used within a test run. */
	private SAXBuilder m_builder;

	/** XML outputter used within a test run. */
	private XMLOutputter m_outputter;

	/**
	 * Constructor.
	 */

	public BenchJDOM() {
		super("JDOM");
	}

	/**
	 * Build document representation by parsing XML. This implementation
	 * creates a SAX builder if one does not already exist, then reuses
	 * that builder for the duration of a test run..
	 *
	 * @param in XML document input stream
	 * @return document representation
	 */

	protected Object build(InputStream in) {
		if (m_builder == null) {
			m_builder = new SAXBuilder(false);
		}
		Object doc = null;
		try {
			doc = m_builder.build(in);
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
		List list = element.getAttributes();
		if (list.size() > 0) {
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				Attribute attr = (Attribute)iter.next();
				summary.addAttribute(attr.getValue().length());
			}
		}

		// loop through children
		list = element.getContent();
		if (list.size() > 0) {
			Iterator iter = list.iterator();
			while (iter.hasNext()) {

				// handle child by type
				Object child = iter.next();
				if (child instanceof String) {
					summary.addContent(((String)child).length());
				} else if (child instanceof Element) {
					summary.addElements(1);
					walkElement((Element)child, summary);
				}

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
		walkElement(((Document)doc).getRootElement(), summary);
	}

	/**
	 * Output a document as XML text. This method uses the method defined
	 * by JDOM to output a text representation of the document.
	 *
	 * @param doc document representation to be output
	 * @param out XML document output stream
	 */

	protected void output(Object doc, OutputStream out) {
		if (m_outputter == null) {
			m_outputter = new XMLOutputter();
		}
		try {
			m_outputter.output((Document)doc, out);
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
		List children = element.getContent();
		if (children.size() > 0) {

			// loop through child nodes
			int ccnt = children.size();
			Namespace namespace = null;
			boolean content = false;
			for (int i = 0; i < ccnt; i++) {

				// handle child by node type
				Object child = children.get(i);
				if (child instanceof String) {

					// trim whitespace from content text
					String trimmed = child.toString().trim();
					if (trimmed.length() == 0) {

						// delete child if only whitespace (adjusting index)
						children.remove(i--);
						--ccnt;

					} else {

						// set namespace if first content found
						if (!content) {
							namespace = element.getNamespace();
							content = true;
						}

						// wrap the trimmed content with new element
						Element text = new Element("text", namespace);
						text.setText(trimmed);
						children.set(i, text);

					}
				} else if (child instanceof Element) {

					// handle child elements with recursive call
					modifyElement((Element)child);

				}
			}

			// check if we've seen any non-whitespace content for element
			if (content) {

				// add attribute flagging content found
				if (namespace.getPrefix().length() == 0) {
					element.setAttribute("text", "true");
				} else {
					element.setAttribute("text", "true", namespace);
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
		modifyElement(((Document)doc).getRootElement());
	}

	/**
	 * Reset test class instance. This discards the SAX builder used
	 * within a test pass.
	 */

	protected void reset() {
		m_builder = null;
		m_outputter = null;
	}
}
