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

package org.apache.axis2.jaxws.dispatch;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPModelBuilder;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * This class uses the JAX-WS Dispatch API to test sending and receiving
 * messages using SOAP 1.2.
 */
public class OMElementDispatchTest {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo");
    
    private static final QName QNAME_SERVICE = new QName(
            "http://org/apache/axis2/jaxws/test/OMELEMENT", "OMElementService");
    private static final QName QNAME_PORT = new QName(
            "http://org/apache/axis2/jaxws/test/OMELEMENT", "OMElementPort");
    
    private static final String sampleRequest = 
        "<test:echoOMElement xmlns:test=\"http://org/apache/axis2/jaxws/test/OMELEMENT\">" +
        "<test:input>SAMPLE REQUEST MESSAGE</test:input>" +
        "</test:echoOMElement>";
    private static final String sampleEnvelopeHead = 
        "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">" +
        "<soapenv:Header /><soapenv:Body>";
    private static final String sampleEnvelopeTail = 
        "</soapenv:Body></soapenv:Envelope>";
    private static final String sampleEnvelope = 
        sampleEnvelopeHead + 
        sampleRequest + 
        sampleEnvelopeTail;

    private static String getEndpoint() throws Exception {
        return server.getEndpoint("OMElementProviderService.OMElementProviderPort");
    }

    /**
     * Test sending a SOAP 1.2 request in PAYLOAD mode
     */
    @Test
    public void testSourceDispatchPayloadMode() throws Exception {
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_BINDING, getEndpoint());
        Dispatch<Source> dispatch = service.createDispatch(
                QNAME_PORT, Source.class, Mode.PAYLOAD);
        
        // Create the Source object with the payload contents.  Since
        // we're in PAYLOAD mode, we don't have to worry about the envelope.
        byte[] bytes = sampleRequest.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        StreamSource request = new StreamSource(bais);
        
        // Send the SOAP 1.2 request
        Source response = dispatch.invoke(request);

        assertTrue("The response was null.  We expected content to be returned.", response != null);
        
        // Convert the response to a more consumable format
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
        
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer trans = factory.newTransformer();
        trans.transform(response, result);
        
        // Check to make sure the contents are correct.  Again, since we're
        // in PAYLOAD mode, we shouldn't have anything related to the envelope
        // in the return, just the contents of the Body.
        String responseText = baos.toString();
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));   
        
        // Invoke a second time to verify
        bais = new ByteArrayInputStream(bytes);
        request = new StreamSource(bais);
        
        // Send the SOAP 1.2 request
        response = dispatch.invoke(request);

        assertTrue("The response was null.  We expected content to be returned.", response != null);
        
        // Convert the response to a more consumable format
        baos = new ByteArrayOutputStream();
        result = new StreamResult(baos);
        
        factory = TransformerFactory.newInstance();
        trans = factory.newTransformer();
        trans.transform(response, result);
        
        // Check to make sure the contents are correct.  Again, since we're
        // in PAYLOAD mode, we shouldn't have anything related to the envelope
        // in the return, just the contents of the Body.
        responseText = baos.toString();
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));    
    }
    


    /**
     * Test sending a SOAP 1.2 request in MESSAGE mode
     */
    @Test
    public void testSourceDispatchMessageMode() throws Exception {
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_BINDING, getEndpoint());
        Dispatch<Source> dispatch = service.createDispatch(
                QNAME_PORT, Source.class, Mode.MESSAGE);
        
        // Create the Source object with the message contents.  Since
        // we're in MESSAGE mode, we'll need to make sure we create this
        // with the right protocol.
        byte[] bytes = sampleEnvelope.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        StreamSource request = new StreamSource(bais);
        
        Source response = dispatch.invoke(request);
        
        // Convert the response to a more consumable format
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
        
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer trans = factory.newTransformer();
        trans.transform(response, result);
        
        // Check to make sure the contents of the message are correct
        String responseText = baos.toString();
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));
        
        // Check to make sure the message returned had the right protocol version
        // TODO: Need to determine whether or not we should be using the hard 
        // coded URLs here, or whether we should be using a constant for the 
        // purposes of the test.
        assertTrue(responseText.contains("http://www.w3.org/2003/05/soap-envelope"));
        assertTrue(!responseText.contains("http://schemas.xmlsoap.org/soap/envelope"));
        
        // Invoke a second time to verify
        bais = new ByteArrayInputStream(bytes);
        request = new StreamSource(bais);
        
        response = dispatch.invoke(request);
        
        // Convert the response to a more consumable format
        baos = new ByteArrayOutputStream();
        result = new StreamResult(baos);
        
        factory = TransformerFactory.newInstance();
        trans = factory.newTransformer();
        trans.transform(response, result);
        
        // Check to make sure the contents of the message are correct
        responseText = baos.toString();
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));
        
        // Check to make sure the message returned had the right protocol version
        // TODO: Need to determine whether or not we should be using the hard 
        // coded URLs here, or whether we should be using a constant for the 
        // purposes of the test.
        assertTrue(responseText.contains("http://www.w3.org/2003/05/soap-envelope"));
        assertTrue(!responseText.contains("http://schemas.xmlsoap.org/soap/envelope"));
    }
    
    /**
     * Test sending a SOAP 1.2 request in PAYLOAD mode
     */
    @Test
    public void testOMElementDispatchPayloadMode() throws Exception {
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_BINDING, getEndpoint());
        Dispatch<OMElement> dispatch = service.createDispatch(
                QNAME_PORT, OMElement.class, Mode.PAYLOAD);
        
        // Create the OMElement object with the payload contents.  Since
        // we're in PAYLOAD mode, we don't have to worry about the envelope.
        StringReader sr = new StringReader(sampleRequest);
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(sr);  
        OMElement om = builder.getDocumentElement();
        
        // Send the SOAP 1.2 request
        OMElement response = dispatch.invoke(om);

        assertTrue("The response was null.  We expected content to be returned.", response != null);
        
        // Check to make sure the contents are correct.  Again, since we're
        // in PAYLOAD mode, we shouldn't have anything related to the envelope
        // in the return, just the contents of the Body.
        String responseText = response.toStringWithConsume();
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));   
        
        // Send the SOAP 1.2 request
        response = dispatch.invoke(om);

        assertTrue("The response was null.  We expected content to be returned.", response != null);
        
        // Check to make sure the contents are correct.  Again, since we're
        // in PAYLOAD mode, we shouldn't have anything related to the envelope
        // in the return, just the contents of the Body.
        responseText = response.toStringWithConsume();
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));    
    }
    


    /**
     * Test sending a SOAP 1.2 request in MESSAGE mode
     */
    @Test
    public void testOMElementDispatchMessageMode() throws Exception {
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_BINDING, getEndpoint());
        Dispatch<OMElement> dispatch = service.createDispatch(
                QNAME_PORT, OMElement.class, Mode.MESSAGE);
        
        // Create the OMElement object with the payload contents.  Since
        // we're in PAYLOAD mode, we don't have to worry about the envelope.
        StringReader sr = new StringReader(sampleEnvelope);
        SOAPModelBuilder builder = OMXMLBuilderFactory.createSOAPModelBuilder(sr); 
        SOAPEnvelope soap12Envelope = (SOAPEnvelope) builder.getDocumentElement();
        
        
        OMElement response = dispatch.invoke(soap12Envelope);
        
        // Check to make sure the contents of the message are correct
        //String responseText = baos.toString();
        String responseText = response.toStringWithConsume();
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));
        
        // Check to make sure the message returned had the right protocol version
        // TODO: Need to determine whether or not we should be using the hard 
        // coded URLs here, or whether we should be using a constant for the 
        // purposes of the test.
        assertTrue(responseText.contains("http://www.w3.org/2003/05/soap-envelope"));
        assertTrue(!responseText.contains("http://schemas.xmlsoap.org/soap/envelope"));
        
        StringReader sr2 = new StringReader(sampleEnvelope);
        builder = OMXMLBuilderFactory.createSOAPModelBuilder(sr2);  
        SOAPEnvelope om = (SOAPEnvelope)builder.getDocumentElement();
        response = dispatch.invoke(om);
        
        // Check to make sure the contents of the message are correct
        responseText = response.toStringWithConsume();
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("SAMPLE RESPONSE MESSAGE"));
        
        // Check to make sure the message returned had the right protocol version
        // TODO: Need to determine whether or not we should be using the hard 
        // coded URLs here, or whether we should be using a constant for the 
        // purposes of the test.
        assertTrue(responseText.contains("http://www.w3.org/2003/05/soap-envelope"));
        assertTrue(!responseText.contains("http://schemas.xmlsoap.org/soap/envelope"));
    }
}
