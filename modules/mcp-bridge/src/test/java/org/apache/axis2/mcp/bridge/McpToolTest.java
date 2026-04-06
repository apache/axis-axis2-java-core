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

/**
 * Unit tests for {@link McpTool}.
 *
 * <p>Verifies path extraction, description fallback, and field accessors.
 */
public class McpToolTest extends TestCase {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ── path extraction ─────────────────────────────────────────────────────

    public void testPathExtractionStripsMethodPrefix() {
        McpTool tool = new McpTool("op", "desc", MAPPER.createObjectNode(),
                "POST /services/MyService/op");
        assertEquals("/services/MyService/op", tool.getPath());
    }

    public void testPathExtractionWithPutPrefix() {
        McpTool tool = new McpTool("op", "desc", MAPPER.createObjectNode(),
                "PUT /services/MyService/op");
        assertEquals("/services/MyService/op", tool.getPath());
    }

    public void testPathExtractionWithGetPrefix() {
        McpTool tool = new McpTool("op", "desc", MAPPER.createObjectNode(),
                "GET /services/MyService/op");
        assertEquals("/services/MyService/op", tool.getPath());
    }

    /**
     * When the endpoint string contains no space, path should equal the full string.
     */
    public void testPathFallsBackToEndpointWhenNoSpace() {
        McpTool tool = new McpTool("op", "desc", MAPPER.createObjectNode(),
                "/services/MyService/op");
        assertEquals("/services/MyService/op", tool.getPath());
    }

    /**
     * getEndpoint() must return the original unmodified endpoint string.
     */
    public void testGetEndpointReturnsOriginal() {
        String endpoint = "POST /services/Svc/doThing";
        McpTool tool = new McpTool("doThing", "desc", MAPPER.createObjectNode(), endpoint);
        assertEquals(endpoint, tool.getEndpoint());
    }

    // ── description fallback ────────────────────────────────────────────────

    public void testNullDescriptionFallsBackToName() {
        McpTool tool = new McpTool("myOp", null, MAPPER.createObjectNode(),
                "POST /services/Svc/myOp");
        assertEquals("myOp", tool.getDescription());
    }

    public void testNonNullDescriptionIsPreserved() {
        McpTool tool = new McpTool("myOp", "Does something useful",
                MAPPER.createObjectNode(), "POST /services/Svc/myOp");
        assertEquals("Does something useful", tool.getDescription());
    }

    public void testEmptyDescriptionIsNotReplacedByName() {
        // Empty string is not null — it should be preserved as-is
        McpTool tool = new McpTool("myOp", "", MAPPER.createObjectNode(),
                "POST /services/Svc/myOp");
        assertEquals("", tool.getDescription());
    }

    // ── other accessors ─────────────────────────────────────────────────────

    public void testGetName() {
        McpTool tool = new McpTool("processBigDataSet", "desc",
                MAPPER.createObjectNode(), "POST /services/Svc/processBigDataSet");
        assertEquals("processBigDataSet", tool.getName());
    }

    public void testGetInputSchemaReturnsStoredNode() throws Exception {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        schema.putObject("properties").put("datasetId", "string");

        McpTool tool = new McpTool("op", "desc", schema, "POST /services/Svc/op");

        JsonNode retrieved = tool.getInputSchema();
        assertNotNull(retrieved);
        assertEquals("object", retrieved.path("type").asText());
        assertTrue(retrieved.path("properties").has("datasetId"));
    }

    public void testNullInputSchemaIsStored() {
        McpTool tool = new McpTool("op", "desc", null, "POST /services/Svc/op");
        assertNull(tool.getInputSchema());
    }

    /**
     * Deep path: "POST /services/BigDataH2Service/processBigDataSet" → path
     * must start with "/" and contain the service and operation names.
     */
    public void testDeepPathExtraction() {
        McpTool tool = new McpTool("processBigDataSet", "BigDataH2Service: processBigDataSet",
                MAPPER.createObjectNode(),
                "POST /services/BigDataH2Service/processBigDataSet");
        String path = tool.getPath();
        assertTrue("Path must start with /", path.startsWith("/"));
        assertTrue("Path must contain service name", path.contains("BigDataH2Service"));
        assertTrue("Path must contain operation name", path.contains("processBigDataSet"));
        assertFalse("Path must not contain 'POST'", path.contains("POST"));
    }

    /**
     * Multiple spaces in endpoint: only the first space separates method from path.
     */
    public void testEndpointWithSpaceInPath() {
        // Contrived but validates substring-from-first-space behaviour
        McpTool tool = new McpTool("op", "desc", MAPPER.createObjectNode(),
                "POST /services/My Service/op");
        // Should strip "POST " and keep the rest including the space
        assertEquals("/services/My Service/op", tool.getPath());
    }
}
