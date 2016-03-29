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
package org.apache.axis2.databinding.axis2_5750;

import static com.google.common.truth.Truth.assertThat;

import javax.xml.ws.Endpoint;

import org.apache.axiom.testutils.PortAllocator;
import org.apache.axis2.databinding.axis2_5750.client.FixedValue;
import org.apache.axis2.databinding.axis2_5750.client.FixedValueServiceStub;
import org.apache.axis2.databinding.axis2_5750.client.NonFixedValue_type1;
import org.apache.axis2.databinding.axis2_5750.service.FixedValueServiceImpl;
import org.junit.Test;

public class ServiceTest {
    @Test
    public void test() throws Exception {
        int port = PortAllocator.allocatePort();
        String address = "http://localhost:" + port + "/service";
        Endpoint endpoint = Endpoint.publish(address, new FixedValueServiceImpl());
        try {
            FixedValue fixedValue = new FixedValue();
            NonFixedValue_type1 nonFixedValue_type1 = new NonFixedValue_type1();
            nonFixedValue_type1.setNonFixedValue_type0("SomeId");
            fixedValue.setNonFixedValue(nonFixedValue_type1);
            FixedValueServiceStub stub = new FixedValueServiceStub(address);
            assertThat(stub.test(fixedValue).getOut()).isEqualTo("OK");
        } finally {
            endpoint.stop();
        }
    }
}
