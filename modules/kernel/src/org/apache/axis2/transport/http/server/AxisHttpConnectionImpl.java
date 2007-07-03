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

package org.apache.axis2.transport.http.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.RequestLine;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.impl.entity.StrictContentLengthStrategy;
import org.apache.http.impl.io.ChunkedInputStream;
import org.apache.http.impl.io.ChunkedOutputStream;
import org.apache.http.impl.io.ContentLengthInputStream;
import org.apache.http.impl.io.ContentLengthOutputStream;
import org.apache.http.impl.io.HttpDataInputStream;
import org.apache.http.impl.io.IdentityOutputStream;
import org.apache.http.impl.io.SocketHttpDataReceiver;
import org.apache.http.impl.io.SocketHttpDataTransmitter;
import org.apache.http.io.HttpDataReceiver;
import org.apache.http.io.HttpDataTransmitter;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.message.BufferedHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.HeaderUtils;

public class AxisHttpConnectionImpl implements AxisHttpConnection {

    private static final Log HEADERLOG =
        LogFactory.getLog("org.apache.axis2.transport.http.server.wire");

    private final Socket socket;
    private final HttpDataTransmitter datatransmitter;
    private final HttpDataReceiver datareceiver;
    private final CharArrayBuffer charbuffer; 
    private final HttpRequestFactory requestfactory;
    private final ContentLengthStrategy contentLenStrategy;
    private final int maxHeaderCount;
    private final int maxLineLen;

    private OutputStream out = null;
    private InputStream in = null;
    
    public AxisHttpConnectionImpl(final Socket socket, final HttpParams params) 
            throws IOException {
        super();
        if (socket == null) {
            throw new IllegalArgumentException("Socket may not be null"); 
        }
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null"); 
        }
        socket.setTcpNoDelay(HttpConnectionParams.getTcpNoDelay(params));
        socket.setSoTimeout(HttpConnectionParams.getSoTimeout(params));
        
        int linger = HttpConnectionParams.getLinger(params);
        if (linger >= 0) {
            socket.setSoLinger(linger > 0, linger);
        }
        
        int buffersize = HttpConnectionParams.getSocketBufferSize(params);
        this.socket = socket;
        this.datatransmitter = new SocketHttpDataTransmitter(socket, buffersize, params); 
        this.datareceiver = new SocketHttpDataReceiver(socket, buffersize, params); 
        this.charbuffer = new CharArrayBuffer(256);
        this.requestfactory = new DefaultHttpRequestFactory();
        this.contentLenStrategy = new StrictContentLengthStrategy();
        this.maxHeaderCount = params.getIntParameter(HttpConnectionParams.MAX_HEADER_COUNT, -1);
        this.maxLineLen = params.getIntParameter(HttpConnectionParams.MAX_LINE_LENGTH, -1);
    }

    public void close() throws IOException {
        this.datatransmitter.flush();
        try {
            this.socket.shutdownOutput();
        } catch (IOException ignore) {
        }
        try {
            this.socket.shutdownInput();
        } catch (IOException ignore) {
        }
        this.socket.close();
    }

    public boolean isOpen() {
        return !this.socket.isClosed();
    }

    public boolean isStale() {
        try {
            this.datareceiver.isDataAvailable(1);
            return false;
        } catch (IOException ex) {
            return true;
        }
    }

    public void shutdown() throws IOException {
        Socket tmpsocket = this.socket;
        if (tmpsocket != null) {
            tmpsocket.close();
        }
    }

    public HttpRequest receiveRequest() throws HttpException, IOException {
        this.charbuffer.clear();
        int i = this.datareceiver.readLine(this.charbuffer);
        if (i == -1) {
            throw new ConnectionClosedException("Client closed connection"); 
        }
        RequestLine requestline = BasicRequestLine.parse(this.charbuffer, 0, this.charbuffer.length());
        HttpRequest request = this.requestfactory.newHttpRequest(requestline);
        Header[] headers = HeaderUtils.parseHeaders(
                this.datareceiver, 
                this.maxHeaderCount,
                this.maxLineLen);
        request.setHeaders(headers);
        
        if (HEADERLOG.isDebugEnabled()) {
            HEADERLOG.debug(">> " + request.getRequestLine().toString());
            for (i = 0; i < headers.length; i++) {
                HEADERLOG.debug(">> " + headers[i].toString());
            }
        }
        
        // Prepare input stream
        this.in = null;
        if (request instanceof HttpEntityEnclosingRequest) {
            long len = this.contentLenStrategy.determineLength(request);
            if (len == ContentLengthStrategy.CHUNKED) {
                this.in = new ChunkedInputStream(this.datareceiver);
            } else if (len == ContentLengthStrategy.IDENTITY) {
                this.in = new HttpDataInputStream(this.datareceiver);                            
            } else {
                this.in = new ContentLengthInputStream(datareceiver, len);
            }
        }
        return request;
    }
    
    public void sendResponse(final HttpResponse response) 
            throws HttpException, IOException {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }

        if (HEADERLOG.isDebugEnabled()) {
            HEADERLOG.debug("<< " + response.getStatusLine().toString());
            Header[] headers = response.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                HEADERLOG.debug("<< " + headers[i].toString());
            }
        }
        
        this.charbuffer.clear();
        BasicStatusLine.format(this.charbuffer, response.getStatusLine());
        this.datatransmitter.writeLine(this.charbuffer);
        for (Iterator it = response.headerIterator(); it.hasNext(); ) {
            Header header = (Header) it.next();
            if (header instanceof BufferedHeader) {
                this.datatransmitter.writeLine(((BufferedHeader)header).getBuffer());
            } else {
                this.charbuffer.clear();
                BasicHeader.format(this.charbuffer, header);
                this.datatransmitter.writeLine(this.charbuffer);
            }
        }
        this.charbuffer.clear();
        this.datatransmitter.writeLine(this.charbuffer);

        // Prepare output stream
        this.out = null;
        HttpVersion ver = response.getStatusLine().getHttpVersion();
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            long len = entity.getContentLength();
            if (entity.isChunked() && ver.greaterEquals(HttpVersion.HTTP_1_1)) {
                this.out = new ChunkedOutputStream(this.datatransmitter);
            } else if (len >= 0) {
                this.out = new ContentLengthOutputStream(this.datatransmitter, len);
            } else {
                this.out = new IdentityOutputStream(this.datatransmitter); 
            }
        } else {
            this.datatransmitter.flush();
        }
    }
    
    public InputStream getInputStream() {
        return this.in;
    }

    public OutputStream getOutputStream() {
        return this.out;
    }
    
    public void flush() throws IOException {
        if (this.out != null) {
            this.out.flush();
        } else {
            this.datatransmitter.flush();
        }
    }

    public void reset() throws IOException {
        if (this.in != null) {
            this.in.close();
            this.in = null;
        }
        if (this.out != null) {
            this.out.flush();
            this.out.close();
            this.out = null;
        }
    }
    
    public int getSocketTimeout() {
        try {
            return this.socket.getSoTimeout();
        } catch (SocketException ex) {
            return -1;
        }
    }

    public void setSocketTimeout(int timeout) {
        try {
            this.socket.setSoTimeout(timeout);
        } catch (SocketException ex) {
        }
    }

    public InetAddress getLocalAddress() {
        if (this.socket != null) {
            return this.socket.getLocalAddress();
        } else {
            return null;
        }
    }

    public int getLocalPort() {
        if (this.socket != null) {
            return this.socket.getLocalPort();
        } else {
            return -1;
        }
    }

    public InetAddress getRemoteAddress() {
        if (this.socket != null) {
            return this.socket.getInetAddress();
        } else {
            return null;
        }
    }

    public int getRemotePort() {
        if (this.socket != null) {
            return this.socket.getPort();
        } else {
            return -1;
        }
    }

    public HttpConnectionMetrics getMetrics() {
        return null;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        if (isOpen()) {
            buffer.append(this.socket.getInetAddress());
        } else {
            buffer.append("closed");
        }
        buffer.append("]");
        return buffer.toString();
    }

}
