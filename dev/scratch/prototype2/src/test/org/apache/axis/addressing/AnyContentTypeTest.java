/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * Author: Eran Chinthaka - Lanka Software Foundation
 * Date: Dec 22, 2004
 * Time: 11:32:03 AM
 */
package org.apache.axis.addressing;

import junit.framework.TestCase;

import javax.xml.namespace.QName;


public class AnyContentTypeTest extends TestCase {
    private AnyContentType anyContentType;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AnyContentTypeTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        anyContentType = new AnyContentType();
    }

    public void testAddAndGetReferenceValue() {
        System.out.println("Testing by putting more than 5 values in this. (this is initialized for 5)");
        for (int i = 0; i < 10; i++) {
            anyContentType.addReferenceValue(new QName("http://www.opensouce.lk/" + i, "" + i), "value " + i * 100);
        }

        for (int i = 0; i < 10; i++) {
            String value = anyContentType.getReferenceValue(new QName("http://www.opensouce.lk/" + i, "" + i));
            assertEquals("Input value differs from what is taken out from AnyContentType", value, "value " + i * 100);
        }

    }


}
