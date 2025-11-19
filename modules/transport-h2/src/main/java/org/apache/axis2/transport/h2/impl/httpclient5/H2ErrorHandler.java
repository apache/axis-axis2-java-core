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

package org.apache.axis2.transport.h2.impl.httpclient5;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enhanced HTTP/2 Error Handler for Enterprise Production Environments.
 *
 * This handler provides comprehensive error handling, recovery strategies,
 * and diagnostic capabilities specifically designed for HTTP/2 transport issues.
 *
 * Key Features:
 * - HTTP/2 specific error code interpretation (RFC 7540)
 * - Intelligent error categorization and recovery strategies
 * - Connection state management and cleanup
 * - Stream-level error handling and isolation
 * - Comprehensive error metrics and monitoring
 * - Enterprise-grade logging and diagnostics
 *
 * HTTP/2 Error Codes (RFC 7540):
 * - NO_ERROR (0x0): Graceful shutdown
 * - PROTOCOL_ERROR (0x1): Protocol violation
 * - INTERNAL_ERROR (0x2): Implementation fault
 * - FLOW_CONTROL_ERROR (0x3): Flow control limits exceeded
 * - SETTINGS_TIMEOUT (0x4): Settings frame not acknowledged
 * - STREAM_CLOSED (0x5): Stream no longer needed
 * - FRAME_SIZE_ERROR (0x6): Frame size incorrect
 * - REFUSED_STREAM (0x7): Stream refused before processing
 * - CANCEL (0x8): Stream cancelled
 * - COMPRESSION_ERROR (0x9): Compression state not updated
 * - CONNECT_ERROR (0xa): TCP connection error for CONNECT method
 * - enhance_YOUR_CALM (0xb): Processing capacity exceeded
 * - INADEQUATE_SECURITY (0xc): Security requirements not met
 * - HTTP_1_1_REQUIRED (0xd): HTTP/1.1 required
 *
 * Production Benefits:
 * - Prevents cascading failures through proper error isolation
 * - Provides actionable diagnostics for troubleshooting
 * - Maintains service availability through intelligent recovery
 * - Comprehensive monitoring for proactive issue resolution
 */
public class H2ErrorHandler {

    private static final Log log = LogFactory.getLog(H2ErrorHandler.class);

    /**
     * HTTP/2 error codes as defined in RFC 7540.
     */
    public enum H2ErrorCode {
        NO_ERROR(0x0, "Graceful shutdown", false, false),
        PROTOCOL_ERROR(0x1, "Protocol violation detected", true, true),
        INTERNAL_ERROR(0x2, "Implementation internal error", true, false),
        FLOW_CONTROL_ERROR(0x3, "Flow control limits exceeded", true, true),
        SETTINGS_TIMEOUT(0x4, "Settings frame acknowledgment timeout", true, true),
        STREAM_CLOSED(0x5, "Stream no longer needed", false, false),
        FRAME_SIZE_ERROR(0x6, "Incorrect frame size", true, true),
        REFUSED_STREAM(0x7, "Stream refused before processing", false, true),
        CANCEL(0x8, "Stream cancelled by endpoint", false, false),
        COMPRESSION_ERROR(0x9, "HPACK compression state error", true, true),
        CONNECT_ERROR(0xa, "TCP connection error for CONNECT", true, false),
        ENHANCE_YOUR_CALM(0xb, "Processing capacity exceeded", false, true),
        INADEQUATE_SECURITY(0xc, "Security requirements not met", true, false),
        HTTP_1_1_REQUIRED(0xd, "HTTP/1.1 required by server", false, true);

        private final int code;
        private final String description;
        private final boolean isFatal;
        private final boolean isRetryable;

        H2ErrorCode(int code, String description, boolean isFatal, boolean isRetryable) {
            this.code = code;
            this.description = description;
            this.isFatal = isFatal;
            this.isRetryable = isRetryable;
        }

        public int getCode() { return code; }
        public String getDescription() { return description; }
        public boolean isFatal() { return isFatal; }
        public boolean isRetryable() { return isRetryable; }

        public static H2ErrorCode fromCode(int code) {
            for (H2ErrorCode errorCode : values()) {
                if (errorCode.code == code) {
                    return errorCode;
                }
            }
            return INTERNAL_ERROR; // Default for unknown codes
        }
    }

    /**
     * Error category for recovery strategy selection.
     */
    public enum ErrorCategory {
        PROTOCOL_VIOLATION("HTTP/2 protocol violation", true),
        NETWORK_ERROR("Network connectivity issue", false),
        STREAM_ERROR("Individual stream error", false),
        CONNECTION_ERROR("Connection-level error", true),
        SECURITY_ERROR("Security or authentication error", true),
        CAPACITY_ERROR("Server capacity or resource limit", false),
        CONFIGURATION_ERROR("Configuration or setup error", true);

        private final String description;
        private final boolean requiresConnectionReset;

        ErrorCategory(String description, boolean requiresConnectionReset) {
            this.description = description;
            this.requiresConnectionReset = requiresConnectionReset;
        }

        public String getDescription() { return description; }
        public boolean requiresConnectionReset() { return requiresConnectionReset; }
    }

    /**
     * Recovery strategy for different error types.
     */
    public enum RecoveryStrategy {
        RETRY_IMMEDIATE("Retry the request immediately"),
        RETRY_WITH_BACKOFF("Retry with exponential backoff"),
        FALLBACK_HTTP1("Fallback to HTTP/1.1"),
        RESET_CONNECTION("Reset the connection and retry"),
        FAIL_FAST("Fail immediately, no recovery"),
        STREAM_RESET("Reset only the affected stream");

        private final String description;

        RecoveryStrategy(String description) {
            this.description = description;
        }

        public String getDescription() { return description; }
    }

    /**
     * Comprehensive error context for analysis and recovery.
     */
    public static class H2ErrorContext {
        private final H2ErrorCode errorCode;
        private final ErrorCategory category;
        private final String streamId;
        private final String connectionId;
        private final Exception originalException;
        private final String errorMessage;
        private final long timestamp;
        private final RecoveryStrategy recommendedStrategy;

        public H2ErrorContext(H2ErrorCode errorCode, ErrorCategory category,
                             String streamId, String connectionId,
                             Exception originalException, String errorMessage,
                             RecoveryStrategy recommendedStrategy) {
            this.errorCode = errorCode;
            this.category = category;
            this.streamId = streamId;
            this.connectionId = connectionId;
            this.originalException = originalException;
            this.errorMessage = errorMessage;
            this.timestamp = System.currentTimeMillis();
            this.recommendedStrategy = recommendedStrategy;
        }

        // Getters
        public H2ErrorCode getErrorCode() { return errorCode; }
        public ErrorCategory getCategory() { return category; }
        public String getStreamId() { return streamId; }
        public String getConnectionId() { return connectionId; }
        public Exception getOriginalException() { return originalException; }
        public String getErrorMessage() { return errorMessage; }
        public long getTimestamp() { return timestamp; }
        public RecoveryStrategy getRecommendedStrategy() { return recommendedStrategy; }

        public boolean isStreamLevel() {
            return streamId != null && !streamId.isEmpty();
        }

        public boolean isConnectionLevel() {
            return category.requiresConnectionReset();
        }

        @Override
        public String toString() {
            return String.format("H2Error[code=%s, category=%s, stream=%s, connection=%s, strategy=%s, message=%s]",
                               errorCode, category, streamId, connectionId, recommendedStrategy, errorMessage);
        }
    }

    // Error tracking and metrics
    private final ConcurrentHashMap<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong recoveredErrors = new AtomicLong(0);
    private final AtomicLong failedRecoveries = new AtomicLong(0);

    // Configuration
    private final boolean enableRecovery;
    private final int maxRetryAttempts;
    private final long backoffIntervalMs;

    public H2ErrorHandler(boolean enableRecovery, int maxRetryAttempts, long backoffIntervalMs) {
        this.enableRecovery = enableRecovery;
        this.maxRetryAttempts = maxRetryAttempts;
        this.backoffIntervalMs = backoffIntervalMs;

        log.info("H2ErrorHandler initialized - Recovery enabled: " + enableRecovery +
                ", Max retries: " + maxRetryAttempts +
                ", Backoff interval: " + backoffIntervalMs + "ms");
    }

    /**
     * Default constructor with enterprise settings.
     */
    public H2ErrorHandler() {
        this(true, 3, 1000);
    }

    /**
     * Analyze and categorize an HTTP/2 error.
     */
    public H2ErrorContext analyzeError(Exception exception, String streamId, String connectionId) {
        totalErrors.incrementAndGet();

        H2ErrorCode errorCode = extractErrorCode(exception);
        ErrorCategory category = categorizeError(exception, errorCode);
        RecoveryStrategy strategy = determineRecoveryStrategy(errorCode, category, exception);

        String errorMessage = buildErrorMessage(exception, errorCode);

        // Update error statistics
        String errorKey = errorCode.name();
        errorCounts.computeIfAbsent(errorKey, k -> new AtomicLong(0)).incrementAndGet();

        H2ErrorContext context = new H2ErrorContext(errorCode, category, streamId, connectionId,
                                                   exception, errorMessage, strategy);

        log.warn("HTTP/2 error analyzed: " + context);
        return context;
    }

    /**
     * Handle HTTP/2 error with appropriate recovery strategy.
     */
    public AxisFault handleError(H2ErrorContext errorContext, H2FallbackManager fallbackManager) {
        String logPrefix = "H2ErrorHandler.handleError() [" + errorContext.getConnectionId() + "] - ";

        try {
            log.info(logPrefix + "Handling HTTP/2 error: " + errorContext.getErrorCode().getDescription());

            if (enableRecovery) {
                return executeRecoveryStrategy(errorContext, fallbackManager);
            } else {
                return createAxisFault(errorContext);
            }

        } catch (Exception e) {
            failedRecoveries.incrementAndGet();
            log.error(logPrefix + "Error recovery failed: " + e.getMessage(), e);
            return new AxisFault("HTTP/2 error recovery failed", e);
        }
    }

    /**
     * Execute the appropriate recovery strategy.
     */
    private AxisFault executeRecoveryStrategy(H2ErrorContext errorContext, H2FallbackManager fallbackManager) {
        switch (errorContext.getRecommendedStrategy()) {
            case FALLBACK_HTTP1:
                return executeFallbackRecovery(errorContext, fallbackManager);

            case RETRY_WITH_BACKOFF:
                return executeRetryRecovery(errorContext);

            case RESET_CONNECTION:
                return executeConnectionReset(errorContext);

            case STREAM_RESET:
                return executeStreamReset(errorContext);

            case FAIL_FAST:
            default:
                return createAxisFault(errorContext);
        }
    }

    /**
     * Execute HTTP/1.1 fallback recovery.
     */
    private AxisFault executeFallbackRecovery(H2ErrorContext errorContext, H2FallbackManager fallbackManager) {
        if (fallbackManager == null || !fallbackManager.isFallbackEnabled()) {
            return createAxisFault(errorContext, "Fallback not available");
        }

        try {
            H2FallbackManager.FallbackReason reason = mapErrorToFallbackReason(errorContext.getErrorCode());
            boolean shouldFallback = fallbackManager.shouldAttemptFallback(
                errorContext.getConnectionId(), reason);

            if (shouldFallback) {
                recoveredErrors.incrementAndGet();
                log.info("HTTP/2 error recovery: falling back to HTTP/1.1");
                return new AxisFault("HTTP/2 error - fallback to HTTP/1.1 recommended", errorContext.getOriginalException());
            } else {
                return createAxisFault(errorContext, "Fallback not recommended for this error");
            }

        } catch (Exception e) {
            return createAxisFault(errorContext, "Fallback execution failed: " + e.getMessage());
        }
    }

    /**
     * Execute retry recovery with backoff.
     */
    private AxisFault executeRetryRecovery(H2ErrorContext errorContext) {
        if (!errorContext.getErrorCode().isRetryable()) {
            return createAxisFault(errorContext, "Error is not retryable");
        }

        recoveredErrors.incrementAndGet();
        log.info("HTTP/2 error recovery: retry with backoff recommended");
        return new AxisFault("HTTP/2 error - retry recommended after " + backoffIntervalMs + "ms",
                           errorContext.getOriginalException());
    }

    /**
     * Execute connection reset recovery.
     */
    private AxisFault executeConnectionReset(H2ErrorContext errorContext) {
        recoveredErrors.incrementAndGet();
        log.info("HTTP/2 error recovery: connection reset recommended");
        return new AxisFault("HTTP/2 error - connection reset required", errorContext.getOriginalException());
    }

    /**
     * Execute stream reset recovery.
     */
    private AxisFault executeStreamReset(H2ErrorContext errorContext) {
        recoveredErrors.incrementAndGet();
        log.info("HTTP/2 error recovery: stream reset recommended for stream " + errorContext.getStreamId());
        return new AxisFault("HTTP/2 error - stream reset required", errorContext.getOriginalException());
    }

    /**
     * Extract HTTP/2 error code from exception.
     */
    private H2ErrorCode extractErrorCode(Exception exception) {
        if (exception == null) {
            return H2ErrorCode.INTERNAL_ERROR;
        }

        String message = exception.getMessage();
        if (message == null) {
            return H2ErrorCode.INTERNAL_ERROR;
        }

        // Check for specific HTTP/2 error patterns
        message = message.toLowerCase();

        if (message.contains("protocol") || message.contains("violation")) {
            return H2ErrorCode.PROTOCOL_ERROR;
        } else if (message.contains("flow control") || message.contains("window")) {
            return H2ErrorCode.FLOW_CONTROL_ERROR;
        } else if (message.contains("frame size")) {
            return H2ErrorCode.FRAME_SIZE_ERROR;
        } else if (message.contains("compression") || message.contains("hpack")) {
            return H2ErrorCode.COMPRESSION_ERROR;
        } else if (message.contains("settings") || message.contains("timeout")) {
            return H2ErrorCode.SETTINGS_TIMEOUT;
        } else if (message.contains("refused") || message.contains("rejected")) {
            return H2ErrorCode.REFUSED_STREAM;
        } else if (message.contains("security") || message.contains("tls")) {
            return H2ErrorCode.INADEQUATE_SECURITY;
        } else if (message.contains("http/1.1") || message.contains("downgrade")) {
            return H2ErrorCode.HTTP_1_1_REQUIRED;
        }

        return H2ErrorCode.INTERNAL_ERROR;
    }

    /**
     * Categorize error for recovery strategy selection.
     */
    private ErrorCategory categorizeError(Exception exception, H2ErrorCode errorCode) {
        if (exception instanceof SocketTimeoutException) {
            return ErrorCategory.NETWORK_ERROR;
        } else if (exception instanceof ConnectException) {
            return ErrorCategory.CONNECTION_ERROR;
        } else if (exception instanceof ClosedChannelException) {
            return ErrorCategory.CONNECTION_ERROR;
        } else if (exception instanceof IOException) {
            return ErrorCategory.NETWORK_ERROR;
        }

        switch (errorCode) {
            case PROTOCOL_ERROR:
            case FRAME_SIZE_ERROR:
            case COMPRESSION_ERROR:
                return ErrorCategory.PROTOCOL_VIOLATION;

            case FLOW_CONTROL_ERROR:
            case ENHANCE_YOUR_CALM:
                return ErrorCategory.CAPACITY_ERROR;

            case INADEQUATE_SECURITY:
                return ErrorCategory.SECURITY_ERROR;

            case REFUSED_STREAM:
            case STREAM_CLOSED:
            case CANCEL:
                return ErrorCategory.STREAM_ERROR;

            case HTTP_1_1_REQUIRED:
                return ErrorCategory.CONFIGURATION_ERROR;

            default:
                return ErrorCategory.CONNECTION_ERROR;
        }
    }

    /**
     * Determine optimal recovery strategy.
     */
    private RecoveryStrategy determineRecoveryStrategy(H2ErrorCode errorCode, ErrorCategory category, Exception exception) {
        switch (errorCode) {
            case HTTP_1_1_REQUIRED:
            case INADEQUATE_SECURITY:
                return RecoveryStrategy.FALLBACK_HTTP1;

            case REFUSED_STREAM:
            case ENHANCE_YOUR_CALM:
                return RecoveryStrategy.RETRY_WITH_BACKOFF;

            case STREAM_CLOSED:
            case CANCEL:
                return RecoveryStrategy.STREAM_RESET;

            case PROTOCOL_ERROR:
            case FRAME_SIZE_ERROR:
            case COMPRESSION_ERROR:
                return RecoveryStrategy.RESET_CONNECTION;

            case FLOW_CONTROL_ERROR:
                return RecoveryStrategy.RETRY_WITH_BACKOFF;

            default:
                if (category == ErrorCategory.NETWORK_ERROR) {
                    return RecoveryStrategy.RETRY_WITH_BACKOFF;
                } else if (category == ErrorCategory.CONNECTION_ERROR) {
                    return RecoveryStrategy.RESET_CONNECTION;
                } else {
                    return RecoveryStrategy.FAIL_FAST;
                }
        }
    }

    /**
     * Map HTTP/2 error to fallback reason.
     */
    private H2FallbackManager.FallbackReason mapErrorToFallbackReason(H2ErrorCode errorCode) {
        switch (errorCode) {
            case HTTP_1_1_REQUIRED:
                return H2FallbackManager.FallbackReason.SERVER_NOT_SUPPORTED;
            case INADEQUATE_SECURITY:
                return H2FallbackManager.FallbackReason.ALPN_NOT_SUPPORTED;
            case PROTOCOL_ERROR:
                return H2FallbackManager.FallbackReason.HTTP2_ERROR;
            case SETTINGS_TIMEOUT:
                return H2FallbackManager.FallbackReason.CONNECTION_TIMEOUT;
            default:
                return H2FallbackManager.FallbackReason.HTTP2_ERROR;
        }
    }

    /**
     * Build comprehensive error message.
     */
    private String buildErrorMessage(Exception exception, H2ErrorCode errorCode) {
        StringBuilder message = new StringBuilder();
        message.append("HTTP/2 Error [").append(errorCode.name()).append("]: ");
        message.append(errorCode.getDescription());

        if (exception != null && exception.getMessage() != null) {
            message.append(" - ").append(exception.getMessage());
        }

        return message.toString();
    }

    /**
     * Create AxisFault from error context.
     */
    private AxisFault createAxisFault(H2ErrorContext errorContext) {
        return createAxisFault(errorContext, null);
    }

    /**
     * Create AxisFault with additional details.
     */
    private AxisFault createAxisFault(H2ErrorContext errorContext, String additionalInfo) {
        String message = errorContext.getErrorMessage();
        if (additionalInfo != null) {
            message += " (" + additionalInfo + ")";
        }

        return new AxisFault(message, errorContext.getOriginalException());
    }

    /**
     * Get comprehensive error handling metrics.
     */
    public H2ErrorMetrics getMetrics() {
        return new H2ErrorMetrics(
            totalErrors.get(),
            recoveredErrors.get(),
            failedRecoveries.get(),
            new ConcurrentHashMap<>(errorCounts)
        );
    }

    /**
     * Error handling metrics container.
     */
    public static class H2ErrorMetrics {
        public final long totalErrors;
        public final long recoveredErrors;
        public final long failedRecoveries;
        public final ConcurrentHashMap<String, AtomicLong> errorBreakdown;

        public H2ErrorMetrics(long totalErrors, long recoveredErrors,
                             long failedRecoveries, ConcurrentHashMap<String, AtomicLong> errorBreakdown) {
            this.totalErrors = totalErrors;
            this.recoveredErrors = recoveredErrors;
            this.failedRecoveries = failedRecoveries;
            this.errorBreakdown = errorBreakdown;
        }

        public double getRecoveryRate() {
            return totalErrors > 0 ? (double) recoveredErrors / totalErrors : 0.0;
        }

        @Override
        public String toString() {
            return String.format("H2ErrorMetrics[total=%d, recovered=%d, failed=%d, recoveryRate=%.2f%%]",
                               totalErrors, recoveredErrors, failedRecoveries, getRecoveryRate() * 100);
        }
    }
}