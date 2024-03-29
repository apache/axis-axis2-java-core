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

/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.sample.faults.FaultyWebServiceFault_Exception;
import org.apache.axis2.jaxws.sample.faults.FaultyWebServicePortType;
import org.apache.axis2.jaxws.sample.faults.FaultyWebServiceService;
import org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrap;
import org.apache.axis2.jaxws.sample.wrap.sei.DocLitWrapService;
import org.apache.axis2.testutils.Axis2Server;
import org.apache.axis2.testutils.Junit4ClassRunnerWithRuntimeIgnore;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.test.faults.FaultyWebServiceResponse;

import jakarta.xml.ws.AsyncHandler;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Response;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;

import static org.apache.axis2.jaxws.framework.TestUtils.await;
import static org.apache.axis2.jaxws.framework.TestUtils.checkUnknownHostURL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RunWith(Junit4ClassRunnerWithRuntimeIgnore.class)
public class FaultyWebServiceTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    private static String getEndpoint() throws Exception {
        return server.getEndpoint("FaultyWebServiceService.FaultyWebServicePortTypeImplPort");
    }

    @Test
    public void testFaultyWebService(){
        TestLogger.logger.debug("----------------------------------");
        FaultyWebServiceService service = new FaultyWebServiceService();
        FaultyWebServicePortType proxy = service.getFaultyWebServicePort();
        
        FaultyWebServiceFault_Exception exception = null;
        try{
            exception = null;
            BindingProvider p =	(BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());

            // the invoke will throw an exception, if the test is performed right
            int total = proxy.faultyWebService(10);

        }catch(FaultyWebServiceFault_Exception e){
            exception = e;
        }catch(Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }

        TestLogger.logger.debug("----------------------------------");

        assertNotNull(exception);
        assertEquals("custom exception", exception.getMessage());
        assertNotNull(exception.getFaultInfo());
        assertEquals("bean custom fault info", exception.getFaultInfo().getFaultInfo());
        assertEquals("bean custom message", exception.getFaultInfo().getMessage());
        
        // Repeat to verify behavior
        try{
            exception = null;
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());

            // the invoke will throw an exception, if the test is performed right
            int total = proxy.faultyWebService(10);

        }catch(FaultyWebServiceFault_Exception e){
            exception = e;
        }catch(Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }

        TestLogger.logger.debug("----------------------------------");

        assertNotNull(exception);
        assertEquals("custom exception", exception.getMessage());
        assertNotNull(exception.getFaultInfo());
        assertEquals("bean custom fault info", exception.getFaultInfo().getFaultInfo());
        assertEquals("bean custom message", exception.getFaultInfo().getMessage());

    }

    @Test
    public void testFaultyWebService_badEndpoint() throws Exception {
        String host = "this.endpoint.is.invalid";
        String badEndpoint = "http://" + host;

        checkUnknownHostURL(badEndpoint);

        TestLogger.logger.debug("----------------------------------");
        FaultyWebServiceService service = new FaultyWebServiceService();
        FaultyWebServicePortType proxy = service.getFaultyWebServicePort();
        
        WebServiceException exception = null;

        try{
            exception = null;
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,badEndpoint);

            // the invoke will throw an exception, if the test is performed right
            int total = proxy.faultyWebService(10);

        }catch(FaultyWebServiceFault_Exception e) {
            // shouldn't get this exception
            fail(e.toString());
        }catch(WebServiceException e) {
            exception = e;
        }catch(Exception e) {
            fail("This testcase should only produce a WebServiceException.  We got: " + e.toString());
        }

        TestLogger.logger.debug("----------------------------------");

        assertNotNull(exception);
        assertTrue(exception.getCause() instanceof UnknownHostException);
        assertTrue(exception.getCause().getMessage().indexOf(host)!=-1);
        
        
        // Repeat to verify behavior
        try{
            exception = null;
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,badEndpoint);

            // the invoke will throw an exception, if the test is performed right
            int total = proxy.faultyWebService(10);

        }catch(FaultyWebServiceFault_Exception e) {
            // shouldn't get this exception
            fail(e.toString());
        }catch(WebServiceException e) {
            exception = e;
        }catch(Exception e) {
            fail("This testcase should only produce a WebServiceException.  We got: " + e.toString());
        }

        TestLogger.logger.debug("----------------------------------");

        assertNotNull(exception);
        assertTrue(exception.getCause() instanceof UnknownHostException);
        assertTrue(exception.getCause().getMessage().indexOf(host)!=-1);

    }

    // TODO should also have an invoke oneway bad endpoint test to make sure
    // we get an exception as indicated in JAXWS 6.4.2.

    @Test
    public void testFaultyWebService_badEndpoint_oneWay() throws Exception {
        String host = "this.endpoint.is.invalid";
        String badEndpoint = "http://" + host;
        
        checkUnknownHostURL(badEndpoint);
        
        DocLitWrapService service = new DocLitWrapService();
        DocLitWrap proxy = service.getDocLitWrapPort();
        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,badEndpoint);
        
       

        WebServiceException exception = null;

        TestLogger.logger.debug("------------------------------");
        try{
            exception = null;
           
            proxy.oneWayVoid();

        }catch(WebServiceException e) {
            exception = e;
        }catch(Exception e) {
            fail("This testcase should only produce a WebServiceException.  We got: " + e.toString());
        }

        TestLogger.logger.debug("----------------------------------");

        assertNotNull(exception);
        assertTrue(exception.getCause() instanceof UnknownHostException);
        assertTrue(exception.getCause().getMessage().indexOf(host)!=-1);
        
        // Repeat to verify behavior
        try{
            exception = null;
           
            proxy.oneWayVoid();

        }catch(WebServiceException e) {
            exception = e;
        }catch(Exception e) {
            fail("This testcase should only produce a WebServiceException.  We got: " + e.toString());
        }

        TestLogger.logger.debug("----------------------------------");

        assertNotNull(exception);
        assertTrue(exception.getCause() instanceof UnknownHostException);
        assertTrue(exception.getCause().getMessage().indexOf(host)!=-1);
    }

    @Test
    public void testFaultyWebService_badEndpoint_AsyncCallback()
    throws Exception {

        String host = "this.endpoint.is.invalid";
        String badEndpoint = "http://" + host;

        TestLogger.logger.debug("------------------------------");

        FaultyWebServiceService service = new FaultyWebServiceService();
        FaultyWebServicePortType proxy = service.getFaultyWebServicePort();
        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                  badEndpoint);

        FaultyAsyncHandler callback = new FaultyAsyncHandler();
        Future<?> future = proxy.faultyWebServiceAsync(1, callback);

        await(future);

        Exception e = callback.getException();

        // Section 4.3.3 states that the top level Exception should be
        // an ExecutionException, with a WebServiceException underneath.
        assertNotNull("The exception was null.", e);
        assertTrue("The thrown exception should be an ExecutionException.", e
                   .getClass().equals(ExecutionException.class));
        assertTrue(
                   "The expected fault type under the ExecutionException should be a "
                   + "SOAPFaultException.  Found type: "
                   + e.getCause().getClass(), e.getCause().getClass()
                   .isAssignableFrom(SOAPFaultException.class));
        
        // Repeat to verify behavior
        callback = new FaultyAsyncHandler();
        future = proxy.faultyWebServiceAsync(1, callback);

        await(future);

        e = callback.getException();

        // Section 4.3.3 states that the top level Exception should be
        // an ExecutionException, with a WebServiceException underneath.
        assertNotNull("The exception was null.", e);
        assertTrue("The thrown exception should be an ExecutionException.", e
                   .getClass().equals(ExecutionException.class));
        assertTrue(
                   "The expected fault type under the ExecutionException should be a "
                   + "SOAPFaultException.  Found type: "
                   + e.getCause().getClass(), e.getCause().getClass()
                   .isAssignableFrom(SOAPFaultException.class));

    }

    @Test
    public void testFaultyWebService_badEndpoint_AsyncPolling()
    throws Exception {

        String host = "this.endpoint.is.invalid";
        String badEndpoint = "http://" + host;

        TestLogger.logger.debug("------------------------------");

        FaultyWebServiceService service = new FaultyWebServiceService();
        FaultyWebServicePortType proxy = service.getFaultyWebServicePort();
        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                  badEndpoint);

        Future<?> future = proxy.faultyWebServiceAsync(1);
        await(future);

        Exception e = null;
        try {
            Object obj = future.get();
        } catch (Exception ex) {
            e = ex;
        }

        // Section 4.3.3 states that the top level Exception should be
        // an ExecutionException, with a WebServiceException underneath.
        assertNotNull("The exception was null.", e);
        assertTrue("The thrown exception should be an ExecutionException.", e
                   .getClass().equals(ExecutionException.class));
        assertTrue(
                   "The expected fault type under the ExecutionException should be a "
                   + "SOAPFaultException.  Found type: "
                   + e.getCause().getClass(), e.getCause().getClass()
                   .isAssignableFrom(SOAPFaultException.class));
        
        
        // Repeat to verify behavior
        proxy.faultyWebServiceAsync(1);
        await(future);

        e = null;
        try {
            Object obj = future.get();
        } catch (Exception ex) {
            e = ex;
        }

        // Section 4.3.3 states that the top level Exception should be
        // an ExecutionException, with a WebServiceException underneath.
        assertNotNull("The exception was null.", e);
        assertTrue("The thrown exception should be an ExecutionException.", e
                   .getClass().equals(ExecutionException.class));
        assertTrue(
                   "The expected fault type under the ExecutionException should be a "
                   + "SOAPFaultException.  Found type: "
                   + e.getCause().getClass(), e.getCause().getClass()
                   .isAssignableFrom(SOAPFaultException.class));

    }

    /*
     * Tests fault processing for user defined fault types
     */      
    @Test
    public void testCustomFault_AsyncCallback() throws Exception {
        TestLogger.logger.debug("------------------------------");

        FaultyWebServiceService service = new FaultyWebServiceService();
        FaultyWebServicePortType proxy = service.getFaultyWebServicePort();
        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint());

        FaultyAsyncHandler callback = new FaultyAsyncHandler();
        Future<?> future = proxy.faultyWebServiceAsync(1, callback);

        await(future);

        Exception e = callback.getException();
        e.printStackTrace();

        // Section 4.3.3 states that the top level Exception should be
        // an ExecutionException, with a WebServiceException underneath.
        assertNotNull("The exception was null.", e);
        assertTrue("The thrown exception should be an ExecutionException.", 
                   e.getClass().equals(ExecutionException.class));
        assertTrue("The expected fault type under the ExecutionException should be a " +
                   "FaultyWebServiceFault_Exception.  Found type: " + e.getCause().getClass(), 
                   e.getCause().getClass().isAssignableFrom(FaultyWebServiceFault_Exception.class));
        
        
        // Repeat to verify behavior
        callback = new FaultyAsyncHandler();
        future = proxy.faultyWebServiceAsync(1, callback);

        await(future);

        e = callback.getException();
        e.printStackTrace();

        // Section 4.3.3 states that the top level Exception should be
        // an ExecutionException, with a WebServiceException underneath.
        assertNotNull("The exception was null.", e);
        assertTrue("The thrown exception should be an ExecutionException.", 
                   e.getClass().equals(ExecutionException.class));
        assertTrue("The expected fault type under the ExecutionException should be a " +
                   "FaultyWebServiceFault_Exception.  Found type: " + e.getCause().getClass(), 
                   e.getCause().getClass().isAssignableFrom(FaultyWebServiceFault_Exception.class));
    }


    /*
     * A callback implementation that can be used to collect the exceptions
     */
    class FaultyAsyncHandler implements AsyncHandler<FaultyWebServiceResponse> {

        Exception exception;

        public void handleResponse(Response<FaultyWebServiceResponse> response) {
            try {
                TestLogger.logger.debug("FaultyAsyncHandler.handleResponse() was called");
                FaultyWebServiceResponse r = response.get();
                TestLogger.logger.debug("No exception was thrown from Response.get()");
            }
            catch (Exception e) {
                TestLogger.logger.debug("An exception was thrown: " + e.getClass());
                exception = e;
            }
        }

        public Exception getException() {
            return exception;
        }
    }

}
