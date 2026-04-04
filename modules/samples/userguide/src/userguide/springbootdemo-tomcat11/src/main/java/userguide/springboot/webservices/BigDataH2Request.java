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

/**
 * HTTP/2 Big Data Request for enterprise JSON processing.
 *
 * This request class demonstrates HTTP/2 transport capabilities for large JSON datasets:
 * - Support for dataset size specifications (MB/GB scale)
 * - Processing mode selection (streaming, multiplexing, standard)
 * - Enterprise security and validation requirements
 * - Memory constraint awareness for 2GB heap environments
 *
 * Example Usage:
 * - Small datasets (< 10MB): Standard HTTP/2 processing
 * - Medium datasets (10-50MB): HTTP/2 multiplexing optimization
 * - Large datasets (50MB+): HTTP/2 streaming with memory management
 */
public class BigDataH2Request {

    private String datasetId;
    private long datasetSize; // Size in bytes
    private String processingMode; // "streaming", "multiplexing", "standard"
    private boolean enableMemoryOptimization;
    private String analyticsType;
    private String[] filterCriteria;

    public BigDataH2Request() {
        // Default constructor
    }

    public BigDataH2Request(String datasetId, long datasetSize) {
        this.datasetId = datasetId;
        this.datasetSize = datasetSize;
        this.processingMode = determineOptimalProcessingMode(datasetSize);
        this.enableMemoryOptimization = datasetSize > 10 * 1024 * 1024; // Enable for 10MB+
    }

    /**
     * Determine optimal processing mode based on dataset size.
     */
    private String determineOptimalProcessingMode(long sizeBytes) {
        if (sizeBytes > 50 * 1024 * 1024) {
            return "streaming"; // 50MB+ requires streaming
        } else if (sizeBytes > 10 * 1024 * 1024) {
            return "multiplexing"; // 10-50MB benefits from multiplexing
        } else {
            return "standard"; // < 10MB uses standard processing
        }
    }

    // Getters and setters
    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public long getDatasetSize() {
        return datasetSize;
    }

    public void setDatasetSize(long datasetSize) {
        this.datasetSize = datasetSize;
        // Auto-adjust processing mode when size changes
        this.processingMode = determineOptimalProcessingMode(datasetSize);
        this.enableMemoryOptimization = datasetSize > 10 * 1024 * 1024;
    }

    public String getProcessingMode() {
        return processingMode;
    }

    public void setProcessingMode(String processingMode) {
        this.processingMode = processingMode;
    }

    public boolean isEnableMemoryOptimization() {
        return enableMemoryOptimization;
    }

    public void setEnableMemoryOptimization(boolean enableMemoryOptimization) {
        this.enableMemoryOptimization = enableMemoryOptimization;
    }

    public String getAnalyticsType() {
        return analyticsType;
    }

    public void setAnalyticsType(String analyticsType) {
        this.analyticsType = analyticsType;
    }

    public String[] getFilterCriteria() {
        return filterCriteria;
    }

    public void setFilterCriteria(String[] filterCriteria) {
        this.filterCriteria = filterCriteria;
    }

    /**
     * Get human-readable dataset size.
     */
    public String getFormattedDatasetSize() {
        if (datasetSize >= 1024 * 1024 * 1024) {
            return String.format("%.2f GB", datasetSize / (1024.0 * 1024.0 * 1024.0));
        } else if (datasetSize >= 1024 * 1024) {
            return String.format("%.2f MB", datasetSize / (1024.0 * 1024.0));
        } else if (datasetSize >= 1024) {
            return String.format("%.2f KB", datasetSize / 1024.0);
        } else {
            return datasetSize + " bytes";
        }
    }

    /**
     * Check if this request qualifies for HTTP/2 streaming optimization.
     */
    public boolean requiresStreaming() {
        return datasetSize > 50 * 1024 * 1024; // 50MB threshold
    }

    /**
     * Check if this request benefits from HTTP/2 multiplexing.
     */
    public boolean benefitsFromMultiplexing() {
        return datasetSize > 10 * 1024 * 1024 && datasetSize <= 50 * 1024 * 1024;
    }

    @Override
    public String toString() {
        return "BigDataH2Request [" +
                "datasetId='" + datasetId + '\'' +
                ", datasetSize=" + getFormattedDatasetSize() +
                ", processingMode='" + processingMode + '\'' +
                ", enableMemoryOptimization=" + enableMemoryOptimization +
                ", analyticsType='" + analyticsType + '\'' +
                ", requiresStreaming=" + requiresStreaming() +
                ", benefitsFromMultiplexing=" + benefitsFromMultiplexing() +
                ']';
    }
}