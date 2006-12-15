/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.description.builder;

import junit.framework.TestCase;

/**
 * Tests the parsing of Generics that are used in the DescriptionBuilderComposite
 * processing.
 */
public class GenericsParsingTests extends TestCase {
    private static String JAXWS_HOLDER = "javax.xml.ws.Holder";
    
    public void testHolder() {
        String holderInputString = JAXWS_HOLDER + "<java.lang.Object>";
        assertTrue(ParameterDescriptionComposite.isHolderType(holderInputString));
        String holderResultString = ParameterDescriptionComposite.getRawType(holderInputString);
        assertEquals(JAXWS_HOLDER, holderResultString);
        
        String actualTypeResult = ParameterDescriptionComposite.getHolderActualType(holderInputString);
        assertEquals("java.lang.Object", actualTypeResult);
    }

    public void testNonHolderGenric() {
        String inputString = "java.util.List<my.package.MyClass>";
        assertFalse(ParameterDescriptionComposite.isHolderType(inputString));
        String genericType = ParameterDescriptionComposite.getRawType(inputString);
        assertEquals("java.util.List", genericType);
        // This should be null since the generic is not a Holder type
        String actualParam = ParameterDescriptionComposite.getHolderActualType(inputString);
        assertNull(actualParam);
    }
    
    public void testHolderGeneric() {
        String holderInputString = JAXWS_HOLDER + "<java.util.List<java.lang.Object>>";
        assertTrue(ParameterDescriptionComposite.isHolderType(holderInputString));
        String holderResultString = ParameterDescriptionComposite.getRawType(holderInputString);
        assertEquals(JAXWS_HOLDER, holderResultString);
        
        String actualTypeResult = ParameterDescriptionComposite.getHolderActualType(holderInputString);
        assertEquals("java.util.List", actualTypeResult);
        
    }
}
