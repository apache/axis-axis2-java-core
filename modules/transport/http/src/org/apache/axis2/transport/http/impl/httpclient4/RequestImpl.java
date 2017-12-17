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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axiom.mime.Header;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.axis2.transport.http.HTTPAuthenticator;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportConstants;
import org.apache.axis2.transport.http.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.util.EntityUtils;

final class RequestImpl implements Request {
    private static final String[] COOKIE_HEADER_NAMES = { HTTPConstants.HEADER_SET_COOKIE, HTTPConstants.HEADER_SET_COOKIE2 };
    
    private static final Log log = LogFactory.getLog(RequestImpl.class);
    
    private final HttpClient httpClient;
    private final MessageContext msgContext;
    private final URL url;
    private final HttpRequestBase method;
    private final HttpHost httpHost;
    private final RequestConfig.Builder requestConfig = RequestConfig.custom();
    private final HttpClientContext clientContext = HttpClientContext.create();
    private HttpResponse response;

    RequestImpl(HttpClient httpClient, MessageContext msgContext, final String methodName, URL url,
            AxisRequestEntity requestEntity) throws AxisFault {
        this.httpClient = httpClient;
        this.msgContext = msgContext;
        this.url = url;
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
        method.setProtocolVersion(HttpVersion.HTTP_1_0);
    }

    @Override
    public void setHeader(String name, String value) {
        method.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        method.addHeader(name, value);
    }

    private static Header[] convertHeaders(org.apache.http.Header[] headers) {
        Header[] result = new Header[headers.length];
        for (int i=0; i<headers.length; i++) {
            result[i] = new Header(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }

    @Override
    public Header[] getRequestHeaders() {
        return convertHeaders(method.getAllHeaders());
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        requestConfig.setConnectTimeout(timeout);
    }

    @Override
    public void setSocketTimeout(int timeout) {
        requestConfig.setSocketTimeout(timeout);
    }

    @Override
    public int getStatusCode() {
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public String getStatusText() {
        return response.getStatusLine().getReasonPhrase();
    }

    @Override
    public String getResponseHeader(String name) {
        org.apache.http.Header header = response.getFirstHeader(name);
        return header == null ? null : header.getValue();
    }

    @Override
    public Header[] getResponseHeaders() {
        return convertHeaders(response.getAllHeaders());
    }

    @Override
    public Map<String,String> getCookies() {
        Map<String,String> cookies = null;
        for (String name : COOKIE_HEADER_NAMES) {
            for (org.apache.http.Header header : response.getHeaders(name)) {
                for (HeaderElement element : header.getElements()) {
                    if (cookies == null) {
                        cookies = new HashMap<String,String>();
                    }
                    cookies.put(element.getName(), element.getValue());
                }
            }
        }
        return cookies;
    }

    @Override
    public InputStream getResponseContent() throws IOException {
        HttpEntity entity = response.getEntity();
        return entity == null ? null : entity.getContent();
    }

    @Override
    public void execute() throws IOException {
        populateHostConfiguration();

        // add compression headers if needed
        if (msgContext.isPropertyTrue(HTTPConstants.MC_ACCEPT_GZIP)) {
            method.addHeader(HTTPConstants.HEADER_ACCEPT_ENCODING,
                             HTTPConstants.COMPRESSION_GZIP);
        }

        String cookiePolicy = (String) msgContext.getProperty(HTTPConstants.COOKIE_POLICY);
        if (cookiePolicy != null) {
            requestConfig.setCookieSpec(cookiePolicy);
        }

        method.setConfig(requestConfig.build());

        response = httpClient.execute(httpHost, method, clientContext);
    }

    @Override
    public void releaseConnection() {
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
            HTTPProxyConfigurator.configure(msgContext, requestConfig, clientContext);
        }
    }

    /*
     * This will handle server Authentication, It could be either NTLM, Digest
     * or Basic Authentication. Apart from that user can change the priory or
     * add a custom authentication scheme.
     */
    @Override
    public void enableAuthentication(HTTPAuthenticator authenticator) {
        requestConfig.setAuthenticationEnabled(true);

        String username = authenticator.getUsername();
        String password = authenticator.getPassword();
        String host = authenticator.getHost();
        String domain = authenticator.getDomain();

        int port = authenticator.getPort();
        String realm = authenticator.getRealm();

        Credentials creds;

        // TODO : Set preemptive authentication, but its not recommended in HC 4

        CredentialsProvider credsProvider = clientContext.getCredentialsProvider();
        if (credsProvider == null) {
            credsProvider = new BasicCredentialsProvider();
            clientContext.setCredentialsProvider(credsProvider);
        }
        if (host != null) {
            if (domain != null) {
                /* Credentials for NTLM Authentication */
                creds = new NTCredentials(username, password, host, domain);
            } else {
                /* Credentials for Digest and Basic Authentication */
                creds = new UsernamePasswordCredentials(username, password);
            }
            credsProvider.setCredentials(new AuthScope(host, port, realm), creds);
        } else {
            if (domain != null) {
                /*
                 * Credentials for NTLM Authentication when host is
                 * ANY_HOST
                 */
                creds = new NTCredentials(username, password, AuthScope.ANY_HOST, domain);
                credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, port, realm), creds);
            } else {
                /* Credentials only for Digest and Basic Authentication */
                creds = new UsernamePasswordCredentials(username, password);
                credsProvider.setCredentials(new AuthScope(AuthScope.ANY), creds);
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
            requestConfig.setTargetPreferredAuthSchemes(authPrefs);
        }
    }
}
