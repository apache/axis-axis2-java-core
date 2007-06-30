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

/**
 * 
 */
package org.apache.axis2.jaxws.misc;

import org.apache.axis2.jaxws.utility.JavaUtils;

import junit.framework.TestCase;

/**
 * Tests Namespace to Package Algorithmh
 *
 */
public class NS2PkgTest extends TestCase {

    public void test01() throws Exception {
        String ns1 = "http://example.org/NewBusiness/";
        String expectedPkg1 = "org.example.newbusiness";
        
        String pkg = JavaUtils.getPackageFromNamespace(ns1);
        assertTrue(expectedPkg1.equals(pkg));
    }
}
