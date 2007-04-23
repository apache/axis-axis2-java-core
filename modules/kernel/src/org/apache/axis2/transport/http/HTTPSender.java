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
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class HTTPSender extends AbstractHTTPSender {

    private static final Log log = LogFactory.getLog(HTTPSender.class);

    public void send(MessageContext msgContext, URL url, String soapActionString)
            throws MalformedURLException, AxisFault, IOException {

        // execute the HtttpMethodBase - a connection manager can be given for
        // handle multiple

        String httpMethod =
                (String) msgContext.getProperty(Constants.Configuration.HTTP_METHOD);

        if ((httpMethod != null)) {

            if (Constants.Configuration.HTTP_METHOD_GET.equalsIgnoreCase(httpMethod)) {
                this.sendViaGet(msgContext, url, soapActionString);

                return;
            } else if (Constants.Configuration.HTTP_METHOD_DELETE.equalsIgnoreCase(httpMethod)) {
                this.sendViaDelete(msgContext, url, soapActionString);

                return;
            } else if (Constants.Configuration.HTTP_METHOD_PUT.equalsIgnoreCase(httpMethod)) {
                this.sendViaPut(msgContext, url, soapActionString);

                return;
            }
        }

        this.sendViaPost(msgContext, url, soapActionString);
    }

    /**
     * Used to send a request via HTTP Get method
     *
     * @param msgContext        - The MessageContext of the message
     * @param url               - The target URL
     * @param soapActiionString - The soapAction string of the request
     * @throws AxisFault - Thrown in case an exception occurs
     */
    private void sendViaGet(MessageContext msgContext, URL url, String soapActiionString)
            throws AxisFault {

        GetMethod getMethod = new GetMethod();
        HttpClient httpClient = getHttpClient(msgContext);
        MessageFormatter messageFormatter =
                populateCommonProperties(msgContext, url, getMethod, httpClient, soapActiionString);

        // Need to have this here because we can have soap action when using the soap response MEP
        String soapAction =
                messageFormatter.formatSOAPAction(msgContext, format, soapActiionString);

        if (soapAction != null) {
            getMethod.setRequestHeader(HTTPConstants.HEADER_SOAP_ACTION, soapAction);
        }
        try {
            executeMethod(httpClient, msgContext, url, getMethod);
            handleResponse(msgContext, getMethod);
        } catch (IOException e) {
            log.info("Unable to sendViaGet to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        }


    }

    /**
     * Used to send a request via HTTP Delete Method
     *
     * @param msgContext        - The MessageContext of the message
     * @param url               - The target URL
     * @param soapActiionString - The soapAction string of the request
     * @throws AxisFault - Thrown in case an exception occurs
     */
    private void sendViaDelete(MessageContext msgContext, URL url, String soapActiionString)
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
        }

    }

    /**
     * Used to send a request via HTTP Post Method
     *
     * @param msgContext       - The MessageContext of the message
     * @param url              - The target URL
     * @param soapActionString - The soapAction string of the request
     * @throws AxisFault - Thrown in case an exception occurs
     */
    private void sendViaPost(MessageContext msgContext, URL url,
                             String soapActionString) throws AxisFault {


        HttpClient httpClient = getHttpClient(msgContext);

        String charEncoding =
                (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

        if (charEncoding == null) {
            charEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        PostMethod postMethod = new PostMethod();
        MessageFormatter messageFormatter =
                populateCommonProperties(msgContext, url, postMethod, httpClient, soapActionString);

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

        /*
         *   main excecution takes place..
         */
        try {
            executeMethod(httpClient, msgContext, url, postMethod);
            handleResponse(msgContext, postMethod);
        } catch (IOException e) {
            log.info("Unable to sendViaPost to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        }

    }

    /**
     * Used to send a request via HTTP Put Method
     *
     * @param msgContext       - The MessageContext of the message
     * @param url              - The target URL
     * @param soapActionString - The soapAction string of the request
     * @throws AxisFault - Thrown in case an exception occurs
     */
    private void sendViaPut(MessageContext msgContext, URL url,
                            String soapActionString) throws AxisFault {


        HttpClient httpClient = getHttpClient(msgContext);

        String charEncoding =
                (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

        if (charEncoding == null) {
            charEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        // TODO - Do something with charEncoding???

        PutMethod putMethod = new PutMethod();
        MessageFormatter messageFormatter =
                populateCommonProperties(msgContext, url, putMethod, httpClient, soapActionString);

        putMethod.setRequestEntity(new AxisRequestEntity(messageFormatter,
                                                         msgContext, format, soapActionString,
                                                         chunked, isAllowedRetry));

        if (!httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10) && chunked) {
            putMethod.setContentChunked(true);
        }

        String soapAction = messageFormatter.formatSOAPAction(msgContext, format, soapActionString);
        if (soapAction != null) {
            putMethod.setRequestHeader(HTTPConstants.HEADER_SOAP_ACTION, soapAction);
        }

        /*
         *   main excecution takes place..
         */
        try {
            executeMethod(httpClient, msgContext, url, putMethod);
            handleResponse(msgContext, putMethod);
        } catch (IOException e) {
            log.info("Unable to sendViaPut to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
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
     * @return MessageFormatter - The messageFormatter for the relavent request message
     * @throws AxisFault - Thrown in case an exception occurs
     */
    private MessageFormatter populateCommonProperties(MessageContext msgContext, URL url,
                                                      HttpMethodBase httpMethod,
                                                      HttpClient httpClient,
                                                      String soapActionString)
            throws AxisFault {

        if (isAuthenticationEnabled(msgContext)) {
            httpMethod.setDoAuthentication(true);
        }

        MessageFormatter messageFormatter = TransportUtils.getMessageFormatter(
                msgContext);

        url = messageFormatter.getTargetAddress(msgContext, format, url);

        httpMethod.setPath(url.getPath());

        httpMethod.setQueryString(url.getQuery());

        httpMethod.setRequestHeader(HTTPConstants.HEADER_CONTENT_TYPE,
                                    messageFormatter.getContentType(msgContext, format,
                                                                    soapActionString));

        httpMethod.setRequestHeader(HTTPConstants.HEADER_HOST, url.getHost());

        //setting the cookie in the out path
        Object cookieString = msgContext.getProperty(HTTPConstants.COOKIE_STRING);

        if (cookieString != null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(Constants.SESSION_COOKIE_JSESSIONID);
            buffer.append("=");
            buffer.append(cookieString);
            httpMethod.setRequestHeader(HTTPConstants.HEADER_COOKIE, buffer.toString());
        }

        if (httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10)) {
            httpClient.getParams().setVersion(HttpVersion.HTTP_1_0);
        }

        // set timeout in client
        long timeout = msgContext.getOptions().getTimeOutInMilliSeconds();

        if (timeout != 0) {
            httpClient.getParams().setSoTimeout((int) timeout);
        }

        return messageFormatter;
    }

    /**
     * Used to handle the HTTP Response
     *
     * @param msgContext - The MessageContext of the message
     * @param method     - The HTTP method used
     * @throws IOException - Thrown in case an exception occurs
     */
    private void handleResponse(MessageContext msgContext,
                                HttpMethodBase method) throws IOException {

        if (method.getStatusCode() == HttpStatus.SC_OK) {
            processResponse(method, msgContext);
        } else if (method.getStatusCode() == HttpStatus.SC_ACCEPTED) {
        } else if (method.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            Header contenttypeHeader =
                    method.getResponseHeader(HTTPConstants.HEADER_CONTENT_TYPE);
            String value = null;
            if (contenttypeHeader != null) {
                value = contenttypeHeader.getValue();
            }

            if (value != null) {

                processResponse(method, msgContext);
            }
        } else {
            throw new AxisFault(Messages.getMessage("transportError",
                                                    String.valueOf(method.getStatusCode()),
                                                    method.getStatusText()));
        }
    }
}
