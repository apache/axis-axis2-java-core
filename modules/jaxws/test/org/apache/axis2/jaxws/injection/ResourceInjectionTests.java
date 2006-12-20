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
package org.apache.axis2.jaxws.injection;

import javax.xml.ws.WebServiceContext;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.context.WebServiceContextImpl;
import org.apache.axis2.jaxws.resourceinjection.sei.ResourceInjectionPortType;
import org.apache.axis2.jaxws.resourceinjection.sei.ResourceInjectionService;
import org.apache.axis2.jaxws.server.endpoint.injection.ResourceInjector;
import org.apache.axis2.jaxws.server.endpoint.injection.factory.ResourceInjectionFactory;

public class ResourceInjectionTests extends TestCase {


	private Object resource = new WebServiceContextImpl();
	public ResourceInjectionTests() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void testInjectionOnField(){
		Object serviceInstance = new ResourceInjectionTestImpl1();
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		try{
			ResourceInjector injector = ResourceInjectionFactory.createResourceInjector(WebServiceContext.class);
			injector.inject(resource, serviceInstance);
			ResourceInjectionTestImpl1 serviceImpl =(ResourceInjectionTestImpl1)serviceInstance;
			assertNotNull(serviceImpl.ctx);
			System.out.println("Resource Injected on Field");
			System.out.println("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	
	public void testInjectionOnMethod(){
		Object serviceInstance = new ResourceInjectionTestImpl2();
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		try{
			ResourceInjector injector = ResourceInjectionFactory.createResourceInjector(WebServiceContext.class);
			injector.inject(resource, serviceInstance);
			ResourceInjectionTestImpl2 serviceImpl =(ResourceInjectionTestImpl2)serviceInstance;
			assertNotNull(serviceImpl.ctx);
			System.out.println("Resource Injected on Method");
			System.out.println("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	
	public void testInjectionOnPrivateField(){
		Object serviceInstance = new ResourceInjectionTestImpl3();
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		try{
			ResourceInjector injector = ResourceInjectionFactory.createResourceInjector(WebServiceContext.class);
			injector.inject(resource, serviceInstance);
			ResourceInjectionTestImpl3 serviceImpl =(ResourceInjectionTestImpl3)serviceInstance;
			assertNotNull(serviceImpl.getCtx());
			System.out.println("Resource Injected on Private Field");
			System.out.println("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	
	public void testInjectionOnProvateMethod(){
		Object serviceInstance = new ResourceInjectionTestImpl4();
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		try{
			ResourceInjector injector = ResourceInjectionFactory.createResourceInjector(WebServiceContext.class);
			injector.inject(resource, serviceInstance);
			ResourceInjectionTestImpl4 serviceImpl =(ResourceInjectionTestImpl4)serviceInstance;
			assertNotNull(serviceImpl.getCtx());
			System.out.println("Resource Injected using private Method");
			System.out.println("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	
	public void testTypedResourceInjectionOnField(){
		Object serviceInstance = new ResourceInjectionTestImpl5();
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		try{
			ResourceInjector injector = ResourceInjectionFactory.createResourceInjector(WebServiceContext.class);
			injector.inject(resource, serviceInstance);
			ResourceInjectionTestImpl5 serviceImpl =(ResourceInjectionTestImpl5)serviceInstance;
			assertNotNull(serviceImpl.ctx);
			System.out.println("Resource Injected on Field");
			System.out.println("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}

	
    public void testEchoWithResourceInjectionAndLifecycleMethods(){
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		try{
			ResourceInjectionService service = new ResourceInjectionService();
			ResourceInjectionPortType proxy = service.getResourceInjectionPort();
			String response = proxy.echo("echo Request");
			System.out.println("Response String = " +response);
			System.out.println("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
   
}
