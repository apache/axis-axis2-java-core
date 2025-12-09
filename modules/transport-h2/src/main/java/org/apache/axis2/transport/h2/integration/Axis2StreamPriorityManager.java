/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.transport.h2.integration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

/**
 * HTTP/2 Stream Priority Manager for Axis2 Services implementing intelligent prioritization.
 *
 * This class implements Phase 3 of the WildFly 32 + Axis2 HTTP/2 Cooperative Integration Plan,
 * providing dynamic stream prioritization based on Axis2 service characteristics, payload analysis,
 * and system load conditions. Optimizes HTTP/2 multiplexing for web service workloads.
 *
 * Key features:
 * - Dynamic stream priority calculation based on service characteristics
 * - Payload-aware prioritization to prevent large transfers from blocking small requests
 * - System load-aware adjustments for optimal resource utilization
 * - Integration with Axis2 service metadata and operation patterns
 * - Real-time priority adjustment based on current system conditions
 */
public class Axis2StreamPriorityManager {
    private static final Log log = LogFactory.getLog(Axis2StreamPriorityManager.class);

    private final Map<String, ServicePriority> servicePriorities;
    private final StreamLoadBalancer loadBalancer;
    private final PayloadAnalyzer payloadAnalyzer;
    private final AtomicLong totalPriorityCalculations;

    // HTTP/2 flow control constants
    private static final String HTTP2_INITIAL_WINDOW_SIZE = "HTTP2_INITIAL_WINDOW_SIZE";
    private static final String HTTP2_FLOW_CONTROL_MODE = "HTTP2_FLOW_CONTROL_MODE";

    /**
     * Initialize stream priority manager with intelligent defaults.
     */
    public Axis2StreamPriorityManager() {
        this.servicePriorities = new ConcurrentHashMap<>();
        this.loadBalancer = new StreamLoadBalancer();
        this.payloadAnalyzer = new PayloadAnalyzer();
        this.totalPriorityCalculations = new AtomicLong(0);

        // Initialize service priority mappings
        initializeServicePriorities();

        log.info("Axis2StreamPriorityManager initialized with intelligent prioritization");
    }

    /**
     * Calculate optimal stream priority for Axis2 service request.
     *
     * @param serviceName Name of the Axis2 service
     * @param operationName Name of the service operation
     * @param estimatedPayloadSize Expected payload size in bytes
     * @return HTTP/2 stream priority (0-255, higher = more important)
     */
    public int calculateStreamPriority(String serviceName, String operationName, long estimatedPayloadSize) {
        totalPriorityCalculations.incrementAndGet();

        ServicePriority basePriority = servicePriorities.getOrDefault(serviceName, ServicePriority.NORMAL);

        // Analyze payload characteristics
        PayloadCharacteristics payload = payloadAnalyzer.analyzePayload(estimatedPayloadSize, operationName);

        // Calculate dynamic priority starting with service base priority
        int priority = basePriority.getWeight();

        // Adjust for payload size (larger = lower priority to prevent blocking)
        if (payload.isLargePayload()) {
            priority -= 10;
            log.debug("Large payload detected (" + formatBytes(estimatedPayloadSize) + "), reducing priority by 10");
        }

        // Boost priority for real-time operations
        if (payload.isRealTimeOperation()) {
            priority += 15;
            log.debug("Real-time operation detected (" + operationName + "), increasing priority by 15");
        }

        // Adjust for interactive operations
        if (payload.isInteractiveOperation()) {
            priority += 8;
            log.debug("Interactive operation detected (" + operationName + "), increasing priority by 8");
        }

        // Reduce priority for batch operations
        if (payload.isBatchOperation()) {
            priority -= 12;
            log.debug("Batch operation detected (" + operationName + "), reducing priority by 12");
        }

        // Adjust based on current system load
        int loadAdjustment = loadBalancer.getLoadAdjustment();
        priority += loadAdjustment;

        if (loadAdjustment != 0) {
            log.debug("System load adjustment: " + loadAdjustment + " for service: " + serviceName);
        }

        // Ensure priority is within valid HTTP/2 range (0-255)
        int finalPriority = Math.max(0, Math.min(255, priority));

        if (log.isDebugEnabled()) {
            log.debug("Calculated stream priority: " + finalPriority + " for " + serviceName + "." + operationName +
                     " (base: " + basePriority.getWeight() + ", load: " + loadAdjustment +
                     ", payload: " + formatBytes(estimatedPayloadSize) + ")");
        }

        return finalPriority;
    }

    /**
     * Configure HTTP/2 stream with calculated priority and flow control settings.
     *
     * @param exchange Undertow HTTP server exchange
     * @param serviceName Axis2 service name
     * @param operationName Axis2 operation name
     */
    public void configureStreamPriority(HttpServerExchange exchange, String serviceName, String operationName) {
        long payloadSize = getEstimatedPayloadSize(exchange);
        int priority = calculateStreamPriority(serviceName, operationName, payloadSize);

        // Set HTTP/2 stream priority header for debugging
        exchange.getResponseHeaders().put(new HttpString("X-HTTP2-Stream-Priority"), String.valueOf(priority));

        // Configure Undertow HTTP/2 stream settings
        configureUndertowStreamSettings(exchange, priority, payloadSize);

        // Register stream with load balancer
        int streamId = getStreamId(exchange);
        loadBalancer.registerStream(streamId, payloadSize);

        log.debug("Configured HTTP/2 stream priority: " + priority + " for " + serviceName + "." + operationName);
    }

    /**
     * Configure Undertow-specific HTTP/2 stream settings based on priority and payload.
     */
    private void configureUndertowStreamSettings(HttpServerExchange exchange, int priority, long payloadSize) {
        // Set stream window size based on payload characteristics
        int windowSize = calculateOptimalWindowSize(payloadSize);
        // Note: Undertow attachment integration would be added here in full implementation

        // Configure flow control mode based on payload size
        FlowControlMode flowControlMode = determineFlowControlMode(payloadSize);
        // Note: Undertow attachment integration would be added here in full implementation

        // Set additional HTTP/2 headers for priority signaling
        exchange.getResponseHeaders().put(new HttpString("X-HTTP2-Window-Size"), String.valueOf(windowSize));
        exchange.getResponseHeaders().put(new HttpString("X-HTTP2-Flow-Control"), flowControlMode.toString());

        log.debug("Configured HTTP/2 settings - Window: " + formatBytes(windowSize) +
                 ", Flow Control: " + flowControlMode + ", Priority: " + priority);
    }

    /**
     * Calculate optimal window size based on payload characteristics.
     */
    private int calculateOptimalWindowSize(long payloadSize) {
        if (payloadSize > 50 * 1024 * 1024) { // > 50MB
            return 1024 * 1024; // 1MB window for very large payloads
        } else if (payloadSize > 10 * 1024 * 1024) { // > 10MB
            return 512 * 1024; // 512KB window for large payloads
        } else {
            return 64 * 1024; // 64KB window (default)
        }
    }

    /**
     * Determine flow control mode based on payload size.
     */
    private FlowControlMode determineFlowControlMode(long payloadSize) {
        if (payloadSize > 10 * 1024 * 1024) { // > 10MB
            return FlowControlMode.CONSERVATIVE;
        } else {
            return FlowControlMode.AGGRESSIVE;
        }
    }

    /**
     * Initialize default service priority mappings.
     */
    private void initializeServicePriorities() {
        // Real-time/interactive services get high priority
        servicePriorities.put("NotificationService", ServicePriority.CRITICAL);
        servicePriorities.put("AuthenticationService", ServicePriority.CRITICAL);
        servicePriorities.put("SessionService", ServicePriority.HIGH);
        servicePriorities.put("UserService", ServicePriority.HIGH);

        // Standard business services get normal priority
        servicePriorities.put("OrderService", ServicePriority.NORMAL);
        servicePriorities.put("ProductService", ServicePriority.NORMAL);
        servicePriorities.put("PaymentService", ServicePriority.HIGH); // Slightly higher for financial operations

        // Batch/reporting services get lower priority
        servicePriorities.put("ReportService", ServicePriority.LOW);
        servicePriorities.put("AnalyticsService", ServicePriority.LOW);
        servicePriorities.put("BackupService", ServicePriority.BULK);
        servicePriorities.put("DataExportService", ServicePriority.BULK);

        log.info("Initialized " + servicePriorities.size() + " service priority mappings");
    }

    /**
     * Add or update service priority mapping.
     *
     * @param serviceName Name of the Axis2 service
     * @param priority Priority level for the service
     */
    public void setServicePriority(String serviceName, ServicePriority priority) {
        servicePriorities.put(serviceName, priority);
        log.info("Updated service priority: " + serviceName + " -> " + priority);
    }

    /**
     * Unregister stream from load balancer when processing completes.
     *
     * @param exchange HTTP server exchange to extract stream ID
     */
    public void unregisterStream(HttpServerExchange exchange) {
        int streamId = getStreamId(exchange);
        loadBalancer.unregisterStream(streamId);
        log.debug("Unregistered HTTP/2 stream: " + streamId);
    }

    /**
     * Get current system statistics for monitoring.
     */
    public StreamPriorityStatistics getStatistics() {
        return new StreamPriorityStatistics(
            totalPriorityCalculations.get(),
            loadBalancer.getCurrentActiveStreams(),
            loadBalancer.getTotalPayloadSize(),
            servicePriorities.size()
        );
    }

    // Utility methods

    private long getEstimatedPayloadSize(HttpServerExchange exchange) {
        String contentLength = exchange.getRequestHeaders().getFirst(Headers.CONTENT_LENGTH);
        if (contentLength != null) {
            try {
                return Long.parseLong(contentLength);
            } catch (NumberFormatException e) {
                log.debug("Invalid content length header: " + contentLength);
            }
        }
        return 65536; // Default estimate: 64KB
    }

    private int getStreamId(HttpServerExchange exchange) {
        // In a real implementation, this would extract the actual HTTP/2 stream ID
        // For now, use a hash-based approach for demonstration
        return Math.abs(exchange.getRequestURI().hashCode()) % 10000;
    }

    private String formatBytes(long bytes) {
        if (bytes >= 1024L * 1024 * 1024) {
            return String.format("%.2fGB", bytes / (1024.0 * 1024.0 * 1024.0));
        } else if (bytes >= 1024L * 1024) {
            return String.format("%.2fMB", bytes / (1024.0 * 1024.0));
        } else if (bytes >= 1024L) {
            return String.format("%.2fKB", bytes / 1024.0);
        } else {
            return bytes + "B";
        }
    }

    // Supporting classes and enums

    /**
     * Service priority levels for HTTP/2 stream prioritization.
     */
    public enum ServicePriority {
        CRITICAL(100),    // Real-time services (notifications, auth)
        HIGH(75),         // Interactive services (user operations)
        NORMAL(50),       // Standard services (business logic)
        LOW(25),          // Batch/background services (reports)
        BULK(10);         // Large data transfers (exports, backups)

        private final int weight;

        ServicePriority(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }
    }

    /**
     * HTTP/2 flow control modes for different payload types.
     */
    public enum FlowControlMode {
        CONSERVATIVE,  // For large payloads
        AGGRESSIVE     // For small payloads
    }

    /**
     * Statistics for stream priority management monitoring.
     */
    public static class StreamPriorityStatistics {
        public final long totalPriorityCalculations;
        public final int currentActiveStreams;
        public final long totalPayloadSize;
        public final int configuredServices;

        public StreamPriorityStatistics(long totalPriorityCalculations, int currentActiveStreams,
                                       long totalPayloadSize, int configuredServices) {
            this.totalPriorityCalculations = totalPriorityCalculations;
            this.currentActiveStreams = currentActiveStreams;
            this.totalPayloadSize = totalPayloadSize;
            this.configuredServices = configuredServices;
        }

        @Override
        public String toString() {
            return "StreamPriorityStatistics{" +
                   "totalCalculations=" + totalPriorityCalculations +
                   ", activeStreams=" + currentActiveStreams +
                   ", totalPayload=" + totalPayloadSize +
                   ", services=" + configuredServices +
                   '}';
        }
    }

    // Note: AttachmentKey integration would be implemented for full Undertow integration
}