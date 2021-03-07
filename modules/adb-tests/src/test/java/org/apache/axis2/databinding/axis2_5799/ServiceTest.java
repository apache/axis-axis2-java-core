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

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.axis2.databinding.axis2_5799.client.ComplexTypeWithAttribute;
import org.apache.axis2.databinding.axis2_5799.client.EchoServiceStub;
import org.apache.axis2.databinding.axis2_5799.service.EchoImpl;
import org.apache.axis2.testutils.ClientHelper;
import org.apache.axis2.testutils.jaxws.JAXWSEndpoint;
import org.junit.ClassRule;
import org.junit.Test;

public class ServiceTest {
    @ClassRule
    public static final ClientHelper clientHelper = new ClientHelper("target/repo/client");

    @ClassRule
    public static final JAXWSEndpoint endpoint = new JAXWSEndpoint(new EchoImpl());

    @Test
    public void test() throws Exception {
        EchoServiceStub stub = clientHelper.createStub(EchoServiceStub.class, endpoint.getAddress());
        ComplexTypeWithAttribute request = new ComplexTypeWithAttribute();
        request.setAttr("value");
        assertThat(stub.echo(request).getAttr()).isEqualTo("value");
    }
}
