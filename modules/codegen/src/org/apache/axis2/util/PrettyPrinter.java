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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tidies up generated Java source by re-indenting it.
 * <p>
 * This used to call google-java-format, which is a full parse-and-reflow
 * formatter and brought guava and a shaded javac with it -- about 6 MB of
 * dependency, shipped in {@code axis2.war}, to tidy machine-generated code that
 * is already well formed because a template emitted it. This replacement has no
 * dependencies.
 * <p>
 * <b>What it does, and deliberately does not do.</b> It rewrites leading
 * whitespace and nothing else. It does not reflow long lines, reorder imports,
 * insert or remove blank lines, or touch a single character that the compiler
 * can see. That is what makes it safe to run over generated source: the worst a
 * bug here can produce is ugly indentation, not code that changed meaning or
 * stopped compiling. Braces are counted with string literals, character
 * literals and comments skipped, so a {@code "}"} inside a string does not
 * shift the indent of everything after it.
 * <p>
 * Text blocks are reproduced verbatim, because their leading whitespace is
 * content: re-indenting one would silently change the string a caller gets back.
 */
public class PrettyPrinter {
    private static final Log log = LogFactory.getLog(PrettyPrinter.class);

    private static final String INDENT = "    ";

    private PrettyPrinter() {
    }

    /**
     * Pretty prints contents of the java source file, in place.
     * <p>
     * The file is left exactly as it was if anything goes wrong. The previous
     * implementation deleted the source before renaming the formatted copy over
     * it, so a formatting failure destroyed the generated file.
     *
     * @param file the java source file to re-indent
     */
    public static void prettify(File file) {
        try {
            String source = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            String formatted = format(source);
            if (!formatted.equals(source)) {
                Files.write(file.toPath(), formatted.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            log.warn("Could not pretty print " + file + "; leaving it as generated", e);
        } catch (RuntimeException e) {
            log.warn("Could not pretty print " + file + "; leaving it as generated", e);
        }
    }

    /**
     * Re-indents Java source.
     *
     * @param source the source to format
     * @return the source with leading whitespace rewritten
     */
    static String format(String source) {
        String lineSeparator = source.indexOf("\r\n") >= 0 ? "\r\n" : "\n";
        List<String> out = new ArrayList<String>();
        Scanner scanner = new Scanner();
        int depth = 0;

        for (String raw : source.split("\r\n|\n|\r", -1)) {
            // Every branch must fold the scan result into depth, including the ones
            // that emit the line unchanged: a line can close a text block or a block
            // comment and still carry code after the delimiter -- "*/ }" is the plain
            // case -- and dropping those braces shifts every later line permanently.
            if (scanner.inTextBlock) {
                // Leading whitespace here is part of the string value.
                out.add(raw);
                depth = Math.max(depth + scanner.scan(raw), 0);
                continue;
            }
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) {
                out.add("");
                depth = Math.max(depth + scanner.scan(raw), 0);
                continue;
            }
            if (scanner.inBlockComment) {
                out.add(indent(depth) + (trimmed.startsWith("*") ? " " + trimmed : trimmed));
                depth = Math.max(depth + scanner.scan(raw), 0);
                continue;
            }
            // A line that starts by closing a block sits at the level of the block
            // it closes first, however many it closes -- "}}" aligns with the inner
            // one, the same place the first token would go on a line of its own.
            int outdent = trimmed.charAt(0) == '}' ? 1 : 0;
            out.add(indent(Math.max(depth - outdent, 0)) + trimmed);
            depth = Math.max(depth + scanner.scan(raw), 0);
        }
        return String.join(lineSeparator, out);
    }

    private static String indent(int depth) {
        StringBuilder sb = new StringBuilder(depth * INDENT.length());
        for (int i = 0; i < depth; i++) {
            sb.append(INDENT);
        }
        return sb.toString();
    }

    /**
     * Walks a line counting braces, carrying block-comment and text-block state
     * across lines. Only braces outside literals and comments are counted.
     */
    private static final class Scanner {
        boolean inBlockComment;
        boolean inTextBlock;

        /** @return the net brace delta contributed by this line */
        int scan(String line) {
            int delta = 0;
            int i = 0;
            int n = line.length();
            while (i < n) {
                char c = line.charAt(i);
                if (inBlockComment) {
                    if (c == '*' && i + 1 < n && line.charAt(i + 1) == '/') {
                        inBlockComment = false;
                        i += 2;
                    } else {
                        i++;
                    }
                    continue;
                }
                if (inTextBlock) {
                    if (c == '"' && i + 2 < n + 1 && line.startsWith("\"\"\"", i)) {
                        inTextBlock = false;
                        i += 3;
                    } else {
                        i++;
                    }
                    continue;
                }
                if (c == '/' && i + 1 < n && line.charAt(i + 1) == '/') {
                    return delta; // rest of the line is a comment
                }
                if (c == '/' && i + 1 < n && line.charAt(i + 1) == '*') {
                    inBlockComment = true;
                    i += 2;
                    continue;
                }
                if (c == '"' && line.startsWith("\"\"\"", i)) {
                    inTextBlock = true;
                    i += 3;
                    continue;
                }
                if (c == '"' || c == '\'') {
                    i = skipLiteral(line, i, c);
                    continue;
                }
                if (c == '{') {
                    delta++;
                } else if (c == '}') {
                    delta--;
                }
                i++;
            }
            return delta;
        }

        /** @return the index just past the closing quote, or the line end if unterminated */
        private int skipLiteral(String line, int start, char quote) {
            int i = start + 1;
            int n = line.length();
            while (i < n) {
                char c = line.charAt(i);
                if (c == '\\') {
                    i += 2;
                    continue;
                }
                if (c == quote) {
                    return i + 1;
                }
                i++;
            }
            return n;
        }
    }
}
