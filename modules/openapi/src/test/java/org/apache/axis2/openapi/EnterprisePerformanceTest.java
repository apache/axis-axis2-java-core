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

import io.swagger.v3.oas.models.OpenAPI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;

import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enterprise performance and load testing for OpenAPI integration.
 * Tests performance characteristics under enterprise-scale loads including:
 * - High-concurrency OpenAPI spec generation
 * - Large-scale service introspection
 * - Memory efficiency with many services
 * - Swagger UI performance under load
 * - Configuration parsing performance
 * - Security scheme validation performance
 */
public class EnterprisePerformanceTest extends TestCase {

    private ConfigurationContext configurationContext;
    private AxisConfiguration axisConfiguration;
    private OpenApiModule module;
    private OpenApiSpecGenerator specGenerator;
    private SwaggerUIHandler uiHandler;
    private OpenApiConfiguration config;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Set up Axis2 configuration
        axisConfiguration = new AxisConfiguration();
        configurationContext = new ConfigurationContext(axisConfiguration);

        // Initialize OpenAPI module
        module = new OpenApiModule();
        AxisModule axisModule = new AxisModule();
        axisModule.setName("openapi");
        module.init(configurationContext, axisModule);

        // Get initialized components
        specGenerator = (OpenApiSpecGenerator) configurationContext.getProperty("axis2.openapi.generator");
        uiHandler = (SwaggerUIHandler) configurationContext.getProperty("axis2.openapi.ui");
        config = (OpenApiConfiguration) configurationContext.getProperty("axis2.openapi.config");
    }

    // ========== Concurrency Performance Tests ==========

    /**
     * Test concurrent OpenAPI spec generation under high load.
     * Simulates multiple clients requesting OpenAPI specs simultaneously.
     */
    public void testConcurrentSpecGeneration() throws Exception {
        // Deploy multiple services to test with
        deployLargeServiceSet(50);

        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<OpenAPI>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // Submit 100 concurrent requests
        for (int i = 0; i < 100; i++) {
            futures.add(executor.submit(() -> {
                try {
                    MockHttpServletRequest request = createMockRequest();
                    OpenAPI spec = specGenerator.generateOpenApiSpec(request);
                    if (spec != null && spec.getPaths() != null) {
                        successCount.incrementAndGet();
                        return spec;
                    } else {
                        errorCount.incrementAndGet();
                        return null;
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    return null;
                }
            }));
        }

        // Wait for all requests to complete
        for (Future<OpenAPI> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }

        executor.shutdown();
        long totalTime = System.currentTimeMillis() - startTime;

        // Validate performance
        assertTrue("Should complete within reasonable time (30 seconds)", totalTime < 30000);
        assertTrue("Success rate should be high", successCount.get() > 95);
        assertTrue("Error rate should be low", errorCount.get() < 5);

        System.out.println("Concurrent spec generation: " + successCount.get() + " successes, " +
                          errorCount.get() + " errors in " + totalTime + "ms");
    }

    /**
     * Test concurrent Swagger UI request handling.
     */
    public void testConcurrentSwaggerUIRequests() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(15);
        List<Future<String>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // Submit 75 concurrent UI requests
        for (int i = 0; i < 75; i++) {
            futures.add(executor.submit(() -> {
                try {
                    MockHttpServletRequest request = createMockRequest();
                    MockHttpServletResponse response = new MockHttpServletResponse();
                    uiHandler.handleSwaggerUIRequest(request, response);

                    String html = response.getContent();
                    if (html != null && html.contains("swagger-ui")) {
                        successCount.incrementAndGet();
                        return html;
                    }
                    return null;
                } catch (Exception e) {
                    return null;
                }
            }));
        }

        // Wait for completion
        for (Future<String> future : futures) {
            future.get(20, TimeUnit.SECONDS);
        }

        executor.shutdown();
        long totalTime = System.currentTimeMillis() - startTime;

        assertTrue("Should complete UI requests quickly", totalTime < 20000);
        assertTrue("Most UI requests should succeed", successCount.get() > 70);

        System.out.println("Concurrent UI requests: " + successCount.get() + " successes in " + totalTime + "ms");
    }

    // ========== Large Scale Service Tests ==========

    /**
     * Test performance with large number of services.
     * Simulates enterprise deployment with many microservices.
     */
    public void testLargeScaleServiceIntrospection() throws Exception {
        // Deploy 200 services to simulate large enterprise deployment
        deployLargeServiceSet(200);

        long startTime = System.currentTimeMillis();

        ServiceIntrospector introspector = new ServiceIntrospector(configurationContext);
        List<ServiceIntrospector.ServiceMetadata> services = introspector.getRestServices();

        long introspectionTime = System.currentTimeMillis() - startTime;

        // Validate performance and results
        assertTrue("Should complete introspection within reasonable time", introspectionTime < 5000);
        assertTrue("Should find many services", services.size() >= 200);

        // Test spec generation with large service set
        startTime = System.currentTimeMillis();
        OpenAPI spec = specGenerator.generateOpenApiSpec(createMockRequest());
        long specGenTime = System.currentTimeMillis() - startTime;

        assertTrue("Should generate spec with many services quickly", specGenTime < 10000);
        assertNotNull("Should generate valid spec", spec);
        assertNotNull("Should have paths", spec.getPaths());

        System.out.println("Large scale test: " + services.size() + " services, introspection: " +
                          introspectionTime + "ms, spec generation: " + specGenTime + "ms");
    }

    /**
     * Test memory efficiency with large configurations.
     */
    public void testMemoryEfficiency() throws Exception {
        if (config == null) {
            config = new OpenApiConfiguration();
        }

        // Record initial memory
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // Add large configuration
        for (int i = 0; i < 5000; i++) {
            config.addResourcePackage("com.enterprise.microservice" + i + ".api");
            config.addResourceClass("com.enterprise.service" + i + ".Controller");
            config.addIgnoredRoute("/internal/service" + i + "/.*");
        }

        // Measure memory after configuration
        runtime.gc();
        long configMemory = runtime.totalMemory() - runtime.freeMemory();

        // Generate spec with large configuration
        deployLargeServiceSet(100);
        OpenAPI spec = specGenerator.generateOpenApiSpec(createMockRequest());

        // Measure final memory
        runtime.gc();
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();

        // Validate memory usage is reasonable
        long memoryGrowth = finalMemory - initialMemory;
        assertTrue("Memory growth should be reasonable (< 50MB)", memoryGrowth < 50 * 1024 * 1024);
        assertNotNull("Should still generate spec", spec);

        System.out.println("Memory test: Initial=" + (initialMemory/1024/1024) + "MB, " +
                          "Config=" + (configMemory/1024/1024) + "MB, " +
                          "Final=" + (finalMemory/1024/1024) + "MB, " +
                          "Growth=" + (memoryGrowth/1024/1024) + "MB");
    }

    // ========== Configuration Performance Tests ==========

    /**
     * Test configuration loading and parsing performance.
     */
    public void testConfigurationPerformance() throws Exception {
        long startTime = System.currentTimeMillis();

        // Test loading advanced configuration
        OpenApiConfiguration advancedConfig = new OpenApiConfiguration("test-advanced-openapi.properties");

        long configLoadTime = System.currentTimeMillis() - startTime;

        // Test configuration application
        startTime = System.currentTimeMillis();

        // Apply configuration multiple times to test caching
        for (int i = 0; i < 50; i++) {
            OpenApiConfiguration copy = advancedConfig.copy();
            copy.addResourcePackage("com.test.package" + i);
        }

        long configProcessTime = System.currentTimeMillis() - startTime;

        // Validate performance
        assertTrue("Configuration loading should be fast", configLoadTime < 1000);
        assertTrue("Configuration processing should be efficient", configProcessTime < 2000);

        System.out.println("Configuration performance: Load=" + configLoadTime + "ms, Process=" + configProcessTime + "ms");
    }

    // ========== Security Performance Tests ==========

    /**
     * Test performance of security scheme validation and processing.
     */
    public void testSecuritySchemePerformance() throws Exception {
        if (config == null) {
            config = new OpenApiConfiguration();
        }

        long startTime = System.currentTimeMillis();

        // Add multiple security schemes
        for (int i = 0; i < 100; i++) {
            io.swagger.v3.oas.models.security.SecurityScheme scheme = new io.swagger.v3.oas.models.security.SecurityScheme();
            scheme.setType(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY);
            scheme.setName("X-API-Key-" + i);
            scheme.setIn(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER);
            config.addSecurityDefinition("apiKey" + i, scheme);
        }

        long securitySetupTime = System.currentTimeMillis() - startTime;

        // Test spec generation with many security schemes
        startTime = System.currentTimeMillis();
        OpenAPI spec = specGenerator.generateOpenApiSpec(createMockRequest());
        long specWithSecurityTime = System.currentTimeMillis() - startTime;

        // Validate performance
        assertTrue("Security scheme setup should be efficient", securitySetupTime < 2000);
        assertTrue("Spec generation with security should be reasonable", specWithSecurityTime < 5000);
        assertNotNull("Should generate spec with security", spec);

        if (spec.getComponents() != null && spec.getComponents().getSecuritySchemes() != null) {
            assertTrue("Should include security schemes", spec.getComponents().getSecuritySchemes().size() > 0);
        }

        System.out.println("Security performance: Setup=" + securitySetupTime + "ms, SpecGen=" + specWithSecurityTime + "ms");
    }

    // ========== JSON/YAML Performance Tests ==========

    /**
     * Test JSON and YAML generation performance under load.
     */
    public void testSerializationPerformance() throws Exception {
        // Deploy moderate number of services
        deployLargeServiceSet(50);

        MockHttpServletRequest request = createMockRequest();

        // Test JSON generation performance
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            String json = specGenerator.generateOpenApiJson(request);
            assertNotNull("Should generate JSON", json);
            assertTrue("Should be valid JSON", json.startsWith("{"));
        }
        long jsonTime = System.currentTimeMillis() - startTime;

        // Test YAML generation performance
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            String yaml = specGenerator.generateOpenApiYaml(request);
            assertNotNull("Should generate YAML", yaml);
            assertTrue("Should be valid YAML", yaml.length() > 0 && (yaml.contains("openapi") || yaml.contains("info")));
        }
        long yamlTime = System.currentTimeMillis() - startTime;

        // Validate performance
        assertTrue("JSON generation should be efficient", jsonTime < 10000);
        assertTrue("YAML generation should be efficient", yamlTime < 15000);

        System.out.println("Serialization performance: JSON=" + jsonTime + "ms, YAML=" + yamlTime + "ms");
    }

    // ========== Stress Tests ==========

    /**
     * Extended stress test combining multiple performance factors.
     */
    public void testEnterpriseStressTest() throws Exception {
        // Setup large configuration
        deployLargeServiceSet(100);

        if (config == null) {
            config = new OpenApiConfiguration();
        }

        // Add extensive configuration
        for (int i = 0; i < 1000; i++) {
            config.addResourcePackage("com.enterprise.api" + i);
        }

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> futures = new ArrayList<>();
        AtomicInteger totalOperations = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // Submit mixed workload
        for (int i = 0; i < 50; i++) {
            futures.add(executor.submit(() -> {
                try {
                    MockHttpServletRequest request = createMockRequest();

                    // Mixed operations
                    OpenAPI spec = specGenerator.generateOpenApiSpec(request);
                    totalOperations.incrementAndGet();

                    String json = specGenerator.generateOpenApiJson(request);
                    totalOperations.incrementAndGet();

                    MockHttpServletResponse response = new MockHttpServletResponse();
                    uiHandler.handleSwaggerUIRequest(request, response);
                    totalOperations.incrementAndGet();

                    return spec != null && json != null && response.getContent() != null;
                } catch (Exception e) {
                    return false;
                }
            }));
        }

        // Wait for completion
        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get(60, TimeUnit.SECONDS)) {
                successCount++;
            }
        }

        executor.shutdown();
        long totalTime = System.currentTimeMillis() - startTime;

        // Validate stress test results
        assertTrue("Should complete stress test within time limit", totalTime < 60000);
        assertTrue("Should have high success rate", successCount > 45);
        assertTrue("Should complete many operations", totalOperations.get() > 135);

        System.out.println("Stress test: " + totalOperations.get() + " operations, " +
                          successCount + " successes in " + totalTime + "ms");
    }

    // ========== Helper Methods ==========

    /**
     * Deploy a large set of services for performance testing.
     */
    private void deployLargeServiceSet(int serviceCount) throws Exception {
        for (int i = 0; i < serviceCount; i++) {
            AxisService service = new AxisService("EnterpriseService" + i);
            service.addParameter(new Parameter("enableREST", "true"));

            // Add multiple operations per service
            for (int j = 0; j < 5; j++) {
                AxisOperation operation = new org.apache.axis2.description.InOutAxisOperation();
                operation.setName(new QName("operation" + j));
                operation.addParameter(new Parameter("HTTPMethod", j % 2 == 0 ? "GET" : "POST"));
                operation.addParameter(new Parameter("RESTPath", "/api/v1/service" + i + "/op" + j));
                service.addOperation(operation);
            }

            axisConfiguration.addService(service);
        }
    }

    /**
     * Create a mock HTTP request for testing.
     */
    private MockHttpServletRequest createMockRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("api.enterprise.com");
        request.setServerPort(443);
        request.setContextPath("/api");
        return request;
    }

    // Mock classes for testing
    private static class MockHttpServletRequest implements HttpServletRequest {
        private String scheme = "https";
        private String serverName = "api.enterprise.com";
        private int serverPort = 443;
        private String contextPath = "/api";

        public void setScheme(String scheme) { this.scheme = scheme; }
        public void setServerName(String serverName) { this.serverName = serverName; }
        public void setServerPort(int serverPort) { this.serverPort = serverPort; }
        public void setContextPath(String contextPath) { this.contextPath = contextPath; }

        @Override public String getScheme() { return scheme; }
        @Override public String getServerName() { return serverName; }
        @Override public int getServerPort() { return serverPort; }
        @Override public String getContextPath() { return contextPath; }

        // Minimal implementation
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
        @Override public boolean isSecure() { return true; }
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

    private static class MockHttpServletResponse implements HttpServletResponse {
        private String contentType;
        private int status;
        private StringWriter writer = new StringWriter();
        private PrintWriter printWriter = new PrintWriter(writer);
        private Map<String, String> headers = new java.util.HashMap<>();

        public String getContentType() { return contentType; }
        public int getStatus() { return status; }
        public String getContent() { return writer.toString(); }

        @Override public void setContentType(String type) { this.contentType = type; }
        @Override public void setStatus(int sc) { this.status = sc; }
        @Override public PrintWriter getWriter() { return printWriter; }
        @Override public void setHeader(String name, String value) { headers.put(name, value); }
        @Override public String getHeader(String name) { return headers.get(name); }

        // Minimal implementation
        @Override public void addCookie(jakarta.servlet.http.Cookie cookie) { }
        @Override public boolean containsHeader(String name) { return headers.containsKey(name); }
        @Override public String encodeURL(String url) { return url; }
        @Override public String encodeRedirectURL(String url) { return url; }
        @Override public void sendError(int sc, String msg) { this.status = sc; }
        @Override public void sendError(int sc) { this.status = sc; }
        @Override public void sendRedirect(String location) { }
        @Override public void setDateHeader(String name, long date) { }
        @Override public void addDateHeader(String name, long date) { }
        @Override public void addHeader(String name, String value) { headers.put(name, value); }
        @Override public void setIntHeader(String name, int value) { }
        @Override public void addIntHeader(String name, int value) { }
        @Override public String getCharacterEncoding() { return "UTF-8"; }
        @Override public jakarta.servlet.ServletOutputStream getOutputStream() { return null; }
        @Override public void setCharacterEncoding(String charset) { }
        @Override public void setContentLength(int len) { }
        @Override public void setContentLengthLong(long len) { }
        @Override public void setBufferSize(int size) { }
        @Override public int getBufferSize() { return 0; }
        @Override public void flushBuffer() { }
        @Override public void resetBuffer() { }
        @Override public boolean isCommitted() { return false; }
        @Override public void reset() { }
        @Override public void setLocale(java.util.Locale loc) { }
        @Override public java.util.Locale getLocale() { return null; }
        @Override public java.util.Collection<String> getHeaders(String name) { return null; }
        @Override public java.util.Collection<String> getHeaderNames() { return headers.keySet(); }
        @Override public void sendRedirect(String location, int sc, boolean clearBuffer) { }
    }
}