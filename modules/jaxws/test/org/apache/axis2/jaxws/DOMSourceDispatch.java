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
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.CallbackHandler;
import org.w3c.dom.Document;

public class DOMSourceDispatch extends TestCase{
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
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
			Document domTree = domBuilder.parse(stream);
			DOMSource srcStream = new DOMSource(domTree);
			
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
					endpointUrl);
			System.out.println(">> Invoking sync Dispatch");
			DOMSource retVal = (DOMSource) dispatch.invoke(srcStream);
			
			assertNotNull("dispatch invoke returned null",retVal);
			
			StringWriter writer = new StringWriter();
			Transformer trasformer = TransformerFactory.newInstance().newTransformer();
			Result result = new StreamResult(writer);
			trasformer.transform(retVal, result);
			System.out.println(">> Response [" + writer.getBuffer().toString() + "]");
		} catch (Exception e) {
			e.printStackTrace();
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
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
			Document domTree = domBuilder.parse(stream);
			DOMSource srcStream = new DOMSource(domTree);
			
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
					endpointUrl);
			System.out.println(">> Invoking sync Dispatch with Message Mode");
			DOMSource retVal = (DOMSource) dispatch.invoke(srcStream);
			
			assertNotNull("dispatch invoke returned null",retVal);
			
			StringWriter writer = new StringWriter();
			Transformer trasformer = TransformerFactory.newInstance().newTransformer();
			Result result = new StreamResult(writer);
			trasformer.transform(retVal, result);
			System.out.println(">> Response [" + writer.getBuffer().toString() + "]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testAsyncCallbackWithMessageMode() {
		try {
			System.out.println("---------------------------------------");
	        CallbackHandler<Source> callbackHandler = new CallbackHandler<Source>();
	        Service svc = Service.create(serviceQname);
			svc.addPort(portQname, null, endpointUrl);
			Dispatch<Source> dispatch = svc.createDispatch(portQname, Source.class,
					Mode.MESSAGE);
			ByteArrayInputStream stream = new ByteArrayInputStream(soapMessage
					.getBytes());
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
			Document domTree = domBuilder.parse(stream);
			DOMSource srcStream = new DOMSource(domTree);
			
			Map<String, Object> requestContext = dispatch.getRequestContext();
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
	                endpointUrl);
			
	        System.out.println(">> Invoking async (callback) Dispatch");
	        Future<?> monitor = dispatch.invokeAsync(srcStream, callbackHandler);
	        
	            while (!monitor.isDone()) {
	                System.out.println(">> Async invocation still not complete");
	                Thread.sleep(1000);
	            }
	            
	            //monitor.get();
	        
	        } catch (Exception e) {
	            e.printStackTrace();
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
            
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
			Document domTree = domBuilder.parse(stream);
			DOMSource srcStream = new DOMSource(domTree);
			System.out.println(">> URL from WSDL [" + url + "]");
			System.out.println(">> Invoking sync Dispatch with WSDL, Service and Port");
			DOMSource retVal = (DOMSource) dispatch.invoke(srcStream);

			StringWriter writer = new StringWriter();
			Transformer trasformer = TransformerFactory.newInstance().newTransformer();
			Result result = new StreamResult(writer);
			trasformer.transform(retVal, result);
			System.out.println(">> Response [" + writer.getBuffer().toString() + "]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	public void testAsyncCallback() {
		try{
			System.out.println("---------------------------------------");
	        CallbackHandler<Source> callbackHandler = new CallbackHandler<Source>();
	        Service svc = Service.create(serviceQname);
			svc.addPort(portQname, null, endpointUrl);
			Dispatch<Source> dispatch = svc.createDispatch(portQname, Source.class,
					null);
			ByteArrayInputStream stream = new ByteArrayInputStream(xmlString
					.getBytes());
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
			Document domTree = domBuilder.parse(stream);
			DOMSource srcStream = new DOMSource(domTree);
			Map<String, Object> requestContext = dispatch.getRequestContext();
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
	                endpointUrl);
			
	        System.out.println(">> Invoking async (callback) Dispatch");
	        Future<?> monitor = dispatch.invokeAsync(srcStream, callbackHandler);
	       
	        while (!monitor.isDone()) {
	            System.out.println(">> Async invocation still not complete");
	            Thread.sleep(1000);
	        }
	            
	            //monitor.get();
        
        } catch (Exception e) {
            e.printStackTrace();
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
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
			Document domTree = domBuilder.parse(stream);
			DOMSource srcStream = new DOMSource(domTree);
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
					endpointUrl);
			System.out.println(">> Invoking One Way Dispatch");
			dispatch.invokeOneWay(srcStream);

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
