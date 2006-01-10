/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
 */

package org.apache.axis2.databinding;

import org.apache.axis2.databinding.utils.ADBPullParser;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ADBSOAPModelBuilderTest extends XMLTestCase {
    public void testSimpleArrayList() throws Exception {
        String expectedXML = "<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header /><soapenv:Body><Person xmlns=\"\"><Name xmlns=\"\">FooOne</Name><DependentOne xmlns=\"\"><Name xmlns=\"\">FooTwo</Name><Age xmlns=\"\">25</Age><Sex xmlns=\"\">Male</Sex></DependentOne><DependentTwo xmlns=\"\"><Name xmlns=\"\">FooTwo</Name><Age xmlns=\"\">25</Age><Sex xmlns=\"\">Male</Sex></DependentTwo><Organization xmlns=\"\">Apache</Organization></Person></soapenv:Body></soapenv:Envelope>";
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
        ADBSOAPModelBuilder builder = new ADBSOAPModelBuilder(pullParser, OMAbstractFactory.getSOAP11Factory());

        OMElement root = builder.getDocumentElement();
        assertTrue("Root element can not be null", root != null);
        Document expectedDOM = newDocument(expectedXML);
        Document actualDom = newDocument(root.toString());
        assertXMLEqual(actualDom, expectedDOM);
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

    public Document newDocument(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(xml.getBytes()));
    }
}
