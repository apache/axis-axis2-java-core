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
package org.apache.axis2.transport.http.impl.httpclient5;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axiom.mime.Header;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.axis2.transport.http.HTTPAuthenticator;
import org.apache.axis2.transport.http.HTTPTransportConstants;
import org.apache.axis2.transport.http.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.StandardAuthScheme;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.message.BasicHeaderValueParser;
import org.apache.hc.core5.http.message.HeaderGroup;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Timeout;

final class RequestImpl implements Request {
    private static final String[] COOKIE_HEADER_NAMES = { HTTPConstants.HEADER_SET_COOKIE, HTTPConstants.HEADER_SET_COOKIE2 };
    
    private static final Log log = LogFactory.getLog(RequestImpl.class);
    
    private final HttpClient httpClient;
    private final MessageContext msgContext;
    private final HttpUriRequestBase httpRequestMethod;
    private final HttpHost httpHost;
    private final RequestConfig.Builder requestConfig;
    private final HttpClientContext clientContext;
    private ClassicHttpResponse response;
    private final String methodName;
    private String path;
    private String scheme;
    private URIAuthority authority;
    private URI requestUri;
    private ProtocolVersion version;
    private HeaderGroup headerGroup;
    private boolean absoluteRequestUri;


    RequestImpl(HttpClient httpClient, MessageContext msgContext, final String methodName, URI requestUri, AxisRequestEntity requestEntity) throws AxisFault {
        this.httpClient = httpClient;
        this.methodName = methodName;
        this.msgContext = msgContext;
        this.requestUri = requestUri;
	this.authority = authority;
	this.requestConfig = RequestConfig.custom();
	this.clientContext = HttpClientContext.create();
        this.httpRequestMethod = new HttpUriRequestBase(this.methodName, this.requestUri);
        if (requestEntity != null) {
            this.httpRequestMethod.setEntity(new AxisRequestEntityImpl(requestEntity));
        }
        try {
            this.httpRequestMethod.setUri(requestUri);
        } catch (Exception ex) {
            throw AxisFault.makeFault(ex);
        }
        int port = requestUri.getPort();
	String protocol;
	// AXIS2-6073
	// This may always be null here, HttpUriRequestBase has the scheme but is unused?
	// And also, protocol doesn't need to be set on HttpUriRequestBase?
	if (this.httpRequestMethod.getVersion() != null && this.httpRequestMethod.getVersion().getProtocol() != null) {
	    protocol = this.httpRequestMethod.getVersion().getProtocol();
            log.debug("Received protocol from this.httpRequestMethod.getVersion().getProtocol(): " + protocol);
        } else if (requestUri.getScheme() != null) {
	    protocol = requestUri.getScheme();
            log.debug("Received protocol from requestUri.getScheme(): " + protocol);
	} else {
	    protocol = "http";
            log.warn("Cannot find protocol, using default as http on requestUri: " + requestUri);
	}
        if (port == -1) {
            if (HTTPTransportConstants.PROTOCOL_HTTP.equals(protocol)) {
                port = 80;
            } else if (HTTPTransportConstants.PROTOCOL_HTTPS.equals(protocol)) {
                port = 443;
            }
        }
        httpHost = new HttpHost(protocol, requestUri.getHost(), port);
    }

    @Override
    public void enableHTTP10() {
        httpRequestMethod.setVersion(HttpVersion.HTTP_1_0);
    }

    @Override
    public void setHeader(String name, String value) {
        httpRequestMethod.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        httpRequestMethod.addHeader(name, value);
    }

    private static Header[] convertHeaders(org.apache.hc.core5.http.Header[] headers) {
        Header[] result = new Header[headers.length];
        for (int i=0; i<headers.length; i++) {
            result[i] = new Header(headers[i].getName(), headers[i].getValue());
        }
        int size = result != null ? result.length : 0;
        return result;
    }

    @Override
    public Header[] getRequestHeaders() {
        int size = httpRequestMethod.getHeaders() != null ? httpRequestMethod.getHeaders().length : 0;
        return convertHeaders(httpRequestMethod.getHeaders());
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        requestConfig.setConnectTimeout(Timeout.ofMilliseconds(timeout));
        requestConfig.setConnectionRequestTimeout(Timeout.ofMilliseconds(timeout));
    }

    @Override
    public void setResponseTimeout(int timeout) {
        // AXIS2-6051 this new param in core5 isn't the same thing as socket timeout.
	// See the SocketConfig of HTTPSenderImpl for socket timeout. 
	// Core5 docs say: Determines the timeout until arrival of a response from the opposite endpoint.
        requestConfig.setResponseTimeout(Timeout.ofMilliseconds(timeout));
    }

    @Override
    public int getStatusCode() {
        return response.getCode();
    }

    @Override
    public String getStatusText() {
        return response.getReasonPhrase();
    }

    @Override
    public String getResponseHeader(String name) {
        org.apache.hc.core5.http.Header header = response.getFirstHeader(name);
        return header == null ? null : header.getValue();
    }

    @Override
    public Header[] getResponseHeaders() {
        return convertHeaders(response.getHeaders());
    }

    @Override
    public Map<String,String> getCookies() {
        Map<String,String> cookies = new HashMap<>();
        for (String name : COOKIE_HEADER_NAMES) {
            for (final org.apache.hc.core5.http.Header header : response.getHeaders(name)) {
                final String headerValue = header.getValue();
                if (headerValue == null) {
                    continue;
                }
                final ParserCursor cursor = new ParserCursor(0, headerValue.length());
                final HeaderElement[] headerElements = BasicHeaderValueParser.INSTANCE.parseElements(headerValue,
                        cursor);
                for (final HeaderElement headerElement : headerElements) {
                    cookies.put(headerElement.getName(), headerElement.getValue());
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
            httpRequestMethod.addHeader(HTTPConstants.HEADER_ACCEPT_ENCODING,
                             HTTPConstants.COMPRESSION_GZIP);
        }

        String cookiePolicy = (String) msgContext.getProperty(HTTPConstants.COOKIE_POLICY);
        if (cookiePolicy != null) {
            requestConfig.setCookieSpec(cookiePolicy);
        }

        clientContext.setRequestConfig(requestConfig.build());
	// AXIS2-6051, the move from javax to jakarta
	// broke HTTPClient by sending Content-Length,
	// resulting in:
	// ProtocolException: Content-Length header already present
	httpRequestMethod.removeHeaders("Content-Length");
        final org.apache.hc.core5.http.Header[] headers = httpRequestMethod.getHeaders();
        for (final org.apache.hc.core5.http.Header header : headers) {
            log.debug("sending HTTP request header: " + header);
        }
        response = httpClient.executeOpen(httpHost, httpRequestMethod, clientContext);
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

        try {
            if (HTTPProxyConfigurator.isProxyEnabled(msgContext, this.requestUri.toURL())) {
                if (log.isDebugEnabled()) {
                    log.debug("Configuring HTTP proxy.");
                }
                HTTPProxyConfigurator.configure(msgContext, requestConfig, clientContext);
            }
        } catch (java.net.MalformedURLException ex) {
            throw AxisFault.makeFault(ex);
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
	//
	// AXIS2-6051, CredentialsProvider no longer has setCredentialsProvider() however BasicCredentialsProvider
	// does have it. clientContext.getCredentialsProvider() returns CredentialsProvider. 
        CredentialsProvider credsProvider = clientContext.getCredentialsProvider();
        if (credsProvider == null) {
            BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
            clientContext.setCredentialsProvider(basicCredentialsProvider);
            if (host != null) {
                if (domain != null) {
                    /* Credentials for NTLM Authentication */
                    creds = new NTCredentials(username, password.toCharArray(), host, domain);
                } else {
                    /* Credentials for Digest and Basic Authentication */
                    creds = new UsernamePasswordCredentials(username, password.toCharArray());
                }
                basicCredentialsProvider.setCredentials(new AuthScope(null, host, port, realm, null), creds);
            } else {
                if (domain != null) {
                    /*
                     * Credentials for NTLM Authentication when host is
                     * ANY_HOST
                     */
                    creds = new NTCredentials(username, password.toCharArray(), null, domain);
                    basicCredentialsProvider.setCredentials(new AuthScope(null, null, port, realm, null), creds);
                } else {
                    /* Credentials only for Digest and Basic Authentication */
                    creds = new UsernamePasswordCredentials(username, password.toCharArray());
                    basicCredentialsProvider.setCredentials(new AuthScope(host, port), creds);
                }
            }
        }
        
        /* Customizing the priority Order */
        List schemes = authenticator.getAuthSchemes();
        if (schemes != null && schemes.size() > 0) {
            List authPrefs = new ArrayList(3);
            for (int i = 0; i < schemes.size(); i++) {
                if (schemes.get(i) instanceof StandardAuthScheme) {
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
