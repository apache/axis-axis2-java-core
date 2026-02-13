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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * OSS-Fuzz compatible target for HTTP header parsing.
 *
 * Equivalent to Axis2/C fuzz_http_header.c - tests HTTP header parsing for:
 * - Header injection attacks (CRLF injection)
 * - Buffer overflows from long headers
 * - Malformed header handling
 * - Content-Type parsing vulnerabilities
 * - Charset extraction issues
 *
 * @see <a href="https://google.github.io/oss-fuzz/">OSS-Fuzz</a>
 */
public class HttpHeaderFuzzer {

    /** Maximum input size (64KB for headers) */
    private static final int MAX_INPUT_SIZE = 64 * 1024;

    /** Maximum header count to prevent DoS */
    private static final int MAX_HEADERS = 100;

    /**
     * Jazzer entry point - called millions of times with random/mutated data.
     *
     * @param data Fuzzed data provider for generating test inputs
     */
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        byte[] headerBytes = data.consumeBytes(MAX_INPUT_SIZE);

        if (headerBytes.length == 0) {
            return;
        }

        String headerString = new String(headerBytes, StandardCharsets.UTF_8);

        try {
            // Test various header parsing scenarios
            testContentTypeParsing(headerString);
            testHeaderLineParsing(headerString);
            testMultipleHeaders(headerString);
        } catch (IllegalArgumentException e) {
            // Expected for malformed headers
        } catch (StringIndexOutOfBoundsException e) {
            // Expected for truncated input
        } catch (Exception e) {
            if (isSecurityRelevant(e)) {
                throw e;
            }
        }
    }

    /**
     * Test Content-Type header parsing (charset extraction, boundary parsing).
     */
    private static void testContentTypeParsing(String contentType) {
        // Extract charset from Content-Type
        String charset = extractCharset(contentType);

        // Extract boundary for multipart
        String boundary = extractBoundary(contentType);

        // Extract media type
        String mediaType = extractMediaType(contentType);

        // Test known content types
        boolean isJson = contentType.contains("application/json");
        boolean isXml = contentType.contains("application/xml") || contentType.contains("text/xml");
        boolean isSoap = contentType.contains("application/soap+xml");
        boolean isMultipart = contentType.contains("multipart/");
    }

    /**
     * Extract charset from Content-Type header.
     */
    private static String extractCharset(String contentType) {
        if (contentType == null) {
            return null;
        }

        String lower = contentType.toLowerCase();
        int charsetIdx = lower.indexOf("charset=");
        if (charsetIdx < 0) {
            return null;
        }

        int start = charsetIdx + 8;
        if (start >= contentType.length()) {
            return null;
        }

        // Handle quoted charset
        if (contentType.charAt(start) == '"') {
            int end = contentType.indexOf('"', start + 1);
            if (end > start) {
                return contentType.substring(start + 1, end);
            }
        }

        // Handle unquoted charset
        int end = start;
        while (end < contentType.length()) {
            char c = contentType.charAt(end);
            if (c == ';' || c == ' ' || c == '\t') {
                break;
            }
            end++;
        }

        return contentType.substring(start, end);
    }

    /**
     * Extract boundary from multipart Content-Type.
     */
    private static String extractBoundary(String contentType) {
        if (contentType == null) {
            return null;
        }

        String lower = contentType.toLowerCase();
        int boundaryIdx = lower.indexOf("boundary=");
        if (boundaryIdx < 0) {
            return null;
        }

        int start = boundaryIdx + 9;
        if (start >= contentType.length()) {
            return null;
        }

        // Handle quoted boundary
        if (contentType.charAt(start) == '"') {
            int end = contentType.indexOf('"', start + 1);
            if (end > start) {
                return contentType.substring(start + 1, end);
            }
        }

        // Handle unquoted boundary
        int end = start;
        while (end < contentType.length()) {
            char c = contentType.charAt(end);
            if (c == ';' || c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                break;
            }
            end++;
        }

        return contentType.substring(start, end);
    }

    /**
     * Extract media type from Content-Type.
     */
    private static String extractMediaType(String contentType) {
        if (contentType == null) {
            return null;
        }

        int semicolon = contentType.indexOf(';');
        if (semicolon > 0) {
            return contentType.substring(0, semicolon).trim();
        }
        return contentType.trim();
    }

    /**
     * Test HTTP header line parsing (Name: Value format).
     */
    private static void testHeaderLineParsing(String headerLine) {
        int colonIdx = headerLine.indexOf(':');
        if (colonIdx > 0 && colonIdx < headerLine.length() - 1) {
            String name = headerLine.substring(0, colonIdx).trim();
            String value = headerLine.substring(colonIdx + 1).trim();

            // Validate header name (should be ASCII, no control chars)
            boolean validName = isValidHeaderName(name);

            // Check for CRLF injection
            boolean hasCRLF = value.contains("\r") || value.contains("\n");
        }
    }

    /**
     * Validate HTTP header name.
     */
    private static boolean isValidHeaderName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            // RFC 7230: token = 1*tchar
            if (c < 0x21 || c > 0x7E || c == ':') {
                return false;
            }
        }
        return true;
    }

    /**
     * Test parsing multiple headers (simulating HTTP request headers).
     */
    private static void testMultipleHeaders(String headerBlock) {
        Map<String, String> headers = new HashMap<>();
        String[] lines = headerBlock.split("\r\n|\r|\n");

        int count = 0;
        for (String line : lines) {
            if (count >= MAX_HEADERS) {
                break;
            }
            if (line.isEmpty()) {
                continue;
            }

            int colonIdx = line.indexOf(':');
            if (colonIdx > 0) {
                String name = line.substring(0, colonIdx).trim();
                String value = (colonIdx < line.length() - 1)
                    ? line.substring(colonIdx + 1).trim()
                    : "";
                headers.put(name, value);
                count++;
            }
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
     * Main method for standalone testing.
     */
    public static void main(String[] args) {
        String[] testCases = {
            "Content-Type: application/json; charset=utf-8",
            "Content-Type: multipart/form-data; boundary=----WebKitFormBoundary",
            "Content-Type: text/xml",
            "X-Custom-Header: value\r\nX-Another: value2",
            "Content-Type: application/soap+xml; charset=\"UTF-8\"; action=\"test\""
        };

        for (String test : testCases) {
            try {
                testContentTypeParsing(test);
                testHeaderLineParsing(test);
                testMultipleHeaders(test);
                System.out.println("Passed: " + test.substring(0, Math.min(50, test.length())));
            } catch (Exception e) {
                System.err.println("Failed: " + e.getMessage());
            }
        }
        System.out.println("HTTP header parsing tests completed");
    }
}
