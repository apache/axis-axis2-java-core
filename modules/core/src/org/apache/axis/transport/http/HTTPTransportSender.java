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

import org.apache.axis.Constants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.AbstractTransportSender;

import java.io.*;
import java.net.*;

/**
 * Class HTTPTransportSender
 */
public class HTTPTransportSender extends AbstractTransportSender {
    /**
     * Field out
     */
    protected Writer out;

    /**
     * Field socket
     */
    private Socket socket;
    private ByteArrayOutputStream outputStream;
 
    protected void writeTransportHeaders(
        Writer out,
        URL url,
        MessageContext msgContext,
        int contentLength)
        throws IOException {
        Object soapAction = msgContext.getWSAAction();
        String soapActionString = soapAction == null ? "" : soapAction.toString();
        StringBuffer buf = new StringBuffer();
        buf.append("POST ").append(url.getFile()).append(" HTTP/1.0\n");
        buf.append("Content-Type: text/xml; charset=utf-8\n");
        buf.append("Accept: application/soap+xml, application/dime, multipart/related, text/*\n");
        buf.append("Host: ").append(url.getHost()).append("\n");
        buf.append("Cache-Control: no-cache\n");
        buf.append("Pragma: no-cache\n");
        buf.append("Content-Length: " + contentLength + "\n");
        if (!this.doREST) {
            buf.append("SOAPAction: \"" + soapActionString + "\"\n");
        }
        buf.append("\n");
        out.write(buf.toString());
    }

    public void finalizeSendWithOutputStreamFromIncomingConnection(
        MessageContext msgContext) {
    }

    public void finalizeSendWithToAddress(MessageContext msgContext)
        throws AxisFault {
        EndpointReference toURL = msgContext.getTo();
        if (toURL != null) {
            try {
                URL url = new URL(toURL.getAddress());
                SocketAddress add =
                    new InetSocketAddress(url.getHost(), url.getPort() == -1 ? 80 : url.getPort());
                socket = new Socket();
                socket.connect(add);
                OutputStream outS = socket.getOutputStream();
                byte[] bytes = outputStream.toByteArray();

                Writer realOut = new OutputStreamWriter(outS);
                //write header to the out put stream
                writeTransportHeaders(realOut, url, msgContext, bytes.length);
                realOut.flush();
                //write the content to the real output stream
                outS.write(bytes);
                outS.flush();

                msgContext.setProperty(
                    MessageContext.TRANSPORT_IN,socket.getInputStream());
                msgContext.setProperty(HTTPConstants.SOCKET, socket);

                socket.shutdownOutput();

            } catch (MalformedURLException e) {
                throw new AxisFault(e.getMessage(), e);
            } catch (IOException e) {
                throw new AxisFault(e.getMessage(), e);
            }
        } else {
            throw new AxisFault("to EPR must be specified");
        }
    }

    protected OutputStream openTheConnection(EndpointReference epr) {
        outputStream = new ByteArrayOutputStream();
        return outputStream;
    }

    public void startSendWithOutputStreamFromIncomingConnection(
        MessageContext msgContext,
    OutputStream out)
        throws AxisFault {
        Object contianerManaged = msgContext.getProperty(Constants.CONTAINER_MANAGED);
        if (contianerManaged == null || !Constants.VALUE_TRUE.equals(contianerManaged)) {
            try {
                out.write(new String(HTTPConstants.HTTP).getBytes());
                out.write(new String(HTTPConstants.OK).getBytes());
                out.write("\n\n".getBytes());
            } catch (IOException e) {
                throw new AxisFault(e);
            }
        }
    }

    public void startSendWithToAddress(MessageContext msgContext,OutputStream out) {
    }

    /* (non-Javadoc)
     * @see org.apache.axis.transport.TransportSender#cleanUp()
     */
    public void cleanUp() throws AxisFault {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }

        } catch (IOException e) {
        }

    }

}
