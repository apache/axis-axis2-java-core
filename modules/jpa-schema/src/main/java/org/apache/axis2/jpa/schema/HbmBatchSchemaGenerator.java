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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Batch generator that scans a directory of Hibernate XML mapping files
 * ({@code *.hbm.xml}) and produces a single JSON file containing OpenAPI
 * 3.0 component schemas for every entity found.
 *
 * <p>Designed to run as a standalone build tool — invoked from Ant, Maven,
 * Gradle, or the command line. The output file is a self-contained OpenAPI
 * {@code components/schemas} fragment that can be merged into an OpenAPI
 * specification or served directly by an Axis2 service.
 *
 * <h3>Command-line usage</h3>
 * <pre>{@code
 * java -cp axis2-jpa-schema.jar:jackson-*.jar \
 *   org.apache.axis2.jpa.schema.HbmBatchSchemaGenerator \
 *   src/main/resources \
 *   build/openapi-schemas.json
 * }</pre>
 *
 * <h3>Ant integration</h3>
 * <pre>{@code
 * <target name="openapi-schema"
 *         description="Generate OpenAPI schemas from HBM XML mappings">
 *     <java classname="org.apache.axis2.jpa.schema.HbmBatchSchemaGenerator"
 *           fork="true" failonerror="true">
 *         <classpath>
 *             <fileset dir="lib" includes="axis2-jpa-schema*.jar,jackson-*.jar,commons-logging*.jar"/>
 *         </classpath>
 *         <arg value="src/main/resources"/>
 *         <arg value="build/openapi-schemas.json"/>
 *     </java>
 * </target>
 * }</pre>
 *
 * <h3>Output format</h3>
 * <pre>{@code
 * {
 *   "openapi": "3.0.1",
 *   "info": {
 *     "title": "Generated from 146 HBM XML mappings",
 *     "version": "1.0.0"
 *   },
 *   "components": {
 *     "schemas": {
 *       "CompanyBO": { "type": "object", "properties": { ... } },
 *       "CompanyBOWrite": { "type": "object", "properties": { ... } },
 *       "DepartmentBO": { ... },
 *       "DepartmentBOWrite": { ... },
 *       ...
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>Each entity produces two schemas: a read schema (all fields, IDs
 * marked {@code readOnly}) and a write schema (excludes
 * {@code @GeneratedValue} IDs, {@code @Version} fields, and any fields
 * annotated with custom write-exclude annotations).
 *
 * <h3>Exit codes</h3>
 * <ul>
 *   <li>0 — success</li>
 *   <li>1 — invalid arguments</li>
 *   <li>2 — input directory does not exist or contains no HBM files</li>
 *   <li>3 — I/O error writing output</li>
 * </ul>
 */
public class HbmBatchSchemaGenerator {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: HbmBatchSchemaGenerator <hbm-dir> <output-json>");
            System.err.println();
            System.err.println("  hbm-dir       Directory containing *.hbm.xml files");
            System.err.println("  output-json   Output file path (e.g., openapi-schemas.json)");
            System.err.println();
            System.err.println("Example:");
            System.err.println("  java -cp ... org.apache.axis2.jpa.schema.HbmBatchSchemaGenerator \\");
            System.err.println("    src/main/resources build/openapi-schemas.json");
            System.exit(1);
        }

        File inputDir = new File(args[0]);
        File outputFile = new File(args[1]);

        if (!inputDir.isDirectory()) {
            System.err.println("ERROR: Not a directory: " + inputDir.getAbsolutePath());
            System.exit(2);
        }

        File[] hbmFiles = inputDir.listFiles((dir, name) ->
                name.endsWith(".hbm.xml"));
        if (hbmFiles == null || hbmFiles.length == 0) {
            System.err.println("ERROR: No *.hbm.xml files found in: " + inputDir.getAbsolutePath());
            System.exit(2);
        }

        // Sort for deterministic output ordering
        Arrays.sort(hbmFiles);

        HbmXmlIntrospector introspector = new HbmXmlIntrospector();
        Map<String, ObjectNode> allSchemas = new LinkedHashMap<>();

        int successCount = 0;
        int errorCount = 0;

        for (File hbmFile : hbmFiles) {
            try (InputStream is = new FileInputStream(hbmFile)) {
                EntitySchemaModel model = introspector.introspect(is, hbmFile.getName());
                if (model == null) {
                    System.err.println("  SKIP " + hbmFile.getName() + " (no entity found)");
                    continue;
                }

                Map<String, ObjectNode> schemas = JpaSchemaGenerator.generateBothSchemas(model);
                allSchemas.putAll(schemas);
                successCount++;

                int fieldCount = model.getFields().size();
                int relCount = (int) model.getFields().values().stream()
                        .filter(f -> f.getRefEntityName() != null).count();
                System.out.println("  OK   " + hbmFile.getName()
                        + " → " + model.getEntityName()
                        + " (" + fieldCount + " fields, " + relCount + " relationships)");

            } catch (IOException e) {
                System.err.println("  FAIL " + hbmFile.getName() + " (I/O): " + e.getMessage());
                errorCount++;
            } catch (RuntimeException e) {
                System.err.println("  FAIL " + hbmFile.getName() + " (unexpected):");
                e.printStackTrace(System.err);
                errorCount++;
            }
        }

        if (allSchemas.isEmpty()) {
            System.err.println("ERROR: No schemas generated from " + hbmFiles.length + " files");
            System.exit(2);
        }

        // Build the OpenAPI 3.0 document structure
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        ObjectNode root = mapper.createObjectNode();
        root.put("openapi", "3.0.1");

        ObjectNode info = root.putObject("info");
        info.put("title", "Generated from " + successCount + " HBM XML mappings");
        info.put("version", "1.0.0");
        info.put("description",
                "Auto-generated OpenAPI component schemas from Hibernate XML mappings. "
                + "Each entity has a read schema (all fields) and a write schema "
                + "(excludes server-managed fields like generated IDs, version, "
                + "and audit timestamps).");

        ObjectNode components = root.putObject("components");
        ObjectNode schemas = components.putObject("schemas");
        for (Map.Entry<String, ObjectNode> entry : allSchemas.entrySet()) {
            schemas.set(entry.getKey(), entry.getValue());
        }

        // Write output
        try {
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(mapper.writeValueAsString(root));
            }
        } catch (IOException e) {
            System.err.println("ERROR: Failed to write output: " + e.getMessage());
            System.exit(3);
        }

        System.out.println();
        System.out.println("Generated " + allSchemas.size() + " schemas"
                + " (" + successCount + " entities × 2 [read + write])"
                + " from " + hbmFiles.length + " HBM files");
        if (errorCount > 0) {
            System.out.println("WARNING: " + errorCount + " files had errors");
        }
        System.out.println("Output: " + outputFile.getAbsolutePath());
    }
}
