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
package org.apache.axis2.engine;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class LongRunningServiceTest extends UtilServerBasedTestCase
        implements TestConstants {

    QName operationName = new QName("longRunning");
    EndpointReference targetEPR = new EndpointReference(
            "http://127.0.0.1:" + (UtilServer.TESTING_PORT)
//            "http://127.0.0.1:" + 5556
                    + "/axis2/services/EchoXMLService/longRunning");
    private static final Log log = LogFactory.getLog(LongRunningServiceTest.class);

    public LongRunningServiceTest() {
        super(LongRunningServiceTest.class.getName());
    }

    public LongRunningServiceTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(LongRunningServiceTest.class));
    }

    protected void setUp() throws Exception {
        UtilServer.start();
        UtilServer.engageAddressingModule();
        AxisService service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);

    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
    }

    public void testLongRunningService() throws Exception {
        ConfigurationContext configConetxt = UtilServer.createClientConfigurationContext();

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMElement payload = TestingUtils.createDummyOMElement();
        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(true);
        options.setAction(operationName.getLocalPart());
        BetterAxisCallback callback = new BetterAxisCallback();

        ServiceClient sender = new ServiceClient(configConetxt, null);
        sender.setOptions(options);

        sender.sendReceiveNonBlocking(payload, callback);

        //Wait till the callback receives the response.
        while (!callback.isComplete()) {
            Thread.sleep(1000);
        }

        sender.cleanup();
        configConetxt.terminate();
    }

    class BetterAxisCallback implements AxisCallback {
        private boolean complete = false;

        public void onComplete() {
            log.info("onComplete\n");
            complete = true;
        }

        public void onFault(MessageContext mc) {
              System.out.println("Fault");
            log.info("onFault\n");
            complete = true;
        }

        public void onMessage(MessageContext mc) {
             TestingUtils.compareWithCreatedOMElement(
                            mc.getEnvelope().getBody().getFirstElement());
            log.info("onMessage\n");
            complete = true;
        }

        public void onError(Exception e) {
            e.printStackTrace();
            log.info("Error\n" + e.getStackTrace());
            complete = true;
        }

        public boolean isComplete() {
            return complete;
        }
    }


}
