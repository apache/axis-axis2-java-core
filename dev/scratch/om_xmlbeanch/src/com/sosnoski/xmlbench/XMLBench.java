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
 * Benchmark program for comparing performance of Java XML parsers and
 * document representations. Performance is measured in terms of both
 * speed and memory requirement for constructing the document representation
 * from a SAX parse, and in terms of speed for walking the representation and
 * generating text output.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */

public class XMLBench
{
	/** Minimum document size to be used for single pass. */
	public static final int MINIMUM_ONEPASS_SIZE = 20000;

	/** Wait time between test documents (ms). */
	public static final int DELAY_BETWEEN_DOCUMENTS = 400;

	/** Output line length for brief format. */
	public static final int BRIEF_LINE_LENGTH = 80;

	/** Width of each item field in brief format. */
	public static final int BRIEF_ITEM_WIDTH = 16;

	/** Number of brief format items per output line. */
	public static final int BRIEF_PER_LINE = BRIEF_LINE_LENGTH/BRIEF_ITEM_WIDTH;

	/**
	 * Get test object. Constructs and returns an instance of the appropriate
	 * test class.
	 *
	 * @param name document model name to be tested
	 * @return document model test class instance (null if name not recognized)
	 */

	private static BenchBase getTestInstance(String name) {
		if (name.equalsIgnoreCase("SAX")) {
			return new BenchSAX();
		} else if (name.equalsIgnoreCase("Crimson")) {
			return new BenchCrimson();
		} else if (name.equalsIgnoreCase("JDOM")) {
			return new BenchJDOM();
		} else if (name.equalsIgnoreCase("dom4j")) {
			return new BenchDOM4J();
		} else if (name.equalsIgnoreCase("Xerces")) {
			return new BenchXercesBase();
		} else if (name.equalsIgnoreCase("XercesD")) {
			return new BenchXercesDeferred();
		} else if (name.equalsIgnoreCase("EXML")) {
			return new BenchElectric();
		} else if (name.equalsIgnoreCase("XPP")) {
			return new BenchXPPBase();
		} else if (name.equalsIgnoreCase("XPPp")) {
			return new BenchXPPPull();
		} else if (name.equalsIgnoreCase("AXTM")) {
			return new BenchATM();
		} else if (name.equalsIgnoreCase("AXTM2")) {
			return new BenchATM2();
		}else if (name.equalsIgnoreCase("AXTM3")) {
			return new BenchATM3();
		}
		return null;
	}

	/**
	 * Read contents of file into byte array.
	 *
	 * @param path file path
	 * @return array of bytes containing all data from file
	 * @throws IOException on file access error
	 */

	private static byte[] getFileBytes(String path) throws IOException {
		File file = new File(path);
		int length = (int)file.length();
		byte[]data = new byte[length];
		FileInputStream in = new FileInputStream(file);
		int offset = 0;
		do {
			offset += in.read(data, offset, length-offset);
		} while (offset < data.length);
		return data;
	}

	/**
	 * Show test file results in brief format. This prints the results with
	 * multiple values per line, using the abbreviated value descriptions.
	 *
	 * @param values test result values (unreported values ignored)
	 * @param descripts value description texts
	 */

	private static void showBrief(int[] values, String[] descripts) {
		StringBuffer line = new StringBuffer(BRIEF_LINE_LENGTH);
		int position = 0;
		for (int j = 0; j < values.length; j++) {
			if (values[j] != Integer.MIN_VALUE) {
				if (position == 0) {
					if (j > 0) {
						System.out.println(line);
						line.setLength(0);
					}
				} else {
					int end = position*BRIEF_ITEM_WIDTH;
					while (line.length() < end) {
						line.append(' ');
					}
				}
				line.append(' ');
				line.append(descripts[j]);
				line.append('=');
				line.append(values[j]);
				position = (position+1) % BRIEF_PER_LINE;
			}
		}
		System.out.println(line);
	}

	/**
	 * Show test file results in full format. This prints the results with
	 * a single value per line, using the detailed value descriptions.
	 *
	 * @param values test result values (unreported values ignored)
	 * @param descripts value description texts
	 */

	private static void showFull(int[] values, String[] descripts) {
		for (int j = 0; j < values.length; j++) {
			if (values[j] != Integer.MIN_VALUE) {
				System.out.println(' ' + descripts[j] + " = " + values[j]);
			}
		}
	}

	/**
	 * Build text for showing results in compressed format. This uses an
	 * abbreviated value description followed by the corresponding result
	 * values for all test files in sequence, with comma separators.
	 *
	 * @param values array of test result value arrays
	 * @param descripts value description texts
	 * @param line buffer for output text accumulation
	 */

	private static void buildCompressed(int[][] values, String[] descripts,
		StringBuffer line) {
		int fcnt = values.length;
		int vcnt = values[0].length;
		for (int i = 0; i < vcnt; i++) {
			line.append(',');
			line.append(descripts[i]);
			for (int j = 0; j < fcnt; j++) {
				line.append(',');
				if (values[j][i] != Integer.MIN_VALUE) {
					line.append(values[j][i]);
				}
			}
		}
	}

	/**
	 * Test driver, just reads the input parameters and executes the test.
	 *
	 * @param argv command line arguments
	 */

	public static void main(String[] argv) {

		// clean up argument text (may have CR-LF line ends, confusing Linux)
		for (int i = 0; i < argv.length; i++) {
			argv[i] = argv[i].trim();
		}

		// parse the leading command line parameters
		boolean valid = true;
		boolean briefflag = false;
		boolean compflag = false;
		boolean detailflag = false;
		boolean interpretflag = false;
		boolean jvmflag = false;
		boolean summaryflag = false;
		boolean memflag = false;
		boolean timeflag = true;
		int npasses = 10;
		int nexclude = 1;
		int anum = 0;
		parse: while (anum < argv.length && argv[anum].charAt(0) == '-') {
			String arg = argv[anum++];
			int cnum = 1;
			while (cnum < arg.length()) {
				char option = Character.toLowerCase(arg.charAt(cnum++));
				switch (option) {

					case 'b':
						briefflag = true;
						break;

					case 'c':
						compflag = true;
						break;

					case 'd':
						detailflag = true;
						break;

					case 'i':
						interpretflag = true;
						break;

					case 'm':
						memflag = true;
						break;

					case 'n':
						timeflag = false;
						break;

					case 's':
						summaryflag = true;
						break;

					case 'v':
						jvmflag = true;
						break;

					case 'p':
					case 'x':
						int value = 0;
						int nchars = 0;
						char chr;
						while (cnum < arg.length() &&
							(chr = arg.charAt(cnum++)) >= '0' && chr <= '9') {
							if (++nchars > 2) {
								valid = false;
								System.err.println("Number value out of range");
								break parse;
							} else {
								value = (value * 10) + (chr - '0');
							}
						}
						if (option == 'p') {
							if (value < 1) {
								valid = false;
								System.err.println("Pass count cannot be 0");
								break parse;
							} else {
								npasses = value;
							}
						} else {
							nexclude = value;
						}
						break;
				}
			}
		}

		// check for invalid pass count and exclude count combination
		if (npasses <= nexclude) {
			System.err.println("Pass count must be greater than exclude count");
			valid = false;
		}

		// next parameter should be the model name
		valid = valid && anum < argv.length;
		BenchBase bench = null;
		if (valid) {

			// construct test instance for requested model
			bench = getTestInstance(argv[anum++]);
			if (bench == null) {
				System.err.println("Unknown model name");
				valid = false;
			} else {
				bench.setPrint(System.out);
				bench.setShowDocument(summaryflag);
				bench.setShowPass(detailflag);
			}
		}

		// handle list of files to be used for test
		if (valid && (memflag || timeflag)) {

			// read data from all input files into array of arrays
			int fcnt = argv.length - anum;
			byte[][] data = new byte[fcnt][];
			int[] reps = new int[fcnt];
			try {
				for (int i = 0; i < fcnt; i++) {
					data[i] = getFileBytes(argv[i+anum]);
					int length = data[i].length;
					reps[i] = (length < MINIMUM_ONEPASS_SIZE) ?
						(MINIMUM_ONEPASS_SIZE + length - 1) / length : 1;
				}
			} catch (IOException ex) {
				ex.printStackTrace(System.err);
				return;
			}

			// report JVM and parameter information
			if (jvmflag) {
				System.out.println("Java version " +
					System.getProperty("java.version"));
				String text = System.getProperty("java.vm.name");
				if (text != null) {
					System.out.println(text);
				}
				text = System.getProperty("java.vm.version");
				if (text != null) {
					System.out.println(text);
				}
				text = System.getProperty("java.vm.vendor");
				if (text == null) {
					text = System.getProperty("java.vendor");
				}
				System.out.println(text);
			}

			// initialize results accumulation array
			int tests = (memflag && timeflag) ? 2 : 1;
			int[][][] results = new int[tests][fcnt][];

			// check for memory test needed
			if (memflag) {

				// execute the test sequence on supplied files
				for (int i = 0; i < fcnt; i++) {

					// check if we're printing results immediately
					if (briefflag | interpretflag) {
						if (i > 0) {
							System.out.println();
						}
						System.out.println("Running " + bench.getName() +
							" with " + npasses + " passes on file " +
							argv[i+anum] + ':');
					}

					// collect test results
					int[] values =
						bench.runSpaceTest(npasses, nexclude, data[i]);
					results[0][i] = values;

					// show results in brief format
					if (briefflag) {
						showBrief(values, BenchBase.s_spaceShortDescriptions);
					}

					// show results in interpreted format
					if (interpretflag) {
						showFull(values, BenchBase.s_spaceFullDescriptions);
					}
				}
			}

			// check for time test needed\
			if (timeflag) {

				// add spacer if running both types of tests
				if (memflag) {
					System.out.println();
				}

				// execute the test sequence on supplied files
				int index = memflag ? 1 : 0;
				for (int i = 0; i < fcnt; i++) {

					// check if we're printing results immediately
					if (briefflag | interpretflag) {
						if (i > 0) {
							System.out.println();
						}
						System.out.print("Running " + bench.getName() +
							" with " + npasses + " passes on file " +
							argv[i+anum]);
						if (reps[i] > 1) {
							System.out.print(" (" + reps[i] +
								" copies per pass)");
						}
						if (nexclude == 1) {
							System.out.print
								(", first pass excluded from averages");
						} else if (nexclude > 1) {
							System.out.print(", first " + nexclude +
								" passes excluded from averages");
						}
						System.out.println(':');
					}

					// collect test results
					int[] values =
						bench.runTimeTest(npasses, reps[i], nexclude, data[i]);
					results[index][i] = values;

					// show results in brief format
					if (briefflag) {
						showBrief(values, BenchBase.s_timeShortDescriptions);
					}

					// show results in interpreted format
					if (interpretflag) {
						showFull(values, BenchBase.s_timeFullDescriptions);
					}
				}
			}

			// print compressed results for all files
			if (compflag) {
				System.out.println("Compressed results for " + bench.getName() +
					" with " + npasses + " passes on the following files:");
				for (int i = 0; i < fcnt; i++) {
					System.out.print(' ' + argv[i+anum]);
					if (reps[i] > 1) {
						System.out.print(" (" + reps[i] + " copies per pass)");
					}
					System.out.println();
				}
				StringBuffer line = new StringBuffer();
				line.append(bench.getName());
				if (memflag) {
					buildCompressed(results[0],
						BenchBase.s_spaceShortDescriptions, line);
				}
				if (timeflag) {
					int index = memflag ? 1 : 0;
					buildCompressed(results[index],
						BenchBase.s_timeShortDescriptions, line);
				}
				System.out.println(line);
			}
			System.out.println();

		} else {
			System.err.println
				("\nUsage: XMLBench [-options] model file-list\n" +
				"Options are:\n" +
				" -b   show brief results (with abbreviated captions)\n" +
				" -c   show compressed results (comma-separated value" +
				" fields with results\n" +
				"      ordered by type and within type by file, useful for" +
				" spreadsheet import)\n" +
				" -d   show detailed per-pass information\n" +
				" -i   show interpreted results (with full captions)\n" +
				" -m   run memory tests (default is time tests only, when" +
				"testing both memory tests are run first)\n" +
				" -n   do not run time tests (default is time tests only)\n" +
				" -pNN run NN passes of each operation on each document," +
				" where NN is 1-99\n" +
				"      (default is p10)\n" +
				" -s   show summary information for each document\n" +
				" -xNN exclude first NN passes of each document from" +
				" averages, where N is\n" +
				"      0-99 (default is x1)\n" +
				" -v   show JVM version information\n" +
				"These options may be concatenated together with a single" +
				" leading dash.\n\n" +
				"Model may be any of the following values:\n" +
				" sax      JAXP-compliant SAX2 parser\n" +
				" crimson  Crimson DOM and parser combination\n" +
				" jdom     JDOM with JAXP-compliant SAX2 parser\n" +
				" dom4j    dom4j with JAXP-compliant SAX2 parser\n" +
				" xerces   Xerces DOM and parser combination\n" +
				" xercesd  Xerces deferred DOM and parser combination\n" +
				" exml     Electric XML model and parser combination\n" +
				" xpp      XPP model and parser combination\n" +
				" xppp     XPP pull model and parser combination\n\n" +
				"The models which support JAXP-compliant SAX2 parsers may " +
				"use any qualifying\n" +
				"parser by setting the javax.xml.parsers.SAXParserFactory " +
				"system property\n" +
				"to the appropriate class.\n");
		}
	}
}
