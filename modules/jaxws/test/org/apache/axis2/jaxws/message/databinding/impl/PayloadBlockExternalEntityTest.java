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

package org.apache.axis2.jaxws.message.databinding.impl;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import jakarta.mail.util.ByteArrayDataSource;

import junit.framework.TestCase;

import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.Protocol;
import org.apache.axis2.jaxws.message.factory.DataSourceBlockFactory;
import org.apache.axis2.jaxws.message.factory.MessageFactory;
import org.apache.axis2.jaxws.message.factory.SourceBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

/**
 * Source, String and DataSource payloads must not have external entities resolved
 * when the block is read, since the content is whatever the application handed in.
 */
public class PayloadBlockExternalEntityTest extends TestCase {

    private static final String SECRET = "external-entity-content";

    private File secretFile;
    private String payload;

    @Override
    protected void setUp() throws Exception {
        secretFile = File.createTempFile("payload-xxe", ".txt");
        Files.write(secretFile.toPath(), SECRET.getBytes(StandardCharsets.UTF_8));
        payload = "<!DOCTYPE r [<!ENTITY e SYSTEM \"" + secretFile.toURI() + "\">]>"
                + "<r xmlns=\"urn:test\">&e;</r>";
    }

    @Override
    protected void tearDown() throws Exception {
        secretFile.delete();
    }

    public void testStreamSourcePayload() throws Exception {
        SourceBlockFactory factory =
                (SourceBlockFactory) FactoryRegistry.getFactory(SourceBlockFactory.class);
        Block block = factory.createFrom(new StreamSource(new StringReader(payload)), null, null);
        assertFalse(serialize(block).contains(SECRET));
    }

    public void testStringPayload() throws Exception {
        XMLStringBlockFactory factory =
                (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
        Block block = factory.createFrom(payload, null, null);
        assertFalse(serialize(block).contains(SECRET));
    }

    public void testDataSourcePayload() throws Exception {
        DataSourceBlockFactory factory =
                (DataSourceBlockFactory) FactoryRegistry.getFactory(DataSourceBlockFactory.class);
        Block block = factory.createFrom(
                new ByteArrayDataSource(payload.getBytes(StandardCharsets.UTF_8), "application/xml"),
                null, null);
        String text;
        try {
            XMLStreamReader reader = block.getXMLStreamReader(true);
            StringBuilder sb = new StringBuilder();
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.CHARACTERS) {
                    sb.append(reader.getText());
                }
            }
            text = sb.toString();
        } catch (Exception e) {
            // Refusing the entity is as good as not expanding it.
            text = "";
        }
        assertFalse(text.contains(SECRET));
    }

    public void testInternalEntityIsStillExpanded() throws Exception {
        SourceBlockFactory factory =
                (SourceBlockFactory) FactoryRegistry.getFactory(SourceBlockFactory.class);
        String internal = "<!DOCTYPE r [<!ENTITY e \"internal-value\">]><r xmlns=\"urn:test\">&e;</r>";
        Block block = factory.createFrom(new StreamSource(new StringReader(internal)), null, null);
        assertTrue(serialize(block).contains("internal-value"));
    }

    /**
     * Puts the block in a SOAP body, as a Dispatch or Provider would, and serializes the
     * message. An exception counts as not expanding the entity.
     */
    private static String serialize(Block block) {
        MessageFactory mf = (MessageFactory) FactoryRegistry.getFactory(MessageFactory.class);
        try {
            Message message = mf.create(Protocol.soap11);
            message.setBodyBlock(block);
            return message.getAsOMElement().toString();
        } catch (Exception e) {
            return "";
        }
    }
}
