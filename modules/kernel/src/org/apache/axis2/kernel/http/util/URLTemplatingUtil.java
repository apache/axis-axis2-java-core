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

package org.apache.axis2.kernel.http.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.WSDL20DefaultValueHolder;
import org.apache.axis2.description.WSDL2Constants;

import javax.xml.namespace.QName;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;


/**
 * This util is used on the client side for creating the URL's for all request (WSDL 2.0 allws to
 * change the URL's of SOAP messages too). It resolves WSDL 2.0 httplocation property and also
 * append parameters to URL's when needed.
 */
public class URLTemplatingUtil {

    /**
     * Appends Query parameters to the URL
     *
     * @param messageContext - The MessageContext of the request
     * @param url            - Original url string
     * @return String containing the appended query parameters
     */
    public static URL appendQueryParameters(MessageContext messageContext, URL url) throws AxisFault {

        String urlString = url.toString();
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

            String legalCharacters = WSDL2Constants.LEGAL_CHARACTERS_IN_QUERY.replaceAll(queryParameterSeparator, "");

            while (iter.hasNext()) {
                OMElement element = (OMElement) iter.next();
                try {
                    params = params + URIEncoderDecoder.quoteIllegal(element.getLocalName(), legalCharacters) + "=" + URIEncoderDecoder.quoteIllegal(element.getText(), legalCharacters) +
                            queryParameterSeparator;
                } catch (UnsupportedEncodingException e) {
                    throw AxisFault.makeFault(e);
                }
            }
        }

        if (!"".equals(params)) {
            int index = urlString.indexOf("?");
            if (index == -1) {
                urlString = urlString + "?" + params.substring(0, params.length() - 1);
            } else if (index == urlString.length() - 1) {
                urlString = urlString + params.substring(0, params.length() - 1);

            } else {
                urlString = urlString + queryParameterSeparator + params.substring(0, params.length() - 1);
            }

            try {
                return new URL(urlString);
            } catch (MalformedURLException e) {
                throw AxisFault.makeFault(e);
            }
        }
        return url;
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

        String httpLocation = (String) messageContext.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION);

//        String urlString = targetURL.toString();
        if (httpLocation != null) {
        String replacedQuery = httpLocation;
        int separator = httpLocation.indexOf('{');
            try {

        if (separator > -1) {
            replacedQuery = URIEncoderDecoder.quoteIllegal(
                    applyURITemplating(messageContext, httpLocation, detach),
                    WSDL2Constants.LEGAL_CHARACTERS_IN_URL);

        }
                URI targetURI;
                URI appendedURI;
                if (replacedQuery.charAt(0) == '?') {
                    appendedURI = new URI(targetURL.toString () + replacedQuery);
                } else {
                     String uriString = targetURL.toString();
                    if (!uriString.endsWith("/")) {
                    targetURI = new URI(uriString + "/");
                    } else {
                        targetURI = new URI(uriString);
                    }
                    appendedURI = targetURI.resolve(replacedQuery);
                }
                
                targetURL = appendedURI.toURL();

            } catch (MalformedURLException e) {
                throw new AxisFault("An error occured while trying to create request URL");
            } catch (URISyntaxException e) {
                throw new AxisFault("An error occured while trying to create request URL");
            } catch (UnsupportedEncodingException e) {
                throw new AxisFault("An error occured while trying to create request URL");
            }
        }

        return targetURL;
    }

    /**
     * Applies URI templating by replacing {name} placeholders in the raw URL
     * with values extracted from the SOAP body elements.
     *
     * @param messageContext - The MessageContext of the request
     * @param rawURLString   - The raw URL containing {name} templates
     * @param detach         - Whether to detach matched elements from the envelope
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

        String result = rawURLString;
        int start;
        while ((start = result.indexOf('{')) != -1) {
            int end = result.indexOf('}', start);
            if (end == -1) {
                break;
            }
            String name = result.substring(start + 1, end);
            String value = getOMElementValue(name, firstElement);
            try {
                // Determine if this template is in the query part or path part
                int queryStart = result.indexOf('?');
                String legalChars;
                if (queryStart != -1 && start > queryStart) {
                    String qpSep = (String) messageContext.getProperty(
                            WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR);
                    if (qpSep == null) {
                        qpSep = WSDL20DefaultValueHolder
                                .ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR_DEFAULT;
                    }
                    legalChars = WSDL2Constants.LEGAL_CHARACTERS_IN_QUERY
                            .replaceAll(qpSep, "");
                } else {
                    legalChars = WSDL2Constants.LEGAL_CHARACTERS_IN_PATH;
                }
                value = URIEncoderDecoder.quoteIllegal(value, legalChars);
            } catch (UnsupportedEncodingException e) {
                throw new AxisFault("Unable to encode URI template value");
            }
            result = result.substring(0, start) + value + result.substring(end + 1);
        }
        return result;
    }

    /**
     * Retrieves the text value of a child element by local name.
     *
     * @param elementName   - The local name of the required element
     * @param parentElement - The parent element to search in
     * @return - The text value of the element, or empty string if not found
     */
    private static String getOMElementValue(String elementName, OMElement parentElement) {
        Iterator children = parentElement.getChildElements();
        while (children.hasNext()) {
            OMElement child = (OMElement) children.next();
            QName qName = child.getQName();
            if (elementName.equals(qName.getLocalPart())) {
                child.detach();
                if (parentElement.getFirstOMChild() == null && parentElement.getParent() != null) {
                    parentElement.detach();
                }
                return child.getText();
            }
        }
        return "";
    }

}
