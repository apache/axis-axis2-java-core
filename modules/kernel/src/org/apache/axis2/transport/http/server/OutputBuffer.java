/*
* $HeadURL$
* $Revision$
* $Date$
*
* ====================================================================
*
*  Copyright 1999-2004 The Apache Software Foundation
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation.  For more
* information on the Apache Software Foundation, please see
* <http://www.apache.org/>.
*/
package org.apache.axis2.transport.http.server;

import org.apache.axis2.transport.OutTransportInfo;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OutputBuffer implements OutTransportInfo, HttpEntity {

    private final ByteArrayBuffer buffer;
    private String contentType;
    private boolean chunked;
    
    public OutputBuffer(int initialCapacity) {
        super();
        this.buffer = new ByteArrayBuffer(initialCapacity);
        this.contentType = "text/xml";
    }
    
    public OutputBuffer() {
        this(1024);
    }

    public OutputStream getOutputStream() {
        return new BufferOutputStream(this.buffer);
    }

    public InputStream getContent() throws IOException, IllegalStateException {
        return new ByteArrayInputStream(this.buffer.toByteArray());
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public Header getContentType() {
        return new BasicHeader(HTTP.CONTENT_TYPE, this.contentType);
    }

    public void consumeContent() throws IOException {
    }

    public Header getContentEncoding() {
        return null;
    }

    public long getContentLength() {
        return this.buffer.length();
    }

    public boolean isChunked() {
        return this.chunked;
    }

    public void setChunked(boolean b) {
        this.chunked = b;
    }

    public boolean isRepeatable() {
        return true;
    }

    public boolean isStreaming() {
        return false;
    }

    public void writeTo(final OutputStream outstream) throws IOException {
        outstream.write(this.buffer.buffer(), 0, this.buffer.length());
    }

    public String toString() {
        return new String(this.buffer.buffer(), 0, this.buffer.length());
    }
    
    private static class BufferOutputStream extends OutputStream {

        private final ByteArrayBuffer buffer;
        private boolean closed = false;

        public BufferOutputStream(final ByteArrayBuffer buffer) {
            super();
            this.buffer = buffer;
        }
        
        public void close() throws IOException {
            this.closed = true;
        }

        private void ensureNotClosed() {
            if (this.closed) {
                throw new IllegalStateException("Stream closed");
            }
        }
        
        public void write(byte[] b, int off, int len) throws IOException {
            ensureNotClosed();
            if (b == null) {
                return;
            }
            this.buffer.append(b, off, len);
        }

        public void write(byte[] b) throws IOException {
            ensureNotClosed();
            if (b == null) {
                return;
            }
            this.buffer.append(b, 0, b.length);
        }

        public void write(int b) throws IOException {
            ensureNotClosed();
            this.buffer.append(b);
        }
                
    }
    
}
