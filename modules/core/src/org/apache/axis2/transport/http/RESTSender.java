package org.apache.axis2.transport.http;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class RESTSender extends AbstractHTTPSender {
    private Log log = LogFactory.getLog(getClass());

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

        getMethod.setPath(url.getFile() + "?" + param);

        // Serialization as "application/x-www-form-urlencoded"
        String charEncoding =
                (String) msgContext.getProperty(MessageContext.CHARACTER_SET_ENCODING);

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

        this.httpClient = new HttpClient();

        HostConfiguration hostConfig = this.getHostConfiguration(httpClient, msgContext, url);

        // Get the timeout values set in the runtime
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
        httpClient = new HttpClient();

        // Get the timeout values set in the runtime
        getTimeoutValues(msgContext);

        // SO_TIMEOUT -- timeout for blocking reads
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeout);

        // timeout for initial connection
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(connectionTimeout);

        // todo giving proxy and NTLM support
        PostMethod postMethod = new PostMethod(url.toString());
        String httpContentType;

        if (msgContext.getProperty(Constants.Configuration.CONTENT_TYPE) != null) {
            httpContentType = (String) msgContext.getProperty(Constants.Configuration.CONTENT_TYPE);
        } else {
            httpContentType = HTTPConstants.MEDIA_TYPE_APPLICATION_XML;
        }

        String charEncoding =
                (String) msgContext.getProperty(MessageContext.CHARACTER_SET_ENCODING);

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
            postMethod.setRequestEntity(new AxisRESTRequestEntity(reqData.bodyRequest,
                                                                  charEncoding, msgContext, httpContentType));

        } else {
            postMethod.setPath(url.getPath());
            postMethod.setRequestEntity(new AxisRequestEntity(dataout, chunked, msgContext,
                                                              charEncoding, soapActionString));
        }

        if (!httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10) && chunked) {
            postMethod.setContentChunked(true);
        }

        postMethod.setRequestHeader(HTTPConstants.HEADER_USER_AGENT, "Axis/2.0");

        if (msgContext.isSOAP11() && !msgContext.isDoingREST()) {
            postMethod.setRequestHeader(HTTPConstants.HEADER_SOAP_ACTION, soapActionString);
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
            HostConfiguration config = this.getHostConfiguration(httpClient, msgContext, url);

            this.httpClient.executeMethod(config, postMethod);

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

    public class AxisRESTRequestEntity implements RequestEntity {
        private String charSetEnc;
        private String contentType;
        private MessageContext msgCtxt;
        private String postRequestBody;

        public AxisRESTRequestEntity(String postRequestBody, String charSetEnc,
                                     MessageContext msgCtxt, String contentType) {
            this.postRequestBody = postRequestBody;
            this.charSetEnc = charSetEnc;
            this.msgCtxt = msgCtxt;
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
