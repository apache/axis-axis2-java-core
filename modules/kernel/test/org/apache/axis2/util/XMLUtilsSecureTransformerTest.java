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
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

/**
 * An identity transform of a StreamSource parses it with the transformer's own
 * parser, so the factory is the only place an external entity can be refused.
 */
public class XMLUtilsSecureTransformerTest extends TestCase {

    private static final String MARKER = "axis2-secret-marker";

    private File secret;

    @Override
    protected void setUp() throws Exception {
        secret = File.createTempFile("axis2-secure-transformer", ".txt");
        FileOutputStream out = new FileOutputStream(secret);
        try {
            out.write(MARKER.getBytes(StandardCharsets.UTF_8));
        } finally {
            out.close();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        secret.delete();
    }

    private static StreamSource source(String xml) {
        return new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private static String transform(StreamSource source) throws TransformerException {
        javax.xml.transform.Transformer transformer =
                XMLUtils.newSecureTransformerFactory().newTransformer();
        // Keep the expected refusal out of the build log.
        transformer.setErrorListener(new ErrorListener() {
            public void warning(TransformerException e) {
            }

            public void error(TransformerException e) throws TransformerException {
                throw e;
            }

            public void fatalError(TransformerException e) throws TransformerException {
                throw e;
            }
        });
        StringWriter out = new StringWriter();
        transformer.transform(source, new StreamResult(out));
        return out.toString();
    }

    public void testPlainDocumentIsCopied() throws Exception {
        String out = transform(source("<a xmlns='urn:test'><b>text</b></a>"));
        assertTrue(out, out.contains("<b>text</b>"));
    }

    public void testExternalEntityIsNotResolved() throws Exception {
        String xml = "<?xml version='1.0'?>"
                + "<!DOCTYPE a [<!ENTITY xxe SYSTEM '" + secret.toURI() + "'>]>"
                + "<a>&xxe;</a>";
        try {
            String out = transform(source(xml));
            assertFalse("external entity was resolved: " + out, out.contains(MARKER));
        } catch (TransformerException expected) {
            // Refusing the document outright is equally acceptable.
        }
    }
}
