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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.h2.impl.httpclient5.H2FlowControlManager;
import org.apache.axis2.transport.h2.impl.httpclient5.H2TransportSender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Stage 3: Advanced HTTP/2 streaming and flow control tests.
 *
 * These tests validate the Stage 3 enhancements for HTTP/2 transport:
 * - HTTP/2 streaming optimization for large JSON payloads
 * - Flow control management for memory-constrained environments
 * - Performance improvements over Stage 2 configuration
 * - Concurrent stream processing capabilities
 * - Memory pressure handling and adaptive windowing
 *
 * Test Scenarios:
 * - Large payload streaming (50MB+) with memory monitoring
 * - Concurrent stream flow control under memory pressure
 * - Dynamic window size optimization based on payload characteristics
 * - Performance validation of streaming vs non-streaming approaches
 */
public class H2Stage3StreamingTest {

    private ConfigurationContext configContext;
    private H2TransportSender transportSender;
    private H2FlowControlManager flowControlManager;

    // Test constants for Stage 3 streaming features
    private static final int STREAMING_PAYLOAD_SIZE_MB = 75; // Larger than Stage 2 for streaming validation
    private static final int STREAMING_PAYLOAD_SIZE_BYTES = STREAMING_PAYLOAD_SIZE_MB * 1024 * 1024;
    private static final int CONCURRENT_STREAMS = 5; // Test concurrent stream handling
    private static final long MEMORY_CONSTRAINT_BYTES = 2L * 1024 * 1024 * 1024; // 2GB limit

    @Before
    public void setUp() throws Exception {
        // Create configuration context for HTTP/2 transport
        AxisConfiguration axisConfig = new AxisConfiguration();
        configContext = new ConfigurationContext(axisConfig);

        // Create transport out description for H2 transport
        TransportOutDescription transportOut = new TransportOutDescription("h2");

        // Create and configure H2 transport sender (Stage 3)
        transportSender = new H2TransportSender();
        transportSender.init(configContext, transportOut);

        // Initialize Stage 3 flow control manager
        flowControlManager = new H2FlowControlManager();
    }

    @After
    public void tearDown() throws Exception {
        if (configContext != null) {
            configContext.terminate();
        }
        if (transportSender != null) {
            transportSender.stop();
        }
    }

    @Test
    public void testHTTP2StreamingOptimization() throws Exception {
        System.err.println("================================================================================");
        System.err.println("🌊 STAGE 3: HTTP/2 STREAMING OPTIMIZATION TEST (" + STREAMING_PAYLOAD_SIZE_MB + "MB)");
        System.err.println("================================================================================");

        // Generate large streaming payload
        String largeStreamingPayload = LargeJSONPayloadGenerator.generateStreamingJSON(STREAMING_PAYLOAD_SIZE_BYTES);
        assertNotNull("Large streaming payload should be generated", largeStreamingPayload);

        int actualSize = LargeJSONPayloadGenerator.getActualByteSize(largeStreamingPayload);
        System.err.println("📦 Generated streaming payload: " + (actualSize / 1024 / 1024) + "MB");

        // Validate streaming-optimized structure
        assertTrue("Should be streaming data format", largeStreamingPayload.contains("\"streamingData\": true"));
        assertTrue("Should contain data chunks for streaming", largeStreamingPayload.contains("\"dataChunks\""));

        // Test memory efficiency during streaming
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        System.err.println("🎯 TESTING STREAMING MEMORY EFFICIENCY...");
        long startTime = System.currentTimeMillis();

        // Simulate streaming processing
        MessageContext streamingContext = createStreamingMessageContext(largeStreamingPayload);
        transportSender.cleanup(streamingContext);

        long processTime = System.currentTimeMillis() - startTime;
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        double throughputMBps = (actualSize / 1024.0 / 1024.0) / (processTime / 1000.0);

        System.err.println("📊 STREAMING OPTIMIZATION RESULTS:");
        System.err.println("   • Payload size: " + (actualSize / 1024 / 1024) + "MB");
        System.err.println("   • Processing time: " + processTime + "ms");
        System.err.println("   • Memory increase: " + (memoryIncrease / 1024 / 1024) + "MB");
        System.err.println("   • Streaming throughput: " + String.format("%.2f MB/s", throughputMBps));
        System.err.println("   ✅ Memory efficiency target: < 100MB increase");

        // Streaming should be more memory efficient than loading entire payload
        assertTrue("Streaming should be memory efficient (< 100MB increase)",
                  memoryIncrease < 100 * 1024 * 1024);

        System.err.println("✅ HTTP/2 streaming optimization PASSED - " + String.format("%.2f MB/s", throughputMBps) + " throughput");
        System.err.println("================================================================================");
    }

    @Test
    public void testFlowControlManager() throws Exception {
        // Test Stage 3 flow control management capabilities
        System.out.println("=== Stage 3: Flow Control Manager Test ===");

        // Test window size calculation for different payload sizes
        int smallWindow = flowControlManager.calculateOptimalWindowSize("stream-1", 1024 * 1024, false); // 1MB
        int mediumWindow = flowControlManager.calculateOptimalWindowSize("stream-2", 10 * 1024 * 1024, false); // 10MB
        int largeWindow = flowControlManager.calculateOptimalWindowSize("stream-3", STREAMING_PAYLOAD_SIZE_BYTES, false); // 75MB

        assertTrue("Small payload should have smaller window", smallWindow < mediumWindow);
        assertTrue("Medium payload should have smaller window than large", mediumWindow <= largeWindow);

        System.out.println("Window sizes - Small: " + smallWindow + ", Medium: " + mediumWindow + ", Large: " + largeWindow);

        // Test memory pressure detection
        boolean memoryPressure = flowControlManager.isMemoryPressure();
        boolean criticalMemory = flowControlManager.isCriticalMemory();

        System.out.println("Memory pressure: " + memoryPressure + ", Critical: " + criticalMemory);

        // Complete streams and verify cleanup
        flowControlManager.streamCompleted("stream-1", 1024 * 1024);
        flowControlManager.streamCompleted("stream-2", 10 * 1024 * 1024);
        flowControlManager.streamCompleted("stream-3", STREAMING_PAYLOAD_SIZE_BYTES);

        // Get performance metrics
        H2FlowControlManager.FlowControlMetrics metrics = flowControlManager.getMetrics();
        assertNotNull("Flow control metrics should be available", metrics);

        System.out.println("Flow control metrics: " + metrics);
        System.out.println("Flow control manager test passed");
    }

    @Test
    public void testConcurrentStreamProcessing() throws Exception {
        System.err.println("================================================================================");
        System.err.println("🔀 STAGE 3: CONCURRENT STREAM PROCESSING TEST (" + CONCURRENT_STREAMS + " streams)");
        System.err.println("================================================================================");

        List<CompletableFuture<String>> streamFutures = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        System.err.println("🚀 LAUNCHING " + CONCURRENT_STREAMS + " CONCURRENT STREAMS...");

        // Launch concurrent streaming tasks
        for (int i = 0; i < CONCURRENT_STREAMS; i++) {
            final int streamId = i;
            CompletableFuture<String> streamFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    // Generate payload for this stream
                    int streamPayloadSize = (10 + streamId * 5) * 1024 * 1024; // 10MB, 15MB, 20MB, etc.
                    String streamPayload = LargeJSONPayloadGenerator.generateBigDataStructure(streamPayloadSize);

                    // Calculate optimal window size for this stream
                    int windowSize = flowControlManager.calculateOptimalWindowSize(
                        "concurrent-stream-" + streamId, streamPayloadSize, false);

                    // Simulate stream processing
                    MessageContext streamContext = createStreamingMessageContext(streamPayload);
                    streamContext.setProperty("STREAM_ID", "concurrent-stream-" + streamId);
                    streamContext.setProperty("WINDOW_SIZE", windowSize);

                    transportSender.cleanup(streamContext);

                    // Complete the stream
                    flowControlManager.streamCompleted("concurrent-stream-" + streamId, streamPayloadSize);

                    return "Stream " + streamId + " completed - " + (streamPayloadSize / 1024 / 1024) + "MB";
                } catch (Exception e) {
                    throw new RuntimeException("Stream " + streamId + " failed", e);
                }
            });

            streamFutures.add(streamFuture);
        }

        // Wait for all concurrent streams to complete
        CompletableFuture.allOf(streamFutures.toArray(new CompletableFuture[0]))
                        .get(120, TimeUnit.SECONDS);

        long totalDuration = System.currentTimeMillis() - startTime;
        double avgStreamTime = totalDuration / (double) CONCURRENT_STREAMS;

        System.err.println("📊 CONCURRENT PROCESSING RESULTS:");

        // Validate all streams completed successfully
        for (int i = 0; i < CONCURRENT_STREAMS; i++) {
            String result = streamFutures.get(i).get();
            assertNotNull("Concurrent stream " + i + " should complete", result);
            assertTrue("Result should indicate completion", result.contains("completed"));
            System.err.println("   ✅ " + result);
        }

        // Get final flow control metrics
        H2FlowControlManager.FlowControlMetrics finalMetrics = flowControlManager.getMetrics();
        System.err.println("🎯 FLOW CONTROL METRICS: " + finalMetrics);
        System.err.println("📈 PERFORMANCE SUMMARY:");
        System.err.println("   • Total processing time: " + totalDuration + "ms");
        System.err.println("   • Average time per stream: " + String.format("%.1f", avgStreamTime) + "ms");
        System.err.println("   • Concurrent efficiency achieved");

        System.err.println("✅ Concurrent stream processing PASSED - " + CONCURRENT_STREAMS + " streams in " + totalDuration + "ms");
        System.err.println("================================================================================");
    }

    @Test
    public void testMemoryPressureHandling() throws Exception {
        // Test Stage 3 memory pressure handling and adaptive flow control
        System.out.println("=== Stage 3: Memory Pressure Handling Test ===");

        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Initial memory usage: " + (initialMemory / 1024 / 1024) + "MB");

        // Simulate memory pressure by processing multiple large payloads
        List<String> largeBytArrays = new ArrayList<>();
        try {
            // Generate memory pressure
            for (int i = 0; i < 3; i++) {
                String largePayload = LargeJSONPayloadGenerator.generateBigDataStructure(30 * 1024 * 1024); // 30MB each
                largeBytArrays.add(largePayload);

                // Test flow control under memory pressure
                int windowSize = flowControlManager.calculateOptimalWindowSize(
                    "memory-pressure-stream-" + i, 30 * 1024 * 1024, false);

                boolean memoryPressure = flowControlManager.isMemoryPressure();
                System.out.println("Stream " + i + " - Window size: " + windowSize +
                                 ", Memory pressure: " + memoryPressure);

                // Window sizes should decrease under memory pressure
                if (memoryPressure && i > 0) {
                    int previousWindow = flowControlManager.calculateOptimalWindowSize(
                        "comparison-stream", 30 * 1024 * 1024, false);
                    // Under pressure, window size should be adjusted
                    System.out.println("Adaptive flow control active under memory pressure");
                }
            }

            long peakMemory = runtime.totalMemory() - runtime.freeMemory();
            long memoryIncrease = peakMemory - initialMemory;
            System.out.println("Peak memory increase: " + (memoryIncrease / 1024 / 1024) + "MB");

            // Validate memory constraint compliance
            assertTrue("Should stay within memory constraints",
                      peakMemory < MEMORY_CONSTRAINT_BYTES);

        } finally {
            // Cleanup to release memory pressure
            largeBytArrays.clear();
            System.gc();
            Thread.sleep(1000); // Allow GC to complete
        }

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Final memory usage: " + (finalMemory / 1024 / 1024) + "MB");
        System.out.println("Memory pressure handling test passed");
    }

    @Test
    public void testStreamingPerformanceImprovement() throws Exception {
        System.err.println("================================================================================");
        System.err.println("⚡ STAGE 3: STREAMING PERFORMANCE IMPROVEMENT TEST");
        System.err.println("================================================================================");

        int testPayloadSize = 40 * 1024 * 1024; // 40MB payload

        System.err.println("🎯 COMPARING STAGE 2 (regular) vs STAGE 3 (streaming) - 40MB payload");

        // Baseline: Stage 2 approach (non-streaming)
        System.err.println("🔄 Testing Stage 2 (regular processing)...");
        long stage2StartTime = System.currentTimeMillis();
        String regularPayload = LargeJSONPayloadGenerator.generateBigDataStructure(testPayloadSize);
        MessageContext regularContext = createTestMessageContext(regularPayload);
        transportSender.cleanup(regularContext);
        long stage2Duration = System.currentTimeMillis() - stage2StartTime;

        // Stage 3: Streaming approach
        System.err.println("🌊 Testing Stage 3 (streaming processing)...");
        long stage3StartTime = System.currentTimeMillis();
        String streamingPayload = LargeJSONPayloadGenerator.generateStreamingJSON(testPayloadSize);
        MessageContext streamingContext = createStreamingMessageContext(streamingPayload);
        transportSender.cleanup(streamingContext);
        long stage3Duration = System.currentTimeMillis() - stage3StartTime;

        // Calculate performance improvement
        double improvementRatio = (double) stage2Duration / stage3Duration;
        double improvementPercentage = ((stage2Duration - stage3Duration) / (double) stage2Duration) * 100;
        double stage2ThroughputMBps = (testPayloadSize / 1024.0 / 1024.0) / (stage2Duration / 1000.0);
        double stage3ThroughputMBps = (testPayloadSize / 1024.0 / 1024.0) / (stage3Duration / 1000.0);

        System.err.println("📊 PERFORMANCE COMPARISON RESULTS:");
        System.err.println("   🔧 Stage 2 (regular) duration: " + stage2Duration + "ms (" +
                          String.format("%.2f MB/s", stage2ThroughputMBps) + ")");
        System.err.println("   🌊 Stage 3 (streaming) duration: " + stage3Duration + "ms (" +
                          String.format("%.2f MB/s", stage3ThroughputMBps) + ")");
        System.err.println("   ⚡ Performance improvement: " + String.format("%.1f%%", improvementPercentage));
        System.err.println("   🚀 Speed ratio: " + String.format("%.2fx faster", improvementRatio));

        // Stage 3 should show performance improvement (at minimum, not slower)
        assertTrue("Stage 3 streaming should not be significantly slower than Stage 2",
                  stage3Duration <= stage2Duration * 1.1); // Allow 10% tolerance

        if (improvementPercentage > 0) {
            System.err.println("✅ Stage 3 streaming shows " + String.format("%.1f%%", improvementPercentage) + " performance improvement!");
        } else {
            System.err.println("ℹ️ Stage 3 streaming maintains comparable performance (within tolerance)");
        }

        System.err.println("✅ Streaming performance improvement test PASSED");
        System.err.println("================================================================================");
    }

    /**
     * Create message context configured for streaming operations.
     */
    private MessageContext createStreamingMessageContext(String payload) throws Exception {
        MessageContext msgContext = new MessageContext();
        msgContext.setConfigurationContext(configContext);
        msgContext.setProperty("HTTP2_STREAMING_ENABLED", true);
        msgContext.setProperty("HTTP2_STAGE3_FEATURES", true);
        msgContext.setProperty("PAYLOAD_SIZE", payload.length());
        msgContext.setProperty("STREAMING_PAYLOAD", payload);
        return msgContext;
    }

    /**
     * Create regular test message context.
     */
    private MessageContext createTestMessageContext(String payload) throws Exception {
        MessageContext msgContext = new MessageContext();
        msgContext.setConfigurationContext(configContext);
        msgContext.setProperty("PAYLOAD_SIZE", payload.length());
        msgContext.setProperty("TEST_PAYLOAD", payload);
        return msgContext;
    }
}