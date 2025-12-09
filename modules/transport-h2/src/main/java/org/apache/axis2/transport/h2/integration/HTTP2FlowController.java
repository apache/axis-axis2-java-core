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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * HTTP/2 flow control coordinator for Moshi JSON streaming integration.
 *
 * This class provides HTTP/2 flow control coordination as part of Phase 4 of the WildFly 32 +
 * Axis2 HTTP/2 Cooperative Integration Plan. It manages stream-level flow control to prevent
 * buffer overflow and optimize throughput during large JSON payload processing.
 *
 * Key features:
 * - Stream registration and lifecycle management
 * - Flow control window management per stream
 * - Coordinated yielding for optimal multiplexing
 * - Back-pressure handling for large payloads
 * - Performance monitoring and statistics
 * - Thread-safe concurrent stream management
 */
public class HTTP2FlowController {
    private static final Log log = LogFactory.getLog(HTTP2FlowController.class);

    private final ConcurrentHashMap<Integer, StreamFlowControl> activeStreams;
    private final AtomicInteger totalActiveStreams;
    private final AtomicLong totalFlowControlChecks;
    private final AtomicLong totalYieldOperations;

    // Flow control configuration
    private static final int DEFAULT_WINDOW_SIZE = 65536; // 64KB
    private static final int MAX_WINDOW_SIZE = 1024 * 1024; // 1MB
    private static final long YIELD_THRESHOLD = 100; // Yield every 100 operations
    private static final long BACKPRESSURE_THRESHOLD = 80; // Apply backpressure at 80% window usage

    /**
     * Initialize HTTP/2 flow controller.
     */
    public HTTP2FlowController() {
        this.activeStreams = new ConcurrentHashMap<>();
        this.totalActiveStreams = new AtomicInteger(0);
        this.totalFlowControlChecks = new AtomicLong(0);
        this.totalYieldOperations = new AtomicLong(0);

        log.info("HTTP2FlowController initialized");
    }

    /**
     * Register a new HTTP/2 stream for flow control management.
     *
     * @param streamId HTTP/2 stream identifier
     * @param estimatedPayloadSize Expected payload size in bytes
     */
    public void registerStream(int streamId, long estimatedPayloadSize) {
        int windowSize = calculateOptimalWindowSize(estimatedPayloadSize);
        StreamFlowControl flowControl = new StreamFlowControl(streamId, windowSize, estimatedPayloadSize);

        activeStreams.put(streamId, flowControl);
        totalActiveStreams.incrementAndGet();

        log.debug("Registered HTTP/2 stream " + streamId + " - Window: " + formatBytes(windowSize) +
                 ", Payload: " + formatBytes(estimatedPayloadSize));
    }

    /**
     * Unregister completed HTTP/2 stream and clean up resources.
     *
     * @param streamId HTTP/2 stream identifier
     */
    public void unregisterStream(int streamId) {
        StreamFlowControl flowControl = activeStreams.remove(streamId);
        if (flowControl != null) {
            totalActiveStreams.decrementAndGet();

            long processingTime = System.currentTimeMillis() - flowControl.startTime;
            log.debug("Unregistered HTTP/2 stream " + streamId + " - Processing time: " + processingTime + "ms");
        }
    }

    /**
     * Check flow control for a stream and apply back-pressure if necessary.
     *
     * @param streamId HTTP/2 stream identifier
     */
    public void checkFlowControl(int streamId) {
        totalFlowControlChecks.incrementAndGet();

        StreamFlowControl flowControl = activeStreams.get(streamId);
        if (flowControl == null) {
            log.warn("Flow control check for unknown stream: " + streamId);
            return;
        }

        // Update flow control state
        flowControl.incrementOperationCount();

        // Apply back-pressure if window usage is high
        if (shouldApplyBackPressure(flowControl)) {
            applyBackPressure(streamId, flowControl);
        }

        // Yield control periodically for optimal multiplexing
        if (flowControl.operationCount % YIELD_THRESHOLD == 0) {
            yieldForFlowControl();
        }
    }

    /**
     * Yield control to allow other streams to process (cooperative multitasking).
     */
    public void yieldForFlowControl() {
        totalYieldOperations.incrementAndGet();

        try {
            // Brief yield to allow other streams to process
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Flow control yield interrupted");
        }

        if (log.isDebugEnabled()) {
            log.debug("Yielded for flow control - Active streams: " + totalActiveStreams.get());
        }
    }

    /**
     * Apply back-pressure to slow down processing for a stream approaching window limits.
     */
    private void applyBackPressure(int streamId, StreamFlowControl flowControl) {
        flowControl.backPressureApplied = true;

        // Calculate delay based on window usage percentage
        double windowUsage = (double) flowControl.bytesProcessed / flowControl.windowSize;
        long delayMs = Math.round(windowUsage * 10); // Up to 10ms delay at 100% usage

        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Back-pressure delay interrupted for stream: " + streamId);
        }

        log.debug("Applied back-pressure to stream " + streamId + " - Delay: " + delayMs + "ms, Usage: " +
                 String.format("%.1f%%", windowUsage * 100));
    }

    /**
     * Determine if back-pressure should be applied based on window usage.
     */
    private boolean shouldApplyBackPressure(StreamFlowControl flowControl) {
        if (flowControl.windowSize <= 0) {
            return false;
        }

        double windowUsage = (double) flowControl.bytesProcessed / flowControl.windowSize;
        return windowUsage >= (BACKPRESSURE_THRESHOLD / 100.0);
    }

    /**
     * Calculate optimal window size based on payload characteristics.
     */
    private int calculateOptimalWindowSize(long estimatedPayloadSize) {
        if (estimatedPayloadSize > 50 * 1024 * 1024) { // > 50MB
            return MAX_WINDOW_SIZE; // 1MB for very large payloads
        } else if (estimatedPayloadSize > 10 * 1024 * 1024) { // > 10MB
            return 512 * 1024; // 512KB for large payloads
        } else if (estimatedPayloadSize > 1024 * 1024) { // > 1MB
            return 256 * 1024; // 256KB for medium payloads
        } else {
            return DEFAULT_WINDOW_SIZE; // 64KB for small payloads
        }
    }

    /**
     * Update bytes processed for a stream (for window usage calculation).
     *
     * @param streamId HTTP/2 stream identifier
     * @param bytesProcessed Number of bytes processed
     */
    public void updateBytesProcessed(int streamId, long bytesProcessed) {
        StreamFlowControl flowControl = activeStreams.get(streamId);
        if (flowControl != null) {
            flowControl.bytesProcessed += bytesProcessed;
        }
    }

    /**
     * Get current flow control statistics for monitoring.
     */
    public FlowControlStatistics getStatistics() {
        long totalBackPressureEvents = 0;
        long totalBytesProcessed = 0;

        for (StreamFlowControl flowControl : activeStreams.values()) {
            if (flowControl.backPressureApplied) {
                totalBackPressureEvents++;
            }
            totalBytesProcessed += flowControl.bytesProcessed;
        }

        return new FlowControlStatistics(
            totalActiveStreams.get(),
            totalFlowControlChecks.get(),
            totalYieldOperations.get(),
            totalBackPressureEvents,
            totalBytesProcessed
        );
    }

    /**
     * Get detailed flow controller status for debugging.
     */
    public String getDetailedStatus() {
        StringBuilder status = new StringBuilder();
        status.append("HTTP2FlowController Status:\n");
        status.append("  Active Streams: ").append(totalActiveStreams.get()).append("\n");
        status.append("  Flow Control Checks: ").append(totalFlowControlChecks.get()).append("\n");
        status.append("  Yield Operations: ").append(totalYieldOperations.get()).append("\n");

        FlowControlStatistics stats = getStatistics();
        status.append("  Back-pressure Events: ").append(stats.totalBackPressureEvents).append("\n");
        status.append("  Total Bytes Processed: ").append(formatBytes(stats.totalBytesProcessed)).append("\n");

        return status.toString();
    }

    /**
     * Reset flow control statistics.
     */
    public void resetStatistics() {
        totalFlowControlChecks.set(0);
        totalYieldOperations.set(0);

        // Reset individual stream statistics
        for (StreamFlowControl flowControl : activeStreams.values()) {
            flowControl.backPressureApplied = false;
        }

        log.info("HTTP2FlowController statistics reset");
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

    /**
     * Internal stream flow control tracking.
     */
    private static class StreamFlowControl {
        final int streamId;
        final int windowSize;
        final long estimatedPayloadSize;
        final long startTime;

        long operationCount = 0;
        long bytesProcessed = 0;
        boolean backPressureApplied = false;

        StreamFlowControl(int streamId, int windowSize, long estimatedPayloadSize) {
            this.streamId = streamId;
            this.windowSize = windowSize;
            this.estimatedPayloadSize = estimatedPayloadSize;
            this.startTime = System.currentTimeMillis();
        }

        void incrementOperationCount() {
            operationCount++;
        }
    }

    /**
     * Flow control statistics for monitoring and analysis.
     */
    public static class FlowControlStatistics {
        public final int activeStreams;
        public final long totalFlowControlChecks;
        public final long totalYieldOperations;
        public final long totalBackPressureEvents;
        public final long totalBytesProcessed;

        public FlowControlStatistics(int activeStreams, long totalFlowControlChecks,
                                   long totalYieldOperations, long totalBackPressureEvents,
                                   long totalBytesProcessed) {
            this.activeStreams = activeStreams;
            this.totalFlowControlChecks = totalFlowControlChecks;
            this.totalYieldOperations = totalYieldOperations;
            this.totalBackPressureEvents = totalBackPressureEvents;
            this.totalBytesProcessed = totalBytesProcessed;
        }

        @Override
        public String toString() {
            return "FlowControlStatistics{" +
                   "activeStreams=" + activeStreams +
                   ", flowControlChecks=" + totalFlowControlChecks +
                   ", yieldOperations=" + totalYieldOperations +
                   ", backPressureEvents=" + totalBackPressureEvents +
                   ", bytesProcessed=" + totalBytesProcessed +
                   '}';
        }
    }
}