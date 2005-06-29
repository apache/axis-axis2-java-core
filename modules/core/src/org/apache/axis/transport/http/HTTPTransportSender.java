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
package org.apache.axis.transport.http;

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

import javax.servlet.http.HttpUtils;

import org.apache.axis.Constants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.Parameter;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMOutput;
import org.apache.axis.transport.AbstractTransportSender;

/**
 * Class HTTPTransportSender
 */
public class HTTPTransportSender extends AbstractTransportSender {
    private boolean chuncked = false;
    private boolean doMTOM = false;
    private String httpVersion = HTTPConstants.HEADER_PROTOCOL_10;
    public static final String TRANSPORT_SENDER_INFO = "TRANSPORT_SENDER_INFO";

    protected void writeTransportHeaders(
        OutputStream out,
        URL url,
        MessageContext msgContext,
        int contentLength)
        throws AxisFault {
        try {
            Object soapAction = msgContext.getWSAAction();
            String soapActionString =
                soapAction == null ? "" : soapAction.toString();
            
            boolean doMTOM = HTTPTransportUtils.doWriteMTOM(msgContext);
            StringBuffer buf = new StringBuffer();
            buf.append(HTTPConstants.HEADER_POST).append(" ");
            buf.append(url.getFile()).append(" ").append(httpVersion).append("\n");
            if(doMTOM){
                buf.append(HTTPConstants.HEADER_CONTENT_TYPE).append(": ").append(OMOutput.getContentType(true)).append("\n");
            }else{
                buf.append(HTTPConstants.HEADER_CONTENT_TYPE).append(": text/xml; charset=utf-8\n");
            }
            
            buf.append(HTTPConstants.HEADER_ACCEPT).append(": application/soap+xml, application/dime, multipart/related, text/*\n");
            buf.append(HTTPConstants.HEADER_HOST).append(": ").append(url.getHost()).append("\n");
            buf.append(HTTPConstants.HEADER_CACHE_CONTROL).append(": no-cache\n");
            buf.append(HTTPConstants.HEADER_PRAGMA).append(": no-cache\n");
            if (chuncked) {
                buf
                    .append(HTTPConstants.HEADER_TRANSFER_ENCODING)
                    .append(": ")
                    .append(HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED)
                    .append("\n");
            } else {
                buf.append(HTTPConstants.HEADER_CONTENT_LENGTH).append(": " + contentLength + "\n");
            }
            if (!this.doREST) {
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
            (TransportSenderInfo) msgContext.getProperty(TRANSPORT_SENDER_INFO);

        EndpointReference toURL = msgContext.getTo();
        if (toURL != null) {
            try {
                URL url = new URL(toURL.getAddress());
                SocketAddress add =
                    new InetSocketAddress(
                        url.getHost(),
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
            throw new AxisFault("to EPR must be specified");
        }
    }

    public void finalizeSendWithToAddress(
        MessageContext msgContext,
        OutputStream out)
        throws AxisFault {
        try {
            TransportSenderInfo transportInfo =
                (TransportSenderInfo) msgContext.getProperty(
                    TRANSPORT_SENDER_INFO);
            InputStream in = null;
            if (chuncked) {
                ((ChunkedOutputStream) out).eos();
            } else {
                openSocket(msgContext);
                OutputStream outS = transportInfo.out;
                in = transportInfo.in;
                byte[] bytes = transportInfo.outputStream.toByteArray();

                //write header to the out put stream
                writeTransportHeaders(
                    outS,
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
                    (String) map.get(HTTPConstants.HEADER_TRANSFER_ENCODING);
                if (transferEncoding != null
                    && HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED.equals(
                        transferEncoding)) {
                    in = new ChunkedInputStream(transportInfo.in);
                }
                msgContext.setProperty(MessageContext.TRANSPORT_IN, in);
            }
        } catch (AxisFault e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    protected OutputStream openTheConnection(
        EndpointReference epr,
        MessageContext msgctx)
        throws AxisFault {
        msgctx.setProperty(TRANSPORT_SENDER_INFO, new TransportSenderInfo());
        if (chuncked) {
            return openSocket(msgctx);
        } else {
            TransportSenderInfo transportInfo =
                (TransportSenderInfo) msgctx.getProperty(TRANSPORT_SENDER_INFO);
            transportInfo.outputStream = new ByteArrayOutputStream();
            return transportInfo.outputStream;
        }
    }

    public OutputStream startSendWithOutputStreamFromIncomingConnection(
        MessageContext msgContext,
        OutputStream out)
        throws AxisFault {
        //        Object contianerManaged =
        //            msgContext.getProperty(Constants.CONTAINER_MANAGED);
        //        if (contianerManaged == null
        //            || !Constants.VALUE_TRUE.equals(contianerManaged)) {
        //            try {
        //                out.write(new String(HTTPConstants.HTTP).getBytes());
        //                out.write(new String(HTTPConstants.OK).getBytes());
        //                out.write("\n\n".getBytes());
        //            } catch (IOException e) {
        //                throw new AxisFault(e);
        //            }
        //        }
        return out;
    }

    public OutputStream startSendWithToAddress(
        MessageContext msgContext,
        OutputStream out)
        throws AxisFault {
        try {
           
            if (chuncked) {
                TransportSenderInfo transportInfo =
                    (TransportSenderInfo) msgContext.getProperty(
                        TRANSPORT_SENDER_INFO);
                writeTransportHeaders(out, transportInfo.url, msgContext, -1);
                out.flush();
                return new ChunkedOutputStream(out);
            } else {
                return out;
            }
        } catch (IOException e) {
            throw new AxisFault(e);
        }

    }

    /* (non-Javadoc)
     * @see org.apache.axis.transport.TransportSender#cleanUp()
     */
    public void cleanUp(MessageContext msgContext) throws AxisFault {
        TransportSenderInfo transportInfo =
            (TransportSenderInfo) msgContext.getProperty(TRANSPORT_SENDER_INFO);
        try {
            if (transportInfo.socket != null) {
                transportInfo.socket.close();
            }

        } catch (IOException e) {
        }

    }

    /* (non-Javadoc)
     * @see org.apache.axis.transport.TransportSender#init(org.apache.axis.context.ConfigurationContext, org.apache.axis.description.TransportOutDescription)
     */
    public void init(
        ConfigurationContext confContext,
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
                    && HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED.equals(
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
