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
package org.apache.axis2.fuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

/**
 * OSS-Fuzz compatible target for Gson JSON parsing.
 *
 * Equivalent to Axis2/C fuzz_json_parser.c - tests JSON parsing for:
 * - Deep nesting stack exhaustion (similar to CVE-2024-57699 in json-smart)
 * - Integer overflow in size calculations
 * - Malformed JSON handling
 * - Memory exhaustion from large payloads
 * - Unicode handling issues
 *
 * Axis2/Java uses Gson for JSON processing in the json module.
 *
 * @see <a href="https://google.github.io/oss-fuzz/">OSS-Fuzz</a>
 */
public class JsonParserFuzzer {

    /** Maximum input size to prevent OOM (10MB matches Axis2/C) */
    private static final int MAX_INPUT_SIZE = 10 * 1024 * 1024;

    /** Maximum depth for nested structures */
    private static final int MAX_DEPTH = 64;

    /** Maximum iterations for object/array traversal */
    private static final int MAX_ITERATIONS = 1000;

    /** Gson instance with lenient parsing */
    private static final Gson gson = new GsonBuilder()
        .setLenient()
        .create();

    /**
     * Jazzer entry point - called millions of times with random/mutated data.
     *
     * @param data Fuzzed data provider for generating test inputs
     */
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        byte[] jsonBytes = data.consumeBytes(MAX_INPUT_SIZE);

        if (jsonBytes.length == 0) {
            return;
        }

        String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);

        try {
            parseAndExercise(jsonString);
        } catch (JsonSyntaxException e) {
            // Expected for malformed JSON - not a bug
        } catch (IllegalStateException e) {
            // Expected for parser state issues - not a bug
        } catch (NumberFormatException e) {
            // Expected for invalid numbers - not a bug
        } catch (Exception e) {
            if (isSecurityRelevant(e)) {
                throw e; // Re-throw security-relevant exceptions
            }
        }
    }

    /**
     * Parse JSON and exercise the resulting structure.
     */
    private static void parseAndExercise(String jsonString) {
        // Method 1: Parse with JsonParser
        JsonElement element = JsonParser.parseString(jsonString);

        if (element != null) {
            exerciseElement(element, 0);
        }

        // Method 2: Parse with JsonReader (streaming)
        try (JsonReader reader = new JsonReader(new StringReader(jsonString))) {
            reader.setLenient(true);
            // Peek to trigger parsing
            reader.peek();
        } catch (Exception e) {
            // Expected for some inputs
        }
    }

    /**
     * Exercise parsed JSON element to trigger potential issues.
     */
    private static void exerciseElement(JsonElement element, int depth) {
        if (depth > MAX_DEPTH) {
            return; // Prevent stack overflow from deep recursion
        }

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();

            int count = 0;
            for (Map.Entry<String, JsonElement> entry : entries) {
                if (count >= MAX_ITERATIONS) break;

                String key = entry.getKey();
                JsonElement value = entry.getValue();

                // Exercise the value
                exerciseElement(value, depth + 1);
                count++;
            }

        } else if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            int size = arr.size();

            for (int i = 0; i < size && i < MAX_ITERATIONS; i++) {
                JsonElement item = arr.get(i);
                exerciseElement(item, depth + 1);
            }

        } else if (element.isJsonPrimitive()) {
            // Exercise primitive value getters
            try {
                if (element.getAsJsonPrimitive().isBoolean()) {
                    boolean b = element.getAsBoolean();
                } else if (element.getAsJsonPrimitive().isNumber()) {
                    // Try various numeric conversions
                    try { int i = element.getAsInt(); } catch (NumberFormatException e) {}
                    try { long l = element.getAsLong(); } catch (NumberFormatException e) {}
                    try { double d = element.getAsDouble(); } catch (NumberFormatException e) {}
                } else if (element.getAsJsonPrimitive().isString()) {
                    String s = element.getAsString();
                }
            } catch (Exception e) {
                // Type conversion failures are expected
            }
        }

        // Test serialization
        try {
            String serialized = gson.toJson(element);
        } catch (Exception e) {
            // Serialization failures are expected for some inputs
        }
    }

    /**
     * Check if a throwable indicates a potential security issue.
     */
    private static boolean isSecurityRelevant(Throwable e) {
        return e instanceof OutOfMemoryError
            || e instanceof StackOverflowError;
    }

    /**
     * Main method for standalone testing outside Jazzer.
     */
    public static void main(String[] args) {
        // Test with various JSON inputs
        String[] testCases = {
            "{\"key\": \"value\"}",
            "[1, 2, 3]",
            "{\"nested\": {\"deep\": {\"value\": 123}}}",
            "\"string\"",
            "12345",
            "true",
            "null"
        };

        for (String json : testCases) {
            try {
                parseAndExercise(json);
                System.out.println("Passed: " + json.substring(0, Math.min(30, json.length())));
            } catch (Exception e) {
                System.err.println("Failed: " + json + " - " + e.getMessage());
            }
        }
        System.out.println("JSON parsing tests completed");
    }
}
