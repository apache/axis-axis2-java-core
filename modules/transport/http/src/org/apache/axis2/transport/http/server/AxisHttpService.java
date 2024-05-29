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

import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.kernel.RequestResponseTransport;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElements;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.ServerSupport;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpResponseFactory;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;

import jakarta.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is an extension of the default HTTP service responsible for
 * maintaining and populating the {@link MessageContext} for incoming Axis
 * requests.
 *
 * @see https://github.com/apache/httpcomponents-core/blob/master/httpcore5/src/main/java/org/apache/hc/core5/http/impl/io/HttpService.java
 */
public class AxisHttpService {

    private static final Log LOG = LogFactory.getLog(AxisHttpService.class);

    private final HttpProcessor httpProcessor;
    private final Http1Config http1Config;
    private final ConnectionReuseStrategy connStrategy;
    private final DefaultClassicHttpResponseFactory responseFactory;
    private final ConfigurationContext configurationContext;
    private final Http1StreamListener streamListener;
    private final Worker worker;

    public AxisHttpService(
            final HttpProcessor httpProcessor,
            final Http1Config http1Config,
            final ConnectionReuseStrategy connStrategy,
	    final Http1StreamListener streamListener,
            final DefaultClassicHttpResponseFactory responseFactory,
            final ConfigurationContext configurationContext,
            final Worker worker) {
        super();
        if (httpProcessor == null) {
            throw new IllegalArgumentException("HTTP processor may not be null");
        }
        if (responseFactory == null) {
            throw new IllegalArgumentException("Response factory may not be null");
        }
        if (worker == null) {
            throw new IllegalArgumentException("Worker may not be null");
        }
        if (configurationContext == null) {
            throw new IllegalArgumentException("Configuration context may not be null");
        }
        this.httpProcessor = httpProcessor;
        this.http1Config = http1Config != null ? http1Config : Http1Config.DEFAULT;
        this.connStrategy = connStrategy != null ? connStrategy : DefaultConnectionReuseStrategy.INSTANCE;
	this.streamListener = streamListener;
        this.responseFactory = responseFactory;
        this.configurationContext = configurationContext;
        this.worker = worker;

    }

    public void handleRequest(final AxisHttpConnection conn, final HttpContext localContext)
            throws IOException, HttpException {

	final AtomicBoolean responseSubmitted = new AtomicBoolean(false);
	final HttpCoreContext context = HttpCoreContext.cast(localContext);

        MessageContext msgContext = configurationContext.createMessageContext();
        msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);

        if (conn != null) {
	    final InetSocketAddress remoteAddress = (InetSocketAddress) conn.getRemoteAddress();
	    String remoteIPAddress = remoteAddress.getAddress().getHostAddress();
	    final InetSocketAddress localAddress = (InetSocketAddress) conn.getLocalAddress();
	    String localIPAddress = localAddress.getAddress().getHostAddress();
            msgContext.setProperty(MessageContext.REMOTE_ADDR,
                                   remoteIPAddress);
            msgContext.setProperty(MessageContext.TRANSPORT_ADDR,
                                   localIPAddress);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Remote address of the connection : " +
                          remoteIPAddress);
            }
        }

        ClassicHttpResponse response = null;
        ClassicHttpRequest request = null;
        try {
            request = conn.receiveRequest();
	    if (request == null) {
                LOG.error("AxisHttpService.handleRequest() returning on null request, will close the connection");
                conn.close();
                return;
            }
            if (streamListener != null) {
                streamListener.onRequestHead(conn, request);
            }
            RequestLine requestLine = new RequestLine(request);
            if (requestLine != null) {
                msgContext.setProperty(HTTPConstants.HTTP_METHOD, requestLine.getMethod());
            }
	    ProtocolVersion transportVersion = request.getVersion();
            if (transportVersion != null && transportVersion.greaterEquals(HttpVersion.HTTP_2)) {
                // Downgrade protocol version if greater than HTTP/1.1 
                transportVersion = HttpVersion.HTTP_1_1;
                LOG.warn("http2 or greater detected, the request has been downgraded to HTTP/1.1");
            }

            context.setProtocolVersion(transportVersion != null ? transportVersion : this.http1Config.getVersion());
	    context.setRequest(request);
            response = this.responseFactory.newHttpResponse
                    (HttpStatus.SC_OK);

	    final HttpClientContext clientContext = HttpClientContext.adapt(context);
            if (clientContext.getRequestConfig().isExpectContinueEnabled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("isExpectContinueEnabled is true");
                }
                ClassicHttpResponse ack = this.responseFactory.newHttpResponse
                        (HttpStatus.SC_CONTINUE);
                conn.sendResponse(ack);
                conn.flush();
            }

            // Create Axis request and response objects
            AxisHttpRequestImpl axisreq = new AxisHttpRequestImpl(
                    conn,
                    request,
                    this.httpProcessor,
                    context);
            AxisHttpResponseImpl axisres = new AxisHttpResponseImpl(
                    conn,
                    response,
                    this.httpProcessor,
                    context);

            // Prepare HTTP request
            axisreq.prepare();

            // Run the service
            doService(axisreq, axisres, context, msgContext);

            // Make sure the request content is fully consumed
            InputStream instream = conn.getInputStream();
            if (instream != null) {
                instream.close();
            }

            // Commit response if not committed
            if (!axisres.isCommitted()) {
                axisres.commit();
            }

            // Make sure the response content is properly terminated
            OutputStream outstream = conn.getOutputStream();
            if (outstream != null) {
                outstream.close();
            }

        } catch (HttpException ex) {
            if (responseSubmitted.get()) {
                throw ex;
            }
            try (final ClassicHttpResponse errorResponse = new BasicClassicHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR)) {
                handleException(ex, errorResponse);
                errorResponse.setHeader(HttpHeaders.CONNECTION, HeaderElements.CLOSE);
                context.setResponse(errorResponse);
                this.httpProcessor.process(errorResponse, errorResponse.getEntity(), context);

                conn.sendResponse(errorResponse);
                if (streamListener != null) {
                    streamListener.onResponseHead(conn, errorResponse);
                }
                conn.close();
	    }
        }

        conn.flush();
	if (request != null && response != null) {
            final boolean keepAlive = this.connStrategy.keepAlive(request, response, localContext);
            if (!keepAlive) {
                conn.close();
            } else {
                conn.reset();
            }
	    // AXIS2-6051, not sure if this is required though the core5 code HttpService does this
	    response.close();
	}	
    }

    protected void handleException(final HttpException ex, final ClassicHttpResponse response) {
        response.setCode(toStatusCode(ex));
        response.setEntity(new StringEntity(ServerSupport.toErrorMessage(ex), ContentType.TEXT_PLAIN));
    }

    protected int toStatusCode(final Exception ex) {
        return ServerSupport.toStatusCode(ex);
    }

    protected void doService(
            final AxisHttpRequest request,
            final AxisHttpResponse response,
            final HttpContext context,
            final MessageContext msgContext) throws HttpException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Request method: " + request.getMethod());
            LOG.debug("Target URI: " + request.getRequestURI());
        }

        try {
            TransportOutDescription transportOut = this.configurationContext.getAxisConfiguration()
                    .getTransportOut(Constants.TRANSPORT_HTTP);
            TransportInDescription transportIn = this.configurationContext.getAxisConfiguration()
                    .getTransportIn(Constants.TRANSPORT_HTTP);

            String sessionKey = (String) context.getAttribute(HTTPConstants.COOKIE_STRING);
            msgContext.setTransportIn(transportIn);
            msgContext.setTransportOut(transportOut);
            msgContext.setServerSide(true);
            msgContext.setProperty(HTTPConstants.COOKIE_STRING, sessionKey);
            msgContext.setProperty(Constants.Configuration.TRANSPORT_IN_URL,
                                   request.getRequestURI());

            // set the transport Headers
            HashMap headerMap = new HashMap();
            for (Iterator it = request.headerIterator(); it.hasNext();) {
                Header header = (Header) it.next();
                headerMap.put(header.getName(), header.getValue());
            }
            msgContext.setProperty(MessageContext.TRANSPORT_HEADERS,
                                   headerMap);
            msgContext.setProperty(Constants.Configuration.CONTENT_TYPE,
                                   request.getContentType());

            msgContext.setProperty(MessageContext.TRANSPORT_OUT,
                                   response.getOutputStream());
            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                                   response);
            msgContext.setTo(new EndpointReference(request.getRequestURI()));
            msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL,
                                   new SimpleHTTPRequestResponseTransport());


            this.worker.service(request, response, msgContext);
        } catch (SocketException ex) {
            // Socket is unreliable. 
            throw ex;
        } catch (HttpException ex) {
            // HTTP protocol violation. Transport is unreliable
            throw ex;
        } catch (Throwable e) {
            LOG.debug("Processing exception", e);

            msgContext.setProperty(MessageContext.TRANSPORT_OUT,
                                   response.getOutputStream());
            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                                   response);

            MessageContext faultContext =
                    MessageContextBuilder.createFaultMessageContext(msgContext, e);
            // If the fault is not going along the back channel we should be 202ing
            if (AddressingHelper.isFaultRedirected(msgContext)) {
                response.setStatus(HttpStatus.SC_ACCEPTED);
            } else {
                String state = (String) msgContext.getProperty(Constants.HTTP_RESPONSE_STATE);
                if (state != null) {
                    int stateInt = Integer.parseInt(state);
                    response.setStatus(stateInt);
                    if (stateInt == HttpServletResponse.SC_UNAUTHORIZED) { // Unauthorized
                        String realm =
                                (String) msgContext.getProperty(Constants.HTTP_BASIC_AUTH_REALM);
                        response.addHeader("WWW-Authenticate",
                                           "basic realm=\"" + realm + "\"");
                    }
                } else {
                    if (e instanceof AxisFault) {
                        response.sendError(getStatusFromAxisFault((AxisFault)e), e.getMessage());
                    } else {
                        response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                                           "Internal server error");
                    }
                }
            }
            AxisEngine.sendFault(faultContext);
        }
    }

    public int getStatusFromAxisFault(AxisFault fault) {
        QName faultCode = fault.getFaultCode();
        if (SOAP12Constants.QNAME_SENDER_FAULTCODE.equals(faultCode) ||
                SOAP11Constants.QNAME_SENDER_FAULTCODE.equals(faultCode)) {
            return HttpServletResponse.SC_BAD_REQUEST;
        }

        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    class SimpleHTTPRequestResponseTransport implements RequestResponseTransport {

        private CountDownLatch responseReadySignal = new CountDownLatch(1);
        RequestResponseTransportStatus status = RequestResponseTransportStatus.WAITING;
        AxisFault faultToBeThrownOut = null;
        private boolean responseWritten = false;

        public void acknowledgeMessage(MessageContext msgContext) throws AxisFault {
            //TODO: Once the core HTTP API allows us to return an ack before unwinding, then the should be fixed
            status = RequestResponseTransportStatus.ACKED;
            responseReadySignal.countDown();
        }

        public void awaitResponse() throws InterruptedException, AxisFault {
            responseReadySignal.await();

            if (faultToBeThrownOut != null) {
                throw faultToBeThrownOut;
            }
        }

        public void signalResponseReady() {
            status = RequestResponseTransportStatus.SIGNALLED;
            responseReadySignal.countDown();
        }

        public RequestResponseTransportStatus getStatus() {
            return status;
        }

        public void signalFaultReady(AxisFault fault) {
            faultToBeThrownOut = fault;
            signalResponseReady();
        }
        
        public boolean isResponseWritten() {
            return responseWritten;
        }

        public void setResponseWritten(boolean responseWritten) {
            this.responseWritten = responseWritten;
        }

    }

}
