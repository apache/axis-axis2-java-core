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

package org.apache.axis2.transport.http;


import org.apache.axiom.mime.ContentType;
import org.apache.axiom.mime.Header;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.NamedValue;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.kernel.http.HTTPConstants;
import org.apache.axis2.kernel.MessageFormatter;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.HttpHeaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import javax.xml.namespace.QName;

import static org.apache.axis2.kernel.http.HTTPConstants.QNAME_HTTP_BAD_REQUEST;
import static org.apache.axis2.kernel.http.HTTPConstants.QNAME_HTTP_CONFLICT;
import static org.apache.axis2.kernel.http.HTTPConstants.QNAME_HTTP_FORBIDDEN;
import static org.apache.axis2.kernel.http.HTTPConstants.QNAME_HTTP_GONE;
import static org.apache.axis2.kernel.http.HTTPConstants.QNAME_HTTP_INTERNAL_SERVER_ERROR;
import static org.apache.axis2.kernel.http.HTTPConstants.QNAME_HTTP_METHOD_NOT_ALLOWED;
import static org.apache.axis2.kernel.http.HTTPConstants.QNAME_HTTP_NOT_ACCEPTABLE;
import static org.apache.axis2.kernel.http.HTTPConstants.QNAME_HTTP_NOT_FOUND;
import static org.apache.axis2.kernel.http.HTTPConstants.QNAME_HTTP_PROXY_AUTH_REQUIRED;
import static org.apache.axis2.kernel.http.HTTPConstants.QNAME_HTTP_REQUEST_TIMEOUT;
import static org.apache.axis2.kernel.http.HTTPConstants.QNAME_HTTP_UNAUTHORIZED;

//TODO - It better if we can define these method in a interface move these into AbstractHTTPSender and get rid of this class.
public abstract class HTTPSender {

    private static final Log log = LogFactory.getLog(HTTPSender.class);
    
    private boolean chunked = false;
    private String httpVersion = HTTPConstants.HEADER_PROTOCOL_11;
    protected TransportOutDescription proxyOutSetting = null;
    protected OMOutputFormat format = new OMOutputFormat();
    
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

    public void setFormat(OMOutputFormat format) {
        this.format = format;
    }

    /**
     * Start a new HTTP request.
     *
     * @param msgContext
     *            The MessageContext of the request message
     * @param methodName
     *            The HTTP method name
     * @param url
     *            The target URL
     * @param requestEntity
     *            The content of the request or {@code null} if the HTTP request shouldn't have any
     *            content (e.g. for {@code GET} requests)
     * @throws AxisFault
     *            Thrown in case an exception occurs
     */
    protected abstract Request createRequest(MessageContext msgContext, String methodName, URL url,
            AxisRequestEntity requestEntity) throws AxisFault;
    
    public void send(MessageContext msgContext, URL url, String soapActionString)
            throws IOException {

        // execute the HtttpMethodBase - a connection manager can be given for
        // handle multiple

        String httpMethod =
                (String) msgContext.getProperty(Constants.Configuration.HTTP_METHOD);
        if (httpMethod == null) {
            httpMethod = Constants.Configuration.HTTP_METHOD_POST;
        }

        MessageFormatter messageFormatter = MessageProcessorSelector
                .getMessageFormatter(msgContext);
        url = messageFormatter.getTargetAddress(msgContext, format, url);
        String contentType = messageFormatter.getContentType(msgContext, format, soapActionString);

        HTTPAuthenticator authenticator;
        Object obj = msgContext.getProperty(HTTPConstants.AUTHENTICATE);
        if (obj == null) {
            authenticator = null;
        } else {
            if (obj instanceof HTTPAuthenticator) {
                authenticator = (HTTPAuthenticator) obj;
            } else {
                throw new AxisFault("HttpTransportProperties.Authenticator class cast exception");
            }
        }

        AxisRequestEntity requestEntity;
        boolean gzip;
        if (Constants.Configuration.HTTP_METHOD_GET.equalsIgnoreCase(httpMethod)
                || Constants.Configuration.HTTP_METHOD_DELETE.equalsIgnoreCase(httpMethod)) {
            requestEntity = null;
            gzip = false;
        } else if (Constants.Configuration.HTTP_METHOD_POST.equalsIgnoreCase(httpMethod)
                || Constants.Configuration.HTTP_METHOD_PUT.equalsIgnoreCase(httpMethod)) {
            gzip = msgContext.isPropertyTrue(HTTPConstants.MC_GZIP_REQUEST);
            requestEntity = new AxisRequestEntity(messageFormatter, msgContext, format,
                    contentType, chunked, gzip, authenticator != null && authenticator.isAllowedRetry());
        } else {
            throw new AxisFault("Unsupported HTTP method " + httpMethod);
        }

        Request request = createRequest(msgContext, httpMethod, url, requestEntity);

        if (msgContext.getOptions() != null && msgContext.getOptions().isManageSession()) {
            // setting the cookie in the out path
            Object cookieString = msgContext.getProperty(HTTPConstants.COOKIE_STRING);

            if (cookieString != null) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(cookieString);
                request.setHeader(HTTPConstants.HEADER_COOKIE, buffer.toString());
            }
        }

        if (httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10)) {
            request.enableHTTP10();
        }
        
        request.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, contentType);

        String soapAction = messageFormatter.formatSOAPAction(msgContext, format, soapActionString);

        if (soapAction != null && !msgContext.isDoingREST()) {
            request.setHeader(HTTPConstants.HEADER_SOAP_ACTION, soapAction);
        }

        if (gzip) {
            request.setHeader(HTTPConstants.HEADER_CONTENT_ENCODING,
                    HTTPConstants.COMPRESSION_GZIP);
        }
        
        // set the custom headers, if available
        addCustomHeaders(msgContext, request);
        
        if (authenticator != null) {
            request.enableAuthentication(authenticator);
        }

        setTimeouts(msgContext, request);

        try {
            request.execute();
            boolean cleanup = true;
            try {
                int statusCode = request.getStatusCode();

                log.trace("Handling response - [content-type='" + contentType + "', statusCode=" + statusCode + "]");

                boolean processResponse;
                boolean fault;
                if (statusCode == HttpStatus.SC_ACCEPTED) {
                    processResponse = false;
                    fault = false;
                } else if (statusCode >= 200 && statusCode < 300) {
                    processResponse = true;
                    fault = false;
                } else if (statusCode >= 400 && statusCode <= 500) {

                    // if the response has a HTTP error code (401/404/500) but is *not* a SOAP response, handle it here
                    if (contentType != null && contentType.startsWith("text/html")) {
                        throw handleNonSoapError(request, statusCode);
                    } else {
                        processResponse = true;
                        fault = true;
                    }

                } else {
                    throw new AxisFault(Messages.getMessage("transportError",
                                                            String.valueOf(statusCode),
                                                            request.getStatusText()));
                }

                obtainHTTPHeaderInformation(request, msgContext);
                if (processResponse) {
                    OperationContext opContext = msgContext.getOperationContext();
                    MessageContext inMessageContext = opContext == null ? null
                            : opContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                    if (opContext != null) {
                        InputStream in = request.getResponseContent();
                        if (in != null) {
                            String contentEncoding = request.getResponseHeader(HTTPConstants.HEADER_CONTENT_ENCODING);
                            if (contentEncoding != null) {
                                if (contentEncoding.equalsIgnoreCase(HTTPConstants.COMPRESSION_GZIP)) {
                                    in = new GZIPInputStream(in);
                                    // If the content-encoding is identity we can basically ignore
                                    // it.
                                } else if (!"identity".equalsIgnoreCase(contentEncoding)) {
                                    throw new AxisFault("HTTP :" + "unsupported content-encoding of '"
                                                        + contentEncoding + "' found");
                                }
                            }
                            opContext.setProperty(MessageContext.TRANSPORT_IN, in);
                            // This implements the behavior of the HTTPClient 3.x based transport in
                            // Axis2 1.7: if AUTO_RELEASE_CONNECTION is enabled, we set the input stream
                            // in the message context, but we nevertheless release the connection.
                            // It is unclear in which situation this would actually be the right thing
                            // to do.
                            if (msgContext.isPropertyTrue(HTTPConstants.AUTO_RELEASE_CONNECTION)) {
                                log.debug("AUTO_RELEASE_CONNECTION enabled; are you sure that you really want that?");
                            } else {
                                cleanup = false;
                            }
                        }
                    }
                    if (fault) {
                        if (inMessageContext != null) {
                            inMessageContext.setProcessingFault(true);
                        }
                        if (Utils.isClientThreadNonBlockingPropertySet(msgContext)) {
                            throw new AxisFault(Messages.
                                    getMessage("transportError",
                                               String.valueOf(statusCode),
                                               request.getStatusText()));
                        }
                    }
                }
            } finally {
                if (cleanup) {
                    request.releaseConnection();
                }
            }
        } catch (IOException e) {
            log.info("Unable to send to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        }
    }

    private void addCustomHeaders(MessageContext msgContext, Request request) {
    
        boolean isCustomUserAgentSet = false;
        // set the custom headers, if available
        Object httpHeadersObj = msgContext.getProperty(HTTPConstants.HTTP_HEADERS);
        if (httpHeadersObj != null) {
            if (httpHeadersObj instanceof List) {
                List httpHeaders = (List) httpHeadersObj;
                for (int i = 0; i < httpHeaders.size(); i++) {
                    NamedValue nv = (NamedValue) httpHeaders.get(i);
                    if (nv != null) {
                        if (HTTPConstants.HEADER_USER_AGENT.equals(nv.getName())) {
                            isCustomUserAgentSet = true;
                        }
                        request.addHeader(nv.getName(), nv.getValue());
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
                    request.addHeader(key, value);
                }
            }
        } else {
            log.trace("addCustomHeaders() found no headers from HTTPConstants.HTTP_HEADERS");
	}	
    
        // we have to consider the TRANSPORT_HEADERS map as well
        Map transportHeaders = (Map) msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
        if (transportHeaders != null) {
            removeUnwantedHeaders(msgContext);
    
            Set headerEntries = transportHeaders.entrySet();
    
            for (Object headerEntry : headerEntries) {
                if (headerEntry instanceof Map.Entry) {
                    Header[] headers = request.getRequestHeaders();
    
                    boolean headerAdded = false;
                    for (Header header : headers) {
                        if (header.getName() != null
                                && header.getName().equals(((Map.Entry) headerEntry).getKey())) {
                            headerAdded = true;
                            break;
                        }
                    }
    
                    if (!headerAdded) {
                        request.addHeader(((Map.Entry) headerEntry).getKey().toString(),
                                ((Map.Entry) headerEntry).getValue().toString());
                    }
                }
            }
        } else {
            log.trace("addCustomHeaders() found no headers from MessageContext.TRANSPORT_HEADERS");
	}	
    
        if (!isCustomUserAgentSet) {
            String userAgentString = getUserAgent(msgContext);
            request.setHeader(HTTPConstants.HEADER_USER_AGENT, userAgentString);
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
            if (HttpHeaders.CONNECTION.equalsIgnoreCase(headerName)
                    || HttpHeaders.TRANSFER_ENCODING.equalsIgnoreCase(headerName)
                    || HttpHeaders.DATE.equalsIgnoreCase(headerName)
                    || HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(headerName)
                    || HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(headerName)) {
                log.trace("removeUnwantedHeaders() is removing header: " + headerName);
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

    /**
     * This is used to get the dynamically set time out values from the message
     * context. If the values are not available or invalid then the default
     * values or the values set by the configuration will be used
     * 
     * @param msgContext
     *            the active MessageContext
     * @param request
     *            request
     */
    private void setTimeouts(MessageContext msgContext, Request request) {
        // If the SO_TIMEOUT of CONNECTION_TIMEOUT is set by dynamically the
        // override the static config
        Integer tempSoTimeoutProperty = (Integer) msgContext.getProperty(HTTPConstants.SO_TIMEOUT);
        Integer tempConnTimeoutProperty = (Integer) msgContext
                .getProperty(HTTPConstants.CONNECTION_TIMEOUT);
        long timeout = msgContext.getOptions().getTimeOutInMilliSeconds();

        if (tempConnTimeoutProperty != null) {
            // timeout for initial connection
            request.setConnectionTimeout(tempConnTimeoutProperty);
        }

        if (tempSoTimeoutProperty != null) {
            // SO_TIMEOUT -- timeout for blocking reads
            request.setResponseTimeout(tempSoTimeoutProperty);
        } else {
            // set timeout in client
            if (timeout > 0) {
                request.setResponseTimeout((int) timeout);
            }
        }
    }

    private void obtainHTTPHeaderInformation(Request request, MessageContext msgContext) throws AxisFault {
        // Set RESPONSE properties onto the REQUEST message context. They will
        // need to be copied off the request context onto
        // the response context elsewhere, for example in the
        // OutInOperationClient.
        msgContext.setProperty(
                MessageContext.TRANSPORT_HEADERS,
                new CommonsTransportHeaders(request.getResponseHeaders()));
        msgContext.setProperty(
                HTTPConstants.MC_HTTP_STATUS_CODE,
                Integer.valueOf(request.getStatusCode()));
        
	// AXIS2-6046 , we supported a null content-type and null charSetEnc up until 1.7.9,
        // so let's support that here now
        String contentTypeString = request.getResponseHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        String charSetEnc = null;
        if (contentTypeString != null) {
            ContentType contentType;
            try {
                contentType = new ContentType(contentTypeString);
            } catch (ParseException ex) {
                throw AxisFault.makeFault(ex);
            }
            charSetEnc = contentType.getParameter(HTTPConstants.CHAR_SET_ENCODING);
        }

        if (contentTypeString == null) {
            log.debug("contentType and charSetEnc detected as null, proceeding anyway");
        }

        MessageContext inMessageContext = msgContext.getOperationContext().getMessageContext(
                WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        if (inMessageContext != null) {
            inMessageContext.setProperty(Constants.Configuration.CONTENT_TYPE, contentTypeString);
            inMessageContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);
        } else {
            // Transport details will be stored in a HashMap so that anybody
            // interested can
            // retrieve them
            Map<String,String> transportInfoMap = new HashMap<String,String>();
            transportInfoMap.put(Constants.Configuration.CONTENT_TYPE, contentTypeString);
            transportInfoMap.put(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);
            // the HashMap is stored in the outgoing message.
            msgContext.setProperty(Constants.Configuration.TRANSPORT_INFO_MAP, transportInfoMap);
        }

        Map<String,String> cookies = request.getCookies();
        if (cookies != null) {
            String customCookieId = (String) msgContext.getProperty(Constants.CUSTOM_COOKIE_ID);
            String cookieString = null;
            if (customCookieId != null) {
                cookieString = buildCookieString(cookies, customCookieId);
            }
            if (cookieString == null) {
                cookieString = buildCookieString(cookies, Constants.SESSION_COOKIE);
            }
            if (cookieString == null) {
                cookieString = buildCookieString(cookies, Constants.SESSION_COOKIE_JSESSIONID);
            }
            if (cookieString != null) {
                msgContext.getServiceContext().setProperty(HTTPConstants.COOKIE_STRING, cookieString);
            }
        }
    }

    private String buildCookieString(Map<String,String> cookies, String name) {
        String value = cookies.get(name);
        return value == null ? null : name + "=" + value;
    }

    /**
     * Handles non-SOAP HTTP error responses (e.g., 404, 500) by creating an AxisFault.
     * <p>
     *   If the response is `text/html`, it extracts the response body and includes it
     *   as fault details, wrapped within a CDATA block.
     * </p>
     *
     * @param request the HTTP request instance
     * @param statusCode the HTTP status code
     * @return AxisFault containing the error details
     */
    private AxisFault handleNonSoapError(final Request request, final int statusCode) {

        String responseContent = null;

        InputStream responseContentInputStream = null;
        try {
            responseContentInputStream = request.getResponseContent();
        } catch (final IOException ex) {
            // NO-OP
        }

        if (responseContentInputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseContentInputStream))) {
                responseContent = reader.lines().collect(Collectors.joining("\n")).trim();
            } catch (IOException e) {
                log.warn("Failed to read response content from HTTP error response", e);
            }
        }

        // Build and throw an AxisFault with the response content
        final String faultMessage =
            Messages.getMessage("transportError", String.valueOf(statusCode), responseContent);

        final QName faultQName = getFaultQNameForStatusCode(statusCode).orElse(null);

        final AxisFault fault = new AxisFault(faultMessage, faultQName);
        final OMElement faultDetail = createFaultDetailForNonSoapError(responseContent);
        fault.setDetail(faultDetail);

        return fault;

    }

    /**
     * Returns an appropriate QName for the given HTTP status code.
     *
     * @param statusCode the HTTP status code (e.g., 404, 500)
     * @return an Optional containing the QName if available, or an empty Optional if the status code is unsupported
     */
    private Optional<QName> getFaultQNameForStatusCode(int statusCode) {

        final QName faultQName;

        switch (statusCode) {
            case HttpStatus.SC_BAD_REQUEST:
                faultQName = QNAME_HTTP_BAD_REQUEST;
                break;
            case HttpStatus.SC_UNAUTHORIZED:
                faultQName = QNAME_HTTP_UNAUTHORIZED;
                break;
            case HttpStatus.SC_FORBIDDEN:
                faultQName = QNAME_HTTP_FORBIDDEN;
                break;
            case HttpStatus.SC_NOT_FOUND:
                faultQName = QNAME_HTTP_NOT_FOUND;
                break;
            case HttpStatus.SC_METHOD_NOT_ALLOWED:
                faultQName = QNAME_HTTP_METHOD_NOT_ALLOWED;
                break;
            case HttpStatus.SC_NOT_ACCEPTABLE:
                faultQName = QNAME_HTTP_NOT_ACCEPTABLE;
                break;
            case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                faultQName = QNAME_HTTP_PROXY_AUTH_REQUIRED;
                break;
            case HttpStatus.SC_REQUEST_TIMEOUT:
                faultQName = QNAME_HTTP_REQUEST_TIMEOUT;
                break;
            case HttpStatus.SC_CONFLICT:
                faultQName = QNAME_HTTP_CONFLICT;
                break;
            case HttpStatus.SC_GONE:
                faultQName = QNAME_HTTP_GONE;
                break;
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                faultQName = QNAME_HTTP_INTERNAL_SERVER_ERROR;
                break;
            default:
                faultQName = null;
                break;
        }

        return Optional.ofNullable(faultQName);

    }

    /**
     * Creates a fault detail element containing the response content.
     */
    private OMElement createFaultDetailForNonSoapError(String responseContent) {

        final OMElement faultDetail =
            OMAbstractFactory.getOMFactory().createOMElement(new QName("http://ws.apache.org/axis2", "Details"));

        final OMElement textNode =
            OMAbstractFactory.getOMFactory().createOMElement(new QName("http://ws.apache.org/axis2", "Text"));

        if (responseContent != null && !responseContent.isEmpty()) {
            textNode.setText(wrapResponseWithCDATA(responseContent));
        } else {
            textNode.setText(wrapResponseWithCDATA("The endpoint returned no response content."));
        }

        faultDetail.addChild(textNode);

        return faultDetail;

    }

    /**
     * Wraps the given HTML response content in a CDATA block to allow it to be added as Text in a fault-detail.
     *
     * @param responseContent the response content
     * @return the CDATA-wrapped response
     */
    private String wrapResponseWithCDATA(final String responseContent) {

        if (responseContent == null || responseContent.isEmpty()) {
            return "<![CDATA[The endpoint returned no response content.]]>";
        }

        // Replace closing CDATA sequences properly
        String safeContent = responseContent.replace("]]>", "]]]]><![CDATA[>").replace("\n", "&#10;");
        return "<![CDATA[" + safeContent + "]]>";

    }

}
