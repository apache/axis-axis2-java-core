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
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
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
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;


public class EchoRawXMLMultipleTest extends TestCase implements TestConstants {

    protected Log log = LogFactory.getLog(getClass());
    protected QName transportName = new QName("http://localhost/my",
            "NullTransport");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService service;

    protected boolean finish = false;

    public EchoRawXMLMultipleTest() {
        super(EchoRawXMLTest.class.getName());
    }

    public EchoRawXMLMultipleTest(String testName) {
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


    public void testEchoXMLMultipleASync() throws Exception {
        OMElement payload = TestingUtils.createDummyOMElement();

        for (int i = 0; i < 5; i++) {
            Options options = new Options();
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

            Callback callback = new Callback() {
                public void onComplete(AsyncResult result) {
                    TestingUtils.campareWithCreatedOMElement(
                            result.getResponseEnvelope()
                                    .getBody().getFirstElement());
                    finish = true;
                }

                public void onError(Exception e) {
                    log.info(e.getMessage());
                    finish = true;
                }
            };
            ConfigurationContextFactory factory = new ConfigurationContextFactory();
            ConfigurationContext configContext =
                    factory.buildConfigurationContext("target/test-resources/integrationRepo");
            ServiceClient sender = new ServiceClient(configContext, null);
            sender.setOptions(options);
            options.setTo(targetEPR);

            sender.sendReceiveNonblocking(payload, callback);


            int index = 0;
            while (!finish) {
                Thread.sleep(1000);
                index++;
                if (index > 10) {
                    throw new AxisFault(
                            "Server was shutdown as the async response take too long to complete");
                }
            }
        }


        log.info("send the request");
    }

    public void testEchoXMLMultipleDuelASync() throws Exception {
        OMElement payload = TestingUtils.createDummyOMElement();

        ConfigurationContextFactory factory = new ConfigurationContextFactory();
        ConfigurationContext configContext =
                factory.buildConfigurationContext("target/test-resources/integrationRepo");
        ServiceClient sender = new ServiceClient(configContext, null);

        for (int i = 0; i < 5; i++) {
            Options options = new Options();
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setUseSeparateListener(true);

            Callback callback = new Callback() {
                public void onComplete(AsyncResult result) {
                    TestingUtils.campareWithCreatedOMElement(
                            result.getResponseEnvelope()
                                    .getBody().getFirstElement());
                    finish = true;
                }

                public void onError(Exception e) {
                    log.info(e.getMessage());
                    finish = true;
                }
            };

            sender.setOptions(options);
            options.setTo(targetEPR);

            sender.sendReceiveNonblocking(payload, callback);

            int index = 0;
            while (!finish) {
                Thread.sleep(1000);
                index++;
                if (index > 10) {
                    throw new AxisFault(
                            "Server is shutdown as the Async response take too longs time");
                }
            }
            sender.finalizeInvoke();
        }

        log.info("send the request");
    }

    public void testEchoXMLMultipleSync() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMElement payload = TestingUtils.createDummyOMElement();
        for (int i = 0; i < 5; i++) {
            Options options = new Options();
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            ConfigurationContextFactory factory = new ConfigurationContextFactory();
            ConfigurationContext configContext =
                    factory.buildConfigurationContext("target/test-resources/integrationRepo");
            ServiceClient sender = new ServiceClient(configContext, null);
            sender.setOptions(options);
            options.setTo(targetEPR);

            OMElement result = sender.sendReceive(payload);


            TestingUtils.campareWithCreatedOMElement(result);
            sender.finalizeInvoke();
        }
    }

    public void testEchoXMLMultipleDuelSync() throws Exception {
        OMElement payload = TestingUtils.createDummyOMElement();
        for (int i = 0; i < 5; i++) {
//            org.apache.axis2.client.Call call =
//                    new org.apache.axis2.client.Call(
//                            "target/test-resources/integrationRepo");

            Options options = new Options();
//            call.setClientOptions(options);
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setUseSeparateListener(true);

//            OMElement result =
//                    call.invokeBlocking(operationName.getLocalPart(),
//                            payload);
            ConfigurationContextFactory factory = new ConfigurationContextFactory();
            ConfigurationContext configContext =
                    factory.buildConfigurationContext("target/test-resources/integrationRepo");
            ServiceClient sender = new ServiceClient(configContext, null);

            sender.setOptions(options);

            OMElement result = sender.sendReceive(payload);

            TestingUtils.campareWithCreatedOMElement(result);
            sender.finalizeInvoke();
//            call.close();
        }
    }


}
