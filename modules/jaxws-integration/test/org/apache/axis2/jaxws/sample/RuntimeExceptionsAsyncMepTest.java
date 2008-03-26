//
// @(#) 1.4 FVT/ws/code/websvcs.fvt/src/jaxws/async/wsfvt/test/asyncmep/RuntimeExceptionsAsyncMepTest.java, WAS.websvcs.fvt, WASX.FVT 2/16/07 12:00:23 [7/11/07 13:14:36]
//
// IBM Confidential OCO Source Material
// (C) COPYRIGHT International Business Machines Corp. 2006
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
//
// Change History:
// Date     UserId      Defect          Description
// ----------------------------------------------------------------------------
// 01/10/07 sedov       413290          New File
// 02/07/07 sedov       419445          Update callback tests to use handler
// 02/16/07 sedov       420835          Updated WSDL Fault test

package org.apache.axis2.jaxws.sample;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.sample.asyncdoclit.client.AsyncClient;
import org.apache.axis2.jaxws.sample.asyncdoclit.client.AsyncPort;
import org.apache.axis2.jaxws.sample.asyncdoclit.client.AsyncService;
import org.apache.axis2.jaxws.sample.asyncdoclit.common.CallbackHandler;
import org.apache.axis2.jaxws.sample.asyncdoclit.client.ThrowExceptionFault;
import org.test.asyncdoclit.ExceptionTypeEnum;
import org.test.asyncdoclit.ThrowExceptionResponse;

/**
 * Test for varios async exceptions whern AsyncMEP is enabled
 */
public class RuntimeExceptionsAsyncMepTest extends AbstractTestCase { //com.ibm.ws.wsfvt.test.framework.FVTTestCase {
	
	private static final String DOCLITWR_ASYNC_ENDPOINT =
        "http://localhost:6060/axis2/services/AsyncService2.DocLitWrappedPortImplPort";
	
	private static final String ASYNC_MEP_PROPERTY = AsyncClient.ASYNC_MEP_PROPERTY;
	
	static final String CONNECT_404_ENDPOINT = DOCLITWR_ASYNC_ENDPOINT //Constants.DOCLITWR_ASYNC_ENDPOINT
			+ "/DoesNotExist";

	static final String HOST_NOT_FOUND_ENDPOINT = "http://this.endpoint.does.not.exist/nope";

    public static Test suite() {
        return getTestSetup(new TestSuite(RuntimeExceptionsAsyncMepTest.class));
    }
	
    private AsyncPort getPort() {

    	AsyncService service = new AsyncService();
        AsyncPort port = service.getAsyncPort();
        assertNotNull("Port is null", port);

        Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                DOCLITWR_ASYNC_ENDPOINT);
        
        return port;
    }

	/**
	 * @testStrategy Invoke the proxy with async-polling method, the proxy is
	 *               configured against an endpoint which does not exist (this
	 *               is a server not found case). Expected to throw a
	 *               EE/WSE/UnknownHostException
	 */
	public void testAsyncPolling_asyncMEP_UnknwonHost() throws Exception {
		
        AsyncPort port = getPort();
		
		Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
		rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				HOST_NOT_FOUND_ENDPOINT);

		Response<ThrowExceptionResponse> resp = port.throwExceptionAsync(ExceptionTypeEnum.WSE);
		
		AsyncClient.waitBlocking(resp);
		try {
			resp.get();

			fail("ExecutionException expected at invoke time when an invalid endpoint address is specified");
		} catch (ExecutionException ee) {
			//Constants.logStack(ee);

			assertTrue("EE.getCause must be WebServiceException", ee.getCause() instanceof WebServiceException);

			assertTrue("WSE.getCause must be UnknownHostException", checkStack(ee, UnknownHostException.class));
		}
	}

	/**
	 * @testStrategy Invoke the proxy with async-polling method, the proxy is
	 *               configured against an endpoint which does not exist (this
	 *               is a 404-Not Found case). Expected to throw a EE/WSE
	 */
	public void testAsyncPolling_asyncMEP_404NotFound() throws Exception {
		
		AsyncPort port = getPort();
		Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
		rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, CONNECT_404_ENDPOINT);
		Response<ThrowExceptionResponse> resp = port.throwExceptionAsync(ExceptionTypeEnum.WSE);

		AsyncClient.waitBlocking(resp);
		try {
			resp.get();

			fail("ExecutionException expected at invoke time when an invalid endpoint address is specified");
		} catch (ExecutionException ee) {
			//Constants.logStack(ee);

			assertTrue("EE.getCause must be WebServiceException",
					ee.getCause() instanceof WebServiceException);

			/*
			 * TODO: REVIEW.  Original test was written expecting a 404 from the bad URL set on the requestcontext.
			 * However, this test actually does make it to the endpoint, and returns the exception specified in
			 * the call to port.throwExceptionAsync(ExceptionTypeEnum.WSE) above...so the assert is commented until
			 * we can review it.  Also, different servers may behave differently, depending on how they want to
			 * parse the incoming request URL.
			 */
			//assertTrue("WSE.getCause must be 404", checkStack(ee, java.net.ConnectException.class));
		}
	}

	/**
	 * @testStrategy Invoke the proxy with async-polling method, the endpoint
	 *               will throw a WSE which should result in a
	 *               EE/SOAPFaultException
	 */
	public void testAsyncPolling_asyncMEP_WebServiceException() throws Exception {
		
		AsyncPort port = getPort();
		Response<ThrowExceptionResponse> resp = port
				.throwExceptionAsync(ExceptionTypeEnum.WSE);

		AsyncClient.waitBlocking(resp);
		try {
			resp.get();
			fail("ExecutionException expected at Response.get when ednpoint throws an exception");
		} catch (ExecutionException ee) {
			//Constants.logStack(ee);

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
	public void testAsyncPolling_asyncMEP_WsdlFault() throws Exception{
		
		AsyncPort port = getPort();
		Response<ThrowExceptionResponse> resp = port
				.throwExceptionAsync(ExceptionTypeEnum.WSDL_FAULT);

		AsyncClient.waitBlocking(resp);
		try {
			resp.get();
			fail("ExecutionException expected at Response.get when ednpoint throws an exception");
		} catch (ExecutionException ee) {
			//Constants.logStack(ee);

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
	public void testAsyncCallback_asyncMEP_UnknownHost() throws Exception {

		AsyncPort port = getPort();
		Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
		rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				HOST_NOT_FOUND_ENDPOINT);

		CallbackHandler<ThrowExceptionResponse> handler = new CallbackHandler<ThrowExceptionResponse>();
		Future<?> resp = port.throwExceptionAsync(ExceptionTypeEnum.WSE, handler);
		
		AsyncClient.waitBlocking(resp);
		try {
			handler.get();

			fail("ExecutionException expected at invoke time when an invalid endpoint address is specified");
		} catch (ExecutionException ee) {
			//Constants.logStack(ee);

			assertTrue(
					"ExecutionException.getCause should be an instance of WebServiceException",
					ee.getCause() instanceof WebServiceException);

			assertTrue("WSE.getCause must be UnknownHostException", checkStack(ee, UnknownHostException.class));

		}
	}

	/**
	 * @testStrategy Invoke the proxy with async-callback method, the proxy is
	 *               configured against an endpoint which does not exist (this
	 *               is a 404 Not Found case). Expected to throw a
	 *               EE/WSE/UnknownHostException
	 */
	public void testAsyncCallback_asyncMEP_404NotFound() throws Exception {
		
		AsyncPort port = getPort();
		Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
		rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, CONNECT_404_ENDPOINT);

		CallbackHandler<ThrowExceptionResponse> handler = new CallbackHandler<ThrowExceptionResponse>();
		Future<?> resp = port.throwExceptionAsync(ExceptionTypeEnum.WSE, handler);
		
		AsyncClient.waitBlocking(resp);
		try {			
			handler.get();

			fail("ExecutionException expected at Response.get when ednpoint throws an exception");
		} catch (ExecutionException ee) {
			//Constants.logStack(ee);

			assertTrue(
					"ExecutionException.getCause should be an instance of WebServiceException",
					ee.getCause() instanceof WebServiceException);

			/*
			 * TODO: REVIEW.  Original test was written expecting a 404 from the bad URL set on the requestcontext.
			 * However, this test actually does make it to the endpoint, and returns the exception specified in
			 * the call to port.throwExceptionAsync(ExceptionTypeEnum.WSE) above...so the assert is commented until
			 * we can review it.  Also, different servers may behave differently, depending on how they want to
			 * parse the incoming request URL.
			 */
			//assertTrue("WSE.getCause should be an instance of ConnectException", checkStack(ee, java.nio.channels.UnresolvedAddressException.class));
		}
	}

	/**
	 * @testStrategy Invoke the proxy with async-callback method, the proxy
	 *               throws a generic WebServiceException
	 */
	public void testAsyncCallback_asyncMEP_WebServiceException() throws Exception {
		
		AsyncPort port = getPort();
		
		Map<String, Object> rc = ((BindingProvider) port).getRequestContext();
        rc.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                DOCLITWR_ASYNC_ENDPOINT);
        rc.put(AddressingConstants.WSA_REPLY_TO, "blarg");
        
		CallbackHandler<ThrowExceptionResponse> handler = new CallbackHandler<ThrowExceptionResponse>();
		Future<?> resp = port.throwExceptionAsync(ExceptionTypeEnum.WSE, handler);

		AsyncClient.waitBlocking(resp);
		try {
			handler.get();

			fail("ExecutionException expected at Response.get when ednpoint throws an exception");
		} catch (ExecutionException ee) {
			//Constants.logStack(ee);

			assertTrue(
						"ExecutionException.getCause should be an instance of WebServiceException",
						ee.getCause() instanceof SOAPFaultException);
		}
	}

	/**
	 * @testStrategy Invoke the proxy with async-callback method, the endpoint
	 *               will throw a wsdl:fault which should result in a
	 *               EE/SimpleFault
	 */
	public void testAsyncCallback_asyncMEP_WsdlFault() throws Exception{
		
		AsyncPort port = getPort();
		CallbackHandler<ThrowExceptionResponse> handler = new CallbackHandler<ThrowExceptionResponse>();
		Future<?> resp = port.throwExceptionAsync(ExceptionTypeEnum.WSDL_FAULT, handler);

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
	
	private static boolean checkStack(Throwable t, Class find){		
		Throwable cur = t;
		boolean found = false;
		do {
			found = cur.getClass().isAssignableFrom(find);
			cur = cur.getCause();
		} while (!found && cur != null);
		
		return found;
	}
}
