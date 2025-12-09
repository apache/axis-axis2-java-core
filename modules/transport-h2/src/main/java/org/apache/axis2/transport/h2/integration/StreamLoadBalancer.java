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

/**
 * Load balancer that considers HTTP/2 stream characteristics for intelligent priority adjustments.
 *
 * This class provides load-aware priority calculations as part of Phase 3 of the WildFly 32 +
 * Axis2 HTTP/2 Cooperative Integration Plan. It monitors system load conditions and adjusts
 * HTTP/2 stream priorities dynamically to maintain optimal performance under varying workloads.
 *
 * Key features:
 * - Real-time monitoring of active HTTP/2 streams
 * - Total payload size tracking for memory pressure detection
 * - Dynamic priority adjustments based on system load
 * - Performance metrics collection for monitoring and tuning
 * - Thread-safe load tracking operations
 */
public class StreamLoadBalancer {
    private static final Log log = LogFactory.getLog(StreamLoadBalancer.class);

    private final AtomicInteger activeStreams = new AtomicInteger(0);
    private final AtomicLong totalPayloadSize = new AtomicLong(0);
    private final Map<Integer, StreamMetrics> streamMetrics = new ConcurrentHashMap<>();
    private final AtomicLong totalStreamsProcessed = new AtomicLong(0);

    // Load thresholds for priority adjustments
    private static final int HIGH_LOAD_THRESHOLD = 80;
    private static final int MODERATE_LOAD_THRESHOLD = 50;
    private static final long HIGH_MEMORY_PRESSURE_THRESHOLD = 500L * 1024 * 1024; // 500MB
    private static final long EXTREME_MEMORY_PRESSURE_THRESHOLD = 1024L * 1024 * 1024; // 1GB

    /**
     * Calculate load adjustment for stream priority based on current system conditions.
     *
     * @return Priority adjustment value (negative = reduce priority, positive = increase priority, 0 = no change)
     */
    public int getLoadAdjustment() {
        int currentStreams = activeStreams.get();
        long currentPayload = totalPayloadSize.get();

        // Calculate load-based adjustments
        int streamLoadAdjustment = calculateStreamLoadAdjustment(currentStreams);
        int memoryPressureAdjustment = calculateMemoryPressureAdjustment(currentPayload);

        // Return the most conservative (lowest) adjustment
        int finalAdjustment = Math.min(streamLoadAdjustment, memoryPressureAdjustment);

        if (log.isDebugEnabled() && finalAdjustment != 0) {
            log.debug("Load adjustment calculated: " + finalAdjustment +
                     " (streams: " + currentStreams + ", payload: " + formatBytes(currentPayload) + ")");
        }

        return finalAdjustment;
    }

    /**
     * Calculate adjustment based on number of active streams.
     */
    private int calculateStreamLoadAdjustment(int currentStreams) {
        if (currentStreams > HIGH_LOAD_THRESHOLD) {
            return -20; // System under heavy stream load
        } else if (currentStreams > MODERATE_LOAD_THRESHOLD) {
            return -10; // System moderately loaded
        } else if (currentStreams < 10) {
            return 5;   // Light load, can increase priorities slightly
        }

        return 0; // Normal stream load
    }

    /**
     * Calculate adjustment based on total payload size (memory pressure).
     */
    private int calculateMemoryPressureAdjustment(long currentPayload) {
        if (currentPayload > EXTREME_MEMORY_PRESSURE_THRESHOLD) {
            return -25; // Extreme memory pressure
        } else if (currentPayload > HIGH_MEMORY_PRESSURE_THRESHOLD) {
            return -15; // High memory pressure
        } else if (currentPayload < 50 * 1024 * 1024) { // < 50MB
            return 3;   // Low memory pressure, can increase priorities
        }

        return 0; // Normal memory pressure
    }

    /**
     * Register new stream with load balancer for tracking.
     *
     * @param streamId HTTP/2 stream identifier
     * @param estimatedPayloadSize Expected payload size in bytes
     */
    public void registerStream(int streamId, long estimatedPayloadSize) {
        activeStreams.incrementAndGet();
        totalPayloadSize.addAndGet(estimatedPayloadSize);
        totalStreamsProcessed.incrementAndGet();

        streamMetrics.put(streamId, new StreamMetrics(System.currentTimeMillis(), estimatedPayloadSize));

        if (log.isDebugEnabled()) {
            log.debug("Registered stream " + streamId + " - Active streams: " + activeStreams.get() +
                     ", Total payload: " + formatBytes(totalPayloadSize.get()));
        }
    }

    /**
     * Unregister completed stream and update load tracking.
     *
     * @param streamId HTTP/2 stream identifier
     */
    public void unregisterStream(int streamId) {
        StreamMetrics metrics = streamMetrics.remove(streamId);
        if (metrics != null) {
            activeStreams.decrementAndGet();
            totalPayloadSize.addAndGet(-metrics.payloadSize);

            long processingTime = System.currentTimeMillis() - metrics.startTime;

            if (log.isDebugEnabled()) {
                log.debug("Unregistered stream " + streamId + " - Processing time: " + processingTime +
                         "ms, Payload: " + formatBytes(metrics.payloadSize) +
                         ", Remaining active: " + activeStreams.get());
            }
        } else {
            log.warn("Attempted to unregister unknown stream: " + streamId);
        }
    }

    /**
     * Get current number of active streams.
     *
     * @return Number of currently active HTTP/2 streams
     */
    public int getCurrentActiveStreams() {
        return activeStreams.get();
    }

    /**
     * Get current total payload size across all active streams.
     *
     * @return Total payload size in bytes
     */
    public long getTotalPayloadSize() {
        return totalPayloadSize.get();
    }

    /**
     * Get total number of streams processed since startup.
     *
     * @return Total streams processed count
     */
    public long getTotalStreamsProcessed() {
        return totalStreamsProcessed.get();
    }

    /**
     * Get current load level description for monitoring.
     *
     * @return String describing current system load level
     */
    public String getCurrentLoadLevel() {
        int currentStreams = activeStreams.get();
        long currentPayload = totalPayloadSize.get();

        if (currentStreams > HIGH_LOAD_THRESHOLD || currentPayload > EXTREME_MEMORY_PRESSURE_THRESHOLD) {
            return "HIGH";
        } else if (currentStreams > MODERATE_LOAD_THRESHOLD || currentPayload > HIGH_MEMORY_PRESSURE_THRESHOLD) {
            return "MODERATE";
        } else if (currentStreams < 10 && currentPayload < 50 * 1024 * 1024) {
            return "LOW";
        } else {
            return "NORMAL";
        }
    }

    /**
     * Get detailed load balancer statistics for monitoring.
     *
     * @return LoadBalancerStatistics containing current metrics
     */
    public LoadBalancerStatistics getStatistics() {
        return new LoadBalancerStatistics(
            activeStreams.get(),
            totalPayloadSize.get(),
            totalStreamsProcessed.get(),
            streamMetrics.size(),
            getCurrentLoadLevel(),
            getLoadAdjustment(),
            calculateAverageProcessingTime()
        );
    }

    /**
     * Calculate average processing time for active streams.
     */
    private long calculateAverageProcessingTime() {
        if (streamMetrics.isEmpty()) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long totalTime = 0;
        int count = 0;

        for (StreamMetrics metrics : streamMetrics.values()) {
            totalTime += (currentTime - metrics.startTime);
            count++;
        }

        return count > 0 ? totalTime / count : 0;
    }

    /**
     * Reset all load balancer statistics (does not affect active stream tracking).
     */
    public void resetStatistics() {
        totalStreamsProcessed.set(0);
        log.info("StreamLoadBalancer statistics reset");
    }

    /**
     * Get detailed status information for debugging.
     */
    public String getDetailedStatus() {
        StringBuilder status = new StringBuilder();
        status.append("StreamLoadBalancer Status:\n");
        status.append("  Active Streams: ").append(activeStreams.get()).append("\n");
        status.append("  Total Payload: ").append(formatBytes(totalPayloadSize.get())).append("\n");
        status.append("  Total Processed: ").append(totalStreamsProcessed.get()).append("\n");
        status.append("  Load Level: ").append(getCurrentLoadLevel()).append("\n");
        status.append("  Load Adjustment: ").append(getLoadAdjustment()).append("\n");
        status.append("  Average Processing Time: ").append(calculateAverageProcessingTime()).append("ms\n");

        return status.toString();
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
     * Stream metrics for internal tracking.
     */
    static class StreamMetrics {
        final long startTime;
        final long payloadSize;

        StreamMetrics(long startTime, long payloadSize) {
            this.startTime = startTime;
            this.payloadSize = payloadSize;
        }
    }

    /**
     * Load balancer statistics for monitoring and analysis.
     */
    public static class LoadBalancerStatistics {
        public final int activeStreams;
        public final long totalPayloadSize;
        public final long totalStreamsProcessed;
        public final int trackedStreams;
        public final String loadLevel;
        public final int loadAdjustment;
        public final long averageProcessingTime;

        public LoadBalancerStatistics(int activeStreams, long totalPayloadSize, long totalStreamsProcessed,
                                     int trackedStreams, String loadLevel, int loadAdjustment,
                                     long averageProcessingTime) {
            this.activeStreams = activeStreams;
            this.totalPayloadSize = totalPayloadSize;
            this.totalStreamsProcessed = totalStreamsProcessed;
            this.trackedStreams = trackedStreams;
            this.loadLevel = loadLevel;
            this.loadAdjustment = loadAdjustment;
            this.averageProcessingTime = averageProcessingTime;
        }

        @Override
        public String toString() {
            return "LoadBalancerStatistics{" +
                   "activeStreams=" + activeStreams +
                   ", totalPayloadSize=" + totalPayloadSize +
                   ", totalProcessed=" + totalStreamsProcessed +
                   ", trackedStreams=" + trackedStreams +
                   ", loadLevel='" + loadLevel + '\'' +
                   ", loadAdjustment=" + loadAdjustment +
                   ", avgProcessingTime=" + averageProcessingTime + "ms" +
                   '}';
        }
    }
}