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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
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
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.TransportSender;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class CommonsHTTPTransportSender extends AbstractHandler implements TransportSender {

    private static final long serialVersionUID = 7929963795196215199L;
    protected static final String PROXY_HOST_NAME = "proxy_host";
    protected static final String PROXY_PORT = "proxy_port";
    int soTimeout = HTTPConstants.DEFAULT_SO_TIMEOUT;

    /**
     * proxydiscription
     */
    protected TransportOutDescription proxyOutSetting = null;
    protected Log log = LogFactory.getLog(getClass().getName());
    protected String httpVersion = HTTPConstants.HEADER_PROTOCOL_11;

    private boolean chunked = false;

    int connectionTimeout = HTTPConstants.DEFAULT_CONNECTION_TIMEOUT;

    public CommonsHTTPTransportSender() {
    }

    public void cleanUp(MessageContext msgContext) throws AxisFault {
        HttpMethod httpMethod =
                (HttpMethod) msgContext.getProperty(HTTPConstants.HTTP_METHOD);

        if (httpMethod != null) {
            httpMethod.releaseConnection();
        }
    }

    public void init(ConfigurationContext confContext,
                     TransportOutDescription transportOut)
            throws AxisFault {

        // <parameter name="PROTOCOL" locked="false">HTTP/1.0</parameter> or
        // <parameter name="PROTOCOL" locked="false">HTTP/1.1</parameter> is
        // checked
        Parameter version =
                transportOut.getParameter(HTTPConstants.PROTOCOL_VERSION);

        if (version != null) {
            if (HTTPConstants.HEADER_PROTOCOL_11.equals(version.getValue())) {
                httpVersion = HTTPConstants.HEADER_PROTOCOL_11;

                Parameter transferEncoding =
                        transportOut.getParameter(
                                HTTPConstants.HEADER_TRANSFER_ENCODING);

                if ((transferEncoding != null)
                        &&
                        HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED.equals(
                                transferEncoding.getValue())) {
                    chunked = true;
                }
            } else if (HTTPConstants.HEADER_PROTOCOL_10.equals(version.getValue())) {
                httpVersion = HTTPConstants.HEADER_PROTOCOL_10;
            } else {
                throw new AxisFault(
                        "Parameter " + HTTPConstants.PROTOCOL_VERSION
                                + " Can have values only HTTP/1.0 or HTTP/1.1");
            }
        }

        // Get the timeout values from the configuration
        try {
            Parameter tempSoTimeoutParam =
                    transportOut.getParameter(HTTPConstants.SO_TIMEOUT);
            Parameter tempConnTimeoutParam =
                    transportOut.getParameter(HTTPConstants.CONNECTION_TIMEOUT);

            if (tempSoTimeoutParam != null) {
                soTimeout = Integer.parseInt(
                        (String) tempSoTimeoutParam.getValue());
            }

            if (tempConnTimeoutParam != null) {
                connectionTimeout = Integer.parseInt(
                        (String) tempConnTimeoutParam.getValue());
            }
        } catch (NumberFormatException nfe) {

            // If there's a problem log it and use the default values
            log.error("Invalid timeout value format: not a number", nfe);
        }
    }


    public void invoke(MessageContext msgContext) throws AxisFault {
        try {
            OMOutputFormat format = new OMOutputFormat();
            String charSetEnc =
                    (String) msgContext
                            .getProperty(MessageContext.CHARACTER_SET_ENCODING);

            if (charSetEnc != null) {
                format.setCharSetEncoding(charSetEnc);
            } else {
                OperationContext opctx = msgContext.getOperationContext();

                if (opctx != null) {
                    charSetEnc = (String) opctx
                            .getProperty(MessageContext.CHARACTER_SET_ENCODING);
                }
            }

            /**
             * If the char set enc is still not found use the default
             */
            if (charSetEnc == null) {
                charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING;
            }

            msgContext.setDoingMTOM(HTTPTransportUtils.doWriteMTOM(msgContext));
            msgContext.setDoingREST(HTTPTransportUtils.isDoingREST(msgContext));
            format.setSOAP11(msgContext.isSOAP11());
            format.setDoOptimize(msgContext.isDoingMTOM());
            format.setCharSetEncoding(charSetEnc);

            // Trasnport URL can be different from the WSA-To. So processing
            // that now.
            EndpointReference epr = null;
            String transportURL =
                    (String) msgContext
                            .getProperty(MessageContextConstants.TRANSPORT_URL);

            if (transportURL != null) {
                epr = new EndpointReference(transportURL);
            } else if ((msgContext.getTo() != null)
                    && !AddressingConstants.Submission.WSA_ANONYMOUS_URL
                    .equals(msgContext.getTo().getAddress()) &&
                    !AddressingConstants.Final
                            .WSA_ANONYMOUS_URL
                            .equals(msgContext.getTo().getAddress())) {
                epr = msgContext.getTo();
            }

            // Check for the REST behaviour, if you desire rest beahaviour
            // put a <parameter name="doREST" value="true"/> at the
            // server.xml/client.xml file
            // ######################################################
            // Change this place to change the wsa:toepr
            // epr = something
            // ######################################################
            OMElement dataOut;

            /**
             * Figuringout the REST properties/parameters
             */
            if (msgContext.isDoingREST()) {
                dataOut = msgContext.getEnvelope().getBody().getFirstElement();
            } else {
                dataOut = msgContext.getEnvelope();
            }

            if (epr != null) {
                if (!epr.getAddress().equals(AddressingConstants.Final.WSA_NONE_URI)) {
                    writeMessageWithCommons(msgContext, epr, dataOut, format);
                }
            } else {
                sendUsingOutputStream(msgContext, format, dataOut);
            }

            if (msgContext.getOperationContext() != null) {
                msgContext.getOperationContext()
                        .setProperty(Constants.RESPONSE_WRITTEN,
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

    private void sendUsingOutputStream(MessageContext msgContext, OMOutputFormat format, OMElement dataOut) throws AxisFault, XMLStreamException {
        OutputStream out =
                (OutputStream) msgContext
                        .getProperty(MessageContext.TRANSPORT_OUT);

        if (msgContext.isServerSide()) {
            OutTransportInfo transportInfo =
                    (OutTransportInfo) msgContext
                            .getProperty(Constants.OUT_TRANSPORT_INFO);

            if (transportInfo != null) {
                boolean soap11 = msgContext.isSOAP11();

                format.setSOAP11(soap11);

                String contentType = format.getContentType();
                String encoding = contentType + "; charset="
                        + format.getCharSetEncoding();

                transportInfo.setContentType(encoding);
            } else {
                throw new AxisFault(Constants.OUT_TRANSPORT_INFO +
                        " has not been set");
            }
        }

        format.setDoOptimize(msgContext.isDoingMTOM());
        dataOut.serializeAndConsume(out, format);
    }

    public void writeMessageWithCommons(MessageContext msgContext,
                                        EndpointReference toURL,
                                        OMElement dataout,
                                        OMOutputFormat format)
            throws AxisFault {
        try {
            URL url = new URL(toURL.getAddress());
            String soapActionString = msgContext.getSoapAction();

            if ((soapActionString == null) || (soapActionString.length() == 0)) {
                soapActionString = msgContext.getWSAAction();
            }

            if (soapActionString == null) {
                soapActionString = "\"\"";
            }

            // select the Message Sender depending on the REST status
            AbstractHTTPSender sender;

            if (!msgContext.isDoingREST()) {
                sender = new SOAPOverHTTPSender();
            } else {
                sender = new RESTSender();
            }
            if (msgContext.getProperty(MessageContextConstants.CHUNKED) != null) {
                chunked = Constants.VALUE_TRUE.equals(msgContext.getProperty(
                        MessageContextConstants.CHUNKED));
            }

            if (msgContext.getProperty(MessageContextConstants.HTTP_PROTOCOL_VERSION) != null) {
                httpVersion = (String) msgContext.getProperty(MessageContextConstants.HTTP_PROTOCOL_VERSION);
            }

            // Following order needed to be preserved because,
            // HTTP/1.0 does not support chunk encoding
            sender.setChunked(chunked);
            sender.setHttpVersion(httpVersion);
            sender.setFormat(format);

            sender.send(msgContext, dataout, url, soapActionString);
        } catch (MalformedURLException e) {
            throw new AxisFault(e);
        } catch (HttpException e) {
            throw new AxisFault(e);
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    public void writeMessageWithToOutPutStream(MessageContext msgContext,
                                               OutputStream out) {
    }
}
