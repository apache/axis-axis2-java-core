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

import java.util.List;

/**
 * User information model containing user profile data.
 */
@Schema(description = "User information")
public class UserInfo {

    @Schema(description = "Unique user identifier")
    @Json(name = "userId")
    private String userId;

    @Schema(description = "User email address", format = "email")
    @Json(name = "email")
    private String email;

    @Schema(description = "User first name")
    @Json(name = "firstName")
    private String firstName;

    @Schema(description = "User last name")
    @Json(name = "lastName")
    private String lastName;

    @Schema(description = "User roles")
    @Json(name = "roles")
    private List<String> roles;

    /**
     * Default constructor for JSON serialization
     */
    public UserInfo() {
    }

    /**
     * Constructor with all fields
     */
    public UserInfo(String userId, String email, String firstName, String lastName, List<String> roles) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", roles=" + roles +
                '}';
    }
}