/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 */
package org.apache.axis2.om.impl.llom.mtom;

import org.apache.axis2.attachments.MIMEHelper;
import org.apache.axis2.om.AbstractTestCase;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.OMXMLParserWrapper;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * @author <a href="mailto:thilina@opensource.lk">Thilina Gunarathne </a>
 */

public class MTOMStAXSOAPModelBuilderTest extends AbstractTestCase {
	MIMEHelper mimeHelper;
	
	String inFileName;
	
	OMXMLParserWrapper builder;
	
	/**
	 * @param testName
	 */
	public MTOMStAXSOAPModelBuilderTest(String testName) {
		super(testName);
	}
	
	String contentTypeString = "multipart/Related; type=\"application/xop+xml\"; boundary=\"----=_AxIs2_Def_boundary_=42214532\"; start=\"<SOAPPart>\"";
	
	protected void setUp() throws Exception {
		super.setUp();
		inFileName = "mtom/MTOMBuilderTestIn.txt";
		InputStream inStream = new FileInputStream(
				getTestResourceFile(inFileName));
		mimeHelper = new MIMEHelper(inStream, contentTypeString);
		XMLStreamReader reader = XMLInputFactory.newInstance()
		.createXMLStreamReader(
				new BufferedReader(new InputStreamReader(mimeHelper
						.getSOAPPartInputStream())));
		builder = new MTOMStAXSOAPModelBuilder(reader, mimeHelper);
		
	}
	
	public void testCreateOMElement() throws Exception {
		OMElement root = (OMElement) builder.getDocumentElement();
		System.out.println(root.getLocalName() + " : "
				+ root.getNamespace().getName());
		OMElement body = (OMElement) root.getFirstChild();
		System.out.println(body.getLocalName() + " : "
				+ body.getNamespace().getName());
		
		OMElement data = (OMElement) body.getFirstChild();
		System.out.println(data.getLocalName() + " : "
				+ data.getNamespace().getName());
		Iterator childIt = data.getChildren();
		//while (childIt.hasNext()) {
		OMElement child = (OMElement) childIt.next();
		OMText blob = (OMText) child.getFirstChild();
		/*
		 * Following is the procedure the user has to follow to read objects in
		 * OBBlob User has to know the object type & whether it is serializable.
		 * If it is not he has to use a Custom Defined DataSource to get the
		 * Object.
		 */
		byte[] expectedObject = new byte[] { 13, 56, 65, 32, 12, 12, 7, -3, -2,
				-1, 98 };
		DataHandler actualDH;
		actualDH = blob.getDataHandler();
		//ByteArrayInputStream object = (ByteArrayInputStream) actualDH
		//.getContent();
		//byte[] actualObject= null;
		//  object.read(actualObject,0,10);
		
		//  assertEquals("Object check", expectedObject[5],actualObject[5] );
	}
	
	public void testGetDataHandler() {
	}
	
}