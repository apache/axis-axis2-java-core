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
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.util.Utils;

public class EchoRawXMLChunkedTest extends TestCase implements TestConstants {

    private AxisService service;

    private static final String CLIENT_HOME = Constants.TESTING_PATH + "chunking-enabledRepository";

    public EchoRawXMLChunkedTest() {
        super(EchoRawXMLChunkedTest.class.getName());
    }

    public EchoRawXMLChunkedTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start(CLIENT_HOME);
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
    }

//    public void testEchoXMLASync() throws Exception {
//                OMElement payload = createEnvelope();
//
//        org.apache.axis2.client.Call call = new org.apache.axis2.client.Call(Constants.TESTING_PATH + "chunking-enabledRepository");
//
//        call.setTo(targetEPR);
//        call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);
//
//        Callback callback = new Callback() {
//            public void onComplete(AsyncResult result) {
//                try {
//                    result.getResponseEnvelope().serializeAndConsume(new OMOutput(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out)));
//                } catch (XMLStreamException e) {
//                    reportError(e);
//                } finally {
//                    finish = true;
//                }
//            }
//
//            public void reportError(Exception e) {
//                finish = true;
//            }
//        };
//
//        call.invokeNonBlocking(operationName.getLocalPart(), payload, callback);
//        int index = 0;
//        while (!finish) {
//            Thread.sleep(1000);
//            index++;
//            if(index > 10 ){
//                throw new AxisFault("Server was shutdown as the async response take too long to complete");
//            }
//        }
//        call.close();
//
//
//        log.info("send the reqest");
//    }

    public void testEchoXMLSync() throws Exception {
        OMElement payload = TestingUtils.createDummyOMElement();

        Options clientOptions = new Options();
        clientOptions.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContextFactory factory = new ConfigurationContextFactory();
        ConfigurationContext configContext =
                factory.createConfigurationContextFromFileSystem(CLIENT_HOME);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(clientOptions);
        clientOptions.setTo(targetEPR);

        OMElement result = sender.sendReceive(payload);


        TestingUtils.campareWithCreatedOMElement(result);
        sender.finalizeInvoke();
    }
}
