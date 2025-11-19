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

package org.apache.axis2.transport.h2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Large JSON payload generator for testing HTTP/2 transport with big data structures.
 *
 * This class generates realistic big data structures for enterprise applications:
 * - 50MB+ JSON payloads for performance testing
 * - Realistic data structures (records, calculations, metrics)
 * - Memory-efficient generation for 2GB heap constraints
 * - Streaming-friendly JSON structure for HTTP/2 flow control
 */
public class LargeJSONPayloadGenerator {

    private static final Random random = new Random(12345); // Fixed seed for reproducible tests
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Big data record templates
    private static final String[] RECORD_TYPES = {
        "TYPE_A", "TYPE_B", "TYPE_C", "TYPE_D", "TYPE_E", "TYPE_F"
    };

    private static final String[] CATEGORIES = {
        "TECHNOLOGY", "HEALTHCARE", "SERVICES", "ENERGY", "CONSUMER",
        "INDUSTRIAL", "UTILITIES", "MATERIALS", "COMMUNICATIONS", "LOGISTICS"
    };

    private static final String[] CURRENCIES = {
        "USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY"
    };

    /**
     * Generate large JSON payload simulating big data structures
     *
     * @param targetSizeBytes Target size in bytes
     * @return JSON string of approximately the target size
     */
    public static String generateBigDataStructure(int targetSizeBytes) {
        StringBuilder json = new StringBuilder();

        // Start data structure
        json.append("{\n");
        json.append("  \"datasetId\": \"bigdata_dataset_").append(System.currentTimeMillis()).append("\",\n");
        json.append("  \"timestamp\": \"").append(LocalDateTime.now().format(ISO_FORMATTER)).append("\",\n");
        json.append("  \"totalValue\": 0.0,\n");
        json.append("  \"currency\": \"USD\",\n");
        json.append("  \"profile\": \"STANDARD\",\n");
        json.append("  \"records\": [\n");

        // Calculate number of records needed to reach target size
        int estimatedRecordSize = 800; // Approximate bytes per record
        int numRecords = Math.max(100, (targetSizeBytes - 2000) / estimatedRecordSize);

        double totalValue = 0.0;

        // Generate records
        for (int i = 0; i < numRecords; i++) {
            if (i > 0) json.append(",\n");

            String record = generateRecord(i);
            json.append(record);

            // Track total value for summary
            totalValue += 15000 + (random.nextDouble() * 50000);
        }

        json.append("\n  ],\n");

        // Add dataset metrics
        json.append("  \"metrics\": {\n");
        json.append("    \"totalRecords\": ").append(numRecords).append(",\n");
        json.append("    \"totalValue\": ").append(String.format("%.2f", totalValue)).append(",\n");
        json.append("    \"distributionScore\": ").append(String.format("%.2f", random.nextDouble() * 100)).append(",\n");
        json.append("    \"qualityScore\": ").append(String.format("%.2f", random.nextDouble() * 10)).append(",\n");
        json.append("    \"expectedGrowth\": ").append(String.format("%.4f", random.nextDouble() * 0.15)).append(",\n");
        json.append("    \"variability\": ").append(String.format("%.4f", random.nextDouble() * 0.30)).append(",\n");
        json.append("    \"efficiency\": ").append(String.format("%.4f", random.nextDouble() * 2.0)).append("\n");
        json.append("  },\n");

        // Add performance history
        json.append("  \"performanceHistory\": [\n");
        for (int i = 0; i < 252; i++) { // Daily data for one year
            if (i > 0) json.append(",\n");
            json.append("    {\n");
            json.append("      \"date\": \"").append(LocalDateTime.now().minusDays(252 - i).format(ISO_FORMATTER)).append("\",\n");
            json.append("      \"value\": ").append(String.format("%.2f", totalValue * (0.8 + random.nextDouble() * 0.4))).append(",\n");
            json.append("      \"return\": ").append(String.format("%.6f", (random.nextDouble() - 0.5) * 0.1)).append("\n");
            json.append("    }");
        }
        json.append("\n  ],\n");

        // Add calculation details
        json.append("  \"calculations\": {\n");
        json.append("    \"lastCalculationTime\": \"").append(LocalDateTime.now().format(ISO_FORMATTER)).append("\",\n");
        json.append("    \"processingEngine\": \"bigdata-engine-v2.1\",\n");
        json.append("    \"processingTimeMs\": ").append(random.nextInt(5000)).append(",\n");
        json.append("    \"memoryUsageMB\": ").append(random.nextInt(512)).append(",\n");
        json.append("    \"cacheHitRatio\": ").append(String.format("%.4f", random.nextDouble())).append("\n");
        json.append("  }\n");

        json.append("}");

        // Pad to exact target size if needed
        String result = json.toString();
        if (result.length() < targetSizeBytes) {
            int paddingNeeded = targetSizeBytes - result.length() - 30; // Reserve space for padding structure
            if (paddingNeeded > 0) {
                String padding = " ".repeat(paddingNeeded);
                result = result.substring(0, result.length() - 1) + ",\n  \"padding\": \"" + padding + "\"\n}";
            }
        }

        // Ensure we don't truncate the JSON in the middle - if we need to truncate, do it safely
        if (result.length() > targetSizeBytes) {
            // Truncate but ensure it ends with a valid JSON structure
            result = result.substring(0, targetSizeBytes - 1) + "}";
        }

        return result;
    }

    /**
     * Generate streaming-optimized JSON for flow control testing
     *
     * @param targetSizeBytes Target size in bytes
     * @return JSON string structured for efficient streaming
     */
    public static String generateStreamingJSON(int targetSizeBytes) {
        StringBuilder json = new StringBuilder();

        json.append("{\n");
        json.append("  \"streamingData\": true,\n");
        json.append("  \"timestamp\": \"").append(LocalDateTime.now().format(ISO_FORMATTER)).append("\",\n");
        json.append("  \"dataChunks\": [\n");

        // Generate data in 1KB chunks for streaming efficiency
        int chunkSize = 1024;
        int numChunks = (targetSizeBytes - 200) / chunkSize;

        for (int i = 0; i < numChunks; i++) {
            if (i > 0) json.append(",\n");

            json.append("    {\n");
            json.append("      \"chunkId\": ").append(i).append(",\n");
            json.append("      \"sequenceNumber\": ").append(i).append(",\n");
            json.append("      \"timestamp\": \"").append(LocalDateTime.now().format(ISO_FORMATTER)).append("\",\n");
            json.append("      \"data\": \"");

            // Fill chunk with base64-like data
            int dataSize = chunkSize - 200; // Account for JSON structure overhead
            for (int j = 0; j < dataSize; j++) {
                json.append((char) ('A' + (j % 26)));
            }

            json.append("\",\n");
            json.append("      \"checksum\": \"").append(Integer.toHexString(i * 12345)).append("\"\n");
            json.append("    }");
        }

        json.append("\n  ],\n");
        json.append("  \"totalChunks\": ").append(numChunks).append(",\n");
        json.append("  \"totalSize\": ").append(targetSizeBytes).append("\n");
        json.append("}");

        return json.toString();
    }

    /**
     * Generate simple large JSON with repeated structures
     *
     * @param targetSizeBytes Target size in bytes
     * @return Simple JSON string for basic testing
     */
    public static String generateSimpleLargeJSON(int targetSizeBytes) {
        StringBuilder json = new StringBuilder();

        json.append("{\"data\": [");

        // Simple array of objects
        int objectSize = 100; // Approximate size per object
        int numObjects = (targetSizeBytes - 50) / objectSize;

        for (int i = 0; i < numObjects; i++) {
            if (i > 0) json.append(",");
            json.append("{\"id\": ").append(i)
                .append(", \"value\": \"data_").append(i)
                .append("\", \"timestamp\": ").append(System.currentTimeMillis() + i)
                .append(", \"flag\": ").append(i % 2 == 0 ? "true" : "false")
                .append("}");
        }

        json.append("]}");

        // Pad to exact size
        String result = json.toString();
        if (result.length() < targetSizeBytes) {
            int paddingNeeded = targetSizeBytes - result.length() - 20;
            result = result.substring(0, result.length() - 1) + ", \"padding\": \"" +
                    "x".repeat(Math.max(0, paddingNeeded)) + "\"}";
        }

        return result.substring(0, Math.min(targetSizeBytes, result.length()));
    }

    /**
     * Generate individual record data
     */
    private static String generateRecord(int index) {
        StringBuilder record = new StringBuilder();

        String recordType = RECORD_TYPES[index % RECORD_TYPES.length];
        String category = CATEGORIES[index % CATEGORIES.length];
        String currency = CURRENCIES[index % CURRENCIES.length];

        double quantity = 100 + random.nextDouble() * 10000;
        double price = 10 + random.nextDouble() * 1000;
        double marketValue = quantity * price;

        record.append("    {\n");
        record.append("      \"recordId\": \"RECORD_").append(String.format("%06d", index)).append("\",\n");
        record.append("      \"identifier\": \"").append(generateIdentifier(index)).append("\",\n");
        record.append("      \"recordType\": \"").append(recordType).append("\",\n");
        record.append("      \"category\": \"").append(category).append("\",\n");
        record.append("      \"currency\": \"").append(currency).append("\",\n");
        record.append("      \"quantity\": ").append(String.format("%.4f", quantity)).append(",\n");
        record.append("      \"price\": ").append(String.format("%.2f", price)).append(",\n");
        record.append("      \"value\": ").append(String.format("%.2f", marketValue)).append(",\n");
        record.append("      \"weight\": ").append(String.format("%.4f", random.nextDouble() * 0.1)).append(",\n");
        record.append("      \"lastUpdated\": \"").append(LocalDateTime.now().format(ISO_FORMATTER)).append("\",\n");
        record.append("      \"attributes\": {\n");
        record.append("        \"factor\": ").append(String.format("%.4f", random.nextDouble() * 2.0)).append(",\n");
        record.append("        \"variability\": ").append(String.format("%.4f", random.nextDouble() * 0.5)).append(",\n");
        record.append("        \"dailyVar\": ").append(String.format("%.2f", random.nextDouble() * 1000)).append("\n");
        record.append("      },\n");
        record.append("      \"metrics\": {\n");
        record.append("        \"ytdGrowth\": ").append(String.format("%.4f", (random.nextDouble() - 0.5) * 0.3)).append(",\n");
        record.append("        \"monthlyGrowth\": ").append(String.format("%.4f", (random.nextDouble() - 0.5) * 0.1)).append(",\n");
        record.append("        \"efficiency\": ").append(String.format("%.4f", random.nextDouble() * 3.0)).append("\n");
        record.append("      }\n");
        record.append("    }");

        return record.toString();
    }

    /**
     * Generate record identifier
     */
    private static String generateIdentifier(int index) {
        String[] prefixes = {"TECH", "HLTH", "SERV", "ENRG", "CONS", "INDU", "UTIL", "MATL"};
        String prefix = prefixes[index % prefixes.length];
        return prefix + String.format("%03d", index % 1000);
    }

    /**
     * Calculate actual byte size of a string (UTF-8 encoding)
     */
    public static int getActualByteSize(String jsonString) {
        return jsonString.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
    }

    /**
     * Validate generated JSON size is within tolerance
     */
    public static boolean validateSize(String jsonString, int targetSize, double tolerance) {
        int actualSize = getActualByteSize(jsonString);
        double difference = Math.abs(actualSize - targetSize) / (double) targetSize;
        return difference <= tolerance;
    }
}