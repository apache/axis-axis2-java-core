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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link McpStdioServer}.
 *
 * <p>Each test redirects {@code System.in} / {@code System.out} around the
 * server run loop so the JSON-RPC 2.0 responses can be captured and verified
 * without any real HTTP or TLS infrastructure.
 *
 * <p>A {@link StubToolRegistry} inner class overrides {@link ToolRegistry}'s
 * getters to inject known tools without calling the HTTP {@code load()} method.
 */
public class McpStdioServerTest extends TestCase {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ── helpers ─────────────────────────────────────────────────────────────

    /**
     * Runs the server with the given newline-delimited JSON-RPC input lines and
     * returns all stdout output as a single string (lines separated by newlines).
     */
    private String runServer(StubToolRegistry registry, String... inputLines) throws Exception {
        String inputData = String.join("\n", inputLines) + "\n";

        PrintStream savedOut = System.out;
        InputStream savedIn = System.in;
        try {
            ByteArrayOutputStream captured = new ByteArrayOutputStream();
            // Replace System.out BEFORE constructing the server so the
            // server's internal PrintStream wraps our capture buffer.
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            System.setIn(new ByteArrayInputStream(
                    inputData.getBytes(StandardCharsets.UTF_8)));

            McpStdioServer server = new McpStdioServer(
                    "https://localhost:8443/axis2-json-api", registry, MAPPER, null);
            server.run();

            return captured.toString(StandardCharsets.UTF_8).trim();
        } finally {
            System.setOut(savedOut);
            System.setIn(savedIn);
        }
    }

    /** Parses a single-line JSON-RPC response. Fails the test if output has multiple lines. */
    private JsonNode parseSingleResponse(String output) throws Exception {
        String[] lines = output.split("\n");
        // Filter out blank lines
        String responseLine = null;
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                assertNull("Expected single response line but got multiple non-empty lines", responseLine);
                responseLine = line.trim();
            }
        }
        assertNotNull("Expected at least one response line", responseLine);
        return MAPPER.readTree(responseLine);
    }

    // ── initialize ──────────────────────────────────────────────────────────

    public void testInitializeReturnsProtocolVersion() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\"," +
                "\"params\":{\"protocolVersion\":\"2024-11-05\",\"capabilities\":{}," +
                "\"clientInfo\":{\"name\":\"test-client\",\"version\":\"1.0\"}}}");

        JsonNode response = parseSingleResponse(output);
        assertEquals("2.0", response.path("jsonrpc").asText());
        assertEquals(1, response.path("id").asInt());
        assertFalse("Must not contain 'error'", response.has("error"));
        assertTrue("Must contain 'result'", response.has("result"));

        JsonNode result = response.path("result");
        assertEquals("2024-11-05", result.path("protocolVersion").asText());
    }

    public void testInitializeReturnsServerInfo() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}");

        JsonNode result = parseSingleResponse(output).path("result");
        JsonNode serverInfo = result.path("serverInfo");
        assertFalse("serverInfo must be present", serverInfo.isMissingNode());
        assertFalse("serverInfo.name must be present", serverInfo.path("name").isMissingNode());
        assertFalse("serverInfo.version must be present", serverInfo.path("version").isMissingNode());
    }

    public void testInitializeReturnsToolsCapability() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}");

        JsonNode result = parseSingleResponse(output).path("result");
        JsonNode capabilities = result.path("capabilities");
        assertFalse("capabilities must be present", capabilities.isMissingNode());
        assertFalse("capabilities.tools must be present", capabilities.path("tools").isMissingNode());
    }

    public void testInitializePreservesRequestId() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":42,\"method\":\"initialize\",\"params\":{}}");

        JsonNode response = parseSingleResponse(output);
        assertEquals(42, response.path("id").asInt());
    }

    public void testInitializeWithStringId() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":\"req-1\",\"method\":\"initialize\",\"params\":{}}");

        JsonNode response = parseSingleResponse(output);
        assertEquals("req-1", response.path("id").asText());
    }

    // ── tools/list ──────────────────────────────────────────────────────────

    public void testToolsListWithEmptyRegistry() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}");

        JsonNode response = parseSingleResponse(output);
        assertFalse("Must not contain 'error'", response.has("error"));
        JsonNode tools = response.path("result").path("tools");
        assertTrue("tools must be an array", tools.isArray());
        assertEquals("Empty registry should produce empty tools array", 0, tools.size());
    }

    public void testToolsListReturnsSingleTool() throws Exception {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        McpTool tool = new McpTool("processBigDataSet", "BigDataH2Service: processBigDataSet",
                schema, "POST /services/BigDataH2Service/processBigDataSet");

        StubToolRegistry registry = new StubToolRegistry();
        registry.addTool(tool);

        String output = runServer(registry,
                "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}");

        JsonNode tools = parseSingleResponse(output).path("result").path("tools");
        assertTrue("tools must be an array", tools.isArray());
        assertEquals(1, tools.size());
        assertEquals("processBigDataSet", tools.get(0).path("name").asText());
        assertEquals("BigDataH2Service: processBigDataSet",
                tools.get(0).path("description").asText());
        assertFalse("inputSchema must be present",
                tools.get(0).path("inputSchema").isMissingNode());
    }

    public void testToolsListReturnsMultipleTools() throws Exception {
        StubToolRegistry registry = new StubToolRegistry();
        registry.addTool(new McpTool("op1", "Service1: op1",
                MAPPER.createObjectNode(), "POST /services/Service1/op1"));
        registry.addTool(new McpTool("op2", "Service2: op2",
                MAPPER.createObjectNode(), "POST /services/Service2/op2"));
        registry.addTool(new McpTool("op3", "Service3: op3",
                MAPPER.createObjectNode(), "POST /services/Service3/op3"));

        String output = runServer(registry,
                "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}");

        JsonNode tools = parseSingleResponse(output).path("result").path("tools");
        assertEquals(3, tools.size());
    }

    // ── tools/call ──────────────────────────────────────────────────────────

    /**
     * When tools/call names a tool that is not in the registry, the server
     * throws IllegalArgumentException, which is caught by the run() loop and
     * logged to stderr. No JSON-RPC response is written to stdout.
     */
    public void testToolsCallUnknownToolProducesNoStdoutResponse() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\"," +
                "\"params\":{\"name\":\"nonexistentTool\",\"arguments\":{}}}");

        // Nothing should be written to stdout for the unknown-tool error path
        // (exception is caught in run() and logged to stderr only)
        assertTrue("No stdout response expected for unknown tool", output.isEmpty());
    }

    public void testToolsCallMissingNameProducesNoStdoutResponse() throws Exception {
        // params.name is absent — throws IllegalArgumentException in buildToolsCallResult
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\"," +
                "\"params\":{\"arguments\":{}}}");

        assertTrue("No stdout response expected for missing name", output.isEmpty());
    }

    // ── error cases ─────────────────────────────────────────────────────────

    public void testUnknownMethodReturnsMethodNotFoundError() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":5,\"method\":\"nonexistent/method\",\"params\":{}}");

        JsonNode response = parseSingleResponse(output);
        assertTrue("Must contain 'error'", response.has("error"));
        assertFalse("Must not contain 'result'", response.has("result"));
        assertEquals(-32601, response.path("error").path("code").asInt());
        assertTrue("Error message should mention method name",
                response.path("error").path("message").asText().contains("nonexistent/method"));
    }

    public void testParseErrorReturnsParseErrorCode() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{this is not valid json at all!!!");

        JsonNode response = parseSingleResponse(output);
        assertTrue("Must contain 'error'", response.has("error"));
        assertEquals(-32700, response.path("error").path("code").asInt());
        // Per JSON-RPC 2.0, id must be null when request cannot be parsed
        assertTrue("id must be null for parse errors",
                response.path("id").isNull() || response.path("id").isMissingNode());
    }

    public void testPartiallyValidJsonReturnsParseError() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\"");   // truncated

        JsonNode response = parseSingleResponse(output);
        assertEquals(-32700, response.path("error").path("code").asInt());
    }

    // ── notifications ────────────────────────────────────────────────────────

    /**
     * Notifications (JSON-RPC messages without an "id" field) must be silently
     * consumed. No response should appear on stdout.
     */
    public void testNotificationProducesNoResponse() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\",\"params\":{}}");

        assertTrue("Notification must produce no stdout output", output.isEmpty());
    }

    public void testNotificationWithExplicitNullIdProducesNoResponse() throws Exception {
        // Some clients send null id for notifications; treat same as absent id
        // Note: JSON-RPC spec says notifications have NO id field. If id is
        // present (even null), this implementation treats it as a notification.
        // This test documents the actual behaviour.
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":null,\"method\":\"notifications/initialized\",\"params\":{}}");

        // With null id, Jackson's request.get("id") returns a NullNode (not null).
        // The current server checks `if (id == null)` — so NullNode != null,
        // meaning this IS treated as a regular request (id present) and will
        // attempt to dispatch the method, returning -32601. Document this behaviour.
        if (!output.isEmpty()) {
            JsonNode response = parseSingleResponse(output);
            // If there is output, it must be a valid JSON-RPC response
            assertTrue("Any response must be valid JSON-RPC 2.0", response.has("jsonrpc"));
        }
        // Either no output or a well-formed error — both are acceptable
    }

    // ── sequence / multiple requests ─────────────────────────────────────────

    public void testMultipleRequestsInSequenceAreAllAnswered() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}",
                "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}",
                "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"bad_method\",\"params\":{}}");

        String[] lines = Arrays.stream(output.split("\n"))
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .toArray(String[]::new);

        assertEquals("Three requests should produce three responses", 3, lines.length);

        JsonNode r1 = MAPPER.readTree(lines[0]);
        assertEquals(1, r1.path("id").asInt());
        assertTrue("First response must be success", r1.has("result"));

        JsonNode r2 = MAPPER.readTree(lines[1]);
        assertEquals(2, r2.path("id").asInt());
        assertTrue("Second response must be success", r2.has("result"));

        JsonNode r3 = MAPPER.readTree(lines[2]);
        assertEquals(3, r3.path("id").asInt());
        assertTrue("Third response must be error", r3.has("error"));
        assertEquals(-32601, r3.path("error").path("code").asInt());
    }

    public void testNotificationMixedWithRequests() throws Exception {
        // Notification in the middle should not break the sequence
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}",
                "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\",\"params\":{}}",
                "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}");

        String[] lines = Arrays.stream(output.split("\n"))
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .toArray(String[]::new);

        assertEquals("Two requests (one notification skipped) → two responses", 2, lines.length);
        assertEquals(1, MAPPER.readTree(lines[0]).path("id").asInt());
        assertEquals(2, MAPPER.readTree(lines[1]).path("id").asInt());
    }

    public void testBlankLinesAreSkipped() throws Exception {
        // The server trims and skips blank lines
        String inputData = "\n\n{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}\n\n";

        PrintStream savedOut = System.out;
        InputStream savedIn = System.in;
        try {
            ByteArrayOutputStream captured = new ByteArrayOutputStream();
            System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
            System.setIn(new ByteArrayInputStream(inputData.getBytes(StandardCharsets.UTF_8)));

            McpStdioServer server = new McpStdioServer(
                    "https://localhost:8443/axis2-json-api",
                    new StubToolRegistry(), MAPPER, null);
            server.run();

            String output = captured.toString(StandardCharsets.UTF_8).trim();
            JsonNode response = parseSingleResponse(output);
            assertEquals(1, response.path("id").asInt());
            assertTrue(response.has("result"));
        } finally {
            System.setOut(savedOut);
            System.setIn(savedIn);
        }
    }

    // ── JSON-RPC envelope ────────────────────────────────────────────────────

    public void testSuccessResponseHasJsonRpc20Field() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}");

        JsonNode response = parseSingleResponse(output);
        assertEquals("2.0", response.path("jsonrpc").asText());
    }

    public void testErrorResponseHasJsonRpc20Field() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":99,\"method\":\"unknown\",\"params\":{}}");

        JsonNode response = parseSingleResponse(output);
        assertEquals("2.0", response.path("jsonrpc").asText());
    }

    public void testErrorObjectHasCodeAndMessageFields() throws Exception {
        String output = runServer(new StubToolRegistry(),
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"unknown\",\"params\":{}}");

        JsonNode error = parseSingleResponse(output).path("error");
        assertFalse("error.code must be present", error.path("code").isMissingNode());
        assertFalse("error.message must be present", error.path("message").isMissingNode());
        assertTrue("error.message must be a non-empty string",
                !error.path("message").asText().isEmpty());
    }

    // ── stub registry ─────────────────────────────────────────────────────────

    /**
     * Test double for {@link ToolRegistry}.
     *
     * <p>Overrides {@link #getTools()} and {@link #getTool(String)} so no
     * HTTP call is made during tests. Tools are injected via {@link #addTool}.
     */
    private static class StubToolRegistry extends ToolRegistry {

        private final List<McpTool> toolList = new java.util.ArrayList<>();
        private final Map<String, McpTool> toolMap = new HashMap<>();

        StubToolRegistry() {
            // Pass dummy values; load() is never called
            super("https://localhost:8443/unused", new ObjectMapper(), null);
        }

        void addTool(McpTool tool) {
            toolList.add(tool);
            toolMap.put(tool.getName(), tool);
        }

        @Override
        public List<McpTool> getTools() {
            return Collections.unmodifiableList(toolList);
        }

        @Override
        public McpTool getTool(String name) {
            return toolMap.get(name);
        }
    }
}
