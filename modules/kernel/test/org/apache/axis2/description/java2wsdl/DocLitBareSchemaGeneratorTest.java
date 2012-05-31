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

package org.apache.axis2.description.java2wsdl;

import java.io.File;

import org.apache.axis2.description.AxisService;

public class DocLitBareSchemaGeneratorTest extends DefaultSchemaGeneratorTest {

    @Override
    protected void setUp() throws Exception {
        axisService = new AxisService();

    }

    @Override
    protected void tearDown() throws Exception {
        this.setGenerator(null);
        super.tearDown();
    }

    @Override
    public String readSchema(Class<?> c, int i) throws Exception {
        return readFile("test-resources" + File.separator + "schemas"
                + File.separator + "doc_lit_generator" + File.separator
                + c.getSimpleName() + "-" + i + ".xml");
    }

    @Override
    public String readSchemaWithCustomSchema(Class<?> c, int i)
            throws Exception {
        return readFile("test-resources" + File.separator + "schemas"
                + File.separator + "doc_lit_generator" + File.separator
                + c.getSimpleName() + "with_custom_schema" + "-" + i + ".xml");
    }

    @Override
    public String readSchemaWithMappingSchema(Class<?> c, int i)
            throws Exception {
        return readFile("test-resources" + File.separator + "schemas"
                + File.separator + "doc_lit_generator" + File.separator
                + c.getSimpleName() + "with_custom_mapping" + "-" + i + ".xml");
    }

    @Override
    public DefaultSchemaGenerator getGenerator(Class<?> c) throws Exception {
        return new DocLitBareSchemaGenerator(getClass().getClassLoader(),
                c.getName(), "http://example.org", "ex", axisService);
    }

    @Override
    public String prepareForMap(String XML1) {
        String XML = super.prepareForMap(XML1);
        XML = XML
                .replaceAll(
                        "<xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"entry\" nillable=\"true\" type=\".*:entry1\"/>",
                        "<xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"entry\" nillable=\"true\" type=\"map:entry1\"/>");
        XML = XML
                .replaceAll(
                        "<xs:element name=\"map\" nillable=\"true\" type=\".*:map1\"/>",
                        "<xs:element name=\"map\" nillable=\"true\" type=\"map:map1\"/>");

        XML = XML
                .replaceAll(
                        "<xs:element name=\"testMapAsReturnTypeResult\" nillable=\"true\" type=\".*:map1\"/>",
                        "<xs:element name=\"testMapAsReturnTypeResult\" nillable=\"true\" type=\"map:map1\"/>");

        return XML;

    }

    @Override
    public String prepareForPOJO(String XML1) {
        String XML = super.prepareForPOJO(XML1);
        XML = XML
                .replaceAll(
                        "<xs:element name=\"o\" nillable=\"true\" type=\".*:DefaultSchemaGeneratorTest_SimplePOJO\"/>",
                        "<xs:element name=\"o\" nillable=\"true\" type=\"pojo:DefaultSchemaGeneratorTest_SimplePOJO\"/>");
        XML = XML
                .replaceAll(
                        "<xs:element name=\"testPOJOAsReturnTypeResult\" nillable=\"true\" type=\".*:DefaultSchemaGeneratorTest_SimplePOJO\"/>",
                        "<xs:element name=\"testPOJOAsReturnTypeResult\" nillable=\"true\" type=\"pojo:DefaultSchemaGeneratorTest_SimplePOJO\"/>");
        return XML;
    }

    @Override
    public String prepareForEnum(String XML1) {
        XML1 = XML1.replaceAll("xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"",
                "#temporory_replacement1#");
        XML1 = XML1
                .replaceAll(
                        "targetNamespace=\"http://java2wsdl.description.axis2.apache.org/xsd\"",
                        "#temporory_replacement2#");
        XML1 = XML1
                .replaceAll(
                        "xmlns:.*=\"http://java2wsdl.description.axis2.apache.org/xsd\"",
                        "xmlns:enum=\"http://java2wsdl.description.axis2.apache.org/xsd\"");
        XML1 = XML1
                .replaceAll(
                        "<xs:element name=\"e\" nillable=\"true\" type=\".*:DefaultSchemaGeneratorTest_SimpleEnum\"/>",
                        "<xs:element name=\"e\" nillable=\"true\" type=\"enum:DefaultSchemaGeneratorTest_SimpleEnum\"/>");
        XML1 = XML1
                .replaceAll(
                        "<xs:element name=\"testEnumAsReturnTypeResult\" nillable=\"true\" type=\".*:DefaultSchemaGeneratorTest_SimpleEnum\"/>",
                        "<xs:element name=\"testEnumAsReturnTypeResult\" nillable=\"true\" type=\"enum:DefaultSchemaGeneratorTest_SimpleEnum\"/>");
        XML1 = XML1.replaceAll("#temporory_replacement1#",
                "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"");
        XML1 = XML1
                .replaceAll("#temporory_replacement2#",
                        "targetNamespace=\"http://java2wsdl.description.axis2.apache.org/xsd\"");
        return XML1;
    }

    @Override
    public void testGeneratesExtraClass() throws Exception {
        // No need to run these inherited methods

    }

    @Override
    public void testGenerateSchema() throws Exception {
        // No need to run these inherited methods

    }

}
