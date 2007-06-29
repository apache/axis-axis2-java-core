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
import java.util.Map;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPFaultException;

import java.io.StringReader;
import java.io.StringWriter;
import junit.framework.TestCase;

import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersClientLogicalHandler;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersClientLogicalHandler2;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersClientLogicalHandler3;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersClientLogicalHandler4;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersClientProtocolHandler;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersHandlerPortType;
import org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersHandlerService;
import org.apache.axis2.jaxws.TestLogger;
import org.test.addnumbershandler.AddNumbersHandlerResponse;

/**
 * @author rott
 *
 */
public class AddNumbersHandlerTests extends TestCase {
	
    String axisEndpoint = "http://localhost:8080/axis2/services/AddNumbersHandlerService";


    /**
     * Client app sends 10, 10 as params to sum.  No client-side handlers are configured
     * for this scenario.  The server-side AddNumbersLogicalHandler is instantiated with a
     * variable "deduction" with value 1.  Upon class initialization using PostConstruct
     * annotation, that internal variable is changed to value 2.  The inbound AddNumbersLogicalHandler
     * subtracts 1 from the first param, then outbound it subtracts 2 from the result sum.
     * 
     * This test accomplishes three things (which also carry over to other tests since they all use
     * the same endpoint and server-side handlers:
     * 1)  PostConstruct annotation honored in the handler framework for handler instantiation
     * 2)  AddNumbersLogicalHandler also sets two message context properties, one with APPLICATION
     *     scope, which the endpoint checks.
     * 3)  Handlers are sharing properties, both APPLICATION scoped and HANDLER scoped
     * 3)  General handler framework functionality; make sure handlers are instantiated and called
     */
    public void testAddNumbersHandler() {
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
            fail(e.getMessage());
		}
	}
    
    public void testAddNumbersHandlerDispatch() {
        try {
            QName serviceName =
                    new QName("http://org/test/addnumbershandler", "AddNumbersHandlerService");
            QName portName =
                    new QName("http://org/test/addnumbershandler", "AddNumbersHandlerPort");

            Service myService = Service.create(serviceName);
            myService.addPort(portName, null, axisEndpoint);
            Dispatch<Source> myDispatch = myService.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);

            // set handler chain for binding provider
            Binding binding = ((BindingProvider) myDispatch).getBinding();

            // create a new list or use the existing one
            List<Handler> handlers = binding.getHandlerChain();
        
            if (handlers == null)
                handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler());
            handlers.add(new AddNumbersClientProtocolHandler());
            binding.setHandlerChain(handlers);
            
            //Invoke the Dispatch
            TestLogger.logger.debug(">> Invoking Async Dispatch");
            Source response = myDispatch.invoke(createRequestSource());
            String resString = getString(response);
            if (!resString.contains("<return>16</return>")) {
                fail("Response string should contain <return>17</return>, but does not.  The resString was: \"" + resString + "\"");
            }

            TestLogger.logger.debug("----------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /*
     * JAXWS 9.2.1.1 conformance test
     */
    public void testAddNumbersHandlerResolver() {
        try {
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());

            AddNumbersHandlerService service = new AddNumbersHandlerService();

            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            service.setHandlerResolver(new MyHandlerResolver());

            BindingProvider p = (BindingProvider) proxy;
            
            /*
             * despite setting MyHandlerResolver on the service, we should get an empty
             * list from the getBinding().getHandlerChain() call below.  JAXWS 9.2.1.1 conformance
             */
            List<Handler> list = p.getBinding().getHandlerChain();
            
            assertTrue("List should be empty.  We've not conformed to JAXWS 9.2.1.1.", list.isEmpty());

            TestLogger.logger.debug("----------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    // TODO: disabled until handler support is more complete
    public void testAddNumbersHandlerWithFault() {
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


    /**
     * testAddNumbersClientHandler performs the same tests as testAddNumbersHandler, except
     * that two client-side handlers are also inserted into the flow.  The inbound AddNumbersClientLogicalHandler
     * checks that the properties set here in this method (the client app) and the properties set in the
     * outbound AddNumbersClientProtocolHandler are accessible.  These properties are also checked here in
     * the client app.  AddNumbersClientLogicalHandler also subtracts 1 from the sum on the inbound flow.
     */
    public void testAddNumbersClientHandler() {
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);
            p.getRequestContext().put("myClientKey", "myClientVal");

            List<Handler> handlers = p.getBinding().getHandlerChain();
            if (handlers == null)
                handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler());
            handlers.add(new AddNumbersClientProtocolHandler());
            p.getBinding().setHandlerChain(handlers);

            int total = proxy.addNumbersHandler(10,10);
            
            // see if I can get an APPLICATION scoped property set during outbound flow.  I should be able to do this according to 4.2.1
            
            // TODO:  assert is now commented out.  This property is set by a client outbound handler, and I don't think it
            // should be available on the request or response contexts.
            //assertNotNull("Should be able to retrieve APPLICATION scoped property, but could not.", ((String)p.getRequestContext().get("AddNumbersClientProtocolHandlerOutboundAppScopedProperty")));

            // should NOT be able to get this HANDLER scoped property though
            assertNull("Should not be able to retrieve HANDLER scoped property, but was able.", (String)p.getResponseContext().get("AddNumbersClientProtocolHandlerOutboundHandlerScopedProperty"));
            // should be able to get this APPLICATION scoped property set during inbound flow
            assertNotNull("Should be able to retrieve APPLICATION scoped property, but could not.", (String)p.getResponseContext().get("AddNumbersClientProtocolHandlerInboundAppScopedProperty"));
            // should NOT be able to get this HANDLER scoped property though
            assertNull("Should not be able to retrieve HANDLER scoped property, but was able.", (String)p.getResponseContext().get("AddNumbersClientProtocolHandlerInboundHandlerScopedProperty"));
            // should be able to get this APPLICATION scoped property set by this client
            assertNotNull("Should be able to retrieve APPLICATION scoped property, but could not.", (String)p.getRequestContext().get("myClientKey"));

            assertEquals("With handler manipulation, total should be 4 less than a proper sumation.", 16, total);
            TestLogger.logger.debug("Total (after handler manipulation) = " + total);
            TestLogger.logger.debug("----------------------------------");
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    /*
     * uses a custom HandlerResolver instead of the default.  MyHandlerResolver
     * puts the AddNumbersClientLogicalHandler and AddNumbersClientProtocolHandler
     * in the flow.  Results should be the same as testAddNumbersClientHandler.
     */
    public void testAddNumbersClientHandlerMyResolver() {
        try{
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersHandlerService service = new AddNumbersHandlerService();
            service.setHandlerResolver(new MyHandlerResolver());
            
            AddNumbersHandlerPortType proxy = service.getAddNumbersHandlerPort();
            
            BindingProvider p = (BindingProvider)proxy;
            
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);

            int total = proxy.addNumbersHandler(10,10);
            
            assertEquals("With handler manipulation, total should be 4 less than a proper sumation.",
                         16,
                         total);
            TestLogger.logger.debug("Total (after handler manipulation) = " + total);
            TestLogger.logger.debug("----------------------------------");
        } catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    
    // TODO: disabled until handler support is more complete
    public void testAddNumbersClientProtoAndLogicalHandler() {
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
            //AXIS2-2417 - assertEquals(((SOAPFaultException)e).getMessage(), "AddNumbersLogicalHandler2 was here");
            assertEquals(((SOAPFaultException)e).getMessage(), "Got value 101.  " +
            		"AddNumbersHandlerPortTypeImpl.addNumbersHandler method is " +
            		"correctly throwing this exception as part of testing");
            
        }
        TestLogger.logger.debug("----------------------------------");
    }
    
    public void testAddNumbersClientHandlerWithFault() {
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
            handlers.add(new AddNumbersClientLogicalHandler4());
            handlers.add(new AddNumbersClientLogicalHandler3());
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

    /**
     * test results should be the same as testAddNumbersClientHandler, except that
     * AddNumbersClientLogicalHandler2 doubles the first param on outbound.  Async, of course.
     *
     */
    public void testAddNumbersClientHandlerAsync() {
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
            handlers.add(new AddNumbersClientProtocolHandler());
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
            ArrayList<Handler> handlers = new ArrayList<Handler>();
            handlers.add(new AddNumbersClientLogicalHandler());
            handlers.add(new AddNumbersClientProtocolHandler());
            return handlers;
        }

    }
    
    private String getString(Source source) throws Exception {
        if (source == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        Result result = new StreamResult(writer);
        t.transform(source, result);
        return writer.getBuffer().toString();

    }
    
    /**
     * Create a Source request to be used by Dispatch<Source>
     */
    private Source createRequestSource() {

        String reqString = null;

        String ns = "http://org/test/addnumbershandler";
        String operation = "addNumbersHandler";

        reqString = "<" + operation + 
                    " xmlns=\"" + ns + "\">" +
                    "<arg0>10</arg0><arg1>10</arg1>" +
                    "</" + operation + ">";

        return new StreamSource(new StringReader(reqString));
    }
}
