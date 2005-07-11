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

package org.apache.axis2.rest;

//todo

import junit.framework.TestCase;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpGetRESTBasedTest extends TestCase {
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");


    public HttpGetRESTBasedTest() {
        super(HttpGetRESTBasedTest.class.getName());
    }

    public HttpGetRESTBasedTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();

        ServiceDescription service =
                Utils.createSimpleService(serviceName, Echo.class.getName(), operationName);
        UtilServer.deployService(service);

    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }

    public void testEchoXMLSync() throws Exception {
        //TODO support the GET with the Simple Axis Server and enable this test case
        URL wsdlrequestUrl =
                new URL("http://127.0.0.1:5555/axis2/services/EchoXMLService/echoOMElement?value1=value1,value2=value2");

        HttpURLConnection connection = (HttpURLConnection) wsdlrequestUrl.openConnection();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(connection.getInputStream()));
        connection.getResponseCode();
        String line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }
    }

}
