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
package org.apache.axis.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.Constants;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.TransportOutDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.util.threadpool.AxisWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HTTPWorker implements AxisWorker {
    protected Log log = LogFactory.getLog(getClass().getName());
    private ConfigurationContext configurationContext;
    private Socket socket;

    public HTTPWorker(ConfigurationContext configurationContext,Socket socket){
        this.configurationContext = configurationContext;
        this.socket = socket;
    } 

    public void doWork() {
        try {
            if (socket != null) {
                if (configurationContext == null) {
                    throw new AxisFault("Engine Must be null");
                }

                InputStream inStream = socket.getInputStream();

                TransportOutDescription transportOut =
                    configurationContext.getAxisConfiguration().getTransportOut(
                        new QName(Constants.TRANSPORT_HTTP));
                MessageContext msgContext =
                    new MessageContext(
                        configurationContext,
                        configurationContext.getAxisConfiguration().getTransportIn(
                            new QName(Constants.TRANSPORT_HTTP)),
                        transportOut);
                msgContext.setServerSide(true);

                // We do not have any Addressing Headers to put
                // let us put the information about incoming transport
                HTTPTransportReceiver reciver = new HTTPTransportReceiver();
                Map map = reciver.parseTheHeaders(inStream, true);

                SimpleHTTPOutputStream out;
                String transferEncoding = (String) map.get(HTTPConstants.HEADER_TRANSFER_ENCODING);
                if (transferEncoding != null
                    && HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED.equals(transferEncoding)) {
                    inStream = new ChunkedInputStream(inStream);
                    out = new SimpleHTTPOutputStream(socket.getOutputStream(), true);
                } else {
                    out = new SimpleHTTPOutputStream(socket.getOutputStream(), false);
                }

                //OutputStream out = socket.getOutputStream();
                msgContext.setProperty(MessageContext.TRANSPORT_OUT, out);

                if (HTTPConstants.HEADER_GET.equals(map.get(HTTPConstants.HTTP_REQ_TYPE))) {
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
            log.error(e);
            e.printStackTrace();
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
