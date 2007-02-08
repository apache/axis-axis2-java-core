/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.dispatch;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Future;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.message.util.Reader2Writer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class tests the JAX-WS Dispatch with various forms of the 
 * javax.xml.transform.dom.DOMSource 
 */
public class DOMSourceDispatch extends TestCase{

    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    
    public void testSyncPayloadMode() throws Exception {
		System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.PAYLOAD);
        
        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleBodyContent);
        
		System.out.println(">> Invoking sync Dispatch");
		Source response = dispatch.invoke(request);
		assertNotNull("dispatch invoke returned null",response);
		
        // Turn the Source into a String so we can check it
        String responseText = createStringFromSource(response);        
        System.out.println(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
	}

	public void testSyncMessageMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.MESSAGE);
        
        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleSoapMessage);
        
        System.out.println(">> Invoking sync Dispatch");
        Source response = dispatch.invoke(request);
        assertNotNull("dispatch invoke returned null",response);
        
        // Turn the Source into a String so we can check it
        String responseText = createStringFromSource(response);        
        System.out.println(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
	}

    public void testAsyncCallbackPayloadMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.PAYLOAD);
        
        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleBodyContent);

        // Setup the callback for async responses
        AsyncCallback<Source> callbackHandler = new AsyncCallback<Source>();
        
        System.out.println(">> Invoking async (callback) Dispatch");
        Future<?> monitor = dispatch.invokeAsync(request, callbackHandler);
            
        while (!monitor.isDone()) {
            System.out.println(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = callbackHandler.getValue();
        assertNotNull(response);
        
        // Turn the Source into a String so we can check it
        String responseText = createStringFromSource(response);        
        System.out.println(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
    }
    
    public void testAsyncCallbackMessageMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.MESSAGE);
        
        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleSoapMessage);

        // Setup the callback for async responses
        AsyncCallback<Source> callbackHandler = new AsyncCallback<Source>();
        
        System.out.println(">> Invoking async (callback) Dispatch");
        Future<?> monitor = dispatch.invokeAsync(request, callbackHandler);
	        
        while (!monitor.isDone()) {
            System.out.println(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = callbackHandler.getValue();
        assertNotNull(response);
        
        // Turn the Source into a String so we can check it
        String responseText = createStringFromSource(response);        
        System.out.println(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
	}
    
    public void testAsyncPollingPayloadMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.PAYLOAD);
        
        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleBodyContent);

        System.out.println(">> Invoking async (polling) Dispatch");
        Response<Source> asyncResponse = dispatch.invokeAsync(request);
            
        while (!asyncResponse.isDone()) {
            System.out.println(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = asyncResponse.get();
        assertNotNull(response);
        
        // Turn the Source into a String so we can check it
        String responseText = createStringFromSource(response);        
        System.out.println(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
    }
    
    public void testAsyncPollingMessageMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.MESSAGE);
        
        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleSoapMessage);

        System.out.println(">> Invoking async (callback) Dispatch");
        Response<Source> asyncResponse = dispatch.invokeAsync(request);
            
        while (!asyncResponse.isDone()) {
            System.out.println(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = asyncResponse.get();
        assertNotNull(response);
        
        // Turn the Source into a String so we can check it
        String responseText = createStringFromSource(response);        
        System.out.println(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
    }
    
    public void testOneWayPayloadMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.PAYLOAD);

        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleBodyContent);

        System.out.println(">> Invoking One Way Dispatch");
        dispatch.invokeOneWay(request);
    }
    
    public void testOneWayMessageMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.MESSAGE);

        // Create the DOMSource
        DOMSource request = createDOMSourceFromString(DispatchTestConstants.sampleSoapMessage);

        System.out.println(">> Invoking One Way Dispatch");
        dispatch.invokeOneWay(request);
	}
    
    public void testBadDOMSource() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                Source.class, Service.Mode.PAYLOAD);

        // Create the DOMSource
        DOMSource request = new DOMSource();

        try {
            dispatch.invokeOneWay(request);
            fail("WebServiceException was expected");
        } catch (WebServiceException e) {
            System.out.println("A Web Service Exception was expected: " + e.toString());
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
        XMLStreamReader reader = inputFactory.createXMLStreamReader(input);
        Reader2Writer r2w = new Reader2Writer(reader);
        String text = r2w.getAsString();
        return text;
    }
}
