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

package org.apache.axis2.samples.swagger.service;

import jakarta.ws.rs.core.Response;
import junit.framework.TestCase;
import org.apache.axis2.samples.swagger.model.ErrorResponse;
import org.apache.axis2.samples.swagger.model.LoginRequest;
import org.apache.axis2.samples.swagger.model.LoginResponse;

/**
 * Unit tests for AuthenticationService.
 * Tests the authentication scenarios described in the OpenAPI REST user guide.
 */
public class AuthenticationServiceTest extends TestCase {

    private AuthenticationService authService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        authService = new AuthenticationService();
    }

    // Helper: call service and return LoginResponse entity (success path)
    private LoginResponse callLogin(LoginRequest request) {
        Response jaxrs = authService.authenticateUser(request);
        return (LoginResponse) jaxrs.getEntity();
    }

    // Helper: call service and return HTTP status code
    private int callLoginStatus(LoginRequest request) {
        return authService.authenticateUser(request).getStatus();
    }

    /**
     * Test successful login with valid credentials.
     * Simulates the user guide example: email and password "demo".
     */
    public void testSuccessfulLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@company.com");
        request.setCredentials("demo");

        Response jaxrs = authService.authenticateUser(request);
        assertEquals("Should return 200 OK", 200, jaxrs.getStatus());

        LoginResponse response = (LoginResponse) jaxrs.getEntity();
        assertNotNull("Response should not be null", response);
        assertNull("Error message should be null for successful login", response.getErrorMessage());
        assertNotNull("Token should be generated", response.getToken());
        assertNotNull("UserInfo should be present", response.getUserInfo());
        assertEquals("Email should match", "user@company.com", response.getUserInfo().getEmail());
        assertNotNull("User ID should be present", response.getUserInfo().getUserId());
        assertTrue("Token should be JWT-like", response.getToken().contains("."));
    }

    /**
     * Test login with invalid email format (no @).
     */
    public void testLoginWithInvalidEmail() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");
        request.setCredentials("demo");

        assertEquals("Should return 401", 401, callLoginStatus(request));
    }

    /**
     * Test login with wrong credentials.
     */
    public void testLoginWithWrongCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@company.com");
        request.setCredentials("wrongpassword");

        assertEquals("Should return 401", 401, callLoginStatus(request));
    }

    /**
     * Test login with null email.
     */
    public void testLoginWithNullEmail() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(null);
        request.setCredentials("demo");

        assertEquals("Should return 400", 400, callLoginStatus(request));
    }

    /**
     * Test login with null credentials.
     */
    public void testLoginWithNullCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@company.com");
        request.setCredentials(null);

        assertEquals("Should return 400", 400, callLoginStatus(request));
    }

    /**
     * Test login with null request.
     */
    public void testLoginWithNullRequest() throws Exception {
        assertEquals("Should return 400", 400, callLoginStatus(null));
    }

    /**
     * Test that tokens are unique across authentication requests.
     */
    public void testTokensAreUnique() throws Exception {
        LoginRequest request1 = new LoginRequest();
        request1.setEmail("user1@company.com");
        request1.setCredentials("demo");

        LoginRequest request2 = new LoginRequest();
        request2.setEmail("user2@company.com");
        request2.setCredentials("demo");

        LoginResponse response1 = callLogin(request1);
        LoginResponse response2 = callLogin(request2);

        assertNotNull("First response should not be null", response1);
        assertNotNull("Second response should not be null", response2);
        assertNotSame("Tokens should be unique", response1.getToken(), response2.getToken());
    }

    /**
     * Test authentication performance.
     * Verifies response time is acceptable for web application use.
     */
    public void testLoginPerformance() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("perf@test.com");
        request.setCredentials("demo");

        long startTime = System.currentTimeMillis();
        Response jaxrs = authService.authenticateUser(request);
        long duration = System.currentTimeMillis() - startTime;

        assertTrue("Login should complete within 1 second", duration < 1000);
        assertEquals("Should return 200", 200, jaxrs.getStatus());
    }

    /**
     * Test that the token format is URL-safe (suitable for JWT use).
     */
    public void testTokenFormat() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("token@test.com");
        request.setCredentials("demo");

        LoginResponse response = callLogin(request);
        assertNotNull("Should get a response", response);
        String token = response.getToken();
        assertNotNull("Token should not be null", token);
        assertFalse("Token should not be empty", token.isEmpty());
        String[] parts = token.split("\\.");
        assertTrue("Token should have JWT-like structure", parts.length >= 2);
    }
}
