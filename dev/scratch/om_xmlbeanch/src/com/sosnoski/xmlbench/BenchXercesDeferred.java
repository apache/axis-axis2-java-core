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
 * Benchmark for measuring performance of the Xerces DOM document
 * representation with deferred node expansion enabled. This requires some
 * special handling to avoid problems with the memory allocation state after
 * running a test, so the main test methods from the base class are overridden
 * with methods that force a clean-up.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public class BenchXercesDeferred extends BenchXerces
{
	/** Dummy document used for clearing out parser/model state. */
	private static final String DUMMY_DOCUMENT =
		"<?xml version=\"1.0\"?>\n<doc>empty</doc>\n";

	/**
	 * Constructor.
	 */

	public BenchXercesDeferred() {
		super("Xerces def.", true);
	}

	/**
	 * Main time test method. This override of the base class method first
	 * invokes the base method, then does a dummy parse with deferred
	 * expansion set to <code>false</code> in order to clear out memory.
	 *
	 * @param passes number of passes of each test
	 * @param reps number of copies of document to use for test measurement
	 * @param text document text for test
	 * @return result times array
	 */

/*	public int[] runTimeTest(int passes, int reps, byte[] text) {
		setDeferExpansion(true);
		int[] results = super.runTimeTest(passes, reps, text);
		setDeferExpansion(false);
		build(new ByteArrayInputStream(DUMMY_DOCUMENT.getBytes()));
		return results;
	}	*/
}
