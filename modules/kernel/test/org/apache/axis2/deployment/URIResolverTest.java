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
}
