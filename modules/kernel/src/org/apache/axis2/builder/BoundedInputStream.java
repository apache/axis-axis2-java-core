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

package org.apache.axis2.builder;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link InputStream} that fails once more than a fixed number of bytes has
 * been read from it.
 *
 * <p>This deliberately throws rather than reporting end-of-stream at the limit,
 * the way {@code org.apache.commons.io.input.BoundedInputStream} does. A builder
 * that silently saw EOF would parse a truncated body and hand the service a
 * partial message, which is worse than rejecting an over-sized request.
 */
public class BoundedInputStream extends FilterInputStream {

    private final long maxBytes;
    private long bytesRead;

    /**
     * Wrap a stream, unless no bound was requested.
     *
     * @param in the stream to bound
     * @param maxBytes the ceiling in bytes, or {@link RequestSizeLimits#UNLIMITED}
     * @return a bounded view of the stream, or {@code in} itself when unbounded
     */
    public static InputStream wrap(InputStream in, long maxBytes) {
        if (in == null || maxBytes < 0) {
            return in;
        }
        return new BoundedInputStream(in, maxBytes);
    }

    public BoundedInputStream(InputStream in, long maxBytes) {
        super(in);
        this.maxBytes = maxBytes;
    }

    public int read() throws IOException {
        int b = in.read();
        if (b != -1) {
            count(1);
        }
        return b;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int n = in.read(b, off, len);
        if (n > 0) {
            count(n);
        }
        return n;
    }

    public long skip(long n) throws IOException {
        long skipped = in.skip(n);
        if (skipped > 0) {
            count(skipped);
        }
        return skipped;
    }

    private void count(long n) throws IOException {
        bytesRead += n;
        if (bytesRead > maxBytes) {
            throw new IOException("Request body exceeds the configured maximum of "
                    + maxBytes + " bytes");
        }
    }
}
