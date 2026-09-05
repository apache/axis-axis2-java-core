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
package org.apache.axis2.openapi;

import java.lang.reflect.Method;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;

import junit.framework.TestCase;

/**
 * The Swagger UI page is built to drive authenticated API calls, and its scripts come
 * from a CDN. Pinning the version in the URL says which release to ask for, not that
 * what came back is that release, so the script tags carry Subresource Integrity
 * hashes. They are version-specific: a deployment pinning a different version has to
 * supply its own, and the page still renders without them rather than going down.
 */
public class SwaggerUIIntegrityTest extends TestCase {

    private String attributesFor(OpenApiConfiguration configuration, String version,
                                 String configured, String shipped) throws Exception {
        ConfigurationContext configContext =
                ConfigurationContextFactory.createEmptyConfigurationContext();
        SwaggerUIHandler handler = new SwaggerUIHandler(configContext, configuration);
        Method method = SwaggerUIHandler.class.getDeclaredMethod("integrityAttributes",
                String.class, String.class, String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(handler, version, configured, shipped,
                "swagger-ui-bundle.js");
    }

    public void testTheShippedHashIsUsedForTheDefaultVersion() throws Exception {
        OpenApiConfiguration configuration = new OpenApiConfiguration();
        String attributes = attributesFor(configuration,
                configuration.getSwaggerUiVersion(), null, "sha384-SHIPPED");
        assertTrue("the shipped hash should be applied, was: " + attributes,
                attributes.contains("integrity=\"sha384-SHIPPED\""));
        assertTrue("the browser only checks integrity cross-origin with this",
                attributes.contains("crossorigin=\"anonymous\""));
    }

    /** A hash for one version says nothing about another, so it must not be reused. */
    public void testTheShippedHashIsNotAppliedToAnotherVersion() throws Exception {
        OpenApiConfiguration configuration = new OpenApiConfiguration();
        String attributes = attributesFor(configuration, "4.15.5", null, "sha384-SHIPPED");
        assertEquals("a hash from a different version would just break the page",
                "", attributes);
    }

    /** Which is why an operator pinning a version can supply their own. */
    public void testAnOperatorSuppliedHashIsUsedForAnyVersion() throws Exception {
        OpenApiConfiguration configuration = new OpenApiConfiguration();
        String attributes =
                attributesFor(configuration, "4.15.5", "sha384-OPERATOR", "sha384-SHIPPED");
        assertTrue("was: " + attributes, attributes.contains("integrity=\"sha384-OPERATOR\""));
        assertTrue(attributes.contains("crossorigin=\"anonymous\""));
    }

    public void testAnOperatorHashWinsOverTheShippedOne() throws Exception {
        OpenApiConfiguration configuration = new OpenApiConfiguration();
        String attributes = attributesFor(configuration,
                configuration.getSwaggerUiVersion(), "sha384-OPERATOR", "sha384-SHIPPED");
        assertTrue(attributes.contains("sha384-OPERATOR"));
        assertFalse(attributes.contains("sha384-SHIPPED"));
    }

    public void testABlankOperatorHashFallsBackRatherThanEmittingNothing() throws Exception {
        OpenApiConfiguration configuration = new OpenApiConfiguration();
        String attributes = attributesFor(configuration,
                configuration.getSwaggerUiVersion(), "   ", "sha384-SHIPPED");
        assertTrue(attributes.contains("sha384-SHIPPED"));
    }

    /** The hashes shipped for the default version must actually be present. */
    public void testTheDefaultVersionShipsRealHashes() throws Exception {
        OpenApiConfiguration configuration = new OpenApiConfiguration();
        String attributes = attributesFor(configuration,
                configuration.getSwaggerUiVersion(), null,
                readShippedHash("DEFAULT_BUNDLE_INTEGRITY"));
        assertTrue("a sha384 SRI hash is expected, was: " + attributes,
                attributes.contains("integrity=\"sha384-"));
    }

    private String readShippedHash(String fieldName) throws Exception {
        java.lang.reflect.Field field = SwaggerUIHandler.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(null);
    }
}
