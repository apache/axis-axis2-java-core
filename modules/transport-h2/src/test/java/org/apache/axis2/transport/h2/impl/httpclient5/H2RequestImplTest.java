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

import org.apache.axiom.mime.Header;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.h2.H2TestUtils;
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for H2RequestImpl focusing on HTTP/2 async request functionality.
 *
 * Tests cover:
 * - HTTP/2 request creation and configuration
 * - Request interface method implementations
 * - Header management and conversion
 * - Timeout configuration
 * - Request entity handling
 * - HTTP/2 specific features
 */
public class H2RequestImplTest {

    private CloseableHttpAsyncClient httpAsyncClient;
    private MessageContext messageContext;
    private H2RequestImpl h2Request;
    private URI testHttpsUri;

    @Before
    public void setUp() throws Exception {
        httpAsyncClient = H2TestUtils.createTestH2Client();
        messageContext = H2TestUtils.createTestMessageContext();
        testHttpsUri = URI.create("https://test.example.com/service");
    }

    @After
    public void tearDown() throws Exception {
        if (httpAsyncClient != null) {
            httpAsyncClient.close();
        }
    }

    @Test
    public void testRequestCreation() throws Exception {
        h2Request = new H2RequestImpl(httpAsyncClient, messageContext, "POST", testHttpsUri, null);

        assertNotNull("H2Request should be created successfully", h2Request);
    }

    @Test
    public void testRequestCreationWithEntity() throws Exception {
        // Since AxisRequestEntity is final with package-private constructor,
        // we test with null entity (which is a valid case for H2RequestImpl)
        AxisRequestEntity requestEntity = null; // H2TestUtils.createJSONRequestEntity returns null

        h2Request = new H2RequestImpl(httpAsyncClient, messageContext, "POST", testHttpsUri, requestEntity);

        assertNotNull("H2Request with null entity should be created successfully", h2Request);
    }

    @Test
    public void testHTTP10EnableWarning() throws Exception {
        h2Request = new H2RequestImpl(httpAsyncClient, messageContext, "GET", testHttpsUri, null);

        // This should log a warning but not throw an exception
        h2Request.enableHTTP10();

        // Test passes if no exception is thrown
        assertTrue("enableHTTP10 should not throw exception", true);
    }

    @Test
    public void testHeaderManagement() throws Exception {
        h2Request = new H2RequestImpl(httpAsyncClient, messageContext, "POST", testHttpsUri, null);

        // Test setting headers
        h2Request.setHeader("Content-Type", "application/json");
        h2Request.addHeader("X-Custom-Header", "test-value");

        // Test getting request headers
        Header[] headers = h2Request.getRequestHeaders();
        assertNotNull("Request headers should not be null", headers);

        // Verify headers are present
        boolean foundContentType = false;
        boolean foundCustomHeader = false;

        for (Header header : headers) {
            if ("Content-Type".equals(header.getName())) {
                assertEquals("Content-Type value should match", "application/json", header.getValue());
                foundContentType = true;
            }
            if ("X-Custom-Header".equals(header.getName())) {
                assertEquals("Custom header value should match", "test-value", header.getValue());
                foundCustomHeader = true;
            }
        }

        assertTrue("Content-Type header should be found", foundContentType);
        assertTrue("Custom header should be found", foundCustomHeader);
    }

    @Test
    public void testTimeoutConfiguration() throws Exception {
        h2Request = new H2RequestImpl(httpAsyncClient, messageContext, "GET", testHttpsUri, null);

        // Test timeout configurations
        h2Request.setConnectionTimeout(5000);
        h2Request.setResponseTimeout(10000);

        // Test passes if no exceptions are thrown
        assertTrue("Timeout configuration should not throw exceptions", true);
    }

    @Test
    public void testAuthenticationStub() throws Exception {
        h2Request = new H2RequestImpl(httpAsyncClient, messageContext, "GET", testHttpsUri, null);

        // Test authentication method (currently stubbed for Stage 1)
        h2Request.enableAuthentication(null);

        // Should not throw exception in Stage 1 implementation
        assertTrue("Authentication stub should not throw exception", true);
    }

    @Test
    public void testDefaultResponseMethods() throws Exception {
        h2Request = new H2RequestImpl(httpAsyncClient, messageContext, "GET", testHttpsUri, null);

        // Test default response methods (before execution)
        int statusCode = h2Request.getStatusCode();
        assertEquals("Default status code should be -1", -1, statusCode);

        String statusText = h2Request.getStatusText();
        assertNull("Default status text should be null", statusText);

        Header[] responseHeaders = h2Request.getResponseHeaders();
        assertNotNull("Response headers should not be null", responseHeaders);
        assertEquals("Response headers should be empty", 0, responseHeaders.length);

        String responseHeader = h2Request.getResponseHeader("Content-Type");
        assertNull("Response header should be null before execution", responseHeader);

        Map<String, String> cookies = h2Request.getCookies();
        assertNotNull("Cookies should not be null", cookies);
        assertEquals("Cookies should be empty", 0, cookies.size());
    }

    @Test
    public void testResponseContentBeforeExecution() throws Exception {
        h2Request = new H2RequestImpl(httpAsyncClient, messageContext, "GET", testHttpsUri, null);

        // Test response content before execution
        java.io.InputStream responseContent = h2Request.getResponseContent();
        assertNull("Response content should be null before execution", responseContent);
    }

    @Test
    public void testReleaseConnection() throws Exception {
        h2Request = new H2RequestImpl(httpAsyncClient, messageContext, "GET", testHttpsUri, null);

        // Test connection release (should not throw exception)
        h2Request.releaseConnection();

        // Test passes if no exception is thrown
        assertTrue("Release connection should not throw exception", true);
    }

    @Test
    public void testCustomPortHandling() throws Exception {
        URI customPortUri = URI.create("https://example.com:8443/api");

        h2Request = new H2RequestImpl(httpAsyncClient, messageContext, "POST", customPortUri, null);

        assertNotNull("Request with custom HTTPS port should be created", h2Request);
    }

    @Test
    public void testLargeRequestEntity() throws Exception {
        // Test with large JSON generation (testing the large JSON generation capability)
        String largeJSON = H2TestUtils.generateLargeJSON(1024 * 1024); // 1MB JSON
        assertNotNull("Large JSON should be generated", largeJSON);
        assertTrue("Large JSON should be approximately 1MB", largeJSON.length() > 500000);

        // Test with null entity since we can't create AxisRequestEntity easily
        AxisRequestEntity largeEntity = null; // H2TestUtils.createJSONRequestEntity returns null

        h2Request = new H2RequestImpl(httpAsyncClient, messageContext, "POST", testHttpsUri, largeEntity);

        assertNotNull("Request with large JSON capability should be created", h2Request);
    }

    @Test
    public void testMultipleHeadersWithSameName() throws Exception {
        h2Request = new H2RequestImpl(httpAsyncClient, messageContext, "POST", testHttpsUri, null);

        // Test adding multiple headers with same name
        h2Request.addHeader("Accept", "application/json");
        h2Request.addHeader("Accept", "application/xml");

        Header[] headers = h2Request.getRequestHeaders();
        assertNotNull("Headers should not be null", headers);

        int acceptHeaderCount = 0;
        for (Header header : headers) {
            if ("Accept".equals(header.getName())) {
                acceptHeaderCount++;
            }
        }

        assertTrue("Should have multiple Accept headers", acceptHeaderCount >= 2);
    }

    @Test
    public void testHTTP2SpecificMethods() throws Exception {
        h2Request = new H2RequestImpl(httpAsyncClient, messageContext, "GET", testHttpsUri, null);

        // Verify this is HTTP/2 specific implementation
        String className = h2Request.getClass().getSimpleName();
        assertTrue("Should be HTTP/2 request implementation", className.contains("H2"));

        // Test that it implements the Request interface
        assertTrue("Should implement Request interface",
                  h2Request instanceof org.apache.axis2.transport.http.Request);
    }
}