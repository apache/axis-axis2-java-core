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

package org.apache.axis2.util;

import javax.wsdl.xml.WSDLLocator;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * A {@link WSDLLocator} that fetches imported WSDL/XSD documents and
 * validates them with a hardened XML parser before returning them to
 * wsdl4j. This prevents XXE (XML External Entity) attacks via
 * {@code <!DOCTYPE>} declarations in imported documents.
 *
 * <p>wsdl4j's internal {@code WSDLReaderImpl.getDocument()} creates its
 * own {@code DocumentBuilderFactory} without XXE hardening. By supplying
 * this locator, Axis2 intercepts import resolution and ensures that
 * every imported document is parsed by a secure parser first. If the
 * document contains a DOCTYPE declaration, parsing fails and the
 * import is rejected before wsdl4j's vulnerable parser sees it.
 *
 * @see org.apache.axis2.description.AxisService#createClientSideAxisService
 * @see org.apache.axis2.description.WSDL11ToAxisServiceBuilder
 */
public class SecureWSDLLocator implements WSDLLocator {

    private final String baseURI;
    private String latestImportURI;

    public SecureWSDLLocator(String baseURI) {
        this.baseURI = baseURI;
    }

    @Override
    public InputSource getBaseInputSource() {
        try {
            return createSecureInputSource(baseURI);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load base WSDL: " + baseURI, e);
        }
    }

    @Override
    public String getBaseURI() {
        return baseURI;
    }

    @Override
    public InputSource getImportInputSource(String parentLocation, String importLocation) {
        try {
            String resolved = resolveURI(parentLocation, importLocation);
            latestImportURI = resolved;
            return createSecureInputSource(resolved);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to securely load imported document: " + importLocation
                    + " (resolved from " + parentLocation + ")", e);
        }
    }

    @Override
    public String getLatestImportURI() {
        return latestImportURI;
    }

    @Override
    public void close() {
        // nothing to close
    }

    /**
     * Fetches the document at the given URI, validates it with a hardened
     * SAX parser to verify it contains no DOCTYPE declaration, then
     * returns a new InputSource over the validated bytes.
     */
    private InputSource createSecureInputSource(String uri) throws Exception {
        byte[] content = fetchBytes(uri);

        // Validate with hardened SAX parser — throws SAXParseException if
        // DOCTYPE is present. SAX avoids building a DOM tree in memory.
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        spf.setXIncludeAware(false);

        org.xml.sax.XMLReader xmlReader = spf.newSAXParser().getXMLReader();
        xmlReader.setEntityResolver(new DefaultEntityResolver());
        xmlReader.parse(new InputSource(new ByteArrayInputStream(content)));

        // Validated — return a fresh InputSource for wsdl4j
        InputSource is = new InputSource(new ByteArrayInputStream(content));
        is.setSystemId(uri);
        return is;
    }

    private static final int CONNECT_TIMEOUT =
            Integer.getInteger("axis2.wsdl.import.connect.timeout", 5000);
    private static final int READ_TIMEOUT =
            Integer.getInteger("axis2.wsdl.import.read.timeout", 15000);
    private static final long MAX_IMPORT_SIZE =
            Long.getLong("axis2.wsdl.import.maxsize", 10 * 1024 * 1024);

    private static byte[] fetchBytes(String uri) throws IOException {
        URL url = new URL(uri);
        String protocol = url.getProtocol();
        if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
            throw new IOException("WSDL import rejected: untrusted protocol '"
                    + protocol + "' in URI: " + uri);
        }
        java.net.URLConnection conn = url.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        try (InputStream in = conn.getInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            long total = 0;
            while ((n = in.read(buf)) != -1) {
                total += n;
                if (total > MAX_IMPORT_SIZE) {
                    throw new IOException("WSDL import exceeds size limit of "
                            + MAX_IMPORT_SIZE + " bytes for URI: " + uri);
                }
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }

    private static String resolveURI(String parent, String relative) {
        try {
            URI parentURI = new URI(parent);
            return parentURI.resolve(relative).toString();
        } catch (Exception e) {
            // Fallback: treat as absolute
            return relative;
        }
    }
}
