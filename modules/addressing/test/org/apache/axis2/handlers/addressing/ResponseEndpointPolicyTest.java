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

package org.apache.axis2.handlers.addressing;

import junit.framework.TestCase;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;

/**
 * Unit tests for the egress policy applied to inbound WS-Addressing ReplyTo and
 * FaultTo endpoint references.
 */
public class ResponseEndpointPolicyTest extends TestCase {

    private AxisConfiguration axisConfiguration;
    private MessageContext messageContext;

    protected void setUp() throws Exception {
        super.setUp();
        axisConfiguration = new AxisConfiguration();
        ConfigurationContext configurationContext = new ConfigurationContext(axisConfiguration);
        messageContext = configurationContext.createMessageContext();
        messageContext.setServerSide(true);
    }

    private void setParameter(String name, String value) throws Exception {
        axisConfiguration.addParameter(new Parameter(name, value));
    }

    /**
     * The anonymous address means "reply on this connection" and drives no
     * outbound send, so it must never be blocked.
     */
    public void testAnonymousAddressIsAlwaysAllowed() {
        EndpointReference anonymous =
                new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL);
        assertTrue(ResponseEndpointPolicy.isAllowed(anonymous, messageContext));
        assertTrue(ResponseEndpointPolicy.isAllowed(null, messageContext));
    }

    /**
     * The cloud instance-metadata address is the canonical target for this class
     * of SSRF and is link-local, so the default policy must refuse it.
     */
    public void testInstanceMetadataAddressIsBlockedByDefault() {
        EndpointReference metadata =
                new EndpointReference("http://169.254.169.254/latest/meta-data/");
        assertFalse(ResponseEndpointPolicy.isAllowed(metadata, messageContext));
    }

    public void testLoopbackAndPrivateAddressesAreBlockedByDefault() {
        assertFalse(ResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://127.0.0.1:8080/sink"), messageContext));
        assertFalse(ResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://10.1.2.3/internal"), messageContext));
        assertFalse(ResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://192.168.1.10/admin"), messageContext));
        assertFalse(ResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://172.16.5.5/admin"), messageContext));
    }

    /**
     * Only transports that can carry a genuine reply are honoured, so the
     * scheme-based SSRF pivots are refused before any host check.
     */
    public void testNonHttpSchemesAreRejected() {
        assertFalse(ResponseEndpointPolicy.isAllowed(
                new EndpointReference("file:///etc/passwd"), messageContext));
        assertFalse(ResponseEndpointPolicy.isAllowed(
                new EndpointReference("gopher://example.com/1"), messageContext));
        assertFalse(ResponseEndpointPolicy.isAllowed(
                new EndpointReference("jar:http://example.com/a.jar!/"), messageContext));
    }

    /**
     * A routable public address is still permitted by default, so decoupled
     * responses to a genuine external endpoint keep working.
     */
    public void testPublicAddressIsAllowedByDefault() {
        EndpointReference publicEpr = new EndpointReference("http://192.0.2.25/replies");
        assertTrue(ResponseEndpointPolicy.isAllowed(publicEpr, messageContext));
    }

    /**
     * The strict posture — the equivalent of what CXF made its default — refuses
     * every non-anonymous response endpoint.
     */
    public void testNonAnonymousCanBeDisabledEntirely() throws Exception {
        setParameter(ResponseEndpointPolicy.ALLOW_NON_ANONYMOUS, "false");
        assertFalse(ResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://192.0.2.25/replies"), messageContext));
        // The anonymous case must still work, or in-out messaging breaks.
        assertTrue(ResponseEndpointPolicy.isAllowed(
                new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL),
                messageContext));
    }

    /**
     * An explicit allow-list supersedes the network-range check, so a deployment
     * that really does reply into its own network can permit exactly that host.
     */
    public void testAllowListPermitsAnOtherwiseBlockedHost() throws Exception {
        setParameter(ResponseEndpointPolicy.ALLOWED_HOSTS, "replies.example.com, 127.0.0.1");
        assertTrue(ResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://127.0.0.1:8080/sink"), messageContext));
        assertFalse(ResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://192.0.2.25/replies"), messageContext));
    }

    /**
     * Turning the range check off restores the pre-2.0.2 behaviour for operators
     * who need it.
     */
    public void testPrivateRangeCheckCanBeDisabled() throws Exception {
        setParameter(ResponseEndpointPolicy.BLOCK_PRIVATE_NETWORKS, "false");
        assertTrue(ResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://127.0.0.1:8080/sink"), messageContext));
    }

    public void testMalformedAddressIsRejected() {
        assertFalse(ResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://[not a uri"), messageContext));
        assertFalse(ResponseEndpointPolicy.isAllowed(
                new EndpointReference("http:///no-host"), messageContext));
    }
}
