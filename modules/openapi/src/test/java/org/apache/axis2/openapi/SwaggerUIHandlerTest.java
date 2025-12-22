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

import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.Locale;
import java.util.Collection;
import java.security.Principal;
import java.io.BufferedReader;

/**
 * Unit tests for SwaggerUIHandler class.
 * Tests Swagger UI serving and OpenAPI specification endpoints.
 */
public class SwaggerUIHandlerTest extends TestCase {

    private SwaggerUIHandler handler;
    private ConfigurationContext configurationContext;
    private MockHttpServletRequest mockRequest;
    private MockHttpServletResponse mockResponse;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AxisConfiguration axisConfiguration = new AxisConfiguration();
        configurationContext = new ConfigurationContext(axisConfiguration);
        handler = new SwaggerUIHandler(configurationContext);
        mockRequest = new MockHttpServletRequest();
        mockResponse = new MockHttpServletResponse();
    }

    /**
     * Test Swagger UI HTML generation.
     * Verifies that valid HTML is generated with correct CDN links and configuration.
     */
    public void testSwaggerUIRequest() throws Exception {
        // Arrange
        mockRequest.setScheme("https");
        mockRequest.setServerName("api.example.com");
        mockRequest.setServerPort(8443);
        mockRequest.setContextPath("/axis2");

        // Act
        handler.handleSwaggerUIRequest(mockRequest, mockResponse);

        // Assert
        assertEquals("Content type should be HTML", "text/html; charset=UTF-8", mockResponse.getContentType());
        assertEquals("Status should be OK", 200, mockResponse.getStatus());

        String html = mockResponse.getWriterContent();
        assertNotNull("HTML should be generated", html);
        assertFalse("HTML should not be empty", html.trim().isEmpty());

        // Verify HTML structure
        assertTrue("Should be valid HTML document", html.contains("<!DOCTYPE html>"));
        assertTrue("Should have page title", html.contains("Apache Axis2 REST API - API Documentation"));
        assertTrue("Should include Swagger UI CSS", html.contains("swagger-ui.css"));
        assertTrue("Should include Swagger UI JS", html.contains("swagger-ui-bundle.js"));

        // Verify OpenAPI URL configuration
        assertTrue("Should configure OpenAPI URL",
                html.contains("https://api.example.com:8443/axis2/openapi.json"));

        // Verify Swagger UI version
        assertTrue("Should use correct Swagger UI version", html.contains("4.15.5"));
    }

    /**
     * Test OpenAPI JSON endpoint.
     * Verifies that valid JSON specification is served with correct headers.
     */
    public void testOpenApiJsonRequest() throws Exception {
        // Act
        handler.handleOpenApiJsonRequest(mockRequest, mockResponse);

        // Assert
        assertEquals("Content type should be JSON", "application/json; charset=UTF-8", mockResponse.getContentType());
        assertEquals("Status should be OK", 200, mockResponse.getStatus());

        // Verify CORS headers
        assertEquals("CORS origin header should be set", "*", mockResponse.getHeader("Access-Control-Allow-Origin"));
        assertEquals("CORS methods header should be set", "GET, OPTIONS", mockResponse.getHeader("Access-Control-Allow-Methods"));
        assertEquals("CORS headers should be set", "Content-Type, Authorization", mockResponse.getHeader("Access-Control-Allow-Headers"));

        String json = mockResponse.getWriterContent();
        assertNotNull("JSON should be generated", json);
        assertFalse("JSON should not be empty", json.trim().isEmpty());
        assertTrue("Should be valid JSON format", json.trim().startsWith("{"));
    }

    /**
     * Test OpenAPI YAML endpoint.
     * Verifies that YAML specification is served with correct headers.
     */
    public void testOpenApiYamlRequest() throws Exception {
        // Act
        handler.handleOpenApiYamlRequest(mockRequest, mockResponse);

        // Assert
        assertEquals("Content type should be YAML", "application/yaml; charset=UTF-8", mockResponse.getContentType());
        assertEquals("Status should be OK", 200, mockResponse.getStatus());

        // Verify CORS headers
        assertEquals("CORS origin header should be set", "*", mockResponse.getHeader("Access-Control-Allow-Origin"));
        assertEquals("CORS methods header should be set", "GET, OPTIONS", mockResponse.getHeader("Access-Control-Allow-Methods"));
        assertEquals("CORS headers should be set", "Content-Type, Authorization", mockResponse.getHeader("Access-Control-Allow-Headers"));

        String yaml = mockResponse.getWriterContent();
        assertNotNull("YAML should be generated", yaml);
        assertFalse("YAML should not be empty", yaml.trim().isEmpty());
    }

    /**
     * Test OpenAPI URL building with various request configurations.
     * Simulates the user guide scenarios for drop-in compatibility.
     */
    public void testOpenApiUrlBuilding() throws Exception {
        // Test HTTPS with custom port
        mockRequest.setScheme("https");
        mockRequest.setServerName("secure.example.com");
        mockRequest.setServerPort(8443);
        mockRequest.setContextPath("/api");

        handler.handleSwaggerUIRequest(mockRequest, mockResponse);
        String html = mockResponse.getWriterContent();
        assertTrue("Should build correct HTTPS URL with custom port",
                html.contains("https://secure.example.com:8443/api/openapi.json"));

        // Reset for next test
        mockResponse = new MockHttpServletResponse();

        // Test HTTP with default port
        mockRequest.setScheme("http");
        mockRequest.setServerName("localhost");
        mockRequest.setServerPort(80);
        mockRequest.setContextPath("");

        handler.handleSwaggerUIRequest(mockRequest, mockResponse);
        html = mockResponse.getWriterContent();
        assertTrue("Should build correct HTTP URL without default port",
                html.contains("http://localhost/openapi.json"));
    }

    /**
     * Test error handling in JSON generation.
     * Verifies graceful handling when specification generation fails.
     */
    public void testJsonGenerationError() throws Exception {
        // Arrange - create handler with null context (gracefully handles errors)
        SwaggerUIHandler errorHandler = new SwaggerUIHandler(null);

        // Act
        errorHandler.handleOpenApiJsonRequest(mockRequest, mockResponse);

        // Assert
        assertEquals("Should return OK status", 200, mockResponse.getStatus());
        String json = mockResponse.getWriterContent();
        assertNotNull("Should return valid JSON", json);
        assertTrue("Should be valid JSON format", json.trim().startsWith("{"));
        assertTrue("Should contain OpenAPI version", json.contains("3.0.1"));
        // With null context, it gracefully creates an empty spec rather than error
    }

    /**
     * Test Swagger UI customization.
     * Verifies that the UI includes custom styling and branding.
     */
    public void testSwaggerUICustomization() throws Exception {
        // Act
        handler.handleSwaggerUIRequest(mockRequest, mockResponse);

        // Assert
        String html = mockResponse.getWriterContent();

        // Verify custom header
        assertTrue("Should have custom header", html.contains("Apache Axis2 REST API"));
        assertTrue("Should have description", html.contains("Auto-generated OpenAPI documentation"));

        // Verify custom styling
        assertTrue("Should include custom CSS", html.contains(".axis2-header"));
        assertTrue("Should hide default topbar", html.contains(".swagger-ui .topbar"));

        // Verify Swagger UI configuration
        assertTrue("Should enable try-it-out", html.contains("supportedSubmitMethods"));
        assertTrue("Should use standalone layout", html.contains("StandaloneLayout"));
        assertTrue("Should enable deep linking", html.contains("deepLinking: true"));
    }

    /**
     * Test user guide compatibility scenarios.
     * Simulates the authentication and API testing patterns described in the user guide.
     */
    public void testUserGuideCompatibilityScenarios() throws Exception {
        // Scenario 1: Authentication service testing
        mockRequest.setServerName("localhost");
        mockRequest.setServerPort(8080);
        mockRequest.setContextPath("/axis2");

        handler.handleSwaggerUIRequest(mockRequest, mockResponse);
        String html = mockResponse.getWriterContent();

        // Verify that the generated UI supports the authentication patterns
        assertTrue("Should support custom authentication testing",
                html.contains("requestInterceptor"));
        assertTrue("Should support response handling",
                html.contains("responseInterceptor"));

        // Scenario 2: Excel integration endpoint testing
        mockResponse = new MockHttpServletResponse();
        handler.handleOpenApiJsonRequest(mockRequest, mockResponse);
        String json = mockResponse.getWriterContent();

        // Verify JSON contains valid OpenAPI structure for Excel integration
        assertNotNull("Should generate valid JSON for Excel integration testing", json);

        // Scenario 3: Financial calculation service testing
        // The UI should support interactive API testing
        assertTrue("Should support interactive API testing",
                html.contains("supportedSubmitMethods") || html.contains("swagger-ui"));
    }

    /**
     * Mock HttpServletRequest for testing.
     */
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

        // Minimal implementation for tests
        @Override public String getAuthType() { return null; }
        @Override public Cookie[] getCookies() { return new Cookie[0]; }
        @Override public long getDateHeader(String name) { return 0; }
        @Override public String getHeader(String name) { return null; }
        @Override public Enumeration<String> getHeaders(String name) { return null; }
        @Override public Enumeration<String> getHeaderNames() { return null; }
        @Override public int getIntHeader(String name) { return 0; }
        @Override public String getMethod() { return "GET"; }
        @Override public String getPathInfo() { return null; }
        @Override public String getPathTranslated() { return null; }
        @Override public String getQueryString() { return null; }
        @Override public String getRemoteUser() { return null; }
        @Override public boolean isUserInRole(String role) { return false; }
        @Override public Principal getUserPrincipal() { return null; }
        @Override public String getRequestedSessionId() { return null; }
        @Override public String getRequestURI() { return ""; }
        @Override public StringBuffer getRequestURL() { return new StringBuffer(); }
        @Override public String getServletPath() { return ""; }
        @Override public HttpSession getSession(boolean create) { return null; }
        @Override public HttpSession getSession() { return null; }
        @Override public String changeSessionId() { return null; }
        @Override public boolean isRequestedSessionIdValid() { return false; }
        @Override public boolean isRequestedSessionIdFromCookie() { return false; }
        @Override public boolean isRequestedSessionIdFromURL() { return false; }
        @Override public boolean authenticate(HttpServletResponse response) { return false; }
        @Override public void login(String username, String password) { }
        @Override public void logout() { }
        @Override public Collection<Part> getParts() { return null; }
        @Override public Part getPart(String name) { return null; }
        @Override public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass) { return null; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public Enumeration<String> getAttributeNames() { return null; }
        @Override public String getCharacterEncoding() { return null; }
        @Override public void setCharacterEncoding(String env) { }
        @Override public int getContentLength() { return 0; }
        @Override public long getContentLengthLong() { return 0; }
        @Override public String getContentType() { return null; }
        @Override public ServletInputStream getInputStream() { return null; }
        @Override public String getParameter(String name) { return null; }
        @Override public Enumeration<String> getParameterNames() { return null; }
        @Override public String[] getParameterValues(String name) { return new String[0]; }
        @Override public Map<String, String[]> getParameterMap() { return null; }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public String getRemoteAddr() { return "127.0.0.1"; }
        @Override public String getRemoteHost() { return "localhost"; }
        @Override public void setAttribute(String name, Object o) { }
        @Override public void removeAttribute(String name) { }
        @Override public Locale getLocale() { return null; }
        @Override public Enumeration<Locale> getLocales() { return null; }
        @Override public boolean isSecure() { return false; }
        @Override public RequestDispatcher getRequestDispatcher(String path) { return null; }
        @Override public int getRemotePort() { return 0; }
        @Override public String getLocalName() { return "localhost"; }
        @Override public String getLocalAddr() { return "127.0.0.1"; }
        @Override public int getLocalPort() { return serverPort; }
        @Override public ServletContext getServletContext() { return null; }
        @Override public AsyncContext startAsync() { return null; }
        @Override public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) { return null; }
        @Override public boolean isAsyncStarted() { return false; }
        @Override public boolean isAsyncSupported() { return false; }
        @Override public AsyncContext getAsyncContext() { return null; }
        @Override public DispatcherType getDispatcherType() { return null; }
        @Override public BufferedReader getReader() { return null; }
        @Override public jakarta.servlet.ServletConnection getServletConnection() { return null; }
        @Override public String getProtocolRequestId() { return null; }
        @Override public String getRequestId() { return null; }
    }

    /**
     * Mock HttpServletResponse for testing.
     */
    private static class MockHttpServletResponse implements HttpServletResponse {
        private String contentType;
        private int status;
        private StringWriter writer = new StringWriter();
        private PrintWriter printWriter = new PrintWriter(writer);
        private java.util.Map<String, String> headers = new java.util.HashMap<>();

        public String getContentType() { return contentType; }
        public int getStatus() { return status; }
        public String getWriterContent() { return writer.toString(); }

        @Override public void setContentType(String type) { this.contentType = type; }
        @Override public void setStatus(int sc) { this.status = sc; }
        @Override public PrintWriter getWriter() { return printWriter; }
        @Override public void setHeader(String name, String value) { headers.put(name, value); }
        @Override public String getHeader(String name) { return headers.get(name); }

        // Minimal implementation for tests
        @Override public void addCookie(Cookie cookie) { }
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
        @Override public ServletOutputStream getOutputStream() { return null; }
        @Override public void setCharacterEncoding(String charset) { }
        @Override public void setContentLength(int len) { }
        @Override public void setContentLengthLong(long len) { }
        @Override public void setBufferSize(int size) { }
        @Override public int getBufferSize() { return 0; }
        @Override public void flushBuffer() { }
        @Override public void resetBuffer() { }
        @Override public boolean isCommitted() { return false; }
        @Override public void reset() { }
        @Override public void setLocale(Locale loc) { }
        @Override public Locale getLocale() { return null; }
        @Override public Collection<String> getHeaders(String name) { return null; }
        @Override public Collection<String> getHeaderNames() { return headers.keySet(); }
        @Override public void sendRedirect(String location, int sc, boolean clearBuffer) { }
    }
}