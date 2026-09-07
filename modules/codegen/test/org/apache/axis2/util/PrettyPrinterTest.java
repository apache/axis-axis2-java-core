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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

/**
 * The formatter rewrites leading whitespace and nothing else. These pin that
 * property, because it is what makes running it over generated source safe: the
 * worst a bug can do is indent badly, never change what the code means.
 */
public class PrettyPrinterTest {

    /** Everything except leading whitespace must survive byte for byte. */
    private void assertOnlyIndentChanged(String source) {
        String formatted = PrettyPrinter.format(source);
        String[] before = source.split("\n", -1);
        String[] after = formatted.split("\n", -1);
        assertEquals(before.length, after.length, "line count must not change");
        for (int i = 0; i < before.length; i++) {
            assertEquals(before[i].trim(), after[i].trim(),
                    "line " + (i + 1) + " changed beyond its indentation");
        }
    }

    @Test
    public void testNestingIsIndented() {
        String out = PrettyPrinter.format(
                "public class A {\n"
                + "public void m() {\n"
                + "if (x) {\n"
                + "y();\n"
                + "}\n"
                + "}\n"
                + "}\n");
        assertEquals(
                "public class A {\n"
                + "    public void m() {\n"
                + "        if (x) {\n"
                + "            y();\n"
                + "        }\n"
                + "    }\n"
                + "}\n", out);
    }

    /** A brace inside a string must not shift everything after it. */
    @Test
    public void testBracesInStringLiteralsAreNotCounted() {
        String out = PrettyPrinter.format(
                "class A {\n"
                + "String s = \"{{{\";\n"
                + "int i = 1;\n"
                + "}\n");
        assertEquals(
                "class A {\n"
                + "    String s = \"{{{\";\n"
                + "    int i = 1;\n"
                + "}\n", out);
    }

    @Test
    public void testBracesInCharLiteralsAndCommentsAreNotCounted() {
        String out = PrettyPrinter.format(
                "class A {\n"
                + "char c = '{';\n"
                + "// } not a closer\n"
                + "/* { neither */\n"
                + "int i = 1;\n"
                + "}\n");
        assertTrue(out.contains("    char c = '{';"), out);
        assertTrue(out.contains("    int i = 1;"), out);
        assertTrue(out.endsWith("}\n"), out);
    }

    /** An escaped quote must not be read as the end of the literal. */
    @Test
    public void testEscapedQuotesInsideLiterals() {
        assertOnlyIndentChanged("class A {\nString s = \"a\\\"{b\";\nint i = 1;\n}\n");
        String out = PrettyPrinter.format("class A {\nString s = \"a\\\"{b\";\nint i = 1;\n}\n");
        assertTrue(out.contains("    int i = 1;"), out);
    }

    /**
     * Leading whitespace inside a text block is part of the string. Re-indenting
     * one would silently change the value a caller gets back.
     */
    @Test
    public void testTextBlockContentIsUntouched() {
        String source =
                "class A {\n"
                + "String s = \"\"\"\n"
                + "      keep   this\n"
                + "        and this\n"
                + "\"\"\";\n"
                + "}\n";
        String out = PrettyPrinter.format(source);
        assertTrue(out.contains("      keep   this\n"), out);
        assertTrue(out.contains("        and this\n"), out);
    }

    @Test
    public void testBlockCommentContinuationLinesAlign() {
        String out = PrettyPrinter.format(
                "class A {\n"
                + "/**\n"
                + "* javadoc\n"
                + "*/\n"
                + "void m() {}\n"
                + "}\n");
        assertTrue(out.contains("    /**\n     * javadoc\n     */"), out);
    }

    /**
     * A line can close a block comment and still carry code after the delimiter.
     * Dropping those braces shifted every later line permanently.
     */
    @Test
    public void testBracesAfterAClosingBlockCommentAreCounted() {
        String out = PrettyPrinter.format(
                "class A {\n"
                + "void m() {\n"
                + "/* comment\n"
                + "*/ }\n"
                + "int after = 1;\n"
                + "}\n");
        assertTrue(out.contains("\n    int after = 1;\n"),
                "the method closed on the comment line, so this sits at class level:\n" + out);
        assertTrue(out.endsWith("}\n"), out);
    }

    /** Same, for a line that closes a text block and then opens a brace. */
    @Test
    public void testBracesAfterAClosingTextBlockAreCounted() {
        String out = PrettyPrinter.format(
                "class A {\n"
                + "String s = \"\"\"\n"
                + "body\n"
                + "\"\"\"; if (x) {\n"
                + "y();\n"
                + "}\n"
                + "}\n");
        assertTrue(out.contains("\n        y();\n"), out);
    }

    @Test
    public void testBlankLinesStayBlankAndCountIsPreserved() {
        assertOnlyIndentChanged("class A {\n\n   \nvoid m() {}\n\n}\n");
        assertTrue(PrettyPrinter.format("class A {\n\n   \nvoid m() {}\n}\n").contains("\n\n\n"));
    }

    @Test
    public void testClosingBraceOnASharedLine() {
        String out = PrettyPrinter.format(
                "class A {\n"
                + "if (x) {\n"
                + "a();\n"
                + "} else {\n"
                + "b();\n"
                + "}\n"
                + "}\n");
        assertTrue(out.contains("    } else {"), out);
        assertTrue(out.contains("        b();"), out);
    }

    @Test
    public void testMultipleClosersOnOneLine() {
        String out = PrettyPrinter.format("class A {\nvoid m() {\nif (x) {\na();\n}}\n}\n");
        assertTrue(out.contains("        }}"), out);
    }

    /** Nothing pathological should make it drop or add lines. */
    @Test
    public void testUnterminatedLiteralDoesNotRunAway() {
        assertOnlyIndentChanged("class A {\nString s = \"unterminated;\nint i = 1;\n}\n");
    }

    @Test
    public void testCrlfInputKeepsCrlf() {
        String out = PrettyPrinter.format("class A {\r\nint i = 1;\r\n}\r\n");
        assertTrue(out.contains("\r\n"), "CRLF input must stay CRLF");
        assertTrue(out.contains("\r\n    int i = 1;\r\n"), out.replace("\r", "\\r"));
    }

    /** A formatting failure must never destroy the generated file. */
    @Test
    public void testFileIsLeftIntactAndFormattedInPlace() throws Exception {
        File f = File.createTempFile("axis2-prettyprint", ".java");
        try {
            Files.write(f.toPath(), "class A {\nint i = 1;\n}\n".getBytes(StandardCharsets.UTF_8));
            PrettyPrinter.prettify(f);
            String out = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            assertEquals("class A {\n    int i = 1;\n}\n", out);
        } finally {
            f.delete();
        }
    }

    @Test
    public void testAMissingFileIsSurvivable() {
        PrettyPrinter.prettify(new File("/nonexistent/axis2-no-such-file.java"));
    }
}
