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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.junit.Before;
import org.junit.Test;

import javax.xml.namespace.QName;

import static org.junit.Assert.*;

/**
 * Tests for MCP auto-schema generation from Java method parameter types.
 * Covers the reflection-based fallback when mcpInputSchema is not set
 * in services.xml.
 */
public class McpAutoSchemaTest {

    private ConfigurationContext configurationContext;
    private OpenApiSpecGenerator generator;
    private ObjectMapper jackson;

    // ── Test service class with typed POJO parameter ──
    public static class SampleRequest {
        private int count;
        private double price;
        private String name;
        private boolean active;
        private long timestamp;
        private double[] values;
        private double[][] matrix;
        private String requestId;
        private java.util.List<String> tags;

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public double[] getValues() { return values; }
        public void setValues(double[] values) { this.values = values; }
        public double[][] getMatrix() { return matrix; }
        public void setMatrix(double[][] matrix) { this.matrix = matrix; }
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        public java.util.List<String> getTags() { return tags; }
        public void setTags(java.util.List<String> tags) { this.tags = tags; }
    }

    public static class SampleResponse {
        private String result;
        public String getResult() { return result; }
        public void setResult(String result) { this.result = result; }
    }

    public static class SampleService {
        public SampleResponse calculate(SampleRequest request) {
            return new SampleResponse();
        }
        public String echo(String message) {
            return message;
        }
    }

    @Before
    public void setUp() throws Exception {
        configurationContext = ConfigurationContextFactory
                .createEmptyConfigurationContext();
        generator = new OpenApiSpecGenerator(configurationContext);
        jackson = io.swagger.v3.core.util.Json.mapper();
    }

    private AxisService createServiceWithClass(String serviceName, String className)
            throws Exception {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        AxisService service = new AxisService(serviceName);
        service.addParameter(new Parameter("ServiceClass", className));
        axisConfig.addService(service);
        return service;
    }

    private void addOperation(AxisService service, String opName) throws Exception {
        AxisOperation op = new InOutAxisOperation(new QName(opName));
        service.addOperation(op);
    }

    @Test
    public void testAutoSchemaFromServiceClass() throws Exception {
        AxisService service = createServiceWithClass("SampleService",
                McpAutoSchemaTest.SampleService.class.getName());
        addOperation(service, "calculate");

        String catalog = generator.generateMcpCatalogJson(null);
        assertNotNull(catalog);

        JsonNode root = jackson.readTree(catalog);
        JsonNode tools = root.get("tools");
        assertNotNull(tools);

        JsonNode calcTool = null;
        for (JsonNode tool : tools) {
            if ("calculate".equals(tool.get("name").asText())) {
                calcTool = tool;
                break;
            }
        }
        assertNotNull("calculate tool should be in catalog", calcTool);

        JsonNode schema = calcTool.get("inputSchema");
        assertNotNull("inputSchema should be present", schema);
        assertEquals("object", schema.get("type").asText());

        JsonNode props = schema.get("properties");
        assertNotNull("properties should be present", props);

        // Verify type mappings
        assertTrue("should have count property", props.has("count"));
        assertEquals("integer", props.get("count").get("type").asText());

        assertTrue("should have price property", props.has("price"));
        assertEquals("number", props.get("price").get("type").asText());

        assertTrue("should have name property", props.has("name"));
        assertEquals("string", props.get("name").get("type").asText());

        assertTrue("should have active property", props.has("active"));
        assertEquals("boolean", props.get("active").get("type").asText());

        assertTrue("should have timestamp property", props.has("timestamp"));
        assertEquals("integer", props.get("timestamp").get("type").asText());
    }

    @Test
    public void testAutoSchemaArrayTypes() throws Exception {
        AxisService service = createServiceWithClass("SampleService",
                McpAutoSchemaTest.SampleService.class.getName());
        addOperation(service, "calculate");

        String catalog = generator.generateMcpCatalogJson(null);
        JsonNode root = jackson.readTree(catalog);
        JsonNode calcTool = null;
        for (JsonNode tool : root.get("tools")) {
            if ("calculate".equals(tool.get("name").asText())) {
                calcTool = tool;
                break;
            }
        }
        JsonNode props = calcTool.get("inputSchema").get("properties");

        // double[] -> array of numbers
        assertTrue("should have values property", props.has("values"));
        assertEquals("array", props.get("values").get("type").asText());
        assertEquals("number", props.get("values").get("items").get("type").asText());

        // double[][] -> array of arrays of numbers
        assertTrue("should have matrix property", props.has("matrix"));
        assertEquals("array", props.get("matrix").get("type").asText());
        assertEquals("array", props.get("matrix").get("items").get("type").asText());
        assertEquals("number", props.get("matrix").get("items").get("items").get("type").asText());
    }

    @Test
    public void testAutoSchemaSkipsPrimitiveParameter() throws Exception {
        // echo(String) has a primitive parameter — should fall back to empty schema
        AxisService service = createServiceWithClass("SampleService",
                McpAutoSchemaTest.SampleService.class.getName());
        addOperation(service, "echo");

        String catalog = generator.generateMcpCatalogJson(null);
        JsonNode root = jackson.readTree(catalog);
        JsonNode echoTool = null;
        for (JsonNode tool : root.get("tools")) {
            if ("echo".equals(tool.get("name").asText())) {
                echoTool = tool;
                break;
            }
        }
        assertNotNull("echo tool should be in catalog", echoTool);
        JsonNode props = echoTool.get("inputSchema").get("properties");
        assertEquals("String param should produce empty properties",
                0, props.size());
    }

    @Test
    public void testExplicitSchemaOverridesAutoGeneration() throws Exception {
        AxisService service = createServiceWithClass("SampleService",
                McpAutoSchemaTest.SampleService.class.getName());
        AxisOperation op = new InOutAxisOperation(new QName("calculate"));
        op.addParameter(new Parameter("mcpInputSchema",
                "{\"type\":\"object\",\"properties\":{\"custom\":{\"type\":\"string\"}}}"));
        service.addOperation(op);

        String catalog = generator.generateMcpCatalogJson(null);
        JsonNode root = jackson.readTree(catalog);
        JsonNode calcTool = null;
        for (JsonNode tool : root.get("tools")) {
            if ("calculate".equals(tool.get("name").asText())) {
                calcTool = tool;
                break;
            }
        }
        JsonNode props = calcTool.get("inputSchema").get("properties");
        assertTrue("explicit schema should have custom property", props.has("custom"));
        assertFalse("explicit schema should NOT have auto-generated count property",
                props.has("count"));
    }

    @Test
    public void testNoServiceClassProducesEmptySchema() throws Exception {
        // Service with SpringBeanName only, no ServiceClass, no HttpServletRequest
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        AxisService service = new AxisService("SpringOnlyService");
        service.addParameter(new Parameter("SpringBeanName", "myBean"));
        axisConfig.addService(service);
        addOperation(service, "doSomething");

        String catalog = generator.generateMcpCatalogJson(null);
        JsonNode root = jackson.readTree(catalog);
        JsonNode tool = null;
        for (JsonNode t : root.get("tools")) {
            if ("doSomething".equals(t.get("name").asText())) {
                tool = t;
                break;
            }
        }
        assertNotNull(tool);
        // Without HttpServletRequest, Spring bean can't be resolved — empty schema
        JsonNode props = tool.get("inputSchema").get("properties");
        assertEquals("Should fall back to empty schema without request", 0, props.size());
    }

    @Test
    public void testBooleanIsGetterDetected() throws Exception {
        AxisService service = createServiceWithClass("SampleService",
                McpAutoSchemaTest.SampleService.class.getName());
        addOperation(service, "calculate");

        String catalog = generator.generateMcpCatalogJson(null);
        JsonNode root = jackson.readTree(catalog);
        JsonNode calcTool = null;
        for (JsonNode tool : root.get("tools")) {
            if ("calculate".equals(tool.get("name").asText())) {
                calcTool = tool;
                break;
            }
        }
        JsonNode props = calcTool.get("inputSchema").get("properties");
        assertTrue("isActive() should produce active property", props.has("active"));
        assertEquals("boolean", props.get("active").get("type").asText());
    }

    @Test
    public void testAutoSchemaGenericListType() throws Exception {
        AxisService service = createServiceWithClass("SampleService",
                McpAutoSchemaTest.SampleService.class.getName());
        addOperation(service, "calculate");

        String catalog = generator.generateMcpCatalogJson(null);
        JsonNode root = jackson.readTree(catalog);
        JsonNode calcTool = null;
        for (JsonNode tool : root.get("tools")) {
            if ("calculate".equals(tool.get("name").asText())) {
                calcTool = tool;
                break;
            }
        }
        JsonNode props = calcTool.get("inputSchema").get("properties");

        // List<String> -> array of strings
        assertTrue("should have tags property", props.has("tags"));
        assertEquals("array", props.get("tags").get("type").asText());
        assertEquals("string", props.get("tags").get("items").get("type").asText());
    }
}
