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
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.EngineContextFactory;
import org.apache.axis.context.MessageContext;
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.impl.llom.builder.StAXBuilder;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.transport.http.SimpleHTTPServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class HTTPTransportReceiver
 */
public class TCPServer implements Runnable {
    private int port = 8000;
    private ServerSocket serversocket;
    private boolean started = false;
    private ConfigurationContext configContext;

    protected Log log = LogFactory.getLog(SimpleHTTPServer.class.getName());

    public TCPServer(int port, String dir) throws AxisFault {
        try {
            EngineContextFactory erfac = new EngineContextFactory();
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
            try {
                Socket socket = null;
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
                    configContext.getEngineConfig().getTransportOut(
                        new QName(Constants.TRANSPORT_HTTP));
                MessageContext msgContext =
                    new MessageContext(
                        null,
                        configContext.getEngineConfig().getTransportIn(
                            new QName(Constants.TRANSPORT_HTTP)),
                        transportOut,
                        configContext);
                msgContext.setServerSide(true);
                
                
                AxisEngine engine = new AxisEngine(configContext);
                try {
                    XMLStreamReader xmlreader = XMLInputFactory.newInstance().createXMLStreamReader(in);
                    StAXBuilder builder = new StAXSOAPModelBuilder(xmlreader);
                    msgContext.setEnvelope((SOAPEnvelope) builder.getDocumentElement());
                } catch (Exception e) {
                    throw new AxisFault(e.getMessage(), e);
                }
                engine.receive(msgContext);
            } catch (Throwable e) {
                log.error(e);
            } 
        }
    }

    public synchronized void start() {
        started = true;
        Thread thread = new Thread(this);
        thread.start();
    }

}
