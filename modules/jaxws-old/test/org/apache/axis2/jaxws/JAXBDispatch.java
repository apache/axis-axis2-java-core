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

import javax.xml.bind.JAXBContext;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import junit.framework.TestCase;
import test.EchoString;
import test.EchoStringResponse;
import test.ObjectFactory;

public class JAXBDispatch extends TestCase {

    private Dispatch<Object> dispatch;
    
    public JAXBDispatch(String name) {
        super(name);
    }
    
    public void setUp() throws Exception {
        //Create the Service object
        Service svc = Service.create(DispatchTestConstants.QNAME_SERVICE);
        svc.addPort(DispatchTestConstants.QNAME_PORT, null, DispatchTestConstants.URL);
        
        //Create the JAX-B Dispatch object
        JAXBContext jbc = null;
        jbc = JAXBContext.newInstance("test");
        dispatch = svc.createDispatch(DispatchTestConstants.QNAME_PORT, 
                jbc, Service.Mode.PAYLOAD);
    }
    
    public void testSync() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());

        // Create the input param
        ObjectFactory factory = new ObjectFactory();
        EchoString request = factory.createEchoString();         
        request.setInput("SYNC JAXB TEST");
        
        // Invoke the Dispatch<Object>
        System.out.println(">> Invoking sync Dispatch with JAX-B Parameter");
        EchoStringResponse response = (EchoStringResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        
        System.out.println(">> Response content: " + response.getEchoStringReturn());
        
        assertTrue("[ERROR] - Response object was null", response != null);
        assertTrue("[ERROR] - No content in response object", response.getEchoStringReturn() != null);
        assertTrue("[ERROR] - Zero length content in response", response.getEchoStringReturn().length() > 0);
    }
    
    public void testAysnc() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        // Create the input param
        ObjectFactory factory = new ObjectFactory();
        EchoString request = factory.createEchoString();         
        request.setInput("ASYNC(CALLBACK) JAXB TEST");
        
        // Create the callback for async responses
        JAXBCallbackHandler<Object> callback = new JAXBCallbackHandler<Object>();
        
        // Invoke the Dispatch<Object> asynchronously
        System.out.println(">> Invoking async(callback) Dispatch with JAX-B Parameter");
        Future<?> monitor = dispatch.invokeAsync(request, callback);
        
        while (!monitor.isDone()) {
             System.out.println(">> Async invocation still not complete");
             Thread.sleep(1000);
        }
    }
    
    public void testOneWay() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());

        // Create the input param
        ObjectFactory factory = new ObjectFactory();
        EchoString request = factory.createEchoString();         
        request.setInput("ONE-WAY JAXB TEST");
        
        // Invoke the Dispatch<Object> one-way
        System.out.println(">> Invoking one-way Dispatch with JAX-B Parameter");
        dispatch.invokeOneWay(request);
    }
}
