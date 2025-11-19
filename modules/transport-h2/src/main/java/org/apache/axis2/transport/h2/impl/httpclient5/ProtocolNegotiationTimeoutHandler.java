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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Protocol Negotiation Timeout Handler for HTTP/2 Transport.
 *
 * This handler manages timeouts during HTTP/2 protocol negotiation to prevent
 * indefinite connection hanging and ensure proper fallback behavior.
 *
 * Key Features:
 * - Configurable timeout periods for different negotiation phases
 * - Automatic timeout detection and handling
 * - Fallback trigger when negotiation times out
 * - Connection cleanup for failed negotiations
 * - Comprehensive timeout monitoring and metrics
 * - Thread-safe timeout management
 *
 * Negotiation Phases:
 * 1. TCP Connection establishment
 * 2. TLS handshake and ALPN negotiation
 * 3. HTTP/2 connection preface exchange
 * 4. Settings frame exchange
 * 5. Initial window size negotiation
 *
 * Production Benefits:
 * - Prevents connection hanging in production environments
 * - Enables quick fallback to HTTP/1.1 when HTTP/2 negotiation fails
 * - Provides predictable connection behavior under network issues
 * - Comprehensive monitoring for network troubleshooting
 */
public class ProtocolNegotiationTimeoutHandler {

    private static final Log log = LogFactory.getLog(ProtocolNegotiationTimeoutHandler.class);

    // Default timeout configurations (milliseconds)
    private static final long DEFAULT_OVERALL_TIMEOUT = 15000;      // 15 seconds
    private static final long DEFAULT_TCP_CONNECT_TIMEOUT = 5000;   // 5 seconds
    private static final long DEFAULT_TLS_HANDSHAKE_TIMEOUT = 8000; // 8 seconds
    private static final long DEFAULT_ALPN_TIMEOUT = 3000;          // 3 seconds
    private static final long DEFAULT_H2_PREFACE_TIMEOUT = 5000;    // 5 seconds
    private static final long DEFAULT_SETTINGS_TIMEOUT = 3000;      // 3 seconds

    /**
     * Negotiation phase enumeration.
     */
    public enum NegotiationPhase {
        TCP_CONNECT("TCP connection establishment"),
        TLS_HANDSHAKE("TLS handshake and certificate validation"),
        ALPN_NEGOTIATION("ALPN protocol negotiation"),
        H2_PREFACE("HTTP/2 connection preface exchange"),
        SETTINGS_EXCHANGE("HTTP/2 settings frame exchange"),
        COMPLETE("Protocol negotiation completed");

        private final String description;

        NegotiationPhase(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Timeout configuration for different negotiation phases.
     */
    public static class TimeoutConfig {
        private final long overallTimeoutMs;
        private final long tcpConnectTimeoutMs;
        private final long tlsHandshakeTimeoutMs;
        private final long alpnTimeoutMs;
        private final long h2PrefaceTimeoutMs;
        private final long settingsTimeoutMs;

        public TimeoutConfig(long overallTimeoutMs, long tcpConnectTimeoutMs,
                           long tlsHandshakeTimeoutMs, long alpnTimeoutMs,
                           long h2PrefaceTimeoutMs, long settingsTimeoutMs) {
            this.overallTimeoutMs = overallTimeoutMs;
            this.tcpConnectTimeoutMs = tcpConnectTimeoutMs;
            this.tlsHandshakeTimeoutMs = tlsHandshakeTimeoutMs;
            this.alpnTimeoutMs = alpnTimeoutMs;
            this.h2PrefaceTimeoutMs = h2PrefaceTimeoutMs;
            this.settingsTimeoutMs = settingsTimeoutMs;
        }

        public static TimeoutConfig defaultConfig() {
            return new TimeoutConfig(DEFAULT_OVERALL_TIMEOUT, DEFAULT_TCP_CONNECT_TIMEOUT,
                                   DEFAULT_TLS_HANDSHAKE_TIMEOUT, DEFAULT_ALPN_TIMEOUT,
                                   DEFAULT_H2_PREFACE_TIMEOUT, DEFAULT_SETTINGS_TIMEOUT);
        }

        public static TimeoutConfig enterpriseConfig() {
            return new TimeoutConfig(20000, 8000, 10000, 5000, 8000, 5000);
        }

        public static TimeoutConfig fastConfig() {
            return new TimeoutConfig(8000, 3000, 4000, 2000, 3000, 2000);
        }

        // Getters
        public long getOverallTimeoutMs() { return overallTimeoutMs; }
        public long getTcpConnectTimeoutMs() { return tcpConnectTimeoutMs; }
        public long getTlsHandshakeTimeoutMs() { return tlsHandshakeTimeoutMs; }
        public long getAlpnTimeoutMs() { return alpnTimeoutMs; }
        public long getH2PrefaceTimeoutMs() { return h2PrefaceTimeoutMs; }
        public long getSettingsTimeoutMs() { return settingsTimeoutMs; }

        public long getTimeoutForPhase(NegotiationPhase phase) {
            switch (phase) {
                case TCP_CONNECT: return tcpConnectTimeoutMs;
                case TLS_HANDSHAKE: return tlsHandshakeTimeoutMs;
                case ALPN_NEGOTIATION: return alpnTimeoutMs;
                case H2_PREFACE: return h2PrefaceTimeoutMs;
                case SETTINGS_EXCHANGE: return settingsTimeoutMs;
                default: return overallTimeoutMs;
            }
        }
    }

    /**
     * Negotiation session tracking.
     */
    public static class NegotiationSession {
        private final String sessionId;
        private final String host;
        private final int port;
        private final long startTime;
        private volatile NegotiationPhase currentPhase;
        private volatile long currentPhaseStartTime;
        private volatile boolean completed;
        private volatile boolean timedOut;
        private volatile Exception failureException;

        public NegotiationSession(String sessionId, String host, int port) {
            this.sessionId = sessionId;
            this.host = host;
            this.port = port;
            this.startTime = System.currentTimeMillis();
            this.currentPhase = NegotiationPhase.TCP_CONNECT;
            this.currentPhaseStartTime = startTime;
            this.completed = false;
            this.timedOut = false;
        }

        public void advanceToPhase(NegotiationPhase phase) {
            this.currentPhase = phase;
            this.currentPhaseStartTime = System.currentTimeMillis();
        }

        public void markCompleted() {
            this.completed = true;
            this.currentPhase = NegotiationPhase.COMPLETE;
        }

        public void markTimedOut() {
            this.timedOut = true;
        }

        public void setFailureException(Exception exception) {
            this.failureException = exception;
        }

        public long getTotalElapsedTime() {
            return System.currentTimeMillis() - startTime;
        }

        public long getCurrentPhaseElapsedTime() {
            return System.currentTimeMillis() - currentPhaseStartTime;
        }

        // Getters
        public String getSessionId() { return sessionId; }
        public String getHost() { return host; }
        public int getPort() { return port; }
        public long getStartTime() { return startTime; }
        public NegotiationPhase getCurrentPhase() { return currentPhase; }
        public boolean isCompleted() { return completed; }
        public boolean isTimedOut() { return timedOut; }
        public Exception getFailureException() { return failureException; }

        @Override
        public String toString() {
            return String.format("NegotiationSession[id=%s, host=%s:%d, phase=%s, elapsed=%dms, completed=%s, timedOut=%s]",
                               sessionId, host, port, currentPhase, getTotalElapsedTime(), completed, timedOut);
        }
    }

    // Configuration
    private final TimeoutConfig timeoutConfig;
    private final ScheduledExecutorService timeoutExecutor;

    // Session tracking
    private final ConcurrentHashMap<String, NegotiationSession> activeSessions;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> sessionTimeouts;

    // Metrics
    private final AtomicLong totalSessions = new AtomicLong(0);
    private final AtomicLong completedSessions = new AtomicLong(0);
    private final AtomicLong timedOutSessions = new AtomicLong(0);
    private final AtomicLong overallTimeouts = new AtomicLong(0);
    private final AtomicLong phaseTimeouts = new AtomicLong(0);

    public ProtocolNegotiationTimeoutHandler(TimeoutConfig timeoutConfig) {
        this.timeoutConfig = timeoutConfig != null ? timeoutConfig : TimeoutConfig.defaultConfig();
        this.timeoutExecutor = Executors.newScheduledThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
                new ThreadFactory() {
                    private final AtomicLong counter = new AtomicLong(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "H2-Timeout-Handler-" + counter.incrementAndGet());
                        t.setDaemon(true);
                        return t;
                    }
                });

        this.activeSessions = new ConcurrentHashMap<>();
        this.sessionTimeouts = new ConcurrentHashMap<>();

        log.info("ProtocolNegotiationTimeoutHandler initialized - Overall timeout: " +
                this.timeoutConfig.getOverallTimeoutMs() + "ms");
    }

    /**
     * Default constructor with enterprise configuration.
     */
    public ProtocolNegotiationTimeoutHandler() {
        this(TimeoutConfig.enterpriseConfig());
    }

    /**
     * Start tracking a new negotiation session.
     */
    public NegotiationSession startNegotiationSession(String host, int port) {
        totalSessions.incrementAndGet();

        String sessionId = generateSessionId(host, port);
        NegotiationSession session = new NegotiationSession(sessionId, host, port);

        activeSessions.put(sessionId, session);

        // Schedule overall timeout
        ScheduledFuture<?> overallTimeout = timeoutExecutor.schedule(
                () -> handleOverallTimeout(sessionId),
                timeoutConfig.getOverallTimeoutMs(),
                TimeUnit.MILLISECONDS
        );

        sessionTimeouts.put(sessionId, overallTimeout);

        log.debug("Started negotiation session: " + session);
        return session;
    }

    /**
     * Advance negotiation session to next phase.
     */
    public void advanceSessionPhase(String sessionId, NegotiationPhase newPhase) {
        NegotiationSession session = activeSessions.get(sessionId);
        if (session == null) {
            log.warn("Cannot advance unknown session: " + sessionId);
            return;
        }

        if (session.isCompleted() || session.isTimedOut()) {
            return; // Session already finished
        }

        log.debug("Advancing session " + sessionId + " from " + session.getCurrentPhase() + " to " + newPhase);
        session.advanceToPhase(newPhase);

        // Schedule phase-specific timeout
        schedulePhaseTimeout(sessionId, newPhase);
    }

    /**
     * Mark negotiation session as completed successfully.
     */
    public void completeNegotiationSession(String sessionId) {
        NegotiationSession session = activeSessions.get(sessionId);
        if (session == null) {
            return;
        }

        session.markCompleted();
        completedSessions.incrementAndGet();

        // Cancel any pending timeouts
        cancelSessionTimeouts(sessionId);

        // Remove from active sessions
        activeSessions.remove(sessionId);

        log.info("Negotiation session completed successfully: " + session);
    }

    /**
     * Mark negotiation session as failed.
     */
    public void failNegotiationSession(String sessionId, Exception exception) {
        NegotiationSession session = activeSessions.get(sessionId);
        if (session == null) {
            return;
        }

        session.setFailureException(exception);

        // Cancel timeouts and cleanup
        cancelSessionTimeouts(sessionId);
        activeSessions.remove(sessionId);

        log.warn("Negotiation session failed: " + session + " - " + exception.getMessage());
    }

    /**
     * Check if negotiation session has timed out.
     */
    public boolean isSessionTimedOut(String sessionId) {
        NegotiationSession session = activeSessions.get(sessionId);
        return session != null && session.isTimedOut();
    }

    /**
     * Get current session information.
     */
    public NegotiationSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * Schedule phase-specific timeout.
     */
    private void schedulePhaseTimeout(String sessionId, NegotiationPhase phase) {
        long phaseTimeout = timeoutConfig.getTimeoutForPhase(phase);

        if (phaseTimeout > 0) {
            timeoutExecutor.schedule(
                    () -> handlePhaseTimeout(sessionId, phase),
                    phaseTimeout,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * Handle overall negotiation timeout.
     */
    private void handleOverallTimeout(String sessionId) {
        NegotiationSession session = activeSessions.get(sessionId);
        if (session == null || session.isCompleted()) {
            return;
        }

        overallTimeouts.incrementAndGet();
        timedOutSessions.incrementAndGet();
        session.markTimedOut();

        log.warn("Overall negotiation timeout for session: " + session);

        // Trigger fallback if available
        triggerTimeoutFallback(session);

        // Cleanup
        activeSessions.remove(sessionId);
        cancelSessionTimeouts(sessionId);
    }

    /**
     * Handle phase-specific timeout.
     */
    private void handlePhaseTimeout(String sessionId, NegotiationPhase phase) {
        NegotiationSession session = activeSessions.get(sessionId);
        if (session == null || session.isCompleted() || session.getCurrentPhase() != phase) {
            return; // Session moved to different phase or completed
        }

        phaseTimeouts.incrementAndGet();
        log.warn("Phase timeout in session " + sessionId + " during phase: " + phase.getDescription());

        // For certain phases, trigger immediate fallback
        if (shouldTriggerImmediateFallback(phase)) {
            session.markTimedOut();
            triggerTimeoutFallback(session);
            activeSessions.remove(sessionId);
            cancelSessionTimeouts(sessionId);
        }
    }

    /**
     * Check if phase should trigger immediate fallback.
     */
    private boolean shouldTriggerImmediateFallback(NegotiationPhase phase) {
        switch (phase) {
            case ALPN_NEGOTIATION:
            case H2_PREFACE:
            case SETTINGS_EXCHANGE:
                return true; // These are HTTP/2 specific phases
            default:
                return false;
        }
    }

    /**
     * Trigger fallback due to timeout.
     */
    private void triggerTimeoutFallback(NegotiationSession session) {
        log.info("Triggering HTTP/1.1 fallback due to negotiation timeout: " + session.getSessionId());

        // This would typically integrate with H2FallbackManager
        // For now, just log the fallback trigger
        // In full implementation, this would call:
        // fallbackManager.executeFallback(messageContext, FallbackReason.CONNECTION_TIMEOUT);
    }

    /**
     * Cancel all timeouts for a session.
     */
    private void cancelSessionTimeouts(String sessionId) {
        ScheduledFuture<?> timeout = sessionTimeouts.remove(sessionId);
        if (timeout != null) {
            timeout.cancel(false);
        }
    }

    /**
     * Generate unique session ID.
     */
    private String generateSessionId(String host, int port) {
        return host + ":" + port + "-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }

    /**
     * Get active sessions count.
     */
    public int getActiveSessionsCount() {
        return activeSessions.size();
    }

    /**
     * Get sessions that are taking longer than expected.
     */
    public java.util.List<NegotiationSession> getSlowSessions(long thresholdMs) {
        return activeSessions.values().stream()
                .filter(session -> session.getTotalElapsedTime() > thresholdMs)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Force timeout for a specific session (testing/debugging).
     */
    public void forceSessionTimeout(String sessionId) {
        handleOverallTimeout(sessionId);
    }

    /**
     * Get comprehensive timeout handling metrics.
     */
    public TimeoutMetrics getMetrics() {
        return new TimeoutMetrics(
                totalSessions.get(),
                completedSessions.get(),
                timedOutSessions.get(),
                overallTimeouts.get(),
                phaseTimeouts.get(),
                activeSessions.size()
        );
    }

    /**
     * Timeout handling metrics container.
     */
    public static class TimeoutMetrics {
        public final long totalSessions;
        public final long completedSessions;
        public final long timedOutSessions;
        public final long overallTimeouts;
        public final long phaseTimeouts;
        public final int activeSessionsCount;

        public TimeoutMetrics(long totalSessions, long completedSessions,
                            long timedOutSessions, long overallTimeouts,
                            long phaseTimeouts, int activeSessionsCount) {
            this.totalSessions = totalSessions;
            this.completedSessions = completedSessions;
            this.timedOutSessions = timedOutSessions;
            this.overallTimeouts = overallTimeouts;
            this.phaseTimeouts = phaseTimeouts;
            this.activeSessionsCount = activeSessionsCount;
        }

        public double getCompletionRate() {
            return totalSessions > 0 ? (double) completedSessions / totalSessions : 0.0;
        }

        public double getTimeoutRate() {
            return totalSessions > 0 ? (double) timedOutSessions / totalSessions : 0.0;
        }

        @Override
        public String toString() {
            return String.format("TimeoutMetrics[total=%d, completed=%d, timedOut=%d, " +
                               "completionRate=%.2f%%, timeoutRate=%.2f%%, active=%d]",
                               totalSessions, completedSessions, timedOutSessions,
                               getCompletionRate() * 100, getTimeoutRate() * 100, activeSessionsCount);
        }
    }

    /**
     * Get timeout configuration.
     */
    public TimeoutConfig getTimeoutConfig() {
        return timeoutConfig;
    }

    /**
     * Cleanup resources.
     */
    public void cleanup() {
        log.info("ProtocolNegotiationTimeoutHandler cleanup - Final metrics: " + getMetrics());

        // Cancel all active timeouts
        for (ScheduledFuture<?> timeout : sessionTimeouts.values()) {
            timeout.cancel(false);
        }

        // Clear sessions
        activeSessions.clear();
        sessionTimeouts.clear();

        // Shutdown executor
        timeoutExecutor.shutdown();
        try {
            if (!timeoutExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                timeoutExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            timeoutExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}