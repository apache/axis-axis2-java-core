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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.impl.httpclient5.HTTPClient5TransportSender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * HTTP/2 to HTTP/1.1 Fallback Manager for Enterprise Production Environments.
 *
 * This manager provides seamless degradation from HTTP/2 to HTTP/1.1 when:
 * - HTTP/2 protocol negotiation fails
 * - Server doesn't support HTTP/2
 * - Network connectivity issues with HTTP/2
 * - ALPN negotiation timeout occurs
 * - HTTP/2 specific errors are encountered
 *
 * Key Features:
 * - Automatic fallback detection and execution
 * - Host-based fallback caching to avoid repeated attempts
 * - Configurable fallback strategies and timeouts
 * - Performance monitoring and fallback statistics
 * - Enterprise-grade error handling and logging
 *
 * Production Benefits:
 * - Zero-downtime degradation for unsupported endpoints
 * - Maintains service availability during protocol issues
 * - Optimizes performance by caching fallback decisions
 * - Provides comprehensive monitoring and diagnostics
 */
public class H2FallbackManager {

    private static final Log log = LogFactory.getLog(H2FallbackManager.class);

    // Fallback configuration constants
    private static final long DEFAULT_FALLBACK_CACHE_TTL = 300000; // 5 minutes
    private static final int DEFAULT_PROTOCOL_NEGOTIATION_TIMEOUT = 5000; // 5 seconds
    private static final int MAX_HTTP2_RETRY_ATTEMPTS = 2;

    // Fallback reasons
    public enum FallbackReason {
        PROTOCOL_NEGOTIATION_FAILED("HTTP/2 protocol negotiation failed"),
        ALPN_NOT_SUPPORTED("ALPN not supported by server"),
        CONNECTION_TIMEOUT("HTTP/2 connection timeout"),
        HTTP2_ERROR("HTTP/2 protocol error"),
        SERVER_NOT_SUPPORTED("Server doesn't support HTTP/2"),
        NETWORK_ERROR("Network error during HTTP/2 connection"),
        CONFIGURATION_ERROR("HTTP/2 configuration error");

        private final String description;

        FallbackReason(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Fallback strategy configuration.
     */
    public enum FallbackStrategy {
        IMMEDIATE("Immediate fallback on first failure"),
        RETRY_ONCE("Retry HTTP/2 once, then fallback"),
        RETRY_TWICE("Retry HTTP/2 twice, then fallback"),
        ADAPTIVE("Adaptive strategy based on error type");

        private final String description;

        FallbackStrategy(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Fallback cache entry for host-specific decisions.
     */
    private static class FallbackCacheEntry {
        private final boolean shouldFallback;
        private final FallbackReason reason;
        private final long timestamp;
        private final long ttl;

        public FallbackCacheEntry(boolean shouldFallback, FallbackReason reason, long ttl) {
            this.shouldFallback = shouldFallback;
            this.reason = reason;
            this.timestamp = System.currentTimeMillis();
            this.ttl = ttl;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ttl;
        }

        public boolean shouldFallback() {
            return shouldFallback;
        }

        public FallbackReason getReason() {
            return reason;
        }
    }

    // Configuration
    private final boolean fallbackEnabled;
    private final FallbackStrategy fallbackStrategy;
    private final long fallbackCacheTtl;
    private final int protocolNegotiationTimeout;

    // State management
    private final ConcurrentHashMap<String, FallbackCacheEntry> fallbackCache;
    private final HTTPClient5TransportSender http1FallbackSender;

    // Metrics
    private final AtomicLong totalFallbackAttempts = new AtomicLong(0);
    private final AtomicLong successfulFallbacks = new AtomicLong(0);
    private final AtomicLong failedFallbacks = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);

    public H2FallbackManager(boolean fallbackEnabled, FallbackStrategy fallbackStrategy,
                           long fallbackCacheTtl, int protocolNegotiationTimeout) {
        this.fallbackEnabled = fallbackEnabled;
        this.fallbackStrategy = fallbackStrategy != null ? fallbackStrategy : FallbackStrategy.ADAPTIVE;
        this.fallbackCacheTtl = fallbackCacheTtl > 0 ? fallbackCacheTtl : DEFAULT_FALLBACK_CACHE_TTL;
        this.protocolNegotiationTimeout = protocolNegotiationTimeout > 0 ?
                                        protocolNegotiationTimeout : DEFAULT_PROTOCOL_NEGOTIATION_TIMEOUT;

        this.fallbackCache = new ConcurrentHashMap<>();
        this.http1FallbackSender = new HTTPClient5TransportSender();

        log.info("H2FallbackManager initialized - Enabled: " + fallbackEnabled +
                ", Strategy: " + this.fallbackStrategy +
                ", Cache TTL: " + this.fallbackCacheTtl + "ms" +
                ", Negotiation Timeout: " + this.protocolNegotiationTimeout + "ms");
    }

    /**
     * Default constructor with standard enterprise settings.
     */
    public H2FallbackManager() {
        this(true, FallbackStrategy.ADAPTIVE, DEFAULT_FALLBACK_CACHE_TTL, DEFAULT_PROTOCOL_NEGOTIATION_TIMEOUT);
    }

    /**
     * Check if fallback should be attempted for the given host.
     */
    public boolean shouldAttemptFallback(String host, FallbackReason reason) {
        if (!fallbackEnabled) {
            return false;
        }

        String cacheKey = getCacheKey(host);
        FallbackCacheEntry cacheEntry = fallbackCache.get(cacheKey);

        // Check cache first
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            cacheHits.incrementAndGet();
            log.debug("Fallback cache hit for host " + host + ": " + cacheEntry.shouldFallback());
            return cacheEntry.shouldFallback();
        }

        // Determine fallback based on strategy and reason
        boolean shouldFallback = determineFallbackDecision(reason);

        // Cache the decision
        cacheFallbackDecision(host, shouldFallback, reason);

        log.info("Fallback decision for host " + host + ": " + shouldFallback +
                " (Reason: " + reason.getDescription() + ")");

        return shouldFallback;
    }

    /**
     * Execute fallback to HTTP/1.1 for the given message context.
     */
    public void executeFallback(MessageContext messageContext, FallbackReason reason) throws AxisFault {
        totalFallbackAttempts.incrementAndGet();

        String host = extractHostFromMessageContext(messageContext);
        String logPrefix = "H2FallbackManager.executeFallback() [" + host + "] - ";

        try {
            log.info(logPrefix + "Executing HTTP/2 to HTTP/1.1 fallback. Reason: " + reason.getDescription());

            // Update message context for HTTP/1.1
            prepareMessageContextForFallback(messageContext);

            // Execute using HTTP/1.1 transport sender
            http1FallbackSender.invoke(messageContext);

            successfulFallbacks.incrementAndGet();
            log.info(logPrefix + "Fallback completed successfully");

        } catch (Exception e) {
            failedFallbacks.incrementAndGet();
            log.error(logPrefix + "Fallback failed: " + e.getMessage(), e);
            throw new AxisFault("HTTP/2 fallback to HTTP/1.1 failed", e);
        }
    }

    /**
     * Determine fallback decision based on strategy and reason.
     */
    private boolean determineFallbackDecision(FallbackReason reason) {
        switch (fallbackStrategy) {
            case IMMEDIATE:
                return true;

            case RETRY_ONCE:
            case RETRY_TWICE:
                // For now, always fallback - retry logic can be added later
                return true;

            case ADAPTIVE:
                return isReasonSuitableForFallback(reason);

            default:
                return true;
        }
    }

    /**
     * Check if the failure reason is suitable for fallback.
     */
    private boolean isReasonSuitableForFallback(FallbackReason reason) {
        switch (reason) {
            case PROTOCOL_NEGOTIATION_FAILED:
            case ALPN_NOT_SUPPORTED:
            case SERVER_NOT_SUPPORTED:
                return true; // These are permanent issues, fallback immediately

            case CONNECTION_TIMEOUT:
            case NETWORK_ERROR:
                return true; // Network issues may benefit from HTTP/1.1

            case HTTP2_ERROR:
            case CONFIGURATION_ERROR:
                return false; // These might be fixable, don't fallback immediately

            default:
                return true;
        }
    }

    /**
     * Cache the fallback decision for the host.
     */
    private void cacheFallbackDecision(String host, boolean shouldFallback, FallbackReason reason) {
        String cacheKey = getCacheKey(host);
        FallbackCacheEntry entry = new FallbackCacheEntry(shouldFallback, reason, fallbackCacheTtl);
        fallbackCache.put(cacheKey, entry);

        log.debug("Cached fallback decision for " + host + ": " + shouldFallback + " (TTL: " + fallbackCacheTtl + "ms)");
    }

    /**
     * Prepare message context for HTTP/1.1 fallback.
     */
    private void prepareMessageContextForFallback(MessageContext messageContext) {
        // Update transport properties for HTTP/1.1
        messageContext.setProperty("TRANSPORT_NAME", "http");
        messageContext.setProperty("HTTP_VERSION", "HTTP/1.1");

        // Remove HTTP/2 specific properties
        messageContext.removeProperty("HTTP2_ENABLED");
        messageContext.removeProperty("HTTP2_STREAMING_ENABLED");
        messageContext.removeProperty("HTTP2_MULTIPLEXING_ENABLED");
        messageContext.removeProperty("HTTP2_MEMORY_OPTIMIZATION");

        // Update endpoint URL if necessary (h2:// -> http://)
        String targetEndpoint = (String) messageContext.getProperty("TRANSPORT_URL");
        if (targetEndpoint != null && targetEndpoint.startsWith("h2://")) {
            String httpEndpoint = targetEndpoint.replace("h2://", "http://");
            messageContext.setProperty("TRANSPORT_URL", httpEndpoint);
            log.debug("Updated endpoint URL for fallback: " + httpEndpoint);
        }
    }

    /**
     * Extract host from message context for caching.
     */
    private String extractHostFromMessageContext(MessageContext messageContext) {
        try {
            String targetEndpoint = (String) messageContext.getProperty("TRANSPORT_URL");
            if (targetEndpoint != null) {
                // Extract host from URL
                java.net.URL url = new java.net.URL(targetEndpoint);
                return url.getHost() + ":" + url.getPort();
            }
        } catch (Exception e) {
            log.debug("Could not extract host from message context: " + e.getMessage());
        }
        return "unknown-host";
    }

    /**
     * Generate cache key for host.
     */
    private String getCacheKey(String host) {
        return "fallback:" + host;
    }

    /**
     * Clear expired entries from fallback cache.
     */
    public void cleanupExpiredCacheEntries() {
        int removedCount = 0;
        for (java.util.Map.Entry<String, FallbackCacheEntry> entry : fallbackCache.entrySet()) {
            if (entry.getValue().isExpired()) {
                fallbackCache.remove(entry.getKey());
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.debug("Cleaned up " + removedCount + " expired fallback cache entries");
        }
    }

    /**
     * Force cache refresh for a specific host.
     */
    public void invalidateCacheForHost(String host) {
        String cacheKey = getCacheKey(host);
        fallbackCache.remove(cacheKey);
        log.info("Invalidated fallback cache for host: " + host);
    }

    /**
     * Get comprehensive fallback statistics.
     */
    public FallbackMetrics getMetrics() {
        return new FallbackMetrics(
            totalFallbackAttempts.get(),
            successfulFallbacks.get(),
            failedFallbacks.get(),
            cacheHits.get(),
            fallbackCache.size()
        );
    }

    /**
     * Fallback manager metrics container.
     */
    public static class FallbackMetrics {
        public final long totalAttempts;
        public final long successfulFallbacks;
        public final long failedFallbacks;
        public final long cacheHits;
        public final int cacheSize;

        public FallbackMetrics(long totalAttempts, long successfulFallbacks,
                             long failedFallbacks, long cacheHits, int cacheSize) {
            this.totalAttempts = totalAttempts;
            this.successfulFallbacks = successfulFallbacks;
            this.failedFallbacks = failedFallbacks;
            this.cacheHits = cacheHits;
            this.cacheSize = cacheSize;
        }

        public double getSuccessRate() {
            return totalAttempts > 0 ? (double) successfulFallbacks / totalAttempts : 0.0;
        }

        public double getCacheHitRate() {
            long totalRequests = cacheHits + totalAttempts;
            return totalRequests > 0 ? (double) cacheHits / totalRequests : 0.0;
        }

        @Override
        public String toString() {
            return String.format("FallbackMetrics[attempts=%d, success=%d, failed=%d, " +
                               "successRate=%.2f%%, cacheHits=%d, cacheHitRate=%.2f%%, cacheSize=%d]",
                               totalAttempts, successfulFallbacks, failedFallbacks,
                               getSuccessRate() * 100, cacheHits, getCacheHitRate() * 100, cacheSize);
        }
    }

    /**
     * Check if fallback is enabled.
     */
    public boolean isFallbackEnabled() {
        return fallbackEnabled;
    }

    /**
     * Get fallback strategy.
     */
    public FallbackStrategy getFallbackStrategy() {
        return fallbackStrategy;
    }

    /**
     * Cleanup resources.
     */
    public void cleanup() {
        log.info("H2FallbackManager cleanup - Final metrics: " + getMetrics());
        fallbackCache.clear();
    }
}