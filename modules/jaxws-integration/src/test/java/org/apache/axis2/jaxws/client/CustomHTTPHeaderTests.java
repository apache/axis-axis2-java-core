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

package org.apache.axis2.jaxws.client;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.apache.axis2.jaxws.sample.addnumbers.AddNumbersPortType;
import org.apache.axis2.jaxws.sample.addnumbers.AddNumbersService;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

public class CustomHTTPHeaderTests {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo");

    @Test
    public void testPort() throws Exception {        
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("MY_HEADER_1", Collections.singletonList("hello"));
        headers.put("MY_HEADER_2", Arrays.asList("value1", "value2"));
        
        AddNumbersService service = new AddNumbersService();
        AddNumbersPortType port = service.getAddNumbersPort();
                
        BindingProvider p = (BindingProvider) port;
        
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                server.getEndpoint("AddNumbersService.AddNumbersPortTypeImplPort"));
        p.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, headers);
        
        assertEquals(777, port.addNumbers(333, 444));
    }
}
