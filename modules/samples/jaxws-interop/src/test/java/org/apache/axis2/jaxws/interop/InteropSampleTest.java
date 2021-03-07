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
package org.apache.axis2.jaxws.interop;

import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.ws.BindingProvider;

import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.metadata.registry.MetadataFactoryRegistry;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.Rule;
import org.junit.Test;
import org.tempuri.BaseDataTypesDocLitBService;
import org.tempuri.IBaseDataTypesDocLitB;

public class InteropSampleTest {
    @Rule
    public final Axis2Server server = new Axis2Server("target/repo");

    @Test
    public void test() throws Exception {
        MetadataFactoryRegistry.setFactory(ClientConfigurationFactory.class, new ClientConfigurationFactory(null, "target/repo/axis2.xml"));
        BaseDataTypesDocLitBService service = new BaseDataTypesDocLitBService();
        IBaseDataTypesDocLitB proxy = service.getBasicHttpBindingIBaseDataTypesDocLitB();
        ((BindingProvider)proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, server.getEndpoint("BaseDataTypesDocLitBService"));
        assertThat(proxy.retBool(true)).isTrue();
        assertThat(proxy.retInt(42)).isEqualTo(42);
        String testString = "This is a test";
        assertThat(proxy.retString(testString)).isEqualTo(testString);
    }
}
