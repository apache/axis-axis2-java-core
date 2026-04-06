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
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.RequestBody;
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

    // ========== Service / Operation Exclusion Tests ==========

    /**
     * Test that a service listed in ignoredServices is omitted from the spec.
     */
    public void testIgnoredServiceIsExcluded() throws Exception {
        // Arrange
        AxisService visible = new AxisService("PublicService");
        AxisOperation visibleOp = new org.apache.axis2.description.InOutAxisOperation();
        visibleOp.setName(javax.xml.namespace.QName.valueOf("getData"));
        visible.addOperation(visibleOp);

        AxisService hidden = new AxisService("InternalService");
        AxisOperation hiddenOp = new org.apache.axis2.description.InOutAxisOperation();
        hiddenOp.setName(javax.xml.namespace.QName.valueOf("secretOp"));
        hidden.addOperation(hiddenOp);

        axisConfiguration.addService(visible);
        axisConfiguration.addService(hidden);

        OpenApiConfiguration config = new OpenApiConfiguration();
        config.addIgnoredService("InternalService");
        OpenApiSpecGenerator filtered = new OpenApiSpecGenerator(configurationContext, config);

        // Act
        OpenAPI openApi = filtered.generateOpenApiSpec(mockRequest);

        // Assert
        assertNotNull(openApi.getPaths());
        assertNotNull("Public service path should be present",
                openApi.getPaths().get("/services/PublicService/getData"));
        assertNull("Hidden service path must be absent",
                openApi.getPaths().get("/services/InternalService/secretOp"));
    }

    /**
     * Test that a qualified "ServiceName/operationName" entry in ignoredOperations
     * removes only that specific operation, leaving other operations on the same
     * service in the spec.
     */
    public void testIgnoredQualifiedOperationIsExcluded() throws Exception {
        // Arrange
        AxisService svc = new AxisService("DataService");

        AxisOperation keep = new org.apache.axis2.description.InOutAxisOperation();
        keep.setName(javax.xml.namespace.QName.valueOf("publicQuery"));
        svc.addOperation(keep);

        AxisOperation drop = new org.apache.axis2.description.InOutAxisOperation();
        drop.setName(javax.xml.namespace.QName.valueOf("adminDump"));
        svc.addOperation(drop);

        axisConfiguration.addService(svc);

        OpenApiConfiguration config = new OpenApiConfiguration();
        config.addIgnoredOperation("DataService/adminDump");
        OpenApiSpecGenerator filtered = new OpenApiSpecGenerator(configurationContext, config);

        // Act
        OpenAPI openApi = filtered.generateOpenApiSpec(mockRequest);

        // Assert
        assertNotNull("publicQuery must remain", openApi.getPaths().get("/services/DataService/publicQuery"));
        assertNull("adminDump must be excluded", openApi.getPaths().get("/services/DataService/adminDump"));
    }

    /**
     * Test that a bare operation name (no service prefix) in ignoredOperations
     * suppresses that operation across every service.
     */
    public void testIgnoredBareOperationIsExcludedAcrossAllServices() throws Exception {
        // Arrange — two services each with the banned operation name
        AxisService svc1 = new AxisService("ServiceAlpha");
        AxisOperation alpha_good = new org.apache.axis2.description.InOutAxisOperation();
        alpha_good.setName(javax.xml.namespace.QName.valueOf("doWork"));
        svc1.addOperation(alpha_good);
        AxisOperation alpha_bad = new org.apache.axis2.description.InOutAxisOperation();
        alpha_bad.setName(javax.xml.namespace.QName.valueOf("internalStatus"));
        svc1.addOperation(alpha_bad);

        AxisService svc2 = new AxisService("ServiceBeta");
        AxisOperation beta_good = new org.apache.axis2.description.InOutAxisOperation();
        beta_good.setName(javax.xml.namespace.QName.valueOf("doWork"));
        svc2.addOperation(beta_good);
        AxisOperation beta_bad = new org.apache.axis2.description.InOutAxisOperation();
        beta_bad.setName(javax.xml.namespace.QName.valueOf("internalStatus"));
        svc2.addOperation(beta_bad);

        axisConfiguration.addService(svc1);
        axisConfiguration.addService(svc2);

        OpenApiConfiguration config = new OpenApiConfiguration();
        config.addIgnoredOperation("internalStatus"); // bare name — applies to both services
        OpenApiSpecGenerator filtered = new OpenApiSpecGenerator(configurationContext, config);

        // Act
        OpenAPI openApi = filtered.generateOpenApiSpec(mockRequest);

        // Assert — doWork present on both, internalStatus absent on both
        assertNotNull(openApi.getPaths().get("/services/ServiceAlpha/doWork"));
        assertNotNull(openApi.getPaths().get("/services/ServiceBeta/doWork"));
        assertNull("internalStatus must be absent from ServiceAlpha",
                openApi.getPaths().get("/services/ServiceAlpha/internalStatus"));
        assertNull("internalStatus must be absent from ServiceBeta",
                openApi.getPaths().get("/services/ServiceBeta/internalStatus"));
    }

    /**
     * Test that ignoredServices and ignoredOperations are populated correctly
     * via the programmatic API (the same code path exercised by properties loading
     * once applyPropertiesConfiguration parses the values).
     */
    public void testIgnoredServicesAndOperationsProgrammaticAPI() throws Exception {
        OpenApiConfiguration config = new OpenApiConfiguration();
        config.addIgnoredService("SecretService");
        config.addIgnoredService("HiddenService");
        config.addIgnoredOperation("AdminService/nukeDatabase");
        config.addIgnoredOperation("debugPing");

        assertTrue("SecretService should be in ignoredServices",
                config.getIgnoredServices().contains("SecretService"));
        assertTrue("HiddenService should be in ignoredServices",
                config.getIgnoredServices().contains("HiddenService"));
        assertTrue("AdminService/nukeDatabase should be in ignoredOperations",
                config.getIgnoredOperations().contains("AdminService/nukeDatabase"));
        assertTrue("debugPing should be in ignoredOperations",
                config.getIgnoredOperations().contains("debugPing"));

        // Verify copy() preserves exclusion sets
        OpenApiConfiguration copy = config.copy();
        assertTrue("copy must preserve ignoredServices",
                copy.getIgnoredServices().contains("SecretService"));
        assertTrue("copy must preserve ignoredOperations",
                copy.getIgnoredOperations().contains("debugPing"));
    }

    /**
     * Test that a qualified operation entry does NOT suppress the same operation
     * name on a different service (targeted exclusion, not global).
     */
    public void testQualifiedIgnoredOperationDoesNotAffectOtherServices() throws Exception {
        // Arrange
        AxisService svcA = new AxisService("ServiceA");
        AxisOperation opA = new org.apache.axis2.description.InOutAxisOperation();
        opA.setName(javax.xml.namespace.QName.valueOf("sensitiveOp"));
        svcA.addOperation(opA);

        AxisService svcB = new AxisService("ServiceB");
        AxisOperation opB = new org.apache.axis2.description.InOutAxisOperation();
        opB.setName(javax.xml.namespace.QName.valueOf("sensitiveOp")); // same name, different service
        svcB.addOperation(opB);

        axisConfiguration.addService(svcA);
        axisConfiguration.addService(svcB);

        OpenApiConfiguration config = new OpenApiConfiguration();
        config.addIgnoredOperation("ServiceA/sensitiveOp"); // target ServiceA only
        OpenApiSpecGenerator filtered = new OpenApiSpecGenerator(configurationContext, config);

        // Act
        OpenAPI openApi = filtered.generateOpenApiSpec(mockRequest);

        // Assert
        assertNull("ServiceA/sensitiveOp must be excluded",
                openApi.getPaths().get("/services/ServiceA/sensitiveOp"));
        assertNotNull("ServiceB/sensitiveOp must remain (different service)",
                openApi.getPaths().get("/services/ServiceB/sensitiveOp"));
    }

    /**
     * Test that generated JSON contains no null fields.
     * Jackson must be configured with Include.NON_NULL so null-valued model
     * fields (e.g. termsOfService, extensions, summary) are omitted entirely.
     */
    public void testNoNullFieldsInJson() throws Exception {
        String json = generator.generateOpenApiJson(mockRequest);

        assertFalse("JSON output must not contain ': null' entries", json.contains(": null"));
        assertFalse("JSON output must not contain ':null' entries", json.contains(":null"));
    }

    /**
     * Test that each generated operation carries a non-null requestBody.
     * All JSON-RPC services accept a POST body; omitting requestBody leaves
     * clients with no schema hint.  Mirrors the pattern in financial-api-schema.json.
     */
    public void testRequestBodyPresentOnOperation() throws Exception {
        // Arrange — register a service with one operation
        AxisService svc = new AxisService("OrderService");
        AxisOperation op = new org.apache.axis2.description.InOutAxisOperation();
        op.setName(javax.xml.namespace.QName.valueOf("placeOrder"));
        svc.addOperation(op);
        axisConfiguration.addService(svc);

        // Act
        OpenAPI openApi = generator.generateOpenApiSpec(mockRequest);

        // Assert — the path for the operation must exist and have a requestBody
        String expectedPath = "/services/OrderService/placeOrder";
        assertNotNull("Path should exist for registered operation", openApi.getPaths());
        PathItem pathItem = openApi.getPaths().get(expectedPath);
        assertNotNull("PathItem must be present at " + expectedPath, pathItem);

        Operation postOp = pathItem.getPost();
        assertNotNull("Operation must be a POST", postOp);

        RequestBody requestBody = postOp.getRequestBody();
        assertNotNull("requestBody must not be null", requestBody);
        assertTrue("requestBody must be required", Boolean.TRUE.equals(requestBody.getRequired()));
        assertNotNull("requestBody must have content", requestBody.getContent());
        assertNotNull("requestBody must declare application/json media type",
                requestBody.getContent().get("application/json"));
    }

    /**
     * Test that generated YAML is genuine YAML, not JSON.
     * financial-api-schema.json demonstrates that a proper OpenAPI endpoint
     * should serve parseable YAML when /openapi.yaml is requested.
     */
    public void testYamlGenerationIsActualYaml() throws Exception {
        String yaml = generator.generateOpenApiYaml(mockRequest);

        assertNotNull("YAML should be generated", yaml);
        assertFalse("YAML must not start with '{' (that would be JSON)", yaml.trim().startsWith("{"));
        assertTrue("YAML must contain openapi key in YAML style", yaml.contains("openapi:"));
    }

    /**
     * Test that the financial-api-schema.json advanced features are structurally
     * sound — components/schemas with $ref, required requestBodies, security
     * schemes, error responses, and both GET and POST operations.
     *
     * This test reads the schema from disk and validates its advanced features,
     * confirming the test infrastructure can parse and assert on production-grade
     * OpenAPI specs of the kind the generator should eventually produce.
     */
    public void testFinancialApiSchemaAdvancedFeatures() throws Exception {
        // Load the financial schema from the swagger-server sample resources
        java.io.InputStream is = getClass().getClassLoader()
                .getResourceAsStream("openapi/financial-api-schema.json");
        if (is == null) {
            // File is in the swagger-server module, not on this module's classpath —
            // load it from the filesystem relative to the repo root.
            java.io.File schemaFile = new java.io.File(
                "../../samples/swagger-server/src/main/resources/openapi/financial-api-schema.json");
            if (!schemaFile.exists()) {
                // Skip gracefully when running outside the full repo checkout
                return;
            }
            is = new java.io.FileInputStream(schemaFile);
        }

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(is);

        // --- Basic version ---
        assertEquals("openapi version must be 3.0.1", "3.0.1", root.get("openapi").asText());

        // --- Components/schemas: advanced feature — schema definitions with $ref ---
        com.fasterxml.jackson.databind.JsonNode schemas = root.path("components").path("schemas");
        assertFalse("components/schemas must be present", schemas.isMissingNode());
        assertTrue("LoginRequest schema must be defined", schemas.has("LoginRequest"));
        assertTrue("LoginResponse schema must be defined", schemas.has("LoginResponse"));

        // LoginRequest must declare required fields
        com.fasterxml.jackson.databind.JsonNode loginReqRequired = schemas.path("LoginRequest").path("required");
        assertFalse("LoginRequest must have required array", loginReqRequired.isMissingNode());
        assertTrue("LoginRequest required must include 'email'",
                loginReqRequired.toString().contains("email"));

        // --- $ref usage inside a schema ---
        com.fasterxml.jackson.databind.JsonNode loginRespUserInfo =
                schemas.path("LoginResponse").path("properties").path("userInfo");
        assertFalse("LoginResponse.userInfo must be present", loginRespUserInfo.isMissingNode());
        assertTrue("LoginResponse.userInfo must use $ref",
                loginRespUserInfo.has("$ref"));

        // --- Security schemes ---
        com.fasterxml.jackson.databind.JsonNode securitySchemes =
                root.path("components").path("securitySchemes");
        assertFalse("securitySchemes must be present", securitySchemes.isMissingNode());
        assertTrue("bearerAuth scheme must be defined", securitySchemes.has("bearerAuth"));
        assertEquals("bearerAuth type must be 'http'",
                "http", securitySchemes.path("bearerAuth").path("type").asText());
        assertEquals("bearerAuth scheme must be 'bearer'",
                "bearer", securitySchemes.path("bearerAuth").path("scheme").asText());

        // --- requestBody required on POST operations ---
        com.fasterxml.jackson.databind.JsonNode loginPath = root.path("paths").path("/bigdataservice/login");
        assertFalse("login path must be present", loginPath.isMissingNode());
        com.fasterxml.jackson.databind.JsonNode loginPost = loginPath.path("post");
        assertFalse("login POST must be present", loginPost.isMissingNode());
        assertTrue("login POST requestBody must be required",
                loginPost.path("requestBody").path("required").asBoolean());

        // --- Error responses (400 / 401) ---
        com.fasterxml.jackson.databind.JsonNode loginResponses = loginPost.path("responses");
        assertTrue("login must declare 401 response", loginResponses.has("401"));

        // --- GET operations (user/info, user/permissions) ---
        com.fasterxml.jackson.databind.JsonNode userInfoPath = root.path("paths").path("/bigdataservice/user/info");
        assertFalse("user/info path must be present", userInfoPath.isMissingNode());
        assertFalse("user/info must be a GET operation", userInfoPath.path("get").isMissingNode());

        // --- Operation-level security (distinct from global) ---
        com.fasterxml.jackson.databind.JsonNode fundsSecurity =
                root.path("paths").path("/bigdataservice/funds/summary").path("post").path("security");
        assertFalse("funds/summary must declare per-operation security", fundsSecurity.isMissingNode());
        assertTrue("per-operation security must reference bearerAuth",
                fundsSecurity.toString().contains("bearerAuth"));
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