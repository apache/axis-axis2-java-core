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

import org.apache.xerces.dom.*;
import org.apache.xerces.parsers.*;
import org.apache.xml.serialize.*;

import org.w3c.dom.*;

import org.xml.sax.*;

/**
 * Abstract base class for benchmarks measuring performance of the Xerces DOM
 * document representation. This base class implementation can be customized
 * by subclasses to experiment with options for the representation, in
 * particular for trying the deferred node expansion feature of Xerces.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public abstract class BenchXerces extends BenchDOM
{
	/** Flag for using deferred node expansion. */
	private boolean m_deferExpansion;

	/** DOM parser used within a test run. */
	private DOMParser m_parser;

	/** XML output serializer used within a test run. */
	private XMLSerializer m_serializer;

	/**
	 * Constructor.
	 *
	 * @param config test configuration name
	 * @param defer defer node expansion flag
	 */

	protected BenchXerces(String config, boolean defer) {
		super(config);
		m_deferExpansion = defer;
	}

	/**
	 * Set deferred node expansion mode.
	 *
	 * @param defer defer node expansion flag
	 */

	protected void setDeferExpansion(boolean defer) {
		m_deferExpansion = defer;
	}

	/**
	 * Build document representation by parsing XML. This implementation
	 * creates a DOM parser if one does not already exist, then reuses
	 * that parser for the duration of a test run..
	 *
	 * @param in XML document input stream
	 * @return document representation
	 */

	protected Object build(InputStream in) {
		if (m_parser == null) {
			m_parser = new DOMParser();
			try {
				m_parser.setFeature
					("http://xml.org/sax/features/validation", false);
				m_parser.setFeature
					("http://apache.org/xml/features/dom/defer-node-expansion",
					m_deferExpansion);
				m_parser.setFeature
					("http://xml.org/sax/features/namespaces", true);
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
				System.exit(0);
			}
		}
		Object doc = null;
		try {
			m_parser.parse(new InputSource(in));
			doc = m_parser.getDocument();
			m_parser.reset();
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(0);
		}
		return doc;
	}

	/**
	 * Output a document as XML text. This method uses the method defined
	 * by the Xerces DOM to output a text representation of the document.
	 *
	 * @param doc document representation to be output
	 * @param out XML document output stream
	 */

	protected void output(Object doc, OutputStream out) {
		if (m_serializer == null) {
			OutputFormat format = new OutputFormat((Document)doc);
			m_serializer = new XMLSerializer(format);
		}
		try {
			m_serializer.reset();
			m_serializer.setOutputByteStream(out);
			m_serializer.serialize(((Document)doc).getDocumentElement());
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(0);
		}
	}

	/**
	 * Reset test class instance. This discards the parser used
	 * within a test pass.
	 */

	protected void reset() {
		m_parser = null;
		m_serializer = null;
	}
}
