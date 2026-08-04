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

package org.apache.axis2.openapi;

import junit.framework.TestCase;

import java.net.URI;

/**
 * Unit tests for the host validation and script-context encoding applied to
 * request-derived values in served documents.
 */
public class RequestUrlPolicyTest extends TestCase {

    public void testAcceptsOrdinaryHosts() {
        assertTrue(RequestUrlPolicy.isSafeHost("localhost"));
        assertTrue(RequestUrlPolicy.isSafeHost("api.example.com"));
        assertTrue(RequestUrlPolicy.isSafeHost("api-gateway.internal.example.com."));
        assertTrue(RequestUrlPolicy.isSafeHost("192.0.2.10"));
        assertTrue(RequestUrlPolicy.isSafeHost("[2001:db8::1]"));
        assertTrue(RequestUrlPolicy.isSafeHost("2001:db8::1"));
    }

    public void testRejectsScriptBreakoutHosts() {
        assertFalse(RequestUrlPolicy.isSafeHost("'-alert(document.domain)-'"));
        assertFalse(RequestUrlPolicy.isSafeHost("x'+alert(1)+'"));
        assertFalse(RequestUrlPolicy.isSafeHost("example.com/../evil"));
        assertFalse(RequestUrlPolicy.isSafeHost("example.com evil.com"));
        assertFalse(RequestUrlPolicy.isSafeHost("<script>"));
    }

    public void testRejectsEmptyAndOverlongHosts() {
        assertFalse(RequestUrlPolicy.isSafeHost(null));
        assertFalse(RequestUrlPolicy.isSafeHost(""));
        StringBuilder overlong = new StringBuilder();
        while (overlong.length() < 300) {
            overlong.append("a");
        }
        assertFalse(RequestUrlPolicy.isSafeHost(overlong.toString()));
    }

    /**
     * URI parsing is not a sanitiser here: the breakout payload is a legal
     * authority and round-trips unchanged, which is why the encoder exists.
     */
    public void testUriCreateDoesNotNeutraliseTheBreakout() {
        String hostile = "http://'-alert(document.domain)-'/openapi.json";
        assertEquals(hostile, URI.create(hostile).toString());
    }

    public void testEscapesQuotesAndParentheses() {
        String escaped = RequestUrlPolicy.escapeJavaScriptString(
                "http://'-alert(document.domain)-'/openapi.json");
        assertFalse("Quotes must not survive", escaped.contains("'"));
        assertFalse("Call syntax must not survive", escaped.contains("("));
        assertFalse("Call syntax must not survive", escaped.contains(")"));
    }

    public void testEscapesScriptElementBreakout() {
        String escaped = RequestUrlPolicy.escapeJavaScriptString("</script><script>alert(1)</script>");
        assertFalse("Angle brackets must not survive", escaped.contains("<"));
        assertFalse("Angle brackets must not survive", escaped.contains(">"));
    }

    public void testEscapesLineTerminators() {
        // U+2028 and U+2029 end a line inside a JavaScript string literal even
        // though nothing else treats them as newlines, so they break out of the
        // quoted URL exactly like a raw newline would.
        String escaped = RequestUrlPolicy.escapeJavaScriptString(
                "a\nb\r\u2028c\u2029d\\e");
        assertFalse("Newline must not survive", escaped.contains("\n"));
        assertFalse("Carriage return must not survive", escaped.contains("\r"));
        assertFalse("U+2028 must not survive", escaped.contains("\u2028"));
        assertFalse("U+2029 must not survive", escaped.contains("\u2029"));
        assertFalse("A live backslash must not survive", escaped.contains("\\e"));
    }

    public void testLeavesOrdinaryUrlsReadable() {
        String url = "https://api.example.com:8443/axis2/openapi.json";
        assertEquals(url, RequestUrlPolicy.escapeJavaScriptString(url));
    }

    public void testNullEncodesToEmptyString() {
        assertEquals("", RequestUrlPolicy.escapeJavaScriptString(null));
    }
}
