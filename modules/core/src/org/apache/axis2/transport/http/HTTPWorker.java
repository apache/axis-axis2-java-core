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
*/


package org.apache.axis2.transport.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.*;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.server.HttpRequestHandler;
import org.apache.axis2.transport.http.server.SimpleHttpServerConnection;
import org.apache.axis2.transport.http.server.SimpleRequest;
import org.apache.axis2.transport.http.server.SimpleResponse;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.*;

public class HTTPWorker implements HttpRequestHandler {

    private static final Log log = LogFactory.getLog(HTTPWorker.class);
    private ConfigurationContext configurationContext;
    // to store session object
    private Hashtable sessionContextTable = new Hashtable();
    private String contextPath = null;
    private String servicePath = null;

    public HTTPWorker(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        contextPath = configurationContext.getContextPath();
        servicePath = configurationContext.getServicePath();
    }

    public boolean processRequest(final SimpleHttpServerConnection conn,
                                  final SimpleRequest request)
            throws IOException {
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
            String cookieID = request.getCookieID();
            SessionContext sessionContext = getSessionContext(cookieID);

            msgContext = new MessageContext();
            msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);
            msgContext.setConfigurationContext(configurationContext);
            msgContext.setSessionContext(sessionContext);
            msgContext.setTransportIn(configurationContext.getAxisConfiguration().getTransportIn(
                    new QName(Constants.TRANSPORT_HTTP)));
            msgContext.setTransportOut(transportOut);
            msgContext.setServerSide(true);

            HttpVersion ver = request.getRequestLine().getHttpVersion();

            if (ver == null) {
                throw new AxisFault("HTTP version can not be Null");
            }


            if (HttpVersion.HTTP_1_0.equals(ver)) {
//                httpVersion = HTTPConstants.HEADER_PROTOCOL_10;
            } else if (HttpVersion.HTTP_1_1.equals(ver)) {
//                httpVersion = HTTPConstants.HEADER_PROTOCOL_11;

                /**
                 * Transport Sender configuration via axis2.xml
                 */
                this.transportOutConfiguration(configurationContext, response);
            } else {
                throw new AxisFault("Unknown supported protocol version " + ver);
            }

            msgContext.setProperty(MessageContext.TRANSPORT_OUT, baos);

            // set the transport Headers
            msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, getHeaders(request));
            msgContext.setServiceGroupContextId(UUIDGenerator.getUUID());

            // This is way to provide access to the transport information to the transport Sender
            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                    new SimpleHTTPOutTransportInfo(response));
            msgContext.setProperty(Constants.Configuration.TRANSPORT_IN_URL, request.getRequestLine().getUri());


            String soapAction = null;

            if (request.getFirstHeader(HTTPConstants.HEADER_SOAP_ACTION) != null) {
                soapAction = request.getFirstHeader(HTTPConstants.HEADER_SOAP_ACTION).getValue();
            }

            if (HTTPConstants.HEADER_GET.equals(request.getRequestLine().getMethod())) {
                String uri = request.getRequestLine().getUri();
                log.debug("HTTP GET:" + uri);
                if (uri.equals("/favicon.ico")) {
                    response.setStatusLine(request.getRequestLine().getHttpVersion(), 301, "Redirect");
                    response.addHeader(new Header("Location", "http://ws.apache.org/favicon.ico"));
                    conn.writeResponse(response);
                    return true;
                }
                if (!uri.startsWith(contextPath)) {
                    response.setStatusLine(request.getRequestLine().getHttpVersion(), 301, "Redirect");
                    response.addHeader(new Header("Location", contextPath));
                    conn.writeResponse(response);
                    return true;
                }

                if (uri.indexOf("?") < 0) {
                    if (!(uri.endsWith(contextPath))) {
                        String serviceName = uri.replaceAll(contextPath, "");
                        if (serviceName.indexOf("/") < 0) {
                            response.addHeader(new Header("Content-Type", "text/html"));
                            String res = HTTPTransportReceiver.printServiceHTML(serviceName, configurationContext);
                            byte[] buf = res.getBytes();
                            response.setBody(new ByteArrayInputStream(buf));
                            conn.writeResponse(response);
                            return true;
                        }
                    }
                }

                if (uri.endsWith("?wsdl")) {
                    String serviceName = uri.substring(uri.lastIndexOf("/") + 1, uri.length() - 5);
                    HashMap services = configurationContext.getAxisConfiguration().getServices();
                    AxisService service = (AxisService) services.get(serviceName);
                    if (service != null) {
                        response.addHeader(new Header("Content-Type", "text/xml"));
//                        String url = conn.getURL(uri.substring(1, uri.length() - 5));
                        String url = conn.getURL("");
                        int ipindex = url.indexOf("//");
                        String ip = null;
                        if (ipindex >= 0) {
                            ip = url.substring(ipindex + 2, url.length());
                            int seperatorIndex = ip.indexOf(":");
                            if (seperatorIndex > 0) {
                                ip = ip.substring(0, seperatorIndex);
                            }
                        }
                        service.printWSDL(baos, ip,servicePath);
                        byte[] buf = baos.toByteArray();
                        response.setBody(new ByteArrayInputStream(buf));
                        conn.writeResponse(response);
                        return true;
                    }
                }
                if (uri.endsWith("?xsd")) {
                    String serviceName = uri.substring(uri.lastIndexOf("/") + 1, uri.length() - 4);
                    HashMap services = configurationContext.getAxisConfiguration().getServices();
                    AxisService service = (AxisService) services.get(serviceName);
                    if (service != null) {
                        response.addHeader(new Header("Content-Type", "text/xml"));
                        service.printSchema(baos);
                        byte[] buf = baos.toByteArray();
                        response.setBody(new ByteArrayInputStream(buf));
                        conn.writeResponse(response);
                        return true;
                    }
                }

                //cater for named xsds - check for the xsd name
                if (uri.indexOf("?xsd=") > 0) {
                    String serviceName = uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf("?xsd="));
                    String schemaName = uri.substring(uri.lastIndexOf("=") + 1);

                    HashMap services = configurationContext.getAxisConfiguration().getServices();
                    AxisService service = (AxisService) services.get(serviceName);
                    if (service != null) {
                        //run the population logic just to be sure
                        service.populateSchemaMappings();
                        //write out the correct schema
                        Hashtable schemaTable = service.getSchemaMappingTable();
                        XmlSchema schema = (XmlSchema) schemaTable.get(schemaName);
                        //schema found - write it to the stream
                        if (schema != null) {
                            response.addHeader(new Header("Content-Type", "text/xml"));
                            schema.write(baos);
                            byte[] buf = baos.toByteArray();
                            response.setBody(new ByteArrayInputStream(buf));
                            conn.writeResponse(response);

                        } else {
                            // no schema available by that name  - send 404
                            response.setStatusLine(
                                    request.getRequestLine().getHttpVersion(),
                                    404, "Schema Not Found!");
                        }

                        return true;

                    }
                }
                // It is GET handle the Get request
                boolean processed = HTTPTransportUtils.processHTTPGetRequest(
                        msgContext, baos,
                        soapAction, request.getRequestLine().getUri(),
                        configurationContext,
                        HTTPTransportReceiver.getGetRequestParameters(
                                request.getRequestLine().getUri()));

                if (!processed) {
                    response.setStatusLine(request.getRequestLine().getHttpVersion(), 200, "OK");
                    response.addHeader(new Header("Content-Type", "text/html"));
                    response.setBodyString(
                            HTTPTransportReceiver.getServicesHTML(configurationContext));
                    setResponseHeaders(conn, request, response, 0, msgContext);
                    conn.writeResponse(response);

                    return true;
                }
            } else {
                ByteArrayOutputStream baosIn = new ByteArrayOutputStream();
                byte[]                bytes = new byte[8192];
                int size;

                while ((size = inStream.read(bytes)) > 0) {
                    baosIn.write(bytes, 0, size);
                }

                inStream = new ByteArrayInputStream(baosIn.toByteArray());

                // It is POST, handle it
                HTTPTransportUtils.processHTTPPostRequest(msgContext, inStream, baos,
                        request.getContentType(), soapAction, request.getRequestLine().getUri());
            }

            OperationContext operationContext = msgContext.getOperationContext();
            Object contextWritten = null;

            if (operationContext != null) {
                contextWritten = operationContext.getProperty(Constants.RESPONSE_WRITTEN);
            }

            if ((contextWritten != null) && Constants.VALUE_TRUE.equals(contextWritten)) {
                response.setStatusLine(request.getRequestLine().getHttpVersion(), 200, "OK");
            } else {
                response.setStatusLine(request.getRequestLine().getHttpVersion(), 202, "OK");
            }

            byte[] buf = baos.toByteArray();
            response.setBody(new ByteArrayInputStream(buf));
            setResponseHeaders(conn, request, response, buf.length, msgContext);

            conn.writeResponse(response);
        } catch (Throwable e) {
            if (!(e instanceof java.net.SocketException)) {
                log.debug(e.getMessage(), e);
            }

            try {
                AxisEngine engine = new AxisEngine(configurationContext);

                if (msgContext != null) {
                    msgContext.setProperty(MessageContext.TRANSPORT_OUT, baos);

                    MessageContext faultContext = engine.createFaultMessageContext(msgContext, e);

                    response.setStatusLine(request.getRequestLine().getHttpVersion(), 500,
                            "Internal server error");
                    engine.sendFault(faultContext);
                    byte[] buf = baos.toByteArray();
                    response.setBody(new ByteArrayInputStream(buf));
                    setResponseHeaders(conn, request, response, buf.length, msgContext);
                    conn.writeResponse(response);
                }
            } catch (SocketException e1) {
                log.debug(e1.getMessage(), e1);
            } catch (Exception e1) {
                log.warn(e1.getMessage(), e1);
            }
        }

        return true;
    }

    /**
     * Simple Axis Transport Selection via deployment
     *
     * @param configContext
     * @param response
     */
    private void transportOutConfiguration(ConfigurationContext configContext,
                                           SimpleResponse response) {
        AxisConfiguration axisConf = configContext.getAxisConfiguration();
        HashMap transportOuts = axisConf.getTransportsOut();
        Iterator values = transportOuts.values().iterator();

        while (values.hasNext()) {
            TransportOutDescription transportOut = (TransportOutDescription) values.next();

            // reading axis2.xml for transport senders..
            Parameter version = transportOut.getParameter(HTTPConstants.PROTOCOL_VERSION);

            if (version != null) {
                if (HTTPConstants.HEADER_PROTOCOL_11.equals(version.getValue())) {

                    Parameter transferEncoding =
                            transportOut.getParameter(HTTPConstants.HEADER_TRANSFER_ENCODING);

                    if (transferEncoding != null) {
                        if (HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED.equals(
                                transferEncoding.getValue())) {
                            response.setHeader(
                                    new Header(
                                            HTTPConstants.HEADER_TRANSFER_ENCODING,
                                            HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED));
                        }
                    }
                }
            }
        }
    }

    private Map getHeaders(SimpleRequest request) {
        HashMap headerMap = new HashMap();
        Header[] headers = request.getHeaders();

        for (int i = 0; i < headers.length; i++) {
            headerMap.put(headers[i].getName(), headers[i].getValue());
        }

        return headerMap;
    }

    private void setResponseHeaders(final SimpleHttpServerConnection conn, SimpleRequest request,
                                    SimpleResponse response, long contentLength, MessageContext msgContext) {
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
        //TODO : provide a way to enable and diable cookies
        //setting the coolie in the out path
        Object cookieString = msgContext.getProperty(Constants.COOKIE_STRING);
        if (cookieString != null) {
            response.addHeader(new Header(HTTPConstants.HEADER_SET_COOKIE, (String) cookieString));
            response.addHeader(new Header(HTTPConstants.HEADER_SET_COOKIE2, (String) cookieString));
        }

        if (!response.containsHeader("Transfer-Encoding")) {
            if (contentLength != 0) {
                Header header = new Header("Content-Length", String.valueOf(contentLength));

                response.addHeader(header);
            }
        }
    }

    /**
     * To get the sessioncontext , if its not there in the hashtable , new one will be created and
     * added to the list.
     *
     * @param cookieID
     * @return <code>SessionContext</code>
     */
    private synchronized SessionContext getSessionContext(String cookieID) {
        SessionContext sessionContext = null;
        if (!(cookieID == null || cookieID.trim().equals(""))) {
            sessionContext = (SessionContext) sessionContextTable.get(cookieID);
        }
        if (sessionContext == null) {
            String cookieString = UUIDGenerator.getUUID();
            sessionContext = new SessionContext(null);
            sessionContext.setCookieID(cookieString);
            sessionContextTable.put(cookieString, sessionContext);
        }
        sessionContext.touch();
        cleanupServiceGroupContexts();
        return sessionContext;
    }

    private void cleanupServiceGroupContexts() {
        synchronized (sessionContextTable) {
            long currentTime = new Date().getTime();
            Iterator sgCtxtMapKeyIter = sessionContextTable.keySet().iterator();
            while (sgCtxtMapKeyIter.hasNext()) {
                String cookieID = (String) sgCtxtMapKeyIter.next();
                SessionContext sessionContext = (SessionContext) sessionContextTable.get(cookieID);
                if ((currentTime - sessionContext.getLastTouchedTime()) >
                        sessionContext.sessionContextTimeoutInterval) {
                    sgCtxtMapKeyIter.remove();
                    Iterator serviceGroupContext = sessionContext.getServiceGroupContext();
                    if (serviceGroupContext != null) {
                        while (serviceGroupContext.hasNext()) {
                            ServiceGroupContext groupContext = (ServiceGroupContext) serviceGroupContext.next();
                            cleanupServiceContextes(groupContext);
                        }
                    }
                }
            }
        }
    }

    private void cleanupServiceContextes(ServiceGroupContext serviceGroupContext) {
        Iterator serviceContecxtes = serviceGroupContext.getServiceContexts();
        while (serviceContecxtes.hasNext()) {
            ServiceContext serviceContext = (ServiceContext) serviceContecxtes.next();
            try {
                DependencyManager.destroyServiceClass(serviceContext);
            } catch (AxisFault axisFault) {
                log.info(axisFault.getMessage());
            }
        }
    }

}
