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
package org.apache.axis2.jaxws.sample;

import org.apache.axis2.jaxws.stringlist.sei.StringListPortType;
import org.apache.axis2.jaxws.stringlist.sei.StringListService;
import org.apache.axis2.jaxws.TestLogger;

import junit.framework.TestCase;


public class StringListTests extends TestCase{
    public void testStringListScenario() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        StringListService sls = new StringListService();
        StringListPortType portType =sls.getStringListPort();
        String[] retString = portType.stringList(new String[]{"String1","String2","String3"});
        assertNotNull(retString);
        assertTrue(retString.length == 3);
    }
}
