package org.apache.axis2.om.impl.llom;

import java.io.ByteArrayOutputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMDocument;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.impl.OMOutputImpl;

import junit.framework.TestCase;

/**
 * This tests the serialize method 
 * 
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public class OMDocumentSerilizationTest extends TestCase {

	private OMDocument document;
	private String xmlDeclStart = "<?xml";
	private String encoding = "encoding='UTF-8'";
	private String encoding_UTF16 = "encoding='UTF-16'";
	private String version = "version='1.0'"; 
	private String version_11 = "version='1.1'";
	
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
		
		String xmlDocuemnt = new String(baos.toByteArray());
		
		assertTrue("XML Declaration missing",-1<xmlDocuemnt.indexOf(xmlDeclStart));
	}
	
	public void testExcludeXMLDeclaration() throws XMLStreamException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OMOutputImpl output = new OMOutputImpl(baos,false);
		document.serialize(output,false);
		output.flush();
		
		String xmlDocuemnt = new String(baos.toByteArray());
		
		assertTrue(
				"XML Declaration is included when serilizing without the declaration",
				-1 == xmlDocuemnt.indexOf(xmlDeclStart));		
	}
	
	public void testCharsetEncoding() throws XMLStreamException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OMOutputImpl output = new OMOutputImpl(baos,false);
		document.serialize(output);
		output.flush();
		
		String xmlDocuemnt = new String(baos.toByteArray());
		
		assertTrue("Charset declaration missing",-1<xmlDocuemnt.indexOf(encoding));		
	}
	
	public void testCharsetEncodingUTF_16() throws XMLStreamException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OMOutputImpl output = new OMOutputImpl(baos,false);
		output.setCharSetEncoding("UTF-16");
		document.serialize(output);
		output.flush();
		
		String xmlDocuemnt = new String(baos.toByteArray());
		assertTrue("Charset declaration missing",-1<xmlDocuemnt.indexOf(encoding_UTF16));		
	}
		
	
	public void testXMLVersion() throws XMLStreamException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OMOutputImpl output = new OMOutputImpl(baos,false);
		document.serialize(output);
		output.flush();
		
		String xmlDocuemnt = new String(baos.toByteArray());
		assertTrue("Charset declaration missing",-1<xmlDocuemnt.indexOf(version));		
	}

	public void testXMLVersion_11() throws XMLStreamException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OMOutputImpl output = new OMOutputImpl(baos,false);
		document.setXMLVersion("1.1");
		document.serialize(output);
		output.flush();
		
		String xmlDocuemnt = new String(baos.toByteArray());
		assertTrue("Charset declaration missing",-1<xmlDocuemnt.indexOf(version_11));		
	}
}
