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
package org.apache.axis2.schema.innerparticles;

import com.mynamespace.testinnerparticle.*;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;


public class InnerParticlesTest extends TestCase {

    public void testInnerParticle1() {

        TestInnerParticle1 testInnerParticle1 = new TestInnerParticle1();
        testInnerParticle1.setParam1("Param1");
        TestInnerParticle1Sequence_type0 testInnerParticle1Sequence_type0 = new TestInnerParticle1Sequence_type0();
        testInnerParticle1Sequence_type0.setParam2("Param2");
        testInnerParticle1Sequence_type0.setParam3("Param3");
        testInnerParticle1.setTestInnerParticle1Sequence_type0(testInnerParticle1Sequence_type0);
        testInnerParticle1.setParam4("Param4");

        OMElement omElement = testInnerParticle1.getOMElement(TestInnerParticle1.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestInnerParticle1 result = TestInnerParticle1.Factory.parse(xmlReader);
            assertEquals(result.getParam1(), "Param1");
            assertEquals(result.getTestInnerParticle1Sequence_type0().getParam2(), "Param2");
            assertEquals(result.getTestInnerParticle1Sequence_type0().getParam3(), "Param3");
            assertEquals(result.getParam4(), "Param4");
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

    }

    public void testInnerParticle2() {

        TestInnerParticle2 testInnerParticle2 = new TestInnerParticle2();
        testInnerParticle2.setParam1("Param1");
        TestInnerParticle2Choice_type5 testInnerParticle2Choice_type1 = new TestInnerParticle2Choice_type5();
        testInnerParticle2Choice_type1.setParam2("Param2");
        testInnerParticle2Choice_type1.setParam3("Param3");
        testInnerParticle2.setTestInnerParticle2Choice_type5(testInnerParticle2Choice_type1);
        testInnerParticle2.setParam4("Param4");

        OMElement omElement =
                testInnerParticle2.getOMElement(TestInnerParticle2.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestInnerParticle2 result = TestInnerParticle2.Factory.parse(xmlReader);
            assertEquals(result.getParam1(), "Param1");
            assertEquals(result.getTestInnerParticle2Choice_type5().getParam3(), "Param3");
            assertEquals(result.getParam4(), "Param4");
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

    }

    public void testInnerParticle31() {

        TestInnerParticle3 testInnerParticle3 = new TestInnerParticle3();
        testInnerParticle3.setParam1("Param1");

        OMElement omElement =
                testInnerParticle3.getOMElement(TestInnerParticle3.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestInnerParticle3 result = TestInnerParticle3.Factory.parse(xmlReader);
            assertEquals(result.getParam1(), "Param1");
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

    }

    public void testInnerParticle32() {

        TestInnerParticle3 testInnerParticle3 = new TestInnerParticle3();
        TestInnerParticle3Choice_type1 testInnerParticle3Choice_type1 = new TestInnerParticle3Choice_type1();
        testInnerParticle3Choice_type1.setParam2("Param2");
        testInnerParticle3Choice_type1.setParam3("Param3");
        testInnerParticle3.setTestInnerParticle3Choice_type1(testInnerParticle3Choice_type1);

        OMElement omElement =
                testInnerParticle3.getOMElement(TestInnerParticle3.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestInnerParticle3 result = TestInnerParticle3.Factory.parse(xmlReader);
            assertEquals(result.getTestInnerParticle3Choice_type1().getParam3(), "Param3");
        } catch (XMLStreamException e) {
            e.printStackTrace();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }

    public void testInnerParticle33() {

        TestInnerParticle3 testInnerParticle3 = new TestInnerParticle3();
        testInnerParticle3.setParam1("Param1");
        TestInnerParticle3Choice_type1 testInnerParticle3Choice_type1 = new TestInnerParticle3Choice_type1();
        testInnerParticle3Choice_type1.setParam2("Param2");
        testInnerParticle3Choice_type1.setParam3("Param3");
        testInnerParticle3.setTestInnerParticle3Choice_type1(testInnerParticle3Choice_type1);
        testInnerParticle3.setParam4("Param4");

        OMElement omElement =
                testInnerParticle3.getOMElement(TestInnerParticle3.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestInnerParticle3 result = TestInnerParticle3.Factory.parse(xmlReader);
            assertEquals(result.getParam4(), "Param4");
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

    }

    public void testInnerParticle41() {

        TestInnerParticle4 testInnerParticle4 = new TestInnerParticle4();
        testInnerParticle4.setParam1("Param1");

        OMElement omElement =
                testInnerParticle4.getOMElement(TestInnerParticle4.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestInnerParticle4 result = TestInnerParticle4.Factory.parse(xmlReader);
            assertEquals(result.getParam1(), "Param1");
        } catch (XMLStreamException e) {
            e.printStackTrace();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }

    public void testInnerParticle42() {

        TestInnerParticle4 testInnerParticle4 = new TestInnerParticle4();
        TestInnerParticle4Sequence_type2 testInnerParticle4Sequence_type2 = new TestInnerParticle4Sequence_type2();
        testInnerParticle4Sequence_type2.setParam2("Param2");
        testInnerParticle4Sequence_type2.setParam3("Param3");
        testInnerParticle4.setTestInnerParticle4Sequence_type2(testInnerParticle4Sequence_type2);

        OMElement omElement =
                testInnerParticle4.getOMElement(TestInnerParticle4.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestInnerParticle4 result = TestInnerParticle4.Factory.parse(xmlReader);
            assertEquals(result.getTestInnerParticle4Sequence_type2().getParam2(), "Param2");
            assertEquals(result.getTestInnerParticle4Sequence_type2().getParam3(), "Param3");
        } catch (XMLStreamException e) {
            e.printStackTrace();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }

    public void testInnerParticle43() {

        TestInnerParticle4 testInnerParticle4 = new TestInnerParticle4();
        testInnerParticle4.setParam4("Param4");

        OMElement omElement =
                testInnerParticle4.getOMElement(TestInnerParticle4.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestInnerParticle4 result = TestInnerParticle4.Factory.parse(xmlReader);
            assertEquals(result.getParam4(), "Param4");
        } catch (XMLStreamException e) {
            e.printStackTrace();
            fail();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }

    public void testInnerParticle5() {

        TestInnerParticle5 testInnerParticle5 = new TestInnerParticle5();
        testInnerParticle5.setParam1("Param1");
        TestInnerParticle5Sequence_type4 testInnerParticle2Choice_type1 = new TestInnerParticle5Sequence_type4();
        testInnerParticle2Choice_type1.setParam2("Param2");
        testInnerParticle2Choice_type1.setParam3("Param3");

        TestInnerParticle5Sequence_type3 testInnerParticle5Sequence_type3 = new TestInnerParticle5Sequence_type3();
        testInnerParticle5Sequence_type3.setParam4("Param4");
        testInnerParticle5Sequence_type3.setParam5("Param5");

        testInnerParticle2Choice_type1.setTestInnerParticle5Sequence_type3(testInnerParticle5Sequence_type3);
        testInnerParticle5.setTestInnerParticle5Sequence_type4(testInnerParticle2Choice_type1);
        testInnerParticle5.setParam6("Param6");

        OMElement omElement =
                testInnerParticle5.getOMElement(TestInnerParticle5.MY_QNAME, OMAbstractFactory.getOMFactory());
        try {
            String omElementString = omElement.toStringWithConsume();
            System.out.println("OM String ==> " + omElementString);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(omElementString.getBytes()));
            TestInnerParticle5 result = TestInnerParticle5.Factory.parse(xmlReader);
            assertEquals(result.getParam1(), "Param1");
            assertEquals(result.getTestInnerParticle5Sequence_type4().getParam2(), "Param2");
            assertEquals(result.getTestInnerParticle5Sequence_type4().getParam3(), "Param3");
            assertEquals(result.getTestInnerParticle5Sequence_type4().getTestInnerParticle5Sequence_type3().getParam4(), "Param4");
            assertEquals(result.getTestInnerParticle5Sequence_type4().getTestInnerParticle5Sequence_type3().getParam5(), "Param5");
            assertEquals(result.getParam6(), "Param6");
        } catch (XMLStreamException e) {
            fail();
        } catch (Exception e) {
            fail();
        }

    }


}
