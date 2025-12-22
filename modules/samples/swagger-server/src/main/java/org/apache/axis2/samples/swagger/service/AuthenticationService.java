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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.apache.axis2.samples.swagger.model.LoginRequest;
import org.apache.axis2.samples.swagger.model.LoginResponse;
import org.apache.axis2.samples.swagger.model.UserInfo;
import org.apache.axis2.samples.swagger.model.ErrorResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/**
 * Authentication service for user login and token management.
 * This service handles user authentication and provides JWT tokens for subsequent API calls.
 */
@Path("/bigdataservice")
@Tag(name = "authentication", description = "User authentication and token management")
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    /**
     * Authenticate user and return access token.
     *
     * @param loginRequest The login credentials
     * @return LoginResponse with authentication token or error message
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "User authentication",
        description = "Authenticate user with email and password, returns JWT token on success"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication failed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request format",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public Response authenticateUser(LoginRequest loginRequest) {
        logger.info("Authentication request for user: {}",
            loginRequest != null ? loginRequest.getEmail() : "null");

        try {
            // Validate input
            if (loginRequest == null ||
                loginRequest.getEmail() == null ||
                loginRequest.getCredentials() == null) {

                ErrorResponse error = new ErrorResponse();
                error.setError("BAD_REQUEST");
                error.setMessage("Email and credentials are required");
                error.setTimestamp(Instant.now().toString());

                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
            }

            // For demo purposes, accept any valid email format with "demo" as password
            if (!isValidEmail(loginRequest.getEmail()) ||
                !"demo".equals(loginRequest.getCredentials())) {

                ErrorResponse error = new ErrorResponse();
                error.setError("AUTHENTICATION_FAILED");
                error.setMessage("Invalid email or credentials");
                error.setTimestamp(Instant.now().toString());

                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(error)
                    .build();
            }

            // Create successful response with mock data
            LoginResponse response = new LoginResponse();
            response.setToken(generateMockJwtToken());
            response.setExpiresIn(3600); // 1 hour

            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(UUID.randomUUID().toString());
            userInfo.setEmail(loginRequest.getEmail());
            userInfo.setFirstName("Demo");
            userInfo.setLastName("User");
            userInfo.setRoles(Arrays.asList("USER", "ANALYST"));

            response.setUserInfo(userInfo);
            response.setErrorMessage(null);

            logger.info("Authentication successful for user: {}", loginRequest.getEmail());

            return Response.ok(response).build();

        } catch (Exception e) {
            logger.error("Authentication error for user: {}",
                loginRequest != null ? loginRequest.getEmail() : "null", e);

            ErrorResponse error = new ErrorResponse();
            error.setError("INTERNAL_ERROR");
            error.setMessage("An internal error occurred during authentication");
            error.setTimestamp(Instant.now().toString());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .build();
        }
    }

    /**
     * Validate email format (simple validation for demo)
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    /**
     * Generate mock JWT token for demo purposes
     */
    private String generateMockJwtToken() {
        // In a real implementation, this would use a proper JWT library
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.demo_token_" +
               System.currentTimeMillis() + "." + UUID.randomUUID().toString();
    }
}