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

package org.apache.axis2.jpa.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;

import static org.junit.Assert.*;

/**
 * Tests for {@link HbmBatchSchemaGenerator} — the CLI batch tool that
 * converts a directory of HBM XML files into an OpenAPI 3.0 schemas JSON.
 *
 * <p>Uses test HBM files on the classpath: CompanyBO.hbm.xml,
 * DepartmentBO.hbm.xml, StockExchangeBO.hbm.xml.
 */
public class HbmBatchSchemaGeneratorTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    // ═══════════════════════════════════════════════════════════════════════
    // StockExchangeBO introspection — validates a production-style HBM
    // mapping with id+generator, version, not-null, many-to-one, set,
    // nested column element
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testStockExchange_parsesEntity() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/StockExchangeBO.hbm.xml")) {
            assertNotNull("StockExchangeBO.hbm.xml must be on classpath", is);

            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "StockExchangeBO.hbm.xml");

            assertNotNull(model);
            assertEquals("StockExchangeBO", model.getEntityName());
            assertEquals("STOCK_EXCHANGE", model.getTableName());
        }
    }

    @Test
    public void testStockExchange_idAndVersion() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/StockExchangeBO.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "StockExchangeBO.hbm.xml");

            assertEquals(1, model.getIdFieldNames().size());
            assertEquals("id", model.getIdFieldNames().get(0));

            EntitySchemaModel.FieldModel idField = model.getFields().get("id");
            assertTrue("ID should be generated", idField.isGeneratedValue());
            assertFalse("ID should not be nullable", idField.isNullable());

            assertEquals("version", model.getVersionFieldName());
            assertTrue(model.getFields().get("version").isVersionField());
            assertTrue(model.getFields().get("version").isExcludedFromWrite());
        }
    }

    @Test
    public void testStockExchange_requiredProperty() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/StockExchangeBO.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "StockExchangeBO.hbm.xml");

            EntitySchemaModel.FieldModel name = model.getFields().get("name");
            assertNotNull(name);
            assertEquals("string", name.getJsonSchemaType());
            assertFalse("name is not-null=true", name.isNullable());
        }
    }

    @Test
    public void testStockExchange_nullableProperties() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/StockExchangeBO.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "StockExchangeBO.hbm.xml");

            for (String field : new String[]{"micCode", "country", "city", "website", "timeZoneName"}) {
                EntitySchemaModel.FieldModel f = model.getFields().get(field);
                assertNotNull(field + " should exist", f);
                assertTrue(field + " should be nullable", f.isNullable());
            }
        }
    }

    @Test
    public void testStockExchange_manyToOneRelationship() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/StockExchangeBO.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "StockExchangeBO.hbm.xml");

            EntitySchemaModel.FieldModel region = model.getFields().get("region");
            assertNotNull("region relationship should exist", region);
            assertEquals("object", region.getJsonSchemaType());
            assertEquals("RegionBO", region.getRefEntityName());
            assertFalse(region.isCollection());
        }
    }

    @Test
    public void testStockExchange_setCollection() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/StockExchangeBO.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "StockExchangeBO.hbm.xml");

            EntitySchemaModel.FieldModel closedDates = model.getFields().get("closedDates");
            assertNotNull("closedDates set should exist", closedDates);
            assertEquals("array", closedDates.getJsonSchemaType());
            assertTrue(closedDates.isCollection());
            assertEquals("ExchangeClosedDateBO", closedDates.getRefEntityName());
        }
    }

    @Test
    public void testStockExchange_nestedColumnNotNull() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/StockExchangeBO.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "StockExchangeBO.hbm.xml");

            EntitySchemaModel.FieldModel deleted = model.getFields().get("deleted");
            assertNotNull(deleted);
            assertEquals("boolean", deleted.getJsonSchemaType());
            assertFalse("deleted with nested column not-null=true should be required",
                    deleted.isNullable());
        }
    }

    @Test
    public void testStockExchange_readSchemaIncludesAll() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/StockExchangeBO.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "StockExchangeBO.hbm.xml");

            JsonNode readSchema = JpaSchemaGenerator.generateReadSchema(model);
            JsonNode props = readSchema.get("properties");

            assertTrue("read has id", props.has("id"));
            assertTrue("read has version", props.has("version"));
            assertTrue("read has name", props.has("name"));
            assertTrue("read has region", props.has("region"));
            assertTrue("read has closedDates", props.has("closedDates"));
            assertTrue("read has deleted", props.has("deleted"));

            // ID is readOnly in read schema
            assertTrue(props.get("id").has("readOnly"));

            // region is $ref
            assertEquals("#/components/schemas/RegionBO",
                    props.get("region").get("$ref").asText());

            // closedDates is array of $ref
            assertEquals("array", props.get("closedDates").get("type").asText());
            assertEquals("#/components/schemas/ExchangeClosedDateBO",
                    props.get("closedDates").get("items").get("$ref").asText());
        }
    }

    @Test
    public void testStockExchange_writeSchemaExcludesServerManaged() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/StockExchangeBO.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "StockExchangeBO.hbm.xml");

            JsonNode writeSchema = JpaSchemaGenerator.generateWriteSchema(model);
            JsonNode props = writeSchema.get("properties");

            assertFalse("write excludes generated ID", props.has("id"));
            assertFalse("write excludes version", props.has("version"));
            assertTrue("write includes name", props.has("name"));
            assertTrue("write includes region", props.has("region"));
            assertTrue("write includes closedDates", props.has("closedDates"));

            // Write schema $refs use "Write" suffix
            assertEquals("#/components/schemas/RegionBOWrite",
                    props.get("region").get("$ref").asText());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Batch generator — end-to-end test using test resources directory
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testBatchGenerator_producesValidOpenApiJson() throws IOException {
        // Find the test resources directory (contains CompanyBO, DepartmentBO, StockExchangeBO)
        URL resourceUrl = getClass().getResource("/StockExchangeBO.hbm.xml");
        assertNotNull("test resource must be on classpath", resourceUrl);
        File resourceDir = new File(resourceUrl.getFile()).getParentFile();

        File outputFile = File.createTempFile("openapi-schemas-", ".json");
        outputFile.deleteOnExit();

        // Run the batch generator
        HbmBatchSchemaGenerator.main(new String[]{
                resourceDir.getAbsolutePath(),
                outputFile.getAbsolutePath()
        });

        // Verify the output is valid JSON
        assertTrue("output file should exist", outputFile.exists());
        assertTrue("output file should not be empty", outputFile.length() > 0);

        JsonNode root = mapper.readTree(outputFile);

        // OpenAPI structure
        assertEquals("3.0.1", root.get("openapi").asText());
        assertNotNull(root.get("info"));
        assertNotNull(root.get("components"));
        assertNotNull(root.get("components").get("schemas"));

        JsonNode schemas = root.get("components").get("schemas");

        // Should have read + write schemas for each entity
        // At minimum: CompanyBO, DepartmentBO, StockExchangeBO (3 entities × 2 = 6 schemas)
        assertTrue("should have at least 6 schemas, got " + schemas.size(),
                schemas.size() >= 6);

        // Verify specific entities exist
        assertTrue("CompanyBO read schema", schemas.has("CompanyBO"));
        assertTrue("CompanyBO write schema", schemas.has("CompanyBOWrite"));
        assertTrue("StockExchangeBO read schema", schemas.has("StockExchangeBO"));
        assertTrue("StockExchangeBO write schema", schemas.has("StockExchangeBOWrite"));
        assertTrue("DepartmentBO read schema", schemas.has("DepartmentBO"));
        assertTrue("DepartmentBO write schema", schemas.has("DepartmentBOWrite"));

        // Spot-check StockExchangeBO schema content
        JsonNode exchangeRead = schemas.get("StockExchangeBO");
        assertEquals("object", exchangeRead.get("type").asText());
        assertTrue(exchangeRead.get("properties").has("name"));
        assertTrue(exchangeRead.get("properties").has("region"));
        assertTrue(exchangeRead.get("properties").has("closedDates"));

        // Write schema should not have id/version
        JsonNode exchangeWrite = schemas.get("StockExchangeBOWrite");
        assertFalse(exchangeWrite.get("properties").has("id"));
        assertFalse(exchangeWrite.get("properties").has("version"));
    }

    @Test
    public void testBatchGenerator_fieldCount() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/StockExchangeBO.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "StockExchangeBO.hbm.xml");

            // 11 properties + 1 id + 1 version + 1 many-to-one + 1 set = 15 fields
            assertEquals("exact field count", 15, model.getFields().size());

            // Count relationships
            long relCount = model.getFields().values().stream()
                    .filter(f -> f.getRefEntityName() != null).count();
            assertEquals("2 relationships (region + closedDates)", 2, relCount);
        }
    }
}
