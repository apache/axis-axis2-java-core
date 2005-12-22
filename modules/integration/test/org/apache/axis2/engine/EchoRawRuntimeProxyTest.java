package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;
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
*
*/

public class EchoRawRuntimeProxyTest extends TestCase {
    public static final EndpointReference targetEPR = new EndpointReference(
            "http://apache.axis2.host" 
                    + "/axis2/services/EchoXMLService/echoOMElement");

    public static final QName serviceName = new QName("EchoXMLService");

    public static final QName operationName = new QName("echoOMElement");


    public EchoRawRuntimeProxyTest() {
        super(EchoRawXMLLoadTest.class.getName());
    }

    public EchoRawRuntimeProxyTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
        AxisService service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);


    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }


    public void testEchoXMLSync() throws Exception {

        OMElement payload = TestingUtils.createDummyOMElement();

        org.apache.axis2.client.Call call =
                new org.apache.axis2.client.Call(
                        "target/test-resources/integrationRepo");
        /**
         * Proxy setting in runtime
         */
        HttpTransportProperties.ProxyProperties proxyproperties = new HttpTransportProperties().new ProxyProperties();
        proxyproperties.setProxyName("localhost");
        proxyproperties.setProxyPort(5555);
        proxyproperties.setDomain("anonymous");
        proxyproperties.setPassWord("anonymous");
        proxyproperties.setUserName("anonymous");

        Options options = new Options();
        call.setClientOptions(options);
        options.setProperty(HTTPConstants.PROXY, proxyproperties);
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        OMElement result =
                call.invokeBlocking(operationName.getLocalPart(),
                        payload);

        TestingUtils.campareWithCreatedOMElement(result);
        call.close();
    }
}
