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
 *
 *  Runtime state of the engine
 */
package org.apache.axis2.transport.tcp;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.AxisFault;
import org.apache.axis2.om.impl.llom.builder.StAXBuilder;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis2.util.threadpool.AxisWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;

public class TCPWorker implements AxisWorker {
    protected Log log = LogFactory.getLog(getClass().getName());
    private ConfigurationContext configurationContext;
    private Socket socket;

    public TCPWorker(ConfigurationContext configurationContext, Socket socket) {
        this.configurationContext = configurationContext;
        this.socket = socket;
    }

    public void doWork() {
        MessageContext msgContext = null;
        try {
            Reader in = new InputStreamReader(socket.getInputStream());
            TransportOutDescription transportOut =
                    configurationContext.getAxisConfiguration()
                    .getTransportOut(new QName(Constants.TRANSPORT_TCP));
            msgContext =
                    new MessageContext(configurationContext,
                            configurationContext.getAxisConfiguration()
                    .getTransportIn(new QName(Constants.TRANSPORT_TCP)),
                            transportOut);
            msgContext.setServerSide(true);
            OutputStream out = socket.getOutputStream();
            msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);

            AxisEngine engine = new AxisEngine(configurationContext);
            try {
                XMLStreamReader xmlreader = XMLInputFactory.newInstance()
                        .createXMLStreamReader(in);
                StAXBuilder builder = new StAXSOAPModelBuilder(xmlreader);
                msgContext.setEnvelope(
                        (SOAPEnvelope) builder.getDocumentElement());
            } catch (Exception e) {
                throw new AxisFault(e.getMessage(), e);
            }
            engine.receive(msgContext);

        } catch (Throwable e) {
            try {
                AxisEngine engine = new AxisEngine(configurationContext);
                if (msgContext != null) {
                    msgContext.setProperty(MessageContext.TRANSPORT_OUT,
                            socket.getOutputStream());
                    engine.handleFault(msgContext, e);
                }
            } catch (Exception e1) {
                log.error(e);
                e.printStackTrace();
            }

        } finally {
            if (socket != null) {
                try {
                    this.socket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }

    }

}
