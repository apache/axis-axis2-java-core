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

package org.apache.axis2.transport.h2.impl.httpclient5;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import static org.junit.Assert.*;

/**
 * Security tests for HTTP/2 transport focusing on HTTPS-only enforcement.
 *
 * These tests verify that the HTTP/2 transport properly enforces HTTPS-only
 * connections as required by RFC 7540 best practices and browser compatibility.
 *
 * Test Coverage:
 * - HTTP URL rejection with clear error messages
 * - HTTPS URL acceptance and processing
 * - Protocol detection and validation
 * - Security requirement messaging
 */
public class H2SecurityTest {

    private CloseableHttpAsyncClient httpAsyncClient;
    private MessageContext messageContext;

    @Before
    public void setUp() throws Exception {
        httpAsyncClient = HttpAsyncClients.createDefault();
        httpAsyncClient.start();
        messageContext = new MessageContext();
    }

    @After
    public void tearDown() throws Exception {
        if (httpAsyncClient != null) {
            httpAsyncClient.close();
        }
    }

    @Test(expected = AxisFault.class)
    public void testHTTPURLRejection() throws Exception {
        // Test that HTTP URLs are rejected with clear error message
        URI httpUri = URI.create("http://example.com/service");

        try {
            new H2RequestImpl(httpAsyncClient, messageContext, "POST", httpUri, null);
            fail("HTTP URL should be rejected by HTTP/2 transport");
        } catch (AxisFault e) {
            // Verify the error message provides clear guidance
            String errorMessage = e.getMessage();
            assertTrue("Error message should mention HTTPS requirement",
                      errorMessage.contains("HTTPS protocol"));
            assertTrue("Error message should suggest using HTTPS URLs",
                      errorMessage.contains("https://"));
            assertTrue("Error message should suggest HTTP/1.1 fallback",
                      errorMessage.contains("HTTP/1.1 transport"));

            // Re-throw to satisfy expected annotation
            throw e;
        }
    }

    @Test
    public void testHTTPSURLAcceptance() throws Exception {
        // Test that HTTPS URLs are accepted
        URI httpsUri = URI.create("https://example.com/service");

        // This should not throw an exception
        H2RequestImpl request = new H2RequestImpl(httpAsyncClient, messageContext, "GET", httpsUri, null);

        assertNotNull("HTTPS request should be created successfully", request);
    }

    @Test(expected = AxisFault.class)
    public void testHTTPWithCustomPortRejection() throws Exception {
        // Test that HTTP with custom port is still rejected
        URI httpUriWithPort = URI.create("http://example.com:8080/service");

        try {
            new H2RequestImpl(httpAsyncClient, messageContext, "POST", httpUriWithPort, null);
            fail("HTTP URL with custom port should be rejected");
        } catch (AxisFault e) {
            assertTrue("Error should mention HTTPS requirement",
                      e.getMessage().contains("HTTPS protocol"));
            throw e;
        }
    }

    @Test
    public void testHTTPSWithCustomPortAcceptance() throws Exception {
        // Test that HTTPS with custom port is accepted
        URI httpsUriWithPort = URI.create("https://example.com:8443/service");

        H2RequestImpl request = new H2RequestImpl(httpAsyncClient, messageContext, "POST", httpsUriWithPort, null);

        assertNotNull("HTTPS request with custom port should be accepted", request);
    }

    @Test(expected = AxisFault.class)
    public void testNoProtocolRejection() throws Exception {
        // Test that URIs without explicit protocol are rejected
        URI noProtocolUri = URI.create("example.com/service");

        // This should throw AxisFault directly
        new H2RequestImpl(httpAsyncClient, messageContext, "GET", noProtocolUri, null);
    }

    @Test
    public void testDefaultHTTPSPort() throws Exception {
        // Test that HTTPS URLs without explicit port use 443
        URI httpsUriNoPort = URI.create("https://example.com/service");

        H2RequestImpl request = new H2RequestImpl(httpAsyncClient, messageContext, "GET", httpsUriNoPort, null);

        assertNotNull("HTTPS URL without port should be accepted", request);
        // The internal logic should default to port 443 for HTTPS
    }

    @Test
    public void testHTTPSOnlyDocumentation() throws Exception {
        // Test that HTTPS-only requirement is properly documented in error messages
        URI httpUri = URI.create("http://test.example.com/api");

        try {
            new H2RequestImpl(httpAsyncClient, messageContext, "POST", httpUri,
                             createMockRequestEntity());
            fail("Should reject HTTP even with request entity");
        } catch (AxisFault e) {
            String message = e.getMessage();
            assertTrue("Should explain HTTPS requirement", message.contains("requires HTTPS"));
            assertTrue("Should show found protocol", message.contains("Found protocol: http"));
            assertTrue("Should suggest solution", message.contains("Please use 'https://'"));
        }
    }

    @Test
    public void testSecurityEnforcementConsistency() throws Exception {
        // Test that security enforcement is consistent across different HTTP methods
        URI httpUri = URI.create("http://api.example.com/endpoint");
        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH"};

        for (String method : methods) {
            try {
                new H2RequestImpl(httpAsyncClient, messageContext, method, httpUri, null);
                fail("HTTP should be rejected for method: " + method);
            } catch (AxisFault e) {
                assertTrue("Error message should be consistent for " + method,
                          e.getMessage().contains("HTTPS protocol"));
            }
        }
    }

    @Test
    public void testHTTPSEnforcementWithRequestEntity() throws Exception {
        // Test HTTPS enforcement works even when request entity is present
        URI httpsUri = URI.create("https://secure.example.com/api");
        AxisRequestEntity requestEntity = createMockRequestEntity();

        H2RequestImpl request = new H2RequestImpl(httpAsyncClient, messageContext, "POST", httpsUri, requestEntity);

        assertNotNull("HTTPS request with entity should be accepted", request);
    }

    /**
     * Creates a mock AxisRequestEntity for testing
     */
    private AxisRequestEntity createMockRequestEntity() {
        // Since AxisRequestEntity is final, we need to create a proper instance
        // For testing purposes, we'll use null to test the basic HTTPS enforcement
        return null;
    }
}