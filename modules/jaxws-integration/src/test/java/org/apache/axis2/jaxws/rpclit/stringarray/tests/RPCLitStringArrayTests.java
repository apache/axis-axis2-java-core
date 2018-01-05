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

package org.apache.axis2.jaxws.rpclit.stringarray.tests;

import org.apache.axis2.jaxws.rpclit.stringarray.sei.Echo;
import org.apache.axis2.jaxws.rpclit.stringarray.sei.RPCLitStringArrayService;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;
import org.test.rpclit.stringarray.StringArray;

import javax.xml.ws.BindingProvider;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

public class RPCLitStringArrayTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    @Test
    public void testStringArrayType() throws Exception {
        System.out.println("------------------------------");
            
        RPCLitStringArrayService service = new RPCLitStringArrayService();
        Echo portType = service.getEchoPort();
        BindingProvider p = (BindingProvider) portType;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                  server.getEndpoint("RPCLitStringArrayService.EchoImplPort"));

        String[] strArray= {"str1", "str2", "str3", "str4 5"};
        StringArray array = new StringArray();
        array.getItem().addAll(Arrays.asList(strArray));
        StringArray result = portType.echoStringArray(array);
            
        assertEquals(array.getItem().size(), result.getItem().size());
        for (int i = 0; i < array.getItem().size(); i++) {
            assertEquals(array.getItem().get(i), result.getItem().get(i));
        }

        // Try a second time to verify
        String[] strArray2 = {"str1", "str2", "str3", "str4 5"};
        array = new StringArray();
        array.getItem().addAll(Arrays.asList(strArray2));
        result = portType.echoStringArray(array);
            
        assertEquals(array.getItem().size(), result.getItem().size());
        for (int i = 0; i < array.getItem().size(); i++) {
            assertEquals(array.getItem().get(i), result.getItem().get(i));
        }

        System.out.print("---------------------------------");
    }

    @Test
    public void testStringArrayTypeNoSEI() throws Exception {
        System.out.println("------------------------------");
            
        RPCLitStringArrayService service = new RPCLitStringArrayService();
        Echo portType = service.getEchoPort();
        BindingProvider p = (BindingProvider) portType;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                                  server.getEndpoint("RPCLitStringArrayEchoNoSEIService.EchoNoSEIPort"));

        String[] strArray= {"str1", "str2", "str3", "str4 5"};
        StringArray array = new StringArray();
        array.getItem().addAll(Arrays.asList(strArray));
        StringArray result = portType.echoStringArray(array);
            
        assertEquals(array.getItem().size(), result.getItem().size());
        for (int i = 0; i < array.getItem().size(); i++) {
            assertEquals(array.getItem().get(i) + "return", result.getItem().get(i));
        }
        
        // Try a second time to verify
        String[]strArray2 = {"str1", "str2", "str3", "str4 5"};
        array = new StringArray();
        array.getItem().addAll(Arrays.asList(strArray2));
        result = portType.echoStringArray(array);
            
        assertEquals(array.getItem().size(), result.getItem().size());
        for (int i = 0; i < array.getItem().size(); i++) {
            assertEquals(array.getItem().get(i) + "return", result.getItem().get(i));
        }

        System.out.print("---------------------------------");
    }
}
