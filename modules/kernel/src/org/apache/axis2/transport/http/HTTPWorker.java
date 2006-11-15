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

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.http.server.HttpUtils;
import org.apache.axis2.transport.http.server.OutputBuffer;
import org.apache.axis2.transport.http.server.Worker;
import org.apache.http.*;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.StringEntity;
import org.apache.ws.commons.schema.XmlSchema;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class HTTPWorker implements Worker {

    public HTTPWorker() {
    }

    public void service(
            final HttpRequest request,
            final HttpResponse response,
            final MessageContext msgContext) throws HttpException, IOException {

        ConfigurationContext configurationContext = msgContext.getConfigurationContext();
        final String servicePath = configurationContext.getServiceContextPath();
        final String contextPath = (servicePath.startsWith("/") ? servicePath : "/" + servicePath) + "/";

        HttpVersion ver = request.getRequestLine().getHttpVersion();
        String uri = request.getRequestLine().getUri();
        String method = request.getRequestLine().getMethod();
        String soapAction = HttpUtils.getSoapAction(request);

        // Adjust version and content chunking based on the config
        boolean chunked = false;
        TransportOutDescription transportOut = msgContext.getTransportOut();
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
                if (!uri.endsWith(contextPath)) {
                    String serviceName = uri.replaceAll(contextPath, "");
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
                    Map schemaTable = service.getSchemaMappingTable();
                    final XmlSchema schema = (XmlSchema) schemaTable.get(schemaName);
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
        Object isTwoChannel = null;
        if (operationContext != null) {
            contextWritten = operationContext.getProperty(Constants.RESPONSE_WRITTEN);
            isTwoChannel = operationContext.getProperty(Constants.DIFFERENT_EPR);
        }


        if ((contextWritten != null) && Constants.VALUE_TRUE.equals(contextWritten)) {
            if ((isTwoChannel != null) && Constants.VALUE_TRUE.equals(isTwoChannel)) {
                response.setStatusLine(new StatusLine(ver, 202, "OK"));
                return;
            }
            response.setStatusLine(new StatusLine(ver, 200, "OK"));
        } else {
            response.setStatusLine(new StatusLine(ver, 202, "OK"));
        }
    }

}
