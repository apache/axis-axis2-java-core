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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * HTTP/2 Stream Manager for coordinated stream handling between WildFly Undertow and Axis2.
 *
 * This class manages HTTP/2 stream lifecycle and resource allocation as part of Phase 2
 * of the WildFly 32 + Axis2 HTTP/2 Cooperative Integration Plan. It provides stream-aware
 * resource management, flow control optimization, and performance monitoring.
 *
 * Key features:
 * - HTTP/2 stream registration and lifecycle management
 * - Coordinated flow control with memory allocation
 * - Stream-specific configuration based on payload characteristics
 * - Performance metrics and monitoring capabilities
 * - Thread-safe stream management operations
 */
public class HTTP2StreamManager {
    private static final Log log = LogFactory.getLog(HTTP2StreamManager.class);

    private final ConcurrentHashMap<Integer, StreamInfo> activeStreams;
    private final HTTP2MemoryCoordinator memoryCoordinator;
    private final AtomicLong totalStreamsProcessed;
    private final AtomicLong currentActiveStreams;

    // Default configuration values
    private static final int DEFAULT_WINDOW_SIZE = 65536; // 64KB
    private static final int MAX_CONCURRENT_STREAMS = 100;
    private static final long LARGE_PAYLOAD_THRESHOLD = 10 * 1024 * 1024; // 10MB

    /**
     * Initialize stream manager with memory coordination.
     *
     * @param memoryCoordinator Memory coordination system for resource management
     */
    public HTTP2StreamManager(HTTP2MemoryCoordinator memoryCoordinator) {
        this.memoryCoordinator = memoryCoordinator;
        this.activeStreams = new ConcurrentHashMap<>();
        this.totalStreamsProcessed = new AtomicLong(0);
        this.currentActiveStreams = new AtomicLong(0);

        log.info("HTTP2StreamManager initialized with memory coordination");
    }

    /**
     * Configure stream for specific payload characteristics.
     *
     * @param streamId HTTP/2 stream identifier
     * @param estimatedPayloadSize Expected payload size in bytes
     * @return StreamConfiguration containing optimized settings
     */
    public StreamConfiguration configureStreamForPayload(int streamId, long estimatedPayloadSize) {
        if (currentActiveStreams.get() >= MAX_CONCURRENT_STREAMS) {
            log.warn("Maximum concurrent streams reached (" + MAX_CONCURRENT_STREAMS + "), rejecting stream: " + streamId);
            return null;
        }

        // Calculate optimal window size based on payload
        int windowSize = calculateOptimalWindowSize(estimatedPayloadSize);

        // Determine flow control strategy
        FlowControlStrategy flowControl = determineFlowControlStrategy(estimatedPayloadSize);

        // Create stream configuration
        StreamConfiguration config = new StreamConfiguration(streamId, windowSize, flowControl, estimatedPayloadSize);

        // Register stream for monitoring
        registerStream(streamId, estimatedPayloadSize);

        log.debug("Configured HTTP/2 stream " + streamId + " - Window: " + windowSize +
                 ", Flow Control: " + flowControl + ", Payload: " + formatBytes(estimatedPayloadSize));

        return config;
    }

    /**
     * Register HTTP/2 stream for tracking and resource management.
     *
     * @param streamId HTTP/2 stream identifier
     * @param estimatedPayloadSize Expected payload size in bytes
     */
    public void registerStream(int streamId, long estimatedPayloadSize) {
        StreamInfo streamInfo = new StreamInfo(streamId, estimatedPayloadSize, System.currentTimeMillis());
        activeStreams.put(streamId, streamInfo);

        currentActiveStreams.incrementAndGet();
        totalStreamsProcessed.incrementAndGet();

        log.debug("Registered HTTP/2 stream " + streamId + " with payload estimate: " + formatBytes(estimatedPayloadSize));
    }

    /**
     * Unregister HTTP/2 stream and release associated resources.
     *
     * @param streamId HTTP/2 stream identifier
     */
    public void unregisterStream(int streamId) {
        StreamInfo streamInfo = activeStreams.remove(streamId);

        if (streamInfo != null) {
            currentActiveStreams.decrementAndGet();

            long processingTime = System.currentTimeMillis() - streamInfo.startTime;
            log.debug("Unregistered HTTP/2 stream " + streamId + " - Processing time: " + processingTime + "ms");
        }
    }

    /**
     * Calculate optimal window size based on payload characteristics.
     *
     * @param payloadSize Expected payload size in bytes
     * @return Optimal HTTP/2 window size
     */
    private int calculateOptimalWindowSize(long payloadSize) {
        if (payloadSize > 50 * 1024 * 1024) { // > 50MB
            return 1024 * 1024; // 1MB window for very large payloads
        } else if (payloadSize > 10 * 1024 * 1024) { // > 10MB
            return 512 * 1024; // 512KB window for large payloads
        } else if (payloadSize > 1024 * 1024) { // > 1MB
            return 256 * 1024; // 256KB window for medium payloads
        } else {
            return DEFAULT_WINDOW_SIZE; // 64KB for small payloads
        }
    }

    /**
     * Determine optimal flow control strategy based on payload size.
     *
     * @param payloadSize Expected payload size in bytes
     * @return Flow control strategy
     */
    private FlowControlStrategy determineFlowControlStrategy(long payloadSize) {
        if (payloadSize > LARGE_PAYLOAD_THRESHOLD) {
            return FlowControlStrategy.CONSERVATIVE;
        } else {
            return FlowControlStrategy.AGGRESSIVE;
        }
    }

    /**
     * Get current stream statistics for monitoring.
     *
     * @return StreamStatistics containing current metrics
     */
    public StreamStatistics getStreamStatistics() {
        return new StreamStatistics(
            currentActiveStreams.get(),
            totalStreamsProcessed.get(),
            activeStreams.size(),
            calculateAverageProcessingTime()
        );
    }

    /**
     * Calculate average processing time for completed streams.
     *
     * @return Average processing time in milliseconds
     */
    private long calculateAverageProcessingTime() {
        if (activeStreams.isEmpty()) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long totalTime = 0;
        int streamCount = 0;

        for (StreamInfo streamInfo : activeStreams.values()) {
            totalTime += (currentTime - streamInfo.startTime);
            streamCount++;
        }

        return streamCount > 0 ? totalTime / streamCount : 0;
    }

    /**
     * Check if stream manager can accept new streams.
     *
     * @return true if new streams can be accepted, false if at capacity
     */
    public boolean canAcceptNewStreams() {
        return currentActiveStreams.get() < MAX_CONCURRENT_STREAMS;
    }

    /**
     * Get detailed stream information for debugging.
     *
     * @return String containing detailed stream manager status
     */
    public String getDetailedStatus() {
        StringBuilder status = new StringBuilder();
        status.append("HTTP2StreamManager Status:\n");
        status.append("  Current Active Streams: ").append(currentActiveStreams.get()).append(" / ").append(MAX_CONCURRENT_STREAMS).append("\n");
        status.append("  Total Streams Processed: ").append(totalStreamsProcessed.get()).append("\n");
        status.append("  Average Processing Time: ").append(calculateAverageProcessingTime()).append("ms\n");

        if (memoryCoordinator != null) {
            status.append("  Memory Coordination: ENABLED\n");
            status.append("  Memory Utilization: ").append(String.format("%.1f%%", memoryCoordinator.getMemoryUtilizationPercentage())).append("\n");
        } else {
            status.append("  Memory Coordination: DISABLED\n");
        }

        return status.toString();
    }

    /**
     * Reset all stream statistics.
     */
    public void resetStatistics() {
        totalStreamsProcessed.set(0);
        log.info("HTTP2StreamManager statistics reset");
    }

    /**
     * Format bytes for human-readable display.
     */
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

    /**
     * Stream configuration settings for HTTP/2 optimization.
     */
    public static class StreamConfiguration {
        public final int streamId;
        public final int windowSize;
        public final FlowControlStrategy flowControlStrategy;
        public final long estimatedPayloadSize;

        public StreamConfiguration(int streamId, int windowSize, FlowControlStrategy flowControlStrategy, long estimatedPayloadSize) {
            this.streamId = streamId;
            this.windowSize = windowSize;
            this.flowControlStrategy = flowControlStrategy;
            this.estimatedPayloadSize = estimatedPayloadSize;
        }

        @Override
        public String toString() {
            return "StreamConfiguration{" +
                   "streamId=" + streamId +
                   ", windowSize=" + windowSize +
                   ", flowControlStrategy=" + flowControlStrategy +
                   ", estimatedPayloadSize=" + estimatedPayloadSize +
                   '}';
        }
    }

    /**
     * Stream statistics for monitoring and debugging.
     */
    public static class StreamStatistics {
        public final long currentActiveStreams;
        public final long totalStreamsProcessed;
        public final int registeredStreams;
        public final long averageProcessingTime;

        public StreamStatistics(long currentActiveStreams, long totalStreamsProcessed,
                               int registeredStreams, long averageProcessingTime) {
            this.currentActiveStreams = currentActiveStreams;
            this.totalStreamsProcessed = totalStreamsProcessed;
            this.registeredStreams = registeredStreams;
            this.averageProcessingTime = averageProcessingTime;
        }

        @Override
        public String toString() {
            return "StreamStatistics{" +
                   "currentActiveStreams=" + currentActiveStreams +
                   ", totalStreamsProcessed=" + totalStreamsProcessed +
                   ", registeredStreams=" + registeredStreams +
                   ", averageProcessingTime=" + averageProcessingTime + "ms" +
                   '}';
        }
    }

    /**
     * Flow control strategies for different payload types.
     */
    public enum FlowControlStrategy {
        /** Conservative flow control for large payloads */
        CONSERVATIVE,
        /** Aggressive flow control for small payloads */
        AGGRESSIVE
    }

    /**
     * Internal stream information tracking.
     */
    private static class StreamInfo {
        final int streamId;
        final long estimatedPayloadSize;
        final long startTime;

        StreamInfo(int streamId, long estimatedPayloadSize, long startTime) {
            this.streamId = streamId;
            this.estimatedPayloadSize = estimatedPayloadSize;
            this.startTime = startTime;
        }
    }
}