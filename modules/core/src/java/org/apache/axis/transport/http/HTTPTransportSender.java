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

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.AbstractTransportSender;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;

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

    /**
     * Method obtainOutputStream
     *
     * @param msgContext
     * @return
     * @throws AxisFault
     */
    protected Writer obtainOutputStream(MessageContext msgContext)
            throws AxisFault {
        if (!msgContext.isServerSide()) {
            EndpointReference toURL = msgContext.getTo();
            if (toURL != null) {
                try {
                    URL url = new URL(toURL.getAddress());
                    SocketAddress add = new InetSocketAddress(url.getHost(),
                            url.getPort());
                    socket = new Socket();
                    socket.connect(add);
                    outS = socket.getOutputStream();
                    out = new BufferedWriter(new OutputStreamWriter(outS));
                    writeTransportHeaders(out, url);
                    msgContext.setProperty(
                            MessageContext.TRANSPORT_READER,
                            new BufferedReader(
                                    new InputStreamReader(socket.getInputStream())));
                    msgContext.setProperty(HTTPConstants.SOCKET, socket);
                } catch (MalformedURLException e) {
                    throw new AxisFault(e.getMessage(), e);
                } catch (IOException e) {
                    throw new AxisFault(e.getMessage(), e);
                }
            } else {
                throw new AxisFault("to EPR must be specified");
            }
        } else {
            out = (Writer) msgContext.getProperty(
                    MessageContext.TRANSPORT_WRITER);
        }
        if (out == null) {
            throw new AxisFault(
                    "can not find the suffient information to find endpoint");
        } else {
            return out;
        }
    }

    /**
     * Method obtainOutputStream
     *
     * @param msgContext
     * @param epr
     * @return
     * @throws AxisFault
     */
    protected Writer obtainOutputStream(
            MessageContext msgContext, EndpointReference epr) throws AxisFault {

        // TODO this is temporay work around
        return obtainOutputStream(msgContext);
    }

    /**
     * Method finalizeSending
     *
     * @param msgContext
     * @throws AxisFault
     */
    protected void finalizeSending(MessageContext msgContext)
            throws AxisFault {
    }

    /**
     * Method startSending
     *
     * @param msgContext
     * @throws AxisFault
     */
    protected void startSending(MessageContext msgContext) throws AxisFault {
    }

    /**
         * Method writeTransportHeaders
         *
         * @param out
         * @param url
         * @throws IOException
         */
    protected void writeTransportHeaders(Writer out, URL url)
            throws IOException {
        StringBuffer buf = new StringBuffer();
        buf.append("POST ").append(url.getFile()).append(" HTTP/1.0\n");
        buf.append("Content-Type: text/xml; charset=utf-8\n");
        buf.append(
                "Accept: application/soap+xml, application/dime, multipart/related, text/*\n");
        buf.append("Host: ").append(url.getHost()).append("\n");
        buf.append("Cache-Control: no-cache\n");
        buf.append("Pragma: no-cache\n");
        buf.append("SOAPAction: \"\"\n\n");
        out.write(buf.toString().toCharArray());
    }
}
