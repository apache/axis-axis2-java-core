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

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.util.Utils;

public class ServiceDispatchingTest extends TestCase implements TestConstants {

    public ServiceDispatchingTest() {
        super(ServiceDispatchingTest.class.getName());
    }

    public ServiceDispatchingTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
        AxisService service = Utils.createSimpleService(serviceName,
                Echo.class.getName(),
                operationName);
        UtilServer.deployService(service);

    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
        UtilServer.unDeployClientService();
    }


    public void testDispatchWithURLOnly() throws Exception {
        OMElement payload = TestingUtils.createDummyOMElement();
        Options options = new Options();
        options.setTo(
                new EndpointReference("http://127.0.0.1:5555/axis/services/EchoXMLService/echoOMElement"));
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);


        ConfigurationContextFactory factory = new ConfigurationContextFactory();
        ConfigurationContext configContext =
                factory.buildConfigurationContext("target/test-resources/integrationRepo");
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);
        options.setTo(targetEPR);

        OMElement result = sender.sendReceive(payload);

        TestingUtils.campareWithCreatedOMElement(result);
    }

    public void testDispatchWithURLAndSOAPAction() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        OMNamespace omNs = fac.createOMNamespace("http://dummyURL", "my");
        OMElement payload = fac.createOMElement("echoOMElementRequest", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(
                fac.createText(value, "Isaac Asimov, The Foundation Trilogy"));
        payload.addChild(value);
        Options options = new Options();
        options.setTo(
                new EndpointReference("http://127.0.0.1:5555/axis/services/EchoXMLService/"));
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setSoapAction("echoOMElement");
        ConfigurationContextFactory factory = new ConfigurationContextFactory();
        ConfigurationContext configContext =
                factory.buildConfigurationContext("target/test-resources/integrationRepo");
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);
        options.setTo(targetEPR);

        OMElement result = sender.sendReceive(payload);
        TestingUtils.campareWithCreatedOMElement(result);
    }

    public void testDispatchWithSOAPBody() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMNamespace omNs = fac.createOMNamespace(
                "http://127.0.0.1:5555/axis/services/EchoXMLService", "my");
        OMElement payload = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.addChild(
                fac.createText(value, "Isaac Asimov, The Foundation Trilogy"));
        payload.addChild(value);
        Options options = new Options();
        options.setTo(
                new EndpointReference("http://127.0.0.1:5555/axis/services/"));
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        ConfigurationContextFactory factory = new ConfigurationContextFactory();
        ConfigurationContext configContext =
                factory.buildConfigurationContext("target/test-resources/integrationRepo");
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);
        options.setTo(targetEPR);

        OMElement result = sender.sendReceive(payload);

        TestingUtils.campareWithCreatedOMElement(result);
    }
}
