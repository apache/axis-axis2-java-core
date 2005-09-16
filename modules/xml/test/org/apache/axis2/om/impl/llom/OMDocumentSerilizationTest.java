/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.om.impl.llom;

import junit.framework.TestCase;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMDocument;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.impl.OMOutputImpl;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;

/**
 * This tests the serialize method 
 */
public class OMDocumentSerilizationTest extends TestCase {

	private OMDocument document;
	private String xmlDeclStart = "<?xml";
	private String encoding = "encoding='UTF-8'";
	private String encoding_UTF16 = "encoding='UTF-16'";
    private String encoding2 = "encoding=\"UTF-8\"";
    private String encoding2_UTF16 = "encoding=\"UTF-16\"";
	private String version = "version='1.0'";
	private String version_11 = "version='1.1'";
    private String version2 = "version=\"1.0\"";
    private String version2_11 = "version=\"1.1\"";

	public void setUp() {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		
		OMNamespace namespace = factory.createOMNamespace("http://testuri.org","test");
		OMElement documentElement = factory.createOMElement("DocumentElement",namespace);
		
		OMElement child1 = factory.createOMElement("Child1",namespace);
		child1.setText("TestText");
		documentElement.addChild(child1);
		
		document = factory.createOMDocument();
		document.setDocumentElement(documentElement);
		
	}
	
	public OMDocumentSerilizationTest(String name) {
		super(name);
	}

	
	public void testXMLDecleration() throws XMLStreamException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OMOutputImpl output = new OMOutputImpl(baos,false);
		document.serialize(output);
		output.flush();
		
		String xmlDocument = new String(baos.toByteArray());
		
		assertTrue("XML Declaration missing",-1<xmlDocument.indexOf(xmlDeclStart));
	}
	
	public void testExcludeXMLDeclaration() throws XMLStreamException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OMOutputImpl output = new OMOutputImpl(baos,false);
		document.serialize(output,false);
		output.flush();
		
		String xmlDocument = new String(baos.toByteArray());
		
		assertTrue(
				"XML Declaration is included when serilizing without the declaration",
				-1 == xmlDocument.indexOf(xmlDeclStart));
	}
	
	public void testCharsetEncoding() throws XMLStreamException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OMOutputImpl output = new OMOutputImpl(baos,false);
        output.ignoreXMLDeclaration(false);
        document.serialize(output);
		output.flush();

        String xmlDocument = new String(baos.toByteArray());
        System.out.println("xmlDocument = " + xmlDocument);

        assertTrue("Charset declaration missing",-1 < xmlDocument.indexOf(encoding) ||
                                                 -1 < xmlDocument.indexOf(encoding.toLowerCase()) ||
                                                 -1 < xmlDocument.indexOf(encoding2.toLowerCase()) ||
                                                 -1 < xmlDocument.indexOf(encoding2));
	}
	
	public void testCharsetEncodingUTF_16() throws XMLStreamException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OMOutputImpl output = new OMOutputImpl(baos,false);
		output.setCharSetEncoding("UTF-16");
		document.serialize(output);
		output.flush();
		
		String xmlDocument = new String(baos.toByteArray());
		assertTrue("Charset declaration missing",-1<xmlDocument.indexOf(encoding_UTF16) ||
                                                 -1<xmlDocument.indexOf(encoding2_UTF16));
	}
		
	
	public void testXMLVersion() throws XMLStreamException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OMOutputImpl output = new OMOutputImpl(baos,false);
		document.serialize(output);
		output.flush();
		
		String xmlDocument = new String(baos.toByteArray());
		assertTrue("Charset declaration missing",-1<xmlDocument.indexOf(version) ||
                                                 -1<xmlDocument.indexOf(version2));
	}

	public void testXMLVersion_11() throws XMLStreamException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OMOutputImpl output = new OMOutputImpl(baos,false);
		document.setXMLVersion("1.1");
		document.serialize(output);
		output.flush();
		
		String xmlDocument = new String(baos.toByteArray());
		assertTrue("Charset declaration missing",-1<xmlDocument.indexOf(version_11) ||
                                                 -1<xmlDocument.indexOf(version2_11));
	}
}
