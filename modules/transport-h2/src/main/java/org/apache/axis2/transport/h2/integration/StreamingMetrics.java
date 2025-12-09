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
import java.util.concurrent.atomic.LongAdder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Performance metrics collection for HTTP/2 JSON streaming pipeline.
 *
 * This class provides comprehensive performance monitoring as part of Phase 4 of the WildFly 32 +
 * Axis2 HTTP/2 Cooperative Integration Plan. It tracks streaming performance, error rates,
 * and processing characteristics for optimization and troubleshooting.
 *
 * Key features:
 * - Stream processing performance metrics
 * - Error tracking and categorization
 * - Throughput and latency measurements
 * - Memory usage and efficiency tracking
 * - Statistical analysis for performance tuning
 * - Thread-safe concurrent metric collection
 */
public class StreamingMetrics {
    private static final Log log = LogFactory.getLog(StreamingMetrics.class);

    // Stream processing metrics
    private final AtomicLong totalStreamsStarted = new AtomicLong(0);
    private final AtomicLong totalStreamsCompleted = new AtomicLong(0);
    private final AtomicLong totalStreamErrors = new AtomicLong(0);
    private final AtomicLong totalResponsesStarted = new AtomicLong(0);
    private final AtomicLong totalResponsesCompleted = new AtomicLong(0);

    // Processing performance metrics
    private final LongAdder totalProcessingTimeMs = new LongAdder();
    private final LongAdder totalBytesProcessed = new LongAdder();
    private final LongAdder totalFieldsProcessed = new LongAdder();
    private final LongAdder totalArrayItemsProcessed = new LongAdder();

    // Active stream tracking
    private final ConcurrentHashMap<Integer, StreamProcessingInfo> activeStreams = new ConcurrentHashMap<>();

    // Error tracking
    private final ConcurrentHashMap<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();

    // Performance thresholds for alerting
    private static final long SLOW_PROCESSING_THRESHOLD_MS = 5000; // 5 seconds
    private static final long LARGE_PAYLOAD_THRESHOLD = 10 * 1024 * 1024; // 10MB

    /**
     * Initialize streaming metrics collection.
     */
    public StreamingMetrics() {
        log.info("StreamingMetrics initialized for HTTP/2 pipeline monitoring");
    }

    /**
     * Record the start of stream processing.
     *
     * @param streamId HTTP/2 stream identifier
     */
    public void recordStreamStart(int streamId) {
        totalStreamsStarted.incrementAndGet();

        StreamProcessingInfo info = new StreamProcessingInfo(streamId, System.currentTimeMillis());
        activeStreams.put(streamId, info);

        log.debug("Started processing stream: " + streamId);
    }

    /**
     * Record the completion of stream processing.
     *
     * @param streamId HTTP/2 stream identifier
     * @param processedElements Number of elements processed (fields, array items, etc.)
     */
    public void recordStreamComplete(int streamId, long processedElements) {
        totalStreamsCompleted.incrementAndGet();

        StreamProcessingInfo info = activeStreams.remove(streamId);
        if (info != null) {
            long processingTime = System.currentTimeMillis() - info.startTime;
            totalProcessingTimeMs.add(processingTime);

            // Track processed elements
            if (processedElements > 0) {
                totalFieldsProcessed.add(processedElements);
            }

            // Log slow processing for analysis
            if (processingTime > SLOW_PROCESSING_THRESHOLD_MS) {
                log.warn("Slow stream processing detected - Stream: " + streamId +
                        ", Time: " + processingTime + "ms, Elements: " + processedElements);
            }

            log.debug("Completed processing stream: " + streamId + " in " + processingTime + "ms");
        }
    }

    /**
     * Record processing progress for active streams.
     *
     * @param streamId HTTP/2 stream identifier
     * @param elementsProcessed Number of elements processed so far
     */
    public void recordProgress(int streamId, long elementsProcessed) {
        StreamProcessingInfo info = activeStreams.get(streamId);
        if (info != null) {
            info.updateProgress(elementsProcessed);

            log.debug("Progress update for stream " + streamId + ": " + elementsProcessed + " elements");
        }
    }

    /**
     * Record an error during stream processing.
     *
     * @param streamId HTTP/2 stream identifier
     * @param error The error that occurred
     */
    public void recordError(int streamId, Exception error) {
        totalStreamErrors.incrementAndGet();

        // Categorize error type for analysis
        String errorType = categorizeError(error);
        errorCounts.computeIfAbsent(errorType, k -> new AtomicLong(0)).incrementAndGet();

        // Remove from active streams if present
        activeStreams.remove(streamId);

        log.error("Stream processing error - Stream: " + streamId + ", Type: " + errorType, error);
    }

    /**
     * Record the start of response generation.
     *
     * @param streamId HTTP/2 stream identifier
     */
    public void recordResponseStart(int streamId) {
        totalResponsesStarted.incrementAndGet();
        log.debug("Started response generation for stream: " + streamId);
    }

    /**
     * Record the completion of response generation.
     *
     * @param streamId HTTP/2 stream identifier
     */
    public void recordResponseComplete(int streamId) {
        totalResponsesCompleted.incrementAndGet();
        log.debug("Completed response generation for stream: " + streamId);
    }

    /**
     * Record bytes processed for throughput calculation.
     *
     * @param streamId HTTP/2 stream identifier
     * @param bytesProcessed Number of bytes processed
     */
    public void recordBytesProcessed(int streamId, long bytesProcessed) {
        totalBytesProcessed.add(bytesProcessed);

        StreamProcessingInfo info = activeStreams.get(streamId);
        if (info != null) {
            info.addBytesProcessed(bytesProcessed);
        }
    }

    /**
     * Record array items processed for performance analysis.
     *
     * @param streamId HTTP/2 stream identifier
     * @param arrayItems Number of array items processed
     */
    public void recordArrayItemsProcessed(int streamId, long arrayItems) {
        totalArrayItemsProcessed.add(arrayItems);
        log.debug("Processed " + arrayItems + " array items for stream: " + streamId);
    }

    /**
     * Get comprehensive pipeline statistics.
     */
    public PipelineStatistics getStatistics() {
        long completedStreams = totalStreamsCompleted.get();
        long avgProcessingTimeMs = completedStreams > 0 ?
            totalProcessingTimeMs.sum() / completedStreams : 0;

        return new PipelineStatistics(
            totalStreamsStarted.get(),
            totalStreamsCompleted.get(),
            totalStreamErrors.get(),
            totalResponsesStarted.get(),
            totalResponsesCompleted.get(),
            avgProcessingTimeMs,
            totalBytesProcessed.sum(),
            totalFieldsProcessed.sum(),
            totalArrayItemsProcessed.sum(),
            activeStreams.size(),
            calculateThroughput(),
            calculateErrorRate(),
            getCurrentActiveStreamsInfo()
        );
    }

    /**
     * Calculate current throughput in bytes per second.
     */
    private double calculateThroughput() {
        long totalBytes = totalBytesProcessed.sum();
        long totalTimeSeconds = totalProcessingTimeMs.sum() / 1000;

        if (totalTimeSeconds > 0) {
            return (double) totalBytes / totalTimeSeconds;
        }
        return 0.0;
    }

    /**
     * Calculate error rate as percentage of total streams.
     */
    private double calculateErrorRate() {
        long totalStreams = totalStreamsStarted.get();
        long errors = totalStreamErrors.get();

        if (totalStreams > 0) {
            return (double) errors / totalStreams * 100.0;
        }
        return 0.0;
    }

    /**
     * Get information about currently active streams.
     */
    private String getCurrentActiveStreamsInfo() {
        StringBuilder info = new StringBuilder();
        long currentTime = System.currentTimeMillis();

        for (StreamProcessingInfo streamInfo : activeStreams.values()) {
            long processingTime = currentTime - streamInfo.startTime;
            info.append("Stream ").append(streamInfo.streamId)
                .append(": ").append(processingTime).append("ms, ")
                .append(formatBytes(streamInfo.bytesProcessed)).append("; ");
        }

        return info.toString();
    }

    /**
     * Categorize error types for analysis.
     */
    private String categorizeError(Throwable error) {
        String errorClass = error.getClass().getSimpleName();

        if (error instanceof java.io.IOException) {
            return "IO_ERROR";
        } else if (error instanceof com.squareup.moshi.JsonDataException) {
            return "JSON_DATA_ERROR";
        } else if (error instanceof com.squareup.moshi.JsonEncodingException) {
            return "JSON_ENCODING_ERROR";
        } else if (error instanceof java.lang.OutOfMemoryError) {
            return "OUT_OF_MEMORY";
        } else if (error instanceof java.lang.InterruptedException) {
            return "INTERRUPTED";
        } else {
            return errorClass.toUpperCase();
        }
    }

    /**
     * Reset all streaming metrics.
     */
    public void resetStatistics() {
        totalStreamsStarted.set(0);
        totalStreamsCompleted.set(0);
        totalStreamErrors.set(0);
        totalResponsesStarted.set(0);
        totalResponsesCompleted.set(0);

        totalProcessingTimeMs.reset();
        totalBytesProcessed.reset();
        totalFieldsProcessed.reset();
        totalArrayItemsProcessed.reset();

        errorCounts.clear();
        activeStreams.clear();

        log.info("StreamingMetrics statistics reset");
    }

    /**
     * Get error breakdown by category.
     */
    public ConcurrentHashMap<String, Long> getErrorBreakdown() {
        ConcurrentHashMap<String, Long> breakdown = new ConcurrentHashMap<>();
        errorCounts.forEach((key, value) -> breakdown.put(key, value.get()));
        return breakdown;
    }

    /**
     * Get detailed metrics report for debugging and monitoring.
     */
    public String getDetailedReport() {
        PipelineStatistics stats = getStatistics();
        StringBuilder report = new StringBuilder();

        report.append("StreamingMetrics Detailed Report:\n");
        report.append("======================================\n");
        report.append("Stream Processing:\n");
        report.append("  Total Started: ").append(stats.totalStreamsStarted).append("\n");
        report.append("  Total Completed: ").append(stats.totalStreamsCompleted).append("\n");
        report.append("  Total Errors: ").append(stats.totalStreamErrors).append("\n");
        report.append("  Error Rate: ").append(String.format("%.2f%%", stats.errorRate)).append("\n");

        report.append("\nPerformance:\n");
        report.append("  Avg Processing Time: ").append(stats.avgProcessingTimeMs).append("ms\n");
        report.append("  Total Bytes Processed: ").append(formatBytes(stats.totalBytesProcessed)).append("\n");
        report.append("  Throughput: ").append(formatBytes((long) stats.throughputBytesPerSecond)).append("/sec\n");

        report.append("\nData Processing:\n");
        report.append("  Total Fields Processed: ").append(stats.totalFieldsProcessed).append("\n");
        report.append("  Total Array Items: ").append(stats.totalArrayItemsProcessed).append("\n");

        report.append("\nActive Streams: ").append(stats.currentActiveStreams).append("\n");
        if (stats.currentActiveStreams > 0) {
            report.append("  Details: ").append(stats.activeStreamsInfo).append("\n");
        }

        report.append("\nError Breakdown:\n");
        getErrorBreakdown().forEach((errorType, count) ->
            report.append("  ").append(errorType).append(": ").append(count).append("\n"));

        return report.toString();
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
     * Internal stream processing information tracking.
     */
    private static class StreamProcessingInfo {
        final int streamId;
        final long startTime;
        long elementsProcessed = 0;
        long bytesProcessed = 0;

        StreamProcessingInfo(int streamId, long startTime) {
            this.streamId = streamId;
            this.startTime = startTime;
        }

        void updateProgress(long elements) {
            this.elementsProcessed = elements;
        }

        void addBytesProcessed(long bytes) {
            this.bytesProcessed += bytes;
        }
    }

    /**
     * Comprehensive pipeline statistics for monitoring and analysis.
     */
    public static class PipelineStatistics {
        public final long totalStreamsStarted;
        public final long totalStreamsCompleted;
        public final long totalStreamErrors;
        public final long totalResponsesStarted;
        public final long totalResponsesCompleted;
        public final long avgProcessingTimeMs;
        public final long totalBytesProcessed;
        public final long totalFieldsProcessed;
        public final long totalArrayItemsProcessed;
        public final int currentActiveStreams;
        public final double throughputBytesPerSecond;
        public final double errorRate;
        public final String activeStreamsInfo;

        public PipelineStatistics(long totalStreamsStarted, long totalStreamsCompleted,
                                long totalStreamErrors, long totalResponsesStarted,
                                long totalResponsesCompleted, long avgProcessingTimeMs,
                                long totalBytesProcessed, long totalFieldsProcessed,
                                long totalArrayItemsProcessed, int currentActiveStreams,
                                double throughputBytesPerSecond, double errorRate,
                                String activeStreamsInfo) {
            this.totalStreamsStarted = totalStreamsStarted;
            this.totalStreamsCompleted = totalStreamsCompleted;
            this.totalStreamErrors = totalStreamErrors;
            this.totalResponsesStarted = totalResponsesStarted;
            this.totalResponsesCompleted = totalResponsesCompleted;
            this.avgProcessingTimeMs = avgProcessingTimeMs;
            this.totalBytesProcessed = totalBytesProcessed;
            this.totalFieldsProcessed = totalFieldsProcessed;
            this.totalArrayItemsProcessed = totalArrayItemsProcessed;
            this.currentActiveStreams = currentActiveStreams;
            this.throughputBytesPerSecond = throughputBytesPerSecond;
            this.errorRate = errorRate;
            this.activeStreamsInfo = activeStreamsInfo;
        }

        @Override
        public String toString() {
            return "PipelineStatistics{" +
                   "started=" + totalStreamsStarted +
                   ", completed=" + totalStreamsCompleted +
                   ", errors=" + totalStreamErrors +
                   ", avgTimeMs=" + avgProcessingTimeMs +
                   ", bytesProcessed=" + totalBytesProcessed +
                   ", throughput=" + String.format("%.2f", throughputBytesPerSecond) + "B/s" +
                   ", errorRate=" + String.format("%.2f%%", errorRate) +
                   ", active=" + currentActiveStreams +
                   '}';
        }
    }
}