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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified representation of a JPA/Hibernate entity's metadata, produced by
 * either {@link AnnotationIntrospector} (JPA annotations) or
 * {@link HbmXmlIntrospector} (.hbm.xml files).
 *
 * <p>This model is mapping-style-agnostic: both introspectors populate the
 * same fields, and {@link JpaSchemaGenerator} converts it to JSON Schema
 * without knowing which source produced it.
 *
 * <p>Two schema variants are generated from each model:
 * <ul>
 *   <li><b>Read schema</b> — all fields, including generated IDs and audit fields</li>
 *   <li><b>Write schema</b> — excludes auto-generated IDs, version fields, and
 *       fields marked with "ignore" annotations (e.g. audit timestamps)</li>
 * </ul>
 */
public class EntitySchemaModel {

    private String entityName;
    private String tableName;
    private final Map<String, FieldModel> fields = new LinkedHashMap<>();
    private final List<String> idFieldNames = new ArrayList<>();
    private String versionFieldName;

    // ── Field model ──────────────────────────────────────────────────────────

    public static class FieldModel {
        private String name;
        private String jsonSchemaType;      // "string", "integer", "number", "boolean", "array", "object"
        private String jsonSchemaFormat;     // "date-time", "int64", etc. (nullable)
        private boolean nullable = true;
        private boolean generatedValue;      // @GeneratedValue or <generator>
        private boolean versionField;        // @Version or <version>
        private boolean ignoredForWrite;     // @IgnoreChanges, @Transient, or custom
        private boolean transientField;      // @Transient — excluded from ALL schemas
        private Integer maxLength;           // @Column(length=) or hbm length attribute
        private String refEntityName;        // for relationships: target entity name
        private boolean collection;          // true for @OneToMany, <set>, <bag>
        private List<String> enumValues;     // for @Enumerated types

        public FieldModel(String name) {
            this.name = name;
        }

        // Getters / setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getJsonSchemaType() { return jsonSchemaType; }
        public void setJsonSchemaType(String jsonSchemaType) { this.jsonSchemaType = jsonSchemaType; }

        public String getJsonSchemaFormat() { return jsonSchemaFormat; }
        public void setJsonSchemaFormat(String jsonSchemaFormat) { this.jsonSchemaFormat = jsonSchemaFormat; }

        public boolean isNullable() { return nullable; }
        public void setNullable(boolean nullable) { this.nullable = nullable; }

        public boolean isGeneratedValue() { return generatedValue; }
        public void setGeneratedValue(boolean generatedValue) { this.generatedValue = generatedValue; }

        public boolean isVersionField() { return versionField; }
        public void setVersionField(boolean versionField) { this.versionField = versionField; }

        public boolean isIgnoredForWrite() { return ignoredForWrite; }
        public void setIgnoredForWrite(boolean ignoredForWrite) { this.ignoredForWrite = ignoredForWrite; }

        public boolean isTransientField() { return transientField; }
        public void setTransientField(boolean transientField) { this.transientField = transientField; }

        public Integer getMaxLength() { return maxLength; }
        public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }

        public String getRefEntityName() { return refEntityName; }
        public void setRefEntityName(String refEntityName) { this.refEntityName = refEntityName; }

        public boolean isCollection() { return collection; }
        public void setCollection(boolean collection) { this.collection = collection; }

        public List<String> getEnumValues() { return enumValues; }
        public void setEnumValues(List<String> enumValues) { this.enumValues = enumValues; }

        /** True if this field should be excluded from write schemas. */
        public boolean isExcludedFromWrite() {
            return transientField || ignoredForWrite || versionField
                    || generatedValue;
        }
    }

    // ── EntitySchemaModel getters / setters ──────────────────────────────────

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public Map<String, FieldModel> getFields() { return fields; }

    public void addField(FieldModel field) { fields.put(field.getName(), field); }

    public List<String> getIdFieldNames() { return idFieldNames; }

    public String getVersionFieldName() { return versionFieldName; }
    public void setVersionFieldName(String versionFieldName) { this.versionFieldName = versionFieldName; }
}
