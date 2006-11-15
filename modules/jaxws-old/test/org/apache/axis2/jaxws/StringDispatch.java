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
package org.apache.axis2.jaxws;

import java.util.concurrent.Future;

import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import junit.framework.TestCase;

public class StringDispatch extends TestCase {

    /**
     * Invoke a sync Dispatch<String> in PAYLOAD mode
     */
    public void testSyncPayloadMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.PAYLOAD);
        
        // Invoke the Dispatch
        System.out.println(">> Invoking sync Dispatch");
        String response = dispatch.invoke(DispatchTestConstants.sampleBodyContent);

        assertNotNull("dispatch invoke returned null", response);
        System.out.println(response);
        
        // Check to make sure the content is correct
        assertTrue(!response.contains("soap"));
        assertTrue(!response.contains("Envelope"));
        assertTrue(!response.contains("Body"));
        assertTrue(response.contains("echoStringResponse"));
	}
    
    /**
     * Invoke a sync Dispatch<String> in MESSAGE mode
     */
    public void testSyncWithMessageMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.MESSAGE);
        
        // Invoke the Dispatch
        System.out.println(">> Invoking sync Dispatch");
        String response = dispatch.invoke(DispatchTestConstants.sampleSoapMessage);

        assertNotNull("dispatch invoke returned null", response);
        System.out.println(response);
        
        // Check to make sure the content is correct
        assertTrue(response.contains("soap"));
        assertTrue(response.contains("Envelope"));
        assertTrue(response.contains("Body"));
        assertTrue(response.contains("echoStringResponse"));
	}
    
	/**
     * Invoke a Dispatch<String> using the async callback API in PAYLOAD mode
	 */
    public void testAsyncCallbackPayloadMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.PAYLOAD);

        // Create the callback for async responses
        CallbackHandler<String> callbackHandler = new CallbackHandler<String>();
        
        System.out.println(">> Invoking async (callback) Dispatch");
        Future<?> monitor = dispatch.invokeAsync(DispatchTestConstants.sampleBodyContent, callbackHandler);
	        
        while (!monitor.isDone()) {
            System.out.println(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
	}
    
    /**
     * Invoke a Dispatch<String> using the async callback API in MESSAGE mode
     */
    public void testAsyncCallbackMessageMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.MESSAGE);

        // Create the callback for async responses
        CallbackHandler<String> callbackHandler = new CallbackHandler<String>();
        
        System.out.println(">> Invoking async (callback) Dispatch with Message Mode");
        Future<?> monitor = dispatch.invokeAsync(DispatchTestConstants.sampleSoapMessage, callbackHandler);
    
        while (!monitor.isDone()) {
            System.out.println(">> Async invocation still not complete");
            Thread.sleep(1000);
        }
	}
	
    /**
     * Invoke a Dispatch<String> one-way in PAYLOAD mode 
     */
    public void testOneWayPayloadMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.PAYLOAD);

        System.out.println(">> Invoking one-way Dispatch");
        dispatch.invokeOneWay(DispatchTestConstants.sampleBodyContent);
    }
    
    /**
     * Invoke a Dispatch<String> one-way in MESSAGE mode 
	 */
    public void testOneWayMessageMode() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Initialize the JAX-WS client artifacts
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        Dispatch<String> dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                String.class, Service.Mode.MESSAGE);

        System.out.println(">> Invoking one-way Dispatch");
        dispatch.invokeOneWay(DispatchTestConstants.sampleSoapMessage);
	}
}
