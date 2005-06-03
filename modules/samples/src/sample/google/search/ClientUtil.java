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

package sample.google.search;

import org.apache.axis.om.*;
import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.context.ConfigurationContextFactory;
import org.apache.axis.context.ConfigurationContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.deployment.DeploymentException;

/**
 * Builds the MessageContext as called by AsynchronousClient
 * First build the request soap envilope
 * then build a messageContext and soap envelope is attached to it
 *
 * @author Gayan Asanka  (gayan@opensource.lk)
 */
public class ClientUtil {

    /** Soap request is included to this and pass it to sendMsg() in AsynchronousClient */
    static MessageContext msgContext;

    /**
     * method getMessageContext
     * @return msgContext
     */
    public static MessageContext getMessageContext() throws DeploymentException {
        OMNamespace namespace,defNs;
        OMElement operation,part1,part2,part3,part4,part5,part6,part7,part8,part9,part10;

        String str_ST_index = Integer.toString(AsynchronousClient.StartIndex);

        defNs = OMAbstractFactory.getSOAP11Factory().createOMNamespace("", "");
        SOAPFactory omFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = omFactory.getDefaultEnvelope();
        namespace = envelope.declareNamespace(
                "http://schemas.xmlsoap.org/soap/envelope/", "SOAP-ENV");
        namespace = envelope.declareNamespace(
                "http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC");
        namespace = envelope.declareNamespace(
                "http://www.w3.org/1999/XMLSchema-instance/", "xsi");
        namespace = envelope.declareNamespace("http://www.w3.org/1999/XMLSchema",
                "xsd");

        operation = omFactory.createOMElement("doGoogleSearch", "urn:GoogleSearch", "ns1");
        envelope.getBody().addChild(operation);
        operation.addAttribute("SOAP-ENV:encordingStyle",
                "http://schemas.xmlsoap.org/soap/encoding/", null);

        part1 = omFactory.createOMElement("key", defNs);
        part1.addAttribute("xsi:type", "xsd:string", null);
        part1.addChild(omFactory.createText(AsynchronousClient.key));
        //a sample valid key "F0wt5EFQFHKxTs+rl3P+27o6D112BTWd"));

        part2 = omFactory.createOMElement("q", defNs);
        part2.addAttribute("xsi:type", "xsd:string", null);
        part2.addChild(omFactory.createText(AsynchronousClient.search));

        part3 = omFactory.createOMElement("start", defNs);
        part3.addAttribute("xsi:type", "xsd:int", null);
        part3.addChild(omFactory.createText(str_ST_index));

        part4 = omFactory.createOMElement("maxResults", defNs);
        part4.addAttribute("xsi:type", "xsd:int", null);
        part4.addChild(omFactory.createText(AsynchronousClient.maxResults));

        part5 = omFactory.createOMElement("filter", defNs);
        part5.addAttribute("xsi:type", "xsd:boolean", null);
        part5.addChild(omFactory.createText("true"));

        part6 = omFactory.createOMElement("restrict", defNs);
        part6.addAttribute("xsi:type", "xsd:string", null);

        part7 = omFactory.createOMElement("safeSearch", defNs);
        part7.addAttribute("xsi:type", "xsd:boolean", null);
        part7.addChild(omFactory.createText("false"));

        part8 = omFactory.createOMElement("lr", defNs);
        part8.addAttribute("xsi:type", "xsd:string", null);

        part9 = omFactory.createOMElement("ie", defNs);
        part9.addAttribute("xsi:type", "xsd:string", null);
        part9.addChild(omFactory.createText("latin1"));

        part10 = omFactory.createOMElement("oe", defNs);
        part10.addAttribute("xsi:type", "xsd:string", null);
        part10.addChild(omFactory.createText("latin1"));

        operation.addChild(part10);
        operation.addChild(part9);
        operation.addChild(part8);
        operation.addChild(part7);
        operation.addChild(part6);
        operation.addChild(part5);
        operation.addChild(part4);
        operation.addChild(part3);
        operation.addChild(part2);
        operation.addChild(part1);

        ConfigurationContextFactory fac = new ConfigurationContextFactory();
        ConfigurationContext configContext = fac.buildClientEngineContext("doGoogleSearch");
        try {
            msgContext = new MessageContext(configContext);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        msgContext.setEnvelope(envelope);
        return msgContext;
    }
}



