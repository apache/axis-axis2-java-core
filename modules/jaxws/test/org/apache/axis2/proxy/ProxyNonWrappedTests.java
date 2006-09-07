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

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.apache.axis2.jaxws.proxy.doclitnonwrapped.sei.DocLitnonWrappedProxy;
import org.test.proxy.doclitnonwrapped.Invoke;
import org.test.proxy.doclitnonwrapped.ObjectFactory;
import org.test.proxy.doclitnonwrapped.ReturnType;


import junit.framework.TestCase;

/**
 * This test cases will use proxy NON wrapped wsdl to invoke methods
 * on a deployed Server Endpoint.
 */
public class ProxyNonWrappedTests extends TestCase {

	QName serviceName = new QName("http://doclitnonwrapped.test.org", "ProxyDocLitUnwrappedService");
	private String axisEndpoint = "http://localhost:8080/axis2/services/ProxyDocLitUnwrappedService";
	private QName portName = new QName("http://org.apache.axis2.proxy.doclitwrapped", "ProxyDocLitWrappedPort");
	public ProxyNonWrappedTests() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public ProxyNonWrappedTests(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	public void testInvoke(){
		System.out.println("-----------------------------------");
		System.out.println("test: " + getName());
		System.out.println(">>Testing Sync Inovoke on Proxy DocLit non-wrapped");
		ObjectFactory factory = new ObjectFactory();
		Invoke invokeObj = factory.createInvoke();
		invokeObj.setInvokeStr("test request for twoWay Operation");
		Service service = Service.create(null, serviceName);
		assertNotNull(service);
		DocLitnonWrappedProxy proxy = service.getPort(portName, DocLitnonWrappedProxy.class);
		assertNotNull(proxy);
		BindingProvider p =	(BindingProvider)proxy;
		p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
		ReturnType response = proxy.invoke(invokeObj);
		assertNotNull(response);
		System.out.println(">>Response =" +response.getReturnStr());
		
		System.out.println("-------------------------------------");
	}
	
	public void testInvokeAsyncCallback(){
		
	}
	
	public void testInvokeAsyncPolling(){
		
	}

}
