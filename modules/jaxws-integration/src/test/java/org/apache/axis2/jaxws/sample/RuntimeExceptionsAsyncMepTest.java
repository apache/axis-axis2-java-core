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
package org.apache.axis2.jaxws.sample;

import static org.apache.axis2.jaxws.framework.TestUtils.checkUnknownHostURL;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Response;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.soap.AddressingFeature;
import jakarta.xml.ws.soap.SOAPFaultException;

import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.deployment.FileSystemConfigurator;
import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.jaxws.sample.asyncdoclit.client.AsyncClient;
import org.apache.axis2.jaxws.sample.asyncdoclit.client.AsyncPort;
import org.apache.axis2.jaxws.sample.asyncdoclit.client.AsyncService;
import org.apache.axis2.jaxws.sample.asyncdoclit.client.ThrowExceptionFault;
import org.apache.axis2.jaxws.sample.asyncdoclit.common.CallbackHandler;
import org.apache.axis2.metadata.registry.MetadataFactoryRegistry;
import org.apache.axis2.testutils.Axis2Server;
import org.apache.axis2.testutils.Junit4ClassRunnerWithRuntimeIgnore;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.test.asyncdoclit.ExceptionTypeEnum;
import org.test.asyncdoclit.ThrowExceptionResponse;

/**
 * Test for varios async exceptions whern AsyncMEP is enabled
 */
@RunWith(Junit4ClassRunnerWithRuntimeIgnore.class)
public class RuntimeExceptionsAsyncMepTest {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/addressing-repo");

    private static final String CONNECT_EXCEPTION_ENDPOINT = "http://localhost:6061/axis2/services/AsyncService2.DocLitWrappedPortImplPort";

    static final String HOST_NOT_FOUND_ENDPOINT = "http://this.endpoint.is.invalid/nope";

    /*
     * For async-on-the-wire exchanges, we need to enable WS-Addressing and get a transport
     * listener setup to receive the inbound request from the service-provider which contains the
     * response.  We only need to do that one time for all the tests.
     */
    static boolean listenerAlreadySetup = false;

    private static String getEndpoint() throws Exception {
        return server.getEndpoint("AsyncService2.DocLitWrappedPortImplPort");
    }

    private AsyncPort getPort() throws Exception {
        return getPort(null);
    }
    
    private AsyncPort getPort(WebServiceFeature... features) throws Exception {

        AsyncService service = new AsyncService();
        AsyncPort port = service.getAsyncPort(features);
        assertNotNull("Port is null", port);

        Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                getEndpoint());

        return port;
    }
    /**
     * @testStrategy Invoke the proxy with sync method, specifying that it should use
     *               the async-on-the-wire MEP.  The proxy enpdoint specifies a port that
     *               does not exist.  Verify that the connection exception is received
     *               by the client.
     */
    @Test
    public void testAsyncCallback_asyncWire_ConnectException() throws Exception {
        setupAddressingAndListener();
        
        AsyncPort port = getPort();
        Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, CONNECT_EXCEPTION_ENDPOINT);
        rc.put("org.apache.axis2.jaxws.use.async.mep", Boolean.TRUE);

        try {
            String resp = port.throwException(ExceptionTypeEnum.WSE);
            fail("Did not get an exception as expected");
        } catch (Exception ee) {
            assertTrue("ExecutionException.getCause should be an instance of WebServiceException",
                       ee instanceof WebServiceException);
            assertTrue("Didn't get a cause within the WebServiceException",
                       ee.getCause() != null);
            assertTrue("Cause was not ConnectionException as expected", 
                       ee.getCause() instanceof ConnectException);
        }
    }

    /**
     * @testStrategy Invoke the proxy with async-polling method, the proxy is
     *               configured against an endpoint which does not exist (this
     *               is a server not found case). Expected to throw a
     *               EE/WSE/UnknownHostException
     */
    @Test
    public void testAsyncPolling_asyncMEP_UnknwonHost() throws Exception {
        checkUnknownHostURL(HOST_NOT_FOUND_ENDPOINT);

        AsyncPort port = getPort();

        Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                HOST_NOT_FOUND_ENDPOINT);

        Response<ThrowExceptionResponse> resp = port
                .throwExceptionAsync(ExceptionTypeEnum.WSE);

        AsyncClient.waitBlocking(resp);
        try {
            resp.get();

            fail("ExecutionException expected at invoke time when an invalid endpoint address is specified");
        } catch (ExecutionException ee) {
            // Constants.logStack(ee);

            assertTrue("EE.getCause must be WebServiceException",
                    ee.getCause() instanceof WebServiceException);

            assertTrue("WSE.getCause must be UnknownHostException", checkStack(
                    ee, UnknownHostException.class));
        }
    }

    /**
     * @testStrategy Invoke the proxy with async-polling method, the proxy is
     *               configured against an endpoint which does not exist (this
     *               is a 404-Not Found case). Expected to throw a EE/WSE
     */
    @Test
    public void testAsyncPolling_asyncMEP_404NotFound() throws Exception {

        AsyncPort port = getPort();
        Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint() + "/DoesNotExist");
        Response<ThrowExceptionResponse> resp = port
                .throwExceptionAsync(ExceptionTypeEnum.WSE);

        AsyncClient.waitBlocking(resp);
        try {
            resp.get();

            fail("ExecutionException expected at invoke time when an invalid endpoint address is specified");
        } catch (ExecutionException ee) {
            // Constants.logStack(ee);

            assertTrue("EE.getCause must be WebServiceException",
                    ee.getCause() instanceof WebServiceException);

            /*
             * TODO: REVIEW. Original test was written expecting a 404 from the
             * bad URL set on the requestcontext. However, this test actually
             * does make it to the endpoint, and returns the exception specified
             * in the call to port.throwExceptionAsync(ExceptionTypeEnum.WSE)
             * above...so the assert is commented until we can review it. Also,
             * different servers may behave differently, depending on how they
             * want to parse the incoming request URL.
             */
            // assertTrue("WSE.getCause must be 404", checkStack(ee,
            // java.net.ConnectException.class));
        }
    }

    /**
     * @testStrategy Invoke the proxy with async-polling method, the endpoint
     *               will throw a WSE which should result in a
     *               EE/SOAPFaultException
     */
    @Test
    public void testAsyncPolling_asyncMEP_WebServiceException()
            throws Exception {

        AsyncPort port = getPort();
        Response<ThrowExceptionResponse> resp = port
                .throwExceptionAsync(ExceptionTypeEnum.WSE);

        AsyncClient.waitBlocking(resp);
        try {
            resp.get();
            fail("ExecutionException expected at Response.get when ednpoint throws an exception");
        } catch (ExecutionException ee) {
            // Constants.logStack(ee);

            assertTrue(
                    "ExecutionException.getCause should be an instance of SOAPFaultException",
                    ee.getCause() instanceof SOAPFaultException);
        }
    }

    /**
     * @testStrategy Invoke the proxy with async-polling method, the endpoint
     *               will throw a wsdl:fault which should result in a
     *               EE/SimpleFault
     */
    @Test
    public void testAsyncPolling_asyncMEP_WsdlFault() throws Exception {

        AsyncPort port = getPort();
        Response<ThrowExceptionResponse> resp = port
                .throwExceptionAsync(ExceptionTypeEnum.WSDL_FAULT);

        AsyncClient.waitBlocking(resp);
        try {
            resp.get();
            fail("ExecutionException expected at Response.get when ednpoint throws an exception");
        } catch (ExecutionException ee) {
            // Constants.logStack(ee);

            assertTrue(
                    "ExecutionException.getCause should be an instance of SimpleFault",
                    ee.getCause() instanceof ThrowExceptionFault);
        }
    }

    /** ******************** Async Callback ******************* */

    /**
     * @testStrategy Invoke the proxy with async-callback method, the proxy is
     *               configured against an endpoint which does not exist (this
     *               is a server not found case). Expected to throw a
     *               EE/WSE/UnknownHostException
     */
    @Test
    public void testAsyncCallback_asyncMEP_UnknownHost() throws Exception {
        checkUnknownHostURL(HOST_NOT_FOUND_ENDPOINT);

        AsyncPort port = getPort();
        Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                HOST_NOT_FOUND_ENDPOINT);

        CallbackHandler<ThrowExceptionResponse> handler = new CallbackHandler<ThrowExceptionResponse>();
        Future<?> resp = port.throwExceptionAsync(ExceptionTypeEnum.WSE,
                handler);

        AsyncClient.waitBlocking(resp);
        try {
            handler.get();

            fail("ExecutionException expected at invoke time when an invalid endpoint address is specified");
        } catch (ExecutionException ee) {
            // Constants.logStack(ee);

            assertTrue(
                    "ExecutionException.getCause should be an instance of WebServiceException",
                    ee.getCause() instanceof WebServiceException);

            assertTrue("WSE.getCause must be UnknownHostException", checkStack(
                    ee, UnknownHostException.class));

        }
    }

    /**
     * @testStrategy Invoke the proxy with async-callback method, the proxy is
     *               configured against an endpoint which does not exist (this
     *               is a 404 Not Found case). Expected to throw a
     *               EE/WSE/UnknownHostException
     */
    @Test
    public void testAsyncCallback_asyncMEP_404NotFound() throws Exception {

        AsyncPort port = getPort();
        Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, getEndpoint() + "/DoesNotExist");

        CallbackHandler<ThrowExceptionResponse> handler = new CallbackHandler<ThrowExceptionResponse>();
        Future<?> resp = port.throwExceptionAsync(ExceptionTypeEnum.WSE,
                handler);

        AsyncClient.waitBlocking(resp);
        try {
            handler.get();

            fail("ExecutionException expected at Response.get when ednpoint throws an exception");
        } catch (ExecutionException ee) {
            // Constants.logStack(ee);

            assertTrue(
                    "ExecutionException.getCause should be an instance of WebServiceException",
                    ee.getCause() instanceof WebServiceException);

            /*
             * TODO: REVIEW. Original test was written expecting a 404 from the
             * bad URL set on the requestcontext. However, this test actually
             * does make it to the endpoint, and returns the exception specified
             * in the call to port.throwExceptionAsync(ExceptionTypeEnum.WSE)
             * above...so the assert is commented until we can review it. Also,
             * different servers may behave differently, depending on how they
             * want to parse the incoming request URL.
             */
            // assertTrue("WSE.getCause should be an instance of
            // ConnectException", checkStack(ee,
            // java.nio.channels.UnresolvedAddressException.class));
        }
    }

    /**
     * @testStrategy Invoke the proxy with async-callback method, the proxy
     *               throws a generic WebServiceException. I think we may have
     *               the record for longest method name in Apache here.
     */
    @Test
    public void testAsyncCallback_asyncMEP_WebServiceException()
            throws Exception {

        AsyncPort port = getPort();

        Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                getEndpoint());
        rc.put(AddressingConstants.WSA_REPLY_TO, "blarg");

        CallbackHandler<ThrowExceptionResponse> handler = new CallbackHandler<ThrowExceptionResponse>();
        Future<?> resp = port.throwExceptionAsync(ExceptionTypeEnum.WSE,
                handler);

        AsyncClient.waitBlocking(resp);
        Exception e = null;
        try {
            handler.get();

            fail("ExecutionException expected at Response.get when ednpoint throws an exception");
        } catch (ExecutionException ee) {
            e = ee;
        }
        assertNotNull(e);
        assertTrue(
                "ExecutionException.getCause should be an instance of WebServiceException",
                e.getCause() instanceof SOAPFaultException);
    }

    /**
     * @testStrategy Invoke the proxy with async-callback method, the proxy
     *               throws a generic WebServiceException. I think we may have
     *               the record for longest method name in Apache here.
     */
    @Test
    public void testAsyncCallback_asyncMEP_asyncWire_Addressing_WebServiceException()
            throws Exception {
        setupAddressingAndListener();
        AsyncPort port = getPort(new AddressingFeature());

        Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                getEndpoint());
        //rc.put(AddressingConstants.WSA_REPLY_TO, AddressingConstants.Final.WSA_ANONYMOUS_URL);
        rc.put("org.apache.axis2.jaxws.use.async.mep", Boolean.TRUE);

        CallbackHandler<ThrowExceptionResponse> handler = new CallbackHandler<ThrowExceptionResponse>();
        Future<?> resp = port.throwExceptionAsync(ExceptionTypeEnum.WSE,
                handler);

        AsyncClient.waitBlocking(resp);
        Exception e = null;
        try {
            handler.get();

            fail("ExecutionException expected at Response.get when ednpoint throws an exception");
        } catch (ExecutionException ee) {
            e = ee;
        }

        assertNotNull(e);
        assertTrue(
                "ExecutionException.getCause should be an instance of WebServiceException",
                e.getCause() instanceof SOAPFaultException);
    }

    /**
     * @testStrategy Invoke the proxy with async-callback method, the endpoint
     *               will throw a wsdl:fault which should result in a
     *               EE/SimpleFault
     */
    @Test
    public void testAsyncCallback_asyncMEP_WsdlFault() throws Exception {

        AsyncPort port = getPort();
        CallbackHandler<ThrowExceptionResponse> handler = new CallbackHandler<ThrowExceptionResponse>();
        Future<?> resp = port.throwExceptionAsync(ExceptionTypeEnum.WSDL_FAULT,
                handler);

        AsyncClient.waitBlocking(resp);

        try {
            handler.get();

            fail("ExecutionException expected at Response.get when ednpoint throws an exception");
        } catch (ExecutionException ee) {
            //Constants.logStack(ee);

            assertTrue(
                    "ExecutionException.getCause should be an instance of SimpleFault",
                    ee.getCause() instanceof ThrowExceptionFault);
        }
    }

    private static boolean checkStack(Throwable t, Class find) {
        Throwable cur = t;
        boolean found = false;
        do {
            found = cur.getClass().isAssignableFrom(find);
            cur = cur.getCause();
        } while (!found && cur != null);

        return found;
    }
    
    
    /**
     * Setup to use addressing and to start a listener to receive inbound async responses
     * from the service-provider.
     * 
     * @throws Exception
     */
    synchronized private void setupAddressingAndListener() throws Exception {
        if (!listenerAlreadySetup) {
            listenerAlreadySetup = true;
            // we want to use addressing on the client side
            String repopath = System.getProperty("build.repository",
                    System.getProperty("basedir", ".") + "/target/client-repo");
            String axis2xmlpath = System.getProperty("basedir", ".")
                    + "/test-resources/axis2_addressing.xml";
            FileSystemConfigurator configurator = new FileSystemConfigurator(
                    repopath, axis2xmlpath);
            ClientConfigurationFactory factory = new ClientConfigurationFactory(
                    configurator);
            MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class,
                    factory);
        }
    }

}
