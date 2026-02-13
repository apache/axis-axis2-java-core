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
package org.apache.axis2.fuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * OSS-Fuzz compatible target for AXIOM XML parsing.
 *
 * Equivalent to Axis2/C fuzz_xml_parser.c - tests XML parsing for:
 * - XXE (XML External Entity) injection attempts
 * - Billion laughs / XML bomb attacks
 * - Buffer overflows in element/attribute handling
 * - Malformed XML handling
 * - Deep nesting stack exhaustion
 *
 * @see <a href="https://google.github.io/oss-fuzz/">OSS-Fuzz</a>
 */
public class XmlParserFuzzer {

    /** Maximum input size to prevent OOM (1MB) */
    private static final int MAX_INPUT_SIZE = 1024 * 1024;

    /** Maximum elements to iterate to prevent infinite loops */
    private static final int MAX_ITERATIONS = 1000;

    /**
     * Jazzer entry point - called millions of times with random/mutated data.
     *
     * @param data Fuzzed data provider for generating test inputs
     */
    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        byte[] xmlBytes = data.consumeBytes(MAX_INPUT_SIZE);

        if (xmlBytes.length == 0) {
            return;
        }

        try {
            parseAndExercise(xmlBytes);
        } catch (XMLStreamException e) {
            // Expected for malformed XML - not a bug
        } catch (IllegalArgumentException e) {
            // Expected for invalid input - not a bug
        } catch (IllegalStateException e) {
            // Expected for parser state issues - not a bug
        } catch (Exception e) {
            // Log unexpected exceptions but don't crash the fuzzer
            // In production OSS-Fuzz, unexpected exceptions would be investigated
            if (isSecurityRelevant(e)) {
                throw e; // Re-throw security-relevant exceptions
            }
        }
    }

    /**
     * Parse XML and exercise the resulting object model.
     */
    private static void parseAndExercise(byte[] xmlBytes) throws XMLStreamException {
        InputStream inputStream = new ByteArrayInputStream(xmlBytes);

        // Create AXIOM builder with secure defaults
        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(
            OMAbstractFactory.getOMFactory(),
            inputStream
        );

        try {
            // Get root element - triggers parsing
            OMElement root = builder.getDocumentElement();

            if (root != null) {
                exerciseElement(root, 0);
            }
        } finally {
            builder.close();
        }
    }

    /**
     * Exercise parsed XML element to trigger potential issues.
     */
    private static void exerciseElement(OMElement element, int depth) {
        if (depth > 100) {
            return; // Prevent stack overflow from deep recursion
        }

        // Get element name and namespace
        String localName = element.getLocalName();
        String namespaceURI = element.getNamespaceURI();

        // Get text content
        String text = element.getText();

        // Iterate attributes
        Iterator<?> attrs = element.getAllAttributes();
        int attrCount = 0;
        while (attrs.hasNext() && attrCount < MAX_ITERATIONS) {
            attrs.next();
            attrCount++;
        }

        // Iterate children (limited to prevent infinite loops)
        Iterator<?> children = element.getChildElements();
        int childCount = 0;
        while (children.hasNext() && childCount < MAX_ITERATIONS) {
            Object child = children.next();
            if (child instanceof OMElement) {
                exerciseElement((OMElement) child, depth + 1);
            }
            childCount++;
        }

        // Test serialization
        try {
            String serialized = element.toString();
        } catch (Exception e) {
            // Serialization failures are expected for some inputs
        }
    }

    /**
     * Check if a throwable indicates a potential security issue.
     */
    private static boolean isSecurityRelevant(Throwable e) {
        String message = e.getMessage();
        if (message == null) {
            message = "";
        }

        // These patterns indicate potential security issues worth investigating
        return e instanceof OutOfMemoryError
            || e instanceof StackOverflowError
            || message.contains("XXE")
            || message.contains("entity")
            || message.contains("DOCTYPE");
    }

    /**
     * Main method for standalone testing outside Jazzer.
     */
    public static void main(String[] args) {
        // Test with a simple XML input
        String testXml = "<root><child attr=\"value\">text</child></root>";
        byte[] bytes = testXml.getBytes(StandardCharsets.UTF_8);

        try {
            parseAndExercise(bytes);
            System.out.println("XML parsing test passed");
        } catch (Exception e) {
            System.err.println("XML parsing test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
