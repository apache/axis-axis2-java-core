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

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.MessageSender;
import org.apache.axis2.engine.AxisFault;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;

/**
 * This is a Client progam that accesses 'MyService' web service in Axis2 samples
 */
public class MailClient {

    private static String toEpr = "http://localhost:8080/axis2/services/MyService";

    public static void main(String[] args) throws AxisFault {
        MessageSender msgSender = new MessageSender();
        msgSender.setTo(new EndpointReference(AddressingConstants.WSA_TO, toEpr));
        msgSender.setSenderTransport(Constants.TRANSPORT_MAIL);
        msgSender.send("echo", getPayload());
    }

    private static OMElement getPayload() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://example1.org/example1", "example1");
        OMElement method = fac.createOMElement("echo", omNs);
        OMElement value = fac.createOMElement("Text", omNs);
        value.addChild(fac.createText(value, "Axis2 Echo String "));
        method.addChild(value);

        return method;
    }
}
