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
 * Login response model containing authentication token and user information.
 */
@Schema(description = "User login response")
public class LoginResponse {

    @Schema(description = "JWT authentication token")
    @Json(name = "token")
    private String token;

    @Schema(description = "Token expiration time in seconds", example = "3600")
    @Json(name = "expiresIn")
    private Integer expiresIn;

    @Schema(description = "Authenticated user information")
    @Json(name = "userInfo")
    private UserInfo userInfo;

    @Schema(description = "Error message if authentication failed", nullable = true)
    @Json(name = "errorMessage")
    private String errorMessage;

    /**
     * Default constructor for JSON serialization
     */
    public LoginResponse() {
    }

    /**
     * Constructor for successful authentication
     */
    public LoginResponse(String token, Integer expiresIn, UserInfo userInfo) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
        this.errorMessage = null;
    }

    /**
     * Constructor for failed authentication
     */
    public LoginResponse(String errorMessage) {
        this.token = null;
        this.expiresIn = null;
        this.userInfo = null;
        this.errorMessage = errorMessage;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='" + (token != null ? "[TOKEN_PROVIDED]" : null) + '\'' +
                ", expiresIn=" + expiresIn +
                ", userInfo=" + userInfo +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}