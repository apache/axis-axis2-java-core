package org.apache.axis2.databinding.utils;

import junit.framework.TestCase;
import org.apache.axis2.databinding.ADBBean;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
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

public class ADBPullParserTest extends TestCase {

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
//            while (pullParser.hasNext()) {
//                int eventCode = pullParser.next();
//                System.out.println(eventCode + ":" + getEventString(eventCode));
//            }

            StringBuffer buff = new StringBuffer();
            while (pullParser.hasNext()) {
                int eventCode = pullParser.next();

                switch (eventCode) {
                    case XMLStreamConstants.START_ELEMENT :
                        buff.append("<");
                        buff.append(pullParser.getLocalName());
                        buff.append(">");
                        break;
                    case XMLStreamConstants.CHARACTERS :
                        buff.append(pullParser.getText());
                        break;
                    case XMLStreamConstants.END_ELEMENT :
                        buff.append("</");
                        buff.append(pullParser.getLocalName());
                        buff.append(">");
                        break;
                    default:
                        System.out.println("No Other event can be trown here");
                }
            }


            assertEquals(exptectedXML, buff.toString());
        } catch (XMLStreamException e) {
            log.error("Parser Error " + e);
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
                    "<Age>25</Age><Sex>Male</Sex></Depemdent></Depemdent></Dependent><Dependent>" +
                    "<Name>FooTwo</Name><Age>25</Age><Sex>Male</Sex><Depemdent><Name>FooTwo</Name>" +
                    "<Age>25</Age><Sex>Male</Sex></Depemdent></Dependent></Person>";


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
            XMLStreamReader pullParser = ADBPullParser.createPullParser(projectQName, propertyList.toArray(), null, true);

            StringBuffer buff = new StringBuffer();
            while (pullParser.hasNext()) {
                int eventCode = pullParser.next();

                switch (eventCode) {
                    case XMLStreamConstants.START_ELEMENT :
                        buff.append("<");
                        buff.append(pullParser.getLocalName());
                        buff.append(">");
                        break;
                    case XMLStreamConstants.CHARACTERS :
                        buff.append(pullParser.getText());
                        break;
                    case XMLStreamConstants.END_ELEMENT :
                        buff.append("</");
                        buff.append(pullParser.getLocalName());
                        buff.append(">");
                        break;
                    default:
                        System.out.println("No Other event can be trown here");
                }
            }


            assertEquals(exptectedXML, buff.toString());
        } catch (XMLStreamException e) {
            log.error("Parser Error " + e);
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
//        OMNamespace attrNS = factory.createOMNamespace("mailto:whoever@whatever.com", "attr");
        OMAttribute[] attribute = new OMAttribute[5];

        for (int i = 0; i < 5; i++) {
            attribute[i] = factory.createOMAttribute("Attr" + (i + 1), null, "Value " + (i + 1));
        }

        String stringXML = getStringXML(ADBPullParser.createPullParser(elementQName, null, attribute, true));
        assertEquals(stringXML, expectedXML);

    }

    private String getStringXML(XMLStreamReader reader) {
        OMElement omelement = new StAXOMBuilder(reader).getDocumentElement();
        return omelement.toString();
    }
}
