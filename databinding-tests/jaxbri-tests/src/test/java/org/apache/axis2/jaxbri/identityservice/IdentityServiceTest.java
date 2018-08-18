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
package org.apache.axis2.jaxbri.identityservice;

import org.apache.axis2.testutils.Axis2Server;
import org.apache.axis2.testutils.ClientHelper;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Regression test for AXIS2-4197.
 */
public class IdentityServiceTest {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo/identityservice");
    
    @ClassRule
    public static ClientHelper clientHelper = new ClientHelper(server);
    
    @Test
    public void test() throws Exception {
        IdentityLinkingService stub = clientHelper.createStub(
                IdentityLinkingServiceStub.class, "IdentityLinkingService");
        LinkIdentitiesType linkIdentities = new LinkIdentitiesType();
        linkIdentities.setOwningPlatform("test");
        stub.createLinkedIdentities(linkIdentities);
        stub.modifyLink(linkIdentities);
        stub.removeLink(linkIdentities);
    }
}
