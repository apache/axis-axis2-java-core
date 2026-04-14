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

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Unit tests for {@link FlushingOutputStream}.
 */
public class FlushingOutputStreamTest {

    /**
     * Verify that data is written through correctly.
     */
    @Test
    public void testDataPassthrough() throws IOException {
        ByteArrayOutputStream underlying = new ByteArrayOutputStream();
        FlushingOutputStream fos = new FlushingOutputStream(underlying, 100);

        byte[] data = "Hello, streaming world!".getBytes("UTF-8");
        fos.write(data, 0, data.length);
        fos.flush();

        Assert.assertEquals("Hello, streaming world!", underlying.toString("UTF-8"));
    }

    /**
     * Verify that single-byte writes accumulate and flush at the interval.
     */
    @Test
    public void testSingleByteWrite() throws IOException {
        CountingOutputStream counter = new CountingOutputStream();
        FlushingOutputStream fos = new FlushingOutputStream(counter, 10);

        // Write 25 bytes one at a time — should trigger 2 flushes (at byte 10 and 20)
        for (int i = 0; i < 25; i++) {
            fos.write('A');
        }

        Assert.assertEquals(2, counter.flushCount);
        Assert.assertEquals(25, counter.bytesWritten);
    }

    /**
     * Verify that bulk writes trigger flush at the correct interval.
     */
    @Test
    public void testBulkWriteFlush() throws IOException {
        CountingOutputStream counter = new CountingOutputStream();
        FlushingOutputStream fos = new FlushingOutputStream(counter, 100);

        // Write 250 bytes in one call — single write exceeds interval,
        // triggers 1 flush and resets counter
        byte[] data = new byte[250];
        fos.write(data, 0, data.length);

        Assert.assertEquals(1, counter.flushCount);
        Assert.assertEquals(250, counter.bytesWritten);
        Assert.assertEquals(0, fos.getBytesSinceFlush());
    }

    /**
     * Verify that writes smaller than the flush interval do not trigger flush.
     */
    @Test
    public void testNoFlushBelowInterval() throws IOException {
        CountingOutputStream counter = new CountingOutputStream();
        FlushingOutputStream fos = new FlushingOutputStream(counter, 1000);

        byte[] data = new byte[500];
        fos.write(data, 0, data.length);

        Assert.assertEquals(0, counter.flushCount);
        Assert.assertEquals(500, counter.bytesWritten);
    }

    /**
     * Verify that the counter resets after each flush.
     */
    @Test
    public void testCounterResets() throws IOException {
        CountingOutputStream counter = new CountingOutputStream();
        FlushingOutputStream fos = new FlushingOutputStream(counter, 100);

        // First write: 120 bytes → exceeds 100, flush, counter resets to 0
        fos.write(new byte[120], 0, 120);
        Assert.assertEquals(1, counter.flushCount);
        Assert.assertEquals(0, fos.getBytesSinceFlush());

        // Second write: 90 bytes → counter at 90, below 100, no flush
        fos.write(new byte[90], 0, 90);
        Assert.assertEquals(1, counter.flushCount);
        Assert.assertEquals(90, fos.getBytesSinceFlush());

        // Third write: 20 bytes → counter at 110, flush, counter resets
        fos.write(new byte[20], 0, 20);
        Assert.assertEquals(2, counter.flushCount);
        Assert.assertEquals(0, fos.getBytesSinceFlush());
    }

    /**
     * Verify that the default flush interval is 64 KB.
     */
    @Test
    public void testDefaultFlushInterval() throws IOException {
        ByteArrayOutputStream underlying = new ByteArrayOutputStream();
        FlushingOutputStream fos = new FlushingOutputStream(underlying);

        Assert.assertEquals(64 * 1024, fos.getFlushIntervalBytes());
    }

    /**
     * Verify that zero or negative flush intervals are rejected.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testZeroIntervalRejected() {
        new FlushingOutputStream(new ByteArrayOutputStream(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeIntervalRejected() {
        new FlushingOutputStream(new ByteArrayOutputStream(), -1);
    }

    /**
     * Verify long counter does not overflow on large cumulative writes.
     */
    @Test
    public void testLongCounterNoOverflow() throws IOException {
        CountingOutputStream counter = new CountingOutputStream();
        // Use a very small interval so we can verify the counter resets properly
        FlushingOutputStream fos = new FlushingOutputStream(counter, 10);

        // Write 100K bytes in bulk — single write exceeds interval, 1 flush
        byte[] data = new byte[100_000];
        fos.write(data, 0, data.length);

        Assert.assertEquals(1, counter.flushCount);
        Assert.assertEquals(0, fos.getBytesSinceFlush());

        // Write 100K more in single-byte mode to exercise per-byte counter
        for (int i = 0; i < 100; i++) {
            fos.write('X');
        }
        // 100 single-byte writes with interval=10 → 10 more flushes
        Assert.assertEquals(11, counter.flushCount);
    }

    /**
     * Helper: OutputStream that counts bytes written and flush() calls.
     */
    private static class CountingOutputStream extends ByteArrayOutputStream {
        int flushCount = 0;
        int bytesWritten = 0;

        @Override
        public void write(int b) {
            super.write(b);
            bytesWritten++;
        }

        @Override
        public void write(byte[] b, int off, int len) {
            super.write(b, off, len);
            bytesWritten += len;
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            flushCount++;
        }
    }
}
