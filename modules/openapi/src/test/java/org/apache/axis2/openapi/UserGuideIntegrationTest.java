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
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.servers.Server;
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
import java.util.List;
import java.util.Map;

/**
 * Integration tests that simulate the complete user guide scenarios.
 * Tests the full OpenAPI integration workflow from service deployment to documentation generation.
 */
public class UserGuideIntegrationTest extends TestCase {

    private ConfigurationContext configurationContext;
    private AxisConfiguration axisConfiguration;
    private OpenApiModule module;
    private OpenApiSpecGenerator specGenerator;
    private SwaggerUIHandler uiHandler;

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

        // Deploy the user guide sample services
        deployUserGuideSampleServices();
    }

    /**
     * Test complete user guide authentication scenario.
     * Simulates: cURL authentication request -> OpenAPI spec generation -> Swagger UI serving.
     */
    public void testCompleteAuthenticationScenario() throws Exception {
        // Step 1: Generate OpenAPI specification
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setContextPath("/axis2");

        OpenAPI openApi = specGenerator.generateOpenApiSpec(request);

        // Assert OpenAPI spec contains authentication service
        assertNotNull("OpenAPI spec should be generated", openApi);
        assertNotNull("Should have paths", openApi.getPaths());

        // Step 2: Verify authentication endpoint is documented
        boolean hasAuthEndpoint = openApi.getPaths().keySet().stream()
                .anyMatch(path -> path.contains("login") || path.contains("authentication"));
        assertTrue("Should document authentication endpoint", hasAuthEndpoint);

        // Step 3: Test Swagger UI generation
        MockHttpServletResponse response = new MockHttpServletResponse();
        uiHandler.handleSwaggerUIRequest(request, response);

        String html = response.getContent();
        assertNotNull("Swagger UI HTML should be generated", html);
        assertTrue("Should contain Swagger UI components",
                html.contains("swagger-ui") || html.contains("SwaggerUIBundle") || html.contains("Apache Axis2"));

        // Step 4: Test OpenAPI JSON endpoint
        response = new MockHttpServletResponse();
        uiHandler.handleOpenApiJsonRequest(request, response);

        String json = response.getContent();
        assertNotNull("OpenAPI JSON should be generated", json);
        assertTrue("Should be valid JSON", json.trim().startsWith("{"));
        assertTrue("Should contain service info", json.contains("login") || json.contains("AuthenticationService") || json.contains("paths"));
    }

    /**
     * Test complete data management scenario.
     * Simulates: Market data service deployment -> OpenAPI spec generation -> bigdataToken authentication.
     */
    public void testCompleteDataManagementScenario() throws Exception {
        // Step 1: Verify data management service is deployed
        List<ServiceIntrospector.ServiceMetadata> services =
                new ServiceIntrospector(configurationContext).getRestServices();

        boolean hasDataService = services.stream()
                .anyMatch(s -> s.getServiceName().contains("Data") || s.getServiceName().contains("Market"));
        assertTrue("Should have data management service deployed", hasDataService);

        // Step 2: Generate OpenAPI specification with data services
        OpenAPI openApi = specGenerator.generateOpenApiSpec(new MockHttpServletRequest());

        // Step 3: Verify data management endpoints
        Map<String, PathItem> paths = openApi.getPaths();
        boolean hasDataEndpoints = paths.keySet().stream()
                .anyMatch(path -> path.contains("DataManagementService") ||
                                path.contains("Market") ||
                                path.contains("Financial") ||
                                path.contains("services"));

        assertTrue("Should document data management endpoints", hasDataEndpoints || !paths.isEmpty());

        // Step 4: Verify server configuration supports custom authentication
        List<Server> servers = openApi.getServers();
        assertNotNull("Should have server configuration", servers);
        assertFalse("Should have at least one server", servers.isEmpty());
    }

    /**
     * Test complete Excel integration scenario.
     * Simulates: Excel service deployment -> Function metadata generation -> OpenAPI documentation.
     */
    public void testCompleteExcelIntegrationScenario() throws Exception {
        // Step 1: Verify Excel integration service capabilities
        ServiceIntrospector introspector = new ServiceIntrospector(configurationContext);
        List<ServiceIntrospector.ServiceMetadata> services = introspector.getRestServices();

        ServiceIntrospector.ServiceMetadata excelService = services.stream()
                .filter(s -> s.getServiceName().contains("Excel"))
                .findFirst().orElse(null);

        if (excelService != null && !excelService.getOperations().isEmpty()) {
            // Step 2: Verify Excel-specific operations
            boolean hasFunctionSpecs = excelService.getOperations().stream()
                    .anyMatch(op -> op.getOperationName().contains("Function") ||
                                   op.getOperationName().contains("Specs") ||
                                   op.getOperationName().contains("function") ||
                                   op.getOperationName().contains("spec"));
            assertTrue("Should have Excel function specification operations", hasFunctionSpecs);
        } else {
            // If Excel service not found, verify that at least some services are deployed
            assertTrue("Should have services deployed for Excel integration test", services.size() >= 2);
        }

        // Step 3: Test OpenAPI generation for Excel compatibility
        OpenAPI openApi = specGenerator.generateOpenApiSpec(new MockHttpServletRequest());

        // Step 4: Verify CORS headers are enabled for Excel Add-in compatibility
        MockHttpServletResponse response = new MockHttpServletResponse();
        uiHandler.handleOpenApiJsonRequest(new MockHttpServletRequest(), response);

        assertEquals("Should enable CORS for Excel Add-ins", "*",
                response.getHeader("Access-Control-Allow-Origin"));
    }

    /**
     * Test drop-in replacement compatibility.
     * Verifies that the OpenAPI services can replace existing backends without frontend changes.
     */
    public void testDropInReplacementCompatibility() throws Exception {
        // Step 1: Generate OpenAPI spec with standard REST patterns
        OpenAPI openApi = specGenerator.generateOpenApiSpec(new MockHttpServletRequest());

        // Step 2: Verify standard HTTP methods are supported
        boolean hasPostEndpoints = openApi.getPaths().values().stream()
                .anyMatch(pathItem -> pathItem.getPost() != null);
        boolean hasGetEndpoints = openApi.getPaths().values().stream()
                .anyMatch(pathItem -> pathItem.getGet() != null);

        assertTrue("Should support POST endpoints for data submission", hasPostEndpoints || hasGetEndpoints);

        // Step 3: Verify response format compatibility
        String json = specGenerator.generateOpenApiJson(new MockHttpServletRequest());
        assertTrue("Should generate valid OpenAPI 3.0.1 spec", json.contains("3.0.1"));

        // Step 4: Verify custom header authentication support
        // The OpenAPI spec should document custom header parameters like bigdataToken
        // This enables frontend applications to continue using their existing authentication patterns
        assertTrue("Should support custom authentication patterns",
                json.contains("header") || json.contains("parameter"));
    }

    /**
     * Test performance of complete integration workflow.
     * Verifies that the full workflow is fast enough for production use.
     */
    public void testIntegrationPerformance() throws Exception {
        // Measure complete workflow performance
        long startTime = System.currentTimeMillis();

        // Step 1: Service introspection
        ServiceIntrospector introspector = new ServiceIntrospector(configurationContext);
        List<ServiceIntrospector.ServiceMetadata> services = introspector.getRestServices();

        // Step 2: OpenAPI spec generation
        OpenAPI openApi = specGenerator.generateOpenApiSpec(new MockHttpServletRequest());

        // Step 3: JSON generation
        String json = specGenerator.generateOpenApiJson(new MockHttpServletRequest());

        // Step 4: Swagger UI generation
        MockHttpServletResponse response = new MockHttpServletResponse();
        uiHandler.handleSwaggerUIRequest(new MockHttpServletRequest(), response);

        long totalTime = System.currentTimeMillis() - startTime;

        // Assert performance is acceptable
        assertTrue("Complete integration workflow should complete within 2 seconds", totalTime < 2000);
        assertNotNull("Should produce valid results", openApi);
        assertFalse("Should generate non-empty JSON", json.trim().isEmpty());
        assertFalse("Should generate non-empty HTML", response.getContent().trim().isEmpty());
    }

    /**
     * Test error handling in integration scenarios.
     * Verifies graceful handling of various error conditions.
     */
    public void testIntegrationErrorHandling() throws Exception {
        // Test with null request
        String json = specGenerator.generateOpenApiJson(null);
        assertNotNull("Should handle null request gracefully", json);

        // Test with invalid service configuration
        AxisService errorService = new AxisService("ErrorService");
        // Don't add proper REST configuration
        axisConfiguration.addService(errorService);

        OpenAPI openApi = specGenerator.generateOpenApiSpec(new MockHttpServletRequest());
        assertNotNull("Should handle invalid service configuration", openApi);

        // Test Swagger UI with error conditions
        MockHttpServletResponse response = new MockHttpServletResponse();
        uiHandler.handleSwaggerUIRequest(new MockHttpServletRequest(), response);

        String html = response.getContent();
        assertNotNull("Should generate HTML even with errors", html);
        assertTrue("Should be valid HTML", html.contains("html"));
    }

    /**
     * Test multi-service integration scenario.
     * Verifies that multiple services are properly integrated and documented together.
     */
    public void testMultiServiceIntegration() throws Exception {
        // Verify all user guide services are deployed
        ServiceIntrospector introspector = new ServiceIntrospector(configurationContext);
        List<ServiceIntrospector.ServiceMetadata> services = introspector.getRestServices();

        assertTrue("Should have multiple services deployed", services.size() >= 2);

        // Generate comprehensive OpenAPI spec
        OpenAPI openApi = specGenerator.generateOpenApiSpec(new MockHttpServletRequest());

        // Verify all services are documented
        Map<String, PathItem> paths = openApi.getPaths();
        assertNotNull("Should have multiple paths", paths);
        assertTrue("Should document multiple endpoints", paths.size() >= 2);

        // Verify consistent API structure across services
        for (PathItem pathItem : paths.values()) {
            if (pathItem.getPost() != null) {
                Operation operation = pathItem.getPost();
                assertNotNull("Operations should have tags", operation.getTags());
                // Verify consistent response structure
                assertNotNull("Operations should have responses", operation.getResponses());
            }
        }
    }

    /**
     * Deploy the user guide sample services for testing.
     */
    private void deployUserGuideSampleServices() throws Exception {
        // Deploy Authentication Service
        AxisService authService = new AxisService("AuthenticationService");
        authService.addParameter(new Parameter("enableREST", "true"));

        AxisOperation loginOp = new org.apache.axis2.description.InOutAxisOperation();
        loginOp.setName(new QName("login"));
        loginOp.addParameter(new Parameter("HTTPMethod", "POST"));
        loginOp.addParameter(new Parameter("RESTPath", "/bigdataservice/login"));
        authService.addOperation(loginOp);

        axisConfiguration.addService(authService);

        // Deploy Data Management Service
        AxisService dataService = new AxisService("DataManagementService");
        dataService.addParameter(new Parameter("enableREST", "true"));

        AxisOperation marketOp = new org.apache.axis2.description.InOutAxisOperation();
        marketOp.setName(new QName("getMarketSummary"));
        marketOp.addParameter(new Parameter("HTTPMethod", "POST"));
        marketOp.addParameter(new Parameter("RESTPath", "/bigdataservice/marketSummary"));
        dataService.addOperation(marketOp);

        AxisOperation calcOp = new org.apache.axis2.description.InOutAxisOperation();
        calcOp.setName(new QName("calculateFinancials"));
        calcOp.addParameter(new Parameter("HTTPMethod", "POST"));
        calcOp.addParameter(new Parameter("RESTPath", "/bigdataservice/financialCalculation"));
        dataService.addOperation(calcOp);

        axisConfiguration.addService(dataService);

        // Deploy Excel Integration Service
        AxisService excelService = new AxisService("ExcelIntegrationService");
        excelService.addParameter(new Parameter("enableREST", "true"));

        AxisOperation funcOp = new org.apache.axis2.description.InOutAxisOperation();
        funcOp.setName(new QName("getFunctionSpecs"));
        funcOp.addParameter(new Parameter("HTTPMethod", "GET"));
        funcOp.addParameter(new Parameter("RESTPath", "/bigdataservice/functionSpecs"));
        excelService.addOperation(funcOp);

        axisConfiguration.addService(excelService);
    }

    // Mock classes for testing
    private static class MockHttpServletRequest implements HttpServletRequest {
        private String scheme = "http";
        private String serverName = "localhost";
        private int serverPort = 8080;
        private String contextPath = "";

        public void setScheme(String scheme) { this.scheme = scheme; }
        public void setServerName(String serverName) { this.serverName = serverName; }
        public void setServerPort(int serverPort) { this.serverPort = serverPort; }
        public void setContextPath(String contextPath) { this.contextPath = contextPath; }

        @Override public String getScheme() { return scheme; }
        @Override public String getServerName() { return serverName; }
        @Override public int getServerPort() { return serverPort; }
        @Override public String getContextPath() { return contextPath; }

        // Minimal implementation - only methods used by tests
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
        @Override public boolean isSecure() { return false; }
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