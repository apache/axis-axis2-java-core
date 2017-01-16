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
package org.apache.axis2.transport.http.impl.httpclient3;

import java.io.IOException;
import java.net.URL;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.Request;
import org.apache.axis2.transport.http.HTTPSender.HTTPStatusCodeFamily;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

final class RequestImpl implements Request {
    private static final Log log = LogFactory.getLog(RequestImpl.class);

    protected final HTTPSenderImpl sender;
    protected final MessageContext msgContext;
    protected final URL url;
    protected final HttpMethodBase method;
    protected final HttpClient httpClient;

    RequestImpl(HTTPSenderImpl sender, MessageContext msgContext, URL url, AxisRequestEntity requestEntity, HttpMethodBase method) throws AxisFault {
        this.sender = sender;
        this.msgContext = msgContext;
        this.url = url;
        this.method = method;
        httpClient = sender.getHttpClient(msgContext);
        sender.populateCommonProperties(msgContext, url, method, httpClient);
        if (requestEntity != null) {
            ((EntityEnclosingMethod)method).setRequestEntity(new AxisRequestEntityImpl(requestEntity));
    
            if (!sender.getHttpVersion().equals(HTTPConstants.HEADER_PROTOCOL_10) && sender.isChunked()) {
                ((EntityEnclosingMethod)method).setContentChunked(true);
            }
        }
    }

    @Override
    public void enableHTTP10() {
        httpClient.getParams().setVersion(HttpVersion.HTTP_1_0);
    }

    @Override
    public void setHeader(String name, String value) {
        method.setRequestHeader(name, value);
    }

    @Override
    public void execute() throws AxisFault {
        try {
            executeMethod();
            handleResponse();
        } catch (IOException e) {
            log.info("Unable to send to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            cleanup();
        }
    }

    private void executeMethod() throws IOException {
        HostConfiguration config = sender.getHostConfiguration(httpClient, msgContext, url);

        // set the custom headers, if available
        sender.addCustomHeaders(method, msgContext);

        // add compression headers if needed
        if (msgContext.isPropertyTrue(HTTPConstants.MC_ACCEPT_GZIP)) {
            method.addRequestHeader(HTTPConstants.HEADER_ACCEPT_ENCODING,
                    HTTPConstants.COMPRESSION_GZIP);
        }

        if (msgContext.isPropertyTrue(HTTPConstants.MC_GZIP_REQUEST)) {
            method.addRequestHeader(HTTPConstants.HEADER_CONTENT_ENCODING,
                    HTTPConstants.COMPRESSION_GZIP);
        }

        if (msgContext.getProperty(HTTPConstants.HTTP_METHOD_PARAMS) != null) {
            HttpMethodParams params = (HttpMethodParams) msgContext
                    .getProperty(HTTPConstants.HTTP_METHOD_PARAMS);
            method.setParams(params);
        }

        String cookiePolicy = (String) msgContext.getProperty(HTTPConstants.COOKIE_POLICY);
        if (cookiePolicy != null) {
            method.getParams().setCookiePolicy(cookiePolicy);
        }
        HttpState httpState = (HttpState) msgContext.getProperty(HTTPConstants.CACHED_HTTP_STATE);

        sender.setTimeouts(msgContext, method);

        httpClient.executeMethod(config, method, httpState);
    }

    private void handleResponse() throws IOException {
        int statusCode = method.getStatusCode();
        HTTPStatusCodeFamily family = sender.getHTTPStatusCodeFamily(statusCode);
        log.trace("Handling response - " + statusCode);
        if (statusCode == HttpStatus.SC_ACCEPTED) {
            /* When an HTTP 202 Accepted code has been received, this will be the case of an execution 
             * of an in-only operation. In such a scenario, the HTTP response headers should be returned,
             * i.e. session cookies. */
            sender.obtainHTTPHeaderInformation(method, msgContext);
            // Since we don't expect any content with a 202 response, we must release the connection
            method.releaseConnection();            
        } else if (HTTPStatusCodeFamily.SUCCESSFUL.equals(family)) {
            // Save the HttpMethod so that we can release the connection when cleaning up
            msgContext.setProperty(HTTPConstants.HTTP_METHOD, method);
            sender.processResponse(method, msgContext);
        } else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
                || statusCode == HttpStatus.SC_BAD_REQUEST) {
            // Save the HttpMethod so that we can release the connection when
            // cleaning up
            msgContext.setProperty(HTTPConstants.HTTP_METHOD, method);
            Header contenttypeHeader = method.getResponseHeader(HTTPConstants.HEADER_CONTENT_TYPE);
            String value = null;
            if (contenttypeHeader != null) {
                value = contenttypeHeader.getValue();
            }
            OperationContext opContext = msgContext.getOperationContext();
            if (opContext != null) {
                MessageContext inMessageContext = opContext
                        .getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inMessageContext != null) {
                    inMessageContext.setProcessingFault(true);
                }
            }
            if (value != null) {

                sender.processResponse(method, msgContext);
            }

            if (org.apache.axis2.util.Utils.isClientThreadNonBlockingPropertySet(msgContext)) {
                throw new AxisFault(Messages.getMessage("transportError",
                        String.valueOf(statusCode), method.getStatusText()));
            }
        } else {
            // Since we don't process the response, we must release the
            // connection immediately
            method.releaseConnection();
            throw new AxisFault(Messages.getMessage("transportError", String.valueOf(statusCode),
                    method.getStatusText()));
        }
    }

    private void cleanup() {
        if (msgContext.isPropertyTrue(HTTPConstants.AUTO_RELEASE_CONNECTION)) {
            log.trace("AutoReleasing " + method);
            method.releaseConnection();
        }
    }
}
