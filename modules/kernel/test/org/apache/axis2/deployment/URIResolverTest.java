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

package org.apache.axis2.deployment;

import junit.framework.TestCase;

import org.apache.axis2.deployment.resolver.AARBasedWSDLLocator;
import org.apache.axis2.deployment.resolver.AARFileBasedURIResolver;
import org.apache.axis2.deployment.resolver.WarFileBasedURIResolver;
import org.xml.sax.InputSource;

public class URIResolverTest extends TestCase {

    /**
     * Verify that remote http/https URLs are blocked by the SSRF
     * hardening in AAR and WAR resolvers. The resolvers should return
     * an empty InputSource instead of fetching the remote URL.
     */
    public void testRemoteUrlBlocked() {
        AARFileBasedURIResolver aar = new AARFileBasedURIResolver(null);
        InputSource inputSource = aar.resolveEntity(null,
                "http://www.test.org/test.xsd",
                "http://www.test.org/schema.xsd");
        assertNotNull(inputSource);
        // Should return empty InputSource, not one with the remote URL
        assertNull("AAR resolver must block remote http URLs (SSRF)",
                inputSource.getSystemId());

        WarFileBasedURIResolver war = new WarFileBasedURIResolver(null);
        inputSource = war.resolveEntity(null, "http://www.test.org/test.xsd",
                "http://www.test.org/schema.xsd");
        assertNotNull(inputSource);
        assertNull("WAR resolver must block remote http URLs (SSRF)",
                inputSource.getSystemId());
    }

    /**
     * Verify that https URLs are also blocked.
     */
    public void testHttpsUrlBlocked() {
        AARFileBasedURIResolver aar = new AARFileBasedURIResolver(null);
        InputSource inputSource = aar.resolveEntity(null,
                "https://www.test.org/test.xsd",
                "https://www.test.org/schema.xsd");
        assertNotNull(inputSource);
        assertNull("AAR resolver must block remote https URLs (SSRF)",
                inputSource.getSystemId());
    }

    /**
     * Verify that a relative schemaLocation with a remote baseUri is
     * blocked — prevents the bypass where a relative path resolves to
     * a remote URL via the base URI.
     */
    public void testRelativePathWithRemoteBaseBlocked() {
        WarFileBasedURIResolver war = new WarFileBasedURIResolver(null);
        InputSource inputSource = war.resolveEntity(null,
                "evil.xsd",
                "http://attacker.example.com/wsdl/");
        assertNotNull(inputSource);
        // Resolved URI is http://attacker.example.com/wsdl/evil.xsd — must be blocked
        assertNull("WAR resolver must block relative path resolving to remote URL",
                inputSource.getSystemId());
    }

    /**
     * Pin the scheme predicate the SSRF guards above are built on.
     *
     * The guards only inspect and block schemes once isAbsolute() says the
     * location is absolute, so widening that predicate silently reroutes
     * locations to the parent resolver. XmlSchema 2.3.3 made
     * DefaultURIResolver.isAbsolute private and replaced it with a
     * URI.isAbsolute() test, under which file: and jar: locations become
     * absolute; these resolvers therefore declare the original predicate
     * themselves. Anything that widens it must revisit the guards first.
     */
    public void testOnlyHttpHttpsAndUrnAreAbsolute() {
        ExposedWarResolver resolver = new ExposedWarResolver();

        assertTrue("http must be absolute",
                resolver.absolute("http://www.test.org/test.xsd"));
        assertTrue("https must be absolute",
                resolver.absolute("https://www.test.org/test.xsd"));
        assertTrue("urn must be absolute",
                resolver.absolute("urn:test:schema"));

        assertFalse("file: must stay relative, so it is read as a resource"
                + " rather than handed to the parent resolver",
                resolver.absolute("file:/tmp/test.xsd"));
        assertFalse("jar: must stay relative",
                resolver.absolute("jar:file:/tmp/app.jar!/test.xsd"));
        assertFalse("a plain relative path must stay relative",
                resolver.absolute("test.xsd"));
    }

    /** Exposes the protected predicate to this package. */
    private static class ExposedWarResolver extends WarFileBasedURIResolver {
        ExposedWarResolver() {
            super(null);
        }

        boolean absolute(String uri) {
            return isAbsolute(uri);
        }
    }

    /**
     * The threat model says these resolvers block file: resolution. Only one of the
     * four ever let a file: location reach the scheme guard: AARBasedWSDLLocator
     * counts file: as absolute, so an absolute file: import passed the guard, fell
     * through to the parent resolver and read the server's filesystem instead of the
     * archive. That is the one this closes.
     */
    public void testAarWsdlLocatorRefusesAFileImport() {
        try {
            new AARBasedWSDLLocator(null, null, null)
                    .getImportInputSource("file:/deploy/service.wsdl", "/etc/passwd");
            fail("an absolute file: import must not be resolved from the filesystem");
        } catch (RuntimeException expected) {
            assertTrue("should say it was blocked, was: " + expected.getMessage(),
                    expected.getMessage().contains("blocked"));
        }
    }

    /**
     * The other three reach safety a different way, and it is worth pinning which:
     * their isAbsolute does not count file: as absolute, so the location is treated
     * as a name to look up inside the archive and never opens a filesystem path. The
     * file: clause added to their scheme guard is therefore unreachable today, and
     * kept only so the guard reads the same way in all four.
     */
    public void testFileLocationsAreLookedUpInTheArchiveNotTheFilesystem() {
        WarFileBasedURIResolver war =
                new WarFileBasedURIResolver(getClass().getClassLoader());
        InputSource inputSource = war.resolveEntity(null,
                "file:///etc/passwd", "file:///etc/passwd");
        assertNotNull(inputSource);
        assertNull("nothing may be read from the filesystem",
                inputSource.getByteStream());
    }
}
