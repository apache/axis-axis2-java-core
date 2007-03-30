package org.apache.axis2.engine;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.RequestCounter;
import org.apache.axis2.engine.util.RequestCounterMessageReceiver;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;

/*
* Copyright 2001-2004 The Apache Software Foundation.
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

public class ServiceGroupContextTest extends UtilServerBasedTestCase {

    /**
     * This test will first sends a request to a dummy service deployed. That service will get
     * message contexts as inputs and will put a property in the service group context to count the
     * number of requests. Then the client, upon receiving the response, extracts the sgc id from
     * the received message (this will come as a reference parameter in the ReplyTo EPR) and sets
     * that as a top level soap header in the next request to the same service group. Server will
     * correctly identify the service group from the information sent by the client and retrieve the
     * sgc earlier used and will use that for the current request as well. The service will retrieve
     * the request count from the sgc and increase that by one.
     * <p/>
     * Test will asserts whether the client gets the number of requests as 2, when he invokes two
     * times.
     */

    protected EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:" +
            (UtilServer.TESTING_PORT) +
            "/axis2/services/RequestCounter");
    protected QName serviceName = new QName("RequestCounter");
    protected QName operationName = new QName("getRequestCount");
    protected QName transportName = new QName("http://localhost/my", "NullTransport");
    protected AxisService service;

    public static Test suite() {
        return getTestSetup(new TestSuite(ServiceGroupContextTest.class));
    }

    protected void setUp() throws Exception {
        service = Utils.createSimpleService(serviceName, new RequestCounterMessageReceiver(),
                                            RequestCounter.class.getName(), operationName);
//        service.setScope(Constants.SCOPE_TRANSPORT_SESSION);
        service.setScope(Constants.SCOPE_SOAP_SESSION);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    public void testEchoXMLSync() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        SOAPEnvelope payload = fac.getDefaultEnvelope();
        Options options = new Options();
        options.setTo(targetEPR);
        options.setManageSession(true);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        options.setAction(operationName.getLocalPart());

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        "target/test-resources/integrationRepo", null);
        configContext.getAxisConfiguration().engageModule("addressing");
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);

        sender.sendReceive(payload.getBody().getFirstElement());

        SOAPEnvelope defaultEnvelope = fac.getDefaultEnvelope();

        //TODO : ple imporove this , what I have done is a hack
        OMElement result2 = sender.sendReceive(defaultEnvelope.getBody().getFirstElement());
        String text = result2.getText();
        assertEquals("Number of requests should be 2", 2, Integer.parseInt(text));
    }

}
