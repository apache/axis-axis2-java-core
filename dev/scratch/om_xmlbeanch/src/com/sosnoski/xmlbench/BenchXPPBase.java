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
 * Benchmark for measuring performance of the XPP document representation
 * with normal nodes.<p>
 *
 * This code is based on a sample provided by Aleksander Slominski.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public class BenchXPPBase extends BenchXPP
{
	/** Pull parser used within a test run. */
	protected XmlPullParser m_parser;

	/**
	 * Constructor.
	 */

	public BenchXPPBase() {
		super("XPP");
	}

	/**
	 * Build document representation by parsing XML. This implementation uses
	 * the method defined by XPP to build the document from an input stream
	 * wrapped in a reader, since direct parsing from a stream is not supported.
	 * Note that XPP supports other methods for constructing the document, but
	 * an input stream is considered the most representative of real
	 * applications.
	 *
	 * @param in XML document input stream
	 * @return document representation
	 */

	protected Object build(InputStream in) {
		XmlNode doc = null;
		try {
			if (m_parser == null) {
				if (m_parserFactory == null) {
					m_parserFactory = XmlPullParserFactory.newInstance();
					m_parserFactory.setNamespaceAware(true);
				}
				m_parser = m_parserFactory.newPullParser();
			} else {
				m_parser.reset();
			}
			m_parser.setInput(new BufferedReader(new InputStreamReader(in)));
			m_parser.next();
			doc = m_parserFactory.newNode();
			m_parser.readNode(doc);
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
			System.exit(0);
		}
		return doc;
	}

	/**
	 * Reset test class instance. This discards the parser used within a test
	 * pass, and calls the superclass method to reset state at that level.
	 */

	protected void reset() {
		m_parser = null;
		super.reset();
	}
}
