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

package org.apache.axis2.rest;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpGetRESTBasedTest { //extends UtilServerBasedTestCase implements TestConstants {


//    public HttpGetRESTBasedTest() {
//        super(HttpGetRESTBasedTest.class.getName());
//    }
//
//    public HttpGetRESTBasedTest(String testName) {
//        super(testName);
//    }
//
//    public static Test suite() {
//        return getTestSetup(new TestSuite(HttpGetRESTBasedTest.class));
//    }

//    protected void setUp() throws Exception {
//        AxisService service =
//                Utils.createSimpleService(serviceName,
//                        Echo.class.getName(),
//                        operationName);
//        UtilServer.deployService(service);
//
//    }
//
//    protected void tearDown() throws Exception {
//        UtilServer.unDeployService(serviceName);
//    }

    public void testEchoXMLSync() throws Exception {
        //TODO support the GET with the Simple Axis Server and enable this test case
        URL wsdlrequestUrl =
                new URL(
                        "http://127.0.0.1:5555/axis2/services/EchoXMLService/echoOMElement?value1=value1,value2=value2");

        HttpURLConnection connection = (HttpURLConnection) wsdlrequestUrl.openConnection();
        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
        connection.getResponseCode();
        String line = reader.readLine();
        while (line != null) {
            line = reader.readLine();
        }
    }

}
