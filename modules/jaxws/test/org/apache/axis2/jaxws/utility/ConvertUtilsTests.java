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

package org.apache.axis2.jaxws.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.stream.StreamSource;

import jakarta.xml.ws.WebServiceException;
import junit.framework.TestCase;

/**
 * A StreamSource backed by a Reader has no InputStream to copy, so convert()
 * copies it through a transformer, which parses it with its own parser.
 */
public class ConvertUtilsTests extends TestCase {

    private static final String MARKER = "JAXWS_XXE_SECRET_MARKER";

    public void testSourceToBytesDoesNotResolveExternalEntity() throws Exception {
        File secret = File.createTempFile("jaxws-xxe", ".txt");
        secret.deleteOnExit();
        try (FileWriter w = new FileWriter(secret)) {
            w.write(MARKER);
        }
        String xml = "<?xml version='1.0'?>"
                + "<!DOCTYPE a [<!ENTITY xxe SYSTEM '" + secret.toURI() + "'>]>"
                + "<a>&xxe;</a>";
        try {
            byte[] bytes = (byte[]) ConvertUtils.convert(
                    new StreamSource(new StringReader(xml)), byte[].class);
            String out = new String(bytes, StandardCharsets.UTF_8);
            assertFalse("external entity was resolved: " + out, out.contains(MARKER));
        } catch (WebServiceException expected) {
            // Refusing the document outright is equally acceptable.
        }
    }

    public void testSourceToBytesCopiesPlainDocument() throws Exception {
        byte[] bytes = (byte[]) ConvertUtils.convert(
                new StreamSource(new StringReader("<a><b>text</b></a>")), byte[].class);
        String out = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(out, out.contains("<b>text</b>"));
    }
}
