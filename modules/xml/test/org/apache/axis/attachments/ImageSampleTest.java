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
package org.apache.axis.attachments;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.om.AbstractTestCase;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMOutput;
import org.apache.axis.om.OMText;
import org.apache.axis.om.impl.llom.OMElementImpl;
import org.apache.axis.om.impl.llom.OMNamespaceImpl;
import org.apache.axis.om.impl.llom.OMTextImpl;
import org.apache.axis.om.impl.llom.mtom.MTOMStAXSOAPModelBuilder;

/**
 * @author <a href="mailto:thilina@opensource.lk">Thilina Gunarathne </a>
 */

public class ImageSampleTest extends AbstractTestCase {
	
	public ImageSampleTest(String testName) {
		super(testName);
	}
	
	/*
	 * @see TestCase#setUp()
	 */
	Image expectedImage;
	
	MTOMStAXSOAPModelBuilder builder;
	
	DataHandler expectedDH;
	
	File outMTOMFile;
	
	File outBase64File;
	
	String outFileName = "mtom/OMSerializeMTOMOut.txt";
	
	String outBase64FileName = "mtom/OMSerializeBase64Out.xml";
	
	String imageInFileName = "mtom/img/test.jpg";
	
	String imageOutFileName = "mtom/img/testOut.jpg";
	
	String inMimeFileName = "mtom/ImageMTOMOut.bin";
	
	String contentTypeString = "multipart/Related; type=\"application/xop+xml\";start=\"<http://example.org/my.hsh>\"; boundary=\"----=_Part_0_2628939.1119398330392\"";
	
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
	}
	
	public void testImageSampleSerialize() throws Exception {
		
		outMTOMFile = getTestResourceFile(outFileName);
		outBase64File = getTestResourceFile(outBase64FileName);
		OMOutput mtomOutput = new OMOutput(new FileOutputStream(outMTOMFile),true);
		OMOutput baseOutput = new OMOutput(new FileOutputStream(outBase64File),false);
		
		OMNamespaceImpl soap = new OMNamespaceImpl(
				"http://schemas.xmlsoap.org/soap/envelope/", "soap");
		OMElement envelope = new OMElementImpl("Envelope", soap);
		OMElement body = new OMElementImpl("Body", soap);
		
		OMNamespaceImpl dataName = new OMNamespaceImpl(
				"http://www.example.org/stuff", "m");
		OMElement data = new OMElementImpl("data", dataName);
		
		expectedImage = new JDK13IO().loadImage(new FileInputStream(
				getTestResourceFile(imageInFileName)));
		ImageDataSource dataSource = new ImageDataSource("WaterLilies.jpg",
				expectedImage);
		expectedDH = new DataHandler(dataSource);
		OMText binaryNode = new OMTextImpl(expectedDH, false);
		
		envelope.addChild(body);
		body.addChild(data);
		data.addChild(binaryNode);
		
		envelope.serialize(baseOutput);
		baseOutput.flush();
		
		envelope.serialize(mtomOutput);
		mtomOutput.flush();
		mtomOutput.complete();
	}
	
	public void testImageSampleDeserialize() throws Exception {
		InputStream inStream = new FileInputStream(
				getTestResourceFile(inMimeFileName));
		MIMEHelper mimeHelper = new MIMEHelper(inStream, contentTypeString);
		XMLStreamReader reader = XMLInputFactory.newInstance()
		.createXMLStreamReader(
				new BufferedReader(new InputStreamReader(mimeHelper
						.getSOAPPartInputStream())));
		builder = new MTOMStAXSOAPModelBuilder(reader, mimeHelper);
		OMElement root = (OMElement) builder.getDocumentElement();
		// System.out.println(root.getLocalName() + " : "
		//        + root.getNamespaceName());
		OMElement body = (OMElement) root.getFirstChild();
		// System.out.println(body.getLocalName() + " : "
		//         + body.getNamespaceName());
		OMElement data = (OMElement) body.getFirstChild();
		// System.out.println(data.getLocalName() + " : "
		//         + data.getNamespaceName());
		
		OMText blob = (OMText) data.getFirstChild();
		/*
		 * Following is the procedure the user has to follow to read objects in
		 * OBBlob User has to know the object type & whether it is serializable.
		 * If it is not he has to use a Custom Defined DataSource to get the
		 * Object.
		 */
		
		DataHandler actualDH;
		actualDH = blob.getDataHandler();
		Image actualObject = new JDK13IO().loadImage(actualDH.getDataSource()
				.getInputStream());
		FileOutputStream imageOutStream = new FileOutputStream(
				getTestResourceFile(imageOutFileName));
		new JDK13IO().saveImage("image/jpeg", actualObject, imageOutStream);
		
		System.out.println(data.getLocalName() + ":-\t" + actualObject);
		
	}
	
}