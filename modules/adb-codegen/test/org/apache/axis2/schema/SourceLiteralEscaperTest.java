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
package org.apache.axis2.schema;

import junit.framework.TestCase;

/**
 * Schema-derived defaults, enumeration facets and range facets are spliced into
 * generated source inside string literals. A value able to close its own literal
 * becomes code in a class-level initializer, which runs when the generated bean
 * class loads -- on the machine compiling the sources, and wherever the built
 * application runs.
 */
public class SourceLiteralEscaperTest extends TestCase {

    /** The injection from the finding: close the literal, then run something. */
    public void testAValueCannotCloseItsLiteral() {
        String hostile = "x\"),Runtime.getRuntime().exec(\"calc\");//";
        String escaped = SourceLiteralEscaper.escape(hostile);
        assertFalse("no bare quote may survive", hasBareQuote(escaped));
        assertTrue("the text is preserved, just escaped", escaped.contains("Runtime"));
    }

    /**
     * Escaping the backslash closes the unicode-escape route too: Java reads a
     * backslash-u sequence as a unicode escape only after an even number of
     * backslashes, so doubling the backslash makes it inert.
     */
    public void testUnicodeEscapesAreNeutralised() {
        assertEquals("\\\\u0022", SourceLiteralEscaper.escape("\\u0022"));
    }

    public void testControlCharactersBecomeEscapes() {
        assertEquals("a\\nb", SourceLiteralEscaper.escape("a\nb"));
        assertEquals("a\\rb", SourceLiteralEscaper.escape("a\rb"));
        assertEquals("a\\tb", SourceLiteralEscaper.escape("a\tb"));
        // Three-digit octal, read the same way by Java and by C.
        String withSoh = "a" + ((char) 1) + "b";
        assertEquals("a\\001b", SourceLiteralEscaper.escape(withSoh));
    }

    public void testOrdinaryValuesAreUnchanged() {
        assertEquals("42", SourceLiteralEscaper.escape("42"));
        assertEquals("2026-09-03T00:00:00Z",
                SourceLiteralEscaper.escape("2026-09-03T00:00:00Z"));
        assertEquals("plain text", SourceLiteralEscaper.escape("plain text"));
        assertNull(SourceLiteralEscaper.escape(null));
    }

    /**
     * SchemaCompiler splices a quote-comma-quote into a QName enumeration on
     * purpose, so one facet becomes two arguments. Escaping it away would break
     * QName enums, so the halves are escaped and the split itself is left alone.
     */
    public void testTheDeliberateQNameSpliceSurvives() {
        String registered = "ns:local\", \"http://example.com/ns";
        assertEquals("ns:local\", \"http://example.com/ns",
                SourceLiteralEscaper.escapeEnumFacet(registered));
    }

    /** A hostile value either side of that split is still escaped. */
    public void testHostileHalvesOfASpliceAreStillEscaped() {
        String hostile = "a\"),x(\"\", \"b\"),y(\"";
        String escaped = SourceLiteralEscaper.escapeEnumFacet(hostile);
        int splices = 0;
        int at = escaped.indexOf("\", \"");
        while (at >= 0) {
            splices++;
            at = escaped.indexOf("\", \"", at + 1);
        }
        assertEquals("exactly the one deliberate split remains", 1, splices);
    }

    /** A quote not preceded by a backslash. */
    private boolean hasBareQuote(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '"' && (i == 0 || value.charAt(i - 1) != '\\')) {
                return true;
            }
        }
        return false;
    }
}
