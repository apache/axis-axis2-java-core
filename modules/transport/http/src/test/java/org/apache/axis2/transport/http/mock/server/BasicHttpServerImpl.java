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

package org.apache.axis2.transport.http.mock.server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.io.DefaultBHttpServerConnectionFactory;
import org.apache.hc.core5.http.impl.io.DefaultBHttpServerConnection;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpResponseFactory;
import org.apache.hc.core5.http.impl.io.HttpService;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.HttpServerConnection;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.HttpEntities;
import org.apache.hc.core5.http.io.support.BasicHttpServerExpectationDecorator;
import org.apache.hc.core5.http.io.support.BasicHttpServerRequestHandler;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.http.protocol.HttpProcessorBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.http.protocol.RequestHandlerRegistry;
import org.apache.hc.core5.http.protocol.ResponseConnControl;
import org.apache.hc.core5.http.protocol.ResponseContent;
import org.apache.hc.core5.http.protocol.ResponseDate;
import org.apache.hc.core5.http.protocol.ResponseServer;
import org.apache.hc.core5.io.CloseMode;

/**
 * The purpose of this server application is facilitate to HTTP related test
 * cases as a back end server based on httpcore. Original code copied from
 * ElementalHttpServer class from httpcore component of Apache HTTPComponents
 * project.
 *
 * AXIS2-6051: In the upgrade to httpclient5 and core5 the classes ClassicFileServerExample
 * and ClassicTestServer are the replacements of ElementalHttpServer.  
 * 
 * @see http://svn.apache.org/repos/asf/httpcomponents/httpcore/trunk/httpcore/src/examples/org/apache/http/examples/ElementalHttpServer.java
 * @see https://hc.apache.org/httpcomponents-core-5.2.x/current/httpcore5/xref-test/org/apache/hc/core5/http/examples/ClassicFileServerExample.html     
 * @see https://github.com/apache/httpcomponents-core/blob/master/httpcore5-testing/src/main/java/org/apache/hc/core5/testing/classic/ClassicTestServer.java
 * @since 1.7.0
 * 
 */
public class BasicHttpServerImpl implements BasicHttpServer {

    private RequestListenerThread serverThread;
    private Map<String, String> headers;
    private byte[] content;
    private String method;
    private String uri;
    private String responseTemplate;
    boolean close;

    public BasicHttpServerImpl() {
        headers = new HashMap<String, String>();
        content = null;
        close = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis2.transport.http.mock.server.BasicHttpServer#start()
     */
    public void start() throws Exception {
        serverThread = new RequestListenerThread(this);
        serverThread.setDaemon(false);
        serverThread.start();
    }

    public int getPort() {
        return serverThread.getServersocket().getLocalPort();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis2.transport.http.mock.server.BasicHttpServer#stop()
     */
    public void stop() throws Exception {
        if (close) {
            serverThread.getServersocket().close();
        }

    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getContent() {
        return content;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public void setHeaders(Map<String, String> headers) {
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setUri(String url) {
        this.uri = uri;
    }

    public int getEntityContentLength() {
        return content.length;
    }

    public String getResponseTemplate() {
        return responseTemplate;
    }

    public void setResponseTemplate(String responseTemplate) {
        this.responseTemplate = responseTemplate;
    }

    public void setCloseManully(boolean close) {

        this.close = close;

    }

    static class HttpServiceHandler implements HttpRequestHandler {

        BasicHttpServer server;

        public HttpServiceHandler(BasicHttpServer server) {
            this.server = server;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.apache.hc.core5.http.io.HttpRequestHandler#handle(org.apache.hc.core5.http.ClassicHttpRequest, org.apache.hc.core5.http.ClassicHttpResponse,
         * org.apache.hc.core5.http.protocol.HttpContext)
         */
        public void handle(final ClassicHttpRequest request, final ClassicHttpResponse response,
                final HttpContext context) throws HttpException, IOException {

            server.setMethod(request.getMethod().toUpperCase(Locale.ENGLISH));
            RequestLine requestLine = new RequestLine(request);
            try {
                server.setUri(requestLine.getUri());
            } catch (final Exception ex) {
                throw new HttpException("setUri() failed in BasicHttpServerImpl.handle(): " + ex.getMessage());
            }

            // process HTTP Headers
            for (Header header : request.getHeaders()) {
                server.getHeaders().put(header.getName(), header.getValue());
            }

	    /*
	     * In HttpClient 5.x one can enclose a request entity with any HTTP method 
	     * even if violates semantic of the method. See: 
	     * https://hc.apache.org/httpcomponents-client-5.3.x/migration-guide/migration-to-classic.html
	     */
	    final HttpEntity incomingEntity = request.getEntity();
	    if (incomingEntity != null) {
                final byte[] entityContent = EntityUtils.toByteArray(incomingEntity);
                server.setContent(entityContent);
            } else {
                BasicHttpRequest bhr = (BasicHttpRequest) request;
                server.setContent(requestLine.getUri().getBytes());
	    }	    

            // Handle response based on "responseTemplate"
            HttpEntity body = null;
            if (server.getResponseTemplate() == null
                    || server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_OK_XML)) {
                response.setCode(HttpStatus.SC_OK);

                body = HttpEntities.create(outStream -> outStream.write(("<Response>ok<Response>").getBytes(StandardCharsets.UTF_8)), ContentType.TEXT_HTML.withCharset(StandardCharsets.UTF_8));

            } else if (server.getResponseTemplate().equals(
                    BasicHttpServer.RESPONSE_HTTP_OK_LOOP_BACK)) {
                response.setCode(HttpStatus.SC_OK);
                body = HttpEntities.create(outStream -> outStream.write(server.getContent()), ContentType.TEXT_HTML.withCharset(StandardCharsets.UTF_8));

            } else if (server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_404)) {
                response.setCode(HttpStatus.SC_NOT_FOUND);
                body = HttpEntities.create(outStream -> outStream.write(("<html><body><h1> not found - 404 </h1></body></html>").getBytes(StandardCharsets.UTF_8)), ContentType.TEXT_HTML.withCharset(StandardCharsets.UTF_8));
                
            } else if (server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_200)) {
                response.setCode(HttpStatus.SC_OK);
                body = HttpEntities.create(outStream -> outStream.write(("<Response> SC_ACCEPTED 202 <Response>").getBytes(StandardCharsets.UTF_8)), ContentType.TEXT_HTML.withCharset(StandardCharsets.UTF_8));

            } else if (server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_201)) {
                response.setCode(HttpStatus.SC_CREATED);
                
            } else if (server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_202)) {
                response.setCode(HttpStatus.SC_ACCEPTED);
                
            } else if (server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_400)) {
                response.setCode(HttpStatus.SC_BAD_REQUEST);
                
            } else if (server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_500)) {
                response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                body = HttpEntities.create(outStream -> outStream.write((" Server Error").getBytes(StandardCharsets.UTF_8)), ContentType.TEXT_HTML.withCharset(StandardCharsets.UTF_8));
                
            } else if (server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_COOKIE)) {
                response.setCode(HttpStatus.SC_OK);
                response.addHeader(HTTPConstants.HEADER_SET_COOKIE, "JSESSIONID=abcde12345; Path=/; HttpOnly");
                body = HttpEntities.create(outStream -> outStream.write(("<Response>Cookie should be set<Response>").getBytes(StandardCharsets.UTF_8)), ContentType.TEXT_HTML.withCharset(StandardCharsets.UTF_8));
            }

	    if (body != null) {
                response.setEntity(body);
	    }

        }

    }

    static class RequestListenerThread extends Thread {

        private final ServerSocket serversocket;
        private final HttpService httpService;
        private final SocketConfig socketConfig;
	private final ExceptionListener exceptionListener;
	private final Http1StreamListener streamListener;

        private final DefaultBHttpServerConnectionFactory connectionFactory;

        /**
         * Instantiates a new request listener thread.
         * 
         * @param port
         *            the port
         * @param server
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public RequestListenerThread(BasicHttpServer server) throws IOException {
            this.serversocket = new ServerSocket(0);

            this.socketConfig = SocketConfig.custom()
                 .setSoTimeout(5000, TimeUnit.MILLISECONDS)
                 .setTcpNoDelay(true)
                 .setSndBufSize(8 * 1024)
                 .setRcvBufSize(8 * 1024)
                 .build();

	    this.exceptionListener = (new ExceptionListener() {

                    @Override
                    public void onError(final Exception ex) {
                        if (ex instanceof SocketException) {
                            System.out.println("BasicHttpServerImpl socket error: " + Thread.currentThread() + " " + ex.getMessage());
                        } else {
                            System.out.println("BasicHttpServerImpl error: " + Thread.currentThread() + " " + ex.getMessage());
                            ex.printStackTrace(System.out);
                        }
                    }

		    @Override
                    public void onError(final HttpConnection connection, final Exception ex) {
                        if (ex instanceof SocketTimeoutException) {
                            System.out.println("BasicHttpServerImp SocketTimeoutException: " + Thread.currentThread() + " time out");
                        } else if (ex instanceof SocketException || ex instanceof ConnectionClosedException) {
                            System.out.println("BasicHttpServerImpl SocketException: " + Thread.currentThread() + " " + ex.getMessage());
                        } else {
                            System.out.println("BasicHttpServerImpl: " + Thread.currentThread() + " " + ex.getMessage());
                            ex.printStackTrace(System.out);
                        }
                    }

                });

                this.streamListener = (new Http1StreamListener() {

                    @Override
                    public void onRequestHead(final HttpConnection connection, final HttpRequest request) {
                        System.out.println(connection.getRemoteAddress() + " " + new RequestLine(request));

                    }

                    @Override
                    public void onResponseHead(final HttpConnection connection, final HttpResponse response) {
                        System.out.println(connection.getRemoteAddress() + " " + new StatusLine(response));
                    }

                    @Override
                    public void onExchangeComplete(final HttpConnection connection, final boolean keepAlive) {
                        if (keepAlive) {
                            System.out.println(connection.getRemoteAddress() + " exchange completed (connection kept alive)");
                        } else {
                            System.out.println(connection.getRemoteAddress() + " exchange completed (connection closed)");
                        }
                    }

                });

            final HttpProcessorBuilder b = HttpProcessorBuilder.create();	
            b.addAll(
                new ResponseDate(),
                new ResponseServer("HttpComponents/1.1"),
                new ResponseContent(),
                new ResponseConnControl());
            HttpProcessor httpproc = b.build();

            // Set up request handlers
	    RequestHandlerRegistry<HttpRequestHandler> registry = new RequestHandlerRegistry<>();
            registry.register(null, "*", new HttpServiceHandler(server));

            // Create HTTP/1.1 protocol configuration
            Http1Config h1Config = Http1Config.custom()
                .setMaxHeaderCount(500)
                .setMaxLineLength(8000)
                .setMaxEmptyLineCount(4)
                .build();


	    this.connectionFactory = new DefaultBHttpServerConnectionFactory(URIScheme.HTTP.id, h1Config, CharCodingConfig.DEFAULT);

            // Set up the HTTP service
            this.httpService = new HttpService(httpproc, new BasicHttpServerExpectationDecorator(new BasicHttpServerRequestHandler(registry, DefaultClassicHttpResponseFactory.INSTANCE)), h1Config, DefaultConnectionReuseStrategy.INSTANCE, this.streamListener);

        }

        /**
         * Gets the serversocket.
         * 
         * @return the serversocket
         */
        public ServerSocket getServersocket() {
            return serversocket;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = this.serversocket.accept();
                    socket.setSoTimeout(this.socketConfig.getSoTimeout().toMillisecondsIntBound());
                    socket.setKeepAlive(this.socketConfig.isSoKeepAlive());
                    socket.setTcpNoDelay(this.socketConfig.isTcpNoDelay());
                    if (this.socketConfig.getRcvBufSize() > 0) {
                        socket.setReceiveBufferSize(this.socketConfig.getRcvBufSize());
                    }
                    if (this.socketConfig.getSndBufSize() > 0) {
                        socket.setSendBufferSize(this.socketConfig.getSndBufSize());
                    }
                    if (this.socketConfig.getSoLinger().toSeconds() >= 0) {
                        socket.setSoLinger(true, this.socketConfig.getSoLinger().toSecondsIntBound());
                    }
                    final DefaultBHttpServerConnection conn = this.connectionFactory.createConnection(socket);
                    conn.bind(socket);

                    // Start worker thread
                    Thread t = new WorkerThread(this.httpService, conn, this.exceptionListener);
                    t.setDaemon(false);
                    t.start();
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    System.err.println("I/O error in connection thread: "
                            + e.getMessage() + " , at time: " +LocalDateTime.now());
                    break;
                }
            }
        }
    }


   /**
    * @see https://github.com/apache/httpcomponents-core/blob/master/httpcore5/src/main/java/org/apache/hc/core5/http/impl/bootstrap/Worker.java
    */
    static class WorkerThread extends Thread {

        private final HttpService httpservice;
        private final HttpServerConnection conn;
	private final ExceptionListener exceptionListener;

        /**
         * Instantiates a new worker thread.
         * 
         * @param httpservice
         *            the httpservice
         * @param conn
         *            the conn
         * @param exceptionListener
         *            the exceptionListener
         */
        public WorkerThread(final HttpService httpservice, final HttpServerConnection conn, ExceptionListener exceptionListener) {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
	    this.exceptionListener = exceptionListener;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        public void run() {
            try {
                final BasicHttpContext localContext = new BasicHttpContext();
                final HttpCoreContext context = HttpCoreContext.adapt(localContext);
                while (!Thread.interrupted() && this.conn.isOpen()) {
                    this.httpservice.handleRequest(this.conn, context);
                    localContext.clear();
                }
                this.conn.close();
            } catch (final Exception ex) {
                this.exceptionListener.onError(this.conn, ex);
            } finally {
                this.conn.close(CloseMode.IMMEDIATE);
            }
        }

    }

}
