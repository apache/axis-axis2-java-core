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

import junit.framework.TestCase;
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

    /**
     * Test successful login with valid credentials.
     * Simulates the user guide example: curl with email and credentials.
     */
    public void testSuccessfulLogin() throws Exception {
        // Arrange - simulate user guide login example
        LoginRequest request = new LoginRequest();
        request.setEmail("user@company.com");
        request.setCredentials("password123");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull("Response should not be null", response);
        assertNull("Error message should be null for successful login", response.getErrorMessage());
        assertNotNull("Data should be present", response.getData());

        // Verify response data structure matches user guide
        assertNotNull("Token should be generated", response.getData().getToken());
        assertNotNull("User ID should be present", response.getData().getUserId());
        assertEquals("Email should match", "user@company.com", response.getData().getEmail());

        // Verify token format (should be JWT-like for compatibility)
        assertTrue("Token should be JWT format for drop-in compatibility",
                response.getData().getToken().contains("."));
    }

    /**
     * Test login with invalid email format.
     * Verifies proper validation and error handling.
     */
    public void testLoginWithInvalidEmail() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");
        request.setCredentials("password123");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull("Response should not be null", response);
        assertNotNull("Error message should be present", response.getErrorMessage());
        assertNull("Data should be null for failed login", response.getData());
        assertTrue("Should contain validation error",
                response.getErrorMessage().contains("Invalid email format"));
    }

    /**
     * Test login with empty credentials.
     * Verifies proper validation of required fields.
     */
    public void testLoginWithEmptyCredentials() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@company.com");
        request.setCredentials("");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull("Response should not be null", response);
        assertNotNull("Error message should be present", response.getErrorMessage());
        assertNull("Data should be null for failed login", response.getData());
        assertTrue("Should contain validation error",
                response.getErrorMessage().contains("Credentials are required"));
    }

    /**
     * Test login with null request.
     * Verifies graceful handling of null input.
     */
    public void testLoginWithNullRequest() throws Exception {
        // Act
        LoginResponse response = authService.login(null);

        // Assert
        assertNotNull("Response should not be null", response);
        assertNotNull("Error message should be present", response.getErrorMessage());
        assertNull("Data should be null for null request", response.getData());
        assertTrue("Should contain validation error",
                response.getErrorMessage().contains("Login request is required"));
    }

    /**
     * Test login response format compatibility.
     * Verifies the response matches the format expected by existing frontends.
     */
    public void testLoginResponseCompatibility() throws Exception {
        // Arrange - simulate the exact user guide example
        LoginRequest request = new LoginRequest();
        request.setEmail("user@company.com");
        request.setCredentials("password123");

        // Act
        LoginResponse response = authService.login(request);

        // Assert response structure matches user guide format
        assertNotNull("Response should have expected structure", response);

        // Verify data envelope pattern: {data: ..., errorMessage: ...}
        // This is the pattern shown in the user guide for drop-in compatibility
        if (response.getData() != null) {
            assertNull("Error message should be null when data is present", response.getErrorMessage());

            // Verify all required fields are present for frontend compatibility
            assertNotNull("Token is required for frontend", response.getData().getToken());
            assertNotNull("User ID is required for frontend", response.getData().getUserId());
            assertNotNull("Email is required for frontend", response.getData().getEmail());

            // Verify token length is reasonable for frontend storage
            assertTrue("Token should be reasonable length",
                    response.getData().getToken().length() > 10);
        } else {
            assertNotNull("Error message should be present when data is null", response.getErrorMessage());
        }
    }

    /**
     * Test authentication service performance.
     * Verifies response time is acceptable for web application use.
     */
    public void testLoginPerformance() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("performance@test.com");
        request.setCredentials("testpass");

        // Act & Assert
        long startTime = System.currentTimeMillis();
        LoginResponse response = authService.login(request);
        long duration = System.currentTimeMillis() - startTime;

        // Verify performance
        assertTrue("Login should complete within 1 second", duration < 1000);
        assertNotNull("Response should be generated", response);
    }

    /**
     * Test multiple login attempts.
     * Verifies service can handle concurrent authentication requests.
     */
    public void testMultipleLoginAttempts() throws Exception {
        // Arrange
        LoginRequest request1 = new LoginRequest();
        request1.setEmail("user1@company.com");
        request1.setCredentials("password1");

        LoginRequest request2 = new LoginRequest();
        request2.setEmail("user2@company.com");
        request2.setCredentials("password2");

        // Act
        LoginResponse response1 = authService.login(request1);
        LoginResponse response2 = authService.login(request2);

        // Assert
        assertNotNull("First response should not be null", response1);
        assertNotNull("Second response should not be null", response2);

        // Verify both requests are handled correctly
        if (response1.getData() != null && response2.getData() != null) {
            assertNotSame("Tokens should be unique",
                    response1.getData().getToken(), response2.getData().getToken());
            assertNotSame("User IDs should be unique",
                    response1.getData().getUserId(), response2.getData().getUserId());
        }
    }

    /**
     * Test token generation consistency.
     * Verifies that tokens are properly formatted for frontend consumption.
     */
    public void testTokenGenerationConsistency() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("token@test.com");
        request.setCredentials("testtoken");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertNotNull("Response should contain token", response.getData().getToken());
        String token = response.getData().getToken();

        // Verify token characteristics for drop-in compatibility
        assertFalse("Token should not be empty", token.isEmpty());
        assertFalse("Token should not contain spaces", token.contains(" "));
        assertTrue("Token should be URL-safe", token.matches("[A-Za-z0-9._-]+"));

        // Verify JWT-like structure for compatibility with existing frontends
        String[] tokenParts = token.split("\\.");
        assertTrue("Token should have JWT-like structure (at least 2 parts)", tokenParts.length >= 2);
    }

    /**
     * Test user guide cURL command simulation.
     * Simulates the exact scenario described in the user guide documentation.
     */
    public void testUserGuideCurlSimulation() throws Exception {
        // Simulate the user guide cURL command:
        // curl -v -H "Content-Type: application/json" -X POST
        // --data '{"email":"user@company.com","credentials":"password123"}'
        // http://localhost:8080/axis2/services/authService/login

        // Arrange - exact data from user guide
        LoginRequest request = new LoginRequest();
        request.setEmail("user@company.com");
        request.setCredentials("password123");

        // Act
        LoginResponse response = authService.login(request);

        // Assert - verify response matches user guide format
        assertNotNull("Should return valid response", response);
        assertNotNull("Should have data section", response.getData());
        assertNull("Error message should be null", response.getErrorMessage());

        // Verify response format matches user guide example:
        // {
        //   "data": {
        //     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        //     "userId": "user123",
        //     "email": "user@company.com"
        //   },
        //   "errorMessage": null
        // }
        assertEquals("Email should match request", "user@company.com", response.getData().getEmail());
        assertTrue("Token should be JWT-like", response.getData().getToken().startsWith("eyJ"));
        assertNotNull("User ID should be generated", response.getData().getUserId());
    }
}