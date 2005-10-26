package org.apache.axis2.databinding.utils;

import org.apache.axis2.Constants;
import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

public class ADBPullParserTest extends XMLTestCase {

    protected Log log = LogFactory.getLog(getClass());


    protected void setUp() throws Exception {

    }

    public void testSimpleArrayList() {
        try {

            /*
            This is what I expect :
            <Person>
                <Name>FooOne</Name>
                <DependentOne>
                    <Name>FooTwo</Name>
                    <Age>25</Age>
                    <Sex>Male</Sex>
                </DependentOne>
                <DependentTwo>
                    <Name>FooTwo</Name>
                    <Age>25</Age>
                    <Sex>Male</Sex>
                </DependentTwo>
                <Organization>Apache</Organization>
            </Person>
            */
            String exptectedXML = "<Person><Name>FooOne</Name><DependentOne><Name>FooTwo</Name>" +
                    "<Age>25</Age><Sex>Male</Sex></DependentOne><DependentTwo><Name>FooTwo</Name>" +
                    "<Age>25</Age><Sex>Male</Sex></DependentTwo><Organization>Apache</Organization>" +
                    "</Person>";

            ArrayList propertyList = new ArrayList();
            propertyList.add("Name");
            propertyList.add("FooOne");
            propertyList.add(new QName("DependentOne"));
            propertyList.add(new DummyADBBean());
            propertyList.add(new QName("DependentTwo"));
            propertyList.add(new DummyADBBean());
            propertyList.add("Organization");
            propertyList.add("Apache");

            QName projectQName = new QName("Person");
            XMLStreamReader pullParser = ADBPullParser.createPullParser(projectQName, propertyList.toArray(), null);

            Document actualDom = newDocument(getStringXML(pullParser));
            Document expectedDocument = newDocument(exptectedXML);
            assertXMLEqual(actualDom, expectedDocument);
        } catch (ParserConfigurationException e) {
            fail("Exception in parsing documents " + e);
        } catch (SAXException e) {
            fail("Exception in parsing documents " + e);
        } catch (IOException e) {
            fail("Exception in parsing documents " + e);
        }

    }

    public void testComplexArrayList() {
        try {

            /*
            This is what I expect :

            <Person>
                <Name>FooOne</Name>
                <Organization>Apache</Organization>
                <Dependent>
                    <Name>FooTwo</Name>
                    <Age>25</Age>
                    <Sex>Male</Sex>
                    <Depemdent>
                        <Name>FooTwo</Name>
                        <Age>25</Age>
                        <Sex>Male</Sex>
                            <Depemdent>
                            <Name>FooTwo</Name>
                            <Age>25</Age>
                            <Sex>Male</Sex>
                    </Depemdent>
                </Depemdent>
            </Dependent>
            <Dependent>
                <Name>FooTwo</Name>
                <Age>25</Age>
                <Sex>Male</Sex>
                <Depemdent>
                    <Name>FooTwo</Name>
                    <Age>25</Age>
                    <Sex>Male</Sex>
                </Depemdent>
            </Dependent>
        </Person>
            */
            String exptectedXML = "<Person><Name>FooOne</Name><Organization>Apache</Organization>" +
                    "<Dependent><Name>FooTwo</Name><Age>25</Age><Sex>Male</Sex><Depemdent>" +
                    "<Name>FooTwo</Name><Age>25</Age><Sex>Male</Sex><Depemdent><Name>FooTwo</Name>" +
                    "<Age>25</Age><Sex>Male</Sex></Depemdent></Depemdent></Dependent>" +
                    "<test:Dependent xmlns:test=\"http://whatever.com\"><Name>FooTwo</Name><Age>25</Age>" +
                    "<Sex>Male</Sex><Depemdent><Name>FooTwo</Name><Age>25</Age><Sex>Male</Sex>" +
                    "</Depemdent></test:Dependent></Person>";


            ArrayList propertyList = new ArrayList();
            propertyList.add("Name");
            propertyList.add("FooOne");

            propertyList.add("Organization");
            propertyList.add("Apache");

            propertyList.add(new QName("Dependent"));
            DummyADBBean dummyBean = new DummyADBBean();
            ADBPullParserTest.DummyADBBean nextdummyBean = dummyBean.addAnotherBean();
            nextdummyBean.addAnotherBean();
            propertyList.add(dummyBean);

            propertyList.add(new QName("http://whatever.com", "Dependent", "test"));
            dummyBean = new DummyADBBean();
            dummyBean.addAnotherBean();
            propertyList.add(dummyBean);

            QName projectQName = new QName("Person");
            XMLStreamReader pullParser = ADBPullParser.createPullParser(projectQName, propertyList.toArray(), null);

            Document actualDom = newDocument(getStringXML(pullParser));
            Document expectedDocument = newDocument(exptectedXML);
            assertXMLEqual(actualDom, expectedDocument);
        } catch (ParserConfigurationException e) {
            fail("Exception in parsing documents " + e);
        } catch (SAXException e) {
            fail("Exception in parsing documents " + e);
        } catch (IOException e) {
            fail("Exception in parsing documents " + e);
        }

    }

    private String getEventString(int eventCode) {
        String event = "";

        switch (eventCode) {
            case 1 :
                event = "START_ELEMENT";
                break;
            case 2 :
                event = "END_ELEMENT";
                break;
            case 3 :
                event = "PROCESSING_INSTRUCTION";
                break;
            case 4 :
                event = "CHARACTERS";
                break;
            case 5 :
                event = "COMMENT";
                break;
            case 6 :
                event = "SPACE";
                break;
            case 7 :
                event = "START_DOCUMENT";
                break;
            case 8 :
                event = "END_DOCUMENT";
                break;
            case 9 :
                event = "ENTITY_REFERENCE";
                break;
            case 10 :
                event = "ATTRIBUTE";
                break;
            case 11 :
                event = "DTD";
                break;
            case 12 :
                event = "CDATA";
                break;
            case 13 :
                event = "NAMESPACE";
                break;
            case 14 :
                event = "NOTATION_DECLARATION";
                break;
            case 15 :
                event = "ENTITY_DECLARATION";
                break;
        }
        return event;
    }

    public class DummyADBBean implements ADBBean {
        ArrayList propertyList = new ArrayList();

        public DummyADBBean() {
            propertyList.add("Name");
            propertyList.add("FooTwo");
            propertyList.add("Age");
            propertyList.add("25");
            propertyList.add("Sex");
            propertyList.add("Male");
        }

        public DummyADBBean addAnotherBean() {
            propertyList.add(new QName("Depemdent"));
            DummyADBBean dummyBean = new DummyADBBean();
            propertyList.add(dummyBean);
            return dummyBean;
        }

        public XMLStreamReader getPullParser(QName adbBeanQName) {
            return ADBPullParser.createPullParser(adbBeanQName, propertyList.toArray(), null);
        }
    }

    public void testAttributes() throws XMLStreamException {

        String expectedXML = "<emp:Employee xmlns:emp=\"http://ec.org/software\" Attr2=\"Value 2\" " +
                "Attr3=\"Value 3\" Attr1=\"Value 1\" Attr5=\"Value 5\" Attr4=\"Value 4\"></emp:Employee>";

        OMFactory factory = OMAbstractFactory.getOMFactory();
        QName elementQName = new QName("http://ec.org/software", "Employee", "emp");
        OMAttribute[] attribute = new OMAttribute[5];

        for (int i = 0; i < 5; i++) {
            attribute[i] = factory.createOMAttribute("Attr" + (i + 1), null, "Value " + (i + 1));
        }

        String stringXML = getStringXML(ADBPullParser.createPullParser(elementQName, null, new Object[]{null, attribute}));
        try {
            Document actualDom = newDocument(stringXML);
            Document expectedDocument = newDocument(expectedXML);
            assertXMLEqual(actualDom, expectedDocument);
        } catch (ParserConfigurationException e) {
            fail("Exception in parsing documents " + e);
        } catch (SAXException e) {
            fail("Exception in parsing documents " + e);
        } catch (IOException e) {
            fail("Exception in parsing documents " + e);
        }


    }

    public void testAttributesWithNamespaces() throws XMLStreamException {

        String expectedXML = "<emp:Employee xmlns:emp=\"http://ec.org/software\" " +
                "xmlns:attrNS=\"mailto:whoever@whatever.com\" attrNS:Attr2=\"Value 2\" " +
                "attrNS:Attr3=\"Value 3\" attrNS:Attr1=\"Value 1\"\n" +
                "              attrNS:Attr5=\"Value 5\" attrNS:Attr4=\"Value 4\"></emp:Employee>";

        OMFactory factory = OMAbstractFactory.getOMFactory();
        QName elementQName = new QName("http://ec.org/software", "Employee", "emp");
        OMNamespace attrNS = factory.createOMNamespace("mailto:whoever@whatever.com", "attrNS");

        // add some attributes with namespaces
        OMAttribute[] attribute = new OMAttribute[5];
        for (int i = 0; i < 5; i++) {
            attribute[i] = factory.createOMAttribute("Attr" + (i + 1), attrNS, "Value " + (i + 1));
        }

        String stringXML = getStringXML(ADBPullParser.createPullParser(elementQName, null, new Object[]{null, attribute}));
        try {
            Document actualDom = newDocument(stringXML);
            Document expectedDocument = newDocument(expectedXML);
            assertXMLEqual(actualDom, expectedDocument);
        } catch (ParserConfigurationException e) {
            fail("Exception in parsing documents " + e);
        } catch (SAXException e) {
            fail("Exception in parsing documents " + e);
        } catch (IOException e) {
            fail("Exception in parsing documents " + e);
        }
    }

    public void testWithOMElements() throws XMLStreamException {

        String expectedXML = "<OMElementTest><axis2:FirstOMElement xmlns:axis2=\"http://ws.apache.org/namespaces/axis2\">" +
                "<axis2:SecondOMElement></axis2:SecondOMElement></axis2:FirstOMElement><Foo>Some Text</Foo>" +
                "<Dependent><Name>FooTwo</Name><Age>25</Age><Sex>Male</Sex></Dependent>" +
                "<axis2:SecondOMElement xmlns:axis2=\"http://ws.apache.org/namespaces/axis2\">" +
                "</axis2:SecondOMElement></OMElementTest>";

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace axis2Namespace = factory.createOMNamespace(Constants.AXIS2_NAMESPACE_URI, Constants.AXIS2_NAMESPACE_PREFIX);
        OMElement firstElement = factory.createOMElement("FirstOMElement", axis2Namespace);
        OMElement secondElement = factory.createOMElement("SecondOMElement", axis2Namespace, firstElement);

        ArrayList propertyList = new ArrayList();

        // add an OMElement
        propertyList.add(firstElement.getQName());
        propertyList.add(firstElement);

        // add some more stuff
        propertyList.add("Foo");
        propertyList.add("Some Text");
        propertyList.add(new QName("Dependent"));
        DummyADBBean dummyBean = new DummyADBBean();
        propertyList.add(dummyBean);

//         lets add one more element
        propertyList.add(secondElement.getQName());
        propertyList.add(secondElement);


        XMLStreamReader pullParser = ADBPullParser.createPullParser(new QName("OMElementTest"), propertyList.toArray(), null);
        String stringXML = getStringXML(pullParser);
        try {
            Document actualDom = newDocument(stringXML);
            Document expectedDocument = newDocument(expectedXML);
            assertXMLEqual(actualDom, expectedDocument);
        } catch (ParserConfigurationException e) {
            fail("Exception in parsing documents " + e);
        } catch (SAXException e) {
            fail("Exception in parsing documents " + e);
        } catch (IOException e) {
            fail("Exception in parsing documents " + e);
        }

    }

    public void testAttributeArray() {

        String expectedXML = "<ns1:TestAttributeArray xmlns:ns1=\"http://testAttributeArray.org\" " +
                "xmlns:attrNS=\"mailto:whoever@whatever.com\" xmlns:myAttr=\"http://www.axis2.net\" " +
                "myAttr:Axis2Attr=\"SomeValue\" attrNS:Attr3=\"Value 3\" attrNS:Attr1=\"Value 1\" " +
                "attrNS:Attr4=\"Value 4\" attrNS:Attr5=\"Value 5\" attrNS:Attr2=\"Value 2\">" +
                "<Foo>Some Text</Foo><Dependent><Name>FooTwo</Name><Age>25</Age><Sex>Male</Sex>" +
                "</Dependent></ns1:TestAttributeArray>";

        // lets first have some properties
        ArrayList propertyList = new ArrayList();
        propertyList.add("Foo");
        propertyList.add("Some Text");
        propertyList.add(new QName("Dependent"));
        DummyADBBean dummyBean = new DummyADBBean();
        propertyList.add(dummyBean);

        ArrayList attributes = new ArrayList();
        OMFactory factory = OMAbstractFactory.getOMFactory();
        QName elementQName = new QName("http://ec.org/software", "Employee", "emp");
        OMNamespace attrNS = factory.createOMNamespace("mailto:whoever@whatever.com", "attrNS");

        // add some attributes with namespaces
        OMAttribute[] attribute = new OMAttribute[5];
        for (int i = 0; i < 5; i++) {
            attribute[i] = factory.createOMAttribute("Attr" + (i + 1), attrNS, "Value " + (i + 1));
        }
        attributes.add(null);
        attributes.add(attribute);
        attributes.add(new QName("http://www.axis2.net", "Axis2Attr", "myAttr"));
        attributes.add("SomeValue");

        XMLStreamReader pullParser = ADBPullParser.createPullParser(new QName("http://testAttributeArray.org", "TestAttributeArray", "ns1"), propertyList.toArray(), attributes.toArray());
        String actualXML = getStringXML(pullParser);

        try {
            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        }

    }

    public void testComplexStringArrayScenario() {

        String expectedXML = "<ns1:TestComplexStringArrayScenario xmlns:ns1=\"http://testComplexStringArrayScenario.org\">" +
                "<Foo>Some Text</Foo>" +
                "<Dependent>" +
                "<Name>FooTwo</Name>" +
                "<Age>25</Age>" +
                "<Sex>Male</Sex>" +
                "</Dependent>" +
                "<StringInfo>Some Text 0</StringInfo>" +
                "<StringInfo>Some Text 1</StringInfo>" +
                "<StringInfo>Some Text 2</StringInfo>" +
                "<StringInfo>Some Text 3</StringInfo>" +
                "<Bar>Some More Text</Bar>" +
                "</ns1:TestComplexStringArrayScenario>";

        ArrayList propertyList = new ArrayList();
        propertyList.add("Foo");
        propertyList.add("Some Text");
        propertyList.add(new QName("Dependent"));
        DummyADBBean dummyBean = new DummyADBBean();
        propertyList.add(dummyBean);

        String[] stringArray = new String[4];
        for (int i = 0; i < 4; i++) {
            stringArray[i] = "Some Text " + i;
        }
        propertyList.add("StringInfo");
        propertyList.add(stringArray);

        propertyList.add("Bar");
        propertyList.add("Some More Text");

        XMLStreamReader pullParser = ADBPullParser.createPullParser(new QName("http://testComplexStringArrayScenario.org", "TestComplexStringArrayScenario", "ns1"), propertyList.toArray(), null);
        String actualXML = getStringXML(pullParser);

        try {
            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        }


    }

    public void testComplexObjectArrayScenario() {

        String expectedXML = "<ns1:TestComplexStringArrayScenario xmlns:ns1=\"http://testComplexStringArrayScenario.org\">" +
                "<Foo>Some Text</Foo>" +
                "<Dependent>" +
                "<Name>FooTwo</Name>" +
                "<Age>25</Age>" +
                "<Sex>Male</Sex>" +
                "</Dependent>" +
                "<AdditionalDependent>" +
                "<Name>FooTwo</Name>" +
                "<Age>25</Age>" +
                "<Sex>Male</Sex>" +
                "</AdditionalDependent>" +
                "<AdditionalDependent>" +
                "<Name>FooTwo</Name>" +
                "<Age>25</Age>" +
                "<Sex>Male</Sex>" +
                "</AdditionalDependent>" +
                "<AdditionalDependent>" +
                "<Name>FooTwo</Name>" +
                "<Age>25</Age>" +
                "<Sex>Male</Sex>" +
                "</AdditionalDependent>" +
                "<AdditionalDependent>" +
                "<Name>FooTwo</Name>" +
                "<Age>25</Age>" +
                "<Sex>Male</Sex>" +
                "</AdditionalDependent>" +
                "<Bar>Some More Text</Bar><" +
                "/ns1:TestComplexStringArrayScenario>";

        ArrayList propertyList = new ArrayList();
        propertyList.add("Foo");
        propertyList.add("Some Text");
        propertyList.add(new QName("Dependent"));
        DummyADBBean dummyBean = new DummyADBBean();
        propertyList.add(dummyBean);

        ADBBean[] adbBeans = new ADBBean[4];
        for (int i = 0; i < 4; i++) {
            adbBeans[i] = new DummyADBBean();
        }
        propertyList.add(new QName("AdditionalDependent"));
        propertyList.add(adbBeans);

        propertyList.add("Bar");
        propertyList.add("Some More Text");

        XMLStreamReader pullParser = ADBPullParser.createPullParser(new QName("http://testComplexStringArrayScenario.org", "TestComplexStringArrayScenario", "ns1"), propertyList.toArray(), null);
        String actualXML = getStringXML(pullParser);

        try {
            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        }
    }

    public void testElementText() {

        String expectedXML = "<ns1:testElementText xmlns:ns1=\"http://testElementText.org\">" +
                "This is some Text for the element</ns1:testElementText>";
        try {
            ArrayList properties = new ArrayList();
            properties.add(ADBPullParser.ELEMENT_TEXT);
            properties.add("This is some Text for the element");

            XMLStreamReader pullParser = ADBPullParser.createPullParser(new QName("http://testElementText.org", "testElementText", "ns1"), properties.toArray(), null);

            String actualXML = getStringXML(pullParser);

            assertXMLEqual(newDocument(expectedXML), newDocument(actualXML));
        } catch (ParserConfigurationException e) {
            fail("Error has occurred " + e);
        } catch (SAXException e) {
            fail("Error has occurred " + e);
        } catch (IOException e) {
            fail("Error has occurred " + e);
        }
    }

    public void testComplexScenarioOne() {
        /*
           <apache:Project xmlns:axis2="http://ws.apache.org/namespaces/axis2" xmlns:apache="http://www.apache.org/" xmlns:myAttr="mailto:myAttributes@axis2.org" myAttr:name="Apache Axis2">
    <CurrentRelease>0.92</CurrentRelease>
    <axis2:Module axis2:name="xml">
        <Description>This is the XML object model for Axis2</Description>
        <Dependencies myAttr:number="2">
            <JarName>stax-api.jar</JarName>
            <JarName>stax-impl.jar</JarName>
        </Dependencies>
    </axis2:Module>
    <axis2:Module axis2:name="core">
        <Description myAttr:number="3">This will handle the main logics of the system</Description>
        <Dependencies>
            <JarName>axis2-xml.jar</JarName>
            <JarName>axis2-wsdl.jar</JarName>
            <JarName>commons-logging.jar</JarName>
        </Dependencies>
    </axis2:Module>
</apache:Project>

        */
        final OMFactory fac = OMAbstractFactory.getOMFactory();
        final OMNamespace axis2NS = fac.createOMNamespace("http://ws.apache.org/namespaces/axis2", "axis2");


        class Dependencies implements ADBBean {

            String[] dependencies;

            public Dependencies(String[] dependencies) {
                this.dependencies = dependencies;
            }

            public XMLStreamReader getPullParser(QName adbBeanQName) {
                if (adbBeanQName == null) {
                    adbBeanQName = new QName("http://ws.apache.org/namespaces/axis2", "Dependencies", "axis2");
                }

                ArrayList properties = new ArrayList();
                properties.add("JarName");
                properties.add(dependencies);

                Object[] attrubutes = new Object[2];
                attrubutes[0] = new QName("mailto:myAttributes@axis2.org", "number", "myAttr");
                attrubutes[1] = new String(dependencies.length + "");

                return ADBPullParser.createPullParser(adbBeanQName, properties.toArray(), attrubutes);
            }
        }

        class Module implements ADBBean {

            String projectName;
            String description;
            Dependencies dependencyBean;

            public Module(String projectName, String description, Dependencies dependencyBean) {
                this.projectName = projectName;
                this.description = description;
                this.dependencyBean = dependencyBean;
            }

            public XMLStreamReader getPullParser(QName adbBeanQName) {
                if (adbBeanQName == null) {
                    adbBeanQName = new QName("http://ws.apache.org/namespaces/axis2", "Module", "axis2");
                }

                ArrayList properties = new ArrayList();
                properties.add("Description");
                properties.add(description);
                properties.add(new QName("Dependencies"));
                properties.add(dependencyBean);

                Object[] attrubutes = new Object[2];
                attrubutes[0] = new QName("http://ws.apache.org/namespaces/axis2", "name", "axis2");
                attrubutes[1] = projectName;

                return ADBPullParser.createPullParser(adbBeanQName, properties.toArray(), attrubutes);
            }
        }


        ArrayList propertyList = new ArrayList();
        propertyList.add("CurrentRelease");
        propertyList.add("0.92");

        Dependencies xmlModuleDeps = new Dependencies(new String[]{"stax-api.jar", "stax-impl.jar"});
        Module xmlModule = new Module("xml", "This is the XML object model for Axis2", xmlModuleDeps);
        propertyList.add(new QName("http://ws.apache.org/namespaces/axis2", "Module", "axis2"));
        propertyList.add(xmlModule);

        Dependencies coreModuleDeps = new Dependencies(new String[]{"axis2-xml.jar", "axis2-wsdl.jar", "commons-logging.jar"});
        Module coreModule = new Module("core", "This will handle the main logics of the system", coreModuleDeps);
        propertyList.add(new QName("http://ws.apache.org/namespaces/axis2", "Module", "axis2"));
        propertyList.add(coreModule);

        Object[]  attributes = new Object[]{new QName("mailto:myAttributes@axis2.org", "name", "myAttr"), "Apache Axis2"};

        XMLStreamReader pullParser = ADBPullParser.createPullParser(new QName("http://www.apache.org/", "Project", "apache"), propertyList.toArray(), attributes);
//        System.out.println(getStringXML(pullParser));

        // this still has some namespace problem. Need to fix this.

    }

    private String getStringXML(XMLStreamReader reader) {
        OMElement omelement = new StAXOMBuilder(reader).getDocumentElement();
        return omelement.toString();
    }

    public Document newDocument(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(xml.getBytes()));
    }
}
