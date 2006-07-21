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
package org.apache.axis2.proxy;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.proxy.doclitwrapped.sei.DocLitWrappedProxy;
import org.test.proxy.doclitwrapped.ReturnType;


public class ProxyTests extends TestCase {
	private QName serviceName = new QName(
			"http://org.apache.axis2.proxy.doclitwrapped", "ProxyDocLitWrappedService");
	private String wasEndpoint = "http://localhost:9081/axis2/services/ProxyDocLitWrappedService";
	private String axisEndpoint = "http://localhost:8080/axis2/services/ProxyDocLitWrappedService";
	private QName portName = new QName("http://org.apache.axis2.proxy.doclitwrapped",
			"ProxyDocLitWrappedPort");
	private String wsdlLocation = "modules/jaxws/test/org/apache/axis2/jaxws/proxy/doclitwrapped/META-INF/ProxyDocLitWrapped.wsdl";
	private boolean runningOnAxis = true;
	
	public void testInvoke(){
		try{ 
			if(!runningOnAxis){
				return;
			}
			System.out.println("---------------------------------------");
			
			File wsdl= new File(wsdlLocation); 
			URL wsdlUrl = wsdl.toURL(); 
			Service service = Service.create(null, serviceName);
			//StockSymbol ss = new StockSymbol(); 
			String request = new String("some string request"); 
			//ss.setSymbol("IBM"); 
			Object proxy =service.getPort(portName, DocLitWrappedProxy.class);
			System.out.println(">>Invoking Binding Provider property");
			BindingProvider p =	(BindingProvider)proxy;
				p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
				
			DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;
			System.out.println(">> Invoking Proxy");
			String response = dwp.invoke(request);
			System.out.println("Proxy Response =" + response);
			System.out.println("---------------------------------------");
		}catch(Exception e){ 
			e.printStackTrace(); 
		}
	}
	
	public void testTwoWay(){
		try{ 
			if(runningOnAxis){
				return;
			}
			File wsdl= new File(wsdlLocation); 
			URL wsdlUrl = wsdl.toURL(); 
			Service service = Service.create(null, serviceName);
			//StockSymbol ss = new StockSymbol(); 
			String request = new String("some string request"); 
			//ss.setSymbol("IBM"); 
			Object proxy =service.getPort(portName, DocLitWrappedProxy.class); 
			BindingProvider p =	(BindingProvider)proxy;
				p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,wasEndpoint);
				
			DocLitWrappedProxy dwp = (DocLitWrappedProxy)proxy;  
			String response = dwp.twoWay(request);
			System.out.println("Response =" + response);
		}catch(Exception e){ 
			e.printStackTrace(); 
		}
	}
	
	public void testOneWay(){
		
	}
	
	public void testHolder(){
		
	}
	
	public void testAsyncCallback(){
		
	}
}
