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

package org.apache.axis2.engine;

//todo

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPFactory;

public class CallUnregisteredServiceTest extends TestCase {

    public CallUnregisteredServiceTest() {
        super(CallUnregisteredServiceTest.class.getName());
    }

    public CallUnregisteredServiceTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
    }

    protected void tearDown() throws Exception {
        UtilServer.stop();
    }

    public void testEchoXMLSync() throws Exception {
        try {
            SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

            OMNamespace omNs = fac.createOMNamespace("http://localhost/my",
                    "my");
            OMElement method = fac.createOMElement("echoOMElement", omNs);
            OMElement value = fac.createOMElement("myValue", omNs);
            value.addChild(
                    fac.createText(value,
                            "Isaac Asimov, The Foundation Trilogy"));
            method.addChild(value);

            EndpointReference targetEPR =
                    new EndpointReference("http://127.0.0.1:"
                            + (UtilServer.TESTING_PORT)
                            + "/axis/services/EchoXMLService1");

            Options options = new Options();
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setTo(targetEPR);

            ConfigurationContextFactory factory = new ConfigurationContextFactory();
            ConfigurationContext configContext =
                    factory.createConfigurationContextFromFileSystem("target/test-resources/integrationRepo");
            ServiceClient sender = new ServiceClient(configContext, null);
            sender.setOptions(options);

            sender.sendReceive(method);
            fail("The test must fail due to wrong service Name");

        } catch (AxisFault e) {
            assertTrue(e.getMessage().indexOf("Service Not found") >= 0);
            tearDown();
            return;
        }

    }
}
