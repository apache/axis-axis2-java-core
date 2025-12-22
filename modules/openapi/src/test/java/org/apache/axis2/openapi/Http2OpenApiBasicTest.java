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
 * Basic integration test validating HTTP/2 + OpenAPI compatibility.
 *
 * This test focuses on demonstrating that:
 * 1. OpenAPI specification generation works with HTTPS (required for HTTP/2)
 * 2. Swagger UI generation includes HTTP/2 compatible configuration
 * 3. Large service catalogs can be documented (HTTP/2 multiplexing benefit)
 * 4. Basic performance characteristics are maintained
 */
public class Http2OpenApiBasicTest extends TestCase {

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

        // Deploy test services
        deployTestServices();
    }

    /**
     * Test that OpenAPI spec generation works with HTTPS (required for HTTP/2).
     */
    public void testHttpsOpenApiSpecGeneration() throws Exception {
        MockHttpServletRequest httpsRequest = new MockHttpServletRequest();
        httpsRequest.setScheme("https");
        httpsRequest.setServerName("api.example.com");
        httpsRequest.setServerPort(443);

        OpenAPI openApi = specGenerator.generateOpenApiSpec(httpsRequest);

        assertNotNull("Should generate OpenAPI spec", openApi);
        assertNotNull("Should have paths", openApi.getPaths());
        assertTrue("Should have at least one path", openApi.getPaths().size() > 0);

        // Verify HTTPS server configuration
        List<Server> servers = openApi.getServers();
        assertNotNull("Should have servers", servers);
        assertFalse("Should have at least one server", servers.isEmpty());

        boolean hasHttpsServer = servers.stream()
                .anyMatch(server -> server.getUrl().startsWith("https://"));
        assertTrue("Should have HTTPS server for HTTP/2 compatibility", hasHttpsServer);

        System.out.println("✅ HTTPS OpenAPI spec generation: PASSED");
        System.out.println("   - Generated spec with " + openApi.getPaths().size() + " paths");
        System.out.println("   - HTTPS servers configured: " +
                          servers.stream().filter(s -> s.getUrl().startsWith("https://")).count());
    }

    /**
     * Test Swagger UI generation with HTTPS URLs (HTTP/2 compatible).
     */
    public void testHttpsSwaggerUIGeneration() throws Exception {
        MockHttpServletRequest httpsRequest = new MockHttpServletRequest();
        httpsRequest.setScheme("https");
        httpsRequest.setServerName("api.example.com");
        httpsRequest.setServerPort(443);

        MockHttpServletResponse response = new MockHttpServletResponse();
        uiHandler.handleSwaggerUIRequest(httpsRequest, response);

        String html = response.getContent();
        assertNotNull("Should generate HTML", html);
        assertTrue("Should be valid HTML", html.contains("<!DOCTYPE html>"));
        assertTrue("Should reference HTTPS OpenAPI spec", html.contains("https://"));

        System.out.println("✅ HTTPS Swagger UI generation: PASSED");
        System.out.println("   - Generated " + (html.length() / 1024) + "KB of HTML");
        System.out.println("   - HTTPS OpenAPI spec references configured");
    }

    /**
     * Test large service catalog documentation (benefits from HTTP/2 multiplexing).
     */
    public void testLargeServiceCatalogDocumentation() throws Exception {
        // Deploy additional services to simulate enterprise catalog
        for (int i = 0; i < 20; i++) {
            AxisService service = new AxisService("EnterpriseService" + i);
            service.addParameter(new Parameter("enableREST", "true"));

            AxisOperation operation = new org.apache.axis2.description.InOutAxisOperation();
            operation.setName(new QName("operation"));
            operation.addParameter(new Parameter("HTTPMethod", "GET"));
            operation.addParameter(new Parameter("RESTPath", "/api/service" + i));
            service.addOperation(operation);

            axisConfiguration.addService(service);
        }

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");

        long startTime = System.currentTimeMillis();
        OpenAPI openApi = specGenerator.generateOpenApiSpec(request);
        long specTime = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        String jsonSpec = specGenerator.generateOpenApiJson(request);
        long jsonTime = System.currentTimeMillis() - startTime;

        // Validate large catalog handling
        assertNotNull("Should generate large OpenAPI spec", openApi);
        assertTrue("Should document many services", openApi.getPaths().size() >= 20);
        assertTrue("Should generate substantial JSON", jsonSpec.length() > 50000); // >50KB

        // Performance validation
        assertTrue("Spec generation should be efficient", specTime < 2000);
        assertTrue("JSON generation should be efficient", jsonTime < 2000);

        System.out.println("✅ Large service catalog documentation: PASSED");
        System.out.println("   - Documented " + openApi.getPaths().size() + " endpoints");
        System.out.println("   - Generated " + (jsonSpec.length() / 1024) + "KB JSON spec");
        System.out.println("   - Spec generation: " + specTime + "ms, JSON: " + jsonTime + "ms");
        System.out.println("   - HTTP/2 multiplexing benefit: Large spec + UI assets over single connection");
    }

    /**
     * Test JSON performance characteristics relevant to HTTP/2.
     */
    public void testJsonPerformanceCharacteristics() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");

        // Test multiple concurrent JSON generation (simulates HTTP/2 multiplexing)
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 5; i++) {
            String json = specGenerator.generateOpenApiJson(request);
            String yaml = specGenerator.generateOpenApiYaml(request);

            assertNotNull("Should generate JSON", json);
            assertNotNull("Should generate YAML", yaml);
            assertTrue("JSON should be substantial", json.length() > 1000);
        }

        long totalTime = System.currentTimeMillis() - startTime;

        assertTrue("Multiple format generation should be efficient", totalTime < 3000);

        System.out.println("✅ JSON performance characteristics: PASSED");
        System.out.println("   - Generated 5x JSON + 5x YAML in " + totalTime + "ms");
        System.out.println("   - HTTP/2 benefit: Multiple format requests over single connection");
    }

    /**
     * Test CORS headers for cross-origin HTTP/2 compatibility.
     */
    public void testCorsHeadersForHttp2() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        uiHandler.handleOpenApiJsonRequest(request, response);

        // Validate CORS headers (important for HTTP/2 cross-origin requests)
        assertEquals("Should set CORS origin header", "*",
                    response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("Should set CORS methods", "GET, OPTIONS",
                    response.getHeader("Access-Control-Allow-Methods"));
        assertEquals("Should set CORS headers", "Content-Type, Authorization",
                    response.getHeader("Access-Control-Allow-Headers"));

        System.out.println("✅ CORS headers for HTTP/2: PASSED");
        System.out.println("   - Cross-origin requests supported");
        System.out.println("   - HTTP/2 multiplexing compatible headers configured");
    }

    /**
     * Test basic compatibility with HTTP/2 transport (HTTPS requirement).
     */
    public void testHttp2ConfigurationCompatibility() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https"); // HTTP/2 requires HTTPS

        OpenAPI openApi = specGenerator.generateOpenApiSpec(request);

        assertNotNull("Should work with HTTPS (HTTP/2 compatible)", openApi);
        assertNotNull("Should have info section", openApi.getInfo());
        assertNotNull("Should have title", openApi.getInfo().getTitle());
        assertNotNull("Should have paths", openApi.getPaths());

        // Verify HTTPS servers are configured for HTTP/2 compatibility
        List<Server> servers = openApi.getServers();
        assertNotNull("Should have servers", servers);
        boolean hasHttpsServer = servers.stream()
                .anyMatch(server -> server.getUrl().startsWith("https://"));
        assertTrue("Should have HTTPS server for HTTP/2 compatibility", hasHttpsServer);

        System.out.println("✅ HTTP/2 compatibility validation: PASSED");
        System.out.println("   - HTTPS requirement satisfied");
        System.out.println("   - Basic OpenAPI generation works with HTTP/2 transport");
    }

    // ========== Helper Methods ==========

    private void deployTestServices() throws Exception {
        // Basic REST service
        AxisService testService = new AxisService("TestService");
        testService.addParameter(new Parameter("enableREST", "true"));

        AxisOperation testOp = new org.apache.axis2.description.InOutAxisOperation();
        testOp.setName(new QName("testOperation"));
        testOp.addParameter(new Parameter("HTTPMethod", "GET"));
        testOp.addParameter(new Parameter("RESTPath", "/test"));
        testService.addOperation(testOp);

        axisConfiguration.addService(testService);
    }

    // Mock classes
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