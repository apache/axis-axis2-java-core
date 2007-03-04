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
package org.apache.axis2.transport.http.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.WSDL20DefaultValueHolder;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.util.JavaUtils;
import org.apache.woden.wsdl20.extensions.http.HTTPLocation;

import javax.xml.namespace.QName;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;


/**
 * This util is used on the client side for creating the URL's for all request (WSDL 2.0 allws to
 * change the URL's of SOAP messages too). It resolves WSDL 2.0 httplocation property and also
 * append parameters to URL's when needed.
 */
public class URLTemplatingUtil {

    /**
     * This method is used to resolve httplocation property. It changes the URL as stipulated by
     * the httplocation property.
     *
     * @param messageContext - The MessageContext of the request
     * @param rawURLString   - The raw URL containing httplocation templates
     * @param detach         - Boolean value specifying whether the element should be detached from the
     *                       envelop. When serializing data as application/x-form-urlencoded what goes in the body is the
     *                       remainder and therefore we should detach the element from the envelop.
     * @return - String with templated values replaced
     * @throws AxisFault - Thrown in case an exception occurs
     */
    private static String applyURITemplating(MessageContext messageContext, String rawURLString,
                                             boolean detach) throws AxisFault {

        OMElement firstElement;
        if (detach) {
            firstElement = messageContext.getEnvelope().getBody().getFirstElement();
        } else {
            firstElement =
                    messageContext.getEnvelope().getBody().getFirstElement().cloneOMElement();
        }

        HTTPLocation httpLocation = new HTTPLocation(rawURLString);
        String[] localNames = httpLocation.getLocalNames();
        String[] values = new String[localNames.length];

        for (int i = 0; i < localNames.length; i++) {
            String localName = localNames[i];
            try {
                values[i] = URIEncoderDecoder.quoteIllegal(
                        getOMElementValue(localName, firstElement),
                        WSDL2Constants.LEGAL_CHARACTERS_IN_URL);
            } catch (UnsupportedEncodingException e) {
                throw new AxisFault("Unable to encode Query String");
            }
        }

        httpLocation.substitute(values);

        return httpLocation.toString();
    }

    /**
     * Appends Query parameters to the URL
     *
     * @param messageContext - The MessageContext of the request
     * @param url            - Original url string
     * @return String containing the appended query parameters
     */
    private static String appendQueryParameters(MessageContext messageContext, String url) {

        OMElement firstElement;
        String queryParameterSeparator = (String) messageContext
                .getProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR);
        // In case queryParameterSeparator is null we better use the default value

        if (queryParameterSeparator == null) {
            queryParameterSeparator = WSDL20DefaultValueHolder
                    .getDefaultValue(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR);
        }

        firstElement = messageContext.getEnvelope().getBody().getFirstElement();
        String params = "";

        if (firstElement != null) {
            Iterator iter = firstElement.getChildElements();

            while (iter.hasNext()) {
                OMElement element = (OMElement) iter.next();
                params = params + element.getLocalName() + "=" + element.getText() +
                        queryParameterSeparator;
            }
        }

        if (!"".equals(params)) {

            int index = url.indexOf("?");
            if (index == -1) {
                url = url + "?" + params.substring(0, params.length() - 1);
            } else if (index == url.length() - 1) {
                url = url + params.substring(0, params.length() - 1);
            } else {
                url = url + queryParameterSeparator + params.substring(0, params.length() - 1);
            }

        }
        return url;
    }

    /**
     * This method is used to retrive elements from the soap envelop
     *
     * @param elementName   - The name of the required element
     * @param parentElement - The parent element that the required element should be retrived from
     * @return - The value of the element as a string
     */
    private static String getOMElementValue(String elementName, OMElement parentElement) {

        OMElement httpURLParam = parentElement.getFirstChildWithName(new QName(elementName));

        if (httpURLParam != null) {
            httpURLParam.detach();

            if (parentElement.getFirstOMChild() == null) {
                parentElement.detach();
            }
        }

        return httpURLParam.getText();

    }

    /**
     * Returns the templated URL given the original URL
     *
     * @param targetURL      - The original URL
     * @param messageContext - The MessageContext of the request
     * @param detach         - Boolean value specifying whether the element should be detached from the
     *                       envelop. When serializing data as application/x-form-urlencoded what goes in the body is the
     *                       remainder and therefore we should detach the element from the envelop.
     * @return The templated URL
     * @throws AxisFault - Thrown in case an exception occurs
     */
    public static URL getTemplatedURL(URL targetURL, MessageContext messageContext, boolean detach)
            throws AxisFault {

        String urlString = targetURL.toString();
        String replacedQuery = "";
        String path = "";
        int separator = urlString.indexOf('{');

        if (separator > 0) {
            path = urlString.substring(0, separator - 1);
            String query = urlString.substring(separator - 1);

            replacedQuery = URLTemplatingUtil.applyURITemplating(messageContext, query, detach);
            try {
                targetURL = new URL(path + replacedQuery);
            } catch (MalformedURLException e) {
                throw new AxisFault("An error occured while trying to create request URL");
            }
        }

        return targetURL;
    }

    /**
     * Methos used to append parameters to URL. First checks whether the parameters should be
     * appended to the URL based on the WSDL 2.0 property whttp:ignoreUncited
     *
     * @param messageContext - The MessageContext of the request
     * @param targetURL      - The original URL
     * @return returns an URL with the query parameters appended to it
     * @throws AxisFault - Thrown in case an exception occurs
     */
    public static URL appendParametersToURL(MessageContext messageContext, URL targetURL)
            throws AxisFault {

        String url = targetURL.toString();
        String ignoreUncited =
                (String) messageContext.getProperty(WSDL2Constants.ATTR_WHTTP_IGNORE_UNCITED);

        if (ignoreUncited == null || !JavaUtils.isTrueExplicitly(ignoreUncited)) {
            url = appendQueryParameters(messageContext, url);
            try {
                targetURL = new URL(url);
            } catch (MalformedURLException e) {
                throw new AxisFault("Unable to create target URL from template");
            }
        } else if (Constants.Configuration.HTTP_METHOD_GET
                .equalsIgnoreCase((String) messageContext.getProperty(
                        Constants.Configuration.HTTP_METHOD))) {
            messageContext.getEnvelope().getBody().getFirstElement().detach();
        }

        return targetURL;
    }
}
