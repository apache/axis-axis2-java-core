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

import edu.emory.mathcs.backport.java.util.concurrent.CountDownLatch;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.http.server.HttpUtils;
import org.apache.axis2.transport.http.server.OutputBuffer;
import org.apache.axis2.transport.http.server.Worker;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.ws.commons.schema.XmlSchema;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
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
        final String contextPath =
                (servicePath.startsWith("/") ? servicePath : "/" + servicePath) + "/";

        HttpVersion ver = request.getRequestLine().getHttpVersion();
        String uri = request.getRequestLine().getUri();
        String method = request.getRequestLine().getMethod();
        String soapAction = HttpUtils.getSoapAction(request);
        OutputBuffer outbuffer;
        InvocationResponse pi;

        if (method.equals(HTTPConstants.HEADER_GET)) {
            if (uri.equals("/favicon.ico")) {
                response.setStatusLine(new BasicStatusLine(ver, 301, "Redirect"));
                response.addHeader(new BasicHeader("Location", "http://ws.apache.org/favicon.ico"));
                return;
            }
            if (!uri.startsWith(contextPath)) {
                response.setStatusLine(new BasicStatusLine(ver, 301, "Redirect"));
                response.addHeader(new BasicHeader("Location", contextPath));
                return;
            }
            if (uri.endsWith("axis2/services/")) {
                response.setStatusLine(new BasicStatusLine(ver, 200, "OK"));
                String s = HTTPTransportReceiver.getServicesHTML(configurationContext);
                StringEntity entity = new StringEntity(s);
                entity.setContentType("text/html");
                response.setEntity(entity);
                return;
            }
            if (uri.indexOf("?") < 0) {
                if (!uri.endsWith(contextPath)) {
                    String serviceName = uri.replaceAll(contextPath, "");
                    if (serviceName.indexOf("/") < 0) {
                        String res = HTTPTransportReceiver
                                .printServiceHTML(serviceName, configurationContext);
                        StringEntity entity = new StringEntity(res);
                        entity.setContentType("text/html");
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
                    final String ip = getHostAddress(request);
                    EntityTemplate entity = new EntityTemplate(new ContentProducer() {

                        public void writeTo(final OutputStream outstream) throws IOException {
                            service.printWSDL2(outstream, ip, servicePath);
                        }

                    });
                    entity.setContentType("text/xml");
                    response.setEntity(entity);
                    return;
                }
            }
            if (uri.endsWith("?wsdl")) {
                String serviceName = uri.substring(uri.lastIndexOf("/") + 1, uri.length() - 5);
                HashMap services = configurationContext.getAxisConfiguration().getServices();
                final AxisService service = (AxisService) services.get(serviceName);
                if (service != null) {
                    final String ip = getHostAddress(request);
                    EntityTemplate entity = new EntityTemplate(new ContentProducer() {

                        public void writeTo(final OutputStream outstream) throws IOException {
                            service.printWSDL(outstream, ip, servicePath);
                        }

                    });
                    entity.setContentType("text/xml");
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
                    response.setEntity(entity);
                    return;
                }
            }
            //cater for named xsds - check for the xsd name
            if (uri.indexOf("?xsd=") > 0) {
                String serviceName =
                        uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf("?xsd="));
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

                            public void writeTo(final OutputStream outstream) {
                                schema.write(outstream);
                            }

                        });
                        entity.setContentType("text/xml");
                        response.setEntity(entity);
                        return;
                    } else {
                        // no schema available by that name  - send 404
                        response.setStatusLine(new BasicStatusLine(ver, 404, "Schema Not Found!"));
                        return;
                    }
                }
            }

            outbuffer = copyCommonProperties(msgContext, request);
            String contentType = null;
            Header[] headers = request.getHeaders(HTTPConstants.HEADER_CONTENT_TYPE);
            if (headers != null && headers.length > 0) {
                contentType = headers[0].getValue();
                int index = contentType.indexOf(';');
                if (index > 0) {
                    contentType = contentType.substring(0, index);
                }
            }
            // deal with GET request
            pi = RESTUtil.processURLRequest(msgContext, outbuffer.getOutputStream(), contentType);

        } else if (method.equals(HTTPConstants.HEADER_POST)) {
            // deal with POST request

            outbuffer = copyCommonProperties(msgContext, request);
            HttpEntity inentity = ((HttpEntityEnclosingRequest) request).getEntity();
            String contentType = processContentType(inentity, msgContext);
            if (HTTPTransportUtils.isRESTRequest(contentType)) {
                pi = RESTUtil.processXMLRequest(msgContext, inentity.getContent(),
                                                outbuffer.getOutputStream(), contentType);
            } else {
                String ip = (String)msgContext.getProperty(MessageContext.TRANSPORT_ADDR);
                if(ip!=null){
                    uri = ip + uri;
                }
                pi = HTTPTransportUtils.processHTTPPostRequest(msgContext, inentity.getContent(),
                                                               outbuffer.getOutputStream(),
                                                               contentType, soapAction, uri);
            }


        } else if (method.equals(HTTPConstants.HEADER_PUT)) {
            outbuffer = copyCommonProperties(msgContext, request);
            HttpEntity inentity = ((HttpEntityEnclosingRequest) request).getEntity();
            String contentType = processContentType(inentity, msgContext);
            pi = RESTUtil.processXMLRequest(msgContext, inentity.getContent(),
                                            outbuffer.getOutputStream(), contentType);

        } else if (method.equals(HTTPConstants.HEADER_DELETE)) {
            outbuffer = copyCommonProperties(msgContext, request);


            pi = RESTUtil.processURLRequest(msgContext, outbuffer.getOutputStream(), null);

        } else {

            throw new MethodNotSupportedException(method + " method not supported");
        }
        handleResponse(pi, response, outbuffer, msgContext);
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
                response.setStatusLine(new BasicStatusLine(ver, 202, "OK"));
                return;
            }
            response.setStatusLine(new BasicStatusLine(ver, 200, "OK"));
        } else {
            response.setStatusLine(new BasicStatusLine(ver, 202, "OK"));
        }
    }

    private void handleResponse(InvocationResponse pi, HttpResponse response,
                                OutputBuffer outbuffer, MessageContext msgContext)
            throws IOException {
        Boolean holdResponse =
                (Boolean) msgContext.getProperty(RequestResponseTransport.HOLD_RESPONSE);

        if (pi.equals(InvocationResponse.SUSPEND) ||
                (holdResponse != null && Boolean.TRUE.equals(holdResponse))) {
            try {
                ((RequestResponseTransport) msgContext
                        .getProperty(RequestResponseTransport.TRANSPORT_CONTROL)).awaitResponse();
            }
            catch (InterruptedException e) {
                throw new IOException("We were interrupted, so this may not function correctly:" +
                        e.getMessage());
            }
        }

        response.setEntity(outbuffer);
    }

    private String processContentType(HttpEntity inentity, MessageContext msgContext) {
        String contentType = null;
        Header header = inentity.getContentType();
        if (header != null) {
            contentType = header.getValue();
        }
        msgContext.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);
        return contentType;
    }

    private OutputBuffer copyCommonProperties(MessageContext msgContext, HttpRequest request) {
        request.getRequestLine().getUri();
        OutputBuffer outbuffer = new OutputBuffer();
        msgContext.setProperty(MessageContext.TRANSPORT_OUT, outbuffer);
        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, outbuffer);
        msgContext.setTo(new EndpointReference(request.getRequestLine().getUri()));
        msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL,
                               new SimpleHTTPRequestResponseTransport());
        return outbuffer;
    }

    public String getHostAddress(HttpRequest request) throws java.net.SocketException {
        try {
            Header hostHeader = request.getFirstHeader("host");
            if (hostHeader != null) {
                String host = hostHeader.getValue();
                return new URI("http://" + host).getHost();
            }
        } catch (Exception e) {

        }
        return HttpUtils.getIpAddress();
    }

    class SimpleHTTPRequestResponseTransport implements RequestResponseTransport {

        private CountDownLatch responseReadySignal = new CountDownLatch(1);
        RequestResponseTransportStatus status = RequestResponseTransportStatus.INITIAL;
        AxisFault faultToBeThrownOut = null;
        
        public void acknowledgeMessage(MessageContext msgContext) throws AxisFault {
            //TODO: Once the core HTTP API allows us to return an ack before unwinding, then the should be fixed
            signalResponseReady();
        }

        public void awaitResponse() throws InterruptedException,AxisFault {
            status = RequestResponseTransportStatus.WAITING;
            responseReadySignal.await();
            
            if (faultToBeThrownOut!=null)
            	throw faultToBeThrownOut;
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
        
    }
}
