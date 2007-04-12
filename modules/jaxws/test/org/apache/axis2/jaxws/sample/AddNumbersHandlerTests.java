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
package org.apache.axis2.jaxws.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersClientLogicalHandler;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersClientLogicalHandler2;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersClientProtocolHandler;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersHandlerPortType;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersHandlerService;
import org.apache.axis2.jaxws.TestLogger;
import org.test.addnumbershandler.AddNumbersHandlerResponse;

public class AddNumbersHandlerTests extends TestCase {
	
    String axisEndpoint = "http://localhost:8080/axis2/services/AddNumbersHandlerService";
    // TODO: disabled until handler support is more complete
    public void _testAddNumbersHandler() {
		try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
			
            AddNumbersHandlerService service = new AddNumbersHandlerService();
			AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
			
            BindingProvider p =	(BindingProvider)proxy;
			p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);	
			int total = proxy.addNumbersHandler(10,10);
			
            assertEquals("With handler manipulation, total should be 3 less than a proper sumation.", 17, total);
            TestLogger.logger.debug("Total (after handler manipulation) = " + total);
            TestLogger.logger.debug("----------------------------------");
		} catch(Exception e) {
			e.printStackTrace();
			fail();
		}
	}

    // TODO: disabled until handler support is more complete
    public void _testAddNumbersHandlerWithFault() {
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);  
            // value 99 triggers the handler to throw an exception, but does
            // NOT trigger the AddNumbersHandler.handlefault method.
            // The spec does not call the handlefault method of a handler that
            // causes a flow reversal
            int total = proxy.addNumbersHandler(99,10);
            
            fail("We should have got an exception due to the handler.");
        } catch(Exception e) {
            e.printStackTrace();
            assertTrue("Exception should be SOAPFaultException", e instanceof SOAPFaultException);
            assertEquals(((SOAPFaultException)e).getMessage(), "AddNumbersLogicalHandler2 was here");
        }
        TestLogger.logger.debug("----------------------------------");
    }
    
    // TODO: disabled until handler support is more complete
    public void _testAddNumbersClientHandler() {
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);

            List<Handler> handlers = p.getBinding().getHandlerChain();
            if (handlers == null)
                handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler());
            p.getBinding().setHandlerChain(handlers);

            int total = proxy.addNumbersHandler(10,10);
            
            assertEquals("With handler manipulation, total should be 4 less than a proper sumation.", 16, total);
            TestLogger.logger.debug("Total (after handler manipulation) = " + total);
            TestLogger.logger.debug("----------------------------------");
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /*
     * uses a custom HandlerResolver instead of the default
     */
    // TODO: disabled until handler support is more complete
    public void _testAddNumbersClientHandlerMyResolver() {
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            
            // There's a HandlerChain annotation on the SEI, but since
            // we're using our own handlerresolver that returns an empty list
            // no client-side handlers will be run
            service.setHandlerResolver(new MyHandlerResolver());
            
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);

            int total = proxy.addNumbersHandler(10,10);
            
            assertEquals("With server-side only handler manipulation, total should be a 17.", 17, total);
            TestLogger.logger.debug("Total (after handler manipulation) = " + total);
            TestLogger.logger.debug("----------------------------------");
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    
    // TODO: disabled until handler support is more complete
    public void _testAddNumbersClientProtoAndLogicalHandler() {
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);

            List<Handler> handlers = p.getBinding().getHandlerChain();
            if (handlers == null)
                handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler());
            handlers.add(new AddNumbersClientProtocolHandler());
            p.getBinding().setHandlerChain(handlers);

            // value 102 triggers an endpoint exception, which will run through the server outbound
            // handleFault methods, then client inbound handleFault methods
            int total = proxy.addNumbersHandler(102,10);
            
            fail("should have got an exception, but didn't");
        } catch(Exception e) {
            e.printStackTrace();
            assertTrue("Exception should be SOAPFaultException", e instanceof SOAPFaultException);
            assertEquals(((SOAPFaultException)e).getMessage(), "AddNumbersLogicalHandler2 was here");
        }
        TestLogger.logger.debug("----------------------------------");
    }
    
    // TODO: disabled until handler support is more complete
    public void _testAddNumbersClientHandlerWithFault() {
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);

            List<Handler> handlers = p.getBinding().getHandlerChain();
            if (handlers == null)
                handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler());
            p.getBinding().setHandlerChain(handlers);

            int total = proxy.addNumbersHandler(99,10);
            
            fail("Should have got an exception, but we didn't.");
            TestLogger.logger.debug("----------------------------------");
        } catch(Exception e) {
            e.printStackTrace();
            assertTrue("Exception should be SOAPFaultException", e instanceof SOAPFaultException);
            assertEquals(((SOAPFaultException)e).getMessage(), "I don't like the value 99");
        }
    }
    
    // TODO: disabled until handler support is more complete
    public void _testAddNumbersClientHandlerAsync() {
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);

            List<Handler> handlers = p.getBinding().getHandlerChain();
            if (handlers == null)
                handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler());
            handlers.add(new AddNumbersClientLogicalHandler2());
            p.getBinding().setHandlerChain(handlers);

            
            AddNumbersHandlerAsyncCallback callback = new AddNumbersHandlerAsyncCallback();
            Future<?> future = proxy.addNumbersHandlerAsync(10, 10, callback);

            while (!future.isDone()) {
                Thread.sleep(1000);
                TestLogger.logger.debug("Async invocation incomplete");
            }
            
            int total = callback.getResponseValue();
            
            assertEquals("With handler manipulation, total should be 26.", 26, total);
            TestLogger.logger.debug("Total (after handler manipulation) = " + total);
            TestLogger.logger.debug("----------------------------------");
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
    
    public void testOneWay() {
        try {
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider bp = (BindingProvider) proxy;
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);
            proxy.oneWayInt(11);
            TestLogger.logger.debug("----------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }       
    }
    
    /*
     * A callback implementation that can be used to collect the exceptions
     */
    class AddNumbersHandlerAsyncCallback implements AsyncHandler<AddNumbersHandlerResponse> {
     
        private Exception exception;
        private int retVal;
        
        public void handleResponse(Response<AddNumbersHandlerResponse> response) {
            try {
                TestLogger.logger.debug("FaultyAsyncHandler.handleResponse() was called");
                AddNumbersHandlerResponse r = response.get();
                TestLogger.logger.debug("No exception was thrown from Response.get()");
                retVal = r.getReturn();
            }
            catch (Exception e) {
                TestLogger.logger.debug("An exception was thrown: " + e.getClass());
                exception = e;
            }
        }
        
        public int getResponseValue() {
            return retVal;
        }
        
        public Exception getException() {
            return exception;
        }
    }
    
    class MyHandlerResolver implements HandlerResolver {

        public List<Handler> getHandlerChain(PortInfo portinfo) {
            return new ArrayList<Handler>();
        }
        
    }
    
}
