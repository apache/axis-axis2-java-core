/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis.transport.http;

import java.io.IOException;
import java.io.InputStream; 

public class NonBlockingBufferedInputStream extends InputStream {

    // current stream to be processed
    private InputStream in;

    // maximum number of bytes allowed to be returned.
    private int remainingContent = Integer.MAX_VALUE;

    // Internal buffer for the input stream
    private byte[] buffer = new byte[4096];
    private int offset = 0;     // bytes before this offset have been processed
    private int numbytes = 0;   // number of valid bytes in this buffer

    /**
     * set the input stream to be used for subsequent reads
     * @param in the InputStream
     */
    public void setInputStream (InputStream in) {
        this.in = in;
        numbytes = 0;
        offset = 0;
        remainingContent = (in==null)? 0 : Integer.MAX_VALUE;
    }

    /**
     * set the maximum number of bytes allowed to be read from this input
     * stream.
     * @param value the Content Length
     */
    public void setContentLength (int value) {
        if (in != null) this.remainingContent = value - (numbytes-offset);
    }

    /**
     * Replenish the buffer with data from the input stream.  This is 
     * guaranteed to read atleast one byte or throw an exception.  When
     * possible, it will read up to the length of the buffer
     * the data is buffered for efficiency.
     * @return the byte read
     */
    private void refillBuffer() throws IOException {
        if (remainingContent <= 0 || in == null) return;

        // determine number of bytes to read
        numbytes = in.available();
        if (numbytes > remainingContent) numbytes=remainingContent;
        if (numbytes > buffer.length) numbytes=buffer.length;
        if (numbytes <= 0) numbytes = 1;

        // actually attempt to read those bytes
        numbytes = in.read(buffer, 0, numbytes);

        // update internal state to reflect this read
        remainingContent -= numbytes;
        offset = 0;
    }

    /**
     * Read a byte from the input stream, blocking if necessary.  Internally
     * the data is buffered for efficiency.
     * @return the byte read
     */
    public int read() throws IOException {
        if (in == null) return -1;
        if (offset >= numbytes) refillBuffer();
        if (offset >= numbytes) return -1;
        return buffer[offset++] & 0xFF;
    }
    
    /**
     * Read bytes from the input stream.  This is guaranteed to return at 
     * least one byte or throw an exception.  When possible, it will return 
     * more bytes, up to the length of the array, as long as doing so would 
     * not require waiting on bytes from the input stream.
     * @param dest      byte array to read into
     * @return the number of bytes actually read
     */
    public int read(byte[] dest) throws IOException {
        return read(dest, 0, dest.length);
    }

    /**
     * Read a specified number of bytes from the input stream.  This is
     * guaranteed to return at least one byte or throw an execption.  When
     * possible, it will return more bytes, up to the length specified,
     * as long as doing so would not require waiting on bytes from the
     * input stream.
     * @param dest      byte array to read into
     * @param off       starting offset into the byte array
     * @param len       maximum number of bytes to read
     * @return the number of bytes actually read
     */
    public int read(byte[] dest, int off, int len) throws IOException {
        int ready = numbytes - offset;

        if (ready >= len) {
            System.arraycopy(buffer, offset, dest, off, len);
            offset += len;
            return len;
        } else if (ready>0) {
            System.arraycopy(buffer, offset, dest, off, ready);
            offset = numbytes;
            return ready;
        } else {
            if (in == null) return -1;
            refillBuffer();
            if (offset >= numbytes) return -1;
            return read(dest,off,len);
        }
    }
    
    /**
     * skip over (and discard) a specified number of bytes in this input
     * stream
     * @param len the number of bytes to be skipped
     * @return the action number of bytes skipped
     */
    public int skip(int len) throws IOException {
        int count = 0;
        while (len-->0 && read()>=0) count++;
        return count;
    }

    /**
     * return the number of bytes available to be read without blocking
     * @return the number of bytes
     */
    public int available() throws IOException {
        if (in == null) return 0;

        // return buffered + available from the stream
        return (numbytes-offset) + in.available();
    }

    /**
     * disassociate from the underlying input stream
     */
    public void close() throws IOException {
        setInputStream(null);
    }

    /**
     * Just like read except byte is not removed from the buffer. 
     * the data is buffered for efficiency.
     * Was added to support multiline http headers. ;-)
     * @return the byte read
     */
    public int peek() throws IOException {
        if (in == null) return -1;
        if (offset >= numbytes) refillBuffer();
        if (offset >= numbytes) return -1;
        return buffer[offset] & 0xFF;
    }
}

