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
package org.apache.axis2.jaxbri.axis2_5919;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.apache.axis2.testutils.Axis2Server;
import org.apache.axis2.testutils.ClientHelper;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Regression test for AXIS2-5919.
 */
public class FaultServiceTest {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo/AXIS2-5919");
    
    @ClassRule
    public static ClientHelper clientHelper = new ClientHelper(server);
    
    @Test
    public void test() throws Exception {
        FaultService stub = clientHelper.createStub(FaultServiceStub.class, "FaultService");
        try {
            stub.test(new TestRequest());
            fail("Expected TestRequest");
        } catch (TestFaultException ex) {
            assertThat(ex.getFaultMessage().getMessage()).isEqualTo("test");
        }
    }
}
