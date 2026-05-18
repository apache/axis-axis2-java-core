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

import java.net.URI;

public class WarFileBasedURIResolver extends DefaultURIResolver {

    protected static final Log log = LogFactory
            .getLog(WarFileBasedURIResolver.class);

    private ClassLoader classLoader;

    public WarFileBasedURIResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public InputSource resolveEntity(
            String targetNamespace,
            String schemaLocation,
            String baseUri) {
        // Resolve against base URI first to catch relative + remote base bypass
        URI resolvedURI;
        try {
            resolvedURI = (baseUri != null)
                    ? URI.create(baseUri).resolve(schemaLocation)
                    : URI.create(schemaLocation);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid URI syntax for schema location: " + schemaLocation);
            return new InputSource(new java.io.ByteArrayInputStream(new byte[0]));
        }
        String resolved = resolvedURI.toString();

        if (isAbsolute(resolved)) {
            // Block remote URLs to prevent SSRF
            if (resolved.regionMatches(true, 0, "http:", 0, 5)
                    || resolved.regionMatches(true, 0, "https:", 0, 6)
                    || resolved.regionMatches(true, 0, "ftp:", 0, 4)
                    || resolved.regionMatches(true, 0, "jar:", 0, 4)) {
                log.warn("Blocked remote schema resolution in WAR deployment: " + resolved);
                return new InputSource(new java.io.ByteArrayInputStream(new byte[0]));
            }
            return super.resolveEntity(
                    targetNamespace, schemaLocation, baseUri);
        } else {
            //validate
            if ((baseUri == null || "".equals(baseUri)) && schemaLocation.startsWith("..")) {
                throw new RuntimeException(
                        "Unsupported schema location " + schemaLocation);
            }
            return new InputSource(classLoader.getResourceAsStream(resolved));
        }
    }
}

