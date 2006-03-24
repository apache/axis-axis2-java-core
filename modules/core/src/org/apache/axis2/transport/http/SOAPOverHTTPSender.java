package org.apache.axis2.transport.http;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
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
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class SOAPOverHTTPSender extends AbstractHTTPSender {


    public void send(MessageContext msgContext, OMElement dataout, URL url, String soapActionString)
            throws MalformedURLException, AxisFault, IOException {

        // execute the HtttpMethodBase - a connection manager can be given for
        // handle multiple
        httpClient = new HttpClient();

        // Get the timeout values set in the runtime
        getTimeoutValues(msgContext);

        // SO_TIMEOUT -- timeout for blocking reads
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(soTimeout);

        // timeout for initial connection
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(connectionTimeout);

        PostMethod postMethod = new PostMethod(url.toString());

        String charEncoding =
                (String) msgContext.getProperty(MessageContext.CHARACTER_SET_ENCODING);

        if (charEncoding == null) {
            charEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        postMethod.setPath(url.getPath());
        postMethod.setRequestEntity(new AxisSOAPRequestEntity(dataout, chunked, msgContext,
                charEncoding, soapActionString));

        if (!httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10) && chunked) {
            postMethod.setContentChunked(true);
        }

        postMethod.setRequestHeader(HTTPConstants.HEADER_USER_AGENT, "Axis/2.0");

        if (msgContext.isSOAP11()) {
            postMethod.setRequestHeader(HTTPConstants.HEADER_SOAP_ACTION, soapActionString);
        } else {
        }
        //setting the coolie in the out path
        Object cookieString = msgContext.getProperty(Constants.COOKIE_STRING);
        if (cookieString != null) {
            String cookie =(String) cookieString;
            int index = cookie.indexOf(";");
            if(index >0){
                cookie = cookie.substring(0,index);
            }
            postMethod.setRequestHeader(HTTPConstants.HEADER_COOKIE,cookie);
            postMethod.setRequestHeader(HTTPConstants.HEADER_COOKIE2, cookie);
        }

        postMethod.setRequestHeader(HTTPConstants.HEADER_HOST, url.getHost());

        if (httpVersion != null) {
            if (httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10)) {
                httpClient.getParams().setVersion(HttpVersion.HTTP_1_0);
//                postMethod.setRequestHeader(HTTPConstants.HEADER_CONNECTION,
//                        HTTPConstants.HEADER_CONNECTION_KEEPALIVE);
            } else {

                // allowing keep-alive for 1.1
//                postMethod.setRequestHeader(HTTPConstants.HEADER_CONNECTION,
//                        HTTPConstants.HEADER_CONNECTION_KEEPALIVE);
                postMethod.setRequestHeader(HTTPConstants.HEADER_EXPECT,
                        HTTPConstants.HEADER_EXPECT_100_Continue);
            }
        }

        /*
         *   main excecution takes place..
         */
        HostConfiguration config = this.getHostConfiguration(httpClient, msgContext, url);

        this.httpClient.executeMethod(config, postMethod);

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
            throw new AxisFault("Axis2 transport error : " + postMethod.getStatusText(), SOAP12Constants.FAULT_CODE_SENDER);
        }

        throw new AxisFault(Messages.getMessage("transportError",
                String.valueOf(postMethod.getStatusCode()), postMethod.getResponseBodyAsString()));
    }

    public class AxisSOAPRequestEntity implements RequestEntity {
        private boolean doingMTOM = false;
        private byte[] bytes;
        private String charSetEnc;
        private boolean chunked;
        private OMElement element;
        private MessageContext msgCtxt;
        private String soapActionString;

        public AxisSOAPRequestEntity(OMElement element, boolean chunked, MessageContext msgCtxt,
                                     String charSetEncoding, String soapActionString) {
            this.element = element;
            this.chunked = chunked;
            this.msgCtxt = msgCtxt;
            this.doingMTOM = msgCtxt.isDoingMTOM();
            this.charSetEnc = charSetEncoding;
            this.soapActionString = soapActionString;
        }

        private void handleOMOutput(OutputStream out, boolean doingMTOM) throws XMLStreamException {
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
                if (chunked) {
                    this.handleOMOutput(out, doingMTOM);
                } else {
                    if (bytes == null) {
                        bytes = writeBytes();
                    }

                    out.write(bytes);
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
                if (chunked) {
                    return -1;
                } else {
                    if (bytes == null) {
                        bytes = writeBytes();
                    }

                    return bytes.length;
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
                    && !"".equals(soapActionString.trim())) {
                contentType = contentType + ";action=\"" + soapActionString + "\";";
            }

            return contentType;
        }

        public boolean isRepeatable() {
            return true;
        }
    }
}
