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

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * SAX parser benchmark test class. This class defines a single test for a
 * SAX parser, parsing the document text and accumulating document
 * characteristics.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public class BenchSAX extends BenchBase
{
	/** Flag for first time through build method, used for get class name. */
	private boolean m_firstTime = true;

	/**
	 * Inner class for handling SAX notifications.
	 */

	protected class InnerHandler extends DefaultHandler
	{
		/** Summary information accumulated for document. */
		private DocumentSummary m_summary;

		/**
		 * Getter for document summary information.
		 *
		 * @return document summary information
		 */

		public DocumentSummary getSummary() {
			return m_summary;
		}

		/**
		 * Setter for document summary information.
		 *
		 * @param summary document summary information
		 */

		public void setSummary(DocumentSummary summary) {
			m_summary = summary;
		}

		/**
		 * Start of document handler. Clears the accumulated document
		 * summary information.
		 */

		public void startDocument() {
			m_summary.reset();
		}

		/**
		 * Start of element handler. Counts the element and attributes.
		 *
		 * @param space namespace URI
		 * @param name local name of element
		 * @param raw raw element name
		 * @param atts attributes for element
		 */

		public void startElement(String space, String name,
			String raw, Attributes atts) {
			m_summary.addElements(1);
			for (int i = 0; i < atts.getLength(); i++) {
				m_summary.addAttribute(atts.getValue(i).length());
			}
		}

		/**
		 * Character data handler. Counts the characters in total for
		 * document.
		 *
		 * @param ch array supplying character data
		 * @param start starting offset in array
		 * @param length number of characters
		 */

		public void characters(char[] ch, int start, int length) {
			m_summary.addContent(length);
		}

		/**
		 * Ignorable whitespace handler. Counts the characters in total for
		 * document.
		 *
		 * @param ch array supplying character data
		 * @param start starting offset in array
		 * @param length number of characters
		 */

		public void ignorableWhitespace(char[] ch, int start, int length) {
			m_summary.addContent(length);
		}
	}

	/**
	 * Constructor.
	 *
	 * @param print test results listing destination (<code>null</code> if not
	 * to be printed)
	 */

	public BenchSAX() {
		super("SAX");
	}

	/**
	 * Main time test method. This implementation of the abstract base class
	 * method just parses the document text repeatedly, accumulating summary
	 * information for the document which can be compared to that obtained
	 * from the document representation tests.
	 *
	 * @param passes number of passes of each test
	 * @param reps number of copies of document to use for test measurement
	 * @param excludes number of initialization passes excluded from averages
	 * @param text document text for test
	 * @return result times array
	 */

	public int[] runTimeTest(int passes, int reps, int excludes, byte[] text) {

		// allocate array for result values
		int[] results = new int[TIME_RESULT_COUNT];
		for (int i = 0; i < results.length; i++) {
			results[i] = Integer.MIN_VALUE;
		}

		// create the input objects
		ByteArrayInputStream in = new ByteArrayInputStream(text);
		InputSource source = new InputSource(in);

		// set start time for tests
		initTime();

		// parse the document the specified number of times
		DocumentSummary first = new DocumentSummary();
		DocumentSummary next = new DocumentSummary();
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		try {
			SAXParser parser = spf.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			if (m_firstTime) {
				setVariant(reader.getClass().getPackage().getName());
				m_firstTime = false;
			}
			InnerHandler handler = new InnerHandler();
			reader.setContentHandler(handler);
			handler.setSummary(first);
			int best = Integer.MAX_VALUE;
			int sum = 0;
			for (int i = 0; i < passes; i++) {
				for (int j = 0; j < reps; j++) {
					in.reset();
					reader.parse(source);
				}
				int time = testPassTime();
				if (m_printPass) {
					reportValue("Parse document pass " + i, time);
				}
				if (best > time) {
					best = time;
				}
				if (i >= excludes) {
					sum += time;
				}
				if (i == 0) {
					handler.setSummary(next);
				} else if (!first.equals(handler.getSummary())) {
					throw new RuntimeException
						("Document summary information mismatch");
				}
			}
			results[BUILD_MIN_INDEX] = best;
			results[BUILD_AVERAGE_INDEX] = sum / (passes - excludes);

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(0);
		}

		// copy document summary values for return
		results[ELEMENT_COUNT_INDEX] = first.getElementCount();
		results[ATTRIBUTE_COUNT_INDEX] = first.getAttributeCount();
		results[CONTENT_COUNT_INDEX] = first.getContentCount();
		results[TEXTCHAR_COUNT_INDEX] = first.getTextCharCount();
		results[ATTRCHAR_COUNT_INDEX] = first.getAttrCharCount();

		// print summary for document
		if (m_printSummary) {
			printSummary("  Document", first, m_printStream);
		}
		return results;
	}

	/**
	 * Main space test method. This implementation of the abstract base class
	 * method just parses the document repeatedly to check memory usage by the
	 * parser.
	 *
	 * @param count number of units of test to run in this pass
	 * @param excludes number of initialization passes excluded from averages
	 * @param text document text for test
	 * @return result times array
	 */

	public int[] runSpaceTest(int passes, int excludes, byte[] text) {

		// allocate array for result values
		int[] results = new int[SPACE_RESULT_COUNT];
		for (int i = 0; i < results.length; i++) {
			results[i] = Integer.MIN_VALUE;
		}

		// create the reusable objects
		ByteArrayInputStream[] ins = new ByteArrayInputStream[passes];
		for (int i = 0; i < passes; i++) {
			ins[i] = new ByteArrayInputStream(text);
		}
		DocumentSummary first = new DocumentSummary();
		DocumentSummary next = new DocumentSummary();
		InnerHandler handler = new InnerHandler();

		// initialize memory information for tests
		initMemory();
		results[INITIAL_MEMORY_INDEX] = (int)m_lastMemory;

		// parse the document the specified number of times
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		try {
			SAXParser parser = spf.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			if (m_firstTime) {
				setVariant(reader.getClass().getPackage().getName());
				m_firstTime = false;
			}
			reader.setContentHandler(handler);
			handler.setSummary(first);
			int base = (int)m_lastMemory;
			for (int i = 0; i < passes; i++) {
				reader.parse(new InputSource(ins[i]));
				if (i == 0) {
					results[FIRST_SPACE_INDEX] = testPassSpace();
					handler.setSummary(next);
					if (excludes == 1) {
						base = (int)m_lastMemory;
					}
				} else {
					if (!first.equals(handler.getSummary())) {
						throw new RuntimeException
							("Document summary information mismatch");
					}
					if ((i+1) == excludes) {
						testPassSpace();
						base = (int)m_lastMemory;
					}
				}
				if (m_printPass) {
					reportValue("Parse pass " + i, testPassSpace());
				}
			}
			results[AVERAGE_SPACE_INDEX] =
				((int)m_lastMemory-base) / (passes - excludes);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(0);
		}

		// free all constructed objects to find final space
		spf = null;
		initMemory();
		results[FINAL_MEMORY_INDEX] = (int)m_lastMemory;
		results[DELTA_MEMORY_INDEX] =
			results[FINAL_MEMORY_INDEX] - results[INITIAL_MEMORY_INDEX];
		return results;
	}
}
