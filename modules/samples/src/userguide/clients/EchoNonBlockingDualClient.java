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


package userguide.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.AsyncResult;
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Callback;
import org.apache.axis2.om.OMElement;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringWriter;

/**
 * Sample for asynchronous dual channel non-blocking service invocation.
 * Message Exchage Pattern IN-OUT
 * Ulitmate asynchronous service invocation sample.
 */
public class EchoNonBlockingDualClient {
    private static EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:8080/axis2/services/MyService");

    public static void main(String[] args) {
        try {
            OMElement payload = ClientUtil.getEchoOMElement();

            Call call = new Call();
            call.setTo(targetEPR);

            //The boolean flag informs the axis2 engine to use two separate transport connection
            //to retrieve the response.
            call.engageModule(new QName(Constants.MODULE_ADDRESSING));
            call.setTransportInfo(Constants.TRANSPORT_HTTP,
                    Constants.TRANSPORT_HTTP,
                    true);

            //Callback to handle the response
            Callback callback = new Callback() {
                public void onComplete(AsyncResult result) {
                    try {
                        StringWriter writer = new StringWriter();
                        result.getResponseEnvelope().serialize(XMLOutputFactory.newInstance()
                                .createXMLStreamWriter(writer));
                        writer.flush();
                        System.out.println(writer.toString());


                    } catch (XMLStreamException e) {
                        reportError(e);
                    }
                }

                public void reportError(Exception e) {
                    e.printStackTrace();
                }
            };

            //Non-Blocking Invocation
            call.invokeNonBlocking("echo", payload, callback);

            //Wait till the callback receives the response.
            while (!callback.isComplete()) {
                Thread.sleep(1000);
            }
            //Need to close the Client Side Listener.
            call.close();

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
