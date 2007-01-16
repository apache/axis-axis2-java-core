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
import java.io.InputStream;
import java.util.concurrent.Future;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.message.util.Reader2Writer;

/**
 * This class tests the JAX-WS Dispatch<Source> functionality with various
 * forms of a StreamSource object. 
 *
 */
public class StreamSourceDispatch extends TestCase {

    private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    
	/**
     * Invoke a Dispatch<Source> synchronously with the content in PAYLOAD mode.
	 */
    public void testSyncPayloadMode() throws Exception {
		System.out.println("---------------------------------------");
		System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
		svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
		Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, Source.class, 
                Service.Mode.PAYLOAD);
		
        // Create a StreamSource with the desired content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        Source srcStream = new StreamSource((InputStream) stream);
        
        // Invoke the Dispatch<Source>
		System.out.println(">> Invoking sync Dispatch with PAYLOAD mode");
		Source response = dispatch.invoke(srcStream);
		assertNotNull(response);
        
        // Prepare the response content for checking
        XMLStreamReader reader = inputFactory.createXMLStreamReader(response);
        Reader2Writer r2w = new Reader2Writer(reader);
        String responseText = r2w.getAsString();
        System.out.println(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));            
	}

    /**
     * Invoke a Dispatch<Source> synchronously with the content in MESSAGE mode.
     */
    public void testSyncMessageMode() throws Exception {
		System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
		Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
		svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
		Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, Source.class,
				Mode.MESSAGE);
		
		// Create a StreamSource with the desired content
        byte[] bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        Source srcStream = new StreamSource((InputStream) stream);
        
        System.out.println(">> Invoking sync Dispatch with MESSAGE Mode");
		StreamSource response = (StreamSource) dispatch.invoke(srcStream);
        assertNotNull(response);

        // Prepare the response content for checking
        XMLStreamReader reader = inputFactory.createXMLStreamReader(response);
        Reader2Writer r2w = new Reader2Writer(reader);
        String responseText = r2w.getAsString();
        System.out.println(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));            
	}

    /**
     * Invoke a Dispatch<Source> asynchronously with the content in PAYLOAD mode.
     */
    public void testAsyncCallbackPayloadMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, Source.class,
                Service.Mode.PAYLOAD);
        
        // We'll need a callback instance to handle the async responses
        AsyncCallback<Source> callbackHandler = new AsyncCallback<Source>();
        
        // Create a StreamSource with the desired content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        Source srcStream = new StreamSource((InputStream) stream);

        System.out.println(">> Invoking async (callback) Dispatch with PAYLOAD mode");
        Future<?> monitor = dispatch.invokeAsync(srcStream, callbackHandler);

        // Wait for the async response to be returned
        while (!monitor.isDone()) {
            System.out.println(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = callbackHandler.getValue();
        assertNotNull(response);
        
        // Prepare the response content for checking
        XMLStreamReader reader = inputFactory.createXMLStreamReader(response);
        Reader2Writer r2w = new Reader2Writer(reader);
        String responseText = r2w.getAsString();
        System.out.println(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
    }
    
    /**
     * Invoke a Dispatch<Source> asynchronously with the content in MESSAGE mode.
     */
	public void testAsyncCallbackMessageMode() throws Exception {
		System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts 
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
		svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, Source.class,
                Mode.MESSAGE);
        
        // We'll need a callback instance to handle the async responses
        AsyncCallback<Source> callbackHandler = new AsyncCallback<Source>();

        // Create a StreamSource with the desired content
        byte[] bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		Source srcStream = new StreamSource((InputStream) stream);
		
        System.out.println(">> Invoking async (callback) Dispatch with MESSAGE mode");
        Future<?> monitor = dispatch.invokeAsync(srcStream, callbackHandler);

        // Wait for the async response to be returned
        while (!monitor.isDone()) {
            System.out.println(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = callbackHandler.getValue();
        assertNotNull(response);

        // Prepare the response content for checking
        XMLStreamReader reader = inputFactory.createXMLStreamReader(response);
        Reader2Writer r2w = new Reader2Writer(reader);
        String responseText = r2w.getAsString();
        System.out.println(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
    }

    /**
     * Invoke a Dispatch<Source> asynchronously with the content in PAYLOAD mode.
     */
    public void testAsyncPollingPayloadMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, Source.class,
                Service.Mode.PAYLOAD);
        // Create a StreamSource with the desired content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        Source srcStream = new StreamSource((InputStream) stream);

        System.out.println(">> Invoking async (callback) Dispatch with PAYLOAD mode");
        Response<Source> asyncResponse = dispatch.invokeAsync(srcStream);

        // Wait for the async response to be returned
        while (!asyncResponse.isDone()) {
            System.out.println(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = asyncResponse.get();
        assertNotNull(response);
        
        // Prepare the response content for checking
        XMLStreamReader reader = inputFactory.createXMLStreamReader(response);
        Reader2Writer r2w = new Reader2Writer(reader);
        String responseText = r2w.getAsString();
        System.out.println(responseText);
        
        // Check to make sure the content is correct
        assertTrue(!responseText.contains("soap"));
        assertTrue(!responseText.contains("Envelope"));
        assertTrue(!responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
    }
    
    /**
     * Invoke a Dispatch<Source> asynchronously with the content in MESSAGE mode.
     */
    public void testAsyncPollingMessageMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts 
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, Source.class,
                Mode.MESSAGE);

        // Create a StreamSource with the desired content
        byte[] bytes = DispatchTestConstants.sampleSoapMessage.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        Source srcStream = new StreamSource((InputStream) stream);
        
        System.out.println(">> Invoking async (callback) Dispatch with MESSAGE mode");
        Response<Source> asyncResponse = dispatch.invokeAsync(srcStream);

        // Wait for the async response to be returned
        while (!asyncResponse.isDone()) {
            System.out.println(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
        
        Source response = asyncResponse.get();
        assertNotNull(response);

        // Prepare the response content for checking
        XMLStreamReader reader = inputFactory.createXMLStreamReader(response);
        Reader2Writer r2w = new Reader2Writer(reader);
        String responseText = r2w.getAsString();
        System.out.println(responseText);
        
        // Check to make sure the content is correct
        assertTrue(responseText.contains("soap"));
        assertTrue(responseText.contains("Envelope"));
        assertTrue(responseText.contains("Body"));
        assertTrue(responseText.contains("echoStringResponse"));
    }
    
    /**
     * Invoke a Dispatch<Source> one-way operation
     */
	public void testOneWayPayloadMode() throws Exception {
		System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
		Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
		svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
		Dispatch<Source> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, Source.class,
				Service.Mode.PAYLOAD);
        
		// Create a StreamSource with the desired content
        byte[] bytes = DispatchTestConstants.sampleBodyContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		Source srcStream = new StreamSource((InputStream) stream);
		
		System.out.println(">> Invoking One Way Dispatch");
		dispatch.invokeOneWay(srcStream);
	}
}
