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

/**
 * 
 */
package org.apache.axis2.jaxws.nonanonymous.complextype;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

import javax.xml.ws.BindingProvider;

public class NonAnonymousComplexTypeTests {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo");

    @Test
    public void testSimpleProxy() throws Exception {
        TestLogger.logger.debug("------------------------------");

        String msg = "Hello Server";
        EchoMessagePortType myPort = (new EchoMessageService()).getEchoMessagePort();
        BindingProvider p = (BindingProvider) myPort;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                server.getEndpoint("EchoMessageService.EchoMessageImplPort"));

        String response = myPort.echoMessage(msg);
        TestLogger.logger.debug(response);
        
        // Try a second time to verify
        response = myPort.echoMessage(msg);
        TestLogger.logger.debug(response);
        TestLogger.logger.debug("------------------------------");
    }

		    


}
