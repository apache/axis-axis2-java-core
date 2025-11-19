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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stage 3: HTTP/2 Flow Control Manager for optimizing large payload transfers.
 *
 * This manager implements HTTP/2 flow control strategies specifically optimized for
 * enterprise big data processing requirements, particularly 50MB+ JSON payloads
 * within 2GB heap constraints.
 *
 * Key Responsibilities:
 * - Dynamic window size adjustment based on payload size and memory availability
 * - Backpressure management to prevent memory overflow
 * - Stream priority optimization for concurrent transfers
 * - Connection-level flow control coordination
 * - Performance monitoring and adaptive tuning
 *
 * Flow Control Strategy:
 * - Small payloads (< 1MB): Standard window sizes for low latency
 * - Medium payloads (1-10MB): Increased windows for throughput
 * - Large payloads (10MB+): Dynamic windowing with memory monitoring
 * - Massive payloads (50MB+): Streaming with aggressive flow control
 */
public class H2FlowControlManager {

    private static final Log log = LogFactory.getLog(H2FlowControlManager.class);

    // Flow control configuration constants
    private static final int SMALL_PAYLOAD_THRESHOLD = 1024 * 1024;     // 1MB
    private static final int MEDIUM_PAYLOAD_THRESHOLD = 10 * 1024 * 1024; // 10MB
    private static final int LARGE_PAYLOAD_THRESHOLD = 50 * 1024 * 1024;  // 50MB

    // Window size configurations for different payload sizes
    private static final int SMALL_WINDOW_SIZE = 64 * 1024;      // 64KB
    private static final int MEDIUM_WINDOW_SIZE = 256 * 1024;    // 256KB
    private static final int LARGE_WINDOW_SIZE = 1024 * 1024;    // 1MB
    private static final int STREAMING_WINDOW_SIZE = 2 * 1024 * 1024; // 2MB

    // Memory pressure thresholds
    private static final long MEMORY_WARNING_THRESHOLD = (long) (2L * 1024 * 1024 * 1024 * 0.8); // 80% of 2GB
    private static final long MEMORY_CRITICAL_THRESHOLD = (long) (2L * 1024 * 1024 * 1024 * 0.9); // 90% of 2GB

    // Per-stream flow control tracking
    private final ConcurrentHashMap<String, StreamFlowControl> activeStreams;
    private final AtomicLong totalMemoryUsage;
    private final AtomicLong totalActiveStreams;

    // Performance metrics
    private final AtomicLong totalBytesTransferred;
    private final AtomicLong totalStreamOptimizations;

    public H2FlowControlManager() {
        this.activeStreams = new ConcurrentHashMap<>();
        this.totalMemoryUsage = new AtomicLong(0);
        this.totalActiveStreams = new AtomicLong(0);
        this.totalBytesTransferred = new AtomicLong(0);
        this.totalStreamOptimizations = new AtomicLong(0);

        log.info("H2FlowControlManager initialized for enterprise big data processing");
    }

    /**
     * Calculate optimal window size based on payload characteristics and system state.
     */
    public int calculateOptimalWindowSize(String streamId, long payloadSize, boolean isUpload) {
        // Check current memory pressure
        long currentMemoryUsage = getCurrentMemoryUsage();
        boolean memoryPressure = currentMemoryUsage > MEMORY_WARNING_THRESHOLD;

        int baseWindowSize = determineBaseWindowSize(payloadSize);

        // Apply memory pressure adjustments
        if (memoryPressure) {
            baseWindowSize = (int) (baseWindowSize * 0.5); // Reduce window by 50% under memory pressure
            log.debug("Reduced window size due to memory pressure: " + baseWindowSize);
        }

        // Apply concurrent stream adjustments
        long activeStreamCount = totalActiveStreams.get();
        if (activeStreamCount > 10) {
            // Reduce window size for high concurrency
            baseWindowSize = (int) (baseWindowSize * (10.0 / activeStreamCount));
            log.debug("Adjusted window size for concurrency (" + activeStreamCount + " streams): " + baseWindowSize);
        }

        // Store stream flow control info
        StreamFlowControl streamControl = new StreamFlowControl(streamId, payloadSize, baseWindowSize, isUpload);
        activeStreams.put(streamId, streamControl);
        totalActiveStreams.incrementAndGet();

        totalStreamOptimizations.incrementAndGet();

        log.info("Calculated optimal window size for stream " + streamId +
                ": " + baseWindowSize + " bytes (payload: " + (payloadSize / 1024 / 1024) + "MB)");

        return Math.max(baseWindowSize, 32768); // Minimum 32KB window
    }

    /**
     * Determine base window size based on payload size category.
     */
    private int determineBaseWindowSize(long payloadSize) {
        if (payloadSize <= SMALL_PAYLOAD_THRESHOLD) {
            return SMALL_WINDOW_SIZE;
        } else if (payloadSize <= MEDIUM_PAYLOAD_THRESHOLD) {
            return MEDIUM_WINDOW_SIZE;
        } else if (payloadSize <= LARGE_PAYLOAD_THRESHOLD) {
            return LARGE_WINDOW_SIZE;
        } else {
            // Massive payloads - use streaming window
            return STREAMING_WINDOW_SIZE;
        }
    }

    /**
     * Update flow control state when stream completes.
     */
    public void streamCompleted(String streamId, long bytesTransferred) {
        StreamFlowControl streamControl = activeStreams.remove(streamId);
        if (streamControl != null) {
            totalActiveStreams.decrementAndGet();
            totalBytesTransferred.addAndGet(bytesTransferred);
            totalMemoryUsage.addAndGet(-streamControl.getEstimatedMemoryUsage());

            log.debug("Stream " + streamId + " completed. Transferred: " +
                     (bytesTransferred / 1024 / 1024) + "MB");
        }
    }

    /**
     * Handle flow control errors and adjust strategy.
     */
    public void handleFlowControlError(String streamId, Exception error) {
        log.warn("Flow control error for stream " + streamId + ": " + error.getMessage());

        StreamFlowControl streamControl = activeStreams.get(streamId);
        if (streamControl != null) {
            // Reduce window size for problematic stream
            int reducedWindow = (int) (streamControl.getWindowSize() * 0.5);
            streamControl.setWindowSize(Math.max(reducedWindow, 16384)); // Minimum 16KB

            log.info("Reduced window size for stream " + streamId + " to " +
                    streamControl.getWindowSize() + " due to flow control error");
        }
    }

    /**
     * Get current system memory usage estimation.
     */
    private long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Check if system is under memory pressure.
     */
    public boolean isMemoryPressure() {
        long currentMemory = getCurrentMemoryUsage();
        return currentMemory > MEMORY_WARNING_THRESHOLD;
    }

    /**
     * Check if system is in critical memory state.
     */
    public boolean isCriticalMemory() {
        long currentMemory = getCurrentMemoryUsage();
        return currentMemory > MEMORY_CRITICAL_THRESHOLD;
    }

    /**
     * Get flow control performance metrics.
     */
    public FlowControlMetrics getMetrics() {
        return new FlowControlMetrics(
            totalActiveStreams.get(),
            totalBytesTransferred.get(),
            totalStreamOptimizations.get(),
            getCurrentMemoryUsage(),
            activeStreams.size()
        );
    }

    /**
     * Stream-specific flow control tracking.
     */
    private static class StreamFlowControl {
        private final String streamId;
        private final long payloadSize;
        private volatile int windowSize;
        private final boolean isUpload;
        private final long createdTime;
        private volatile long estimatedMemoryUsage;

        public StreamFlowControl(String streamId, long payloadSize, int windowSize, boolean isUpload) {
            this.streamId = streamId;
            this.payloadSize = payloadSize;
            this.windowSize = windowSize;
            this.isUpload = isUpload;
            this.createdTime = System.currentTimeMillis();
            this.estimatedMemoryUsage = Math.min(payloadSize, windowSize * 2); // Estimate buffer usage
        }

        public int getWindowSize() { return windowSize; }
        public void setWindowSize(int windowSize) { this.windowSize = windowSize; }
        public long getEstimatedMemoryUsage() { return estimatedMemoryUsage; }
    }

    /**
     * Flow control performance metrics.
     */
    public static class FlowControlMetrics {
        private final long activeStreams;
        private final long totalBytesTransferred;
        private final long totalOptimizations;
        private final long currentMemoryUsage;
        private final int trackedStreams;

        public FlowControlMetrics(long activeStreams, long totalBytesTransferred,
                                long totalOptimizations, long currentMemoryUsage, int trackedStreams) {
            this.activeStreams = activeStreams;
            this.totalBytesTransferred = totalBytesTransferred;
            this.totalOptimizations = totalOptimizations;
            this.currentMemoryUsage = currentMemoryUsage;
            this.trackedStreams = trackedStreams;
        }

        @Override
        public String toString() {
            return String.format("H2FlowControl[streams=%d, transferred=%dMB, optimizations=%d, memory=%dMB, tracked=%d]",
                activeStreams, totalBytesTransferred / 1024 / 1024, totalOptimizations,
                currentMemoryUsage / 1024 / 1024, trackedStreams);
        }

        // Getters
        public long getActiveStreams() { return activeStreams; }
        public long getTotalBytesTransferred() { return totalBytesTransferred; }
        public long getTotalOptimizations() { return totalOptimizations; }
        public long getCurrentMemoryUsage() { return currentMemoryUsage; }
        public int getTrackedStreams() { return trackedStreams; }
    }
}