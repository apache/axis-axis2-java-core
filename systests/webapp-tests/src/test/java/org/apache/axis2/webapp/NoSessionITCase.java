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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test that pages that don't require login don't create HTTP sessions. Pages that create HTTP
 * sessions without the user being logged in may be exploited in session fixation attacks.
 */
@RunWith(Parameterized.class)
public class NoSessionITCase {
    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "/" },
            { "/services/listServices" },
            { "/services/ListFaultyServices" },
            { "/axis2-web/HappyAxis.jsp" },
            { "/axis2-admin/" } });
    }
    
    @Parameter
    public String page;
    
    @Rule
    public Axis2WebTester tester = new Axis2WebTester();
    
    @Test
    public void test() {
        tester.beginAt(page);
        assertThat(tester.getSessionId()).isNull();
    }
}
