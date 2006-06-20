package org.apache.axis2.jaxws;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.CallbackHandler;
import org.apache.axis2.jaxws.framework.StartServer;
import org.apache.axis2.jaxws.framework.StopServer;
import org.xml.sax.InputSource;

public class SAXSourceDispatch extends TestCase{
	private String urlHost = "localhost";
    private String urlPort = "8080";
    private String urlSuffix = "/axis2/services/EchoService";
    private String endpointUrl = "http://" + urlHost + ":" + urlPort + urlSuffix;
	private String soapMessage ="<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns1:echoString xmlns:ns1=\"http://test\"><ns1:input xmlns=\"http://test\">HELLO THERE!!!</ns1:input></ns1:echoString></soap:Body></soap:Envelope>";
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

	public void testSync() {
		try {
			System.out.println("---------------------------------------");
			Service svc = Service.create(serviceQname);
			svc.addPort(portQname, null, null);
			Dispatch<Source> dispatch = svc.createDispatch(portQname, Source.class,
					null);
			ByteArrayInputStream stream = new ByteArrayInputStream(xmlString
					.getBytes());
			Map<String, Object> requestContext = dispatch.getRequestContext();
			InputSource input = new InputSource(stream);
			Source srcStream = new SAXSource(input);
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
					endpointUrl);
			System.out.println(">> Invoking sync Dispatch");
			SAXSource retVal = (SAXSource) dispatch.invoke(srcStream);
			assertNotNull("dispatch invoke returned null",retVal);
			byte b;
			StringBuffer buffer = new StringBuffer();
			while ((b = (byte) retVal.getInputSource().getByteStream().read()) != -1) {
				char c = (char) b;
				buffer.append(c);

			}
			System.out.println(">> Response [" + buffer + "]");
		} catch (Exception e) {
			e.printStackTrace();
            fail("[ERROR] - Dispatch invocation failed.");
		}
	}

	public void testSyncWithMessageMode(){
		try {
			System.out.println("---------------------------------------");
			Service svc = Service.create(serviceQname);
			svc.addPort(portQname, null, null);
			Dispatch<Source> dispatch = svc.createDispatch(portQname, Source.class,
					Mode.MESSAGE);
			ByteArrayInputStream stream = new ByteArrayInputStream(soapMessage.getBytes());
			Map<String, Object> requestContext = dispatch.getRequestContext();
			InputSource input = new InputSource(stream);
			Source srcStream = new SAXSource(input);
			
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
					endpointUrl);
			System.out.println(">> Invoking sync Dispatch with Message Mode");
			SAXSource retVal = (SAXSource) dispatch.invoke(srcStream);

			byte b;
			StringBuffer buffer = new StringBuffer();
			while ((b = (byte) retVal.getInputSource().getByteStream().read()) != -1) {
				char c = (char) b;
				buffer.append(c);

			}
			System.out.println(">> Response [" + buffer + "]");
		} catch (Exception e) {
			e.printStackTrace();
            fail("[ERROR] - Dispatch invocation failed.");
		}
	}

	public void testAsyncCallbackWithMessageMode() {
		
			System.out.println("---------------------------------------");
	        CallbackHandler<Source> callbackHandler = new CallbackHandler<Source>();
	        Service svc = Service.create(serviceQname);
			svc.addPort(portQname, null, null);
			Dispatch<Source> dispatch = svc.createDispatch(portQname, Source.class,
					Mode.MESSAGE);
			ByteArrayInputStream stream = new ByteArrayInputStream(soapMessage
					.getBytes());
			InputSource input = new InputSource(stream);
			Source srcStream = new SAXSource(input);
			Map<String, Object> requestContext = dispatch.getRequestContext();
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
	                endpointUrl);
			
	        System.out.println(">> Invoking async (callback) Dispatch");
	        Future<?> monitor = dispatch.invokeAsync(srcStream, callbackHandler);
	        try {
	            while (!monitor.isDone()) {
	                System.out.println(">> Async invocation still not complete");
	                Thread.sleep(1000);
	            }
	            
	            //monitor.get();
	        
	        } catch (InterruptedException e) {
	            e.printStackTrace();
                fail("[ERROR] - Dispatch invocation failed.");
	        }
	}
	/*
	public void testSyncWithWSDL() {
		try {
			System.out.println("---------------------------------------");
			 URL wsdlUrl = new URL(endpointUrl + "?wsdl");
			Service svc = Service.create(wsdlUrl,serviceQname);
			svc.addPort(portQname, null, null);
			Dispatch<Source> dispatch = svc.createDispatch(portQname, Source.class,
					Mode.PAYLOAD);
			ByteArrayInputStream stream = new ByteArrayInputStream(xmlString
					.getBytes());
			Map<String, Object> requestContext = dispatch.getRequestContext();
			
			String url = (String) requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            assertNotNull("ERROR: the URL should not be null", url);
            
            InputSource input = new InputSource(stream);
			Source srcStream = new SAXSource(input);
			System.out.println(">> URL from WSDL [" + url + "]");
			System.out.println(">> Invoking sync Dispatch with WSDL, Service and Port");
			SAXSource retVal = (SAXSource) dispatch.invoke(srcStream);
			byte b;
			StringBuffer buffer = new StringBuffer();
			while ((b = (byte) retVal.getInputSource().getByteStream().read()) != -1) {
				char c = (char) b;
				buffer.append(c);

			}
			System.out.println(">> Response [" + buffer + "]");
		} catch (Exception e) {
			e.printStackTrace();
            fail("[ERROR] - Dispatch invocation failed.");
		}
		 
	}
	*/
  
	public void testAsyncCallback() {
		System.out.println("---------------------------------------");
        CallbackHandler<Source> callbackHandler = new CallbackHandler<Source>();
        Service svc = Service.create(serviceQname);
		svc.addPort(portQname, null, null);
		Dispatch<Source> dispatch = svc.createDispatch(portQname, Source.class,
				null);
		ByteArrayInputStream stream = new ByteArrayInputStream(xmlString
				.getBytes());
		InputSource input = new InputSource(stream);
		Source srcStream = new SAXSource(input);
		Map<String, Object> requestContext = dispatch.getRequestContext();
		requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpointUrl);
		
        System.out.println(">> Invoking async (callback) Dispatch");
        Future<?> monitor = dispatch.invokeAsync(srcStream, callbackHandler);
        try {
            while (!monitor.isDone()) {
                System.out.println(">> Async invocation still not complete");
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("[ERROR] - Dispatch invocation failed.");
        }
	}

	public void testOneWay() {
		try {
			System.out.println("---------------------------------------");
			Service svc = Service.create(serviceQname);
			svc.addPort(portQname, null, null);
			Dispatch<Source> dispatch = svc.createDispatch(portQname, Source.class,
					null);
			ByteArrayInputStream stream = new ByteArrayInputStream(xmlString
					.getBytes());
			Map<String, Object> requestContext = dispatch.getRequestContext();
			InputSource input = new InputSource(stream);
			Source srcStream = new SAXSource(input);
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
					endpointUrl);
			System.out.println(">> Invoking One Way Dispatch");
			dispatch.invokeOneWay(srcStream);
		} catch (Exception e) {
			e.printStackTrace();
            fail("[ERROR] - Dispatch invocation failed.");
		}
	}
}
