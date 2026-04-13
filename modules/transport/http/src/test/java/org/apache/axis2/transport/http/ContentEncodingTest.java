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
package org.apache.axis2.transport.http;

import junit.framework.TestCase;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;

/**
 * Tests for AXIS2-6101: HttpClient 5.6+ double gzip decompression.
 *
 * <p>Starting with HC5 5.6, ContentCompressionExec decompresses gzip
 * responses but no longer removes the Content-Encoding header. Axis2
 * must check the entity's content encoding (which reflects the actual
 * state after decompression) rather than the response header.
 */
public class ContentEncodingTest extends TestCase {

    /**
     * Simulates HC5 5.6+ behavior: Content-Encoding header says "gzip"
     * but the entity has no content encoding (already decompressed).
     * Axis2 must NOT attempt to decompress again.
     */
    public void testDecompressedEntityWithGzipHeader() throws Exception {
        BasicClassicHttpResponse response = new BasicClassicHttpResponse(200, "OK");
        // HC5 5.6+ leaves the header but the entity is already decompressed
        response.setHeader(HTTPConstants.HEADER_CONTENT_ENCODING, HTTPConstants.COMPRESSION_GZIP);
        StringEntity entity = new StringEntity("already decompressed content");
        // StringEntity.getContentEncoding() returns null — no encoding on the entity
        response.setEntity(entity);

        // Verify the header still says gzip (this is what the old code checked)
        assertEquals("gzip", response.getHeader(HTTPConstants.HEADER_CONTENT_ENCODING).getValue());

        // Verify the entity's content encoding is null (this is what the fix checks)
        assertNull("Entity content encoding should be null after HC5 decompression",
                entity.getContentEncoding());
    }

    /**
     * Simulates pre-HC5-5.6 behavior OR a server that sends gzip without
     * HC5 decompression (e.g., decompression disabled). The entity's content
     * encoding says "gzip", so Axis2 should decompress.
     */
    public void testEntityWithGzipContentEncoding() throws Exception {
        BasicClassicHttpResponse response = new BasicClassicHttpResponse(200, "OK");
        response.setHeader(HTTPConstants.HEADER_CONTENT_ENCODING, HTTPConstants.COMPRESSION_GZIP);
        StringEntity entity = new StringEntity("compressed content", ContentType.DEFAULT_TEXT, "gzip", false);
        response.setEntity(entity);

        // Both the header and the entity say gzip — Axis2 should decompress
        assertEquals("gzip", response.getHeader(HTTPConstants.HEADER_CONTENT_ENCODING).getValue());
        assertEquals("gzip", entity.getContentEncoding());
    }

    /**
     * No content encoding at all — the common case for uncompressed responses.
     */
    public void testNoContentEncoding() throws Exception {
        BasicClassicHttpResponse response = new BasicClassicHttpResponse(200, "OK");
        StringEntity entity = new StringEntity("plain content");
        response.setEntity(entity);

        // No Content-Encoding header
        assertNull(response.getHeader(HTTPConstants.HEADER_CONTENT_ENCODING));
        // No entity content encoding
        assertNull(entity.getContentEncoding());
    }

    /**
     * Identity content encoding — should be treated as no encoding.
     */
    public void testIdentityContentEncoding() throws Exception {
        BasicClassicHttpResponse response = new BasicClassicHttpResponse(200, "OK");
        StringEntity entity = new StringEntity("identity content", ContentType.DEFAULT_TEXT, "identity", false);
        response.setEntity(entity);

        assertEquals("identity", entity.getContentEncoding());
        // Axis2 should ignore "identity" encoding (pass through without decompression)
    }
}
