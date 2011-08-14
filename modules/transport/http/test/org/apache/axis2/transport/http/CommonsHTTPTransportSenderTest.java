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
package org.apache.axis2.transport.http;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.junit.Test;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;

public class CommonsHTTPTransportSenderTest {
    /**
     * Tests that HTTP connections are properly released when the server returns a 404 error. This
     * is a regression test for AXIS2-5093.
     * 
     * @throws Exception
     */
    @Test
    public void test() throws Exception {
        // Create a Jetty server instance without any contexts. It will always return HTTP 404.
        Server server = new Server();
        SocketListener listener = new SocketListener();
        server.addListener(listener);
        server.start();
        try {
            ConfigurationContext configurationContext =
                    ConfigurationContextFactory.createConfigurationContextFromURIs(
                            CommonsHTTPTransportSenderTest.class.getResource("axis2.xml"), null);
            ServiceClient serviceClient = new ServiceClient(configurationContext, null);
            Options options = serviceClient.getOptions();
            options.setTo(new EndpointReference("http://localhost:" + listener.getPort() + "/nonexisting"));
            OMElement request = OMAbstractFactory.getOMFactory().createOMElement(new QName("urn:test", "test"));
            // If connections are not properly released then we will end up with a
            // ConnectionPoolTimeoutException here.
            for (int i=0; i<200; i++) {
                try {
                    serviceClient.sendReceive(request);
                } catch (AxisFault ex) {
                    // Check that this is a 404 error
                    assertNull(ex.getCause());
                    assertTrue(ex.getMessage().contains("404"));
                }
                serviceClient.cleanupTransport();
            }
        } finally {
            server.stop();
        }
    }
}
