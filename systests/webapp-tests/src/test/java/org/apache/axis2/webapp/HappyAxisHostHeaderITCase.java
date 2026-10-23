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
package org.apache.axis2.webapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

/**
 * HappyAxis.jsp probes the Version service with a request the server sends to itself.
 * The address of that request used to come from the client's Host header, so an
 * anonymous caller could make the server connect to a host and port of their choosing.
 */
public class HappyAxisHostHeaderITCase {
    private final List<String> received = new CopyOnWriteArrayList<>();
    private HttpServer listener;

    @Before
    public void startListener() throws Exception {
        listener = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        listener.createContext("/", exchange -> {
            received.add(exchange.getRequestMethod() + " " + exchange.getRequestURI());
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        listener.start();
    }

    @After
    public void stopListener() {
        listener.stop(0);
    }

    @Test
    public void testHostHeaderDoesNotChooseProbeTarget() throws Exception {
        int port = Integer.getInteger("jetty.httpPort", 8080);
        String hostileHost = "127.0.0.1:" + listener.getAddress().getPort();
        String response;
        try (Socket socket = new Socket("localhost", port)) {
            socket.setSoTimeout(120000);
            OutputStream out = socket.getOutputStream();
            out.write(("GET /axis2/axis2-web/HappyAxis.jsp HTTP/1.0\r\n"
                    + "Host: " + hostileHost + "\r\n\r\n").getBytes(StandardCharsets.ISO_8859_1));
            out.flush();
            InputStream in = socket.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int n;
            while ((n = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, n);
            }
            response = buffer.toString("UTF-8");
        }
        String statusLine = response.substring(0, Math.max(response.indexOf("\r\n"), 0));
        assertThat(statusLine).contains(" 200 ");
        // The probe must never go to the host the request named...
        assertThat(received).isEmpty();
        // ...and must still reach the Version service on this server.
        assertThat(response).contains("Found Axis2 default Version service");
    }
}
