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

package org.temp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxbri.JaxbSchemaGenerator;
import org.apache.ws.commons.schema.XmlSchema;
import org.w3c.dom.Document;

public class JaxbSchemaGeneratorTest extends XMLSchemaTest {

    protected AxisService axisService;
    private JaxbSchemaGenerator generator;

    public JaxbSchemaGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(JaxbSchemaGenerator generator) {
        this.generator = generator;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        axisService = new AxisService();

    }

    @Override
    protected void tearDown() throws Exception {
        axisService = null;
        generator = null;
        super.tearDown();
    }

    public static class TestWebService {

    }

    private void testClass(Class<?> c) throws Exception {
        generator = getGenerator(c);
        generator.setAxisService(new AxisService());
        Collection<XmlSchema> schemaColl = generator.generateSchema();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Iterator<XmlSchema> iterator = schemaColl.iterator();
        int i = 0;
        for (XmlSchema xmlSchema : schemaColl) {
            i++;
            byteArrayOutputStream = new ByteArrayOutputStream();
            xmlSchema.write(byteArrayOutputStream);
            String XML1 = byteArrayOutputStream.toString();
            String XML2 = readSchema(c, i);
            assertSimilarXML(XML1, XML2);
            assertIdenticalXML(XML1, XML2);
        }

    }

    private void testClass(Class<?> c, String customSchemaLocation) throws Exception {
        generator = getGenerator(c);
        generator.setAxisService(new AxisService());
        generator.setCustomSchemaLocation(customSchemaLocation);
        Collection<XmlSchema> schemaColl = generator.generateSchema();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        for (XmlSchema xmlSchema : schemaColl) {
            i++;
            byteArrayOutputStream = new ByteArrayOutputStream();
            xmlSchema.write(byteArrayOutputStream);
            String XML1 = byteArrayOutputStream.toString();
            String XML2 = readSchemaWithCustomSchema(c, i);
            assertSimilarXML(XML1, XML2);
            assertIdenticalXML(XML1, XML2);
        }

    }

    private void testClassWithMapping(Class<?> c, String mappingLocation) throws Exception {
        generator = getGenerator(c);
        generator.setAxisService(new AxisService());
        generator.setMappingFileLocation(mappingLocation);
        Collection<XmlSchema> schemaColl = generator.generateSchema();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 0;
        for (XmlSchema xmlSchema : schemaColl) {
            i++;
            byteArrayOutputStream = new ByteArrayOutputStream();
            xmlSchema.write(byteArrayOutputStream);
            String XML1 = byteArrayOutputStream.toString();
            String XML2 = readSchemaWithMappingSchema(c, i);
            XML1 = prepareForMapping(XML1);
            assertSimilarXML(XML1, XML2);
            assertIdenticalXML(XML1, XML2);
        }

    }

    private String prepareForMapping(String XML1) {
        XML1 = XML1.replaceAll(XMLSchemaNameSpace, "#temporory_replacement1#");
        XML1 = XML1.replaceAll(
                "targetNamespace=\"http://java2wsdl.description.axis2.apache.org/xsd\"",
                "#temporory_replacement2#");
        XML1 = XML1.replaceAll("xmlns:.*=\"http://java2wsdl.description.axis2.apache.org/xsd\"",
                "xmlns:mapping=\"http://java2wsdl.description.axis2.apache.org/xsd\"");
        XML1 = XML1.replaceAll("xmlns:.*=\"http://www.abc.com/soaframework/common/types\"",
                "xmlns:mapping2=\"http://www.abc.com/soaframework/common/types\"");
        XML1 = XML1.replaceAll("#temporory_replacement1#", XMLSchemaNameSpace);
        XML1 = XML1
                .replaceAll(
                        "<xs:element name=\"MappingTestResult\" nillable=\"true\" type=\".*:ErrorMessage\"/>",
                        "<xs:element name=\"MappingTestResult\" nillable=\"true\" type=\"mapping:ErrorMessage\"/>");
        XML1 = XML1
                .replaceAll(
                        "<xs:element minOccurs=\"0\" name=\"return\" nillable=\"true\" type=\".*:ErrorMessage\"/>",
                        "<xs:element minOccurs=\"0\" name=\"return\" nillable=\"true\" type=\"mapping:ErrorMessage\"/>");
        XML1 = XML1.replaceAll("#temporory_replacement2#",
                "targetNamespace=\"http://java2wsdl.description.axis2.apache.org/xsd\"");
        return XML1;
    }

    public JaxbSchemaGenerator getGenerator(Class<?> c) throws Exception {
        return new JaxbSchemaGenerator(getClass().getClassLoader(), c.getName(),
                "http://example.org", "ex");
    }

    public String readSchema(Class<?> c, int i) throws Exception {
        return readFile("src" + File.separator + "test" + File.separator + "schemas" + File.separator
                + "default_generator" + File.separator + c.getSimpleName() + "-" + i + ".xml");
    }

    public String readSchemaWithCustomSchema(Class<?> c, int i) throws Exception {
        return readFile("src" + File.separator + "test" + File.separator + "schemas" + File.separator
                + "default_generator" + File.separator + c.getSimpleName() + "with_custom_schema-"
                + i + ".xml");
    }

    public String readSchemaWithMappingSchema(Class<?> c, int i) throws Exception {
        return readFile("src" + File.separator + "test" + File.separator + "schemas" + File.separator
                + "default_generator" + File.separator + c.getSimpleName() + "with_custom_mapping-"
                + i + ".xml");
    }

    public void testDOMClass(Class<?> c) throws Exception {
        generator = getGenerator(c);
        generator.setAxisService(new AxisService());
        Collection<XmlSchema> schemaColl = generator.generateSchema();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Iterator<XmlSchema> iterator = schemaColl.iterator();
        int i = 0;
        for (XmlSchema xmlSchema : schemaColl) {
            i++;
            byteArrayOutputStream = new ByteArrayOutputStream();
            xmlSchema.write(byteArrayOutputStream);
            String XML1 = byteArrayOutputStream.toString();
            XML1 = prepareForDOM(XML1);
            String XML2 = readSchema(c, i);
            assertSimilarXML(XML1, XML2);
            assertIdenticalXML(XML1, XML2);
        }

    }

    private String prepareForDOM(String XML1) {
        XML1 = XML1.replaceAll(XMLSchemaNameSpace, "#temporory_replacement1#");
        XML1 = XML1.replaceAll("targetNamespace=\"http://java2wsdl.description.axis2.apache.org\"",
                "#temporory_replacement2#");
        XML1 = XML1.replaceAll("xmlns:.*=\"http://java2wsdl.description.axis2.apache.org\"",
                "xmlns:DOM=\"http://java2wsdl.description.axis2.apache.org\"");
        XML1 = XML1.replaceAll("#temporory_replacement1#", XMLSchemaNameSpace);
        XML1 = XML1.replaceAll("#temporory_replacement2#",
                "targetNamespace=\"http://java2wsdl.description.axis2.apache.org\"");
        return XML1;
    }

  
    // to test classes which involves enums
    public void testEnumClass(Class<?> c) throws Exception {
        generator = getGenerator(c);
        generator.setAxisService(new AxisService());
        Collection<XmlSchema> schemaColl = generator.generateSchema();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Iterator<XmlSchema> iterator = schemaColl.iterator();
        int i = 0;
        for (XmlSchema xmlSchema : schemaColl) {
            i++;
            // /XmlSchema schema = schemaColl.iterator().next();
            byteArrayOutputStream = new ByteArrayOutputStream();
            xmlSchema.write(byteArrayOutputStream);
            String XML1 = byteArrayOutputStream.toString();

            // Enum has differences when generating schema files
            if (XML1.contains("\"http://ws.apache.org/namespaces/axis2/enum\"")) {
                XML1 = prepareForEnum(XML1);
            }
            XML1 = prepareForEnum(XML1);
            String XML2 = readSchema(c, i);
            assertSimilarXML(XML1, XML2);
            assertIdenticalXML(XML1, XML2);
        }
    }

    public String prepareForEnum(String XML1) {
        XML1 = XML1.replaceAll(XMLSchemaNameSpace, "#temporory_replacement1#");
        XML1 = XML1.replaceAll("targetNamespace=\"http://ws.apache.org/namespaces/axis2/enum\"",
                "#temporory_replacement2#");
        XML1 = XML1.replaceAll("xmlns:.*=\"http://ws.apache.org/namespaces/axis2/enum\"",
                "xmlns:enum=\"http://ws.apache.org/namespaces/axis2/enum\"");

        XML1 = XML1.replaceAll("#temporory_replacement1#", XMLSchemaNameSpace);
        XML1 = XML1.replaceAll("#temporory_replacement2#",
                "targetNamespace=\"http://ws.apache.org/namespaces/axis2/enum\"");
        XML1 = XML1
                .replaceAll(
                        "<xs:element minOccurs=\"0\" name=\"e\" nillable=\"true\" type=\".*:SimpleEnum\"/>",
                        "<xs:element minOccurs=\"0\" name=\"e\" nillable=\"true\" type=\"enum:SimpleEnum\"/>");

        XML1 = XML1
                .replaceAll(
                        "<xs:element minOccurs=\"0\" name=\"return\" nillable=\"true\" type=\".*:SimpleEnum\"/>",
                        "<xs:element minOccurs=\"0\" name=\"return\" nillable=\"true\" type=\"enum:SimpleEnum\"/>");
        return XML1;
    }

    public String readFile(String fileName) throws Exception {
       return readXMLfromSchemaFile(fileName);
    }

    public void testNoParameter() throws Exception {
        testClass(NoParameterOrReturnType.class);
    }

    public void testNoParameterWithDefaultShchema() throws Exception {
        testClass(NoParameterOrReturnType.class, CustomSchemaLocation);

    }

    public void testParametersWithDefaultSchema() throws Exception {
        testClass(PrimitivesAsParameters.class, CustomSchemaLocation);
    }

    public void testWithMappingSchema() throws Exception {
        testClassWithMapping(MappingCheck.class, MappingFileLocation);
    }

    public void testPrimitivesAsParameters() throws Exception {
        testClass(PrimitivesAsParameters.class);
    }

    public void testCollectionsAsParameters() throws Exception {
        testClass(ColectionAsParameter.class);
    }

    public void testPrimitiveArrraysAsParaameters() throws Exception {
        testClass(PrimitiveArraysAsParametrs.class);
    }

    public void TestStringAsReturnType() throws Exception {
        testClass(StringAsReturnType.class);
    }

    public void testIntAsReturnType() throws Exception {
        testClass(intAsReturnType.class);
    }

    public void testDoubleAsReturnType() throws Exception {
        testClass(doubleAsReturnType.class);
    }

    public void testCharAsReturnType() throws Exception {
        testClass(charAsReturnType.class);
    }

    public void testRunTimeException() throws Exception {
        testClass(RunTimeExceptionCheck.class);
    }

    public void testIntArrayAsReturnType() throws Exception {
        testClass(IntArrayAsReturnType.class);
    }

    public void testDoubleArrayAsReturnType() throws Exception {
        testClass(DoubleArrayAsReturnType.class);
    }

    public void testCharArrayAsReturnType() throws Exception {
        testClass(CharArrayAsReturnType.class);
    }

    public void testEnumAsParameter() throws Exception {
        testEnumClass(EnumAsParameter.class);
    }

    public void testEnumAsReturnTYpe() throws Exception {
        testEnumClass(EnumAsReturnType.class);
    }

    public void testDOMAsParameter() throws Exception {
        testDOMClass(DOMasParameter.class);
    }

    public void testDOMAsReturnType() throws Exception {
        testDOMClass(DomAsReturnType.class);
    }

    public void testListAsParameter() throws Exception {
        testClass(ListAsParameter.class);
    }

    public void testListAsReturnType() throws Exception {
        testClass(ListAsReturnType.class);
    }
    
   

    class NoParameterOrReturnType {
        public void testNoParamiters() {

        }
    }

    class StringAsParameter {
        public void testStringParameter(String s) {

        }
    }

    class PrimitivesAsParameters {
        public void testPrimitiveTypes(byte b, short s, int i, long l, float f, double d,
                boolean isTrue, char c) {

        }
    }

    class ColectionAsParameter {
        public void testCollecttionAsPParameter(List<Object> l) {

        }
    }

    class PrimitiveArraysAsParametrs {
        public void testPrimitiveTypes(byte[] b, short[] s, int[] i, long[] l, float[] f,
                double[] d, boolean[] isTrue, char[] c) {

        }
    }

    class MapAsParameter {
        public void testMapAsParameter(Map<Object, Object> map) {

        }
    }

    class StringAsReturnType {
        public String testStringReturnType() {
            return null;
        }
    }

    class CollectionAsReturnType {
        public Collection<Object> TestCollectionReturnType() {
            return null;
        }
    }

    class ListAsParameter {
        public void testList(List l) {

        }
    }

    class ListAsReturnType {
        public List testListAsReturnType() {
            return null;
        }
    }

    class MapAsReturnType {
        public Map<Object, Object> testMapAsReturnType() {
            return null;
        }
    }

    class intAsReturnType {
        public int testIntAsResult() {
            return -1;
        }
    }

    class doubleAsReturnType {
        public double testDoubleAsResult() {
            return 1.84984398;

        }
    }

    class charAsReturnType {
        public char testCharAsResult() {
            return 'c';
        }
    }

    class RunTimeExceptionCheck {
        public void testRunTImeException() throws RuntimeException {
            throw new RuntimeErrorException(null, "message");
        }
    }

    class IntArrayAsReturnType {
        public int[] testIntArrayAsReturnType() {
            return null;
        }

    }

    class CharArrayAsReturnType {
        public char[] testCharArrayAsReturnType() {
            return null;
        }
    }

    class DoubleArrayAsReturnType {
        public double[] testDoubleArrayAsReturnType() {
            return null;
        }
    }

    class EnumAsParameter {
        public void testEnumAsParameter(SimpleEnum e) {

        }
    }

    class EnumAsReturnType {
        public SimpleEnum testEnumAsReturnType() {
            return null;
        }
    }

    class DOMasParameter {
        public void testDOM(Document document) {

        }
    }

    class DomAsReturnType {
        public Document testDOM() {
            return null;
        }
    }

    class MappingCheck {
        public DummyClass MappingTest() {
            return null;
        }
    }

    public enum SimpleEnum {
        WHITE, BLACK, RED, YELLOW, BLUE;
    }
}
