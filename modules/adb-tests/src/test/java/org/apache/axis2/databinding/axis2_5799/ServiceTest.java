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
package org.apache.axis2.databinding.axis2_5799;

import static com.google.common.truth.Truth.assertThat;

import javax.xml.ws.Endpoint;

import org.apache.axiom.testutils.PortAllocator;
import org.apache.axis2.databinding.axis2_5799.client.ComplexTypeWithAttribute;
import org.apache.axis2.databinding.axis2_5799.client.EchoServiceStub;
import org.apache.axis2.databinding.axis2_5799.service.EchoImpl;
import org.junit.Test;

public class ServiceTest {
    @Test
    public void test() throws Exception {
        int port = PortAllocator.allocatePort();
        String address = "http://localhost:" + port + "/service";
        Endpoint endpoint = Endpoint.publish(address, new EchoImpl());
        try {
            EchoServiceStub stub = new EchoServiceStub(address);
            ComplexTypeWithAttribute request = new ComplexTypeWithAttribute();
            request.setAttr("value");
            assertThat(stub.echo(request).getAttr()).isEqualTo("value");
        } finally {
            endpoint.stop();
        }
    }
}
