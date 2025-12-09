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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Payload analyzer for intelligent HTTP/2 stream prioritization based on operation characteristics.
 *
 * This class analyzes Axis2 service operations and payload sizes to determine optimal prioritization
 * strategies as part of Phase 3 of the WildFly 32 + Axis2 HTTP/2 Cooperative Integration Plan.
 * It provides intelligent classification of operations for priority-based HTTP/2 multiplexing.
 *
 * Key features:
 * - Operation type classification (real-time, interactive, batch)
 * - Payload size analysis for flow control optimization
 * - Pattern-based operation recognition using naming conventions
 * - Configurable thresholds for different operation categories
 * - Performance-focused classification for web service workloads
 */
public class PayloadAnalyzer {
    private static final Log log = LogFactory.getLog(PayloadAnalyzer.class);

    // Payload size thresholds
    private static final long LARGE_PAYLOAD_THRESHOLD = 10 * 1024 * 1024; // 10MB
    private static final long MEDIUM_PAYLOAD_THRESHOLD = 1024 * 1024;     // 1MB
    private static final long SMALL_PAYLOAD_THRESHOLD = 64 * 1024;        // 64KB

    // Operation pattern sets for classification
    private final Set<String> realTimeOperations;
    private final Set<String> interactiveOperations;
    private final Set<String> batchOperations;

    // Compiled regex patterns for operation matching
    private final Pattern realTimePattern;
    private final Pattern interactivePattern;
    private final Pattern batchPattern;

    /**
     * Initialize payload analyzer with operation classification patterns.
     */
    public PayloadAnalyzer() {
        this.realTimeOperations = initializeRealTimeOperations();
        this.interactiveOperations = initializeInteractiveOperations();
        this.batchOperations = initializeBatchOperations();

        // Compile regex patterns for efficient matching
        this.realTimePattern = Pattern.compile(
            ".*(notify|alert|push|live|stream|event|trigger|urgent).*",
            Pattern.CASE_INSENSITIVE
        );

        this.interactivePattern = Pattern.compile(
            ".*(get|find|search|lookup|validate|check|verify|confirm).*",
            Pattern.CASE_INSENSITIVE
        );

        this.batchPattern = Pattern.compile(
            ".*(export|import|backup|migrate|bulk|batch|process|generate|report).*",
            Pattern.CASE_INSENSITIVE
        );

        log.info("PayloadAnalyzer initialized - Real-time: " + realTimeOperations.size() +
                ", Interactive: " + interactiveOperations.size() +
                ", Batch: " + batchOperations.size() + " operations");
    }

    /**
     * Analyze payload and operation characteristics for priority calculation.
     *
     * @param payloadSize Expected payload size in bytes
     * @param operationName Name of the Axis2 operation
     * @return PayloadCharacteristics containing analysis results
     */
    public PayloadCharacteristics analyzePayload(long payloadSize, String operationName) {
        if (operationName == null) {
            operationName = "";
        }

        // Analyze payload size characteristics
        PayloadSizeCategory sizeCategory = categorizePayloadSize(payloadSize);

        // Analyze operation type characteristics
        OperationType operationType = classifyOperationType(operationName);

        // Create comprehensive characteristics object
        PayloadCharacteristics characteristics = new PayloadCharacteristics(
            payloadSize,
            operationName,
            sizeCategory,
            operationType
        );

        if (log.isDebugEnabled()) {
            log.debug("Analyzed payload - Operation: " + operationName +
                     ", Size: " + formatBytes(payloadSize) +
                     ", Category: " + sizeCategory +
                     ", Type: " + operationType);
        }

        return characteristics;
    }

    /**
     * Categorize payload size for flow control optimization.
     */
    private PayloadSizeCategory categorizePayloadSize(long payloadSize) {
        if (payloadSize >= LARGE_PAYLOAD_THRESHOLD) {
            return PayloadSizeCategory.LARGE;
        } else if (payloadSize >= MEDIUM_PAYLOAD_THRESHOLD) {
            return PayloadSizeCategory.MEDIUM;
        } else if (payloadSize >= SMALL_PAYLOAD_THRESHOLD) {
            return PayloadSizeCategory.SMALL;
        } else {
            return PayloadSizeCategory.TINY;
        }
    }

    /**
     * Classify operation type based on name patterns and known operations.
     */
    private OperationType classifyOperationType(String operationName) {
        if (operationName == null || operationName.trim().isEmpty()) {
            return OperationType.UNKNOWN;
        }

        String lowerName = operationName.toLowerCase();

        // Check exact matches first (most accurate)
        if (realTimeOperations.contains(lowerName)) {
            return OperationType.REAL_TIME;
        }
        if (interactiveOperations.contains(lowerName)) {
            return OperationType.INTERACTIVE;
        }
        if (batchOperations.contains(lowerName)) {
            return OperationType.BATCH;
        }

        // Check pattern matches (less accurate but more flexible)
        if (realTimePattern.matcher(operationName).matches()) {
            return OperationType.REAL_TIME;
        }
        if (batchPattern.matcher(operationName).matches()) {
            return OperationType.BATCH;
        }
        if (interactivePattern.matcher(operationName).matches()) {
            return OperationType.INTERACTIVE;
        }

        // Default classification based on common web service patterns
        return classifyByCommonPatterns(lowerName);
    }

    /**
     * Classify operations based on common web service naming patterns.
     */
    private OperationType classifyByCommonPatterns(String lowerName) {
        // Authentication and security operations are typically real-time
        if (lowerName.contains("auth") || lowerName.contains("login") || lowerName.contains("session")) {
            return OperationType.REAL_TIME;
        }

        // CRUD operations are typically interactive
        if (lowerName.contains("create") || lowerName.contains("update") ||
            lowerName.contains("delete") || lowerName.contains("save")) {
            return OperationType.INTERACTIVE;
        }

        // Reporting and analytics are typically batch
        if (lowerName.contains("report") || lowerName.contains("analytics") ||
            lowerName.contains("summary") || lowerName.contains("statistics")) {
            return OperationType.BATCH;
        }

        return OperationType.STANDARD;
    }

    /**
     * Initialize known real-time operations.
     */
    private Set<String> initializeRealTimeOperations() {
        Set<String> operations = new HashSet<>();

        // Authentication and session operations
        operations.add("authenticate");
        operations.add("login");
        operations.add("logout");
        operations.add("refreshtoken");
        operations.add("validatetoken");

        // Notification and messaging operations
        operations.add("sendnotification");
        operations.add("publishevent");
        operations.add("pushmessage");
        operations.add("broadcastalert");

        // Real-time monitoring operations
        operations.add("getstatus");
        operations.add("heartbeat");
        operations.add("healthcheck");
        operations.add("ping");

        // Live data operations
        operations.add("getlivedata");
        operations.add("streamdata");
        operations.add("realtimeupdate");

        return operations;
    }

    /**
     * Initialize known interactive operations.
     */
    private Set<String> initializeInteractiveOperations() {
        Set<String> operations = new HashSet<>();

        // Query and retrieval operations
        operations.add("getuser");
        operations.add("findproduct");
        operations.add("searchorder");
        operations.add("lookupaccount");
        operations.add("getdetails");

        // Validation operations
        operations.add("validateuser");
        operations.add("checkinventory");
        operations.add("verifyorder");
        operations.add("confirmpayment");

        // CRUD operations
        operations.add("createuser");
        operations.add("updateprofile");
        operations.add("deleteitem");
        operations.add("saveorder");

        // Interactive business operations
        operations.add("calculateprice");
        operations.add("processorder");
        operations.add("makepayment");

        return operations;
    }

    /**
     * Initialize known batch operations.
     */
    private Set<String> initializeBatchOperations() {
        Set<String> operations = new HashSet<>();

        // Data export/import operations
        operations.add("exportdata");
        operations.add("importdata");
        operations.add("bulkimport");
        operations.add("bulkexport");

        // Reporting operations
        operations.add("generatereport");
        operations.add("createanalytics");
        operations.add("buildsummary");
        operations.add("compilestats");

        // Batch processing operations
        operations.add("processbatch");
        operations.add("bulkupdate");
        operations.add("massdelete");
        operations.add("batchcreate");

        // Maintenance operations
        operations.add("backup");
        operations.add("restore");
        operations.add("migrate");
        operations.add("cleanup");
        operations.add("optimize");

        return operations;
    }

    /**
     * Add custom operation classification.
     *
     * @param operationName Name of the operation
     * @param operationType Type classification for the operation
     */
    public void addOperationClassification(String operationName, OperationType operationType) {
        if (operationName == null || operationType == null) {
            return;
        }

        String lowerName = operationName.toLowerCase();

        // Remove from other categories first
        realTimeOperations.remove(lowerName);
        interactiveOperations.remove(lowerName);
        batchOperations.remove(lowerName);

        // Add to appropriate category
        switch (operationType) {
            case REAL_TIME:
                realTimeOperations.add(lowerName);
                break;
            case INTERACTIVE:
                interactiveOperations.add(lowerName);
                break;
            case BATCH:
                batchOperations.add(lowerName);
                break;
            default:
                // Don't add STANDARD or UNKNOWN operations to specific sets
                break;
        }

        log.info("Added operation classification: " + operationName + " -> " + operationType);
    }

    /**
     * Get analysis statistics for monitoring.
     */
    public PayloadAnalyzerStatistics getStatistics() {
        return new PayloadAnalyzerStatistics(
            realTimeOperations.size(),
            interactiveOperations.size(),
            batchOperations.size()
        );
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
     * Payload size categories for flow control optimization.
     */
    public enum PayloadSizeCategory {
        TINY,    // < 64KB
        SMALL,   // 64KB - 1MB
        MEDIUM,  // 1MB - 10MB
        LARGE    // > 10MB
    }

    /**
     * Operation type classifications for priority calculation.
     */
    public enum OperationType {
        REAL_TIME,    // Immediate response required (auth, notifications)
        INTERACTIVE,  // User-facing operations (queries, CRUD)
        STANDARD,     // Regular business operations
        BATCH,        // Background processing (reports, exports)
        UNKNOWN       // Unable to classify
    }

    /**
     * Payload analyzer statistics for monitoring.
     */
    public static class PayloadAnalyzerStatistics {
        public final int realTimeOperations;
        public final int interactiveOperations;
        public final int batchOperations;

        public PayloadAnalyzerStatistics(int realTimeOperations, int interactiveOperations, int batchOperations) {
            this.realTimeOperations = realTimeOperations;
            this.interactiveOperations = interactiveOperations;
            this.batchOperations = batchOperations;
        }

        @Override
        public String toString() {
            return "PayloadAnalyzerStatistics{" +
                   "realTime=" + realTimeOperations +
                   ", interactive=" + interactiveOperations +
                   ", batch=" + batchOperations +
                   '}';
        }
    }
}