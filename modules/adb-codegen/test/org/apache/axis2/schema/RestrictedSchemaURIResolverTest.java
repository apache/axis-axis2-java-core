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

import java.io.InputStream;

import junit.framework.TestCase;

import org.xml.sax.InputSource;

/**
 * The code generator's input is a contract written elsewhere, so its
 * {@code schemaLocation} values are attacker-chosen. Only filesystem resolution is
 * allowed by default: a schema set spread over sibling files needs nothing more,
 * while a URL location would make the build fetch whatever it names.
 */
public class RestrictedSchemaURIResolverTest extends TestCase {

    private static final String LOCAL_BASE = "file:/home/dev/project/schemas/main.xsd";

    private boolean refused(InputSource source) throws Exception {
        if (source == null) {
            return true;
        }
        InputStream stream = source.getByteStream();
        return stream != null && stream.available() == 0 && source.getSystemId() == null;
    }

    public void testSiblingSchemaFilesStillResolve() throws Exception {
        InputSource resolved = new RestrictedSchemaURIResolver()
                .resolveEntity(null, "common.xsd", LOCAL_BASE);
        assertFalse("a relative include next to the input must still resolve",
                refused(resolved));
    }

    public void testRemoteLocationsAreRefused() throws Exception {
        RestrictedSchemaURIResolver resolver = new RestrictedSchemaURIResolver();
        assertTrue("http import must not be fetched", refused(
                resolver.resolveEntity(null, "http://attacker.example.com/evil.xsd", LOCAL_BASE)));
        assertTrue("the instance metadata address is the point of this", refused(
                resolver.resolveEntity(null, "http://169.254.169.254/latest/meta-data/", LOCAL_BASE)));
        assertTrue("https is no better", refused(
                resolver.resolveEntity(null, "https://attacker.example.com/evil.xsd", LOCAL_BASE)));
    }

    /**
     * The reason the check is on the resolved location rather than the text: with a
     * remote base, an innocent-looking relative include is remote too.
     */
    public void testRelativeLocationAgainstARemoteBaseIsRefused() throws Exception {
        assertTrue(refused(new RestrictedSchemaURIResolver()
                .resolveEntity(null, "common.xsd", "http://attacker.example.com/main.xsd")));
    }

    /**
     * The control: the refusals above must follow the flag, not something incidental.
     * With absolute locations allowed, the very same http location is not refused.
     */
    public void testTheRefusalFollowsTheFlag() throws Exception {
        String remote = "http://attacker.example.com/evil.xsd";
        assertTrue("refused while the default holds",
                refused(new RestrictedSchemaURIResolver().resolveEntity(null, remote, LOCAL_BASE)));
        assertFalse("not refused once absolute locations are allowed",
                refused(new RestrictedSchemaURIResolver(true).resolveEntity(null, remote, LOCAL_BASE)));
    }

    public void testAbsoluteLocationsResolveOnceAllowed() throws Exception {
        RestrictedSchemaURIResolver resolver = new RestrictedSchemaURIResolver(true);
        // Allowed through: it is handed to the default resolver, which is free to
        // fail on its own if nothing answers. What matters is that we did not refuse.
        assertNotNull(resolver.resolveEntity(null, "file:/etc/schemas/common.xsd", LOCAL_BASE));
    }
}
