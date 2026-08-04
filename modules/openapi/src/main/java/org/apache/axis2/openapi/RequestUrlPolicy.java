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

package org.apache.axis2.openapi;

import java.util.regex.Pattern;

/**
 * Validation and output-encoding helpers for values taken from the inbound
 * request that end up in documents this module serves — the Swagger UI page
 * and the OpenAPI/MCP specifications.
 *
 * <p>{@code HttpServletRequest.getServerName()} returns the client-supplied
 * Host (or the proxy-supplied forwarded host where the container is configured
 * to honour it), so it is attacker-controlled. Two separate protections apply:
 * {@link #isSafeHost} keeps a host that is not a syntactically valid registered
 * name or IP literal out of served URLs entirely, and
 * {@link #escapeJavaScriptString} encodes whatever does get emitted for the
 * JavaScript string context it lands in.
 */
final class RequestUrlPolicy {

    /**
     * A registered name as permitted by RFC 1123: dot-separated labels of
     * alphanumerics and hyphens, where a label neither starts nor ends with a
     * hyphen. A single trailing dot (the fully-qualified form) is allowed.
     */
    private static final Pattern REGISTERED_NAME = Pattern.compile(
            "[A-Za-z0-9](?:[A-Za-z0-9\\-]*[A-Za-z0-9])?"
                    + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9\\-]*[A-Za-z0-9])?)*\\.?");

    /**
     * An IPv6 literal, with or without the surrounding brackets — containers
     * differ on whether {@code getServerName()} strips them. The character set
     * covers the embedded-IPv4 and zone-id forms as well.
     */
    private static final Pattern IP_LITERAL = Pattern.compile(
            "\\[?[0-9A-Fa-f:.]{2,45}(?:%25?[A-Za-z0-9._~\\-]{1,32})?]?");

    /** RFC 1035 caps a domain name at 255 octets including length prefixes. */
    private static final int MAX_HOST_LENGTH = 253;

    private RequestUrlPolicy() {
    }

    /**
     * Whether a request-derived host may be placed into a served URL.
     *
     * @param host the value from {@code HttpServletRequest.getServerName()}
     * @return true if the host is a syntactically valid registered name or IP
     *         literal, false for null, empty, over-long, or malformed input
     */
    static boolean isSafeHost(String host) {
        if (host == null || host.isEmpty() || host.length() > MAX_HOST_LENGTH) {
            return false;
        }
        return REGISTERED_NAME.matcher(host).matches() || IP_LITERAL.matcher(host).matches();
    }

    /**
     * Encode a value for interpolation into a quoted JavaScript string literal
     * inside an inline {@code <script>} element.
     *
     * <p>Anything outside a conservative alphanumeric-plus-URL-punctuation set
     * is emitted as a numeric escape, which covers the quote and backslash
     * breakouts, the {@code </script>} breakout, and the U+2028/U+2029 line
     * terminators that JavaScript treats as newlines inside a string.
     *
     * @param value the value to encode; null yields an empty string
     * @return a value safe to place between quotes in a script element
     */
    static String escapeJavaScriptString(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            boolean unreserved = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '-' || c == '_' || c == '.' || c == '~'
                    || c == ':' || c == '/' || c == '?' || c == '#'
                    || c == '@' || c == '!' || c == '$' || c == '*'
                    || c == '+' || c == ',' || c == ';' || c == '='
                    || c == '[' || c == ']' || c == '%';
            if (unreserved) {
                out.append(c);
            } else if (c < 0x100) {
                out.append(String.format("\\x%02x", (int) c));
            } else {
                out.append(String.format("\\u%04x", (int) c));
            }
        }
        return out.toString();
    }
}
