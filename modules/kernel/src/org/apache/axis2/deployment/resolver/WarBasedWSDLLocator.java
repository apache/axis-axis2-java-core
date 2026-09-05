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

package org.apache.axis2.deployment.resolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.resolver.DefaultURIResolver;
import org.xml.sax.InputSource;

import javax.wsdl.xml.WSDLLocator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class WarBasedWSDLLocator extends DefaultURIResolver implements WSDLLocator {
    protected static final Log log = LogFactory
            .getLog(WarBasedWSDLLocator.class);

    private InputStream baseInputStream;
    private URI lastImportLocation;
    private String baseURI;
    private ClassLoader classLoader;

    public WarBasedWSDLLocator(String baseURI, ClassLoader classLoader,
                               InputStream baseInputStream) {
        this.baseURI = baseURI;
        this.baseInputStream = baseInputStream;
        this.classLoader = classLoader;
    }

    public InputSource getBaseInputSource() {
        return new InputSource(baseInputStream);
    }

    /**
     * @param parentLocation
     * @param importLocation
     */
    public InputSource getImportInputSource(String parentLocation, String importLocation) {
        lastImportLocation = URI.create(parentLocation).resolve(importLocation);
        String loc = lastImportLocation.toString();

        if (isAbsolute(loc)) {
            // Block remote URLs to prevent SSRF in WSDL imports
            if (loc.regionMatches(true, 0, "http:", 0, 5)
                    || loc.regionMatches(true, 0, "https:", 0, 6)
                    || loc.regionMatches(true, 0, "ftp:", 0, 4)
                    || loc.regionMatches(true, 0, "jar:", 0, 4)
                    // file: as well. The threat model says these resolvers block it,
                    // and an absolute file: import would read the server's own
                    // filesystem rather than the archive these resolvers exist to read
                    // from. Only AARBasedWSDLLocator counts file: as absolute, so only
                    // there did one reach this branch and fall through to the parent
                    // resolver; the rest are covered so the guard reads the same way.
                    || loc.regionMatches(true, 0, "file:", 0, 5)) {
                throw new RuntimeException(
                        "Remote WSDL import blocked: " + loc);
            }
            return super.resolveEntity(
                    null, importLocation, parentLocation);
        } else {
            return new InputSource(classLoader.getResourceAsStream(loc));
        }
    }

    /**
     * As for the zip there is no point in returning
     * a base URI
     */
    public String getBaseURI() {
        // we don't care
        return baseURI;
    }

    /**
     * returns the latest import
     */
    public String getLatestImportURI() {
        //we don't care about this either
        return lastImportLocation.toString();
    }

    public void close() {
        //TODO: FIXME:
    }

    // Woden URIResolver.resolveURI() removed in 2.0.1 (AXIS2-6102)

    /**
     * Override logic in DefaultURIResolver class, which made this method
     * private in XmlSchema 2.3.3 so that it is no longer inherited.
     *
     * These are deliberately the 2.3.2 semantics rather than the
     * URI.isAbsolute() test that replaced them upstream: the remote-scheme
     * guard above relies on file: locations falling through to the relative
     * branch, and widening this predicate would send them to the parent
     * resolver instead. Keep it protected -- 2.3.2 still declares it
     * protected, and narrowing an inherited member will not compile.
     *
     * @param uri
     * @return boolean
     */
    protected boolean isAbsolute(String uri) {
        return uri.startsWith("http://")
                || uri.startsWith("https://")
                || uri.startsWith("urn:");
    }
}
