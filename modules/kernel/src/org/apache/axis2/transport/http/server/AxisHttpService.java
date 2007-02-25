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

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpInetConnection;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpVersion;
import org.apache.http.RequestLine;
import org.apache.http.UnsupportedHttpVersionException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpService;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class is an extension of the defaulf HTTP service responsible for 
 * maintaining and polulating the {@link MessageContext} for incoming Axis 
 * requests.
 */
public class AxisHttpService extends HttpService {

    private static final Log LOG = LogFactory.getLog(AxisHttpService.class);

    private final MessageContext msgContext;
    private final ConfigurationContext configurationContext;
    private final SessionManager sessionManager;
    private final Worker worker;
  
    public AxisHttpService(
            final HttpProcessor httpProcessor,
            final ConnectionReuseStrategy connStrategy,
            final HttpResponseFactory responseFactory,
            final ConfigurationContext configurationContext,
            final SessionManager sessionManager,
            final Worker worker) {
        super(httpProcessor, connStrategy, responseFactory);
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

        this.msgContext = ContextFactory.createMessageContext(configurationContext);
        this.msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);
    }

    protected void doService(
            final HttpRequest request, 
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {
        RequestLine reqline = request.getRequestLine();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Request method: " + reqline.getMethod());
            LOG.debug("Target URI: " + reqline.getUri());
        }

        HttpVersion ver = reqline.getHttpVersion();
        if (!ver.lessEquals(HttpVersion.HTTP_1_1)) {
            throw new UnsupportedHttpVersionException("Unsupported HTTP version: " + ver);
        }

        try {
            TransportOutDescription transportOut = this.configurationContext.getAxisConfiguration()
                    .getTransportOut(new QName(Constants.TRANSPORT_HTTP));
            TransportInDescription transportIn = this.configurationContext.getAxisConfiguration()
                    .getTransportIn(new QName(Constants.TRANSPORT_HTTP));

            String sessionKey = (String) context.getAttribute(HTTPConstants.COOKIE_STRING);
            this.msgContext.setTransportIn(transportIn);
            this.msgContext.setTransportOut(transportOut);
            this.msgContext.setServerSide(true);
            this.msgContext.setProperty(HTTPConstants.COOKIE_STRING,sessionKey);
            this.msgContext.setProperty(Constants.Configuration.TRANSPORT_IN_URL, reqline.getUri());

            // set the transport Headers
            HashMap headerMap = new HashMap();
            for (Iterator it = request.headerIterator(); it.hasNext();) {
                Header header = (Header) it.next();
                headerMap.put(header.getName(), header.getValue());
            }
            this.msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
            this.worker.service(request, response, this.msgContext);
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
                this.msgContext.setProperty(MessageContext.TRANSPORT_OUT, outbuffer.getOutputStream());
                this.msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, outbuffer);

                MessageContext faultContext = MessageContextBuilder.createFaultMessageContext(msgContext, e);
                // If the fault is not going along the back channel we should be 202ing
                if (AddressingHelper.isFaultRedirected(this.msgContext)) {
                    response.setStatusLine(new BasicStatusLine(ver, 202, "Accepted"));
                } else {
                    response.setStatusLine(new BasicStatusLine(ver, 500, "Internal server error"));
                }
                engine.sendFault(faultContext);
                response.setEntity(outbuffer);
            } catch (Exception ex) {
                if (AddressingHelper.isFaultRedirected(this.msgContext)) {
                    response.setStatusLine(new BasicStatusLine(ver, 202, "Accepted"));
                } else {
                    response.setStatusLine(new BasicStatusLine(ver, 500, "Internal server error"));
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

    public void handleRequest(final HttpServerConnection conn, final HttpContext context) 
            throws IOException, HttpException {
        if (conn instanceof HttpInetConnection) {
            HttpInetConnection inetconn = (HttpInetConnection) conn;
            InetAddress address = inetconn.getRemoteAddress();
            this.msgContext.setProperty(MessageContext.REMOTE_ADDR, address.getHostAddress());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Remote address of the connection : " + address);
            }
        }
        super.handleRequest(conn, context);
    }

}
