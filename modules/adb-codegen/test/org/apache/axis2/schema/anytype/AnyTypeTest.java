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

package org.apache.axis2.schema.anytype;

import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.schema.AbstractTestCase;

import test.adb.anytype.*;

import javax.xml.namespace.QName;
import java.io.StringReader;

public class AnyTypeTest extends AbstractTestCase {

    public void testAnyTypeElement1() throws Exception {
        TestAnyTypeElement1 testAnyTypeElement;

        testAnyTypeElement = new TestAnyTypeElement1();
        testAnyTypeElement.setTestAnyTypeElement1("test");
        testSerializeDeserialize(testAnyTypeElement);

        testAnyTypeElement = new TestAnyTypeElement1();
        testAnyTypeElement.setTestAnyTypeElement1(null);
        
        assertSerializationFailure(testAnyTypeElement);
    }

    public void testAnyTypeElement2() throws Exception {
        TestAnyTypeElement2 testAnyTypeElement;

        testAnyTypeElement = new TestAnyTypeElement2();
        testAnyTypeElement.setTestAnyTypeElement2("test");
        testSerializeDeserialize(testAnyTypeElement);

        testAnyTypeElement = new TestAnyTypeElement2();
        testAnyTypeElement.setTestAnyTypeElement2(null);
        testSerializeDeserialize(testAnyTypeElement);
    }

    public void testAnyTypeElement3() throws Exception {
        TestAnyTypeElement3 testAnyTypeElement;
        testAnyTypeElement = new TestAnyTypeElement3();
        testAnyTypeElement.setParam1(new Object[]{"test1","test2"});

        testSerializeDeserialize(testAnyTypeElement);

        testAnyTypeElement = new TestAnyTypeElement3();
        testAnyTypeElement.setParam1(null);

        TestAnyTypeElement3 result = (TestAnyTypeElement3)serializeDeserialize(testAnyTypeElement);
        assertEquals(result.getParam1()[0],null);

        testAnyTypeElement = new TestAnyTypeElement3();
        testAnyTypeElement.setParam1(new Object[]{"test",null});

        testSerializeDeserialize(testAnyTypeElement);
    }


     public void testAnyTypeElement4() throws Exception {
        TestAnyTypeElement4 testAnyTypeElement;
        testAnyTypeElement = new TestAnyTypeElement4();
        testAnyTypeElement.setParam1(new Object[]{"test1","test2"});
        testSerializeDeserialize(testAnyTypeElement);

        testAnyTypeElement = new TestAnyTypeElement4();
        testAnyTypeElement.setParam1(null);
        testSerializeDeserialize(testAnyTypeElement);

        testAnyTypeElement = new TestAnyTypeElement4();
        testAnyTypeElement.setParam1(new Object[]{"test",null});

        TestAnyTypeElement4 result = (TestAnyTypeElement4)serializeDeserialize(testAnyTypeElement);
        assertEquals(result.getParam1()[0],"test");
    }


    public void testAnyTypeElement5() throws Exception {
        TestAnyTypeElement5 testAnyTypeElement;
        testAnyTypeElement = new TestAnyTypeElement5();
        testAnyTypeElement.setParam1(new Object[]{"test1","test2"});
        testSerializeDeserialize(testAnyTypeElement);

        testAnyTypeElement = new TestAnyTypeElement5();
        testAnyTypeElement.setParam1(null);

        TestAnyTypeElement5 result = (TestAnyTypeElement5)serializeDeserialize(testAnyTypeElement);
        assertEquals(result.getParam1()[0],null);

        testAnyTypeElement = new TestAnyTypeElement5();
        testAnyTypeElement.setParam1(new Object[]{"test",null});
        testSerializeDeserialize(testAnyTypeElement);
    }


    public void testAnyTypeElement6() throws Exception {
        TestAnyTypeElement6 testAnyTypeElement;
        testAnyTypeElement = new TestAnyTypeElement6();
        testAnyTypeElement.setParam1(new Object[]{"test1","test2"});
        testSerializeDeserialize(testAnyTypeElement);

        testAnyTypeElement = new TestAnyTypeElement6();
        testAnyTypeElement.setParam1(null);
        assertSerializationFailure(testAnyTypeElement);

        testAnyTypeElement = new TestAnyTypeElement6();
        testAnyTypeElement.setParam1(new Object[]{"test",null});
        assertSerializationFailure(testAnyTypeElement);
    }

    public void testAnyTypeElement61() throws Exception {
        TestAnyTypeElement6 testAnyTypeElement6 = new TestAnyTypeElement6();

        TestComplexParent[] testComplexParents = new TestComplexParent[2];
        testComplexParents[0] = new TestComplexParent();
        testComplexParents[0].setParam1("test param1");

        TestComplexChild testComplexChild = new TestComplexChild();
        testComplexChild.setParam1("test param1");
        testComplexChild.setParam2(3);
        testComplexParents[1] = testComplexChild;

        testAnyTypeElement6.setParam1(testComplexParents);

        testSerializeDeserialize(testAnyTypeElement6);
    }

    public void testAnyTypeElement7() throws Exception {
        TestAnyTypeElement7 testAnyTypeElement;

        testAnyTypeElement = new TestAnyTypeElement7();
        testAnyTypeElement.setParam1("test");
        testSerializeDeserialize(testAnyTypeElement);

        testAnyTypeElement = new TestAnyTypeElement7();
        testAnyTypeElement.setParam1(null);
        testSerializeDeserialize(testAnyTypeElement);
    }

    public void testAnyTypeElement71() throws Exception {
        TestAnyTypeElement7 testAnyTypeElement7 = new TestAnyTypeElement7();
        TestComplexParent testComplexParent = new TestComplexParent();
        testComplexParent.setParam1("test param1");
        testAnyTypeElement7.setParam1(testComplexParent);
        testSerializeDeserialize(testAnyTypeElement7);
    }

    public void testAnyTypeElement8() throws Exception {
        TestAnyTypeElement8 testAnyTypeElement;

        testAnyTypeElement = new TestAnyTypeElement8();
        testAnyTypeElement.setParam1("test");
        testSerializeDeserialize(testAnyTypeElement);

        testAnyTypeElement = new TestAnyTypeElement8();
        testAnyTypeElement.setParam1(null);
        testSerializeDeserialize(testAnyTypeElement);
    }

    public void testAnyTypeElement9() throws Exception {
        TestAnyTypeElement9 testAnyTypeElement;

        testAnyTypeElement = new TestAnyTypeElement9();
        testAnyTypeElement.setParam1("test");
        testSerializeDeserialize(testAnyTypeElement);

        testAnyTypeElement = new TestAnyTypeElement9();
        testAnyTypeElement.setParam1(null);
        testSerializeDeserialize(testAnyTypeElement);
    }

    public void testAnyTypeElement10() throws Exception {
        TestAnyTypeElement10 testAnyTypeElement;

        testAnyTypeElement = new TestAnyTypeElement10();
        testAnyTypeElement.setParam1("test");
        testSerializeDeserialize(testAnyTypeElement);

        testAnyTypeElement = new TestAnyTypeElement10();
        testAnyTypeElement.setParam1(null);
        assertSerializationFailure(testAnyTypeElement);
    }

    public void testAnyElementInteger() throws Exception {
        // datatype tests
        TestAnyTypeElement1 testAnyTypeElement;
        testAnyTypeElement = new TestAnyTypeElement1();
        testAnyTypeElement.setTestAnyTypeElement1(new Integer(5));
        testSerializeDeserialize(testAnyTypeElement);
    }

    public void testAnyElementQName() throws Exception {
        // datatype tests
        TestAnyTypeElement1 testAnyTypeElement;
        testAnyTypeElement = new TestAnyTypeElement1();
        testAnyTypeElement.setTestAnyTypeElement1(new QName("http://wso2.org","testElement"));
        testSerializeDeserialize(testAnyTypeElement);
    }

    public void testTestElement() throws Exception {
        TestElement testElement = new TestElement();

        DynamicProperty[] dynamicProperties = new DynamicProperty[3];
        TestComplexParent testComplexParent = null;

        dynamicProperties[0] = new DynamicProperty();
        dynamicProperties[0].setName("test name");
        dynamicProperties[0].setVal(new Integer(5));

        dynamicProperties[1] = new DynamicProperty();
        dynamicProperties[1].setName("test name");
        testComplexParent = new TestComplexParent();
        testComplexParent.setParam1("test complext type");
        dynamicProperties[1].setVal(testComplexParent);

        TestSimpleType testSimpleType = new TestSimpleType();
        testSimpleType.setTestSimpleType("test simple string");
        dynamicProperties[2] = new DynamicProperty();
        dynamicProperties[2].setName("test name");
        dynamicProperties[2].setVal(testSimpleType);


        testElement.setParam1(dynamicProperties);

        testSerializeDeserialize(testElement);
    }
    
    // Regression test for AXIS2-4273
    public void testTestMixedWithNilledAnyType() throws Exception {
        TestMixed test = TestMixed.Factory.parse(StAXUtils.createXMLStreamReader(new StringReader(
                "<TestMixed xmlns='http://adb.test/anyType' " +
                "           xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
                "  <param1 xsi:nil='true'/>" +
                "  <param2>test</param2>" +
                "</TestMixed>")));
        assertNull(test.getParam1());
        assertEquals("test", test.getParam2());
    }
}

