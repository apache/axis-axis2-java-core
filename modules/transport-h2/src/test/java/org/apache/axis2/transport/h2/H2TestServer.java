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

import org.apache.axis2.addressing.EndpointReference;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

/**
 * Simple HTTP/2 test server for integration testing.
 *
 * This server provides basic HTTP/2 endpoint simulation for testing
 * without requiring complex Jetty dependencies.
 *
 * Features:
 * - HTTPS endpoint simulation
 * - JSON service endpoint simulation
 * - Memory-efficient request handling for testing
 * - Configurable ports and paths
 */
public class H2TestServer {

    private boolean started = false;
    private int port;
    private String baseUrl;

    // Default test configuration
    private static final int DEFAULT_PORT = 8443; // HTTPS port
    private static final String CONTEXT_PATH = "/axis2";

    public H2TestServer() {
        this(DEFAULT_PORT);
    }

    public H2TestServer(int port) {
        this.port = port;
        this.baseUrl = "https://localhost:" + port + CONTEXT_PATH;
    }

    /**
     * Start the test server (simulation)
     */
    public void start() throws Exception {
        if (started) {
            return;
        }

        // For testing purposes, we'll simulate server startup
        // In a real implementation, this would start an actual HTTP/2 server
        System.out.println("H2TestServer simulation started at: " + baseUrl);
        started = true;

        // Simulate server initialization delay
        Thread.sleep(100);
    }

    /**
     * Stop the test server
     */
    public void stop() throws Exception {
        if (started) {
            System.out.println("H2TestServer simulation stopped");
            started = false;
        }
    }

    /**
     * Check if server is started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Get endpoint reference for a specific service
     */
    public EndpointReference getEndpointReference(String serviceName) {
        return new EndpointReference(baseUrl + "/" + serviceName);
    }

    /**
     * Get endpoint URL for a specific service
     */
    public String getEndpoint(String serviceName) {
        return baseUrl + "/" + serviceName;
    }

    /**
     * Get base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Get port
     */
    public int getPort() {
        return port;
    }

    /**
     * Simulate JSON service response
     */
    public String simulateJSONServiceResponse(String request) {
        return "{\n" +
               "  \"status\": \"success\",\n" +
               "  \"requestSize\": " + request.length() + ",\n" +
               "  \"processedAt\": \"" + System.currentTimeMillis() + "\",\n" +
               "  \"protocol\": \"HTTP/2\",\n" +
               "  \"message\": \"JSON payload processed successfully via HTTP/2 simulation\"\n" +
               "}";
    }

    /**
     * Simulate JSON-RPC service response
     */
    public String simulateJSONRPCServiceResponse(String request) {
        return "{\n" +
               "  \"jsonrpc\": \"2.0\",\n" +
               "  \"result\": {\n" +
               "    \"portfolioMetrics\": {\"totalValue\": 1500000.00, \"riskScore\": 7.2},\n" +
               "    \"performance\": {\"ytdReturn\": 12.5, \"volatility\": 18.3},\n" +
               "    \"processedVia\": \"HTTP/2 simulation\"\n" +
               "  },\n" +
               "  \"id\": 1\n" +
               "}";
    }

    /**
     * Simulate echo service response
     */
    public String simulateEchoServiceResponse(String request) {
        return "HTTP/2 Echo Response:\n" +
               "Protocol: HTTP/2\n" +
               "Content-Length: " + request.length() + "\n" +
               "Request Body: " + request;
    }

    /**
     * Validate server configuration
     */
    public boolean validateConfiguration() {
        return port > 0 && port < 65536 && baseUrl != null && !baseUrl.isEmpty();
    }

    /**
     * Get server status information
     */
    public String getStatusInfo() {
        return "H2TestServer{" +
               "started=" + started +
               ", port=" + port +
               ", baseUrl='" + baseUrl + '\'' +
               '}';
    }

    /**
     * Simulate connection test
     */
    public boolean testConnection() {
        if (!started) {
            return false;
        }

        try {
            // Simulate connection test delay
            Thread.sleep(10);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Simulate load testing capability
     */
    public void simulateLoad(int concurrentConnections, int requestsPerConnection) {
        if (!started) {
            throw new IllegalStateException("Server not started");
        }

        System.out.println("Simulating load test: " + concurrentConnections +
                          " concurrent connections, " + requestsPerConnection +
                          " requests per connection");

        // Simulate load processing
        try {
            Thread.sleep(concurrentConnections * requestsPerConnection);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Load test simulation completed");
    }
}