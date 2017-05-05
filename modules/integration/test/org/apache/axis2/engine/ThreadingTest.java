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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ThreadingTest extends UtilServerBasedTestCase implements TestConstants {
    private static final Log log = LogFactory.getLog(ThreadingTest.class);

    private static class Invoker implements Runnable {
        private final int threadNumber;
        private final CountDownLatch latch;
        private Exception thrownException;

        Invoker(int threadNumber, CountDownLatch latch) throws AxisFault {
            this.threadNumber = threadNumber;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                log.info("Starting Thread number " + threadNumber + " .............");
                OMElement payload = TestingUtils.createDummyOMElement();

                Options options = new Options();
                options.setTo(new EndpointReference("http://127.0.0.1:"
                        + UtilServer.TESTING_PORT
                        + "/axis2/services/EchoXMLService/echoOMElement"));
                options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
                ServiceClient sender = new ServiceClient();
                sender.setOptions(options);
                OMElement result = sender.sendReceive(payload);

                TestingUtils.compareWithCreatedOMElement(result);
                log.info("Finishing Thread number " + threadNumber + " .....");
            } catch (Exception axisFault) {
                thrownException = axisFault;
                log.error("Error has occured invoking the service ", axisFault);
            }
            latch.countDown();
        }

        Exception getThrownException() {
            return thrownException;
        }
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(ThreadingTest.class));
    }

    protected void setUp() throws Exception {
        AxisService service =
                Utils.createSimpleService(serviceName,
                                          Echo.class.getName(),
                                          operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    public void testEchoXMLSync() throws Exception {
        Invoker[] invokers = new Invoker[5];
        CountDownLatch latch = new CountDownLatch(invokers.length);

        for (int i = 0; i < invokers.length; i++) {
            Invoker invoker = new Invoker(i + 1, latch);
            invokers[i] = invoker;
            new Thread(invoker).start();
        }

        latch.await(30, TimeUnit.SECONDS);

        for (Invoker invoker : invokers) {
            Exception exception = invoker.getThrownException();
            if (exception != null) {
                throw exception;
            }
        }
    }
}
