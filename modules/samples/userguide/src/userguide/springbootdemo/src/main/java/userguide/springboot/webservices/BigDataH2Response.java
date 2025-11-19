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
package userguide.springboot.webservices;

import java.util.List;

/**
 * HTTP/2 Big Data Response for enterprise JSON processing results.
 *
 * This response class demonstrates HTTP/2 transport benefits:
 * - Memory-efficient data structures for large result sets
 * - Performance metrics and optimization indicators
 * - HTTP/2 feature utilization reporting
 * - Enterprise analytics processing results
 *
 * Performance Features:
 * - Supports both full result sets and summary-only responses
 * - Memory optimization flags for heap constraint management
 * - HTTP/2 optimization indicators for transport monitoring
 * - Processing time metrics for performance analysis
 */
public class BigDataH2Response {

    private String status;
    private String errorMessage;
    private long processingTimeMs;

    // Result data (full or summary based on size)
    private List<BigDataH2Service.DataRecord> processedRecords;
    private int processedRecordCount;
    private long totalProcessedBytes;
    private String resultSummary;

    // HTTP/2 optimization indicators
    private boolean memoryOptimized;
    private boolean http2Optimized;
    private String optimizationDetails;

    // Performance metrics
    private long memoryUsedBytes;
    private double throughputMBps;
    private int concurrentStreams;

    public BigDataH2Response() {
        this.status = "PENDING";
        this.memoryOptimized = false;
        this.http2Optimized = false;
    }

    // Status and error handling
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    // Result data
    public List<BigDataH2Service.DataRecord> getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(List<BigDataH2Service.DataRecord> processedRecords) {
        this.processedRecords = processedRecords;
    }

    public int getProcessedRecordCount() {
        return processedRecordCount;
    }

    public void setProcessedRecordCount(int processedRecordCount) {
        this.processedRecordCount = processedRecordCount;
    }

    public long getTotalProcessedBytes() {
        return totalProcessedBytes;
    }

    public void setTotalProcessedBytes(long totalProcessedBytes) {
        this.totalProcessedBytes = totalProcessedBytes;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
    }

    // HTTP/2 optimization indicators
    public boolean isMemoryOptimized() {
        return memoryOptimized;
    }

    public void setMemoryOptimized(boolean memoryOptimized) {
        this.memoryOptimized = memoryOptimized;
    }

    public boolean isHttp2Optimized() {
        return http2Optimized;
    }

    public void setHttp2Optimized(boolean http2Optimized) {
        this.http2Optimized = http2Optimized;
    }

    public String getOptimizationDetails() {
        return optimizationDetails;
    }

    public void setOptimizationDetails(String optimizationDetails) {
        this.optimizationDetails = optimizationDetails;
    }

    // Performance metrics
    public long getMemoryUsedBytes() {
        return memoryUsedBytes;
    }

    public void setMemoryUsedBytes(long memoryUsedBytes) {
        this.memoryUsedBytes = memoryUsedBytes;
    }

    public double getThroughputMBps() {
        return throughputMBps;
    }

    public void setThroughputMBps(double throughputMBps) {
        this.throughputMBps = throughputMBps;
    }

    public int getConcurrentStreams() {
        return concurrentStreams;
    }

    public void setConcurrentStreams(int concurrentStreams) {
        this.concurrentStreams = concurrentStreams;
    }

    /**
     * Calculate and set throughput based on processing metrics.
     */
    public void calculateThroughput() {
        if (processingTimeMs > 0 && totalProcessedBytes > 0) {
            double processingTimeSeconds = processingTimeMs / 1000.0;
            double processedMB = totalProcessedBytes / (1024.0 * 1024.0);
            this.throughputMBps = processedMB / processingTimeSeconds;
        }
    }

    /**
     * Get formatted processing time.
     */
    public String getFormattedProcessingTime() {
        if (processingTimeMs >= 1000) {
            return String.format("%.2f seconds", processingTimeMs / 1000.0);
        } else {
            return processingTimeMs + "ms";
        }
    }

    /**
     * Get formatted processed data size.
     */
    public String getFormattedProcessedSize() {
        if (totalProcessedBytes >= 1024 * 1024 * 1024) {
            return String.format("%.2f GB", totalProcessedBytes / (1024.0 * 1024.0 * 1024.0));
        } else if (totalProcessedBytes >= 1024 * 1024) {
            return String.format("%.2f MB", totalProcessedBytes / (1024.0 * 1024.0));
        } else if (totalProcessedBytes >= 1024) {
            return String.format("%.2f KB", totalProcessedBytes / 1024.0);
        } else {
            return totalProcessedBytes + " bytes";
        }
    }

    /**
     * Get formatted memory usage.
     */
    public String getFormattedMemoryUsage() {
        if (memoryUsedBytes >= 1024 * 1024) {
            return String.format("%.2f MB", memoryUsedBytes / (1024.0 * 1024.0));
        } else if (memoryUsedBytes >= 1024) {
            return String.format("%.2f KB", memoryUsedBytes / 1024.0);
        } else {
            return memoryUsedBytes + " bytes";
        }
    }

    /**
     * Get HTTP/2 optimization summary.
     */
    public String getOptimizationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("HTTP/2 Features: ");

        if (http2Optimized) {
            summary.append("Enabled");
            if (memoryOptimized) {
                summary.append(" (Memory Optimized)");
            }
            if (concurrentStreams > 1) {
                summary.append(" (").append(concurrentStreams).append(" Concurrent Streams)");
            }
        } else {
            summary.append("Standard");
        }

        return summary.toString();
    }

    /**
     * Check if response was successful.
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(status);
    }

    /**
     * Check if large dataset processing was used.
     */
    public boolean isLargeDatasetProcessing() {
        return totalProcessedBytes > 50 * 1024 * 1024; // 50MB+
    }

    @Override
    public String toString() {
        return "BigDataH2Response [" +
                "status='" + status + '\'' +
                ", processingTime=" + getFormattedProcessingTime() +
                ", processedRecords=" + processedRecordCount +
                ", processedSize=" + getFormattedProcessedSize() +
                ", http2Optimized=" + http2Optimized +
                ", memoryOptimized=" + memoryOptimized +
                ", throughput=" + String.format("%.2f MB/s", throughputMBps) +
                ']';
    }
}