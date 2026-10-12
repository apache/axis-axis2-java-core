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
 * <p>Uses test HBM files on the classpath: Library.hbm.xml,
 * Branch.hbm.xml, Book.hbm.xml.
 */
public class HbmBatchSchemaGeneratorTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    // ═══════════════════════════════════════════════════════════════════════
    // Book introspection — validates an HBM
    // mapping with id+generator, version, not-null, many-to-one, set,
    // nested column element
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testBook_parsesEntity() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Book.hbm.xml")) {
            assertNotNull("Book.hbm.xml must be on classpath", is);

            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Book.hbm.xml");

            assertNotNull(model);
            assertEquals("Book", model.getEntityName());
            assertEquals("BOOK", model.getTableName());
        }
    }

    @Test
    public void testBook_idAndVersion() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Book.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Book.hbm.xml");

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
    public void testBook_requiredProperty() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Book.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Book.hbm.xml");

            EntitySchemaModel.FieldModel name = model.getFields().get("title");
            assertNotNull(name);
            assertEquals("string", name.getJsonSchemaType());
            assertFalse("title is not-null=true", name.isNullable());
        }
    }

    @Test
    public void testBook_nullableProperties() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Book.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Book.hbm.xml");

            for (String field : new String[]{"isbn", "language", "publisher", "edition", "summary"}) {
                EntitySchemaModel.FieldModel f = model.getFields().get(field);
                assertNotNull(field + " should exist", f);
                assertTrue(field + " should be nullable", f.isNullable());
            }
        }
    }

    @Test
    public void testBook_manyToOneRelationship() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Book.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Book.hbm.xml");

            EntitySchemaModel.FieldModel author = model.getFields().get("author");
            assertNotNull("author relationship should exist", author);
            assertEquals("object", author.getJsonSchemaType());
            assertEquals("Author", author.getRefEntityName());
            assertFalse(author.isCollection());
        }
    }

    @Test
    public void testBook_setCollection() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Book.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Book.hbm.xml");

            EntitySchemaModel.FieldModel loans = model.getFields().get("loans");
            assertNotNull("loans set should exist", loans);
            assertEquals("array", loans.getJsonSchemaType());
            assertTrue(loans.isCollection());
            assertEquals("Loan", loans.getRefEntityName());
        }
    }

    @Test
    public void testBook_nestedColumnNotNull() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Book.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Book.hbm.xml");

            EntitySchemaModel.FieldModel deleted = model.getFields().get("deleted");
            assertNotNull(deleted);
            assertEquals("boolean", deleted.getJsonSchemaType());
            assertFalse("deleted with nested column not-null=true should be required",
                    deleted.isNullable());
        }
    }

    @Test
    public void testBook_readSchemaIncludesAll() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Book.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Book.hbm.xml");

            JsonNode readSchema = JpaSchemaGenerator.generateReadSchema(model);
            JsonNode props = readSchema.get("properties");

            assertTrue("read has id", props.has("id"));
            assertTrue("read has version", props.has("version"));
            assertTrue("read has title", props.has("title"));
            assertTrue("read has author", props.has("author"));
            assertTrue("read has loans", props.has("loans"));
            assertTrue("read has deleted", props.has("deleted"));

            // ID is readOnly in read schema
            assertTrue(props.get("id").has("readOnly"));

            // author is $ref
            assertEquals("#/components/schemas/Author",
                    props.get("author").get("$ref").asText());

            // loans is array of $ref
            assertEquals("array", props.get("loans").get("type").asText());
            assertEquals("#/components/schemas/Loan",
                    props.get("loans").get("items").get("$ref").asText());
        }
    }

    @Test
    public void testBook_writeSchemaExcludesServerManaged() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Book.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Book.hbm.xml");

            JsonNode writeSchema = JpaSchemaGenerator.generateWriteSchema(model);
            JsonNode props = writeSchema.get("properties");

            assertFalse("write excludes generated ID", props.has("id"));
            assertFalse("write excludes version", props.has("version"));
            assertTrue("write includes title", props.has("title"));
            assertTrue("write includes author", props.has("author"));
            assertTrue("write includes loans", props.has("loans"));

            // Write schema $refs use "Write" suffix
            assertEquals("#/components/schemas/AuthorWrite",
                    props.get("author").get("$ref").asText());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Batch generator — end-to-end test using test resources directory
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testBatchGenerator_producesValidOpenApiJson() throws IOException {
        // Find the test resources directory (contains Library, Branch, Book)
        URL resourceUrl = getClass().getResource("/Book.hbm.xml");
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
        // At minimum: Library, Branch, Book (3 entities × 2 = 6 schemas)
        assertTrue("should have at least 6 schemas, got " + schemas.size(),
                schemas.size() >= 6);

        // Verify specific entities exist
        assertTrue("Library read schema", schemas.has("Library"));
        assertTrue("Library write schema", schemas.has("LibraryWrite"));
        assertTrue("Book read schema", schemas.has("Book"));
        assertTrue("Book write schema", schemas.has("BookWrite"));
        assertTrue("Branch read schema", schemas.has("Branch"));
        assertTrue("Branch write schema", schemas.has("BranchWrite"));

        // Spot-check Book schema content
        JsonNode bookRead = schemas.get("Book");
        assertEquals("object", bookRead.get("type").asText());
        assertTrue(bookRead.get("properties").has("title"));
        assertTrue(bookRead.get("properties").has("author"));
        assertTrue(bookRead.get("properties").has("loans"));

        // Write schema should not have id/version
        JsonNode bookWrite = schemas.get("BookWrite");
        assertFalse(bookWrite.get("properties").has("id"));
        assertFalse(bookWrite.get("properties").has("version"));
    }

    @Test
    public void testBatchGenerator_fieldCount() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Book.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Book.hbm.xml");

            // 11 properties + 1 id + 1 version + 1 many-to-one + 1 set = 15 fields
            assertEquals("exact field count", 15, model.getFields().size());

            // Count relationships
            long relCount = model.getFields().values().stream()
                    .filter(f -> f.getRefEntityName() != null).count();
            assertEquals("2 relationships (author + loans)", 2, relCount);
        }
    }
}
