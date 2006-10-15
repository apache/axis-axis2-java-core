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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.FactoryConfigurationError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class RESTSender extends AbstractHTTPSender {
    private static final Log log = LogFactory.getLog(RESTSender.class);

    /*Obtain two strings;one to go in the url and rest to pass in the body
    **when doing POST in application/x-www-form-urlencoded form.
    */
    public RequestData createRequest(MessageContext msgContext, OMElement dataout) {

        RequestData data = new RequestData();
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
            boolean has = false;

            for (int i = 0; i < urlParameterList.length; i++) {
                if (urlParameterList[i].equals(ele.getLocalName())) {
                    has = true;

                    break;
                }
            }

            String parameter1;

            if (has) {
                parameter1 = ele.getLocalName() + "=" + ele.getText();
                urlList.add(parameter1);
            } else {
                bodypara.addChild(ele);
            }
        }

        String urlString = "";
        for (int i = 0; i < urlList.size(); i++) {
            String c = (String) urlList.get(i);
            urlString = "".equals(urlString) ? c : (urlString + "&" + c);
            data.urlRequest = urlString;
        }

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

            if ((httpMethod != null)
                    && Constants.Configuration.HTTP_METHOD_GET.equalsIgnoreCase(httpMethod)) {
                this.sendViaGet(msgContext, url);

                return;
            }

            this.sendViaPost(msgContext, dataout, url, soapActionString);
        } catch (Exception e) {
            log.error("Error in extracting transport properties from message context", e);
        }
    }

    private void sendViaGet(MessageContext msgContext, URL url)
            throws MalformedURLException, AxisFault, IOException {
        String param = getParam(msgContext);
        GetMethod getMethod = new GetMethod();
        if (isAuthenticationEnabled(msgContext)) {
            getMethod.setDoAuthentication(true);
        }

        if (param != null && param.length() > 0) {
            getMethod.setPath(url.getFile() + "?" + param);
        } else {
            getMethod.setPath(url.getFile());
        }

        // Serialization as "application/x-www-form-urlencoded"
        String charEncoding =
                (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

        // Default encoding scheme
        if (charEncoding == null) {
            getMethod.setRequestHeader(HTTPConstants.HEADER_CONTENT_TYPE,
                    HTTPConstants.MEDIA_TYPE_X_WWW_FORM + "; charset="
                            + MessageContext.DEFAULT_CHAR_SET_ENCODING);
        } else {
            getMethod.setRequestHeader(HTTPConstants.HEADER_CONTENT_TYPE,
                    HTTPConstants.MEDIA_TYPE_X_WWW_FORM + "; charset="
                            + charEncoding);
        }

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
                             String soapActionString) {

        // execute the HtttpMethodBase - a connection manager can be given for
        // handle multiple
        HttpClient httpClient = getHttpClient(msgContext);

        PostMethod postMethod = new PostMethod(url.toString());
        if(isAuthenticationEnabled(msgContext)) {
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

        // if POST as application/x-www-form-urlencoded
        RequestData reqData;

        if (httpContentType.equalsIgnoreCase(HTTPConstants.MEDIA_TYPE_X_WWW_FORM)) {
            reqData = createRequest(msgContext, dataout);
            postMethod.setPath(url.getPath() + ((reqData.urlRequest) != null
                    ? ("?" + reqData.urlRequest)
                    : ""));

            if (reqData.bodyRequest == null) {
                reqData.bodyRequest = "0";
            }
            postMethod.setRequestEntity(new AxisRESTRequestEntity(reqData.bodyRequest,httpContentType));

        } else {
            postMethod.setPath(url.getPath());
            postMethod.setRequestEntity(new AxisRequestEntity(dataout, chunked, msgContext,
                    charEncoding, soapActionString));
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

    public String getParam(MessageContext msgContext) {
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

        public AxisRequestEntity(OMElement element, boolean chunked,
                                 MessageContext msgCtxt,
                                 String charSetEncoding,
                                 String soapActionString) {
            this.element = element;
            this.chunked = chunked;
            this.msgCtxt = msgCtxt;
            this.doingMTOM = msgCtxt.isDoingMTOM();
            this.charSetEnc = charSetEncoding;
            this.soapActionString = soapActionString;
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
                    } else {
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
                    } else {
                        if (bytes == null) {
                            bytes = writeBytes();
                        }

                        return bytes.length;
                    }
                }
            } catch (AxisFault e) {
                return -1;
            }
        }

        public String getContentType() {
            String encoding = format.getCharSetEncoding();
            String contentType = format.getContentType();

            if (encoding != null) {
                contentType += "; charset=" + encoding;
            }

            // action header is not mandated in SOAP 1.2. So putting it, if available
            if (!msgCtxt.isSOAP11() && (soapActionString != null)
                && !"".equals(soapActionString.trim()) && ! "\"\"".equals(soapActionString.trim())) {
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

        public AxisRESTRequestEntity(String postRequestBody,String contentType) {
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
