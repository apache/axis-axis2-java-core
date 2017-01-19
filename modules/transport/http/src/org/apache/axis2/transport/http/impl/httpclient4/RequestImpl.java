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
package org.apache.axis2.transport.http.impl.httpclient4;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.NamedValue;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.axis2.transport.http.HTTPAuthenticator;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportConstants;
import org.apache.axis2.transport.http.Request;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

final class RequestImpl implements Request {
    private static final Log log = LogFactory.getLog(RequestImpl.class);
    
    protected final HTTPSenderImpl sender;
    protected final MessageContext msgContext;
    protected final URL url;
    protected final HttpRequestBase method;
    protected final AbstractHttpClient httpClient;
    private final HttpHost httpHost;

    RequestImpl(HTTPSenderImpl sender, MessageContext msgContext, final String methodName, URL url,
            AxisRequestEntity requestEntity) throws AxisFault {
        this.sender = sender;
        this.msgContext = msgContext;
        this.url = url;
        httpClient = sender.getHttpClient(msgContext);
        if (requestEntity == null) {
            method = new HttpRequestBase() {
                @Override
                public String getMethod() {
                    return methodName;
                }
            };
        } else {
            HttpEntityEnclosingRequestBase entityEnclosingRequest = new HttpEntityEnclosingRequestBase() {
                @Override
                public String getMethod() {
                    return methodName;
                }
            };
            entityEnclosingRequest.setEntity(new AxisRequestEntityImpl(requestEntity));
            method = entityEnclosingRequest;
        }
        try {
            method.setURI(url.toURI());
        } catch (URISyntaxException ex) {
            throw AxisFault.makeFault(ex);
        }
        int port = url.getPort();
        String protocol = url.getProtocol();
        if (port == -1) {
            if (HTTPTransportConstants.PROTOCOL_HTTP.equals(protocol)) {
                port = 80;
            } else if (HTTPTransportConstants.PROTOCOL_HTTPS.equals(protocol)) {
                port = 443;
            }
        }
        httpHost = new HttpHost(url.getHost(), port, url.getProtocol());
    }

    @Override
    public void enableHTTP10() {
        httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_0);
    }

    @Override
    public void setHeader(String name, String value) {
        method.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        method.addHeader(name, value);
    }

    @Override
    public NamedValue[] getRequestHeaders() {
        Header[] headers = method.getAllHeaders();
        NamedValue[] result = new NamedValue[headers.length];
        for (int i=0; i<headers.length; i++) {
            result[i] = new NamedValue(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }

    @Override
    public void execute() throws AxisFault {
        HttpResponse response = null;
        try {
            response = executeMethod();
            handleResponse(response);
        } catch (IOException e) {
            log.info("Unable to send to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            cleanup(response);
        }
    }

    private HttpResponse executeMethod() throws IOException {
        populateHostConfiguration();

        // add compression headers if needed
        if (msgContext.isPropertyTrue(HTTPConstants.MC_ACCEPT_GZIP)) {
            method.addHeader(HTTPConstants.HEADER_ACCEPT_ENCODING,
                             HTTPConstants.COMPRESSION_GZIP);
        }

        if (msgContext.getProperty(HTTPConstants.HTTP_METHOD_PARAMS) != null) {
            HttpParams params = (HttpParams) msgContext
                    .getProperty(HTTPConstants.HTTP_METHOD_PARAMS);
            method.setParams(params);
        }

        String cookiePolicy = (String) msgContext.getProperty(HTTPConstants.COOKIE_POLICY);
        if (cookiePolicy != null) {
            method.getParams().setParameter(ClientPNames.COOKIE_POLICY, cookiePolicy);
        }

        sender.setTimeouts(msgContext, method);
        HttpContext localContext = new BasicHttpContext();
        // Why do we have add context here
        return httpClient.execute(httpHost, method, localContext);
    }

    private void handleResponse(HttpResponse response)
            throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        log.trace("Handling response - " + statusCode);
        if (statusCode == HttpStatus.SC_ACCEPTED) {
            msgContext.setProperty(HTTPConstants.CLEANUP_RESPONSE, Boolean.TRUE);
            /*
            * When an HTTP 202 Accepted code has been received, this will be
            * the case of an execution of an in-only operation. In such a
            * scenario, the HTTP response headers should be returned, i.e.
            * session cookies.
            */
            sender.obtainHTTPHeaderInformation(response, msgContext);

        } else if (statusCode >= 200 && statusCode < 300) {
            // We don't clean the response here because the response will be used afterwards
            msgContext.setProperty(HTTPConstants.CLEANUP_RESPONSE, Boolean.FALSE);
            sender.processResponse(response, msgContext);

        } else if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR
                   || statusCode == HttpStatus.SC_BAD_REQUEST) {
            msgContext.setProperty(HTTPConstants.CLEANUP_RESPONSE, Boolean.TRUE);
            Header contentTypeHeader = response.getFirstHeader(HTTPConstants.HEADER_CONTENT_TYPE);
            String value = null;
            if (contentTypeHeader != null) {
                value = contentTypeHeader.getValue();
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
                msgContext.setProperty(HTTPConstants.CLEANUP_RESPONSE, Boolean.FALSE);
                sender.processResponse(response, msgContext);
            }

            if (org.apache.axis2.util.Utils.isClientThreadNonBlockingPropertySet(msgContext)) {
                throw new AxisFault(Messages.
                        getMessage("transportError",
                                   String.valueOf(statusCode),
                                   response.getStatusLine().toString()));
            }
        } else {
            msgContext.setProperty(HTTPConstants.CLEANUP_RESPONSE, Boolean.TRUE);
            throw new AxisFault(Messages.getMessage("transportError", String.valueOf(statusCode),
                                                    response.getStatusLine().toString()));
        }
    }

    private void cleanup(HttpResponse response) {
        if (msgContext.isPropertyTrue(HTTPConstants.CLEANUP_RESPONSE)) {
            log.trace("Cleaning response : " + response);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (IOException e) {
                    log.error("Error while cleaning response : " + response, e);
                }
            }
        }
    }

    /**
     * getting host configuration to support standard http/s, proxy and NTLM
     * support
     *
     * @return a HostConfiguration set up with proxy information
     * @throws org.apache.axis2.AxisFault if problems occur
     */
    private void populateHostConfiguration() throws AxisFault {
        // proxy configuration

        if (HTTPProxyConfigurator.isProxyEnabled(msgContext, url)) {
            if (log.isDebugEnabled()) {
                log.debug("Configuring HTTP proxy.");
            }
            HTTPProxyConfigurator.configure(msgContext, httpClient);
        }
    }

    /*
     * This will handle server Authentication, It could be either NTLM, Digest
     * or Basic Authentication. Apart from that user can change the priory or
     * add a custom authentication scheme.
     */
    @Override
    public void enableAuthentication(HTTPAuthenticator authenticator) {
        method.getParams().setBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, true);

        String username = authenticator.getUsername();
        String password = authenticator.getPassword();
        String host = authenticator.getHost();
        String domain = authenticator.getDomain();

        int port = authenticator.getPort();
        String realm = authenticator.getRealm();

        Credentials creds;

        // TODO : Set preemptive authentication, but its not recommended in HC 4

        if (host != null) {
            if (domain != null) {
                /* Credentials for NTLM Authentication */
                httpClient.getAuthSchemes().register("ntlm",new NTLMSchemeFactory());
                creds = new NTCredentials(username, password, host, domain);
            } else {
                /* Credentials for Digest and Basic Authentication */
                creds = new UsernamePasswordCredentials(username, password);
            }
            httpClient.getCredentialsProvider().
                    setCredentials(new AuthScope(host, port, realm), creds);
        } else {
            if (domain != null) {
                /*
                 * Credentials for NTLM Authentication when host is
                 * ANY_HOST
                 */
                httpClient.getAuthSchemes().register("ntlm",new NTLMSchemeFactory());
                creds = new NTCredentials(username, password, AuthScope.ANY_HOST, domain);
                httpClient.getCredentialsProvider().
                        setCredentials(new AuthScope(AuthScope.ANY_HOST, port, realm),
                                       creds);
            } else {
                /* Credentials only for Digest and Basic Authentication */
                creds = new UsernamePasswordCredentials(username, password);
                httpClient.getCredentialsProvider().
                        setCredentials(new AuthScope(AuthScope.ANY), creds);
            }
        }
        /* Customizing the priority Order */
        List schemes = authenticator.getAuthSchemes();
        if (schemes != null && schemes.size() > 0) {
            List authPrefs = new ArrayList(3);
            for (int i = 0; i < schemes.size(); i++) {
                if (schemes.get(i) instanceof AuthPolicy) {
                    authPrefs.add(schemes.get(i));
                    continue;
                }
                String scheme = (String) schemes.get(i);
                authPrefs.add(authenticator.getAuthPolicyPref(scheme));

            }
            httpClient.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, authPrefs);
        }
    }
}
