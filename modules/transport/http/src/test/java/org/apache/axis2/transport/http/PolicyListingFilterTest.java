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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.namespace.QName;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;

/**
 * A module lists the policy assertions that are local to the server in the
 * {@code local-policy-assertions} element of its module.xml (Rampart does so for
 * RampartConfig, which carries the user, callback class and keystore settings).
 * The WSDL generators drop them when they embed a policy, and the {@code ?policy}
 * route has to drop them as well.
 */
public class PolicyListingFilterTest extends TestCase {

    private static final String RAMPART_NS = "http://ws.apache.org/rampart/policy";

    private static final String POLICY =
            "<wsp:Policy xmlns:wsp='http://schemas.xmlsoap.org/ws/2004/09/policy'"
            + " xmlns:wsu='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd'"
            + " wsu:Id='SecPolicy'>"
            + "<wsp:ExactlyOne><wsp:All>"
            + "<sp:TransportBinding xmlns:sp='http://schemas.xmlsoap.org/ws/2005/07/securitypolicy'/>"
            + "<ramp:RampartConfig xmlns:ramp='" + RAMPART_NS + "'>"
            + "<ramp:user>service</ramp:user>"
            + "<ramp:property name='org.apache.ws.security.crypto.merlin.keystore.password'>"
            + "storepass</ramp:property>"
            + "</ramp:RampartConfig>"
            + "</wsp:All></wsp:ExactlyOne></wsp:Policy>";

    private ConfigurationContext configContext;

    @Override
    protected void setUp() throws Exception {
        configContext = ConfigurationContextFactory.createEmptyConfigurationContext();
        configContext.getAxisConfiguration().addLocalPolicyAssertion(
                new QName(RAMPART_NS, "RampartConfig"));

        AxisService service = new AxisService("Secured");
        Policy policy = PolicyEngine.getPolicy(
                new ByteArrayInputStream(POLICY.getBytes(StandardCharsets.UTF_8)));
        service.getPolicySubject().attachPolicy(policy);
        configContext.getAxisConfiguration().addService(service);
    }

    private String policyListing(String id) throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURL())
                .thenReturn(new StringBuffer("http://localhost:8080/axis2/services/Secured"));
        when(req.getQueryString()).thenReturn(id == null ? "policy" : "policy&id=" + id);
        when(req.getParameter("id")).thenReturn(id);
        final ByteArrayOutputStream body = new ByteArrayOutputStream();
        HttpServletResponse res = mock(HttpServletResponse.class);
        when(res.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(int b) {
                body.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }
        });
        new ListingAgent(configContext).processListService(req, res);
        return new String(body.toByteArray(), StandardCharsets.UTF_8);
    }

    private void assertLocalAssertionWithheld(String listing) {
        assertTrue("the published assertion should still be listed: " + listing,
                listing.contains("TransportBinding"));
        assertFalse("a local assertion was published: " + listing,
                listing.contains("RampartConfig"));
        assertFalse("the keystore password was published: " + listing,
                listing.contains("storepass"));
    }

    public void testEffectivePolicyWithholdsLocalAssertions() throws Exception {
        assertLocalAssertionWithheld(policyListing(null));
    }

    public void testPolicyByIdWithholdsLocalAssertions() throws Exception {
        assertLocalAssertionWithheld(policyListing("SecPolicy"));
    }
}
