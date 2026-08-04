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

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Decides whether a {@code wsa:ReplyTo} or {@code wsa:FaultTo} endpoint
 * reference taken off an inbound message may be used as the destination of a
 * server-initiated send.
 *
 * <p>A non-anonymous response endpoint is a spec-defined WS-Addressing feature
 * — the SOAP binding says a response SHOULD be delivered to it — but the same
 * specification's security considerations put the burden of deciding whether to
 * honour one on the receiver, noting that "great care should be taken before
 * honoring a [reply endpoint] or [fault endpoint] to avoid inadvertent
 * participation in the activities of malicious SOAP message senders". Where
 * WS-Security is not engaged to bind the EPR to a trusted issuer, an anonymous
 * caller otherwise chooses the address the server connects to.
 *
 * <p>Three parameters control this, resolved through the usual Axis2 chain
 * (service, service group, then {@code axis2.xml}):
 *
 * <dl>
 * <dt>{@code allowNonAnonymousResponseEndpoints} (default {@code true})</dt>
 * <dd>Set to {@code false} to refuse every non-anonymous response endpoint, so
 * replies and faults only ever travel back down the inbound connection. This is
 * the strictest posture and the right one for a deployment that does not use
 * decoupled or dual-channel responses.</dd>
 *
 * <dt>{@code blockPrivateNetworkResponseEndpoints} (default {@code true})</dt>
 * <dd>Rejects response endpoints that resolve to loopback, link-local (which
 * covers the cloud instance-metadata addresses), site-local/RFC-1918, or
 * wildcard addresses — the destinations that turn this into a probe of the
 * server's own network rather than a genuine reply.</dd>
 *
 * <dt>{@code allowedResponseEndpointHosts} (no default)</dt>
 * <dd>A comma-separated host allow-list. When set, a response endpoint host must
 * appear in it, which supersedes the network-range check.</dd>
 * </dl>
 */
final class ResponseEndpointPolicy {

    private static final Log log = LogFactory.getLog(ResponseEndpointPolicy.class);

    static final String ALLOW_NON_ANONYMOUS = "allowNonAnonymousResponseEndpoints";
    static final String BLOCK_PRIVATE_NETWORKS = "blockPrivateNetworkResponseEndpoints";
    static final String ALLOWED_HOSTS = "allowedResponseEndpointHosts";

    /** Only transports that carry a genuine reply are worth honouring. */
    private static final Set<String> ALLOWED_SCHEMES =
            new HashSet<String>(Arrays.asList("http", "https"));

    private ResponseEndpointPolicy() {
    }

    /**
     * Whether the server may send to this response endpoint.
     *
     * <p>Anonymous and null endpoints are always permitted: they mean "reply on
     * the inbound connection" and drive no outbound connection at all.
     *
     * @param epr the ReplyTo or FaultTo taken from the inbound message
     * @param messageContext the inbound message, for parameter resolution
     * @return true if the endpoint may be used as a send destination
     */
    static boolean isAllowed(EndpointReference epr, MessageContext messageContext) {
        if (epr == null || epr.hasAnonymousAddress() || epr.hasNoneAddress()) {
            return true;
        }
        String address = epr.getAddress();
        if (address == null || address.trim().isEmpty()) {
            return true;
        }

        if (!booleanParameter(messageContext, ALLOW_NON_ANONYMOUS, true)) {
            log.warn("Rejecting non-anonymous WS-Addressing response endpoint: "
                    + ALLOW_NON_ANONYMOUS + " is false");
            return false;
        }

        URI uri;
        try {
            uri = new URI(address.trim());
        } catch (URISyntaxException e) {
            log.warn("Rejecting unparseable WS-Addressing response endpoint address");
            return false;
        }

        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ENGLISH))) {
            log.warn("Rejecting WS-Addressing response endpoint with unsupported scheme: " + scheme);
            return false;
        }

        String host = uri.getHost();
        if (host == null || host.isEmpty()) {
            log.warn("Rejecting WS-Addressing response endpoint with no host component");
            return false;
        }

        String allowedHosts = stringParameter(messageContext, ALLOWED_HOSTS);
        if (allowedHosts != null && !allowedHosts.trim().isEmpty()) {
            for (String allowed : allowedHosts.split(",")) {
                if (allowed.trim().equalsIgnoreCase(host)) {
                    return true;
                }
            }
            log.warn("Rejecting WS-Addressing response endpoint host absent from "
                    + ALLOWED_HOSTS);
            return false;
        }

        if (booleanParameter(messageContext, BLOCK_PRIVATE_NETWORKS, true)) {
            return !resolvesToRestrictedAddress(host);
        }

        return true;
    }

    /**
     * Whether a host resolves to an address the server should not be induced to
     * connect to. Every address the name resolves to has to pass, so a name with
     * one public and one loopback record is rejected.
     *
     * <p>This resolves the name to inspect it and the connection resolves it
     * again later, so a hostile DNS server could in principle answer differently
     * the second time. Closing that fully means resolving once and connecting to
     * the pinned address, which the transport layer does not currently support;
     * the check still removes the direct-IP and static-name cases that make this
     * reachable in practice.
     */
    private static boolean resolvesToRestrictedAddress(String host) {
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            // Unresolvable here means the send would fail anyway, so refuse
            // rather than pass an unknown destination through.
            log.warn("Rejecting WS-Addressing response endpoint with unresolvable host");
            return true;
        }
        for (int i = 0; i < addresses.length; i++) {
            InetAddress address = addresses[i];
            if (address.isLoopbackAddress()
                    || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress()
                    || address.isAnyLocalAddress()
                    || address.isMulticastAddress()
                    || isUniqueLocalIPv6(address)
                    || isSharedAddressSpace(address)) {
                log.warn("Rejecting WS-Addressing response endpoint resolving to a "
                        + "loopback, link-local, or private address");
                return true;
            }
        }
        return false;
    }

    /** IPv6 unique local addresses, fc00::/7, which isSiteLocalAddress misses. */
    private static boolean isUniqueLocalIPv6(InetAddress address) {
        byte[] bytes = address.getAddress();
        return bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
    }

    /** RFC 6598 carrier-grade NAT space, 100.64.0.0/10. */
    private static boolean isSharedAddressSpace(InetAddress address) {
        byte[] bytes = address.getAddress();
        return bytes.length == 4
                && (bytes[0] & 0xff) == 100
                && (bytes[1] & 0xc0) == 0x40;
    }

    private static boolean booleanParameter(MessageContext messageContext, String name,
                                            boolean defaultValue) {
        if (messageContext == null) {
            return defaultValue;
        }
        Parameter parameter = messageContext.getParameter(name);
        if (parameter == null || parameter.getValue() == null) {
            return defaultValue;
        }
        if (defaultValue) {
            return !JavaUtils.isFalseExplicitly(parameter.getValue());
        }
        return JavaUtils.isTrueExplicitly(parameter.getValue());
    }

    private static String stringParameter(MessageContext messageContext, String name) {
        if (messageContext == null) {
            return null;
        }
        Parameter parameter = messageContext.getParameter(name);
        if (parameter == null || parameter.getValue() == null) {
            return null;
        }
        return parameter.getValue().toString();
    }
}
