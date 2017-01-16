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


import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.util.MessageProcessorSelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URL;

//TODO - It better if we can define these method in a interface move these into AbstractHTTPSender and get rid of this class.
public abstract class HTTPSender extends AbstractHTTPSender {

    private static final Log log = LogFactory.getLog(HTTPSender.class);
    /**
     * Used to send a request via HTTP Get method
     *
     * @param msgContext        - The MessageContext of the message
     * @param url               - The target URL
     * @param soapActiionString - The soapAction string of the request
     * @throws AxisFault - Thrown in case an exception occurs
     */
    protected abstract Request prepareGet(MessageContext msgContext, URL url, String soapActiionString, MessageFormatter messageFormatter)
            throws AxisFault;
    
    /**
     * Used to send a request via HTTP Delete Method
     *
     * @param msgContext        - The MessageContext of the message
     * @param url               - The target URL
     * @param soapActiionString - The soapAction string of the request
     * @throws AxisFault - Thrown in case an exception occurs
     */
    protected abstract Request prepareDelete(MessageContext msgContext, URL url, String soapActiionString, MessageFormatter messageFormatter)
            throws AxisFault; 
    /**
     * Used to send a request via HTTP Post Method
     *
     * @param msgContext       - The MessageContext of the message
     * @param url              - The target URL
     * @param soapActionString - The soapAction string of the request
     * @throws AxisFault - Thrown in case an exception occurs
     */
    protected abstract Request preparePost(MessageContext msgContext, URL url,
                             String soapActionString, MessageFormatter messageFormatter) throws AxisFault;


    /**
     * Used to send a request via HTTP Put Method
     *
     * @param msgContext       - The MessageContext of the message
     * @param url              - The target URL
     * @param soapActionString - The soapAction string of the request
     * @throws AxisFault - Thrown in case an exception occurs
     */
    protected abstract Request preparePut(MessageContext msgContext, URL url,
                            String soapActionString, MessageFormatter messageFormatter) throws AxisFault;

    public void send(MessageContext msgContext, URL url, String soapActionString)
            throws IOException {

        // execute the HtttpMethodBase - a connection manager can be given for
        // handle multiple

        String httpMethod =
                (String) msgContext.getProperty(Constants.Configuration.HTTP_METHOD);

        MessageFormatter messageFormatter = MessageProcessorSelector
                .getMessageFormatter(msgContext);
        url = messageFormatter.getTargetAddress(msgContext, format, url);
        
        Request request = null;
        if ((httpMethod != null)) {

            if (Constants.Configuration.HTTP_METHOD_GET.equalsIgnoreCase(httpMethod)) {
                request = prepareGet(msgContext, url, soapActionString, messageFormatter);
            } else if (Constants.Configuration.HTTP_METHOD_DELETE.equalsIgnoreCase(httpMethod)) {
                request = prepareDelete(msgContext, url, soapActionString, messageFormatter);
            } else if (Constants.Configuration.HTTP_METHOD_PUT.equalsIgnoreCase(httpMethod)) {
                request = preparePut(msgContext, url, soapActionString, messageFormatter);
            }
        }

        if (request == null) {
            request = preparePost(msgContext, url, soapActionString, messageFormatter);
        }
        
        request.setHeader(HTTPConstants.HEADER_CONTENT_TYPE,
                messageFormatter.getContentType(msgContext, format, soapActionString));

        String soapAction = messageFormatter.formatSOAPAction(msgContext, format, soapActionString);

        if (soapAction != null && !msgContext.isDoingREST()) {
            request.setHeader(HTTPConstants.HEADER_SOAP_ACTION, soapAction);
        }

        request.execute();
    }   

    public AxisRequestEntity buildRequestEntity(MessageFormatter messageFormatter, MessageContext msgContext,
            String soapActionString) {
        return new AxisRequestEntity(messageFormatter, msgContext, format,
                soapActionString, chunked, isAllowedRetry);
    }

    /**
     * Used to determine the family of HTTP status codes to which the given code
     * belongs.
     * 
     * @param statusCode
     *            - The HTTP status code
     */
    protected HTTPStatusCodeFamily getHTTPStatusCodeFamily(int statusCode) {
        switch (statusCode / 100) {
        case 1:
            return HTTPStatusCodeFamily.INFORMATIONAL;
        case 2:
            return HTTPStatusCodeFamily.SUCCESSFUL;
        case 3:
            return HTTPStatusCodeFamily.REDIRECTION;
        case 4:
            return HTTPStatusCodeFamily.CLIENT_ERROR;
        case 5:
            return HTTPStatusCodeFamily.SERVER_ERROR;
        default:
            return HTTPStatusCodeFamily.OTHER;
        }
    }
    /**
     * The set of HTTP status code families.
     */
    protected enum HTTPStatusCodeFamily {
        INFORMATIONAL, SUCCESSFUL, REDIRECTION, CLIENT_ERROR, SERVER_ERROR, OTHER
    }

}
