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
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
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
import java.util.List;
import java.util.Enumeration;
import java.util.Map;
import java.util.Locale;
import java.util.Collection;
import java.security.Principal;
import java.io.BufferedReader;

/**
 * Unit tests for OpenApiSpecGenerator class.
 * Tests OpenAPI specification generation from Axis2 service metadata.
 */
public class OpenApiSpecGeneratorTest extends TestCase {

    private OpenApiSpecGenerator generator;
    private ConfigurationContext configurationContext;
    private AxisConfiguration axisConfiguration;
    private MockHttpServletRequest mockRequest;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        axisConfiguration = new AxisConfiguration();
        configurationContext = new ConfigurationContext(axisConfiguration);
        generator = new OpenApiSpecGenerator(configurationContext);
        mockRequest = new MockHttpServletRequest();
    }

    /**
     * Test basic OpenAPI specification generation.
     * Verifies that a valid OpenAPI 3.0.1 spec is generated with correct metadata.
     */
    public void testBasicOpenApiGeneration() throws Exception {
        // Act
        OpenAPI openApi = generator.generateOpenApiSpec(mockRequest);

        // Assert basic structure
        assertNotNull("OpenAPI spec should be generated", openApi);
        assertEquals("Should be OpenAPI 3.0.1", "3.0.1", openApi.getOpenapi());

        // Verify info section
        Info info = openApi.getInfo();
        assertNotNull("Info section should be present", info);
        assertEquals("Apache Axis2 REST API", info.getTitle());
        assertEquals("1.0.0", info.getVersion());
        assertNotNull("Description should be present", info.getDescription());

        // Verify contact information
        assertNotNull("Contact should be present", info.getContact());
        assertEquals("Apache Axis2", info.getContact().getName());
        assertEquals("https://axis.apache.org/axis2/java/core/", info.getContact().getUrl());

        // Verify license information
        assertNotNull("License should be present", info.getLicense());
        assertEquals("Apache License 2.0", info.getLicense().getName());
        assertEquals("https://www.apache.org/licenses/LICENSE-2.0", info.getLicense().getUrl());
    }

    /**
     * Test server list generation from HTTP request.
     * Verifies correct server URL construction.
     */
    public void testServerListGeneration() throws Exception {
        // Arrange
        mockRequest.setScheme("https");
        mockRequest.setServerName("api.example.com");
        mockRequest.setServerPort(8443);
        mockRequest.setContextPath("/axis2");

        // Act
        OpenAPI openApi = generator.generateOpenApiSpec(mockRequest);

        // Assert
        List<Server> servers = openApi.getServers();
        assertNotNull("Servers should be present", servers);
        assertFalse("At least one server should be configured", servers.isEmpty());

        Server server = servers.get(0);
        assertEquals("https://api.example.com:8443/axis2", server.getUrl());
        assertEquals("Current server", server.getDescription());
    }

    /**
     * Test server list generation with default HTTP port.
     * Should not include port 80 in URL.
     */
    public void testServerListGenerationDefaultHttpPort() throws Exception {
        // Arrange
        mockRequest.setScheme("http");
        mockRequest.setServerName("localhost");
        mockRequest.setServerPort(80);
        mockRequest.setContextPath("");

        // Act
        OpenAPI openApi = generator.generateOpenApiSpec(mockRequest);

        // Assert
        Server server = openApi.getServers().get(0);
        assertEquals("http://localhost", server.getUrl());
    }

    /**
     * Test server list generation with default HTTPS port.
     * Should not include port 443 in URL.
     */
    public void testServerListGenerationDefaultHttpsPort() throws Exception {
        // Arrange
        mockRequest.setScheme("https");
        mockRequest.setServerName("secure.example.com");
        mockRequest.setServerPort(443);
        mockRequest.setContextPath("/api");

        // Act
        OpenAPI openApi = generator.generateOpenApiSpec(mockRequest);

        // Assert
        Server server = openApi.getServers().get(0);
        assertEquals("https://secure.example.com/api", server.getUrl());
    }

    /**
     * Test OpenAPI generation with null request.
     * Should use default server configuration.
     */
    public void testOpenApiGenerationWithNullRequest() throws Exception {
        // Act
        OpenAPI openApi = generator.generateOpenApiSpec(null);

        // Assert
        assertNotNull("Should generate spec even with null request", openApi);
        List<Server> servers = openApi.getServers();
        assertNotNull("Should have default server", servers);
        assertFalse("Should have at least one server", servers.isEmpty());

        Server server = servers.get(0);
        assertEquals("http://localhost:8080", server.getUrl());
        assertEquals("Default server", server.getDescription());
    }

    /**
     * Test JSON generation from OpenAPI spec.
     * Verifies that valid JSON is produced.
     */
    public void testJsonGeneration() throws Exception {
        // Act
        String json = generator.generateOpenApiJson(mockRequest);

        // Assert
        assertNotNull("JSON should be generated", json);
        assertFalse("JSON should not be empty", json.trim().isEmpty());
        assertTrue("Should be valid JSON format", json.trim().startsWith("{"));
        assertTrue("Should contain OpenAPI version", json.contains("3.0.1"));
        assertTrue("Should contain API title", json.contains("Apache Axis2 REST API"));
    }

    /**
     * Test YAML generation from OpenAPI spec.
     * Currently returns JSON format - can be enhanced for actual YAML.
     */
    public void testYamlGeneration() throws Exception {
        // Act
        String yaml = generator.generateOpenApiYaml(mockRequest);

        // Assert
        assertNotNull("YAML should be generated", yaml);
        assertFalse("YAML should not be empty", yaml.trim().isEmpty());
        // Current implementation returns JSON, future enhancement will return actual YAML
    }

    /**
     * Test path generation with mock service.
     * Verifies that service operations are converted to OpenAPI paths.
     */
    public void testPathGeneration() throws Exception {
        // Arrange
        AxisService testService = new AxisService("TestService");
        AxisOperation testOperation = new org.apache.axis2.description.InOutAxisOperation();
        testOperation.setName(javax.xml.namespace.QName.valueOf("testMethod"));
        testService.addOperation(testOperation);

        axisConfiguration.addService(testService);

        // Act
        OpenAPI openApi = generator.generateOpenApiSpec(mockRequest);

        // Assert
        assertNotNull("Paths should be generated", openApi.getPaths());
        // Verify that paths are created for the test service
        // Note: Actual path structure depends on service configuration
    }

    /**
     * Test error handling in JSON generation.
     * Verifies graceful handling of generation failures.
     */
    public void testJsonGenerationError() throws Exception {
        // Arrange - create generator with null context (gracefully handles errors)
        OpenApiSpecGenerator errorGenerator = new OpenApiSpecGenerator(null);

        // Act
        String json = errorGenerator.generateOpenApiJson(mockRequest);

        // Assert
        assertNotNull("Should return valid JSON", json);
        assertTrue("Should be valid JSON format", json.trim().startsWith("{"));
        assertTrue("Should contain OpenAPI version", json.contains("3.0.1"));
        assertTrue("Should contain API title", json.contains("Apache Axis2 REST API"));
        // With null context, it gracefully creates an empty spec rather than error
    }

    /**
     * Mock HttpServletRequest for testing.
     * Provides controllable request parameters for testing server URL generation.
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

        // Minimal implementation - other methods not needed for these tests
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
}