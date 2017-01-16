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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.http.AxisRequestEntity;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPSender;
import org.apache.axis2.transport.http.Request;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class HTTPSenderImpl extends HTTPSender {

    private static final Log log = LogFactory.getLog(HTTPSenderImpl.class);

    boolean isChunked() {
        return chunked;
    }

    String getHttpVersion() {
        return httpVersion;
    }

    // TODO: this shouldn't be here (see AXIS2-4021)
    void setAllowedRetry(boolean isAllowedRetry) {
        this.isAllowedRetry = isAllowedRetry;
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

    protected boolean isAuthenticationEnabled(MessageContext msgCtx) {
        return (msgCtx.getProperty(HTTPConstants.AUTHENTICATE) != null);
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

}
