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
package org.apache.axis2.wsdl.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URL;

import org.junit.jupiter.api.Test;

/**
 * WSDL2Java probes an http location for a redirect before parsing it, because a
 * redirected URL confuses some databindings. It took the Location header from
 * whatever the server answered -- any status code -- and used it verbatim, so a
 * hostile WSDL host could point the tool at a file: URL or a relative path it never
 * resolved. The document being fetched is by definition one the developer does not
 * control, so the redirect is the server's input, not the developer's.
 */
public class WsdlRedirectTest {

    private URL requested() throws Exception {
        return new URL("https://vendor.example/svc?wsdl");
    }

    @Test
    public void testAnOrdinaryRedirectIsFollowed() throws Exception {
        assertEquals("https://vendor.example/real.wsdl",
                CodeGenConfiguration.redirectTarget(requested(), 302,
                        "https://vendor.example/real.wsdl"));
        assertEquals("https://vendor.example/real.wsdl",
                CodeGenConfiguration.redirectTarget(requested(), 301,
                        "https://vendor.example/real.wsdl"));
        assertEquals("https://vendor.example/real.wsdl",
                CodeGenConfiguration.redirectTarget(requested(), 308,
                        "https://vendor.example/real.wsdl"));
    }

    /** RFC 9110 allows a relative target; it was previously used as given. */
    @Test
    public void testARelativeTargetIsResolvedAgainstTheRequest() throws Exception {
        assertEquals("https://vendor.example/wsdl/real.wsdl",
                CodeGenConfiguration.redirectTarget(requested(), 302, "/wsdl/real.wsdl"));
        assertEquals("https://vendor.example/real.wsdl",
                CodeGenConfiguration.redirectTarget(requested(), 302, "real.wsdl"));
    }

    /** A Location on a non-redirect status is not a redirect. */
    @Test
    public void testLocationOnANonRedirectStatusIsIgnored() throws Exception {
        assertNull(CodeGenConfiguration.redirectTarget(requested(), 200,
                "https://vendor.example/real.wsdl"));
        assertNull(CodeGenConfiguration.redirectTarget(requested(), 404,
                "https://vendor.example/real.wsdl"));
        assertNull(CodeGenConfiguration.redirectTarget(requested(), 500,
                "https://vendor.example/real.wsdl"));
    }

    @Test
    public void testNoLocationIsNoRedirect() throws Exception {
        assertNull(CodeGenConfiguration.redirectTarget(requested(), 302, null));
        assertNull(CodeGenConfiguration.redirectTarget(requested(), 302, "   "));
    }

    /** The retargeting case: a redirect must not aim the parse at the local disk. */
    @Test
    public void testARedirectToANonHttpSchemeIsRefused() throws Exception {
        assertRefused("file:///etc/passwd");
        assertRefused("jar:file:/tmp/a.jar!/x.wsdl");
        assertRefused("ftp://internal-host/x.wsdl");
    }

    private void assertRefused(String location) throws Exception {
        try {
            CodeGenConfiguration.redirectTarget(requested(), 302, location);
            fail("should have refused the redirect to " + location);
        } catch (CodeGenerationException expected) {
            assertTrue(expected.getMessage().contains(location.substring(0, 4)),
                    () -> "should name the target, was: " + expected.getMessage());
        }
    }

    /** Only http(s) documents are probed at all. */
    @Test
    public void testOnlyHttpLocationsAreProbed() {
        assertNotNull(CodeGenConfiguration.asHttpUrl("http://vendor.example/svc?wsdl"));
        assertNotNull(CodeGenConfiguration.asHttpUrl("https://vendor.example/svc?wsdl"));
        assertNull(CodeGenConfiguration.asHttpUrl("file:/tmp/a.wsdl"));
        assertNull(CodeGenConfiguration.asHttpUrl("test-resources/wsdls/Version.wsdl"));
        assertNull(CodeGenConfiguration.asHttpUrl(null));
    }

    /**
     * The old test was a startsWith("http") on the raw string, which also matched a
     * scheme merely beginning with those letters.
     */
    @Test
    public void testASchemeThatOnlyStartsWithHttpIsNotProbed() {
        assertNull(CodeGenConfiguration.asHttpUrl("httpx://vendor.example/svc"));
    }
}
