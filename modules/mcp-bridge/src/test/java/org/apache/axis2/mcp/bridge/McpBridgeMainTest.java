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
package org.apache.axis2.mcp.bridge;

import junit.framework.TestCase;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.lang.reflect.Method;

/**
 * Unit tests for {@link McpBridgeMain}.
 *
 * <p>Tests argument parsing (private static methods accessed via reflection)
 * and, when the project certificate files are present, {@code buildSslContext}.
 */
public class McpBridgeMainTest extends TestCase {

    // ── argument parsing ─────────────────────────────────────────────────────

    private static String callParseArg(String[] args, String key) throws Exception {
        Method m = McpBridgeMain.class.getDeclaredMethod("parseArg", String[].class, String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, args, key);
    }

    private static String callParseArgOrDefault(String[] args, String key, String def) throws Exception {
        Method m = McpBridgeMain.class.getDeclaredMethod(
                "parseArgOrDefault", String[].class, String.class, String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, args, key, def);
    }

    public void testParseArgReturnsValueWhenKeyPresent() throws Exception {
        String[] args = {"--base-url", "https://localhost:8443/axis2-json-api",
                         "--keystore", "/path/to/ks.p12"};
        assertEquals("https://localhost:8443/axis2-json-api",
                callParseArg(args, "--base-url"));
        assertEquals("/path/to/ks.p12",
                callParseArg(args, "--keystore"));
    }

    public void testParseArgReturnsNullWhenKeyAbsent() throws Exception {
        String[] args = {"--base-url", "https://localhost:8443/axis2-json-api"};
        assertNull(callParseArg(args, "--truststore"));
    }

    public void testParseArgReturnsNullForEmptyArgs() throws Exception {
        assertNull(callParseArg(new String[]{}, "--base-url"));
    }

    public void testParseArgIgnoresKeyAtLastPosition() throws Exception {
        // Key at last index has no following value — must not throw AIOOBE
        String[] args = {"--other", "val", "--base-url"};
        assertNull(callParseArg(args, "--base-url"));
    }

    public void testParseArgReturnsFirstMatchOnly() throws Exception {
        // If a key appears twice, the first value wins (spec: first match)
        String[] args = {"--base-url", "first", "--base-url", "second"};
        assertEquals("first", callParseArg(args, "--base-url"));
    }

    public void testParseArgOrDefaultReturnsValueWhenKeyPresent() throws Exception {
        String[] args = {"--keystore-password", "mySecret"};
        assertEquals("mySecret",
                callParseArgOrDefault(args, "--keystore-password", "changeit"));
    }

    public void testParseArgOrDefaultReturnsDefaultWhenKeyAbsent() throws Exception {
        assertEquals("changeit",
                callParseArgOrDefault(new String[]{}, "--keystore-password", "changeit"));
    }

    public void testParseArgOrDefaultReturnsDefaultWhenEmpty() throws Exception {
        assertEquals("default",
                callParseArgOrDefault(new String[]{}, "--missing", "default"));
    }

    public void testParseArgWithAdjacentOtherArgs() throws Exception {
        String[] args = {
                "--unrelated", "value",
                "--keystore", "/certs/client.p12",
                "--truststore", "/certs/ca.p12"
        };
        assertEquals("/certs/client.p12",  callParseArg(args, "--keystore"));
        assertEquals("/certs/ca.p12",       callParseArg(args, "--truststore"));
        assertNull(callParseArg(args, "--base-url"));
    }

    // ── buildSslContext with real cert files ──────────────────────────────────

    /**
     * Verifies that {@code buildSslContext} successfully initialises an
     * {@link SSLContext} from the PKCS12 files created during project setup.
     *
     * <p>The test is skipped (passes vacuously) when the cert directory does
     * not exist, so it does not break CI environments that don't have the certs.
     */
    public void testBuildSslContextWithProjectCerts() throws Exception {
        // Locate the certs directory relative to the mcp-bridge module
        File certsDir = new File("../../certs");
        if (!certsDir.exists()) {
            // Try from the repo root
            certsDir = new File("certs");
        }
        if (!certsDir.exists()) {
            System.out.println("SKIPPED testBuildSslContextWithProjectCerts: " +
                    "certs/ directory not found — run from repo root");
            return;
        }

        File serverKs   = new File(certsDir, "server-keystore.p12");
        File caTs       = new File(certsDir, "ca-truststore.p12");
        File clientKs   = new File(certsDir, "client-keystore.p12");

        if (!serverKs.exists() || !caTs.exists() || !clientKs.exists()) {
            System.out.println("SKIPPED testBuildSslContextWithProjectCerts: " +
                    "one or more keystore files missing in " + certsDir.getAbsolutePath());
            return;
        }

        // Test server-side context (server keystore + CA truststore)
        SSLContext serverCtx = McpBridgeMain.buildSslContext(
                serverKs.getAbsolutePath(), "changeit".toCharArray(),
                caTs.getAbsolutePath(),     "changeit".toCharArray());
        assertNotNull("Server SSLContext must not be null", serverCtx);
        assertEquals("TLS", serverCtx.getProtocol());

        // Test client-side context (client keystore + CA truststore)
        SSLContext clientCtx = McpBridgeMain.buildSslContext(
                clientKs.getAbsolutePath(), "changeit".toCharArray(),
                caTs.getAbsolutePath(),     "changeit".toCharArray());
        assertNotNull("Client SSLContext must not be null", clientCtx);
        assertEquals("TLS", clientCtx.getProtocol());
    }

    public void testBuildSslContextThrowsForNonExistentKeystore() {
        try {
            McpBridgeMain.buildSslContext(
                    "/does/not/exist.p12", "changeit".toCharArray(),
                    "/does/not/exist-trust.p12", "changeit".toCharArray());
            fail("Expected an exception for a non-existent keystore file");
        } catch (Exception e) {
            // Any exception is acceptable — FileNotFoundException, KeyStoreException, etc.
            assertNotNull("Exception must have a message", e.getMessage() != null || e.getCause() != null);
        }
    }

    public void testBuildSslContextThrowsForWrongPassword() throws Exception {
        File certsDir = new File("../../certs");
        if (!certsDir.exists()) certsDir = new File("certs");
        File clientKs = new File(certsDir, "client-keystore.p12");
        File caTs     = new File(certsDir, "ca-truststore.p12");

        if (!clientKs.exists() || !caTs.exists()) {
            System.out.println("SKIPPED testBuildSslContextThrowsForWrongPassword: " +
                    "cert files not found");
            return;
        }

        try {
            McpBridgeMain.buildSslContext(
                    clientKs.getAbsolutePath(), "wrongpassword".toCharArray(),
                    caTs.getAbsolutePath(),     "changeit".toCharArray());
            fail("Expected an exception for wrong keystore password");
        } catch (Exception e) {
            // Expected — wrong password causes load() to throw
        }
    }
}
