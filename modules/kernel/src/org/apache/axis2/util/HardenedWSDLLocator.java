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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import javax.wsdl.xml.WSDLLocator;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * A {@link WSDLLocator} that refuses a DOCTYPE declaration in the WSDL, or in
 * anything it imports, before wsdl4j sees it.
 * <p>
 * wsdl4j parses with its own unhardened {@code DocumentBuilderFactory}, and it
 * fetches the whole import chain itself, so a document reaching it unscreened is an
 * XXE in the server JVM. {@link SecureWSDLLocator} screens the client-side path, but
 * it fetches over http/https only -- deliberately, since that is all that path needs
 * -- so it cannot be used where a WSDL is loaded from a file, an archive, a
 * classpath resource or a catalog, which is what the deployment and runtime paths do.
 * <p>
 * This one separates the two concerns. It never decides <em>where</em> a document
 * comes from: either a delegate locator resolves it, keeping whatever catalog or
 * archive behaviour that locator implements, or it is read from its URI with any
 * scheme the JVM supports. What it does in both cases is validate the bytes with a
 * hardened SAX parse first, and hand wsdl4j a stream over the validated bytes. So
 * hardening does not narrow what can be loaded.
 */
public class HardenedWSDLLocator implements WSDLLocator {

    private static final Log log = LogFactory.getLog(HardenedWSDLLocator.class);

    private static final int CONNECT_TIMEOUT =
            Integer.getInteger("axis2.wsdl.import.connect.timeout", 5000);
    private static final int READ_TIMEOUT =
            Integer.getInteger("axis2.wsdl.import.read.timeout", 15000);
    private static final long MAX_SIZE =
            Long.getLong("axis2.wsdl.import.maxsize", 10 * 1024 * 1024);

    private final WSDLLocator delegate;
    private final String baseURI;
    private String latestImportURI;

    /**
     * Validates whatever a locator already in use resolves, leaving its resolution
     * behaviour -- catalogs, archives, the classpath -- exactly as it was.
     *
     * @param delegate the locator to wrap
     */
    public HardenedWSDLLocator(WSDLLocator delegate) {
        this.delegate = delegate;
        this.baseURI = delegate.getBaseURI();
    }

    /**
     * Reads from a URI with any scheme the JVM supports, which is what the call
     * sites replaced by this used to let wsdl4j do for itself.
     *
     * @param baseURI the WSDL location
     */
    public HardenedWSDLLocator(String baseURI) {
        this.delegate = null;
        this.baseURI = baseURI;
    }

    public InputSource getBaseInputSource() {
        if (delegate != null) {
            return validated(delegate.getBaseInputSource(), baseURI);
        }
        return validated(read(baseURI), baseURI);
    }

    public String getBaseURI() {
        return baseURI;
    }

    public InputSource getImportInputSource(String parentLocation, String importLocation) {
        if (delegate != null) {
            InputSource source = delegate.getImportInputSource(parentLocation, importLocation);
            latestImportURI = delegate.getLatestImportURI();
            return validated(source, latestImportURI);
        }
        String resolved = resolve(parentLocation, importLocation);
        latestImportURI = resolved;
        return validated(read(resolved), resolved);
    }

    public String getLatestImportURI() {
        return latestImportURI;
    }

    public void close() {
        if (delegate != null) {
            delegate.close();
        }
    }

    /**
     * Reads the source fully, rejects a DOCTYPE, and returns a fresh stream over the
     * validated bytes. The source has to be consumed to be checked, which is why the
     * bytes are buffered and replayed rather than handed on directly.
     */
    private InputSource validated(InputSource source, String systemId) {
        if (source == null) {
            return null;
        }
        try {
            byte[] content = drain(source);

            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setXIncludeAware(false);
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();
            xmlReader.setEntityResolver(new DefaultEntityResolver());
            xmlReader.parse(new InputSource(new ByteArrayInputStream(content)));

            InputSource validatedSource = new InputSource(new ByteArrayInputStream(content));
            validatedSource.setSystemId(systemId != null ? systemId : source.getSystemId());
            return validatedSource;
        } catch (Exception e) {
            // Fail closed. A WSDL that will not survive a hardened parse is not one
            // to hand to a parser that resolves entities.
            throw new RuntimeException("Refusing WSDL document " + systemId
                    + ": it did not pass a hardened parse", e);
        }
    }

    private byte[] drain(InputSource source) throws IOException {
        InputStream in = source.getByteStream();
        if (in == null) {
            if (source.getSystemId() == null) {
                throw new IOException("The locator returned a source with no stream"
                        + " and no system id");
            }
            in = openStream(source.getSystemId());
        }
        try {
            return readBounded(in, source.getSystemId());
        } finally {
            in.close();
        }
    }

    private InputSource read(String uri) {
        try {
            InputStream in = openStream(uri);
            try {
                InputSource source = new InputSource(
                        new ByteArrayInputStream(readBounded(in, uri)));
                source.setSystemId(uri);
                return source;
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load WSDL document " + uri, e);
        }
    }

    /**
     * Opens a location the way wsdl4j's own {@code readWSDL(String)} would: as a URL
     * if it is one, otherwise as a file path. Codegen and the deployment paths hand
     * over bare relative paths, so accepting only URLs here would refuse the ordinary
     * case rather than the dangerous one.
     */
    private InputStream openStream(String uri) throws IOException {
        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException notAUrl) {
            File file = new File(uri);
            if (!file.isFile()) {
                throw new IOException("WSDL location is neither a URL nor a"
                        + " readable file: " + uri, notAUrl);
            }
            url = file.toURI().toURL();
        }
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        return connection.getInputStream();
    }

    private byte[] readBounded(InputStream in, String uri) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;
        while ((read = in.read(buffer)) != -1) {
            total += read;
            if (MAX_SIZE >= 0 && total > MAX_SIZE) {
                throw new IOException("WSDL document " + uri + " exceeds "
                        + MAX_SIZE + " bytes");
            }
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private String resolve(String parentLocation, String importLocation) {
        try {
            if (parentLocation == null) {
                return importLocation;
            }
            return URI.create(parentLocation).resolve(importLocation).toString();
        } catch (IllegalArgumentException e) {
            if (log.isDebugEnabled()) {
                log.debug("Could not resolve " + importLocation + " against "
                        + parentLocation + "; using it as given");
            }
            return importLocation;
        }
    }
}
