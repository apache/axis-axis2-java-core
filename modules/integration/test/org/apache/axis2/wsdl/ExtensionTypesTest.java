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

package org.apache.axis2.wsdl;

import java.net.MalformedURLException;
import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;

/**
 * Tests whether extension attributes in {@link javax.wsdl.extensions.ExtensionRegistry} are of correct type
 * for WSDL Definition instances created by Axis2.
 * 
 * @see {@link org.apache.axis2.wsdl.WSDLUtil#registerDefaultExtensionAttributeTypes(javax.wsdl.extensions.ExtensionRegistry)}
 */
public class ExtensionTypesTest extends UtilServerBasedTestCase implements TestConstants {
	protected AxisService service;

	public static Test suite() {
		return getTestSetup(new TestSuite(ExtensionTypesTest.class));
	}

	protected void setUp() throws Exception {
		service = AxisService.createService(Echo.class.getName(), UtilServer.getConfigurationContext().getAxisConfiguration());
		service.setName(serviceName.getLocalPart());
		UtilServer.deployService(service);
	}

	protected void tearDown() throws Exception {
		UtilServer.unDeployService(serviceName);
		UtilServer.unDeployClientService();
	}

	/**
	 * Creates a service client for EchoXMLService, obtains the WSDL Definition and verifies whether the Addressing <code>Action</code> extension
	 * attributes on operation's input and output elements are of correct type and value (expected to be String rather than QName).
	 * @throws MalformedURLException 
	 * @throws AxisFault 
	 * 
	 * @throws Exception
	 */
	public void testExtensionTypes() throws MalformedURLException, AxisFault {					 
		URL wsdlURL = new URL(String.format("http://localhost:%s/axis2/services/EchoXMLService?wsdl", UtilServer.TESTING_PORT));
		ServiceClient serviceClient = new ServiceClient(null, wsdlURL, new QName("http://engine.axis2.apache.org", "EchoXMLService"), "EchoHttpSoap11Endpoint");
		Definition definition = (Definition) serviceClient.getAxisService().getParameter("wsdl4jDefinition").getValue();
		PortType pt = definition.getPortType(new QName("http://engine.axis2.apache.org", "EchoXMLServicePortType"));
		Operation op = pt.getOperation("echoOM", null, null);

		QName addressingActionAttName = new QName("http://www.w3.org/2006/05/addressing/wsdl", "Action");
		Object addressingActionAttValue = op.getInput().getExtensionAttributes().get(addressingActionAttName);
		assertNotNull(String.format("The value of extension attribute %s on %s operation's input is null", addressingActionAttName, op.getName()), addressingActionAttValue);
		assertTrue(String.format("The value of extension attribute %s on %s operation's input is not a String: %s", addressingActionAttName, op.getName(), addressingActionAttValue.getClass().getName()),
				addressingActionAttValue instanceof String);
		
		assertEquals("urn:echoOM", (String) addressingActionAttValue);
			
		addressingActionAttValue = op.getOutput().getExtensionAttributes().get(addressingActionAttName);
		assertNotNull(String.format("The value of extension attribute %s on %s operation's output is null", addressingActionAttName, op.getName()), addressingActionAttValue);
		assertTrue(String.format("The value of extension attribute %s on %s operation's input is not a String: %s", addressingActionAttName, op.getName(), addressingActionAttValue.getClass().getName()),
				addressingActionAttValue instanceof String);
		assertEquals("urn:echoOMResponse", (String) addressingActionAttValue);
	}
}
