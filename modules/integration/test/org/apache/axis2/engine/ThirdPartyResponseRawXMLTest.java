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
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.testutils.PortAllocator;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.axis2.util.Utils;

import static com.google.common.truth.Truth.assertThat;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;

public class ThirdPartyResponseRawXMLTest extends UtilServerBasedTestCase implements TestConstants {
    public static Test suite() {
        return getTestSetup(new TestSuite(ThirdPartyResponseRawXMLTest.class));
    }
    
    private final BlockingQueue<OMElement> received = new ArrayBlockingQueue<>(1);
    protected AxisService service;
    private SimpleHTTPServer receiver;
    private String callbackOperation;
    private String callbackServiceName = "CallbackService";
    private int callbackserverPort = PortAllocator.allocatePort();
    
    protected void setUp() throws Exception {
        service = Utils.createSimpleService(serviceName,
                Echo.class.getName(),
                operationName);
        UtilServer.deployService(service);
        
        callbackOperation = "callback";
    	AxisService callbackService  = Utils.createSimpleInOnlyService(new QName(callbackServiceName),new MessageReceiver(){
            public void receive(MessageContext messageCtx) throws AxisFault {
                SOAPEnvelope envelope = messageCtx.getEnvelope();
                OMElement bodyContent = envelope.getBody().getFirstElement();
                bodyContent.build();
                try {
                    received.put(bodyContent);
                } catch (InterruptedException ex) {
                    // Do nothing
                }
            }
        },new QName(callbackOperation));
        UtilServer.deployService(callbackService);
        
        receiver = new SimpleHTTPServer(UtilServer.getConfigurationContext(), callbackserverPort);
        receiver.start();
    }

    public void testOneWay() throws Exception {
        ConfigurationContext configContext =
            ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                    TestingUtils.prefixBaseDirectory(Constants.TESTING_PATH + "integrationRepo/"), null);
        ServiceClient sender = new ServiceClient(configContext, null);
        Options op = new Options();
        op.setTo(targetEPR);
        
        op.setReplyTo(new EndpointReference("http://127.0.0.1:"+(callbackserverPort)+"/axis2/services/"+callbackServiceName+ "/"+callbackOperation));
        op.setAction("urn:SomeAction");
        sender.setOptions(op);
        sender.engageModule(Constants.MODULE_ADDRESSING);
        sender.fireAndForget(TestingUtils.createDummyOMElement());
        OMElement bodyContent = received.poll(20, TimeUnit.SECONDS);
        assertThat(bodyContent).isNotNull();
        TestingUtils.compareWithCreatedOMElement(bodyContent);
    }

    protected void tearDown() throws Exception {
        receiver.stop();
    }

}
