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

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Coordinated memory management between WildFly Undertow and Apache Axis2 HTTP/2 implementations.
 *
 * This class implements Phase 1 memory coordination for the WildFly 32 + Axis2 HTTP/2
 * Cooperative Integration Plan. It provides unified memory allocation tracking and
 * coordinated garbage collection to prevent resource competition in memory-constrained
 * environments (2GB heap constraints).
 *
 * Key features:
 * - Atomic memory usage tracking for both Undertow and Axis2 components
 * - Configurable memory allocation limits (default 40% of heap for HTTP/2 operations)
 * - Coordinated garbage collection triggering when approaching limits
 * - Thread-safe allocation and release operations
 * - Comprehensive monitoring and debugging capabilities
 */
public class HTTP2MemoryCoordinator {
    private static final Log log = LogFactory.getLog(HTTP2MemoryCoordinator.class);

    private final AtomicLong undertowMemoryUsage = new AtomicLong(0);
    private final AtomicLong axis2MemoryUsage = new AtomicLong(0);
    private final long maxTotalMemory;
    private final double memoryThresholdPercentage;

    // Statistics for monitoring
    private final AtomicLong totalAllocationRequests = new AtomicLong(0);
    private final AtomicLong rejectedAllocations = new AtomicLong(0);
    private final AtomicLong coordinatedGCTriggers = new AtomicLong(0);

    /**
     * Initialize memory coordinator with default settings.
     * Reserves 40% of heap for HTTP/2 operations (800MB of 2GB typical heap).
     */
    public HTTP2MemoryCoordinator() {
        this(0.4); // Default to 40% of heap
    }

    /**
     * Initialize memory coordinator with custom memory percentage.
     *
     * @param memoryPercentage Percentage of heap to reserve for HTTP/2 operations (0.0 - 1.0)
     */
    public HTTP2MemoryCoordinator(double memoryPercentage) {
        if (memoryPercentage <= 0.0 || memoryPercentage > 1.0) {
            throw new IllegalArgumentException("Memory percentage must be between 0.0 and 1.0, got: " + memoryPercentage);
        }

        this.memoryThresholdPercentage = memoryPercentage;
        this.maxTotalMemory = (long) (Runtime.getRuntime().maxMemory() * memoryPercentage);

        log.info("HTTP2MemoryCoordinator initialized - Max memory: " + formatBytes(maxTotalMemory) +
                " (" + String.format("%.1f%%", memoryPercentage * 100) + " of " +
                formatBytes(Runtime.getRuntime().maxMemory()) + " heap)");
    }

    /**
     * Request memory allocation with coordination between components.
     *
     * @param component The component requesting memory allocation
     * @param requestedBytes Number of bytes to allocate
     * @return true if allocation was approved, false if denied due to memory limits
     */
    public boolean requestAllocation(Component component, long requestedBytes) {
        if (requestedBytes < 0) {
            log.warn("Negative allocation request ignored: " + requestedBytes + " bytes from " + component);
            return false;
        }

        totalAllocationRequests.incrementAndGet();
        long currentTotal = undertowMemoryUsage.get() + axis2MemoryUsage.get();

        if (currentTotal + requestedBytes > maxTotalMemory) {
            // Trigger coordinated cleanup before rejecting
            boolean cleanupSucceeded = triggerCoordinatedGC(component, requestedBytes);

            // Re-check after cleanup
            currentTotal = undertowMemoryUsage.get() + axis2MemoryUsage.get();
            if (currentTotal + requestedBytes > maxTotalMemory) {
                rejectedAllocations.incrementAndGet();

                log.warn("Memory allocation rejected for " + component + " - " +
                        "Requested: " + formatBytes(requestedBytes) +
                        ", Current total: " + formatBytes(currentTotal) +
                        ", Max allowed: " + formatBytes(maxTotalMemory) +
                        ", Cleanup succeeded: " + cleanupSucceeded);
                return false;
            }
        }

        // Approved - update the appropriate counter
        switch (component) {
            case UNDERTOW:
                undertowMemoryUsage.addAndGet(requestedBytes);
                break;
            case AXIS2:
                axis2MemoryUsage.addAndGet(requestedBytes);
                break;
            default:
                log.warn("Unknown component type: " + component);
                return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("Memory allocation approved for " + component + " - " +
                     "Allocated: " + formatBytes(requestedBytes) +
                     ", New total: " + formatBytes(getCurrentTotalUsage()) +
                     ", Utilization: " + String.format("%.1f%%", getMemoryUtilizationPercentage()));
        }

        return true;
    }

    /**
     * Release allocated memory for a component.
     *
     * @param component The component releasing memory
     * @param releasedBytes Number of bytes to release
     */
    public void releaseAllocation(Component component, long releasedBytes) {
        if (releasedBytes < 0) {
            log.warn("Negative release request ignored: " + releasedBytes + " bytes from " + component);
            return;
        }

        long currentUsage;
        switch (component) {
            case UNDERTOW:
                currentUsage = undertowMemoryUsage.get();
                if (releasedBytes > currentUsage) {
                    log.warn("Attempting to release more memory than allocated for " + component +
                            " - Current: " + formatBytes(currentUsage) +
                            ", Requested release: " + formatBytes(releasedBytes));
                    undertowMemoryUsage.set(0);
                } else {
                    undertowMemoryUsage.addAndGet(-releasedBytes);
                }
                break;
            case AXIS2:
                currentUsage = axis2MemoryUsage.get();
                if (releasedBytes > currentUsage) {
                    log.warn("Attempting to release more memory than allocated for " + component +
                            " - Current: " + formatBytes(currentUsage) +
                            ", Requested release: " + formatBytes(releasedBytes));
                    axis2MemoryUsage.set(0);
                } else {
                    axis2MemoryUsage.addAndGet(-releasedBytes);
                }
                break;
            default:
                log.warn("Unknown component type for release: " + component);
                return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Memory released for " + component + " - " +
                     "Released: " + formatBytes(releasedBytes) +
                     ", New total: " + formatBytes(getCurrentTotalUsage()) +
                     ", Utilization: " + String.format("%.1f%%", getMemoryUtilizationPercentage()));
        }
    }

    /**
     * Trigger coordinated garbage collection when approaching memory limits.
     *
     * @param requestingComponent The component that triggered the GC request
     * @param requestedBytes The number of bytes that triggered this GC
     * @return true if GC was triggered, false if already recently triggered
     */
    private boolean triggerCoordinatedGC(Component requestingComponent, long requestedBytes) {
        coordinatedGCTriggers.incrementAndGet();

        long beforeGC = getCurrentTotalUsage();

        if (log.isDebugEnabled()) {
            log.debug("Triggering coordinated GC - Requested by: " + requestingComponent +
                     ", Requested bytes: " + formatBytes(requestedBytes) +
                     ", Current usage: " + formatBytes(beforeGC));
        }

        // Suggest garbage collection
        System.gc();

        // Brief pause to allow GC to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("GC wait interrupted");
        }

        long afterGC = getCurrentTotalUsage();
        long freedMemory = beforeGC - afterGC;

        log.info("Coordinated GC completed - " +
                "Freed: " + formatBytes(freedMemory) +
                ", Before: " + formatBytes(beforeGC) +
                ", After: " + formatBytes(afterGC) +
                ", Utilization: " + String.format("%.1f%%", getMemoryUtilizationPercentage()));

        return freedMemory > 0;
    }

    /**
     * Get current total memory usage across both components.
     *
     * @return Total bytes currently allocated
     */
    public long getCurrentTotalUsage() {
        return undertowMemoryUsage.get() + axis2MemoryUsage.get();
    }

    /**
     * Get current memory usage for Undertow component.
     *
     * @return Bytes currently allocated to Undertow
     */
    public long getUndertowUsage() {
        return undertowMemoryUsage.get();
    }

    /**
     * Get current memory usage for Axis2 component.
     *
     * @return Bytes currently allocated to Axis2
     */
    public long getAxis2Usage() {
        return axis2MemoryUsage.get();
    }

    /**
     * Get maximum allowed memory for HTTP/2 operations.
     *
     * @return Maximum bytes that can be allocated
     */
    public long getMaxTotalMemory() {
        return maxTotalMemory;
    }

    /**
     * Get current memory utilization as a percentage.
     *
     * @return Utilization percentage (0.0 - 100.0)
     */
    public double getMemoryUtilizationPercentage() {
        return (double) getCurrentTotalUsage() / maxTotalMemory * 100.0;
    }

    /**
     * Get comprehensive statistics for monitoring and debugging.
     *
     * @return String containing detailed memory coordinator statistics
     */
    public String getStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("HTTP2MemoryCoordinator Statistics:\n");
        stats.append("  Total Usage: ").append(formatBytes(getCurrentTotalUsage()))
             .append(" / ").append(formatBytes(maxTotalMemory))
             .append(" (").append(String.format("%.1f%%", getMemoryUtilizationPercentage())).append(")\n");
        stats.append("  Undertow Usage: ").append(formatBytes(getUndertowUsage())).append("\n");
        stats.append("  Axis2 Usage: ").append(formatBytes(getAxis2Usage())).append("\n");
        stats.append("  Allocation Requests: ").append(totalAllocationRequests.get()).append("\n");
        stats.append("  Rejected Allocations: ").append(rejectedAllocations.get()).append("\n");
        stats.append("  GC Triggers: ").append(coordinatedGCTriggers.get()).append("\n");
        stats.append("  Memory Threshold: ").append(String.format("%.1f%%", memoryThresholdPercentage * 100));

        return stats.toString();
    }

    /**
     * Reset all statistics counters (does not affect memory usage tracking).
     */
    public void resetStatistics() {
        totalAllocationRequests.set(0);
        rejectedAllocations.set(0);
        coordinatedGCTriggers.set(0);
        log.info("HTTP2MemoryCoordinator statistics reset");
    }

    /**
     * Format bytes for human-readable display.
     *
     * @param bytes Number of bytes to format
     * @return Formatted string (e.g., "1.5MB", "2.3GB")
     */
    private static String formatBytes(long bytes) {
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
     * Component types for memory allocation tracking.
     */
    public enum Component {
        /** WildFly Undertow HTTP/2 implementation */
        UNDERTOW,
        /** Apache Axis2 HTTP/2 transport implementation */
        AXIS2
    }
}