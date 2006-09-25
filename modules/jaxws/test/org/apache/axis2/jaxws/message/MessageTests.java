/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.message;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.SAAJConverterFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.message.util.Reader2Writer;
import org.apache.axis2.jaxws.message.util.SAAJConverter;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

import test.EchoStringResponse;
import test.ObjectFactory;

/**
 * MessageTests
 * Tests to create and validate Message processing
 * These are not client/server tests.
 */
public class MessageTests extends TestCase {

	// String test variables
	private static final String soap11env = "http://schemas.xmlsoap.org/soap/envelope/";
	private static final String soap12env = "http://www.w3.org/2003/05/soap-envelope";
    private static final String sampleEnvelopeHead11 =
        "<soapenv:Envelope xmlns:soapenv=\"" + soap11env + "\">" +
        "<soapenv:Header /><soapenv:Body>";
    
    private static final String sampleEnvelopeHead12 =
        "<soapenv:Envelope xmlns:soapenv=\"" + soap12env + "\">" +
        "<soapenv:Header /><soapenv:Body>";
    
    private static final String sampleEnvelopeTail = 
        "</soapenv:Body></soapenv:Envelope>";
    
    private static final String sampleText =
		"<pre:a xmlns:pre=\"urn://sample\">" +
		"<b>Hello</b>" +
		"<c>World</c>" +
		"</pre:a>";
	
    private static final String sampleEnvelope11 = 
        sampleEnvelopeHead11 +
        sampleText +
        sampleEnvelopeTail;
    
    private static final String sampleEnvelope12 = 
        sampleEnvelopeHead12 +
        sampleText +
        sampleEnvelopeTail;
        
    private static final String sampleJAXBText = 
        "<echoStringResponse xmlns=\"http://test\">" +
        "<echoStringReturn>sample return value</echoStringReturn>" + 
        "</echoStringResponse>";
    
    private static final String sampleJAXBEnvelope11 = 
        sampleEnvelopeHead11 + 
        sampleJAXBText + 
        sampleEnvelopeTail;
    
    private static final String sampleJAXBEnvelope12 = 
        sampleEnvelopeHead12 + 
        sampleJAXBText + 
        sampleEnvelopeTail;

    private static final String sampleEnvelopeNoHeader11 =
        "<soapenv:Envelope xmlns:soapenv=\""+ soap11env +"\">" +
        "<soapenv:Body>" + 
        sampleText + 
        "</soapenv:Body></soapenv:Envelope>";
    
    private static final String sampleEnvelopeNoHeader12 =
        "<soapenv:Envelope xmlns:soapenv=\""+ soap12env +"\">" +
        "<soapenv:Body>" + 
        sampleText + 
        "</soapenv:Body></soapenv:Envelope>";
    
    
    
	private static final QName sampleQName = new QName("urn://sample", "a");
	
	private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	
	public MessageTests() {
		super();
	}

	public MessageTests(String arg0) {
		super(arg0);
	}
	
	/**
	 * Create a Block representing an XMLString and simulate a 
	 * normal Dispatch<String> flow
	 * @throws Exception
	 */
	public void testStringOutflow() throws Exception {
		
		// Create a SOAP 1.1 Message
		MessageFactory mf = (MessageFactory)
			FactoryRegistry.getFactory(MessageFactory.class);
		Message m = mf.create(Protocol.soap11);
		
		// Get the BlockFactory
		XMLStringBlockFactory f = (XMLStringBlockFactory)
			FactoryRegistry.getFactory(XMLStringBlockFactory.class);
		
		// Create a Block using the sample string as the content.  This simulates
		// what occurs on the outbound JAX-WS dispatch<String> client
		Block block = f.createFrom(sampleText, null, null);
		
		// Add the block to the message as normal body content.
		m.setBodyBlock(0, block);
		
		// Assuming no handlers are installed, the next thing that will happen
		// is a XMLStreamReader will be requested...to go to OM.   At this point the
		// block should be consumed.
		OMElement om = m.getAsOMElement();
		
		// The block should not be consumed yet...because the message has not been read
		assertTrue(!block.isConsumed());
		
		// To check that the output is correct, get the String contents of the 
		// reader
		Reader2Writer r2w = new Reader2Writer(om.getXMLStreamReaderWithoutCaching());
		String newText = r2w.getAsString();
		System.out.println(newText);
		assertTrue(newText.contains(sampleText));
		assertTrue(newText.contains("soap"));
		assertTrue(newText.contains("Envelope"));
		assertTrue(newText.contains("Body"));
		
		// The block should be consumed at this point
		assertTrue(block.isConsumed());
	}

	/**
	 * Create a Block representing an XMLString and simulate a 
	 * normal Dispatch<String> flow with an application handler
	 * @throws Exception
	 */
	public void testStringOutflow2() throws Exception {
		
		// Create a SOAP 1.1 Message
		MessageFactory mf = (MessageFactory)
			FactoryRegistry.getFactory(MessageFactory.class);
		Message m = mf.create(Protocol.soap11);
		
		// Get the BlockFactory
		XMLStringBlockFactory f = (XMLStringBlockFactory)
			FactoryRegistry.getFactory(XMLStringBlockFactory.class);
		
		// Create a Block using the sample string as the content.  This simulates
		// what occurs on the outbound JAX-WS dispatch<String> client
		Block block = f.createFrom(sampleText, null, null);
		
		// Add the block to the message as normal body content.
		m.setBodyBlock(0, block);
		
		// If there is a JAX-WS handler, the Message is converted into a SOAPEnvelope
		SOAPEnvelope soapEnvelope = m.getAsSOAPEnvelope();
		
		// Normally the handler would not touch the body...but for our scenario, assume that it does.
		String name = soapEnvelope.getBody().getFirstChild().getLocalName();
		assertTrue("a".equals(name));
		
		// The block should be consumed at this point
		assertTrue(block.isConsumed());
		
		// After the handler processing the message is obtained as an OM
		OMElement om = m.getAsOMElement();
		
		// To check that the output is correct, get the String contents of the 
		// reader
		Reader2Writer r2w = new Reader2Writer(om.getXMLStreamReaderWithoutCaching());
		String newText = r2w.getAsString();
		System.out.println(newText);
		assertTrue(newText.contains(sampleText));
		assertTrue(newText.contains("soap"));
		assertTrue(newText.contains("Envelope"));
		assertTrue(newText.contains("Body"));
		
		
	}
	
	
	/**
	 * Create a Block representing an XMLString and simulate a 
	 * normal Dispatch<String> input flow
	 * @throws Exception
	 */
	public void testStringInflow_soap11() throws Exception {
		_testStringInflow(sampleEnvelope11);
	}
	public void testStringInflow_soap12() throws Exception {
		_testStringInflow(sampleEnvelope12);
	}
	public void _testStringInflow(String sampleEnvelope) throws Exception {
		
		// On inbound, there will already be an OM
		// which represents the message.  The following code simulates the input
		// OM
		StringReader sr = new StringReader(sampleEnvelope);
		XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
		StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
		OMElement omElement = builder.getSOAPEnvelope();
		
		// The JAX-WS layer creates a Message from the OM
		MessageFactory mf = (MessageFactory)
			FactoryRegistry.getFactory(MessageFactory.class);
		Message m = mf.createFrom(omElement);
		
		// Assuming no handlers are installed, the next thing that will happen
		// is the proxy code will ask for the business object (String).
		XMLStringBlockFactory blockFactory = 
			(XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
		Block block = m.getBodyBlock(0, null, blockFactory);
		Object bo = block.getBusinessObject(true);
		assertTrue(bo instanceof String);
		
		// The block should be consumed
		assertTrue(block.isConsumed());
		
		// Check the String for accuracy
		assertTrue(sampleText.equals(bo.toString()));
		
	}
	
	/**
	 * Create a Block representing an XMLString and simulate a 
	 * normal Dispatch<String> input flow with a JAX-WS Handler
	 * @throws Exception
	 */
	public void testStringInflow2_soap11() throws Exception {
		_testStringInflow2(sampleEnvelope11);
	}
	public void testStringInflow2_soap12() throws Exception {
		// Only run test if an SAAJ 1.3 MessageFactory is available
		javax.xml.soap.MessageFactory mf = null;
		try {
			mf = getSAAJConverter().createMessageFactory(soap12env);
		} catch (Exception e) {}
		if (mf != null) {
			_testStringInflow2(sampleEnvelope12);
		}
	}
	public void _testStringInflow2(String sampleEnvelope) throws Exception {
		
		// On inbound, there will already be an OM
		// which represents the message.  The following code simulates the input
		// OM
		StringReader sr = new StringReader(sampleEnvelope);
		XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
		StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
		OMElement omElement = builder.getSOAPEnvelope();
		
		// The JAX-WS layer creates a Message from the OM
		MessageFactory mf = (MessageFactory)
			FactoryRegistry.getFactory(MessageFactory.class);
		Message m = mf.createFrom(omElement);
		
		// If there is a JAX-WS handler, the Message is converted into a SOAPEnvelope
		SOAPEnvelope soapEnvelope = m.getAsSOAPEnvelope();
		
		// Normally the handler would not touch the body...but for our scenario, assume that it does.
		String name = soapEnvelope.getBody().getFirstChild().getLocalName();
		assertTrue("a".equals(name));
		
		// The next thing that will happen
		// is the proxy code will ask for the business object (String).
		XMLStringBlockFactory blockFactory = 
			(XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
		Block block = m.getBodyBlock(0, null, blockFactory);
		Object bo = block.getBusinessObject(true);
		assertTrue(bo instanceof String);
		
		// The block should be consumed
		assertTrue(block.isConsumed());
		
		// Check the String for accuracy
		assertTrue(sampleText.equals(bo.toString()));
		
	}
	
	/**
	 * Create a Block representing an XMLString and simulate a 
	 * normal Dispatch<String> input flow with a JAX-WS Handler that needs the whole Message
	 * @throws Exception
	 */
	public void testStringInflow3_soap11() throws Exception {
		_testStringInflow3(sampleEnvelope11);
	}
	public void testStringInflow3_soap12() throws Exception {
		//Only run test if an SAAJ 1.3 MessageFactory is available
		javax.xml.soap.MessageFactory mf = null;
		try {
			mf = getSAAJConverter().createMessageFactory(soap12env);
		} catch (Exception e) {}
		if (mf != null) {
			_testStringInflow3(sampleEnvelope12);
		}
	}
	public void _testStringInflow3(String sampleEnvelope) throws Exception {
		
		// On inbound, there will already be an OM
		// which represents the message.  The following code simulates the input
		// OM
		StringReader sr = new StringReader(sampleEnvelope);
		XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
		StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
		OMElement omElement = builder.getSOAPEnvelope();
		
		// The JAX-WS layer creates a Message from the OM
		MessageFactory mf = (MessageFactory)
			FactoryRegistry.getFactory(MessageFactory.class);
		Message m = mf.createFrom(omElement);
		
		// If there is a JAX-WS handler, the Message is converted into a SOAPEnvelope
		SOAPMessage sm = m.getAsSOAPMessage();
		
		// Normally the handler would not touch the body...but for our scenario, assume that it does.
		String name = sm.getSOAPBody().getFirstChild().getLocalName();
		assertTrue("a".equals(name));
		
		// The next thing that will happen
		// is the proxy code will ask for the business object (String).
		XMLStringBlockFactory blockFactory = 
			(XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
		Block block = m.getBodyBlock(0, null, blockFactory);
		Object bo = block.getBusinessObject(true);
		assertTrue(bo instanceof String);
		
		// The block should be consumed
		assertTrue(block.isConsumed());
		
		// Check the String for accuracy
		assertTrue(sampleText.equals(bo.toString()));
		
	}
    
    /**
     * Create a Block representing an XMLString, but this time use one that
     * doesn't have a &lt;soap:Header&gt; element in it.
     * @throws Exception
     */
	public void testStringInflow4_soap11() throws Exception {
		_testStringInflow4(sampleEnvelopeNoHeader11);
	}
	public void testStringInflow4_soap12() throws Exception {
		_testStringInflow4(sampleEnvelopeNoHeader12);
	}
	public void _testStringInflow4(String sampleEnvelopeNoHeader) throws Exception {
        // On inbound, there will already be an OM
        // which represents the message.  The following code simulates the input
        // OM
        StringReader sr = new StringReader(sampleEnvelopeNoHeader);
        XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
        OMElement omElement = builder.getSOAPEnvelope();
        
        // The JAX-WS layer creates a Message from the OM
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.createFrom(omElement);
        
        // The next thing that will happen
        // is the proxy code will ask for the business object (String).
        XMLStringBlockFactory blockFactory = 
            (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
        Block block = m.getBodyBlock(0, null, blockFactory);
        Object bo = block.getBusinessObject(true);
        assertTrue(bo instanceof String);
        
        // The block should be consumed
        assertTrue(block.isConsumed());
        
        // Check the String for accuracy
        assertTrue(sampleText.equals(bo.toString()));
        
    }
    
    /**
     * Create a JAXBBlock containing a JAX-B business object 
     * and simulate a normal Dispatch<Object> output flow
     * @throws Exception
     */
    public void testJAXBOutflow() throws Exception {
        // Create a SOAP 1.1 Message
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.create(Protocol.soap11);
        
        // Get the BlockFactory
        JAXBBlockFactory bf = (JAXBBlockFactory)
            FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        // Create the JAX-B object
        ObjectFactory of = new ObjectFactory();
        EchoStringResponse obj = of.createEchoStringResponse();
        obj.setEchoStringReturn("sample return value");
        
        // Create the JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("test");
        
        
        // Create a JAXBBlock using the Echo object as the content.  This simulates
        // what occurs on the outbound JAX-WS Dispatch<Object> client
        Block block = bf.createFrom(obj, jbc, null);
        
        // Add the block to the message as normal body content.
        m.setBodyBlock(0, block);
        
        // On an outbound flow, we need to convert the Message 
        // to an OMElement, specifically an OM SOAPEnvelope, 
        // so we can set it on the Axis2 MessageContext
        org.apache.axiom.soap.SOAPEnvelope env = 
            (org.apache.axiom.soap.SOAPEnvelope) m.getAsOMElement();
        
        // Serialize the Envelope using the same mechanism as the 
        // HTTP client.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        env.serializeAndConsume(baos, new OMOutputFormat());
        
        // To check that the output is correct, get the String contents of the 
        // reader
        String newText = baos.toString();
        System.out.println(newText);
        assertTrue(newText.contains(sampleJAXBText));
        assertTrue(newText.contains("soap"));
        assertTrue(newText.contains("Envelope"));
        assertTrue(newText.contains("Body"));
    }
    
    public void testJAXBInflow_soap11() throws Exception {
		_testJAXBInflow(sampleJAXBEnvelope11);
	}
	public void testJAXBInflow_soap12() throws Exception {
		_testJAXBInflow(sampleJAXBEnvelope12);
	}
	public void _testJAXBInflow(String sampleJAXBEnvelope) throws Exception {
        // Create a SOAP OM out of the sample incoming XML.  This
        // simulates what Axis2 will be doing with the inbound message. 
        StringReader sr = new StringReader(sampleJAXBEnvelope);
        XMLStreamReader inflow = inputFactory.createXMLStreamReader(sr);
        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow, null);
        OMElement omElement = builder.getSOAPEnvelope();
        
        // Create a SOAP 1.1 Message from the sample incoming XML
        MessageFactory mf = (MessageFactory)
            FactoryRegistry.getFactory(MessageFactory.class);
        Message m = mf.createFrom(omElement);
        
        // Get the BlockFactory
        JAXBBlockFactory bf = (JAXBBlockFactory)
            FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
        // Create the JAXBContext instance that will be used
        // to deserialize the JAX-B object content in the message.
        JAXBContext jbc = JAXBContext.newInstance("test");
        
        // Get the JAXBBlock that wraps the content
        Block b = m.getBodyBlock(0, jbc, bf);
     
        // Get the business object from the block, which should be a 
        // JAX-B object
        Object bo = b.getBusinessObject(true);
        
        // Check to make sure the right object was returned
        assertNotNull(bo);
        assertTrue(bo instanceof EchoStringResponse);
        
        // Check to make sure the content of that object is correct
        EchoStringResponse esr = (EchoStringResponse) bo;
        assertNotNull(esr.getEchoStringReturn());
        assertTrue(esr.getEchoStringReturn().equals("sample return value"));
    }
	SAAJConverter converter = null;
	private SAAJConverter getSAAJConverter() {
		if (converter == null) {
			SAAJConverterFactory factory = (
						SAAJConverterFactory)FactoryRegistry.getFactory(SAAJConverterFactory.class);
			converter = factory.getSAAJConverter();
		}
		return converter;
	}
}
