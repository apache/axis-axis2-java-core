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

import org.gjt.xpp.*;

/**
 * Abstract base class for benchmarks measuring performance of the XPP
 * document representation. This base class implementation can be customized
 * by subclasses to experiment with options for the representation, in
 * particular for trying the pull node feature.<p>
 *
 * This code is based on a sample provided by Aleksander Slominski.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public abstract class BenchXPP extends BenchDocBase
{
	/** Pull parser factory used within a test run. */
	protected XmlPullParserFactory m_parserFactory;

	/** XML recorder used within a test run. */
	protected XmlRecorder m_recorder;

	/**
	 * Constructor.
	 *
	 * @param config test configuration name
	 */

	protected BenchXPP(String config) {
		super(config);
	}

	/**
	 * Walk subtree for element. This recursively walks through the document
	 * nodes under an element, accumulating summary information.
	 *
	 * @param element element to be walked
	 * @param summary document summary information
	 */

	protected void walkElement(XmlNode element, DocumentSummary summary) {

		// include attribute values in summary
		int acnt = element.getAttributeCount();
		for (int i = 0; i < acnt; i++) {
			summary.addAttribute(element.getAttributeValue(i).length());
		}

		// loop through children
		int ccnt = element.getChildrenCount();
		for (int i = 0; i < ccnt; i++) {

			// handle child by type
			Object child = element.getChildAt(i);
			if (child instanceof String) {
				summary.addContent(child.toString().length());
			} else if (child instanceof XmlNode) {
				summary.addElements(1);
				walkElement((XmlNode)child, summary);
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
		walkElement((XmlNode)doc, summary);
	}

	/**
	 * Output a document as XML text. This implementation uses the method
	 * defined by XPP to output a text representation of the document.
	 *
	 * @param doc document representation to be output
	 * @param out XML document output stream
	 */

	protected void output(Object doc, OutputStream out) {
		try {
			if (m_recorder == null) {
				if (m_parserFactory == null) {
					m_parserFactory = XmlPullParserFactory.newInstance();
					m_parserFactory.setNamespaceAware(true);
				}
				m_recorder = m_parserFactory.newRecorder();
			}
			Writer writer = new OutputStreamWriter(out);
			m_recorder.setOutput(writer);
			m_recorder.writeNode((XmlNode)doc);
			writer.close();
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
	 * @throws XmlPullParserException on error walking tree
	 */

	protected void modifyElement(XmlNode element)
		throws XmlPullParserException {

		// check for children present
		int ccnt = element.getChildrenCount();
		if (ccnt > 0) {

			// loop through child nodes
			String uri = null;
			String prefix = null;
			String raw = null;
			boolean content = false;
			for (int i = 0; i < ccnt; i++) {

				// handle child by node type
				Object child = element.getChildAt(i);
				if (child instanceof String) {

					// trim whitespace from content text
					String trimmed = child.toString().trim();
					if (trimmed.length() == 0) {

						// delete child if only whitespace (adjusting index)
						element.removeChildAt(i--);
						--ccnt;

					} else {

						// construct qualified name for wrapper element
						if (!content) {
							uri = element.getNamespaceUri();
							prefix = element.getPrefix();
							raw = (prefix == null) ? "text" : prefix + ":text";
							content = true;
						}

						// wrap the trimmed content with new element
						XmlNode text = m_parserFactory.newNode();
						text.appendChild(trimmed);
						element.replaceChildAt(i, text);
						text.modifyTag(uri, "text", raw);

					}
				} else if (child instanceof XmlNode) {

					// handle child elements with recursive call
					modifyElement((XmlNode)child);

				}
			}

			// check if we've seen any non-whitespace content for element
			if (content) {

				// add attribute flagging content found
				element.addAttribute(uri, "text", raw, "true");

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
		try {
			modifyElement((XmlNode)doc);
		} catch (XmlPullParserException ex) {
			ex.printStackTrace(System.err);
			System.exit(0);
		}
	}

	/**
	 * Reset test class instance. This discards the parser factory and recorder
	 * used within a test pass.
	 */

	protected void reset() {
		m_parserFactory = null;
		m_recorder = null;
	}
}
