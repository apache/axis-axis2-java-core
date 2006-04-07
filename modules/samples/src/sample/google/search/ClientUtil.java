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

package sample.google.search;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

/**
 * Builds the MessageContext as called by AsynchronousClient
 * First build the request soap envilope
 * then build a messageContext and soap envelope is attached to it
 */
public class ClientUtil {

    /** Soap request is included to this and pass it to sendMsg() in AsynchronousClient */
    //static MessageContext msgContext;

    /**
     * method getMessageContext
     *
     * @return msgContext
     */
    public static MessageContext getMessageContext(
            AsynchronousClient asyncClient)
            throws AxisFault {
        OMNamespace defNs;
        OMElement operation;
        MessageContext msgContext = null;

        String str_ST_index = Integer.toString(asyncClient.getStartIndex());

        defNs = OMAbstractFactory.getSOAP11Factory().createOMNamespace("", "");
        SOAPFactory omFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = omFactory.getDefaultEnvelope();
        envelope.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/",
                "soapenv");
        envelope.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/",
                "SOAP-ENC");
        envelope.declareNamespace("http://www.w3.org/2001/XMLSchema-instance",
                "xsi");
        envelope.declareNamespace("http://www.w3.org/2001/XMLSchema",
                "xsd");

        operation =
                omFactory.createOMElement("doGoogleSearch",
                        "urn:GoogleSearch",
                        "ns1");
        envelope.getBody().addChild(operation);
        operation.addAttribute("soapenv:encordingStyle",
                "http://schemas.xmlsoap.org/soap/encoding/", null);

        operation.addChild(
                getOMElement(omFactory,
                        defNs,
                        "key",
                        "xsd:string",
                        asyncClient.getKey()));
        operation.addChild(
                getOMElement(omFactory,
                        defNs,
                        "q",
                        "xsd:string",
                        asyncClient.getSearch()));
        operation.addChild(
                getOMElement(omFactory,
                        defNs,
                        "start",
                        "xsd:int",
                        str_ST_index));
        operation.addChild(
                getOMElement(omFactory,
                        defNs,
                        "maxResults",
                        "xsd:int",
                        asyncClient.getMaxResults()));
        operation.addChild(
                getOMElement(omFactory,
                        defNs,
                        "filter",
                        "xsd:boolean",
                        "true"));
        operation.addChild(
                getOMElement(omFactory, defNs, "restrict", "xsd:string", ""));
        operation.addChild(
                getOMElement(omFactory,
                        defNs,
                        "safeSearch",
                        "xsd:boolean",
                        "false"));
        operation.addChild(
                getOMElement(omFactory, defNs, "lr", "xsd:string", ""));
        operation.addChild(
                getOMElement(omFactory, defNs, "ie", "xsd:string", "latin1"));
        operation.addChild(
                getOMElement(omFactory, defNs, "oe", "xsd:string", "latin1"));

        msgContext = new MessageContext();
        msgContext.setEnvelope(envelope);
        return msgContext;
    }

    private static OMElement getOMElement(OMFactory factory, OMNamespace ns, String elementName,
                                          String type, String text) {
        OMElement part = factory.createOMElement(elementName, ns);
        part.addAttribute("xsi:type", type, null);
        part.addChild(factory.createOMText(text));
        return part;
    }
}



