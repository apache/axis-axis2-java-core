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

import org.apache.crimson.jaxp.*;
import org.apache.crimson.tree.*;

/**
 * Benchmark for measuring performance of the Apache Crimson DOM document
 * representation. Since we may have several parsers and document models in
 * the classpath, this creates the Crimson parser directly in order to avoid
 * any confusion in going through JAXP.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public class BenchCrimson extends BenchDOM
{
	/** Document builder used within a test run. */
	private DocumentBuilder m_builder;

	/**
	 * Constructor.
	 */

	public BenchCrimson() {
		super("Crimson DOM");
	}

	/**
	 * Build document representation by parsing XML. This implementation
	 * creates a document builder if one does not already exist, then reuses
	 * that builder for the duration of a test run..
	 *
	 * @param in XML document input stream
	 * @return document representation
	 */

	protected Object build(InputStream in) {
		if (m_builder == null) {
			DocumentBuilderFactory dbf =
				DocumentBuilderFactoryImpl.newInstance();
			try {
				dbf.setValidating(false);
				dbf.setNamespaceAware(true);
				m_builder = dbf.newDocumentBuilder();
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
				System.exit(0);
			}
		}
		Object doc = null;
		try {
			doc = m_builder.parse(in);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(0);
		}
		return doc;
	}

	/**
	 * Output a document as XML text. This method uses the method defined
	 * by the Crimson DOM to output a text representation of the document.
	 *
	 * @param doc document representation to be output
	 * @param out XML document output stream
	 */

	protected void output(Object doc, OutputStream out) {
		XmlDocument cdoc = (XmlDocument)doc;
		try {
			cdoc.write(out);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(0);
		}
	}

	/**
	 * Reset test class instance. This discards the document builder used
	 * within a test pass.
	 */

	protected void reset() {
		m_builder = null;
	}
}
