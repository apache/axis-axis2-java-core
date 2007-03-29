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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;


public class EchoRawXMLMultipleSyncTest extends UtilServerBasedTestCase implements TestConstants {

    private static final Log log = LogFactory.getLog(EchoRawXMLMultipleSyncTest.class);
    protected QName transportName = new QName("http://localhost/my",
            "NullTransport");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService service;

    protected boolean finish = false;

    public EchoRawXMLMultipleSyncTest() {
        super(EchoRawXMLTest.class.getName());
    }

    public EchoRawXMLMultipleSyncTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(EchoRawXMLMultipleSyncTest.class));
    }

    protected void setUp() throws Exception {
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    public void testEchoXMLMultipleSync() throws Exception {
        OMElement payload = TestingUtils.createDummyOMElement();
        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);
        for (int i = 0; i < 5; i++) {
            OMElement result = sender.sendReceive(payload);
            TestingUtils.campareWithCreatedOMElement(result);
        }
        sender.cleanup();
        configContext.terminate();
    }

    public void testEchoXMLMultipleDuelSync() throws Exception {
        OMElement payload = TestingUtils.createDummyOMElement();
        Options options = new Options();
        options.setTo(targetEPR);

        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(true);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem("target/test-resources/integrationRepo",
                        "target/test-resources/integrationRepo/conf/axis2.xml");
        ServiceClient sender = new ServiceClient(configContext, null);
        options.setAction(Constants.AXIS2_NAMESPACE_URI + "/" + operationName.getLocalPart());
        options.setAction("urn:echoOMElement");
        sender.setOptions(options);

        for (int i = 0; i < 5; i++) {
            OMElement result = sender.sendReceive(payload);
            TestingUtils.campareWithCreatedOMElement(result);
        }
        sender.cleanup();
        configContext.terminate();
    }


}
