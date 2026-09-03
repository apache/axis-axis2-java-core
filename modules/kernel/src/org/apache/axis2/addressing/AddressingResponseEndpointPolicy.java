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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
 * <dt>{@code allowNonAnonymousResponseEndpoints} (default {@code false})</dt>
 * <dd>Off by default, so replies and faults only ever travel back down the
 * inbound connection and an inbound header cannot name an outbound destination
 * at all. Apache CXF made the same call for the same reason in its
 * {@code org.apache.cxf.ws.addressing.decoupled.enabled} property. Set it to
 * {@code true} for a deployment that genuinely uses decoupled responses — the
 * separate-listener ("Dual") clients, or a third-party callback endpoint.</dd>
 *
 * <dt>{@code blockPrivateNetworkResponseEndpoints} (default {@code false})</dt>
 * <dd>Additionally rejects response endpoints that resolve to loopback,
 * site-local/RFC-1918, IPv6 unique-local or carrier-grade-NAT addresses. This is
 * off by default because a callback endpoint inside the same private network is
 * how most real decoupled deployments are wired, and refusing it would break
 * them; turn it on wherever the caller is not already inside the trusted
 * network. Link-local, wildcard and multicast destinations are refused
 * regardless — see {@link #resolvesToRestrictedAddress}.</dd>
 *
 * <dt>{@code allowedResponseEndpointHosts} (no default)</dt>
 * <dd>A comma-separated host allow-list. When set, a response endpoint host must
 * appear in it, which supersedes the network-range check.</dd>
 *
 * <dt>{@code allowedResponseEndpointSchemes} (default {@code https})</dt>
 * <dd>A comma-separated scheme allow-list, applied once decoupled responses are
 * enabled at all. See {@link #DEFAULT_ALLOWED_SCHEMES} for why the default is
 * HTTPS alone.</dd>
 * </dl>
 */
public final class AddressingResponseEndpointPolicy {

    private static final Log log = LogFactory.getLog(AddressingResponseEndpointPolicy.class);

    public static final String ALLOW_NON_ANONYMOUS = "allowNonAnonymousResponseEndpoints";
    public static final String BLOCK_PRIVATE_NETWORKS = "blockPrivateNetworkResponseEndpoints";
    public static final String ALLOWED_HOSTS = "allowedResponseEndpointHosts";
    public static final String ALLOWED_SCHEMES_PARAMETER = "allowedResponseEndpointSchemes";
    public static final String RESOLVE_TIMEOUT = "responseEndpointResolveTimeoutMillis";

    /** Long enough for a healthy resolver, short enough not to pin a thread. */
    static final long DEFAULT_RESOLVE_TIMEOUT_MILLIS = 2000L;

    private static ExecutorService resolver;

    /** Caps concurrent name lookups so a burst cannot spawn a thread per request. */
    private static final int MAX_RESOLVER_THREADS = 4;

    /** Lookups beyond this wait; beyond the wait they are refused. */
    private static final int RESOLVER_QUEUE_DEPTH = 64;

    /**
     * Schemes a decoupled reply may use, unless {@link #ALLOWED_SCHEMES_PARAMETER}
     * widens it. Just {@code https}.
     *
     * <p>Plain {@code http} is excluded deliberately, and it is the exclusion that
     * carries the most weight: the cloud instance-metadata services this class of
     * SSRF is usually aimed at — AWS and Azure on 169.254.169.254, GCP on
     * metadata.google.internal — are reachable over HTTP only and offer no HTTPS
     * listener. Refusing plain HTTP therefore takes that target away by scheme
     * alone, without depending on the address checks below.
     *
     * <p>{@code jms}, {@code mailto} and {@code tcp} have senders in the tree but
     * are vanishingly rare as reply destinations, so they are opt-in rather than
     * default-on. A deployment that needs any of these, or that genuinely replies
     * over plain HTTP inside a trusted network, names them in
     * {@link #ALLOWED_SCHEMES_PARAMETER}.
     */
    private static final Set<String> DEFAULT_ALLOWED_SCHEMES =
            new HashSet<String>(Arrays.asList("https"));

    /** Schemes for which a missing host means the address is unusable. */
    private static final Set<String> HOST_BEARING_SCHEMES =
            new HashSet<String>(Arrays.asList("http", "https"));

    private AddressingResponseEndpointPolicy() {
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
    public static boolean isAllowed(EndpointReference epr, MessageContext messageContext) {
        if (epr == null || epr.hasAnonymousAddress() || epr.hasNoneAddress()) {
            return true;
        }
        String address = epr.getAddress();
        if (address == null || address.trim().isEmpty()) {
            return true;
        }

        if (!booleanParameter(messageContext, ALLOW_NON_ANONYMOUS, false)) {
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
        if (scheme == null) {
            log.warn("Rejecting WS-Addressing response endpoint with no scheme");
            return false;
        }
        scheme = scheme.toLowerCase(Locale.ENGLISH);
        if (!allowedSchemes(messageContext).contains(scheme)) {
            log.warn("Rejecting WS-Addressing response endpoint with unsupported scheme: " + scheme);
            return false;
        }

        String host = uri.getHost();
        if (host == null || host.isEmpty()) {
            if (HOST_BEARING_SCHEMES.contains(scheme)) {
                log.warn("Rejecting WS-Addressing response endpoint with no host component");
                return false;
            }
            // mailto: and JNDI-style jms: addresses name a destination rather
            // than a network host, so there is no address to range-check. The
            // transport behind them has to be enabled by an administrator.
            //
            // Returning true here screens nothing about the rest of the address,
            // and for these schemes the query string is not inert: a jms: EPR
            // carries the JNDI environment used to resolve the destination. That
            // is screened where the meaning of those parameters is known, in
            // JMSSender, which refuses a caller-supplied java.naming.* on a
            // decoupled response. Do not read this early return as a statement
            // that host-less addresses are harmless.
            return true;
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

        return !resolvesToRestrictedAddress(host,
                booleanParameter(messageContext, BLOCK_PRIVATE_NETWORKS, false),
                longParameter(messageContext, RESOLVE_TIMEOUT, DEFAULT_RESOLVE_TIMEOUT_MILLIS));
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
    private static boolean resolvesToRestrictedAddress(String host, boolean blockPrivateNetworks,
                                                       long resolveTimeoutMillis) {
        InetAddress[] addresses;
        if (isIpLiteral(host)) {
            // No name to look up, so the common cases — a direct IP, including
            // the instance-metadata address — cost nothing.
            try {
                addresses = new InetAddress[] { InetAddress.getByName(host) };
            } catch (UnknownHostException e) {
                log.warn("Rejecting WS-Addressing response endpoint with unparseable address");
                return true;
            }
        } else {
            addresses = resolveWithTimeout(host, resolveTimeoutMillis);
            if (addresses == null) {
                // Unresolved here means the send would fail or hang anyway, so
                // refuse rather than pass an unknown destination through.
                return true;
            }
        }
        for (int i = 0; i < addresses.length; i++) {
            InetAddress address = addresses[i];

            // Never a legitimate destination for a reply, so these are refused
            // whatever the configuration says. Link-local covers the cloud
            // instance-metadata addresses, which is what gives this class of
            // SSRF most of its impact.
            if (address.isLinkLocalAddress()
                    || address.isAnyLocalAddress()
                    || address.isMulticastAddress()) {
                log.warn("Rejecting WS-Addressing response endpoint resolving to a "
                        + "link-local, wildcard, or multicast address");
                return true;
            }

            // Loopback and private ranges, by contrast, are where a great many
            // real decoupled deployments put their callback endpoint: both ends
            // of an intranet dual-channel exchange are usually on RFC 1918. So
            // this is opt-in, and worth enabling anywhere the caller is not
            // already inside the trusted network.
            if (blockPrivateNetworks
                    && (address.isLoopbackAddress()
                        || address.isSiteLocalAddress()
                        || isUniqueLocalIPv6(address)
                        || isSharedAddressSpace(address))) {
                log.warn("Rejecting WS-Addressing response endpoint resolving to a "
                        + "loopback or private address (" + BLOCK_PRIVATE_NETWORKS + " is true)");
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the host is already a literal address, so no name lookup is needed.
     * Deliberately syntactic — anything with only hex digits, dots, colons and
     * brackets cannot be a registered name.
     */
    private static boolean isIpLiteral(String host) {
        for (int i = 0; i < host.length(); i++) {
            char c = host.charAt(i);
            boolean literalChar = (c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')
                    || c == '.' || c == ':' || c == '[' || c == ']' || c == '%';
            if (!literalChar) {
                return false;
            }
        }
        return host.indexOf(':') >= 0 || host.indexOf('.') >= 0;
    }

    /**
     * Resolve a host name without letting a slow or unresponsive DNS server hold
     * the request thread.
     *
     * <p>The lookup runs on a separate thread so the caller can give up on it.
     * {@code InetAddress} exposes no timeout of its own, and this runs on the
     * request path, so without a bound a caller could pin threads simply by
     * naming hosts that resolve slowly.
     *
     * @return the resolved addresses, or null if the name did not resolve in time
     */
    private static InetAddress[] resolveWithTimeout(final String host, long timeoutMillis) {
        Future<InetAddress[]> pending;
        try {
            pending = resolver().submit(new Callable<InetAddress[]>() {
                public InetAddress[] call() throws UnknownHostException {
                    return InetAddress.getAllByName(host);
                }
            });
        } catch (RejectedExecutionException e) {
            log.warn("Rejecting WS-Addressing response endpoint: too many name lookups in flight");
            return null;
        }
        try {
            return pending.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            pending.cancel(true);
            log.warn("Rejecting WS-Addressing response endpoint: host did not resolve within "
                    + timeoutMillis + "ms");
            return null;
        } catch (ExecutionException e) {
            log.warn("Rejecting WS-Addressing response endpoint with unresolvable host");
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pending.cancel(true);
            return null;
        }
    }

    /**
     * The pool that runs name lookups, created on first use.
     *
     * <p>Deliberately bounded. An unbounded pool would have swapped one denial
     * of service for another: a caller naming many distinct slow-resolving hosts
     * could spawn a thread per request. Excess lookups queue, and once the queue
     * is full they are refused outright — which fails closed, since a refused
     * lookup rejects the endpoint.
     *
     * <p>Threads are daemons and retire when idle, and {@link #shutdown()} stops
     * the pool so it cannot pin a web application's class loader across a
     * redeployment.
     */
    private static synchronized ExecutorService resolver() {
        if (resolver == null) {
            ThreadPoolExecutor pool = new ThreadPoolExecutor(
                    0, MAX_RESOLVER_THREADS,
                    60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(RESOLVER_QUEUE_DEPTH),
                    new ThreadFactory() {
                        public Thread newThread(Runnable r) {
                            Thread t = new Thread(r, "axis2-wsa-endpoint-resolver");
                            t.setDaemon(true);
                            return t;
                        }
                    },
                    new ThreadPoolExecutor.AbortPolicy());
            pool.allowCoreThreadTimeOut(true);
            resolver = pool;
        }
        return resolver;
    }

    /**
     * Stop the resolver pool. Called from the addressing module's shutdown so
     * the threads do not outlive the configuration that created them.
     */
    public static synchronized void shutdown() {
        if (resolver != null) {
            resolver.shutdownNow();
            resolver = null;
        }
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

    /**
     * The permitted scheme set, overridable so a deployment with a custom
     * transport can name its scheme without having to disable the policy.
     */
    private static Set<String> allowedSchemes(MessageContext messageContext) {
        String configured = stringParameter(messageContext, ALLOWED_SCHEMES_PARAMETER);
        if (configured == null || configured.trim().isEmpty()) {
            return DEFAULT_ALLOWED_SCHEMES;
        }
        Set<String> schemes = new HashSet<String>();
        String[] parts = configured.split(",");
        for (int i = 0; i < parts.length; i++) {
            String scheme = parts[i].trim().toLowerCase(Locale.ENGLISH);
            if (!scheme.isEmpty()) {
                schemes.add(scheme);
            }
        }
        return schemes;
    }

    private static long longParameter(MessageContext messageContext, String name,
                                      long defaultValue) {
        String value = stringParameter(messageContext, name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            log.warn("Ignoring non-numeric value '" + value + "' for parameter '" + name
                    + "'; using the default of " + defaultValue + "ms");
            return defaultValue;
        }
    }

    /**
     * Whether this send is a WS-Addressing decoupled response: a server-side send to
     * a destination the caller named, rather than an ordinary client-side request.
     * <p>
     * Transports use this to decide whether to follow redirects. The scheme allow-list
     * and the address checks in {@link #isAllowed} run against the endpoint reference
     * the caller supplied, and nothing re-examines where a redirect leads, so following
     * one steps straight past them: a 307 from the caller's own endpoint to an address
     * this policy had already refused would be honoured. A decoupled reply is
     * fire-and-forget, so nothing legitimate depends on following a redirect.
     *
     * @param messageContext the message being sent; may be {@code null}
     * @return whether the destination was nominated by the caller
     */
    public static boolean isDecoupledResponse(MessageContext messageContext) {
        if (messageContext == null || !messageContext.isServerSide()) {
            return false;
        }
        EndpointReference to = messageContext.getTo();
        return to != null && !to.hasAnonymousAddress() && !to.hasNoneAddress();
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
