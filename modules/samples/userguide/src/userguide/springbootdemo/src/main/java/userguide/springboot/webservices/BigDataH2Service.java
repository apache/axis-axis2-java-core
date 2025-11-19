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

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Validator;

import org.springframework.stereotype.Component;

/**
 * HTTP/2 Big Data Service demonstrating enterprise JSON processing capabilities.
 *
 * This service showcases HTTP/2 transport benefits for large JSON payloads:
 * - Streaming optimization for 10MB+ JSON datasets
 * - Connection multiplexing for concurrent requests
 * - Memory-efficient processing within 2GB heap constraints
 * - Enhanced performance for enterprise big data analytics
 *
 * Key Features:
 * - Large JSON dataset generation and processing
 * - HTTP/2 streaming support for memory efficiency
 * - Enterprise security validation with OWASP ESAPI
 * - Performance monitoring and metrics collection
 */
@Component
public class BigDataH2Service {

    private static final Logger logger = LogManager.getLogger(BigDataH2Service.class);

    /**
     * Process large JSON datasets using HTTP/2 streaming optimization.
     * Demonstrates enterprise big data processing capabilities.
     */
    public BigDataH2Response processBigDataSet(BigDataH2Request request) {
        String uuid = UUID.randomUUID().toString();
        String logPrefix = "BigDataH2Service.processBigDataSet() , uuid: " + uuid + " , ";

        logger.info(logPrefix + "processing HTTP/2 big data request with dataset size: " +
                   (request.getDatasetSize() / 1024 / 1024) + "MB");

        BigDataH2Response response = new BigDataH2Response();
        long startTime = System.currentTimeMillis();

        try {
            // Security validation for all inputs
            Validator validator = ESAPI.validator();

            // Validate dataset identifier
            boolean datasetIdValid = validator.isValidInput("datasetId",
                request.getDatasetId(), "SafeString", 100, false);
            if (!datasetIdValid) {
                logger.error(logPrefix + "invalid dataset ID: " + request.getDatasetId());
                response.setStatus("FAILED");
                response.setErrorMessage("Invalid dataset identifier");
                return response;
            }

            // Validate processing mode
            if (request.getProcessingMode() != null) {
                boolean modeValid = validator.isValidInput("processingMode",
                    request.getProcessingMode(), "SafeString", 50, false);
                if (!modeValid) {
                    logger.error(logPrefix + "invalid processing mode: " + request.getProcessingMode());
                    response.setStatus("FAILED");
                    response.setErrorMessage("Invalid processing mode");
                    return response;
                }
            }

            // Process large dataset based on size requirements
            if (request.getDatasetSize() > 50 * 1024 * 1024) {
                // Use HTTP/2 streaming for datasets > 50MB
                response = processLargeDatasetWithStreaming(request, uuid);
            } else if (request.getDatasetSize() > 10 * 1024 * 1024) {
                // Use HTTP/2 multiplexing for medium datasets 10-50MB
                response = processMediumDatasetWithMultiplexing(request, uuid);
            } else {
                // Standard processing for smaller datasets
                response = processStandardDataset(request, uuid);
            }

            long processingTime = System.currentTimeMillis() - startTime;
            response.setProcessingTimeMs(processingTime);
            response.setStatus("SUCCESS");

            logger.info(logPrefix + "completed processing in " + processingTime + "ms. " +
                       "Memory efficient: " + response.isMemoryOptimized() +
                       ", HTTP/2 features: " + response.isHttp2Optimized());

            return response;

        } catch (Exception ex) {
            logger.error(logPrefix + "processing failed: " + ex.getMessage(), ex);
            response.setStatus("FAILED");
            response.setErrorMessage("Big data processing error: " + ex.getMessage());
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            return response;
        }
    }

    /**
     * Process large datasets (50MB+) using HTTP/2 streaming optimization.
     */
    private BigDataH2Response processLargeDatasetWithStreaming(BigDataH2Request request, String uuid) {
        String logPrefix = "BigDataH2Service.processLargeDatasetWithStreaming() , uuid: " + uuid + " , ";
        logger.info(logPrefix + "using HTTP/2 streaming for large dataset: " +
                   (request.getDatasetSize() / 1024 / 1024) + "MB");

        BigDataH2Response response = new BigDataH2Response();

        // Generate big data structure for processing
        List<DataRecord> processedRecords = new ArrayList<>();
        int numRecords = request.getDatasetSize() / 1024; // Estimate records based on size

        // Simulate streaming processing in chunks
        int chunkSize = 1000;
        long totalProcessedBytes = 0;

        for (int i = 0; i < numRecords; i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, numRecords);

            // Process chunk of records
            for (int j = i; j < endIndex; j++) {
                DataRecord record = new DataRecord();
                record.setRecordId("STREAM_RECORD_" + String.format("%08d", j));
                record.setIdentifier("dataset_" + request.getDatasetId() + "_record_" + j);
                record.setCategory("streaming_analytics");
                record.setValue(j * 1.5 + Math.random() * 100);
                record.setProcessedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                processedRecords.add(record);
                totalProcessedBytes += 256; // Estimate bytes per record
            }

            // Memory management - clear processed chunks periodically
            if (processedRecords.size() > 10000) {
                logger.debug(logPrefix + "clearing processed records to manage memory");
                processedRecords.clear();
                System.gc(); // Suggest garbage collection
            }
        }

        response.setProcessedRecordCount(numRecords);
        response.setTotalProcessedBytes(totalProcessedBytes);
        response.setMemoryOptimized(true);
        response.setHttp2Optimized(true);
        response.setOptimizationDetails("HTTP/2 streaming enabled for 50MB+ dataset with chunked processing");

        // Provide summary instead of full dataset to manage memory
        response.setResultSummary("Processed " + numRecords + " records using HTTP/2 streaming optimization. " +
                                "Total data processed: " + (totalProcessedBytes / 1024 / 1024) + "MB. " +
                                "Memory efficient chunked processing applied.");

        return response;
    }

    /**
     * Process medium datasets (10-50MB) using HTTP/2 multiplexing.
     */
    private BigDataH2Response processMediumDatasetWithMultiplexing(BigDataH2Request request, String uuid) {
        String logPrefix = "BigDataH2Service.processMediumDatasetWithMultiplexing() , uuid: " + uuid + " , ";
        logger.info(logPrefix + "using HTTP/2 multiplexing for medium dataset: " +
                   (request.getDatasetSize() / 1024 / 1024) + "MB");

        BigDataH2Response response = new BigDataH2Response();

        int numRecords = request.getDatasetSize() / 512; // Medium density
        List<DataRecord> processedRecords = new ArrayList<>();

        // Simulate concurrent processing streams
        for (int i = 0; i < numRecords; i++) {
            DataRecord record = new DataRecord();
            record.setRecordId("H2_RECORD_" + String.format("%06d", i));
            record.setIdentifier("dataset_" + request.getDatasetId() + "_item_" + i);
            record.setCategory("analytics_medium");
            record.setValue(i * 2.3 + Math.random() * 50);
            record.setProcessedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            processedRecords.add(record);
        }

        response.setProcessedRecords(processedRecords);
        response.setProcessedRecordCount(numRecords);
        response.setTotalProcessedBytes(request.getDatasetSize());
        response.setMemoryOptimized(true);
        response.setHttp2Optimized(true);
        response.setOptimizationDetails("HTTP/2 connection multiplexing for concurrent processing");
        response.setResultSummary("Processed " + numRecords + " records with HTTP/2 multiplexing optimization");

        return response;
    }

    /**
     * Process standard datasets (<10MB) with regular HTTP/2 features.
     */
    private BigDataH2Response processStandardDataset(BigDataH2Request request, String uuid) {
        String logPrefix = "BigDataH2Service.processStandardDataset() , uuid: " + uuid + " , ";
        logger.info(logPrefix + "processing standard dataset: " +
                   (request.getDatasetSize() / 1024) + "KB");

        BigDataH2Response response = new BigDataH2Response();

        int numRecords = Math.min(request.getDatasetSize() / 256, 1000); // Smaller datasets
        List<DataRecord> processedRecords = new ArrayList<>();

        for (int i = 0; i < numRecords; i++) {
            DataRecord record = new DataRecord();
            record.setRecordId("STD_RECORD_" + String.format("%04d", i));
            record.setIdentifier("dataset_" + request.getDatasetId() + "_std_" + i);
            record.setCategory("standard_analytics");
            record.setValue(i * 1.2 + Math.random() * 25);
            record.setProcessedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            processedRecords.add(record);
        }

        response.setProcessedRecords(processedRecords);
        response.setProcessedRecordCount(numRecords);
        response.setTotalProcessedBytes(request.getDatasetSize());
        response.setMemoryOptimized(false);
        response.setHttp2Optimized(true);
        response.setOptimizationDetails("Standard HTTP/2 processing for small datasets");
        response.setResultSummary("Processed " + numRecords + " records with standard HTTP/2 transport");

        return response;
    }

    /**
     * Data record structure for big data processing results.
     */
    public static class DataRecord {
        private String recordId;
        private String identifier;
        private String category;
        private double value;
        private String processedAt;

        // Getters and setters
        public String getRecordId() { return recordId; }
        public void setRecordId(String recordId) { this.recordId = recordId; }

        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }

        public String getProcessedAt() { return processedAt; }
        public void setProcessedAt(String processedAt) { this.processedAt = processedAt; }
    }
}