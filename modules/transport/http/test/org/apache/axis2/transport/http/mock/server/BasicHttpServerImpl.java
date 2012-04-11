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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.util.EntityUtils;

/**
 * The purpose of this server application is facilitate to HTTP related test
 * cases as a back end server based on httpcore. Original code copied from
 * ElementalHttpServer class from httpcore component of Apache HTTPComponents
 * project.
 * 
 * @see http://svn.apache.org/repos/asf/httpcomponents/httpcore/trunk/httpcore/src
 *      /examples/org/apache/http/examples/ElementalHttpServer.java
 * @since 1.7.0
 * 
 */
public class BasicHttpServerImpl implements BasicHttpServer {

    private RequestListenerThread serverThread;
    private Map<String, String> headers;
    private byte[] content;
    private String method;
    private String url;
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

    public String getUrl() {
        return url;
    }

    public void setHeaders(Map<String, String> headers) {
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setUrl(String url) {
        this.url = url;
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
         * org.apache.http.protocol.HttpRequestHandler#handle(org.apache.http
         * .HttpRequest, org.apache.http.HttpResponse,
         * org.apache.http.protocol.HttpContext)
         */
        public void handle(final HttpRequest request, final HttpResponse response,
                final HttpContext context) throws HttpException, IOException {

            server.setMethod(request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH));
            server.setUrl(request.getRequestLine().getUri());

            // process HTTP Headers
            for (Header header : request.getAllHeaders()) {
                server.getHeaders().put(header.getName(), header.getValue());
            }

            // TODO implement processing for other Entity types
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                byte[] entityContent = EntityUtils.toByteArray(entity);
                server.setContent(entityContent);
            } else if (request instanceof BasicHttpRequest) {
                BasicHttpRequest bhr = (BasicHttpRequest) request;
                server.setContent(bhr.getRequestLine().getUri().getBytes());
            }

            // Handle response based on "responseTemplate"
            EntityTemplate body = null;
            if (server.getResponseTemplate() == null
                    || server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_OK_XML)) {
                response.setStatusCode(HttpStatus.SC_OK);
                body = new EntityTemplate(new ContentProducer() {
                    public void writeTo(final OutputStream outstream) throws IOException {
                        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                        writer.write("<Response>ok<Response>");
                        writer.flush();
                    }
                });

                response.setEntity(body);

            } else if (server.getResponseTemplate().equals(
                    BasicHttpServer.RESPONSE_HTTP_OK_LOOP_BACK)) {
                response.setStatusCode(HttpStatus.SC_OK);
                body = new EntityTemplate(new ContentProducer() {
                    public void writeTo(final OutputStream outstream) throws IOException {
                        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                        writer.write(new String(server.getContent()));
                        writer.flush();
                    }
                });
            } else if (server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_404)) {
                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                body = new EntityTemplate(new ContentProducer() {

                    public void writeTo(final OutputStream outstream) throws IOException {
                        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                        writer.write("<html><body><h1>");
                        writer.write(" not found - 404");
                        writer.write("</h1></body></html>");
                        writer.flush();
                    }

                });
                
            } else if (server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_200)) {
                response.setStatusCode(HttpStatus.SC_OK);
                body = new EntityTemplate(new ContentProducer() {

                    public void writeTo(final OutputStream outstream) throws IOException {
                        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                        writer.write("<Response> SC_ACCEPTED 202 <Response>");
                        writer.flush();
                    }

                });
                
            } else if (server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_201)) {
                response.setStatusCode(HttpStatus.SC_CREATED);
                body = new EntityTemplate(new ContentProducer() {

                    public void writeTo(final OutputStream outstream) throws IOException {
                        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                        //writer.write("<Response> SC_ACCEPTED 202 <Response>");
                        writer.flush();
                    }

                });
                
            } else if (server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_202)) {
                response.setStatusCode(HttpStatus.SC_ACCEPTED);
                body = new EntityTemplate(new ContentProducer() {
                    public void writeTo(final OutputStream outstream) throws IOException {
                        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                        //writer.write("<Response> SC_ACCEPTED 202 <Response>");
                        writer.flush();
                    }

                });
                
            } else if (server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_400)) {
                response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                body = new EntityTemplate(new ContentProducer() {
                    public void writeTo(final OutputStream outstream) throws IOException {
                        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                        //writer.write("<Response> SC_ACCEPTED 202 <Response>");
                        writer.flush();
                    }

                });
                
            } else if (server.getResponseTemplate().equals(BasicHttpServer.RESPONSE_HTTP_500)) {
                response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                body = new EntityTemplate(new ContentProducer() {
                    public void writeTo(final OutputStream outstream) throws IOException {
                        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                        writer.write(" Server Error");
                        writer.flush();
                    }

                });
                
            }            
            
            // TODO - customize to send content type depend on expectations.
            body.setContentType("text/html; charset=UTF-8");
            response.setEntity(body);
        }

    }

    static class RequestListenerThread extends Thread {

        private final ServerSocket serversocket;
        private final HttpParams params;
        private final HttpService httpService;

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
            this.params = new BasicHttpParams();
            // Basic configuration.
            this.params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                    .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                    .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                    .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                    .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

            BasicHttpProcessor httpproc = new BasicHttpProcessor();
            httpproc.addInterceptor(new ResponseDate());
            httpproc.addInterceptor(new ResponseServer());
            httpproc.addInterceptor(new ResponseContent());
            httpproc.addInterceptor(new ResponseConnControl());

            // Set up request handlers
            HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
            reqistry.register("*", new HttpServiceHandler(server));

            // Set up the HTTP service
            this.httpService = new HttpService(httpproc, new DefaultConnectionReuseStrategy(),
                    new DefaultHttpResponseFactory());
            this.httpService.setParams(this.params);
            this.httpService.setHandlerResolver(reqistry);
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
            System.out.println("Listening on port " + this.serversocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = this.serversocket.accept();
                    DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                    System.out.println("Incoming connection from " + socket.getInetAddress());
                    conn.bind(socket, this.params);

                    // Start worker thread
                    Thread t = new WorkerThread(this.httpService, conn);
                    t.setDaemon(false);
                    t.start();
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    System.err.println("I/O error initialising connection thread: "
                            + e.getMessage());
                    break;
                }
            }
        }
    }

    static class WorkerThread extends Thread {

        private final HttpService httpservice;
        private final HttpServerConnection conn;

        /**
         * Instantiates a new worker thread.
         * 
         * @param httpservice
         *            the httpservice
         * @param conn
         *            the conn
         */
        public WorkerThread(final HttpService httpservice, final HttpServerConnection conn) {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Thread#run()
         */
        public void run() {
            System.out.println("New connection thread");
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && this.conn.isOpen()) {
                    this.httpservice.handleRequest(this.conn, context);
                }
            } catch (ConnectionClosedException ex) {
                System.err.println("Client closed connection");
            } catch (IOException ex) {
                System.err.println("I/O error: " + ex.getMessage());
            } catch (HttpException ex) {
                System.err.println("Unrecoverable HTTP protocol violation: " + ex.getMessage());
            } finally {
                try {
                    this.conn.shutdown();
                } catch (IOException ignore) {
                }
            }
        }

    }

}
