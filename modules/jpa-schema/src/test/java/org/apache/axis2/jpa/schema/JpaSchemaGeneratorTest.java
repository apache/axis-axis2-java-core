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
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for the full JPA schema generation pipeline:
 * annotation introspection, HBM XML introspection, and schema generation.
 */
public class JpaSchemaGeneratorTest {

    // ═══════════════════════════════════════════════════════════════════════
    // Test entities (annotation-based)
    // ═══════════════════════════════════════════════════════════════════════

    /** Custom "exclude from write" annotation — stands in for a project-specific audit marker. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface AuditField {}

    public enum ProductStatus { ACTIVE, INACTIVE, SUSPENDED }

    @Entity
    @Table(name = "PRODUCT")
    public static class TestProduct {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private BigDecimal productID;

        @Column(nullable = false, length = 200)
        private String name;

        private Double unitPrice;

        @Enumerated(EnumType.STRING)
        private ProductStatus status;

        @Version
        private Long objVersion;

        @AuditField
        private Timestamp created;

        @AuditField
        private BigDecimal createdUserID;

        @Transient
        private String computedLabel;

        @ManyToOne
        private TestDepartment department;

        @OneToMany
        private List<TestLineItem> lineItems;
    }

    @Entity
    public static class TestDepartment {
        @Id
        @GeneratedValue
        private Long departmentID;
        private String name;
    }

    @Entity
    public static class TestLineItem {
        @Id
        @GeneratedValue
        private Long lineItemID;
        private Double weight;
    }

    /** Non-entity class — should be rejected by introspector. */
    public static class NotAnEntity {
        private String name;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Annotation introspector tests
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testAnnotationIntrospector_entityDetection() {
        assertTrue(AnnotationIntrospector.isEntity(TestProduct.class));
        assertFalse(AnnotationIntrospector.isEntity(NotAnEntity.class));
    }

    @Test
    public void testAnnotationIntrospector_nonEntityReturnsNull() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        assertNull(introspector.introspect(NotAnEntity.class));
    }

    @Test
    public void testAnnotationIntrospector_entityNameAndTable() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        EntitySchemaModel model = introspector.introspect(TestProduct.class);
        assertNotNull(model);
        assertEquals("TestProduct", model.getEntityName());
        assertEquals("PRODUCT", model.getTableName());
    }

    @Test
    public void testAnnotationIntrospector_idField() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        EntitySchemaModel model = introspector.introspect(TestProduct.class);
        assertEquals(1, model.getIdFieldNames().size());
        assertEquals("productID", model.getIdFieldNames().get(0));

        EntitySchemaModel.FieldModel idField = model.getFields().get("productID");
        assertNotNull(idField);
        assertTrue(idField.isGeneratedValue());
        assertFalse(idField.isNullable());
        assertEquals("number", idField.getJsonSchemaType()); // BigDecimal → number
    }

    @Test
    public void testAnnotationIntrospector_columnConstraints() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        EntitySchemaModel model = introspector.introspect(TestProduct.class);

        EntitySchemaModel.FieldModel nameField = model.getFields().get("name");
        assertNotNull(nameField);
        assertEquals("string", nameField.getJsonSchemaType());
        assertFalse(nameField.isNullable());
        assertEquals(Integer.valueOf(200), nameField.getMaxLength());
    }

    @Test
    public void testAnnotationIntrospector_versionField() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        EntitySchemaModel model = introspector.introspect(TestProduct.class);
        assertEquals("objVersion", model.getVersionFieldName());

        EntitySchemaModel.FieldModel vf = model.getFields().get("objVersion");
        assertTrue(vf.isVersionField());
        assertTrue(vf.isExcludedFromWrite());
    }

    @Test
    public void testAnnotationIntrospector_transientField() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        EntitySchemaModel model = introspector.introspect(TestProduct.class);

        EntitySchemaModel.FieldModel tf = model.getFields().get("computedLabel");
        assertTrue(tf.isTransientField());
    }

    @Test
    public void testAnnotationIntrospector_customWriteExcludeAnnotation() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        introspector.addWriteExcludeAnnotation(AuditField.class.getName());
        EntitySchemaModel model = introspector.introspect(TestProduct.class);

        EntitySchemaModel.FieldModel createdField = model.getFields().get("created");
        assertTrue("@AuditField should exclude from write", createdField.isIgnoredForWrite());
        assertTrue(createdField.isExcludedFromWrite());

        EntitySchemaModel.FieldModel createdUserId = model.getFields().get("createdUserID");
        assertTrue(createdUserId.isIgnoredForWrite());
    }

    @Test
    public void testAnnotationIntrospector_manyToOneRelationship() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        EntitySchemaModel model = introspector.introspect(TestProduct.class);

        EntitySchemaModel.FieldModel deptField = model.getFields().get("department");
        assertNotNull(deptField);
        assertEquals("object", deptField.getJsonSchemaType());
        assertEquals("TestDepartment", deptField.getRefEntityName());
        assertFalse(deptField.isCollection());
    }

    @Test
    public void testAnnotationIntrospector_oneToManyRelationship() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        EntitySchemaModel model = introspector.introspect(TestProduct.class);

        EntitySchemaModel.FieldModel itemsField = model.getFields().get("lineItems");
        assertNotNull(itemsField);
        assertEquals("array", itemsField.getJsonSchemaType());
        assertEquals("TestLineItem", itemsField.getRefEntityName());
        assertTrue(itemsField.isCollection());
    }

    @Test
    public void testAnnotationIntrospector_enumField() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        EntitySchemaModel model = introspector.introspect(TestProduct.class);

        EntitySchemaModel.FieldModel statusField = model.getFields().get("status");
        assertNotNull(statusField);
        assertEquals("string", statusField.getJsonSchemaType());
        assertNotNull(statusField.getEnumValues());
        assertEquals(3, statusField.getEnumValues().size());
        assertTrue(statusField.getEnumValues().contains("ACTIVE"));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Schema generator tests (from annotations)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testReadSchema_includesAllFields() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        introspector.addWriteExcludeAnnotation(AuditField.class.getName());
        EntitySchemaModel model = introspector.introspect(TestProduct.class);

        ObjectNode readSchema = JpaSchemaGenerator.generateReadSchema(model);
        JsonNode props = readSchema.get("properties");

        // Read schema should include everything except @Transient
        assertTrue(props.has("productID"));
        assertTrue(props.has("name"));
        assertTrue(props.has("created"));         // audit field — present in read
        assertTrue(props.has("objVersion"));       // version — present in read
        assertFalse(props.has("computedLabel"));   // @Transient — never present

        // ID should be marked readOnly
        assertTrue(props.get("productID").has("readOnly"));
        assertTrue(props.get("productID").get("readOnly").asBoolean());
    }

    @Test
    public void testWriteSchema_excludesServerManagedFields() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        introspector.addWriteExcludeAnnotation(AuditField.class.getName());
        EntitySchemaModel model = introspector.introspect(TestProduct.class);

        ObjectNode writeSchema = JpaSchemaGenerator.generateWriteSchema(model);
        JsonNode props = writeSchema.get("properties");

        // Write schema should exclude server-managed fields
        assertFalse("@GeneratedValue ID excluded", props.has("productID"));
        assertFalse("@Version excluded", props.has("objVersion"));
        assertFalse("@AuditField excluded", props.has("created"));
        assertFalse("@AuditField excluded", props.has("createdUserID"));
        assertFalse("@Transient excluded", props.has("computedLabel"));

        // Regular fields should be present
        assertTrue(props.has("name"));
        assertTrue(props.has("unitPrice"));
        assertTrue(props.has("status"));
        assertTrue(props.has("department"));
        assertTrue(props.has("lineItems"));
    }

    @Test
    public void testSchema_requiredArray() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        EntitySchemaModel model = introspector.introspect(TestProduct.class);

        ObjectNode readSchema = JpaSchemaGenerator.generateReadSchema(model);
        JsonNode required = readSchema.get("required");
        assertNotNull(required);

        boolean hasProductID = false;
        boolean hasName = false;
        for (JsonNode r : required) {
            if ("productID".equals(r.asText())) hasProductID = true;
            if ("name".equals(r.asText())) hasName = true;
        }
        assertTrue("productID should be required", hasProductID);
        assertTrue("name should be required", hasName);
    }

    @Test
    public void testSchema_relationshipRefs() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        EntitySchemaModel model = introspector.introspect(TestProduct.class);

        ObjectNode readSchema = JpaSchemaGenerator.generateReadSchema(model);
        JsonNode props = readSchema.get("properties");

        // ManyToOne → $ref
        JsonNode dept = props.get("department");
        assertEquals("#/components/schemas/TestDepartment", dept.get("$ref").asText());

        // OneToMany → array of $ref
        JsonNode items = props.get("lineItems");
        assertEquals("array", items.get("type").asText());
        assertEquals("#/components/schemas/TestLineItem",
                items.get("items").get("$ref").asText());
    }

    @Test
    public void testSchema_enumValues() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        EntitySchemaModel model = introspector.introspect(TestProduct.class);

        ObjectNode readSchema = JpaSchemaGenerator.generateReadSchema(model);
        JsonNode statusProp = readSchema.get("properties").get("status");
        assertTrue(statusProp.has("enum"));
        assertEquals(3, statusProp.get("enum").size());
    }

    @Test
    public void testBothSchemas_returnsReadAndWrite() {
        AnnotationIntrospector introspector = new AnnotationIntrospector();
        EntitySchemaModel model = introspector.introspect(TestProduct.class);

        Map<String, ObjectNode> schemas = JpaSchemaGenerator.generateBothSchemas(model);
        assertTrue(schemas.containsKey("TestProduct"));
        assertTrue(schemas.containsKey("TestProductWrite"));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HBM XML introspector tests
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testHbmXml_parsesLibraryEntity() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Library.hbm.xml")) {
            assertNotNull("Test HBM XML file must be on classpath", is);

            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Library.hbm.xml");

            assertNotNull(model);
            assertEquals("Library", model.getEntityName());
            assertFalse("Should have at least one ID field", model.getIdFieldNames().isEmpty());
            assertNotNull("Should have a version field", model.getVersionFieldName());
            assertTrue("Should have multiple fields", model.getFields().size() > 3);
        }
    }

    @Test
    public void testHbmXml_idFieldIsGeneratedAndRequired() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Library.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Library.hbm.xml");

            String idName = model.getIdFieldNames().get(0);
            EntitySchemaModel.FieldModel idField = model.getFields().get(idName);
            assertTrue("ID field should be generated", idField.isGeneratedValue());
            assertFalse("ID field should not be nullable", idField.isNullable());
        }
    }

    @Test
    public void testHbmXml_versionFieldExcludedFromWrite() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Library.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Library.hbm.xml");

            EntitySchemaModel.FieldModel vf = model.getFields().get(model.getVersionFieldName());
            assertNotNull(vf);
            assertTrue(vf.isVersionField());
            assertTrue(vf.isExcludedFromWrite());
        }
    }

    @Test
    public void testHbmXml_generatesBothSchemas() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Library.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Library.hbm.xml");

            ObjectNode readSchema = JpaSchemaGenerator.generateReadSchema(model);
            ObjectNode writeSchema = JpaSchemaGenerator.generateWriteSchema(model);

            JsonNode readProps = readSchema.get("properties");
            JsonNode writeProps = writeSchema.get("properties");

            // Read schema should contain generated ID and version; write should not
            assertTrue("Read schema must contain the generated ID field 'id'",
                    readProps.has("id"));
            assertFalse("Write schema must NOT contain the generated ID field 'id'",
                    writeProps.has("id"));
            assertTrue("Read schema must contain the version field 'version'",
                    readProps.has("version"));
            assertFalse("Write schema must NOT contain the version field 'version'",
                    writeProps.has("version"));
        }
    }

    @Test
    public void testHbmXml_propertyTypesMapCorrectly() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Library.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Library.hbm.xml");

            EntitySchemaModel.FieldModel nameField = model.getFields().get("name");
            if (nameField != null) {
                assertEquals("string", nameField.getJsonSchemaType());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HBM XML — large mapping (Branch: 50+ fields)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void testHbmXml_branch_parsesLargeEntity() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Branch.hbm.xml")) {
            assertNotNull("Branch.hbm.xml must be on classpath", is);

            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Branch.hbm.xml");

            assertNotNull(model);
            assertEquals("Branch", model.getEntityName());
            assertEquals("BRANCH", model.getTableName());

            // 50+ fields: properties + relationships + component fields
            assertTrue("Should have 40+ fields, got " + model.getFields().size(),
                    model.getFields().size() >= 40);
        }
    }

    @Test
    public void testHbmXml_branch_allTypesMapped() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Branch.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Branch.hbm.xml");

            // String property
            assertEquals("string", model.getFields().get("name").getJsonSchemaType());
            assertFalse("name is not-null", model.getFields().get("name").isNullable());

            // Timestamp property
            assertEquals("string", model.getFields().get("created").getJsonSchemaType());
            assertEquals("date-time", model.getFields().get("created").getJsonSchemaFormat());

            // Boolean property
            assertEquals("boolean", model.getFields().get("active").getJsonSchemaType());

            // Double property
            assertEquals("number", model.getFields().get("occupancyRate").getJsonSchemaType());

            // Long property
            assertEquals("integer", model.getFields().get("loansLast7Days").getJsonSchemaType());
            assertEquals("int64", model.getFields().get("loansLast7Days").getJsonSchemaFormat());

            // text type (maps to string)
            assertEquals("string", model.getFields().get("notes").getJsonSchemaType());
        }
    }

    @Test
    public void testHbmXml_branch_manyToOneRelationships() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Branch.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Branch.hbm.xml");

            // 4 many-to-one relationships
            EntitySchemaModel.FieldModel library = model.getFields().get("library");
            assertNotNull("library relationship must be parsed", library);
            assertEquals("object", library.getJsonSchemaType());
            assertEquals("Library", library.getRefEntityName());
            assertFalse("library is not-null", library.isNullable());

            EntitySchemaModel.FieldModel policy = model.getFields().get("accessPolicy");
            assertNotNull(policy);
            assertEquals("AccessPolicy", policy.getRefEntityName());
            assertTrue("accessPolicy is nullable", policy.isNullable());
        }
    }

    @Test
    public void testHbmXml_branch_collectionSets() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Branch.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Branch.hbm.xml");

            // 4 <set> collections
            EntitySchemaModel.FieldModel members = model.getFields().get("members");
            assertNotNull("members collection must be parsed", members);
            assertEquals("array", members.getJsonSchemaType());
            assertTrue(members.isCollection());
            assertEquals("Member", members.getRefEntityName());

            EntitySchemaModel.FieldModel catalogItems = model.getFields().get("catalogItems");
            assertNotNull("catalogItems collection must be parsed", catalogItems);
            assertEquals("CatalogItem", catalogItems.getRefEntityName());

            EntitySchemaModel.FieldModel events = model.getFields().get("events");
            assertNotNull(events);
            assertEquals("BranchEvent", events.getRefEntityName());

            EntitySchemaModel.FieldModel reservations = model.getFields().get("reservations");
            assertNotNull(reservations);
            assertEquals("RoomReservation", reservations.getRefEntityName());
        }
    }

    @Test
    public void testHbmXml_branch_componentFlattened() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Branch.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Branch.hbm.xml");

            // Component fields are flattened with prefix: branchAddress.city
            EntitySchemaModel.FieldModel city = model.getFields().get("branchAddress.city");
            assertNotNull("component field branchAddress.city must be present", city);
            assertEquals("string", city.getJsonSchemaType());

            EntitySchemaModel.FieldModel zip = model.getFields().get("branchAddress.zip");
            assertNotNull(zip);
            assertEquals("integer", zip.getJsonSchemaType());
        }
    }

    @Test
    public void testHbmXml_branch_nestedColumnNotNull() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Branch.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Branch.hbm.xml");

            // "deleted" uses nested <column not-null="true"> instead of attribute
            EntitySchemaModel.FieldModel deleted = model.getFields().get("deleted");
            assertNotNull(deleted);
            assertEquals("boolean", deleted.getJsonSchemaType());
            assertFalse("deleted with nested column not-null=true should be required",
                    deleted.isNullable());
        }
    }

    @Test
    public void testHbmXml_branch_schemaGeneration() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/Branch.hbm.xml")) {
            HbmXmlIntrospector introspector = new HbmXmlIntrospector();
            EntitySchemaModel model = introspector.introspect(is, "Branch.hbm.xml");

            ObjectNode readSchema = JpaSchemaGenerator.generateReadSchema(model);
            ObjectNode writeSchema = JpaSchemaGenerator.generateWriteSchema(model);

            JsonNode readProps = readSchema.get("properties");
            JsonNode writeProps = writeSchema.get("properties");

            // Read schema has everything
            assertTrue(readProps.has("id"));
            assertTrue(readProps.has("version"));
            assertTrue(readProps.has("name"));
            assertTrue(readProps.has("members"));
            assertTrue(readProps.has("library"));
            assertTrue(readProps.has("branchAddress.city"));

            // Write schema excludes generated ID and version
            assertFalse(writeProps.has("id"));
            assertFalse(writeProps.has("version"));

            // Write schema still has regular fields, relationships, collections
            assertTrue(writeProps.has("name"));
            assertTrue(writeProps.has("members"));
            assertTrue(writeProps.has("library"));
            assertTrue(writeProps.has("branchAddress.city"));

            // Relationship refs in read schema
            assertEquals("#/components/schemas/Library",
                    readProps.get("library").get("$ref").asText());
            assertEquals("array", readProps.get("members").get("type").asText());
            assertEquals("#/components/schemas/Member",
                    readProps.get("members").get("items").get("$ref").asText());
        }
    }
}
