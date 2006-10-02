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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPOutputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.MIMEOutputUtils;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

public class SOAPOverHTTPSender extends AbstractHTTPSender {


    public void send(MessageContext msgContext, OMElement dataout, URL url, String soapActionString)
            throws MalformedURLException, AxisFault, IOException {

        // execute the HtttpMethodBase - a connection manager can be given for
        // handle multiple
        HttpClient httpClient = getHttpClient(msgContext);
        PostMethod postMethod = new PostMethod(url.toString());
        if (isAuthenticationEnabled(msgContext)) {
            postMethod.setDoAuthentication(true);
        }

        String charEncoding =
                (String) msgContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);

        if (charEncoding == null) {
            charEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
        }

        postMethod.setPath(url.getPath());
        postMethod.setRequestEntity(new AxisSOAPRequestEntity(dataout, chunked, msgContext,
                charEncoding, soapActionString));

        if (!httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10) && chunked) {
            postMethod.setContentChunked(true);
        }

        if (msgContext.isSOAP11()) {
            if ("".equals(soapActionString)){
               //if the soap action is empty then we should add two ""
               postMethod.setRequestHeader(HTTPConstants.HEADER_SOAP_ACTION, "\"\"");
            }else{
                if (soapActionString != null && !soapActionString.startsWith("\"")) {  // SOAPAction string must be a quoted string
                    soapActionString = "\"" + soapActionString + "\"";
                }
                postMethod.setRequestHeader(HTTPConstants.HEADER_SOAP_ACTION, soapActionString);
            }

        } else {
        }
        //setting the cookie in the out path
        Object cookieString = msgContext.getProperty(HTTPConstants.COOKIE_STRING);
        if (cookieString != null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(Constants.SESSION_COOKIE);
            buffer.append("=");
            buffer.append(cookieString);
            postMethod.setRequestHeader(HTTPConstants.HEADER_COOKIE, buffer.toString());
        }

        postMethod.setRequestHeader(HTTPConstants.HEADER_HOST, url.getHost());

        if (httpVersion != null) {
            if (httpVersion.equals(HTTPConstants.HEADER_PROTOCOL_10)) {
                httpClient.getParams().setVersion(HttpVersion.HTTP_1_0);
            } else {
                postMethod.setRequestHeader(HTTPConstants.HEADER_EXPECT,
                        HTTPConstants.HEADER_EXPECT_100_Continue);
            }
        }

        // set timeout in client
        long timeout = msgContext.getOptions().getTimeOutInMilliSeconds();
        if (timeout != 0) {
            httpClient.getParams().setSoTimeout((int)timeout);
        }

        /*
         *   main excecution takes place..
         */
        executeMethod(httpClient, msgContext, url, postMethod);

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
            throw new AxisFault(Messages.getMessage("httpTransportError",
                String.valueOf(postMethod.getStatusCode()), postMethod.getStatusText()), SOAP12Constants.FAULT_CODE_SENDER);
        }

        throw new AxisFault(Messages.getMessage("transportError",
                String.valueOf(postMethod.getStatusCode()), postMethod.getResponseBodyAsString()));
    }

    public class AxisSOAPRequestEntity implements RequestEntity {
        private boolean doingMTOM = false;
        private boolean doingSWA = false;
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
            this.doingSWA =  msgCtxt.isDoingSwA();
            this.charSetEnc = charSetEncoding;
            this.soapActionString = soapActionString;
        }

        private void handleOMOutput(OutputStream out, boolean doingMTOM)
                throws XMLStreamException {
            format.setDoOptimize(doingMTOM);
			format.setDoingSWA(doingSWA);

			if (!doingMTOM & doingSWA) {
				 StringWriter bufferedSOAPBody = new StringWriter();
				if (isAllowedRetry) {
					element.serialize(bufferedSOAPBody, format);
				} else {
					element.serializeAndConsume(bufferedSOAPBody, format);
				}
				MIMEOutputUtils.writeSOAPWithAttachmentsMessage(bufferedSOAPBody,out,msgCtxt.getAttachmentMap(), format);
			} else {
				if (isAllowedRetry) {
					element.serialize(out, format);
				} else {
					element.serializeAndConsume(out, format);
				}
			}
        }

        public byte[] writeBytes() throws AxisFault {
            try {
                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

                if (!doingMTOM) {
                	// why are we creating a new OMOutputFormat
                    OMOutputFormat format2 = new OMOutputFormat();
					format2.setCharSetEncoding(charSetEnc);
					if (doingSWA) {
			            StringWriter bufferedSOAPBody = new StringWriter();
			            element.serializeAndConsume(bufferedSOAPBody,format2);
						MIMEOutputUtils.writeSOAPWithAttachmentsMessage(bufferedSOAPBody,bytesOut,msgCtxt.getAttachmentMap(), format2);
					} else {
						element.serializeAndConsume(bytesOut, format2);
					}
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
            Object gzip = msgCtxt.getOptions().getProperty(HTTPConstants.MC_GZIP_REQUEST);
            if(gzip != null && JavaUtils.isTrueExplicitly(gzip) && chunked) {
                out = new GZIPOutputStream(out);
            }
            try {
                if (chunked) {
                    this.handleOMOutput(out, doingMTOM);
                } else {
                    if (bytes == null) {
                        bytes = writeBytes();
                    }

                    out.write(bytes);
                }

                if(out instanceof GZIPOutputStream){
                    ((GZIPOutputStream)out).finish();
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
                    && !"".equals(soapActionString.trim()) && ! "\"\"".equals(soapActionString.trim())) {
                contentType = contentType + ";action=\"" + soapActionString + "\";";
            }
            return contentType;
        }

        public boolean isRepeatable() {
            return true;
        }
    }
}
