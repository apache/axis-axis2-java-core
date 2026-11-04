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

package org.apache.axis2.transport.http.impl.httpclient5;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Proxy credentials must answer the proxy's challenge only.
 *
 * <p>The client sends the same credentials provider to the proxy and to the
 * origin server. Credentials registered for any host would therefore also be
 * sent in reply to a 401 from the origin, which hands the proxy password to
 * whatever server the request happens to reach.
 */
public class ProxyCredentialScopeTest extends TestCase {

    private static final String PROXY_USER = "proxyuser";
    private static final String PROXY_PASSWORD = "proxypass";
    private static final String ORIGIN_USER = "originuser";
    private static final String ORIGIN_PASSWORD = "originpass";

    private HttpServer proxy;
    private int proxyPort;
    private final AtomicReference<String> originAuthorization = new AtomicReference<String>();
    private final AtomicReference<String> proxyAuthorization = new AtomicReference<String>();

    protected void setUp() throws Exception {
        super.setUp();
        originAuthorization.set(null);
        proxyAuthorization.set(null);
        proxy = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        proxyPort = proxy.getAddress().getPort();

        // Stands in for the proxy and, behind it, the origin server. Requests
        // arrive in absolute form; the path says which of the two challenges.
        proxy.createContext("/", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                exchange.getRequestBody().readAllBytes();
                String path = exchange.getRequestURI().getPath();
                if ("/origin-challenge".equals(path)) {
                    String auth = exchange.getRequestHeaders().getFirst("Authorization");
                    if (auth == null) {
                        exchange.getResponseHeaders().add("WWW-Authenticate",
                                "Basic realm=\"origin\"");
                        exchange.sendResponseHeaders(401, -1);
                    } else {
                        originAuthorization.set(auth);
                        exchange.sendResponseHeaders(200, -1);
                    }
                } else if ("/proxy-challenge".equals(path)) {
                    String auth = exchange.getRequestHeaders().getFirst("Proxy-Authorization");
                    if (auth == null) {
                        exchange.getResponseHeaders().add("Proxy-Authenticate",
                                "Basic realm=\"proxy\"");
                        exchange.sendResponseHeaders(407, -1);
                    } else {
                        proxyAuthorization.set(auth);
                        exchange.sendResponseHeaders(200, -1);
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
                exchange.close();
            }
        });
        proxy.start();
    }

    protected void tearDown() throws Exception {
        proxy.stop(0);
        super.tearDown();
    }

    /** The case that matters: a 401 from the origin must not be answered with the proxy password. */
    public void testOriginChallengeDoesNotReceiveProxyCredentials() throws Exception {
        RequestImpl request = execute(newContext(), null, "/origin-challenge");
        assertEquals(401, request.getStatusCode());
        assertNull("The proxy credentials were sent to the origin server",
                originAuthorization.get());
    }

    /** The control: the proxy's own challenge is still answered. */
    public void testProxyChallengeReceivesProxyCredentials() throws Exception {
        RequestImpl request = execute(newContext(), null, "/proxy-challenge");
        assertEquals(200, request.getStatusCode());
        assertEquals(basic(PROXY_USER, PROXY_PASSWORD), proxyAuthorization.get());
    }

    /** Credentials configured for the origin are kept when the proxy is configured on top. */
    public void testOriginCredentialsSurviveProxyConfiguration() throws Exception {
        HttpTransportPropertiesImpl.Authenticator authenticator =
                new HttpTransportPropertiesImpl.Authenticator();
        authenticator.setUsername(ORIGIN_USER);
        authenticator.setPassword(ORIGIN_PASSWORD);
        authenticator.setHost("origin.example");
        RequestImpl request = execute(newContext(), authenticator, "/origin-challenge");
        assertEquals(200, request.getStatusCode());
        assertEquals(basic(ORIGIN_USER, ORIGIN_PASSWORD), originAuthorization.get());
    }

    private MessageContext newContext() throws Exception {
        ConfigurationContext cc = new ConfigurationContext(new AxisConfiguration());
        MessageContext mc = cc.createMessageContext();
        HttpTransportProperties.ProxyProperties proxyProperties =
                new HttpTransportProperties.ProxyProperties();
        proxyProperties.setProxyName("127.0.0.1");
        proxyProperties.setProxyPort(proxyPort);
        proxyProperties.setUserName(PROXY_USER);
        proxyProperties.setPassWord(PROXY_PASSWORD);
        mc.setProperty(HTTPConstants.PROXY, proxyProperties);
        return mc;
    }

    private RequestImpl execute(MessageContext mc, HttpTransportPropertiesImpl.Authenticator authenticator,
            String path) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        RequestImpl request = new RequestImpl(client, mc, "GET",
                URI.create("http://origin.example" + path), null);
        if (authenticator != null) {
            request.enableAuthentication(authenticator);
        }
        request.execute();
        request.releaseConnection();
        return request;
    }

    private static String basic(String user, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(
                (user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
