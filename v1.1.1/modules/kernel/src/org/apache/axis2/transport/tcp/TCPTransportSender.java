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


package org.apache.axis2.transport.tcp;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.transport.AbstractTransportSender;
import org.apache.axis2.util.URL;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;

public class TCPTransportSender extends AbstractTransportSender {

    private static final long serialVersionUID = -6780125098288186598L;

    /**
     * Field out
     */
    protected Writer out;

    /**
     * Field socket
     */
    private Socket socket;

    public void cleanup(MessageContext msgContext) throws AxisFault {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
        }
    }

    public void finalizeSendWithOutputStreamFromIncomingConnection(MessageContext msgContext,
                                                                   OutputStream out) {
    }

    public void finalizeSendWithToAddress(MessageContext msgContext, OutputStream out)
            throws AxisFault {
        try {
            socket.shutdownOutput();
            msgContext.setProperty(MessageContext.TRANSPORT_IN, socket.getInputStream());
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    protected OutputStream openTheConnection(EndpointReference toURL, MessageContext msgContext)
            throws AxisFault {
        if (toURL != null) {
            try {
                URL url = new URL(toURL.getAddress());
                SocketAddress add = new InetSocketAddress(url.getHost(), (url.getPort() == -1)
                        ? 80
                        : url.getPort());

                socket = new Socket();
                socket.connect(add);

                return socket.getOutputStream();
            } catch (MalformedURLException e) {
                throw new AxisFault(e.getMessage(), e);
            } catch (IOException e) {
                throw new AxisFault(e.getMessage(), e);
            }
        } else {
            throw new AxisFault(Messages.getMessage("canNotBeNull", "Can not Be Null"));
        }
    }

    public OutputStream startSendWithOutputStreamFromIncomingConnection(MessageContext msgContext,
                                                                        OutputStream out)
            throws AxisFault {
        return out;
    }

    public OutputStream startSendWithToAddress(MessageContext msgContext, OutputStream out) {
        return out;
    }

    /**
     * Method writeTransportHeaders
     *
     * @param out
     * @param url
     * @param msgContext
     * @throws IOException
     */
    protected void writeTransportHeaders(Writer out, URL url, MessageContext msgContext,
                                         int contentLength)
            throws IOException {
    }

    public void stop() {
    }
}
