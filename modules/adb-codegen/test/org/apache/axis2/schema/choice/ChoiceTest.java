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

package org.apache.axis2.schema.choice;

import org.apache.axis2.schema.AbstractTestCase;

import test.axis2.apache.org.TestChoise;

public class ChoiceTest extends AbstractTestCase {

    public void testChoice() throws Exception {
        TestChoise testChoise = new TestChoise();
        testChoise.setChoice1("test 2");
        testChoise.setChoice2(5);
        
        TestChoise newTestChoice = (TestChoise)serializeDeserialize(testChoise);
        assertEquals(5, newTestChoice.getChoice2());
    }
}
