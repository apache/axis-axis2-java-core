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

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.xmlsoap.schemas.soap.envelope.Body;
import org.xmlsoap.schemas.soap.envelope.Envelope;
import test.EchoString;
import test.EchoStringResponse;
import test.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import static org.apache.axis2.jaxws.framework.TestUtils.await;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Future;

public class JAXBDispatchTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    private Dispatch<Object> dispatchPayload;
    private Dispatch<Object> dispatchMessage;
    private JAXBContext jbc;
    
    @Before
    public void setUp() throws Exception {
        //Create the Service object
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, server.getEndpoint("EchoService"));
        
        //Create the JAX-B Dispatch object to recognize the test and soap packages
        jbc = JAXBContext.newInstance("test:org.xmlsoap.schemas.soap.envelope");
        
        // Create Payload and Message Dispatch
        dispatchPayload = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                jbc, Service.Mode.PAYLOAD);
        dispatchMessage = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                jbc, Service.Mode.MESSAGE);
    }
    
    @Test
    public void testSyncPayload() throws Exception {
        TestLogger.logger.debug("---------------------------------------");

        // Create the input param
        ObjectFactory factory = new ObjectFactory();
        EchoString request = factory.createEchoString();         
        request.setInput("SYNC JAXB PAYLOAD TEST");
        
        // Invoke the Dispatch<Object>
        TestLogger.logger.debug(">> Invoking sync Dispatch with JAX-B Parameter");
        EchoStringResponse response = (EchoStringResponse) dispatchPayload.invoke(request);
        
        assertNotNull(response);

        TestLogger.logger.debug(">> Response content: " + response.getEchoStringReturn());
        
        assertTrue("[ERROR] - Response object was null", response != null);
        assertTrue("[ERROR] - No content in response object", response.getEchoStringReturn() != null);
        assertTrue("[ERROR] - Zero length content in response", response.getEchoStringReturn().length() > 0);
        
        // Invoke the Dispatch<Object> a second time
        TestLogger.logger.debug(">> Invoking sync Dispatch with JAX-B Parameter");
        response = (EchoStringResponse) dispatchPayload.invoke(request);
        
        assertNotNull(response);

        TestLogger.logger.debug(">> Response content: " + response.getEchoStringReturn());
        
        assertTrue("[ERROR] - Response object was null", response != null);
        assertTrue("[ERROR] - No content in response object", response.getEchoStringReturn() != null);
        assertTrue("[ERROR] - Zero length content in response", response.getEchoStringReturn().length() > 0);
    }
    
    @Test
    public void testAysncPayload() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        // Create the input param
        ObjectFactory factory = new ObjectFactory();
        EchoString request = factory.createEchoString();         
        request.setInput("ASYNC(CALLBACK) JAXB PAYLOAD TEST");
        
        // Create the callback for async responses
        JAXBCallbackHandler<Object> callback = new JAXBCallbackHandler<Object>();
        
        // Invoke the Dispatch<Object> asynchronously
        TestLogger.logger.debug(">> Invoking async(callback) Dispatch with JAX-B Parameter");
        Future<?> monitor = dispatchPayload.invokeAsync(request, callback);
        
        await(monitor);
        
        EchoStringResponse response = (EchoStringResponse) callback.getData();
        assertNotNull(response);

        TestLogger.logger.debug(">> Response content: " + response.getEchoStringReturn());
        
        assertTrue("[ERROR] - Response object was null", response != null);
        assertTrue("[ERROR] - No content in response object", response.getEchoStringReturn() != null);
        assertTrue("[ERROR] - Zero length content in response", response.getEchoStringReturn().length() > 0);

        
        // Invoke a second time
        
        // Create the callback for async responses
        callback = new JAXBCallbackHandler<Object>();
        
        // Invoke the Dispatch<Object> asynchronously
        TestLogger.logger.debug(">> Invoking async(callback) Dispatch with JAX-B Parameter");
        monitor = dispatchPayload.invokeAsync(request, callback);
        
        await(monitor);
        
        response = (EchoStringResponse) callback.getData();
        assertNotNull(response);

        TestLogger.logger.debug(">> Response content: " + response.getEchoStringReturn());
        
        assertTrue("[ERROR] - Response object was null", response != null);
        assertTrue("[ERROR] - No content in response object", response.getEchoStringReturn() != null);
        assertTrue("[ERROR] - Zero length content in response", response.getEchoStringReturn().length() > 0);
    }
    
    @Test
    public void testOneWayPayload() throws Exception {
        TestLogger.logger.debug("---------------------------------------");

        // Create the input param
        ObjectFactory factory = new ObjectFactory();
        EchoString request = factory.createEchoString();         
        request.setInput("ONE-WAY JAXB PAYLOAD TEST");
        
        // Invoke the Dispatch<Object> one-way
        TestLogger.logger.debug(">> Invoking one-way Dispatch with JAX-B Parameter");
        dispatchPayload.invokeOneWay(request);
        
        // Invoke the Dispatch<Object> one-way a second time
        TestLogger.logger.debug(">> Invoking one-way Dispatch with JAX-B Parameter");
        dispatchPayload.invokeOneWay(request);
    }
    
    @Test
    public void testSyncMessage() throws Exception {
        TestLogger.logger.debug("---------------------------------------");

        // Create the input param
        ObjectFactory factory = new ObjectFactory();
        EchoString echoString = factory.createEchoString();         
        echoString.setInput("SYNC JAXB MESSAGETEST");
        
        JAXBElement<Envelope> request = createJAXBEnvelope();
        request.getValue().getBody().getAny().add(echoString);
        
        jbc.createMarshaller().marshal(request,System.out);
        
        // Invoke the Dispatch<Object>
        TestLogger.logger.debug(">> Invoking sync Dispatch with JAX-B Parameter");
        JAXBElement<Envelope> jaxbResponse = (JAXBElement<Envelope>) dispatchMessage.invoke(request);
        
        assertNotNull(jaxbResponse);
        Envelope response = jaxbResponse.getValue();
        assertNotNull(response);
        assertNotNull(response.getBody());
        EchoStringResponse echoStringResponse = (EchoStringResponse) response.getBody().getAny().get(0);

        TestLogger.logger.debug(">> Response content: " + echoStringResponse.getEchoStringReturn());
        assertTrue("[ERROR] - No content in response object", echoStringResponse.getEchoStringReturn() != null);
        assertTrue("[ERROR] - Zero length content in response", echoStringResponse.getEchoStringReturn().length() > 0);
    
        
        
        // Invoke the Dispatch<Object> a second time
        TestLogger.logger.debug(">> Invoking sync Dispatch with JAX-B Parameter");
        jaxbResponse = (JAXBElement<Envelope>) dispatchMessage.invoke(request);
        
        assertNotNull(jaxbResponse);
        response = jaxbResponse.getValue();
        assertNotNull(response);
        assertNotNull(response.getBody());
        echoStringResponse = (EchoStringResponse) response.getBody().getAny().get(0);

        TestLogger.logger.debug(">> Response content: " + echoStringResponse.getEchoStringReturn());
        assertTrue("[ERROR] - No content in response object", echoStringResponse.getEchoStringReturn() != null);
        assertTrue("[ERROR] - Zero length content in response", echoStringResponse.getEchoStringReturn().length() > 0);
    }
    
    @Test
    public void testAysncMessage() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        // Create the input param
        ObjectFactory factory = new ObjectFactory();
        EchoString echoString = factory.createEchoString();         
        echoString.setInput("ASYNC(CALLBACK) JAXB MESSAGE TEST");
        
        JAXBElement<Envelope> request = createJAXBEnvelope();
        request.getValue().getBody().getAny().add(echoString);
        
        
        // Create the callback for async responses
        JAXBCallbackHandler<Object> callback = new JAXBCallbackHandler<Object>();
        
        // Invoke the Dispatch<Object> asynchronously
        TestLogger.logger.debug(">> Invoking async(callback) Dispatch with JAX-B Parameter");
        Future<?> monitor = dispatchMessage.invokeAsync(request, callback);
        
        await(monitor);
        
        JAXBElement<Envelope> jaxbResponse = (JAXBElement<Envelope>) callback.getData();
        
        assertNotNull(jaxbResponse);
        Envelope response = jaxbResponse.getValue();
        
        assertNotNull(response);
        assertNotNull(response.getBody());
        EchoStringResponse echoStringResponse = (EchoStringResponse) response.getBody().getAny().get(0);

        TestLogger.logger.debug(">> Response content: " + echoStringResponse.getEchoStringReturn());
        assertTrue("[ERROR] - No content in response object", echoStringResponse.getEchoStringReturn() != null);
        assertTrue("[ERROR] - Zero length content in response", echoStringResponse.getEchoStringReturn().length() > 0);

        // Invoke a second time
        // Create the callback for async responses
        callback = new JAXBCallbackHandler<Object>();
        
        // Invoke the Dispatch<Object> asynchronously
        TestLogger.logger.debug(">> Invoking async(callback) Dispatch with JAX-B Parameter");
        monitor = dispatchMessage.invokeAsync(request, callback);
        
        await(monitor);
        
        jaxbResponse = (JAXBElement<Envelope>) callback.getData();
        
        assertNotNull(jaxbResponse);
        response = jaxbResponse.getValue();
        
        assertNotNull(response);
        assertNotNull(response.getBody());
        echoStringResponse = (EchoStringResponse) response.getBody().getAny().get(0);

        TestLogger.logger.debug(">> Response content: " + echoStringResponse.getEchoStringReturn());
        assertTrue("[ERROR] - No content in response object", echoStringResponse.getEchoStringReturn() != null);
        assertTrue("[ERROR] - Zero length content in response", echoStringResponse.getEchoStringReturn().length() > 0);

        
    }
    
    @Test
    public void testOneWayMessge() throws Exception {
        TestLogger.logger.debug("---------------------------------------");

        // Create the input param
        ObjectFactory factory = new ObjectFactory();
        EchoString echoString = factory.createEchoString();         
        echoString.setInput("ONE-WAY JAXB MESSAGE TEST");
        
        JAXBElement<Envelope> request = createJAXBEnvelope();
        request.getValue().getBody().getAny().add(echoString);
        
        // Invoke the Dispatch<Object> one-way
        TestLogger.logger.debug(">> Invoking one-way Dispatch with JAX-B Parameter");
        dispatchMessage.invokeOneWay(request);
        
        // Invoke the Dispatch<Object> one-way a second time
        TestLogger.logger.debug(">> Invoking one-way Dispatch with JAX-B Parameter");
        dispatchMessage.invokeOneWay(request);
    }
    
    private JAXBElement<Envelope> createJAXBEnvelope() {
        org.xmlsoap.schemas.soap.envelope.ObjectFactory factory = 
            new org.xmlsoap.schemas.soap.envelope.ObjectFactory();
        Envelope env = new Envelope();
        
        Body body = new Body();
        env.setBody(body);
        
        JAXBElement<Envelope> jaxbEnv = factory.createEnvelope(env);
        return jaxbEnv;
    }
}
