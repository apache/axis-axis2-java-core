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

package org.apache.axis2.addressing;

import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;

/**
 * Unit tests for the egress policy applied to inbound WS-Addressing ReplyTo and
 * FaultTo endpoint references.
 */
public class AddressingResponseEndpointPolicyTest extends TestCase {

    private AxisConfiguration axisConfiguration;
    private MessageContext messageContext;

    protected void setUp() throws Exception {
        super.setUp();
        axisConfiguration = new AxisConfiguration();
        ConfigurationContext configurationContext = new ConfigurationContext(axisConfiguration);
        messageContext = configurationContext.createMessageContext();
        messageContext.setServerSide(true);
        // Decoupled responses are off by default. Most tests here cover what the
        // policy does to an endpoint it is actually willing to consider, so they
        // opt in; testDecoupledResponsesAreOffByDefault covers the default.
        setParameter(AddressingResponseEndpointPolicy.ALLOW_NON_ANONYMOUS, "true");
        // Only https is permitted by default. These tests exercise the address
        // checks over plain http for readability, so widen the list here;
        // testOnlyHttpsIsAllowedByDefault covers the shipped default.
        setParameter(AddressingResponseEndpointPolicy.ALLOWED_SCHEMES_PARAMETER, "http,https");
    }

    /**
     * Plain HTTP is not permitted unless asked for. This matters beyond tidiness:
     * the cloud instance-metadata endpoints are HTTP-only, so refusing the scheme
     * removes that target without relying on the address checks at all.
     */
    public void testOnlyHttpsIsAllowedByDefault() throws Exception {
        AxisConfiguration config = new AxisConfiguration();
        config.addParameter(
                new Parameter(AddressingResponseEndpointPolicy.ALLOW_NON_ANONYMOUS, "true"));
        MessageContext mc = new ConfigurationContext(config).createMessageContext();
        mc.setServerSide(true);

        assertTrue("https must be allowed", AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("https://192.0.2.25/cb"), mc));
        assertFalse("plain http must be opt-in", AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://192.0.2.25/cb"), mc));
        assertFalse("the metadata endpoint is refused on scheme alone",
                AddressingResponseEndpointPolicy.isAllowed(
                        new EndpointReference("http://169.254.169.254/latest/meta-data/"), mc));
        assertFalse("jms must be opt-in", AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("jms:/ReplyQueue"), mc));
        assertFalse("tcp must be opt-in", AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("tcp://192.0.2.25:6060/svc"), mc));
    }

    /** A context without the opt-in, for testing the shipped default. */
    private MessageContext defaultConfigured() throws Exception {
        AxisConfiguration config = new AxisConfiguration();
        MessageContext mc = new ConfigurationContext(config).createMessageContext();
        mc.setServerSide(true);
        return mc;
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
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(anonymous, messageContext));
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(null, messageContext));
    }

    /**
     * The cloud instance-metadata address is the canonical target for this class
     * of SSRF and is link-local, so the default policy must refuse it.
     */
    public void testInstanceMetadataAddressIsBlockedByDefault() {
        EndpointReference metadata =
                new EndpointReference("http://169.254.169.254/latest/meta-data/");
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(metadata, messageContext));
    }

    /**
     * A callback endpoint on loopback or inside the same private network is how
     * most real decoupled deployments are wired — Axis2's own
     * ThirdPartyResponseRawXMLTest replies to 127.0.0.1 — so the default policy
     * must not refuse them.
     */
    public void testLoopbackAndPrivateAddressesAllowedWhenDecoupledEnabled() {
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://127.0.0.1:8080/sink"), messageContext));
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://10.1.2.3/internal"), messageContext));
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://192.168.1.10/admin"), messageContext));
    }

    /** Opting in to the strict posture refuses them. */
    public void testLoopbackAndPrivateAddressesBlockedWhenOptedIn() throws Exception {
        setParameter(AddressingResponseEndpointPolicy.BLOCK_PRIVATE_NETWORKS, "true");
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://127.0.0.1:8080/sink"), messageContext));
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://10.1.2.3/internal"), messageContext));
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://192.168.1.10/admin"), messageContext));
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://172.16.5.5/admin"), messageContext));
    }

    /**
     * Wildcard and multicast are never a legitimate reply destination, so they
     * are refused without opting in.
     */
    public void testWildcardAndMulticastAlwaysBlocked() {
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://0.0.0.0/sink"), messageContext));
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://239.1.2.3/sink"), messageContext));
    }

    /**
     * IPv6 destinations are classified, not waved through.
     *
     * <p>Nothing here needed a code change — {@code URI.getHost} keeps the
     * brackets, {@code InetAddress.getByName} accepts that form, and the
     * {@code isLinkLocalAddress}/{@code isAnyLocalAddress}/
     * {@code isMulticastAddress} family is address-family agnostic. The suite
     * had no IPv6 case at all, though, so the behaviour was correct and
     * unverified, and a regression here would be silent. The equivalent checks
     * in Axis2/C had to be written by hand and were wrong until they were.
     */
    public void testIPv6DestinationsAreClassified() {
        // Refused whatever the configuration says.
        assertFalse("link-local", AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://[fe80::1]/sink"), messageContext));
        assertFalse("link-local with a port", AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://[fe80::1]:8080/sink"), messageContext));
        assertFalse("unspecified", AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://[::]/sink"), messageContext));
        assertFalse("multicast", AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://[ff02::1]/sink"), messageContext));

        // A global address is a legitimate destination; without this the rest
        // would pass just as well if IPv6 were refused wholesale.
        assertTrue("a global address must still be allowed",
                AddressingResponseEndpointPolicy.isAllowed(
                        new EndpointReference("http://[2001:db8::1]/sink"), messageContext));
    }

    /**
     * The IPv4-mapped form reaches the same metadata service the dotted quad
     * does, so it has to be refused the same way. The JDK resolves
     * {@code ::ffff:169.254.169.254} to an {@code Inet4Address}, which is what
     * makes this work without a special case — worth pinning, because it is a
     * property of the JDK rather than of this code.
     */
    public void testIPv4MappedMetadataAddressIsBlocked() {
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://[::ffff:169.254.169.254]/latest/meta-data/"),
                messageContext));
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://[::ffff:0.0.0.0]/sink"), messageContext));
    }

    /**
     * Loopback and unique-local follow the private-network switch, as their
     * IPv4 counterparts do.
     *
     * <p>{@code fc00::/7} is the one part of this that is not the JDK's doing:
     * {@code isSiteLocalAddress} answers for the deprecated {@code fec0::/10}
     * and returns false for a unique-local address, so {@code isUniqueLocalIPv6}
     * covers it. That is exactly the kind of gap this test exists to hold shut.
     */
    public void testIPv6LoopbackAndUniqueLocalFollowThePrivateSwitch() throws Exception {
        assertTrue("loopback is allowed until the switch is set",
                AddressingResponseEndpointPolicy.isAllowed(
                        new EndpointReference("http://[::1]/sink"), messageContext));
        assertTrue("unique-local is allowed until the switch is set",
                AddressingResponseEndpointPolicy.isAllowed(
                        new EndpointReference("http://[fd00::1]/sink"), messageContext));

        setParameter(AddressingResponseEndpointPolicy.BLOCK_PRIVATE_NETWORKS, "true");

        assertFalse("::1", AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://[::1]/sink"), messageContext));
        assertFalse("fd00::/8 unique-local", AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://[fd00::1]/sink"), messageContext));
        assertFalse("fc00::/7 unique-local", AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://[fc00::1]/sink"), messageContext));
        assertFalse("fec0::/10 site-local", AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://[fec0::1]/sink"), messageContext));
        assertFalse("v4-mapped loopback", AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://[::ffff:127.0.0.1]/sink"), messageContext));
    }

    /**
     * The schemes that only ever serve as an SSRF pivot are refused before any
     * host check.
     */
    public void testPivotSchemesAreRejected() {
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("file:///etc/passwd"), messageContext));
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("gopher://example.com/1"), messageContext));
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("jar:http://example.com/a.jar!/"), messageContext));
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("ftp://example.com/drop"), messageContext));
    }

    /**
     * A decoupled reply over JMS, mail or TCP is a legitimate Axis2
     * configuration — the response transport is resolved from the endpoint
     * reference's scheme against whatever is registered — so the policy must not
     * restrict replies to HTTP.
     */
    public void testNonHttpTransportSchemesAreAllowedWhenNamed() throws Exception {
        setParameter(AddressingResponseEndpointPolicy.ALLOWED_SCHEMES_PARAMETER,
                "http,https,jms,mailto,tcp");
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("jms:/ReplyQueue?transport.jms.ConnectionFactory=qcf"),
                messageContext));
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("mailto:replies@example.com"), messageContext));
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("tcp://192.0.2.25:6060/svc"), messageContext));
    }

    /**
     * The range check is about the destination address, not the scheme, so a
     * non-HTTP address that does name a host is still screened.
     */
    public void testNonHttpSchemeStillGetsTheRangeCheck() throws Exception {
        setParameter(AddressingResponseEndpointPolicy.ALLOWED_SCHEMES_PARAMETER, "http,https,tcp");
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("tcp://169.254.169.254:6060/svc"), messageContext));
    }

    public void testSchemeListIsConfigurable() throws Exception {
        setParameter(AddressingResponseEndpointPolicy.ALLOWED_SCHEMES_PARAMETER, "http,https");
        assertFalse("A scheme outside the configured list should be refused",
                AddressingResponseEndpointPolicy.isAllowed(
                        new EndpointReference("mailto:replies@example.com"), messageContext));
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://192.0.2.25/replies"), messageContext));
    }

    /**
     * A routable public address is still permitted by default, so decoupled
     * responses to a genuine external endpoint keep working.
     */
    public void testPublicAddressAllowedWhenDecoupledEnabled() {
        EndpointReference publicEpr = new EndpointReference("http://192.0.2.25/replies");
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(publicEpr, messageContext));
    }

    /**
     * The shipped default refuses every non-anonymous response endpoint, so no
     * inbound header can name an outbound destination at all — the same call
     * Apache CXF made in org.apache.cxf.ws.addressing.decoupled.enabled.
     */
    public void testDecoupledResponsesAreOffByDefault() throws Exception {
        MessageContext defaults = defaultConfigured();
        assertFalse("A public reply endpoint must be refused by default",
                AddressingResponseEndpointPolicy.isAllowed(
                        new EndpointReference("http://192.0.2.25/replies"), defaults));
        assertFalse("A private reply endpoint must be refused by default",
                AddressingResponseEndpointPolicy.isAllowed(
                        new EndpointReference("http://10.1.2.3/internal"), defaults));

        // Anonymous must still work by default, or ordinary in-out messaging breaks.
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL), defaults));
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(null, defaults));
    }

    /**
     * An explicit allow-list supersedes the network-range check, so a deployment
     * that really does reply into its own network can permit exactly that host.
     */
    public void testAllowListPermitsAnOtherwiseBlockedHost() throws Exception {
        setParameter(AddressingResponseEndpointPolicy.ALLOWED_HOSTS, "replies.example.com, 127.0.0.1");
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://127.0.0.1:8080/sink"), messageContext));
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://192.0.2.25/replies"), messageContext));
    }

    /**
     * The instance-metadata block holds even with the private-range option
     * explicitly off: link-local is refused unconditionally.
     */
    public void testMetadataAddressStillBlockedWithPrivateRangeCheckOff() throws Exception {
        setParameter(AddressingResponseEndpointPolicy.BLOCK_PRIVATE_NETWORKS, "false");
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://169.254.169.254/latest/meta-data/"), messageContext));
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://127.0.0.1:8080/sink"), messageContext));
    }

    /**
     * A name that cannot resolve is refused rather than passed through, and the
     * lookup is bounded so a slow resolver cannot hold the request thread. The
     * .invalid TLD is reserved by RFC 2606 precisely so it never resolves.
     */
    public void testUnresolvableHostIsRejected() {
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://no-such-host.invalid/replies"), messageContext));
    }

    /**
     * A literal address needs no name lookup at all, so the check costs nothing
     * on the request path for the direct-IP cases.
     */
    public void testLiteralAddressNeedsNoResolution() throws Exception {
        setParameter(AddressingResponseEndpointPolicy.RESOLVE_TIMEOUT, "1");
        // Would time out if this went to the resolver; it must not.
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://169.254.169.254/latest/meta-data/"), messageContext));
        assertTrue(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://192.0.2.25/replies"), messageContext));
    }

    public void testMalformedAddressIsRejected() {
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http://[not a uri"), messageContext));
        assertFalse(AddressingResponseEndpointPolicy.isAllowed(
                new EndpointReference("http:///no-host"), messageContext));
    }
}
