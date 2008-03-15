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

package org.apache.axis2.async;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.Utils;
import org.apache.axis2.util.threadpool.ThreadPool;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class AsyncService2Test extends UtilServerBasedTestCase implements TestConstants {

    private static final Log log = LogFactory.getLog(AsyncService2Test.class);
    protected QName transportName = new QName("http://localhost/my",
                                              "NullTransport");
    EndpointReference targetEPR = new EndpointReference(
            "http://127.0.0.1:" + (UtilServer.TESTING_PORT)
//            "http://127.0.0.1:" + 5556
                    + "/axis2/services/EchoXMLService/echoOMElement");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService service;
    private boolean finish = false;

    public static Test suite() {
        return getTestSetup(new TestSuite(AsyncService2Test.class));
    }

    protected void setUp() throws Exception {
        service = Utils.createSimpleService(serviceName,
                                            new AsyncMessageReceiver(),
                                            Echo.class.getName(),
                                            operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    private static final int MILLISECONDS = 1000;
    private static final Integer TIMEOUT = new Integer(
            200 * MILLISECONDS);
    private int counter = 0;
    private static final int MAX_REQUESTS = 10;

    public void testEchoXMLCompleteASyncWithLimitedNumberOfConnections() throws Exception {
        AxisService service =
                Utils.createSimpleServiceforClient(serviceName,
                                                   Echo.class.getName(),
                                                   operationName);

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        HttpConnectionManagerParams connectionManagerParams = new HttpConnectionManagerParams();
        // Maximum one socket connection to a specific host
        connectionManagerParams.setDefaultMaxConnectionsPerHost(1);
        connectionManagerParams.setTcpNoDelay(true);
        connectionManagerParams.setStaleCheckingEnabled(true);
        connectionManagerParams.setLinger(0);
        connectionManager.setParams(connectionManagerParams);

        HttpClient httpClient = new HttpClient(connectionManager);

        ConfigurationContext configcontext = UtilServer.createClientConfigurationContext();

        // Use max of 3 threads for the async thread pool
        configcontext.setThreadPool(new ThreadPool(1, 3));
        configcontext.setProperty(HTTPConstants.REUSE_HTTP_CLIENT,
                Boolean.TRUE);
        configcontext.setProperty(HTTPConstants.CACHED_HTTP_CLIENT,
                httpClient);


        OMFactory fac = OMAbstractFactory.getOMFactory();
        ServiceClient sender = null;
        try {
            Options options = new Options();
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setUseSeparateListener(true);
            options.setAction(operationName.getLocalPart());

            options.setTimeOutInMilliSeconds(200 * MILLISECONDS);
            options.setProperty(HTTPConstants.CHUNKED, Boolean.TRUE);
            options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT);
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT);
            options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT,
                    Boolean.TRUE);
            options.setProperty(HTTPConstants.AUTO_RELEASE_CONNECTION,
                    Boolean.TRUE);


            Callback callback = new Callback() {
                public void onComplete(AsyncResult result) {
                    TestingUtils.compareWithCreatedOMElement(
                            result.getResponseEnvelope().getBody()
                                    .getFirstElement());
                    System.out.println("result = " + result.getResponseEnvelope().getBody()
                            .getFirstElement());
                    counter++;
                }

                public void onError(Exception e) {
                    log.info(e.getMessage());
                    counter++;
                }
            };

            sender = new ServiceClient(configcontext, service);
            sender.setOptions(options);
            for (int i = 0; i < MAX_REQUESTS; i++) {
                OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
                OMElement method = fac.createOMElement("echoOMElement", omNs);
                OMElement value = fac.createOMElement("myValue", omNs);
                value.setText("Isaac Asimov, The Foundation Trilogy");
                method.addChild(value);
                sender.sendReceiveNonBlocking(operationName, method, callback);
                System.out.println("sent the request # : " + i);
            }
            System.out.print("waiting");
            int index = 0;
            while (counter < MAX_REQUESTS) {
                System.out.print('.');
                Thread.sleep(1000);
                index++;
                if (index > 60) {
                    throw new AxisFault(
                            "Server was shutdown as the async response take too long to complete");
                }
            }
        } finally {
            if (sender != null)
                sender.cleanup();
        }

    }
}
