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

/**
 * Escapes a schema-derived string so that it stays a string when the templates
 * splice it into generated Java or C source.
 * <p>
 * Default and fixed values, enumeration facets and the numeric range facets are
 * copied out of the schema and emitted inside string literals -- a field
 * initializer such as {@code ConverterUtil.convertToInt("<value>")}, or a
 * {@code ConverterUtil.compare(param, "<value>")} call. The schema is written by
 * whoever authored the contract, so an unescaped quote closes the literal and the
 * rest of the value becomes code in a class-level initializer, which runs when the
 * bean class loads: on the machine that compiles the generated sources, and again
 * wherever the built application runs. Nobody reads thousands of lines of generated
 * stubs.
 * <p>
 * Escaping the backslash also disposes of the {@code \\uXXXX} route, because Java
 * treats {@code \\u} as a unicode escape only when preceded by an even number of
 * backslashes.
 * <p>
 * The escapes used -- backslash, quote, the named control escapes and three-digit
 * octal -- mean the same thing in Java and in C, so one escaper serves both writers.
 * {@code SchemaCompiler} has escaped the pattern facet this way for years; these are
 * the neighbouring values that were missed.
 */
public final class SourceLiteralEscaper {

    /**
     * The literal break {@code SchemaCompiler} splices into a QName enumeration on
     * purpose, so that one facet value becomes two arguments to
     * {@code ConverterUtil.convertToQName}. It has to survive escaping.
     */
    private static final String QNAME_ARGUMENT_SPLICE = "\", \"";

    private SourceLiteralEscaper() {
    }

    /**
     * @param value a schema-derived string, may be null
     * @return the value, safe to place inside a Java or C string literal
     */
    public static String escape(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder escaped = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\': escaped.append("\\\\"); break;
                case '"':  escaped.append("\\\""); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                case '\b': escaped.append("\\b"); break;
                case '\f': escaped.append("\\f"); break;
                default:
                    if (c < 0x20 || c == 0x7F) {
                        // Three-digit octal, which both languages read the same way.
                        escaped.append('\\');
                        escaped.append((char) ('0' + ((c >> 6) & 0x7)));
                        escaped.append((char) ('0' + ((c >> 3) & 0x7)));
                        escaped.append((char) ('0' + (c & 0x7)));
                    } else {
                        escaped.append(c);
                    }
            }
        }
        return escaped.toString();
    }

    /**
     * Escapes an enumeration facet value, leaving the deliberate QName argument
     * split intact by escaping the text either side of it.
     *
     * @param value the registered facet value, may be null
     * @return the value, safe to place inside a string literal
     */
    public static String escapeEnumFacet(String value) {
        if (value == null) {
            return null;
        }
        int splice = value.indexOf(QNAME_ARGUMENT_SPLICE);
        if (splice < 0) {
            return escape(value);
        }
        return escape(value.substring(0, splice))
                + QNAME_ARGUMENT_SPLICE
                + escape(value.substring(splice + QNAME_ARGUMENT_SPLICE.length()));
    }
}
