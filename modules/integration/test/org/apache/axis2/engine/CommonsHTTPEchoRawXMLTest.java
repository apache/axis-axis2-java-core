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
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CommonsHTTPEchoRawXMLTest extends TestCase implements TestConstants {

    private Log log = LogFactory.getLog(getClass());

    private AxisService service;

    private boolean finish = false;

    public CommonsHTTPEchoRawXMLTest() {
        super(CommonsHTTPEchoRawXMLTest.class.getName());
    }

    public CommonsHTTPEchoRawXMLTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
        UtilServer.unDeployClientService();
    }

    public void testEchoXMLASync() throws Exception {
        OMElement payload = TestingUtils.createDummyOMElement();

//        Call call = new Call(
//                Constants.TESTING_PATH + "commons-http-enabledRepository");

        Options options = new Options();
//        call.setClientOptions(options);
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        Callback callback = new Callback() {
            public void onComplete(AsyncResult result) {
                TestingUtils.campareWithCreatedOMElement(
                        result.getResponseEnvelope().getBody().getFirstElement());
                finish = true;
            }

            public void reportError(Exception e) {
                log.info(e.getMessage());
                finish = true;
            }
        };

//        call.invokeNonBlocking(operationName.getLocalPart(),
//                payload,
//                callback);

        ConfigurationContextFactory factory = new ConfigurationContextFactory();
        ConfigurationContext configContext =
                factory.buildConfigurationContext(Constants.TESTING_PATH + "commons-http-enabledRepository");
        ServiceClient sender = new ServiceClient(configContext);
        sender.setOptions(options);
        options.setTo(targetEPR);

        sender.sendReceiveNonblocking(payload,callback);

        int index = 0;
        while (!finish) {
            Thread.sleep(1000);
            index++;
            if (index > 10) {
                throw new AxisFault(
                        "Server was shutdown as the async response take too long to complete");
            }
        }
//        call.close();
//

        log.info("send the reqest");
    }

    public void testEchoXMLSync() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        OMElement payload = TestingUtils.createDummyOMElement();

//        Call call = new Call(
//                Constants.TESTING_PATH + "commons-http-enabledRepository");

        Options options = new Options();
//        call.setClientOptions(options);
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

//        OMElement result =
//                call.invokeBlocking(operationName.getLocalPart(),
//                        payload);

        ConfigurationContextFactory factory = new ConfigurationContextFactory();
        ConfigurationContext configContext =
                factory.buildConfigurationContext(Constants.TESTING_PATH + "commons-http-enabledRepository");
        ServiceClient sender = new ServiceClient(configContext);
        sender.setOptions(options);
        options.setTo(targetEPR);

        OMElement result = sender.sendReceive(payload);

        TestingUtils.campareWithCreatedOMElement(result);
//        call.close();
    }

}
