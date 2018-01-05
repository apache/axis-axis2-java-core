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

package org.apache.axis2.jaxws.anytype.tests;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.anytype.AnyTypeMessagePortType;
import org.apache.axis2.jaxws.anytype.AnyTypeMessageService;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import javax.xml.ws.BindingProvider;

public class AnyTypeTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");
    
    @Test
    public void testAnyTypeElementinWrappedWSDL(){
        // Run test a few times to ensure correct 
        _testAnyTypeElementinWrappedWSDL();
        _testAnyTypeElementinWrappedWSDL();
        _testAnyTypeElementinWrappedWSDL();
    }
    
    public void _testAnyTypeElementinWrappedWSDL(){
        AnyTypeMessageService service = new AnyTypeMessageService();
        AnyTypeMessagePortType portType = service.getAnyTypePort();
        BindingProvider p = (BindingProvider) portType;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                "http://localhost:" + server.getPort() + "/axis2/services/AnyTypeMessageService.AnyTypeMessagePortTypeImplPort");

        String req = new String("Request as String");
        Object response = portType.echoMessage(req);
        assertTrue(response instanceof String);
        TestLogger.logger.debug("Response =" + response);
    }
}
