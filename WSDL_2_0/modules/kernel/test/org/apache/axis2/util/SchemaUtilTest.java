package org.apache.axis2.util;

import junit.framework.TestCase;

import java.util.*;
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

public class SchemaUtilTest extends TestCase {

    private Map parameterMap;

    protected void setUp() throws Exception {
        parameterMap = new HashMap();

        /**
         * <year>2006</year>
         <month>11</month>
         <day>18</day>
         <time>16:40:00</time>
         <time>20:00:42</time>
         */

    }

    public void testCreateHttpLocationParameterMap() {

        parameterMap.put("firstNamePart", "ABCD");
        parameterMap.put("last", "DCBA");

        String httpLocation = "test=1&last={LastName}&firstNamePart={FirstName}";

        MultipleEntryHashMap multipleEntryHashMap = new MultipleEntryHashMap();

        SchemaUtil.extractParametersFromQueryPart(httpLocation, "&", multipleEntryHashMap, parameterMap);
        assertEquals("ABCD", multipleEntryHashMap.get("FirstName"));
        assertEquals("DCBA", multipleEntryHashMap.get("LastName"));
    }

    public void testServerSideURLTemplateHandlingOne() {
        String template = "datespace/{year}/{month}/{day}/{time}-{time}.html";

        StringBuffer actualPath = new StringBuffer("http://example.org/datespace/2006/11/18/16:40:00-20:00:42.html");

        MultipleEntryHashMap multipleEntryHashMap = new MultipleEntryHashMap();

        SchemaUtil.extractParametersFromPath(template, multipleEntryHashMap, actualPath);

        assertEquals(multipleEntryHashMap.get("year"), "2006");
        assertEquals(multipleEntryHashMap.get("month"), "11");
        assertEquals(multipleEntryHashMap.get("day"), "18");
        assertEquals(multipleEntryHashMap.get("time"), "16:40:00");
        assertEquals(multipleEntryHashMap.get("time"), "20:00:42");
    }

    public void testServerSideURLTemplateHandlingTwo() {

        parameterMap.put("day", "18");
        parameterMap.put("start", "16:40:00");
        parameterMap.put("end", "20:00:42");
        String template = "datespace/{year}/{month}.html?day={day};start={time};end={time}";

        StringBuffer actualPath = new StringBuffer("http://example.org/datespace/2006/11.html?day=18;start=16:40:00;end=20:00:42");

        MultipleEntryHashMap multipleEntryHashMap = new MultipleEntryHashMap();

        String[] urlParts = template.split("\\?");
        String templatedPath = urlParts[0];
        String templatedQueryParams = urlParts[1];

        SchemaUtil.extractParametersFromPath(templatedPath, multipleEntryHashMap, actualPath);
        SchemaUtil.extractParametersFromQueryPart(templatedQueryParams, ";", multipleEntryHashMap, parameterMap);

        assertEquals(multipleEntryHashMap.get("year"), "2006");
        assertEquals(multipleEntryHashMap.get("month"), "11");
        assertEquals(multipleEntryHashMap.get("day"), "18");
        assertEquals(multipleEntryHashMap.get("time"), "16:40:00");
        assertEquals(multipleEntryHashMap.get("time"), "20:00:42");
    }

}
