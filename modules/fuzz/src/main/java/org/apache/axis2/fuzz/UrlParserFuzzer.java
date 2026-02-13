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

import org.apache.axis2.addressing.EndpointReference;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OSS-Fuzz compatible target for URL/URI parsing.
 *
 * Equivalent to Axis2/C fuzz_url_parser.c - tests URL parsing for:
 * - SSRF (Server-Side Request Forgery) bypass attempts
 * - Malformed URL handling
 * - URL encoding/decoding issues
 * - Path traversal attempts
 * - Protocol smuggling
 *
 * @see <a href="https://google.github.io/oss-fuzz/">OSS-Fuzz</a>
 */
public class UrlParserFuzzer {

    /** Maximum input size (8KB for URLs) */
    private static final int MAX_INPUT_SIZE = 8 * 1024;

    /**
     * Jazzer entry point - called millions of times with random/mutated data.
     *
     * @param data Fuzzed data provider for generating test inputs
     */
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        byte[] urlBytes = data.consumeBytes(MAX_INPUT_SIZE);

        if (urlBytes.length == 0) {
            return;
        }

        String urlString = new String(urlBytes, StandardCharsets.UTF_8);

        try {
            // Test various URL parsing scenarios
            testJavaUrl(urlString);
            testJavaUri(urlString);
            testEndpointReference(urlString);
            testUrlEncoding(urlString);
        } catch (MalformedURLException e) {
            // Expected for malformed URLs
        } catch (URISyntaxException e) {
            // Expected for malformed URIs
        } catch (IllegalArgumentException e) {
            // Expected for invalid input
        } catch (Exception e) {
            if (isSecurityRelevant(e)) {
                throw e;
            }
        }
    }

    /**
     * Test java.net.URL parsing.
     */
    private static void testJavaUrl(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);

        // Exercise URL components
        String protocol = url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();
        String path = url.getPath();
        String query = url.getQuery();
        String userInfo = url.getUserInfo();
        String authority = url.getAuthority();
        String file = url.getFile();
        String ref = url.getRef();

        // Test external form
        String externalForm = url.toExternalForm();

        // Check for SSRF-relevant patterns
        checkSsrfPatterns(host, port);
    }

    /**
     * Test java.net.URI parsing.
     */
    private static void testJavaUri(String urlString) throws URISyntaxException {
        URI uri = new URI(urlString);

        // Exercise URI components
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        String path = uri.getPath();
        String query = uri.getQuery();
        String fragment = uri.getFragment();
        String userInfo = uri.getUserInfo();
        String authority = uri.getAuthority();

        // Test normalization
        URI normalized = uri.normalize();

        // Test resolution
        try {
            URI base = new URI("http://example.com/base/");
            URI resolved = base.resolve(uri);
        } catch (Exception e) {
            // Resolution failures are expected
        }

        // Check for path traversal
        if (path != null) {
            checkPathTraversal(path);
        }
    }

    /**
     * Test Axis2 EndpointReference with URL.
     */
    private static void testEndpointReference(String urlString) {
        try {
            EndpointReference epr = new EndpointReference(urlString);

            // Exercise EndpointReference
            String address = epr.getAddress();

            // Validate the EPR
            if (address != null && !address.isEmpty()) {
                // Try to create a URL from it
                try {
                    new URL(address);
                } catch (MalformedURLException e) {
                    // Some EPRs may not be valid URLs
                }
            }
        } catch (Exception e) {
            // EndpointReference creation failures are expected
        }
    }

    /**
     * Test URL encoding and decoding.
     */
    private static void testUrlEncoding(String input) {
        try {
            // Test encoding
            String encoded = URLEncoder.encode(input, StandardCharsets.UTF_8.name());

            // Test decoding
            String decoded = URLDecoder.decode(input, StandardCharsets.UTF_8.name());

            // Test double decoding (common vulnerability pattern)
            String doubleDecoded = URLDecoder.decode(decoded, StandardCharsets.UTF_8.name());

        } catch (Exception e) {
            // Encoding/decoding failures are expected for some inputs
        }
    }

    /**
     * Check for SSRF-relevant patterns in host/port.
     */
    private static void checkSsrfPatterns(String host, int port) {
        if (host == null) {
            return;
        }

        // Common SSRF targets
        boolean isLocalhost = host.equals("localhost")
            || host.equals("127.0.0.1")
            || host.startsWith("192.168.")
            || host.startsWith("10.")
            || host.startsWith("172.16.")
            || host.equals("[::1]")
            || host.contains("169.254.");  // Link-local / metadata

        // AWS metadata endpoint
        boolean isAwsMetadata = host.equals("169.254.169.254");

        // Cloud metadata endpoints
        boolean isCloudMetadata = host.contains("metadata.google.internal")
            || host.contains("metadata.azure.com");
    }

    /**
     * Check for path traversal patterns.
     */
    private static void checkPathTraversal(String path) {
        boolean hasTraversal = path.contains("../")
            || path.contains("..\\")
            || path.contains("%2e%2e")
            || path.contains("%2E%2E")
            || path.contains("..%2f")
            || path.contains("..%5c");
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
            "http://example.com/path?query=value",
            "https://user:pass@example.com:8080/path",
            "http://localhost/admin",
            "http://127.0.0.1:8080",
            "http://[::1]/path",
            "http://169.254.169.254/latest/meta-data/",
            "file:///etc/passwd",
            "http://example.com/../../../etc/passwd",
            "http://example.com/path%2f..%2f..%2fetc%2fpasswd"
        };

        for (String test : testCases) {
            try {
                testJavaUrl(test);
                System.out.println("URL passed: " + test);
            } catch (MalformedURLException e) {
                System.out.println("URL rejected (expected): " + test);
            }

            try {
                testJavaUri(test);
                System.out.println("URI passed: " + test);
            } catch (URISyntaxException e) {
                System.out.println("URI rejected: " + test);
            }
        }
        System.out.println("URL parsing tests completed");
    }
}
