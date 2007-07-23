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
package org.apache.axis2.schema.anytype;

import junit.framework.TestCase;
import org.w3.www._2005._05.xmlmime.*;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.databinding.ADBException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;


public class AnyTypeTest extends TestCase {

    public void testAnyTypeElement1() {
        TestAnyTypeElement1 testAnyTypeElement;

        testAnyTypeElement = new TestAnyTypeElement1();
        testAnyTypeElement.setTestAnyTypeElement1("test");
        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement1.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement1 result = TestAnyTypeElement1.Factory.parse(xmlReader);
            assertEquals(result.getTestAnyTypeElement1(),"test");
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

        testAnyTypeElement = new TestAnyTypeElement1();
        testAnyTypeElement.setTestAnyTypeElement1(null);
        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement1.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            fail();
        } catch (ADBException e) {
            assertTrue(true);
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

    }

    public void testAnyTypeElement2() {
        TestAnyTypeElement2 testAnyTypeElement;

        testAnyTypeElement = new TestAnyTypeElement2();
        testAnyTypeElement.setTestAnyTypeElement2("test");
        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement2.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement2 result = TestAnyTypeElement2.Factory.parse(xmlReader);
            assertEquals(result.getTestAnyTypeElement2(),"test");
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

        testAnyTypeElement = new TestAnyTypeElement2();
        testAnyTypeElement.setTestAnyTypeElement2(null);
        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement1.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement2 result = TestAnyTypeElement2.Factory.parse(xmlReader);
            assertEquals(result.getTestAnyTypeElement2(),null);
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    public void testAnyTypeElement3() {
        TestAnyTypeElement3 testAnyTypeElement;
        testAnyTypeElement = new TestAnyTypeElement3();
        testAnyTypeElement.setParam1(new Object[]{"test1","test2"});

        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement3.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement3 result = TestAnyTypeElement3.Factory.parse(xmlReader);
            assertEquals(result.getParam1()[0],"test1");
            assertEquals(result.getParam1()[1],"test2");
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

        testAnyTypeElement = new TestAnyTypeElement3();
        testAnyTypeElement.setParam1(null);

        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement3.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement3 result = TestAnyTypeElement3.Factory.parse(xmlReader);
            assertEquals(result.getParam1()[0],null);
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

        testAnyTypeElement = new TestAnyTypeElement3();
        testAnyTypeElement.setParam1(new Object[]{"test",null});

        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement3.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement3 result = TestAnyTypeElement3.Factory.parse(xmlReader);
            assertEquals(result.getParam1()[0],"test");
            assertEquals(result.getParam1()[1],null);
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }


     public void testAnyTypeElement4() {
        TestAnyTypeElement4 testAnyTypeElement;
        testAnyTypeElement = new TestAnyTypeElement4();
        testAnyTypeElement.setParam1(new Object[]{"test1","test2"});

        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement4.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement4 result = TestAnyTypeElement4.Factory.parse(xmlReader);
            assertEquals(result.getParam1()[0],"test1");
            assertEquals(result.getParam1()[1],"test2");
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

        testAnyTypeElement = new TestAnyTypeElement4();
        testAnyTypeElement.setParam1(null);

        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement4.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement4 result = TestAnyTypeElement4.Factory.parse(xmlReader);
            assertEquals(result.getParam1(),null);
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

        testAnyTypeElement = new TestAnyTypeElement4();
        testAnyTypeElement.setParam1(new Object[]{"test",null});

        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement4.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement4 result = TestAnyTypeElement4.Factory.parse(xmlReader);
            assertEquals(result.getParam1()[0],"test");
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }


    public void testAnyTypeElement5() {
        TestAnyTypeElement5 testAnyTypeElement;
        testAnyTypeElement = new TestAnyTypeElement5();
        testAnyTypeElement.setParam1(new Object[]{"test1","test2"});

        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement5.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement5 result = TestAnyTypeElement5.Factory.parse(xmlReader);
            assertEquals(result.getParam1()[0],"test1");
            assertEquals(result.getParam1()[1],"test2");
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

        testAnyTypeElement = new TestAnyTypeElement5();
        testAnyTypeElement.setParam1(null);

        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement5.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement5 result = TestAnyTypeElement5.Factory.parse(xmlReader);
            assertEquals(result.getParam1()[0],null);
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

        testAnyTypeElement = new TestAnyTypeElement5();
        testAnyTypeElement.setParam1(new Object[]{"test",null});

        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement5.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement5 result = TestAnyTypeElement5.Factory.parse(xmlReader);
            assertEquals(result.getParam1()[0],"test");
            assertEquals(result.getParam1()[1],null);
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }


    public void testAnyTypeElement6() {
        TestAnyTypeElement6 testAnyTypeElement;
        testAnyTypeElement = new TestAnyTypeElement6();
        testAnyTypeElement.setParam1(new Object[]{"test1","test2"});

        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement6.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement6 result = TestAnyTypeElement6.Factory.parse(xmlReader);
            assertEquals(result.getParam1()[0],"test1");
            assertEquals(result.getParam1()[1],"test2");
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

        testAnyTypeElement = new TestAnyTypeElement6();
        testAnyTypeElement.setParam1(null);

        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement6.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            fail();
        } catch (ADBException e) {
            assertTrue(true);
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

        testAnyTypeElement = new TestAnyTypeElement6();
        testAnyTypeElement.setParam1(new Object[]{"test",null});

        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement6.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            fail();
        } catch (ADBException e) {
            assertTrue(true);
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    public void testAnyTypeElement7() {
        TestAnyTypeElement7 testAnyTypeElement;

        testAnyTypeElement = new TestAnyTypeElement7();
        testAnyTypeElement.setParam1("test");
        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement7.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement7 result = TestAnyTypeElement7.Factory.parse(xmlReader);
            assertEquals(result.getParam1(),"test");
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

        testAnyTypeElement = new TestAnyTypeElement7();
        testAnyTypeElement.setParam1(null);
        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement7.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement7 result = TestAnyTypeElement7.Factory.parse(xmlReader);
            assertEquals(result.getParam1(),null);
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    public void testAnyTypeElement8() {
        TestAnyTypeElement8 testAnyTypeElement;

        testAnyTypeElement = new TestAnyTypeElement8();
        testAnyTypeElement.setParam1("test");
        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement8.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement8 result = TestAnyTypeElement8.Factory.parse(xmlReader);
            assertEquals(result.getParam1(),"test");
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

        testAnyTypeElement = new TestAnyTypeElement8();
        testAnyTypeElement.setParam1(null);
        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement8.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement8 result = TestAnyTypeElement8.Factory.parse(xmlReader);
            assertEquals(result.getParam1(),null);
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    public void testAnyTypeElement9() {
        TestAnyTypeElement9 testAnyTypeElement;

        testAnyTypeElement = new TestAnyTypeElement9();
        testAnyTypeElement.setParam1("test");
        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement9.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement9 result = TestAnyTypeElement9.Factory.parse(xmlReader);
            assertEquals(result.getParam1(),"test");
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

        testAnyTypeElement = new TestAnyTypeElement9();
        testAnyTypeElement.setParam1(null);
        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement9.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement9 result = TestAnyTypeElement9.Factory.parse(xmlReader);
            assertEquals(result.getParam1(),null);
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    public void testAnyTypeElement10() {
        TestAnyTypeElement10 testAnyTypeElement;

        testAnyTypeElement = new TestAnyTypeElement10();
        testAnyTypeElement.setParam1("test");
        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement10.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement10 result = TestAnyTypeElement10.Factory.parse(xmlReader);
            assertEquals(result.getParam1(),"test");
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

        testAnyTypeElement = new TestAnyTypeElement10();
        testAnyTypeElement.setParam1(null);
        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement10.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            fail();
        } catch (ADBException e) {
            assertTrue(true);
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    public void testAnyElementInteger(){
        // datatype tests
        TestAnyTypeElement1 testAnyTypeElement;
        testAnyTypeElement = new TestAnyTypeElement1();
        testAnyTypeElement.setTestAnyTypeElement1(new Integer(5));
        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement1.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement1 result = TestAnyTypeElement1.Factory.parse(xmlReader);
            assertEquals(result.getTestAnyTypeElement1(),new Integer(5));
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }

    public void testAnyElementQName(){
        // datatype tests
        TestAnyTypeElement1 testAnyTypeElement;
        testAnyTypeElement = new TestAnyTypeElement1();
        testAnyTypeElement.setTestAnyTypeElement1(new QName("http://wso2.org","testElement"));
        try {
            OMElement omElement = testAnyTypeElement.getOMElement(TestAnyTypeElement1.MY_QNAME,
                    OMAbstractFactory.getOMFactory());
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM Element String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAnyTypeElement1 result = TestAnyTypeElement1.Factory.parse(xmlReader);
            assertEquals(result.getTestAnyTypeElement1(),new QName("http://wso2.org","testElement"));
        } catch (ADBException e) {
            fail();
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }
    }
}

