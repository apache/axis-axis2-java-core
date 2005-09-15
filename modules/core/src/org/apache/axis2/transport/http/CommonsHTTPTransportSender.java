/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*/

package org.apache.axis2.transport.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.transport.TransportSender;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

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

public class CommonsHTTPTransportSender
        extends AbstractHandler
        implements TransportSender {
    private boolean chuncked = false;

    private String httpVersion = HTTPConstants.HEADER_PROTOCOL_11;

    public static final String HTTP_METHOD = "HTTP_METHOD";

    protected HttpClient httpClient;

    protected OMElement outputMessage;

    protected OMOutputImpl omOutput = new OMOutputImpl();

    public CommonsHTTPTransportSender() {
    } //default

    public void invoke(MessageContext msgContext) throws AxisFault {
        try {
            String charSetEnc =
                    (String) msgContext.getProperty(
                            MessageContext.CHARACTER_SET_ENCODING);
            if (charSetEnc != null) {
                omOutput.setCharSetEncoding(charSetEnc);
            } else {
                OperationContext opctx = msgContext.getOperationContext();
                if(opctx != null) {
                    charSetEnc = (String)opctx.getProperty(MessageContext.CHARACTER_SET_ENCODING);
                }
            }
            /**
             * If the char set enc is still not found use the default
             */
            if(charSetEnc == null) {
                charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }
            msgContext.setDoingMTOM(HTTPTransportUtils.doWriteMTOM(msgContext));
            omOutput.setSoap11(msgContext.isSOAP11());
            omOutput.setDoOptimize(msgContext.isDoingMTOM());

            omOutput.setCharSetEncoding(charSetEnc);
            
            //Check for the REST behaviour, if you desire rest beahaviour
            //put a <parameter name="doREST" value="true"/> at the
            // server.xml/client.xml file
            EndpointReference epr = null;
            if (msgContext.getTo() != null
                    && !AddressingConstants.Submission.WSA_ANONYMOUS_URL.equals(
                            msgContext.getTo().getAddress())
                    && !AddressingConstants.Final.WSA_ANONYMOUS_URL.equals(
                            msgContext.getTo().getAddress())) {
                epr = msgContext.getTo();
            }

            OMElement dataOut;
            if (msgContext.isDoingREST()) {
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
                        transportInfo.setCharacterEncoding(omOutput.getCharSetEncoding());
                        transportInfo.setContentType(omOutput.getContentType());
                    }else{
                        throw new AxisFault(HTTPConstants.HTTPOutTransportInfo + " does not set");
                    }
                }
                omOutput.setOutputStream(out, msgContext.isDoingMTOM());
                dataOut.serialize(omOutput);
                omOutput.flush();
            }
            if(msgContext.getOperationContext() != null){
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

    public void writeMessageWithToOutPutStream(
            MessageContext msgContext,
            OutputStream out) {

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
            }
            if (msgContext.isDoingREST()) {
                if (msgContext.isRestThroughPOST()) {
                    this.transportConfigurationPOST(
                            msgContext,
                            dataout,
                            url,
                            soapActionString);
                } else {
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
    protected HostConfiguration getHostConfiguration(
            MessageContext context,
            URL targetURL) {
        //TODO cheaking wheather the host is a proxy
        HostConfiguration config = new HostConfiguration();
        config.setHost(
                targetURL.getHost(),
                targetURL.getPort() == -1 ? 80 : targetURL.getPort());
        return config;
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
                    element.serialize(output);
                    output.flush();
                    return bytesOut.toByteArray();
                } else {
                    omOutput.setCharSetEncoding(charSetEnc);
                    omOutput.setOutputStream(bytesOut, true);  //changed...
                    element.serialize(omOutput);
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
            element.serialize(omOutput);
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
            if(encoding != null){
                contentType += ";charset=" + encoding;
            }

            // action header is not mandated in SOAP 1.2. So putting it, if available
            if(!msgCtxt.isSOAP11() && soapActionString != null && !"".equals(soapActionString.trim())) {
                contentType = contentType + ";action=" + soapActionString + ";";
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

        // SO_TIMEOUT -- timeout for blocking reads
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(60000);
        // timeout for initial connection
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(60000);

        PostMethod postMethod = new PostMethod(url.toString());
        postMethod.setPath(url.getFile());

        msgContext.setProperty(HTTP_METHOD, postMethod);

        String charEncoding =
                (String) msgContext.getProperty(
                        MessageContext.CHARACTER_SET_ENCODING);
        if(charEncoding == null){
            charEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        postMethod.setRequestEntity(
                new AxisRequestEntity(
                        dataout,
                        chuncked,
                        msgContext,
                        charEncoding,
                        soapActionString));


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
                //postMethod.setHttp11(false); todo method to findout the transport version...
                //allowing keep-alive for 1.0
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
        
        this.httpClient.executeMethod(postMethod);

        if (postMethod.getStatusCode() == HttpStatus.SC_OK) {
            processResponse(postMethod, msgContext);
        } else if (postMethod.getStatusCode() == HttpStatus.SC_ACCEPTED) {
            return;
        } else  if (postMethod.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR){

            Header contenttypeHheader = postMethod.getResponseHeader(
                    HTTPConstants.HEADER_CONTENT_TYPE);

            String value = contenttypeHheader.getValue();
            if(value != null){
                if(value.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE)>=0||
                        value.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) >=0){
                    processResponse(postMethod, msgContext);
                }
            }
        }else{
            throw new AxisFault(
                    Messages.getMessage(
                            "transportError",
                            String.valueOf(postMethod.getStatusCode()),
                            postMethod.getResponseBodyAsString()));
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

    private void transportConfigurationGET(MessageContext msgContext, URL url)
            throws MalformedURLException, AxisFault, IOException {
        GetMethod getMethod = new GetMethod();
        getMethod.setPath(url.getFile());

        String charEncoding =
                (String) msgContext.getProperty(
                        MessageContext.CHARACTER_SET_ENCODING);
        if (charEncoding == null) //Default encoding scheme
            getMethod.setRequestHeader(
                    HTTPConstants.HEADER_CONTENT_TYPE,
                    "text/xml; charset="
                    + MessageContext.DEFAULT_CHAR_SET_ENCODING);
        else
            getMethod.setRequestHeader(
                    HTTPConstants.HEADER_CONTENT_TYPE,
                    "text/xml; charset=" + charEncoding);

        this.httpClient = new HttpClient();
        HostConfiguration hostConfig =
                this.getHostConfiguration(msgContext, url);

        this.httpClient.executeMethod(hostConfig, getMethod);

        if (getMethod.getStatusCode() == HttpStatus.SC_OK) {
            processResponse(getMethod, msgContext);

//            obatainHTTPHeaderInformation(getMethod, msgContext);
//
//            InputStream in = getMethod.getResponseBodyAsStream();
//            if (in == null) {
//                throw new AxisFault(
//                        Messages.getMessage("canNotBeNull", "InputStream"));
//            }
//            msgContext.getOperationContext().setProperty(
//                    MessageContext.TRANSPORT_IN,
//                    in);
        } else if (getMethod.getStatusCode() == HttpStatus.SC_ACCEPTED) {
            return;
        }else  if (getMethod.getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR){

            Header contenttypeHheader = getMethod.getResponseHeader(
                    HTTPConstants.HEADER_CONTENT_TYPE);

             String value = contenttypeHheader.getValue();
            if(value != null){
                if(value.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE)>=0||
                        value.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) >=0){
                    processResponse(getMethod, msgContext);
                }

            }
        }else{
            throw new AxisFault(
                    Messages.getMessage(
                            "transportError",
                            String.valueOf(getMethod.getStatusCode()),
                            getMethod.getResponseBodyAsString()));
        }
    }

    /**
     * Collect the HTTP header information and set them in the message context
     * @param method
     * @param msgContext
     */
    private void obatainHTTPHeaderInformation(HttpMethodBase method,MessageContext msgContext) {
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
                                    charsetEnc);
                }
            }
        }

    }

}