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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracts {@link EntitySchemaModel} from Hibernate XML mapping files (.hbm.xml).
 *
 * <p>Parses the Hibernate 3.0 DTD mapping format:
 * <pre>{@code
 * <hibernate-mapping package="com.example.bo">
 *   <class name="CompanyBO" table="`COMPANY`">
 *     <id name="id" type="java.lang.Long" column="`companyID`">
 *       <generator class="native"/>
 *     </id>
 *     <version name="version" type="java.lang.Long" column="OBJ_VERSION"/>
 *     <property name="name" type="java.lang.String" not-null="true"/>
 *     <many-to-one name="createdUser" class="...UserBO" column="`createdUserID`"/>
 *     <set name="departments" cascade="all-delete-orphan">
 *       <key column="`companyID`"/>
 *       <one-to-many class="...DepartmentBO"/>
 *     </set>
 *   </class>
 * </hibernate-mapping>
 * }</pre>
 *
 * <p>The parser ignores the Hibernate DTD (resolves it to empty) to avoid
 * network fetches and to work in air-gapped environments.
 */
public class HbmXmlIntrospector {

    private static final Log log = LogFactory.getLog(HbmXmlIntrospector.class);

    // Hibernate type → JSON Schema type mapping
    private static final Map<String, String[]> TYPE_MAP = new HashMap<>();

    static {
        // [jsonType, jsonFormat]
        TYPE_MAP.put("java.lang.String",  new String[]{"string",  null});
        TYPE_MAP.put("string",            new String[]{"string",  null});
        TYPE_MAP.put("java.lang.Integer", new String[]{"integer", "int32"});
        TYPE_MAP.put("integer",           new String[]{"integer", "int32"});
        TYPE_MAP.put("int",               new String[]{"integer", "int32"});
        TYPE_MAP.put("java.lang.Long",    new String[]{"integer", "int64"});
        TYPE_MAP.put("long",              new String[]{"integer", "int64"});
        TYPE_MAP.put("java.lang.Short",   new String[]{"integer", null});
        TYPE_MAP.put("short",             new String[]{"integer", null});
        TYPE_MAP.put("java.lang.Double",  new String[]{"number",  null});
        TYPE_MAP.put("double",            new String[]{"number",  null});
        TYPE_MAP.put("java.lang.Float",   new String[]{"number",  null});
        TYPE_MAP.put("float",             new String[]{"number",  null});
        TYPE_MAP.put("java.math.BigDecimal", new String[]{"number", null});
        TYPE_MAP.put("big_decimal",       new String[]{"number",  null});
        TYPE_MAP.put("java.lang.Boolean", new String[]{"boolean", null});
        TYPE_MAP.put("boolean",           new String[]{"boolean", null});
        TYPE_MAP.put("yes_no",            new String[]{"boolean", null});
        TYPE_MAP.put("true_false",        new String[]{"boolean", null});
        TYPE_MAP.put("java.lang.Byte",    new String[]{"boolean", null});
        TYPE_MAP.put("byte",              new String[]{"boolean", null});
        TYPE_MAP.put("timestamp",         new String[]{"string",  "date-time"});
        TYPE_MAP.put("java.sql.Timestamp", new String[]{"string", "date-time"});
        TYPE_MAP.put("java.util.Date",    new String[]{"string",  "date-time"});
        TYPE_MAP.put("date",              new String[]{"string",  "date"});
        TYPE_MAP.put("java.sql.Date",     new String[]{"string",  "date"});
        TYPE_MAP.put("binary",            new String[]{"string",  "byte"});
        TYPE_MAP.put("text",              new String[]{"string",  null});
        TYPE_MAP.put("clob",              new String[]{"string",  null});
        TYPE_MAP.put("blob",              new String[]{"string",  "byte"});
    }

    /**
     * Parse a .hbm.xml file and produce an {@link EntitySchemaModel}.
     *
     * @param inputStream the .hbm.xml file content
     * @param resourceName name for logging (e.g. "CompanyBO.hbm.xml")
     * @return the schema model, or null on parse failure
     */
    public EntitySchemaModel introspect(InputStream inputStream, String resourceName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Security: allow DOCTYPE (needed for legacy HBM files that declare
            // the Hibernate DTD), but disable all external entity processing
            // to prevent XXE attacks.  The EntityResolver below further blocks
            // network DTD fetches by returning empty content for all entities.
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            // Resolve all DTD references to empty — avoids network fetch for
            // Hibernate DTDs and works in air-gapped environments.
            builder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId)
                        throws SAXException, IOException {
                    return new InputSource(new StringReader(""));
                }
            });

            Document doc = builder.parse(inputStream);
            Element root = doc.getDocumentElement(); // <hibernate-mapping>

            // Package prefix for class name resolution
            String packageName = root.getAttribute("package");

            // Find the <class> element (typically one per .hbm.xml)
            NodeList classNodes = root.getElementsByTagName("class");
            if (classNodes.getLength() == 0) {
                log.debug("No <class> element found in " + resourceName);
                return null;
            }

            Element classEl = (Element) classNodes.item(0);
            return parseClassElement(classEl, packageName, resourceName);

        } catch (Exception e) {
            // Sanitize log inputs to prevent CR/LF log injection from
            // malformed XML content or attacker-controlled file names.
            String safeName = resourceName != null
                    ? resourceName.replaceAll("[\\r\\n]", "_") : "<null>";
            String safeMsg = e.getMessage() != null
                    ? e.getMessage().replaceAll("[\\r\\n]", "_") : "<null>";
            log.warn("Failed to parse HBM XML '" + safeName + "': " + safeMsg);
            return null;
        }
    }

    // ── Private parsing ──────────────────────────────────────────────────────

    private EntitySchemaModel parseClassElement(Element classEl, String packageName,
                                                 String resourceName) {
        EntitySchemaModel model = new EntitySchemaModel();

        String className = classEl.getAttribute("name");
        model.setEntityName(className);

        // Table name: strip backticks (SQL Server quoting)
        String tableName = classEl.getAttribute("table");
        if (tableName != null) {
            tableName = tableName.replace("`", "");
        }
        model.setTableName(tableName != null ? tableName : className);

        // ── <id> element ────────────────────────────────────────────────
        NodeList idNodes = getDirectChildren(classEl, "id");
        for (int i = 0; i < idNodes.getLength(); i++) {
            Element idEl = (Element) idNodes.item(i);
            EntitySchemaModel.FieldModel fm = parsePropertyElement(idEl);
            fm.setNullable(false);
            // Check for <generator> child — if present, it's auto-generated
            NodeList generators = idEl.getElementsByTagName("generator");
            if (generators.getLength() > 0) {
                fm.setGeneratedValue(true);
            }
            model.addField(fm);
            model.getIdFieldNames().add(fm.getName());
        }

        // ── <composite-id> ──────────────────────────────────────────────
        NodeList compositeIdNodes = getDirectChildren(classEl, "composite-id");
        for (int i = 0; i < compositeIdNodes.getLength(); i++) {
            Element compEl = (Element) compositeIdNodes.item(i);
            NodeList keyProps = compEl.getElementsByTagName("key-property");
            for (int j = 0; j < keyProps.getLength(); j++) {
                EntitySchemaModel.FieldModel fm = parsePropertyElement((Element) keyProps.item(j));
                fm.setNullable(false);
                model.addField(fm);
                model.getIdFieldNames().add(fm.getName());
            }
        }

        // ── <version> element ───────────────────────────────────────────
        NodeList versionNodes = getDirectChildren(classEl, "version");
        for (int i = 0; i < versionNodes.getLength(); i++) {
            Element versionEl = (Element) versionNodes.item(i);
            EntitySchemaModel.FieldModel fm = parsePropertyElement(versionEl);
            fm.setVersionField(true);
            model.addField(fm);
            model.setVersionFieldName(fm.getName());
        }

        // ── <property> elements ─────────────────────────────────────────
        NodeList propNodes = getDirectChildren(classEl, "property");
        for (int i = 0; i < propNodes.getLength(); i++) {
            Element propEl = (Element) propNodes.item(i);
            EntitySchemaModel.FieldModel fm = parsePropertyElement(propEl);
            model.addField(fm);
        }

        // ── <many-to-one> elements ──────────────────────────────────────
        NodeList manyToOneNodes = getDirectChildren(classEl, "many-to-one");
        for (int i = 0; i < manyToOneNodes.getLength(); i++) {
            Element mtoEl = (Element) manyToOneNodes.item(i);
            EntitySchemaModel.FieldModel fm = new EntitySchemaModel.FieldModel(
                    mtoEl.getAttribute("name"));
            fm.setJsonSchemaType("object");

            // Resolve target entity name from class attribute
            String targetClass = mtoEl.getAttribute("class");
            if (targetClass != null && !targetClass.isEmpty()) {
                fm.setRefEntityName(simpleClassName(targetClass));
            }

            String notNull = mtoEl.getAttribute("not-null");
            fm.setNullable(!"true".equals(notNull));

            model.addField(fm);
        }

        // ── <set>, <bag>, <list> with <one-to-many> ─────────────────────
        for (String collTag : new String[]{"set", "bag", "list"}) {
            NodeList collNodes = getDirectChildren(classEl, collTag);
            for (int i = 0; i < collNodes.getLength(); i++) {
                Element collEl = (Element) collNodes.item(i);
                EntitySchemaModel.FieldModel fm = new EntitySchemaModel.FieldModel(
                        collEl.getAttribute("name"));
                fm.setJsonSchemaType("array");
                fm.setCollection(true);

                // Find <one-to-many> child for target class
                NodeList otmNodes = collEl.getElementsByTagName("one-to-many");
                if (otmNodes.getLength() > 0) {
                    Element otmEl = (Element) otmNodes.item(0);
                    String targetClass = otmEl.getAttribute("class");
                    if (targetClass != null && !targetClass.isEmpty()) {
                        fm.setRefEntityName(simpleClassName(targetClass));
                    }
                }

                // Find <many-to-many> child for target class
                NodeList mtmNodes = collEl.getElementsByTagName("many-to-many");
                if (mtmNodes.getLength() > 0) {
                    Element mtmEl = (Element) mtmNodes.item(0);
                    String targetClass = mtmEl.getAttribute("class");
                    if (targetClass != null && !targetClass.isEmpty()) {
                        fm.setRefEntityName(simpleClassName(targetClass));
                    }
                }

                model.addField(fm);
            }
        }

        // ── <component> elements (embedded objects) ─────────────────────
        // Flatten component properties into the parent schema with a prefix.
        NodeList componentNodes = getDirectChildren(classEl, "component");
        for (int i = 0; i < componentNodes.getLength(); i++) {
            Element compEl = (Element) componentNodes.item(i);
            String prefix = compEl.getAttribute("name");
            NodeList compProps = compEl.getElementsByTagName("property");
            for (int j = 0; j < compProps.getLength(); j++) {
                EntitySchemaModel.FieldModel fm = parsePropertyElement((Element) compProps.item(j));
                // Prefix the field name for clarity: companyAddress.address1
                fm.setName(prefix + "." + fm.getName());
                model.addField(fm);
            }
        }

        log.debug("Parsed HBM XML '" + resourceName + "' → entity '"
                + model.getEntityName() + "' with " + model.getFields().size() + " fields");
        return model;
    }

    private EntitySchemaModel.FieldModel parsePropertyElement(Element propEl) {
        String name = propEl.getAttribute("name");
        EntitySchemaModel.FieldModel fm = new EntitySchemaModel.FieldModel(name);

        // Type mapping
        String hbmType = propEl.getAttribute("type");
        if (hbmType != null && !hbmType.isEmpty()) {
            String[] mapping = TYPE_MAP.get(hbmType);
            if (mapping != null) {
                fm.setJsonSchemaType(mapping[0]);
                fm.setJsonSchemaFormat(mapping[1]);
            } else {
                // Unknown type — default to string
                fm.setJsonSchemaType("string");
                log.debug("Unknown HBM type '" + hbmType + "' for property '"
                        + name + "', defaulting to string");
            }
        } else {
            // No type attribute — check for nested <column> with sql-type
            fm.setJsonSchemaType("string");
        }

        // Nullability: check attribute on <property>, then on nested <column>
        String notNull = propEl.getAttribute("not-null");
        if ("true".equals(notNull)) {
            fm.setNullable(false);
        } else {
            // Check nested <column not-null="true">
            NodeList columns = propEl.getElementsByTagName("column");
            for (int i = 0; i < columns.getLength(); i++) {
                Element colEl = (Element) columns.item(i);
                if ("true".equals(colEl.getAttribute("not-null"))) {
                    fm.setNullable(false);
                    break;
                }
            }
        }

        // Length
        String length = propEl.getAttribute("length");
        if (length != null && !length.isEmpty() && "string".equals(fm.getJsonSchemaType())) {
            try {
                fm.setMaxLength(Integer.parseInt(length));
            } catch (NumberFormatException ignored) {
            }
        }

        return fm;
    }

    /** Get direct child elements with a specific tag name (not deep descendants). */
    private NodeList getDirectChildren(Element parent, String tagName) {
        // getElementsByTagName is recursive; we need direct children only.
        // Using a simple wrapper that filters.
        List<Element> result = new ArrayList<>();
        org.w3c.dom.Node child = parent.getFirstChild();
        while (child != null) {
            if (child instanceof Element && tagName.equals(((Element) child).getTagName())) {
                result.add((Element) child);
            }
            child = child.getNextSibling();
        }
        // Return as a simple NodeList wrapper
        return new NodeList() {
            @Override
            public org.w3c.dom.Node item(int index) { return result.get(index); }
            @Override
            public int getLength() { return result.size(); }
        };
    }

    /** Extract simple class name from a potentially fully-qualified name. */
    private String simpleClassName(String fqcn) {
        int dot = fqcn.lastIndexOf('.');
        return dot >= 0 ? fqcn.substring(dot + 1) : fqcn;
    }
}
