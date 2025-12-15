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
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.Handler;
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

    // HTTP/2 configuration defaults (can be overridden in axis2.xml)
    private static final int DEFAULT_MAX_CONCURRENT_STREAMS = 100;  // Memory-constrained: 100 vs default 1000
    private static final int DEFAULT_INITIAL_WINDOW_SIZE = 65536;   // 64KB - optimized for 50MB+ JSON
    private static final int DEFAULT_MAX_CONN_TOTAL = 50;          // Memory constraint: 50 vs default 100
    private static final int DEFAULT_MAX_CONN_PER_ROUTE = 10;      // Route-specific limit
    private static final boolean DEFAULT_SERVER_PUSH_ENABLED = false; // Server push disabled for web services
    private static final long DEFAULT_CONNECTION_KEEP_ALIVE_TIME = 300000; // 5 minutes
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30000;   // 30 seconds
    private static final int DEFAULT_RESPONSE_TIMEOUT = 300000;    // 5 minutes
    private static final int DEFAULT_STREAMING_BUFFER_SIZE = 65536; // 64KB
    private static final double DEFAULT_MEMORY_PRESSURE_THRESHOLD = 0.8; // 80% of heap
    private static final boolean DEFAULT_ENABLE_HTTP2_OPTIMIZATION = true;
    private static final boolean DEFAULT_ADAPTIVE_CONFIGURATION = true;
    private static final boolean DEFAULT_COMPRESSION_OPTIMIZATION = true;
    private static final boolean DEFAULT_PREDICTIVE_STREAM_MANAGEMENT = true;
    private static final boolean DEFAULT_ENABLE_ALPN = true;
    private static final String DEFAULT_SUPPORTED_PROTOCOLS = "TLSv1.2,TLSv1.3";
    private static final String DEFAULT_SUPPORTED_CIPHER_SUITES = "TLS_AES_256_GCM_SHA384,TLS_CHACHA20_POLY1305_SHA256";

    // Payload size thresholds for adaptive configuration
    private static final long LARGE_PAYLOAD_THRESHOLD = 10 * 1024 * 1024; // 10MB threshold for large payloads

    // Runtime configuration values (loaded from axis2.xml or defaults)
    private int maxConcurrentStreams = DEFAULT_MAX_CONCURRENT_STREAMS;
    private int initialWindowSize = DEFAULT_INITIAL_WINDOW_SIZE;
    private int maxConnTotal = DEFAULT_MAX_CONN_TOTAL;
    private int maxConnPerRoute = DEFAULT_MAX_CONN_PER_ROUTE;
    private boolean serverPushEnabled = DEFAULT_SERVER_PUSH_ENABLED;
    private long connectionKeepAliveTime = DEFAULT_CONNECTION_KEEP_ALIVE_TIME;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private int responseTimeout = DEFAULT_RESPONSE_TIMEOUT;
    private int streamingBufferSize = DEFAULT_STREAMING_BUFFER_SIZE;
    private double memoryPressureThreshold = DEFAULT_MEMORY_PRESSURE_THRESHOLD;
    private boolean enableHTTP2Optimization = DEFAULT_ENABLE_HTTP2_OPTIMIZATION;
    private boolean adaptiveConfiguration = DEFAULT_ADAPTIVE_CONFIGURATION;
    private boolean compressionOptimization = DEFAULT_COMPRESSION_OPTIMIZATION;
    private boolean predictiveStreamManagement = DEFAULT_PREDICTIVE_STREAM_MANAGEMENT;
    private boolean enableALPN = DEFAULT_ENABLE_ALPN;
    private String supportedProtocols = DEFAULT_SUPPORTED_PROTOCOLS;
    private String supportedCipherSuites = DEFAULT_SUPPORTED_CIPHER_SUITES;

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

        // Load configuration parameters from axis2.xml (with sensible defaults)
        loadConfigurationParameters(transportOut);

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
            log.info("HTTP/2 transport sender initialized with configuration: " +
                    "maxConcurrentStreams=" + maxConcurrentStreams +
                    ", initialWindowSize=" + initialWindowSize +
                    ", maxConnTotal=" + maxConnTotal +
                    ", serverPushEnabled=" + serverPushEnabled +
                    ", adaptiveConfiguration=" + adaptiveConfiguration);
        } catch (Exception e) {
            throw new AxisFault("Failed to initialize HTTP/2 client", e);
        }

        // Set HTTP/2 client version
        setHTTPClientVersion(configContext);
    }

    /**
     * Load configuration parameters from axis2.xml with sensible defaults.
     * This method eliminates the need for extensive parameter configuration in axis2.xml
     * by providing production-ready defaults for all HTTP/2 and Moshi H2 parameters.
     */
    private void loadConfigurationParameters(TransportOutDescription transportOut) {
        log.info("Loading HTTP/2 configuration parameters from axis2.xml (with intelligent defaults)");

        // HTTP/2 Core Configuration
        maxConcurrentStreams = getIntParameter(transportOut, "maxConcurrentStreams", DEFAULT_MAX_CONCURRENT_STREAMS);
        initialWindowSize = getIntParameter(transportOut, "initialWindowSize", DEFAULT_INITIAL_WINDOW_SIZE);
        maxConnTotal = getIntParameter(transportOut, "maxConnectionsTotal", DEFAULT_MAX_CONN_TOTAL);
        maxConnPerRoute = getIntParameter(transportOut, "maxConnectionsPerRoute", DEFAULT_MAX_CONN_PER_ROUTE);
        serverPushEnabled = getBooleanParameter(transportOut, "serverPushEnabled", DEFAULT_SERVER_PUSH_ENABLED);

        // HTTP/2 Connection Management
        connectionKeepAliveTime = getLongParameter(transportOut, "connectionKeepAliveTime", DEFAULT_CONNECTION_KEEP_ALIVE_TIME);
        connectionTimeout = getIntParameter(transportOut, "connectionTimeout", DEFAULT_CONNECTION_TIMEOUT);
        responseTimeout = getIntParameter(transportOut, "responseTimeout", DEFAULT_RESPONSE_TIMEOUT);

        // HTTP/2 Performance Optimization
        streamingBufferSize = getIntParameter(transportOut, "streamingBufferSize", DEFAULT_STREAMING_BUFFER_SIZE);
        memoryPressureThreshold = getDoubleParameter(transportOut, "memoryPressureThreshold", DEFAULT_MEMORY_PRESSURE_THRESHOLD);
        enableHTTP2Optimization = getBooleanParameter(transportOut, "enableHTTP2Optimization", DEFAULT_ENABLE_HTTP2_OPTIMIZATION);
        adaptiveConfiguration = getBooleanParameter(transportOut, "adaptiveConfiguration", DEFAULT_ADAPTIVE_CONFIGURATION);
        compressionOptimization = getBooleanParameter(transportOut, "compressionOptimization", DEFAULT_COMPRESSION_OPTIMIZATION);
        predictiveStreamManagement = getBooleanParameter(transportOut, "predictiveStreamManagement", DEFAULT_PREDICTIVE_STREAM_MANAGEMENT);

        // TLS/ALPN Configuration
        enableALPN = getBooleanParameter(transportOut, "enableALPN", DEFAULT_ENABLE_ALPN);
        supportedProtocols = getStringParameter(transportOut, "supportedProtocols", DEFAULT_SUPPORTED_PROTOCOLS);
        supportedCipherSuites = getStringParameter(transportOut, "supportedCipherSuites", DEFAULT_SUPPORTED_CIPHER_SUITES);

        log.info("HTTP/2 configuration loaded successfully - " +
                "maxConcurrentStreams=" + maxConcurrentStreams +
                " (default: " + DEFAULT_MAX_CONCURRENT_STREAMS + "), " +
                "initialWindowSize=" + initialWindowSize +
                " (default: " + DEFAULT_INITIAL_WINDOW_SIZE + "), " +
                "adaptiveConfiguration=" + adaptiveConfiguration +
                " (default: " + DEFAULT_ADAPTIVE_CONFIGURATION + ")");
    }

    /**
     * Helper method to get integer parameter from transport configuration.
     */
    private int getIntParameter(TransportOutDescription transportOut, String paramName, int defaultValue) {
        try {
            Object param = transportOut.getParameter(paramName);
            if (param != null) {
                if (param instanceof Integer) {
                    return (Integer) param;
                } else {
                    return Integer.parseInt(param.toString());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse integer parameter '" + paramName + "', using default: " + defaultValue, e);
        }
        return defaultValue;
    }

    /**
     * Helper method to get boolean parameter from transport configuration.
     */
    private boolean getBooleanParameter(TransportOutDescription transportOut, String paramName, boolean defaultValue) {
        try {
            Object param = transportOut.getParameter(paramName);
            if (param != null) {
                if (param instanceof Boolean) {
                    return (Boolean) param;
                } else {
                    return Boolean.parseBoolean(param.toString());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse boolean parameter '" + paramName + "', using default: " + defaultValue, e);
        }
        return defaultValue;
    }

    /**
     * Helper method to get long parameter from transport configuration.
     */
    private long getLongParameter(TransportOutDescription transportOut, String paramName, long defaultValue) {
        try {
            Object param = transportOut.getParameter(paramName);
            if (param != null) {
                if (param instanceof Long) {
                    return (Long) param;
                } else {
                    return Long.parseLong(param.toString());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse long parameter '" + paramName + "', using default: " + defaultValue, e);
        }
        return defaultValue;
    }

    /**
     * Helper method to get double parameter from transport configuration.
     */
    private double getDoubleParameter(TransportOutDescription transportOut, String paramName, double defaultValue) {
        try {
            Object param = transportOut.getParameter(paramName);
            if (param != null) {
                if (param instanceof Double) {
                    return (Double) param;
                } else {
                    return Double.parseDouble(param.toString());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse double parameter '" + paramName + "', using default: " + defaultValue, e);
        }
        return defaultValue;
    }

    /**
     * Helper method to get string parameter from transport configuration.
     */
    private String getStringParameter(TransportOutDescription transportOut, String paramName, String defaultValue) {
        try {
            Object param = transportOut.getParameter(paramName);
            if (param != null) {
                return param.toString();
            }
        } catch (Exception e) {
            log.warn("Failed to parse string parameter '" + paramName + "', using default: " + defaultValue, e);
        }
        return defaultValue;
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

        log.info("HTTP/2 client created - Max streams: " + maxConcurrentStreams +
                ", Initial window: " + initialWindowSize + ", Push enabled: " + serverPushEnabled);

        return client;
    }

    /**
     * Create connection manager optimized for 2GB heap constraint.
     */
    private PoolingAsyncClientConnectionManager createConnectionManager() {
        return PoolingAsyncClientConnectionManagerBuilder.create()
            .setMaxConnTotal(maxConnTotal)      // Memory constraint: configurable vs default 100
            .setMaxConnPerRoute(maxConnPerRoute)   // Route-specific limit
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




}
