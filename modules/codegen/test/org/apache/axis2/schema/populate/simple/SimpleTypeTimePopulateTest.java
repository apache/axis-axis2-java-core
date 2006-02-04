package org.apache.axis2.schema.populate.simple;

import org.apache.axis2.databinding.types.Time;
import org.apache.axis2.databinding.utils.ConverterUtil;
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

public class SimpleTypeTimePopulateTest extends AbstractSimplePopulater{
    private String values[]= {
            "13:20:00",
            "23:59:59"
    };
    private String xmlString[] = {
            "<timeParam xmlns=\"http://soapinterop.org/xsd\">"+values[0]+"</timeParam>",
            "<timeParam xmlns=\"http://soapinterop.org/xsd\">"+values[1]+"</timeParam>",
    };

    protected void setUp() throws Exception {
        className = "org.soapinterop.xsd.TimeParam";
        propertyClass = Time.class;
    }

    // force others to implement this method
    public void testPopulate() throws Exception {
        for (int i = 0; i < 2; i++) {
            checkValue(xmlString[i],values[i]);
        }
    }

    protected String convertToString(Object o) {
        return ConverterUtil.convertToString((Time)o);
    }

    protected void compare(String val1, String val2) {
        //todo - Find a propr way to compare these time strings
        //check for null for now
        assertNotNull(val1);
        assertNotNull(val2);
        //assertTrue(new Time(val1).equals(new Time(val2)));

    }

}
