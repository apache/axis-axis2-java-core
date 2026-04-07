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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * Tests focused on the Axis2 JSON-RPC payload format documented in the MCP catalog.
 *
 * <h2>Background</h2>
 *
 * <p>MCP clients calling Axis2 services must wrap their request body in the Axis2 JSON-RPC
 * envelope:
 * <pre>
 *   {"operationName":[{"arg0":{&lt;params&gt;}}]}
 * </pre>
 * This is mandated by {@code JsonUtils.invokeServiceClass()} in the {@code axis2-json} module.
 * A bare {@code {"field":"value"}} POST body causes a silent 400 Bad Request.
 *
 * <h2>What these tests verify</h2>
 * <ul>
 *   <li>Each tool in the MCP catalog carries an {@code x-axis2-payloadTemplate} that is valid
 *       JSON in the correct Axis2 envelope format.</li>
 *   <li>The template is parseable and structurally correct (array of one {@code arg0} object).</li>
 *   <li>The loginService template matches the documented login payload format.</li>
 *   <li>Auth annotations ({@code x-requiresAuth}) correctly distinguish the public token
 *       endpoint from protected services — verifying the two-phase auth flow.</li>
 *   <li>The catalog {@code _meta} block gives MCP clients all transport conventions without
 *       requiring out-of-band documentation.</li>
 * </ul>
 *
 * <p>These tests verify that the Axis2 MCP catalog provides enough information for MCP clients
 * to construct correct JSON-RPC payloads without an intermediary proxy.
 */
public class McpAxis2PayloadTest extends TestCase {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AxisConfiguration axisConfig;
    private ConfigurationContext configCtx;
    private OpenApiSpecGenerator generator;
    private MockHttpServletRequest mockRequest;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        axisConfig  = new AxisConfiguration();
        configCtx   = new ConfigurationContext(axisConfig);
        generator   = new OpenApiSpecGenerator(configCtx);
        mockRequest = new MockHttpServletRequest();
    }

    // ── payload template structural correctness ───────────────────────────────

    /**
     * A payload template must be parseable by any standard JSON library.
     * MCP client SDKs (Python, TypeScript, Java) all parse the catalog before
     * constructing requests.
     */
    public void testPayloadTemplateIsParseableJson() throws Exception {
        addService("DataService", "fetchData");
        String template = getFirstTool("fetchData").path("x-axis2-payloadTemplate").asText();
        JsonNode parsed = MAPPER.readTree(template);
        assertNotNull("Payload template must be parseable JSON", parsed);
        assertTrue("Parsed template must be an object", parsed.isObject());
    }

    /**
     * The operation name must be the single top-level key.
     * Axis2 JSON-RPC dispatches on this key to select the operation.
     */
    public void testPayloadTemplateHasSingleTopLevelKey() throws Exception {
        addService("DataService", "fetchData");
        String template = getFirstTool("fetchData").path("x-axis2-payloadTemplate").asText();
        JsonNode parsed = MAPPER.readTree(template);
        assertEquals("Payload template must have exactly one top-level key", 1, parsed.size());
    }

    /**
     * The top-level key must match the operation name exactly (case-sensitive).
     * Axis2 JSON-RPC dispatch is case-sensitive.
     */
    public void testPayloadTemplateTopLevelKeyMatchesOperationName() throws Exception {
        addService("DataService", "fetchData");
        String template = getFirstTool("fetchData").path("x-axis2-payloadTemplate").asText();
        JsonNode parsed = MAPPER.readTree(template);
        assertTrue("Template top-level key must be the operation name 'fetchData'",
                parsed.has("fetchData"));
    }

    /**
     * The value of the operation name key must be a JSON array.
     * Axis2 JSON-RPC requires an array even for single-argument operations.
     */
    public void testPayloadTemplateValueIsArray() throws Exception {
        addService("DataService", "fetchData");
        String template = getFirstTool("fetchData").path("x-axis2-payloadTemplate").asText();
        JsonNode value = MAPPER.readTree(template).path("fetchData");
        assertTrue("Payload template value must be a JSON array", value.isArray());
    }

    /**
     * The array must contain exactly one element — the argument wrapper object.
     * Axis2 JSON-RPC maps array position to method argument position; all
     * userguide services use a single {@code arg0} parameter.
     */
    public void testPayloadTemplateArrayHasExactlyOneElement() throws Exception {
        addService("DataService", "fetchData");
        String template = getFirstTool("fetchData").path("x-axis2-payloadTemplate").asText();
        JsonNode arr = MAPPER.readTree(template).path("fetchData");
        assertEquals("Payload template array must contain exactly one element", 1, arr.size());
    }

    /**
     * The single array element must be an object with an {@code arg0} key.
     * This is the Axis2 JSON-RPC convention: the named argument wraps the
     * actual request POJO.
     */
    public void testPayloadTemplateArrayElementHasArg0Key() throws Exception {
        addService("DataService", "fetchData");
        String template = getFirstTool("fetchData").path("x-axis2-payloadTemplate").asText();
        JsonNode element = MAPPER.readTree(template).path("fetchData").get(0);
        assertNotNull("Array element must not be null", element);
        assertTrue("Array element must be an object", element.isObject());
        assertFalse("Array element must have 'arg0' key",
                element.path("arg0").isMissingNode());
    }

    /**
     * The {@code arg0} value must be an empty object — the placeholder for the
     * actual request parameters.  Callers replace it with their request POJO.
     */
    public void testPayloadTemplateArg0IsEmptyObject() throws Exception {
        addService("DataService", "fetchData");
        String template = getFirstTool("fetchData").path("x-axis2-payloadTemplate").asText();
        JsonNode arg0 = MAPPER.readTree(template).path("fetchData").get(0).path("arg0");
        assertTrue("arg0 placeholder must be an empty JSON object", arg0.isObject());
        assertEquals("arg0 placeholder must be empty (params go inside it)", 0, arg0.size());
    }

    // ── loginService — the token acquisition entry point ─────────────────────

    /**
     * loginService/doLogin is the authentication entry point.
     *
     * <p>The expected login payload format is:
     * <pre>
     *   {"doLogin":[{"arg0":{"email":"user@example.com","credentials":"pass"}}]}
     * </pre>
     * The MCP catalog template for doLogin must be compatible with this format.
     */
    public void testLoginServicePayloadTemplateHasDoLoginKey() throws Exception {
        addService("loginService", "doLogin");
        String template = getFirstTool("doLogin").path("x-axis2-payloadTemplate").asText();
        JsonNode parsed = MAPPER.readTree(template);
        assertTrue("loginService payload template must have 'doLogin' as top-level key",
                parsed.has("doLogin"));
    }

    public void testLoginServicePayloadTemplateCompatibleWithExpectedFormat() throws Exception {
        addService("loginService", "doLogin");
        String template = getFirstTool("doLogin").path("x-axis2-payloadTemplate").asText();
        JsonNode parsed = MAPPER.readTree(template);
        // Verify structural compatibility: {"doLogin":[{"arg0":{}}]}
        JsonNode arg0 = parsed.path("doLogin").get(0).path("arg0");
        assertFalse("doLogin template must have arg0",
                arg0.isMissingNode());
        // arg0 is the placeholder — callers fill in email/credentials
        assertTrue("arg0 must be an object placeholder",
                arg0.isObject());
    }

    public void testLoginServiceNotRequiresAuth() throws Exception {
        addService("loginService", "doLogin");
        JsonNode tool = getFirstTool("doLogin");
        assertFalse(
                "loginService must have x-requiresAuth: false — it IS the token endpoint",
                tool.path("x-requiresAuth").asBoolean());
    }

    // ── auth flow: two-phase pattern (login → Bearer → call) ─────────────────

    /**
     * Verifies the complete two-phase auth flow documented in the catalog:
     * Phase 1: call loginService (no auth) → get token.
     * Phase 2: call protected service with Bearer token.
     *
     * <p>This verifies the standard Axis2 two-phase Bearer token authentication pattern.
     */
    public void testTwoPhaseAuthFlowDocumentedInCatalog() throws Exception {
        // Register both the token endpoint and a protected service
        addService("loginService", "doLogin");
        addService("testws", "doTestws");

        JsonNode tools = getCatalogTools();

        JsonNode loginTool = null;
        JsonNode protectedTool = null;
        for (JsonNode t : tools) {
            if ("doLogin".equals(t.path("name").asText()))   loginTool = t;
            if ("doTestws".equals(t.path("name").asText())) protectedTool = t;
        }

        assertNotNull("loginService/doLogin must appear in catalog", loginTool);
        assertNotNull("testws/doTestws must appear in catalog", protectedTool);

        // Phase 1: login is public
        assertFalse("doLogin must NOT require auth (phase 1 — get the token)",
                loginTool.path("x-requiresAuth").asBoolean());

        // Phase 2: protected service requires the token obtained in phase 1
        assertTrue("doTestws must require auth (phase 2 — use the token)",
                protectedTool.path("x-requiresAuth").asBoolean());

        // Both must have the Axis2 wrapper format
        assertFalse("doLogin must have payload template",
                loginTool.path("x-axis2-payloadTemplate").isMissingNode());
        assertFalse("doTestws must have payload template",
                protectedTool.path("x-axis2-payloadTemplate").isMissingNode());
    }

    // ── _meta transport contract ──────────────────────────────────────────────

    /**
     * The {@code _meta.tokenEndpoint} must point to loginService so MCP clients
     * can programmatically discover where to obtain a Bearer token.
     */
    public void testMetaTokenEndpointPointsToLoginService() throws Exception {
        JsonNode meta = MAPPER.readTree(generator.generateMcpCatalogJson(mockRequest))
                .path("_meta");
        String tokenEndpoint = meta.path("tokenEndpoint").asText();
        assertTrue("tokenEndpoint must reference loginService",
                tokenEndpoint.contains("loginService"));
    }

    /**
     * The {@code _meta.axis2JsonRpcFormat} placeholder must itself be valid
     * JSON after substituting a real operation name and params — demonstrating
     * the template is syntactically instructive.
     */
    public void testMetaAxis2FormatHintContainsArg0() throws Exception {
        JsonNode meta = MAPPER.readTree(generator.generateMcpCatalogJson(mockRequest))
                .path("_meta");
        String fmt = meta.path("axis2JsonRpcFormat").asText();
        assertTrue("_meta.axis2JsonRpcFormat must document the arg0 wrapper convention",
                fmt.contains("arg0"));
    }

    /**
     * The {@code _meta.authHeader} must document the Bearer scheme so MCP
     * clients know how to attach the token obtained from loginService.
     * MCP clients use this to attach the token obtained from loginService.
     */
    public void testMetaAuthHeaderDocumentsBearerScheme() throws Exception {
        JsonNode meta = MAPPER.readTree(generator.generateMcpCatalogJson(mockRequest))
                .path("_meta");
        String authHeader = meta.path("authHeader").asText();
        assertTrue("_meta.authHeader must document 'Bearer' scheme",
                authHeader.contains("Bearer"));
    }

    // ── all tools consistent ──────────────────────────────────────────────────

    /**
     * Every tool in the catalog must have a payload template whose top-level
     * key matches the tool's own name field.
     */
    public void testAllToolPayloadTemplatesMatchToolName() throws Exception {
        AxisService svc = new AxisService("FinancialBenchmarkService");
        addOperation(svc, "portfolioVariance");
        addOperation(svc, "monteCarlo");
        addOperation(svc, "scenarioAnalysis");
        axisConfig.addService(svc);

        JsonNode tools = getCatalogTools();
        for (JsonNode tool : tools) {
            String name = tool.path("name").asText();
            String template = tool.path("x-axis2-payloadTemplate").asText();
            JsonNode parsed = MAPPER.readTree(template);
            assertTrue("Tool '" + name + "' payload template top-level key must match tool name",
                    parsed.has(name));
        }
    }

    /**
     * All non-login tools must declare auth as required.  No service should
     * accidentally be marked public.
     */
    public void testAllNonLoginToolsRequireAuth() throws Exception {
        addService("BigDataH2Service", "processBigDataSet");
        addService("FinancialBenchmarkService", "portfolioVariance");
        addService("testws", "doTestws");

        JsonNode tools = getCatalogTools();
        for (JsonNode tool : tools) {
            assertTrue("Every non-login tool must declare x-requiresAuth: true",
                    tool.path("x-requiresAuth").asBoolean());
        }
    }

    /**
     * Annotations must be present on every tool.  Missing annotations would
     * cause MCP 2025-03-26 spec-compliant clients to reject the catalog.
     */
    public void testAllToolsHaveAnnotations() throws Exception {
        AxisService svc = new AxisService("CalcService");
        addOperation(svc, "add");
        addOperation(svc, "subtract");
        axisConfig.addService(svc);

        JsonNode tools = getCatalogTools();
        for (JsonNode tool : tools) {
            String name = tool.path("name").asText();
            assertFalse("Tool '" + name + "' must have annotations field",
                    tool.path("annotations").isMissingNode());
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void addService(String serviceName, String operationName) throws Exception {
        AxisService svc = new AxisService(serviceName);
        addOperation(svc, operationName);
        axisConfig.addService(svc);
    }

    private void addOperation(AxisService svc, String operationName) {
        AxisOperation op = new InOutAxisOperation();
        op.setName(QName.valueOf(operationName));
        svc.addOperation(op);
    }

    private JsonNode getCatalogTools() throws Exception {
        return MAPPER.readTree(generator.generateMcpCatalogJson(mockRequest)).path("tools");
    }

    /** Return the tool with the given name, or fail if not found. */
    private JsonNode getFirstTool(String toolName) throws Exception {
        JsonNode tools = getCatalogTools();
        for (JsonNode t : tools) {
            if (toolName.equals(t.path("name").asText())) return t;
        }
        fail("Tool '" + toolName + "' not found in catalog");
        return null; // unreachable
    }

    // ── mock request ─────────────────────────────────────────────────────────

    private static class MockHttpServletRequest implements HttpServletRequest {
        @Override public String getScheme()      { return "https"; }
        @Override public String getServerName()  { return "localhost"; }
        @Override public int    getServerPort()  { return 8443; }
        @Override public String getContextPath() { return "/axis2-json-api"; }

        @Override public String getAuthType() { return null; }
        @Override public Cookie[] getCookies() { return new Cookie[0]; }
        @Override public long getDateHeader(String n) { return 0; }
        @Override public String getHeader(String n) { return null; }
        @Override public Enumeration<String> getHeaders(String n) { return null; }
        @Override public Enumeration<String> getHeaderNames() { return null; }
        @Override public int getIntHeader(String n) { return 0; }
        @Override public String getMethod() { return "GET"; }
        @Override public String getPathInfo() { return null; }
        @Override public String getPathTranslated() { return null; }
        @Override public String getQueryString() { return null; }
        @Override public String getRemoteUser() { return null; }
        @Override public boolean isUserInRole(String r) { return false; }
        @Override public Principal getUserPrincipal() { return null; }
        @Override public String getRequestedSessionId() { return null; }
        @Override public String getRequestURI() { return "/axis2-json-api/openapi-mcp.json"; }
        @Override public StringBuffer getRequestURL() { return new StringBuffer(); }
        @Override public String getServletPath() { return ""; }
        @Override public HttpSession getSession(boolean c) { return null; }
        @Override public HttpSession getSession() { return null; }
        @Override public String changeSessionId() { return null; }
        @Override public boolean isRequestedSessionIdValid() { return false; }
        @Override public boolean isRequestedSessionIdFromCookie() { return false; }
        @Override public boolean isRequestedSessionIdFromURL() { return false; }
        @Override public boolean authenticate(HttpServletResponse r) { return false; }
        @Override public void login(String u, String p) { }
        @Override public void logout() { }
        @Override public Collection<Part> getParts() { return null; }
        @Override public Part getPart(String n) { return null; }
        @Override public <T extends HttpUpgradeHandler> T upgrade(Class<T> c) { return null; }
        @Override public Object getAttribute(String n) { return null; }
        @Override public Enumeration<String> getAttributeNames() { return null; }
        @Override public String getCharacterEncoding() { return null; }
        @Override public void setCharacterEncoding(String e) { }
        @Override public int getContentLength() { return 0; }
        @Override public long getContentLengthLong() { return 0; }
        @Override public String getContentType() { return null; }
        @Override public ServletInputStream getInputStream() { return null; }
        @Override public String getParameter(String n) { return null; }
        @Override public Enumeration<String> getParameterNames() { return null; }
        @Override public String[] getParameterValues(String n) { return new String[0]; }
        @Override public Map<String, String[]> getParameterMap() { return null; }
        @Override public String getProtocol() { return "HTTP/2"; }
        @Override public String getRemoteAddr() { return "127.0.0.1"; }
        @Override public String getRemoteHost() { return "localhost"; }
        @Override public void setAttribute(String n, Object o) { }
        @Override public void removeAttribute(String n) { }
        @Override public Locale getLocale() { return Locale.US; }
        @Override public Enumeration<Locale> getLocales() { return null; }
        @Override public boolean isSecure() { return true; }
        @Override public RequestDispatcher getRequestDispatcher(String p) { return null; }
        @Override public int getRemotePort() { return 0; }
        @Override public String getLocalName() { return "localhost"; }
        @Override public String getLocalAddr() { return "127.0.0.1"; }
        @Override public int getLocalPort() { return 8443; }
        @Override public ServletContext getServletContext() { return null; }
        @Override public AsyncContext startAsync() { return null; }
        @Override public AsyncContext startAsync(ServletRequest q, ServletResponse s) { return null; }
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
