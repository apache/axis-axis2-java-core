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


import org.apache.axiom.mime.Header;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.NamedValue;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

//TODO - It better if we can define these method in a interface move these into AbstractHTTPSender and get rid of this class.
public abstract class HTTPSender extends AbstractHTTPSender {

    private static final Log log = LogFactory.getLog(HTTPSender.class);
    
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

        request.execute();
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
