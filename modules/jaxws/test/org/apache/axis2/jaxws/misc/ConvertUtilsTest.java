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
package org.apache.axis2.jaxws.misc;

import org.apache.axis2.jaxws.utility.ConvertUtils;

import junit.framework.TestCase;

/**
 * Unit Test for the ConvertUtils utility
 */
public class ConvertUtilsTest extends TestCase {

    public void test1() throws Exception {
        Byte[] input = new Byte[3];
        input[0] = new Byte((byte) 0);
        input[1] = new Byte((byte) 1);
        input[2] = new Byte((byte) 2);
        
        byte[] output = new byte[3];
        
        output = (byte[]) ConvertUtils.convert(input, 
                                               output.getClass());
        
        assertTrue(output.length == 3);
        assertTrue(output[0] == (byte) 0);
        assertTrue(output[1] == (byte) 1);
        assertTrue(output[2] == (byte) 2);
    }
}
