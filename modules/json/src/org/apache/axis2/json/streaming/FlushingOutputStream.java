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

package org.apache.axis2.json.streaming;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream wrapper that flushes to the underlying stream every N bytes.
 *
 * <p>When wrapping a servlet/transport OutputStream, each flush pushes
 * buffered data to the HTTP transport layer as a chunk (HTTP/1.1 chunked
 * transfer encoding) or DATA frame (HTTP/2). This prevents reverse proxies
 * from rejecting large responses due to body-size limits — the proxy sees
 * a stream of small chunks, never the full response body.</p>
 *
 * <p>Used by {@link JSONStreamingMessageFormatter} to enable transparent
 * streaming for any Axis2 JSON service without service code changes.</p>
 *
 * <p>Default flush interval is 64 KB, chosen to align with typical
 * HTTP/2 DATA frame sizes and reverse proxy buffer thresholds.</p>
 *
 * @since 2.0.1
 */
public class FlushingOutputStream extends FilterOutputStream {

    /** Default flush interval: 64 KB */
    public static final int DEFAULT_FLUSH_INTERVAL = 64 * 1024;

    private final int flushIntervalBytes;
    private long bytesSinceFlush;

    /**
     * Wrap an OutputStream with the default flush interval (64 KB).
     *
     * @param out the underlying output stream
     */
    public FlushingOutputStream(OutputStream out) {
        this(out, DEFAULT_FLUSH_INTERVAL);
    }

    /**
     * Wrap an OutputStream with a custom flush interval.
     *
     * @param out                the underlying output stream
     * @param flushIntervalBytes flush every N bytes (must be &gt; 0)
     */
    public FlushingOutputStream(OutputStream out, int flushIntervalBytes) {
        super(out);
        if (flushIntervalBytes <= 0) {
            throw new IllegalArgumentException(
                "flushIntervalBytes must be > 0, got " + flushIntervalBytes);
        }
        this.flushIntervalBytes = flushIntervalBytes;
        this.bytesSinceFlush = 0;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        if (++bytesSinceFlush >= flushIntervalBytes) {
            out.flush();
            bytesSinceFlush = 0;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        bytesSinceFlush += len;
        if (bytesSinceFlush >= flushIntervalBytes) {
            out.flush();
            bytesSinceFlush = 0;
        }
    }

    /**
     * Returns the configured flush interval in bytes.
     */
    public int getFlushIntervalBytes() {
        return flushIntervalBytes;
    }

    /**
     * Returns the number of bytes written since the last flush.
     */
    public long getBytesSinceFlush() {
        return bytesSinceFlush;
    }
}
