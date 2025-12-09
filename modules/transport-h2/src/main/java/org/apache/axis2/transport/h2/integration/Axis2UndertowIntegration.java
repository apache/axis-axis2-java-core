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

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Import Moshi-specific HTTP/2 integration classes
import org.apache.axis2.transport.h2.integration.moshi.Axis2HTTP2Handler;
import org.apache.axis2.transport.h2.integration.moshi.UndertowAxis2BufferIntegration;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.handlers.ServletRequestContext;
import io.undertow.util.Headers;

/**
 * Configure WildFly Undertow to use Moshi-optimized Axis2 HTTP/2 handler integration.
 *
 * This ServletContextListener implements Phase 2 of the WildFly 32 + Axis2 HTTP/2
 * Cooperative Integration Plan by configuring direct Undertow handler integration
 * with Moshi JSON processing optimization. It registers the Moshi-specific
 * Axis2HTTP2Handler in WildFly's handler chain for optimized HTTP/2 request processing.
 *
 * Key features:
 * - Automatic detection and configuration of Undertow deployment
 * - Registration of Moshi-optimized Axis2HTTP2Handler in WildFly handler chain
 * - Moshi-specific buffer integration with shared memory coordination
 * - Integration with existing Axis2 servlet configuration
 * - Graceful fallback to servlet processing when integration unavailable
 * - Comprehensive logging and error handling
 * - Performance-optimized JSON processing using Moshi library
 */
@WebListener
public class Axis2UndertowIntegration implements ServletContextListener {
    private static final Log log = LogFactory.getLog(Axis2UndertowIntegration.class);

    // WildFly/Undertow specific attribute keys
    private static final String DEPLOYMENT_INFO_KEY = "io.undertow.servlet.deploymentInfo";
    private static final String UNDERTOW_HANDLER_CHAIN_KEY = "io.undertow.servlet.initialHandlerChainWrappers";

    // Axis2 configuration integration
    private HTTP2MemoryCoordinator memoryCoordinator;
    private Axis2HTTP2Handler axis2Handler;
    private UndertowAxis2BufferIntegration bufferIntegration;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        log.info("Initializing Axis2 Undertow HTTP/2 integration");

        try {
            // Initialize memory coordination
            initializeMemoryCoordination(servletContext);

            // Initialize buffer integration
            initializeBufferIntegration(servletContext);

            // Configure Undertow handler integration
            configureUndertowHandlerIntegration(servletContext);

            log.info("Axis2 Undertow HTTP/2 integration initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize Axis2 Undertow integration - falling back to servlet processing", e);
            // Continue without HTTP/2 optimization - servlet processing will still work
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Destroying Axis2 Undertow HTTP/2 integration");

        try {
            // Cleanup resources
            if (memoryCoordinator != null) {
                log.info("Memory coordinator statistics: " + memoryCoordinator.getStatistics());
            }

            if (axis2Handler != null) {
                log.info("Axis2 handler metrics: " + axis2Handler.getServiceMetrics());
            }

            // Reset statistics
            if (memoryCoordinator != null) {
                memoryCoordinator.resetStatistics();
            }

            if (axis2Handler != null) {
                axis2Handler.resetMetrics();
            }

            log.info("Axis2 Undertow HTTP/2 integration destroyed");

        } catch (Exception e) {
            log.error("Error during Axis2 Undertow integration cleanup", e);
        }
    }

    /**
     * Initialize memory coordination system.
     */
    private void initializeMemoryCoordination(ServletContext servletContext) {
        // Initialize with default 40% of heap for HTTP/2 operations
        this.memoryCoordinator = new HTTP2MemoryCoordinator();

        // Store in servlet context for access by other components
        servletContext.setAttribute("axis2.http2.memoryCoordinator", memoryCoordinator);

        log.info("Memory coordination initialized: " + memoryCoordinator.getStatistics());
    }

    /**
     * Initialize buffer pool integration.
     */
    private void initializeBufferIntegration(ServletContext servletContext) {
        try {
            this.bufferIntegration = new UndertowAxis2BufferIntegration(servletContext);

            // Store in servlet context for access by transport components
            servletContext.setAttribute("axis2.http2.bufferIntegration", bufferIntegration);

            log.info("Buffer integration initialized: " + bufferIntegration.getIntegrationStatus());

        } catch (Exception e) {
            log.warn("Buffer integration initialization failed - proceeding without shared buffer optimization", e);
        }
    }

    /**
     * Configure Undertow handler integration.
     */
    private void configureUndertowHandlerIntegration(ServletContext servletContext) {
        // Get Undertow deployment info
        DeploymentInfo deploymentInfo = (DeploymentInfo) servletContext.getAttribute(DEPLOYMENT_INFO_KEY);

        if (deploymentInfo == null) {
            log.warn("Undertow DeploymentInfo not found - HTTP/2 handler integration not available");
            return;
        }

        // Get Axis2 configuration context
        ConfigurationContext axisConfig = getAxisConfiguration(servletContext);
        if (axisConfig == null) {
            log.warn("Axis2 ConfigurationContext not found - HTTP/2 handler integration not available");
            return;
        }

        // Create and configure Axis2 HTTP/2 handler
        this.axis2Handler = new Axis2HTTP2Handler(axisConfig, memoryCoordinator);

        // Add Axis2 HTTP/2 handler to Undertow chain
        deploymentInfo.addInitialHandlerChainWrapper(createHandlerWrapper());

        // Store handler reference for cleanup
        servletContext.setAttribute("axis2.http2.handler", axis2Handler);

        log.info("Undertow handler integration configured - Axis2 HTTP/2 handler registered");
    }

    /**
     * Create handler wrapper for Undertow integration.
     */
    private HandlerWrapper createHandlerWrapper() {
        return new HandlerWrapper() {
            @Override
            public HttpHandler wrap(HttpHandler next) {
                // Create path handler with Axis2 optimization
                PathHandler pathHandler = new PathHandler(next);

                // Add Axis2 HTTP/2 optimized paths
                pathHandler.addPrefixPath("/services", new CompositeHandler(axis2Handler, next));
                pathHandler.addPrefixPath("/axis2-web", new CompositeHandler(axis2Handler, next));

                // Add health check endpoint for integration monitoring
                pathHandler.addExactPath("/axis2/integration/health",
                    exchange -> {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                        exchange.getResponseSender().send(getIntegrationHealthStatus());
                    }
                );

                log.debug("Axis2 HTTP/2 handler paths configured: /services, /axis2-web, /axis2/integration/health");

                return pathHandler;
            }
        };
    }

    /**
     * Composite handler that tries Axis2 HTTP/2 optimization first, falls back to standard processing.
     */
    private static class CompositeHandler implements HttpHandler {
        private final HttpHandler axis2Handler;
        private final HttpHandler fallbackHandler;

        public CompositeHandler(HttpHandler axis2Handler, HttpHandler fallbackHandler) {
            this.axis2Handler = axis2Handler;
            this.fallbackHandler = fallbackHandler;
        }

        @Override
        public void handleRequest(io.undertow.server.HttpServerExchange exchange) throws Exception {
            try {
                // Try Axis2 HTTP/2 optimized processing first
                axis2Handler.handleRequest(exchange);

                // If exchange is not complete, it means Axis2 handler didn't process it
                if (!exchange.isComplete()) {
                    // Fall back to standard servlet processing
                    fallbackHandler.handleRequest(exchange);
                }
            } catch (Exception e) {
                log.debug("Axis2 HTTP/2 handler failed, falling back to servlet processing", e);
                // Fall back to standard processing
                fallbackHandler.handleRequest(exchange);
            }
        }
    }

    /**
     * Get Axis2 configuration context from servlet context.
     */
    private ConfigurationContext getAxisConfiguration(ServletContext servletContext) {
        try {
            // First try to get from AxisServlet attribute
            ConfigurationContext configContext = (ConfigurationContext) servletContext
                .getAttribute(AxisServlet.CONFIGURATION_CONTEXT);

            if (configContext != null) {
                return configContext;
            }

            // Alternative attribute names used by different Axis2 configurations
            configContext = (ConfigurationContext) servletContext
                .getAttribute("org.apache.axis2.CONFIG_CONTEXT");

            if (configContext != null) {
                return configContext;
            }

            log.warn("Axis2 ConfigurationContext not found in servlet context");
            return null;

        } catch (Exception e) {
            log.error("Error retrieving Axis2 ConfigurationContext", e);
            return null;
        }
    }

    /**
     * Generate integration health status for monitoring.
     */
    private String getIntegrationHealthStatus() {
        StringBuilder health = new StringBuilder();
        health.append("{\n");
        health.append("  \"status\": \"active\",\n");
        health.append("  \"integration\": \"axis2-undertow-http2\",\n");

        if (memoryCoordinator != null) {
            health.append("  \"memoryCoordination\": {\n");
            health.append("    \"enabled\": true,\n");
            health.append("    \"utilization\": ").append(String.format("%.1f", memoryCoordinator.getMemoryUtilizationPercentage())).append(",\n");
            health.append("    \"maxMemory\": \"").append(formatBytes(memoryCoordinator.getMaxTotalMemory())).append("\"\n");
            health.append("  },\n");
        } else {
            health.append("  \"memoryCoordination\": { \"enabled\": false },\n");
        }

        if (bufferIntegration != null) {
            health.append("  \"bufferIntegration\": {\n");
            health.append("    \"enabled\": ").append(bufferIntegration.isIntegrationAvailable()).append(",\n");
            health.append("    \"status\": \"").append(bufferIntegration.getIntegrationStatus()).append("\"\n");
            health.append("  },\n");
        } else {
            health.append("  \"bufferIntegration\": { \"enabled\": false },\n");
        }

        if (axis2Handler != null) {
            health.append("  \"handler\": {\n");
            health.append("    \"enabled\": true,\n");
            health.append("    \"serviceMetrics\": ").append(axis2Handler.getServiceMetrics().size()).append("\n");
            health.append("  }\n");
        } else {
            health.append("  \"handler\": { \"enabled\": false }\n");
        }

        health.append("}");

        return health.toString();
    }

    /**
     * Format bytes for human-readable display.
     */
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
}