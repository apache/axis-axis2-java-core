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
package org.apache.axis2.schema.attribute;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.StAXUtils;
import org.tempuri.attribute.*;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;

public class AttributeTest extends TestCase {

    public void testElement1(){

        TestElement1 testElement = new TestElement1();
        testElement.setAttribute1(1);

        OMElement omElement = testElement.getOMElement(TestElement1.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OMElement ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestElement1 result = TestElement1.Factory.parse(xmlReader);
            assertEquals(result.getAttribute1(),1);
        } catch (Exception e) {
            fail();
        }

        testElement = new TestElement1();
        testElement.setAttribute1(Integer.MIN_VALUE);

        omElement = testElement.getOMElement(TestElement1.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OMElement ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestElement1 result = TestElement1.Factory.parse(xmlReader);
            assertEquals(result.getAttribute1(),Integer.MIN_VALUE);
        } catch (Exception e) {
            fail();
        }
    }

    public void testElement2(){
        TestElement2 testElement = new TestElement2();
        testElement.setAttribute1(1);

        OMElement omElement = testElement.getOMElement(TestElement2.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OMElement ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestElement2 result = TestElement2.Factory.parse(xmlReader);
            assertEquals(result.getAttribute1(),1);
        } catch (Exception e) {
            fail();
        }

        testElement = new TestElement2();
        testElement.setAttribute1(Integer.MIN_VALUE);

        omElement = testElement.getOMElement(TestElement2.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OMElement ==> " + omElementString);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testElement3(){
        TestElement3 testElement = new TestElement3();
        testElement.setAttribute1("test");

        OMElement omElement = testElement.getOMElement(TestElement3.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OMElement ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestElement3 result = TestElement3.Factory.parse(xmlReader);
            assertEquals(result.getAttribute1(),"test");
        } catch (Exception e) {
            fail();
        }

        testElement = new TestElement3();

        omElement = testElement.getOMElement(TestElement3.MY_QNAME, OMAbstractFactory.getOMFactory());

        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OMElement ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestElement3 result = TestElement3.Factory.parse(xmlReader);
            assertEquals(result.getAttribute1(),null);
        } catch (Exception e) {
            fail();
        }
    }

     public void testElement4(){
        TestElement4 testElement = new TestElement4();
        testElement.setAttribute1("test");

        OMElement omElement = testElement.getOMElement(TestElement4.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OMElement ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestElement4 result = TestElement4.Factory.parse(xmlReader);
            assertEquals(result.getAttribute1(),"test");
        } catch (Exception e) {
            fail();
        }

        testElement = new TestElement4();

        omElement = testElement.getOMElement(TestElement4.MY_QNAME, OMAbstractFactory.getOMFactory());

        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OMElement ==> " + omElementString);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testAttributeSimpleType(){
        TestAttributeSimpleType testAttributeSimpleType = new TestAttributeSimpleType();
        Attribute1_type0 attribute1_type0 = new Attribute1_type0();
        attribute1_type0.setAttribute1_type0("test attribute");
        testAttributeSimpleType.setAttribute1(attribute1_type0);

        OMElement omElement = testAttributeSimpleType.getOMElement(TestAttributeSimpleType.MY_QNAME,
                OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OMString ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestAttributeSimpleType result = TestAttributeSimpleType.Factory.parse(xmlReader);
            assertEquals(result.getAttribute1().getAttribute1_type0(),"test attribute");
        } catch (Exception e) {
            fail();
        }
    }
}
