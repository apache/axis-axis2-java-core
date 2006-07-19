package org.apache.axis2.databinding.utils;

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.math.BigInteger;
import java.lang.reflect.Array;
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

public class ConverterUtilTest extends TestCase {

    /**
     * Test conversion of Big Integer
     */
    public void testBigInteger(){
        List l = new ArrayList();
        l.add("23445");
        l.add("23446");
        l.add("23456646");
        l.add("1113646");

        Object convertedObj = ConverterUtil.convertToArray(
                BigInteger.class,l);

        assertTrue(convertedObj.getClass().isArray());
        assertTrue(convertedObj.getClass().equals(BigInteger[].class));

    }

    /**
     * integer arrays
     */
    public void testInt(){
        List l = new ArrayList();
        l.add("23445");
        l.add("23446");
        l.add("23456646");
        l.add("1113646");

        Object convertedObj = ConverterUtil.convertToArray(
                int.class,l);

        assertTrue(convertedObj.getClass().isArray());
        assertTrue(convertedObj.getClass().equals(int[].class));

    }

     /**
     * boolean arrays
     */
    public void testBool(){
        List l = new ArrayList();
        l.add("true");
        l.add("false");
        l.add("true");
        l.add("false");

        Object convertedObj = ConverterUtil.convertToArray(
                boolean.class,l);

        assertTrue(convertedObj.getClass().isArray());
        assertTrue(convertedObj.getClass().equals(boolean[].class));

    }

}
