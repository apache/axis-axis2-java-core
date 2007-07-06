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

package org.apache.axis2.fastinfoset;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.rmi.RemoteException;

import junit.framework.TestCase;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.http.SimpleHTTPServer;

/**
 * @author Sanjaya Karunasena (sanjayak@yahoo.com)
 * @date Feb 16, 2007
 */
public class FastInfosetTest extends TestCase {

	private SimpleHTTPServer server;
	private static int serverCount = 0;
	
	private AxisService service;
	
	private EndpointReference target;
	private ConfigurationContext configurationContext;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		System.out.println("Setting up the Simple HTTP Server");
		
		if (serverCount == 0) {
			int port = findAvailablePort();
			port = 8080; //Uncomment to test with tcpmon
			target = new EndpointReference("http://127.0.0.1:" + (port)
                    + "/axis2/services/SimpleAddService");
			
			File configFile = new File(System.getProperty("basedir",".") + "/test-resources/axis2.xml");
			configurationContext = ConfigurationContextFactory
            .createConfigurationContextFromFileSystem("target/test-classes", configFile
                    .getAbsolutePath());

			server = new SimpleHTTPServer(configurationContext, port);
    
			server.start();
		}
		
		serverCount++;
		
		service = AxisService.createService("org.apache.axis2.fastinfoset.SimpleAddService", 
				server.getConfigurationContext().getAxisConfiguration());
		
		server.getConfigurationContext().getAxisConfiguration().addService(
                service);
        
		System.out.println("Simple HTTP Server is started");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
    	if(serverCount == 1) {
    		server.stop();
    		serverCount = 0;
    		System.out.println("Stopped the Simple HTTP Server");
    	}
	}
	
	public void testAdd() throws RemoteException {
		SimpleAddServiceClient client = new SimpleAddServiceClient(target); //Comment to test with tcpmon.
//		SimpleAddServiceClient client = new SimpleAddServiceClient(); //Uncomment to test with tcpmon.
		
		String result = client.addStrings("Hello ", "World!");
		System.out.println("Output: " + result);
		TestCase.assertEquals("Hello World!", result);
		
		int result1 = client.addInts(17, 33);
		System.out.println("Output: " + result1);
		TestCase.assertEquals(50, result1);
		
		float result2 = client.addFloats(17.64f, 32.36f);
		System.out.println("Output: " + result2);
		TestCase.assertEquals(50.0f, result2, 0.0005f);
	}


	public void testAdd2() throws RemoteException {
//        SimpleAddServiceClient client = new SimpleAddServiceClient(target, true); //Comment to test with tcpmon.
////		SimpleAddServiceClient client = new SimpleAddServiceClient(); //Uncomment to test with tcpmon.
////
//		String result = client.addStrings("Hello ", "World!");
//		System.out.println("Output: " + result);
//		TestCase.assertEquals("Hello World!", result);
//
//		int result1 = client.addInts(17, 33);
//		System.out.println("Output: " + result1);
//		TestCase.assertEquals(50, result1);
//
//		float result2 = client.addFloats(17.64f, 32.36f);
//		System.out.println("Output: " + result2);
//		TestCase.assertEquals(50.0f, result2, 0.0005f);
	}

	private int findAvailablePort() throws SocketException, IOException {
		//Create a server socket on any free socket to find a free socket.
		ServerSocket ss = new ServerSocket(0);
		int port = ss.getLocalPort();
		ss.close();
   	
    	return port;
    }
	
	/**
	 * Run this class as a Java application, will host this SimpleAddService for further testing.
	 * There is a main method in SimpleAddServiceClient which can be used with this.
	 * 
	 * Note: Useful when debugging.
	 * 
	 * @param args - Not required.
	 */
	public static void main(String args[]) {
		FastInfosetTest test = new FastInfosetTest();
		try {
			test.setUp();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
