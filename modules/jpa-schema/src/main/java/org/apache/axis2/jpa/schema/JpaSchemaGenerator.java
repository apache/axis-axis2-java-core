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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Converts an {@link EntitySchemaModel} to JSON Schema (as Jackson ObjectNode trees).
 *
 * <p>Produces two schema variants per entity:
 * <ul>
 *   <li><b>Read schema</b> ({@link #generateReadSchema}) — all fields including
 *       auto-generated IDs, version, and audit fields.  Suitable for GET response bodies.</li>
 *   <li><b>Write schema</b> ({@link #generateWriteSchema}) — excludes fields that
 *       the server manages: {@code @GeneratedValue} IDs, {@code @Version} fields,
 *       and fields marked with custom "ignore for write" annotations.
 *       Suitable for POST/PUT request bodies.</li>
 * </ul>
 *
 * <p>Relationships are emitted as {@code $ref} pointers using the convention
 * {@code #/components/schemas/{EntityName}}.  The caller is responsible for
 * ensuring that referenced entity schemas are also generated and added to
 * the OpenAPI components section.
 *
 * <p>Usage:
 * <pre>{@code
 * EntitySchemaModel model = annotationIntrospector.introspect(Fund.class);
 * ObjectNode readSchema = JpaSchemaGenerator.generateReadSchema(model);
 * ObjectNode writeSchema = JpaSchemaGenerator.generateWriteSchema(model);
 * }</pre>
 */
public class JpaSchemaGenerator {

    private static final Log log = LogFactory.getLog(JpaSchemaGenerator.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Generate the read (response) schema — includes all fields.
     */
    public static ObjectNode generateReadSchema(EntitySchemaModel model) {
        return generateSchema(model, false);
    }

    /**
     * Generate the write (request) schema — excludes auto-generated and
     * audit fields.
     */
    public static ObjectNode generateWriteSchema(EntitySchemaModel model) {
        return generateSchema(model, true);
    }

    /**
     * Generate a combined schema with both read and write variants as
     * separate schemas: {@code {EntityName}} (read) and
     * {@code {EntityName}Write} (write).
     *
     * @return a map of schema name → ObjectNode
     */
    public static Map<String, ObjectNode> generateBothSchemas(EntitySchemaModel model) {
        Map<String, ObjectNode> schemas = new java.util.LinkedHashMap<>();
        schemas.put(model.getEntityName(), generateReadSchema(model));
        schemas.put(model.getEntityName() + "Write", generateWriteSchema(model));
        return schemas;
    }

    // ── Private implementation ───────────────────────────────────────────────

    private static ObjectNode generateSchema(EntitySchemaModel model, boolean writeMode) {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("type", "object");
        schema.put("description", (writeMode ? "Write schema for " : "")
                + model.getEntityName()
                + (model.getTableName() != null
                    ? " (table: " + model.getTableName() + ")" : ""));

        ObjectNode properties = schema.putObject("properties");
        ArrayNode required = schema.putArray("required");

        for (EntitySchemaModel.FieldModel field : model.getFields().values()) {
            // Skip @Transient fields from all schemas
            if (field.isTransientField()) {
                continue;
            }

            // In write mode, skip server-managed fields
            if (writeMode && field.isExcludedFromWrite()) {
                continue;
            }

            ObjectNode prop = generateFieldSchema(field, writeMode);
            properties.set(field.getName(), prop);

            // Add to required array if not nullable
            if (!field.isNullable()) {
                required.add(field.getName());
            }
        }

        // Remove empty required array (cleaner output)
        if (required.size() == 0) {
            schema.remove("required");
        }

        return schema;
    }

    private static ObjectNode generateFieldSchema(EntitySchemaModel.FieldModel field,
                                                    boolean writeMode) {
        ObjectNode prop = mapper.createObjectNode();

        // ── Relationship: $ref to another entity schema ─────────────────
        if (field.getRefEntityName() != null) {
            if (field.isCollection()) {
                // OneToMany / ManyToMany → array of $ref
                prop.put("type", "array");
                ObjectNode items = prop.putObject("items");
                String refTarget = writeMode
                        ? field.getRefEntityName() + "Write"
                        : field.getRefEntityName();
                items.put("$ref", "#/components/schemas/" + refTarget);
            } else {
                // ManyToOne / OneToOne → $ref
                String refTarget = writeMode
                        ? field.getRefEntityName() + "Write"
                        : field.getRefEntityName();
                prop.put("$ref", "#/components/schemas/" + refTarget);
            }
            return prop;
        }

        // ── Enum ────────────────────────────────────────────────────────
        if (field.getEnumValues() != null && !field.getEnumValues().isEmpty()) {
            prop.put("type", "string");
            ArrayNode enumNode = prop.putArray("enum");
            for (String val : field.getEnumValues()) {
                enumNode.add(val);
            }
            return prop;
        }

        // ── Simple type ─────────────────────────────────────────────────
        prop.put("type", field.getJsonSchemaType());
        if (field.getJsonSchemaFormat() != null) {
            prop.put("format", field.getJsonSchemaFormat());
        }
        if (field.getMaxLength() != null) {
            prop.put("maxLength", field.getMaxLength());
        }

        // Mark auto-generated IDs as readOnly in read schemas
        // (they don't appear in write schemas at all)
        if (!writeMode && field.isGeneratedValue()) {
            prop.put("readOnly", true);
        }

        return prop;
    }
}
