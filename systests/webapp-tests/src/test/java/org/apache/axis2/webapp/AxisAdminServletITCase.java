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

import static com.google.common.truth.Truth.assertThat;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AxisAdminServletITCase {
    @Rule
    public Axis2WebTester tester = new Axis2WebTester();

    @Before
    public void setUp() {
        tester.beginAt("/axis2-admin/");
        tester.setTextField("userName", "admin");
        tester.setTextField("password", "axis2");
        tester.submit();
    }

    @Test
    public void testAvailableServices() {
        tester.clickLinkWithText("Available Services");
        tester.assertMatch("Service EPR : http://localhost:[0-9]+/axis2/services/Version");
        tester.assertTextPresent("Service Description : This service is to get the running Axis version");
    }

    /**
     * Tests that the admin console is not vulnerable to session fixation attacks. This tests
     * attempts to log in with an existing session. This should result in a new session with a
     * different session ID.
     */
    @Test
    public void loginInvalidatesExistingSession() {
        String sessionId = tester.getSessionId();
        assertThat(sessionId).isNotNull();
        tester.gotoPage("/axis2-admin/welcome");
        tester.setTextField("userName", "admin");
        tester.setTextField("password", "axis2");
        tester.submit();
        assertThat(tester.getSessionId()).isNotEqualTo(sessionId);
    }

    @Test
    public void testUploadRemoveService() throws Exception {
        tester.clickLinkWithText("Upload Service");
        String echoServiceLocation = IOUtils.toString(AxisAdminServletITCase.class.getResource("/echo-service-location.txt"));
        tester.setTextField("filename", echoServiceLocation);
        tester.clickButtonWithText(" Upload ");
        tester.assertMatch("File echo-.+\\.aar successfully uploaded");
        int attempt = 0;
        while (true) {
            attempt++;
            tester.clickLinkWithText("Available Services");
            try {
                tester.assertFormPresent("Echo");
                break;
            } catch (AssertionError ex) {
                if (attempt < 30) {
                    Thread.sleep(1000);
                } else {
                    throw ex;
                } 
            }
        }
        tester.setWorkingForm("Echo");
        tester.submit();
        tester.assertTextPresent("Service 'Echo' has been successfully removed.");
    }

    @Test
    public void testEditServiceParameters() {
        tester.clickLinkWithText("Edit Parameters");
        tester.selectOption("axisService", "Version");
        tester.clickButtonWithText(" Edit Parameters ");
        tester.assertTextFieldEquals("Version_ServiceClass", "sample.axisversion.Version");
    }
}
