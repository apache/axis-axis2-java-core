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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class SOAPOverHTTPSender extends AbstractHTTPSender {


    public void send(MessageContext msgContext, OMElement dataout, URL url, String soapActionString)
            throws MalformedURLException, AxisFault, IOException {

        // execute the HtttpMethodBase - a connection manager can be given for
        // handle multiple
        HttpClient httpClient = getHttpClient(msgContext);

        String charEncoding =
                (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

        if (charEncoding == null) {
            charEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        MessageFormatter messageFormatter = TransportUtils.getMessageFormatter(
                msgContext);
        url = messageFormatter.getTargetAddress(msgContext, format, url);
        // Check whther the url has httpLocation
        String urlString = url.toString();
        int separator = urlString.indexOf('{');
        if (separator > 0) {
            String path = urlString.substring(0, separator - 1);
            String query = urlString.substring(separator - 1);
            String replacedQuery;
            replacedQuery = applyURITemplating(msgContext, query, false);
            url = new URL(path + replacedQuery);
        }
        PostMethod postMethod = new PostMethod();
        postMethod.setPath(url.getPath());
        postMethod.setQueryString(url.getQuery());
        postMethod.setPath(url.getPath());

        if (isAuthenticationEnabled(msgContext)) {
            postMethod.setDoAuthentication(true);
        }

        postMethod.setRequestEntity(new AxisRequestEntity(messageFormatter,
                                                          msgContext, format, soapActionString,
                                                          chunked, isAllowedRetry));
        if (!httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10) && chunked) {
            postMethod.setContentChunked(true);
        }

        String soapAction = messageFormatter.formatSOAPAction(msgContext, format, soapActionString);
        if (soapAction != null) {
            postMethod.setRequestHeader(HTTPConstants.HEADER_SOAP_ACTION, soapAction);
        }

        //setting the cookie in the out path
        Object cookieString = msgContext.getProperty(HTTPConstants.COOKIE_STRING);
        if (cookieString != null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(Constants.SESSION_COOKIE);
            buffer.append("=");
            buffer.append(cookieString);
            postMethod.setRequestHeader(HTTPConstants.HEADER_COOKIE, buffer.toString());
        }

        postMethod.setRequestHeader(HTTPConstants.HEADER_HOST, url.getHost());

        if (httpVersion != null) {
            if (httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10)) {
                httpClient.getParams().setVersion(HttpVersion.HTTP_1_0);
            } else {
                postMethod.setRequestHeader(HTTPConstants.HEADER_EXPECT,
                                            HTTPConstants.HEADER_EXPECT_100_Continue);
            }
        }

        // set timeout in client
        long timeout = msgContext.getOptions().getTimeOutInMilliSeconds();
        if (timeout != 0) {
            httpClient.getParams().setSoTimeout((int) timeout);
        }

        /*
         *   main excecution takes place..
         */
        executeMethod(httpClient, msgContext, url, postMethod);

        /*
         *   Execution is over
         */
        if (postMethod.getStatusCode() == HttpStatus.SC_OK) {
            processResponse(postMethod, msgContext);
            return;
        } else if (postMethod.getStatusCode() == HttpStatus.SC_ACCEPTED) {
            return;
        } else if (postMethod.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            Header contentTypeHeader =
                    postMethod.getResponseHeader(HTTPConstants.HEADER_CONTENT_TYPE);

            if (contentTypeHeader != null) {
                String value = contentTypeHeader.getValue();

                if ((value.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) >= 0)
                        || (value.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) >= 0)) {
                    processResponse(postMethod, msgContext);

                    return;
                }
            }
        } else {
            throw new AxisFault(Messages.getMessage("httpTransportError",
                                                    String.valueOf(postMethod.getStatusCode()),
                                                    postMethod.getStatusText()),
                                SOAP12Constants.FAULT_CODE_SENDER);
        }

        throw new AxisFault(Messages.getMessage("transportError",
                                                String.valueOf(postMethod.getStatusCode()),
                                                postMethod.getResponseBodyAsString()));
    }
}
