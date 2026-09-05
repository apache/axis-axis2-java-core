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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;

import org.apache.axis2.description.AxisService;

/**
 * Serves a schema or WSDL document that a service archive packages under
 * {@code META-INF/}, by the name the request asked for.
 * <p>
 * Two properties have to hold on every route that does this, and they used to hold on
 * some and not others, which is why the lookup lives in one place now:
 * <ul>
 * <li><b>Only schema and WSDL documents.</b> A service archive's {@code META-INF} holds
 *     more than those -- {@code services.xml}, whose parameters name keystores and
 *     password-callback classes, plus {@code MANIFEST.MF} and module policies.</li>
 * <li><b>Only the queried service's own archive.</b> The lookup uses
 *     {@link URLClassLoader#findResource}, which does not delegate to the parent, so a
 *     request cannot reach a {@code META-INF} resource in an unrelated jar further up
 *     the chain -- Axis2's own jars, {@code WEB-INF/lib}, or the container's shared
 *     libraries. That is the AXIS2-5846 rule.</li>
 * </ul>
 * A classloader that is not a {@link URLClassLoader} cannot be searched without
 * delegating, so nothing is served in that case rather than serving too much. Deployed
 * archives always get a {@code DeploymentClassLoader}, which is one; an embedder that
 * builds an {@code AxisService} against some other classloader loses this route and
 * keeps every other way of publishing a schema.
 */
public class MetaInfResources {

    private MetaInfResources() {
    }

    /**
     * Whether a requested name may be served at all: a named {@code .xsd} or
     * {@code .wsdl}, with no traversal, no scheme and no absolute path.
     *
     * @param name the name as the request gave it
     * @return true if it is a document this route is allowed to serve
     */
    public static boolean isServable(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ENGLISH);
        int extension = lower.endsWith(".xsd") ? 4 : (lower.endsWith(".wsdl") ? 5 : 0);
        if (extension == 0) {
            return false;
        }
        // Require something to be named, so that a bare ".xsd" is not a document.
        String base = name.substring(0, name.length() - extension);
        if (base.isEmpty() || base.endsWith("/")) {
            return false;
        }
        return name.indexOf("..") < 0 && name.indexOf(':') < 0 && !name.startsWith("/");
    }

    /**
     * Opens a packaged document from the service's own archive.
     *
     * @param service the service whose archive is being asked
     * @param name    the document name as the request gave it
     * @return a stream over the document, or null if it is not servable, not present,
     *         or the service's classloader cannot be searched without delegating
     */
    public static InputStream getResourceAsStream(AxisService service, String name) {
        if (service == null || !isServable(name)) {
            return null;
        }
        ClassLoader classLoader = service.getClassLoader();
        if (!(classLoader instanceof URLClassLoader)) {
            return null;
        }
        URL url = ((URLClassLoader) classLoader).findResource("META-INF/" + name);
        if (url == null) {
            return null;
        }
        try {
            return url.openStream();
        } catch (IOException ex) {
            return null;
        }
    }
}
