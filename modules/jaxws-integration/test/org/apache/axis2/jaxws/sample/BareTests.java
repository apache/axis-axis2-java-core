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

/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import javax.xml.ws.BindingProvider;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.framework.StartServer;
import org.apache.axis2.jaxws.framework.StopServer;
import org.apache.axis2.jaxws.sample.doclitbare.sei.BareDocLitService;
import org.apache.axis2.jaxws.sample.doclitbare.sei.DocLitBarePortType;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.log4j.BasicConfigurator;

public class BareTests extends TestCase {
    
	String axisEndpoint = "http://localhost:6060/axis2/services/BareDocLitService.DocLitBarePortTypeImplPort";
    
	public BareTests() {
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
    	
	public void testTwoWaySync(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		
		try{
			
			BareDocLitService service = new BareDocLitService();
			DocLitBarePortType proxy = service.getBareDocLitPort();
			 BindingProvider p = (BindingProvider) proxy;
	            p.getRequestContext().put(
	                    BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
	            p.getRequestContext().put(
	                    BindingProvider.SOAPACTION_URI_PROPERTY, "twoWaySimple");
	            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);

			String response = proxy.twoWaySimple(10);
            TestLogger.logger.debug("Sync Response =" + response);
            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
	
    public void testTwoWaySyncWithBodyRouting(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
        
        try{
            
            BareDocLitService service = new BareDocLitService();
            DocLitBarePortType proxy = service.getBareDocLitPort();
			BindingProvider p = (BindingProvider) proxy;
	        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);

            String response = proxy.twoWaySimple(10);
            TestLogger.logger.debug("Sync Response =" + response);
            TestLogger.logger.debug("------------------------------");
        }catch(Exception e){
            e.printStackTrace();
            fail();
        }
    }

    public void testOneWayEmpty(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
		
		try{
			
			BareDocLitService service = new BareDocLitService();
			DocLitBarePortType proxy = service.getBareDocLitPort();
			BindingProvider p = (BindingProvider) proxy;

			p.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY,
					Boolean.TRUE);
			p.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,
					"oneWayEmpty");
			p.getRequestContext().put(
					BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
			proxy.oneWayEmpty();

            TestLogger.logger.debug("------------------------------");
		}catch(Exception e){
			e.printStackTrace();
			fail();
		}
	}
}
