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

/**
 * Abstract base class for document representation benchmark tests. This class
 * defines the basic tests along with some implementation methods which must
 * be defined by the subclass for each particular document representation to
 * be tested.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public abstract class BenchDocBase extends BenchBase
{
	/**
	 * Constructor.
	 *
	 * @param config test configuration name
	 */

	protected BenchDocBase(String config) {
		super(config);
	}

	/**
	 * Build document representation by parsing XML. This method must be
	 * implemented by each subclass to use the appropriate construction
	 * technique.
	 *
	 * @param in XML document input stream
	 * @return document representation
	 */

	protected abstract Object build(InputStream in);

	/**
	 * Walk and summarize document. This method should walk through the nodes
	 * of the document, accumulating summary information. It must be
	 * implemented by each subclass.
	 *
	 * @param doc document representation to be walked
	 * @param summary output document summary information
	 */

	protected abstract void walk(Object doc, DocumentSummary summary);

	/**
	 * Walk an array of documents. This method walks through the elements of
	 * each document in the array, using the document walker method
	 * implemented by the subclass.
	 *
	 * @param docs array of document representations
	 * @return representative document summary information
	 */

	protected DocumentSummary walkAll(Object[] docs) {
		DocumentSummary base = new DocumentSummary();
		DocumentSummary last = new DocumentSummary();
		for (int i = 0; i < docs.length; i++) {
			if (i == 0) {
				walk(docs[i], base);
			} else {
				last.reset();
				walk(docs[i], last);
				if (!base.equals(last)) {
					throw new RuntimeException
						("Document summary information mismatch");
				}
			}
		}
		return base;
	}

	/**
	 * Output a document as XML text. This method must be implemented by each
	 * subclass to use the appropriate output technique.
	 *
	 * @param doc document representation to be output
	 * @param out XML document output stream
	 */

	protected abstract void output(Object document, OutputStream out);

	/**
	 * Modify a document representation. This method must be implemented by each
	 * subclass to walk the document representation performing the following
	 * modifications: remove all content segments which consist only of
	 * whitespace; add an attribute "text" set to "true" to any elements which
	 * directly contain non-whitespace text content; and replace each
	 * non-whitespace text content segment with a "text" element which wraps
	 * the content.
	 *
	 * @param doc document representation to be modified
	 */

	protected abstract void modify(Object document);

	/**
	 * Reset test class instance. This method should be overridden by any
	 * subclasses which retain state information during the execution of a
	 * test. Any such information should be cleared when this method is called.
	 */

	protected void reset() {}

	/**
	 * Serialize a document to a byte array.
	 *
	 * @param doc document representation to be serialized
	 * @param out serialized document output stream
	 * @return <code>true</code> if successful, <code>false</code> if
	 * representation does not support serialization
	 */

	protected boolean serialize(Object doc, OutputStream out) {
		try {
			ObjectOutputStream os = new ObjectOutputStream(out);
			os.writeObject(doc);
			os.close();
			return true;
		} catch (NotSerializableException ex) {
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(0);
		}
		return false;
	}

	/**
	 * Unserialize a document from a byte array.
	 *
	 * @param in serialized document input stream
	 * @return unserialized document representation
	 */

	protected Object unserialize(InputStream in) {
		Object restored = null;
		try {
			ObjectInputStream os = new ObjectInputStream(in);
			restored = os.readObject();
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(0);
		}
		return restored;
	}

	/**
	 * Main time test method. This implementation of the abstract base class
	 * method performs the normal sequence of speed tests. Subclasses which
	 * cannot use the normal test sequence must override this method with
	 * their own variation.
	 *
	 * @param passes number of passes of each test
	 * @param reps number of copies of document to use for test measurement
	 * @param excludes number of initialization passes excluded from averages
	 * @param text document text for test
	 * @return result times array
	 */

//	private boolean s_firstTime = true;

	public int[] runTimeTest(int passes, int reps, int excludes, byte[] text) {

		// allocate array for result values
		int[] results = new int[TIME_RESULT_COUNT];
		for (int i = 0; i < results.length; i++) {
			results[i] = Integer.MIN_VALUE;
		}

		// create the reusable objects
		ByteArrayInputStream[][] ins = new ByteArrayInputStream[passes][reps];
		for (int i = 0; i < passes; i++) {
			for (int j = 0; j < reps; j++) {
				ins[i][j] = new ByteArrayInputStream(text);
			}
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream(text.length*2);

		// set start time for tests
		initTime();

		// first build the specified number of copies of the document
		Object[][] docs = new Object[passes][reps];
		int best = Integer.MAX_VALUE;
		int sum = 0;
		for (int i = 0; i < passes; i++) {
			for (int j = 0; j < reps; j++) {
				docs[i][j] = build(ins[i][j]);
			}
			int time = testPassTime();
			if (m_printPass) {
				reportValue("Build document pass " + i, time);
			}
			if (best > time) {
				best = time;
			}
			if (i >= excludes) {
				sum += time;
			}
		}
		results[BUILD_MIN_INDEX] = best;
		results[BUILD_AVERAGE_INDEX] = sum / (passes - excludes);

		// walk the constructed document copies
		DocumentSummary info = new DocumentSummary();
		best = Integer.MAX_VALUE;
		sum = 0;
		for (int i = 0; i < passes; i++) {
			for (int j = 0; j < reps; j++) {
				info.reset();
				walk(docs[i][j], info);
			}
			int time = testPassTime();
			if (m_printPass) {
				reportValue("Walk document pass " + i, time);
			}
			if (best > time) {
				best = time;
			}
			if (i >= excludes) {
				sum += time;
			}
		}
		results[WALK_MIN_INDEX] = best;
		results[WALK_AVERAGE_INDEX] = sum / (passes - excludes);

		// generate text representation of document copies
		byte[] output = null;
		best = Integer.MAX_VALUE;
		sum = 0;
		for (int i = 0; i < passes; i++) {
			for (int j = 0; j < reps; j++) {
				out.reset();
				output(docs[i][j], out);
			}
			int time = testPassTime();
			if (m_printPass) {
				reportValue("Generate text pass " + i, time);
			}
			if (best > time) {
				best = time;
			}
			if (i >= excludes) {
				sum += time;
			}
		}
		results[TEXT_MIN_INDEX] = best;
		results[TEXT_AVERAGE_INDEX] = sum / (passes - excludes);

		// save copy of output for later check parse
		output = out.toByteArray();
		initTime();

		// attempt serialization of document
		byte[] serial = null;
		Object restored = null;
		out.reset();
		if (!serialize(docs[0][0], out)) {
			if (m_printPass) {
				m_printStream.println("  **Serialization not supported by model**");
			}
		} else {

			// serialize with printing of times (first copy already done)
			best = Integer.MAX_VALUE;
			sum = 0;
			serial = out.toByteArray();
			for (int i = 0; i < passes; i++) {
				for (int j = (i == 0) ? 1 : 0; j < reps; j++) {
					out.reset();
					serialize(docs[i][j], out);
				}
				int time = testPassTime();
				if (m_printPass) {
					reportValue("Serialize pass " + i, time);
				}
				if (best > time) {
					best = time;
				}
				if (i >= excludes) {
					sum += time;
				}
			}
			results[SERIALIZE_MIN_INDEX] = best;
			results[SERIALIZE_AVERAGE_INDEX] = sum / (passes - excludes);
			results[SERIALIZE_SIZE_INDEX] = serial.length;

			// restore from serialized form
			ByteArrayInputStream sin = new ByteArrayInputStream(serial);
			best = Integer.MAX_VALUE;
			sum = 0;
			for (int i = 0; i < passes; i++) {
				for (int j = 0; j < reps; j++) {
					sin.reset();
					restored = unserialize(sin);
				}
				int time = testPassTime();
				if (m_printPass) {
					reportValue("Unserialize pass " + i, time);
				}
				if (best > time) {
					best = time;
				}
				if (i >= excludes) {
					sum += time;
				}
			}
			results[UNSERIALIZE_MIN_INDEX] = best;
			results[UNSERIALIZE_AVERAGE_INDEX] = sum / (passes - excludes);
		}

		// modify the document representation
		initTime();
		best = Integer.MAX_VALUE;
		sum = 0;
		for (int i = 0; i < passes; i++) {
			for (int j = 0; j < reps; j++) {
				modify(docs[i][j]);
			}
			int time = testPassTime();
			if (m_printPass) {
				reportValue("Modify pass " + i, time);
			}
			if (best > time) {
				best = time;
			}
			if (i >= excludes) {
				sum += time;
			}
		}
		results[MODIFY_MIN_INDEX] = best;
		results[MODIFY_AVERAGE_INDEX] = sum / (passes - excludes);

		// make sure generated text matches original document (outside timing)
		Object check = build(new ByteArrayInputStream(output));
		DocumentSummary verify = new DocumentSummary();
		walk(check, verify);
		if (!info.structureEquals(verify)) {
			PrintStream err = m_printStream != null ?
				m_printStream : System.err;
			err.println("  **" + getName() + " Error: " +
				"Document built from output text does " +
				"not match original document**");
			printSummary("  Original", info, err);
			printSummary("  Rebuild", verify, err);
		}

		// check if restored from serialized form
		if (restored != null) {

			// validate the serialization for exact match (outside timing)
			verify.reset();
			walk(restored, verify);
			if (!info.equals(verify)) {
				PrintStream err = m_printStream != null ?
					m_printStream : System.err;
				err.println("  **" + getName() + " Error: " +
					"Document built from output text does " +
					"not match original document**");
				printSummary("  Original", info, err);
				printSummary("  Rebuild", verify, err);
			}
		}

		// copy document summary values for return
		results[ELEMENT_COUNT_INDEX] = info.getElementCount();
		results[ATTRIBUTE_COUNT_INDEX] = info.getAttributeCount();
		results[CONTENT_COUNT_INDEX] = info.getContentCount();
		results[TEXTCHAR_COUNT_INDEX] = info.getTextCharCount();
		results[ATTRCHAR_COUNT_INDEX] = info.getAttrCharCount();

		// print summary for document
		if (m_printSummary) {
			printSummary("  Document", info, m_printStream);
			m_printStream.println("  Original text size was " + text.length +
				", output text size was " + output.length);
			if (serial != null) {
				m_printStream.println("  Serialized length was " +
					serial.length);
			}
			info.reset();
			walk(docs[0][0], info);
			printSummary("  Modified document", info, m_printStream);
/*			if (s_firstTime) {
				out.reset();
				output(docs[0][0], out);
				m_printStream.println(" Text of modified document:");
				m_printStream.println(out.toString());
				s_firstTime = false;
			}	*/
		}
		reset();
		return results;
	}

	/**
	 * Main space test method. This implementation of the abstract base class
	 * method performs the normal sequence of space tests.
	 *
	 * @param count number of units of test to run in this pass
	 * @param excludes number of initialization passes excluded from averages
	 * @param text document text for test
	 * @return result values array
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
		DocumentSummary info = new DocumentSummary();

		// initialize memory information for tests
		initMemory();
		results[INITIAL_MEMORY_INDEX] = (int)m_lastMemory;

		// first build the specified number of copies of the document
		Object[] docs = new Object[passes];
		int base = (int)m_lastMemory;
		for (int i = 0; i < passes; i++) {
			docs[i] = build(ins[i]);
			if (i == 0) {
				results[FIRST_SPACE_INDEX] = testPassSpace();
				if (excludes == 1) {
					base = (int)m_lastMemory;
				}
			} else if ((i+1) == excludes) {
				testPassSpace();
				base = (int)m_lastMemory;
			}
			if (m_printPass) {
				reportValue("Build document pass " + i, testPassSpace());
			}
		}
		testPassSpace();
		results[AVERAGE_SPACE_INDEX] =
			((int)m_lastMemory-base) / (passes - excludes);

		// now walk the constructed document copies
		base = (int)m_lastMemory;
		for (int i = 0; i < passes; i++) {
			info.reset();
			walk(docs[i], info);
			if ((i+1) == excludes) {
				testPassSpace();
				base = (int)m_lastMemory;
			}
			if (m_printPass) {
				reportValue("Walk document pass " + i, testPassSpace());
			}
		}
		testPassSpace();
		results[WALKED_SPACE_INDEX] =
			((int)m_lastMemory-base) / (passes - excludes);

		// free all constructed objects to find final space
		docs = null;
		reset();
		initMemory();
		results[FINAL_MEMORY_INDEX] = (int)m_lastMemory;
		results[DELTA_MEMORY_INDEX] =
			results[FINAL_MEMORY_INDEX] - results[INITIAL_MEMORY_INDEX];
		return results;
	}
}
