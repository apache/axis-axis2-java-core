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
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A decoupled WS-Addressing response must not follow redirects.
 *
 * <p>The destination of such a response is chosen by the caller, and
 * {@code AddressingResponseEndpointPolicy} screens that destination — its
 * scheme and the address it resolves to — before the send. A redirect is
 * evaluated by no one: the caller's own endpoint can answer 307 and name
 * somewhere the policy would have refused, such as the HTTP-only
 * instance-metadata address, and the client would honour it. That is the
 * protocol-downgrade route around the scheme allow-list, so redirects are
 * disabled for this kind of send.
 */
public class DecoupledResponseRedirectTest extends TestCase {

    private HttpServer server;
    private final AtomicBoolean redirectTargetHit = new AtomicBoolean();
    private int port;

    protected void setUp() throws Exception {
        super.setUp();
        redirectTargetHit.set(false);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();

        // Stands in for the caller's own reply endpoint, which answers with a
        // redirect rather than accepting the response.
        server.createContext("/cb", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                drain(exchange);
                exchange.getResponseHeaders().add("Location",
                        "http://127.0.0.1:" + port + "/rebound");
                exchange.sendResponseHeaders(307, -1);
                exchange.close();
            }
        });

        // Stands in for the address the policy would never have permitted.
        server.createContext("/rebound", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                redirectTargetHit.set(true);
                drain(exchange);
                exchange.sendResponseHeaders(200, -1);
                exchange.close();
            }
        });
        server.start();
    }

    protected void tearDown() throws Exception {
        server.stop(0);
        super.tearDown();
    }

    private static void drain(HttpExchange exchange) throws IOException {
        exchange.getRequestBody().readAllBytes();
    }

    /** The case that matters: a server-side reply to a caller-named endpoint. */
    public void testDecoupledResponseDoesNotFollowRedirect() throws Exception {
        send(true, new EndpointReference("http://127.0.0.1:" + port + "/cb"));
        assertFalse("A decoupled response must not follow a redirect off the "
                + "endpoint the policy screened", redirectTargetHit.get());
    }

    /**
     * The control. Without it the test above proves nothing — it would pass just
     * as well if the request never left the building.
     */
    public void testClientRequestStillFollowsRedirect() throws Exception {
        send(false, new EndpointReference("http://127.0.0.1:" + port + "/cb"));
        assertTrue("An ordinary client-side request should still follow redirects",
                redirectTargetHit.get());
    }

    /**
     * An anonymous reply travels back down the inbound connection and is not a
     * caller-named destination, so it keeps the default behaviour.
     */
    public void testAnonymousServerResponseIsUnaffected() throws Exception {
        MessageContext mc = newContext(true,
                new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL));
        // The request URI is what is actually dialled; the anonymous To is what
        // decides whether the restriction applies.
        execute(mc, "http://127.0.0.1:" + port + "/cb");
        assertTrue("An anonymous server-side reply should keep default redirect "
                + "handling", redirectTargetHit.get());
    }

    private void send(boolean serverSide, EndpointReference to) throws Exception {
        execute(newContext(serverSide, to), to.getAddress());
    }

    private MessageContext newContext(boolean serverSide, EndpointReference to)
            throws Exception {
        ConfigurationContext cc = new ConfigurationContext(new AxisConfiguration());
        MessageContext mc = cc.createMessageContext();
        mc.setServerSide(serverSide);
        mc.setTo(to);
        return mc;
    }

    private void execute(MessageContext mc, String uri) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        RequestImpl request =
                new RequestImpl(client, mc, "POST", URI.create(uri), null);
        request.execute();
        request.releaseConnection();
    }
}
