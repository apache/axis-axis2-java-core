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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.AbstractTransportSender;

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
    /**
     * Method obtainOutputStream
     *
     * @param msgContext
     * @return
     * @throws AxisFault
     */
    protected Writer obtainWriter(MessageContext msgContext) throws AxisFault {
        if (!msgContext.isServerSide()) {
            //create a new byte buffer output stream
            outputStream = new ByteArrayOutputStream();
            out = new OutputStreamWriter(outputStream);
        } else {
            out = (Writer) msgContext.getProperty(MessageContext.TRANSPORT_WRITER);
        }
        if (out == null) {
            throw new AxisFault("can not find the suffient information to find endpoint");
        } else {
            return out;
        }
    }


    /**
     * Method writeTransportHeaders
     *
     * @param out
     * @param url
     * @param msgContext
     * @throws IOException
     */
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
        buf.append("SOAPAction: \"" + soapActionString + "\"\n\n");
        out.write(buf.toString());
    }

    public void finalizeSendWithOutputStreamFromIncomingConnection(
        MessageContext msgContext,
        Writer writer) {
    }

    public void finalizeSendWithToAddress(MessageContext msgContext, EndpointReference epr,Writer writer)
        throws AxisFault {
            EndpointReference toURL = msgContext.getTo();
            if (toURL != null) {
                try {
                    URL url = new URL(toURL.getAddress());
                    SocketAddress add =
                        new InetSocketAddress(
                            url.getHost(),
                            url.getPort() == -1 ? 80 : url.getPort());
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
                        MessageContext.TRANSPORT_READER,
                        new InputStreamReader(socket.getInputStream()));
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

    protected Writer openTheConnection(EndpointReference epr) {
        outputStream = new ByteArrayOutputStream();
        return new OutputStreamWriter(outputStream);
    }

    public void startSendWithOutputStreamFromIncomingConnection(
        MessageContext msgContext,
        Writer writer) {
            try {
                writer.write(new String(HTTPConstants.HTTP).toCharArray());
                writer.write(new String(HTTPConstants.OK).toCharArray());
                writer.write("\n\n".toCharArray());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    public void startSendWithToAddress(MessageContext msgContext, EndpointReference epr,Writer writer) {
    }

}
