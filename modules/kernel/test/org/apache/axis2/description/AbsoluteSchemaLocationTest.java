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
package org.apache.axis2.description;

import junit.framework.TestCase;

/**
 * The default schema resolver refuses an absolute schemaLocation. It decided what was
 * absolute with java.net.URI and swallowed URISyntaxException, so a location URI
 * rejects but java.net.URL accepts -- one containing a space, say -- was classified as
 * relative and fetched. These pin the classification, including the cases that used to
 * be unclassified.
 */
public class AbsoluteSchemaLocationTest extends TestCase {

    private void assertAbsolute(String loc) {
        assertTrue("should be refused: " + loc,
                WSDLToAxisServiceBuilder.isAbsoluteSchemaLocation(loc));
    }

    private void assertRelative(String loc) {
        assertFalse("should still resolve: " + loc,
                WSDLToAxisServiceBuilder.isAbsoluteSchemaLocation(loc));
    }

    public void testOrdinaryAbsoluteLocationsAreRefused() {
        assertAbsolute("http://internal-host/schema.xsd");
        assertAbsolute("https://internal-host/schema.xsd");
        assertAbsolute("ftp://internal-host/schema.xsd");
        assertAbsolute("jar:file:/tmp/a.jar!/schema.xsd");
        assertAbsolute("file:///etc/passwd");
    }

    /**
     * The bypass: java.net.URI throws on the space, so the old guard caught, gave up
     * on classifying, and resolved it anyway.
     */
    public void testAnAbsoluteLocationWithAUriIllegalCharacterIsStillRefused() {
        assertAbsolute("http://169.254.169.254/latest/meta-data/iam a.xsd");
        assertAbsolute("http://internal-host/a|b.xsd");
        assertAbsolute("http://internal-host/a{b}.xsd");
        assertAbsolute("http://internal-host/a^b.xsd");
        assertAbsolute("http://internal-host/a\\b.xsd");
    }

    /** Leading whitespace must not hide the scheme either. */
    public void testWhitespaceDoesNotHideAScheme() {
        assertAbsolute("  http://internal-host/schema.xsd");
        assertAbsolute("\thttps://internal-host/schema.xsd");
    }

    /** A network-path reference takes the base document's scheme. */
    public void testANetworkPathReferenceIsRefused() {
        assertAbsolute("//internal-host/schema.xsd");
    }

    /** The ordinary co-packaged case must keep working; that is the whole point. */
    public void testRelativeLocationsStillResolve() {
        assertRelative("wsat.xsd");
        assertRelative("./wsat.xsd");
        assertRelative("../common/wsat.xsd");
        assertRelative("schemas/wsat.xsd");
        assertRelative("/absolute/path/wsat.xsd");
        assertRelative(null);
        assertRelative("");
    }

    /**
     * A relative name that java.net.URI also rejects. It resolved before and must
     * keep resolving: failing closed on everything URI dislikes would have broken
     * ordinary deployments rather than the attack.
     */
    public void testARelativeNameWithASpaceStillResolves() {
        assertRelative("my schema.xsd");
        assertRelative("schemas/my schema.xsd");
    }

    /**
     * A Windows absolute path is refused. It parses as a one-letter scheme, and the
     * forward-slash form "C:/schemas/a.xsd" was already refused; only the backslash
     * form slipped through, by being URI-invalid. An absolute local path is the LFI
     * case this guard is for, so the two forms agreeing is the fix, not a casualty.
     */
    public void testAWindowsAbsolutePathIsRefusedInBothForms() {
        assertAbsolute("C:/schemas/a.xsd");
        assertAbsolute("C:\\schemas\\a.xsd");
    }

    /** A scheme needs a leading letter, so these are names, not locations. */
    public void testThingsThatOnlyLookLikeSchemes() {
        assertRelative("2foo:bar.xsd");
        assertRelative(":leading-colon.xsd");
    }
}
