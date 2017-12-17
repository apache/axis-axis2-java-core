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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.NamedValue;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.HTTPAuthenticator;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPSender;
import org.apache.axis2.transport.http.HTTPTransportConstants;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;

public class HTTPSenderImpl extends HTTPSender {

    private static final Log log = LogFactory.getLog(HTTPSenderImpl.class);

    /**
     * Used to send a request via HTTP Get method
     * 
     * @param msgContext
     *            - The MessageContext of the message
     * @param url
     *            - The target URL
     * @param soapActiionString
     *            - The soapAction string of the request
     * @throws AxisFault
     *             - Thrown in case an exception occurs
     */
    protected void sendViaGet(MessageContext msgContext, URL url, String soapActiionString)
            throws AxisFault {

        GetMethod getMethod = new GetMethod();
        HttpClient httpClient = getHttpClient(msgContext);
        MessageFormatter messageFormatter = populateCommonProperties(msgContext, url, getMethod,
                httpClient, soapActiionString);

        // Need to have this here because we can have soap action when using the
        // soap response MEP
        String soapAction = messageFormatter
                .formatSOAPAction(msgContext, format, soapActiionString);

        if (soapAction != null && !msgContext.isDoingREST()) {
            getMethod.setRequestHeader(HTTPConstants.HEADER_SOAP_ACTION, soapAction);
        }
        try {
            executeMethod(httpClient, msgContext, url, getMethod);
            handleResponse(msgContext, getMethod);
        } catch (IOException e) {
            log.info("Unable to sendViaGet to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            cleanup(msgContext, getMethod);
        }
    }

    protected void cleanup(MessageContext msgContext, Object httpMmethod) {
        if (httpMmethod instanceof HttpMethod) {
            if (msgContext.isPropertyTrue(HTTPConstants.AUTO_RELEASE_CONNECTION)) {
                log.trace("AutoReleasing " + httpMmethod);
                ((HttpMethod) httpMmethod).releaseConnection();
            }
        }
    }

    /**
     * Used to send a request via HTTP Delete Method
     * 
     * @param msgContext
     *            - The MessageContext of the message
     * @param url
     *            - The target URL
     * @param soapActiionString
     *            - The soapAction string of the request
     * @throws AxisFault
     *             - Thrown in case an exception occurs
     */
    protected void sendViaDelete(MessageContext msgContext, URL url, String soapActiionString)
            throws AxisFault {

        DeleteMethod deleteMethod = new DeleteMethod();
        HttpClient httpClient = getHttpClient(msgContext);
        populateCommonProperties(msgContext, url, deleteMethod, httpClient, soapActiionString);

        try {
            executeMethod(httpClient, msgContext, url, deleteMethod);
            handleResponse(msgContext, deleteMethod);
        } catch (IOException e) {
            log.info("Unable to sendViaDelete to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            cleanup(msgContext, deleteMethod);
        }
    }

    /**
     * Used to send a request via HTTP Post Method
     * 
     * @param msgContext
     *            - The MessageContext of the message
     * @param url
     *            - The target URL
     * @param soapActionString
     *            - The soapAction string of the request
     * @throws AxisFault
     *             - Thrown in case an exception occurs
     */
    protected void sendViaPost(MessageContext msgContext, URL url, String soapActionString)
            throws AxisFault {

        HttpClient httpClient = getHttpClient(msgContext);

        /*
         * What's up with this, it never gets used anywhere?? --Glen String
         * charEncoding = (String)
         * msgContext.getProperty(Constants.Configuration
         * .CHARACTER_SET_ENCODING);
         * 
         * if (charEncoding == null) { charEncoding =
         * MessageContext.DEFAULT_CHAR_SET_ENCODING; }
         */

        PostMethod postMethod = new PostMethod();
        if (log.isTraceEnabled()) {
            log.trace(Thread.currentThread() + " PostMethod " + postMethod + " / " + httpClient);
        }
        MessageFormatter messageFormatter = populateCommonProperties(msgContext, url, postMethod,
                httpClient, soapActionString);

        postMethod.setRequestEntity(new AxisRequestEntityImpl(messageFormatter, msgContext, format,
                soapActionString, chunked, isAllowedRetry));

        if (!httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10) && chunked) {
            postMethod.setContentChunked(true);
        }

        String soapAction = messageFormatter.formatSOAPAction(msgContext, format, soapActionString);

        if (soapAction != null && !msgContext.isDoingREST()) {
            postMethod.setRequestHeader(HTTPConstants.HEADER_SOAP_ACTION, soapAction);
        }

        /*
         * main excecution takes place..
         */
        try {
            executeMethod(httpClient, msgContext, url, postMethod);
            handleResponse(msgContext, postMethod);
        } catch (IOException e) {
            log.info("Unable to sendViaPost to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            cleanup(msgContext, postMethod);
        }
    }

    /**
     * Used to send a request via HTTP Put Method
     * 
     * @param msgContext
     *            - The MessageContext of the message
     * @param url
     *            - The target URL
     * @param soapActionString
     *            - The soapAction string of the request
     * @throws AxisFault
     *             - Thrown in case an exception occurs
     */
    protected void sendViaPut(MessageContext msgContext, URL url, String soapActionString)
            throws AxisFault {

        HttpClient httpClient = getHttpClient(msgContext);

        /*
         * Same deal - this value never gets used, why is it here? --Glen String
         * charEncoding = (String)
         * msgContext.getProperty(Constants.Configuration
         * .CHARACTER_SET_ENCODING);
         * 
         * if (charEncoding == null) { charEncoding =
         * MessageContext.DEFAULT_CHAR_SET_ENCODING; }
         */

        PutMethod putMethod = new PutMethod();
        MessageFormatter messageFormatter = populateCommonProperties(msgContext, url, putMethod,
                httpClient, soapActionString);

        putMethod.setRequestEntity(new AxisRequestEntityImpl(messageFormatter, msgContext, format,
                soapActionString, chunked, isAllowedRetry));

        if (!httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10) && chunked) {
            putMethod.setContentChunked(true);
        }

        String soapAction = messageFormatter.formatSOAPAction(msgContext, format, soapActionString);
        if (soapAction != null && !msgContext.isDoingREST()) {
            putMethod.setRequestHeader(HTTPConstants.HEADER_SOAP_ACTION, soapAction);
        }

        /*
         * main excecution takes place..
         */
        try {
            executeMethod(httpClient, msgContext, url, putMethod);
            handleResponse(msgContext, putMethod);
        } catch (IOException e) {
            log.info("Unable to sendViaPut to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            cleanup(msgContext, putMethod);
        }
    }

    /**
     * Used to handle the HTTP Response
     * 
     * @param msgContext
     *            - The MessageContext of the message
     * @param method
     *            - The HTTP method used
     * @throws IOException
     *             - Thrown in case an exception occurs
     */
    protected void handleResponse(MessageContext msgContext, Object httpMethodBase)
            throws IOException {
        HttpMethodBase method;
        if (httpMethodBase instanceof HttpMethodBase) {
            method = (HttpMethodBase) httpMethodBase;
        } else {
            log.trace("HttpMethodBase expected, but found - " + httpMethodBase);
            return;
        }
        int statusCode = method.getStatusCode();
        HTTPStatusCodeFamily family = getHTTPStatusCodeFamily(statusCode);
        log.trace("Handling response - " + statusCode);
        if (statusCode == HttpStatus.SC_ACCEPTED) {
            /* When an HTTP 202 Accepted code has been received, this will be the case of an execution 
             * of an in-only operation. In such a scenario, the HTTP response headers should be returned,
             * i.e. session cookies. */
            obtainHTTPHeaderInformation(method, msgContext);
            // Since we don't expect any content with a 202 response, we must release the connection
            method.releaseConnection();            
        } else if (HTTPStatusCodeFamily.SUCCESSFUL.equals(family)) {
            // Save the HttpMethod so that we can release the connection when cleaning up
            msgContext.setProperty(HTTPConstants.HTTP_METHOD, method);
            processResponse(method, msgContext);            
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

                processResponse(method, msgContext);
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

    /**
     * Collect the HTTP header information and set them in the message context
     * 
     * @param method
     *            HttpMethodBase from which to get information
     * @param msgContext
     *            the MessageContext in which to place the information... OR
     *            NOT!
     * @throws AxisFault
     *             if problems occur
     */
    protected void obtainHTTPHeaderInformation(Object httpMethodBase, MessageContext msgContext)
            throws AxisFault {
        HttpMethod method;
        if (httpMethodBase instanceof HttpMethodBase) {
            method = (HttpMethod) httpMethodBase;
        } else {
            return;
        }
        // Set RESPONSE properties onto the REQUEST message context. They will
        // need to be copied off the request context onto
        // the response context elsewhere, for example in the
        // OutInOperationClient.
        Map transportHeaders = new HTTPTransportHeaders(method.getResponseHeaders());
        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, transportHeaders);
        msgContext.setProperty(HTTPConstants.MC_HTTP_STATUS_CODE,
                new Integer(method.getStatusCode()));
        Header header = method.getResponseHeader(HTTPConstants.HEADER_CONTENT_TYPE);

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
        Header[] cookieHeaders = method.getResponseHeaders(HTTPConstants.HEADER_SET_COOKIE);
        String customCoookiId = (String) msgContext.getProperty(Constants.CUSTOM_COOKIE_ID);

        // The following only check for JSESSIONID / axis_session / custom-cookie-id from the Set-Cookie header.
        // But it will ignore if there are other Set-Cookie values, which may cause issues at client level,
        // when invoking via a load-balancer, which expect some specific Cookie value.
        // So the correct fix is to add whatever the value(s) in the Set-Cookie header as session cookie.

//        for (int i = 0; i < cookieHeaders.length; i++) {
//            HeaderElement[] elements = cookieHeaders[i].getElements();
//            for (int e = 0; e < elements.length; e++) {
//                HeaderElement element = elements[e];
//                if (Constants.SESSION_COOKIE.equalsIgnoreCase(element.getName())
//                        || Constants.SESSION_COOKIE_JSESSIONID.equalsIgnoreCase(element.getName())) {
//                    sessionCookie = processCookieHeader(element);
//                }
//                if (customCoookiId != null && customCoookiId.equalsIgnoreCase(element.getName())) {
//                    sessionCookie = processCookieHeader(element);
//                }
//            }
//        }

        sessionCookie = processSetCookieHeaders(cookieHeaders);

        // Overwrite old style cookies with new style ones if present
        cookieHeaders = method.getResponseHeaders(HTTPConstants.HEADER_SET_COOKIE2);
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

        if (sessionCookie != null && !sessionCookie.equals("")) {
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


    private String processSetCookieHeaders(Header[] headers) {
        String cookie = "";
        for (Header header : headers) {
            if (!cookie.equals("")) {
                cookie = cookie + ";";
            }
            HeaderElement[] elements = header.getElements();
            for (HeaderElement element : elements) {
                cookie = cookie + element.getName() + "=" + element.getValue();
                NameValuePair[] parameters = element.getParameters();
                for (NameValuePair parameter : parameters) {
                    cookie = cookie + "; " + parameter.getName() + "=" + parameter.getValue();
                }
            }
        }
        return cookie;
    }

    protected void processResponse(HttpMethodBase httpMethod, MessageContext msgContext)
            throws IOException {
        obtainHTTPHeaderInformation(httpMethod, msgContext);

        InputStream in = httpMethod.getResponseBodyAsStream();
        if (in == null) {
            throw new AxisFault(Messages.getMessage("canNotBeNull", "InputStream"));
        }
        Header contentEncoding = httpMethod
                .getResponseHeader(HTTPConstants.HEADER_CONTENT_ENCODING);
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
     * @param client
     *            active HttpClient
     * @param msgCtx
     *            active MessageContext
     * @param targetURL
     *            the target URL
     * @return a HostConfiguration set up with proxy information
     * @throws AxisFault
     *             if problems occur
     */
    protected HostConfiguration getHostConfiguration(HttpClient client, MessageContext msgCtx,
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
        HostConfiguration config = client.getHostConfiguration();
        if (config == null) {
            config = new HostConfiguration();
        }

        // one might need to set his own socket factory. Let's allow that case
        // as well.
        Protocol protocolHandler = (Protocol) msgCtx.getOptions().getProperty(
                HTTPConstants.CUSTOM_PROTOCOL_HANDLER);

        // setting the real host configuration
        // I assume the 90% case, or even 99% case will be no protocol handler
        // case.
        if (protocolHandler == null) {
            config.setHost(targetURL.getHost(), port, targetURL.getProtocol());
        } else {
            config.setHost(targetURL.getHost(), port, protocolHandler);
        }

        if (isAuthenticationEnabled) {
            // Basic, Digest, NTLM and custom authentications.
            this.setAuthenticationInfo(client, msgCtx, config);
        }
        // proxy configuration

        if (HTTPProxyConfigurator.isProxyEnabled(msgCtx, targetURL)) {
            if (log.isDebugEnabled()) {
                log.debug("Configuring HTTP proxy.");
            }
            HTTPProxyConfigurator.configure(msgCtx, client, config);
        }

        return config;
    }

    protected boolean isAuthenticationEnabled(MessageContext msgCtx) {
        return (msgCtx.getProperty(HTTPConstants.AUTHENTICATE) != null);
    }

    /*
     * This will handle server Authentication, It could be either NTLM, Digest
     * or Basic Authentication. Apart from that user can change the priory or
     * add a custom authentication scheme.
     */
    protected void setAuthenticationInfo(HttpClient agent, MessageContext msgCtx,
            HostConfiguration config) throws AxisFault {
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

                HttpState tmpHttpState = null;
                HttpState httpState = (HttpState) msgCtx
                        .getProperty(HTTPConstants.CACHED_HTTP_STATE);
                if (httpState != null) {
                    tmpHttpState = httpState;
                } else {
                    tmpHttpState = agent.getState();
                }

                agent.getParams().setAuthenticationPreemptive(
                        authenticator.getPreemptiveAuthentication());

                if (host != null) {
                    if (domain != null) {
                        /* Credentials for NTLM Authentication */
                        creds = new NTCredentials(username, password, host, domain);
                    } else {
                        /* Credentials for Digest and Basic Authentication */
                        creds = new UsernamePasswordCredentials(username, password);
                    }
                    tmpHttpState.setCredentials(new AuthScope(host, port, realm), creds);
                } else {
                    if (domain != null) {
                        /*
                         * Credentials for NTLM Authentication when host is
                         * ANY_HOST
                         */
                        creds = new NTCredentials(username, password, AuthScope.ANY_HOST, domain);
                        tmpHttpState.setCredentials(new AuthScope(AuthScope.ANY_HOST, port, realm),
                                creds);
                    } else {
                        /* Credentials only for Digest and Basic Authentication */
                        creds = new UsernamePasswordCredentials(username, password);
                        tmpHttpState.setCredentials(new AuthScope(AuthScope.ANY), creds);
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
                    agent.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
                }

            } else {
                throw new AxisFault("HttpTransportProperties.Authenticator class cast exception");
            }
        }

    }

    /**
     * Method used to copy all the common properties
     * 
     * @param msgContext
     *            - The messageContext of the request message
     * @param url
     *            - The target URL
     * @param httpMethod
     *            - The http method used to send the request
     * @param httpClient
     *            - The httpclient used to send the request
     * @param soapActionString
     *            - The soap action atring of the request message
     * @return MessageFormatter - The messageFormatter for the relavent request
     *         message
     * @throws AxisFault
     *             - Thrown in case an exception occurs
     */
    protected MessageFormatter populateCommonProperties(MessageContext msgContext, URL url,
            HttpMethodBase httpMethod, HttpClient httpClient, String soapActionString)
            throws AxisFault {

        if (isAuthenticationEnabled(msgContext)) {
            httpMethod.setDoAuthentication(true);
        }

        MessageFormatter messageFormatter = MessageProcessorSelector
                .getMessageFormatter(msgContext);

        url = messageFormatter.getTargetAddress(msgContext, format, url);

        httpMethod.setPath(url.getPath());

        httpMethod.setQueryString(url.getQuery());

        httpMethod.setRequestHeader(HTTPConstants.HEADER_CONTENT_TYPE,
                messageFormatter.getContentType(msgContext, format, soapActionString));

        httpMethod.setRequestHeader(HTTPConstants.HEADER_HOST, url.getHost());

        if (msgContext.getOptions() != null && msgContext.getOptions().isManageSession()) {
            // setting the cookie in the out path
            Object cookieString = msgContext.getProperty(HTTPConstants.COOKIE_STRING);

            if (cookieString != null) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(cookieString);
                httpMethod.setRequestHeader(HTTPConstants.HEADER_COOKIE, buffer.toString());
            }
        }

        if (httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10)) {
            httpClient.getParams().setVersion(HttpVersion.HTTP_1_0);
        }
        return messageFormatter;
    }

    /**
     * This is used to get the dynamically set time out values from the message
     * context. If the values are not available or invalid then the default
     * values or the values set by the configuration will be used
     * 
     * @param msgContext
     *            the active MessageContext
     * @param httpClient
     */
    protected void initializeTimeouts(MessageContext msgContext, HttpClient httpClient) {
        // If the SO_TIMEOUT of CONNECTION_TIMEOUT is set by dynamically the
        // override the static config
        Integer tempSoTimeoutProperty = (Integer) msgContext.getProperty(HTTPConstants.SO_TIMEOUT);
        Integer tempConnTimeoutProperty = (Integer) msgContext
                .getProperty(HTTPConstants.CONNECTION_TIMEOUT);
        long timeout = msgContext.getOptions().getTimeOutInMilliSeconds();

        if (tempConnTimeoutProperty != null) {
            int connectionTimeout = tempConnTimeoutProperty.intValue();
            // timeout for initial connection
            httpClient.getHttpConnectionManager().getParams()
                    .setConnectionTimeout(connectionTimeout);
        } else {
            // set timeout in client
            if (timeout > 0) {
                httpClient.getHttpConnectionManager().getParams()
                        .setConnectionTimeout((int) timeout);
            }
        }

        if (tempSoTimeoutProperty != null) {
            int soTimeout = tempSoTimeoutProperty.intValue();
            // SO_TIMEOUT -- timeout for blocking reads
            httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeout);
            httpClient.getParams().setSoTimeout(soTimeout);
        } else {
            // set timeout in client
            if (timeout > 0) {
                httpClient.getHttpConnectionManager().getParams().setSoTimeout((int) timeout);
                httpClient.getParams().setSoTimeout((int) timeout);
            }
        }
    }

    /**
     * This is used to get the dynamically set time out values from the message
     * context. If the values are not available or invalid then the default
     * values or the values set by the configuration will be used
     * 
     * @param msgContext
     *            the active MessageContext
     * @param httpMethod
     *            method
     */
    protected void setTimeouts(MessageContext msgContext, HttpMethod httpMethod) {
        // If the SO_TIMEOUT of CONNECTION_TIMEOUT is set by dynamically the
        // override the static config
        Integer tempSoTimeoutProperty = (Integer) msgContext.getProperty(HTTPConstants.SO_TIMEOUT);
        Integer tempConnTimeoutProperty = (Integer) msgContext
                .getProperty(HTTPConstants.CONNECTION_TIMEOUT);
        long timeout = msgContext.getOptions().getTimeOutInMilliSeconds();

        if (tempConnTimeoutProperty != null) {
            // timeout for initial connection
            httpMethod.getParams().setParameter("http.connection.timeout", tempConnTimeoutProperty);
        }

        if (tempSoTimeoutProperty != null) {
            // SO_TIMEOUT -- timeout for blocking reads
            httpMethod.getParams().setSoTimeout(tempSoTimeoutProperty);
        } else {
            // set timeout in client
            if (timeout > 0) {
                httpMethod.getParams().setSoTimeout((int) timeout);
            }
        }
    }

    protected HttpClient getHttpClient(MessageContext msgContext) {
        ConfigurationContext configContext = msgContext.getConfigurationContext();

        HttpClient httpClient = (HttpClient) msgContext
                .getProperty(HTTPConstants.CACHED_HTTP_CLIENT);

        if (httpClient == null) {
            httpClient = (HttpClient) configContext.getProperty(HTTPConstants.CACHED_HTTP_CLIENT);
        }

        if (httpClient != null) {
            return httpClient;
        }

        synchronized (this) {
            httpClient = (HttpClient) msgContext.getProperty(HTTPConstants.CACHED_HTTP_CLIENT);

            if (httpClient == null) {
                httpClient = (HttpClient) configContext
                        .getProperty(HTTPConstants.CACHED_HTTP_CLIENT);
            }

            if (httpClient != null) {
                return httpClient;
            }

            HttpConnectionManager connManager = (HttpConnectionManager) msgContext
                    .getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);
            if (connManager == null) {
                connManager = (HttpConnectionManager) msgContext
                        .getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);
            }
            if (connManager == null) {
                // reuse HttpConnectionManager
                synchronized (configContext) {
                    connManager = (HttpConnectionManager) configContext
                            .getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);
                    if (connManager == null) {
                        log.trace("Making new ConnectionManager");
                        connManager = new MultiThreadedHttpConnectionManager();
                        configContext.setProperty(
                                HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER, connManager);
                    }
                }
            }
            /*
             * Create a new instance of HttpClient since the way it is used here
             * it's not fully thread-safe.
             */
            httpClient = new HttpClient(connManager);

            // Set the default timeout in case we have a connection pool
            // starvation to 30sec
            httpClient.getParams().setConnectionManagerTimeout(30000);

            // Get the timeout values set in the runtime
            initializeTimeouts(msgContext, httpClient);

            return httpClient;
        }
    }

    protected void executeMethod(HttpClient httpClient, MessageContext msgContext, URL url,
            HttpMethod method) throws IOException {
        HostConfiguration config = this.getHostConfiguration(httpClient, msgContext, url);

        // set the custom headers, if available
        addCustomHeaders(method, msgContext);

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

        setTimeouts(msgContext, method);

        httpClient.executeMethod(config, method, httpState);
    }

    public void addCustomHeaders(HttpMethod method, MessageContext msgContext) {

        boolean isCustomUserAgentSet = false;
        // set the custom headers, if available
        Object httpHeadersObj = msgContext.getProperty(HTTPConstants.HTTP_HEADERS);
        if (httpHeadersObj != null) {
            if (httpHeadersObj instanceof List) {
                List httpHeaders = (List) httpHeadersObj;
                for (int i = 0; i < httpHeaders.size(); i++) {
                    NamedValue nv = (NamedValue) httpHeaders.get(i);
                    if (nv != null) {
                        Header header = new Header(nv.getName(), nv.getValue());
                        if (HTTPConstants.HEADER_USER_AGENT.equals(header.getName())) {
                            isCustomUserAgentSet = true;
                        }
                        method.addRequestHeader(header);
                    }
                }

            }
            if (httpHeadersObj instanceof Map) {
                Map httpHeaders = (Map) httpHeadersObj;
                for (Iterator iterator = httpHeaders.entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    if (HTTPConstants.HEADER_USER_AGENT.equals(key)) {
                        isCustomUserAgentSet = true;
                    }
                    method.addRequestHeader(key, value);
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
                    Header[] headers = method.getRequestHeaders();

                    boolean headerAdded = false;
                    for (Header header : headers) {
                        if (header.getName() != null
                                && header.getName().equals(((Map.Entry) headerEntry).getKey())) {
                            headerAdded = true;
                            break;
                        }
                    }

                    if (!headerAdded) {
                        method.addRequestHeader(((Map.Entry) headerEntry).getKey().toString(),
                                ((Map.Entry) headerEntry).getValue().toString());
                    }
                }
            }
        }

        if (!isCustomUserAgentSet) {
            String userAgentString = getUserAgent(msgContext);
            method.setRequestHeader(HTTPConstants.HEADER_USER_AGENT, userAgentString);
        }

    }

    /**
     * Remove unwanted headers from the transport headers map of outgoing
     * request. These are headers which should be dictated by the transport and
     * not the user. We remove these as these may get copied from the request
     * messages
     * 
     * @param msgContext
     *            the Axis2 Message context from which these headers should be
     *            removed
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
