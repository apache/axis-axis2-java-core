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

package org.apache.axis2.samples.swagger.model;

import com.squareup.moshi.Json;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Login request model containing user credentials.
 */
@Schema(description = "User login request")
public class LoginRequest {

    @Schema(description = "User email address", required = true, example = "user@example.com")
    @Json(name = "email")
    private String email;

    @Schema(description = "User password or authentication credentials", required = true)
    @Json(name = "credentials")
    private String credentials;

    /**
     * Default constructor for JSON deserialization
     */
    public LoginRequest() {
    }

    /**
     * Constructor with fields
     */
    public LoginRequest(String email, String credentials) {
        this.email = email;
        this.credentials = credentials;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", credentials='[PROTECTED]'" +
                '}';
    }
}