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

package org.apache.axis2.json.rpc;

/**
 * Exception thrown by JSON-RPC service methods to signal a structured error
 * with a specific HTTP status code.
 *
 * <h3>How it flows through the stack</h3>
 * <ol>
 *   <li>Service method (e.g. {@code FinancialBenchmarkService.portfolioVariance()})
 *       throws this on input validation failure</li>
 *   <li>{@code Method.invoke()} wraps it in {@link java.lang.reflect.InvocationTargetException}</li>
 *   <li>{@link JsonRpcMessageReceiver#invokeService} unwraps the ITE, detects this type,
 *       and sets:
 *       <ul>
 *         <li>{@code Constants.HTTP_RESPONSE_STATE} → the HTTP status (e.g. 422)</li>
 *         <li>{@code JsonConstant.RETURN_OBJECT} → the {@link Axis2JsonErrorResponse}</li>
 *       </ul>
 *       The response then flows through the normal JSON formatter path (not the
 *       SOAP fault path), producing a clean JSON error envelope.</li>
 * </ol>
 *
 * <h3>Why a checked exception instead of return-value error patterns</h3>
 * Returning {@code XxxResponse.failed(msg)} with HTTP 200 made it impossible
 * for intermediaries (proxies, MCP clients) to distinguish errors from success
 * without parsing the body.  Throwing produces proper HTTP semantics:
 * 422 for validation, 429 for rate-limit, 503 for unavailability.
 *
 * <p>Usage in a service method:
 * <pre>{@code
 * if (request.getWeights() == null) {
 *     throw JsonRpcFaultException.validationError(
 *         "Missing required field: \"weights\" array");
 * }
 * }</pre>
 *
 * @see Axis2JsonErrorResponse
 * @see JsonRpcMessageReceiver#invokeService
 */
public class JsonRpcFaultException extends Exception {

    private final int httpStatusCode;
    private final Axis2JsonErrorResponse errorResponse;

    public JsonRpcFaultException(int httpStatusCode, Axis2JsonErrorResponse errorResponse) {
        super(errorResponse.getMessage());
        this.httpStatusCode = httpStatusCode;
        this.errorResponse = errorResponse;
    }

    public JsonRpcFaultException(int httpStatusCode, Axis2JsonErrorResponse errorResponse,
                                  Throwable cause) {
        super(errorResponse.getMessage(), cause);
        this.httpStatusCode = httpStatusCode;
        this.errorResponse = errorResponse;
    }

    /**
     * Convenience: throw a 422 validation error.
     */
    public static JsonRpcFaultException validationError(String message) {
        return new JsonRpcFaultException(422, Axis2JsonErrorResponse.validationError(message));
    }

    /**
     * Convenience: throw a 429 rate-limit error.
     */
    public static JsonRpcFaultException rateLimited(String message, int retryAfterSeconds) {
        return new JsonRpcFaultException(429,
                Axis2JsonErrorResponse.rateLimited(message, retryAfterSeconds));
    }

    /**
     * Convenience: throw a 503 service-unavailable error.
     */
    public static JsonRpcFaultException serviceUnavailable(String message,
                                                            Integer retryAfterSeconds) {
        return new JsonRpcFaultException(503,
                Axis2JsonErrorResponse.serviceUnavailable(message, retryAfterSeconds));
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public Axis2JsonErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
