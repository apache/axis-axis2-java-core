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
 * Base class for XML benchmark tests. This class provides some basic methods
 * used by the testing. It must be subclassed for each particular parser or
 * document representation to be tested.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public abstract class BenchBase
{
	// Indices for vector of values returned by time test
	/** Best document build time result index. */
	public static final int BUILD_MIN_INDEX = 0;
	/** Average document build time result index. */
	public static final int BUILD_AVERAGE_INDEX = 1;
	/** Best document walk time result index. */
	public static final int WALK_MIN_INDEX = 2;
	/** Average document walk time result index. */
	public static final int WALK_AVERAGE_INDEX = 3;
	/** Best document text generation time result index. */
	public static final int TEXT_MIN_INDEX = 4;
	/** Average document text generation time result index. */
	public static final int TEXT_AVERAGE_INDEX = 5;
	/** Best serialization time result index. */
	public static final int SERIALIZE_MIN_INDEX = 6;
	/** Average serialization time result index. */
	public static final int SERIALIZE_AVERAGE_INDEX = 7;
	/** Best unserialization time walk time result index. */
	public static final int UNSERIALIZE_MIN_INDEX = 8;
	/** Average unserialization time walk time result index. */
	public static final int UNSERIALIZE_AVERAGE_INDEX = 9;
	/** Best modification time result index. */
	public static final int MODIFY_MIN_INDEX = 10;
	/** Average modification time result index. */
	public static final int MODIFY_AVERAGE_INDEX = 11;
	/** Serialized size result index. */
	public static final int SERIALIZE_SIZE_INDEX = 12;
	/** Element count index. */
	public static final int ELEMENT_COUNT_INDEX = 13;
	/** Content text segment count index. */
	public static final int CONTENT_COUNT_INDEX = 14;
	/** Attribute count index. */
	public static final int ATTRIBUTE_COUNT_INDEX = 15;
	/** Text character count index. */
	public static final int TEXTCHAR_COUNT_INDEX = 16;
	/** Attribute character count index. */
	public static final int ATTRCHAR_COUNT_INDEX = 17;
	/** Count of result values returned. */
	public static final int TIME_RESULT_COUNT = 18;

	// Indices for vector of values returned by space test
	/** Initial memory usage (before document construction). */
	public static final int INITIAL_MEMORY_INDEX = 0;
	/** Final memory usage (all documents released). */
	public static final int FINAL_MEMORY_INDEX = 1;
	/** Net change in memory usage from start to end. */
	public static final int DELTA_MEMORY_INDEX = 2;
	/** First document memory usage. */
	public static final int FIRST_SPACE_INDEX = 3;
	/** Last document memory usage. */
	public static final int AVERAGE_SPACE_INDEX = 4;
	/** Memory usage change after walking document. */
	public static final int WALKED_SPACE_INDEX = 5;
	/** Count of result values returned. */
	public static final int SPACE_RESULT_COUNT = 6;

	/** Abbreviated descriptions of time test result values. */
	static public final String[] s_timeShortDescriptions =
	{
		"Build mn",
		"Build av",
		"Walk mn",
		"Walk av",
		"Write mn",
		"Write av",
		"Ser mn",
		"Ser av",
		"Unser mn",
		"Unser av",
		"Mod mn",
		"Mod av",
		"Ser sz",
		"Elems",
		"Conts",
		"Attrs",
		"Text ch",
		"Attr ch"
	};

	/** Full descriptions of time test result values. */
	static public final String[] s_timeFullDescriptions =
	{
		"Build document minimum time (ms)",
		"Build document average time (ms)",
		"Walk document minimum time (ms)",
		"Walk document average time (ms)",
		"Write document text minimum time (ms)",
		"Write document text average time (ms)",
		"Serialize document minimum time (ms)",
		"Serialize document average time (ms)",
		"Unserialize document minimum time (ms)",
		"Unserialize document average time (ms)",
		"Modify document minimum time (ms)",
		"Modify document average time (ms)",
		"Serialized document size (bytes)",
		"Elements in document",
		"Content text segments in document",
		"Attributes in document",
		"Text content characters in document",
		"Attribute value characters in document"
	};

	/** Abbreviated descriptions of space test result values. */
	static public final String[] s_spaceShortDescriptions =
	{
		"Init mem",
		"End mem",
		"Chg mem",
		"First sz",
		"Avg sz",
		"Walked sz"
	};

	/** Full descriptions of space test result values. */
	static public final String[] s_spaceFullDescriptions =
	{
		"Initial memory usage before document parse (bytes)",
		"End memory usage after documents released (bytes)",
		"Net change in memory usage (bytes)",
		"First document copy memory size (bytes)",
		"Average document copy memory size (bytes)",
		"Last walked document memory increase (bytes)"
	};

	/** Interval in milliseconds to wait for garbage collection. */
	public static final long GARBAGE_COLLECT_DELAY = 1000;

	/** Memory usage at start of test. */
	protected static long m_lastMemory;

	/** Time at start of test. */
	protected static long m_lastTime;

	/** Name for this test configuration. */
	protected final String m_configName;

	/** Optional variant information for test configuration. */
	protected String m_configVariant;

	/** Destination for test results listing. */
	protected PrintStream m_printStream;

	/** Flag for printing document summary information. */
	protected boolean m_printSummary;

	/** Flag for printing detailed pass results. */
	protected boolean m_printPass;

	/**
	 * Constructor.
	 *
	 * @param config test configuration name
	 */

	protected BenchBase(String config) {
		m_configName = config;
	}

	/**
	 * Initializes the memory state prior to a test run. This method first
	 * requests a garbage collection operation, then waits for a fixed interval
	 * in order to encourage the JVM to do the collection. It also sets the
	 * start of test value for memory usage.
	 */

	protected void initMemory() {
		long done = System.currentTimeMillis() + GARBAGE_COLLECT_DELAY;
		Runtime rt = Runtime.getRuntime();
		while (System.currentTimeMillis() < done) {
			rt.gc();
			try {
				Thread.sleep(GARBAGE_COLLECT_DELAY);
			} catch (InterruptedException ex) {}
		}
		m_lastMemory = rt.totalMemory() - rt.freeMemory();
	}

	/**
	 * Initializes the time prior to a test run. This method justs sets the
	 * start of test time from the system clock.
	 */

	protected void initTime() {
		m_lastTime = System.currentTimeMillis();
	}

	/**
	 * Report a value. Prints the leading text and the value with a space
	 * between, if printing is enabled.
	 *
	 * @param lead leading text for test results
	 * @param value value to be printed
	 */

	protected void reportValue(String lead, int value) {
		if (m_printStream != null) {
			m_printStream.println("  " + lead + ' ' + value);
		}
	}

	/**
	 * Find test pass time. Besides returning the time for the last test pass,
	 * this sets the current time as the start of the next test pass.
	 *
	 * @return milliseconds taken for the test
	 */

	protected int testPassTime() {
		long now = System.currentTimeMillis();
		int time = (int)(now-m_lastTime);
		m_lastTime = now;
		return time;
	}

	/**
	 * Find test pass space. Besides returning the space for the last test pass,
	 * this sets the current space usage as the start of the next test pass.
	 *
	 * @return bytes of memory added by test pass (negative if space released)
	 */

	protected int testPassSpace() {
		long done = System.currentTimeMillis() + GARBAGE_COLLECT_DELAY;
		while (System.currentTimeMillis() < done) {
			System.gc();
			try {
				Thread.sleep(GARBAGE_COLLECT_DELAY);
			} catch (InterruptedException ex) {}
		}
		Runtime rt = Runtime.getRuntime();
		long used = rt.totalMemory() - rt.freeMemory();
		long diff = used - m_lastMemory;
		m_lastMemory = used;
		return (int)diff;
	}

	/**
	 * Report the results of a time test run. Prints the time taken for the
	 * last test and sets the current time as the start time for the next
	 * test.
	 *
	 * @param test test description for display
	 * @return milliseconds taken for the test
	 */

	protected int reportTime(String test) {
		int time = testPassTime();
		if (m_printStream != null) {
			m_printStream.println("  " + test + " in " + time + " ms.");
		}
		return time;
	}

	/**
	 * Report the results of a memory test run. First attempts a
	 * garbage collection operation before computing the difference between
	 * the memory in use at the end of the test and that in use at the start
	 * of the test. Prints the space used by the test and sets the current
	 * space as the base for the next test.
	 *
	 * @param test test description for display
	 * @return space used by test
	 */

	protected int reportSpace(String test) {
		int space = testPassSpace();
		if (m_printStream != null) {
			m_printStream.println("  " + test + " used space " + space);
		}
		return space;
	}

	/**
	 * Print document summary information. Prints the information with a
	 * supplied lead phrase.
	 *
	 * @param lead lead text phrase for document summary
	 * @param info document summary information
	 * @param print stream on which to print
	 */

	protected void printSummary(String lead, DocumentSummary info,
		PrintStream print) {
		print.println(lead + " has " + info.getElementCount() +
			" elements, " +	info.getAttributeCount() +
			" attributes with " + info.getAttrCharCount() +
			" characters of data, and " + info.getContentCount() +
			" content text segements with " + info.getTextCharCount() +
			" characters of text");
	}

	/**
	 * Get configuration name. Returns the name of the document model used
	 * by this test, with any supplied variant information appended in
	 * parenthesis.
	 *
	 * @return document model name
	 */

	public String getName() {
		if (m_configVariant == null) {
			return m_configName;
		} else {
			return m_configName + " (" + m_configVariant + ')';
		}
	}

	/**
	 * Set configuration variant information. This may be used by subclasses
	 * which need to deal with several variations of a single configuration.
	 *
	 * @param variant configuration variant description, appended to name
	 */

	protected void setVariant(String variant) {
		m_configVariant = variant;
	}

	/**
	 * Set output print stream for printing detailed test run results.
	 *
	 * @param print test results listing destination (<code>null</code> if
	 * no listing output)
	 */

	public void setPrint(PrintStream print) {
		m_printStream = print;
	}

	/**
	 * Set flag for printing document summary information.
	 *
	 * @param show flag for document summary information to be printed
	 */

	public void setShowDocument(boolean show) {
		m_printSummary = show;
	}

	/**
	 * Set flag for printing individual test pass results.
	 *
	 * @param show flag for document summary information to be printed
	 */

	public void setShowPass(boolean show) {
		m_printPass = show;
	}

	/**
	 * Main time test method. This must be implemented by the subclass to
	 * perform the sequence of speed tests appropriate to the test
	 * platform.
	 *
	 * @param passes number of passes of each test
	 * @param reps number of copies of document to use for test measurement
	 * @param excludes number of initialization passes excluded from averages
	 * @param text document text for test
	 * @return result times array
	 */

	public abstract int[] runTimeTest(int passes, int reps, int excludes,
		byte[] text);

	/**
	 * Main space test method. This must be implemented by the subclass to
	 * perform the sequence of space tests appropriate to the test
	 * platform.
	 *
	 * @param count number of units of test to run in this pass
	 * @param excludes number of initialization passes excluded from averages
	 * @param text document text for test
	 * @return result spaces array
	 */

	public abstract int[] runSpaceTest(int count, int excludes, byte[] text);
}
