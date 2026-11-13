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

package org.apache.axis2.datasource;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;

import junit.framework.TestCase;

import org.apache.axiom.om.OMException;
import org.xml.sax.InputSource;

/**
 * A SAXSource without its own XMLReader is copied through a transformer, which
 * parses it with its own parser.
 */
public class SourceDataSourceTests extends TestCase {

    private static final String MARKER = "JAXWS_XXE_SECRET_MARKER";

    private static String text(SAXSource source) throws Exception {
        XMLStreamReader reader = new SourceDataSource(source).getReader();
        StringBuilder text = new StringBuilder();
        while (reader.hasNext()) {
            if (reader.next() == XMLStreamReader.CHARACTERS) {
                text.append(reader.getText());
            }
        }
        return text.toString();
    }

    public void testExternalEntityIsNotResolved() throws Exception {
        File secret = File.createTempFile("jaxws-xxe", ".txt");
        secret.deleteOnExit();
        try (FileWriter w = new FileWriter(secret)) {
            w.write(MARKER);
        }
        String xml = "<?xml version='1.0'?>"
                + "<!DOCTYPE a [<!ENTITY xxe SYSTEM '" + secret.toURI() + "'>]>"
                + "<a>&xxe;</a>";
        try {
            String out = text(new SAXSource(new InputSource(new StringReader(xml))));
            assertFalse("external entity was resolved: " + out, out.contains(MARKER));
        } catch (OMException expected) {
            // Refusing the document outright is equally acceptable.
        }
    }

    public void testPlainDocumentIsRead() throws Exception {
        String out = text(new SAXSource(new InputSource(new StringReader("<a><b>text</b></a>"))));
        assertEquals("text", out);
    }
}
