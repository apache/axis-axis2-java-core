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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.MessageContextConstants;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.OMNodeEx;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.transport.TransportSender;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class CommonsHTTPTransportSender
        extends AbstractHandler
        implements TransportSender {
    private boolean chuncked = false;

    private String httpVersion = HTTPConstants.HEADER_PROTOCOL_11;

    int soTimeout = HTTPConstants.DEFAULT_SO_TIMEOUT;

    int connectionTimeout = HTTPConstants.DEFAULT_CONNECTION_TIMEOUT;

    public static final String HTTP_METHOD = "HTTP_METHOD";

    protected HttpClient httpClient;

    protected OMElement outputMessage;

    protected OMOutputImpl omOutput = new OMOutputImpl();

    /**
     * proxydiscription
     */
    protected TransportOutDescription proxyOutSetting = null;

    protected static final String PROXY_HOST_NAME = "proxy_host";
    protected static final String PROXY_PORT = "proxy_port";


    protected Log log = LogFactory.getLog(getClass().getName());

    /**
     * {@value}
     */
    private static final String ANONYMOUS = "anonymous";

    public CommonsHTTPTransportSender() {
    } //default

    public RequestData createRequest(MessageContext msgContext) {
        //This used to obtain two strings to go with the url and to pass in the body when doing
        //POST with application/x-www-form-urlencoded
        RequestData data = new RequestData();
        String contentType = findContentType(true, msgContext);
        OMElement dataOut = msgContext.getEnvelope().getBody().getFirstElement();

        Iterator iter1 = dataOut.getChildElements();
        ArrayList paraList = new ArrayList();
        ArrayList urlList = new ArrayList();

        //TODO: s is ALWAYS EMPTY. so what gets added to urllist????
        String[] s = new String[]{};
        OMElement bodypara = OMAbstractFactory.getOMFactory().createOMElement("dummy", null);

        while (iter1.hasNext()) {
            OMElement ele = (OMElement) iter1.next();
            boolean has = false;

            for (int i = 0; i < s.length; i++) {
                if (s[i].equals(ele.getLocalName())) {
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
            urlString = urlString + "&" + c;
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
            paraString = paraString + "&" + b;
            data.bodyRequest = paraString;
        }
        return data;
    }

    public synchronized void invoke(MessageContext msgContext) throws AxisFault {
        try {
            String charSetEnc =
                    (String) msgContext.getProperty(
                            MessageContext.CHARACTER_SET_ENCODING);
            if (charSetEnc != null) {
                omOutput.setCharSetEncoding(charSetEnc);
            } else {
                OperationContext opctx = msgContext.getOperationContext();
                if (opctx != null) {
                    charSetEnc = (String) opctx.getProperty(MessageContext.CHARACTER_SET_ENCODING);
                }
            }
            /**
             * If the char set enc is still not found use the default
             */
            if (charSetEnc == null) {
                charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }
            msgContext.setDoingMTOM(HTTPTransportUtils.doWriteMTOM(msgContext));
            omOutput.setSoap11(msgContext.isSOAP11());
            omOutput.setDoOptimize(msgContext.isDoingMTOM());

            omOutput.setCharSetEncoding(charSetEnc);

            // Trasnport URL can be different from the WSA-To. So processing that now.
            EndpointReference epr = null;
            String transportURL = (String) msgContext.getProperty(MessageContextConstants.TRANSPORT_URL);
            if (transportURL != null) {
                epr = new EndpointReference(transportURL);
            } else if (msgContext.getTo() != null
                    && !AddressingConstants.Submission.WSA_ANONYMOUS_URL.equals(
                    msgContext.getTo().getAddress())
                    && !AddressingConstants.Final.WSA_ANONYMOUS_URL.equals(
                    msgContext.getTo().getAddress())) {

                epr = msgContext.getTo();
            }

            //Check for the REST behaviour, if you desire rest beahaviour
            //put a <parameter name="doREST" value="true"/> at the
            // server.xml/client.xml file

            // ######################################################
            //Change this place to change the wsa:toepr
            //epr = something
            // ######################################################

            OMElement dataOut;
            /**
             * Figuringout the REST properties/parameters
             */
            msgContext.setDoingREST(HTTPTransportUtils.isDoingREST(msgContext));
            msgContext.setRestThroughPOST(HTTPTransportUtils.isDoingRESTThoughPost(msgContext));
            boolean isRest = msgContext.isDoingREST();

            if (isRest) {
                dataOut = msgContext.getEnvelope().getBody().getFirstElement();
            } else {
                dataOut = msgContext.getEnvelope();
            }

            //TODO timeout, configuration
            if (epr != null) {
                writeMessageWithCommons(msgContext, epr, dataOut);
            } else {
                OutputStream out =
                        (OutputStream) msgContext.getProperty(
                                MessageContext.TRANSPORT_OUT);
                if (msgContext.isServerSide()) {
                    HTTPOutTransportInfo transportInfo =
                            (HTTPOutTransportInfo) msgContext.getProperty(
                                    HTTPConstants.HTTPOutTransportInfo);
                    if (transportInfo != null) {
                        omOutput.setSoap11(msgContext.isSOAP11());
                        //this is the servlet2.3 way of setting encodings
                        String contentType = findContentType(isRest, msgContext);
                        String encoding = contentType + "; charset=" + omOutput.getCharSetEncoding();
                        transportInfo.setContentType(encoding);
                    } else {
                        throw new AxisFault(HTTPConstants.HTTPOutTransportInfo + " does not set");
                    }
                }
                omOutput.setOutputStream(out, msgContext.isDoingMTOM());
                ((OMNodeEx)dataOut).serializeAndConsume(omOutput);
                omOutput.flush();
            }
            if (msgContext.getOperationContext() != null) {
                msgContext.getOperationContext().setProperty(
                        Constants.RESPONSE_WRITTEN,
                        Constants.VALUE_TRUE);
            }
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError e) {
            throw new AxisFault(e);
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    private String findContentType(boolean isRest, MessageContext msgContext) {
        if (isRest) {
            if (msgContext.getProperty(HTTPConstants.HTTP_CONTENT_TYPE) != null) {
                String contentType = (String) msgContext.getProperty(HTTPConstants.HTTP_CONTENT_TYPE);
                //get the users setting from the axis2.xml parameters
                //if present return that
                //else return the default (application/xml)
                return contentType;
            } else {
                return "application/xml";
            }
        } else {
            return omOutput.getContentType();
        }
    }

    public void writeMessageWithCommons(
            MessageContext msgContext,
            EndpointReference toURL,
            OMElement dataout)
            throws AxisFault {
        try {
            URL url = new URL(toURL.getAddress());

            String soapActionString = msgContext.getSoapAction();
            if (soapActionString == null || soapActionString.length() == 0) {
                soapActionString = msgContext.getWSAAction();
            }
            if (soapActionString == null) {
                soapActionString = "";
            }
            //supporting RESTFacility..

            if (!msgContext.isDoingREST()) {
                this.transportConfigurationPOST(
                        msgContext,
                        dataout,
                        url,
                        soapActionString);
            }else {
                if(msgContext.isRestThroughPOST()){
                   this.transportConfigurationPOST(
                            msgContext,
                            dataout,
                            url,
                            soapActionString);
                }else {
                     this.transportConfigurationGET(msgContext, url);
                }
            }
        } catch (MalformedURLException e) {
            throw new AxisFault(e);
        } catch (HttpException e) {
            throw new AxisFault(e);
        } catch (IOException e) {
            throw new AxisFault(e);
        }

    }
    // POST application/x-www-form-urlencoded

    public class PostAxisRequestEntity implements RequestEntity {

        private String charSetEnc;
        private String postRequestBody;
        private MessageContext msgCtxt;
        private String contentType;

        public PostAxisRequestEntity(String postRequestBody, String charSetEnc, MessageContext msgCtxt, String contentType) {
            this.postRequestBody = postRequestBody;
            this.charSetEnc = charSetEnc;
            this.msgCtxt = msgCtxt;
            this.contentType = contentType;
        }

        public boolean isRepeatable() {
            return true;
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

    }

    //get the contentLength...
    public class AxisRequestEntity implements RequestEntity {

        private String charSetEnc;

        private OMElement element;

        private boolean chuncked;

        private byte[] bytes;

        private boolean doingMTOM = false;

        private String soapActionString;

        private MessageContext msgCtxt;

        public AxisRequestEntity(
                OMElement element,
                boolean chuncked,
                MessageContext msgCtxt,
                String charSetEncoding,
                String soapActionString) {
            this.element = element;
            this.chuncked = chuncked;
            this.msgCtxt = msgCtxt;
            this.doingMTOM = msgCtxt.isDoingMTOM();
            this.charSetEnc = charSetEncoding;
            this.soapActionString = soapActionString;
        }

        public boolean isRepeatable() {
            return true;
        }

        public byte[] writeBytes() throws AxisFault {
            try {
                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

                if (!doingMTOM) {
                    XMLStreamWriter outputWriter =
                            XMLOutputFactory.newInstance()
                                    .createXMLStreamWriter(bytesOut,
                                            charSetEnc);
                    OMOutputImpl output = new OMOutputImpl(outputWriter);
                    output.setCharSetEncoding(charSetEnc);
                    ((OMNodeEx)element).serializeAndConsume(output);
                    output.flush();
                    return bytesOut.toByteArray();

                } else {
                    omOutput.setCharSetEncoding(charSetEnc);
                    omOutput.setOutputStream(bytesOut, true);  //changed...
                    ((OMNodeEx)element).serializeAndConsume(omOutput);
                    omOutput.flush();
                    return bytesOut.toByteArray();
                }
            } catch (XMLStreamException e) {
                throw new AxisFault(e);
            } catch (FactoryConfigurationError e) {
                throw new AxisFault(e);
            }
        }

        private void handleOMOutput(OutputStream out, boolean doingMTOM)
                throws XMLStreamException {
            omOutput.setOutputStream(out, doingMTOM);
            ((OMNodeEx)element).serializeAndConsume(omOutput);
            omOutput.flush();
        }

        public void writeRequest(OutputStream out) throws IOException {
            try {
                if (doingMTOM) { //chagened ..
                    if (chuncked) {
                        this.handleOMOutput(out, doingMTOM);
                    } else {
                        if (bytes == null) {
                            bytes = writeBytes();
                        }
                        out.write(bytes);
                    }

                } else {
                    if (chuncked) {
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
                if (doingMTOM) {    //chagened
                    if (chuncked) {
                        return -1;
                    } else {
                        if (bytes == null) {
                            bytes = writeBytes();
                        }
                        return bytes.length;
                    }
                } else {
                    if (chuncked) {
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

            String encoding = omOutput.getCharSetEncoding();
            String contentType = omOutput.getContentType();
            if (encoding != null) {
                contentType += "; charset=" + encoding;
            }

            // action header is not mandated in SOAP 1.2. So putting it, if available
            if (!msgCtxt.isSOAP11() && soapActionString != null && !"".equals(soapActionString.trim())) {
                contentType = contentType + ";action=\"" + soapActionString + "\";";
            }
            return contentType;
        }
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.transport.TransportSender#cleanUp(org.apache.axis2.context.MessageContext)
    */
    public void cleanUp(MessageContext msgContext) throws AxisFault {
        HttpMethod httpMethod =
                (HttpMethod) msgContext.getProperty(HTTP_METHOD);
        if (httpMethod != null) {
            httpMethod.releaseConnection();
        }

    }

    public void init(
            ConfigurationContext confContext,
            TransportOutDescription transportOut)
            throws AxisFault {
        //<parameter name="PROTOCOL" locked="false">HTTP/1.0</parameter> or
        //<parameter name="PROTOCOL" locked="false">HTTP/1.1</parameter> is
        // checked
        Parameter version =
                transportOut.getParameter(HTTPConstants.PROTOCOL_VERSION);
        if (version != null) {
            if (HTTPConstants.HEADER_PROTOCOL_11.equals(version.getValue())) {
                this.httpVersion = HTTPConstants.HEADER_PROTOCOL_11;
                Parameter transferEncoding =
                        transportOut.getParameter(
                                HTTPConstants.HEADER_TRANSFER_ENCODING);
                if (transferEncoding != null
                        && HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED.equals(
                        transferEncoding.getValue())) {
                    this.chuncked = true;
                }
            } else if (
                    HTTPConstants.HEADER_PROTOCOL_10.equals(version.getValue())) {
                this.httpVersion = HTTPConstants.HEADER_PROTOCOL_10;
            } else {
                throw new AxisFault(
                        "Parameter "
                                + HTTPConstants.PROTOCOL_VERSION
                                + " Can have values only HTTP/1.0 or HTTP/1.1");
            }
        }

        //Get the timeout values from the configuration
        try {
            Parameter tempSoTimeoutParam = transportOut
                    .getParameter(HTTPConstants.SO_TIMEOUT);
            Parameter tempConnTimeoutParam = transportOut
                    .getParameter(HTTPConstants.CONNECTION_TIMEOUT);

            if (tempSoTimeoutParam != null) {
                soTimeout = Integer.parseInt((String) tempSoTimeoutParam
                        .getValue());
            }

            if (tempConnTimeoutParam != null) {
                connectionTimeout = Integer
                        .parseInt((String) tempConnTimeoutParam.getValue());
            }

        } catch (NumberFormatException nfe) {
            //If there's a problem log it and use the default values
            log.error("Invalid timeout value format: not a number", nfe);
        }
    }

    private void transportConfigurationPOST(
            MessageContext msgContext,
            OMElement dataout,
            URL url,
            String soapActionString)
            throws MalformedURLException, AxisFault, IOException {

        //execuite the HtttpMethodBase - a connection manager can be given for handle multiple
        httpClient = new HttpClient();
        //hostConfig handles the socket functions..
        //HostConfiguration hostConfig = getHostConfiguration(msgContext, url);

        //Get the timeout values set in the runtime
        getTimeoutValues(msgContext);

        // SO_TIMEOUT -- timeout for blocking reads
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeout);
        // timeout for initial connection
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(connectionTimeout);

        //todo giving proxy and NTLM support

        PostMethod postMethod = new PostMethod(url.toString());
        String contentType = findContentType(true, msgContext);

        msgContext.setProperty(HTTP_METHOD, postMethod);
        String charEncoding =
                (String) msgContext.getProperty(
                        MessageContext.CHARACTER_SET_ENCODING);
        if (charEncoding == null) {
            charEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        //if POST as application/x-www-form-urlencoded
        RequestData reqData = null;
        if (contentType.equalsIgnoreCase(HTTPConstants.MEDIA_TYPE_X_WWW_FORM)) {
            reqData = createRequest(msgContext);
            postMethod.setPath(url.getPath() + ((reqData.urlRequest) != null ? ("?" + reqData.urlRequest) : ""));
            postMethod.setRequestEntity(new PostAxisRequestEntity(reqData.bodyRequest, charEncoding, msgContext, contentType));

        } else {
            postMethod.setPath(url.getPath());


            postMethod.setRequestEntity(
                    new AxisRequestEntity(
                            dataout,
                            chuncked,
                            msgContext,
                            charEncoding,
                            soapActionString));
        }

        if (!httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10)
                && chuncked) {
            postMethod.setContentChunked(true);
        }
        postMethod.setRequestHeader(
                HTTPConstants.HEADER_USER_AGENT,
                "Axis/2.0");
        if (msgContext.isSOAP11() && !msgContext.isDoingREST()) {
            postMethod.setRequestHeader(
                    HTTPConstants.HEADER_SOAP_ACTION,
                    soapActionString);
        }
        postMethod.setRequestHeader(HTTPConstants.HEADER_HOST, url.getHost());
        if (httpVersion != null) {
            if (httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10)) {

                httpClient.getParams().setVersion(HttpVersion.HTTP_1_0);
                postMethod.setRequestHeader(
                        HTTPConstants.HEADER_CONNECTION,
                        HTTPConstants.HEADER_CONNECTION_KEEPALIVE);
            } else {
                // allowing keep-alive for 1.1
                postMethod.setRequestHeader(
                        HTTPConstants.HEADER_CONNECTION,
                        HTTPConstants.HEADER_CONNECTION_KEEPALIVE);
                postMethod.setRequestHeader(
                        HTTPConstants.HEADER_EXPECT,
                        HTTPConstants.HEADER_EXPECT_100_Continue);
            }
        }

        /**
         * main excecution takes place..
         */

        HostConfiguration config = this.getHostConfiguration(httpClient, msgContext, url);

        this.httpClient.executeMethod(config, postMethod);

        if (postMethod.getStatusCode() == HttpStatus.SC_OK) {
            processResponse(postMethod, msgContext);
            return;
        } else if (postMethod.getStatusCode() == HttpStatus.SC_ACCEPTED) {
            return;
        } else if (postMethod.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            Header contenttypeHheader = postMethod.getResponseHeader(
                    HTTPConstants.HEADER_CONTENT_TYPE);

            if (contenttypeHheader != null) {
                String value = contenttypeHheader.getValue();
                if (value.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) >= 0 ||
                        value.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) >= 0) {
                    processResponse(postMethod, msgContext);
                    return;
                }
            }
        }
        throw new AxisFault(
                Messages.getMessage(
                        "transportError",
                        String.valueOf(postMethod.getStatusCode()),
                        postMethod.getResponseBodyAsString()));
    }

    /**
     * This is used to get the dynamically set time out values from the
     * message context. If the values are not available or invalid then
     * teh default values or the values set by teh configuration will be used
     *
     * @param msgContext
     */
    private void getTimeoutValues(MessageContext msgContext) {
        try {
            // If the SO_TIMEOUT of CONNECTION_TIMEOUT is set by dynamically the
            // override the static config
            Integer tempSoTimeoutProperty = (Integer) msgContext
                    .getProperty(HTTPConstants.SO_TIMEOUT);
            Integer tempConnTimeoutProperty = (Integer) msgContext
                    .getProperty(HTTPConstants.CONNECTION_TIMEOUT);

            if (tempSoTimeoutProperty != null) {
                soTimeout = tempSoTimeoutProperty.intValue();
            }

            if (tempConnTimeoutProperty != null) {
                connectionTimeout = tempConnTimeoutProperty.intValue();
            }
        } catch (NumberFormatException nfe) {
            //If there's a problem log it and use the default values
            log.error("Invalid timeout value format: not a number", nfe);
        }
    }

    private void processResponse(HttpMethodBase httpMethod, MessageContext msgContext) throws IOException {
        obatainHTTPHeaderInformation(httpMethod, msgContext);
        InputStream in = httpMethod.getResponseBodyAsStream();
        if (in == null) {
            throw new AxisFault(
                    Messages.getMessage("canNotBeNull", "InputStream"));
        }
        msgContext.getOperationContext().setProperty(
                MessageContext.TRANSPORT_IN,
                in);
    }

    //Method to return the parameter string to pass with the URL when using GET

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
            paraString = paraString + "&" + c;
        }
        return paraString;
    }

    private void transportConfigurationGET(MessageContext msgContext, URL url)
            throws MalformedURLException, AxisFault, IOException {

        String param = getParam(msgContext);
        GetMethod getMethod = new GetMethod();
        getMethod.setPath(url.getFile() + "?" + param);

        //Serialization as "application/x-www-form-urlencoded"

        String charEncoding =
                (String) msgContext.getProperty(
                        MessageContext.CHARACTER_SET_ENCODING);
        if (charEncoding == null) //Default encoding scheme
            getMethod.setRequestHeader(
                    HTTPConstants.HEADER_CONTENT_TYPE,
                    HTTPConstants.MEDIA_TYPE_X_WWW_FORM + "; charset="
                            + MessageContext.DEFAULT_CHAR_SET_ENCODING);
        else
            getMethod.setRequestHeader(
                    HTTPConstants.HEADER_CONTENT_TYPE,
                    HTTPConstants.MEDIA_TYPE_X_WWW_FORM + "; charset=" + charEncoding);

        this.httpClient = new HttpClient();


        HostConfiguration hostConfig = this.getHostConfiguration(httpClient, msgContext, url);
        //this.getHostConfiguration(msgContext, url);

        //Get the timeout values set in the runtime
        getTimeoutValues(msgContext);

        // SO_TIMEOUT -- timeout for blocking reads
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeout);
        // timeout for initial connection
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(connectionTimeout);

        /**
         * with HostConfiguration
         */
        this.httpClient.executeMethod(hostConfig, getMethod, null);

        if (getMethod.getStatusCode() == HttpStatus.SC_OK) {
            processResponse(getMethod, msgContext);
        } else if (getMethod.getStatusCode() == HttpStatus.SC_ACCEPTED) {
            return;
        } else if (getMethod.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {

            Header contenttypeHheader = getMethod.getResponseHeader(
                    HTTPConstants.HEADER_CONTENT_TYPE);

            String value = contenttypeHheader.getValue();
            if (value != null) {
                if (value.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) >= 0 ||
                        value.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) >= 0) {
                    processResponse(getMethod, msgContext);
                }

            }
        } else {
            throw new AxisFault(
                    Messages.getMessage(
                            "transportError",
                            String.valueOf(getMethod.getStatusCode()),
                            getMethod.getResponseBodyAsString()));
        }
    }

    /**
     * Collect the HTTP header information and set them in the message context
     *
     * @param method
     * @param msgContext
     */
    private void obatainHTTPHeaderInformation(HttpMethodBase method, MessageContext msgContext) {
        Header header =
                method.getResponseHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        if (header != null) {
            HeaderElement[] headers = header.getElements();
            for (int i = 0; i < headers.length; i++) {
                NameValuePair charsetEnc = headers[i]
                        .getParameterByName(HTTPConstants.CHAR_SET_ENCODING);
                if (headers[i]
                        .getName()
                        .equalsIgnoreCase(
                                HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED)) {
                    OperationContext opContext =
                            msgContext.getOperationContext();
                    if (opContext != null) {
                        opContext.setProperty(
                                HTTPConstants.MTOM_RECIVED_CONTENT_TYPE,
                                header.getValue());
                    }
                } else if (charsetEnc != null) {

                    msgContext
                            .setProperty(
                                    MessageContext.CHARACTER_SET_ENCODING,
                                    charsetEnc.getValue());  //change to the value, which is text/xml or application/xml+soap
                }
            }
        }

    }

    /**
     * getting host configuration to support standard http/s, proxy and NTLM support
     */
    private HostConfiguration getHostConfiguration(HttpClient client, MessageContext msgCtx, URL targetURL) throws AxisFault {
        boolean isHostProxy = isProxyListed(msgCtx); //list the proxy
        int port = targetURL.getPort();
        if (port == -1) port = 80;
        // to see the host is a proxy and in the proxy list - available in axis2.xml
        HostConfiguration config = new HostConfiguration();

        if (!isHostProxy) {
            config.setHost(targetURL.getHost(), port, targetURL.getProtocol());
        } else {
            //proxy and NTLM configuration
            this.configProxyAuthentication(client, proxyOutSetting, config, msgCtx);
        }

        return config;
    }

    private boolean isProxyListed(MessageContext msgCtx) throws AxisFault {
        boolean returnValue = false;
        Parameter par = null;
        proxyOutSetting = msgCtx.getSystemContext()
                .getAxisConfiguration()
                .getTransportOut(new QName(Constants.TRANSPORT_HTTP));
        if (proxyOutSetting != null) {
            par = proxyOutSetting.getParameter(HTTPConstants.PROXY);
        }
        OMElement hostElement = null;
        if (par != null) {
            hostElement =
                    par.getParameterElement();
        } else {
            return returnValue;
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
        HttpTransportProperties.ProxyProperties proxyProperties = null;
        if ((proxyProperties = (HttpTransportProperties.ProxyProperties) msgCtx
                .getProperty(HTTPConstants.PROXY)) != null) {
            if (proxyProperties.getProxyHostName() != null) {
                returnValue = true;
            }
        }
        return returnValue;
    }

    /**
     * Helper method to Proxy and NTLM authentication
     *
     * @param client
     * @param proxySetting
     * @param config
     */

    private void configProxyAuthentication(HttpClient client,
                                           TransportOutDescription proxySetting,
                                           HostConfiguration config, MessageContext msgCtx) throws AxisFault {
        Parameter proxyParam = proxySetting.getParameter(HTTPConstants.PROXY);
        String value = (String) proxyParam.getValue();
        String split[] = value.split(":");
        //values being hard coded due best practise
        String usrName = split[0];
        String domain = split[1];
        String passwd = split[2];
        //
        Credentials proxyCred = null;

        String proxyHostName = null;
        int proxyPort = -1;

        if (proxyParam != null) {
            OMElement proxyParamElement = proxyParam.getParameterElement();
            Iterator ite = proxyParamElement.getAllAttributes();
            while (ite.hasNext()) {
                OMAttribute att = (OMAttribute) ite.next();
                if (att.getLocalName().equalsIgnoreCase(PROXY_HOST_NAME)) {
                    proxyHostName = att.getAttributeValue();
                }
                if (att.getLocalName().equalsIgnoreCase(PROXY_PORT)) {
                    proxyPort = new Integer(att.getAttributeValue()).intValue();
                }
            }

        }

        if (domain.equals("") || domain == null || domain.equals(ANONYMOUS)) {
            if (usrName.equals(ANONYMOUS) && passwd.equals(ANONYMOUS)) {
                proxyCred = new UsernamePasswordCredentials("", "");
            } else {
                proxyCred = new UsernamePasswordCredentials(usrName,
                        passwd); //proxy
            }
        } else {
            proxyCred = new NTCredentials(usrName, passwd, proxyHostName,
                    domain); //NTLM authentication with additionals prams
        }


        HttpTransportProperties.ProxyProperties proxyProperties = (HttpTransportProperties.ProxyProperties) msgCtx.getProperty(HTTPConstants.PROXY);
        if (proxyProperties != null) {
            if (proxyProperties.getProxyPort() != -1) {
                proxyPort = proxyProperties.getProxyPort();
            }
            if (proxyProperties.getProxyHostName().equals("") || proxyProperties.getProxyHostName() != null) {
                proxyHostName = proxyProperties.getProxyHostName();
            } else {
                throw new AxisFault("Proxy Name is not valied");
            }
            if (proxyProperties.getUserName().equals(ANONYMOUS) || proxyProperties.getPassWord().equals(ANONYMOUS)) {
                proxyCred = new UsernamePasswordCredentials("", "");
            } else {
                usrName = proxyProperties.getUserName();
                passwd = proxyProperties.getPassWord();
                domain = proxyProperties.getPassWord();
            }
        }
        client.getState().setProxyCredentials(AuthScope.ANY, proxyCred);
        config.setProxy(proxyHostName, proxyPort);
    }

    //
    private class RequestData {
        String urlRequest;
        String bodyRequest;
    }

    String urlRequest;
    String bodyRequest;
}


