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
package org.apache.axis2.transport.http;

import org.apache.axis2.Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.threadpool.AxisWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;

public class HTTPWorker implements AxisWorker {
    protected Log log = LogFactory.getLog(getClass().getName());
    private ConfigurationContext configurationContext;
    private Socket socket;

    public HTTPWorker(ConfigurationContext configurationContext, Socket socket) {
        this.configurationContext = configurationContext;
        this.socket = socket;
    }

    public void doWork() {
        MessageContext msgContext = null;
        SimpleHTTPOutputStream out = null;
        try {
            if (socket != null) {
                if (configurationContext == null) {
                    throw new AxisFault(Messages.getMessage("cannotBeNullConfigurationContext"));
                }

                InputStream inStream = socket.getInputStream();

                TransportOutDescription transportOut =
                    configurationContext.getAxisConfiguration().getTransportOut(
                        new QName(Constants.TRANSPORT_HTTP));
                msgContext =
                    new MessageContext(
                        configurationContext,
                        configurationContext.getAxisConfiguration().getTransportIn(
                            new QName(Constants.TRANSPORT_HTTP)),
                        transportOut);
                msgContext.setServerSide(true);
                

                //parse the Transport Headers
                HTTPTransportReceiver receiver = new HTTPTransportReceiver();
                Map map = receiver.parseTheHeaders(inStream, true);

                //build a way to write the respone if the Axis choose to do so

                String transferEncoding = (String) map.get(HTTPConstants.HEADER_TRANSFER_ENCODING);
                if (transferEncoding != null
                    && HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED.equals(transferEncoding)) {
                    inStream = new ChunkedInputStream(inStream);
                    out = new SimpleHTTPOutputStream(socket.getOutputStream(), true);
                } else {
                    out = new SimpleHTTPOutputStream(socket.getOutputStream(), false);
                }
                msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);
                //set the transport Headers
                msgContext.setProperty(MessageContext.TRANSPORT_HEADERS,map);
                
                //This is way to provide Accsess to the transport information to the transport Sender
                msgContext.setProperty(
                    HTTPConstants.HTTPOutTransportInfo,
                    new SimpleHTTPOutTransportInfo(out));

                if (HTTPConstants.HEADER_GET.equals(map.get(HTTPConstants.HTTP_REQ_TYPE))) {
                    //It is GET handle the Get request 
                    boolean processed =
                        HTTPTransportUtils.processHTTPGetRequest(
                            msgContext,
                            inStream,
                            out,
                            (String) map.get(HTTPConstants.HEADER_CONTENT_TYPE),
                            (String) map.get(HTTPConstants.HEADER_SOAP_ACTION),
                            (String) map.get(HTTPConstants.REQUEST_URI),
                            configurationContext,
                            HTTPTransportReceiver.getGetRequestParameters(
                                (String) map.get(HTTPConstants.REQUEST_URI)));

                    if (!processed) {
                        out.write(
                            HTTPTransportReceiver.getServicesHTML(configurationContext).getBytes());
                        out.flush();
                    }
                } else {
                    //It is POST, handle it
                    HTTPTransportUtils.processHTTPPostRequest(
                        msgContext,
                        inStream,
                        out,
                        (String) map.get(HTTPConstants.HEADER_CONTENT_TYPE),
                        (String) map.get(HTTPConstants.HEADER_SOAP_ACTION),
                        (String) map.get(HTTPConstants.REQUEST_URI),
                        configurationContext);
                }

                out.finalize();
            }
        } catch (Throwable e) {
            try {
                AxisEngine engine = new AxisEngine(configurationContext);
                if (msgContext != null) {
                    msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);
                    MessageContext faultContext = engine.createFaultMessageContext(msgContext, e);
                    engine.sendFault(faultContext);
                } else {
                    log.error(e);
//                    e.printStackTrace();
                }
            } catch (Exception e1) {
                log.error(e1);
//                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    this.socket.close();
                } catch (IOException e1) {
                    log.error(e1);
                }
            }
        }

    }

}
