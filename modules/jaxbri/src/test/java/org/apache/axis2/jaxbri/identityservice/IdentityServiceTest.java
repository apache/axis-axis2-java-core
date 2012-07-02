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

import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.testutils.UtilServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Regression test for AXIS2-4197.
 */
public class IdentityServiceTest {
    private static final String ENDPOINT = "http://127.0.0.1:" + UtilServer.TESTING_PORT + "/axis2/services/IdentityLinkingService";
    
    @BeforeClass
    public static void startServer() throws Exception {
        UtilServer.start(System.getProperty("basedir", ".") + "/target/repo/identityservice");
        AxisConfiguration axisConfiguration = UtilServer.getConfigurationContext().getAxisConfiguration();
        AxisService service = axisConfiguration.getService("IdentityLinkingService");
        service.getParameter(Constants.SERVICE_CLASS).setValue(IdentityLinkingServiceImpl.class.getName());
        service.setScope(Constants.SCOPE_APPLICATION);
    }
    
    @AfterClass
    public static void stopServer() throws Exception {
        UtilServer.stop();
    }
    
    @Test
    public void test() throws Exception {
        IdentityLinkingService stub = new IdentityLinkingServiceStub(UtilServer.getConfigurationContext(), ENDPOINT);
        LinkIdentitiesType linkIdentities = new LinkIdentitiesType();
        linkIdentities.setOwningPlatform("test");
        stub.createLinkedIdentities(linkIdentities);
        stub.modifyLink(linkIdentities);
        stub.removeLink(linkIdentities);
    }
}
