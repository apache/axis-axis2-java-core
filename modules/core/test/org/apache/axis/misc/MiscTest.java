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
 
package org.apache.axis.misc;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.context.SessionContext;
import org.apache.axis.engine.AxisFault;

import java.lang.reflect.InvocationTargetException;

public class MiscTest extends AbstractTestCase {

    /**
     * @param testName
     */
    public MiscTest(String testName) {
        super(testName);
    }

    public void testSessionContext() {
        SessionContext sc = new SessionContext(null);
        String key = "Hello";
        Object val = new Object();
        sc.setProperty(key, val);
        assertEquals(sc.getProperty(key), val);
    }




    public void testAxisFault() {
        Exception e = new InvocationTargetException(new Exception());
        assertNotSame(AxisFault.makeFault(e), e);

        e = new AxisFault("");
    }

}
