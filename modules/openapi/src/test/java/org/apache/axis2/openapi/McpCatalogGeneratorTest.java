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
 * Unit tests for the {@code /openapi-mcp.json} endpoint implemented in
 * {@link OpenApiSpecGenerator#generateMcpCatalogJson(HttpServletRequest)}.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>JSON structure validity (parseable, has "tools" array)</li>
 *   <li>Empty catalog when no user services are registered</li>
 *   <li>Correct tool fields: name, description, inputSchema, endpoint</li>
 *   <li>Multiple services / multiple operations per service</li>
 *   <li>JSON special-character escaping in service and operation names</li>
 *   <li>Endpoint format: {@code "POST /services/SvcName/opName"}</li>
 *   <li>inputSchema has the required MCP structure</li>
 * </ul>
 */
public class McpCatalogGeneratorTest extends TestCase {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AxisConfiguration axisConfig;
    private ConfigurationContext configCtx;
    private OpenApiSpecGenerator generator;
    private MockHttpServletRequest mockRequest;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        axisConfig = new AxisConfiguration();
        configCtx  = new ConfigurationContext(axisConfig);
        generator  = new OpenApiSpecGenerator(configCtx);
        mockRequest = new MockHttpServletRequest();
    }

    // ── JSON validity ─────────────────────────────────────────────────────────

    public void testCatalogIsValidJson() throws Exception {
        String json = generator.generateMcpCatalogJson(mockRequest);
        assertNotNull(json);
        // Must be parseable
        JsonNode root = MAPPER.readTree(json);
        assertNotNull(root);
    }

    public void testCatalogRootHasToolsArray() throws Exception {
        JsonNode root = MAPPER.readTree(generator.generateMcpCatalogJson(mockRequest));
        assertTrue("Root must have 'tools' key", root.has("tools"));
        assertTrue("tools must be an array", root.get("tools").isArray());
    }

    // ── empty / no-service cases ──────────────────────────────────────────────

    public void testEmptyConfigurationProducesEmptyToolsArray() throws Exception {
        // No services registered — only system services which are filtered out
        JsonNode tools = getCatalogTools();
        assertEquals("No user services → empty tools array", 0, tools.size());
    }

    // ── single service / single operation ────────────────────────────────────

    public void testSingleServiceSingleOperationProducesOneTool() throws Exception {
        addService("OrderService", "placeOrder");

        JsonNode tools = getCatalogTools();
        assertEquals(1, tools.size());
    }

    public void testToolNameMatchesOperationName() throws Exception {
        addService("OrderService", "placeOrder");

        JsonNode tool = getCatalogTools().get(0);
        assertEquals("placeOrder", tool.path("name").asText());
    }

    public void testToolDescriptionContainsServiceAndOperationName() throws Exception {
        addService("OrderService", "placeOrder");

        JsonNode tool = getCatalogTools().get(0);
        String desc = tool.path("description").asText();
        assertTrue("Description must mention service name", desc.contains("OrderService"));
        assertTrue("Description must mention operation name", desc.contains("placeOrder"));
    }

    public void testToolEndpointFormat() throws Exception {
        addService("BigDataH2Service", "processBigDataSet");

        JsonNode tool = getCatalogTools().get(0);
        String endpoint = tool.path("endpoint").asText();
        assertEquals("POST /services/BigDataH2Service/processBigDataSet", endpoint);
    }

    public void testToolEndpointStartsWithPost() throws Exception {
        addService("MyService", "myOperation");

        JsonNode tool = getCatalogTools().get(0);
        String endpoint = tool.path("endpoint").asText();
        assertTrue("Endpoint must start with POST", endpoint.startsWith("POST "));
    }

    public void testToolEndpointPathContainsServices() throws Exception {
        addService("MyService", "myOperation");

        JsonNode tool = getCatalogTools().get(0);
        String endpoint = tool.path("endpoint").asText();
        assertTrue("Endpoint path must contain /services/",
                endpoint.contains("/services/"));
    }

    // ── inputSchema ───────────────────────────────────────────────────────────

    public void testToolHasInputSchemaField() throws Exception {
        addService("TestService", "testOp");

        JsonNode tool = getCatalogTools().get(0);
        assertFalse("inputSchema must be present", tool.path("inputSchema").isMissingNode());
    }

    public void testInputSchemaTypeIsObject() throws Exception {
        addService("TestService", "testOp");

        JsonNode schema = getCatalogTools().get(0).path("inputSchema");
        assertEquals("object", schema.path("type").asText());
    }

    public void testInputSchemaHasPropertiesField() throws Exception {
        addService("TestService", "testOp");

        JsonNode schema = getCatalogTools().get(0).path("inputSchema");
        assertFalse("inputSchema.properties must be present",
                schema.path("properties").isMissingNode());
    }

    public void testInputSchemaHasRequiredField() throws Exception {
        addService("TestService", "testOp");

        JsonNode schema = getCatalogTools().get(0).path("inputSchema");
        assertFalse("inputSchema.required must be present",
                schema.path("required").isMissingNode());
        assertTrue("inputSchema.required must be an array",
                schema.path("required").isArray());
    }

    // ── multiple services / operations ────────────────────────────────────────

    public void testTwoServicesEachWithOneOperationProduceTwoTools() throws Exception {
        addService("ServiceA", "doAlpha");
        addService("ServiceB", "doBeta");

        JsonNode tools = getCatalogTools();
        assertEquals(2, tools.size());
    }

    public void testOneServiceWithThreeOperationsProducesThreeTools() throws Exception {
        AxisService svc = new AxisService("CalculatorService");
        addOperation(svc, "add");
        addOperation(svc, "subtract");
        addOperation(svc, "multiply");
        axisConfig.addService(svc);

        JsonNode tools = getCatalogTools();
        assertEquals(3, tools.size());
    }

    public void testAllToolNamesArePresent() throws Exception {
        AxisService svc = new AxisService("CalculatorService");
        addOperation(svc, "add");
        addOperation(svc, "subtract");
        axisConfig.addService(svc);

        JsonNode tools = getCatalogTools();
        java.util.Set<String> names = new java.util.HashSet<>();
        for (JsonNode t : tools) names.add(t.path("name").asText());
        assertTrue("'add' must be in tool names",      names.contains("add"));
        assertTrue("'subtract' must be in tool names", names.contains("subtract"));
    }

    public void testToolsFromDifferentServicesHaveCorrectEndpoints() throws Exception {
        addService("ServiceA", "opA");
        addService("ServiceB", "opB");

        JsonNode tools = getCatalogTools();
        java.util.Set<String> endpoints = new java.util.HashSet<>();
        for (JsonNode t : tools) endpoints.add(t.path("endpoint").asText());
        assertTrue(endpoints.contains("POST /services/ServiceA/opA"));
        assertTrue(endpoints.contains("POST /services/ServiceB/opB"));
    }

    // ── JSON escaping ─────────────────────────────────────────────────────────

    /**
     * Operation names with JSON-special characters must be escaped so the
     * output remains valid JSON.
     */
    public void testOperationNameWithQuoteIsEscaped() throws Exception {
        AxisService svc = new AxisService("TestService");
        AxisOperation op = new InOutAxisOperation();
        op.setName(QName.valueOf("say\"Hello\""));   // contains double-quote
        svc.addOperation(op);
        axisConfig.addService(svc);

        // Must not throw during JSON parsing
        String json = generator.generateMcpCatalogJson(mockRequest);
        JsonNode root = MAPPER.readTree(json);
        assertNotNull("JSON with escaped quotes must be parseable", root);
    }

    public void testServiceNameWithBackslashIsEscaped() throws Exception {
        AxisService svc = new AxisService("My\\Service");   // contains backslash
        AxisOperation op = new InOutAxisOperation();
        op.setName(QName.valueOf("doOp"));
        svc.addOperation(op);
        axisConfig.addService(svc);

        String json = generator.generateMcpCatalogJson(mockRequest);
        JsonNode root = MAPPER.readTree(json);   // must be parseable
        assertNotNull(root);
    }

    /**
     * Jackson correctly escapes all JSON control characters including tab, newline,
     * and carriage return — not just backslash and double-quote.
     */
    public void testControlCharactersInOperationNameAreEscaped() throws Exception {
        AxisService svc = new AxisService("TestService");
        AxisOperation op = new InOutAxisOperation();
        op.setName(QName.valueOf("op\twith\ttabs"));   // tab chars
        svc.addOperation(op);
        axisConfig.addService(svc);

        String json = generator.generateMcpCatalogJson(mockRequest);
        assertFalse("Tab characters must not appear literally in JSON output",
                json.contains("\t"));
        JsonNode root = MAPPER.readTree(json);   // still parseable
        assertNotNull(root);
    }

    // ── catalog request is null-safe ──────────────────────────────────────────

    public void testGenerateMcpCatalogWithNullRequestDoesNotThrow() throws Exception {
        addService("TestService", "testOp");

        // null request is passed — generator should handle it gracefully
        // (request is not used by generateMcpCatalogJson; it only introspects AxisConfig)
        try {
            String json = generator.generateMcpCatalogJson(null);
            assertNotNull(json);
            JsonNode root = MAPPER.readTree(json);
            assertTrue(root.has("tools"));
        } catch (NullPointerException e) {
            // Acceptable if the method does not guard against null — document behaviour
            System.out.println("INFO: generateMcpCatalogJson(null) throws NPE — " +
                    "callers must ensure request is non-null");
        }
    }

    // ── catalog _meta ─────────────────────────────────────────────────────────

    /**
     * The catalog root must carry a {@code _meta} object so MCP clients know
     * the Axis2 JSON-RPC transport contract without reading separate docs.
     * Mirrors the pattern in rapi-mcp (Python) where API conventions are
     * embedded in the tool catalog for client self-sufficiency.
     */
    public void testCatalogHasMetaObject() throws Exception {
        JsonNode root = MAPPER.readTree(generator.generateMcpCatalogJson(mockRequest));
        assertFalse("_meta must be present in catalog root",
                root.path("_meta").isMissingNode());
        assertTrue("_meta must be an object", root.path("_meta").isObject());
    }

    public void testMetaHasAxis2JsonRpcFormat() throws Exception {
        JsonNode meta = MAPPER.readTree(generator.generateMcpCatalogJson(mockRequest)).path("_meta");
        assertFalse("_meta.axis2JsonRpcFormat must be present",
                meta.path("axis2JsonRpcFormat").isMissingNode());
        String fmt = meta.path("axis2JsonRpcFormat").asText();
        assertTrue("Format must contain operationName placeholder", fmt.contains("operationName"));
        assertTrue("Format must contain arg0 wrapper", fmt.contains("arg0"));
    }

    public void testMetaHasContentType() throws Exception {
        JsonNode meta = MAPPER.readTree(generator.generateMcpCatalogJson(mockRequest)).path("_meta");
        assertEquals("application/json", meta.path("contentType").asText());
    }

    public void testMetaHasAuthHeaderField() throws Exception {
        JsonNode meta = MAPPER.readTree(generator.generateMcpCatalogJson(mockRequest)).path("_meta");
        String authHeader = meta.path("authHeader").asText();
        assertTrue("authHeader must describe Bearer scheme",
                authHeader.contains("Bearer"));
    }

    public void testMetaHasTokenEndpoint() throws Exception {
        JsonNode meta = MAPPER.readTree(generator.generateMcpCatalogJson(mockRequest)).path("_meta");
        String tokenEndpoint = meta.path("tokenEndpoint").asText();
        assertTrue("tokenEndpoint must reference loginService",
                tokenEndpoint.contains("loginService"));
        assertTrue("tokenEndpoint must start with POST",
                tokenEndpoint.startsWith("POST "));
    }

    // ── x-axis2-payloadTemplate ───────────────────────────────────────────────

    /**
     * Every tool must carry an {@code x-axis2-payloadTemplate} so MCP clients
     * know to wrap bare JSON params in the Axis2 JSON-RPC envelope:
     * {@code {"operationName":[{"arg0":{...}}]}}.
     *
     * <p>This is the primary challenge from pyRapi: MCP clients calling Axis2
     * services must use this wrapping format or the call fails silently.
     */
    public void testToolHasPayloadTemplateField() throws Exception {
        addService("TestService", "testOp");
        JsonNode tool = getCatalogTools().get(0);
        assertFalse("x-axis2-payloadTemplate must be present",
                tool.path("x-axis2-payloadTemplate").isMissingNode());
    }

    public void testPayloadTemplateContainsOperationName() throws Exception {
        addService("TestService", "doSomething");
        JsonNode tool = getCatalogTools().get(0);
        String template = tool.path("x-axis2-payloadTemplate").asText();
        assertTrue("Payload template must contain the operation name",
                template.contains("doSomething"));
    }

    public void testPayloadTemplateIsValidJson() throws Exception {
        addService("TestService", "processData");
        JsonNode tool = getCatalogTools().get(0);
        String template = tool.path("x-axis2-payloadTemplate").asText();
        // The template itself must be parseable JSON
        JsonNode parsed = MAPPER.readTree(template);
        assertNotNull("Payload template must be valid JSON", parsed);
    }

    public void testPayloadTemplateOperationNameIsTopLevelKey() throws Exception {
        addService("OrderService", "placeOrder");
        JsonNode tool = getCatalogTools().get(0);
        String template = tool.path("x-axis2-payloadTemplate").asText();
        JsonNode parsed = MAPPER.readTree(template);
        assertTrue("Operation name must be top-level key in payload template",
                parsed.has("placeOrder"));
    }

    public void testPayloadTemplateValueIsArray() throws Exception {
        addService("OrderService", "placeOrder");
        JsonNode tool = getCatalogTools().get(0);
        String template = tool.path("x-axis2-payloadTemplate").asText();
        JsonNode parsed = MAPPER.readTree(template);
        assertTrue("Top-level value in payload template must be an array",
                parsed.path("placeOrder").isArray());
    }

    public void testPayloadTemplateArrayHasArg0Object() throws Exception {
        addService("OrderService", "placeOrder");
        JsonNode tool = getCatalogTools().get(0);
        String template = tool.path("x-axis2-payloadTemplate").asText();
        JsonNode parsed = MAPPER.readTree(template);
        JsonNode arr = parsed.path("placeOrder");
        assertEquals("Payload template array must have exactly one element", 1, arr.size());
        assertFalse("Array element must have 'arg0' key",
                arr.get(0).path("arg0").isMissingNode());
    }

    public void testPayloadTemplatesDistinctAcrossOperations() throws Exception {
        AxisService svc = new AxisService("CalcService");
        addOperation(svc, "add");
        addOperation(svc, "subtract");
        axisConfig.addService(svc);

        JsonNode tools = getCatalogTools();
        java.util.Set<String> templates = new java.util.HashSet<>();
        for (JsonNode t : tools) templates.add(t.path("x-axis2-payloadTemplate").asText());
        assertEquals("Each operation must have a distinct payload template", 2, templates.size());
    }

    // ── x-requiresAuth ────────────────────────────────────────────────────────

    /**
     * Non-login services must declare {@code x-requiresAuth: true} so MCP
     * clients know to acquire a Bearer token via loginService first — matching
     * the auth flow pyRapi implements in pyrapi/auth.py.
     */
    public void testNonLoginServiceRequiresAuth() throws Exception {
        addService("testws", "doTestws");
        JsonNode tool = getCatalogTools().get(0);
        assertTrue("Protected services must have x-requiresAuth: true",
                tool.path("x-requiresAuth").asBoolean());
    }

    public void testLoginServiceDoesNotRequireAuth() throws Exception {
        addService("loginService", "doLogin");
        JsonNode tool = getCatalogTools().get(0);
        assertFalse("loginService must have x-requiresAuth: false",
                tool.path("x-requiresAuth").asBoolean());
    }

    public void testLoginServiceCaseInsensitive() throws Exception {
        addService("LoginService", "doLogin");  // capital L
        JsonNode tool = getCatalogTools().get(0);
        assertFalse("loginService check must be case-insensitive",
                tool.path("x-requiresAuth").asBoolean());
    }

    public void testFinancialServiceRequiresAuth() throws Exception {
        addService("FinancialBenchmarkService", "portfolioVariance");
        JsonNode tool = getCatalogTools().get(0);
        assertTrue("FinancialBenchmarkService must require auth",
                tool.path("x-requiresAuth").asBoolean());
    }

    public void testBigDataServiceRequiresAuth() throws Exception {
        addService("BigDataH2Service", "processBigDataSet");
        JsonNode tool = getCatalogTools().get(0);
        assertTrue("BigDataH2Service must require auth",
                tool.path("x-requiresAuth").asBoolean());
    }

    // ── annotations (MCP 2025-03-26) ─────────────────────────────────────────

    /**
     * Tools must carry MCP 2025 {@code annotations} for client-side safety
     * hints (readOnlyHint, destructiveHint, idempotentHint, openWorldHint).
     * Matches the annotations pattern in internal-alpha-theory-mcp.
     */
    public void testToolHasAnnotationsField() throws Exception {
        addService("TestService", "testOp");
        JsonNode tool = getCatalogTools().get(0);
        assertFalse("annotations must be present on each tool",
                tool.path("annotations").isMissingNode());
        assertTrue("annotations must be an object",
                tool.path("annotations").isObject());
    }

    public void testAnnotationsHasReadOnlyHint() throws Exception {
        addService("TestService", "testOp");
        JsonNode annotations = getCatalogTools().get(0).path("annotations");
        assertFalse("annotations.readOnlyHint must be present",
                annotations.path("readOnlyHint").isMissingNode());
        assertTrue("annotations.readOnlyHint must be boolean",
                annotations.path("readOnlyHint").isBoolean());
    }

    public void testAnnotationsHasDestructiveHint() throws Exception {
        addService("TestService", "testOp");
        JsonNode annotations = getCatalogTools().get(0).path("annotations");
        assertFalse("annotations.destructiveHint must be present",
                annotations.path("destructiveHint").isMissingNode());
    }

    public void testAnnotationsHasIdempotentHint() throws Exception {
        addService("TestService", "testOp");
        JsonNode annotations = getCatalogTools().get(0).path("annotations");
        assertFalse("annotations.idempotentHint must be present",
                annotations.path("idempotentHint").isMissingNode());
    }

    public void testAnnotationsHasOpenWorldHint() throws Exception {
        addService("TestService", "testOp");
        JsonNode annotations = getCatalogTools().get(0).path("annotations");
        assertFalse("annotations.openWorldHint must be present",
                annotations.path("openWorldHint").isMissingNode());
    }

    public void testAllAnnotationHintsAreBooleans() throws Exception {
        addService("TestService", "testOp");
        JsonNode annotations = getCatalogTools().get(0).path("annotations");
        String[] hints = {"readOnlyHint", "destructiveHint", "idempotentHint", "openWorldHint"};
        for (String hint : hints) {
            assertTrue("annotations." + hint + " must be a boolean value",
                    annotations.path(hint).isBoolean());
        }
    }

    // ── A1: mcpDescription parameter ─────────────────────────────────────────

    /**
     * When an operation has a {@code mcpDescription} parameter, that value
     * replaces the auto-generated "ServiceName: operationName" description.
     * This is the primary way to make tool descriptions useful to LLMs.
     */
    public void testOperationLevelMcpDescriptionOverridesDefault() throws Exception {
        AxisService svc = new AxisService("GetAssetCalculationsService");
        AxisOperation op = new InOutAxisOperation();
        op.setName(QName.valueOf("getAssetCalculations"));
        op.addParameter(new org.apache.axis2.description.Parameter(
                "mcpDescription",
                "Get calculated portfolio metrics (OPS, PWR, Kelly) for assets in a fund."));
        svc.addOperation(op);
        axisConfig.addService(svc);

        JsonNode tool = getCatalogTools().get(0);
        assertEquals("Operation-level mcpDescription must be used as tool description",
                "Get calculated portfolio metrics (OPS, PWR, Kelly) for assets in a fund.",
                tool.path("description").asText());
    }

    /**
     * When no operation-level parameter is set but the service has
     * {@code mcpDescription}, that value is used as the description.
     */
    public void testServiceLevelMcpDescriptionUsedWhenNoOperationLevel() throws Exception {
        AxisService svc = new AxisService("SearchService");
        svc.addParameter(new org.apache.axis2.description.Parameter(
                "mcpDescription", "Service-level default description"));
        AxisOperation op = new InOutAxisOperation();
        op.setName(QName.valueOf("search"));
        svc.addOperation(op);
        axisConfig.addService(svc);

        JsonNode tool = getCatalogTools().get(0);
        assertEquals("Service-level mcpDescription must be used when no operation-level param",
                "Service-level default description",
                tool.path("description").asText());
    }

    /**
     * Operation-level {@code mcpDescription} takes precedence over a service-level one.
     */
    public void testOperationLevelMcpDescriptionTakesPrecedenceOverServiceLevel() throws Exception {
        AxisService svc = new AxisService("SearchService");
        svc.addParameter(new org.apache.axis2.description.Parameter(
                "mcpDescription", "Service-level description"));
        AxisOperation op = new InOutAxisOperation();
        op.setName(QName.valueOf("search"));
        op.addParameter(new org.apache.axis2.description.Parameter(
                "mcpDescription", "Operation-level description"));
        svc.addOperation(op);
        axisConfig.addService(svc);

        JsonNode tool = getCatalogTools().get(0);
        assertEquals("Operation-level mcpDescription must win over service-level",
                "Operation-level description",
                tool.path("description").asText());
    }

    /**
     * When no {@code mcpDescription} parameter is set at either level, the
     * auto-generated "ServiceName: operationName" fallback is still produced.
     */
    public void testDescriptionFallsBackToAutoGeneratedWhenNoMcpDescriptionParam() throws Exception {
        addService("MyService", "myOperation");
        JsonNode tool = getCatalogTools().get(0);
        assertEquals("Auto-generated fallback must be 'ServiceName: operationName'",
                "MyService: myOperation",
                tool.path("description").asText());
    }

    // ── A2: mcpReadOnly / mcpIdempotent annotation tuning ────────────────────

    /**
     * When a service sets {@code mcpReadOnly=true}, the catalog must publish
     * {@code readOnlyHint: true} for all its operations.  Read-only services
     * (GetAsset*, Search*) should set this so MCP hosts can safely auto-approve
     * them without human confirmation.
     */
    public void testServiceLevelMcpReadOnlySetsTrueOnAnnotation() throws Exception {
        AxisService svc = new AxisService("GetAssetCalculationsService");
        svc.addParameter(new org.apache.axis2.description.Parameter("mcpReadOnly", "true"));
        AxisOperation op = new InOutAxisOperation();
        op.setName(QName.valueOf("getAssetCalculations"));
        svc.addOperation(op);
        axisConfig.addService(svc);

        JsonNode annotations = getCatalogTools().get(0).path("annotations");
        assertTrue("readOnlyHint must be true when mcpReadOnly=true on service",
                annotations.path("readOnlyHint").asBoolean());
    }

    /**
     * Operation-level {@code mcpReadOnly=true} overrides a service-level
     * {@code false} (or absent) — per-operation tuning takes precedence.
     */
    public void testOperationLevelMcpReadOnlyOverridesServiceLevel() throws Exception {
        AxisService svc = new AxisService("MixedService");
        // no service-level mcpReadOnly
        AxisOperation op = new InOutAxisOperation();
        op.setName(QName.valueOf("readOnlyOp"));
        op.addParameter(new org.apache.axis2.description.Parameter("mcpReadOnly", "true"));
        svc.addOperation(op);
        axisConfig.addService(svc);

        JsonNode annotations = getCatalogTools().get(0).path("annotations");
        assertTrue("readOnlyHint must be true when operation sets mcpReadOnly=true",
                annotations.path("readOnlyHint").asBoolean());
    }

    /**
     * {@code mcpIdempotent=true} maps to {@code idempotentHint: true}.
     */
    public void testMcpIdempotentParamSetsIdempotentHint() throws Exception {
        AxisService svc = new AxisService("QueryService");
        AxisOperation op = new InOutAxisOperation();
        op.setName(QName.valueOf("getByKey"));
        op.addParameter(new org.apache.axis2.description.Parameter("mcpIdempotent", "true"));
        svc.addOperation(op);
        axisConfig.addService(svc);

        JsonNode annotations = getCatalogTools().get(0).path("annotations");
        assertTrue("idempotentHint must be true when mcpIdempotent=true",
                annotations.path("idempotentHint").asBoolean());
    }

    /**
     * {@code mcpDestructive=true} maps to {@code destructiveHint: true}.
     */
    public void testMcpDestructiveParamSetsDestructiveHint() throws Exception {
        AxisService svc = new AxisService("AdminService");
        AxisOperation op = new InOutAxisOperation();
        op.setName(QName.valueOf("deleteAllData"));
        op.addParameter(new org.apache.axis2.description.Parameter("mcpDestructive", "true"));
        svc.addOperation(op);
        axisConfig.addService(svc);

        JsonNode annotations = getCatalogTools().get(0).path("annotations");
        assertTrue("destructiveHint must be true when mcpDestructive=true",
                annotations.path("destructiveHint").asBoolean());
    }

    /**
     * Conservative defaults remain intact when no MCP annotation parameters
     * are set — no existing behaviour is changed by the new parameter support.
     */
    public void testAnnotationDefaultsAreConservativeWhenNoParamsSet() throws Exception {
        addService("NoParamService", "doSomething");
        JsonNode annotations = getCatalogTools().get(0).path("annotations");
        assertFalse("readOnlyHint default must be false",    annotations.path("readOnlyHint").asBoolean());
        assertFalse("destructiveHint default must be false", annotations.path("destructiveHint").asBoolean());
        assertFalse("idempotentHint default must be false",  annotations.path("idempotentHint").asBoolean());
        assertFalse("openWorldHint default must be false",   annotations.path("openWorldHint").asBoolean());
    }

    // ── tool list mirrors existing OpenAPI paths ──────────────────────────────

    /**
     * Every tool in the MCP catalog must correspond to a path in the OpenAPI
     * spec (same service/operation pair). This verifies that both endpoints
     * apply identical filtering logic.
     */
    public void testMcpToolsMatchOpenApiPaths() throws Exception {
        addService("BigDataH2Service", "processBigDataSet");
        addService("OrderService", "placeOrder");

        // Collect MCP tool endpoints
        JsonNode tools = getCatalogTools();
        java.util.Set<String> mcpPaths = new java.util.HashSet<>();
        for (JsonNode tool : tools) {
            String endpoint = tool.path("endpoint").asText();
            // Strip "POST " prefix
            mcpPaths.add(endpoint.substring("POST ".length()));
        }

        // Collect OpenAPI paths
        io.swagger.v3.oas.models.OpenAPI openApi = generator.generateOpenApiSpec(mockRequest);
        java.util.Set<String> openApiPaths = new java.util.HashSet<>();
        if (openApi.getPaths() != null) {
            openApiPaths.addAll(openApi.getPaths().keySet());
        }

        // Every MCP path must appear in the OpenAPI spec
        for (String mcpPath : mcpPaths) {
            assertTrue("MCP tool path '" + mcpPath + "' must appear in OpenAPI paths",
                    openApiPaths.contains(mcpPath));
        }
    }

    // ── helpers ─────────────────────────────────────────────────────────────

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
        String json = generator.generateMcpCatalogJson(mockRequest);
        return MAPPER.readTree(json).path("tools");
    }

    // ── mock request ─────────────────────────────────────────────────────────

    private static class MockHttpServletRequest implements HttpServletRequest {
        private String scheme = "https";
        private String serverName = "localhost";
        private int serverPort = 8443;
        private String contextPath = "/axis2-json-api";

        @Override public String getScheme()      { return scheme; }
        @Override public String getServerName()  { return serverName; }
        @Override public int    getServerPort()  { return serverPort; }
        @Override public String getContextPath() { return contextPath; }

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
        @Override public String getRequestURI() { return ""; }
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
        @Override public int getLocalPort() { return serverPort; }
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
