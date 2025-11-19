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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for LargeJSONPayloadGenerator.
 *
 * Tests cover:
 * - Accurate size generation for performance testing
 * - Valid JSON structure generation
 * - Memory efficiency during generation
 * - Big data structure validation
 */
public class LargeJSONPayloadGeneratorTest {

    private static final double SIZE_TOLERANCE = 0.05; // 5% tolerance

    @Test
    public void testSimpleLargeJSONGeneration() {
        // Test generation of simple large JSON
        int targetSize = 1024 * 1024; // 1MB
        String json = LargeJSONPayloadGenerator.generateSimpleLargeJSON(targetSize);

        assertNotNull("Generated JSON should not be null", json);
        assertTrue("Generated JSON size should be within tolerance",
                  LargeJSONPayloadGenerator.validateSize(json, targetSize, SIZE_TOLERANCE));

        // Verify JSON structure
        assertTrue("JSON should start with object", json.startsWith("{"));
        assertTrue("JSON should end with object", json.endsWith("}"));
        assertTrue("JSON should contain data array", json.contains("\"data\""));
    }

    @Test
    public void testBigDataStructureGeneration() {
        // Test generation of realistic big data structure JSON
        int targetSize = 10 * 1024 * 1024; // 10MB
        String json = LargeJSONPayloadGenerator.generateBigDataStructure(targetSize);

        assertNotNull("Big data structure JSON should not be null", json);
        assertTrue("Big data JSON size should be within tolerance",
                  LargeJSONPayloadGenerator.validateSize(json, targetSize, SIZE_TOLERANCE));

        // Verify big data structure
        assertTrue("Should contain datasetId", json.contains("\"datasetId\""));
        assertTrue("Should contain records array", json.contains("\"records\""));
        assertTrue("Should contain metrics", json.contains("\"metrics\""));
        assertTrue("Should contain performance history", json.contains("\"performanceHistory\""));
        assertTrue("Should contain calculations", json.contains("\"calculations\""));

        // Verify big data record elements
        assertTrue("Should contain record identifiers", json.contains("\"identifier\""));
        assertTrue("Should contain record types", json.contains("\"recordType\""));
        assertTrue("Should contain values", json.contains("\"value\""));
        assertTrue("Should contain attributes data", json.contains("\"attributes\""));
        assertTrue("Should contain metrics data", json.contains("\"metrics\""));
        assertTrue("Should contain variability data", json.contains("\"variability\""));
    }

    @Test
    public void testStreamingJSONGeneration() {
        // Test generation of streaming-optimized JSON
        int targetSize = 5 * 1024 * 1024; // 5MB
        String json = LargeJSONPayloadGenerator.generateStreamingJSON(targetSize);

        assertNotNull("Streaming JSON should not be null", json);
        assertTrue("Streaming JSON size should be within tolerance",
                  LargeJSONPayloadGenerator.validateSize(json, targetSize, SIZE_TOLERANCE));

        // Verify streaming structure
        assertTrue("Should indicate streaming data", json.contains("\"streamingData\": true"));
        assertTrue("Should contain data chunks", json.contains("\"dataChunks\""));
        assertTrue("Should contain chunk IDs", json.contains("\"chunkId\""));
        assertTrue("Should contain sequence numbers", json.contains("\"sequenceNumber\""));
        assertTrue("Should contain checksums", json.contains("\"checksum\""));
    }

    @Test
    public void testLargePayloadGeneration50MB() {
        // Core business requirement: Test 50MB payload generation
        int targetSize = 50 * 1024 * 1024; // 50MB

        long startTime = System.currentTimeMillis();
        String json = LargeJSONPayloadGenerator.generateBigDataStructure(targetSize);
        long generationTime = System.currentTimeMillis() - startTime;

        assertNotNull("50MB JSON should be generated", json);
        assertTrue("50MB JSON should be within size tolerance",
                  LargeJSONPayloadGenerator.validateSize(json, targetSize, SIZE_TOLERANCE));

        // Performance validation
        assertTrue("50MB generation should complete within reasonable time (30s)",
                  generationTime < 30000);

        System.out.println("50MB JSON generation completed in " + generationTime + "ms");
        System.out.println("Actual size: " +
                          (LargeJSONPayloadGenerator.getActualByteSize(json) / 1024 / 1024) + "MB");
    }

    @Test
    public void testMemoryEfficiencyDuringGeneration() {
        // Test memory efficiency during large payload generation
        Runtime runtime = Runtime.getRuntime();
        System.gc(); // Force garbage collection
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // Generate multiple large payloads to test memory efficiency
        for (int i = 0; i < 3; i++) {
            String json = LargeJSONPayloadGenerator.generateBigDataStructure(10 * 1024 * 1024);
            assertNotNull("JSON " + i + " should be generated", json);

            // Allow some processing time
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.gc(); // Force garbage collection
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        // Memory increase should be reasonable (less than 100MB for 30MB total generated)
        assertTrue("Memory increase should be reasonable during generation: " +
                  (memoryIncrease / 1024 / 1024) + "MB",
                  memoryIncrease < 100 * 1024 * 1024);

        System.out.println("Memory increase during generation: " +
                          (memoryIncrease / 1024 / 1024) + "MB");
    }

    @Test
    public void testJSONValidityAndStructure() {
        // Test that generated JSON has valid structure
        String json = LargeJSONPayloadGenerator.generateBigDataStructure(1024 * 1024);

        // Basic JSON validity checks
        assertTrue("JSON should start with {", json.startsWith("{"));
        assertTrue("JSON should end with }", json.endsWith("}"));

        // Count opening and closing braces (should be balanced)
        long openBraces = json.chars().filter(ch -> ch == '{').count();
        long closeBraces = json.chars().filter(ch -> ch == '}').count();
        assertEquals("Opening and closing braces should be balanced", openBraces, closeBraces);

        // Count opening and closing brackets (should be balanced)
        long openBrackets = json.chars().filter(ch -> ch == '[').count();
        long closeBrackets = json.chars().filter(ch -> ch == ']').count();
        assertEquals("Opening and closing brackets should be balanced", openBrackets, closeBrackets);

        // Verify no unescaped quotes in string values (basic check)
        assertFalse("Should not contain unescaped quotes", json.contains("\"\""));
    }

    @Test
    public void testSizeAccuracy() {
        // Test size accuracy across different target sizes
        int[] testSizes = {
            1024,           // 1KB
            100 * 1024,     // 100KB
            1024 * 1024,    // 1MB
            5 * 1024 * 1024 // 5MB
        };

        for (int targetSize : testSizes) {
            String json = LargeJSONPayloadGenerator.generateSimpleLargeJSON(targetSize);
            int actualSize = LargeJSONPayloadGenerator.getActualByteSize(json);

            double tolerance = targetSize < 1024 * 1024 ? 0.1 : 0.05; // Higher tolerance for smaller sizes
            assertTrue("Size should be accurate for " + targetSize + " bytes. " +
                      "Actual: " + actualSize + ", Target: " + targetSize,
                      LargeJSONPayloadGenerator.validateSize(json, targetSize, tolerance));
        }
    }

    @Test
    public void testDifferentGenerationMethods() {
        // Test that different generation methods produce different structures
        int targetSize = 1024 * 1024; // 1MB

        String simpleJSON = LargeJSONPayloadGenerator.generateSimpleLargeJSON(targetSize);
        String financialJSON = LargeJSONPayloadGenerator.generateBigDataStructure(targetSize);
        String streamingJSON = LargeJSONPayloadGenerator.generateStreamingJSON(targetSize);

        // All should be valid and appropriately sized
        assertNotNull("Simple JSON should not be null", simpleJSON);
        assertNotNull("Big data JSON should not be null", financialJSON);
        assertNotNull("Streaming JSON should not be null", streamingJSON);

        // Should have different structures
        assertNotEquals("Simple and financial JSON should be different", simpleJSON, financialJSON);
        assertNotEquals("Financial and streaming JSON should be different", financialJSON, streamingJSON);
        assertNotEquals("Simple and streaming JSON should be different", simpleJSON, streamingJSON);

        // Should contain different key elements
        assertTrue("Simple JSON should contain data array", simpleJSON.contains("\"data\""));
        assertTrue("Big data JSON should contain dataset structure", financialJSON.contains("\"datasetId\""));
        assertTrue("Streaming JSON should contain streaming indicators", streamingJSON.contains("\"streamingData\""));
    }

    @Test
    public void testReproducibility() {
        // Test that generation is reproducible (using fixed random seed)
        int targetSize = 1024 * 1024; // 1MB

        String json1 = LargeJSONPayloadGenerator.generateBigDataStructure(targetSize);
        String json2 = LargeJSONPayloadGenerator.generateBigDataStructure(targetSize);

        // Due to timestamps, they won't be identical, but structure should be consistent
        int size1 = LargeJSONPayloadGenerator.getActualByteSize(json1);
        int size2 = LargeJSONPayloadGenerator.getActualByteSize(json2);

        // Sizes should be very close (within 1%)
        double sizeDifference = Math.abs(size1 - size2) / (double) Math.max(size1, size2);
        assertTrue("Generated sizes should be consistent: " + size1 + " vs " + size2,
                  sizeDifference < 0.01);
    }
}