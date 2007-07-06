/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.message;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.message.util.Reader2Writer;
import org.apache.axis2.jaxws.message.util.XMLStreamReaderSplitter;

/**
 * Tests XMLStreamReaderSplitter
 */
public class XMLStreamReaderSplitterTests extends TestCase {

	// String test variables
	private static final String sampleText =
		"<body>" + 
		"spurious text" +
		"<pre:a1 xmlns:pre=\"urn://sample\">" +
		"<b1>Hello</b1>" +
		"<c1>World</c1>" +
		"</pre:a1>" +
		"<!-- Spurious Comment -->" +
		"<pre:a2 xmlns:pre=\"urn://sample2\">" +
		"<b2>Hello</b2>" +
		"<c2>World</c2>" +
		"</pre:a2>" +
		"</body>";
	
	
	private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	private static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	
	
	
	public XMLStreamReaderSplitterTests() {
		super();
	}

	public XMLStreamReaderSplitterTests(String arg0) {
		super(arg0);
	}
	
	/**
	 * Test XMLStreamReaderSplitter 
	 * @throws Exception
	 */
	public void test() throws Exception {
		// Create a full XMLStreamReader for the message
		StringReader sr = new StringReader(sampleText);
		XMLStreamReader fullReader = inputFactory.createXMLStreamReader(sr);
		
		// Advance past the first element (body)
		fullReader.next();
		fullReader.next();
		
		// Create a Splitter
		XMLStreamReaderSplitter splitter = new XMLStreamReaderSplitter(fullReader);
		
		// Pipe the splitter to the writer.  This should generated only the 
		// first element tree.
		StringWriter sw = new StringWriter();
		XMLStreamWriter writer = outputFactory.createXMLStreamWriter(sw);
		Reader2Writer r2w = new Reader2Writer(splitter);
		r2w.outputTo(writer);
		writer.flush();
		sw.flush();
		String tree1 = sw.toString();
		
		// Now get the next Stream
		XMLStreamReader stream2 = splitter.getNextReader();
		r2w = new Reader2Writer(stream2);
		sw = new StringWriter();
		writer = outputFactory.createXMLStreamWriter(sw);
		r2w.outputTo(writer);
		writer.flush();
		sw.flush();
		String tree2 = sw.toString();
		
		// Do assertion checks
		assertTrue(!tree1.contains("text"));
		assertTrue(!tree1.contains("Comment"));
		assertTrue( tree1.contains("a1"));
		assertTrue(!tree1.contains("a2"));
		
		assertTrue(!tree2.contains("text"));
		assertTrue(!tree2.contains("Comment"));
		assertTrue(!tree2.contains("a1"));
		assertTrue( tree2.contains("a2"));
	}
}