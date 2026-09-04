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
package org.apache.axis2.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import javax.wsdl.xml.WSDLLocator;

import junit.framework.TestCase;

import org.xml.sax.InputSource;

/**
 * wsdl4j parses with its own unhardened parser and fetches imports itself, so a
 * document has to be screened before it gets there. The screen must not narrow what
 * can be loaded, though: these paths load WSDLs from files, archives, the classpath
 * and catalogs, and codegen passes bare relative paths.
 */
public class HardenedWSDLLocatorTest extends TestCase {

    private static final String PLAIN_WSDL =
            "<?xml version='1.0'?>"
            + "<definitions xmlns='http://schemas.xmlsoap.org/wsdl/'"
            + " targetNamespace='urn:test'><types/></definitions>";

    private static final String WSDL_WITH_DOCTYPE =
            "<?xml version='1.0'?>"
            + "<!DOCTYPE definitions [<!ENTITY xxe SYSTEM 'file:///etc/passwd'>]>"
            + "<definitions xmlns='http://schemas.xmlsoap.org/wsdl/'"
            + " targetNamespace='urn:test'><types>&xxe;</types></definitions>";

    private File written;

    @Override
    protected void tearDown() throws Exception {
        if (written != null) {
            written.delete();
        }
    }

    private File write(String content, String suffix) throws Exception {
        written = File.createTempFile("axis2-hardened-locator", suffix);
        FileOutputStream out = new FileOutputStream(written);
        try {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        } finally {
            out.close();
        }
        return written;
    }

    /** A locator already in use keeps resolving; only the content is screened. */
    private WSDLLocator delegateReturning(final String content) {
        return new WSDLLocator() {
            public InputSource getBaseInputSource() {
                return new InputSource(new ByteArrayInputStream(
                        content.getBytes(StandardCharsets.UTF_8)));
            }

            public String getBaseURI() {
                return "urn:delegate";
            }

            public InputSource getImportInputSource(String parent, String location) {
                return getBaseInputSource();
            }

            public String getLatestImportURI() {
                return "urn:delegate-import";
            }

            public void close() {
            }
        };
    }

    public void testAnOrdinaryWsdlPassesThroughADelegate() {
        InputSource source = new HardenedWSDLLocator(delegateReturning(PLAIN_WSDL))
                .getBaseInputSource();
        assertNotNull(source);
        assertNotNull("wsdl4j must still get a readable stream", source.getByteStream());
    }

    public void testADoctypeFromADelegateIsRefused() {
        HardenedWSDLLocator locator =
                new HardenedWSDLLocator(delegateReturning(WSDL_WITH_DOCTYPE));
        try {
            locator.getBaseInputSource();
            fail("a DOCTYPE must not reach wsdl4j");
        } catch (RuntimeException expected) {
            assertTrue("should say what was refused, was: " + expected.getMessage(),
                    expected.getMessage().contains("Refusing WSDL document"));
        }
    }

    public void testAnImportFromADelegateIsScreenedToo() {
        HardenedWSDLLocator locator =
                new HardenedWSDLLocator(delegateReturning(WSDL_WITH_DOCTYPE));
        try {
            locator.getImportInputSource("urn:parent", "child.wsdl");
            fail("imports are fetched by wsdl4j too, so they need the same screen");
        } catch (RuntimeException expected) {
            assertTrue(expected.getMessage().contains("Refusing WSDL document"));
        }
    }

    /**
     * Codegen and the deployment paths hand over bare relative paths, which is what
     * wsdl4j's own readWSDL(String) accepts. Refusing those would have broken the
     * ordinary case rather than the dangerous one -- as it did on the first attempt,
     * caught by the codegen module's own WSDL generation.
     */
    public void testARelativeFilePathStillLoads() throws Exception {
        File wsdl = write(PLAIN_WSDL, ".wsdl");
        String relative = new File(".").getCanonicalFile().toURI()
                .relativize(wsdl.getCanonicalFile().toURI()).getPath();
        // Only meaningful if the temp file really is under the working directory;
        // otherwise fall back to the absolute path, which must also work.
        String location = new File(relative).isFile() ? relative : wsdl.getAbsolutePath();

        InputSource source = new HardenedWSDLLocator(location).getBaseInputSource();
        assertNotNull("a path wsdl4j would accept must still load", source);
        assertNotNull(source.getByteStream());
    }

    public void testAFileUrlStillLoads() throws Exception {
        File wsdl = write(PLAIN_WSDL, ".wsdl");
        InputSource source =
                new HardenedWSDLLocator(wsdl.toURI().toString()).getBaseInputSource();
        assertNotNull("file: URLs are how deployment loads WSDLs", source);
    }

    public void testADoctypeInAFileIsRefused() throws Exception {
        File wsdl = write(WSDL_WITH_DOCTYPE, ".wsdl");
        try {
            new HardenedWSDLLocator(wsdl.toURI().toString()).getBaseInputSource();
            fail("a DOCTYPE in a local WSDL must be refused as well");
        } catch (RuntimeException expected) {
            assertTrue(expected.getMessage().contains("Refusing WSDL document"));
        }
    }

    public void testAMissingLocationSaysSo() {
        try {
            new HardenedWSDLLocator("no-such-file-anywhere.wsdl").getBaseInputSource();
            fail("a location that cannot be opened should report that");
        } catch (RuntimeException expected) {
            assertTrue("was: " + expected.getMessage(),
                    expected.getMessage().contains("Failed to load WSDL document"));
        }
    }
}
