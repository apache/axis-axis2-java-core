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

package org.apache.axis2.jaxws.server;

import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.registry.InvocationListenerRegistry;
import org.apache.axis2.jaxws.server.EndpointController;
import org.apache.axis2.jaxws.server.EndpointInvocationContext;
import org.apache.axis2.jaxws.server.EndpointInvocationContextImpl;
import org.apache.axis2.jaxws.server.InvocationListenerBean;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;

public class JAXWSServerTests extends TestCase {
    
    InvocationListenerFactory fac1 = new TestInvocationProcessorFactory1();
    InvocationListenerFactory fac2 = new TestInvocationProcessorFactory2();
    
    public void setUp() {
        InvocationListenerRegistry.addFactory(fac1);
        InvocationListenerRegistry.addFactory(fac2);
    }
    
    /**
     * This verifies that multiple InvocationProcessorFactories can be
     * registered with the FactoryRegistry.
     */
    public void testRegisterFactories() {
        Collection<InvocationListenerFactory> factories = InvocationListenerRegistry.getFactories();
        assertNotNull(factories);
        assertEquals(factories.size(), 2);
    }
    
    /**
     * This will verify that the JAXWSMessageReceiver is able to find and
     * store InvocationProcessorFactories on the EndpointInvocationContext.
     */
    public void testAddFactoriesToEIC() {
        EndpointInvocationContext eic = new EndpointInvocationContextImpl();
        JAXWSMessageReceiver receiver = new JAXWSMessageReceiver();
        receiver.addInvocationListenerFactories(eic);
        Collection<InvocationListenerFactory> factories = eic.getInvocationListenerFactories();
        assertNotNull(factories);
        assertEquals(factories.size(), 2);
    }

    /**
     * This will tests that registered InvocationListeners are properly called
     * by the JAX-WS server-side code. This test approximates a synchronous
     * message request
     */
    public void testSyncInvocationListener() {
        EndpointController controller = new EndpointController();
        EndpointInvocationContext eic = new EndpointInvocationContextImpl();
        MessageContext request = new MessageContext();
        MessageContext response = new MessageContext();
        eic.setRequestMessageContext(request);
        eic.setResponseMessageContext(response);
        JAXWSMessageReceiver receiver = new JAXWSMessageReceiver();
        receiver.addInvocationListenerFactories(eic);
        controller.requestReceived(eic);
        assertNotNull(request.getProperty("requestReceived"));
        assertTrue((Boolean) request.getProperty("requestReceived"));
        controller.responseReady(eic);
        assertNotNull(response.getProperty("responseReady"));
        assertTrue((Boolean) response.getProperty("responseReady"));
    }
    
    /**
     * This will tests that registered InvocationListeners are properly called
     * by the JAX-WS server-side code. This test approximates an asynchronous
     * message request
     */
    public void testAsyncInvocationListener() {
        EndpointController controller = new EndpointController();
        EndpointCallback callback = new EndpointCallback();
        EndpointInvocationContext eic = new EndpointInvocationContextImpl();
        MessageContext request = new MessageContext();
        MessageContext response = new MessageContext();
        eic.setRequestMessageContext(request);
        eic.setResponseMessageContext(response);
        JAXWSMessageReceiver receiver = new JAXWSMessageReceiver();
        receiver.addInvocationListenerFactories(eic);
        controller.requestReceived(eic);
        assertNotNull(request.getProperty("requestReceived"));
        assertTrue((Boolean) request.getProperty("requestReceived"));
        callback.responseReady(eic);
        assertNotNull(response.getProperty("responseReady"));
        assertTrue((Boolean) response.getProperty("responseReady"));
    }
    
    static class TestInvocationProcessorFactory1 implements InvocationListenerFactory {
        public InvocationListener createInvocationListener(MessageContext context) {
            return new TestInvocationListener();
        }
    }
    
    static class TestInvocationProcessorFactory2 implements InvocationListenerFactory {
        public InvocationListener createInvocationListener(MessageContext context) {
            return new TestInvocationListener();
        }
    }
    
    
    static class TestInvocationListener implements InvocationListener {
        
        public void notify(InvocationListenerBean bean) {
            if(bean.getState().equals(InvocationListenerBean.State.REQUEST)) {
                bean.getEndpointInvocationContext().getRequestMessageContext().
                    setProperty("requestReceived", true);
            }
            else {
                bean.getEndpointInvocationContext().getResponseMessageContext().
                    setProperty("responseReady", true);
            }
        }
    }
}
