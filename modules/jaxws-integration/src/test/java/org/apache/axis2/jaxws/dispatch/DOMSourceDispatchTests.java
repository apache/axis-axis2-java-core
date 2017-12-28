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
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
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
 * This class tests the JAX-WS Dispatch with various forms of the 
 * javax.xml.transform.dom.DOMSource 
 */
public class DOMSourceDispatchTests {
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
        
        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleBodyContent);

        TestLogger.logger.debug(">> Invoking sync Dispatch");
		Source response = dispatch.invoke(request);
		assertNotNull("dispatch invoke returned null",response);
		
        // Turn the Source into a String so we can check it
        String responseText = createStringFromSource(response);
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
        
        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleSoapMessage);

        TestLogger.logger.debug(">> Invoking sync Dispatch");
        Source response = dispatch.invoke(request);
        assertNotNull("dispatch invoke returned null",response);
        
        // Turn the Source into a String so we can check it
        String responseText = createStringFromSource(response);
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        // Invoke a second time
        response = dispatch.invoke(request);
        assertNotNull("dispatch invoke returned null",response);
        
        // Turn the Source into a String so we can check it
        responseText = createStringFromSource(response);
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
        
        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleBodyContent);

        // Setup the callback for async responses
        AsyncCallback<Source> callbackHandler = new AsyncCallback<Source>();

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        Future<?> monitor = dispatch.invokeAsync(request, callbackHandler);
            
        await(monitor);
        
        Source response = callbackHandler.getValue();
        assertNotNull(response);
        
        // Turn the Source into a String so we can check it
        String responseText = createStringFromSource(response);
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        // Invoke a second time
        // Setup the callback for async responses
        callbackHandler = new AsyncCallback<Source>();

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        monitor = dispatch.invokeAsync(request, callbackHandler);
            
        await(monitor);
        
        response = callbackHandler.getValue();
        assertNotNull(response);
        
        // Turn the Source into a String so we can check it
        responseText = createStringFromSource(response);
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
        
        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleSoapMessage);

        // Setup the callback for async responses
        AsyncCallback<Source> callbackHandler = new AsyncCallback<Source>();

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        Future<?> monitor = dispatch.invokeAsync(request, callbackHandler);
	        
        await(monitor);
        
        Source response = callbackHandler.getValue();
        assertNotNull(response);
        
        // Turn the Source into a String so we can check it
        String responseText = createStringFromSource(response);
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        
        
        // Invoke a second time
        // Setup the callback for async responses
        callbackHandler = new AsyncCallback<Source>();

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        monitor = dispatch.invokeAsync(request, callbackHandler);
                
        await(monitor);
        
        response = callbackHandler.getValue();
        assertNotNull(response);
        
        // Turn the Source into a String so we can check it
        responseText = createStringFromSource(response);
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
        
        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleBodyContent);

        TestLogger.logger.debug(">> Invoking async (polling) Dispatch");
        Response<Source> asyncResponse = dispatch.invokeAsync(request);
            
        await(asyncResponse);
        
        Source response = asyncResponse.get();
        assertNotNull(response);
        
        // Turn the Source into a String so we can check it
        String responseText = createStringFromSource(response);
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        // Invoke a second time
        TestLogger.logger.debug(">> Invoking async (polling) Dispatch");
        asyncResponse = dispatch.invokeAsync(request);
            
        await(asyncResponse);
        
        response = asyncResponse.get();
        assertNotNull(response);
        
        // Turn the Source into a String so we can check it
        responseText = createStringFromSource(response);
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
        
        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleSoapMessage);

        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        Response<Source> asyncResponse = dispatch.invokeAsync(request);
            
        await(asyncResponse);
        
        Source response = asyncResponse.get();
        assertNotNull(response);
        
        // Turn the Source into a String so we can check it
        String responseText = createStringFromSource(response);
        TestLogger.logger.debug(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
        
        
        // Invoke a second time
        TestLogger.logger.debug(">> Invoking async (callback) Dispatch");
        asyncResponse = dispatch.invokeAsync(request);
            
        await(asyncResponse);
        
        response = asyncResponse.get();
        assertNotNull(response);
        
        // Turn the Source into a String so we can check it
        responseText = createStringFromSource(response);
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

        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleBodyContent);

        TestLogger.logger.debug(">> Invoking One Way Dispatch");
        dispatch.invokeOneWay(request);
        
        // Invoke a second time
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

        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleSoapMessage);

        TestLogger.logger.debug(">> Invoking One Way Dispatch");
        dispatch.invokeOneWay(request);
        
        // Invoke a second time
        TestLogger.logger.debug(">> Invoking One Way Dispatch");
        dispatch.invokeOneWay(request);
	}
    
    @Test
    public void testBadDOMSource() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, server.getEndpoint("EchoService"));
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.PAYLOAD);

        // Create the DOMSource
        DOMSource request = new DOMSource();

        try {
            dispatch.invokeOneWay(request);
            fail("WebServiceException was expected");
        } catch (WebServiceException e) {
            TestLogger.logger.debug("A Web Service Exception was expected: " + e.toString());
            assertTrue(e.getMessage() != null);
        } catch (Exception e) {
            fail("WebServiceException was expected, but received " + e);
        }
        
        // Invoke a second time
        try {
            dispatch.invokeOneWay(request);
            fail("WebServiceException was expected");
        } catch (WebServiceException e) {
            TestLogger.logger.debug("A Web Service Exception was expected: " + e.toString());
            assertTrue(e.getMessage() != null);
        } catch (Exception e) {
            fail("WebServiceException was expected, but received " + e);
        }
        
    }
	/**
     * Create a DOMSource with the provided String as the content
     * @param input
     * @return
	 */
    private DOMSource createDOMSourceFromString(String input) throws Exception {
        byte[] bytes = input.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
        Document domTree = domBuilder.parse(stream);
        Node node = domTree.getDocumentElement();
        
        DOMSource domSource = new DOMSource(node);
        return domSource;
    }
    
    /**
     * Create a String from the provided Source
     * @param input
     * @return
     */
    private String createStringFromSource(Source input) throws Exception {
        StringWriter sw = new StringWriter();
        OMXMLBuilderFactory.createOMBuilder(input).getDocument().serializeAndConsume(sw);
        return sw.toString();
    }
}
