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

package org.apache.axis2.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.testutils.UtilServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONIntegrationTest implements JSONTestConstants {

    private String expectedString;

    private static EndpointReference echoTargetEPR;
    private static String pojoUri;

    @BeforeClass
    public static void startServer() throws Exception {
        UtilServer.start("test-repository/json", "test-repository/json/axis2.xml");
        echoTargetEPR = new EndpointReference(
                "http://127.0.0.1:" + UtilServer.TESTING_PORT
                        + "/axis2/services/EchoXMLService/echoOM");
        pojoUri = "http://127.0.0.1:" + UtilServer.TESTING_PORT + "/axis2/services/POJOService";
    }

    @AfterClass
    public static void stopServer() throws Exception {
		UtilServer.stop();
    }

    protected OMElement createEnvelope() throws Exception {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("", "");
        OMElement rpcWrapEle = fac.createOMElement("echoOM", omNs);
        OMElement data = fac.createOMElement("data", omNs);
        OMElement data1 = fac.createOMElement("data", omNs);
        expectedString = "my json string";
        String expectedString1 = "my second json string";
        data.setText(expectedString);
        data1.setText(expectedString1);
        rpcWrapEle.addChild(data);
        rpcWrapEle.addChild(data1);
        return rpcWrapEle;
    }

    private void doEchoOM(String messageType, String httpMethod) throws Exception{
    	OMElement payload = createEnvelope();
        Options options = new Options();
        options.setTo(echoTargetEPR);
        options.setProperty(Constants.Configuration.MESSAGE_TYPE, messageType);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setProperty(Constants.Configuration.HTTP_METHOD, httpMethod);
//        ConfigurationContext clientConfigurationContext = ConfigurationContextFactory.createDefaultConfigurationContext();
        ServiceClient sender = new ServiceClient(UtilServer.getConfigurationContext(), null);
        options.setAction(null);
        sender.setOptions(options);
        options.setTo(echoTargetEPR);
        OMElement result = sender.sendReceive(payload);
        OMElement ele = (OMElement)result.getFirstOMChild();
        compareWithCreatedOMText(ele.getText());
    }

    @Test
    public void testEchoOMWithJSONBadgerfish() throws Exception{
    	doEchoOM("application/json/badgerfish", Constants.Configuration.HTTP_METHOD_POST);
    }

    @Test
    public void testEchoOMWithJSON() throws Exception {
    	doEchoOM("application/json", Constants.Configuration.HTTP_METHOD_POST);
    }

    @Test
    public void testEchoOMWithJSONInGET() throws Exception {
        doEchoOM("application/json", Constants.Configuration.HTTP_METHOD_GET);
    }

    @Test
    public void testPOJOServiceWithJSONBadgerfish() throws Exception {
        HttpURLConnection conn = (HttpURLConnection)new URL(pojoUri).openConnection();
        conn.setDoOutput(true);
        conn.addRequestProperty("Content-Type", "application/json/badgerfish");
        Writer out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
        // XML is: <sayHello xmlns="http://example.org"><myName>Joe</myName></sayHello>
        out.write("{ \"sayHello\" : { \"@xmlns\" : { \"$\" : \"http://example.org\" }, \"myName\" : { \"$\" : \"Joe\" } } }");
        out.close();
        assertEquals(200, conn.getResponseCode());
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        assertTrue(in.readLine().contains("Hello Joe!"));
        in.close();
    }
    

    @Test
    public void testPOJOServiceWithJSONMapped() throws Exception {
        HttpURLConnection conn = (HttpURLConnection)new URL(pojoUri).openConnection();
        conn.setDoOutput(true);
        conn.addRequestProperty("Content-Type", "application/json");
        Writer out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
        out.write("{ \"sayHello\" : { \"myName\" : \"Joe\" } }");
        out.close();
        assertEquals(200, conn.getResponseCode());
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        assertEquals("{\"sayHelloResponse\":{\"return\":\"Hello Joe!\"}}", in.readLine());
        in.close();
    }
    
    protected void compareWithCreatedOMText(String response) {
        assertEquals(response, expectedString);
    }
}
