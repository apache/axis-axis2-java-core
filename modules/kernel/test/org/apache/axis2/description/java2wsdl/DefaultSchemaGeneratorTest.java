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

import org.apache.axis2.description.AxisService;
import org.apache.ws.commons.schema.XmlSchema;
import org.w3c.dom.Document;

import javax.management.RuntimeErrorException;
import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DefaultSchemaGeneratorTest extends XMLSchemaTest {

    protected AxisService axisService;
    private DefaultSchemaGenerator generator;

    public DefaultSchemaGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(DefaultSchemaGenerator generator) {
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
    
    public void testGeneratesExtraClass() throws Exception {

        generator = new DefaultSchemaGenerator(getClass().getClassLoader(),
                TestWebService.class.getName(), "http://example.org", "ex",
                axisService);
        ArrayList<String> extraClasses = new ArrayList<String>();
        extraClasses.add(ExtraClass.class.getName());
        generator.setExtraClasses(extraClasses);
        
        Collection<XmlSchema> schemaColl = generator.generateSchema();
        assertEquals(1, schemaColl.size());
        XmlSchema schema = schemaColl.iterator().next();

        boolean foundExtra = false;
        for (QName name : schema.getSchemaTypes().keySet()) {
            if (name.getLocalPart().equals("ExtraClass"))
                foundExtra = true;
        }
        assertTrue(foundExtra);
    }

    public void testGenerateSchema() throws Exception {
        AxisService axisService = new AxisService();
        generator = new DefaultSchemaGenerator(getClass().getClassLoader(),
                TestWebService.class.getName(), "http://example.org", "ex",
                axisService);
        Collection<XmlSchema> schemas = generator.generateSchema();
        for (int i = 0; i < 50; i++) {
            System.out.println(schemas.size());
        }
        assertNotNull(schemas);
    }

    private void testClass(Class<?> c) throws Exception {
        generator = getGenerator(c);
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

            assertSimilarXML(XML2, XML1);
        }

    }

    private void testClass(Class<?> c, String customSchemaLocation)
            throws Exception {
        generator = getGenerator(c);
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
            assertSimilarXML(XML2, XML1);
        }

    }

    private void testClassWithMapping(Class<?> c, String mappingLocation)
            throws Exception {
        generator = getGenerator(c);
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
            assertSimilarXML(XML2, XML1);
        }

    }

    private String prepareForMapping(String XML1) {
        XML1 = XML1.replaceAll(XMLSchemaNameSpace, "#temporory_replacement1#");
        XML1 = XML1
                .replaceAll(
                        "targetNamespace=\"http://java2wsdl.description.axis2.apache.org/xsd\"",
                        "#temporory_replacement2#");
        XML1 = XML1
                .replaceAll(
                        "xmlns:.*=\"http://java2wsdl.description.axis2.apache.org/xsd\"",
                        "xmlns:mapping=\"http://java2wsdl.description.axis2.apache.org/xsd\"");
        XML1 = XML1
                .replaceAll(
                        "xmlns:.*=\"http://www.abc.com/soaframework/common/types\"",
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
        XML1 = XML1
                .replaceAll("#temporory_replacement2#",
                        "targetNamespace=\"http://java2wsdl.description.axis2.apache.org/xsd\"");
        return XML1;
    }

    public DefaultSchemaGenerator getGenerator(Class<?> c) throws Exception {
        return new DefaultSchemaGenerator(getClass().getClassLoader(),
                c.getName(), "http://example.org", "ex", axisService);
    }

    public String readSchema(Class<?> c, int i) throws Exception {
        return readFile("test-resources" + File.separator + "schemas"
                + File.separator + "default_generator" + File.separator
                + c.getSimpleName() + "-" + i + ".xml");
    }

    public String readSchemaWithCustomSchema(Class<?> c, int i)
            throws Exception {
        return readFile("test-resources" + File.separator + "schemas"
                + File.separator + "default_generator" + File.separator
                + c.getSimpleName() + "with_custom_schema-" + i + ".xml");
    }

    public String readSchemaWithMappingSchema(Class<?> c, int i)
            throws Exception {
        return readFile("test-resources" + File.separator + "schemas"
                + File.separator + "default_generator" + File.separator
                + c.getSimpleName() + "with_custom_mapping-" + i + ".xml");
    }

    public void testDOMClass(Class<?> c) throws Exception {
        generator = getGenerator(c);
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
            assertSimilarXML(XML2, XML1);
        }

    }

    private String prepareForDOM(String XML1) {
        XML1 = XML1.replaceAll(XMLSchemaNameSpace, "#temporory_replacement1#");
        XML1 = XML1
                .replaceAll(
                        "targetNamespace=\"http://java2wsdl.description.axis2.apache.org\"",
                        "#temporory_replacement2#");
        XML1 = XML1.replaceAll(
                "xmlns:.*=\"http://java2wsdl.description.axis2.apache.org\"",
                "xmlns:DOM=\"http://java2wsdl.description.axis2.apache.org\"");
        XML1 = XML1.replaceAll("#temporory_replacement1#", XMLSchemaNameSpace);
        XML1 = XML1
                .replaceAll("#temporory_replacement2#",
                        "targetNamespace=\"http://java2wsdl.description.axis2.apache.org\"");
        return XML1;
    }

    // to test classes which involves java.util.map
    public void testMapClass(Class<?> c) throws Exception {
        generator = getGenerator(c);
        Collection<XmlSchema> schemaColl = generator.generateSchema();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Iterator<XmlSchema> iterator = schemaColl.iterator();
        int i = 0;
        for (XmlSchema xmlSchema : schemaColl) {
            i++;
            byteArrayOutputStream = new ByteArrayOutputStream();
            xmlSchema.write(byteArrayOutputStream);
            String XML1 = byteArrayOutputStream.toString();
            if (XML1.contains("\"http://ws.apache.org/namespaces/axis2/map\"")) {
                XML1 = prepareForMap(XML1);
            }
            String XML2 = readSchema(c, i);
            assertSimilarXML(XML2, XML1);
        }

    }

    public String prepareForMap(String XML1) {
        XML1 = XML1.replaceAll(XMLSchemaNameSpace, "#temporory_replacement1#");
        XML1 = XML1
                .replaceAll(
                        "targetNamespace=\"http://ws.apache.org/namespaces/axis2/map\"",
                        "#temporory_replacement2#");
        XML1 = XML1.replaceAll(
                "xmlns:.*=\"http://ws.apache.org/namespaces/axis2/map\"",
                "xmlns:map=\"http://ws.apache.org/namespaces/axis2/map\"");
        XML1 = XML1
                .replaceAll(
                        " <xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"entry\" nillable=\"true\" type=\".*:entry1\"/>",
                        " <xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"entry\" nillable=\"true\" type=\"map:entry1\"/>");
        XML1 = XML1.replaceAll("#temporory_replacement1#", XMLSchemaNameSpace);
        XML1 = XML1
                .replaceAll("#temporory_replacement2#",
                        "targetNamespace=\"http://ws.apache.org/namespaces/axis2/map\"");
        XML1 = XML1
                .replaceAll(
                        "<xs:element minOccurs=\"0\" name=\"map\" nillable=\"true\" type=\".*:map1\"/>",
                        "<xs:element minOccurs=\"0\" name=\"map\" nillable=\"true\" type=\"map:map1\"/>");
        XML1 = XML1
                .replaceAll(
                        "<xs:element minOccurs=\"0\" name=\"return\" nillable=\"true\" type=\".*:map1\"/>",
                        "<xs:element minOccurs=\"0\" name=\"return\" nillable=\"true\" type=\"map:map1\"/>");
        return XML1;
    }

    // to test classes which involves POJO objects
    public void testPOJOClass(Class<?> c) throws Exception {
        generator = getGenerator(c);
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

            // map has differences when generating schema files
            if (XML1.contains("http://java2wsdl.description.axis2.apache.org/xsd")) {
                XML1 = prepareForPOJO(XML1);
            }
            String XML2 = readSchema(c, i);
            assertSimilarXML(XML2, XML1);
        }

    }

    public String prepareForPOJO(String XML1) {
        XML1 = XML1.replaceAll(XMLSchemaNameSpace, "#temporory_replacement1#");
        XML1 = XML1
                .replaceAll(
                        "targetNamespace=\"http://java2wsdl.description.axis2.apache.org/xsd\"",
                        "#temporory_replacement2#");
        XML1 = XML1.replaceAll(
                "type=\".*:DefaultSchemaGeneratorTest_SimplePOJO\"",
                "type=\"pojo:DefaultSchemaGeneratorTest_SimplePOJO\"");
        XML1 = XML1
                .replaceAll(
                        "xmlns:.*=\"http://java2wsdl.description.axis2.apache.org/xsd\"",
                        "xmlns:pojo=\"http://java2wsdl.description.axis2.apache.org/xsd\"");
        XML1 = XML1.replaceAll("#temporory_replacement1#", XMLSchemaNameSpace);
        XML1 = XML1
                .replaceAll("#temporory_replacement2#",
                        "targetNamespace=\"http://java2wsdl.description.axis2.apache.org/xsd\"");
        return XML1;
    }

    // to test classes which involves Custom Exceptions
    public void testExcClass(Class<?> c) throws Exception {
        generator = getGenerator(c);
        Collection<XmlSchema> schemaColl = generator.generateSchema();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Iterator<XmlSchema> iterator = schemaColl.iterator();
        int i = 0;
        for (XmlSchema xmlSchema : schemaColl) {
            i++;
            byteArrayOutputStream = new ByteArrayOutputStream();
            xmlSchema.write(byteArrayOutputStream);
            String XML1 = byteArrayOutputStream.toString();

            if (XML1.contains("http://java2wsdl.description.axis2.apache.org/xsd")) {
                XML1 = prepareForExc(XML1);
            }
            String XML2 = readSchema(c, i);
            assertSimilarXML(XML2, XML1);
        }

    }

    public String prepareForExc(String XML1) {
        XML1 = XML1.replaceAll(XMLSchemaNameSpace, "#temporory_replacement1#");
        XML1 = XML1
                .replaceAll(
                        "targetNamespace=\"http://java2wsdl.description.axis2.apache.org/xsd\"",
                        "#temporory_replacement2#");
        XML1 = XML1.replaceAll(
                "type=\".*:DefaultSchemaGeneratorTest_CustomException\"",
                "type=\"exc:DefaultSchemaGeneratorTest_CustomException\"");
        XML1 = XML1
                .replaceAll(
                        "xmlns:.*=\"http://java2wsdl.description.axis2.apache.org/xsd\"",
                        "xmlns:exc=\"http://java2wsdl.description.axis2.apache.org/xsd\"");
        XML1 = XML1.replaceAll("#temporory_replacement1#", XMLSchemaNameSpace);
        XML1 = XML1
                .replaceAll("#temporory_replacement2#",
                        "targetNamespace=\"http://java2wsdl.description.axis2.apache.org/xsd\"");
        return XML1;
    }

    // to test classes which involves enums
    public void testEnumClass(Class<?> c) throws Exception {
        generator = getGenerator(c);
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
            assertSimilarXML(XML2, XML1);
        }
    }

    public String prepareForEnum(String XML1) {
        XML1 = XML1.replaceAll(XMLSchemaNameSpace, "#temporory_replacement1#");
        XML1 = XML1
                .replaceAll(
                        "targetNamespace=\"http://ws.apache.org/namespaces/axis2/enum\"",
                        "#temporory_replacement2#");
        XML1 = XML1.replaceAll(
                "xmlns:.*=\"http://ws.apache.org/namespaces/axis2/enum\"",
                "xmlns:enum=\"http://ws.apache.org/namespaces/axis2/enum\"");

        XML1 = XML1.replaceAll("#temporory_replacement1#", XMLSchemaNameSpace);
        XML1 = XML1
                .replaceAll("#temporory_replacement2#",
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
        File file = new File(fileName);
        char[] buffer = null;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        buffer = new char[(int) file.length()];
        int i = 0;
        int c = bufferedReader.read();
        while (c != -1) {
            buffer[i++] = (char) c;
            c = bufferedReader.read();
        }
        return new String(buffer);
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

    public void testMapAsParameter() throws Exception {
        testMapClass(MapAsParameter.class);
    }

    public void testPojoAsParameter() throws Exception {
        testPOJOClass(PojoAsParameter.class);
    }

    public void TestStringAsReturnType() throws Exception {
        testClass(StringAsReturnType.class);
    }

    public void testCollectionAsReturnType() throws Exception {
        testClass(CollectionAsReturnType.class);
    }

    public void testMapAsReturnType() throws Exception {
        testMapClass(MapAsReturnType.class);
    }

    public void testPojoAsReturnType() throws Exception {
        testPOJOClass(POJOAsReturnType.class);
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

    public void testCustomExcepion() throws Exception {
        testExcClass(CustomExceptionCheck.class);
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

    // public void testComplexPOJO() throws Exception{
    // testPOJOClass(ComplexPOJO.class); // }

    public void testExtendedPOJO() throws Exception {
        testClass(ExtendedPOJO.class);
    }

    public void testAbstractPOJO() throws Exception {
        testClass(AbstractPOJO.class);
    }

    public void testConcretePOJO() throws Exception {
        testClass(ConcretePOJO.class);
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
        public void testPrimitiveTypes(byte b, short s, int i, long l, float f,
                double d, boolean isTrue, char c) {

        }
    }

    class ColectionAsParameter {
        public void testCollecttionAsPParameter(List<Object> l) {

        }
    }

    class PrimitiveArraysAsParametrs {
        public void testPrimitiveTypes(byte[] b, short[] s, int[] i, long[] l,
                float[] f, double[] d, boolean[] isTrue, char[] c) {

        }
    }

    class MapAsParameter {
        public void testMapAsParameter(Map<Object, Object> map) {

        }
    }

    class PojoAsParameter {
        public void testPojoAsParameter(SimplePOJO o) {

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

    class POJOAsReturnType {
        public SimplePOJO testPOJOAsReturnType() {
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

    class CustomExceptionCheck {
        public void testCustomException() throws CustomException {
            throw new CustomException("message");
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

    final class CustomException extends Exception {
        String mistake;

        public CustomException() {
            super();
            mistake = "unknown";
        }

        public CustomException(String err) {
            super(err);
            mistake = err; // save message
        }

        public String getError() {
            return mistake;
        }
    }

    class SimplePOJO {

        private int i;
        private double d;
        private String s;
        private char c;
        private byte b;
        private boolean bool;
        private long l;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public double getD() {
            return d;
        }

        public void setD(double d) {
            this.d = d;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        public char getC() {
            return c;
        }

        public void setC(char c) {
            this.c = c;
        }

        public byte getB() {
            return b;
        }

        public void setB(byte b) {
            this.b = b;
        }

        public boolean isBool() {
            return bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }

        public long getL() {
            return l;
        }

        public void setL(long l) {
            this.l = l;
        }

    }

    final class ComplexPOJO {
        private List list;
        private String[] strings;
        private Map<Object, Object> map;
        private SimplePOJO simplePOJO;
        private int[] i;
        private double[] d;
        private char[] c;
        private byte[] b;
        private boolean bool;
        private long[] l;

        public List getList() {
            return list;
        }

        public void setList(List list) {
            this.list = list;
        }

        public String[] getStrings() {
            return strings;
        }

        public void setStrings(String[] strings) {
            this.strings = strings;
        }

        public Map<Object, Object> getMap() {
            return map;
        }

        public void setMap(Map<Object, Object> map) {
            this.map = map;
        }

        public SimplePOJO getSimplePOJO() {
            return simplePOJO;
        }

        public void setSimplePOJO(SimplePOJO simplePOJO) {
            this.simplePOJO = simplePOJO;
        }

        public int[] getI() {
            return i;
        }

        public void setI(int[] i) {
            this.i = i;
        }

        public double[] getD() {
            return d;
        }

        public void setD(double[] d) {
            this.d = d;
        }

        public char[] getC() {
            return c;
        }

        public void setC(char[] c) {
            this.c = c;
        }

        public byte[] getB() {
            return b;
        }

        public void setB(byte[] b) {
            this.b = b;
        }

        public boolean isBool() {
            return bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }

        public long[] getL() {
            return l;
        }

        public void setL(long[] l) {
            this.l = l;
        }

    }

    abstract class AbstractPOJO {

        private int i;
        private double d;
        private String s;
        private char c;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public double getD() {
            return d;
        }

        public void setD(double d) {
            this.d = d;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        public char getC() {
            return c;
        }

        public void setC(char c) {
            this.c = c;
        }
    }

    final class ConcretePOJO extends AbstractPOJO {
        private byte b;
        private boolean bool;
        private long l;

        public byte getB() {
            return b;
        }

        public void setB(byte b) {
            this.b = b;
        }

        public boolean isBool() {
            return bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }

        public long getL() {
            return l;
        }

        public void setL(long l) {
            this.l = l;
        }

    }

    final class ExtendedPOJO extends SimplePOJO {
        private int j;
        private double dd;
        private String sss;
        private char cc;
        private byte bb;

        public int getJ() {
            return j;
        }

        public void setJ(int j) {
            this.j = j;
        }

        public double getDd() {
            return dd;
        }

        public void setDd(double dd) {
            this.dd = dd;
        }

        public String getSss() {
            return sss;
        }

        public void setSss(String sss) {
            this.sss = sss;
        }

        public char getCc() {
            return cc;
        }

        public void setCc(char cc) {
            this.cc = cc;
        }

        public byte getBb() {
            return bb;
        }

        public void setBb(byte bb) {
            this.bb = bb;
        }

    }

    public enum SimpleEnum {
        WHITE, BLACK, RED, YELLOW, BLUE;
    }
}
