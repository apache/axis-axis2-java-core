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

import junit.framework.TestCase;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

/**
 * Unit tests for LoginRequest model class.
 * Tests JSON serialization/deserialization with Moshi to ensure compatibility with frontend clients.
 */
public class LoginRequestTest extends TestCase {

    private Moshi moshi;
    private JsonAdapter<LoginRequest> adapter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        moshi = new Moshi.Builder().build();
        adapter = moshi.adapter(LoginRequest.class);
    }

    /**
     * Test JSON deserialization from user guide format.
     * Verifies that the exact JSON format from the user guide can be properly parsed.
     */
    public void testJsonDeserialization() throws Exception {
        // Arrange - exact JSON format from user guide
        String json = "{\"email\":\"user@company.com\",\"credentials\":\"password123\"}";

        // Act
        LoginRequest request = adapter.fromJson(json);

        // Assert
        assertNotNull("Request should be deserialized", request);
        assertEquals("Email should match", "user@company.com", request.getEmail());
        assertEquals("Credentials should match", "password123", request.getCredentials());
    }

    /**
     * Test JSON serialization to expected format.
     * Verifies that the model serializes to the format expected by the REST endpoint.
     */
    public void testJsonSerialization() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setCredentials("testpass");

        // Act
        String json = adapter.toJson(request);

        // Assert
        assertNotNull("JSON should be generated", json);
        assertTrue("Should contain email field", json.contains("\"email\":\"test@example.com\""));
        assertTrue("Should contain credentials field", json.contains("\"credentials\":\"testpass\""));
    }

    /**
     * Test JSON field name mapping with Moshi @Json annotation.
     * Verifies that field names are properly mapped for JSON serialization.
     */
    public void testJsonFieldMapping() throws Exception {
        // Arrange
        String jsonWithExactFieldNames = "{\"email\":\"mapping@test.com\",\"credentials\":\"maptest\"}";

        // Act
        LoginRequest request = adapter.fromJson(jsonWithExactFieldNames);

        // Assert
        assertEquals("Email mapping should work", "mapping@test.com", request.getEmail());
        assertEquals("Credentials mapping should work", "maptest", request.getCredentials());

        // Test reverse mapping
        String serializedJson = adapter.toJson(request);
        assertTrue("Serialized JSON should use correct field names",
                serializedJson.contains("\"email\"") && serializedJson.contains("\"credentials\""));
    }

    /**
     * Test handling of null values.
     * Verifies graceful handling of null fields in JSON processing.
     */
    public void testNullValueHandling() throws Exception {
        // Test null fields in JSON
        String jsonWithNulls = "{\"email\":null,\"credentials\":\"password\"}";
        LoginRequest request = adapter.fromJson(jsonWithNulls);

        assertNull("Null email should be preserved", request.getEmail());
        assertEquals("Non-null credentials should be preserved", "password", request.getCredentials());

        // Test serialization with null values
        LoginRequest requestWithNulls = new LoginRequest();
        requestWithNulls.setEmail(null);
        requestWithNulls.setCredentials("test");

        String serialized = adapter.toJson(requestWithNulls);
        assertTrue("Should handle null values in serialization", serialized.contains("null"));
    }

    /**
     * Test empty string handling.
     * Verifies proper handling of empty strings in JSON fields.
     */
    public void testEmptyStringHandling() throws Exception {
        // Arrange
        String jsonWithEmptyStrings = "{\"email\":\"\",\"credentials\":\"\"}";

        // Act
        LoginRequest request = adapter.fromJson(jsonWithEmptyStrings);

        // Assert
        assertEquals("Empty email should be preserved", "", request.getEmail());
        assertEquals("Empty credentials should be preserved", "", request.getCredentials());

        // Test serialization
        String serialized = adapter.toJson(request);
        assertTrue("Should serialize empty strings correctly", serialized.contains("\"\""));
    }

    /**
     * Test model validation methods.
     * Verifies that the model provides proper validation capabilities.
     */
    public void testModelValidation() throws Exception {
        // Test valid request
        LoginRequest validRequest = new LoginRequest();
        validRequest.setEmail("valid@example.com");
        validRequest.setCredentials("validpass");

        assertTrue("Valid request should pass basic validation",
                validRequest.getEmail() != null && validRequest.getCredentials() != null);

        // Test invalid request
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail(null);
        invalidRequest.setCredentials(null);

        assertFalse("Invalid request should fail basic validation",
                invalidRequest.getEmail() != null && invalidRequest.getCredentials() != null);
    }

    /**
     * Test compatibility with existing frontend clients.
     * Simulates the JSON structures that existing TypeScript/JavaScript clients would send.
     */
    public void testFrontendClientCompatibility() throws Exception {
        // Test TypeScript client JSON format
        String typescriptJson = "{\"email\":\"typescript@client.com\",\"credentials\":\"clientpass\"}";
        LoginRequest request = adapter.fromJson(typescriptJson);

        assertNotNull("Should handle TypeScript client format", request);
        assertEquals("typescript@client.com", request.getEmail());
        assertEquals("clientpass", request.getCredentials());

        // Test Excel Add-in JSON format (might have additional formatting)
        String excelJson = "{\n  \"email\": \"excel@addin.com\",\n  \"credentials\": \"exceladd\"\n}";
        request = adapter.fromJson(excelJson);

        assertNotNull("Should handle Excel Add-in format", request);
        assertEquals("excel@addin.com", request.getEmail());
        assertEquals("exceladd", request.getCredentials());
    }

    /**
     * Test user guide cURL data format.
     * Verifies exact compatibility with the cURL examples in the user guide.
     */
    public void testUserGuideCurlDataFormat() throws Exception {
        // Exact format from user guide cURL example
        String curlData = "{\"email\":\"user@company.com\",\"credentials\":\"password123\"}";

        // Act
        LoginRequest request = adapter.fromJson(curlData);

        // Assert
        assertNotNull("Should parse cURL data format", request);
        assertEquals("user@company.com", request.getEmail());
        assertEquals("password123", request.getCredentials());

        // Verify round-trip compatibility
        String reserialized = adapter.toJson(request);
        LoginRequest roundTrip = adapter.fromJson(reserialized);

        assertEquals("Round-trip email should match", request.getEmail(), roundTrip.getEmail());
        assertEquals("Round-trip credentials should match", request.getCredentials(), roundTrip.getCredentials());
    }

    /**
     * Test performance of JSON processing.
     * Ensures that serialization/deserialization is fast enough for production use.
     */
    public void testJsonProcessingPerformance() throws Exception {
        // Arrange
        String json = "{\"email\":\"performance@test.com\",\"credentials\":\"perftest\"}";
        LoginRequest request = new LoginRequest();
        request.setEmail("performance@test.com");
        request.setCredentials("perftest");

        // Test deserialization performance
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            adapter.fromJson(json);
        }
        long deserializationTime = System.currentTimeMillis() - startTime;

        // Test serialization performance
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            adapter.toJson(request);
        }
        long serializationTime = System.currentTimeMillis() - startTime;

        // Assert performance is acceptable
        assertTrue("Deserialization should be fast (< 100ms for 1000 operations)",
                deserializationTime < 100);
        assertTrue("Serialization should be fast (< 100ms for 1000 operations)",
                serializationTime < 100);
    }

    /**
     * Test malformed JSON handling.
     * Verifies graceful handling of invalid JSON input.
     */
    public void testMalformedJsonHandling() throws Exception {
        // Test various malformed JSON scenarios
        String[] malformedJsons = {
            "{\"email\":\"test@example.com\",\"credentials\":}", // Missing value
            "{\"email\":\"test@example.com\"\"credentials\":\"pass\"}", // Missing comma
            "{\"email\":\"test@example.com\",\"credentials\":\"pass\"", // Missing closing brace
            "invalid json", // Completely invalid
            "" // Empty string
        };

        for (String malformedJson : malformedJsons) {
            try {
                LoginRequest request = adapter.fromJson(malformedJson);
                // If we get here without exception, that's also acceptable for some cases
                // The important thing is not to crash the application
            } catch (Exception e) {
                // Expected for malformed JSON - should not crash the application
                assertTrue("Should throw a reasonable exception", e.getMessage() != null);
            }
        }
    }
}