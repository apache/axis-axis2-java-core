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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public abstract class AbstractHTTPSender {
    protected static final String ANONYMOUS = "anonymous";
    protected static final String PROXY_HOST_NAME = "proxy_host";
    protected static final String PROXY_PORT = "proxy_port";
    protected boolean chunked = false;
    protected String httpVersion = HTTPConstants.HEADER_PROTOCOL_11;
    private static final Log log = LogFactory.getLog(AbstractHTTPSender.class);
    int soTimeout = HTTPConstants.DEFAULT_SO_TIMEOUT;
    
    protected static final String PROTOCOL_HTTP = "http";
    protected static final String PROTOCOL_HTTPS = "https";
    
    /**
     * proxydiscription
     */
    protected TransportOutDescription proxyOutSetting = null;
    protected OMOutputFormat format = new OMOutputFormat();
    int connectionTimeout = HTTPConstants.DEFAULT_CONNECTION_TIMEOUT;

    /**
     * isAllowedRetry will be using to check where the
     * retry should be allowed or not.
     */
    protected boolean isAllowedRetry = false;

    public void setChunked(boolean chunked) {
        this.chunked = chunked;
    }

    public void setHttpVersion(String version) throws AxisFault {
        if (version != null) {
            if (HTTPConstants.HEADER_PROTOCOL_11.equals(version)) {
                this.httpVersion = HTTPConstants.HEADER_PROTOCOL_11;
            } else if (HTTPConstants.HEADER_PROTOCOL_10.equals(version)) {
                this.httpVersion = HTTPConstants.HEADER_PROTOCOL_10;
                // chunked is not possible with HTTP/1.0
                this.chunked = false;
            } else {
                throw new AxisFault(
                        "Parameter " + HTTPConstants.PROTOCOL_VERSION
                                + " Can have values only HTTP/1.0 or HTTP/1.1");
            }
        }
    }

    /**
     * Helper method to Proxy and NTLM authentication
     *
     * @param client
     * @param proxySetting
     * @param config
     */
    protected void configProxyAuthentication(HttpClient client,
                                             TransportOutDescription proxySetting,
                                             HostConfiguration config,
                                             MessageContext msgCtx)
            throws AxisFault {
        Parameter proxyParam = proxySetting.getParameter(HTTPConstants.PROXY);
        String usrName;
        String domain;
        String passwd;
        Credentials proxyCred = null;
        String proxyHostName = null;
        int proxyPort = -1;

        if (proxyParam != null) {
            String value = (String) proxyParam.getValue();
            String split[] = value.split(":");

            // values being hard coded due best practise
            usrName = split[0];
            domain = split[1];
            passwd = split[2];

            OMElement proxyParamElement = proxyParam.getParameterElement();
            Iterator ite = proxyParamElement.getAllAttributes();

            while (ite.hasNext()) {
                OMAttribute att = (OMAttribute) ite.next();

                if (att.getLocalName().equalsIgnoreCase(PROXY_HOST_NAME)) {
                    proxyHostName = att.getAttributeValue();
                }

                if (att.getLocalName().equalsIgnoreCase(PROXY_PORT)) {
                    proxyPort = Integer.parseInt(att.getAttributeValue());
                }
            }

            if (domain.length() == 0 || domain.equals(ANONYMOUS)) {
                if (usrName.equals(ANONYMOUS) && passwd.equals(ANONYMOUS)) {
                    proxyCred = new UsernamePasswordCredentials("", "");
                } else {
                    proxyCred = new UsernamePasswordCredentials(usrName,
                                                                passwd);    // proxy
                }
            } else {
                proxyCred = new NTCredentials(usrName, passwd, proxyHostName,
                                              domain);    // NTLM authentication with additionals prams
            }
        }

        HttpTransportProperties.ProxyProperties proxyProperties =
                (HttpTransportProperties.ProxyProperties) msgCtx
                        .getProperty(HTTPConstants.PROXY);

        if (proxyProperties != null) {
            if (proxyProperties.getProxyPort() != -1) {
                proxyPort = proxyProperties.getProxyPort();
            }

            proxyHostName = proxyProperties.getProxyHostName();
            if (proxyHostName == null
                    || proxyHostName.length() == 0) {
                throw new AxisFault("Proxy Name is not valid");
            }

            if (proxyProperties.getUserName().equals(ANONYMOUS)
                    || proxyProperties.getPassWord().equals(ANONYMOUS)) {
                proxyCred = new UsernamePasswordCredentials("", "");
            }
            if (!proxyProperties.getUserName().equals(ANONYMOUS) &&
                    !proxyProperties.getPassWord().equals(ANONYMOUS)) {
                proxyCred = new UsernamePasswordCredentials(
                        proxyProperties.getUserName().trim(),
                        proxyProperties
                                .getPassWord().trim()); // Basic Authentication
            }
            if (!proxyProperties.getDomain().equals(ANONYMOUS)) {
                if (!proxyProperties.getUserName().equals(ANONYMOUS) &&
                        !proxyProperties.getPassWord().equals(ANONYMOUS) &&
                        !proxyProperties.getDomain().equals(ANONYMOUS)) {
                    proxyCred = new NTCredentials(
                            proxyProperties.getUserName().trim(),
                            proxyProperties.getPassWord().trim(), proxyHostName,
                            proxyProperties
                                    .getDomain().trim()); // NTLM Authentication
                }
            }
        }

        client.getState().setProxyCredentials(AuthScope.ANY, proxyCred);
        config.setProxy(proxyHostName, proxyPort);
        config.setHost(proxyHostName);
    }

    /**
     * Collect the HTTP header information and set them in the message context
     *
     * @param method
     * @param msgContext
     */
    protected void obtainHTTPHeaderInformation(HttpMethodBase method,
                                               MessageContext msgContext) throws AxisFault {
        Map transportHeaders = new CommonsTransportHeaders(method.getResponseHeaders());
        msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, transportHeaders);
        Header header = method.getResponseHeader(HTTPConstants.HEADER_CONTENT_TYPE);

        if (header != null) {
            HeaderElement[] headers = header.getElements();
            MessageContext inMessageContext = msgContext.getOperationContext().getMessageContext(
                    WSDLConstants.MESSAGE_LABEL_IN_VALUE);

            Object contentType = header.getValue();
            Object charSetEnc = null;
            
            for (int i = 0; i < headers.length; i++) {
                NameValuePair charsetEnc = headers[i].getParameterByName(
                        HTTPConstants.CHAR_SET_ENCODING);
                if (charsetEnc != null) {
                	charSetEnc = charsetEnc.getValue();    
                }
            }
            
            if (inMessageContext != null) {
                inMessageContext
                        .setProperty(Constants.Configuration.CONTENT_TYPE, contentType);
                inMessageContext
                	.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);
            } else {
            	
            	//Transport details will be stored in a HashMap so that anybody interested can retriece them
            	HashMap transportInfoMap = new HashMap ();
            	transportInfoMap.put(Constants.Configuration.CONTENT_TYPE, contentType);
            	transportInfoMap.put(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);
            	
            	//the HashMap is stored in the outgoing message.
            	msgContext.setProperty(Constants.Configuration.TRANSPORT_INFO_MAP, transportInfoMap);
            }
            
        }

        String sessionCookie = null;
        // Process old style headers first
        Header[] cookieHeaders = method.getResponseHeaders(HTTPConstants.HEADER_SET_COOKIE);
        for (int i = 0; i < cookieHeaders.length; i++) {
            HeaderElement[] elements = cookieHeaders[i].getElements();
            for (int e = 0; e < elements.length; e++) {
                HeaderElement element = elements[e];
                if (Constants.SESSION_COOKIE.equalsIgnoreCase(element.getName()) ||
                        Constants.SESSION_COOKIE_JSESSIONID.equalsIgnoreCase(element.getName())) {
                    sessionCookie = element.getValue();
                }
            }
        }
        // Overwrite old style cookies with new style ones if present
        cookieHeaders = method.getResponseHeaders(HTTPConstants.HEADER_SET_COOKIE2);
        for (int i = 0; i < cookieHeaders.length; i++) {
            HeaderElement[] elements = cookieHeaders[i].getElements();
            for (int e = 0; e < elements.length; e++) {
                HeaderElement element = elements[e];
                if (Constants.SESSION_COOKIE.equalsIgnoreCase(element.getName()) ||
                        Constants.SESSION_COOKIE_JSESSIONID.equalsIgnoreCase(element.getName())) {
                    sessionCookie = element.getValue();
                }
            }
        }

        if (sessionCookie != null) {
            msgContext.getServiceContext().setProperty(HTTPConstants.COOKIE_STRING, sessionCookie);
        }
    }

    protected void processResponse(HttpMethodBase httpMethod,
                                   MessageContext msgContext)
            throws IOException {
        obtainHTTPHeaderInformation(httpMethod, msgContext);

        InputStream in = httpMethod.getResponseBodyAsStream();
        if (in == null) {
            throw new AxisFault(Messages.getMessage("canNotBeNull", "InputStream"));
        }
        Header contentEncoding =
                httpMethod.getResponseHeader(HTTPConstants.HEADER_CONTENT_ENCODING);
        if (contentEncoding != null) {
            if (contentEncoding.getValue().
                    equalsIgnoreCase(HTTPConstants.COMPRESSION_GZIP)) {
                in = new GZIPInputStream(in);
            } else {
                throw new AxisFault("HTTP :" + "unsupported content-encoding of '"
                        + contentEncoding.getValue() + "' found");
            }
        }

        if (msgContext.getOperationContext() != null) {
            msgContext.getOperationContext()
                    .setProperty(MessageContext.TRANSPORT_IN, in);
        }
    }

    public abstract void send(MessageContext msgContext, URL url, String soapActionString)
            throws AxisFault, IOException;

    /**
     * getting host configuration to support standard http/s, proxy and NTLM support
     */
    protected HostConfiguration getHostConfiguration(HttpClient client,
                                                     MessageContext msgCtx,
                                                     URL targetURL)
            throws AxisFault {
        boolean isHostProxy = isProxyListed(msgCtx);    // list the proxy


        boolean isAuthenticationEnabled = isAuthenticationEnabled(msgCtx);
        int port = targetURL.getPort();

        String protocal = targetURL.getProtocol();
        if (port == -1) {
            if (PROTOCOL_HTTP.equals(protocal)) {
                port = 80;
            } else if (PROTOCOL_HTTPS.equals(protocal)){
                port = 443;                
            }
            
        }

        // to see the host is a proxy and in the proxy list - available in axis2.xml
        HostConfiguration config = new HostConfiguration();

        if (isAuthenticationEnabled) {
            // premtive authentication Basic or NTLM
            this.setAuthenticationInfo(client, msgCtx, config, targetURL);
        }

        // proxy configuration
        if (!isHostProxy) {
            config.setHost(targetURL.getHost(), port, targetURL.getProtocol());
        } else {
            this.configProxyAuthentication(client, proxyOutSetting, config,
                                           msgCtx);
        }

        return config;
    }

    protected boolean isAuthenticationEnabled(MessageContext msgCtx) {
        return (msgCtx.getProperty(HTTPConstants.AUTHENTICATE) != null);
    }

    /*
    This will handle server Authentication, It could be either NTLM, Digest or Basic Authentication.
    Apart from that user can change the priory or add a custom authentication scheme.
    */
    protected void setAuthenticationInfo(HttpClient agent,
                                         MessageContext msgCtx,
                                         HostConfiguration config,
                                         URL targetURL) throws AxisFault {
        config.setHost(targetURL.getHost(), targetURL.getPort(),
                       targetURL.getProtocol());

        HttpTransportProperties.Authenticator authenticator;
        Object obj = msgCtx.getProperty(HTTPConstants.AUTHENTICATE);
        if (obj != null) {
            if (obj instanceof HttpTransportProperties.Authenticator) {
                authenticator = (HttpTransportProperties.Authenticator) obj;

                String username = authenticator.getUsername();
                String password = authenticator.getPassword();
                String host = authenticator.getHost();
                String domain = authenticator.getDomain();

                int port = authenticator.getPort();
                String realm = authenticator.getRealm();

                /* If retrying is available set it first */
                isAllowedRetry = authenticator.isAllowedRetry();

                Credentials creds;

                agent.getParams()
                        .setAuthenticationPreemptive(authenticator.getPreemptiveAuthentication());

                if (host != null) {
                    if (domain != null) {
                        /*Credentials for NTLM Authentication*/
                        creds = new NTCredentials(username, password, host, domain);
                    } else {
                        /*Credentials for Digest and Basic Authentication*/
                        creds = new UsernamePasswordCredentials(username, password);
                    }
                    agent.getState().setCredentials(new AuthScope(host, port, realm), creds);
                } else {
                    if (domain != null) {
                        /*Credentials for NTLM Authentication when host is ANY_HOST*/
                        creds = new NTCredentials(username, password, AuthScope.ANY_HOST, domain);
                        agent.getState().setCredentials(
                                new AuthScope(AuthScope.ANY_HOST, port, realm), creds);
                    } else {
                        /*Credentials only for Digest and Basic Authentication*/
                        creds = new UsernamePasswordCredentials(username, password);
                        agent.getState().setCredentials(new AuthScope(AuthScope.ANY), creds);
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
                        if (HttpTransportProperties.Authenticator.BASIC.equals(scheme)) {
                            authPrefs.add(AuthPolicy.BASIC);
                        } else if (HttpTransportProperties.Authenticator.NTLM.equals(scheme)) {
                            authPrefs.add(AuthPolicy.NTLM);
                        } else if (HttpTransportProperties.Authenticator.DIGEST.equals(scheme)) {
                            authPrefs.add(AuthPolicy.DIGEST);
                        }
                    }
                    agent.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY,
                                                   authPrefs);
                }

            } else {
                throw new AxisFault("HttpTransportProperties.Authenticator class cast exception");
            }
        }

    }

    /**
     * This is used to get the dynamically set time out values from the
     * message context. If the values are not available or invalid then
     * teh default values or the values set by teh configuration will be used
     *
     * @param msgContext
     */
    protected void getTimeoutValues(MessageContext msgContext) {
        try {

            // If the SO_TIMEOUT of CONNECTION_TIMEOUT is set by dynamically the
            // override the static config
            Integer tempSoTimeoutProperty =
                    (Integer) msgContext.getProperty(HTTPConstants.SO_TIMEOUT);
            Integer tempConnTimeoutProperty =
                    (Integer) msgContext
                            .getProperty(HTTPConstants.CONNECTION_TIMEOUT);

            if (tempSoTimeoutProperty != null) {
                soTimeout = tempSoTimeoutProperty.intValue();
            }

            if (tempConnTimeoutProperty != null) {
                connectionTimeout = tempConnTimeoutProperty.intValue();
            }
        } catch (NumberFormatException nfe) {

            // If there's a problem log it and use the default values
            log.error("Invalid timeout value format: not a number", nfe);
        }
    }

    private boolean isProxyListed(MessageContext msgCtx) throws AxisFault {
        boolean returnValue = false;
        Parameter par = null;

        proxyOutSetting = msgCtx.getConfigurationContext()
                .getAxisConfiguration().getTransportOut(Constants.TRANSPORT_HTTP);

        if (proxyOutSetting != null) {
            par = proxyOutSetting.getParameter(HTTPConstants.PROXY);
        }

        OMElement hostElement = null;

        if (par != null) {
            hostElement = par.getParameterElement();
        }

        if (hostElement != null) {
            Iterator ite = hostElement.getAllAttributes();

            while (ite.hasNext()) {
                OMAttribute attribute = (OMAttribute) ite.next();

                if (attribute.getLocalName().equalsIgnoreCase(PROXY_HOST_NAME)) {
                    returnValue = true;
                }
            }
        }

        HttpTransportProperties.ProxyProperties proxyProperties;

        if ((proxyProperties =
                (HttpTransportProperties.ProxyProperties) msgCtx.getProperty(
                        HTTPConstants.PROXY)) != null) {
            if (proxyProperties.getProxyHostName() != null) {
                returnValue = true;
            }
        }

        return returnValue;
    }

    public void setFormat(OMOutputFormat format) {
        this.format = format;
    }

    protected HttpClient getHttpClient(MessageContext msgContext) {
        HttpClient httpClient;
        Object reuse = msgContext.getOptions().getProperty(HTTPConstants.REUSE_HTTP_CLIENT);
        if (reuse != null && JavaUtils.isTrueExplicitly(reuse)) {
            httpClient = (HttpClient) msgContext.getConfigurationContext()
                    .getProperty(HTTPConstants.CACHED_HTTP_CLIENT);
            if (httpClient == null) {
                MultiThreadedHttpConnectionManager connectionManager =
                        new MultiThreadedHttpConnectionManager();
                httpClient = new HttpClient(connectionManager);
                msgContext.getConfigurationContext()
                        .setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
            }
        } else {
            httpClient = new HttpClient();
        }

        // Get the timeout values set in the runtime
        getTimeoutValues(msgContext);

        // SO_TIMEOUT -- timeout for blocking reads
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeout);

        // timeout for initial connection
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(connectionTimeout);
        return httpClient;
    }

    protected void executeMethod(HttpClient httpClient, MessageContext msgContext, URL url,
                                 HttpMethod method) throws IOException {
        HostConfiguration config = this.getHostConfiguration(httpClient, msgContext, url);
        msgContext.setProperty(HTTPConstants.HTTP_METHOD, method);

        // set the custom headers, if available
        addCustomHeaders(method, msgContext);

        // add compression headers if needed
        if (Utils.isExplicitlyTrue(msgContext, HTTPConstants.MC_ACCEPT_GZIP)) {
            method.addRequestHeader(HTTPConstants.HEADER_ACCEPT_ENCODING,
                                    HTTPConstants.COMPRESSION_GZIP);
        }

        if (Utils.isExplicitlyTrue(msgContext, HTTPConstants.MC_GZIP_REQUEST)) {
            method.addRequestHeader(HTTPConstants.HEADER_CONTENT_ENCODING,
                                    HTTPConstants.COMPRESSION_GZIP);
        }

        httpClient.executeMethod(config, method);
    }

    public void addCustomHeaders(HttpMethod method, MessageContext msgContext) {

        boolean isCustomUserAgentSet = false;
        // set the custom headers, if available
        Object httpHeadersObj = msgContext.getProperty(HTTPConstants.HTTP_HEADERS);
        if (httpHeadersObj != null && httpHeadersObj instanceof ArrayList) {
            ArrayList httpHeaders = (ArrayList) httpHeadersObj;
            Header header;
            for (int i = 0; i < httpHeaders.size(); i++) {
                header = (Header) httpHeaders.get(i);
                if (HTTPConstants.HEADER_USER_AGENT.equals(header.getName())) {
                    isCustomUserAgentSet = true;
                }
                method.addRequestHeader(header);
            }

        }

        if (!isCustomUserAgentSet) {
            String userAgentString = getUserAgent(msgContext);
            method.setRequestHeader(HTTPConstants.HEADER_USER_AGENT, userAgentString);
        }

    }

    private String getUserAgent(MessageContext messageContext) {
        String userAgentString = "Axis2";
        boolean locked = false;
        if (messageContext.getParameter(HTTPConstants.USER_AGENT) != null) {
            OMElement userAgentElement =
                    messageContext.getParameter(HTTPConstants.USER_AGENT).getParameterElement();
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
