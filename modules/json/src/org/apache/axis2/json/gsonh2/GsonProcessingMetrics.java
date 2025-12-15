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

package org.apache.axis2.json.gsonh2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Map;

/**
 * Performance Metrics Collection for Enhanced GSON H2 JSON Processing.
 *
 * This class provides comprehensive performance monitoring capabilities extracted
 * from the HTTP/2 integration StreamingMetrics research, adapted for standard JSON
 * processing using GSON instead of Moshi.
 *
 * Key Features Extracted from HTTP/2 Integration:
 * - Thread-safe metrics collection using atomic operations (from StreamingMetrics)
 * - Request-level tracking with unique identifiers
 * - Performance statistics aggregation (latency, throughput, errors)
 * - Memory usage monitoring and optimization suggestions (from HTTP2MemoryCoordinator)
 * - Field-level processing metrics for optimization analysis (from field processing patterns)
 * - Historical performance trend analysis for optimization recommendations
 * - Large payload processing metrics (from MoshiStreamingPipelineCooperativeTest patterns)
 */
public class GsonProcessingMetrics {
    private static final Log log = LogFactory.getLog(GsonProcessingMetrics.class);

    // Thread-safe counters for aggregate metrics (pattern from StreamingMetrics)
    private final AtomicLong totalRequestsProcessed = new AtomicLong(0);
    private final AtomicLong totalBytesProcessed = new AtomicLong(0);
    private final AtomicLong totalProcessingTimeMs = new AtomicLong(0);
    private final AtomicLong totalErrorCount = new AtomicLong(0);
    private final AtomicLong asyncRequestCount = new AtomicLong(0);
    private final AtomicLong largePayloadCount = new AtomicLong(0);

    // Performance tracking (concepts from HTTP/2 integration analysis)
    private final AtomicReference<Long> minProcessingTimeMs = new AtomicReference<>(Long.MAX_VALUE);
    private final AtomicReference<Long> maxProcessingTimeMs = new AtomicReference<>(0L);
    private final AtomicReference<Long> maxPayloadSize = new AtomicReference<>(0L);

    // Active request tracking (pattern from HTTP/2 integration)
    private final Map<String, RequestMetrics> activeRequests = new ConcurrentHashMap<>();

    // Field-level optimization metrics (extracted from HTTP/2 field processing analysis)
    private final Map<String, FieldProcessingStats> fieldStatistics = new ConcurrentHashMap<>();

    // Performance trend tracking (for 12-18s response time analysis)
    private final AtomicLong slowRequestCount = new AtomicLong(0); // >10s requests
    private final AtomicLong verySlowRequestCount = new AtomicLong(0); // >15s requests

    /**
     * Record the start of request processing (pattern from StreamingMetrics).
     */
    public void recordProcessingStart(String requestId, long payloadSize, boolean isAsync) {
        RequestMetrics request = new RequestMetrics(requestId, payloadSize, System.currentTimeMillis(), isAsync);
        activeRequests.put(requestId, request);

        // Update aggregate counters
        totalRequestsProcessed.incrementAndGet();
        totalBytesProcessed.addAndGet(payloadSize);

        if (isAsync) {
            asyncRequestCount.incrementAndGet();
        }

        // Track large payload patterns (from HTTP/2 integration analysis)
        if (payloadSize > 10 * 1024 * 1024) { // 10MB threshold from HTTP/2 integration
            largePayloadCount.incrementAndGet();
        }

        // Update max payload size
        updateMaxPayloadSize(payloadSize);

        if (log.isDebugEnabled()) {
            log.debug("Started GSON H2 processing request: " + requestId + " (" + formatBytes(payloadSize) +
                      ", async=" + isAsync + ")");
        }
    }

    /**
     * Record successful completion of request processing.
     */
    public void recordProcessingComplete(String requestId, long payloadSize, long processingTimeMs) {
        RequestMetrics request = activeRequests.remove(requestId);

        if (request != null) {
            // Update timing statistics
            totalProcessingTimeMs.addAndGet(processingTimeMs);
            updateMinMaxProcessingTime(processingTimeMs);

            // Track slow request patterns (based on production 12-18s analysis)
            if (processingTimeMs > 10000) { // 10s threshold
                slowRequestCount.incrementAndGet();
                log.warn("Slow GSON H2 processing detected: " + requestId + " took " + processingTimeMs + "ms");
            }
            if (processingTimeMs > 15000) { // 15s threshold (production issue analysis)
                verySlowRequestCount.incrementAndGet();
                log.error("Very slow GSON H2 processing detected: " + requestId + " took " + processingTimeMs + "ms - " +
                         "This matches the production 12-18s issue pattern");
            }

            if (log.isDebugEnabled()) {
                log.debug("Completed GSON H2 processing request: " + requestId + " in " + processingTimeMs + "ms");
            }
        } else {
            // Fallback for requests not properly tracked at start
            recordProcessingStart(requestId, payloadSize, false);
            totalProcessingTimeMs.addAndGet(processingTimeMs);
            updateMinMaxProcessingTime(processingTimeMs);
        }
    }

    /**
     * Record processing error.
     */
    public void recordProcessingError(String requestId, Exception error, long processingTimeMs) {
        RequestMetrics request = activeRequests.remove(requestId);
        totalErrorCount.incrementAndGet();

        if (processingTimeMs > 0) {
            totalProcessingTimeMs.addAndGet(processingTimeMs);
            updateMinMaxProcessingTime(processingTimeMs);
        }

        log.warn("GSON H2 processing error for request: " + requestId + " after " + processingTimeMs + "ms: " +
                 error.getMessage());
    }

    /**
     * Record field-specific processing statistics (extracted from HTTP/2 field optimization patterns).
     */
    public void recordFieldProcessing(String fieldName, String fieldType, long processingTimeNs, int itemCount) {
        FieldProcessingStats stats = fieldStatistics.computeIfAbsent(fieldName,
            k -> new FieldProcessingStats(fieldName, fieldType));

        stats.addProcessingTime(processingTimeNs);
        stats.addItemCount(itemCount);

        // Log performance insights for optimization (pattern from HTTP/2 integration)
        if (stats.getTotalInvocations() % 1000 == 0) {
            if (log.isDebugEnabled()) {
                log.debug("Field processing stats for '" + fieldName + "': " +
                          "avg=" + (stats.getTotalProcessingTimeNs() / stats.getTotalInvocations() / 1000000.0) + "ms, " +
                          "count=" + stats.getTotalInvocations());
            }
        }
    }

    /**
     * Record large array processing (optimization pattern from HTTP/2 integration).
     */
    public void recordLargeArrayProcessing(String fieldName, int arraySize, long processingTimeMs) {
        recordFieldProcessing(fieldName, "LARGE_ARRAY", processingTimeMs * 1000000, arraySize);

        // Special tracking for very large arrays (pattern from HTTP/2 integration)
        if (arraySize > 10000) {
            log.info("Processed large array field '" + fieldName + "': " + arraySize +
                     " items in " + processingTimeMs + "ms (GSON H2 optimization)");
        }
    }

    /**
     * Record memory optimization trigger (concept from HTTP2MemoryCoordinator).
     */
    public void recordMemoryOptimization(String requestId, String optimizationType) {
        if (log.isDebugEnabled()) {
            log.debug("Memory optimization applied for GSON H2 request: " + requestId +
                      " (type: " + optimizationType + ")");
        }
    }

    /**
     * Record streaming pipeline activity (pattern from HTTP/2 integration).
     */
    public void recordStreamingActivity(String requestId, String activityType, long durationMs) {
        if (log.isDebugEnabled()) {
            log.debug("GSON H2 streaming activity: " + requestId + " - " + activityType + " (" + durationMs + "ms)");
        }
    }

    /**
     * Get comprehensive statistics (API from HTTP/2 StreamingMetrics).
     */
    public Statistics getStatistics() {
        long totalRequests = totalRequestsProcessed.get();
        long totalBytes = totalBytesProcessed.get();
        long totalTime = totalProcessingTimeMs.get();
        long errorCount = totalErrorCount.get();

        double avgProcessingTime = totalRequests > 0 ? (double) totalTime / totalRequests : 0.0;
        double throughputMBps = totalTime > 0 ? (totalBytes / 1024.0 / 1024.0) / (totalTime / 1000.0) : 0.0;
        double errorRate = totalRequests > 0 ? (double) errorCount / totalRequests : 0.0;

        return new Statistics(
            totalRequests,
            totalBytes,
            avgProcessingTime,
            minProcessingTimeMs.get() == Long.MAX_VALUE ? 0 : minProcessingTimeMs.get(),
            maxProcessingTimeMs.get(),
            maxPayloadSize.get(),
            throughputMBps,
            errorRate,
            asyncRequestCount.get(),
            largePayloadCount.get(),
            activeRequests.size(),
            fieldStatistics.size(),
            slowRequestCount.get(),
            verySlowRequestCount.get()
        );
    }

    /**
     * Get field-level optimization insights (extracted from HTTP/2 field processing patterns).
     */
    public Map<String, FieldProcessingStats> getFieldStatistics() {
        return new ConcurrentHashMap<>(fieldStatistics);
    }

    /**
     * Get performance optimization recommendations based on collected metrics and HTTP/2 integration analysis.
     */
    public String getOptimizationRecommendations() {
        StringBuilder recommendations = new StringBuilder();
        Statistics stats = getStatistics();

        recommendations.append("Enhanced GSON H2 JSON Processing Optimization Recommendations:\n");

        // Async processing recommendations (based on HTTP/2 integration patterns)
        double asyncPercentage = stats.getTotalRequests() > 0 ?
            (double) stats.getAsyncRequestCount() / stats.getTotalRequests() * 100 : 0;

        if (asyncPercentage < 20 && stats.getAverageProcessingTimeMs() > 1000) {
            recommendations.append("- Consider increasing async processing threshold for better performance\n");
            recommendations.append("  Current async threshold: 1MB, consider lowering to 512KB for your workload\n");
        }

        // Large payload optimization (from HTTP/2 integration analysis)
        double largePayloadPercentage = stats.getTotalRequests() > 0 ?
            (double) stats.getLargePayloadCount() / stats.getTotalRequests() * 100 : 0;

        if (largePayloadPercentage > 30) {
            recommendations.append("- High large payload ratio (").append(String.format("%.1f", largePayloadPercentage))
                            .append("%) - consider streaming optimizations from HTTP/2 integration\n");
        }

        // Error rate analysis
        if (stats.getErrorRate() > 0.05) { // 5% error rate
            recommendations.append("- High error rate (").append(String.format("%.2f", stats.getErrorRate() * 100))
                            .append("%) - investigate error patterns\n");
        }

        // Performance analysis based on production 12-18s issue
        if (stats.getAverageProcessingTimeMs() > 5000) {
            recommendations.append("- Average processing time (").append(String.format("%.1f", stats.getAverageProcessingTimeMs()))
                            .append("ms) is high - consider implementing async processing patterns from HTTP/2 integration\n");
        }

        // Slow request analysis (based on production issue patterns)
        if (stats.getSlowRequestCount() > 0) {
            double slowPercentage = stats.getTotalRequests() > 0 ?
                (double) stats.getSlowRequestCount() / stats.getTotalRequests() * 100 : 0;
            recommendations.append("- Slow requests (>10s): ").append(stats.getSlowRequestCount())
                            .append(" (").append(String.format("%.2f", slowPercentage)).append("%) detected\n");
            recommendations.append("  Consider implementing CompletableFuture async processing for large payloads\n");
        }

        if (stats.getVerySlowRequestCount() > 0) {
            recommendations.append("- Very slow requests (>15s): ").append(stats.getVerySlowRequestCount())
                            .append(" - This matches the production 12-18s issue pattern!\n");
            recommendations.append("  CRITICAL: Enable async processing immediately for payloads >1MB\n");
        }

        // Field-level recommendations (from HTTP/2 field processing analysis)
        for (Map.Entry<String, FieldProcessingStats> entry : fieldStatistics.entrySet()) {
            FieldProcessingStats fieldStats = entry.getValue();
            double avgFieldTime = fieldStats.getTotalInvocations() > 0 ?
                fieldStats.getTotalProcessingTimeNs() / fieldStats.getTotalInvocations() / 1000000.0 : 0;

            if (avgFieldTime > 100 && fieldStats.getTotalInvocations() > 100) { // >100ms avg for frequently used fields
                recommendations.append("- Field '").append(entry.getKey())
                                .append("' has high processing time (").append(String.format("%.1f", avgFieldTime))
                                .append("ms avg) - consider specialized optimization patterns from HTTP/2 integration\n");
            }
        }

        return recommendations.toString();
    }

    /**
     * Reset all statistics.
     */
    public void resetStatistics() {
        totalRequestsProcessed.set(0);
        totalBytesProcessed.set(0);
        totalProcessingTimeMs.set(0);
        totalErrorCount.set(0);
        asyncRequestCount.set(0);
        largePayloadCount.set(0);
        slowRequestCount.set(0);
        verySlowRequestCount.set(0);
        minProcessingTimeMs.set(Long.MAX_VALUE);
        maxProcessingTimeMs.set(0L);
        maxPayloadSize.set(0L);

        activeRequests.clear();
        fieldStatistics.clear();

        log.info("GSON H2 GsonProcessingMetrics statistics reset");
    }

    // Private helper methods

    private void updateMinMaxProcessingTime(long processingTimeMs) {
        minProcessingTimeMs.updateAndGet(current -> Math.min(current, processingTimeMs));
        maxProcessingTimeMs.updateAndGet(current -> Math.max(current, processingTimeMs));
    }

    private void updateMaxPayloadSize(long payloadSize) {
        maxPayloadSize.updateAndGet(current -> Math.max(current, payloadSize));
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
     * Request-level metrics tracking (pattern from HTTP/2 integration).
     */
    private static class RequestMetrics {
        private final String requestId;
        private final long payloadSize;
        private final long startTimeMs;
        private final boolean isAsync;

        public RequestMetrics(String requestId, long payloadSize, long startTimeMs, boolean isAsync) {
            this.requestId = requestId;
            this.payloadSize = payloadSize;
            this.startTimeMs = startTimeMs;
            this.isAsync = isAsync;
        }

        public String getRequestId() { return requestId; }
        public long getPayloadSize() { return payloadSize; }
        public long getStartTimeMs() { return startTimeMs; }
        public boolean isAsync() { return isAsync; }
    }

    /**
     * Field-level processing statistics (extracted from HTTP/2 field processing optimization analysis).
     */
    public static class FieldProcessingStats {
        private final String fieldName;
        private final String fieldType;
        private final AtomicLong totalProcessingTimeNs = new AtomicLong(0);
        private final AtomicLong totalInvocations = new AtomicLong(0);
        private final AtomicLong totalItemCount = new AtomicLong(0);

        public FieldProcessingStats(String fieldName, String fieldType) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
        }

        public void addProcessingTime(long processingTimeNs) {
            totalProcessingTimeNs.addAndGet(processingTimeNs);
            totalInvocations.incrementAndGet();
        }

        public void addItemCount(int itemCount) {
            totalItemCount.addAndGet(itemCount);
        }

        public String getFieldName() { return fieldName; }
        public String getFieldType() { return fieldType; }
        public long getTotalProcessingTimeNs() { return totalProcessingTimeNs.get(); }
        public long getTotalInvocations() { return totalInvocations.get(); }
        public long getTotalItemCount() { return totalItemCount.get(); }

        public double getAverageProcessingTimeMs() {
            long invocations = totalInvocations.get();
            return invocations > 0 ? totalProcessingTimeNs.get() / invocations / 1000000.0 : 0.0;
        }
    }

    /**
     * Comprehensive statistics data class (based on HTTP/2 StreamingMetrics).
     */
    public static class Statistics {
        private final long totalRequests;
        private final long totalBytes;
        private final double averageProcessingTimeMs;
        private final long minProcessingTimeMs;
        private final long maxProcessingTimeMs;
        private final long maxPayloadSize;
        private final double throughputMBps;
        private final double errorRate;
        private final long asyncRequestCount;
        private final long largePayloadCount;
        private final int activeRequestCount;
        private final int trackedFieldCount;
        private final long slowRequestCount;        // Added for production issue analysis
        private final long verySlowRequestCount;    // Added for production issue analysis

        public Statistics(long totalRequests, long totalBytes, double averageProcessingTimeMs,
                         long minProcessingTimeMs, long maxProcessingTimeMs, long maxPayloadSize,
                         double throughputMBps, double errorRate, long asyncRequestCount,
                         long largePayloadCount, int activeRequestCount, int trackedFieldCount,
                         long slowRequestCount, long verySlowRequestCount) {
            this.totalRequests = totalRequests;
            this.totalBytes = totalBytes;
            this.averageProcessingTimeMs = averageProcessingTimeMs;
            this.minProcessingTimeMs = minProcessingTimeMs;
            this.maxProcessingTimeMs = maxProcessingTimeMs;
            this.maxPayloadSize = maxPayloadSize;
            this.throughputMBps = throughputMBps;
            this.errorRate = errorRate;
            this.asyncRequestCount = asyncRequestCount;
            this.largePayloadCount = largePayloadCount;
            this.activeRequestCount = activeRequestCount;
            this.trackedFieldCount = trackedFieldCount;
            this.slowRequestCount = slowRequestCount;
            this.verySlowRequestCount = verySlowRequestCount;
        }

        // Getters
        public long getTotalRequests() { return totalRequests; }
        public long getTotalBytes() { return totalBytes; }
        public double getAverageProcessingTimeMs() { return averageProcessingTimeMs; }
        public long getMinProcessingTimeMs() { return minProcessingTimeMs; }
        public long getMaxProcessingTimeMs() { return maxProcessingTimeMs; }
        public long getMaxPayloadSize() { return maxPayloadSize; }
        public double getThroughputMBps() { return throughputMBps; }
        public double getErrorRate() { return errorRate; }
        public long getAsyncRequestCount() { return asyncRequestCount; }
        public long getLargePayloadCount() { return largePayloadCount; }
        public int getActiveRequestCount() { return activeRequestCount; }
        public int getTrackedFieldCount() { return trackedFieldCount; }
        public long getSlowRequestCount() { return slowRequestCount; }
        public long getVerySlowRequestCount() { return verySlowRequestCount; }

        @Override
        public String toString() {
            return String.format(
                "GsonH2Metrics{requests=%d, bytes=%s, avgTime=%.1fms, " +
                "minTime=%dms, maxTime=%dms, maxPayload=%s, throughput=%.2fMB/s, " +
                "errorRate=%.2f%%, async=%d, largePayload=%d, active=%d, fields=%d, " +
                "slow=%d, verySlow=%d}",
                totalRequests, formatBytesStatic(totalBytes), averageProcessingTimeMs,
                minProcessingTimeMs, maxProcessingTimeMs, formatBytesStatic(maxPayloadSize),
                throughputMBps, errorRate * 100, asyncRequestCount, largePayloadCount,
                activeRequestCount, trackedFieldCount, slowRequestCount, verySlowRequestCount
            );
        }

        private static String formatBytesStatic(long bytes) {
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
    }
}