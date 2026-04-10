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

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import java.io.IOException;
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
public class ToolRegistry implements java.io.Closeable {

    private final String baseUrl;
    private final ObjectMapper mapper;
    private final CloseableHttpClient httpClient;

    private List<McpTool> tools = Collections.emptyList();
    private Map<String, McpTool> toolMap = Collections.emptyMap();

    public ToolRegistry(String baseUrl, ObjectMapper mapper, SSLContext sslContext) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.mapper = mapper;
        this.httpClient = buildHttpClient(sslContext);
    }

    private static CloseableHttpClient buildHttpClient(SSLContext sslContext) {
        HttpClientConnectionManager connManager;
        if (sslContext != null) {
            connManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                            .setSslContext(sslContext)
                            .build())
                    .setMaxConnTotal(10)
                    .setMaxConnPerRoute(10)
                    .build();
        } else {
            connManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setMaxConnTotal(10)
                    .setMaxConnPerRoute(10)
                    .build();
        }
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(10))
                .setResponseTimeout(Timeout.ofSeconds(15))
                .build();
        return HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * Fetches {@code /openapi-mcp.json} and builds the tool registry.
     * Logs to stderr; does not throw on partial failure (empty catalog is valid).
     */
    public void load() throws IOException {
        String catalogUrl = baseUrl + "/openapi-mcp.json";
        System.err.println("[axis2-mcp-bridge] Loading tool catalog from: " + catalogUrl);

        HttpGet httpGet = new HttpGet(catalogUrl);
        httpGet.setHeader("Accept", "application/json");

        String responseBody = httpClient.execute(httpGet, response -> {
            int status = response.getCode();
            if (status != 200) {
                throw new IOException("Tool catalog fetch failed: HTTP " + status
                        + " from " + catalogUrl);
            }
            return EntityUtils.toString(response.getEntity());
        });

        JsonNode root = mapper.readTree(responseBody);
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

    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}
