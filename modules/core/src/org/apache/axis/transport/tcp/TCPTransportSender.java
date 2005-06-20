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
package org.apache.axis.transport.tcp;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.transport.AbstractTransportSender;
import org.apache.axis.util.URL;

import java.io.*;
import java.net.*;

/**
 * Class HTTPTransportSender
 */
public class TCPTransportSender extends AbstractTransportSender {
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
        //TCP no headers   :)
    }

    public void finalizeSendWithOutputStreamFromIncomingConnection(MessageContext msgContext) {
    }

    public void finalizeSendWithToAddress(MessageContext msgContext) throws AxisFault {
        try {
            socket.shutdownOutput();
            msgContext.setProperty(
                MessageContext.TRANSPORT_IN,socket.getInputStream());
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    protected OutputStream openTheConnection(EndpointReference toURL) throws AxisFault {
        if (toURL != null) {
            try {
                URL url = new URL(toURL.getAddress());
                SocketAddress add =
                    new InetSocketAddress(url.getHost(), url.getPort() == -1 ? 80 : url.getPort());
                socket = new Socket();
                socket.connect(add);
                return socket.getOutputStream();
            } catch (MalformedURLException e) {
                throw new AxisFault(e.getMessage(), e);
            } catch (IOException e) {
                throw new AxisFault(e.getMessage(), e);
            }
        } else {
            throw new AxisFault("to EPR must be specified");
        }
    }

    public void startSendWithOutputStreamFromIncomingConnection(
        MessageContext msgContext,
        OutputStream out)
        throws AxisFault {
    }

    public void startSendWithToAddress(MessageContext msgContext, OutputStream out) {
    }

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
