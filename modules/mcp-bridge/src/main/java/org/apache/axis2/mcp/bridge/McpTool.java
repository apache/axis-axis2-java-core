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

/**
 * Represents one MCP tool entry parsed from {@code /openapi-mcp.json}.
 *
 * <p>Fields mirror the tool catalog format produced by {@code axis2-openapi}:
 * <pre>
 * {
 *   "name":        "processBigDataSet",
 *   "description": "BigDataH2Service: processBigDataSet",
 *   "inputSchema": { "type": "object", "properties": {}, "required": [] },
 *   "endpoint":    "POST /services/BigDataH2Service/processBigDataSet"
 * }
 * </pre>
 */
public class McpTool {

    private final String name;
    private final String description;
    private final JsonNode inputSchema;
    /** Raw endpoint string, e.g. {@code "POST /services/Svc/op"}. */
    private final String endpoint;
    /** Just the path portion, e.g. {@code "/services/Svc/op"}. */
    private final String path;

    public McpTool(String name, String description, JsonNode inputSchema, String endpoint) {
        this.name = name;
        this.description = description != null ? description : name;
        this.inputSchema = inputSchema;
        this.endpoint = endpoint;
        // Strip the HTTP method prefix to get just the path
        this.path = endpoint.contains(" ") ? endpoint.substring(endpoint.indexOf(' ') + 1) : endpoint;
    }

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public JsonNode getInputSchema() { return inputSchema; }
    public String getEndpoint()    { return endpoint; }
    public String getPath()        { return path; }
}
