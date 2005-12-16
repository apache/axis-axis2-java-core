package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.RequestCounter;
import org.apache.axis2.engine.util.RequestCounterMessageReceiver;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

public class ServiceGroupContextTest extends TestCase {

    /**
     * This test will first sends a request to a dummy service deployed. That service will get
     * message contexts as inputs and will put a property in the service group context to count the
     * number of requests.
     * Then the client, upon receiving the response, extracts the sgc id from the received message
     * (this will come as a reference parameter in the ReplyTo EPR) and sets that as a top level
     * soap header in the next request to the same service group.
     * Server will correctly identify the service group from the information sent by the client and
     * retrieve the sgc earlier used and will use that for the current request as well.
     * The service will retrieve the request count from the sgc and increase that by one.
     * <p/>
     * Test will asserts whether the client gets the number of requests as 2, when he invokes two times.
     */

    protected EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:" +
            (UtilServer.TESTING_PORT) +
            "/axis/services/RequestCounter");
    protected Log log = LogFactory.getLog(getClass());
    protected QName serviceName = new QName("RequestCounter");
    protected QName operationName = new QName("getRequestCount");
    protected QName transportName = new QName("http://localhost/my", "NullTransport");
    protected AxisService service;

    protected void setUp() throws Exception {
        UtilServer.start();
        service = Utils.createSimpleService(serviceName, new RequestCounterMessageReceiver(),
                RequestCounter.class.getName(), operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
        UtilServer.unDeployClientService();
    }

    public void testEchoXMLSync() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        SOAPEnvelope payload = fac.getDefaultEnvelope();

        org.apache.axis2.client.Call call = new org.apache.axis2.client.Call("target/test-resources/integrationRepo");

        Options options = new Options();
        call.setClientOptions(options);
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);

        options.setAction(operationName.getLocalPart());

        SOAPEnvelope result = call.invokeBlocking(operationName.getLocalPart(), payload);


        OMNamespace axis2Namespace = fac.createOMNamespace(Constants.AXIS2_NAMESPACE_URI,
                Constants.AXIS2_NAMESPACE_PREFIX);
        SOAPEnvelope defaultEnvelope = fac.getDefaultEnvelope();
        SOAPHeaderBlock soapHeaderBlock = defaultEnvelope.getHeader().addHeaderBlock(Constants.SERVICE_GROUP_ID,
                axis2Namespace);

        System.out.println("soapHeaderBlock = " + soapHeaderBlock);
        String serviceGroupId = result.getHeader().getFirstChildWithName(new QName("ReplyTo"))
                .getFirstChildWithName(new QName("ReferenceParameters")).
                getFirstChildWithName(new QName("ServiceGroupId")).getText();

        soapHeaderBlock.setText(serviceGroupId);

        SOAPEnvelope soapEnvelope = call.invokeBlocking(operationName.getLocalPart(),
                defaultEnvelope);
        String text = soapEnvelope.getBody().getFirstElement().getText();
        assertEquals("Number of requests should be 2", 2, Integer.parseInt(text));
    }

}
