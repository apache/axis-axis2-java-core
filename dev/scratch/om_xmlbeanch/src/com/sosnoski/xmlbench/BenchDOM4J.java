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

import org.dom4j.*;
import org.dom4j.io.*;


/**
 * Benchmark for measuring performance of the dom4j document representation.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public class BenchDOM4J extends BenchDocBase
{
	/** SAX reader used within a test run. */
	private SAXReader m_reader;

	/** Document factory used within a test run (copied from reader). */
	private DocumentFactory m_factory;

	/** XML output serializer used within a test run. */
	private XMLWriter m_writer;

	/**
	 * Constructor.
	 */

	public BenchDOM4J() {
		super("dom4j");
	}

	/**
	 * Build document representation by parsing XML. This implementation
	 * creates a SAX reader if one does not already exist, then reuses
	 * that reader for the duration of a test run..
	 *
	 * @param in XML document input stream
	 * @return document representation
	 */

	protected Object build(InputStream in) {
		if (m_reader == null) {
			m_reader = new SAXReader(false);
			m_factory = m_reader.getDocumentFactory();
		}
		Object doc = null;
		try {
			doc = m_reader.read(in);
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
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
		int acnt = element.attributeCount();
		for (int i = 0; i < acnt; i++) {
			summary.addAttribute(element.attribute(i).getValue().length());
		}

		// loop through children
		int ncnt = element.nodeCount();
		for (int i = 0; i < ncnt; i++) {

			// handle child by type
			Node child = element.node(i);
			int type = child.getNodeType();
			if (type == Node.TEXT_NODE) {
				summary.addContent(child.getText().length());
			} else if (type == Node.ELEMENT_NODE) {
				summary.addElements(1);
				walkElement((Element)child, summary);
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
	 * by dom4j to output a text representation of the document.
	 *
	 * @param doc document representation to be output
	 * @param out XML document output stream
	 */

	protected void output(Object doc, OutputStream out) {
		try {
			if (m_writer == null) {
				m_writer = new XMLWriter();
			}
			m_writer.setOutputStream(out);
			m_writer.write((Document)doc);
			m_writer.flush();
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
		if (element.nodeCount() > 0) {

			// loop through child nodes
			List children = element.content();
			int ccnt = children.size();
			QName qname = null;
			boolean content = false;
			for (int i = 0; i < ccnt; i++) {

				// handle child by node type
				Node child = (Node)children.get(i);
				if (child.getNodeType() == Node.TEXT_NODE) {

					// trim whitespace from content text
					String trimmed = child.getText().trim();
					if (trimmed.length() == 0) {

						// delete child if only whitespace (adjusting index)
						children.remove(i--);
						--ccnt;

					} else {

						// construct qualified name for wrapper element
						if (!content) {
							qname = m_factory.createQName("text",
								element.getNamespace());
							content = true;
						}

						// wrap the trimmed content with new element
						Element text = m_factory.createElement(qname);
						text.addText(trimmed);
						children.set(i, text);

					}
				} else if (child.getNodeType() == Node.ELEMENT_NODE) {

					// handle child elements with recursive call
					modifyElement((Element)child);

				}
			}

			// check if we've seen any non-whitespace content for element
			if (content) {

				// add attribute flagging content found
				element.addAttribute(qname, "true");

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
	 * Reset test class instance. This discards the SAX reader used
	 * within a test pass.
	 */

	protected void reset() {
		m_reader = null;
		m_factory = null;
		m_writer = null;
	}
}
