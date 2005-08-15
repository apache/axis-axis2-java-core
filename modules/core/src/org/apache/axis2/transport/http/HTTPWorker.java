/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
 *
 *  Runtime state of the engine
 */
package org.apache.axis2.transport.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.http.server.HttpRequestHandler;
import org.apache.axis2.transport.http.server.SimpleHttpServerConnection;
import org.apache.axis2.transport.http.server.SimpleRequest;
import org.apache.axis2.transport.http.server.SimpleResponse;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.threadpool.AxisWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.Header;

import javax.xml.namespace.QName;
import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

public class HTTPWorker implements HttpRequestHandler {
    protected Log log = LogFactory.getLog(getClass().getName());
    private ConfigurationContext configurationContext;

    public HTTPWorker(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public boolean processRequest(final SimpleHttpServerConnection conn, final SimpleRequest request) throws IOException {
        MessageContext msgContext = null;
        SimpleResponse response = new SimpleResponse();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            if (configurationContext == null) {
                throw new AxisFault(Messages.getMessage("cannotBeNullConfigurationContext"));
            }

            InputStream inStream = request.getBody();

            TransportOutDescription transportOut =
                    configurationContext.getAxisConfiguration().getTransportOut(
                            new QName(Constants.TRANSPORT_HTTP));
            msgContext =
                    new MessageContext(
                            configurationContext,
                            configurationContext.getAxisConfiguration().getTransportIn(
                                    new QName(Constants.TRANSPORT_HTTP)),
                            transportOut);
            msgContext.setServerSide(true);

            HttpVersion ver = request.getRequestLine().getHttpVersion();
            if (ver == null) {
                throw new AxisFault("HTTP version can not be Null");
            }
            String httpVersion = null;
            if (HttpVersion.HTTP_1_0.equals(ver)) {
                httpVersion = HTTPConstants.HEADER_PROTOCOL_10;
            } else if (HttpVersion.HTTP_1_1.equals(ver)) {
                httpVersion = HTTPConstants.HEADER_PROTOCOL_11;
                response.setHeader(new Header(HTTPConstants.HEADER_TRANSFER_ENCODING, HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED));
            } else {
                throw new AxisFault("Unknown supported protocol version " + ver);
            }


            msgContext.setProperty(MessageContext.TRANSPORT_OUT, baos);

            //set the transport Headers
            msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, getHeaders(request));

            //This is way to provide Accsess to the transport information to the transport Sender
            msgContext.setProperty(
                    HTTPConstants.HTTPOutTransportInfo,
                    new SimpleHTTPOutTransportInfo(response));

            String soapAction = null;
            if (request.getFirstHeader(HTTPConstants.HEADER_SOAP_ACTION) != null) {
                soapAction = request.getFirstHeader(HTTPConstants.HEADER_SOAP_ACTION).getValue();
            }
            if (HTTPConstants.HEADER_GET.equals(request.getRequestLine().getMethod())) {
                //It is GET handle the Get request
                boolean processed =
                        HTTPTransportUtils.processHTTPGetRequest(
                                msgContext,
                                inStream,
                                baos,
                                request.getContentType(),
                                soapAction,
                                request.getRequestLine().getUri(),
                                configurationContext,
                                HTTPTransportReceiver.getGetRequestParameters(
                                        request.getRequestLine().getUri()));
                if (!processed) {
                    response.setStatusLine(request.getRequestLine().getHttpVersion(), 200, "OK");
                    response.setBodyString(HTTPTransportReceiver.getServicesHTML(configurationContext));
                    setResponseHeaders(conn, request, response);
                    conn.writeResponse(response);
                    return true;
                }
            } else {
                //It is POST, handle it
                HTTPTransportUtils.processHTTPPostRequest(
                        msgContext,
                        inStream,
                        baos,
                        request.getContentType(),
                        soapAction,
                        request.getRequestLine().getUri(),
                        configurationContext);
            }
            response.setStatusLine(request.getRequestLine().getHttpVersion(), 200, "OK");
            response.setBody(new ByteArrayInputStream(baos.toByteArray()));
            setResponseHeaders(conn, request, response);
            conn.writeResponse(response);
        } catch (Throwable e) {
            try {
                AxisEngine engine = new AxisEngine(configurationContext);
                if (msgContext != null) {
                    msgContext.setProperty(MessageContext.TRANSPORT_OUT, baos);
                    MessageContext faultContext = engine.createFaultMessageContext(msgContext, e);
                    response.setStatusLine(request.getRequestLine().getHttpVersion(), 500, "Internal server error");
                    engine.sendFault(faultContext);
                    response.setBody(new ByteArrayInputStream(baos.toByteArray()));
                    setResponseHeaders(conn, request, response);
                    conn.writeResponse(response);
                } else {
                    log.error(e, e);
                }
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
            }
            log.error(e.getMessage(), e);
        }
        return true;
    }

    private void setResponseHeaders(final SimpleHttpServerConnection conn, SimpleRequest request, SimpleResponse response) {
        if (!response.containsHeader("Connection")) {
            // See if the the client explicitly handles connection persistence
            Header connheader = request.getFirstHeader("Connection");
            if (connheader != null) {
                if (connheader.getValue().equalsIgnoreCase("keep-alive")) {
                    Header header = new Header("Connection", "keep-alive"); 
                    response.addHeader(header);
                    conn.setKeepAlive(true);
                }
                if (connheader.getValue().equalsIgnoreCase("close")) {
                    Header header = new Header("Connection", "close"); 
                    response.addHeader(header);
                    conn.setKeepAlive(false);
                }
            } else {
                // Use protocol default connection policy
                if (response.getHttpVersion().greaterEquals(HttpVersion.HTTP_1_1)) {
                    conn.setKeepAlive(true);
                } else {
                    conn.setKeepAlive(false);
                }
            }
        }
        System.out.println("HTTPWorker.isKeepAlive : " + conn.isKeepAlive());
    }

    private Map getHeaders(SimpleRequest request) {
        HashMap headerMap = new HashMap();
        Header[] headers = request.getHeaders();
        for (int i = 0; i < headers.length; i++) {
            headerMap.put(headers[i].getName(), headers[i].getValue());
        }
        return headerMap;
    }
}
