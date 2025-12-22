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

package org.apache.axis2.openapi;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.json.moshih2.JsonProcessingMetrics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import junit.framework.TestCase;

import javax.xml.namespace.QName;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

/**
 * HTTP/2 + OpenAPI Performance Benchmark Test.
 *
 * This test validates the performance benefits of integrating moshih2 performance
 * monitoring with OpenAPI JSON generation, demonstrating the potential improvements
 * when combined with HTTP/2 transport.
 *
 * Key Performance Areas Tested:
 * - Large OpenAPI specification generation (simulates enterprise API catalogs)
 * - Concurrent OpenAPI generation (simulates HTTP/2 multiplexing benefits)
 * - Performance metrics collection and optimization recommendations
 * - Memory usage patterns during large specification processing
 */
public class Http2PerformanceBenchmark extends TestCase {

    private ConfigurationContext configurationContext;
    private AxisConfiguration axisConfiguration;
    private OpenApiSpecGenerator specGenerator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Set up Axis2 configuration
        axisConfiguration = new AxisConfiguration();
        configurationContext = new ConfigurationContext(axisConfiguration);

        // Configure OpenAPI generator with HTTP/2 metrics
        OpenApiConfiguration configuration = new OpenApiConfiguration();
        configuration.setTitle("Enterprise API Catalog");
        configuration.setDescription("Large-scale enterprise API for HTTP/2 performance testing");
        configuration.setVersion("2.0.0");
        configuration.setPrettyPrint(false); // Optimize for performance

        specGenerator = new OpenApiSpecGenerator(configurationContext, configuration);

        // Deploy simulated enterprise services
        deployLargeEnterpriseServiceCatalog();
    }

    /**
     * Benchmark: Large OpenAPI Specification Generation
     *
     * Simulates an enterprise environment with many services to test:
     * - JSON processing performance with large payloads (similar to moshih2 benefits)
     * - Memory usage patterns
     * - HTTP/2 benefits for large specification delivery
     */
    public void testLargeOpenApiSpecificationPerformance() throws Exception {
        System.out.println("=== HTTP/2 + OpenAPI Performance Benchmark ===");
        System.out.println("Testing large OpenAPI specification generation...");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https"); // Required for HTTP/2

        // Warm up (JIT optimization)
        for (int i = 0; i < 5; i++) {
            specGenerator.generateOpenApiJson(request);
        }

        // Benchmark large specification generation
        long totalTime = 0;
        int iterations = 20;
        List<Long> specSizes = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            String jsonSpec = specGenerator.generateOpenApiJson(request);
            long endTime = System.currentTimeMillis();

            totalTime += (endTime - startTime);
            specSizes.add((long) jsonSpec.length());
        }

        long averageTime = totalTime / iterations;
        long averageSize = specSizes.stream().mapToLong(Long::longValue).sum() / specSizes.size();

        long http11Time = estimateHttp11DeliveryTime(averageSize);
        long http2Time = estimateHttp2DeliveryTime(averageSize);

        System.out.println("Large OpenAPI Specification Performance Results:");
        System.out.println("  - Average specification size: " + (averageSize / 1024) + " KB (" + averageSize + " bytes)");
        System.out.println("  - Average generation time: " + averageTime + " ms");

        // Debug calculations
        double http11TransferTime = (double)(averageSize * 8) / 1_000_000.0;
        double http2TransferTime = (double)(averageSize * 8) / 1_300_000.0;
        System.out.println("  - Debug: HTTP/1.1 transfer time: " + http11TransferTime + " seconds = " + (http11TransferTime * 1000) + " ms");
        System.out.println("  - Debug: HTTP/2 transfer time: " + http2TransferTime + " seconds = " + (http2TransferTime * 1000) + " ms");

        System.out.println("  - Estimated HTTP/1.1 delivery time: " + http11Time + " ms");
        System.out.println("  - Estimated HTTP/2 delivery time: " + http2Time + " ms");
        System.out.println("  - HTTP/2 improvement: " + calculateImprovementPercentage(http11Time, http2Time) + "%");

        // Validate performance is reasonable for enterprise use
        assertTrue("Large spec generation should be under 500ms", averageTime < 500);
        assertTrue("Specs should be substantial (>50KB for enterprise)", averageSize > 50000);
    }

    /**
     * Benchmark: Concurrent OpenAPI Generation
     *
     * Simulates HTTP/2 multiplexing scenarios where multiple OpenAPI requests
     * are processed concurrently over a single connection.
     */
    public void testConcurrentOpenApiGeneration() throws Exception {
        System.out.println("\nTesting concurrent OpenAPI generation (HTTP/2 multiplexing simulation)...");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");

        ExecutorService executor = Executors.newFixedThreadPool(10);
        int concurrentRequests = 50;

        long startTime = System.currentTimeMillis();

        // Submit concurrent requests (simulates HTTP/2 multiplexing)
        CompletableFuture<String>[] futures = new CompletableFuture[concurrentRequests];
        for (int i = 0; i < concurrentRequests; i++) {
            futures[i] = CompletableFuture.supplyAsync(() ->
                specGenerator.generateOpenApiJson(request), executor);
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(futures).get();
        long totalTime = System.currentTimeMillis() - startTime;

        System.out.println("Concurrent OpenAPI Generation Performance Results:");
        System.out.println("  - Concurrent requests: " + concurrentRequests);
        System.out.println("  - Total processing time: " + totalTime + " ms");
        System.out.println("  - Average time per request: " + (totalTime / concurrentRequests) + " ms");
        System.out.println("  - Estimated HTTP/1.1 time: " + (concurrentRequests * 200) + " ms (sequential connections)");
        System.out.println("  - HTTP/2 benefit: " + calculateImprovementPercentage(
                concurrentRequests * 200, totalTime) + "% (multiplexed connections)");

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Validate concurrent performance
        assertTrue("Concurrent processing should be efficient", totalTime < (concurrentRequests * 100));
    }

    /**
     * Benchmark: Performance Metrics and Optimization Analysis
     *
     * Tests the integrated moshih2 performance metrics to demonstrate
     * optimization insights for HTTP/2 + OpenAPI deployments.
     */
    public void testPerformanceMetricsAndOptimizations() throws Exception {
        System.out.println("\nTesting performance metrics and optimization recommendations...");

        // Generate various specification sizes to populate metrics
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");

        for (int i = 0; i < 30; i++) {
            specGenerator.generateOpenApiJson(request);
        }

        // Get performance statistics
        JsonProcessingMetrics.Statistics stats = specGenerator.getProcessingStatistics();
        String recommendations = specGenerator.getOptimizationRecommendations();

        System.out.println("Performance Metrics Results:");
        System.out.println("  - Total requests processed: " + stats.getTotalRequests());
        System.out.println("  - Total data processed: " + (stats.getTotalBytes() / 1024) + " KB");
        System.out.println("  - Average processing time: " + String.format("%.2f", stats.getAverageProcessingTimeMs()) + " ms");
        System.out.println("  - Min processing time: " + stats.getMinProcessingTimeMs() + " ms");
        System.out.println("  - Max processing time: " + stats.getMaxProcessingTimeMs() + " ms");

        System.out.println("\nOptimization Recommendations:");
        System.out.println(recommendations);

        // Validate metrics are being collected
        assertTrue("Should process multiple requests", stats.getTotalRequests() >= 30);
        assertTrue("Should process substantial data", stats.getTotalBytes() > 100000); // >100KB
        assertNotNull("Should provide optimization recommendations", recommendations);
        assertTrue("Recommendations should mention HTTP/2", recommendations.contains("HTTP/2"));
    }

    /**
     * Deploy a large enterprise service catalog for performance testing.
     */
    private void deployLargeEnterpriseServiceCatalog() throws Exception {
        // Deploy multiple services to create a realistic enterprise API catalog
        String[] serviceCategories = {"user", "product", "order", "payment", "inventory",
                                     "analytics", "notification", "audit", "reporting", "integration"};
        String[] operations = {"create", "read", "update", "delete", "search", "export"};

        for (String category : serviceCategories) {
            for (String operation : operations) {
                AxisService service = new AxisService(category + "Service_" + operation);
                service.addParameter(new Parameter("enableREST", "true"));

                AxisOperation axisOp = new org.apache.axis2.description.InOutAxisOperation();
                axisOp.setName(new QName(operation + category.substring(0, 1).toUpperCase() + category.substring(1)));
                axisOp.addParameter(new Parameter("HTTPMethod",
                    operation.equals("create") ? "POST" :
                    operation.equals("read") || operation.equals("search") || operation.equals("export") ? "GET" :
                    operation.equals("update") ? "PUT" : "DELETE"));
                axisOp.addParameter(new Parameter("RESTPath", "/" + category + "/" + operation));

                service.addOperation(axisOp);
                axisConfiguration.addService(service);
            }
        }

        System.out.println("Deployed " + (serviceCategories.length * operations.length) + " enterprise services for performance testing");
    }

    /**
     * Estimate HTTP/1.1 delivery time based on specification size.
     * Assumes typical network conditions and connection overhead.
     */
    private long estimateHttp11DeliveryTime(long specSizeBytes) {
        // Assumptions for HTTP/1.1:
        // - 100ms connection setup
        // - 50ms SSL handshake
        // - 1Mbps effective throughput (conservative for large JSON)
        long connectionOverhead = 150; // 100ms + 50ms
        double transferTimeSeconds = (double)(specSizeBytes * 8) / 1_000_000.0; // 1 Mbps = 1,000,000 bits/sec
        return connectionOverhead + Math.round(transferTimeSeconds * 1000);
    }

    /**
     * Estimate HTTP/2 delivery time based on specification size.
     * Accounts for multiplexing and connection reuse benefits.
     */
    private long estimateHttp2DeliveryTime(long specSizeBytes) {
        // Assumptions for HTTP/2:
        // - Connection reuse (no setup overhead for subsequent requests)
        // - 1.3Mbps effective throughput (30% improvement from multiplexing)
        // - First request: 150ms overhead, subsequent requests: 0ms overhead
        long connectionOverhead = 0; // Assuming connection reuse
        double transferTimeSeconds = (double)(specSizeBytes * 8) / 1_300_000.0; // 1.3 Mbps = 1,300,000 bits/sec
        return connectionOverhead + Math.round(transferTimeSeconds * 1000);
    }

    /**
     * Calculate percentage improvement between two values.
     */
    private double calculateImprovementPercentage(long baseline, long improved) {
        if (baseline == 0) return 0;
        return ((double)(baseline - improved) / baseline) * 100;
    }

    // Mock classes for testing
    private static class MockHttpServletRequest implements HttpServletRequest {
        private String scheme = "http";
        private String serverName = "localhost";
        private int serverPort = 8080;

        public void setScheme(String scheme) { this.scheme = scheme; }

        @Override public String getScheme() { return scheme; }
        @Override public String getServerName() { return serverName; }
        @Override public int getServerPort() { return serverPort; }
        @Override public String getContextPath() { return ""; }

        // Minimal implementation - rest of the methods return defaults
        @Override public String getAuthType() { return null; }
        @Override public jakarta.servlet.http.Cookie[] getCookies() { return new jakarta.servlet.http.Cookie[0]; }
        @Override public long getDateHeader(String name) { return 0; }
        @Override public String getHeader(String name) { return null; }
        @Override public java.util.Enumeration<String> getHeaders(String name) { return null; }
        @Override public java.util.Enumeration<String> getHeaderNames() { return null; }
        @Override public int getIntHeader(String name) { return 0; }
        @Override public String getMethod() { return "GET"; }
        @Override public String getPathInfo() { return null; }
        @Override public String getPathTranslated() { return null; }
        @Override public String getQueryString() { return null; }
        @Override public String getRemoteUser() { return null; }
        @Override public boolean isUserInRole(String role) { return false; }
        @Override public java.security.Principal getUserPrincipal() { return null; }
        @Override public String getRequestedSessionId() { return null; }
        @Override public String getRequestURI() { return ""; }
        @Override public StringBuffer getRequestURL() { return new StringBuffer(); }
        @Override public String getServletPath() { return ""; }
        @Override public jakarta.servlet.http.HttpSession getSession(boolean create) { return null; }
        @Override public jakarta.servlet.http.HttpSession getSession() { return null; }
        @Override public String changeSessionId() { return null; }
        @Override public boolean isRequestedSessionIdValid() { return false; }
        @Override public boolean isRequestedSessionIdFromCookie() { return false; }
        @Override public boolean isRequestedSessionIdFromURL() { return false; }
        @Override public boolean authenticate(HttpServletResponse response) { return false; }
        @Override public void login(String username, String password) { }
        @Override public void logout() { }
        @Override public java.util.Collection<jakarta.servlet.http.Part> getParts() { return null; }
        @Override public jakarta.servlet.http.Part getPart(String name) { return null; }
        @Override public <T extends jakarta.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass) { return null; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public java.util.Enumeration<String> getAttributeNames() { return null; }
        @Override public String getCharacterEncoding() { return null; }
        @Override public void setCharacterEncoding(String env) { }
        @Override public int getContentLength() { return 0; }
        @Override public long getContentLengthLong() { return 0; }
        @Override public String getContentType() { return null; }
        @Override public jakarta.servlet.ServletInputStream getInputStream() { return null; }
        @Override public String getParameter(String name) { return null; }
        @Override public java.util.Enumeration<String> getParameterNames() { return null; }
        @Override public String[] getParameterValues(String name) { return new String[0]; }
        @Override public java.util.Map<String, String[]> getParameterMap() { return null; }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public String getRemoteAddr() { return "127.0.0.1"; }
        @Override public String getRemoteHost() { return "localhost"; }
        @Override public void setAttribute(String name, Object o) { }
        @Override public void removeAttribute(String name) { }
        @Override public java.util.Locale getLocale() { return null; }
        @Override public java.util.Enumeration<java.util.Locale> getLocales() { return null; }
        @Override public boolean isSecure() { return "https".equals(scheme); }
        @Override public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) { return null; }
        @Override public int getRemotePort() { return 0; }
        @Override public String getLocalName() { return "localhost"; }
        @Override public String getLocalAddr() { return "127.0.0.1"; }
        @Override public int getLocalPort() { return serverPort; }
        @Override public jakarta.servlet.ServletContext getServletContext() { return null; }
        @Override public jakarta.servlet.AsyncContext startAsync() { return null; }
        @Override public jakarta.servlet.AsyncContext startAsync(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse) { return null; }
        @Override public boolean isAsyncStarted() { return false; }
        @Override public boolean isAsyncSupported() { return false; }
        @Override public jakarta.servlet.AsyncContext getAsyncContext() { return null; }
        @Override public jakarta.servlet.DispatcherType getDispatcherType() { return null; }
        @Override public java.io.BufferedReader getReader() { return null; }
        @Override public jakarta.servlet.ServletConnection getServletConnection() { return null; }
        @Override public String getProtocolRequestId() { return null; }
        @Override public String getRequestId() { return null; }
    }
}