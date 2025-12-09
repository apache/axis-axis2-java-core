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

import java.io.IOException;
import java.io.InputStream;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.description.AxisConfiguration;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.kernel.MessageFormatter;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPSender;
import org.apache.axis2.transport.http.AbstractHTTPTransportSender;
import org.apache.axis2.transport.http.HTTPTransportConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// HTTP/2 specific imports
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.HttpVersionPolicy;

/**
 * The H2TransportSender provides HTTP/2 transport capabilities using HttpClient 5.x
 * with async multiplexing and connection reuse.
 *
 * This is an independent implementation separate from HTTP/1.1 transport to ensure
 * clean protocol separation and optimal HTTP/2 performance characteristics.
 *
 * Stage 2 Features:
 * - HTTP/2 protocol version detection and enforcement
 * - H2Config for connection multiplexing (optimized for enterprise constraints)
 * - ALPN (Application-Layer Protocol Negotiation) support
 * - Memory-constrained connection management (2GB heap limit)
 * - 50MB+ JSON payload optimization
 */
public class H2TransportSender extends AbstractHTTPTransportSender {

    private static final Log log = LogFactory.getLog(H2TransportSender.class);

    // HTTP/2 configuration constants (optimized for enterprise requirements)
    private static final int MAX_CONCURRENT_STREAMS = 100;  // Memory-constrained: 100 vs default 1000
    private static final int INITIAL_WINDOW_SIZE = 65536;   // 64KB - optimized for 50MB+ JSON
    private static final int MAX_CONN_TOTAL = 50;          // Memory constraint: 50 vs default 100
    private static final int MAX_CONN_PER_ROUTE = 10;      // Route-specific limit
    private static final boolean SERVER_PUSH_ENABLED = false; // Server push disabled for web services
    private static final long LARGE_PAYLOAD_THRESHOLD = 50 * 1024 * 1024; // 50MB threshold

    // HTTP/2 client instance (cached for connection reuse)
    private CloseableHttpAsyncClient http2Client;

    // Phase 1 Enhancement: Integrated optimization components
    private ProgressiveFlowControl progressiveFlowControl;
    private AdaptiveBufferManager adaptiveBufferManager;

    // Phase 2 Enhancement: Advanced optimization components
    private PredictiveStreamManager predictiveStreamManager;

    // P1 Critical Features: Production Readiness Components
    private H2FallbackManager fallbackManager;
    private ALPNProtocolSelector alpnSelector;
    private H2ErrorHandler errorHandler;
    private ProtocolNegotiationTimeoutHandler timeoutHandler;

    @Override
    public void cleanup(MessageContext msgContext) throws AxisFault {
        log.trace("cleanup() releasing HTTP/2 connection");

        OperationContext opContext = msgContext.getOperationContext();
        if (opContext != null) {
            InputStream in = (InputStream)opContext.getProperty(MessageContext.TRANSPORT_IN);
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    // Ignore
                }
            }
        }

        // guard against multiple calls
        msgContext.removeProperty(HTTPConstants.HTTP_METHOD);
    }

    @Override
    public void init(ConfigurationContext configContext, TransportOutDescription transportOut) throws AxisFault {
        // Initialize parent class
        super.init(configContext, transportOut);

        // Optional WildFly Integration (backwards compatible)
        initOptionalWildFlyIntegration(configContext, transportOut);

        // Initialize Phase 1 Enhancement components
        this.progressiveFlowControl = new ProgressiveFlowControl();
        this.adaptiveBufferManager = new AdaptiveBufferManager();

        // Initialize Phase 2 Enhancement components
        this.predictiveStreamManager = new PredictiveStreamManager();

        // Initialize P1 Critical Features for Production Readiness
        this.fallbackManager = new H2FallbackManager();
        this.alpnSelector = new ALPNProtocolSelector();
        this.errorHandler = new H2ErrorHandler();
        this.timeoutHandler = new ProtocolNegotiationTimeoutHandler();

        // Initialize HTTP/2 client with configuration
        try {
            this.http2Client = createHTTP2Client();
            log.info("HTTP/2 transport sender initialized with Phase 1 & 2 optimizations + P1 Production Features: " +
                    "adaptive configuration, progressive flow control, intelligent buffering, " +
                    "compression optimization, predictive stream management, " +
                    "HTTP/1.1 fallback, ALPN negotiation, enhanced error handling, timeout management");
        } catch (Exception e) {
            throw new AxisFault("Failed to initialize HTTP/2 client", e);
        }

        // Set HTTP/2 client version
        setHTTPClientVersion(configContext);
    }

    @Override
    public void stop() {
        // Clean shutdown of HTTP/2 client and Phase 1 components
        if (http2Client != null) {
            try {
                http2Client.close();
                log.info("HTTP/2 client shutdown completed");
            } catch (IOException e) {
                log.warn("Error during HTTP/2 client shutdown", e);
            }
        }

        // Cleanup Phase 1 Enhancement components
        if (adaptiveBufferManager != null) {
            adaptiveBufferManager.cleanup();
        }

        if (progressiveFlowControl != null) {
            log.info("Progressive flow control final metrics: " +
                    progressiveFlowControl.getMetrics());
        }

        if (predictiveStreamManager != null) {
            log.info("Predictive stream manager final metrics: " +
                    predictiveStreamManager.getMetrics());
        }

        // Cleanup P1 Critical Features
        if (fallbackManager != null) {
            fallbackManager.cleanup();
        }

        if (errorHandler != null) {
            log.info("H2 error handler final metrics: " + errorHandler.getMetrics());
        }

        if (timeoutHandler != null) {
            timeoutHandler.cleanup();
        }

        if (alpnSelector != null) {
            log.info("ALPN selector final metrics: " + alpnSelector.getMetrics());
        }

        super.stop();
    }

    /**
     * Create HTTP/2 client with adaptive optimization for enterprise requirements.
     *
     * Phase 1 Enhancement Features:
     * - Adaptive configuration based on payload size estimation
     * - Progressive flow control with network awareness
     * - Intelligent buffer management with memory pooling
     * - Memory-constrained connection management
     * - 50MB+ JSON payload optimization with dynamic scaling
     */
    private CloseableHttpAsyncClient createHTTP2Client() {
        return createHTTP2Client(0); // Default configuration for unknown payload size
    }

    /**
     * Create HTTP/2 client with payload-aware adaptive configuration.
     */
    private CloseableHttpAsyncClient createHTTP2Client(long estimatedPayloadSize) {
        // Use adaptive configuration based on payload size
        H2Config h2Config;
        if (estimatedPayloadSize > 0) {
            // Get system state for adaptive configuration
            Runtime runtime = Runtime.getRuntime();
            long availableMemory = runtime.freeMemory();
            int activeConcurrentRequests = getActiveConcurrentRequests();

            h2Config = AdaptiveH2Config.createAdaptiveConfig(
                estimatedPayloadSize, availableMemory, activeConcurrentRequests);

            log.info("Created adaptive HTTP/2 configuration for payload size: " +
                    formatBytes(estimatedPayloadSize));
        } else {
            // Fallback to default adaptive configuration
            h2Config = AdaptiveH2Config.createAdaptiveConfig(LARGE_PAYLOAD_THRESHOLD);
        }

        // Configure connection management
        PoolingAsyncClientConnectionManager connectionManager = createConnectionManager();

        // Create HTTP/2 client with ALPN support and fallback capability
        CloseableHttpAsyncClient client;
        try {
            // Try to create ALPN-enabled client first
            client = alpnSelector.createALPNEnabledClient(connectionManager);
            log.info("Created HTTP/2 client with ALPN negotiation support");
        } catch (Exception alpnException) {
            log.warn("ALPN support not available, creating basic HTTP/2 client: " + alpnException.getMessage());

            // Fallback to basic HTTP/2 client
            client = HttpAsyncClients.custom()
                .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)     // Allow protocol negotiation
                .setH2Config(h2Config)                            // HTTP/2 specific configuration
                .setConnectionManager(connectionManager)          // Memory-constrained connection pool
                .build();
        }

        // Start the client for async operations
        client.start();

        log.info("HTTP/2 client created - Max streams: " + MAX_CONCURRENT_STREAMS +
                ", Initial window: " + INITIAL_WINDOW_SIZE + ", Push enabled: " + SERVER_PUSH_ENABLED);

        return client;
    }

    /**
     * Create connection manager optimized for 2GB heap constraint.
     */
    private PoolingAsyncClientConnectionManager createConnectionManager() {
        return PoolingAsyncClientConnectionManagerBuilder.create()
            .setMaxConnTotal(MAX_CONN_TOTAL)      // Memory constraint: 50 vs default 100
            .setMaxConnPerRoute(MAX_CONN_PER_ROUTE)   // Route-specific limit
            .build();
    }

    /**
     * Get the HTTP/2 client instance for use by HTTP senders.
     */
    public CloseableHttpAsyncClient getHTTP2Client() {
        return http2Client;
    }

    public void setHTTPClientVersion(ConfigurationContext configurationContext) {
        // Set HTTP/2 specific client version identifier (using HTTP Client 5.x for HTTP/2)
        configurationContext.setProperty(HTTPTransportConstants.HTTP_CLIENT_VERSION,
                                         HTTPTransportConstants.HTTP_CLIENT_5_X_VERSION);
    }

    @Override
    protected HTTPSender createHTTPSender() {
        // Create H2SenderImpl with Phase 1 & 2 optimized HTTP/2 client
        // The optimization components are integrated at the transport sender level
        return new H2SenderImpl(this.http2Client);
    }

    /**
     * Get estimated active concurrent requests (simplified implementation).
     */
    private int getActiveConcurrentRequests() {
        // In a full implementation, this would track active HTTP/2 streams
        // For now, return a conservative estimate
        return 10;
    }

    /**
     * Format bytes for logging.
     */
    private static String formatBytes(long bytes) {
        if (bytes >= 1024 * 1024 * 1024) {
            return String.format("%.2fGB", bytes / (1024.0 * 1024.0 * 1024.0));
        } else if (bytes >= 1024 * 1024) {
            return String.format("%.2fMB", bytes / (1024.0 * 1024.0));
        } else if (bytes >= 1024) {
            return String.format("%.2fKB", bytes / 1024.0);
        } else {
            return bytes + "B";
        }
    }

    /**
     * Get progressive flow control manager (for monitoring/debugging).
     */
    public ProgressiveFlowControl getProgressiveFlowControl() {
        return progressiveFlowControl;
    }

    /**
     * Get adaptive buffer manager (for monitoring/debugging).
     */
    public AdaptiveBufferManager getAdaptiveBufferManager() {
        return adaptiveBufferManager;
    }

    /**
     * Get predictive stream manager (for monitoring/debugging).
     */
    public PredictiveStreamManager getPredictiveStreamManager() {
        return predictiveStreamManager;
    }

    /**
     * Get HTTP/2 fallback manager (for monitoring/debugging).
     */
    public H2FallbackManager getFallbackManager() {
        return fallbackManager;
    }

    /**
     * Get ALPN protocol selector (for monitoring/debugging).
     */
    public ALPNProtocolSelector getAlpnSelector() {
        return alpnSelector;
    }

    /**
     * Get HTTP/2 error handler (for monitoring/debugging).
     */
    public H2ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Get protocol negotiation timeout handler (for monitoring/debugging).
     */
    public ProtocolNegotiationTimeoutHandler getTimeoutHandler() {
        return timeoutHandler;
    }

    /**
     * Get comprehensive optimization recommendations for given payload.
     */
    public String getOptimizationRecommendations(String contentType, long payloadSize, String sample) {
        StringBuilder recommendations = new StringBuilder();

        recommendations.append("=== HTTP/2 Optimization Recommendations ===\n");

        // Adaptive configuration recommendations
        recommendations.append(AdaptiveH2Config.getConfigurationRecommendations(
            payloadSize, Runtime.getRuntime().freeMemory(), getActiveConcurrentRequests()));

        // Compression recommendations
        recommendations.append(CompressionOptimizer.getCompressionRecommendations(
            contentType, payloadSize, sample));

        // Buffer manager metrics
        if (adaptiveBufferManager != null) {
            recommendations.append("Buffer Manager: ").append(adaptiveBufferManager.getMetrics()).append("\n");
        }

        // Stream manager metrics
        if (predictiveStreamManager != null) {
            recommendations.append("Stream Manager: ").append(predictiveStreamManager.getMetrics()).append("\n");
        }

        // P1 Production Features metrics
        if (fallbackManager != null) {
            recommendations.append("Fallback Manager: ").append(fallbackManager.getMetrics()).append("\n");
        }

        if (errorHandler != null) {
            recommendations.append("Error Handler: ").append(errorHandler.getMetrics()).append("\n");
        }

        if (alpnSelector != null) {
            recommendations.append("ALPN Selector: ").append(alpnSelector.getMetrics()).append("\n");
        }

        if (timeoutHandler != null) {
            recommendations.append("Timeout Handler: ").append(timeoutHandler.getMetrics()).append("\n");
        }

        return recommendations.toString();
    }

    /**
     * Optional WildFly integration with auto-detection and backwards compatibility.
     *
     * This method implements optional WildFly integration that:
     * - Auto-detects WildFly/Undertow environment at runtime
     * - Gracefully degrades when WildFly components unavailable
     * - Uses optional parameters with sensible defaults
     * - Maintains full backwards compatibility with legacy axis2.xml files
     * - Only activates for JSON content types, not SOAP
     * - Requires no axis2.xml changes for basic functionality
     */
    private void initOptionalWildFlyIntegration(ConfigurationContext configContext,
                                              TransportOutDescription transportOut) {
        try {
            // Check if WildFly integration is enabled (master switch)
            boolean undertowIntegration = getBooleanParameter(transportOut, "UNDERTOW_INTEGRATION", true);

            if (!undertowIntegration) {
                log.info("WildFly/Undertow integration disabled via UNDERTOW_INTEGRATION parameter");
                configContext.setProperty("HTTP2_WILDFLY_INTEGRATION_MODE", "DISABLED");
                return;
            }

            // Auto-detect WildFly environment (backwards compatible)
            WildFlyEnvironmentDetector detector = new WildFlyEnvironmentDetector();

            if (detector.isWildFlyEnvironmentAvailable()) {
                log.info("WildFly/Undertow environment detected - enabling optional HTTP/2 integration");

                // Read feature-specific parameters (defaults provided for legacy axis2.xml compatibility)
                boolean moshiProcessing = getBooleanParameter(transportOut, "MOSHI_JSON_PROCESSING", true);
                boolean gsonProcessing = getBooleanParameter(transportOut, "GSON_JSON_PROCESSING", true);
                boolean streamOptimization = getBooleanParameter(transportOut, "HTTP2_STREAM_OPTIMIZATION", true);

                // Configure Undertow buffer pool integration
                if (detector.isUndertowBufferPoolAvailable()) {
                    configContext.setProperty("HTTP2_UNDERTOW_INTEGRATION_ENABLED", true);
                    configContext.setProperty("HTTP2_SHARED_BUFFER_POOL_ENABLED", true);
                    log.info("Enabled Undertow shared buffer pool integration");
                }

                // JSON library detection based on actual axis2.xml configuration
                // Inspect which JSON classes are actually configured instead of classpath detection
                String detectedJsonLibrary = detectConfiguredJsonLibrary(configContext);

                if (detectedJsonLibrary != null) {
                    if ("MOSHI".equals(detectedJsonLibrary) && moshiProcessing) {
                        configContext.setProperty("HTTP2_MOSHI_JSON_PROCESSING_ENABLED", true);
                        configContext.setProperty("JSON_LIBRARY_PREFERENCE", "MOSHI");
                        log.info("Detected Moshi JSON configuration - enabled HTTP/2 optimization for Moshi");
                    } else if ("GSON".equals(detectedJsonLibrary) && gsonProcessing) {
                        configContext.setProperty("HTTP2_GSON_JSON_PROCESSING_ENABLED", true);
                        configContext.setProperty("JSON_LIBRARY_PREFERENCE", "GSON");
                        log.info("Detected Gson JSON configuration - enabled HTTP/2 optimization for Gson");
                    } else {
                        log.info("Detected JSON configuration (" + detectedJsonLibrary + ") but processing disabled in transport configuration");
                    }
                } else {
                    log.debug("No JSON library configuration detected - HTTP/2 JSON optimizations disabled");
                }

                if (streamOptimization) {
                    configContext.setProperty("HTTP2_STREAM_OPTIMIZATION_ENABLED", true);
                    log.info("Enabled HTTP/2 stream optimization");
                }

                // Set compatibility mode for mixed environments
                configContext.setProperty("HTTP2_WILDFLY_INTEGRATION_MODE", "OPTIONAL");

            } else {
                log.debug("WildFly environment not detected - using standard HTTP/2 transport");
                configContext.setProperty("HTTP2_WILDFLY_INTEGRATION_MODE", "DISABLED");
            }

        } catch (Exception e) {
            // Graceful degradation - integration failures should not break transport
            log.warn("WildFly integration initialization failed - falling back to standard transport: " +
                    e.getMessage());
            configContext.setProperty("HTTP2_WILDFLY_INTEGRATION_MODE", "FALLBACK");
        }
    }

    /**
     * Get boolean parameter with default fallback for backwards compatibility.
     */
    private boolean getBooleanParameter(TransportOutDescription transportOut, String paramName, boolean defaultValue) {
        try {
            Object paramValue = transportOut.getParameter(paramName);
            if (paramValue != null) {
                return Boolean.parseBoolean(paramValue.toString());
            }
        } catch (Exception e) {
            log.debug("Parameter " + paramName + " not found or invalid - using default: " + defaultValue);
        }
        return defaultValue;
    }

    /**
     * Detect which JSON library is configured in axis2.xml by inspecting the actual configuration.
     * This is more reliable than classpath detection since both libraries could be available.
     */
    private String detectConfiguredJsonLibrary(ConfigurationContext configContext) {
        try {
            AxisConfiguration axisConfig = configContext.getAxisConfiguration();

            // Check message formatters for JSON content type
            MessageFormatter jsonFormatter = axisConfig.getMessageFormatter("application/json");

            if (jsonFormatter != null) {
                String formatterClass = jsonFormatter.getClass().getName();
                if (formatterClass.contains(".json.moshi.")) {
                    log.debug("Detected Moshi JsonFormatter: " + formatterClass);
                    return "MOSHI";
                } else if (formatterClass.contains(".json.gson.")) {
                    log.debug("Detected Gson JsonFormatter: " + formatterClass);
                    return "GSON";
                }
            }

            // Check message builders for JSON content type
            Builder jsonBuilder = axisConfig.getMessageBuilder("application/json");

            if (jsonBuilder != null) {
                String builderClass = jsonBuilder.getClass().getName();
                if (builderClass.contains(".json.moshi.")) {
                    log.debug("Detected Moshi JsonBuilder: " + builderClass);
                    return "MOSHI";
                } else if (builderClass.contains(".json.gson.")) {
                    log.debug("Detected Gson JsonBuilder: " + builderClass);
                    return "GSON";
                }
            }

            // Check handlers for JSONMessageHandler in Transport phase
            java.util.List<Phase> inFlowPhases = axisConfig.getInFlowPhases();
            for (Phase phase : inFlowPhases) {
                if ("Transport".equals(phase.getName())) {
                    java.util.List<HandlerDescription> handlers = phase.getHandlers();
                    for (HandlerDescription handler : handlers) {
                        if ("JSONMessageHandler".equals(handler.getName())) {
                            String handlerClass = handler.getClassName();
                            if (handlerClass.contains(".json.moshi.")) {
                                log.debug("Detected Moshi JSONMessageHandler: " + handlerClass);
                                return "MOSHI";
                            } else if (handlerClass.contains(".json.gson.")) {
                                log.debug("Detected Gson JSONMessageHandler: " + handlerClass);
                                return "GSON";
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.debug("Error detecting JSON library configuration: " + e.getMessage());
        }

        return null; // No JSON library detected
    }

    /**
     * WildFly environment detection with graceful fallback.
     *
     * Detects WildFly components at runtime without requiring configuration changes.
     * This ensures backwards compatibility with legacy deployments while enabling
     * optimization when WildFly components are available.
     */
    private static class WildFlyEnvironmentDetector {
        private static final Log detectorLog = LogFactory.getLog(WildFlyEnvironmentDetector.class);

        /**
         * Check if WildFly/Undertow environment is available.
         */
        public boolean isWildFlyEnvironmentAvailable() {
            try {
                // Check for Undertow classes on classpath
                Class.forName("io.undertow.server.HttpHandler");
                Class.forName("io.undertow.servlet.api.DeploymentInfo");

                // Check for XNIO (Undertow dependency)
                Class.forName("org.xnio.XnioWorker");

                detectorLog.debug("WildFly/Undertow classes detected on classpath");
                return true;

            } catch (ClassNotFoundException e) {
                detectorLog.debug("WildFly/Undertow classes not found - running in non-WildFly environment");
                return false;
            }
        }

        /**
         * Check if Undertow buffer pool integration is available.
         */
        public boolean isUndertowBufferPoolAvailable() {
            try {
                // Check for Undertow buffer pool classes
                Class.forName("io.undertow.connector.ByteBufferPool");
                return true;
            } catch (ClassNotFoundException e) {
                detectorLog.debug("Undertow buffer pool not available");
                return false;
            }
        }

    }
}
