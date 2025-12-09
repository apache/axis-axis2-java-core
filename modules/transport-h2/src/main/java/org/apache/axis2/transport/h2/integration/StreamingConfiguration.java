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
 * Configuration for HTTP/2 JSON streaming optimization based on payload characteristics.
 *
 * This class provides streaming configuration as part of Phase 4 of the WildFly 32 + Axis2
 * HTTP/2 Cooperative Integration Plan. It determines optimal streaming strategies based on
 * payload size, content type, and system conditions for maximum performance.
 *
 * Key features:
 * - Payload-based streaming strategy selection
 * - Flow control interval configuration
 * - Memory management settings
 * - Performance optimization parameters
 * - Adaptive configuration based on payload characteristics
 */
public class StreamingConfiguration {
    private final long payloadSize;
    private final StreamingStrategy strategy;
    private final int flowControlInterval;
    private final int gcInterval;
    private final int bufferSize;
    private final boolean enableBackPressure;

    // Payload size thresholds for strategy selection
    private static final long TINY_PAYLOAD_THRESHOLD = 64 * 1024;        // 64KB
    private static final long SMALL_PAYLOAD_THRESHOLD = 1024 * 1024;     // 1MB
    private static final long MEDIUM_PAYLOAD_THRESHOLD = 10 * 1024 * 1024; // 10MB
    private static final long LARGE_PAYLOAD_THRESHOLD = 50 * 1024 * 1024;  // 50MB

    // Default configuration values
    private static final int DEFAULT_FLOW_CONTROL_INTERVAL = 100;
    private static final int DEFAULT_GC_INTERVAL = 1000;
    private static final int DEFAULT_BUFFER_SIZE = 65536; // 64KB

    /**
     * Create streaming configuration based on payload size.
     *
     * @param payloadSize Expected payload size in bytes
     */
    public StreamingConfiguration(long payloadSize) {
        this.payloadSize = payloadSize;
        this.strategy = determineStreamingStrategy(payloadSize);
        this.flowControlInterval = calculateFlowControlInterval(payloadSize, strategy);
        this.gcInterval = calculateGCInterval(payloadSize, strategy);
        this.bufferSize = calculateBufferSize(payloadSize, strategy);
        this.enableBackPressure = shouldEnableBackPressure(payloadSize, strategy);
    }

    /**
     * Create streaming configuration with custom parameters.
     *
     * @param payloadSize Expected payload size in bytes
     * @param strategy Streaming strategy to use
     * @param flowControlInterval Interval for flow control checks
     * @param gcInterval Interval for garbage collection suggestions
     * @param bufferSize Buffer size for streaming operations
     * @param enableBackPressure Whether to enable back-pressure control
     */
    public StreamingConfiguration(long payloadSize, StreamingStrategy strategy,
                                 int flowControlInterval, int gcInterval,
                                 int bufferSize, boolean enableBackPressure) {
        this.payloadSize = payloadSize;
        this.strategy = strategy;
        this.flowControlInterval = flowControlInterval;
        this.gcInterval = gcInterval;
        this.bufferSize = bufferSize;
        this.enableBackPressure = enableBackPressure;
    }

    // Accessor methods

    /**
     * Get the payload size this configuration is optimized for.
     *
     * @return Payload size in bytes
     */
    public long getPayloadSize() {
        return payloadSize;
    }

    /**
     * Get the streaming strategy for this configuration.
     *
     * @return Streaming strategy enum value
     */
    public StreamingStrategy getStrategy() {
        return strategy;
    }

    /**
     * Get the flow control check interval.
     *
     * @return Number of operations between flow control checks
     */
    public int getFlowControlInterval() {
        return flowControlInterval;
    }

    /**
     * Get the garbage collection suggestion interval.
     *
     * @return Number of operations between GC suggestions
     */
    public int getGCInterval() {
        return gcInterval;
    }

    /**
     * Get the buffer size for streaming operations.
     *
     * @return Buffer size in bytes
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Check if back-pressure control is enabled.
     *
     * @return true if back-pressure should be applied, false otherwise
     */
    public boolean isBackPressureEnabled() {
        return enableBackPressure;
    }

    // Convenience methods for strategy-based decisions

    /**
     * Check if this is a large payload requiring special handling.
     *
     * @return true for large payloads, false otherwise
     */
    public boolean isLargePayload() {
        return strategy == StreamingStrategy.LARGE_PAYLOAD_STREAMING ||
               strategy == StreamingStrategy.MASSIVE_PAYLOAD_STREAMING;
    }

    /**
     * Check if this payload should use streaming processing.
     *
     * @return true if streaming is recommended, false for in-memory processing
     */
    public boolean shouldUseStreaming() {
        return strategy != StreamingStrategy.IN_MEMORY_PROCESSING;
    }

    /**
     * Check if aggressive flow control should be used.
     *
     * @return true for aggressive flow control, false for conservative
     */
    public boolean shouldUseAggressiveFlowControl() {
        return strategy == StreamingStrategy.IN_MEMORY_PROCESSING ||
               strategy == StreamingStrategy.SMALL_PAYLOAD_STREAMING;
    }

    /**
     * Check if conservative memory management should be used.
     *
     * @return true for conservative memory management, false otherwise
     */
    public boolean shouldUseConservativeMemoryManagement() {
        return strategy == StreamingStrategy.LARGE_PAYLOAD_STREAMING ||
               strategy == StreamingStrategy.MASSIVE_PAYLOAD_STREAMING;
    }

    // Configuration calculation methods

    /**
     * Determine optimal streaming strategy based on payload size.
     */
    private StreamingStrategy determineStreamingStrategy(long payloadSize) {
        if (payloadSize <= TINY_PAYLOAD_THRESHOLD) {
            return StreamingStrategy.IN_MEMORY_PROCESSING;
        } else if (payloadSize <= SMALL_PAYLOAD_THRESHOLD) {
            return StreamingStrategy.SMALL_PAYLOAD_STREAMING;
        } else if (payloadSize <= MEDIUM_PAYLOAD_THRESHOLD) {
            return StreamingStrategy.MEDIUM_PAYLOAD_STREAMING;
        } else if (payloadSize <= LARGE_PAYLOAD_THRESHOLD) {
            return StreamingStrategy.LARGE_PAYLOAD_STREAMING;
        } else {
            return StreamingStrategy.MASSIVE_PAYLOAD_STREAMING;
        }
    }

    /**
     * Calculate optimal flow control interval based on payload and strategy.
     */
    private int calculateFlowControlInterval(long payloadSize, StreamingStrategy strategy) {
        switch (strategy) {
            case IN_MEMORY_PROCESSING:
                return 500; // Less frequent checks for small payloads
            case SMALL_PAYLOAD_STREAMING:
                return 200; // Moderate frequency
            case MEDIUM_PAYLOAD_STREAMING:
                return DEFAULT_FLOW_CONTROL_INTERVAL; // Default frequency
            case LARGE_PAYLOAD_STREAMING:
                return 50; // More frequent checks for large payloads
            case MASSIVE_PAYLOAD_STREAMING:
                return 20; // Very frequent checks for massive payloads
            default:
                return DEFAULT_FLOW_CONTROL_INTERVAL;
        }
    }

    /**
     * Calculate optimal garbage collection interval based on payload and strategy.
     */
    private int calculateGCInterval(long payloadSize, StreamingStrategy strategy) {
        switch (strategy) {
            case IN_MEMORY_PROCESSING:
                return 5000; // Infrequent GC for small payloads
            case SMALL_PAYLOAD_STREAMING:
                return 2000; // Moderate GC frequency
            case MEDIUM_PAYLOAD_STREAMING:
                return DEFAULT_GC_INTERVAL; // Default frequency
            case LARGE_PAYLOAD_STREAMING:
                return 500; // More frequent GC for large payloads
            case MASSIVE_PAYLOAD_STREAMING:
                return 200; // Very frequent GC for massive payloads
            default:
                return DEFAULT_GC_INTERVAL;
        }
    }

    /**
     * Calculate optimal buffer size based on payload and strategy.
     */
    private int calculateBufferSize(long payloadSize, StreamingStrategy strategy) {
        switch (strategy) {
            case IN_MEMORY_PROCESSING:
                return 32 * 1024; // 32KB for small payloads
            case SMALL_PAYLOAD_STREAMING:
                return DEFAULT_BUFFER_SIZE; // 64KB default
            case MEDIUM_PAYLOAD_STREAMING:
                return 128 * 1024; // 128KB for medium payloads
            case LARGE_PAYLOAD_STREAMING:
                return 256 * 1024; // 256KB for large payloads
            case MASSIVE_PAYLOAD_STREAMING:
                return 512 * 1024; // 512KB for massive payloads
            default:
                return DEFAULT_BUFFER_SIZE;
        }
    }

    /**
     * Determine if back-pressure should be enabled based on payload and strategy.
     */
    private boolean shouldEnableBackPressure(long payloadSize, StreamingStrategy strategy) {
        // Enable back-pressure for medium and larger payloads
        return strategy == StreamingStrategy.MEDIUM_PAYLOAD_STREAMING ||
               strategy == StreamingStrategy.LARGE_PAYLOAD_STREAMING ||
               strategy == StreamingStrategy.MASSIVE_PAYLOAD_STREAMING;
    }

    /**
     * Create a configuration optimized for throughput (less memory management overhead).
     *
     * @param payloadSize Expected payload size in bytes
     * @return Configuration optimized for maximum throughput
     */
    public static StreamingConfiguration forThroughput(long payloadSize) {
        StreamingConfiguration base = new StreamingConfiguration(payloadSize);
        return new StreamingConfiguration(
            payloadSize,
            base.strategy,
            base.flowControlInterval * 2, // Less frequent flow control checks
            base.gcInterval * 2,          // Less frequent GC suggestions
            base.bufferSize * 2,          // Larger buffers
            false                         // Disable back-pressure for max speed
        );
    }

    /**
     * Create a configuration optimized for memory efficiency (more conservative).
     *
     * @param payloadSize Expected payload size in bytes
     * @return Configuration optimized for memory efficiency
     */
    public static StreamingConfiguration forMemoryEfficiency(long payloadSize) {
        StreamingConfiguration base = new StreamingConfiguration(payloadSize);
        return new StreamingConfiguration(
            payloadSize,
            base.strategy,
            base.flowControlInterval / 2, // More frequent flow control checks
            base.gcInterval / 2,          // More frequent GC suggestions
            base.bufferSize / 2,          // Smaller buffers
            true                          // Enable back-pressure
        );
    }

    /**
     * Get human-readable description of the configuration.
     *
     * @return String describing the configuration parameters
     */
    public String getConfigurationDescription() {
        return String.format("StreamingConfiguration{" +
                "payload=%s, strategy=%s, flowInterval=%d, gcInterval=%d, " +
                "bufferSize=%s, backPressure=%s}",
                formatBytes(payloadSize), strategy, flowControlInterval, gcInterval,
                formatBytes(bufferSize), enableBackPressure);
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

    @Override
    public String toString() {
        return getConfigurationDescription();
    }

    /**
     * Streaming strategies for different payload characteristics.
     */
    public enum StreamingStrategy {
        /** Process small payloads entirely in memory for maximum speed */
        IN_MEMORY_PROCESSING,

        /** Stream small payloads with minimal overhead */
        SMALL_PAYLOAD_STREAMING,

        /** Stream medium payloads with balanced memory/performance tradeoffs */
        MEDIUM_PAYLOAD_STREAMING,

        /** Stream large payloads with conservative memory management */
        LARGE_PAYLOAD_STREAMING,

        /** Stream massive payloads with aggressive memory optimization */
        MASSIVE_PAYLOAD_STREAMING
    }
}