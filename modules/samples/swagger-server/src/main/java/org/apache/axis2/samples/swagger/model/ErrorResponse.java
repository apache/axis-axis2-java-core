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
 * Error response model for API errors.
 */
@Schema(description = "Error response")
public class ErrorResponse {

    @Schema(description = "Error code")
    @Json(name = "error")
    private String error;

    @Schema(description = "Error message")
    @Json(name = "message")
    private String message;

    @Schema(description = "Error timestamp", format = "date-time")
    @Json(name = "timestamp")
    private String timestamp;

    /**
     * Default constructor for JSON serialization
     */
    public ErrorResponse() {
    }

    /**
     * Constructor with all fields
     */
    public ErrorResponse(String error, String message, String timestamp) {
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "error='" + error + '\'' +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}