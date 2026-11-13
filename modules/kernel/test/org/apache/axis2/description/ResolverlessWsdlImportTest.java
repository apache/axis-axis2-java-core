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
package org.apache.axis2.description;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpServer;

import junit.framework.TestCase;

/**
 * A WSDL handed to the builder as a stream, with no resolver, is a document whose
 * origin nothing can check. wsdl4j fetches wsdl:import locations and xsd:import
 * schemaLocations itself when it is given a parsed Document, so until 2.0.3 such a
 * document could name any host and the server would send it a request. The
 * resolver-less read now goes through a locator that refuses an absolute location
 * before anything is opened; a relative one still resolves, since that is how
 * co-packaged documents refer to each other.
 */
public class ResolverlessWsdlImportTest extends TestCase {

    private static final String HEAD =
            "<?xml version='1.0'?>"
            + "<wsdl:definitions xmlns:wsdl='http://schemas.xmlsoap.org/wsdl/'"
            + " xmlns:xs='http://www.w3.org/2001/XMLSchema'"
            + " targetNamespace='urn:test'>";

    private HttpServer server;
    private final AtomicInteger requests = new AtomicInteger();

    @Override
    protected void setUp() throws Exception {
        // Stands in for an internal host. It answers, so a request that does get
        // through fails the count assertion instead of hanging the test.
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            byte[] body = (HEAD + "</wsdl:definitions>").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            OutputStream out = exchange.getResponseBody();
            out.write(body);
            out.close();
        });
        server.start();
    }

    @Override
    protected void tearDown() throws Exception {
        server.stop(0);
    }

    private String origin() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private static String wsdlImporting(String location) {
        return HEAD + "<wsdl:import namespace='urn:imported' location='" + location + "'/>"
                + "</wsdl:definitions>";
    }

    private static String wsdlWithSchemaImport(String location) {
        return HEAD + "<wsdl:types><xs:schema targetNamespace='urn:test'>"
                + "<xs:import namespace='urn:types' schemaLocation='" + location + "'/>"
                + "</xs:schema></wsdl:types></wsdl:definitions>";
    }

    private static WSDL11ToAxisServiceBuilder builderFor(String wsdl) {
        return new WSDL11ToAxisServiceBuilder(
                new ByteArrayInputStream(wsdl.getBytes(StandardCharsets.UTF_8)), null, null);
    }

    private static boolean causeChainMentions(Throwable t, String text) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c.getMessage() != null && c.getMessage().contains(text)) {
                return true;
            }
        }
        return false;
    }

    private void assertRefusedWithoutFetching(WSDL11ToAxisServiceBuilder builder,
                                              String expectedInMessage) {
        try {
            builder.populateService();
            fail("the import must be refused");
        } catch (Exception expected) {
            assertTrue("should say why it was refused, was: " + expected,
                    causeChainMentions(expected, expectedInMessage));
        }
        assertEquals("no request may reach the host the document named",
                0, requests.get());
    }

    public void testAnAbsoluteWsdlImportIsRefusedBeforeItIsFetched() {
        assertRefusedWithoutFetching(
                builderFor(wsdlImporting(origin() + "/imported.wsdl")),
                "Absolute import location blocked");
    }

    public void testAnAbsoluteSchemaImportIsRefusedBeforeItIsFetched() {
        assertRefusedWithoutFetching(
                builderFor(wsdlWithSchemaImport(origin() + "/types.xsd")),
                "Absolute import location blocked");
    }

    /** A network-path reference would take the base document's scheme. */
    public void testANetworkPathImportIsRefused() {
        assertRefusedWithoutFetching(
                builderFor(wsdlImporting("//127.0.0.1:" + server.getAddress().getPort()
                        + "/imported.wsdl")),
                "Absolute import location blocked");
    }

    /** Refusing absolute locations must not stop a relative one resolving. */
    public void testARelativeImportStillResolvesAgainstTheBaseUri() throws Exception {
        File wsdl = new File("test-resources/wsdl/imports/binding/EchoService.wsdl");
        InputStream in = new FileInputStream(wsdl);
        try {
            WSDL11ToAxisServiceBuilder builder = new WSDL11ToAxisServiceBuilder(in);
            builder.setBaseUri(wsdl.getParentFile().toURI().toString());
            builder.setDocumentBaseUri(wsdl.toURI().toString());
            AxisService service = builder.populateService();
            assertNotNull(service.getEndpoint("EchoServiceHttpSoap12Endpoint"));
        } finally {
            in.close();
        }
        assertEquals(0, requests.get());
    }

    /** With no base URI there is nothing to resolve a relative location against. */
    public void testARelativeImportWithNoBaseUriFailsClosed() {
        assertRefusedWithoutFetching(
                builderFor(wsdlImporting("imported.wsdl")), "no base URI");
    }
}
