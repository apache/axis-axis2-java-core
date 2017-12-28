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

import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import static org.apache.axis2.jaxws.framework.TestUtils.await;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.concurrent.Future;

/**
 * This class tests the JAX-WS Dispatch<Source> with content in various 
 * forms of a javax.xml.transform.sax.SAXSource.
 */
public class SAXSourceDispatchTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    @Test
	public void testSyncPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, server.getEndpoint("EchoService"));
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                                                       Source.class, Service.Mode.PAYLOAD);

        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking sync Dispatch");
        Source response = dispatch.invoke(request);

        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        StringWriter sw = new StringWriter();
        OMXMLBuilderFactory.createOMBuilder(response).getDocument().serializeAndConsume(sw);
        String responseText = sw.toString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        // Invoke a second time
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking sync Dispatch");
        response = dispatch.invoke(request);

        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        sw = new StringWriter();
        OMXMLBuilderFactory.createOMBuilder(response).getDocument().serializeAndConsume(sw);
        responseText = sw.toString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
     }

    @Test
	public void testSyncMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, server.getEndpoint("EchoService"));
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.MESSAGE);
		
        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking sync Dispatch with Message Mode");
		Source response = dispatch.invoke(request);

        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        StringWriter sw = new StringWriter();
        OMXMLBuilderFactory.createOMBuilder(response).getDocument().serializeAndConsume(sw);
        String responseText = sw.toString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        
        // Invoke a second time
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking sync Dispatch with Message Mode");
        response = dispatch.invoke(request);

        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        sw = new StringWriter();
        OMXMLBuilderFactory.createOMBuilder(response).getDocument().serializeAndConsume(sw);
        responseText = sw.toString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
	}

    @Test
    public void testAsyncCallbackPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, server.getEndpoint("EchoService"));
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.PAYLOAD);
        
        // Setup the callback for async responses
        AsyncCallback<Source> callbackHandler = new AsyncCallback<Source>();
        
        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        Future<?> monitor = dispatch.invokeAsync(request, callbackHandler);

        await(monitor);
        
        Source response = callbackHandler.getValue();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        StringWriter sw = new StringWriter();
        OMXMLBuilderFactory.createOMBuilder(response).getDocument().serializeAndConsume(sw);
        String responseText = sw.toString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        
        
        // Invoke a second time
        callbackHandler = new AsyncCallback<Source>();
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        monitor = dispatch.invokeAsync(request, callbackHandler);

        await(monitor);
        
        response = callbackHandler.getValue();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        sw = new StringWriter();
        OMXMLBuilderFactory.createOMBuilder(response).getDocument().serializeAndConsume(sw);
        responseText = sw.toString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
    }
    
    @Test
    public void testAsyncCallbackMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, server.getEndpoint("EchoService"));
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.MESSAGE);
		
        // Setup the callback for async responses
        AsyncCallback<Source> callbackHandler = new AsyncCallback<Source>();
        
        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        Future<?> monitor = dispatch.invokeAsync(request, callbackHandler);

        await(monitor);
        
        Source response = callbackHandler.getValue();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        StringWriter sw = new StringWriter();
        OMXMLBuilderFactory.createOMBuilder(response).getDocument().serializeAndConsume(sw);
        String responseText = sw.toString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        
        // Invoke a second time to verify
        callbackHandler = new AsyncCallback<Source>();
        
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        monitor = dispatch.invokeAsync(request, callbackHandler);

        await(monitor);
        
        response = callbackHandler.getValue();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        sw = new StringWriter();
        OMXMLBuilderFactory.createOMBuilder(response).getDocument().serializeAndConsume(sw);
        responseText = sw.toString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
	}
    
    @Test
    public void testAsyncPollingPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, server.getEndpoint("EchoService"));
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.PAYLOAD);
        
        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (polling) Dispatch");
        Response<Source> asyncResponse = dispatch.invokeAsync(request);

        await(asyncResponse);
        
        Source response = asyncResponse.get();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        StringWriter sw = new StringWriter();
        OMXMLBuilderFactory.createOMBuilder(response).getDocument().serializeAndConsume(sw);
        String responseText = sw.toString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        
        // Invoke a second time to verify
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (polling) Dispatch");
        asyncResponse = dispatch.invokeAsync(request);

        await(asyncResponse);
        
        response = asyncResponse.get();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        sw = new StringWriter();
        OMXMLBuilderFactory.createOMBuilder(response).getDocument().serializeAndConsume(sw);
        responseText = sw.toString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
    }
    
    @Test
    public void testAsyncPollingMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, server.getEndpoint("EchoService"));
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.MESSAGE);
        
        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        Response<Source> asyncResponse = dispatch.invokeAsync(request);

        await(asyncResponse);
        
        Source response = asyncResponse.get();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        StringWriter sw = new StringWriter();
        OMXMLBuilderFactory.createOMBuilder(response).getDocument().serializeAndConsume(sw);
        String responseText = sw.toString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        
        // Invoke a second time to verify
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        asyncResponse = dispatch.invokeAsync(request);

        await(asyncResponse);
        
        response = asyncResponse.get();
        assertNotNull("dispatch invoke returned null", response);
        
        // Prepare the response content for checking
        sw = new StringWriter();
        OMXMLBuilderFactory.createOMBuilder(response).getDocument().serializeAndConsume(sw);
        responseText = sw.toString();
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
    }
    
    @Test
    public void testOneWayPayloadMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, server.getEndpoint("EchoService"));
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.PAYLOAD);
        
        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking One Way Dispatch");
        dispatch.invokeOneWay(request);
        
        
        // Invoke a second time to verify
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking One Way Dispatch");
        dispatch.invokeOneWay(request);
    }
    
    @Test
    public void testOneWayMessageMode() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, server.getEndpoint("EchoService"));
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.MESSAGE);
        
        // Create a SAXSource out of the string content
        byte[] bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        InputSource input = new InputSource(stream);
        Source request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking One Way Dispatch");
	dispatch.invokeOneWay(request);
                
        // Invoke a second time to verify
        stream = new ByteArrayInputStream(bytes);
        input = new InputSource(stream);
        request = new SAXSource(input);

        TestLogger.logger.debug(">> Invoking One Way Dispatch");
        dispatch.invokeOneWay(request);
	}
    
    @Test
    public void testBadSAXSource() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, server.getEndpoint("EchoService"));
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.MESSAGE);
        
        // Create an empty (invalid) SAXSource
        Source request = new SAXSource();
        
        try {
            dispatch.invoke(request);
            fail("WebServiceException was expected");
        } catch (WebServiceException e) {
            TestLogger.logger.debug("A Web Service Exception was expected: " + e.toString());
            assertTrue(e.getMessage() != null);
        } catch (Exception e) {
            fail("WebServiceException was expected, but received:" + e);
        }
        
        // Invoke a second time to verify
        try {
            dispatch.invoke(request);
            fail("WebServiceException was expected");
        } catch (WebServiceException e) {
            TestLogger.logger.debug("A Web Service Exception was expected: " + e.toString());
            assertTrue(e.getMessage() != null);
        } catch (Exception e) {
            fail("WebServiceException was expected, but received:" + e);
        }
        
        try {
            dispatch.invokeOneWay(request);
            fail("WebServiceException was expected");
        } catch (WebServiceException e) {
            TestLogger.logger.debug("A Web Service Exception was expected: " + e.toString());
            assertTrue(e.getMessage() != null);
        } catch (Exception e) {
            fail("WebServiceException was expected, but received:" + e);
        }
        
    }
    
}
