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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.ListenerManager;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.context.MessageContext;
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.description.Parameter;
import org.apache.axis.description.TransportInDescription;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.impl.llom.builder.StAXBuilder;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.transport.TransportListener;
import org.apache.axis.transport.http.SimpleHTTPServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class HTTPTransportReceiver
 */
public class TCPServer extends TransportListener implements Runnable {
    private int port = 8000;
    private ServerSocket serversocket;
    private boolean started = false;
    private ConfigurationContext configContext;

    protected Log log = LogFactory.getLog(SimpleHTTPServer.class.getName());
    public TCPServer(){}

    public TCPServer(int port, String dir) throws AxisFault {
        try {
            ConfigurationContextFactory erfac = new ConfigurationContextFactory();
            ConfigurationContext configContext = erfac.buildEngineContext(dir);
            this.configContext = configContext;
            serversocket = new ServerSocket(port);
        } catch (DeploymentException e1) {
            throw new AxisFault(e1);
        } catch (IOException e1) {
            throw new AxisFault(e1);
        }
    }

    public TCPServer(int port, ConfigurationContext configContext) throws AxisFault {
        try {
            this.configContext = configContext;
            serversocket = new ServerSocket(port);
        } catch (IOException e1) {
            throw new AxisFault(e1);
        }
    }

    public void run() {
        while (started) {
            Socket socket = null;
            try {

                try {
                    socket = serversocket.accept();
                } catch (java.io.InterruptedIOException iie) {
                } catch (Exception e) {
                    log.debug(e);
                    break;
                }

                Writer out = new OutputStreamWriter(socket.getOutputStream());
                Reader in = new InputStreamReader(socket.getInputStream());
                TransportOutDescription transportOut =
                    configContext.getAxisConfiguration().getTransportOut(
                        new QName(Constants.TRANSPORT_TCP));
                MessageContext msgContext =
                    new MessageContext(
                        configContext,
                        configContext.getAxisConfiguration().getTransportIn(
                            new QName(Constants.TRANSPORT_TCP)),
                        transportOut);
                msgContext.setServerSide(true);
                msgContext.setProperty(MessageContext.TRANSPORT_WRITER, out);
                msgContext.setProperty(MessageContext.TRANSPORT_READER, in);

                AxisEngine engine = new AxisEngine(configContext);
                try {
                    XMLStreamReader xmlreader =
                        XMLInputFactory.newInstance().createXMLStreamReader(in);
                    StAXBuilder builder = new StAXSOAPModelBuilder(xmlreader);
                    msgContext.setEnvelope((SOAPEnvelope) builder.getDocumentElement());
                } catch (Exception e) {
                    throw new AxisFault(e.getMessage(), e);
                }
                engine.receive(msgContext);
            } catch (Throwable e) {
                log.error(e);
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                        if (!started) {
                            serversocket.close();
                        }
                    }

                } catch (IOException e1) {
                    log.error(e1);
                }
            }
        }

    }

    public synchronized void start() {
        started = true;
        Thread thread = new Thread(this);
        thread.start();
    }

    /* (non-Javadoc)
     * @see org.apache.axis.transport.TransportListener#replyToEPR(java.lang.String)
     */
    public EndpointReference replyToEPR(String serviceName) throws AxisFault {
        return new EndpointReference(
            AddressingConstants.WSA_REPLY_TO,
            "tcp://127.0.0.1:" + (serversocket.getLocalPort()) + "/axis/services/" + serviceName);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.transport.TransportListener#stop()
     */
    public void stop() throws AxisFault {
        try {
            this.serversocket.close();
            started = false;
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    public void init(ConfigurationContext axisConf, TransportInDescription transprtIn)
        throws AxisFault {
        this.configContext = axisConf;
        Parameter param = transprtIn.getParameter(PARAM_PORT);
        if (param != null) {
            int port = Integer.parseInt((String) param.getValue());
            serversocket = ListenerManager.openSocket(port);
        }

    }

}
