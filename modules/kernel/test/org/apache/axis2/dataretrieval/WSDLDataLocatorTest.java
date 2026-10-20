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

package org.apache.axis2.dataretrieval;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.kernel.TransportListener;
import org.apache.axis2.util.Utils;

public class WSDLDataLocatorTest extends TestCase {
    private AxisService service;

    @Override
    protected void setUp() throws Exception {
        ConfigurationContext configContext =
                ConfigurationContextFactory.createEmptyConfigurationContext();
        AxisConfiguration axisConfig = configContext.getAxisConfiguration();
        TransportInDescription httpIn = new TransportInDescription("http");
        httpIn.setReceiver(new HostEchoingListener());
        axisConfig.addTransportIn(httpIn);
        service = Utils.createSimpleService(new QName("EchoService"), "", new QName("echo"));
        axisConfig.addService(service);
        org.apache.axis2.deployment.util.Utils.addEndpointsToService(service);
    }

    @Override
    protected void tearDown() throws Exception {
        service = null;
        super.tearDown();
    }

    public void testRequestHostIsUsedButNotStoredOnService() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.printWSDL(out, "requester.example");
        String wsdl = out.toString("UTF-8");
        assertTrue(wsdl, wsdl.contains(
                "location=\"http://requester.example:8080/axis2/services/EchoService"));
        assertNull(service.getEndpointURL());
    }

    /**
     * Builds the EPRs the way the HTTP listeners do: from the host the request
     * was made to.
     */
    private static class HostEchoingListener implements TransportListener {
        public void init(ConfigurationContext configContext, TransportInDescription transportIn) {
        }

        public void start() {
        }

        public void stop() {
        }

        public void destroy() {
        }

        public EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault {
            return getEPRsForService(serviceName, ip)[0];
        }

        public EndpointReference[] getEPRsForService(String serviceName, String ip) {
            return new EndpointReference[] {
                    new EndpointReference("http://" + ip + ":8080/axis2/services/" + serviceName) };
        }

        public SessionContext getSessionContext(MessageContext messageContext) {
            return null;
        }
    }
}
