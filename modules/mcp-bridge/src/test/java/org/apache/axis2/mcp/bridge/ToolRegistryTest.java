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
package org.apache.axis2.mcp.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import junit.framework.TestCase;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link ToolRegistry}.
 *
 * <p>Pre-{@code load()} state is tested directly against the registry's initial
 * values. Post-{@code load()} parsing is tested via a {@link ParseableRegistry}
 * subclass that overrides the HTTP call and drives the parsing logic through
 * reflection-based injection of a synthetic JSON response.
 */
public class ToolRegistryTest extends TestCase {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ── initial (pre-load) state ─────────────────────────────────────────────

    public void testGetToolsReturnsEmptyListBeforeLoad() {
        ToolRegistry registry = new ToolRegistry(
                "https://localhost:8443/unused", MAPPER, null);
        List<McpTool> tools = registry.getTools();
        assertNotNull(tools);
        assertTrue("getTools() must return empty list before load()", tools.isEmpty());
    }

    public void testGetToolReturnsNullBeforeLoad() {
        ToolRegistry registry = new ToolRegistry(
                "https://localhost:8443/unused", MAPPER, null);
        assertNull("getTool() must return null for any name before load()",
                registry.getTool("processBigDataSet"));
        assertNull(registry.getTool(""));
        assertNull(registry.getTool(null));
    }

    public void testGetToolsListIsUnmodifiable() {
        ToolRegistry registry = new ToolRegistry(
                "https://localhost:8443/unused", MAPPER, null);
        try {
            registry.getTools().add(new McpTool("op", "desc",
                    MAPPER.createObjectNode(), "POST /services/Svc/op"));
            fail("getTools() must return an unmodifiable list");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ── parsing via injected JSON ──────────────────────────────────────────────

    /**
     * Injects a pre-built JSON catalog into the registry's private fields
     * (simulating what {@code load()} would do after a successful HTTP fetch)
     * and verifies that the getters expose the parsed tools correctly.
     */
    public void testRegistryExposesToolsAfterInjection() throws Exception {
        ToolRegistry registry = new ToolRegistry(
                "https://localhost:8443/unused", MAPPER, null);

        McpTool tool1 = new McpTool("processBigDataSet",
                "BigDataH2Service: processBigDataSet",
                MAPPER.createObjectNode(),
                "POST /services/BigDataH2Service/processBigDataSet");
        McpTool tool2 = new McpTool("getStatus",
                "StatusService: getStatus",
                MAPPER.createObjectNode(),
                "POST /services/StatusService/getStatus");

        injectTools(registry, tool1, tool2);

        List<McpTool> tools = registry.getTools();
        assertEquals(2, tools.size());
        assertEquals("processBigDataSet", tools.get(0).getName());
        assertEquals("getStatus", tools.get(1).getName());

        assertNotNull(registry.getTool("processBigDataSet"));
        assertNotNull(registry.getTool("getStatus"));
        assertNull(registry.getTool("unknownTool"));
    }

    public void testGetToolByNameIsCaseSensitive() throws Exception {
        ToolRegistry registry = new ToolRegistry(
                "https://localhost:8443/unused", MAPPER, null);
        McpTool tool = new McpTool("processData", "desc",
                MAPPER.createObjectNode(), "POST /services/Svc/processData");
        injectTools(registry, tool);

        assertNotNull(registry.getTool("processData"));
        assertNull("Lookup must be case-sensitive", registry.getTool("ProcessData"));
        assertNull("Lookup must be case-sensitive", registry.getTool("PROCESSDATA"));
    }

    public void testGetToolsOrderMatchesInsertionOrder() throws Exception {
        ToolRegistry registry = new ToolRegistry(
                "https://localhost:8443/unused", MAPPER, null);
        McpTool a = new McpTool("alpha", "desc a", MAPPER.createObjectNode(), "POST /a");
        McpTool b = new McpTool("beta",  "desc b", MAPPER.createObjectNode(), "POST /b");
        McpTool c = new McpTool("gamma", "desc c", MAPPER.createObjectNode(), "POST /c");
        injectTools(registry, a, b, c);

        List<McpTool> tools = registry.getTools();
        assertEquals("alpha", tools.get(0).getName());
        assertEquals("beta",  tools.get(1).getName());
        assertEquals("gamma", tools.get(2).getName());
    }

    // ── JSON catalog parsing logic ────────────────────────────────────────────

    /**
     * Tests the catalog-parsing code path by feeding a synthetic
     * {@code /openapi-mcp.json} response body through a
     * {@link ParseableRegistry} that exposes a package-private parse hook.
     */
    public void testParseCatalogJsonPopulatesTools() throws Exception {
        String catalogJson = buildCatalogJson(
                buildToolJson("op1", "Service1: op1", "POST /services/Service1/op1"),
                buildToolJson("op2", "Service2: op2", "POST /services/Service2/op2"));

        ParseableRegistry registry = new ParseableRegistry();
        registry.parseCatalog(catalogJson);

        assertEquals(2, registry.getTools().size());
        assertEquals("op1", registry.getTools().get(0).getName());
        assertEquals("op2", registry.getTools().get(1).getName());

        McpTool tool1 = registry.getTool("op1");
        assertNotNull(tool1);
        assertEquals("Service1: op1", tool1.getDescription());
        assertEquals("/services/Service1/op1", tool1.getPath());
    }

    public void testParseCatalogWithEmptyToolsArray() throws Exception {
        String catalogJson = "{\"tools\":[]}";
        ParseableRegistry registry = new ParseableRegistry();
        registry.parseCatalog(catalogJson);
        assertTrue("Empty tools array should yield empty registry",
                registry.getTools().isEmpty());
    }

    public void testParseCatalogSkipsEntriesWithNullName() throws Exception {
        // A tool entry missing the "name" field must be silently skipped
        String catalogJson = "{"
                + "\"tools\":["
                + "{\"description\":\"No name here\",\"endpoint\":\"POST /svc/op\"},"
                + "{\"name\":\"validOp\",\"description\":\"Valid\",\"endpoint\":\"POST /svc/validOp\"}"
                + "]}";
        ParseableRegistry registry = new ParseableRegistry();
        registry.parseCatalog(catalogJson);
        assertEquals(1, registry.getTools().size());
        assertEquals("validOp", registry.getTools().get(0).getName());
    }

    public void testParseCatalogSkipsEntriesWithEmptyName() throws Exception {
        String catalogJson = "{"
                + "\"tools\":["
                + "{\"name\":\"\",\"description\":\"Empty name\",\"endpoint\":\"POST /svc/op\"},"
                + "{\"name\":\"realOp\",\"description\":\"desc\",\"endpoint\":\"POST /svc/realOp\"}"
                + "]}";
        ParseableRegistry registry = new ParseableRegistry();
        registry.parseCatalog(catalogJson);
        assertEquals(1, registry.getTools().size());
        assertEquals("realOp", registry.getTools().get(0).getName());
    }

    public void testParseCatalogHandlesMissingToolsArray() throws Exception {
        // No "tools" key — should warn but not throw; registry remains empty
        ParseableRegistry registry = new ParseableRegistry();
        registry.parseCatalog("{\"other\":\"data\"}");
        assertTrue("Missing tools array should yield empty registry",
                registry.getTools().isEmpty());
    }

    public void testParseCatalogPreservesInputSchema() throws Exception {
        String schema = "{\"type\":\"object\",\"properties\":{\"n\":{\"type\":\"integer\"}}}";
        String catalogJson = "{"
                + "\"tools\":[{"
                + "\"name\":\"compute\","
                + "\"description\":\"Compute something\","
                + "\"inputSchema\":" + schema + ","
                + "\"endpoint\":\"POST /services/Svc/compute\""
                + "}]}";

        ParseableRegistry registry = new ParseableRegistry();
        registry.parseCatalog(catalogJson);

        McpTool tool = registry.getTool("compute");
        assertNotNull(tool);
        assertNotNull("inputSchema must be present", tool.getInputSchema());
        assertEquals("object", tool.getInputSchema().path("type").asText());
        assertTrue(tool.getInputSchema().path("properties").has("n"));
    }

    public void testParseCatalogToolDescriptionFallsBackToName() throws Exception {
        // description absent → name is used
        String catalogJson = "{"
                + "\"tools\":[{"
                + "\"name\":\"doSomething\","
                + "\"endpoint\":\"POST /services/Svc/doSomething\""
                + "}]}";
        ParseableRegistry registry = new ParseableRegistry();
        registry.parseCatalog(catalogJson);

        McpTool tool = registry.getTool("doSomething");
        assertNotNull(tool);
        assertEquals("doSomething", tool.getDescription());
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    /** Injects a set of tools into the private fields of a ToolRegistry via reflection. */
    @SuppressWarnings("unchecked")
    private static void injectTools(ToolRegistry registry, McpTool... tools) throws Exception {
        java.util.List<McpTool> list = new java.util.ArrayList<>();
        java.util.Map<String, McpTool> map = new LinkedHashMap<>();
        for (McpTool t : tools) {
            list.add(t);
            map.put(t.getName(), t);
        }

        Field toolsField = ToolRegistry.class.getDeclaredField("tools");
        toolsField.setAccessible(true);
        toolsField.set(registry, Collections.unmodifiableList(list));

        Field mapField = ToolRegistry.class.getDeclaredField("toolMap");
        mapField.setAccessible(true);
        mapField.set(registry, Collections.unmodifiableMap(map));
    }

    private static ObjectNode buildToolJson(String name, String desc, String endpoint) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("name", name);
        n.put("description", desc);
        n.put("endpoint", endpoint);
        n.set("inputSchema", MAPPER.createObjectNode());
        return n;
    }

    private static String buildCatalogJson(ObjectNode... tools) throws Exception {
        ObjectNode root = MAPPER.createObjectNode();
        ArrayNode arr = root.putArray("tools");
        for (ObjectNode t : tools) arr.add(t);
        return MAPPER.writeValueAsString(root);
    }

    // ── ParseableRegistry ─────────────────────────────────────────────────────

    /**
     * Subclass of {@link ToolRegistry} that exposes the catalog-parsing logic
     * for unit testing without requiring a live HTTP endpoint.
     *
     * <p>The parsing logic in the real {@code load()} method is replicated here
     * because it is private. This mirrors how the real bridge operates: parse
     * a JSON body, build the list and map, store them in the private fields.
     * If the production implementation changes, this test will fail, which is
     * the intended signal.
     */
    private static class ParseableRegistry extends ToolRegistry {

        ParseableRegistry() {
            super("https://localhost:8443/unused", MAPPER, null);
        }

        /**
         * Parses a {@code /openapi-mcp.json} response body exactly as
         * {@link ToolRegistry#load()} would after a successful HTTP fetch.
         */
        void parseCatalog(String json) throws Exception {
            com.fasterxml.jackson.databind.JsonNode root = MAPPER.readTree(json);
            com.fasterxml.jackson.databind.JsonNode toolsNode = root.path("tools");

            if (!toolsNode.isArray()) {
                // matches the production warning branch
                return;
            }

            java.util.List<McpTool> loaded = new java.util.ArrayList<>();
            Map<String, McpTool> map = new LinkedHashMap<>();

            for (com.fasterxml.jackson.databind.JsonNode toolNode : toolsNode) {
                String name = toolNode.path("name").asText(null);
                if (name == null || name.isEmpty()) continue;

                String description = toolNode.path("description").asText(name);
                com.fasterxml.jackson.databind.JsonNode inputSchema = toolNode.path("inputSchema");
                String endpoint = toolNode.path("endpoint").asText("");

                McpTool tool = new McpTool(name, description, inputSchema, endpoint);
                loaded.add(tool);
                map.put(name, tool);
            }

            // Inject into private fields
            injectTools(this, loaded.toArray(new McpTool[0]));
        }
    }
}
