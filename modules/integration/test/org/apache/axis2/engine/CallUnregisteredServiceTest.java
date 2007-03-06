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
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;

public class CallUnregisteredServiceTest extends UtilServerBasedTestCase {

    public CallUnregisteredServiceTest() {
        super(CallUnregisteredServiceTest.class.getName());
    }

    public CallUnregisteredServiceTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(CallUnregisteredServiceTest.class));
    }

    public void testEchoXMLSync() throws Exception {
        try {
            SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

            OMNamespace omNs = fac.createOMNamespace("http://localhost/my",
                    "my");
            OMElement method = fac.createOMElement("echoOMElement", omNs);
            OMElement value = fac.createOMElement("myValue", omNs);
            value.addChild(
                    fac.createOMText(value,
                            "Isaac Asimov, The Foundation Trilogy"));
            method.addChild(value);

            EndpointReference targetEPR =
                    new EndpointReference("http://127.0.0.1:"
                            + (UtilServer.TESTING_PORT)
                            + "/axis2/services/EchoXMLService1");

            Options options = new Options();
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setTo(targetEPR);

            ConfigurationContext configContext =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem("target/test-resources/integrationRepo",null);
            ServiceClient sender = new ServiceClient(configContext, null);
            sender.setOptions(options);

            sender.sendReceive(method);
            fail("The test must fail due to wrong service Name");

        } catch (AxisFault e) {
//            e.printStackTrace();
            System.out.println(e.getMessage());
            assertTrue(e.getMessage().indexOf("Service not found for the") >= 0);
            tearDown();
            return;
        }

    }
}
