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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extracts {@link EntitySchemaModel} from JPA-annotated entity classes.
 *
 * <p>Reads standard Jakarta Persistence annotations ({@code @Entity},
 * {@code @Column}, {@code @Id}, {@code @ManyToOne}, etc.) and maps them to
 * the unified schema model.  Also supports custom "ignore for write"
 * annotations (configurable via {@link #addWriteExcludeAnnotation}) for
 * project-specific audit field markers.
 *
 * <p>Usage:
 * <pre>{@code
 * AnnotationIntrospector introspector = new AnnotationIntrospector();
 * introspector.addWriteExcludeAnnotation("com.example.IgnoreChanges");
 * EntitySchemaModel model = introspector.introspect(Product.class);
 * }</pre>
 */
public class AnnotationIntrospector {

    private static final Log log = LogFactory.getLog(AnnotationIntrospector.class);

    /** Fully-qualified names of annotations that mark fields as excluded from write schemas. */
    private final Set<String> writeExcludeAnnotations = new HashSet<>();

    /**
     * Register a custom annotation that marks fields as excluded from write schemas.
     * For example, {@code "com.example.project.audit.IgnoreOnUpdate"}.
     */
    public void addWriteExcludeAnnotation(String annotationClassName) {
        writeExcludeAnnotations.add(annotationClassName);
    }

    /**
     * Introspect a JPA entity class and produce an {@link EntitySchemaModel}.
     *
     * @param entityClass the class annotated with {@code @Entity}
     * @return the schema model, or null if the class is not an {@code @Entity}
     */
    public EntitySchemaModel introspect(Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            return null;
        }

        EntitySchemaModel model = new EntitySchemaModel();

        // Entity name: @Entity(name=...) or simple class name
        Entity entityAnn = entityClass.getAnnotation(Entity.class);
        String entityName = (entityAnn.name() != null && !entityAnn.name().isEmpty())
                ? entityAnn.name() : entityClass.getSimpleName();
        model.setEntityName(entityName);

        // Table name: @Table(name=...) or entity name
        Table tableAnn = entityClass.getAnnotation(Table.class);
        if (tableAnn != null && tableAnn.name() != null && !tableAnn.name().isEmpty()) {
            model.setTableName(tableAnn.name());
        } else {
            model.setTableName(entityName);
        }

        // Walk all declared fields up the class hierarchy
        List<Field> allFields = getAllFields(entityClass);
        for (Field field : allFields) {
            EntitySchemaModel.FieldModel fm = introspectField(field);
            if (fm != null) {
                model.addField(fm);
                if (field.isAnnotationPresent(Id.class)) {
                    model.getIdFieldNames().add(fm.getName());
                }
                if (field.isAnnotationPresent(Version.class)) {
                    model.setVersionFieldName(fm.getName());
                }
            }
        }

        log.debug("Introspected JPA entity '" + entityName + "' with "
                + model.getFields().size() + " fields ("
                + model.getIdFieldNames().size() + " ID fields)");
        return model;
    }

    /**
     * Check if a class is a JPA entity (has @Entity annotation).
     */
    public static boolean isEntity(Class<?> clazz) {
        return clazz.isAnnotationPresent(Entity.class);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private EntitySchemaModel.FieldModel introspectField(Field field) {
        // @Transient fields are excluded from all schemas
        if (field.isAnnotationPresent(Transient.class)) {
            EntitySchemaModel.FieldModel fm = new EntitySchemaModel.FieldModel(field.getName());
            fm.setTransientField(true);
            return fm;
        }

        EntitySchemaModel.FieldModel fm = new EntitySchemaModel.FieldModel(field.getName());

        // ── Type mapping ────────────────────────────────────────────────
        Class<?> fieldType = field.getType();

        // Check for relationships first
        if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
            fm.setJsonSchemaType("object");
            fm.setRefEntityName(fieldType.getSimpleName());
        } else if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) {
            fm.setJsonSchemaType("array");
            fm.setCollection(true);
            // Extract generic type argument for the collection
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
                if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                    fm.setRefEntityName(((Class<?>) typeArgs[0]).getSimpleName());
                }
            }
        } else if (field.isAnnotationPresent(Enumerated.class)) {
            fm.setJsonSchemaType("string");
            if (fieldType.isEnum()) {
                List<String> enumValues = new ArrayList<>();
                for (Object constant : fieldType.getEnumConstants()) {
                    enumValues.add(constant.toString());
                }
                fm.setEnumValues(enumValues);
            }
        } else {
            mapJavaType(fieldType, fm);
        }

        // ── Column metadata ─────────────────────────────────────────────
        Column columnAnn = field.getAnnotation(Column.class);
        if (columnAnn != null) {
            fm.setNullable(columnAnn.nullable());
            if (columnAnn.length() != 255 && "string".equals(fm.getJsonSchemaType())) {
                // 255 is the JPA default — only set maxLength if explicitly specified
                fm.setMaxLength(columnAnn.length());
            }
        }

        // ── ID / GeneratedValue ─────────────────────────────────────────
        if (field.isAnnotationPresent(Id.class)) {
            fm.setNullable(false);
            if (field.isAnnotationPresent(GeneratedValue.class)) {
                fm.setGeneratedValue(true);
            }
        }

        // ── Version ─────────────────────────────────────────────────────
        if (field.isAnnotationPresent(Version.class)) {
            fm.setVersionField(true);
        }

        // ── Custom "ignore for write" annotations ───────────────────────
        // Check if any of the field's annotations match the configured
        // write-exclude annotation names.  Uses string comparison on the
        // annotation class name so we don't need a compile-time dependency
        // on project-specific annotation classes.
        for (Annotation ann : field.getAnnotations()) {
            if (writeExcludeAnnotations.contains(ann.annotationType().getName())) {
                fm.setIgnoredForWrite(true);
                break;
            }
        }

        return fm;
    }

    /**
     * Map a Java type to JSON Schema type + format.
     *
     * <p>Handles the common types found in JPA entities:
     * <ul>
     *   <li>BigDecimal used as ID → integer (common in legacy schemas)</li>
     *   <li>BigDecimal used as value → number</li>
     *   <li>Byte used as boolean flag → boolean (common in SQL Server entities)</li>
     * </ul>
     */
    private void mapJavaType(Class<?> type, EntitySchemaModel.FieldModel fm) {
        if (type == String.class) {
            fm.setJsonSchemaType("string");
        } else if (type == int.class || type == Integer.class) {
            fm.setJsonSchemaType("integer");
            fm.setJsonSchemaFormat("int32");
        } else if (type == long.class || type == Long.class) {
            fm.setJsonSchemaType("integer");
            fm.setJsonSchemaFormat("int64");
        } else if (type == BigInteger.class) {
            fm.setJsonSchemaType("integer");
        } else if (type == BigDecimal.class) {
            // BigDecimal is ambiguous: ID fields use it as integer, value fields as number.
            // Default to number; the caller can override for ID fields.
            fm.setJsonSchemaType("number");
        } else if (type == double.class || type == Double.class
                || type == float.class || type == Float.class) {
            fm.setJsonSchemaType("number");
        } else if (type == boolean.class || type == Boolean.class) {
            fm.setJsonSchemaType("boolean");
        } else if (type == byte.class || type == Byte.class) {
            // Byte is commonly used as a boolean flag in SQL Server entities
            // (0 = false, 1 = true).  Map to boolean for JSON Schema.
            fm.setJsonSchemaType("boolean");
        } else if (type == short.class || type == Short.class) {
            fm.setJsonSchemaType("integer");
        } else if (type == Timestamp.class || type == Date.class
                || type == Instant.class || type == LocalDateTime.class) {
            fm.setJsonSchemaType("string");
            fm.setJsonSchemaFormat("date-time");
        } else if (type == LocalDate.class) {
            fm.setJsonSchemaType("string");
            fm.setJsonSchemaFormat("date");
        } else if (type == byte[].class || type == Byte[].class) {
            fm.setJsonSchemaType("string");
            fm.setJsonSchemaFormat("byte"); // base64
        } else if (Collection.class.isAssignableFrom(type) || type.isArray()) {
            fm.setJsonSchemaType("array");
            fm.setCollection(true);
        } else {
            // Unknown type — treat as object
            fm.setJsonSchemaType("object");
        }
    }

    /** Collect all declared fields from the class and its superclasses. */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }
}
