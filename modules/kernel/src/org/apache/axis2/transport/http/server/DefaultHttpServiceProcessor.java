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

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis2.Constants;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.UnsupportedHttpVersionException;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

public class DefaultHttpServiceProcessor extends HttpServiceProcessor {

    private static final Log LOG = LogFactory.getLog(DefaultHttpServiceProcessor.class);
    private static final Log HEADERLOG = LogFactory.getLog("org.apache.axis2.transport.http.server.wire");

    private final ConfigurationContext configurationContext;
    private final SessionManager sessionManager;
    private final Worker worker;
    private final IOProcessorCallback callback;
    private HttpServerConnection conn;

    private HttpContext httpcontext = null;

    public DefaultHttpServiceProcessor(
            final HttpServerConnection conn,
            final ConfigurationContext configurationContext,
            final SessionManager sessionManager,
            final Worker worker,
            final IOProcessorCallback callback) {
        super(conn);
        this.conn = conn;
        if (worker == null) {
            throw new IllegalArgumentException("Worker may not be null");
        }
        if (configurationContext == null) {
            throw new IllegalArgumentException("Configuration context may not be null");
        }
        if (sessionManager == null) {
            throw new IllegalArgumentException("Session manager may not be null");
        }
        this.configurationContext = configurationContext;
        this.sessionManager = sessionManager;
        this.worker = worker;
        this.callback = callback;

        // Add required protocol interceptors
        addInterceptor(new RequestSessionCookie());
        addInterceptor(new ResponseDate());
        addInterceptor(new ResponseServer());
        addInterceptor(new ResponseContent());
        addInterceptor(new ResponseConnControl());
        addInterceptor(new ResponseSessionCookie());
    }

    protected void postprocessResponse(final HttpResponse response, final HttpContext context)
            throws IOException, HttpException {
        super.postprocessResponse(response, context);
        if (HEADERLOG.isDebugEnabled()) {
            HEADERLOG.debug("<< " + response.getStatusLine().toString());
            Header[] headers = response.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                HEADERLOG.debug("<< " + headers[i].toString());
            }
        }
    }

    protected void preprocessRequest(final HttpRequest request, final HttpContext context)
            throws IOException, HttpException {
        // As of next version of HttpCore the HTTP execution context can be retrieved 
        // by calling #getContext()
        this.httpcontext = context;
        super.preprocessRequest(request, context);
        if (HEADERLOG.isDebugEnabled()) {
            HEADERLOG.debug(">> " + request.getRequestLine().toString());
            Header[] headers = request.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                HEADERLOG.debug(">> " + headers[i].toString());
            }
        }
    }

    protected void doService(final HttpRequest request, final HttpResponse response)
            throws HttpException, IOException {
        RequestLine reqline = request.getRequestLine();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Request method: " + reqline.getMethod());
            LOG.debug("Target URI: " + reqline.getUri());
        }

        HttpVersion ver = reqline.getHttpVersion();
        if (!ver.lessEquals(HttpVersion.HTTP_1_1)) {
            throw new UnsupportedHttpVersionException("Unsupported HTTP version: " + ver);
        }

        MessageContext msgContext = new MessageContext();
        msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);

        if (conn instanceof DefaultHttpConnectionFactory.Axis2HttpServerConnection) {
            DefaultHttpConnectionFactory.Axis2HttpServerConnection axis2Con =
                (DefaultHttpConnectionFactory.Axis2HttpServerConnection) conn;
            msgContext.setProperty(MessageContext.REMOTE_ADDR, axis2Con.getRemoteIPAddress());
            LOG.debug("Remote address of the connection : " + axis2Con.getRemoteIPAddress());
        }

        try {
            TransportOutDescription transportOut = this.configurationContext.getAxisConfiguration()
                    .getTransportOut(new QName(Constants.TRANSPORT_HTTP));
            TransportInDescription transportIn = this.configurationContext.getAxisConfiguration()
                    .getTransportIn(new QName(Constants.TRANSPORT_HTTP));

            msgContext.setConfigurationContext(this.configurationContext);

            String sessionKey = (String) this.httpcontext.getAttribute(HTTPConstants.COOKIE_STRING);
            if (this.configurationContext.getAxisConfiguration().isManageTransportSession()) {
                SessionContext sessionContext = this.sessionManager.getSessionContext(sessionKey);
                msgContext.setSessionContext(sessionContext);
            }
            msgContext.setTransportIn(transportIn);
            msgContext.setTransportOut(transportOut);
            msgContext.setServiceGroupContextId(UUIDGenerator.getUUID());
            msgContext.setServerSide(true);
            msgContext.setProperty(Constants.Configuration.TRANSPORT_IN_URL, reqline.getUri());

            // set the transport Headers
            HashMap headerMap = new HashMap();
            for (Iterator it = request.headerIterator(); it.hasNext();) {
                Header header = (Header) it.next();
                headerMap.put(header.getName(), header.getValue());
            }
            msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);

            this.httpcontext.setAttribute(AxisParams.MESSAGE_CONTEXT, msgContext);

            this.worker.service(request, response, msgContext);
        } catch (SocketException ex) {
            // Socket is unreliable. 
            throw ex;
        } catch (HttpException ex) {
            // HTTP protocol violation. Transport is unrelaible
            throw ex;
        } catch (Throwable e) {
            try {
                AxisEngine engine = new AxisEngine(this.configurationContext);

                OutputBuffer outbuffer = new OutputBuffer();
                msgContext.setProperty(MessageContext.TRANSPORT_OUT, outbuffer.getOutputStream());
                msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, outbuffer);

                MessageContext faultContext = engine.createFaultMessageContext(msgContext, e);
                // If the fault is not going along the back channel we should be 202ing
                if (AddressingHelper.isFaultRedirected(msgContext)) {
                    response.setStatusLine(new StatusLine(ver, 202, "Accepted"));
                } else {
                    response.setStatusLine(new StatusLine(ver, 500, "Internal server error"));
                }
                engine.sendFault(faultContext);
                response.setEntity(outbuffer);
            } catch (Exception ex) {
                if (AddressingHelper.isFaultRedirected(msgContext)) {
                    response.setStatusLine(new StatusLine(ver, 202, "Accepted"));
                } else {
                    response.setStatusLine(new StatusLine(ver, 500, "Internal server error"));
                    String msg = ex.getMessage();
                    StringEntity entity;
                    if (msg != null && msg.trim().length() != 0) {
                        entity = new StringEntity(msg);
                    } else {
                        entity = new StringEntity("Exception message unknown");
                    }
                    entity.setContentType("text/plain");
                    response.setEntity(entity);
                }
            }
        }

    }

    protected void logIOException(final IOException ex) {
        if (ex instanceof SocketTimeoutException) {
            LOG.debug(ex.getMessage());
        } else if (ex instanceof SocketException) {
            LOG.debug(ex.getMessage());
        } else {
            LOG.warn(ex.getMessage(), ex);
        }
    }

    protected void logMessage(final String s) {
        LOG.debug(s);
    }

    protected void logProtocolException(final HttpException ex) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("HTTP protocol error: " + ex.getMessage());
        }
    }

    public void close() throws IOException {
        closeConnection();
    }

    public void run() {
        LOG.debug("New connection thread");
        try {
            while (!Thread.interrupted() && !isDestroyed() && isActive()) {
                handleRequest();
            }
        } finally {
            destroy();
            if (this.callback != null) {
                this.callback.completed(this);
            }
        }
    }

}
