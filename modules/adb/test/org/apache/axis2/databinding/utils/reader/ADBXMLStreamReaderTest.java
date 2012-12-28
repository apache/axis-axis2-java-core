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

package org.apache.axis2.databinding.utils.reader;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.serialize.StreamingOMSerializer;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axis2.databinding.utils.Constants;
import org.apache.axis2.util.StreamWrapper;
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ADBXMLStreamReaderTest extends XMLTestCase {

    private DocumentBuilder db;

    protected void setUp() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        db = dbf.newDocumentBuilder();
    }

    /** complex array scenario */
    public void testComplexObjectArrayScenario() throws Exception {
        String expectedXML =
                "<ns1:TestComplexStringArrayScenario xmlns:ns1=\"http://testComplexStringArrayScenario.org\">" +
                        "<Foo>Some Text</Foo>" +
                        "<Dependent>" +
                        "<age>25</age>" +
                        "<name>FooTwo</name>" +
                        "<sex>Male</sex>" +
                        "</Dependent>" +
                        "<AdditionalDependent>" +
                        "<age>25</age>" +
                        "<name>FooTwo</name>" +
                        "<sex>Male</sex>" +
                        "</AdditionalDependent>" +
                        "<AdditionalDependent>" +
                        "<age>25</age>" +
                        "<name>FooTwo</name>" +
                        "<sex>Male</sex>" +
                        "</AdditionalDependent>" +
                        "<AdditionalDependent>" +
                        "<age>25</age>" +
                        "<name>FooTwo</name>" +
                        "<sex>Male</sex>" +
                        "</AdditionalDependent>" +
                        "<AdditionalDependent>" +
                        "<age>25</age>" +
                        "<name>FooTwo</name>" +
                        "<sex>Male</sex>" +
                        "</AdditionalDependent>" +
                        "<Bar>Some More Text</Bar><" +
                        "/ns1:TestComplexStringArrayScenario>";

        ArrayList propertyList = new ArrayList();
        propertyList.add("Foo");
        propertyList.add("Some Text");
        propertyList.add(new QName("Dependent"));
        DummyBean dummyBean = new DummyBean();
        propertyList.add(dummyBean);

        DummyBean[] beans = new DummyBean[4];
        for (int i = 0; i < 4; i++) {
            beans[i] = new DummyBean();
        }
        for (int i = 0; i < beans.length; i++) {
            propertyList.add(new QName("AdditionalDependent"));
            propertyList.add(beans[i]);

        }

        propertyList.add("Bar");
        propertyList.add("Some More Text");

        XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(new QName(
                "http://testComplexStringArrayScenario.org", "TestComplexStringArrayScenario",
                "ns1"), propertyList.toArray(), null);
        String actualXML = getStringXML(pullParser);


        assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
    }

    /** complex array scenario with nulls in between */
    public void testComplexObjectArrayScenarioWithNulls() throws Exception {
        String expectedXML =
                "<ns1:TestComplexStringArrayScenario xmlns:ns1=\"http://testComplexStringArrayScenario.org\">" +
                        "<AdditionalDependent>" +
                        "<age>25</age>" +
                        "<name>FooTwo</name>" +
                        "<sex>Male</sex>" +
                        "</AdditionalDependent>" +
                        "<AdditionalDependent>" +
                        "<age>25</age>" +
                        "<name>FooTwo</name>" +
                        "<sex>Male</sex>" +
                        "</AdditionalDependent>" +
                        "<AdditionalDependent>" +
                        "<age>25</age>" +
                        "<name>FooTwo</name>" +
                        "<sex>Male</sex>" +
                        "</AdditionalDependent>" +
                        "<AdditionalDependent xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                        "</AdditionalDependent>" +
                        "<Bar>Some More Text</Bar><" +
                        "/ns1:TestComplexStringArrayScenario>";

        ArrayList propertyList = new ArrayList();

        DummyBean[] beans = new DummyBean[4];
        for (int i = 0; i < 4; i++) {
            beans[i] = new DummyBean();
        }

        beans[3] = null;

        for (int i = 0; i < beans.length; i++) {
            propertyList.add(new QName("AdditionalDependent"));
            propertyList.add(beans[i]);

        }

        propertyList.add("Bar");
        propertyList.add("Some More Text");

        XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(new QName(
                "http://testComplexStringArrayScenario.org", "TestComplexStringArrayScenario",
                "ns1"), propertyList.toArray(), null);
        String actualXML = getStringXML(pullParser);

        assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
    }

    /** Empty array */
    public void testComplexObjectArrayScenarioEmptyArray() throws Exception {
        String expectedXML =
                "<ns1:TestComplexStringArrayScenario xmlns:ns1=\"http://testComplexStringArrayScenario.org\">" +
                        "<Foo>Some Text</Foo>" +
                        "<Dependent>" +
                        "<age>25</age>" +
                        "<name>FooTwo</name>" +
                        "<sex>Male</sex>" +
                        "</Dependent>" +
                        "<Bar>Some More Text</Bar><" +
                        "/ns1:TestComplexStringArrayScenario>";

        ArrayList propertyList = new ArrayList();
        propertyList.add("Foo");
        propertyList.add("Some Text");
        propertyList.add(new QName("Dependent"));
        DummyBean dummyBean = new DummyBean();
        propertyList.add(dummyBean);

        String[] array = new String[] {};
        propertyList.add(new QName("AdditionalDependent"));
        propertyList.add(array);

        propertyList.add("Bar");
        propertyList.add("Some More Text");

        XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                new QName("http://testComplexStringArrayScenario.org",
                          "TestComplexStringArrayScenario", "ns1"),
                propertyList.toArray(),
                null);
        String actualXML = getStringXML(pullParser);
        assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
    }

    /** test a complex array list */
    public void testComplexArrayList() throws Exception {
        String exptectedXML = "<Person><Name>FooOne</Name><Organization>Apache</Organization>" +
        		"<Dependent><age>25</age><dependent><age>25</age><dependent><age>25</age>" +
        		"<name>FooTwo</name><sex>Male</sex></dependent><name>FooTwo</name><sex>Male</sex>" +
        		"</dependent><name>FooTwo</name><sex>Male</sex></Dependent>" +
        		"<test:Dependent xmlns:test=\"http://whatever.com\"><test:age>25</test:age>" +
        		"<test:dependent><test:age>25</test:age><test:name>FooTwo</test:name>" +
        		"<test:sex>Male</test:sex></test:dependent><test:name>FooTwo</test:name>" +
        		"<test:sex>Male</test:sex></test:Dependent></Person>";


        ArrayList propertyList = new ArrayList();
        propertyList.add("Name");
        propertyList.add("FooOne");

        propertyList.add("Organization");
        propertyList.add("Apache");

        propertyList.add(new QName("Dependent"));
        DummyBean dummyBean = new DummyBean();
        DummyBean nextdummyBean = dummyBean.addAnotherBean();
        nextdummyBean.addAnotherBean();
        propertyList.add(dummyBean);

        propertyList.add(new QName("http://whatever.com", "Dependent", "test"));
        dummyBean = new DummyBean();
        dummyBean.addAnotherBean();
        propertyList.add(dummyBean);

        QName projectQName = new QName("Person");
        XMLStreamReader pullParser =
                new ADBXMLStreamReaderImpl(projectQName, propertyList.toArray(), null);

        Document actualDom = newDocument(getStringXML(pullParser));
        Document expectedDocument = newDocument(exptectedXML);
        assertXMLEqual(actualDom, expectedDocument);
    }

        /** test List<java.lang.Integer> list */
    public void testGenericWrapperArrayList() throws Exception {
        String exptectedXML = " <genericIntegerList>" +
                                    "<genericInt>123</genericInt>" +
                                    "<genericInt>1234</genericInt>" +
                                    "<genericInt>12345</genericInt>" +
                                "</genericIntegerList>";

        List propertyList = new ArrayList();

        propertyList.add("genericInt");
        propertyList.add(new Integer(123));

        propertyList.add("genericInt");
        propertyList.add(new Integer(1234));

        propertyList.add("genericInt");
        propertyList.add(new Integer(12345));

        QName projectQName = new QName("genericIntegerList");
        XMLStreamReader pullParser =
                new ADBXMLStreamReaderImpl(projectQName, propertyList.toArray(), null);

        Document actualDom = newDocument(getStringXML(pullParser));
        Document expectedDocument = newDocument(exptectedXML);
        assertXMLEqual(actualDom, expectedDocument);
    }

    /** test List<java.lang.String> list */
    public void testGenericWrapperArrayList2() throws Exception {
        String exptectedXML = " <genericStringList>" +
                                    "<genericString>test1</genericString>" +
                                    "<genericString>test2</genericString>" +
                                    "<genericString>test3</genericString>" +
                                "</genericStringList>";

        List propertyList = new ArrayList();

        propertyList.add("genericString");
        propertyList.add(new String("test1"));

        propertyList.add("genericString");
        propertyList.add(new String("test2"));

        propertyList.add("genericString");
        propertyList.add(new String("test3"));

        QName projectQName = new QName("genericStringList");
        XMLStreamReader pullParser =
                new ADBXMLStreamReaderImpl(projectQName, propertyList.toArray(), null);

        Document actualDom = newDocument(getStringXML(pullParser));
        Document expectedDocument = newDocument(exptectedXML);
        assertXMLEqual(actualDom, expectedDocument);
    }

    /** test List<java.lang.Long> list */
    public void testGenericWrapperArrayList3() throws Exception {
        String exptectedXML = " <genericLongList>" +
                                    "<genericLong>12345678910</genericLong>" +
                                    "<genericLong>-12345678910</genericLong>" +
                                    "<genericLong>1234567891011</genericLong>" +
                                "</genericLongList>";

        List propertyList = new ArrayList();

        propertyList.add("genericLong");
        propertyList.add(new Long(12345678910L));

        propertyList.add("genericLong");
        propertyList.add(new Long(-12345678910L));

        propertyList.add("genericLong");
        propertyList.add(new Long(1234567891011L));

        QName projectQName = new QName("genericLongList");
        XMLStreamReader pullParser =
                new ADBXMLStreamReaderImpl(projectQName, propertyList.toArray(), null);

        Document actualDom = newDocument(getStringXML(pullParser));
        Document expectedDocument = newDocument(exptectedXML);
        assertXMLEqual(actualDom, expectedDocument);
    }

    /** test List<java.lang.Float> list */
    public void testGenericWrapperArrayList4() throws Exception {
        String exptectedXML = " <genericFloatList>" +
                                    "<genericFloat>123.0</genericFloat>" +
                                    "<genericFloat>-123.55</genericFloat>" +
                                    "<genericFloat>12345.99</genericFloat>" +
                                "</genericFloatList>";

        List propertyList = new ArrayList();

        propertyList.add("genericFloat");
        propertyList.add(new Float(123.0f));

        propertyList.add("genericFloat");
        propertyList.add(new Float(-123.55f));

        propertyList.add("genericFloat");
        propertyList.add(new Float(12345.99f));

        QName projectQName = new QName("genericFloatList");
        XMLStreamReader pullParser =
                new ADBXMLStreamReaderImpl(projectQName, propertyList.toArray(), null);

        Document actualDom = newDocument(getStringXML(pullParser));
        Document expectedDocument = newDocument(exptectedXML);
        assertXMLEqual(actualDom, expectedDocument);
    }

    /** test List<java.lang.Double> list */
    public void testGenericWrapperArrayList5() throws Exception {
        String exptectedXML = " <genericDoubleList>" +
                                    "<genericDouble>3456.12345</genericDouble>" +
                                    "<genericDouble>-3456.12345</genericDouble>" +
                                    "<genericDouble>123456.12345</genericDouble>" +
                                "</genericDoubleList>";

        List propertyList = new ArrayList();

        propertyList.add("genericDouble");
        propertyList.add(new Double(3456.12345d));

        propertyList.add("genericDouble");
        propertyList.add(new Double(-3456.12345d));

        propertyList.add("genericDouble");
        propertyList.add(new Double(123456.12345d));

        QName projectQName = new QName("genericDoubleList");
        XMLStreamReader pullParser =
                new ADBXMLStreamReaderImpl(projectQName, propertyList.toArray(), null);

        Document actualDom = newDocument(getStringXML(pullParser));
        Document expectedDocument = newDocument(exptectedXML);
        assertXMLEqual(actualDom, expectedDocument);
    }

/** test List<java.lang.Character> list */
    public void testGenericWrapperArrayList6() throws Exception {
        String exptectedXML = " <genericCharacterList>" +
                                    "<genericCharacter>A</genericCharacter>" +
                                    "<genericCharacter>B</genericCharacter>" +
                                    "<genericCharacter>C</genericCharacter>" +
                                "</genericCharacterList>";

        List propertyList = new ArrayList();

        propertyList.add("genericCharacter");
        propertyList.add(new Character('A'));

        propertyList.add("genericCharacter");
        propertyList.add(new Character('B'));

        propertyList.add("genericCharacter");
        propertyList.add(new Character('C'));

        QName projectQName = new QName("genericCharacterList");
        XMLStreamReader pullParser =
                new ADBXMLStreamReaderImpl(projectQName, propertyList.toArray(), null);

        Document actualDom = newDocument(getStringXML(pullParser));
        Document expectedDocument = newDocument(exptectedXML);
        assertXMLEqual(actualDom, expectedDocument);
    }

/** test List<java.lang.Short> list */
    public void testGenericWrapperArrayList7() throws Exception {
        String exptectedXML = " <genericShortList>" +
                                    "<genericShort>10</genericShort>" +
                                    "<genericShort>20</genericShort>" +
                                    "<genericShort>30</genericShort>" +
                                "</genericShortList>";

        List propertyList = new ArrayList();

        propertyList.add("genericShort");
        propertyList.add(new Short((short) 10));

        propertyList.add("genericShort");
        propertyList.add(new Short((short) 20));

        propertyList.add("genericShort");
        propertyList.add(new Short((short) 30));

        QName projectQName = new QName("genericShortList");
        XMLStreamReader pullParser =
                new ADBXMLStreamReaderImpl(projectQName, propertyList.toArray(), null);

        Document actualDom = newDocument(getStringXML(pullParser));
        Document expectedDocument = newDocument(exptectedXML);
        assertXMLEqual(actualDom, expectedDocument);
    }

    public void testWithOMElements() throws Exception {

        String expectedXML =
                "<OMElementTest><axis2:FirstOMElement xmlns:axis2=\"http://ws.apache.org/namespaces/axis2\">" +
                        "<axis2:SecondOMElement></axis2:SecondOMElement></axis2:FirstOMElement><Foo>Some Text</Foo>" +
                        "<Dependent><age>25</age><name>FooTwo</name><sex>Male</sex></Dependent>" +
                        "<axis2:SecondOMElement xmlns:axis2=\"http://ws.apache.org/namespaces/axis2\">" +
                        "</axis2:SecondOMElement></OMElementTest>";

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace axis2Namespace = factory.createOMNamespace(
                org.apache.axis2.Constants.AXIS2_NAMESPACE_URI,
                org.apache.axis2.Constants.AXIS2_NAMESPACE_PREFIX);
        OMElement firstElement = factory.createOMElement("FirstOMElement", axis2Namespace);
        OMElement secondElement =
                factory.createOMElement("SecondOMElement", axis2Namespace, firstElement);

        ArrayList propertyList = new ArrayList();

        // add an OMElement
        propertyList.add(firstElement.getQName());
        propertyList.add(firstElement);

        // add some more stuff
        propertyList.add("Foo");
        propertyList.add("Some Text");
        propertyList.add(new QName("Dependent"));
        DummyBean dummyBean = new DummyBean();
        propertyList.add(dummyBean);

//         lets add one more element
        propertyList.add(secondElement.getQName());
        propertyList.add(secondElement);


        XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(new QName("OMElementTest"),
                                                                propertyList.toArray(), null);
        String stringXML = getStringXML(pullParser);
        Document actualDom = newDocument(stringXML);
        Document expectedDocument = newDocument(expectedXML);
        assertXMLEqual(actualDom, expectedDocument);
    }

    /** Test a completely null element */
    public void testNullableAttribute() throws Exception {
        /*
        This is what I expect :

        */
        String exptectedXML =
                "<Person xmlns=\"\"><Name xmlns=\"\">FooOne</Name><DependentOne xmlns=\"\" " +
                        "xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>" +
                        "</Person>";

        ArrayList propertyList = new ArrayList();
        propertyList.add("Name");
        propertyList.add("FooOne");
        propertyList.add(new QName("DependentOne"));
        propertyList.add(null);

        QName projectQName = new QName("Person");
        XMLStreamReader pullParser =
                new ADBXMLStreamReaderImpl(projectQName, propertyList.toArray(), null);

        Document actualDom = newDocument(getStringXML(pullParser));
        Document expectedDocument = newDocument(exptectedXML);
        assertXMLEqual(actualDom, expectedDocument);
    }

    /** Test a simple array */
    public void testSimpleStringArrayScenario() throws Exception {
        String expectedXML =
                "<ns1:TestComplexStringArrayScenario xmlns:ns1=\"http://testComplexStringArrayScenario.org\">" +
                        "<StringInfo><array>Some Text 0</array>" +
                        "<array>Some Text 1</array>" +
                        "<array>Some Text 2</array>" +
                        "<array>Some Text 3</array></StringInfo>" +
                        "</ns1:TestComplexStringArrayScenario>";

        ArrayList propertyList = new ArrayList();

        String[] stringArray = new String[4];
        for (int i = 0; i < 4; i++) {
            stringArray[i] = "Some Text " + i;
        }
        propertyList.add("StringInfo");
        propertyList.add(stringArray);

        XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                new QName("http://testComplexStringArrayScenario.org",
                          "TestComplexStringArrayScenario", "ns1"),
                propertyList.toArray(), null);
        String actualXML = getStringXML(pullParser);


        assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
    }

    /** Test a simple array with null's inbetween */
    public void testSimpleStringArrayScenarioWithNulls() throws Exception {
        String expectedXML =
                "<ns1:TestComplexStringArrayScenario xmlns:ns1=\"http://testComplexStringArrayScenario.org\">" +
                        "<StringInfo><array>Some Text 0</array>" +
                        "<array xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>" +
                        "<array>Some Text 2</array>" +
                        "<array>Some Text 3</array></StringInfo>" +
                        "</ns1:TestComplexStringArrayScenario>";

        ArrayList propertyList = new ArrayList();

        String[] stringArray = new String[4];
        for (int i = 0; i < 4; i++) {
            stringArray[i] = "Some Text " + i;
        }
        stringArray[1] = null;

        propertyList.add("StringInfo");
        propertyList.add(stringArray);

        XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                new QName("http://testComplexStringArrayScenario.org",
                          "TestComplexStringArrayScenario", "ns1"),
                propertyList.toArray(), null);
        String actualXML = getStringXML(pullParser);


        assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
    }


    /** test the mixed content */
    public void testComplexStringArrayScenarioWithMixedContent() throws Exception {
        String expectedXML =
                "<ns1:TestComplexStringArrayScenario xmlns:ns1=\"http://testComplexStringArrayScenario.org\">" +
                        "<Foo>Some Text</Foo>" +
                        "<Dependent>" +
                        "<age>25</age>" +
                        "<name>FooTwo</name>" +
                        "<sex>Male</sex>" +
                        "</Dependent>" +
                        "<StringInfo><array>Some Text 0</array>" +
                        "<array>Some Text 1</array>" +
                        "<array>Some Text 2</array>" +
                        "<array>Some Text 3</array></StringInfo>" +
                        "<Bar>Some More Text</Bar>" +
                        "</ns1:TestComplexStringArrayScenario>";

        ArrayList propertyList = new ArrayList();
        propertyList.add("Foo");
        propertyList.add("Some Text");
        propertyList.add(new QName("Dependent"));
        DummyBean dummyBean = new DummyBean();
        propertyList.add(dummyBean);

        String[] stringArray = new String[4];
        for (int i = 0; i < 4; i++) {
            stringArray[i] = "Some Text " + i;
        }
        propertyList.add("StringInfo");
        propertyList.add(stringArray);

        propertyList.add("Bar");
        propertyList.add("Some More Text");

        XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                new QName("http://testComplexStringArrayScenario.org",
                          "TestComplexStringArrayScenario", "ns1"),
                propertyList.toArray(),
                null);
        String actualXML = getStringXML(pullParser);


        assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
    }

    /** Test a simple array with one element nil */
    public void testComplexStringArrayScenarioWithNull() throws Exception {
        String expectedXML = "<ns1:TestComplexStringArrayScenario " +
                "xmlns:ns1=\"http://testComplexStringArrayScenario.org\" " +
                ">" +
                "<StringInfo><array>Some Text 0</array>" +
                "<array xsi:nil=\"true\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"></array>" +
                "<array>Some Text 2</array>" +
                "<array>Some Text 3</array></StringInfo>" +
                "</ns1:TestComplexStringArrayScenario>";

        ArrayList propertyList = new ArrayList();

        String[] stringArray = new String[4];
        for (int i = 0; i < 4; i++) {
            if (i != 1) stringArray[i] = "Some Text " + i;
        }
        stringArray[1] = null;

        propertyList.add("StringInfo");
        propertyList.add(stringArray);

        XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                new QName("http://testComplexStringArrayScenario.org",
                          "TestComplexStringArrayScenario", "ns1"),
                propertyList.toArray(), null);
        String actualXML = getStringXML(pullParser);


        assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
    }

    /**
     * Test multiple unqulified attributes
     *
     * @throws XMLStreamException
     */
    public void testAttributes() throws Exception {

        String expectedXML =
                "<emp:Employee xmlns:emp=\"http://ec.org/software\" Attr2=\"Value 2\" " +
                        "Attr3=\"Value 3\" Attr1=\"Value 1\" Attr5=\"Value 5\" Attr4=\"Value 4\"></emp:Employee>";

        OMFactory factory = OMAbstractFactory.getOMFactory();
        QName elementQName = new QName("http://ec.org/software", "Employee", "emp");
        OMAttribute[] attribute = new OMAttribute[5];

        for (int i = 0; i < 5; i++) {
            attribute[i] = factory.createOMAttribute("Attr" + (i + 1), null, "Value " + (i + 1));
        }

        List omAttribList = new ArrayList();
        for (int i = 0; i < attribute.length; i++) {
            omAttribList.add(Constants.OM_ATTRIBUTE_KEY);
            omAttribList.add(attribute[i]);
        }


        String stringXML = getStringXML(new ADBXMLStreamReaderImpl(elementQName,
                                                                   null,
                                                                   omAttribList.toArray()));
        Document actualDom = newDocument(stringXML);
        Document expectedDocument = newDocument(expectedXML);
        assertXMLEqual(actualDom, expectedDocument);
    }

    /** A text only element */
    public void testElementText() throws Exception {

        String expectedXML = "<ns1:testElementText xmlns:ns1=\"http://testElementText.org\">" +
                "This is some Text for the element</ns1:testElementText>";
        
        ArrayList properties = new ArrayList();
        properties.add(ADBXMLStreamReader.ELEMENT_TEXT);
        properties.add("This is some Text for the element");

        XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                new QName("http://testElementText.org", "testElementText", "ns1"),
                properties.toArray(), null);

        String actualXML = getStringXML(pullParser);

        assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
    }

/// todo Fails due to a bug in WSTX writer
//    /**
//     * Test multiple qualified attributes
//     * @throws XMLStreamException
//     */
//    public void testAttributesWithNamespaces() throws XMLStreamException {
//
//        String expectedXML = "<emp:Employee xmlns:emp=\"http://ec.org/software\" " +
//                "xmlns:attrNS=\"mailto:whoever@whatever.com\" attrNS:Attr2=\"Value 2\" " +
//                "attrNS:Attr3=\"Value 3\" attrNS:Attr1=\"Value 1\"\n" +
//                "              attrNS:Attr5=\"Value 5\" attrNS:Attr4=\"Value 4\"></emp:Employee>";
//
//        OMFactory factory = OMAbstractFactory.getOMFactory();
//        QName elementQName = new QName("http://ec.org/software", "Employee", "emp");
//        OMNamespace attrNS = factory.createOMNamespace("mailto:whoever@whatever.com", "attrNS");
//
//        // add some attributes with namespaces
//        OMAttribute[] attribute = new OMAttribute[5];
//        for (int i = 0; i < 5; i++) {
//            attribute[i] = factory.createOMAttribute("Attr" + (i + 1), attrNS, "Value " + (i + 1));
//        }
//
//        List omAttribList = new ArrayList();
//        for (int i = 0; i < attribute.length; i++) {
//            omAttribList.add(Constants.OM_ATTRIBUTE_KEY);
//            omAttribList.add(attribute[i]);
//        }
//        String stringXML = getStringXML(new ADBXMLStreamReaderImpl(elementQName,
//                null,
//                omAttribList.toArray()));
//        try {
//            Document actualDom = newDocument(stringXML);
//            Document expectedDocument = newDocument(expectedXML);
//            assertXMLEqual(actualDom, expectedDocument);
//        } catch (ParserConfigurationException e) {
//            fail("Exception in parsing documents " + e);
//        } catch (SAXException e) {
//            fail("Exception in parsing documents " + e);
//        } catch (IOException e) {
//            fail("Exception in parsing documents " + e);
//        }
//    }

    /** test for qualified attributes */
    public void testUnQualifiedAttributes() throws Exception {

        String expectedXML =
                "<ns1:testElementText xmlns:ns1=\"http://testElementText.org\" MyUnQualifiedAttribute=\"MyAttributeValue\">" +
                        "<ns2:QualifiedElement xmlns:ns2=\"http://testQElementText.org\">" +
                        "This is some Text for the element</ns2:QualifiedElement></ns1:testElementText>";
        
        ArrayList properties = new ArrayList();
        properties.add(new QName("http://testQElementText.org", "QualifiedElement", "ns2"));
        properties.add("This is some Text for the element");

        String[] attributes = new String[2];
        attributes[0] = "MyUnQualifiedAttribute";
        attributes[1] = "MyAttributeValue";


        XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                new QName("http://testElementText.org", "testElementText", "ns1"),
                properties.toArray(),
                attributes);

        String actualXML = getStringXML(pullParser);

        assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
    }

    /** test for base64 */
    public void testBase64EncodedText() throws Exception {

        String textTobeSent = "33344MthwrrewrIOTEN)(&**^E(W)EW";

        String expectedXML = "<ns1:testElementText xmlns:ns1=\"http://testElementText.org\">" +
                "<ns2:QualifiedElement xmlns:ns2=\"http://testQElementText.org\">" +
                Base64Utils.encode(textTobeSent.getBytes()) +
                "</ns2:QualifiedElement></ns1:testElementText>";
        
        ArrayList properties = new ArrayList();
        properties.add(new QName("http://testQElementText.org", "QualifiedElement", "ns2"));
        properties.add(new DataHandler(new ByteArrayDataSource(textTobeSent.getBytes())));

        XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                new QName("http://testElementText.org", "testElementText", "ns1"),
                properties.toArray(),
                null);

        String actualXML = getStringXML(pullParser);

        assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
    }

    /** test the qualified elements A qulified element has been associated with a namespace */
    public void testQualifiedElement() throws Exception {

        String expectedXML = "<ns1:testElementText xmlns:ns1=\"http://testElementText.org\">" +
                "<ns2:QualifiedElement xmlns:ns2=\"http://testQElementText.org\">" +
                "This is some Text for the element</ns2:QualifiedElement></ns1:testElementText>";
        
        ArrayList properties = new ArrayList();
        properties.add(new QName("http://testQElementText.org", "QualifiedElement", "ns2"));
        properties.add("This is some Text for the element");

        XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                new QName("http://testElementText.org", "testElementText", "ns1"),
                properties.toArray(),
                null);

        String actualXML = getStringXML(pullParser);
        assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
    }

    /**
     * Util method to convert the pullstream to a string
     *
     * @param reader
     * @return
     */
    private String getStringXML(XMLStreamReader reader) throws XMLStreamException {
        //the returned pullparser starts at an Element rather than the start
        //document event. This is somewhat disturbing but since an ADBBean
        //denotes an XMLFragment, it is justifiable to keep the current event
        //at the Start-element rather than the start document
        //What it boils down to is that we need to wrap the reader in a
        //stream wrapper to get a fake start-document event

        StreamingOMSerializer ser = new StreamingOMSerializer();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XMLStreamWriter writer = StAXUtils.createXMLStreamWriter(byteArrayOutputStream);
        ser.serialize(
                new StreamWrapper(reader),
                writer);
        writer.flush();
        return byteArrayOutputStream.toString();
    }

//     /**
//     * Util method to convert the pullstream to a string
//     * @param reader
//     * @return
//     */
//    private String getStringXML(XMLStreamReader reader) {
//        //the returned pullparser starts at an Element rather than the start
//        //document event. This is somewhat disturbing but since an ADBBean
//        //denotes an XMLFragment, it is justifiable to keep the current event
//        //at the Start-element rather than the start document
//        //What it boils down to is that we need to wrap the reader in a
//        //stream wrapper to get a fake start-document event
//        StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(
//                new StreamWrapper(reader));
//        //stAXOMBuilder.setDoDebug(true);
//        OMElement omelement = stAXOMBuilder.getDocumentElement();
//        return omelement.toString();
//    }

    /**
     * Creates a DOM document from the string
     *
     * @param xml
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public Document newDocument(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        return db.parse(new ByteArrayInputStream(xml.getBytes()));
    }
}
