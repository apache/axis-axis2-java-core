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

import org.apache.axis2.AxisFault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ContentLengthStrategy;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.BasicEndpointDetails;
import org.apache.hc.core5.http.impl.DefaultContentLengthStrategy;
import org.apache.hc.core5.http.impl.io.AbstractMessageParser;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpRequestFactory;
import org.apache.hc.core5.http.impl.io.DefaultHttpRequestParserFactory;
import org.apache.hc.core5.http.impl.io.DefaultHttpResponseWriterFactory;
import org.apache.hc.core5.http.impl.io.ChunkedInputStream;
import org.apache.hc.core5.http.impl.io.ChunkedOutputStream;
import org.apache.hc.core5.http.impl.io.ContentLengthInputStream;
import org.apache.hc.core5.http.impl.io.ContentLengthOutputStream;
import org.apache.hc.core5.http.impl.io.IdentityInputStream;
import org.apache.hc.core5.http.impl.io.IdentityOutputStream;
import org.apache.hc.core5.http.impl.io.SessionInputBufferImpl;
import org.apache.hc.core5.http.impl.io.SessionOutputBufferImpl;
import org.apache.hc.core5.http.impl.io.SocketHolder;
import org.apache.hc.core5.http.io.HttpMessageParser;
import org.apache.hc.core5.http.io.HttpMessageWriter;
import org.apache.hc.core5.http.io.HttpMessageParserFactory;
import org.apache.hc.core5.http.io.HttpMessageWriterFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.SessionInputBuffer;
import org.apache.hc.core5.http.io.SessionOutputBuffer;
import org.apache.hc.core5.http.io.entity.EmptyInputStream;
import org.apache.hc.core5.http.message.LazyLineParser;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.net.InetAddressUtils;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSession;

import java.util.UUID;

public class AxisHttpConnectionImpl implements AxisHttpConnection {

    private static final Log HEADERLOG =
        LogFactory.getLog("org.apache.axis2.transport.http.server.wire");

    private static final Timeout STALE_CHECK_TIMEOUT = Timeout.ofMilliseconds(1);

    private final String scheme;
    private final SessionOutputBufferImpl outbuffer;
    private final SessionInputBufferImpl inbuffer;
    private final HttpMessageParser<ClassicHttpRequest> requestParser;
    private final HttpMessageWriter<ClassicHttpResponse> responseWriter;
    private final ContentLengthStrategy contentLenStrategy; 
    private final AtomicReference<SocketHolder> socketHolderRef; 
    private final Http1Config http1Config;
    // Lazily initialized chunked request buffer provided to ChunkedOutputStream.
    private byte[] chunkedRequestBuffer;

    volatile ProtocolVersion version;
    volatile EndpointDetails endpointDetails;
    
    private OutputStream out = null;
    private InputStream in = null;

    public AxisHttpConnectionImpl(final String scheme, final Socket socket, final Http1Config http1Config, final SocketConfig socketConfig) 
            throws IOException {
        super();
        if (socket == null) {
            throw new IllegalArgumentException("Socket may not be null"); 
        }
        if (socketConfig == null) {
            throw new IllegalArgumentException("socketConfig may not be null"); 
        }
        this.scheme = scheme != null ? scheme : URIScheme.HTTP.getId();
        socket.setTcpNoDelay(socketConfig.isTcpNoDelay());
        socket.setSoTimeout(socketConfig.getSoTimeout().toMillisecondsIntBound());
        
        int linger = socketConfig.getSoLinger().toMillisecondsIntBound();
        if (linger >= 0) {
            socket.setSoLinger(linger > 0, linger);
        }
        
        int buffersize = socketConfig.getRcvBufSize();
        this.inbuffer = new SessionInputBufferImpl(buffersize, 8000); 
        this.outbuffer = new SessionOutputBufferImpl(buffersize); 
        this.contentLenStrategy = new DefaultContentLengthStrategy();

        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        HttpMessageParserFactory<ClassicHttpRequest> requestParserFactory = new DefaultHttpRequestParserFactory(this.http1Config);

        HttpMessageWriterFactory<ClassicHttpResponse> responseWriterFactory = new DefaultHttpResponseWriterFactory(this.http1Config);
        this.requestParser = requestParserFactory.create(this.http1Config);
        this.responseWriter = responseWriterFactory.create();
	socketHolderRef = new AtomicReference<>(new SocketHolder(socket));
    }

    @Override
    public void close(final CloseMode closeMode) {
        final SocketHolder socketHolder = this.socketHolderRef.getAndSet(null);
        if (socketHolder != null) {
            final Socket socket = socketHolder.getSocket();
            try {
                if (closeMode == CloseMode.IMMEDIATE) {
                    // force abortive close (RST)
                    socket.setSoLinger(true, 0);
                }
            } catch (final IOException ignore) {
            } finally {
                Closer.closeQuietly(socket);
            }
        }
    }

    @Override
    public void close() throws IOException {
	if (this.outbuffer != null && this.out != null) {
            this.outbuffer.flush(this.out);
	}    	

        final SocketHolder socketHolder = this.socketHolderRef.getAndSet(null);
        final Socket socket = socketHolder.getSocket();
        try {
            socket.shutdownOutput();
        } catch (IOException ignore) {
        }
        try {
            socket.shutdownInput();
        } catch (IOException ignore) {
        }
        socket.close();
    }

    public boolean isOpen() {
        return this.socketHolderRef.get() != null;
    }

    public boolean isStale() throws IOException {
        if (!isOpen()) {
            return true;
        }
        try {
            final int bytesRead = fillInputBuffer(STALE_CHECK_TIMEOUT);
            return bytesRead < 0;
        } catch (final SocketTimeoutException ex) {
            return false;
        } catch (final SocketException ex) {
            return true;
        }
    }

    private int fillInputBuffer(final Timeout timeout) throws IOException {
        final SocketHolder socketHolder = ensureOpen();
        final Socket socket = socketHolder.getSocket();
        final int oldtimeout = socket.getSoTimeout();
        try {
            socket.setSoTimeout(timeout.toMillisecondsIntBound());
            return this.inbuffer.fillBuffer(socketHolder.getInputStream());
        } finally {
            socket.setSoTimeout(oldtimeout);
        }
    }

    public void shutdown() throws IOException {
        final SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder == null) {
            return;
        }
        final Socket socket = socketHolder.getSocket();
        if (socket != null) {
            socket.close();
        }
    }

    // see org.apache.hc.core5.http.impl.io.DefaultBHttpServerConnection methods
    // receiveRequest() and receiveRequestEntity()
    @Override
    public ClassicHttpRequest receiveRequest() throws HttpException, IOException {
        String uuid = UUID.randomUUID().toString();
        // Prepare input stream
        final SocketHolder socketHolder = ensureOpen();
        this.in = socketHolder.getInputStream();
	CharArrayBuffer headLine = new CharArrayBuffer(128);
        this.inbuffer.clear();
        final int i = this.inbuffer.readLine(headLine, this.in);
        if (i == -1) {
            throw new IOException("readLine() SessionInputBufferImpl returned -1 in method receiveRequest() with uuid: " +uuid+ "  ... at time: " +LocalDateTime.now());
        }

	final Header[] headers = AbstractMessageParser.parseHeaders(
                    this.inbuffer,
                    this.in,
                    this.http1Config.getMaxHeaderCount(),
                    this.http1Config.getMaxLineLength(),
                    LazyLineParser.INSTANCE);

        final RequestLine requestLine = LazyLineParser.INSTANCE.parseRequestLine(headLine);
        final ClassicHttpRequest request = DefaultClassicHttpRequestFactory.INSTANCE.newHttpRequest(requestLine.getMethod(), requestLine.getUri());
        request.setHeaders(headers);

	final long len = DefaultContentLengthStrategy.INSTANCE.determineLength(request);
	this.in = createContentInputStream(len, this.inbuffer, this.in);
	
        if (HEADERLOG.isDebugEnabled()) {
            HEADERLOG.debug(">> " + new RequestLine(request));
            for (final Header header : request.getHeaders()) {
                HEADERLOG.debug(">> " + header);
            }
        }
        return request;
    }
    
    public void sendResponse(final ClassicHttpResponse response)
            throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        if (HEADERLOG.isDebugEnabled()) {
            HEADERLOG.debug("<< " + new StatusLine(response));
	    final Header[] headers = response.getHeaders();
            for (final Header header : headers) {
                HEADERLOG.debug(">> " + header);
            }
        }
        final HttpEntity entity = response.getEntity();
        if (entity == null) {
            if (this.out != null) {
                HEADERLOG.debug("AxisHttpConnectionImpl.sendResponseEntity() found null entity, will flush() and return  ... ");
                this.outbuffer.flush(this.out);
	    }
            return;
        }
        final long len = this.contentLenStrategy.determineLength(response);
        final SocketHolder socketHolder = ensureOpen();
        this.out = null;
        this.out = createContentOutputStream(len, this.outbuffer, socketHolder.getOutputStream(), entity.getTrailers());
	// send HTTP response headers and status etc, see core5 method sendResponseHeaders()
        this.responseWriter.write(response, this.outbuffer, this.out);
	// send HTTP response content, leave cleanup via flush and close etc to the caller
	// see core5 method sendResponseEntity()
        entity.writeTo(this.out);
    }

    private byte[] getChunkedRequestBuffer() {
        if (chunkedRequestBuffer == null) {
            final int chunkSizeHint = this.http1Config.getChunkSizeHint();
            chunkedRequestBuffer = new byte[chunkSizeHint > 0 ? chunkSizeHint : 8192];
        }
        return chunkedRequestBuffer;
    }

    public InputStream createContentInputStream(
            final long len,
            final SessionInputBuffer buffer,
            final InputStream inputStream) {
        if (len > 0) {
            return new ContentLengthInputStream(buffer, inputStream, len);
        } else if (len == 0) {
            return EmptyInputStream.INSTANCE;
        } else if (len == ContentLengthStrategy.CHUNKED) {
            return new ChunkedInputStream(buffer, inputStream, this.http1Config);
        } else {
            return new IdentityInputStream(buffer, inputStream);
        }
    }

    public OutputStream createContentOutputStream(
            final long len,
            final SessionOutputBuffer buffer,
            final OutputStream outputStream,
            final Supplier<List<? extends Header>> trailers) {
        if (len >= 0) {
            return new ContentLengthOutputStream(buffer, outputStream, len);
        } else if (len == ContentLengthStrategy.CHUNKED) {
            return new ChunkedOutputStream(buffer, outputStream, getChunkedRequestBuffer(), trailers);
        } else {
            return new IdentityOutputStream(buffer, outputStream);
        }
    }

    public SocketHolder getSocketHolder() {
        return this.socketHolderRef.get();
    }
    
    public InputStream getInputStream() {
        try {
            return this.in;
        } catch (Exception ex) {
            HEADERLOG.error("getInputStream() error: " + ex.getMessage(), ex);
	    return null;
        }
    }

    public OutputStream getOutputStream() {
        return this.out;
    }

    protected SocketHolder ensureOpen() throws IOException {
        final SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder == null) {
            throw new ConnectionClosedException();
        }
        return socketHolder;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.version;
    }

    @Override
    public void flush() throws IOException {
	if (this.out != null) {
            this.out.flush();
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
    
    @Override
    public Timeout getSocketTimeout() {
	final SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder != null) {
            try {
                return Timeout.ofMilliseconds(socketHolder.getSocket().getSoTimeout());
            } catch (final SocketException ignore) {
            }
        }
        return Timeout.INFINITE;    
    }

    @Override
    public void setSocketTimeout(final Timeout timeout) {
        final SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder != null) {
            try {
                socketHolder.getSocket().setSoTimeout(Timeout.defaultsToInfinite(timeout).toMillisecondsIntBound());
            } catch (final SocketException ignore) {
                // It is not quite clear from the Sun's documentation if there are any
                // other legitimate cases for a socket exception to be thrown when setting
                // SO_TIMEOUT besides the socket being already closed
            }
        }
    }

    @Override
    public SocketAddress getLocalAddress() {
        final SocketHolder socketHolder = this.socketHolderRef.get();

        return socketHolder != null ? socketHolder.getSocket().getLocalSocketAddress() : null;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        final SocketHolder socketHolder = this.socketHolderRef.get();
        return socketHolder != null ? socketHolder.getSocket().getRemoteSocketAddress() : null;
    }

    @Override
    public SSLSession getSSLSession() {
        final SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder != null) {
            final Socket socket = socketHolder.getSocket();
            return socket instanceof SSLSocket ? ((SSLSocket) socket).getSession() : null;
        }
        return null;
    }

    @Override
    public EndpointDetails getEndpointDetails() {
        if (endpointDetails == null) {
            final SocketHolder socketHolder = this.socketHolderRef.get();
            if (socketHolder != null) {
                @SuppressWarnings("resource")
                final Socket socket = socketHolder.getSocket();
                Timeout socketTimeout;
                try {
                    socketTimeout = Timeout.ofMilliseconds(socket.getSoTimeout());
                } catch (final SocketException e) {
                    socketTimeout = Timeout.INFINITE;
                }
                endpointDetails = new BasicEndpointDetails(
                        socket.getRemoteSocketAddress(),
                        socket.getLocalSocketAddress(),
                        null,
                        socketTimeout);
            }
        }
        return endpointDetails;
    }

    @Override
    public String toString() {
        final SocketHolder socketHolder = this.socketHolderRef.get();
        if (socketHolder != null) {
            final Socket socket = socketHolder.getSocket();
            final StringBuilder buffer = new StringBuilder();
            final SocketAddress remoteAddress = socket.getRemoteSocketAddress();
            final SocketAddress localAddress = socket.getLocalSocketAddress();
            if (remoteAddress != null && localAddress != null) {
                InetAddressUtils.formatAddress(buffer, localAddress);
                buffer.append("<->");
                InetAddressUtils.formatAddress(buffer, remoteAddress);
            }
            return buffer.toString();
        }
        return "[Not bound]";
    }

}
