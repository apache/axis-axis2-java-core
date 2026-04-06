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

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fetches the MCP tool catalog from {@code {baseUrl}/openapi-mcp.json} and
 * makes it available for {@link McpStdioServer}.
 *
 * <p>The catalog endpoint is produced by the {@code axis2-openapi} module.
 * It is fetched once at startup; restart the bridge to pick up newly deployed
 * services.
 */
public class ToolRegistry {

    private final String baseUrl;
    private final ObjectMapper mapper;
    private final HttpClient httpClient;

    private List<McpTool> tools = Collections.emptyList();
    private Map<String, McpTool> toolMap = Collections.emptyMap();

    public ToolRegistry(String baseUrl, ObjectMapper mapper, SSLContext sslContext) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.mapper = mapper;
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10));
        if (sslContext != null) {
            builder.sslContext(sslContext);
        }
        this.httpClient = builder.build();
    }

    /**
     * Fetches {@code /openapi-mcp.json} and builds the tool registry.
     * Logs to stderr; does not throw on partial failure (empty catalog is valid).
     */
    public void load() throws IOException, InterruptedException {
        String catalogUrl = baseUrl + "/openapi-mcp.json";
        System.err.println("[axis2-mcp-bridge] Loading tool catalog from: " + catalogUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(catalogUrl))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Tool catalog fetch failed: HTTP " + response.statusCode()
                    + " from " + catalogUrl);
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode toolsNode = root.path("tools");

        if (!toolsNode.isArray()) {
            System.err.println("[axis2-mcp-bridge] Warning: 'tools' array not found in catalog — no tools registered");
            return;
        }

        List<McpTool> loaded = new ArrayList<>();
        Map<String, McpTool> map = new LinkedHashMap<>();

        for (JsonNode toolNode : toolsNode) {
            String name = toolNode.path("name").asText(null);
            if (name == null || name.isEmpty()) continue;

            String description = toolNode.path("description").asText(name);
            JsonNode inputSchema = toolNode.path("inputSchema");
            String endpoint = toolNode.path("endpoint").asText("");

            McpTool tool = new McpTool(name, description, inputSchema, endpoint);
            loaded.add(tool);
            map.put(name, tool);
        }

        this.tools = Collections.unmodifiableList(loaded);
        this.toolMap = Collections.unmodifiableMap(map);
        System.err.println("[axis2-mcp-bridge] Loaded " + tools.size() + " tool(s): " + map.keySet());
    }

    public List<McpTool> getTools() { return tools; }

    public McpTool getTool(String name) { return toolMap.get(name); }
}
