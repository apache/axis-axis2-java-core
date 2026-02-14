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

package org.apache.axis2.schema;

import junit.framework.TestCase;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Test for AXIS2-5972: Verifies that xs:attribute ref= preserves the namespace
 * from the ref QName in the generated metainfo.
 */
public class SchemaCompilerRefAttributeTest extends TestCase {

    private static final String XMLMIME_NS = "http://www.w3.org/2005/05/xmlmime";

    /**
     * Compiles xmlmime.xsd and verifies that the contentType attribute on the
     * base64Binary type is registered with the correct namespace URI from the
     * ref QName, not with an empty namespace.
     */
    public void testRefAttributeNamespacePreserved() throws Exception {
        // Load the xmlmime.xsd schema
        String basedir = System.getProperty("basedir", ".");
        File schemaFile = new File(basedir + "/test-resources/std/xmlmime.xsd");
        assertTrue("xmlmime.xsd not found at " + schemaFile.getAbsolutePath(), schemaFile.exists());

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(schemaFile);

        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(doc, null);

        // Compile the schema with generateAll=true since xmlmime.xsd has
        // only types (no elements), and types are only compiled when generateAll is set
        CompilerOptions compilerOptions = new CompilerOptions();
        compilerOptions.setGenerateAll(true);
        SchemaCompiler compiler = new SchemaCompiler(compilerOptions);
        compiler.compile(schema);

        // Access the processedTypeMetaInfoMap via reflection
        Field metaInfoMapField = SchemaCompiler.class.getDeclaredField("processedTypeMetaInfoMap");
        metaInfoMapField.setAccessible(true);
        @SuppressWarnings("unchecked")
        HashMap<QName, BeanWriterMetaInfoHolder> metaInfoMap =
                (HashMap<QName, BeanWriterMetaInfoHolder>) metaInfoMapField.get(compiler);

        // Check the base64Binary type's metainfo
        QName base64BinaryQName = new QName(XMLMIME_NS, "base64Binary");
        BeanWriterMetaInfoHolder metainf = metaInfoMap.get(base64BinaryQName);
        assertNotNull("MetaInfo for base64Binary type should exist", metainf);

        // The contentType attribute should be registered with the xmlmime namespace
        QName expectedAttrQName = new QName(XMLMIME_NS, "contentType");
        QName wrongAttrQName = new QName("", "contentType");

        // Verify the attribute is registered with the correct namespace
        String javaClass = metainf.getClassNameForQName(expectedAttrQName);
        assertNotNull(
                "contentType attribute should be registered with namespace " + XMLMIME_NS +
                        " (AXIS2-5972: ref attribute namespace must be preserved)",
                javaClass);

        // Verify the attribute is NOT registered with an empty namespace
        String wrongJavaClass = metainf.getClassNameForQName(wrongAttrQName);
        assertNull(
                "contentType attribute should NOT be registered with empty namespace",
                wrongJavaClass);

        // Verify it's flagged as an attribute type
        assertTrue(
                "contentType should be flagged as an attribute",
                metainf.getAttributeStatusForQName(expectedAttrQName));

        // Also check hexBinary type
        QName hexBinaryQName = new QName(XMLMIME_NS, "hexBinary");
        BeanWriterMetaInfoHolder hexMetainf = metaInfoMap.get(hexBinaryQName);
        assertNotNull("MetaInfo for hexBinary type should exist", hexMetainf);

        String hexJavaClass = hexMetainf.getClassNameForQName(expectedAttrQName);
        assertNotNull(
                "contentType attribute on hexBinary should also have correct namespace",
                hexJavaClass);
    }
}
