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

package org.apache.axis2.jaxws.sample;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.sample.stringlist.sei.StringListPortType;
import org.apache.axis2.jaxws.sample.stringlist.sei.StringListService;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.ws.BindingProvider;

public class StringListTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    @Test
    public void testStringListScenario() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        StringListService sls = new StringListService();
        StringListPortType portType =sls.getStringListPort();
        BindingProvider p =	(BindingProvider)portType;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                server.getEndpoint("StringListService.StringListPortTypeImplPort"));
        String[] send = new String[]{"String1","String2","String3","String Space"};
        // since the array is serilized as xsd:list the string with space will be converted
        // to a new array element. so we send array.length of 3 but get back array.length of 5
        String[] expected = new String[]{"String1","String2","String3","String","Space"};
        String[] retString = portType.stringList(send);
        assertNotNull(retString);
        assertEquals(expected.length, retString.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], retString[i]);
        }
        
        // Repeat to ensure validity
        retString = portType.stringList(send);
        assertNotNull(retString);
        assertEquals(expected.length, retString.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], retString[i]);
        }
    }
}
