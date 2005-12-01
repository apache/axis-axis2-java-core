/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.MessageSender;
import org.apache.axis2.client.Options;
import org.apache.axis2.om.OMElement;

/**
 * Sample for fire-and-forget service invocation
 * Message Exchage Pattern IN-Only
 */
public class PingClient {
    private static EndpointReference targetEPR = new EndpointReference("http://localhost:8080/axis2/services/MyService");

    public static void main(String[] args) {
        try {
            OMElement payload = ClientUtil.getPingOMElement();

            MessageSender msgSender = new MessageSender();

            Options options = new Options();
            msgSender.setClientOptions(options);
            options.setTo(targetEPR);

            msgSender.send("ping", payload);

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
    }

}
