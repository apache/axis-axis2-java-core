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

package org.apache.axis2.json.gson.rpc;

import org.apache.axis2.json.gson.UtilTest;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.regex.Pattern;

public class JSONRPCIntegrationTest {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo/gson");

    // UUID pattern: 8-4-4-4-12 hex digits
    private static final Pattern UUID_PATTERN =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    @Test
    public void testJsonRpcMessageReceiver() throws Exception {
        String jsonRequest = "{\"echoPerson\":[{\"arg0\":{\"name\":\"Simon\",\"age\":\"35\",\"gender\":\"male\"}}]}";
        String echoPersonUrl = server.getEndpoint("JSONPOJOService") + "echoPerson";
        String expectedResponse = "{\"response\":{\"name\":\"Simon\",\"age\":\"35\",\"gender\":\"male\"}}";
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

    // ── correlation ID / error hardening tests ────────────────────────────────

    /**
     * A completely malformed JSON body (not even valid JSON) must return an
     * error response that contains "Bad Request" — the security-safe message —
     * rather than leaking a Java exception class name or stack trace.
     */
    @Test
    public void testMalformedJsonBodyReturnsBadRequest() throws Exception {
        String echoPersonUrl = server.getEndpoint("JSONPOJOService") + "echoPerson";
        UtilTest.TestResponse result = UtilTest.postForResponse("NOT_VALID_JSON", echoPersonUrl);
        String body = result.body;
        Assert.assertTrue("Response must contain 'Bad Request' for malformed JSON body",
                body.contains("Bad Request"));
    }

    /**
     * A malformed request must include an errorRef (correlation ID) so that
     * developers can grep server logs without the client seeing any structural
     * detail about the failure.
     */
    @Test
    public void testMalformedJsonBodyIncludesCorrelationId() throws Exception {
        String echoPersonUrl = server.getEndpoint("JSONPOJOService") + "echoPerson";
        UtilTest.TestResponse result = UtilTest.postForResponse("NOT_VALID_JSON", echoPersonUrl);
        String body = result.body;
        Assert.assertTrue("Response must contain 'errorRef=' correlation ID",
                body.contains("errorRef="));
    }

    /**
     * The errorRef value embedded in the fault message must be a valid UUID so
     * that it is grep-able in server logs and carries no structural information
     * about the request path.
     */
    @Test
    public void testMalformedJsonBodyCorrelationIdIsUuid() throws Exception {
        String echoPersonUrl = server.getEndpoint("JSONPOJOService") + "echoPerson";
        UtilTest.TestResponse result = UtilTest.postForResponse("NOT_VALID_JSON", echoPersonUrl);
        String body = result.body;
        Assert.assertTrue("errorRef in fault must be a UUID",
                UUID_PATTERN.matcher(body).find());
    }

    /**
     * A correctly enveloped request that uses the wrong operation name wrapper
     * (e.g. missing the outer array) must return "Bad Request" with an errorRef,
     * not a Java stack trace or IOException message.
     */
    @Test
    public void testMissingOuterArrayReturnsBadRequestWithCorrelationId() throws Exception {
        // Valid JSON but wrong envelope: missing the [{...}] wrapper
        String badEnvelope = "{\"echoPerson\":{\"name\":\"Simon\"}}";
        String echoPersonUrl = server.getEndpoint("JSONPOJOService") + "echoPerson";
        UtilTest.TestResponse result = UtilTest.postForResponse(badEnvelope, echoPersonUrl);
        String body = result.body;
        Assert.assertTrue("Wrong-envelope request must return 'Bad Request'",
                body.contains("Bad Request"));
        Assert.assertTrue("Wrong-envelope response must contain an errorRef",
                body.contains("errorRef="));
    }

    /**
     * Error responses must NOT leak Java exception class names (e.g.
     * "MalformedJsonException" or "IOException").  The correlation ID pattern
     * ensures the fault message is purely "Bad Request [errorRef=<uuid>]".
     */
    @Test
    public void testMalformedJsonBodyDoesNotLeakExceptionClassName() throws Exception {
        String echoPersonUrl = server.getEndpoint("JSONPOJOService") + "echoPerson";
        UtilTest.TestResponse result = UtilTest.postForResponse("NOT_VALID_JSON", echoPersonUrl);
        String body = result.body;
        Assert.assertFalse("Response must not leak 'MalformedJsonException'",
                body.contains("MalformedJsonException"));
        Assert.assertFalse("Response must not leak 'IOException'",
                body.contains("IOException"));
        Assert.assertFalse("Response must not leak stack trace element 'at org.apache'",
                body.contains("at org.apache"));
    }

    /**
     * The InOnly receiver (fire-and-forget) must apply the same correlation ID
     * pattern for malformed requests — no exception leak on that path either.
     */
    @Test
    public void testInOnlyMalformedJsonBodyReturnsBadRequestWithCorrelationId() throws Exception {
        String pingUrl = server.getEndpoint("JSONPOJOService") + "ping";
        UtilTest.TestResponse result = UtilTest.postForResponse("NOT_VALID_JSON", pingUrl);
        String body = result.body;
        Assert.assertTrue("InOnly malformed request must return 'Bad Request'",
                body.contains("Bad Request"));
        Assert.assertTrue("InOnly malformed request must contain an errorRef",
                body.contains("errorRef="));
    }
}
