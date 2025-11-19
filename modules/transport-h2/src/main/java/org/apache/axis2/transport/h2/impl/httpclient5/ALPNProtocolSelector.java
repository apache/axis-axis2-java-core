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

package org.apache.axis2.transport.h2.impl.httpclient5;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ALPN (Application-Layer Protocol Negotiation) Protocol Selector for HTTP/2.
 *
 * This selector handles the negotiation between HTTP/2 and HTTP/1.1 protocols
 * over secure HTTPS connections using ALPN extension (RFC 7301).
 *
 * Key Features:
 * - Automatic HTTP/2 and HTTP/1.1 protocol negotiation
 * - ALPN extension support for TLS connections
 * - Fallback strategy configuration for unsupported protocols
 * - Protocol preference ordering and selection
 * - Enterprise security compliance and validation
 * - Comprehensive negotiation monitoring and logging
 *
 * ALPN Protocol Identifiers:
 * - "h2" - HTTP/2 over TLS
 * - "http/1.1" - HTTP/1.1 over TLS
 *
 * Production Benefits:
 * - Seamless protocol negotiation for optimal performance
 * - Automatic fallback to HTTP/1.1 when HTTP/2 not supported
 * - Standards-compliant ALPN negotiation (RFC 7301)
 * - Enhanced security with proper TLS configuration
 */
public class ALPNProtocolSelector {

    private static final Log log = LogFactory.getLog(ALPNProtocolSelector.class);

    // Standard ALPN protocol identifiers
    public static final String ALPN_HTTP2 = "h2";
    public static final String ALPN_HTTP1_1 = "http/1.1";

    // Protocol preference ordering (most preferred first)
    private static final List<String> DEFAULT_PROTOCOL_PREFERENCES = Arrays.asList(
        ALPN_HTTP2,     // Prefer HTTP/2
        ALPN_HTTP1_1    // Fallback to HTTP/1.1
    );

    /**
     * ALPN negotiation result.
     */
    public enum NegotiationResult {
        HTTP2_SELECTED("HTTP/2 protocol selected"),
        HTTP1_SELECTED("HTTP/1.1 protocol selected"),
        NEGOTIATION_FAILED("ALPN negotiation failed"),
        ALPN_NOT_SUPPORTED("ALPN not supported by server"),
        TIMEOUT("Protocol negotiation timeout");

        private final String description;

        NegotiationResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean isHttp2() {
            return this == HTTP2_SELECTED;
        }

        public boolean isHttp1() {
            return this == HTTP1_SELECTED;
        }

        public boolean isSuccessful() {
            return this == HTTP2_SELECTED || this == HTTP1_SELECTED;
        }
    }

    /**
     * ALPN configuration settings.
     */
    public static class ALPNConfig {
        private final List<String> protocolPreferences;
        private final boolean requireHttp2;
        private final boolean allowFallback;
        private final int negotiationTimeoutMs;

        public ALPNConfig(List<String> protocolPreferences, boolean requireHttp2,
                         boolean allowFallback, int negotiationTimeoutMs) {
            this.protocolPreferences = protocolPreferences != null ?
                                     protocolPreferences : DEFAULT_PROTOCOL_PREFERENCES;
            this.requireHttp2 = requireHttp2;
            this.allowFallback = allowFallback;
            this.negotiationTimeoutMs = negotiationTimeoutMs;
        }

        public static ALPNConfig defaultConfig() {
            return new ALPNConfig(DEFAULT_PROTOCOL_PREFERENCES, false, true, 5000);
        }

        public static ALPNConfig http2Required() {
            return new ALPNConfig(Arrays.asList(ALPN_HTTP2), true, false, 5000);
        }

        public static ALPNConfig enterpriseConfig() {
            return new ALPNConfig(DEFAULT_PROTOCOL_PREFERENCES, false, true, 10000);
        }

        public List<String> getProtocolPreferences() { return protocolPreferences; }
        public boolean isRequireHttp2() { return requireHttp2; }
        public boolean isAllowFallback() { return allowFallback; }
        public int getNegotiationTimeoutMs() { return negotiationTimeoutMs; }
    }

    // Configuration
    private final ALPNConfig config;

    // Metrics
    private final AtomicLong totalNegotiations = new AtomicLong(0);
    private final AtomicLong http2Negotiations = new AtomicLong(0);
    private final AtomicLong http1Negotiations = new AtomicLong(0);
    private final AtomicLong failedNegotiations = new AtomicLong(0);
    private final AtomicLong timeoutNegotiations = new AtomicLong(0);

    public ALPNProtocolSelector(ALPNConfig config) {
        this.config = config != null ? config : ALPNConfig.defaultConfig();

        log.info("ALPNProtocolSelector initialized - Protocols: " + this.config.getProtocolPreferences() +
                ", Require HTTP/2: " + this.config.isRequireHttp2() +
                ", Allow Fallback: " + this.config.isAllowFallback() +
                ", Timeout: " + this.config.getNegotiationTimeoutMs() + "ms");
    }

    /**
     * Default constructor with enterprise configuration.
     */
    public ALPNProtocolSelector() {
        this(ALPNConfig.enterpriseConfig());
    }

    /**
     * Create HTTP client with ALPN protocol negotiation support.
     */
    public CloseableHttpAsyncClient createALPNEnabledClient(PoolingAsyncClientConnectionManager connectionManager)
            throws Exception {
        totalNegotiations.incrementAndGet();

        log.debug("Creating ALPN-enabled HTTP client with protocol preferences: " + config.getProtocolPreferences());

        try {
            // Create TLS strategy with ALPN support
            TlsStrategy tlsStrategy = createALPNTlsStrategy();

            // Create connection manager with ALPN TLS strategy
            PoolingAsyncClientConnectionManager alpnConnectionManager =
                PoolingAsyncClientConnectionManagerBuilder.create()
                    .setTlsStrategy(tlsStrategy)
                    .setMaxConnTotal(connectionManager.getMaxTotal())
                    .setMaxConnPerRoute(connectionManager.getDefaultMaxPerRoute())
                    .build();

            // Configure HTTP client with ALPN-enabled connection manager
            CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                    .setConnectionManager(alpnConnectionManager)
                    .setVersionPolicy(HttpVersionPolicy.NEGOTIATE) // Allow protocol negotiation
                    .build();

            // Start the client
            client.start();

            log.info("ALPN-enabled HTTP client created successfully");
            return client;

        } catch (Exception e) {
            failedNegotiations.incrementAndGet();
            log.error("Failed to create ALPN-enabled HTTP client: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Create TLS strategy with ALPN support.
     */
    private TlsStrategy createALPNTlsStrategy() throws Exception {
        // Create SSL context with ALPN support
        SSLContext sslContext = createALPNSSLContext();

        // Build TLS strategy with ALPN protocols
        return ClientTlsStrategyBuilder.create()
                .setSslContext(sslContext)
                .setTlsDetailsFactory(sslEngine -> {
                    // Configure ALPN protocols
                    SSLParameters sslParams = sslEngine.getSSLParameters();
                    String[] protocols = config.getProtocolPreferences().toArray(new String[0]);
                    sslParams.setApplicationProtocols(protocols);
                    sslEngine.setSSLParameters(sslParams);

                    log.debug("Configured ALPN protocols: " + Arrays.toString(protocols));
                    return null; // Return null for default TLS details
                })
                .build();
    }

    /**
     * Create SSL context with ALPN support.
     */
    private SSLContext createALPNSSLContext() throws Exception {
        // Use default SSL context with ALPN support
        SSLContext sslContext = SSLContexts.custom()
                .setSecureRandom(new SecureRandom())
                .build();

        log.debug("Created SSL context with ALPN support");
        return sslContext;
    }

    /**
     * Determine negotiation result from SSL engine.
     */
    public NegotiationResult determineNegotiationResult(SSLEngine sslEngine) {
        try {
            if (sslEngine == null) {
                failedNegotiations.incrementAndGet();
                return NegotiationResult.NEGOTIATION_FAILED;
            }

            // Get the negotiated application protocol
            String negotiatedProtocol = sslEngine.getApplicationProtocol();

            if (negotiatedProtocol == null || negotiatedProtocol.isEmpty()) {
                // No ALPN negotiation occurred
                if (config.isAllowFallback()) {
                    http1Negotiations.incrementAndGet();
                    log.info("No ALPN negotiation, falling back to HTTP/1.1");
                    return NegotiationResult.HTTP1_SELECTED;
                } else {
                    failedNegotiations.incrementAndGet();
                    log.warn("ALPN negotiation failed and fallback not allowed");
                    return NegotiationResult.ALPN_NOT_SUPPORTED;
                }
            }

            // Process negotiated protocol
            switch (negotiatedProtocol) {
                case ALPN_HTTP2:
                    http2Negotiations.incrementAndGet();
                    log.info("ALPN negotiated HTTP/2 protocol");
                    return NegotiationResult.HTTP2_SELECTED;

                case ALPN_HTTP1_1:
                    http1Negotiations.incrementAndGet();
                    log.info("ALPN negotiated HTTP/1.1 protocol");
                    return NegotiationResult.HTTP1_SELECTED;

                default:
                    failedNegotiations.incrementAndGet();
                    log.warn("ALPN negotiated unknown protocol: " + negotiatedProtocol);
                    return NegotiationResult.NEGOTIATION_FAILED;
            }

        } catch (Exception e) {
            failedNegotiations.incrementAndGet();
            log.error("Error determining negotiation result: " + e.getMessage(), e);
            return NegotiationResult.NEGOTIATION_FAILED;
        }
    }

    /**
     * Validate ALPN support in current Java environment.
     */
    public static boolean isALPNSupported() {
        try {
            // Check if ALPN is available in the current JVM
            SSLContext sslContext = SSLContext.getDefault();
            SSLEngine sslEngine = sslContext.createSSLEngine();

            // Try to set application protocols
            SSLParameters sslParams = sslEngine.getSSLParameters();
            sslParams.setApplicationProtocols(new String[]{ALPN_HTTP2, ALPN_HTTP1_1});
            sslEngine.setSSLParameters(sslParams);

            log.info("ALPN support validated successfully");
            return true;

        } catch (Exception e) {
            log.warn("ALPN not supported in current environment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get preferred protocol for negotiation.
     */
    public String getPreferredProtocol() {
        return config.getProtocolPreferences().isEmpty() ? ALPN_HTTP2 : config.getProtocolPreferences().get(0);
    }

    /**
     * Check if HTTP/2 is preferred protocol.
     */
    public boolean isHttp2Preferred() {
        return ALPN_HTTP2.equals(getPreferredProtocol());
    }

    /**
     * Get comprehensive ALPN negotiation metrics.
     */
    public ALPNMetrics getMetrics() {
        return new ALPNMetrics(
                totalNegotiations.get(),
                http2Negotiations.get(),
                http1Negotiations.get(),
                failedNegotiations.get(),
                timeoutNegotiations.get()
        );
    }

    /**
     * ALPN negotiation metrics container.
     */
    public static class ALPNMetrics {
        public final long totalNegotiations;
        public final long http2Negotiations;
        public final long http1Negotiations;
        public final long failedNegotiations;
        public final long timeoutNegotiations;

        public ALPNMetrics(long totalNegotiations, long http2Negotiations,
                          long http1Negotiations, long failedNegotiations, long timeoutNegotiations) {
            this.totalNegotiations = totalNegotiations;
            this.http2Negotiations = http2Negotiations;
            this.http1Negotiations = http1Negotiations;
            this.failedNegotiations = failedNegotiations;
            this.timeoutNegotiations = timeoutNegotiations;
        }

        public double getHttp2Rate() {
            return totalNegotiations > 0 ? (double) http2Negotiations / totalNegotiations : 0.0;
        }

        public double getSuccessRate() {
            long successful = http2Negotiations + http1Negotiations;
            return totalNegotiations > 0 ? (double) successful / totalNegotiations : 0.0;
        }

        @Override
        public String toString() {
            return String.format("ALPNMetrics[total=%d, http2=%d, http1=%d, failed=%d, timeout=%d, " +
                               "http2Rate=%.2f%%, successRate=%.2f%%]",
                               totalNegotiations, http2Negotiations, http1Negotiations,
                               failedNegotiations, timeoutNegotiations,
                               getHttp2Rate() * 100, getSuccessRate() * 100);
        }
    }

    /**
     * Get ALPN configuration.
     */
    public ALPNConfig getConfig() {
        return config;
    }

    /**
     * Create configuration for HTTP/2 only (no fallback).
     */
    public static ALPNConfig createHttp2OnlyConfig(int timeoutMs) {
        return new ALPNConfig(Arrays.asList(ALPN_HTTP2), true, false, timeoutMs);
    }

    /**
     * Create configuration with custom protocol preferences.
     */
    public static ALPNConfig createCustomConfig(List<String> protocols, boolean allowFallback, int timeoutMs) {
        return new ALPNConfig(protocols, false, allowFallback, timeoutMs);
    }
}