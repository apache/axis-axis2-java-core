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
package org.apache.axis2.schema;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.resolver.DefaultURIResolver;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.xml.sax.InputSource;

/**
 * Resolves {@code xs:include} and {@code xs:import} locations for the code
 * generator, and only from the filesystem.
 * <p>
 * The generator's input is a contract somebody else wrote, so its
 * {@code schemaLocation} values are attacker-chosen text. Left to the default
 * resolver they are dereferenced with whatever scheme they name: an
 * {@code http://169.254.169.254/...} import turns the developer's machine into an
 * SSRF client, and a {@code file:///} one reads local files into the generated
 * sources. Resolving a schema set spread over sibling files -- the ordinary case --
 * needs no network access at all.
 * <p>
 * The check is on the location <em>after</em> it has been resolved against the base
 * URI, not on the text as written, because a relative location resolves against
 * whatever the base is: with a remote base, {@code common.xsd} is remote too.
 * <p>
 * Set {@code allowAbsoluteLocations} where a build genuinely resolves schemas over
 * the network. It is off by default because the safe case does not need it.
 */
public class RestrictedSchemaURIResolver implements URIResolver {

    private static final Log log = LogFactory.getLog(RestrictedSchemaURIResolver.class);

    /** Delegated to rather than extended: its protected surface is not stable. */
    private final DefaultURIResolver delegate = new DefaultURIResolver();

    private final boolean allowAbsoluteLocations;

    public RestrictedSchemaURIResolver() {
        this(false);
    }

    public RestrictedSchemaURIResolver(boolean allowAbsoluteLocations) {
        this.allowAbsoluteLocations = allowAbsoluteLocations;
    }

    public InputSource resolveEntity(String targetNamespace, String schemaLocation,
                                     String baseUri) {
        if (!allowAbsoluteLocations && !resolvesToAFile(schemaLocation, baseUri)) {
            log.warn("Refusing to resolve the schema location " + schemaLocation
                    + " relative to " + baseUri + ": only local schema files are"
                    + " resolved unless absolute locations are allowed");
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        }
        return delegate.resolveEntity(targetNamespace, schemaLocation, baseUri);
    }

    /**
     * @return whether the location, once resolved against the base, names a file
     */
    private boolean resolvesToAFile(String schemaLocation, String baseUri) {
        if (schemaLocation == null || schemaLocation.trim().isEmpty()) {
            return true;
        }
        try {
            URI location = new URI(schemaLocation.trim());
            if (baseUri != null && !baseUri.trim().isEmpty()) {
                location = new URI(baseUri.trim()).resolve(location);
            }
            if (!location.isAbsolute()) {
                // Nothing to resolve against, so nothing can be fetched remotely.
                return true;
            }
            String scheme = location.getScheme();
            return scheme != null && "file".equals(scheme.toLowerCase(Locale.ENGLISH));
        } catch (URISyntaxException e) {
            log.warn("Refusing an unparseable schema location: " + schemaLocation);
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("Refusing an unresolvable schema location: " + schemaLocation);
            return false;
        }
    }
}
