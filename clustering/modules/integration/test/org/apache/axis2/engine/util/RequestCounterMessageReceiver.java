package org.apache.axis2.engine.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;

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
 *
 */

public class RequestCounterMessageReceiver extends AbstractInOutSyncMessageReceiver {

    public RequestCounterMessageReceiver() {
    }

    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {
        RequestCounter requestCounter = new RequestCounter();
        requestCounter.getRequestCount(inMessage, outMessage);

        SOAPFactory factory;

        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(inMessage.getEnvelope().getNamespace().getNamespaceURI())) {
            factory = OMAbstractFactory.getSOAP12Factory();
        } else {
            factory = OMAbstractFactory.getSOAP11Factory();
        }

        SOAPEnvelope defaultEnvelope = factory.getDefaultEnvelope();
        outMessage.setEnvelope(defaultEnvelope);

        OMNamespace axis2Namespace = factory.createOMNamespace("http://ws.apache.org/axis2/namespaces/", "axis2");
        OMElement firstElement = factory.createOMElement("RequestCount", axis2Namespace, defaultEnvelope.getBody());
        firstElement.setText(""+((Integer)inMessage.getServiceGroupContext().getProperty(RequestCounter.REQUEST_COUNT)).intValue());

    }
}
