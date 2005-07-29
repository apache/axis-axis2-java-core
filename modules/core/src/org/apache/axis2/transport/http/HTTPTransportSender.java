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

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.soap.impl.llom.soap11.SOAP11Constants;
import org.apache.axis2.soap.impl.llom.soap12.SOAP12Constants;
import org.apache.axis2.transport.AbstractTransportSender;
import org.apache.axis2.AxisFault;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.Map;

/**
 * Class HTTPTransportSender
 */
public class HTTPTransportSender extends AbstractTransportSender {
    private boolean chuncked = false;

    private String httpVersion = HTTPConstants.HEADER_PROTOCOL_10;
    public static final String TRANSPORT_SENDER_INFO = "TRANSPORT_SENDER_INFO";

    protected void writeTransportHeaders(OutputStream out, URL url,
			MessageContext msgContext, int contentLength) throws AxisFault {
		try {

			String soapActionString = msgContext.getSoapAction();
			if (soapActionString == null || soapActionString.length() == 0) {
				soapActionString = msgContext.getWSAAction();
			}
			if (soapActionString == null) {
				soapActionString = "";
			}

			boolean doMTOM = msgContext.isDoingMTOM();
			StringBuffer buf = new StringBuffer();
			buf.append(HTTPConstants.HEADER_POST).append(" ");
			buf.append(url.getFile()).append(" ").append(httpVersion).append(
					"\n");

			//Get the char set encoding if set
			String charSetEnc = (String) msgContext
					.getProperty(MessageContext.CHARACTER_SET_ENCODING);
			if (charSetEnc == null)
				charSetEnc = MessageContext.DEFAULT_CHAR_SET_ENCODING; 

			if (doMTOM) {
				buf.append(HTTPConstants.HEADER_CONTENT_TYPE).append(": ")
						.append(omOutput.getOptimizedContentType())
						.append("\n");
			} else {
				String nsURI = msgContext.getEnvelope().getNamespace()
						.getName();

				if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
					buf.append(HTTPConstants.HEADER_CONTENT_TYPE).append(": ")
							.append(SOAP12Constants.SOAP_12_CONTENT_TYPE);
					buf.append("; charset=" + charSetEnc + "\n");
				} else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI
						.equals(nsURI)) {
					buf.append(HTTPConstants.HEADER_CONTENT_TYPE).append(
							": text/xml; charset=" + charSetEnc + "\n");
				} else {
					throw new AxisFault(
							"Unknown SOAP Version. Current Axis handles only SOAP 1.1 and SOAP 1.2 messages");
				}

			}

			buf
					.append(HTTPConstants.HEADER_ACCEPT)
					.append(
							": application/soap+xml, application/dime, multipart/related, text/*\n");
			buf.append(HTTPConstants.HEADER_HOST).append(": ").append(
					url.getHost()).append("\n");
			buf.append(HTTPConstants.HEADER_CACHE_CONTROL).append(
					": no-cache\n");
			buf.append(HTTPConstants.HEADER_PRAGMA).append(": no-cache\n");
			if (chuncked) {
				buf.append(HTTPConstants.HEADER_TRANSFER_ENCODING).append(": ")
						.append(HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED)
						.append("\n");
			}
			if (!chuncked && !msgContext.isDoingMTOM()) {
				buf.append(HTTPConstants.HEADER_CONTENT_LENGTH).append(
						": " + contentLength + "\n");
			}
			if (!msgContext.isDoingREST()) {
				buf.append("SOAPAction: \"" + soapActionString + "\"\n");
			}
			buf.append("\n");
			out.write(buf.toString().getBytes());
		} catch (IOException e) {
			throw new AxisFault(e);
		}
	}
    
    public void finalizeSendWithOutputStreamFromIncomingConnection(
            MessageContext msgContext,
            OutputStream out) {
    }

    private OutputStream openSocket(MessageContext msgContext)
            throws AxisFault {
        TransportSenderInfo transportInfo =
                (TransportSenderInfo) msgContext.getProperty(
                        TRANSPORT_SENDER_INFO);

        EndpointReference toURL = msgContext.getTo();
        if (toURL != null) {
            try {
                URL url = new URL(toURL.getAddress());
                SocketAddress add =
                        new InetSocketAddress(url.getHost(),
                                url.getPort() == -1 ? 80 : url.getPort());
                Socket socket = new Socket();
                socket.connect(add);
                transportInfo.url = url;
                transportInfo.in = socket.getInputStream();
                transportInfo.out = socket.getOutputStream();
                transportInfo.socket = socket;
                return transportInfo.out;
            } catch (MalformedURLException e) {
                throw new AxisFault(e.getMessage(), e);
            } catch (IOException e) {
                throw new AxisFault(e.getMessage(), e);
            }
        } else {
            throw new AxisFault(Messages.getMessage("notFound", "TO EPR"));
        }
    }

    public void finalizeSendWithToAddress(MessageContext msgContext,
                                          OutputStream out)
            throws AxisFault {
        try {
            TransportSenderInfo transportInfo =
                    (TransportSenderInfo) msgContext.getProperty(
                            TRANSPORT_SENDER_INFO);
            InputStream in;
            if (chuncked || msgContext.isDoingMTOM()) {
                if (chuncked) {
                    ((ChunkedOutputStream) out).eos();
                    in = new ChunkedInputStream(transportInfo.in);
                }
                in = transportInfo.in;
            } else {
                openSocket(msgContext);
                OutputStream outS = transportInfo.out;
                in = transportInfo.in;
                byte[] bytes = transportInfo.outputStream.toByteArray();

                //write header to the out put stream
                writeTransportHeaders(outS,
                        transportInfo.url,
                        msgContext,
                        bytes.length);
                outS.flush();
                //write the content to the real output stream
                outS.write(bytes);
            }

            transportInfo.socket.shutdownOutput();
            HTTPTransportReceiver tr = new HTTPTransportReceiver();
            Map map = tr.parseTheHeaders(transportInfo.in, false);
            if (!HTTPConstants
                    .RESPONSE_ACK_CODE_VAL
                    .equals(map.get(HTTPConstants.RESPONSE_CODE))) {
                String transferEncoding =
                        (String) map.get(
                                HTTPConstants.HEADER_TRANSFER_ENCODING);
                if (transferEncoding != null
                        &&
                        HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED.equals(
                                transferEncoding)) {
                    in = new ChunkedInputStream(transportInfo.in);
                }
                msgContext.setProperty(MessageContext.TRANSPORT_IN, in);

                String contentType = (String) map.get(
                        HTTPConstants.HEADER_CONTENT_TYPE);
                if (contentType != null &&
                        contentType.indexOf(
                                HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED) >=
                        0) {
                    OperationContext opContext = msgContext.getOperationContext();
                    if (opContext != null) {
                        opContext.setProperty(
                                HTTPConstants.MTOM_RECIVED_CONTENT_TYPE,
                                contentType);
                    }
                }
            }
        } catch (IOException e) {
            throw new AxisFault(e);
        }

    }

    protected OutputStream openTheConnection(EndpointReference epr,
                                             MessageContext msgctx)
            throws AxisFault {
        msgctx.setProperty(TRANSPORT_SENDER_INFO, new TransportSenderInfo());

        if (msgctx.isDoingMTOM() || chuncked) {
            return openSocket(msgctx);
        } else {
            TransportSenderInfo transportInfo =
                    (TransportSenderInfo) msgctx.getProperty(
                            TRANSPORT_SENDER_INFO);
            transportInfo.outputStream = new ByteArrayOutputStream();
            return transportInfo.outputStream;
        }
    }

    public OutputStream startSendWithOutputStreamFromIncomingConnection(
            MessageContext msgContext,
            OutputStream out)
            throws AxisFault {
        if (msgContext.isDoingMTOM()) {
            HTTPOutTransportInfo httpOutTransportInfo = (HTTPOutTransportInfo) msgContext.getProperty(
                    HTTPConstants.HTTPOutTransportInfo);
            if (httpOutTransportInfo != null) {
                httpOutTransportInfo.setContentType(
                        omOutput.getOptimizedContentType());
            } else {
                throw new AxisFault(
                        "Property " + HTTPConstants.HTTPOutTransportInfo +
                        " not set by the Server");
            }

        }
        return out;
    }

    public OutputStream startSendWithToAddress(MessageContext msgContext,
                                               OutputStream out)
            throws AxisFault {
        try {
            if (msgContext.isDoingMTOM() || chuncked) {
                TransportSenderInfo transportInfo =
                        (TransportSenderInfo) msgContext.getProperty(
                                TRANSPORT_SENDER_INFO);
                writeTransportHeaders(out, transportInfo.url, msgContext, -1);
                out.flush();
                if (chuncked) {
                    return new ChunkedOutputStream(out);
                } else {
                    return out;
                }
            } else {
                return out;
            }
        } catch (IOException e) {
            throw new AxisFault(e);
        }

    }

    /* (non-Javadoc)
     * @see org.apache.axis2.transport.TransportSender#cleanUp()
     */
    public void cleanUp(MessageContext msgContext) throws AxisFault {
        TransportSenderInfo transportInfo =
                (TransportSenderInfo) msgContext.getProperty(
                        TRANSPORT_SENDER_INFO);
        try {
            if (transportInfo.socket != null) {
                transportInfo.socket.close();
            }

        } catch (IOException e) {
        }

    }

    /* (non-Javadoc)
     * @see org.apache.axis2.transport.TransportSender#init(org.apache.axis2.context.ConfigurationContext, org.apache.axis2.description.TransportOutDescription)
     */
    public void init(ConfigurationContext confContext,
                     TransportOutDescription transportOut)
            throws AxisFault {
        //<parameter name="PROTOCOL" locked="xsd:false">HTTP/1.0</parameter> or 
        //<parameter name="PROTOCOL" locked="xsd:false">HTTP/1.1</parameter> is checked
        Parameter version =
                transportOut.getParameter(HTTPConstants.PROTOCOL_VERSION);
        if (version != null) {
            if (HTTPConstants.HEADER_PROTOCOL_11.equals(version.getValue())) {
                this.httpVersion = HTTPConstants.HEADER_PROTOCOL_11;
                Parameter transferEncoding =
                        transportOut.getParameter(
                                HTTPConstants.HEADER_TRANSFER_ENCODING);
                if (transferEncoding != null
                        &&
                        HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED.equals(
                                transferEncoding.getValue())) {
                    this.chuncked = true;
                }
            } else if (
                    HTTPConstants.HEADER_PROTOCOL_10.equals(version.getValue())) {
                //TODO HTTP1.0 specific parameters
            } else {
                throw new AxisFault(
                        "Parameter "
                        + HTTPConstants.PROTOCOL_VERSION
                        + " Can have values only HTTP/1.0 or HTTP/1.1");
            }
        }

    }

    private class TransportSenderInfo {
        public InputStream in;
        public OutputStream out;
        public ByteArrayOutputStream outputStream;
        public URL url;
        public Socket socket;
    }

}
