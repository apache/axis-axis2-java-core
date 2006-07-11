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

import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.message.util.Reader2Writer;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

/**
 * MessageTests
 * Tests to create and validate Message processing
 * These are not client/server tests.
 */
public class MessageTests extends TestCase {

	// String test variables
	private static final String sampleText =
		"<pre:a xmlns:pre=\"urn://sample\">" +
		"<b>Hello</b>" +
		"<c>World</c>" +
		"</pre:a>";
	
	private static final String sampleEnvelope = 
		"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
		"<soapenv:Header /><soapenv:Body>" +
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

	public void testStringInflow() throws Exception {
		
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
	public void testStringInflow2() throws Exception {
		
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
	
}
