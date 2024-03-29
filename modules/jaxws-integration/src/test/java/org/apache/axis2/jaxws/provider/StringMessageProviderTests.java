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

package org.apache.axis2.jaxws.provider;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;

public class StringMessageProviderTests extends ProviderTestCase {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    String xmlString = "<test>test input</test>";
    private QName serviceName = new QName("http://ws.apache.org/axis2", "StringMessageProviderService");

    @Test
    public void testProviderString() throws Exception {
        TestLogger.logger.debug("---------------------------------------");
        
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, server.getEndpoint("StringMessageProviderService.StringMessageProviderPort"));
        
        Dispatch<String> dispatch = svc
                .createDispatch(portName, String.class, Service.Mode.PAYLOAD);

        TestLogger.logger.debug(">> Invoking Dispatch<String> StringMessageProviderService");
        String retVal = dispatch.invoke(xmlString);
        TestLogger.logger.debug(">> Response [" + retVal + "]");
        
        
        // Try again to verify
        TestLogger.logger.debug(">> Invoking Dispatch<String> StringMessageProviderService");
        retVal = dispatch.invoke(xmlString);
        TestLogger.logger.debug(">> Response [" + retVal + "]");
    }
}
