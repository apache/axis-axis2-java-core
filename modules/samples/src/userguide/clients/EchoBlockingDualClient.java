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
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.om.OMElement;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import java.io.StringWriter;

/**
 * Sample for synchronous dual channel blocking service invocation.
 * Message Exchage Pattern IN-OUT
 */
public class EchoBlockingDualClient {
    private static EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:8080/axis2/services/MyService/echo");

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

            //Blocking Invocation
            OMElement result = call.invokeBlocking("echo",
                    payload);

            StringWriter writer = new StringWriter();
            result.serializeWithCache(XMLOutputFactory.newInstance()
                    .createXMLStreamWriter(writer));
            writer.flush();
            System.out.println(writer.toString());


            //Need to close the Client Side Listener.
            call.close();

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
