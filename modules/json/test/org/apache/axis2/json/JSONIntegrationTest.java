/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.axis2.json;

import java.io.File;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.axis2.util.Utils;

public class JSONIntegrationTest extends TestCase implements JSONTestConstants {

	private AxisService service;

	private String expectedString;

	private SimpleHTTPServer server;
	
	private ConfigurationContext configurationContext;

	public JSONIntegrationTest() {
	}

	protected void setUp() throws Exception {

//		File configFile = new File("test-resources/axis2.xml");
//		configurationContext = ConfigurationContextFactory
//				.createConfigurationContextFromFileSystem(null, configFile
//						.getAbsolutePath());
//		server = new SimpleHTTPServer(configurationContext, TESTING_PORT);
//		try {
//			server.start();
//		} finally {
//
//		}
//		service = Utils.createSimpleService(serviceName, org.apache.axis2.json.Echo.class.getName(),
//				operationName);
//		server.getConfigurationContext().getAxisConfiguration().addService(
//				service);
	}

	protected void tearDown() throws Exception {
//		server.stop();
	}

	protected OMElement createEnvelope() throws Exception {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace("", "");
		OMElement rpcWrapEle = fac.createOMElement("echoOM", omNs);
		OMElement data = fac.createOMElement("data", omNs);
		expectedString = "my json string";
		data.setText(expectedString);
		rpcWrapEle.addChild(data);
		return rpcWrapEle;
	}

	public void testEchoOMWithJSON() throws Exception {
//		OMElement payload = createEnvelope();
//		Options options = new Options();
//		options.setTo(targetEPR);
//
//		options.setProperty(Constants.Configuration.MESSAGE_TYPE,
//				"application/json");
//		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
////		options.setTimeOutInMilliSeconds(1000);
//		
//		ServiceClient sender = new ServiceClient(configurationContext, null);
//		options.setAction(null);
//		sender.setOptions(options);
//		options.setTo(targetEPR);
//		OMElement result = sender.sendReceive(payload);
//		OMElement ele = (OMElement) result.getFirstOMChild();
//		compareWithCreatedOMText(ele.getText());
	}

	protected void compareWithCreatedOMText(String response) {
		TestCase.assertEquals(response, expectedString);
	}

}