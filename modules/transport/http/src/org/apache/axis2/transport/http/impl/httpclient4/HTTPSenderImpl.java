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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.NamedValue;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.axis2.transport.http.HTTPAuthenticator;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPSender;
import org.apache.axis2.transport.http.HTTPTransportConstants;
import org.apache.axis2.transport.http.Request;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class HTTPSenderImpl extends HTTPSender {

    private static final Log log = LogFactory.getLog(HTTPSenderImpl.class);

    boolean isChunked() {
        return chunked;
    }

    String getHttpVersion() {
        return httpVersion;
    }

    /**
     * Used to send a request via HTTP Get method
     *
     * @param msgContext        - The MessageContext of the message
     * @param url               - The target URL
     * @param soapActionString - The soapAction string of the request
     * @throws org.apache.axis2.AxisFault - Thrown in case an exception occurs
     */
    @Override
    protected Request prepareGet(final MessageContext msgContext, final URL url)
            throws AxisFault {
        return new RequestImpl(this, msgContext, url, null, new HttpGet());
    }

    /**
     * Used to send a request via HTTP Delete Method
     *
     * @param msgContext        - The MessageContext of the message
     * @param url               - The target URL
     * @param soapActionString - The soapAction string of the request
     * @throws org.apache.axis2.AxisFault - Thrown in case an exception occurs
     */
    @Override
    protected Request prepareDelete(final MessageContext msgContext, final URL url)
            throws AxisFault {
        return new RequestImpl(this, msgContext, url, null, new HttpDelete());
    }

    /**
     * Used to send a request via HTTP Post Method
     *
     * @param msgContext       - The MessageContext of the message
     * @param url              - The target URL
     * @throws org.apache.axis2.AxisFault - Thrown in case an exception occurs
     */
    @Override
    protected Request preparePost(final MessageContext msgContext, final URL url, AxisRequestEntity requestEntity)
            throws AxisFault {
        return new RequestImpl(this, msgContext, url, requestEntity, new HttpPost());
    }

    /**
     * Used to send a request via HTTP Put Method
     *
     * @param msgContext       - The MessageContext of the message
     * @param url              - The target URL
     * @throws org.apache.axis2.AxisFault - Thrown in case an exception occurs
     */
    @Override
    protected Request preparePut(final MessageContext msgContext, final URL url, AxisRequestEntity requestEntity)
            throws AxisFault {
        return new RequestImpl(this, msgContext, url, requestEntity, new HttpPut());
    }

    /**
     * Collect the HTTP header information and set them in the message context
     *
     * @param httpResponse which holds the header information
     * @param msgContext the MessageContext in which to place the information... OR
     *                   NOT!
     * @throws AxisFault if problems occur
     */
    protected void obtainHTTPHeaderInformation(Object httpResponse, MessageContext msgContext)
            throws AxisFault {
        HttpResponse response;
        if (httpResponse instanceof HttpResponse) {
            response = (HttpResponse) httpResponse;
        } else {
            return;
        }
        // Set RESPONSE properties onto the REQUEST message context. They will
        // need to be copied off the request context onto
        // the response context elsewhere, for example in the
        // OutInOperationClient.
        Map transportHeaders = new HTTPTransportHeaders(response.getAllHeaders());
        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, transportHeaders);
        msgContext.setProperty(HTTPConstants.MC_HTTP_STATUS_CODE,
                               new Integer(response.getStatusLine().getStatusCode()));
        Header header = response.getFirstHeader(HTTPConstants.HEADER_CONTENT_TYPE);

        if (header != null) {
            HeaderElement[] headers = header.getElements();
            MessageContext inMessageContext = msgContext.getOperationContext().getMessageContext(
                    WSDLConstants.MESSAGE_LABEL_IN_VALUE);

            Object contentType = header.getValue();
            Object charSetEnc = null;

            for (int i = 0; i < headers.length; i++) {
                NameValuePair charsetEnc = headers[i]
                        .getParameterByName(HTTPConstants.CHAR_SET_ENCODING);
                if (charsetEnc != null) {
                    charSetEnc = charsetEnc.getValue();
                }
            }

            if (inMessageContext != null) {
                inMessageContext.setProperty(Constants.Configuration.CONTENT_TYPE, contentType);
                inMessageContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING,
                                             charSetEnc);
            } else {

                // Transport details will be stored in a HashMap so that anybody
                // interested can
                // retrieve them
                HashMap transportInfoMap = new HashMap();
                transportInfoMap.put(Constants.Configuration.CONTENT_TYPE, contentType);
                transportInfoMap.put(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);

                // the HashMap is stored in the outgoing message.
                msgContext
                        .setProperty(Constants.Configuration.TRANSPORT_INFO_MAP, transportInfoMap);
            }
        }

        String sessionCookie = null;
        // Process old style headers first
        Header[] cookieHeaders = response.getHeaders(HTTPConstants.HEADER_SET_COOKIE);
        String customCoookiId = (String) msgContext.getProperty(Constants.CUSTOM_COOKIE_ID);
        for (int i = 0; i < cookieHeaders.length; i++) {
            HeaderElement[] elements = cookieHeaders[i].getElements();
            for (int e = 0; e < elements.length; e++) {
                HeaderElement element = elements[e];
                if (Constants.SESSION_COOKIE.equalsIgnoreCase(element.getName())
                    || Constants.SESSION_COOKIE_JSESSIONID.equalsIgnoreCase(element.getName())) {
                    sessionCookie = processCookieHeader(element);
                }
                if (customCoookiId != null && customCoookiId.equalsIgnoreCase(element.getName())) {
                    sessionCookie = processCookieHeader(element);
                }
            }
        }
        // Overwrite old style cookies with new style ones if present
        cookieHeaders = response.getHeaders(HTTPConstants.HEADER_SET_COOKIE2);
        for (int i = 0; i < cookieHeaders.length; i++) {
            HeaderElement[] elements = cookieHeaders[i].getElements();
            for (int e = 0; e < elements.length; e++) {
                HeaderElement element = elements[e];
                if (Constants.SESSION_COOKIE.equalsIgnoreCase(element.getName())
                    || Constants.SESSION_COOKIE_JSESSIONID.equalsIgnoreCase(element.getName())) {
                    sessionCookie = processCookieHeader(element);
                }
                if (customCoookiId != null && customCoookiId.equalsIgnoreCase(element.getName())) {
                    sessionCookie = processCookieHeader(element);
                }
            }
        }

        if (sessionCookie != null) {
            msgContext.getServiceContext().setProperty(HTTPConstants.COOKIE_STRING, sessionCookie);
        }
    }

    private String processCookieHeader(HeaderElement element) {
        String cookie = element.getName() + "=" + element.getValue();
        NameValuePair[] parameters = element.getParameters();
        for (int j = 0; parameters != null && j < parameters.length; j++) {
            NameValuePair parameter = parameters[j];
            cookie = cookie + "; " + parameter.getName() + "=" + parameter.getValue();
        }
        return cookie;
    }

    protected void processResponse(HttpResponse response, MessageContext msgContext)
            throws IOException {
        obtainHTTPHeaderInformation(response, msgContext);

        HttpEntity httpEntity = response.getEntity();
        InputStream in = httpEntity.getContent();
        if (in == null) {
            throw new AxisFault(Messages.getMessage("canNotBeNull", "InputStream"));
        }
        Header contentEncoding = httpEntity.getContentEncoding();
        if (contentEncoding != null) {
            if (contentEncoding.getValue().equalsIgnoreCase(HTTPConstants.COMPRESSION_GZIP)) {
                in = new GZIPInputStream(in);
                // If the content-encoding is identity we can basically ignore
                // it.
            } else if (!"identity".equalsIgnoreCase(contentEncoding.getValue())) {
                throw new AxisFault("HTTP :" + "unsupported content-encoding of '"
                                    + contentEncoding.getValue() + "' found");
            }
        }

        OperationContext opContext = msgContext.getOperationContext();
        if (opContext != null) {
            opContext.setProperty(MessageContext.TRANSPORT_IN, in);
        }
    }

    /**
     * getting host configuration to support standard http/s, proxy and NTLM
     * support
     *
     * @param client    active HttpClient
     * @param msgCtx    active MessageContext
     * @param targetURL the target URL
     * @return a HostConfiguration set up with proxy information
     * @throws org.apache.axis2.AxisFault if problems occur
     */
    protected HttpHost getHostConfiguration(AbstractHttpClient client, MessageContext msgCtx,
                                            URL targetURL) throws AxisFault {

        boolean isAuthenticationEnabled = isAuthenticationEnabled(msgCtx);
        int port = targetURL.getPort();

        String protocol = targetURL.getProtocol();
        if (port == -1) {
            if (HTTPTransportConstants.PROTOCOL_HTTP.equals(protocol)) {
                port = 80;
            } else if (HTTPTransportConstants.PROTOCOL_HTTPS.equals(protocol)) {
                port = 443;
            }
        }
        // to see the host is a proxy and in the proxy list - available in
        // axis2.xml
        HttpHost hostConfig = new HttpHost(targetURL.getHost(), port, targetURL.getProtocol());

        // TODO : one might need to set his own socket factory. We have to allow that case as well.

        if (isAuthenticationEnabled) {
            // Basic, Digest, NTLM and custom authentications.
            this.setAuthenticationInfo(client, msgCtx);
        }
        // proxy configuration

        if (HTTPProxyConfigurator.isProxyEnabled(msgCtx, targetURL)) {
            if (log.isDebugEnabled()) {
                log.debug("Configuring HTTP proxy.");
            }
            HTTPProxyConfigurator.configure(msgCtx, client);
        }

        return hostConfig;
    }

    protected boolean isAuthenticationEnabled(MessageContext msgCtx) {
        return (msgCtx.getProperty(HTTPConstants.AUTHENTICATE) != null);
    }

    /*
     * This will handle server Authentication, It could be either NTLM, Digest
     * or Basic Authentication. Apart from that user can change the priory or
     * add a custom authentication scheme.
     */
    protected void setAuthenticationInfo(AbstractHttpClient agent, MessageContext msgCtx)
            throws AxisFault {
        HTTPAuthenticator authenticator;
        Object obj = msgCtx.getProperty(HTTPConstants.AUTHENTICATE);
        if (obj != null) {
            if (obj instanceof HTTPAuthenticator) {
                authenticator = (HTTPAuthenticator) obj;

                String username = authenticator.getUsername();
                String password = authenticator.getPassword();
                String host = authenticator.getHost();
                String domain = authenticator.getDomain();

                int port = authenticator.getPort();
                String realm = authenticator.getRealm();

                /* If retrying is available set it first */
                isAllowedRetry = authenticator.isAllowedRetry();

                Credentials creds;

                // TODO : Set preemptive authentication, but its not recommended in HC 4

                if (host != null) {
                    if (domain != null) {
                        /* Credentials for NTLM Authentication */
                        agent.getAuthSchemes().register("ntlm",new NTLMSchemeFactory());
                        creds = new NTCredentials(username, password, host, domain);
                    } else {
                        /* Credentials for Digest and Basic Authentication */
                        creds = new UsernamePasswordCredentials(username, password);
                    }
                    agent.getCredentialsProvider().
                            setCredentials(new AuthScope(host, port, realm), creds);
                } else {
                    if (domain != null) {
                        /*
                         * Credentials for NTLM Authentication when host is
                         * ANY_HOST
                         */
                        agent.getAuthSchemes().register("ntlm",new NTLMSchemeFactory());
                        creds = new NTCredentials(username, password, AuthScope.ANY_HOST, domain);
                        agent.getCredentialsProvider().
                                setCredentials(new AuthScope(AuthScope.ANY_HOST, port, realm),
                                               creds);
                    } else {
                        /* Credentials only for Digest and Basic Authentication */
                        creds = new UsernamePasswordCredentials(username, password);
                        agent.getCredentialsProvider().
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
                    agent.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, authPrefs);
                }

            } else {
                throw new AxisFault("HttpTransportProperties.Authenticator class cast exception");
            }
        }

    }

    /**
     * Method used to copy all the common properties
     *
     * @param msgContext       - The messageContext of the request message
     * @param url              - The target URL
     * @param httpMethod       - The http method used to send the request
     * @param httpClient       - The httpclient used to send the request
     * @param soapActionString - The soap action atring of the request message
     * @return MessageFormatter - The messageFormatter for the relavent request
     *         message
     * @throws org.apache.axis2.AxisFault - Thrown in case an exception occurs
     */
    protected void populateCommonProperties(MessageContext msgContext, URL url,
                                                        HttpRequestBase httpMethod,
                                                        AbstractHttpClient httpClient)
            throws AxisFault {

        if (isAuthenticationEnabled(msgContext)) {
            httpMethod.getParams().setBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, true);
        }

        try {
            httpMethod.setURI(url.toURI());
        } catch (URISyntaxException e) {
            log.error("Error in URI : " + url, e);
        }
    }

    /**
     * This is used to get the dynamically set time out values from the message
     * context. If the values are not available or invalid then the default
     * values or the values set by the configuration will be used
     *
     * @param msgContext the active MessageContext
     * @param httpClient
     */
    protected void initializeTimeouts(MessageContext msgContext, AbstractHttpClient httpClient) {
        // If the SO_TIMEOUT of CONNECTION_TIMEOUT is set by dynamically the
        // override the static config
        Integer tempSoTimeoutProperty = (Integer) msgContext.getProperty(HTTPConstants.SO_TIMEOUT);
        Integer tempConnTimeoutProperty = (Integer) msgContext
                .getProperty(HTTPConstants.CONNECTION_TIMEOUT);
        long timeout = msgContext.getOptions().getTimeOutInMilliSeconds();

        if (tempConnTimeoutProperty != null) {
            int connectionTimeout = tempConnTimeoutProperty.intValue();
            // timeout for initial connection
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                                                connectionTimeout);
        } else {
            // set timeout in client
            if (timeout > 0) {
                httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                                                    (int) timeout);
            }
        }

        if (tempSoTimeoutProperty != null) {
            int soTimeout = tempSoTimeoutProperty.intValue();
            // SO_TIMEOUT -- timeout for blocking reads
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout);
        } else {
            // set timeout in client
            if (timeout > 0) {
                httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, (int) timeout);
            }
        }
    }

    /**
     * This is used to get the dynamically set time out values from the message
     * context. If the values are not available or invalid then the default
     * values or the values set by the configuration will be used
     *
     * @param msgContext the active MessageContext
     * @param httpMethod method
     */
    protected void setTimeouts(MessageContext msgContext, HttpRequestBase httpMethod) {
        // If the SO_TIMEOUT of CONNECTION_TIMEOUT is set by dynamically the
        // override the static config
        Integer tempSoTimeoutProperty = (Integer) msgContext.getProperty(HTTPConstants.SO_TIMEOUT);
        Integer tempConnTimeoutProperty = (Integer) msgContext
                .getProperty(HTTPConstants.CONNECTION_TIMEOUT);
        long timeout = msgContext.getOptions().getTimeOutInMilliSeconds();

        if (tempConnTimeoutProperty != null) {
            // timeout for initial connection
            httpMethod.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                                                tempConnTimeoutProperty);
        }

        if (tempSoTimeoutProperty != null) {
            // SO_TIMEOUT -- timeout for blocking reads
            httpMethod.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
                                                tempSoTimeoutProperty);
        } else {
            // set timeout in client
            if (timeout > 0) {
                httpMethod.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, (int) timeout);
            }
        }
    }

    protected AbstractHttpClient getHttpClient(MessageContext msgContext) {
        ConfigurationContext configContext = msgContext.getConfigurationContext();

        AbstractHttpClient httpClient = (AbstractHttpClient) msgContext
                .getProperty(HTTPConstants.CACHED_HTTP_CLIENT);

        if (httpClient == null) {
            httpClient = (AbstractHttpClient) configContext.
                    getProperty(HTTPConstants.CACHED_HTTP_CLIENT);
        }

        if (httpClient != null) {
            return httpClient;
        }

        synchronized (this) {
            httpClient = (AbstractHttpClient) msgContext.
                    getProperty(HTTPConstants.CACHED_HTTP_CLIENT);

            if (httpClient == null) {
                httpClient = (AbstractHttpClient) configContext
                        .getProperty(HTTPConstants.CACHED_HTTP_CLIENT);
            }

            if (httpClient != null) {
                return httpClient;
            }

            ClientConnectionManager connManager = (ClientConnectionManager) msgContext
                    .getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);
            if (connManager == null) {
                connManager = (ClientConnectionManager) msgContext
                        .getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);
            }
            if (connManager == null) {
                // reuse HttpConnectionManager
                synchronized (configContext) {
                    connManager = (ClientConnectionManager) configContext
                            .getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);
                    if (connManager == null) {
                        log.trace("Making new ConnectionManager");
                        SchemeRegistry schemeRegistry = new SchemeRegistry();
                        schemeRegistry.register(
                                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
                        schemeRegistry.register(
                                new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

                        connManager = new PoolingClientConnectionManager(schemeRegistry);
                        ((PoolingClientConnectionManager)connManager).setMaxTotal(200);
                        ((PoolingClientConnectionManager)connManager).setDefaultMaxPerRoute(200);
                        configContext.setProperty(
                                HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER, connManager);
                    }
                }
            }
            /*
             * Create a new instance of HttpClient since the way it is used here
             * it's not fully thread-safe.
             */
            HttpParams clientParams = new BasicHttpParams();
            clientParams.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
            httpClient = new DefaultHttpClient(connManager, clientParams);

            //We don't need to set timeout for connection manager, since we are doing it below
            // and its enough

            // Get the timeout values set in the runtime
            initializeTimeouts(msgContext, httpClient);

            return httpClient;
        }
    }

    public void addCustomHeaders(HttpRequestBase method, MessageContext msgContext) {

        boolean isCustomUserAgentSet = false;
        // set the custom headers, if available
        Object httpHeadersObj = msgContext.getProperty(HTTPConstants.HTTP_HEADERS);
        if (httpHeadersObj != null) {
            if (httpHeadersObj instanceof List) {
                List httpHeaders = (List) httpHeadersObj;
                for (int i = 0; i < httpHeaders.size(); i++) {
                    NamedValue nv = (NamedValue) httpHeaders.get(i);
                    if (nv != null) {
                        Header header = new BasicHeader(nv.getName(), nv.getValue());
                        if (HTTPConstants.HEADER_USER_AGENT.equals(header.getName())) {
                            isCustomUserAgentSet = true;
                        }
                        method.addHeader(header);
                    }
                }

            }
            if (httpHeadersObj instanceof Map) {
                Map httpHeaders = (Map) httpHeadersObj;
                for (Iterator iterator = httpHeaders.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    if (HTTPConstants.HEADER_USER_AGENT.equals(key)) {
                        isCustomUserAgentSet = true;
                    }
                    method.addHeader(key, value);
                }
            }
        }

        // we have to consider the TRANSPORT_HEADERS map as well
        Map transportHeaders = (Map) msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
        if (transportHeaders != null) {
            removeUnwantedHeaders(msgContext);

            Set headerEntries = transportHeaders.entrySet();

            for (Object headerEntry : headerEntries) {
                if (headerEntry instanceof Map.Entry) {
                    Header[] headers = method.getAllHeaders();

                    boolean headerAdded = false;
                    for (Header header : headers) {
                        if (header.getName() != null
                            && header.getName().equals(((Map.Entry) headerEntry).getKey())) {
                            headerAdded = true;
                            break;
                        }
                    }

                    if (!headerAdded) {
                        method.addHeader(((Map.Entry) headerEntry).getKey().toString(),
                                         ((Map.Entry) headerEntry).getValue().toString());
                    }
                }
            }
        }

        if (!isCustomUserAgentSet) {
            String userAgentString = getUserAgent(msgContext);
            method.setHeader(HTTPConstants.HEADER_USER_AGENT, userAgentString);
        }

    }

    /**
     * Remove unwanted headers from the transport headers map of outgoing
     * request. These are headers which should be dictated by the transport and
     * not the user. We remove these as these may get copied from the request
     * messages
     *
     * @param msgContext the Axis2 Message context from which these headers should be
     *                   removed
     */
    private void removeUnwantedHeaders(MessageContext msgContext) {
        Map headers = (Map) msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);

        if (headers == null || headers.isEmpty()) {
            return;
        }

        Iterator iter = headers.keySet().iterator();
        while (iter.hasNext()) {
            String headerName = (String) iter.next();
            if (HTTP.CONN_DIRECTIVE.equalsIgnoreCase(headerName)
                || HTTP.TRANSFER_ENCODING.equalsIgnoreCase(headerName)
                || HTTP.DATE_HEADER.equalsIgnoreCase(headerName)
                || HTTP.CONTENT_TYPE.equalsIgnoreCase(headerName)
                || HTTP.CONTENT_LEN.equalsIgnoreCase(headerName)) {
                iter.remove();
            }
        }
    }

    private String getUserAgent(MessageContext messageContext) {
        String userAgentString = "Axis2";
        boolean locked = false;
        if (messageContext.getParameter(HTTPConstants.USER_AGENT) != null) {
            OMElement userAgentElement = messageContext.getParameter(HTTPConstants.USER_AGENT)
                    .getParameterElement();
            userAgentString = userAgentElement.getText().trim();
            OMAttribute lockedAttribute = userAgentElement.getAttribute(new QName("locked"));
            if (lockedAttribute != null) {
                if (lockedAttribute.getAttributeValue().equalsIgnoreCase("true")) {
                    locked = true;
                }
            }
        }
        // Runtime overing part
        if (!locked) {
            if (messageContext.getProperty(HTTPConstants.USER_AGENT) != null) {
                userAgentString = (String) messageContext.getProperty(HTTPConstants.USER_AGENT);
            }
        }

        return userAgentString;
    }

}
