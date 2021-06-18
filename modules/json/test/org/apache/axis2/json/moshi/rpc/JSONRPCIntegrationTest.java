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

package org.apache.axis2.json.moshi.rpc;

import org.apache.axis2.json.moshi.UtilTest;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

public class JSONRPCIntegrationTest {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo/moshi");
    
    @Test
    public void testJsonRpcMessageReceiver() throws Exception {
        String jsonRequest = "{\"echoPerson\":[{\"arg0\":{\"name\":\"Simon\",\"age\":\"35\",\"gender\":\"male\"}}]}";
        String echoPersonUrl = server.getEndpoint("JSONPOJOService") + "echoPerson";
	// moshi uses alphabetical order, not field declaration order like gson
        // String expectedResponse = "{\"response\":{\"name\":\"Simon\",\"age\":\"35\",\"gender\":\"male\"}}";
        String expectedResponse = "{\"response\":{\"age\":\"35\",\"gender\":\"male\",\"name\":\"Simon\"}}";
        String response = UtilTest.post(jsonRequest, echoPersonUrl);
        Assert.assertNotNull(response);
        Assert.assertEquals(expectedResponse , response);
    }

    @Test
    public void testJsonInOnlyRPCMessageReceiver() throws Exception {
        String jsonRequest = "{\"ping\":[{\"arg0\":{\"name\":\"Simon\",\"age\":\"35\",\"gender\":\"male\"}}]}";
        String echoPersonUrl = server.getEndpoint("JSONPOJOService") + "ping";
        String response = UtilTest.post(jsonRequest, echoPersonUrl);
        Assert.assertEquals("", response);
    }
}
