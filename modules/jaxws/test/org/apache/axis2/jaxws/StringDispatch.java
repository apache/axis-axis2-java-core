package org.apache.axis2.jaxws;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;

import org.apache.axis2.jaxws.CallbackHandler;
import org.apache.axis2.jaxws.framework.StartServer;
import org.apache.axis2.jaxws.framework.StopServer;

import junit.framework.TestCase;

public class StringDispatch extends TestCase {

	private String urlHost = "localhost";
    private String urlPort = "8080";
    private String urlSuffix = "/axis2/services/EchoService";
    private String endpointUrl = "http://" + urlHost + ":" + urlPort + urlSuffix;
    private String soapMessage ="<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" ><soap:Body><ns1:echoString xmlns:ns1=\"http://test\"><ns1:input xmlns=\"http://test\">HELLO THERE!!!</ns1:input></ns1:echoString></soap:Body></soap:Envelope>";
    private String xmlString = "<ns1:echoString xmlns:ns1=\"http://test\"><ns1:input xmlns=\"http://test\">HELLO THERE!!!</ns1:input></ns1:echoString>";
    private QName serviceQname = new QName("http://ws.apache.org/axis2", "EchoService");
    private QName portQname = new QName("http://ws.apache.org/axis2", "EchoServiceSOAP11port0");
    
    // FIXME: The server should not be restarted each time
	protected void setUp() throws Exception {
		super.setUp();
		StartServer startServer = new StartServer("server1");
		startServer.testStartServer();
	}

	// FIXME: The server should not be restarted each time
	protected void tearDown() throws Exception {
		super.tearDown();
		StopServer stopServer = new StopServer("server1");
		stopServer.testStopServer();
	}
    
    /*
     * Invoke a sync Dispatch with no WSDL configuration
     */
    public void testSync() {
        System.out.println("---------------------------------------");
        Service svc = Service.create(serviceQname);
        svc.addPort(portQname,null,null);
        try{
			Dispatch<String> dispatch = svc
					.createDispatch(portQname, String.class, null);
	        
			Map<String, Object> requestContext = dispatch.getRequestContext();
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
			 		endpointUrl);
			System.out.println(">> Invoking sync Dispatch");
	        String retVal = dispatch.invoke(xmlString);
	        System.out.println(">> Response [" + retVal + "]");
        }catch(WebServiceException e){
        	e.printStackTrace();
          fail(e.toString());
        }
	}
    
    
    
    /*
     * Invoke a sync Dispatch using the remote WSDL document to provide
     * configuration
     */
    /*
    public void testSyncWithWSDL() {
        System.out.println("---------------------------------------");
        try {
            URL wsdlUrl = new URL(endpointUrl + "?wsdl");
            // TODO (NLG): Also add a failing test if the service qname is null
            Service svc = Service.create(wsdlUrl, serviceQname);
            Dispatch<String> dispatch = svc.createDispatch(portQname, String.class, Service.Mode.PAYLOAD);
            
            Map<String, Object> requestContext = dispatch.getRequestContext();
            String url = (String) requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            assertNotNull("ERROR: the URL should not be null", url);
            
            assertTrue("ERROR: incorrect WSDL URL", url.endsWith(urlSuffix));
            
            System.out.println(">> URL from WSDL [" + url + "]");
            System.out.println(">> Invoking sync Dispatch");
            String retVal = dispatch.invoke(xmlString);
            System.out.println(">> Response [" + retVal + "]");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch(WebServiceException e){
        	e.printStackTrace();
        }
    }
    */
    /*
     * Invoke a sync Dispatch with Message Mode configuration
     */
    public void testSyncWithMessageMode() {
    	System.out.println("---------------------------------------");
    	try{
        Service svc = Service.create(serviceQname);
        svc.addPort(portQname, null, endpointUrl);
		Dispatch<String> dispatch = svc
				.createDispatch(portQname, String.class, Mode.MESSAGE);
		Map<String, Object> requestContext = dispatch.getRequestContext();
		//requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
		// 		endpointUrl);
		System.out.println(">> Invoking sync Dispatch with message mode");
        String retVal = dispatch.invoke(soapMessage);
        System.out.println(">> Response [" + retVal + "]");
    	}catch(WebServiceException e){
    		e.printStackTrace();
    		fail(e.toString());
    	}
	}
    
    /*
    //Not for Alpha Use
	public void testAsyncPooling() {
		Service svc = Service.create(null);
		String xmlString = "<test:getBookPrice xmlns:test=\"http://BookPrice/\"><test:ISBN>10</test:ISBN></test:getBookPrice>";
		Dispatch<String> dispatch = svc
				.createDispatch(null, String.class, null);
		Map<String, Object> requestContext = dispatch.getRequestContext();
		requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
				"http://localhost:8080/axis2/services/BookPrice");
		Response res = dispatch.invokeAsync(xmlString);
		try {
			while (!res.isDone()) {
				Thread.sleep(10);
			}
			String retVal = (String) res.get();
			System.out.println(retVal);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
    */

	/*
     * Invoke a Dispatch using the async callback API and use the 
     * same thread to wait for the response.
	 */
    public void testAsyncCallback() {
    
        System.out.println("---------------------------------------");
        try{
	        CallbackHandler<String> callbackHandler = new CallbackHandler<String>();
			    Service svc = Service.create(serviceQname);
			    svc.addPort(portQname, null, null);
			    Dispatch<String> dispatch = svc
					  .createDispatch(portQname, String.class, null);
			    Map<String, Object> requestContext = dispatch.getRequestContext();
			    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
			        endpointUrl);
			
	        System.out.println(">> Invoking async (callback) Dispatch");
	        Future<?> monitor = dispatch.invokeAsync(xmlString, callbackHandler);
	        
	        while (!monitor.isDone()) {
	             System.out.println(">> Async invocation still not complete");
	             Thread.sleep(1000);
	        }
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }catch(WebServiceException e){
        	e.printStackTrace();
        	fail(e.toString());
        }
	}
    
    /*
     * Invoke a Async Dispatch with Message Mode configuration
     */
    public void testAsyncCallbackWithMessageMode() {
    	 System.out.println("---------------------------------------");
    	 try{
	        CallbackHandler<String> callbackHandler = new CallbackHandler<String>();
	 		Service svc = Service.create(serviceQname);
	 		svc.addPort(portQname, null, null);
	 		Dispatch<String> dispatch = svc
	 				.createDispatch(portQname, String.class, Mode.MESSAGE);
	 		Map<String, Object> requestContext = dispatch.getRequestContext();
	 		requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
	                 endpointUrl);
	 		
	        System.out.println(">> Invoking async (callback) Dispatch with Message Mode");
	        Future<?> monitor = dispatch.invokeAsync(soapMessage, callbackHandler);
	    
            while (!monitor.isDone()) {
                System.out.println(">> Async invocation still not complete");
                Thread.sleep(1000);
            }
         } catch (InterruptedException e) {
             e.printStackTrace();
             fail();
         }catch(WebServiceException e){
        	 e.printStackTrace();
        	 fail();
         }
	}
	
	/*
     * Invoke a Dispatch one-way 
	 */
    public void testOneWay(){
        System.out.println("---------------------------------------");
        try{
	        Service svc = Service.create(serviceQname);
	        svc.addPort(portQname, null, null);
	        Dispatch<String> dispatch = svc
					.createDispatch(portQname, String.class, null);
			Map<String, Object> requestContext = dispatch.getRequestContext();
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
					endpointUrl);
			System.out.println(">> Invoking one-way Dispatch");
	        dispatch.invokeOneWay(xmlString);
        }catch(WebServiceException e){
        	e.printStackTrace();
        	fail();
        }
	}
}
