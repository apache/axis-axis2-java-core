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

import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;
import jakarta.xml.ws.soap.SOAPBinding;
import jakarta.xml.ws.soap.SOAPFaultException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * This class uses the JAX-WS Dispatch API to test sending and receiving
 * messages using SOAP 1.2.
 */
public class SOAP12DispatchTest {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo");
    
    private static final QName QNAME_SERVICE = new QName(
            "http://org/apache/axis2/jaxws/test/SOAP12", "SOAP12Service");
    private static final QName QNAME_PORT = new QName(
            "http://org/apache/axis2/jaxws/test/SOAP12", "SOAP12Port");
    
    private static final String sampleRequest = 
        "<test:echoString xmlns:test=\"http://org/apache/axis2/jaxws/test/SOAP12\">" +
        "<test:input>SAMPLE REQUEST MESSAGE</test:input>" +
        "</test:echoString>";
    private static final String sampleEnvelopeHead = 
        "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">" +
        "<soapenv:Header /><soapenv:Body>";
    private static final String sampleEnvelopeHead_MustUnderstand = 
        "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">" +
        "<soapenv:Header>" +
        "<soapenv:codeHeaderSOAP12 soapenv:mustUnderstand=\"true\">" +
        "<code>default</code>" +
        "</soapenv:codeHeaderSOAP12>" +
        "</soapenv:Header>" +
        "<soapenv:Body>";
    private static final String sampleEnvelopeTail = 
        "</soapenv:Body></soapenv:Envelope>";
    private static final String sampleEnvelope = 
        sampleEnvelopeHead + 
        sampleRequest + 
        sampleEnvelopeTail;
    
    private static final String sampleEnvelope_MustUnderstand = 
        sampleEnvelopeHead_MustUnderstand + 
        sampleRequest + 
        sampleEnvelopeTail;
    
    private static String getEndpoint() throws Exception {
        return server.getEndpoint("SOAP12ProviderService.SOAP12ProviderPort");
    }

    /**
     * Test sending a SOAP 1.2 request in PAYLOAD mode
     */
    @Test
    public void testSOAP12DispatchPayloadMode() throws Exception {
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
        assertTrue(responseText.contains("echoStringResponse"));   
        
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
        assertTrue(responseText.contains("echoStringResponse"));    
    }
    
    /**
     * Test sending a SOAP 1.2 request in PAYLOAD mode using SOAP/JMS
     */
    /*
     * This test was shown to be invalid by the changes made under Jira AXIS2-4855.  Basically, this test was passing 
     * based on a bug in modules/jaxws/src/org/apache/axis2/jaxws/message/Protocol.java creating the protocol table.  
     * There is only one JMS namespace defined by the JMS spec, and it is used for both SOAP11 and SOAP12.  Therefore, 
     * when the SOAP12 protocol was registered after the SOAP11 protocol, it overwrote the previous value (since the namespace
     * is used as the key).  
     * 
     * For a WSDL-based client or service, the SOAP version is determined by the SOAP namespace used on the
     * binding.  For a WSDL-less client or service, there is no JMS spec-defined way to determine the difference.  For now
     * JAX-WS will default to SOAP11, and SOAP12 is not registered as a protocol for the JMS namespace.  See AXIS2-4855
     * for more information.
     */
    @Ignore
    @Test
    public void testSOAP12JMSDispatchPayloadMode() throws Exception {
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
		service.addPort(QNAME_PORT, MDQConstants.SOAP12JMS_BINDING, getEndpoint());
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
        assertTrue(responseText.contains("echoStringResponse"));
        
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
        assertTrue(responseText.contains("echoStringResponse"));

    }
    

    /**
     * Test sending a SOAP 1.2 request in MESSAGE mode
     */
    @Test
    public void testSOAP12DispatchMessageMode() throws Exception {
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
        assertTrue(responseText.contains("echoStringResponse"));
        
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
        assertTrue(responseText.contains("echoStringResponse"));
        
        // Check to make sure the message returned had the right protocol version
        // TODO: Need to determine whether or not we should be using the hard 
        // coded URLs here, or whether we should be using a constant for the 
        // purposes of the test.
        assertTrue(responseText.contains("http://www.w3.org/2003/05/soap-envelope"));
        assertTrue(!responseText.contains("http://schemas.xmlsoap.org/soap/envelope"));
    }
    
    /**
     * Test sending a SOAP 1.2 request in MESSAGE mode
     */
    @Test
    public void testSOAP12DispatchMessageMode_MustUnderstand() throws Exception {
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_BINDING, getEndpoint());
        Dispatch<Source> dispatch = service.createDispatch(
                QNAME_PORT, Source.class, Mode.MESSAGE);
        
        // Create the Source object with the message contents.  Since
        // we're in MESSAGE mode, we'll need to make sure we create this
        // with the right protocol.
        byte[] bytes = sampleEnvelope_MustUnderstand.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        StreamSource request = new StreamSource(bais);
        
        SOAPFaultException e = null;
        try {
            Source response = dispatch.invoke(request);
        } catch (SOAPFaultException ex) {
            e = ex;
        }
        
        assertNotNull("We should have an exception, but none was thrown.", e);
        assertEquals("FaultCode should be \"MustUnderstand\"", "MustUnderstand", e.getFault().getFaultCodeAsQName().getLocalPart());
        
        // Invoke a second time to verify
        bais = new ByteArrayInputStream(bytes);
        request = new StreamSource(bais);
        
        e = null;
        try {
            Source response = dispatch.invoke(request);
        } catch (SOAPFaultException ex) {
            e = ex;
        }
        
        assertNotNull("We should have an exception, but none was thrown.", e);
        assertEquals("FaultCode should be \"MustUnderstand\"", "MustUnderstand", e.getFault().getFaultCodeAsQName().getLocalPart());
        
        
    }
}
