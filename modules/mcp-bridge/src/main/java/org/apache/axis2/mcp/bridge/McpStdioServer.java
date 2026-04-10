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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * MCP stdio server: reads JSON-RPC 2.0 requests from stdin, writes responses
 * to stdout. All diagnostic output goes to stderr so it does not pollute the
 * MCP protocol stream.
 *
 * <p>Implements the three methods required by the MCP specification:
 * <ul>
 *   <li>{@code initialize} — returns server capabilities and protocol version</li>
 *   <li>{@code tools/list} — returns the Axis2 tool catalog</li>
 *   <li>{@code tools/call} — proxies the call to the Axis2 HTTP endpoint</li>
 * </ul>
 *
 * <p>Notifications (messages without an {@code id} field) are silently consumed
 * with no response, as required by JSON-RPC 2.0.
 */
public class McpStdioServer implements java.io.Closeable {

    private static final String PROTOCOL_VERSION = "2024-11-05";
    private static final String SERVER_NAME = "axis2-mcp-bridge";
    private static final String SERVER_VERSION = "2.0.1-SNAPSHOT";

    private final String baseUrl;
    private final ToolRegistry registry;
    private final ObjectMapper mapper;
    private final CloseableHttpClient httpClient;
    private final PrintStream out;

    public McpStdioServer(String baseUrl, ToolRegistry registry, ObjectMapper mapper, SSLContext sslContext) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.registry = registry;
        this.mapper = mapper;
        this.httpClient = buildHttpClient(sslContext);
        // stdout must be raw bytes in UTF-8; replace the default PrintStream
        this.out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    }

    private static CloseableHttpClient buildHttpClient(SSLContext sslContext) {
        HttpClientConnectionManager connManager;
        if (sslContext != null) {
            connManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                            .setSslContext(sslContext)
                            .build())
                    .setMaxConnTotal(20)
                    .setMaxConnPerRoute(20)
                    .build();
        } else {
            connManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setMaxConnTotal(20)
                    .setMaxConnPerRoute(20)
                    .build();
        }
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(10))
                .setResponseTimeout(Timeout.ofSeconds(60))
                .build();
        return HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * Blocking read loop. Returns when stdin is closed (client disconnected).
     */
    public void run() throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            try {
                processLine(line);
            } catch (Exception e) {
                System.err.println("[axis2-mcp-bridge] Unhandled error: " + e.getMessage());
            }
        }
        System.err.println("[axis2-mcp-bridge] stdin closed — exiting");
    }

    private void processLine(String line) throws Exception {
        JsonNode request;
        try {
            request = mapper.readTree(line);
        } catch (Exception e) {
            // Cannot parse — write parse error with null id per JSON-RPC 2.0 spec
            writeError(mapper.nullNode(), -32700, "Parse error: " + e.getMessage());
            return;
        }

        String method = request.path("method").asText("");
        JsonNode id = request.get("id");

        // Notifications have no "id" field — consume silently, no response
        if (id == null) {
            System.err.println("[axis2-mcp-bridge] Notification: " + method);
            return;
        }

        JsonNode params = request.path("params");
        System.err.println("[axis2-mcp-bridge] Request id=" + id + " method=" + method);

        try {
            switch (method) {
                case "initialize":
                    writeSuccess(id, buildInitializeResult(params));
                    break;
                case "tools/list":
                    writeSuccess(id, buildToolsListResult());
                    break;
                case "tools/call":
                    writeSuccess(id, buildToolsCallResult(params));
                    break;
                default:
                    writeError(id, -32601, "Method not found: " + method);
            }
        } catch (IllegalArgumentException e) {
            // Invalid params: unknown tool name, missing required param, etc.
            writeError(id, -32602, "Invalid params: " + e.getMessage());
        } catch (IOException e) {
            writeError(id, -32000, "Server error during tool call: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[axis2-mcp-bridge] Internal error id=" + id + ": " + e.getMessage());
            e.printStackTrace(System.err);
            writeError(id, -32603, "Internal error: " + e.getMessage());
        }
    }

    // ── initialize ──────────────────────────────────────────────────────────

    private ObjectNode buildInitializeResult(JsonNode params) {
        ObjectNode result = mapper.createObjectNode();
        result.put("protocolVersion", PROTOCOL_VERSION);

        // capabilities.tools presence signals this server supports the tools feature
        ObjectNode capabilities = result.putObject("capabilities");
        capabilities.putObject("tools");

        ObjectNode serverInfo = result.putObject("serverInfo");
        serverInfo.put("name", SERVER_NAME);
        serverInfo.put("version", SERVER_VERSION);

        return result;
    }

    // ── tools/list ──────────────────────────────────────────────────────────

    private ObjectNode buildToolsListResult() {
        ArrayNode toolsArray = mapper.createArrayNode();
        for (McpTool tool : registry.getTools()) {
            ObjectNode toolNode = mapper.createObjectNode();
            toolNode.put("name", tool.getName());
            toolNode.put("description", tool.getDescription());
            toolNode.set("inputSchema", tool.getInputSchema());
            toolsArray.add(toolNode);
        }
        ObjectNode result = mapper.createObjectNode();
        result.set("tools", toolsArray);
        return result;
    }

    // ── tools/call ──────────────────────────────────────────────────────────

    private ObjectNode buildToolsCallResult(JsonNode params) throws IOException {
        String toolName = params.path("name").asText(null);
        if (toolName == null || toolName.isEmpty()) {
            throw new IllegalArgumentException("tools/call missing required param 'name'");
        }

        McpTool tool = registry.getTool(toolName);
        if (tool == null) {
            throw new IllegalArgumentException("Unknown tool: " + toolName);
        }

        JsonNode arguments = params.path("arguments");
        String responseBody = callAxis2(tool, arguments);

        // Return response as MCP text content
        ObjectNode result = mapper.createObjectNode();
        ArrayNode content = result.putArray("content");
        ObjectNode textItem = content.addObject();
        textItem.put("type", "text");
        textItem.put("text", responseBody);
        return result;
    }

    /**
     * POST to the Axis2 endpoint. Wraps MCP arguments in the Axis2 JSON-RPC
     * request envelope: {@code {operationName: [arguments]}}.
     */
    private String callAxis2(McpTool tool, JsonNode arguments) throws IOException {
        String url = baseUrl + tool.getPath();

        // Axis2 JSON-RPC envelope: {"operationName": [arguments]}
        ObjectNode body = mapper.createObjectNode();
        ArrayNode argArray = body.putArray(tool.getName());
        argArray.add(arguments.isNull() ? mapper.createObjectNode() : arguments);

        String requestBody = mapper.writeValueAsString(body);
        System.err.println("[axis2-mcp-bridge] Calling: POST " + url);
        System.err.println("[axis2-mcp-bridge] Body: " + requestBody);

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        return httpClient.execute(httpPost, response -> {
            int status = response.getCode();
            System.err.println("[axis2-mcp-bridge] Response: HTTP " + status);
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        });
    }

    // ── JSON-RPC 2.0 response helpers ───────────────────────────────────────

    private void writeSuccess(JsonNode id, JsonNode result) throws IOException {
        ObjectNode envelope = mapper.createObjectNode();
        envelope.put("jsonrpc", "2.0");
        envelope.set("id", id);
        envelope.set("result", result);
        writeLine(mapper.writeValueAsString(envelope));
    }

    private void writeError(JsonNode id, int code, String message) throws IOException {
        ObjectNode envelope = mapper.createObjectNode();
        envelope.put("jsonrpc", "2.0");
        envelope.set("id", id);
        ObjectNode error = envelope.putObject("error");
        error.put("code", code);
        error.put("message", message);
        writeLine(mapper.writeValueAsString(envelope));
    }

    private void writeLine(String json) {
        out.println(json);
        out.flush();
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}
