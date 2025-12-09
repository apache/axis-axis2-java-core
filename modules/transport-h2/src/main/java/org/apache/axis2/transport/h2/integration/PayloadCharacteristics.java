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

/**
 * Payload characteristics for HTTP/2 stream priority calculation and flow control optimization.
 *
 * This class encapsulates the analysis results from PayloadAnalyzer as part of Phase 3 of the
 * WildFly 32 + Axis2 HTTP/2 Cooperative Integration Plan. It provides structured information
 * about payload size and operation type characteristics for intelligent stream prioritization.
 *
 * Key features:
 * - Comprehensive payload size and operation type information
 * - Convenient boolean methods for priority decision logic
 * - Immutable characteristics object for thread-safe usage
 * - Human-readable string representation for debugging
 * - Integration with HTTP/2 flow control strategies
 */
public class PayloadCharacteristics {
    private final long payloadSize;
    private final String operationName;
    private final PayloadAnalyzer.PayloadSizeCategory sizeCategory;
    private final PayloadAnalyzer.OperationType operationType;

    /**
     * Create payload characteristics with complete analysis results.
     *
     * @param payloadSize Expected payload size in bytes
     * @param operationName Name of the Axis2 operation
     * @param sizeCategory Categorized payload size classification
     * @param operationType Classified operation type
     */
    public PayloadCharacteristics(long payloadSize, String operationName,
                                 PayloadAnalyzer.PayloadSizeCategory sizeCategory,
                                 PayloadAnalyzer.OperationType operationType) {
        this.payloadSize = payloadSize;
        this.operationName = operationName != null ? operationName : "";
        this.sizeCategory = sizeCategory != null ? sizeCategory : PayloadAnalyzer.PayloadSizeCategory.SMALL;
        this.operationType = operationType != null ? operationType : PayloadAnalyzer.OperationType.STANDARD;
    }

    // Accessor methods

    /**
     * Get the expected payload size in bytes.
     *
     * @return Payload size in bytes
     */
    public long getPayloadSize() {
        return payloadSize;
    }

    /**
     * Get the operation name.
     *
     * @return Axis2 operation name
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Get the payload size category.
     *
     * @return Categorized payload size classification
     */
    public PayloadAnalyzer.PayloadSizeCategory getSizeCategory() {
        return sizeCategory;
    }

    /**
     * Get the operation type classification.
     *
     * @return Classified operation type
     */
    public PayloadAnalyzer.OperationType getOperationType() {
        return operationType;
    }

    // Convenience methods for priority calculation

    /**
     * Check if this is a large payload that may impact HTTP/2 multiplexing.
     *
     * @return true if payload is classified as LARGE, false otherwise
     */
    public boolean isLargePayload() {
        return sizeCategory == PayloadAnalyzer.PayloadSizeCategory.LARGE;
    }

    /**
     * Check if this is a medium-sized payload requiring moderate flow control.
     *
     * @return true if payload is classified as MEDIUM, false otherwise
     */
    public boolean isMediumPayload() {
        return sizeCategory == PayloadAnalyzer.PayloadSizeCategory.MEDIUM;
    }

    /**
     * Check if this is a small payload suitable for aggressive multiplexing.
     *
     * @return true if payload is classified as SMALL or TINY, false otherwise
     */
    public boolean isSmallPayload() {
        return sizeCategory == PayloadAnalyzer.PayloadSizeCategory.SMALL ||
               sizeCategory == PayloadAnalyzer.PayloadSizeCategory.TINY;
    }

    /**
     * Check if this operation requires real-time processing with high priority.
     *
     * @return true if operation type is REAL_TIME, false otherwise
     */
    public boolean isRealTimeOperation() {
        return operationType == PayloadAnalyzer.OperationType.REAL_TIME;
    }

    /**
     * Check if this operation is user-facing and requires interactive response times.
     *
     * @return true if operation type is INTERACTIVE, false otherwise
     */
    public boolean isInteractiveOperation() {
        return operationType == PayloadAnalyzer.OperationType.INTERACTIVE;
    }

    /**
     * Check if this operation is a background/batch process that can accept lower priority.
     *
     * @return true if operation type is BATCH, false otherwise
     */
    public boolean isBatchOperation() {
        return operationType == PayloadAnalyzer.OperationType.BATCH;
    }

    /**
     * Check if this operation is a standard business operation with normal priority.
     *
     * @return true if operation type is STANDARD, false otherwise
     */
    public boolean isStandardOperation() {
        return operationType == PayloadAnalyzer.OperationType.STANDARD;
    }

    /**
     * Check if the operation type could not be classified.
     *
     * @return true if operation type is UNKNOWN, false otherwise
     */
    public boolean isUnknownOperation() {
        return operationType == PayloadAnalyzer.OperationType.UNKNOWN;
    }

    /**
     * Determine if this payload should use conservative flow control.
     * Conservative flow control is recommended for large payloads or batch operations.
     *
     * @return true if conservative flow control is recommended, false for aggressive
     */
    public boolean shouldUseConservativeFlowControl() {
        return isLargePayload() || isBatchOperation();
    }

    /**
     * Determine if this payload should use aggressive flow control.
     * Aggressive flow control is suitable for small payloads and real-time operations.
     *
     * @return true if aggressive flow control is recommended, false for conservative
     */
    public boolean shouldUseAggressiveFlowControl() {
        return isSmallPayload() && (isRealTimeOperation() || isInteractiveOperation());
    }

    /**
     * Calculate suggested HTTP/2 window size based on payload characteristics.
     *
     * @return Suggested window size in bytes
     */
    public int getSuggestedWindowSize() {
        switch (sizeCategory) {
            case LARGE:
                return 1024 * 1024; // 1MB for large payloads
            case MEDIUM:
                return 512 * 1024;  // 512KB for medium payloads
            case SMALL:
                return 256 * 1024;  // 256KB for small payloads
            case TINY:
            default:
                return 64 * 1024;   // 64KB for tiny payloads
        }
    }

    /**
     * Get priority boost/penalty based on operation characteristics.
     * Positive values increase priority, negative values decrease priority.
     *
     * @return Priority adjustment value
     */
    public int getPriorityAdjustment() {
        int adjustment = 0;

        // Operation type adjustments
        switch (operationType) {
            case REAL_TIME:
                adjustment += 15; // High priority boost for real-time operations
                break;
            case INTERACTIVE:
                adjustment += 8;  // Moderate priority boost for interactive operations
                break;
            case BATCH:
                adjustment -= 12; // Priority penalty for batch operations
                break;
            case STANDARD:
                adjustment += 0;  // No adjustment for standard operations
                break;
            case UNKNOWN:
                adjustment -= 2;  // Small penalty for unknown operations (conservative approach)
                break;
        }

        // Payload size adjustments
        switch (sizeCategory) {
            case LARGE:
                adjustment -= 10; // Priority penalty for large payloads
                break;
            case MEDIUM:
                adjustment -= 3;  // Small penalty for medium payloads
                break;
            case SMALL:
                adjustment += 2;  // Small boost for small payloads
                break;
            case TINY:
                adjustment += 5;  // Priority boost for tiny payloads
                break;
        }

        return adjustment;
    }

    /**
     * Format payload size for human-readable display.
     *
     * @return Formatted payload size string
     */
    public String getFormattedPayloadSize() {
        if (payloadSize >= 1024L * 1024 * 1024) {
            return String.format("%.2fGB", payloadSize / (1024.0 * 1024.0 * 1024.0));
        } else if (payloadSize >= 1024L * 1024) {
            return String.format("%.2fMB", payloadSize / (1024.0 * 1024.0));
        } else if (payloadSize >= 1024L) {
            return String.format("%.2fKB", payloadSize / 1024.0);
        } else {
            return payloadSize + "B";
        }
    }

    @Override
    public String toString() {
        return "PayloadCharacteristics{" +
               "operation='" + operationName + '\'' +
               ", size=" + getFormattedPayloadSize() +
               ", sizeCategory=" + sizeCategory +
               ", operationType=" + operationType +
               ", priorityAdjustment=" + getPriorityAdjustment() +
               ", windowSize=" + getSuggestedWindowSize() +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PayloadCharacteristics that = (PayloadCharacteristics) o;

        if (payloadSize != that.payloadSize) return false;
        if (!operationName.equals(that.operationName)) return false;
        if (sizeCategory != that.sizeCategory) return false;
        return operationType == that.operationType;
    }

    @Override
    public int hashCode() {
        int result = (int) (payloadSize ^ (payloadSize >>> 32));
        result = 31 * result + operationName.hashCode();
        result = 31 * result + sizeCategory.hashCode();
        result = 31 * result + operationType.hashCode();
        return result;
    }
}