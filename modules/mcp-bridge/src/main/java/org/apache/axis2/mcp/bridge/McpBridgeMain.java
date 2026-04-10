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

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * Entry point for the Axis2 MCP bridge.
 *
 * <p>Usage (mTLS — required):
 * <pre>
 *   java -jar axis2-mcp-bridge-exe.jar \
 *     --base-url https://localhost:8443/axis2-json-api \
 *     --keystore  /path/to/client-keystore.p12 \
 *     --truststore /path/to/ca-truststore.p12
 * </pre>
 *
 * <p>The keystore holds the client certificate and private key (PKCS12).
 * The truststore holds the CA certificate used to verify the server.
 * Both use the password {@code changeit} by default; override with
 * {@code --keystore-password} and {@code --truststore-password}.
 *
 * <p>Claude Desktop configuration ({@code ~/.config/claude/claude_desktop_config.json}):
 * <pre>
 * {
 *   "mcpServers": {
 *     "axis2-demo": {
 *       "command": "java",
 *       "args": ["-jar", "/path/to/axis2-mcp-bridge-exe.jar",
 *                "--base-url",    "https://localhost:8443/axis2-json-api",
 *                "--keystore",    "/path/to/client-keystore.p12",
 *                "--truststore",  "/path/to/ca-truststore.p12"]
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>All diagnostic output is written to stderr so it does not corrupt the
 * MCP protocol stream on stdout.
 */
public class McpBridgeMain {

    public static void main(String[] args) {
        String baseUrl       = parseArg(args, "--base-url");
        String keystorePath  = parseArg(args, "--keystore");
        String truststorePath = parseArg(args, "--truststore");
        String keystorePass  = parseArgOrDefault(args, "--keystore-password", "changeit");
        String truststorePass = parseArgOrDefault(args, "--truststore-password", "changeit");

        if (baseUrl == null || keystorePath == null || truststorePath == null) {
            System.err.println("Usage: axis2-mcp-bridge");
            System.err.println("  --base-url    https://localhost:8443/axis2-json-api");
            System.err.println("  --keystore    /path/to/client-keystore.p12");
            System.err.println("  --truststore  /path/to/ca-truststore.p12");
            System.err.println("  [--keystore-password  changeit]");
            System.err.println("  [--truststore-password changeit]");
            System.exit(1);
        }

        System.err.println("[axis2-mcp-bridge] Starting — base-url: " + baseUrl);

        ObjectMapper mapper = new ObjectMapper();

        try {
            SSLContext sslContext = buildSslContext(
                    keystorePath, keystorePass.toCharArray(),
                    truststorePath, truststorePass.toCharArray());

            try (ToolRegistry registry = new ToolRegistry(baseUrl, mapper, sslContext)) {
                registry.load();

                try (McpStdioServer server = new McpStdioServer(baseUrl, registry, mapper, sslContext)) {
                    server.run();
                }
            }

        } catch (Exception e) {
            System.err.println("[axis2-mcp-bridge] Fatal: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    /**
     * Builds an {@link SSLContext} for mTLS from PKCS12 keystore and truststore files.
     */
    static SSLContext buildSslContext(String keystorePath, char[] keystorePass,
                                      String truststorePath, char[] truststorePass)
            throws Exception {
        // Client certificate + private key
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePass);
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keystorePass);

        // CA certificate for server verification
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(truststorePath)) {
            trustStore.load(fis, truststorePass);
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        System.err.println("[axis2-mcp-bridge] mTLS SSLContext initialized");
        return sslContext;
    }

    private static String parseArg(String[] args, String key) {
        for (int i = 0; i < args.length - 1; i++) {
            if (key.equals(args[i])) return args[i + 1];
        }
        return null;
    }

    private static String parseArgOrDefault(String[] args, String key, String defaultValue) {
        String v = parseArg(args, key);
        return v != null ? v : defaultValue;
    }
}
