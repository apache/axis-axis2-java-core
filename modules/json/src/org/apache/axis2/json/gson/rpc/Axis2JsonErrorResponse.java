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

package org.apache.axis2.json.gson.rpc;

import java.time.Instant;
import java.util.UUID;

/**
 * Structured JSON error response for Axis2 JSON-RPC services.
 *
 * <p>This is the canonical error envelope that every JSON-RPC error path
 * serializes to the client.  It replaces the previous pattern of returning
 * HTTP 200 with {@code {"status":"FAILED","error_message":"..."}} inside
 * domain-specific response objects (e.g. {@code PortfolioVarianceResponse.failed()}).
 *
 * <h3>Error taxonomy</h3>
 * <ul>
 *   <li>400 BAD_REQUEST — malformed JSON or missing required envelope fields</li>
 *   <li>422 VALIDATION_ERROR — valid JSON, fails business-rule checks</li>
 *   <li>429 RATE_LIMITED — too many requests; {@code retryAfter} tells when to retry</li>
 *   <li>500 INTERNAL_ERROR — opaque; real details in server log keyed by errorRef</li>
 *   <li>503 SERVICE_UNAVAILABLE — downstream dependency or overload</li>
 * </ul>
 *
 * <p>The {@code errorRef} UUID is logged server-side for correlation
 * while keeping client-facing messages safe from information disclosure (CWE-209).
 * For 500/400, only the errorRef is exposed; for 422, the full validation
 * message is safe to return since it describes the caller's own input.
 *
 * <h3>Integration points</h3>
 * <ul>
 *   <li>{@link JsonRpcMessageReceiver} — catches {@link JsonRpcFaultException},
 *       sets this as RETURN_OBJECT + HTTP status on the outgoing MessageContext</li>
 *   <li>{@link JsonUtils#createSecureFault} — builds this for parse/internal errors</li>
 *   <li>OpenAPI spec — {@code components/schemas/ErrorResponse} mirrors this shape</li>
 *   <li>MCP catalog — {@code _meta.errorContract} describes these fields</li>
 * </ul>
 *
 * <p>Wire format:
 * <pre>{@code
 * {
 *   "error": "VALIDATION_ERROR",
 *   "message": "weights sum to 1.60000000, expected 1.0",
 *   "errorRef": "a1b2c3d4-...",
 *   "timestamp": "2026-05-04T12:00:00Z",
 *   "retryAfter": null
 * }
 * }</pre>
 *
 * @see JsonRpcFaultException
 * @see JsonRpcMessageReceiver#invokeService
 */
public class Axis2JsonErrorResponse {

    /** Error code — e.g. VALIDATION_ERROR, RATE_LIMITED, SERVICE_UNAVAILABLE, INTERNAL_ERROR */
    private String error;

    /** Human-readable error message */
    private String message;

    /** Opaque correlation ID for server-side log lookup */
    private String errorRef;

    /** ISO 8601 timestamp of when the error occurred */
    private String timestamp;

    /** Optional: seconds until the client should retry (for 429/503) */
    private Integer retryAfter;

    public Axis2JsonErrorResponse() {
    }

    public Axis2JsonErrorResponse(String error, String message, String errorRef,
                                   String timestamp, Integer retryAfter) {
        this.error = error;
        this.message = message;
        this.errorRef = errorRef;
        this.timestamp = timestamp;
        this.retryAfter = retryAfter;
    }

    /**
     * Build a validation error (HTTP 422).
     */
    public static Axis2JsonErrorResponse validationError(String message) {
        return new Axis2JsonErrorResponse(
                "VALIDATION_ERROR", message,
                UUID.randomUUID().toString(),
                Instant.now().toString(), null);
    }

    /**
     * Build a rate-limit error (HTTP 429).
     */
    public static Axis2JsonErrorResponse rateLimited(String message, int retryAfterSeconds) {
        return new Axis2JsonErrorResponse(
                "RATE_LIMITED", message,
                UUID.randomUUID().toString(),
                Instant.now().toString(), retryAfterSeconds);
    }

    /**
     * Build a service-unavailable error (HTTP 503).
     */
    public static Axis2JsonErrorResponse serviceUnavailable(String message, Integer retryAfterSeconds) {
        return new Axis2JsonErrorResponse(
                "SERVICE_UNAVAILABLE", message,
                UUID.randomUUID().toString(),
                Instant.now().toString(), retryAfterSeconds);
    }

    /**
     * Build an internal error (HTTP 500). The message is kept opaque;
     * full context is in server logs keyed by errorRef.
     *
     * <p>Unlike validationError(), the errorRef is passed IN rather than
     * generated here — the caller (JsonUtils.createSecureFault) generates
     * it first so the same UUID appears in both the log line and the response.
     */
    public static Axis2JsonErrorResponse internalError(String errorRef) {
        return new Axis2JsonErrorResponse(
                "INTERNAL_ERROR",
                "Internal Server Error [errorRef=" + errorRef + "]",
                errorRef,
                Instant.now().toString(), null);
    }

    /**
     * Build a bad-request / parse error (HTTP 400).
     */
    public static Axis2JsonErrorResponse badRequest(String errorRef) {
        return new Axis2JsonErrorResponse(
                "BAD_REQUEST",
                "Bad Request [errorRef=" + errorRef + "]",
                errorRef,
                Instant.now().toString(), null);
    }

    // ── Getters / setters ────────────────────────────────────────────────────

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErrorRef() { return errorRef; }
    public void setErrorRef(String errorRef) { this.errorRef = errorRef; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Integer getRetryAfter() { return retryAfter; }
    public void setRetryAfter(Integer retryAfter) { this.retryAfter = retryAfter; }

    @Override
    public String toString() {
        return "Axis2JsonErrorResponse{" +
                "error='" + error + '\'' +
                ", message='" + message + '\'' +
                ", errorRef='" + errorRef + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", retryAfter=" + retryAfter +
                '}';
    }
}
