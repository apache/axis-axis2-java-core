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
package org.apache.axis2.jaxws.sample;

import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.framework.StartServer;
import org.apache.axis2.jaxws.framework.StopServer;
import org.apache.axis2.jaxws.sample.addnumbers.AddNumbersPortType;
import org.apache.axis2.jaxws.sample.addnumbers.AddNumbersService;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.log4j.BasicConfigurator;

public class AddNumbersTests extends TestCase {
	
    String axisEndpoint = "http://localhost:6060/axis2/services/AddNumbersService.AddNumbersPortTypeImplPort";

	public AddNumbersTests() {
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
    	    
    public void testAddNumbers() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());

        AddNumbersService service = new AddNumbersService();
        AddNumbersPortType proxy = service.getAddNumbersPort();

        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, axisEndpoint);
        int total = proxy.addNumbers(10, 10);

        TestLogger.logger.debug("Total =" + total);
        TestLogger.logger.debug("----------------------------------");

        assertEquals("sum", 20, total);
        assertEquals("http response code", 
                     new Integer(200), p.getResponseContext().get(MessageContext.HTTP_RESPONSE_CODE));
        Map headers = (Map) p.getResponseContext().get(MessageContext.HTTP_RESPONSE_HEADERS);
        // the map should contain some headers
        assertTrue("http response headers", headers != null && !headers.isEmpty());
    }
    
    public void testOneWay() {
        try {
            TestLogger.logger.debug("----------------------------------");
            TestLogger.logger.debug("test: " + getName());
            
            AddNumbersService service = new AddNumbersService();
            AddNumbersPortType proxy = service.getAddNumbersPort();
            
            BindingProvider bp = (BindingProvider) proxy;
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, 
                    axisEndpoint);
            proxy.oneWayInt(11);
            TestLogger.logger.debug("----------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }       
    }
}
