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

import junit.framework.TestCase;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;

/**
 * AXIS2-5858: Verify IPv6 addresses are bracketed in EPR URLs.
 */
public class HTTPTransportUtilsIPv6Test extends TestCase {

    private ConfigurationContext configContext;
    private TransportInDescription httpTransport;

    @Override
    protected void setUp() throws Exception {
        AxisConfiguration axisConfig = new AxisConfiguration();
        configContext = new ConfigurationContext(axisConfig);
        configContext.setServicePath("services");
        configContext.setContextRoot("/");
        httpTransport = new TransportInDescription("http");
    }

    public void testIPv4AddressNotBracketed() throws Exception {
        EndpointReference[] eprs = HTTPTransportUtils.getEPRsForService(
                configContext, httpTransport, "MyService", "192.168.1.100", 8080);
        String epr = eprs[0].getAddress();
        assertTrue("IPv4 should not have brackets: " + epr,
                epr.contains("://192.168.1.100:8080/"));
        assertFalse("IPv4 should not have brackets: " + epr,
                epr.contains("["));
    }

    public void testIPv6AddressBracketed() throws Exception {
        EndpointReference[] eprs = HTTPTransportUtils.getEPRsForService(
                configContext, httpTransport, "MyService", "fe80::1", 8080);
        String epr = eprs[0].getAddress();
        assertTrue("IPv6 should be bracketed: " + epr,
                epr.contains("://[fe80::1]:8080/"));
    }

    public void testIPv6FullAddressBracketed() throws Exception {
        EndpointReference[] eprs = HTTPTransportUtils.getEPRsForService(
                configContext, httpTransport, "MyService",
                "2001:0db8:85a3:0000:0000:8a2e:0370:7334", 443);
        String epr = eprs[0].getAddress();
        assertTrue("Full IPv6 should be bracketed: " + epr,
                epr.contains("[2001:0db8:85a3:0000:0000:8a2e:0370:7334]"));
    }

    public void testIPv6LoopbackBracketed() throws Exception {
        EndpointReference[] eprs = HTTPTransportUtils.getEPRsForService(
                configContext, httpTransport, "MyService", "::1", 8080);
        String epr = eprs[0].getAddress();
        assertTrue("IPv6 loopback should be bracketed: " + epr,
                epr.contains("://[::1]:8080/"));
    }

    public void testNullIpHandled() throws Exception {
        // null IP falls through to Utils.getIpAddress() which returns an IPv4
        // address — just verify no NPE
        EndpointReference[] eprs = HTTPTransportUtils.getEPRsForService(
                configContext, httpTransport, "MyService", null, 8080);
        assertNotNull(eprs);
        assertNotNull(eprs[0].getAddress());
    }
}
