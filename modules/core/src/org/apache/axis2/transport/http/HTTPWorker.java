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

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportReceiver;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.transport.http.server.OutputBuffer;
import org.apache.axis2.transport.http.server.HttpUtils;
import org.apache.axis2.transport.http.server.Worker;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.StatusLine;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.StringEntity;
import org.apache.ws.commons.schema.XmlSchema;

public class HTTPWorker implements Worker {

    private static final Log log = LogFactory.getLog(HTTPWorker.class);
    
    private ConfigurationContext configurationContext;
    // to store session object
    private static Hashtable sessionContextTable = new Hashtable();
    private String contextPath = null;
    private String servicePath = null;

    public HTTPWorker(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        contextPath = configurationContext.getContextPath() + "/";
        servicePath = configurationContext.getServicePath();
    }

    public void service(final HttpRequest request, final HttpResponse response) 
            throws HttpException, IOException {    
        MessageContext msgContext = new MessageContext();
        msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);
        try {
            if (configurationContext == null) {
                throw new AxisFault(Messages.getMessage("cannotBeNullConfigurationContext"));
            }
            // Get relevant request parameters
            HttpVersion ver = request.getRequestLine().getHttpVersion();
            String uri = request.getRequestLine().getUri();
            String method = request.getRequestLine().getMethod();

            String soapAction = HttpUtils.getSoapAction(request);
            String cookieID = HttpUtils.getCookieID(request);
            
            boolean chunked = false;

            TransportOutDescription transportOut = configurationContext.getAxisConfiguration()
                .getTransportOut(new QName(Constants.TRANSPORT_HTTP));
            TransportInDescription transportIn = configurationContext.getAxisConfiguration()
                .getTransportIn(new QName(Constants.TRANSPORT_HTTP));            
        
            // Configure message context
            msgContext.setConfigurationContext(configurationContext);
            if (configurationContext.getAxisConfiguration().isManageTransportSession()) {
                SessionContext sessionContext = getSessionContext(cookieID);
                msgContext.setSessionContext(sessionContext);
            }
            msgContext.setTransportIn(transportIn);
            msgContext.setTransportOut(transportOut);
            msgContext.setServiceGroupContextId(UUIDGenerator.getUUID());
            msgContext.setServerSide(true);
            msgContext.setProperty(Constants.Configuration.TRANSPORT_IN_URL, uri);

            // set the transport Headers
            HashMap headerMap = new HashMap();
            Header[] headers = request.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i].getName(), headers[i].getValue());
            }
            msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);

            // Adjust version and content chunking based on the config
            if (transportOut != null) {
                Parameter p = transportOut.getParameter(HTTPConstants.PROTOCOL_VERSION);
                if (p != null) {
                    if (HTTPConstants.HEADER_PROTOCOL_10.equals(p.getValue())) {
                        ver = HttpVersion.HTTP_1_0;
                    }
                }
                if (ver.greaterEquals(HttpVersion.HTTP_1_1)) {
                    p = transportOut.getParameter(HTTPConstants.HEADER_TRANSFER_ENCODING);
                    if (p != null) {
                        if (HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED.equals(p.getValue())) {
                            chunked = true;
                        }
                    }
                }
            }

            if (method.equals(HTTPConstants.HEADER_GET)) {
                if (uri.equals("/favicon.ico")) {
                    response.setStatusLine(new StatusLine(ver, 301, "Redirect"));
                    response.addHeader(new Header("Location", "http://ws.apache.org/favicon.ico"));
                    return;
                }
                if (!uri.startsWith(contextPath)) {
                    response.setStatusLine(new StatusLine(ver, 301, "Redirect"));
                    response.addHeader(new Header("Location", contextPath));
                    return;
                }
                if (uri.indexOf("?") < 0) {
                    if (!(uri.endsWith(contextPath) || uri.endsWith(contextPath+"/"))) {
                        String serviceName = uri.replaceAll(contextPath+"/", "");
                        if (serviceName.indexOf("/") < 0) {
                            String res = HTTPTransportReceiver.printServiceHTML(serviceName, configurationContext);
                            StringEntity entity = new StringEntity(res);
                            entity.setContentType("text/html");
                            entity.setChunked(chunked);
                            response.setEntity(entity);
                            return;
                        }
                    }
                }
                if (uri.endsWith("?wsdl2")) {
                    String serviceName = uri.substring(uri.lastIndexOf("/") + 1, uri.length() - 6);
                    HashMap services = configurationContext.getAxisConfiguration().getServices();
                    final AxisService service = (AxisService) services.get(serviceName);
                    if (service != null) {
                        final String ip = HttpUtils.getIpAddress();
                        EntityTemplate entity = new EntityTemplate(new ContentProducer() {

                            public void writeTo(final OutputStream outstream) throws IOException {
                                service.printWSDL2(outstream, ip, servicePath);
                            }
                            
                        });
                        entity.setContentType("text/xml");
                        entity.setChunked(chunked);
                        response.setEntity(entity);
                        return;
                    }
                }
                if (uri.endsWith("?wsdl")) {
                    String serviceName = uri.substring(uri.lastIndexOf("/") + 1, uri.length() - 5);
                    HashMap services = configurationContext.getAxisConfiguration().getServices();
                    final AxisService service = (AxisService) services.get(serviceName);
                    if (service != null) {
                        final String ip = HttpUtils.getIpAddress();
                        EntityTemplate entity = new EntityTemplate(new ContentProducer() {

                            public void writeTo(final OutputStream outstream) throws IOException {
                                service.printWSDL(outstream, ip, servicePath);
                            }
                            
                        });
                        entity.setContentType("text/xml");
                        entity.setChunked(chunked);
                        response.setEntity(entity);
                        return;
                    }
                }
                if (uri.endsWith("?xsd")) {
                    String serviceName = uri.substring(uri.lastIndexOf("/") + 1, uri.length() - 4);
                    HashMap services = configurationContext.getAxisConfiguration().getServices();
                    final AxisService service = (AxisService) services.get(serviceName);
                    if (service != null) {
                        EntityTemplate entity = new EntityTemplate(new ContentProducer() {

                            public void writeTo(final OutputStream outstream) throws IOException {
                                service.printSchema(outstream);
                            }
                            
                        });
                        entity.setContentType("text/xml");
                        entity.setChunked(chunked);
                        response.setEntity(entity);
                        return;
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
                        final XmlSchema schema = (XmlSchema)schemaTable.get(schemaName);
                        //schema found - write it to the stream
                        if (schema != null) {
                            EntityTemplate entity = new EntityTemplate(new ContentProducer() {

                                public void writeTo(final OutputStream outstream) throws IOException {
                                    schema.write(outstream);
                                }
                                
                            });
                            entity.setContentType("text/xml");
                            entity.setChunked(chunked);
                            response.setEntity(entity);
                            return;
                        } else {
                            // no schema available by that name  - send 404
                            response.setStatusLine(new StatusLine(ver, 404, "Schema Not Found!"));
                            return;
                        }
                    }
                }

                OutputBuffer outbuffer = new OutputBuffer(); 
                msgContext.setProperty(MessageContext.TRANSPORT_OUT, outbuffer);
                msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, outbuffer);

                // deal with GET request
                boolean processed = HTTPTransportUtils.processHTTPGetRequest(
                        msgContext, 
                        outbuffer.getOutputStream(), 
                        soapAction, 
                        uri,
                        configurationContext,
                        HTTPTransportReceiver.getGetRequestParameters(uri));
                
                if (processed) {
                    outbuffer.setChunked(chunked);
                    response.setEntity(outbuffer);
                } else {
                    response.setStatusLine(new StatusLine(ver, 200, "OK"));
                    String s = HTTPTransportReceiver.getServicesHTML(configurationContext);
                    StringEntity entity = new StringEntity(s);
                    entity.setContentType("text/html");
                    entity.setChunked(chunked);
                    response.setEntity(entity);
                }
                
            } else if (method.equals(HTTPConstants.HEADER_POST)) {
                // deal with POST request

                OutputBuffer outbuffer = new OutputBuffer(); 
                msgContext.setProperty(MessageContext.TRANSPORT_OUT, outbuffer);
                msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, outbuffer);

                HttpEntity inentity = ((HttpEntityEnclosingRequest) request).getEntity();
                String contenttype = null;
                if (inentity.getContentType() != null) {
                    contenttype = inentity.getContentType().getValue();
                }
                HTTPTransportUtils.processHTTPPostRequest(
                        msgContext, 
                        inentity.getContent(), 
                        outbuffer.getOutputStream(),
                        contenttype, 
                        soapAction, 
                        uri);
                
                outbuffer.setChunked(chunked);
                response.setEntity(outbuffer);
                
            } else {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            
            // Finalize response
            OperationContext operationContext = msgContext.getOperationContext();
            Object contextWritten = null;
            if (operationContext != null) {
                contextWritten = operationContext.getProperty(Constants.RESPONSE_WRITTEN);
            }
            if ((contextWritten != null) && Constants.VALUE_TRUE.equals(contextWritten)) {
                response.setStatusLine(new StatusLine(ver, 200, "OK"));
            } else {
                response.setStatusLine(new StatusLine(ver, 202, "OK"));
            }
            setCookie(response, msgContext);
            
        } catch (SocketException ex) {
            // Socket is unreliable. 
            throw ex;
        } catch (HttpException ex) {
            // HTTP protocol violation. Transport is unrelaible
            throw ex;
        } catch (Throwable e) {
            HttpVersion ver = request.getRequestLine().getHttpVersion();
            try {
                AxisEngine engine = new AxisEngine(configurationContext);
                
                OutputBuffer outbuffer = new OutputBuffer(); 
                msgContext.setProperty(MessageContext.TRANSPORT_OUT, outbuffer.getOutputStream());
                msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, outbuffer);

                MessageContext faultContext = engine.createFaultMessageContext(msgContext, e);

                response.setStatusLine(new StatusLine(ver, 500, "Internal server error"));
                engine.sendFault(faultContext);
                response.setEntity(outbuffer);
                setCookie(response, msgContext);
            } catch (Exception ex) {
                response.setStatusLine(new StatusLine(ver, 500, "Internal server error"));
                StringEntity entity = new StringEntity(ex.getMessage());
                entity.setContentType("text/plain");
                response.setEntity(entity);
            }
        }
    }

    private static void setCookie(final HttpResponse response, MessageContext msgContext) {
        //TODO : provide a way to enable and diable cookies
        //setting the coolie in the out path
        Object cookieString = msgContext.getProperty(Constants.COOKIE_STRING);
        if (cookieString != null) {
            response.addHeader(new Header(HTTPConstants.HEADER_SET_COOKIE, (String) cookieString));
            response.addHeader(new Header(HTTPConstants.HEADER_SET_COOKIE2, (String) cookieString));
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
