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

package org.apache.axis2.schema.particalmaxoccurs;

import com.mynamespace.testparticlemaxoccurs.*;
import org.apache.axis2.schema.AbstractTestCase;

public class ParticalMaxOccursTest extends AbstractTestCase {

    private int i = 0;

    public void testParticalSequenceMaxOccursTest1() throws Exception {
        TestParticalSequenceMaxOccurs1 testParticalMaxOccures1 = new TestParticalSequenceMaxOccurs1();
        TestParticalSequenceMaxOccursType1 testParticalMaxOccursType1 = new TestParticalSequenceMaxOccursType1();
        testParticalMaxOccures1.setTestParticalSequenceMaxOccurs1(testParticalMaxOccursType1);

        TestParticalSequenceMaxOccursType1Sequence[] testSequences = new TestParticalSequenceMaxOccursType1Sequence[2];

        testSequences[0] = new TestParticalSequenceMaxOccursType1Sequence();
        testSequences[0].setParm1("Param11");
        testSequences[0].setParm2("Param12");

        testSequences[1] = new TestParticalSequenceMaxOccursType1Sequence();
        testSequences[1].setParm1("Param21");
        testSequences[1].setParm2("Param22");

        testParticalMaxOccursType1.setTestParticalSequenceMaxOccursType1Sequence(testSequences);

        testSerializeDeserialize(testParticalMaxOccures1);
    }

    public void testParticleSequenceMaxOccurs2() throws Exception {
        TestParticalSequenceMaxOccurs2 testParticalMaxOccures2 = new TestParticalSequenceMaxOccurs2();
        TestParticalSequenceMaxOccursType2 testParticalMaxOccursType2 = new TestParticalSequenceMaxOccursType2();
        testParticalMaxOccures2.setTestParticalSequenceMaxOccurs2(testParticalMaxOccursType2);

        TestParticalSequenceMaxOccursType2Sequence[] testSequences = new TestParticalSequenceMaxOccursType2Sequence[2];

        testSequences[0] = new TestParticalSequenceMaxOccursType2Sequence();
        testSequences[0].setParm1(new String[]{"Param111", "Param112", "Param113"});
        testSequences[0].setParm2(new String[]{"Param111", "Param112", "Param113"});

        testSequences[1] = new TestParticalSequenceMaxOccursType2Sequence();
        testSequences[1].setParm1(new String[]{"Param121", "Param122", "Param123"});
        testSequences[1].setParm2(new String[]{"Param121", "Param122", "Param123"});

        testParticalMaxOccursType2.setTestParticalSequenceMaxOccursType2Sequence(testSequences);

        testSerializeDeserialize(testParticalMaxOccures2);
    }

    public void testParticleSequenceMaxOccurs3() throws Exception {
        TestParticalSequenceMaxOccurs3 testParticalMaxOccures3 = new TestParticalSequenceMaxOccurs3();
        TestParticalSequenceMaxOccursType3 testParticalMaxOccursType3 = new TestParticalSequenceMaxOccursType3();
        testParticalMaxOccures3.setTestParticalSequenceMaxOccurs3(testParticalMaxOccursType3);

        TestParticalSequenceMaxOccursType3Sequence[] testSequences = new TestParticalSequenceMaxOccursType3Sequence[2];

        testSequences[0] = new TestParticalSequenceMaxOccursType3Sequence();
        testSequences[0].setParm2(new String[]{"Param111", null, "Param113"});

        testSequences[1] = new TestParticalSequenceMaxOccursType3Sequence();
        testSequences[1].setParm1(new String[]{"Param121", "Param122", null});

        testParticalMaxOccursType3.setTestParticalSequenceMaxOccursType3Sequence(testSequences);

        TestParticalSequenceMaxOccurs3 result = (TestParticalSequenceMaxOccurs3)serializeDeserialize(testParticalMaxOccures3);
        TestParticalSequenceMaxOccursType3Sequence[] resultSequences =
                result.getTestParticalSequenceMaxOccurs3().getTestParticalSequenceMaxOccursType3Sequence();
        assertEquals(resultSequences[0].getParm2()[0], "Param111");
        assertEquals(resultSequences[0].getParm2()[1], null);
        assertEquals(resultSequences[0].getParm2()[2], "Param113");
        assertEquals(resultSequences[1].getParm1()[0], "Param121");
        assertEquals(resultSequences[1].getParm1()[1], "Param122");
        assertEquals(resultSequences[1].getParm1()[2], null);
    }

    public void testParticleSequenceMaxOccurs4() throws Exception {

        TestParticalSequenceMaxOccurs4 testParticalSequenceMaxOccurs4 = new TestParticalSequenceMaxOccurs4();
        TestParticalSequenceMaxOccursType4 testParticalSequenceMaxOccursType4 = new TestParticalSequenceMaxOccursType4();
        testParticalSequenceMaxOccurs4.setTestParticalSequenceMaxOccurs4(testParticalSequenceMaxOccursType4);

        TestParticalSequenceMaxOccursType4Sequence[] testParticalSequenceMaxOccursType4Sequence =
                new TestParticalSequenceMaxOccursType4Sequence[3];

        testParticalSequenceMaxOccursType4.setTestParticalSequenceMaxOccursType4Sequence(testParticalSequenceMaxOccursType4Sequence);
        testParticalSequenceMaxOccursType4Sequence[0] = new TestParticalSequenceMaxOccursType4Sequence();
        testParticalSequenceMaxOccursType4Sequence[0].setParm1(getNewCustomType());

        testParticalSequenceMaxOccursType4Sequence[1] = new TestParticalSequenceMaxOccursType4Sequence();
        testParticalSequenceMaxOccursType4Sequence[1].setParm2("Param2");

        testParticalSequenceMaxOccursType4Sequence[2] = new TestParticalSequenceMaxOccursType4Sequence();
        testParticalSequenceMaxOccursType4Sequence[2].setParm3(getNewCustomType());

        testSerializeDeserialize(testParticalSequenceMaxOccurs4);
    }

    public void testParticleSequenceMaxOccurs5() throws Exception {

        TestParticalSequenceMaxOccurs5 testParticalSequenceMaxOccurs5 = new TestParticalSequenceMaxOccurs5();
        TestParticalSequenceMaxOccursType5 testParticalSequenceMaxOccursType5 = new TestParticalSequenceMaxOccursType5();
        testParticalSequenceMaxOccurs5.setTestParticalSequenceMaxOccurs5(testParticalSequenceMaxOccursType5);

        TestParticalSequenceMaxOccursType5Sequence[] testParticalSequenceMaxOccursType5Sequence =
                new TestParticalSequenceMaxOccursType5Sequence[3];

        testParticalSequenceMaxOccursType5.setTestParticalSequenceMaxOccursType5Sequence(testParticalSequenceMaxOccursType5Sequence);

        testParticalSequenceMaxOccursType5Sequence[0] = new TestParticalSequenceMaxOccursType5Sequence();
        testParticalSequenceMaxOccursType5Sequence[0].setParm1(new TestCustomType[]{getNewCustomType()});
        testParticalSequenceMaxOccursType5Sequence[0].setParm3(new TestCustomType[]{getNewCustomType()});

        testParticalSequenceMaxOccursType5Sequence[1] = new TestParticalSequenceMaxOccursType5Sequence();
        testParticalSequenceMaxOccursType5Sequence[1].setParm1(new TestCustomType[]{getNewCustomType()});
        testParticalSequenceMaxOccursType5Sequence[1].setParm2("Param2");
        testParticalSequenceMaxOccursType5Sequence[1].setParm3(new TestCustomType[]{getNewCustomType()});

        testParticalSequenceMaxOccursType5Sequence[2] = new TestParticalSequenceMaxOccursType5Sequence();
        testParticalSequenceMaxOccursType5Sequence[2].setParm1(new TestCustomType[]{getNewCustomType()});
        testParticalSequenceMaxOccursType5Sequence[2].setParm3(new TestCustomType[]{getNewCustomType()});

        testSerializeDeserialize(testParticalSequenceMaxOccurs5);
    }

    public void testParticalSequenceMaxOccursTest6() throws Exception {
        TestParticalSequenceMaxOccurs6 testParticalMaxOccures6 = new TestParticalSequenceMaxOccurs6();
        TestParticalSequenceMaxOccursType6 testParticalMaxOccursType6 = new TestParticalSequenceMaxOccursType6();
        testParticalMaxOccures6.setTestParticalSequenceMaxOccurs6(testParticalMaxOccursType6);

        TestParticalSequenceMaxOccursType6Sequence[] testSequences = new TestParticalSequenceMaxOccursType6Sequence[2];

        testSequences[0] = new TestParticalSequenceMaxOccursType6Sequence();
        testSequences[0].setParm1("Param11");
        testSequences[0].setParm2("Param12");

        testSequences[1] = new TestParticalSequenceMaxOccursType6Sequence();
        testSequences[1].setParm1("Param21");
        testSequences[1].setParm2("Param22");

        testParticalMaxOccursType6.setTestParticalSequenceMaxOccursType6Sequence(testSequences);
        testParticalMaxOccursType6.setAttribute1("Attribute1");
        testParticalMaxOccursType6.setAttribute2("Attribute2");

        testSerializeDeserialize(testParticalMaxOccures6);
    }

    public void testParticalMaxOccursTest() throws Exception {
        TestParticalChoiceMaxOccurs testParticalChoiceMaxOccurs = new TestParticalChoiceMaxOccurs();
        TestParticalChoiceMaxOccursType testParticalChoiceMaxOccursType = new TestParticalChoiceMaxOccursType();
        testParticalChoiceMaxOccurs.setTestParticalChoiceMaxOccurs(testParticalChoiceMaxOccursType);

        testParticalChoiceMaxOccursType.setParm1("Param1");

        testSerializeDeserialize(testParticalChoiceMaxOccurs);
    }

    public void testParticalChoiceMaxOccursTest1() throws Exception {
        TestParticalChoiceMaxOccurs1 testParticalMaxOccures1 = new TestParticalChoiceMaxOccurs1();
        TestParticalChoiceMaxOccursType1 testParticalMaxOccursType1 = new TestParticalChoiceMaxOccursType1();
        testParticalMaxOccures1.setTestParticalChoiceMaxOccurs1(testParticalMaxOccursType1);

        TestParticalChoiceMaxOccursType1Choice[] testChoices = new TestParticalChoiceMaxOccursType1Choice[2];

        testChoices[0] = new TestParticalChoiceMaxOccursType1Choice();
        testChoices[0].setParm1("Param11");

        testChoices[1] = new TestParticalChoiceMaxOccursType1Choice();
        testChoices[1].setParm2("Param12");

        testParticalMaxOccursType1.setTestParticalChoiceMaxOccursType1Choice(testChoices);

        testSerializeDeserialize(testParticalMaxOccures1);
    }

    public void testParticleChoiceMaxOccurs2() throws Exception {
        TestParticalChoiceMaxOccurs2 testParticalMaxOccures2 = new TestParticalChoiceMaxOccurs2();
        TestParticalChoiceMaxOccursType2 testParticalMaxOccursType2 = new TestParticalChoiceMaxOccursType2();
        testParticalMaxOccures2.setTestParticalChoiceMaxOccurs2(testParticalMaxOccursType2);

        TestParticalChoiceMaxOccursType2Choice[] testChoices = new TestParticalChoiceMaxOccursType2Choice[2];

        testChoices[0] = new TestParticalChoiceMaxOccursType2Choice();
        testChoices[0].setParm1(new String[]{"Param111", "Param112", "Param113"});

        testChoices[1] = new TestParticalChoiceMaxOccursType2Choice();
        testChoices[1].setParm2(new String[]{"Param121", "Param122", "Param123"});

        testParticalMaxOccursType2.setTestParticalChoiceMaxOccursType2Choice(testChoices);

        testSerializeDeserialize(testParticalMaxOccures2);
    }

    public void testParticleChoiceMaxOccurs3() throws Exception {
        TestParticalChoiceMaxOccurs3 testParticalMaxOccures3 = new TestParticalChoiceMaxOccurs3();
        TestParticalChoiceMaxOccursType3 testParticalMaxOccursType3 = new TestParticalChoiceMaxOccursType3();
        testParticalMaxOccures3.setTestParticalChoiceMaxOccurs3(testParticalMaxOccursType3);

        TestParticalChoiceMaxOccursType3Choice[] testChoices = new TestParticalChoiceMaxOccursType3Choice[2];

        testChoices[0] = new TestParticalChoiceMaxOccursType3Choice();
        testChoices[0].setParm1(new String[]{"Param111", null, "Param113"});

        testChoices[1] = new TestParticalChoiceMaxOccursType3Choice();
        testChoices[1].setParm2(new String[]{"Param121", "Param122", null});

        testParticalMaxOccursType3.setTestParticalChoiceMaxOccursType3Choice(testChoices);

        testSerializeDeserialize(testParticalMaxOccures3);
    }

    public void testParticleChoiceMaxOccurs4() throws Exception {

        TestParticalChoiceMaxOccurs4 testParticalChoiceMaxOccurs4 = new TestParticalChoiceMaxOccurs4();
        TestParticalChoiceMaxOccursType4 testParticalChoiceMaxOccursType4 = new TestParticalChoiceMaxOccursType4();
        testParticalChoiceMaxOccurs4.setTestParticalChoiceMaxOccurs4(testParticalChoiceMaxOccursType4);

        TestParticalChoiceMaxOccursType4Choice[] testParticalChoiceMaxOccursType4Choice =
                new TestParticalChoiceMaxOccursType4Choice[3];

        testParticalChoiceMaxOccursType4.setTestParticalChoiceMaxOccursType4Choice(testParticalChoiceMaxOccursType4Choice);
        testParticalChoiceMaxOccursType4Choice[0] = new TestParticalChoiceMaxOccursType4Choice();
        testParticalChoiceMaxOccursType4Choice[0].setParm1(getNewCustomType());

        testParticalChoiceMaxOccursType4Choice[1] = new TestParticalChoiceMaxOccursType4Choice();
        testParticalChoiceMaxOccursType4Choice[1].setParm2("Param2");

        testParticalChoiceMaxOccursType4Choice[2] = new TestParticalChoiceMaxOccursType4Choice();
        testParticalChoiceMaxOccursType4Choice[2].setParm3(getNewCustomType());

        testSerializeDeserialize(testParticalChoiceMaxOccurs4);
    }

    public void testParticleChoiceMaxOccurs5() throws Exception {

        TestParticalChoiceMaxOccurs5 testParticalChoiceMaxOccurs5 = new TestParticalChoiceMaxOccurs5();
        TestParticalChoiceMaxOccursType5 testParticalChoiceMaxOccursType5 = new TestParticalChoiceMaxOccursType5();
        testParticalChoiceMaxOccurs5.setTestParticalChoiceMaxOccurs5(testParticalChoiceMaxOccursType5);

        TestParticalChoiceMaxOccursType5Choice[] testParticalChoiceMaxOccursType5Choice =
                new TestParticalChoiceMaxOccursType5Choice[3];

        testParticalChoiceMaxOccursType5.setTestParticalChoiceMaxOccursType5Choice(testParticalChoiceMaxOccursType5Choice);

        testParticalChoiceMaxOccursType5Choice[0] = new TestParticalChoiceMaxOccursType5Choice();
        testParticalChoiceMaxOccursType5Choice[0].setParm1(new TestCustomType[]{getNewCustomType()});

        testParticalChoiceMaxOccursType5Choice[1] = new TestParticalChoiceMaxOccursType5Choice();
        testParticalChoiceMaxOccursType5Choice[1].setParm2("Param2");

        testParticalChoiceMaxOccursType5Choice[2] = new TestParticalChoiceMaxOccursType5Choice();
        testParticalChoiceMaxOccursType5Choice[2].setParm3(new TestCustomType[]{getNewCustomType()});

        testSerializeDeserialize(testParticalChoiceMaxOccurs5);
    }

    public void testParticalChoiceMaxOccursTest6() throws Exception {
        TestParticalChoiceMaxOccurs6 testParticalMaxOccures6 = new TestParticalChoiceMaxOccurs6();
        TestParticalChoiceMaxOccursType6 testParticalMaxOccursType6 = new TestParticalChoiceMaxOccursType6();
        testParticalMaxOccures6.setTestParticalChoiceMaxOccurs6(testParticalMaxOccursType6);

        TestParticalChoiceMaxOccursType6Choice[] testChoices = new TestParticalChoiceMaxOccursType6Choice[2];

        testChoices[0] = new TestParticalChoiceMaxOccursType6Choice();
        testChoices[0].setParm1("Param11");

        testChoices[1] = new TestParticalChoiceMaxOccursType6Choice();
        testChoices[1].setParm2("Param12");

        testParticalMaxOccursType6.setTestParticalChoiceMaxOccursType6Choice(testChoices);
        testParticalMaxOccursType6.setAttribute1("Attribute1");
        testParticalMaxOccursType6.setAttribute2("Attribute2");

        testSerializeDeserialize(testParticalMaxOccures6);
    }

    private TestCustomType getNewCustomType() {
        i++;
        TestCustomType testCustomType = new TestCustomType();
        testCustomType.setParam1("Param" + i + "2");
        testCustomType.setParam2(new String[]{"Param" + i + "21", "Param" + i + "22", "Param" + i + "23"});
        testCustomType.setParam3("Param" + i + "3");
        return testCustomType;
    }
}
