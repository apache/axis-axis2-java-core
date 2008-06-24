/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.transport.tcp;

import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;

/**
 * This Class is the work hoarse of the TCP request, this process the incomming SOAP Message.
 */
public class TCPWorker implements Runnable {
    private static final Log log = LogFactory.getLog(TCPWorker.class);
    private ConfigurationContext configurationContext;
    private Socket socket;

    public TCPWorker(ConfigurationContext configurationContext, Socket socket) {
        this.configurationContext = configurationContext;
        this.socket = socket;
    }

    public void run() {
        MessageContext msgContext = null;

        try {
            AxisConfiguration axisConf = configurationContext.getAxisConfiguration();
            TransportOutDescription transportOut =
                    axisConf.getTransportOut(Constants.TRANSPORT_TCP);
            TransportInDescription transportIn =
                    axisConf.getTransportIn(Constants.TRANSPORT_TCP);

            if ((transportOut != null) && (transportIn != null)) {

                // create the Message Context and fill in the values
                msgContext = configurationContext.createMessageContext();
                msgContext.setIncomingTransportName(Constants.TRANSPORT_TCP);
                msgContext.setTransportIn(transportIn);
                msgContext.setTransportOut(transportOut);
                msgContext.setServerSide(true);

                OutputStream out = socket.getOutputStream();

                msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);

                // create the SOAP Envelope
                Reader in = new InputStreamReader(socket.getInputStream());
                OMXMLParserWrapper builder = BuilderUtil.getBuilder(in);
                SOAPEnvelope envelope = (SOAPEnvelope) builder.getDocumentElement();

                msgContext.setEnvelope(envelope);
                AxisEngine.receive(msgContext);
            } else {
                throw new AxisFault(Messages.getMessage("unknownTransport",
                                                        Constants.TRANSPORT_TCP));
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            try {

                if (msgContext != null) {
                    msgContext.setProperty(MessageContext.TRANSPORT_OUT, socket.getOutputStream());

                    MessageContext faultContext =
                            MessageContextBuilder.createFaultMessageContext(msgContext, e);

                    AxisEngine.sendFault(faultContext);
                }
            } catch (Exception e1) {
                log.error(e1.getMessage(), e1);
            }
        } finally {
            if (socket != null) {
                try {
                    this.socket.close();
                } catch (IOException e1) {
                    // Do nothing
                }
            }
        }
    }
}
