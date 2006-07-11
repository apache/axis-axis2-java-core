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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import org.apache.axis2.jaxws.CallbackHandler;

import junit.framework.TestCase;

public class SourceDispatch extends TestCase {
	private String urlHost = "localhost";
    private String urlPort = "8080";
    private String urlSuffix = "/axis2/services/EchoService";
    private String endpointUrl = "http://" + urlHost + ":" + urlPort + urlSuffix;
	private String soapMessage ="<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns1:echoString xmlns:ns1=\"http://test\"><ns1:input xmlns=\"http://test\">HELLO THERE!!!</ns1:input></ns1:echoString></soap:Body></soap:Envelope>";
	private String xmlString = "<ns1:echoString xmlns:ns1=\"http://test\"><ns1:input xmlns=\"http://test\">HELLO THERE!!!</ns1:input></ns1:echoString>";
	private QName serviceQname = new QName("http://ws.apache.org/axis2", "EchoService");
	private QName portQname = new QName("http://ws.apache.org/axis2", "EchoServiceSOAP11port0");

  public void testSync() {
		try {
			System.out.println("---------------------------------------");
			Service svc = Service.create(serviceQname);
			svc.addPort(portQname, null, endpointUrl);
			Dispatch<Source> dispatch = svc.createDispatch(portQname, Source.class,
					null);
			ByteArrayInputStream stream = new ByteArrayInputStream(xmlString
					.getBytes());
			Map<String, Object> requestContext = dispatch.getRequestContext();
			Source srcStream = new StreamSource((InputStream) stream);
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
					endpointUrl);
			System.out.println(">> Invoking sync Dispatch");
			StreamSource retVal = (StreamSource) dispatch.invoke(srcStream);

			byte b;
			StringBuffer buffer = new StringBuffer();
			while ((b = (byte) retVal.getInputStream().read()) != -1) {
				char c = (char) b;
				buffer.append(c);

			}
			System.out.println(">> Response [" + buffer + "]");
		} catch (Exception e) {
			e.printStackTrace();
            fail("[ERROR] - Dispatch invocation failed.");
		}
	}

	// Not for Alpha
	public void testAsyncPooling() {

	}
	
	public void testSyncWithMessageMode(){
		try {
			System.out.println("---------------------------------------");
			Service svc = Service.create(serviceQname);
			svc.addPort(portQname, null, endpointUrl);
			Dispatch<Source> dispatch = svc.createDispatch(portQname, Source.class,
					Mode.MESSAGE);
			
			ByteArrayInputStream stream = new ByteArrayInputStream(soapMessage.getBytes());
			Map<String, Object> requestContext = dispatch.getRequestContext();
			Source srcStream = new StreamSource((InputStream) stream);
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
					endpointUrl);
			System.out.println(">> Invoking sync Dispatch with Message Mode");
			StreamSource retVal = (StreamSource) dispatch.invoke(srcStream);

			byte b;
			StringBuffer buffer = new StringBuffer();
			while ((b = (byte) retVal.getInputStream().read()) != -1) {
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
			svc.addPort(portQname, null, endpointUrl);
			Dispatch<Source> dispatch = svc.createDispatch(portQname, Source.class,
					Mode.MESSAGE);
			ByteArrayInputStream stream = new ByteArrayInputStream(soapMessage
					.getBytes());
			Source srcStream = new StreamSource((InputStream) stream);
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
			svc.addPort(portQname, null, endpointUrl);
			Dispatch<Source> dispatch = svc.createDispatch(portQname, Source.class,
					Mode.PAYLOAD);
			ByteArrayInputStream stream = new ByteArrayInputStream(xmlString
					.getBytes());
			Map<String, Object> requestContext = dispatch.getRequestContext();
			
			String url = (String) requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            assertNotNull("ERROR: the URL should not be null", url);
            
			Source srcStream = new StreamSource((InputStream) stream);
			System.out.println(">> URL from WSDL [" + url + "]");
			System.out.println(">> Invoking sync Dispatch with WSDL, Service and Port");
			StreamSource retVal = (StreamSource) dispatch.invoke(srcStream);

			byte b;
			StringBuffer buffer = new StringBuffer();
			while ((b = (byte) retVal.getInputStream().read()) != -1) {
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
		svc.addPort(portQname, null, endpointUrl);
		Dispatch<Source> dispatch = svc.createDispatch(portQname, Source.class,
				null);
		ByteArrayInputStream stream = new ByteArrayInputStream(xmlString
				.getBytes());
		Source srcStream = new StreamSource((InputStream) stream);
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

	public void testOneWay() {
		try {
			System.out.println("---------------------------------------");
			Service svc = Service.create(serviceQname);
			svc.addPort(portQname, null, endpointUrl);
			Dispatch<Source> dispatch = svc.createDispatch(portQname, Source.class,
					null);
			ByteArrayInputStream stream = new ByteArrayInputStream(xmlString
					.getBytes());
			Map<String, Object> requestContext = dispatch.getRequestContext();
			Source srcStream = new StreamSource((InputStream) stream);
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
