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
package org.apache.axis2.webapp;

import org.junit.Rule;
import org.junit.Test;

public class AxisServletITCase {
    @Rule
    public Axis2WebTester tester = new Axis2WebTester();

    @Test
    public void testListServices() {
        tester.beginAt("/");
        tester.clickLinkWithExactText("Services");
        tester.assertLinkPresentWithExactText("Version");
        tester.assertTextPresent("Service Description : This service is to get the running Axis version");
    }

    /**
     * Regression test for AXIS2-5683.
     */
    @Test
    public void testHandlePolicyRequestXSS() {
        tester.setIgnoreFailingStatusCodes(true);
        tester.beginAt("/services/Version?policy&id=<xss>");
        tester.assertResponseCode(404);
    }
}
