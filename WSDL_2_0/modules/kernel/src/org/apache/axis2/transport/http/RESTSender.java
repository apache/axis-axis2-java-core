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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.transport.http.util.ComplexPart;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.nio.charset.Charset;

public class RESTSender extends AbstractHTTPSender {
    private static final Log log = LogFactory.getLog(RESTSender.class);

    public Part [] createMultipatFormDataRequest(MessageContext msgContext, OMElement dataOut) {
        ArrayList parts = new ArrayList();
        String requestData = "";
        if (dataOut != null) {
            Iterator iter1 = dataOut.getChildElements();

            while (iter1.hasNext()) {
               OMElement ele = (OMElement) iter1.next();
                // check whether the element is a complex type
                if (ele.getFirstElement() != null) {
                    requestData = "<" + ele.getQName().getLocalPart() + ">";
                    requestData = requestData + processComplexType(ele.getChildElements());
                    requestData = requestData + "</" + ele.getQName().getLocalPart() + ">";
                    parts.add(new ComplexPart(ele.getQName().getLocalPart(),requestData));
                } else {
                    parts.add(new StringPart(ele.getQName().getLocalPart(),ele.getText()));
                }
            }
        }
        Part [] partsArray = new Part[parts.size()];
        return (Part [])parts.toArray(partsArray);
    }

    private String processComplexType(Iterator iter) {
        String data = "";
        while (iter.hasNext()) {
            OMElement ele = (OMElement) iter.next();
            data = data + "<" + ele.getQName().getLocalPart() + ">\n";
            if (ele.getFirstElement() != null) {
                data = data + processComplexType(ele.getChildElements());
            } else {
                data = data + "<" + ele.getQName().getLocalPart() + ">";
                data = data + ele.getText();
                data = data + "</" + ele.getQName().getLocalPart() + ">\n";
            }
        }
        return data;
    }

    /*Obtain two strings;one to go in the url and rest to pass in the body
    **when doing POST in application/x-www-form-urlencoded form.
    */
    public RequestData createRequest(MessageContext msgContext, OMElement dataout) {

        RequestData data = new RequestData();
        if (dataout != null) {
        Iterator iter1 = dataout.getChildElements();
        ArrayList paraList = new ArrayList();
        ArrayList urlList = new ArrayList();

        // urlParameterList contains the parameters which go in the URL
        String[] urlParameterList = new String[0];
        if (msgContext.getProperty(Constants.Configuration.URL_PARAMETER_LIST) != null) {
            urlParameterList = (String[]) msgContext.getProperty(Constants.Configuration.URL_PARAMETER_LIST);
        }

        OMElement bodypara = OMAbstractFactory.getOMFactory().createOMElement("temp", null);

        while (iter1.hasNext()) {
            OMElement ele = (OMElement) iter1.next();
            boolean parameterFound = false;

            for (int i = 0; i < urlParameterList.length; i++) {
                if (urlParameterList[i].equals(ele.getLocalName())) {
                    parameterFound = true;
                    break;
                }
            }

            String parameter;

            if (parameterFound) {
                parameter = ele.getLocalName() + "=" + ele.getText();
                urlList.add(parameter);
            } else {
                bodypara.addChild(ele);
            }
        }

        String urlString = "";
        for (int i = 0; i < urlList.size(); i++) {
            String nameValuePair = (String) urlList.get(i);
            urlString = "".equals(urlString) ? nameValuePair : (urlString + "&" + nameValuePair);
        }
        data.urlRequest = urlString;

        Iterator it = bodypara.getChildElements();

        while (it.hasNext()) {
            OMElement ele1 = (OMElement) it.next();
            String parameter2;

            parameter2 = ele1.getLocalName() + "=" + ele1.getText();
            paraList.add(parameter2);
        }

        String paraString = "";

        for (int j = 0; j < paraList.size(); j++) {
            String b = (String) paraList.get(j);
            paraString = "".equals(paraString) ? b : (paraString + "&" + b);
            data.bodyRequest = paraString;
        }

        if (dataout.getFirstOMChild() == null) {
            dataout.detach();
        }
        }

        return data;
    }

    /**
     * By this time, you must have identified that you are doing REST here. Following default values
     * will apply.
     * If the HTTPMethod is not set, I prefer to set it as POST by default.
     *
     * @param msgContext
     * @param dataout
     * @param url
     * @param soapActionString
     */
    public void send(MessageContext msgContext, OMElement dataout, URL url,
                     String soapActionString) {
        try {
            String httpMethod =
                    (String) msgContext.getProperty(Constants.Configuration.HTTP_METHOD);

            if (httpMethod != null) {
                if (Constants.Configuration.HTTP_METHOD_GET.equalsIgnoreCase(httpMethod)) {
                    this.sendViaGet(msgContext, url);
                    return;
                } else if (Constants.Configuration.HTTP_METHOD_POST.equalsIgnoreCase(httpMethod)) {
                    this.sendViaPost(msgContext, dataout, url, soapActionString);
                    return;
                } else if (Constants.Configuration.HTTP_METHOD_DELETE.equalsIgnoreCase(httpMethod)) {
                    this.sendViaDelete(msgContext, url);
                    return;
                }
                else if (Constants.Configuration.HTTP_METHOD_PUT.equalsIgnoreCase(httpMethod)) {
                    this.sendViaPut(msgContext, dataout, url, soapActionString);
                    return;
                }
            } else {
                this.sendViaPost(msgContext, dataout, url, soapActionString);
            }
        } catch (Exception e) {
            log.error("Error in extracting transport properties from message context", e);
        }
    }

//    /**
//     * This will be used to support http location. User will create an OMElement to be plugged in to
//     * SOAP body. Then he has to name the parameters that should go in the url. This method will extract
//     * those parameters from the body first child and append to the url.
//     * <p/>
//     * In addition to that, user can set a URL template, like ?firstName={FirstName}. In this case,
//     * first name must be taken from the body and will replace the url.
//     *
//     * @param messageContext
//     * @param urlString
//     * @return - the URL after appending the properties
//     */
//    protected String appendParametersToURL(MessageContext messageContext, String urlString, String queryPart) {
//        StringBuffer buffer = null;
//        try {
//            OMElement firstElement = messageContext.getEnvelope().getBody().getFirstElement();
//
//            // first process the situ where user had explicitly put some params to go in the URL
//            ArrayList httpLocationParams = (ArrayList) messageContext.getProperty(
//                    Constants.Configuration.URL_HTTP_LOCATION_PARAMS_LIST);
//
//            URL url = new URL(urlString);
//            String path = url.getPath();
//
////            if (httpLocationParams != null) {
////                for (int i = 0; i < httpLocationParams.size(); i++) {
////                    String httpLocationParam = (String) httpLocationParams.get(i);
////                    OMElement httpURLParam = firstElement.getFirstChildWithName(new QName(httpLocationParam));
////                    if (httpURLParam != null) {
////                        path += "/" + httpURLParam.getText();
////                        httpURLParam.detach();
////                    }
////                }
////            }
//
//            if (queryPart != null && queryPart.length() > 0) {
//                if (queryPart.startsWith("?")) {
//                    path = urlString + queryPart;
//                } else {
//                    path = urlString + "?" + queryPart;
//                }
//            }
//
//            // now let's process URL templates.
//            String patternString = "\\{[A-Z0-9a-z._%-]+\\}";
//            Pattern pattern = Pattern.compile(patternString);
//
//            buffer = new StringBuffer(path);
//
//            Matcher matcher = pattern.matcher(buffer);
//
//            while (matcher.find()) {
//                String match = matcher.group();
//
//                // Get indices of matching string
//                int start = matcher.start();
//                int end = matcher.end();
//
//                CharSequence charSequence = match.subSequence(1, match.length() - 1);
//
//                buffer.delete(start, end);
//                buffer.insert(start, getOMElementValue(charSequence.toString(), firstElement));
//
//            }
//
//            return buffer.toString();
//
//
//        } catch (MalformedURLException e) {
//            log.error("Error in processing POST request", e);
//        }
//        catch (StringIndexOutOfBoundsException e) {
//            log.error("Error in processing POST request", e);
//            return buffer.toString();
//        }
//
//        return null;
//    }

    private String getOMElementValue(String elementName, OMElement parentElement) {
        OMElement httpURLParam = parentElement.getFirstChildWithName(new QName(elementName));

        if (httpURLParam != null) {
            httpURLParam.detach();
            return httpURLParam.getText();
        }

        return null;

    }

    private void sendViaDelete(MessageContext msgContext, URL url)
            throws AxisFault, IOException {

        DeleteMethod deleteMethod = new DeleteMethod();
        if (isAuthenticationEnabled(msgContext)) {
            deleteMethod.setDoAuthentication(true);
        }
        String urlString = url.toString();
        int separator = urlString.indexOf('{');
        if (separator > 0) {
            String path = urlString.substring(0, separator - 1);
            String query = urlString.substring(separator - 1);
            String replacedQuery ;
                 replacedQuery = applyURITemplating(msgContext, query, true);
            url = new URL(path + replacedQuery);
        }

        deleteMethod.setPath(url.getPath());
        String query = url.getQuery();
        String ignoreUncited = (String) msgContext.getProperty(WSDL2Constants.ATTR_WHTTP_IGNORE_UNCITED);
        // If ignoreUncited property is true we can ignore the uncited parameters in the url, If it is not specified it
        // defaults to false hence we append the additional query parameters.
        if (ignoreUncited != null && JavaUtils.isTrueExplicitly(ignoreUncited)) {
            deleteMethod.setQueryString(query);
        } else {
            deleteMethod.setQueryString(appendQueryParameters(msgContext, url.getQuery()));
        }
        // Serialization as "application/x-www-form-urlencoded"
        String charEncoding =
                (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

        String contentType = null;

        // Default encoding scheme
        if (charEncoding == null) {
            contentType = HTTPConstants.MEDIA_TYPE_X_WWW_FORM + "; charset="
                            + MessageContext.DEFAULT_CHAR_SET_ENCODING;
        } else {
            contentType = HTTPConstants.MEDIA_TYPE_X_WWW_FORM + "; charset="
                            + charEncoding;
        }

        String action = msgContext.getOptions().getAction();

        if (action != null) {
            contentType = contentType + ";" + "action=" + action;
        }

        deleteMethod.setRequestHeader(HTTPConstants.HEADER_CONTENT_TYPE,contentType);

        HttpClient httpClient = getHttpClient(msgContext);
        executeMethod(httpClient, msgContext, url, deleteMethod);

        if (deleteMethod.getStatusCode() == HttpStatus.SC_OK) {
            processResponse(deleteMethod, msgContext);
        } else if (deleteMethod.getStatusCode() == HttpStatus.SC_ACCEPTED) {
        } else if (deleteMethod.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            Header contenttypeHheader =
                    deleteMethod.getResponseHeader(HTTPConstants.HEADER_CONTENT_TYPE);
            String value = contenttypeHheader.getValue();

            if (value != null) {
                if ((value.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) >= 0)
                        || (value.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) >= 0)) {
                    processResponse(deleteMethod, msgContext);
                }
            }
        } else {
            throw new AxisFault(Messages.getMessage("transportError",
                    String.valueOf(deleteMethod.getStatusCode()),
                    deleteMethod.getResponseBodyAsString()));
        }
    }

    private void sendViaGet(MessageContext msgContext, URL url)
            throws AxisFault, IOException {
        String param = getQueryParameters(msgContext);
        GetMethod getMethod = new GetMethod();
        if (isAuthenticationEnabled(msgContext)) {
            getMethod.setDoAuthentication(true);
        }

        String urlString = url.toString();
        int separator = urlString.indexOf('{');
        if (separator > 0) {
            String path = urlString.substring(0, separator - 1);
            String query = urlString.substring(separator - 1);
            String replacedQuery ;
                 replacedQuery = applyURITemplating(msgContext, query, true);
            url = new URL(path + replacedQuery);
        }

        getMethod.setPath(url.getPath());
        String query = url.getQuery();
        String ignoreUncited = (String) msgContext.getProperty(WSDL2Constants.ATTR_WHTTP_IGNORE_UNCITED);
        // If ignoreUncited property is true we can ignore the uncited parameters in the url, If it is not specified it
        // defaults to false hence we append the additional query parameters.
        if (ignoreUncited != null && JavaUtils.isTrueExplicitly(ignoreUncited)) {
            getMethod.setQueryString(query);
        } else {
            getMethod.setQueryString(appendQueryParameters(msgContext, url.getQuery()));
        }
        // Serialization as "application/x-www-form-urlencoded"
        String charEncoding =
                (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

        String contentType = null;

        // Default encoding scheme
        if (charEncoding == null) {
            contentType = HTTPConstants.MEDIA_TYPE_X_WWW_FORM + "; charset="
                            + MessageContext.DEFAULT_CHAR_SET_ENCODING;
        } else {
            contentType = HTTPConstants.MEDIA_TYPE_X_WWW_FORM + "; charset="
                            + charEncoding;
        }

        String action = msgContext.getOptions().getAction();

        if (action != null) {
            contentType = contentType + ";" + "action=" + action; 
        }

        getMethod.setRequestHeader(HTTPConstants.HEADER_CONTENT_TYPE,contentType);

        HttpClient httpClient = getHttpClient(msgContext);
        executeMethod(httpClient, msgContext, url, getMethod);

        if (getMethod.getStatusCode() == HttpStatus.SC_OK) {
            processResponse(getMethod, msgContext);
        } else if (getMethod.getStatusCode() == HttpStatus.SC_ACCEPTED) {
        } else if (getMethod.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            Header contenttypeHheader =
                    getMethod.getResponseHeader(HTTPConstants.HEADER_CONTENT_TYPE);
            String value = contenttypeHheader.getValue();

            if (value != null) {
                if ((value.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) >= 0)
                        || (value.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) >= 0)) {
                    processResponse(getMethod, msgContext);
                }
            }
        } else {
            throw new AxisFault(Messages.getMessage("transportError",
                    String.valueOf(getMethod.getStatusCode()),
                    getMethod.getResponseBodyAsString()));
        }
    }

    private void sendViaPost(MessageContext msgContext, OMElement dataout, URL url,
                             String soapActionString) throws MalformedURLException {

        // execute the HtttpMethodBase - a connection manager can be given for
        // handle multiple
        HttpClient httpClient = getHttpClient(msgContext);

        PostMethod postMethod = new PostMethod(url.toString());
        if (isAuthenticationEnabled(msgContext)) {
            postMethod.setDoAuthentication(true);
        }
        String httpContentType;

        if (msgContext.getProperty(Constants.Configuration.CONTENT_TYPE) != null) {
            httpContentType = (String) msgContext.getProperty(Constants.Configuration.CONTENT_TYPE);
        } else {
            httpContentType = HTTPConstants.MEDIA_TYPE_APPLICATION_XML;
        }

        String charEncoding =
                (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

        if (charEncoding == null) {
            charEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        String urlString = url.toString();
        int separator = urlString.indexOf('{');
        if (separator > 0) {
            String path = urlString.substring(0, separator - 1);
            String query = urlString.substring(separator - 1);
            String replacedQuery ;
            if (httpContentType.equalsIgnoreCase(HTTPConstants.MEDIA_TYPE_X_WWW_FORM)) {
            replacedQuery = applyURITemplating(msgContext, query, true);
            }
            else {
                 replacedQuery = applyURITemplating(msgContext, query, false);
            }
            url = new URL(path + replacedQuery);
        }

        postMethod.setPath(url.getPath());
        postMethod.setQueryString(url.getQuery());

        // if POST as application/x-www-form-urlencoded
        RequestData reqData;

        if (httpContentType.equalsIgnoreCase(HTTPConstants.MEDIA_TYPE_X_WWW_FORM)) {
            reqData = createRequest(msgContext, dataout);
            if (reqData.bodyRequest == null) {
                reqData.bodyRequest = "0";
            }
            postMethod.setRequestEntity(new AxisRESTRequestEntity(reqData.bodyRequest, httpContentType));

        } else if (httpContentType.equalsIgnoreCase(HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA)) {
//            String uuid = UUIDGenerator.getUUID();
//            String uuid = "-------------------------"+System.currentTimeMillis();
            Part[] parts = createMultipatFormDataRequest(msgContext, dataout);   
//            postMethod.setRequestEntity(new AxisRESTRequestEntity(bodyData, httpContentType + "; boundry=" + uuid));

//            Part[] parts = {new ComplexPart("param_name", "value"), new StringPart("second param", "second value")};
            postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
        } else {
            postMethod.setRequestEntity(new AxisRequestEntity(dataout, chunked, msgContext,
                    charEncoding, soapActionString, httpContentType));
        }

        if (!httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10) && chunked) {
            postMethod.setContentChunked(true);
        }

        postMethod.setRequestHeader(HTTPConstants.HEADER_HOST, url.getHost());

        if (httpVersion != null) {
            if (httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10)) {
                httpClient.getParams().setVersion(HttpVersion.HTTP_1_0);
                postMethod.setRequestHeader(HTTPConstants.HEADER_CONNECTION,
                        HTTPConstants.HEADER_CONNECTION_KEEPALIVE);
            } else {

                // allowing keep-alive for 1.1
                postMethod.setRequestHeader(HTTPConstants.HEADER_CONNECTION,
                        HTTPConstants.HEADER_CONNECTION_KEEPALIVE);
                postMethod.setRequestHeader(HTTPConstants.HEADER_EXPECT,
                        HTTPConstants.HEADER_EXPECT_100_Continue);
            }
        }

        /**
         * main excecution takes place..
         */
        try {
            executeMethod(httpClient, msgContext, url, postMethod);

            if (postMethod.getStatusCode() == HttpStatus.SC_OK) {
                processResponse(postMethod, msgContext);

                return;
            } else if (postMethod.getStatusCode() == HttpStatus.SC_ACCEPTED) {
                return;
            } else if (postMethod.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                Header contenttypeHheader =
                        postMethod.getResponseHeader(HTTPConstants.HEADER_CONTENT_TYPE);

                if (contenttypeHheader != null) {
                    String value = contenttypeHheader.getValue();

                    if ((value.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) >= 0)
                            || (value.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) >= 0)) {
                        processResponse(postMethod, msgContext);

                        return;
                    }
                }
            }

            throw new AxisFault(Messages.getMessage("transportError",
                    String.valueOf(postMethod.getStatusCode()),
                    postMethod.getResponseBodyAsString()));
        } catch (Exception e) {
            log.error("Error in processing POST request", e);
        }
    }


    private void sendViaPut(MessageContext msgContext, OMElement dataout, URL url,
                             String soapActionString) throws MalformedURLException {

        // execute the HtttpMethodBase - a connection manager can be given for
        // handle multiple
        HttpClient httpClient = getHttpClient(msgContext);

        PutMethod putMethod = new PutMethod(url.toString());
        if (isAuthenticationEnabled(msgContext)) {
            putMethod.setDoAuthentication(true);
        }
        String httpContentType;

        if (msgContext.getProperty(Constants.Configuration.CONTENT_TYPE) != null) {
            httpContentType = (String) msgContext.getProperty(Constants.Configuration.CONTENT_TYPE);
        } else {
            httpContentType = HTTPConstants.MEDIA_TYPE_APPLICATION_XML;
        }

        String charEncoding =
                (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

        if (charEncoding == null) {
            charEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        String urlString = url.toString();
        int separator = urlString.indexOf('{');
        if (separator > 0) {
            String path = urlString.substring(0, separator - 1);
            String query = urlString.substring(separator - 1);
            String replacedQuery ;
            if (httpContentType.equalsIgnoreCase(HTTPConstants.MEDIA_TYPE_X_WWW_FORM)) {
            replacedQuery = applyURITemplating(msgContext, query, true);
            }
            else {
                 replacedQuery = applyURITemplating(msgContext, query, false);
            }
            url = new URL(path + replacedQuery);
        }

        putMethod.setPath(url.getPath());
        putMethod.setQueryString(url.getQuery());

        // if POST as application/x-www-form-urlencoded
        RequestData reqData;

        if (httpContentType.equalsIgnoreCase(HTTPConstants.MEDIA_TYPE_X_WWW_FORM)) {
            reqData = createRequest(msgContext, dataout);
            if (reqData.bodyRequest == null) {
                reqData.bodyRequest = "0";
            }
            putMethod.setRequestEntity(new AxisRESTRequestEntity(reqData.bodyRequest, httpContentType));

        } else if (httpContentType.equalsIgnoreCase(HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA)) {
//            String uuid = UUIDGenerator.getUUID();
//            String uuid = "-------------------------"+System.currentTimeMillis();
            Part[] parts = createMultipatFormDataRequest(msgContext, dataout);
//            postMethod.setRequestEntity(new AxisRESTRequestEntity(bodyData, httpContentType + "; boundry=" + uuid));

//            Part[] parts = {new ComplexPart("param_name", "value"), new StringPart("second param", "second value")};
            putMethod.setRequestEntity(new MultipartRequestEntity(parts, putMethod.getParams()));
        } else {
            putMethod.setRequestEntity(new AxisRequestEntity(dataout, chunked, msgContext,
                    charEncoding, soapActionString, httpContentType));
        }

        if (!httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10) && chunked) {
            putMethod.setContentChunked(true);
        }

        putMethod.setRequestHeader(HTTPConstants.HEADER_HOST, url.getHost());

        if (httpVersion != null) {
            if (httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10)) {
                httpClient.getParams().setVersion(HttpVersion.HTTP_1_0);
                putMethod.setRequestHeader(HTTPConstants.HEADER_CONNECTION,
                        HTTPConstants.HEADER_CONNECTION_KEEPALIVE);
            } else {

                // allowing keep-alive for 1.1
                putMethod.setRequestHeader(HTTPConstants.HEADER_CONNECTION,
                        HTTPConstants.HEADER_CONNECTION_KEEPALIVE);
                putMethod.setRequestHeader(HTTPConstants.HEADER_EXPECT,
                        HTTPConstants.HEADER_EXPECT_100_Continue);
            }
        }

        /**
         * main excecution takes place..
         */
        try {
            executeMethod(httpClient, msgContext, url, putMethod);

            if (putMethod.getStatusCode() == HttpStatus.SC_OK) {
                processResponse(putMethod, msgContext);

                return;
            } else if (putMethod.getStatusCode() == HttpStatus.SC_ACCEPTED) {
                return;
            } else if (putMethod.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                Header contenttypeHheader =
                        putMethod.getResponseHeader(HTTPConstants.HEADER_CONTENT_TYPE);

                if (contenttypeHheader != null) {
                    String value = contenttypeHheader.getValue();

                    if ((value.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) >= 0)
                            || (value.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) >= 0)) {
                        processResponse(putMethod, msgContext);

                        return;
                    }
                }
            }

            throw new AxisFault(Messages.getMessage("transportError",
                    String.valueOf(putMethod.getStatusCode()),
                    putMethod.getResponseBodyAsString()));
        } catch (Exception e) {
            log.error("Error in processing POST request", e);
        }
    }

    public String getQueryParameters(MessageContext msgContext) {
        OMElement dataOut;

        dataOut = msgContext.getEnvelope().getBody().getFirstElement();

        Iterator iter1 = dataOut.getChildElements();
        ArrayList paraList = new ArrayList();

        while (iter1.hasNext()) {
            OMElement ele = (OMElement) iter1.next();
            String parameter;

            parameter = ele.getLocalName() + "=" + ele.getText();
            paraList.add(parameter);
        }

        String paraString = "";
        int count = paraList.size();

        for (int i = 0; i < count; i++) {
            String c = (String) paraList.get(i);
            paraString = "".equals(paraString) ? c : (paraString + "&" + c);
        }

        return paraString;
    }

    public class AxisRequestEntity implements RequestEntity {
        private boolean doingMTOM = false;
        private byte[] bytes;
        private String charSetEnc;
        private boolean chunked;
        private OMElement element;
        private MessageContext msgCtxt;
        private String soapActionString;
        private String contentType;

        public AxisRequestEntity(OMElement element, boolean chunked,
                                 MessageContext msgCtxt,
                                 String charSetEncoding,
                                 String soapActionString,
                                 String contentType) {
            this.element = element;
            this.chunked = chunked;
            this.msgCtxt = msgCtxt;
            this.doingMTOM = msgCtxt.isDoingMTOM();
            this.charSetEnc = charSetEncoding;
            this.soapActionString = soapActionString;
            this.contentType = contentType;
        }

        private void handleOMOutput(OutputStream out, boolean doingMTOM)
                throws XMLStreamException {
            format.setDoOptimize(doingMTOM);
            element.serializeAndConsume(out, format);
        }

        public byte[] writeBytes() throws AxisFault {
            try {
                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

                if (!doingMTOM) {
                    OMOutputFormat format2 = new OMOutputFormat();

                    format2.setCharSetEncoding(charSetEnc);
                    element.serializeAndConsume(bytesOut, format2);

                    return bytesOut.toByteArray();
                } else {
                    format.setCharSetEncoding(charSetEnc);
                    format.setDoOptimize(true);
                    element.serializeAndConsume(bytesOut, format);

                    return bytesOut.toByteArray();
                }
            } catch (XMLStreamException e) {
                throw new AxisFault(e);
            } catch (FactoryConfigurationError e) {
                throw new AxisFault(e);
            }
        }

        public void writeRequest(OutputStream out) throws IOException {
            try {
                {
                    if (chunked) {
                        this.handleOMOutput(out, doingMTOM);
                    } else if (element != null) {
                        if (bytes == null) {
                            bytes = writeBytes();
                        }

                        out.write(bytes);
                    }
                }

                out.flush();
            } catch (XMLStreamException e) {
                throw new AxisFault(e);
            } catch (FactoryConfigurationError e) {
                throw new AxisFault(e);
            } catch (IOException e) {
                throw new AxisFault(e);
            }
        }

        public long getContentLength() {
            try {
                {
                    if (chunked) {
                        return -1;
                    } else if (element != null) {
                        if (bytes == null) {
                            bytes = writeBytes();
                        }

                        return bytes.length;
                    }

                    return 0;
                }
            } catch (AxisFault e) {
                return -1;
            }
        }

        public String getContentType() {
            String encoding = format.getCharSetEncoding();
            if (encoding != null) {
                contentType += "; charset=" + encoding;
            }

            // action header is not mandated in SOAP 1.2. So putting it, if available
            if (!msgCtxt.isSOAP11() && (soapActionString != null)
                    && !"".equals(soapActionString.trim()) && !"\"\"".equals(soapActionString.trim())) {
                contentType =
                        contentType + ";action=\"" + soapActionString + "\";";
            }

            return contentType;
        }

        public boolean isRepeatable() {
            return true;
        }
    }

    public class AxisRESTRequestEntity implements RequestEntity {
        private String contentType;
        private String postRequestBody;

        public AxisRESTRequestEntity(String postRequestBody, String contentType) {
            this.postRequestBody = postRequestBody;
            this.contentType = contentType;
        }

        public void writeRequest(OutputStream output) throws IOException {
            output.write(postRequestBody.getBytes());
        }

        public long getContentLength() {
            return this.postRequestBody.getBytes().length;
        }

        public String getContentType() {
            return this.contentType;
        }

        public boolean isRepeatable() {
            return true;
        }
    }

    private class RequestData {
        private String bodyRequest;
        private String urlRequest;
    }
}
