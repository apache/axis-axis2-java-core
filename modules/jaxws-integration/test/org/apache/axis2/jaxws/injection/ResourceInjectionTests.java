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
package org.apache.axis2.jaxws.injection;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceContext;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.context.WebServiceContextImpl;
import org.apache.axis2.jaxws.framework.StartServer;
import org.apache.axis2.jaxws.framework.StopServer;
import org.apache.axis2.jaxws.resourceinjection.sei.ResourceInjectionPortType;
import org.apache.axis2.jaxws.resourceinjection.sei.ResourceInjectionService;
import org.apache.axis2.jaxws.server.endpoint.injection.ResourceInjector;
import org.apache.axis2.jaxws.server.endpoint.injection.factory.ResourceInjectionFactory;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.log4j.BasicConfigurator;

public class ResourceInjectionTests extends TestCase {
    String axisEndpoint = "http://localhost:6060/axis2/services/ResourceInjectionService.ResourceInjectionPortTypeImplPort";

	private Object resource = new WebServiceContextImpl();
	
	public ResourceInjectionTests() {
		super();
		// TODO Auto-generated constructor stub
	}

	static {
        BasicConfigurator.configure();
    }

    public void setUp() {
    	TestLogger.logger.debug("Starting the server for: " +this.getClass().getName());
    	StartServer startServer = new StartServer("server1");
    	startServer.testStartServer();
    }
    
    public void tearDown() {
    	TestLogger.logger.debug("Stopping the server for: " +this.getClass().getName());
    	StopServer stopServer = new StopServer("server1");
    	stopServer.testStopServer();
    }
    	

	public void testInjectionOnField(){
		Object serviceInstance = new ResourceInjectionTestImpl1();
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		try{
			ResourceInjector injector = ResourceInjectionFactory.createResourceInjector(WebServiceContext.class);
			injector.inject(resource, serviceInstance);
			ResourceInjectionTestImpl1 serviceImpl =(ResourceInjectionTestImpl1)serviceInstance;
			assertNotNull(serviceImpl.ctx);
            TestLogger.logger.debug("Resource Injected on Field");
            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	
	public void testInjectionOnMethod(){
		Object serviceInstance = new ResourceInjectionTestImpl2();
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		try{
			ResourceInjector injector = ResourceInjectionFactory.createResourceInjector(WebServiceContext.class);
			injector.inject(resource, serviceInstance);
			ResourceInjectionTestImpl2 serviceImpl =(ResourceInjectionTestImpl2)serviceInstance;
			assertNotNull(serviceImpl.ctx);
            TestLogger.logger.debug("Resource Injected on Method");
            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	
	public void testInjectionOnPrivateField(){
		Object serviceInstance = new ResourceInjectionTestImpl3();
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		try{
			ResourceInjector injector = ResourceInjectionFactory.createResourceInjector(WebServiceContext.class);
			injector.inject(resource, serviceInstance);
			ResourceInjectionTestImpl3 serviceImpl =(ResourceInjectionTestImpl3)serviceInstance;
			assertNotNull(serviceImpl.getCtx());
            TestLogger.logger.debug("Resource Injected on Private Field");
            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	
	public void testInjectionOnProvateMethod(){
		Object serviceInstance = new ResourceInjectionTestImpl4();
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		try{
			ResourceInjector injector = ResourceInjectionFactory.createResourceInjector(WebServiceContext.class);
			injector.inject(resource, serviceInstance);
			ResourceInjectionTestImpl4 serviceImpl =(ResourceInjectionTestImpl4)serviceInstance;
			assertNotNull(serviceImpl.getCtx());
            TestLogger.logger.debug("Resource Injected using private Method");
            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	
	public void testTypedResourceInjectionOnField(){
		Object serviceInstance = new ResourceInjectionTestImpl5();
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		try{
			ResourceInjector injector = ResourceInjectionFactory.createResourceInjector(WebServiceContext.class);
			injector.inject(resource, serviceInstance);
			ResourceInjectionTestImpl5 serviceImpl =(ResourceInjectionTestImpl5)serviceInstance;
			assertNotNull(serviceImpl.ctx);
            TestLogger.logger.debug("Resource Injected on Field");
            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}

	
    public void testEchoWithResourceInjectionAndLifecycleMethods(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		try{
			ResourceInjectionService service = new ResourceInjectionService();
			ResourceInjectionPortType proxy = service.getResourceInjectionPort();
	        BindingProvider p = (BindingProvider) proxy;
	        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);

			String response = proxy.echo("echo Request");
            TestLogger.logger.debug("Response String = " + response);
            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
   
}
