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
package org.apache.axis2.transport.http;

import junit.framework.TestCase;

/**
 * The {@code ?xsd=} route reaches a service's packaged META-INF with the request's
 * value verbatim, and that directory holds more than schemas: {@code services.xml}
 * names keystores and password-callback classes. Only schema and WSDL documents are
 * servable, and the check lives inside the shared stream helper so that every caller
 * inherits it.
 */
public class MetaInfResourceGuardTest extends TestCase {

    public void testOnlySchemaAndWsdlDocumentsAreServable() {
        assertTrue(HTTPTransportUtils.isServableMetadataResource("Foo.xsd"));
        assertTrue(HTTPTransportUtils.isServableMetadataResource("Foo.wsdl"));
        assertTrue("the extension check is case-insensitive",
                HTTPTransportUtils.isServableMetadataResource("Foo.WSDL"));
        assertTrue("a name inside a packaged subdirectory is still fine",
                HTTPTransportUtils.isServableMetadataResource("schemas/Foo.xsd"));
    }

    /** The finding: services.xml carries the deployment's security parameters. */
    public void testServiceDescriptorsAreNotServable() {
        assertFalse(HTTPTransportUtils.isServableMetadataResource("services.xml"));
        assertFalse(HTTPTransportUtils.isServableMetadataResource("MANIFEST.MF"));
        assertFalse(HTTPTransportUtils.isServableMetadataResource("module.xml"));
    }

    public void testEscapesAndAbsoluteNamesAreNotServable() {
        assertFalse(HTTPTransportUtils.isServableMetadataResource("../../services.xml"));
        assertFalse("an escape ending in .xsd is still an escape",
                HTTPTransportUtils.isServableMetadataResource("../../../etc/passwd.xsd"));
        assertFalse(HTTPTransportUtils.isServableMetadataResource("/etc/passwd.xsd"));
        assertFalse("a URL would leave the archive entirely",
                HTTPTransportUtils.isServableMetadataResource("http://attacker.example.com/x.xsd"));
    }

    public void testEmptyAndNullNamesAreNotServable() {
        assertFalse(HTTPTransportUtils.isServableMetadataResource(null));
        assertFalse(HTTPTransportUtils.isServableMetadataResource(""));
        assertFalse(HTTPTransportUtils.isServableMetadataResource(".xsd"));
    }
}
